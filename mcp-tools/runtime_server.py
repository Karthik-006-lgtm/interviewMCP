from __future__ import annotations

import json
import mimetypes
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parent


def load_manifest(tool_name: str) -> dict[str, Any]:
    manifest_path = ROOT / tool_name / "tool.json"
    if not manifest_path.exists():
        raise FileNotFoundError(f"Tool manifest not found: {tool_name}")
    return json.loads(manifest_path.read_text(encoding="utf-8"))


def list_tools() -> list[dict[str, Any]]:
    tools: list[dict[str, Any]] = []
    for directory in sorted(ROOT.iterdir()):
        if not directory.is_dir():
            continue
        manifest_path = directory / "tool.json"
        if manifest_path.exists():
            tools.append(json.loads(manifest_path.read_text(encoding="utf-8")))
    return tools


def resolve_action(manifest: dict[str, Any], action_name: str) -> dict[str, Any]:
    for action in manifest.get("actions", []):
        if action.get("name") == action_name:
            return action
    raise KeyError(f"Action '{action_name}' not found for tool '{manifest.get('name')}'")


def encode_multipart(data: dict[str, Any]) -> tuple[bytes, str]:
    boundary = "----InterviewPrepMcpBoundary"
    parts: list[bytes] = []
    for key, value in data.items():
        if value is None:
            continue
        if key == "file":
            file_path = Path(str(value)).expanduser().resolve()
            file_bytes = file_path.read_bytes()
            content_type = mimetypes.guess_type(file_path.name)[0] or "application/octet-stream"
            parts.extend(
                [
                    f"--{boundary}\r\n".encode(),
                    f'Content-Disposition: form-data; name="{key}"; filename="{file_path.name}"\r\n'.encode(),
                    f"Content-Type: {content_type}\r\n\r\n".encode(),
                    file_bytes,
                    b"\r\n",
                ]
            )
        elif isinstance(value, list):
            for item in value:
                parts.extend(
                    [
                        f"--{boundary}\r\n".encode(),
                        f'Content-Disposition: form-data; name="{key}"\r\n\r\n'.encode(),
                        str(item).encode(),
                        b"\r\n",
                    ]
                )
        else:
            parts.extend(
                [
                    f"--{boundary}\r\n".encode(),
                    f'Content-Disposition: form-data; name="{key}"\r\n\r\n'.encode(),
                    str(value).encode(),
                    b"\r\n",
                ]
            )
    parts.append(f"--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def invoke_action(
    tool_name: str,
    action_name: str,
    arguments: dict[str, Any] | None,
    auth_header: str | None = None,
) -> Any:
    manifest = load_manifest(tool_name)
    action = resolve_action(manifest, action_name)
    payload = dict(arguments or {})
    base_url = manifest["baseUrl"].rstrip("/")
    method = action.get("method", "GET").upper()
    path = action.get("path", "/")

    for key in list(payload.keys()):
        token = "{" + key + "}"
        if token in path:
            path = path.replace(token, urllib.parse.quote(str(payload.pop(key))))

    url = f"{base_url}{path}"
    headers = {"Accept": "application/json"}
    if auth_header:
        headers["Authorization"] = auth_header

    data: bytes | None = None
    if method in {"GET", "DELETE"}:
        if payload:
            query = urllib.parse.urlencode(
                [(key, item) for key, value in payload.items() for item in (value if isinstance(value, list) else [value])]
            )
            url = f"{url}?{query}"
    else:
        if action.get("contentType") == "multipart/form-data" or "file" in payload:
            data, content_type = encode_multipart(payload)
            headers["Content-Type"] = content_type
        else:
            data = json.dumps(payload).encode("utf-8")
            headers["Content-Type"] = "application/json"

    request = urllib.request.Request(url=url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request) as response:
            raw = response.read().decode("utf-8")
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8")
        raise RuntimeError(f"HTTP {exc.code}: {body}") from exc


def write_message(payload: dict[str, Any]) -> None:
    sys.stdout.write(json.dumps(payload) + "\n")
    sys.stdout.flush()


def handle_message(message: dict[str, Any]) -> dict[str, Any] | None:
    method = message.get("method")
    params = message.get("params", {})
    message_id = message.get("id")

    if method == "initialize":
        return {
            "jsonrpc": "2.0",
            "id": message_id,
            "result": {
                "protocolVersion": "2024-11-05",
                "serverInfo": {"name": "interview-prep-mcp-runtime", "version": "0.0.1"},
                "capabilities": {"tools": {}},
            },
        }

    if method == "tools/list":
        tools = [
            {
                "name": tool["name"],
                "description": tool.get("description", ""),
                "inputSchema": {
                    "type": "object",
                    "properties": {
                        action["name"]: action.get("inputSchema", {"type": "object"})
                        for action in tool.get("actions", [])
                    },
                },
            }
            for tool in list_tools()
        ]
        return {"jsonrpc": "2.0", "id": message_id, "result": {"tools": tools}}

    if method == "tools/call":
        tool_name = params.get("name")
        arguments = params.get("arguments", {})
        action_name = arguments.pop("action", None)
        auth_header = arguments.pop("authorization", None)
        if not tool_name or not action_name:
            raise ValueError("tools/call requires 'name' and arguments.action")
        result = invoke_action(tool_name, action_name, arguments, auth_header)
        return {
            "jsonrpc": "2.0",
            "id": message_id,
            "result": {"content": [{"type": "text", "text": json.dumps(result)}]},
        }

    if method == "ping":
        return {"jsonrpc": "2.0", "id": message_id, "result": {"ok": True}}

    if message_id is None:
        return None
    return {
        "jsonrpc": "2.0",
        "id": message_id,
        "error": {"code": -32601, "message": f"Unsupported method: {method}"},
    }


def main() -> int:
    for line in sys.stdin:
        payload = line.strip()
        if not payload:
            continue
        try:
            message = json.loads(payload)
            response = handle_message(message)
            if response is not None:
                write_message(response)
        except Exception as exc:  # noqa: BLE001
            write_message(
                {
                    "jsonrpc": "2.0",
                    "id": None,
                    "error": {"code": -32000, "message": str(exc)},
                }
            )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
