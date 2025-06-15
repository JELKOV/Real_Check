// stats.js - 관리자 통계 페이지 JS 리팩토링 (함수화 & lazy-loading)
const chartInstanceMap = {};

document.addEventListener("DOMContentLoaded", () => {
  fetchOverviewStats();
  fetchLogStats();
  fetchTopUsers();
  fetchUserSignupChart();

  // 탭 lazy-loading 설정
  addLazyTab("#point-stats-tab", fetchPointStats);
  addLazyTab("#place-stats-tab", fetchPlaceStats);
  addLazyTab("#request-stats-tab", fetchRequestStats);
});

// ─────────────────────────────────────────────────────────────────
// [ 공통 유틸 함수 ]
// ─────────────────────────────────────────────────────────────────
function createChart(canvasId, type, labels, data, label = "", options = {}) {
  const ctx = document.getElementById(canvasId);

  // 이미 생성된 차트가 있다면 제거
  if (chartInstanceMap[canvasId]) {
    chartInstanceMap[canvasId].destroy();
  }

  // 새 차트 생성
  const newChart = new Chart(ctx, {
    type,
    data: {
      labels,
      datasets: [{ label, data, borderWidth: 1 }],
    },
    options: Object.assign(
      {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true } },
      },
      options
    ),
  });

  // 새 차트를 저장
  chartInstanceMap[canvasId] = newChart;

  return newChart;
}

function fillTable(tbodyId, items, rowFn) {
  const tbody = document.getElementById(tbodyId);
  tbody.innerHTML = "";
  items.forEach((item, i) => {
    const tr = document.createElement("tr");
    tr.innerHTML = rowFn(item, i);
    tbody.appendChild(tr);
  });
}

function addLazyTab(selector, callback) {
  document
    .querySelector(selector)
    .addEventListener("shown.bs.tab", callback, { once: true });
}

// ─────────────────────────────────────────────────────────────────
// [1] Overview 통계
// ─────────────────────────────────────────────────────────────────
function fetchOverviewStats() {
  fetch("/api/admin/stats/reports")
    .then((res) => res.json())
    .then(
      (data) =>
        (document.getElementById("reportCount").textContent = data.reportCount)
    );

  fetch("/api/admin/stats/points")
    .then((res) => res.json())
    .then(
      (data) =>
        (document.getElementById("pointTotal").textContent = data.pointTotal)
    );
}

// ─────────────────────────────────────────────────────────────────
// [2] 로그 통계
// ─────────────────────────────────────────────────────────────────
function fetchLogStats() {
  fetch("/api/admin/stats/logs/monthly")
    .then((res) => res.json())
    .then((data) => {
      const labels = data.map(
        (d) => `${d.year}-${d.month.toString().padStart(2, "0")}`
      );
      const counts = data.map((d) => d.count);
      createChart("logChart", "bar", labels, counts, "등록 수");
    });

  fetch("/api/admin/stats/logs/category")
    .then((res) => res.json())
    .then((data) => {
      const labels = data.map((d) => d.category);
      const counts = data.map((d) => d.count);
      document.getElementById(
        "totalLogsByCategory"
      ).textContent = `${counts.reduce((a, b) => a + b, 0)} 건`;
      createChart("categoryChart", "pie", labels, counts);
    });
}

// ─────────────────────────────────────────────────────────────────
// [3] 사용자 통계
// ─────────────────────────────────────────────────────────────────
function fetchUserSignupChart() {
  // 1. 가입자 수
  fetch("/api/admin/stats/users/monthly")
    .then((res) => res.json())
    .then((data) => {
      const labels = data.map(
        (d) => `${d.year}-${d.month.toString().padStart(2, "0")}`
      );
      const counts = data.map((d) => d.signUpCount);
      createChart("userSignUpChart", "line", labels, counts, "가입자 수");
    });

  // 2. 탈퇴자 수
  fetch("/api/admin/stats/users/deleted/monthly")
    .then((res) => res.json())
    .then((data) => {
      const labels = data.map(
        (d) => `${d.year}-${d.month.toString().padStart(2, "0")}`
      );
      const counts = data.map((d) => d.count);
      createChart("userDeletionChart", "line", labels, counts, "탈퇴자 수");
    });

  // 3. 누적 활성/비활성
  fetch("/api/admin/stats/users/active-stats")
    .then((res) => res.json())
    .then((data) => {
      createChart(
        "userActiveChart",
        "doughnut",
        ["활성", "비활성"],
        [data.active, data.inactive]
      );
    });
}

// ─────────────────────────────────────────────────────────────────
// [4] Top 사용자 통계
// ─────────────────────────────────────────────────────────────────
function fetchTopUsers() {
  fetch("/api/admin/stats/reports/top-users")
    .then((res) => res.json())
    .then((data) =>
      fillTable(
        "topReportedUsers",
        data,
        (u, i) => `
      <td>${i + 1}</td><td>${u.userId}</td><td>${u.nickname}</td><td>${
          u.reportCount
        }</td>
    `
      )
    );

  fetch("/api/admin/stats/users/top")
    .then((res) => res.json())
    .then((data) =>
      fillTable(
        "topContributingUsers",
        data,
        (u, i) => `
      <td>${i + 1}</td><td>${u.userId}</td><td>${u.nickname}</td><td>${
          u.logCount
        }</td>
    `
      )
    );
}

// ─────────────────────────────────────────────────────────────────
// [5] 포인트 통계
// ─────────────────────────────────────────────────────────────────
function fetchPointStats() {
  fetch("/api/admin/points/daily-aggregate")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "pointFlowChart",
        "line",
        data.map((d) => d.date),
        data.map((d) => d.sumAmount),
        "일별 포인트 증감"
      )
    );

  fetch("/api/admin/points/recent?limit=10")
    .then((res) => res.json())
    .then((data) =>
      fillTable(
        "recentPointHistory",
        data,
        (p) => `
      <td>${p.earnedAt.replace("T", " ")}</td>
      <td>${p.userId}</td>
      <td>${p.amount}</td>
      <td>${p.reason}</td>
      <td>${p.type}</td>
    `
      )
    );

  fetch("/api/admin/points/distribution")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "pointDistributionChart",
        "pie",
        ["잔액 > 0", "잔액 = 0"],
        [data.positiveCount, data.zeroCount]
      )
    );

  fetch("/api/admin/points/top?limit=10")
    .then((res) => res.json())
    .then((data) =>
      fillTable(
        "topPointUsers",
        data,
        (u, i) => `
      <td>${i + 1}</td><td>${u.userId}</td><td>${u.nickname}</td><td>${
          u.points
        }</td>
    `
      )
    );

  fetch("/api/admin/points/zero/ratio")
    .then((res) => res.json())
    .then((data) => {
      const pct = (data.zeroRatio * 100).toFixed(1) + "%";
      document.getElementById(
        "zeroBalanceRatio"
      ).textContent = `${data.zeroCount} / ${data.totalCount} (${pct})`;
    });
}

// ─────────────────────────────────────────────────────────────────
// [6] 장소 통계
// ─────────────────────────────────────────────────────────────────
function fetchPlaceStats() {
  fetch("/api/admin/stats/places/status")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "placeStatusChart",
        "pie",
        ["승인", "반려", "대기 중"],
        [data.approved, data.rejected, data.pending]
      )
    );

  fetch("/api/admin/stats/places/monthly")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "placeMonthlyChart",
        "bar",
        data.map((d) => `${d.year}-${d.month.toString().padStart(2, "0")}`),
        data.map((d) => d.count),
        "장소 등록 수"
      )
    );
}

// ─────────────────────────────────────────────────────────────────
// [7] 요청 통계
// ─────────────────────────────────────────────────────────────────
function fetchRequestStats() {
  fetch("/api/admin/stats/monthly")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "requestMonthlyChart",
        "bar",
        data.map((d) => `${d.year}-${d.month.toString().padStart(2, "0")}`),
        data.map((d) => d.count),
        "요청 등록 수"
      )
    );

  fetch("/api/admin/stats/category")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "requestCategoryChart",
        "pie",
        data.map((d) => d.category),
        data.map((d) => d.count)
      )
    );

  fetch("/api/admin/stats/status")
    .then((res) => res.json())
    .then((data) =>
      createChart(
        "requestStatusChart",
        "doughnut",
        ["진행 중", "마감됨"],
        [data.open, data.closed]
      )
    );

  fetch("/api/admin/stats/top-users")
    .then((res) => res.json())
    .then((data) =>
      fillTable(
        "topRequestUsers",
        data,
        (u, i) => `
      <td>${i + 1}</td><td>${u.userId}</td><td>${u.nickname}</td><td>${
          u.requestCount
        }</td>
    `
      )
    );
}
