# Agentic Frontends — Mock Firebase Backend

Stands in for the "Firebase" box in the architecture diagram. It does not do any AI — all
inference happens on-device in the Android app's Frontend Agent. This server only receives
and stores the *insights* the agent produces, and serves a live dashboard contrasting that
with what traditional raw-event analytics would look like.

## Install & run

```bash
npm install
npm start
```

Listens on `http://0.0.0.0:4000` (override with `PORT=xxxx npm start`).

- Dashboard: http://localhost:4000/
- From the Android emulator, reach it at: `http://10.0.2.2:4000`

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | Health check → `{ status: "ok" }` |
| POST | `/api/insights` | Store a persona/engagement/intent insight from the Frontend Agent |
| GET | `/api/insights` | List all insights, newest first |
| GET | `/api/insights/:userId` | List insights for one user, newest first |
| POST | `/api/session-summary` | Store a session summary from the Frontend Agent |
| GET | `/api/session-summaries` | List all session summaries, newest first |
| GET | `/api/session-summaries/:userId` | List session summaries for one user, newest first |
| POST | `/api/reset` | Clear all stored data (for resetting between demo runs) |

### `POST /api/insights` body shape

```json
{
  "userId": "user-1",
  "sessionId": "session-abc",
  "persona": "Researcher",
  "confidence": 92,
  "engagement": 92,
  "purchaseIntent": 88,
  "predictedNextScreen": "Cart"
}
```

### `POST /api/session-summary` body shape

```json
{
  "userId": "user-1",
  "sessionId": "session-abc",
  "interest": "Electronics",
  "engagement": "High",
  "intent": "Buy Mobile Phone",
  "eventCount": 14
}
```

Data persists to `data/store.json` (gitignored — regenerated on first run).
