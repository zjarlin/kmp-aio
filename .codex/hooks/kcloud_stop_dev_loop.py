#!/usr/bin/env python3

from __future__ import annotations

import json
import os
import signal
import subprocess
import sys
import time
from pathlib import Path

TARGET_REPO_ROOT = Path("/Users/zjarlin/IdeaProjects/kmp-aio").resolve()

RELEVANT_PREFIXES = (
    "apps/kcloud/",
    "lib/compose/",
    "lib/tool-kmp/",
    "checkouts/build-logic/",
)
RELEVANT_EXACT = {
    "build.gradle.kts",
    "gradle.properties",
    "settings.gradle.kts",
    "gradle/libs.versions.toml",
}
COMPILE_TASK = ["./gradlew", ":apps:kcloud:ui:compileKotlinJvm", "--no-daemon"]
RUN_TASK = ["./gradlew", ":apps:kcloud:ui:jvmRun", "--no-daemon"]
RUN_LOG_PATH = Path("/tmp/kmp-aio-kcloud-desktop.log")


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


def load_state(repo_root: Path, session_id: str) -> tuple[Path, dict]:
    state_path = session_state_path(repo_root, session_id)
    if state_path.exists():
        return state_path, json.loads(state_path.read_text(encoding="utf-8"))
    now_ns = time.time_ns()
    state = {
        "session_id": session_id,
        "repo_root": str(repo_root),
        "start_ns": now_ns,
        "last_handled_ns": now_ns,
        "processed_deleted": [],
    }
    return state_path, state


def save_state(state_path: Path, state: dict) -> None:
    state_path.write_text(
        json.dumps(state, ensure_ascii=True, indent=2),
        encoding="utf-8",
    )


def is_relevant(path: str) -> bool:
    if path.startswith(".codex/"):
        return False
    if path in RELEVANT_EXACT:
        return True
    if path.endswith("/build.gradle.kts"):
        return True
    return any(path.startswith(prefix) for prefix in RELEVANT_PREFIXES)


def git_status_paths(repo_root: Path) -> list[tuple[str, bool]]:
    output = subprocess.check_output(
        ["git", "-C", str(repo_root), "status", "--porcelain=v1", "--untracked-files=all"],
        text=True,
    )
    results: list[tuple[str, bool]] = []
    for line in output.splitlines():
        if not line:
            continue
        status = line[:2]
        raw_path = line[3:]
        path = raw_path.split(" -> ", 1)[-1]
        deleted = "D" in status
        results.append((path, deleted))
    return results


def tail_text(path: Path, lines: int = 40) -> str:
    if not path.exists():
        return ""
    content = path.read_text(encoding="utf-8", errors="replace").splitlines()
    return "\n".join(content[-lines:])


def run_command(command: list[str], cwd: Path, log_path: Path) -> tuple[int, str]:
    with log_path.open("w", encoding="utf-8") as stream:
        completed = subprocess.run(
            command,
            cwd=cwd,
            stdout=stream,
            stderr=subprocess.STDOUT,
            text=True,
        )
    return completed.returncode, tail_text(log_path)


def collect_candidate_changes(repo_root: Path, state: dict) -> tuple[list[str], int, set[str]]:
    last_handled_ns = int(state.get("last_handled_ns", 0))
    processed_deleted = set(state.get("processed_deleted", []))
    changed_paths: list[str] = []
    newest_mtime_ns = last_handled_ns
    deleted_paths: set[str] = set()

    for path, deleted in git_status_paths(repo_root):
        if not is_relevant(path):
            continue
        full_path = repo_root / path
        if deleted or not full_path.exists():
            if path not in processed_deleted:
                changed_paths.append(path)
                deleted_paths.add(path)
            continue
        mtime_ns = full_path.stat().st_mtime_ns
        if mtime_ns > last_handled_ns:
            changed_paths.append(path)
            newest_mtime_ns = max(newest_mtime_ns, mtime_ns)

    return changed_paths, newest_mtime_ns, deleted_paths


def matching_pids() -> list[int]:
    patterns = [
        "site.addzero.kcloud.bootstrap.MainKt",
        ":apps:kcloud:ui:jvmRun",
        "/tmp/kmp-aio-kcloud-desktop.log",
    ]
    pids: set[int] = set()
    current_pid = os.getpid()
    for pattern in patterns:
        completed = subprocess.run(
            ["pgrep", "-f", pattern],
            capture_output=True,
            text=True,
        )
        if completed.returncode not in (0, 1):
            continue
        for line in completed.stdout.splitlines():
            line = line.strip()
            if not line:
                continue
            pid = int(line)
            if pid != current_pid:
                pids.add(pid)
    return sorted(pids)


def stop_existing_processes() -> None:
    pids = matching_pids()
    for pid in pids:
        try:
            os.kill(pid, signal.SIGTERM)
        except ProcessLookupError:
            continue
    if pids:
        time.sleep(3)
    for pid in pids:
        try:
            os.kill(pid, 0)
        except ProcessLookupError:
            continue
        try:
            os.kill(pid, signal.SIGKILL)
        except ProcessLookupError:
            continue


def start_app(repo_root: Path) -> tuple[int, bool, str]:
    log_stream = RUN_LOG_PATH.open("w", encoding="utf-8")
    process = subprocess.Popen(
        RUN_TASK,
        cwd=repo_root,
        stdout=log_stream,
        stderr=subprocess.STDOUT,
        start_new_session=True,
        text=True,
    )
    time.sleep(8)
    alive = process.poll() is None
    log_stream.flush()
    log_stream.close()
    return process.pid, alive, tail_text(RUN_LOG_PATH)


def hook_response(system_message: str | None = None) -> str:
    payload = {"continue": True}
    if system_message:
        payload["systemMessage"] = system_message
    return json.dumps(payload, ensure_ascii=True)


def main() -> int:
    payload = json.load(sys.stdin)
    cwd = payload.get("cwd") or "."
    session_id = payload.get("session_id") or "default"
    repo_root = git_root(cwd)
    if repo_root != TARGET_REPO_ROOT:
        print(hook_response())
        return 0
    state_path, state = load_state(repo_root, session_id)
    compile_log_path = repo_root / ".codex" / "state" / "kcloud-compile.log"

    changed_paths, newest_mtime_ns, deleted_paths = collect_candidate_changes(repo_root, state)
    if not changed_paths:
        print(hook_response())
        return 0

    compile_code, compile_tail = run_command(COMPILE_TASK, repo_root, compile_log_path)
    if compile_code != 0:
        message = (
            "KCloud compile failed after recent edits. "
            f"Changed files: {', '.join(changed_paths[:8])}. "
            f"Compile log: {compile_log_path}. "
            f"Tail:\n{compile_tail}"
        )
        print(hook_response(message))
        return 0

    stop_existing_processes()
    pid, alive, run_tail = start_app(repo_root)
    if not alive:
        message = (
            "KCloud app restart failed after a successful compile. "
            f"Run log: {RUN_LOG_PATH}. "
            f"Tail:\n{run_tail}"
        )
        print(hook_response(message))
        return 0

    state["last_handled_ns"] = max(newest_mtime_ns, time.time_ns())
    state["processed_deleted"] = sorted(set(state.get("processed_deleted", [])) | deleted_paths)
    save_state(state_path, state)
    print(hook_response())
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
