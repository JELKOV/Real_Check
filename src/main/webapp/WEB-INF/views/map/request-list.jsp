<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>지도 요청 탐색 - RealCheck</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
<%@ include file="../common/header.jsp" %>
<div class="container mt-4">
  <h3 class="text-center mb-4">🗺️ 지도에서 요청 확인 & 답변</h3>
  <div id="map" style="width: 100%; height: 500px; border: 1px solid #ccc;"></div>
</div>
<script>
// TODO: 지도 연동 + 마커 클릭 시 /request/{id} 이동 구현 예정
</script>
<%@ include file="../common/footer.jsp" %>
</body>
</html>