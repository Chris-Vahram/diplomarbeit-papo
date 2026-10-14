---
name: graphify
description: Navigate PAPO's local code graph for cross-file dependencies and call paths, or rebuild/export it when explicitly requested. Use source files for exact facts and business rules.
---

# Graphify in PAPO

Run commands from the repository root. `graphify-out/` is a local, ignored cache. `.graphifyignore` limits semantic extraction to current core documents.

- Known symbol: `graphify explain "Symbol"`. Two symbols: `graphify path "A" "B"`.
- Open question about code relationships: `graphify query "specific question" --budget 800`. If output is broad or truncated, narrow once, then inspect the cited source files. Use `rg` directly for exact text or specification wording.
- After relevant code edits: `graphify update .` (local AST, no model call). For changed indexed documents or an explicit full rebuild: `graphify extract . --backend ollama --model qwen2.5:14b --max-concurrency 1 --token-budget 4000 --max-workers 4 --api-timeout 300`, then `graphify cluster-only .`. Do not rebuild for routine questions.
- For visual output: `graphify export html` or `graphify export obsidian --dir graphify-out/obsidian`. Export only into this generated vault; do not target an existing personal vault.

Graph edges can be inferred or stale. Verify consequential claims in code and the source order in `AGENTS.md`; never treat the graph or saved Q&A as a specification. Avoid loading `GRAPH_REPORT.md` into context unless a broad architecture summary is requested. Use `graphify --help` for other CLI commands; read `references/query.md` only for advanced traversal.
