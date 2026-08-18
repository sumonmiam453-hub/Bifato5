# Project Custom Instructions & Guidelines

## 1. No Gemini API / Secrets Plugin
- This application does not use AI models or Gemini APIs.
- Do NOT include `libs.plugins.secrets` or `firebase.ai` in `build.gradle.kts`.
- Do NOT introduce dependencies on `GEMINI_API_KEY` or the Secrets plugin.

## 2. Release Signing Configuration
- For `release` builds, always use the default debug signing configuration:
  `signingConfig = signingConfigs.getByName("debugConfig")`
- Do NOT configure external `.jks` keystores or environment-variable passwords that cause build errors during `./gradlew assembleRelease`.
