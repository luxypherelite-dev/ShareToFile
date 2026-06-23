# Share to File

A native Android app that exports AI chat conversations to PDF, TXT, or Markdown.

## Building

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17

### Steps
1. Open Android Studio
2. File → Open → select this `ShareToFile` folder
3. Wait for Gradle sync to complete (requires internet to download dependencies)
4. Build → Build Bundle(s)/APK(s) → Build APK(s)
5. The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Sideloading
Because the app uses Accessibility Service for overlay capture, it cannot be distributed
via the Play Store. Sideload the APK:
- Enable "Install unknown apps" in Android Settings → Apps → Special app access
- Transfer the APK to your device and tap to install
- Or: `adb install app/build/outputs/apk/debug/app-debug.apk`

### First-run setup
1. Settings → Choose export folder (tap once, persists forever)
2. Settings → Enable Accessibility → find "Share to File – Chat Capture" → toggle on
3. Settings → Allow Overlay (for floating button)
4. Settings → Watched Apps → add package names (e.g. `com.openai.chatgpt`)

## Package
`com.aiexport.shareordirect`

## Features
- Share intent: share text/URL from any app → parse → export
- URL fetch: OkHttp + Jsoup extract readable content
- Overlay capture: floating button in watched apps → auto-scroll → deduplicated transcript
- Block parser: heading / code / list / Q&A / paragraph
- Export: PDF (PdfDocument API), TXT, Markdown
- Persisted export folder via DataStore + Storage Access Framework
