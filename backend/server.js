const path = require("path");
const express = require("express");
const cors = require("cors");
const store = require("./store");

const PORT = process.env.PORT || 4000;

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

function requireFields(body, fields) {
  const missing = fields.filter((f) => body[f] === undefined || body[f] === null || body[f] === "");
  return missing;
}

app.get("/api/health", (req, res) => {
  res.json({ status: "ok" });
});

app.post("/api/insights", (req, res) => {
  const required = [
    "userId",
    "sessionId",
    "persona",
    "confidence",
    "engagement",
    "purchaseIntent",
    "predictedNextScreen",
  ];
  const missing = requireFields(req.body, required);
  if (missing.length) {
    return res.status(400).json({ error: `Missing required field(s): ${missing.join(", ")}` });
  }
  const doc = store.addInsight(req.body);
  res.status(201).json(doc);
});

app.get("/api/insights", (req, res) => {
  res.json(store.getInsights());
});

app.get("/api/insights/:userId", (req, res) => {
  res.json(store.getInsights(req.params.userId));
});

app.post("/api/session-summary", (req, res) => {
  const required = ["userId", "sessionId", "interest", "engagement", "intent", "eventCount"];
  const missing = requireFields(req.body, required);
  if (missing.length) {
    return res.status(400).json({ error: `Missing required field(s): ${missing.join(", ")}` });
  }
  const doc = store.addSessionSummary(req.body);
  res.status(201).json(doc);
});

app.get("/api/session-summaries", (req, res) => {
  res.json(store.getSessionSummaries());
});

app.get("/api/session-summaries/:userId", (req, res) => {
  res.json(store.getSessionSummaries(req.params.userId));
});

app.post("/api/reset", (req, res) => {
  store.reset();
  res.json({ status: "reset" });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Agentic Frontends mock-Firebase backend listening on http://0.0.0.0:${PORT}`);
  console.log(`Dashboard: http://localhost:${PORT}/`);
  console.log(`From Android emulator, reach it at: http://10.0.2.2:${PORT}`);
});
