# Running the Agentic Frontends Demo

This repo implements the shopping-app demo described in `README.md`: an Android app
(`android/`) with an on-device Frontend Agent, and a Node backend (`backend/`) that stands
in for the "Firebase" box in the architecture diagram.

```
android/   EdgeShop — Kotlin + Jetpack Compose shopping app with the on-device Frontend Agent
backend/   Mock-Firebase Node server: receives insights/session summaries, serves a live dashboard
```

## How it maps to the talk

| README capability | Where it lives |
|---|---|
| 1. Persona Generation | `agent/FrontendAgentModel.kt` (on-device TFLite inference) |
| 2. Dynamic UI Adaptation | `ui/product/ProductDetailScreen.kt` (Researcher vs. ImpulseBuyer layouts) |
| 3. Predictive Navigation | `AgentRepository.recompute()` preloads Cart when `predictedNextScreen == "Cart"` |
| 4. Smart Notification Agent | `agent/NotificationAgent.kt`, shown on the Agent Dashboard |
| 5. Session Summarization | `AgentRepository.endSession()` → `POST /api/session-summary` |
| 6. Offline Recommendations | `data/event/ProductPreference.kt` (Room cache) + Airplane Mode toggle on Home |
| 7. Smart Search Assistant | `ui/search/SearchScreen.kt` (ML Kit Entity Extraction, on-device) |
| 8. Self-Healing Forms | `agent/SelfHealingFormAgent.kt`, wired into `ui/checkout/CheckoutScreen.kt` |
| 9. Accessibility Agent | `agent/AccessibilityAgent.kt`, `ui/accessibility/AccessibilityDemoScreen.kt` |
| 10. Edge Fraud Detection | `agent/FraudRiskEngine.kt`, `ui/fraud/FraudDemoScreen.kt` |

The Frontend Agent's "brain" (persona/engagement/intent/next-screen) is a small multi-head
TensorFlow Lite model trained on synthetic data — see `android/ml/README.md` for how it was
trained and how to retrain it. Everything else (notifications, offline recs, accessibility,
self-healing forms, fraud) is a transparent on-device heuristic, by design — no LLM, no
Ollama, no server round-trip for AI. The Node backend only stores the insights the agent
already produced, matching the talk's "send intelligence, not events" thesis.

## Prerequisites

- Node.js
- Android SDK + an emulator or device (API 26+), Java 17
- The Gradle wrapper (`android/gradlew`) handles the rest

## 1. Start the backend

```bash
cd backend
npm install
npm start
```

Dashboard at http://localhost:4000/. Leave this running.

## 2. Build and run the Android app

```bash
cd android
JAVA_HOME=<path-to-a-JDK-17> ./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.agenticedge.shopdemo/.MainActivity
```

The app talks to the backend at `http://10.0.2.2:4000/` (the emulator's alias for the host
machine), configured in `android/app/build.gradle.kts` as `BACKEND_BASE_URL`. If you're running
on a **physical device** instead of the emulator, change that to your machine's LAN IP and
make sure the device is on the same network — cleartext HTTP to that IP is already allowed in
`network_security_config.xml` via the manifest's `usesCleartextTraffic`.

## 3. Walk through the demo

1. **Home** → browse products, notice the Airplane Mode toggle (Offline Recommendations).
2. **Search** → type a product name; on-device suggestions appear before you finish typing.
3. Open a product → tap **Compare** and **Read reviews** a couple of times → watch the
   **Agent status bar** at the top flip persona to *Researcher* and Specifications/Reviews
   become prominent.
4. **Add to Cart** on a product instead → persona shifts toward *ImpulseBuyer*, the layout
   swaps to a Buy-Now hero with a discount banner, and a "Predictive Navigation" banner shows
   the agent already preloaded Cart.
5. **Cart → Checkout** → tap "Simulate 10 more checkouts abandoning here" to see the
   Self-Healing Form Agent mark Address Line 2 optional and add a hint.
6. **Agent tab** → see live persona/engagement/intent, trigger the Smart Notification Agent
   buttons, then tap **End Session** to send a compact session summary to the backend.
7. Refresh http://localhost:4000/ to see the insights and session summaries arrive live,
   next to a static example of what traditional raw-event analytics looks like in contrast.
8. From **Home → Demo extras**: try the **Accessibility Agent** (3 taps auto-activates
   large-text mode app-wide) and **Fraud Risk Check** (toggle device/location/velocity
   signals and see the on-device risk score).

## Notes

- All persona/engagement/intent/next-screen inference happens on-device via TensorFlow Lite —
  toggle Airplane Mode and the agent keeps working (only the backend POSTs are skipped).
- The backend has a `POST /api/reset` endpoint to clear stored insights between demo runs.
