<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>통계 보기 - 관리자</title>
  <link rel="stylesheet" href="/css/style.css" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<%@ include file="../common/header.jsp" %>

<div class="container mt-5">
  <h3 class="text-center fw-bold mb-4">📊 관리자 통계</h3>

  <div class="row text-center mb-4">
    <div class="col-md-4">
      <div class="border rounded p-3 shadow-sm">
        <h5>총 신고 수</h5>
        <p class="fs-4" id="reportCount">-</p>
      </div>
    </div>
    <div class="col-md-4">
      <div class="border rounded p-3 shadow-sm">
        <h5>포인트 총합</h5>
        <p class="fs-4" id="pointTotal">-</p>
      </div>
    </div>
  </div>

  <div class="mt-5">
    <h5 class="text-center">📈 월별 등록 통계</h5>
    <canvas id="logChart" height="100"></canvas>
  </div>
</div>

<%@ include file="../common/footer.jsp" %>

<script>
document.addEventListener("DOMContentLoaded", () => {
  // [1] 총 신고 수 가져오기
  fetch("/api/admin/stats/reports")
    .then(res => res.json())
    .then(data => {
      document.getElementById("reportCount").textContent = data.reportCount;
    });

  // [2] 포인트 총합 가져오기
  fetch("/api/admin/stats/points")
    .then(res => res.json())
    .then(data => {
      document.getElementById("pointTotal").textContent = data.pointTotal;
    });

  // [3] 월별 상태 로그 수 그래프 그리기
  fetch("/api/admin/stats/logs/monthly")
    .then(res => res.json())
    .then(data => {
      const labels = data.map(item => `${item.year}-${item.month}`);
      const counts = data.map(item => item.count);

      new Chart(document.getElementById("logChart"), {
        type: "bar",
        data: {
          labels: labels,
          datasets: [{
            label: "등록 수",
            data: counts,
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              stepSize: 1
            }
          }
        }
      });
    });
});
</script>

</body>
</html>
