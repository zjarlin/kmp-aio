#!/usr/bin/env python3

from __future__ import annotations

import json
import subprocess
import sys
import time
from pathlib import Path

TARGET_REPO_ROOT = Path("/Users/zjarlin/IdeaProjects/kmp-aio").resolve()


def git_root(cwd: str) -> Path | None:
    completed = subprocess.run(
        ["git", "-C", cwd, "rev-parse", "--show-toplevel"],
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        return None
    return Path(completed.stdout.strip()).resolve()


def session_state_path(repo_root: Path, session_id: str) -> Path:
    state_dir = repo_root / ".codex" / "state"
    state_dir.mkdir(parents=True, exist_ok=True)
    safe_session_id = "".join(
        ch if ch.isalnum() or ch in {"-", "_"} else "_"
        for ch in session_id
    )
    return state_dir / f"{safe_session_id}.json"


def main() -> int:
    payload = json.load(sys.stdin)
    cwd = payload.get("cwd") or "."
    session_id = payload.get("session_id") or "default"
    now_ns = time.time_ns()
    repo_root = git_root(cwd)
    if repo_root != TARGET_REPO_ROOT:
        print(json.dumps({"continue": True}))
        return 0
    state_path = session_state_path(repo_root, session_id)
    state = {
        "session_id": session_id,
        "repo_root": str(repo_root),
        "start_ns": now_ns,
        "last_handled_ns": now_ns,
        "processed_deleted": [],
    }
    state_path.write_text(
        json.dumps(state, ensure_ascii=True, indent=2),
        encoding="utf-8",
    )
    print(json.dumps({"continue": True}))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
