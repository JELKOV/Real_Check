<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>내 요청 목록 - RealCheck</title>
  <link rel="stylesheet" href="/css/style.css" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>

<%@ include file="../common/header.jsp" %>

<div class="container mt-5">
  <h3 class="mb-4">내 요청 목록</h3>

  <div id="requestTableContainer"></div>
</div>

<script>
  $(function () {
    $.get("/api/request/my", function (data) {
      if (data.length === 0) {
        $("#requestTableContainer").html(`
          <div class="alert alert-secondary">
            등록한 요청이 없습니다.
          </div>
        `);
        return;
      }

      let html = `
        <table class="table table-bordered align-middle text-center">
          <thead class="table-light">
            <tr>
              <th>제목</th>
              <th>내용</th>
              <th>답변 수</th>
              <th>상태</th>
              <th>등록일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
      `;

      data.forEach(function (req) {
        const status = req.closed ? "✅ 마감됨" : "🟢 진행 중";
        const isAlertNeeded = !req.closed && req.answerCount === 3;
        const alertText = isAlertNeeded ? `<div class='text-info small'>채택해주세요</div>` : "";
        const formattedDate = new Date(req.createdAt).toLocaleString();

        html += `
          <tr>
            <td>${"${req.title}"}</td>
            <td>${"${req.content}"}</td>
            <td>${"${req.answerCount}"}</td>
            <td>${"${status}${alertText}"}</td>
            <td>${"${formattedDate}"}</td>
            <td>
              <a href="#" class="btn btn-outline-primary btn-sm mt-2 view-detail" data-id="${"${req.id}"}">상세보기</a>
            </td>
          </tr>
        `;
      });

      html += `</tbody></table>`;
      $("#requestTableContainer").html(html);
    });

    $(document).on("click", ".view-detail", function (e) {
        e.preventDefault();
        const id = $(this).data("id");
        window.location.href = `/request/${"${id}"}`;
      });
  });
</script>

<%@ include file="../common/footer.jsp" %>

</body>
</html>
