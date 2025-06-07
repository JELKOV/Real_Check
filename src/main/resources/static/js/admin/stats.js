document.addEventListener("DOMContentLoaded", () => {
  // ─────────────────────────────────────────────────────────────────
  // [1] 총 신고 수 가져오기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/reports")
    .then((res) => res.json())
    .then((data) => {
      document.getElementById("reportCount").textContent = data.reportCount;
    });

  // ─────────────────────────────────────────────────────────────────
  // [2] 포인트 총합 가져오기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/points")
    .then((res) => res.json())
    .then((data) => {
      document.getElementById("pointTotal").textContent = data.pointTotal;
    });

  // ─────────────────────────────────────────────────────────────────
  // [3] 월별 상태 로그 등록 수 그래프 그리기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/logs/monthly")
    .then((res) => res.json())
    .then((data) => {
      const labels = data.map((item) => {
        // 월을 두 자리 포맷으로 맞추기 (예: 2025-04)
        const mm = item.month.toString().padStart(2, "0");
        return `${item.year}-${mm}`;
      });
      const counts = data.map((item) => item.count);

      new Chart(document.getElementById("logChart"), {
        type: "bar",
        data: {
          labels: labels,
          datasets: [
            {
              label: "등록 수",
              data: counts,
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              stepSize: 1,
            },
          },
        },
      });
    });

  // ─────────────────────────────────────────────────────────────────
  // [4] 카테고리별 상태로그 수 가져오기 및 차트 그리기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/logs/category")
    .then((res) => res.json())
    .then((data) => {
      // data: [{ category: "ANSWER", count: 52 }, { category: "REGISTER", count: 37 }, ...]
      const labels = data.map((item) => item.category);
      const counts = data.map((item) => item.count);

      // 총합을 표시 (선택)
      const total = counts.reduce((sum, cur) => sum + cur, 0);
      document.getElementById("totalLogsByCategory").textContent =
        total + " 건";

      new Chart(document.getElementById("categoryChart"), {
        type: "pie",
        data: {
          labels: labels,
          datasets: [
            {
              label: "카테고리별 건수",
              data: counts,
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
        },
      });
    });

  // ─────────────────────────────────────────────────────────────────
  // [5] 월별 사용자 가입 통계 가져오기 및 차트 그리기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/users/monthly")
    .then((res) => res.json())
    .then((data) => {
      // data: [{ year: 2025, month: 4, signUpCount: 42 }, ...]
      const labels = data.map((item) => {
        const mm = item.month.toString().padStart(2, "0");
        return `${item.year}-${mm}`;
      });
      const counts = data.map((item) => item.signUpCount);

      new Chart(document.getElementById("userSignUpChart"), {
        type: "line",
        data: {
          labels: labels,
          datasets: [
            {
              label: "가입자 수",
              data: counts,
              borderWidth: 2,
              fill: false,
            },
          ],
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              stepSize: 1,
            },
          },
        },
      });
    });

  // ─────────────────────────────────────────────────────────────────
  // [6] 신고 Top 10 유저 데이터 가져오기 및 테이블 채우기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/reports/top-users")
    .then((res) => res.json())
    .then((data) => {
      // data: [{ userId: 5, nickname: "alice", reportCount: 10 }, ...]
      const tbody = document.getElementById("topReportedUsers");
      tbody.innerHTML = ""; // 초기화

      data.forEach((item, index) => {
        const tr = document.createElement("tr");

        // 순위
        const rankTd = document.createElement("td");
        rankTd.textContent = index + 1;
        tr.appendChild(rankTd);

        // 유저 ID
        const idTd = document.createElement("td");
        idTd.textContent = item.userId;
        tr.appendChild(idTd);

        // 닉네임
        const nameTd = document.createElement("td");
        nameTd.textContent = item.nickname;
        tr.appendChild(nameTd);

        // 신고 건수
        const countTd = document.createElement("td");
        countTd.textContent = item.reportCount;
        tr.appendChild(countTd);

        tbody.appendChild(tr);
      });
    });

  // ─────────────────────────────────────────────────────────────────
  // [7] 기여 Top 10 유저 데이터 가져오기 및 테이블 채우기
  // ─────────────────────────────────────────────────────────────────
  fetch("/api/admin/stats/users/top")
    .then((res) => res.json())
    .then((data) => {
      // data: [{ userId: 8, nickname: "bob", logCount: 42 }, ...]
      const tbody = document.getElementById("topContributingUsers");
      tbody.innerHTML = "";

      data.forEach((item, index) => {
        const tr = document.createElement("tr");

        // 순위
        const rankTd = document.createElement("td");
        rankTd.textContent = index + 1;
        tr.appendChild(rankTd);

        // 유저 ID
        const idTd = document.createElement("td");
        idTd.textContent = item.userId;
        tr.appendChild(idTd);

        // 닉네임
        const nameTd = document.createElement("td");
        nameTd.textContent = item.nickname;
        tr.appendChild(nameTd);

        // 작성 로그 수
        const countTd = document.createElement("td");
        countTd.textContent = item.logCount;
        tr.appendChild(countTd);

        tbody.appendChild(tr);
      });
    });

  // ─────────────────────────────────────────────────────────────────
  // [8] “포인트 통계” 탭이 활성화될 때만 API 호출
  // ─────────────────────────────────────────────────────────────────
  const pointTabEl = document.querySelector("#point-stats-tab");
  pointTabEl.addEventListener("shown.bs.tab", () => {
    // 8-1. 일별 포인트 흐름 차트
    fetch("/api/admin/points/daily-aggregate")
      .then((res) => res.json())
      .then((data) => {
        const labels = data.map((item) => item.date); // "2025-05-01" 형태
        const sums = data.map((item) => item.sumAmount); // ex) 120, -30, ...
        new Chart(document.getElementById("pointFlowChart"), {
          type: "line",
          data: {
            labels,
            datasets: [
              {
                label: "일별 포인트 증감 합계",
                data: sums,
                borderWidth: 2,
                fill: false,
              },
            ],
          },
          options: {
            responsive: true,
            scales: { y: { beginAtZero: false } },
          },
        });
      });

    // 8-2. 최근 포인트 내역 테이블 (최신 10건)
    fetch("/api/admin/points/recent?limit=10")
      .then((res) => res.json())
      .then((data) => {
        const tbody = document.getElementById("recentPointHistory");
        tbody.innerHTML = "";
        data.forEach((item) => {
          const tr = document.createElement("tr");
          tr.innerHTML = `
            <td>${item.earnedAt.replace("T", " ")}</td>
            <td>${item.userId}</td>
            <td>${item.amount}</td>
            <td>${item.reason}</td>
            <td>${item.type}</td>
          `;
          tbody.appendChild(tr);
        });
      });
  });

  // 8-3. 포인트 잔액 분포도
  fetch("/api/admin/points/distribution")
    .then((res) => res.json())
    .then((data) => {
      // 백엔드에서 negativeCount는 더이상 반환하지 않으니 제거
      const labels = ["잔액 > 0", "잔액 = 0"];
      const counts = [data.positiveCount, data.zeroCount];

      new Chart(document.getElementById("pointDistributionChart"), {
        type: "pie",
        data: {
          labels,
          datasets: [
            {
              data: counts,
              borderWidth: 1,
            },
          ],
        },
        options: { responsive: true },
      });
    });

  // 8-4. 잔액 높은 Top N 사용자
  fetch("/api/admin/points/top?limit=10")
    .then((res) => res.json())
    .then((data) => {
      // data: [{ userId, nickname, points }, …]
      const tbody = document.getElementById("topPointUsers");
      tbody.innerHTML = "";
      data.forEach((u, i) => {
        tbody.insertAdjacentHTML(
          "beforeend",
          `
          <tr>
            <td>${i + 1}</td>
            <td>${u.userId}</td>
            <td>${u.nickname}</td>
            <td>${u.points}</td>
          </tr>
        `
        );
      });
    });

  // 8-5. 잔액 이 0원인 비율
  fetch("/api/admin/points/zero/ratio")
    .then((res) => res.json())
    .then((data) => {
      const pct = (data.zeroRatio * 100).toFixed(1) + "%";
      document.getElementById(
        "zeroBalanceRatio"
      ).textContent = `${data.zeroCount} / ${data.totalCount} (${pct})`;
    });
});
