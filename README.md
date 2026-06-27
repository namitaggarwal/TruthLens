# TruthLens

A privacy-first Android app that helps users identify potentially misleading, unverified, or manipulated content visible on their screen in supported social media, news, and messaging apps.

---

## Features

- **Background monitoring** via Android Accessibility Service — reads visible text in opted-in apps only
- **Non-intrusive overlay alerts** — small banner at the top of the active app with colour-coded risk level
- **Mock + remote fact-check engine** — swap `MockFactCheckProvider` → `RemoteFactCheckProvider` for production
- **Local risk scoring** — on-device heuristic analysis with no network required
- **Scan history** — local-only, encrypted at rest by Android, never uploaded
- **Privacy controls** — master toggle, per-app toggles, clear/delete all data, export
- **Full onboarding** — explains every permission before asking

---

## Supported Apps (toggleable)

| App | Package |
|-----|---------|
| Chrome | com.android.chrome |
| Instagram | com.instagram.android |
| X (Twitter) | com.twitter.android |
| Facebook | com.facebook.katana |
| WhatsApp | com.whatsapp |
| YouTube | com.google.android.youtube |
| + custom apps | user-defined |

---

## Architecture

```
app/
└── src/main/java/com/truthlens/app/
    ├── di/                      # Hilt modules
    ├── domain/
    │   ├── model/               # RiskLevel, FactCheckResult, ScanHistory, …
    │   ├── provider/            # FactCheckProvider, OcrProvider interfaces
    │   ├── repository/          # Repository interfaces
    │   └── usecase/             # AnalyzeContentUseCase, GetScanHistoryUseCase, …
    ├── data/
    │   ├── local/db/            # Room: TruthLensDatabase, DAOs, entities
    │   ├── local/datastore/     # UserPreferencesDataStore (DataStore<Preferences>)
    │   ├── remote/              # Retrofit API + DTOs
    │   └── repository/          # Repository implementations
    ├── factcheck/               # MockFactCheckProvider, RemoteFactCheckProvider, LocalRiskScorer
    ├── ocr/                     # MlKitOcrProvider
    ├── service/                 # AccessibilityService, OverlayService, ForegroundService, BootReceiver
    └── ui/
        ├── theme/               # Material 3 colours, typography
        ├── navigation/          # NavGraph, Screen sealed class
        ├── onboarding/          # 4-page onboarding flow
        ├── dashboard/           # App monitoring toggles
        ├── details/             # FactCheckDetailScreen
        ├── history/             # ScanHistoryScreen
        ├── settings/            # Settings, Privacy Policy, How It Works, Permissions
        └── overlay/             # OverlayBanner composable
```

**Pattern**: MVVM + Clean Architecture, unidirectional data flow via `StateFlow`.

---

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- A connected device or emulator running Android 10+ (API 29+)

### 1. Clone and open

```bash
git clone https://github.com/your-org/TruthLens.git
cd TruthLens
```

Open the project in Android Studio (`File → Open → select the TruthLens folder`).

### 2. Configure the API endpoint (optional for development)

In `di/NetworkModule.kt`, the `BASE_URL` is set to `https://api.truthlens.app/`.  
For development, `MockFactCheckProvider` is bound via `di/FactCheckModule.kt` — no backend needed.

To switch to a real backend:
1. Update `BASE_URL` in `NetworkModule.kt`
2. In `FactCheckModule.kt`, change the binding from `MockFactCheckProvider` to `RemoteFactCheckProvider`

### 3. Build

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or use Android Studio's Run button (▶).

### 5. Grant permissions (on device)

After launching the app, complete onboarding and enable:

1. **Accessibility Service**: Settings → Accessibility → TruthLens Content Monitor → Enable
2. **Display over apps**: Settings → Apps → TruthLens → Display over other apps → Allow

These are the only two required permissions. OCR and notification access are optional.

---

## Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Uses `MockFactCheckProvider`, debug logging via Timber, applicationId suffix `.debug` |
| `release` | Minified via R8, requires signing config. Swap fact-check provider before release. |

### Release APK

```bash
# Add your keystore to app/keystore.jks, then:
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=keystore.jks \
  -Pandroid.injected.signing.store.password=YOUR_STORE_PASS \
  -Pandroid.injected.signing.key.alias=YOUR_KEY_ALIAS \
  -Pandroid.injected.signing.key.password=YOUR_KEY_PASS
```

---

## Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

Key test files:
- `LocalRiskScorerTest` — verifies heuristic scoring behaviour
- `AnalyzeContentUseCaseTest` — verifies use case logic, repo interactions, error handling

---

## Privacy & Security Notes

### What TruthLens does NOT do

- ❌ Read passwords, PIN fields, or keyboard input
- ❌ Monitor apps the user has not explicitly enabled
- ❌ Upload screenshots or raw message content
- ❌ Access encrypted content in end-to-end encrypted apps (e.g., WhatsApp DMs)
- ❌ Bypass Android security restrictions
- ❌ Declare content definitively fake

### What it does

- ✅ Read **visible accessibility text** from opted-in apps only
- ✅ Perform on-device heuristic scoring (no network required)
- ✅ Optionally send **anonymised claim text** to a fact-check API over HTTPS
- ✅ Store scan history **locally only** in an encrypted Room database
- ✅ Allow users to **delete all data** at any time

### Sensitive content protection

`TruthLensAccessibilityService` skips scanning when:
- Any node in the accessibility tree has `isPassword = true`
- Hint/content description/view ID contains keywords like `password`, `cvv`, `pin`, `card number`, `bank`
- The app package is in `AppMonitorConfig.BLOCKED_PACKAGES`

### Network security

`network_security_config.xml` disables cleartext HTTP. All API calls use HTTPS only.

---

## Switching from Mock to Real Fact-Check API

The `FactCheckProvider` interface is the only contract the rest of the app depends on:

```kotlin
interface FactCheckProvider {
    suspend fun analyzeClaim(text: String, sourceApp: String): FactCheckResult
    fun isAvailable(): Boolean
}
```

To integrate a real backend:
1. Implement `FactCheckProvider` (or use the existing `RemoteFactCheckProvider`)
2. Update `BASE_URL` in `NetworkModule.kt`
3. Change the `@Binds` in `FactCheckModule.kt`

Trusted free/public fact-check APIs to consider:
- [Google Fact Check Tools API](https://developers.google.com/fact-check/tools/api)
- [ClaimBuster API](https://idir.uta.edu/claimbuster/)
- [Snopes API](https://www.snopes.com/api/) (requires partnership)

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Follow the existing MVVM + Clean Architecture pattern
4. Add unit tests for new use cases and risk scoring logic
5. Submit a pull request

---

## Disclaimer

TruthLens is an informational tool. It does not determine absolute truth. Always consult multiple reliable sources before forming conclusions or sharing information.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
