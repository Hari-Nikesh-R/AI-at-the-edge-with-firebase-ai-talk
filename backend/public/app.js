const insightsList = document.getElementById("insights-list");
const summariesList = document.getElementById("summaries-list");
const counter = document.getElementById("counter");
const resetBtn = document.getElementById("reset-btn");

function timeAgo(ts) {
  const seconds = Math.floor((Date.now() - ts) / 1000);
  if (seconds < 5) return "just now";
  if (seconds < 60) return `${seconds}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  return `${hours}h ago`;
}

function renderInsights(insights) {
  if (!insights.length) {
    insightsList.innerHTML = '<div class="empty">Waiting for insights from the Android app…</div>';
    return;
  }
  insightsList.innerHTML = insights
    .map(
      (i) => `
    <div class="card">
      <div class="card-title">
        <span>${i.persona} (${i.confidence}% confidence)</span>
        <span class="card-meta">${timeAgo(i.receivedAt)}</span>
      </div>
      <div class="card-meta">user: ${i.userId} · session: ${i.sessionId}</div>
      <div class="card-body">
        <span>Engagement: ${i.engagement}</span>
        <span>Purchase Intent: ${i.purchaseIntent}</span>
        <span>Predicted Next: ${i.predictedNextScreen}</span>
      </div>
    </div>`
    )
    .join("");
}

function renderSummaries(summaries) {
  if (!summaries.length) {
    summariesList.innerHTML = '<div class="empty">Waiting for session summaries…</div>';
    return;
  }
  summariesList.innerHTML = summaries
    .map(
      (s) => `
    <div class="card">
      <div class="card-title">
        <span>${s.interest}</span>
        <span class="card-meta">${timeAgo(s.receivedAt)}</span>
      </div>
      <div class="card-meta">user: ${s.userId} · session: ${s.sessionId}</div>
      <div class="card-body">
        <span>Engagement: ${s.engagement}</span>
        <span>Intent: ${s.intent}</span>
        <span>Events: ${s.eventCount}</span>
      </div>
    </div>`
    )
    .join("");
}

async function poll() {
  try {
    const [insightsRes, summariesRes] = await Promise.all([
      fetch("/api/insights"),
      fetch("/api/session-summaries"),
    ]);
    const insights = await insightsRes.json();
    const summaries = await summariesRes.json();
    renderInsights(insights);
    renderSummaries(summaries);
    counter.textContent = `${insights.length} insights received / ${summaries.length} session summaries received`;
  } catch (err) {
    console.error("Poll failed", err);
  }
}

resetBtn.addEventListener("click", async () => {
  await fetch("/api/reset", { method: "POST" });
  poll();
});

poll();
setInterval(poll, 2000);
