from __future__ import annotations

import os

try:
    from pydantic_settings import BaseSettings, SettingsConfigDict

    class Settings(BaseSettings):
        app_name: str = "Interview Prep MCP AI Service"
        app_version: str = "0.0.1"
        language_tool_url: str | None = None
        whisper_model_name: str = "base"
        model_config = SettingsConfigDict(env_file=".env", extra="ignore")

except ImportError:
    class Settings:
        def __init__(self) -> None:
            self.app_name = os.getenv("APP_NAME", "Interview Prep MCP AI Service")
            self.app_version = os.getenv("APP_VERSION", "0.0.1")
            self.language_tool_url = os.getenv("LANGUAGE_TOOL_URL") or None
            self.whisper_model_name = os.getenv("WHISPER_MODEL_NAME", "base")


settings = Settings()
