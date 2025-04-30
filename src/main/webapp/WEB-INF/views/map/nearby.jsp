<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>현장 정보 보기 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link href="/css/map.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=fyljbu3cv5"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-4">
      <h3 class="text-center mb-4">등록된 현장 정보 탐색</h3>
      <div id="statusMap" class="map-container position-relative">
        <div class="map-control-button" id="myLocationButton" title="내 위치">
          📍
        </div>
        <div
          class="map-control-button"
          id="refreshNearbyButton"
          title="주변 정보"
        >
          🔄
        </div>
      </div>
    </div>

    <script>
      let map = null;
      let dataMarkers = [];
      let userCircle = null; // 내 위치 원형 객체

      function loadMapWithPosition(lat, lng) {
        const center = new naver.maps.LatLng(lat, lng);

        if (!map) {
          map = new naver.maps.Map("statusMap", {
            center: center,
            zoom: 14,
          });
        } else {
          map.setCenter(center);
        }

        // 이전 마커 제거
        dataMarkers.forEach((m) => m.setMap(null));
        dataMarkers = [];

        // 이전 원형 제거
        if (userCircle) {
          userCircle.setMap(null);
        }

        userCircle = new naver.maps.Circle({
          map: map,
          center: center,
          radius: 3000, // 미터 단위 (3km)
          strokeColor: "#007BFF",
          strokeOpacity: 0.6,
          strokeWeight: 2,
          fillColor: "#007BFF",
          fillOpacity: 0.15,
        });

        // 주변 현장 로그 가져오기
        $.get(
          `/api/status/nearby?lat=${"${lat}"}&lng=${"${lng}"}`,
          function (logs) {
            $(".alert").remove(); // 이전 메시지 제거

            if (!logs || logs.length === 0) {
              const message = `
              <div class="text-center mt-3">
                <div class="alert alert-info" role="alert">
                  주변에 등록된 현장 정보가 없습니다.
                </div>
              </div>`;
              $("#statusMap").after(message);
              return;
            }

            logs.forEach((log) => {
              const marker = new naver.maps.Marker({
                map: map,
                position: new naver.maps.LatLng(log.lat, log.lng),
              });

              const infoWindow = new naver.maps.InfoWindow({
                content: `
                <div style="padding: 5px; max-width: 200px;">
                  <strong>${"${log.placeId}"}</strong><br/>
                  ${"${log.content}"}<br/>
                  <small>${"${new Date(log.createdAt).toLocaleString()}"}</small>
                </div>`,
              });

              naver.maps.Event.addListener(marker, "click", function () {
                infoWindow.open(map, marker);
              });

              dataMarkers.push(marker);
            });
          }
        );
      }

      function getUserLocation() {
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(
            function (pos) {
              loadMapWithPosition(pos.coords.latitude, pos.coords.longitude);
            },
            function () {
              alert("위치 정보를 가져올 수 없습니다.");
            },
            {
              enableHighAccuracy: true,
              timeout: 10000,
              maximumAge: 0,
            }
          );
        } else {
          alert("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        }
      }

      $(document).ready(function () {
        getUserLocation();

        $("#myLocationButton").on("click", function () {
          getUserLocation();
        });

        $("#refreshNearbyButton").on("click", function () {
          if (map) {
            const center = map.getCenter();
            loadMapWithPosition(center.lat(), center.lng());
          }
        });
      });
    </script>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
