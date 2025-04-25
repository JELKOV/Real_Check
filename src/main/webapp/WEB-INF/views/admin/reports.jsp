<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>신고 관리 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">🚨 신고 관리</h3>

      <table class="table table-bordered">
        <thead>
          <tr>
            <th>신고자</th>
            <th>로그 ID</th>
            <th>사유</th>
            <th>날짜</th>
            <th>기능</th>
          </tr>
        </thead>
        <tbody id="reportTableBody"></tbody>
      </table>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      function loadReports() {
        $.get("/api/admin/report/all", function (reports) {
          const rows = reports.map(
            (report) => `
      <tr>
        <td>${'${report.reporterEmail}'}</td>
        <td>${'${report.statusLogId}'}</td>
        <td>${'${report.reason}'}</td>
        <td>${'${report.createdAt}'}</td>
        <td>
          <button class="btn btn-sm btn-info" onclick="getReportCount(${'${report.statusLogId}'})">신고 수</button>
        </td>
      </tr>
    `
          );
          $("#reportTableBody").html(rows.join(""));
        });
      }

      function getReportCount(statusLogId) {
        $.get(
          `/api/admin/report/count?statusLogId=${'${statusLogId}'}`,
          function (data) {
            alert(`이 상태 로그에 대한 신고 수: ${'${data.count}'}`);
          }
        );
      }

      $(document).ready(() => {
        loadReports();
      });
    </script>
  </body>
</html>
