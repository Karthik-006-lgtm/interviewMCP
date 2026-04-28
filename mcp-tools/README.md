# MCP Tools

This folder contains manifest-style tool definitions that can automate common platform workflows against the backend and AI service.
The backend also exposes these manifests through `GET /api/mcp/tools` and `GET /api/mcp/tools/{toolName}` for catalog discovery.
Each tool action can now be invoked through `POST /api/mcp/tools/{toolName}/actions/{actionName}/invoke` with an `input` object payload, turning the manifest catalog into a runnable MCP-style bridge.
There is also a standalone local runtime entrypoint at `mcp-tools/runtime_server.py` that exposes a minimal MCP-compatible stdio server for `initialize`, `tools/list`, and `tools/call`.

- `resume_tool`: upload resumes and trigger analysis
- `company_tool`: fetch company matches, detail, and simulation-ready interview context
- `grammar_tool`: run answer breakdown scoring, grammar feedback, and live coaching hints against answer drafts
- `interview_tool`: create company-aware simulation sessions, request live coach hints, and submit answers for adaptive scoring
- `speech_tool`: upload interview audio for transcript, confidence, fluency, clarity, tone, pronunciation, and emotion analysis
- `recommendation_tool`: fetch candidate roadmap, reports dashboard data, missing skills, and improvement planning guidance

Simulation features exposed through the MCP layer now include:

- company-aware interviewer tone and role alignment
- adaptive difficulty progression
- timed reality modes with interruptions, panel voices, and offline lag simulation
- answer breakdown scoring for structure, impact, hesitation, and fillers
- live coaching prompts plus roadmap generation

## Standalone runtime

Run the local MCP runtime with:

```bash
cd mcp-tools
python runtime_server.py
```

Example JSON-RPC messages over stdio:

```json
{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"interview_tool","arguments":{"action":"create_session","authorization":"Bearer <jwt>","selectedRoles":["Backend Engineer"],"personalityProfile":"Analytical and structured"}}}
```

The runtime resolves tool manifests from this folder and forwards actions to the configured backend and AI service URLs.
