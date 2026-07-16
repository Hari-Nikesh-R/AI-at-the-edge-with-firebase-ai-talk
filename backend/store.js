const fs = require("fs");
const path = require("path");

const DATA_DIR = path.join(__dirname, "data");
const STORE_FILE = path.join(DATA_DIR, "store.json");

const EMPTY_STORE = { insights: [], sessionSummaries: [] };

function ensureStore() {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
  if (!fs.existsSync(STORE_FILE)) {
    fs.writeFileSync(STORE_FILE, JSON.stringify(EMPTY_STORE, null, 2));
  }
}

ensureStore();

let cache = JSON.parse(fs.readFileSync(STORE_FILE, "utf8"));

function persist() {
  fs.writeFileSync(STORE_FILE, JSON.stringify(cache, null, 2));
}

function nextId(list) {
  return list.length ? Math.max(...list.map((item) => item.id)) + 1 : 1;
}

function addInsight(insight) {
  const doc = { id: nextId(cache.insights), receivedAt: Date.now(), ...insight };
  cache.insights.push(doc);
  persist();
  return doc;
}

function addSessionSummary(summary) {
  const doc = { id: nextId(cache.sessionSummaries), receivedAt: Date.now(), ...summary };
  cache.sessionSummaries.push(doc);
  persist();
  return doc;
}

function getInsights(userId) {
  const list = userId ? cache.insights.filter((i) => i.userId === userId) : cache.insights;
  return [...list].sort((a, b) => b.receivedAt - a.receivedAt);
}

function getSessionSummaries(userId) {
  const list = userId
    ? cache.sessionSummaries.filter((s) => s.userId === userId)
    : cache.sessionSummaries;
  return [...list].sort((a, b) => b.receivedAt - a.receivedAt);
}

function reset() {
  cache = { insights: [], sessionSummaries: [] };
  persist();
}

module.exports = {
  addInsight,
  addSessionSummary,
  getInsights,
  getSessionSummaries,
  reset,
};
