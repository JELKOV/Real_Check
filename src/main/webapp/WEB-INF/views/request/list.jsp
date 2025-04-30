<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>요청 목록 - RealCheck</title>
  <link rel="stylesheet" href="/css/style.css" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<%@ include file="../common/header.jsp" %>

<div class="container mt-5">
  <h3 class="text-center mb-4">전체 요청 목록</h3>
  <div id="requestList" class="row g-3"></div>
</div>

<script>
$(function () {
  $.get("/api/request/open", function (data) {
    var html = "";

    data.forEach(function (req) {
      html +=
        '<div class="col-md-6">' +
          '<div class="card h-100 shadow-sm">' +
            '<div class="card-body">' +
              '<h5 class="card-title">' + req.title + '</h5>' +
              '<p class="card-text">' + req.content + '</p>' +
              '<small>포인트: ' + req.point + '</small><br/>' +
              '<a href="/request/' + req.id + '" class="btn btn-outline-primary btn-sm mt-2">상세보기</a>' +
            '</div>' +
          '</div>' +
        '</div>';
    });

    $("#requestList").html(html);
  });
});
</script>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
