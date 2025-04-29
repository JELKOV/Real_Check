<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>요청 등록 - RealCheck</title>
  <link rel="stylesheet" href="/css/style.css" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <!-- 카카오 지도 API 필요 시 추가 -->
</head>
<body>
<%@ include file="../common/header.jsp" %>

<div class="container mt-5" style="max-width: 700px;">
  <h3 class="text-center mb-4">요청 등록</h3>

  <!-- 지도 (선택된 위치 lat/lng 보여주기) -->
  <div id="map" style="width: 100%; height: 400px; margin-bottom: 20px; border: 1px solid #ccc;"></div>

  <form id="requestForm">
    <div class="mb-3">
      <label for="title" class="form-label">요청 제목</label>
      <input type="text" class="form-control" id="title" name="title" required placeholder="예: 붕어빵 가게 열었나요?" />
    </div>

    <div class="mb-3">
      <label for="content" class="form-label">요청 내용</label>
      <textarea class="form-control" id="content" name="content" rows="3" required></textarea>
    </div>

    <div class="mb-3">
      <label for="point" class="form-label">포인트 설정</label>
      <input type="number" class="form-control" id="point" name="point" min="1" value="10" required />
    </div>

    <div class="mb-3">
      <label class="form-label">선택된 위치</label>
      <input type="text" class="form-control" id="placeId" name="placeId" readonly required />
      <input type="hidden" id="lat" name="lat" />
      <input type="hidden" id="lng" name="lng" />
    </div>

    <button type="submit" class="btn btn-primary w-100">요청 등록</button>
  </form>
</div>

<%@ include file="../common/footer.jsp" %>

<script>
$(document).ready(function () {
  // [TODO] 지도 클릭 시 placeId, lat, lng 설정하기
  $("#map").text("여기 지도 연동 예정");

  // 요청 등록 처리
  $("#requestForm").on("submit", function (e) {
    e.preventDefault();

    const requestData = {
      title: $("#title").val(),
      content: $("#content").val(),
      point: parseInt($("#point").val()),
      placeId: $("#placeId").val(),
      lat: parseFloat($("#lat").val()),
      lng: parseFloat($("#lng").val())
    };

    $.ajax({
      url: "/api/request",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(requestData),
      success: function () {
        alert("요청이 등록되었습니다!");
        location.href = "/request/list";
      },
      error: function (xhr) {
        alert("등록 실패: " + xhr.responseText);
      }
    });
  });
});
</script>
</body>
</html>
