#!/usr/bin/env python3
"""Private, dependency-free evidence collector for PAPO AI sessions."""

import argparse
from datetime import datetime, timezone
import fcntl
import hashlib
import json
import os
from pathlib import Path
import plistlib
import re
import shutil
import subprocess
import sys
import tempfile


# Person column of new register rows; Tobias sets PAPO_KI_PERSON=Tobias on his machine.
PERSON = os.environ.get("PAPO_KI_PERSON", "Chris")
REPO = Path(__file__).resolve().parents[1]
REGISTER = REPO / "Documentation/KI-Nachweise/prompt-register.md"
DEFAULT_ARCHIVE = Path.home() / "Documents/PAPO-KI-Roharchiv"
DEFAULT_CODEX = Path.home() / ".codex/sessions"
DEFAULT_CLAUDE = Path.home() / ".claude"
AGENT_LABEL = "at.papo.ai-evidence-log"
AGENT_PATH = Path.home() / "Library/LaunchAgents" / f"{AGENT_LABEL}.plist"
TABLE_HEADER = "| ID | Datum | Person | Werkzeug |"
SECRET = re.compile(r"(?i)(api[_-]?key|token|password|secret|bearer\s+[a-z0-9._-]+)")


def atomic_write(path, text, mode=0o600):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, name = tempfile.mkstemp(dir=path.parent, prefix=f".{path.name}.")
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as out:
            out.write(text)
        os.chmod(name, mode)
        os.replace(name, path)
    finally:
        if os.path.exists(name):
            os.unlink(name)


def load_json(path, default):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (FileNotFoundError, json.JSONDecodeError):
        return default


def json_lines(path):
    unknown = []
    with path.open(encoding="utf-8") as source:
        for number, line in enumerate(source, 1):
            try:
                yield json.loads(line)
            except json.JSONDecodeError:
                unknown.append(number)
    return unknown


def text_content(value):
    if isinstance(value, str):
        return value
    if isinstance(value, list):
        return "\n".join(filter(None, (text_content(item) for item in value)))
    if isinstance(value, dict):
        if value.get("type") in {"tool_use", "tool_result", "thinking", "reasoning"}:
            return ""
        return text_content(value.get("text", value.get("content", "")))
    return ""


def codex_session(path, project):
    events = list(json_lines(path))
    meta = next((event.get("payload", {}) for event in events if event.get("type") == "session_meta"), {})
    if Path(meta.get("cwd", "/")).resolve() != project.resolve():
        return None
    messages, unknown = [], set()
    for event in events:
        payload = event.get("payload", {})
        if event.get("type") != "response_item":
            continue
        if payload.get("type") != "message":
            if payload.get("type") not in {"reasoning", "function_call", "function_call_output"}:
                unknown.add(str(payload.get("type")))
            continue
        role = payload.get("role")
        if role not in {"user", "assistant"}:
            continue
        content = text_content(payload.get("content", "")).strip()
        if content:
            messages.append({"role": role, "text": content})
    return session_record("Codex", meta.get("session_id") or meta.get("id") or path.stem, meta.get("timestamp"), messages, unknown)


def claude_session(path, project):
    events = list(json_lines(path))
    relevant = [event for event in events if event.get("cwd") and Path(event["cwd"]).resolve() == project.resolve()]
    if not relevant:
        return None
    messages, unknown = [], set()
    for event in relevant:
        kind = event.get("type")
        message = event.get("message", {})
        role = message.get("role") if isinstance(message, dict) else None
        if event.get("isSidechain") or role not in {"user", "assistant"}:
            if kind not in {"user", "assistant", "progress", "system", "file-history-snapshot"}:
                unknown.add(str(kind))
            continue
        content = text_content(message.get("content", "")).strip()
        if content:
            messages.append({"role": role, "text": content})
    return session_record("Claude Code", path.stem, relevant[0].get("timestamp"), messages, unknown)


def session_record(tool, session_id, date, messages, unknown):
    return {"tool": tool, "id": session_id, "date": date or "", "messages": messages, "unknown": sorted(unknown)}


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def archive_session(session, source, archive_root, previous=None):
    target = archive_root / session["tool"].lower().replace(" ", "-") / session["id"]
    target.mkdir(mode=0o700, parents=True, exist_ok=True)
    raw = target / "source.jsonl"
    source_hash = sha256(source)
    if not raw.exists() or sha256(raw) != source_hash:
        temp = raw.with_suffix(".tmp")
        shutil.copyfile(source, temp)
        os.chmod(temp, 0o600)
        os.replace(temp, raw)
    dialog = {"tool": session["tool"], "id": session["id"], "messages": session["messages"], "unknown_event_types": session["unknown"]}
    dialog_text = json.dumps(dialog, ensure_ascii=False, indent=2) + "\n"
    dialog_path = target / "dialog.json"
    if not dialog_path.exists() or dialog_path.read_text(encoding="utf-8") != dialog_text:
        atomic_write(dialog_path, dialog_text)
    return {
        "tool": session["tool"],
        "id": session["id"],
        "source_sha256": source_hash,
        "dialog_sha256": sha256(dialog_path),
        "archived_at": (previous or {}).get("archived_at") or datetime.now(timezone.utc).isoformat(),
    }


# Claude Code names its per-project folder after the absolute path with every
# non-alphanumeric character (not only "/") replaced by "-", e.g. "4.Klasse" -> "4-Klasse".
def claude_project_dir(project):
    return re.sub(r"[^A-Za-z0-9]", "-", str(project))


def collect(project, codex_root, claude_root):
    if codex_root.exists():
        for path in codex_root.rglob("*.jsonl"):
            session = codex_session(path, project)
            if session:
                yield session, path
    claude_project = claude_root / "projects" / claude_project_dir(project)
    if claude_project.exists():
        for path in claude_project.glob("*.jsonl"):
            session = claude_session(path, project)
            if session:
                yield session, path


def row_person(line):
    cells = line.split("|")
    return cells[3].strip() if len(cells) > 3 else ""


def register_rows(sessions, register=REGISTER, archived_ids=()):
    text = register.read_text(encoding="utf-8")
    if TABLE_HEADER not in text:
        raise ValueError("Prompt-Register hat kein erwartetes Tabellenformat")
    lines = []
    for line in text.splitlines():
        generated = line.startswith("| AUSSTEHEND-") and "Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen" in line
        # Only this machine's own placeholders may be pruned: another person's sessions are
        # never in the local archive, so their rows must survive a sync here.
        own = generated and row_person(line) == PERSON
        # Keep placeholders of archived sessions even after Codex/Claude pruned the local source.
        if own and not any(f"`{session_id}`" in line for session_id in archived_ids):
            continue
        lines.append(line)
    insertion = next((index for index, line in enumerate(lines) if line.startswith("## Noch rückwirkend")), len(lines))
    rows = []
    for session in sessions:
        marker = f"`{session['id']}`"
        if any(marker in line for line in lines):
            continue
        date = (session["date"] or "unbekannt")[:10]
        rows.append(f"| AUSSTEHEND-{session['id'][:8]} | {date} | {PERSON} | {session['tool']} | Fachliche Einordnung offen | lokale Sitzung `{session['id']}` | `AUSSTEHEND` | Automatisch privat gesichert; Zusammenfassung und Prüfung nachtragen |")
    needs_gap = rows and insertion < len(lines) and lines[insertion] != ""
    lines[insertion:insertion] = rows + ([""] if needs_gap else [])
    text = "\n".join(lines)
    return text if text.endswith("\n") else text + "\n"


def pending_file(archive_root, sessions, error):
    data = {"error": str(error), "sessions": [{"tool": item["tool"], "id": item["id"]} for item in sessions]}
    atomic_write(archive_root / "pending-register.json", json.dumps(data, ensure_ascii=False, indent=2) + "\n")


def sync(args):
    archive_root = Path(args.archive).expanduser().resolve()
    archive_root.mkdir(mode=0o700, parents=True, exist_ok=True)
    with (archive_root / ".lock").open("w") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        found_by_key = {}
        for session, source in collect(REPO, Path(args.codex_root).expanduser(), Path(args.claude_root).expanduser()):
            key = (session["tool"], session["id"])
            if key not in found_by_key or source.stat().st_mtime > found_by_key[key][1].stat().st_mtime:
                found_by_key[key] = (session, source)
        found = list(found_by_key.values())
        old = {(item["tool"], item["id"]): item for item in load_json(archive_root / "manifest.json", [])}
        manifest = [archive_session(session, source, archive_root, old.get((session["tool"], session["id"]))) for session, source in found]
        # Codex/Claude prune old local sessions; their private archive copy stays, so keep the entry.
        manifest += [item for key, item in old.items() if key not in found_by_key]
        manifest_text = json.dumps(sorted(manifest, key=lambda item: (item["tool"], item["id"])), ensure_ascii=False, indent=2) + "\n"
        atomic_write(archive_root / "manifest.json", manifest_text)
        try:
            archived_ids = {item["id"] for item in manifest}
            atomic_write(Path(args.register), register_rows([session for session, _ in found], Path(args.register), archived_ids), 0o644)
            pending = archive_root / "pending-register.json"
            if pending.exists():
                pending.unlink()
        except (OSError, ValueError) as error:
            pending_file(archive_root, [session for session, _ in found], error)
        state = {"last_sync": datetime.now(timezone.utc).isoformat(), "sessions": len(found), "pending_register": (archive_root / "pending-register.json").exists()}
        atomic_write(archive_root / "state.json", json.dumps(state, ensure_ascii=False, indent=2) + "\n")
    print(f"{len(found)} PAPO-Sitzungen privat gesichert.")


def enrich(args):
    values = [args.purpose, args.result, *args.check]
    if any("|" in value or "\n" in value or SECRET.search(value) for value in values):
        raise SystemExit("Metadaten enthalten unerlaubte Tabellen-, Zeilenumbruch- oder Secret-Muster.")
    register = Path(args.register)
    lines = register.read_text(encoding="utf-8").splitlines()
    session_id = args.session_id or latest_session_id()
    marker = f"`{session_id}`"
    match = next((index for index, line in enumerate(lines) if marker in line), None)
    if match is None:
        raise SystemExit("Sitzung ist noch nicht im Register. Zuerst sync ausführen.")
    columns = [column.strip() for column in lines[match].strip("|").split("|")]
    if len(columns) != 8:
        raise SystemExit("Registerzeile hat nicht das erwartete Tabellenformat.")
    columns[4] = args.purpose
    columns[6] = "`EXAKT`"
    checks = "; ".join(args.check) if args.check else "keine technische Prüfung angegeben"
    columns[7] = f"{args.result}; Prüfungen: {checks}"
    lines[match] = "| " + " | ".join(columns) + " |"
    atomic_write(register, "\n".join(lines) + "\n", 0o644)


def latest_session_id():
    found = list(collect(REPO, DEFAULT_CODEX, DEFAULT_CLAUDE))
    if not found:
        raise SystemExit("Keine PAPO-Sitzung gefunden.")
    return max(found, key=lambda item: item[1].stat().st_mtime)[0]["id"]


def status(args):
    archive_root = Path(args.archive).expanduser()
    manifest = load_json(archive_root / "manifest.json", [])
    state = load_json(archive_root / "state.json", {})
    register_text = Path(args.register).read_text(encoding="utf-8")
    print(f"Archivierte Sitzungen: {len(manifest)}")
    print(f"Register AUSSTEHEND: {register_text.count('`AUSSTEHEND`')}")
    print(f"Registerkonflikt: {'ja' if state.get('pending_register') else 'nein'}")
    print("ChatGPT-Webexport: nicht bereitgestellt")
    print("Claude-Webexport: nicht bereitgestellt")


def plist_data(args):
    command = [sys.executable, str(Path(__file__).resolve()), "sync", "--archive", str(Path(args.archive).expanduser()), "--register", str(Path(args.register).resolve())]
    return {"Label": AGENT_LABEL, "ProgramArguments": command, "RunAtLoad": True, "StartInterval": 900, "WorkingDirectory": str(REPO), "StandardOutPath": str(Path(args.archive).expanduser() / "launchagent.log"), "StandardErrorPath": str(Path(args.archive).expanduser() / "launchagent-error.log")}


def install(args):
    Path(args.archive).expanduser().mkdir(mode=0o700, parents=True, exist_ok=True)
    AGENT_PATH.parent.mkdir(parents=True, exist_ok=True)
    atomic_write(AGENT_PATH, plistlib.dumps(plist_data(args)).decode(), 0o600)
    subprocess.run(["launchctl", "bootout", f"gui/{os.getuid()}", str(AGENT_PATH)], check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    subprocess.run(["launchctl", "bootstrap", f"gui/{os.getuid()}", str(AGENT_PATH)], check=True)
    print(f"LaunchAgent installiert: {AGENT_PATH}")


def agent_status(_args):
    result = subprocess.run(["launchctl", "print", f"gui/{os.getuid()}/{AGENT_LABEL}"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    print("LaunchAgent: aktiv" if result.returncode == 0 else "LaunchAgent: nicht aktiv")
    return result.returncode


def uninstall(_args):
    subprocess.run(["launchctl", "bootout", f"gui/{os.getuid()}", str(AGENT_PATH)], check=False)
    if AGENT_PATH.exists():
        AGENT_PATH.unlink()
    print("LaunchAgent entfernt; privates Archiv bleibt erhalten.")


def parser():
    main_parser = argparse.ArgumentParser()
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument("--archive", default=str(DEFAULT_ARCHIVE))
    common.add_argument("--register", default=str(REGISTER))
    commands = main_parser.add_subparsers(dest="command", required=True)
    sync_parser = commands.add_parser("sync", parents=[common])
    sync_parser.add_argument("--codex-root", default=str(DEFAULT_CODEX))
    sync_parser.add_argument("--claude-root", default=str(DEFAULT_CLAUDE))
    sync_parser.set_defaults(func=sync)
    enrich_parser = commands.add_parser("enrich", parents=[common])
    enrich_parser.add_argument("--session-id")
    enrich_parser.add_argument("--purpose", required=True)
    enrich_parser.add_argument("--result", required=True)
    enrich_parser.add_argument("--check", action="append", default=[])
    enrich_parser.set_defaults(func=enrich)
    commands.add_parser("status", parents=[common]).set_defaults(func=status)
    commands.add_parser("install", parents=[common]).set_defaults(func=install)
    commands.add_parser("agent-status").set_defaults(func=agent_status)
    commands.add_parser("uninstall").set_defaults(func=uninstall)
    return main_parser


def main():
    args = parser().parse_args()
    result = args.func(args)
    raise SystemExit(result or 0)


if __name__ == "__main__":
    main()
