import argparse
import io
import json
import os
import shutil
from contextlib import redirect_stdout
from pathlib import Path
import tempfile
import unittest
import sys
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).parent))
import ai_evidence_log as log


FIXTURES = Path(__file__).parent / "fixtures/ai-evidence"


class EvidenceLogTest(unittest.TestCase):
    def test_codex_project_filter_and_redaction_boundary(self):
        session = log.codex_session(FIXTURES / "codex.jsonl", Path("/repo/papo"))
        self.assertEqual([item["text"] for item in session["messages"]], ["Menschlicher Prompt", "Sichtbare Antwort"])
        self.assertNotIn("Nicht übernehmen", json.dumps(session))
        self.assertIsNone(log.codex_session(FIXTURES / "codex.jsonl", Path("/other")))

    def test_claude_excludes_sidechain(self):
        session = log.claude_session(FIXTURES / "claude.jsonl", Path("/repo/papo"))
        self.assertEqual([item["text"] for item in session["messages"]], ["Menschlicher Prompt", "Sichtbare Antwort"])
        self.assertNotIn("Subagent", json.dumps(session))

    def test_archive_is_idempotent_and_updates_growing_source(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            source = root / "source.jsonl"
            source.write_bytes((FIXTURES / "codex.jsonl").read_bytes())
            session = log.codex_session(source, Path("/repo/papo"))
            first = log.archive_session(session, source, root / "archive")
            manifest = json.dumps(first, sort_keys=True)
            second = log.archive_session(session, source, root / "archive", first)
            self.assertEqual(manifest, json.dumps(second, sort_keys=True))
            source.write_text(source.read_text() + "\n", encoding="utf-8")
            third = log.archive_session(session, source, root / "archive", second)
            self.assertNotEqual(second["source_sha256"], third["source_sha256"])
            self.assertEqual(second["archived_at"], third["archived_at"])

    def test_register_conflict_stays_private_pending(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            register = root / "register.md"
            register.write_text("ungültig\n", encoding="utf-8")
            archive = root / "archive"
            session = log.codex_session(FIXTURES / "codex.jsonl", Path("/repo/papo"))
            with self.assertRaises(ValueError):
                log.register_rows([session], register)
            log.pending_file(archive, [session], ValueError("Konflikt"))
            self.assertEqual(register.read_text(), "ungültig\n")
            pending = (archive / "pending-register.json").read_text()
            self.assertIn("codex-fixture", pending)
            self.assertNotIn("Menschlicher Prompt", pending)

    def test_register_prunes_only_stale_generated_rows(self):
        with tempfile.TemporaryDirectory() as tmp:
            register = Path(tmp) / "register.md"
            register.write_text(
                f"{log.TABLE_HEADER}\n|---|---|---|---|---|---|---|---|\n"
                "| AUSSTEHEND-old | 2026 | Chris | Claude Code | offen | lokale Sitzung `old` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |\n"
                "| MANUELL | 2026 | Chris | Codex | kuratiert | lokale Sitzung `keep` | `EXAKT` | geprüft |\n",
                encoding="utf-8",
            )
            session = log.session_record("Codex", "new", "2026", [], [])
            result = log.register_rows([session], register)
            self.assertNotIn("`old`", result)
            self.assertIn("`keep`", result)
            self.assertIn("`new`", result)

    def test_enrich_updates_only_metadata_and_rejects_secrets(self):
        with tempfile.TemporaryDirectory() as tmp:
            register = Path(tmp) / "register.md"
            register.write_text(f"{log.TABLE_HEADER}\n|---|---|---|---|---|---|---|---|\n| X | 2026 | Chris | Codex | offen | Sitzung `abc` | `AUSSTEHEND` | offen |\n", encoding="utf-8")
            args = argparse.Namespace(register=str(register), session_id="abc", purpose="Parser prüfen", result="Erfolgreich", check=["unittest: OK"])
            log.enrich(args)
            text = register.read_text()
            self.assertIn("Parser prüfen", text)
            self.assertIn("`EXAKT`", text)
            args.result = "token=geheim"
            with self.assertRaises(SystemExit):
                log.enrich(args)

    def test_latest_session_is_selected_by_source_mtime(self):
        with tempfile.TemporaryDirectory() as tmp:
            older = Path(tmp) / "older"
            newer = Path(tmp) / "newer"
            older.touch()
            newer.touch()
            os.utime(older, (1, 1))
            os.utime(newer, (2, 2))
            with patch.object(log, "collect", return_value=[({"id": "old"}, older), ({"id": "new"}, newer)]):
                self.assertEqual(log.latest_session_id(), "new")

    def test_status_reports_missing_exports_without_access(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "register.md").write_text("`AUSSTEHEND`\n", encoding="utf-8")
            log.atomic_write(root / "archive/manifest.json", "[]\n")
            args = argparse.Namespace(archive=str(root / "archive"), register=str(root / "register.md"))
            output = io.StringIO()
            with redirect_stdout(output):
                log.status(args)
            self.assertIn("ChatGPT-Webexport: nicht bereitgestellt", output.getvalue())

    def test_full_sync_with_temporary_archive_is_data_minimal(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            codex = root / "codex"
            claude = root / "claude/projects/-repo-papo"
            codex.mkdir()
            claude.mkdir(parents=True)
            shutil.copyfile(FIXTURES / "codex.jsonl", codex / "codex.jsonl")
            shutil.copyfile(FIXTURES / "claude.jsonl", claude / "claude.jsonl")
            register = root / "register.md"
            register.write_text(f"{log.TABLE_HEADER}\n|---|---|---|---|---|---|---|---|\n", encoding="utf-8")
            args = argparse.Namespace(archive=str(root / "archive"), register=str(register), codex_root=str(codex), claude_root=str(root / "claude"))
            with patch.object(log, "REPO", Path("/repo/papo")), redirect_stdout(io.StringIO()):
                log.sync(args)
            manifest = json.loads((root / "archive/manifest.json").read_text())
            public = register.read_text()
            self.assertEqual(len(manifest), 2)
            self.assertNotIn("Menschlicher Prompt", public)
            self.assertNotIn("Sichtbare Antwort", public)

    def test_sync_deduplicates_stable_tool_session_key(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            codex = root / "codex"
            codex.mkdir()
            shutil.copyfile(FIXTURES / "codex.jsonl", codex / "first.jsonl")
            shutil.copyfile(FIXTURES / "codex.jsonl", codex / "second.jsonl")
            register = root / "register.md"
            register.write_text(f"{log.TABLE_HEADER}\n|---|---|---|---|---|---|---|---|\n", encoding="utf-8")
            args = argparse.Namespace(archive=str(root / "archive"), register=str(register), codex_root=str(codex), claude_root=str(root / "missing"))
            with patch.object(log, "REPO", Path("/repo/papo")), redirect_stdout(io.StringIO()):
                log.sync(args)
            self.assertEqual(len(json.loads((root / "archive/manifest.json").read_text())), 1)
            self.assertEqual(register.read_text().count("`codex-fixture`"), 1)

    def test_lock_prevents_second_writer(self):
        with tempfile.TemporaryDirectory() as tmp:
            lock_path = Path(tmp) / "lock"
            with lock_path.open("w") as first, lock_path.open("w") as second:
                log.fcntl.flock(first, log.fcntl.LOCK_EX | log.fcntl.LOCK_NB)
                with self.assertRaises(BlockingIOError):
                    log.fcntl.flock(second, log.fcntl.LOCK_EX | log.fcntl.LOCK_NB)

    def test_atomic_files_are_private(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "private"
            log.atomic_write(path, "x")
            self.assertEqual(os.stat(path).st_mode & 0o777, 0o600)


if __name__ == "__main__":
    unittest.main()
