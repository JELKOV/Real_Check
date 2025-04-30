<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>요청 지도 보기 - RealCheck</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <!-- 카카오 지도 SDK (JS Key 필수) -->
  <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_KAKAO_API_KEY"></script>
</head>
<body>
<%@ include file="../common/header.jsp" %>

<div class="container mt-4">
  <h3 class="text-center mb-4">🧭 지도에서 요청 보기</h3>
  <div id="map" style="width: 100%; height: 500px; border: 1px solid #ccc;"></div>
</div>

<%@ include file="../common/footer.jsp" %>

<script>
$(function () {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (position) {
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;

      const mapContainer = document.getElementById("map");
      const mapOption = {
        center: new kakao.maps.LatLng(lat, lng),
        level: 4,
      };

      const map = new kakao.maps.Map(mapContainer, mapOption);

      // 현재 위치 마커
      const myMarker = new kakao.maps.Marker({
        map: map,
        position: new kakao.maps.LatLng(lat, lng),
      });

      // 서버에서 요청 목록 가져오기
      $.get("/api/request/open", function (requests) {
        requests.forEach(req => {
          if (!req.lat || !req.lng) return;

          const marker = new kakao.maps.Marker({
            map: map,
            position: new kakao.maps.LatLng(req.lat, req.lng),
          });

          const content = `
            <div style="padding:8px; font-size:13px;">
              <strong>${"${req.title}"}</strong><br/>
              포인트: ${"${req.point}"}pt<br/>
              <a href="/request/${"${req.id}"}" class="btn btn-sm btn-primary mt-1">자세히 보기</a>
            </div>
          `;

          const infowindow = new kakao.maps.InfoWindow({
            content: content
          });

          kakao.maps.event.addListener(marker, 'click', function () {
            infowindow.open(map, marker);
          });
        });
      });
    });
  } else {
    alert("위치 정보를 불러올 수 없습니다.");
  }
});
</script>
</body>
</html>
