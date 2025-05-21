<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 지도 보기 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link href="/css/map.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-4">
      <h3 class="text-center mb-4">지도에서 요청 보기</h3>
      <div id="map" class="map-container position-relative">
        <div class="map-control-button" id="myLocationButton" title="내 위치">
          📍
        </div>
        <div
          class="map-control-button"
          id="refreshNearbyButton"
          title="요청 조회"
        >
          🔄
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      let map = null;
      let userCircle = null;
      let requestMarkers = [];

      // 요청 마커 로드 함수
      function loadRequestMarkers(lat, lng) {
        const center = new naver.maps.LatLng(lat, lng);

        // 지도 초기화 또는 재중심
        if (!map) {
          map = new naver.maps.Map("map", {
            center: center,
            zoom: 14,
          });
        } else {
          map.setCenter(center);
        }

        // 사용자 위치 원형 표시
        if (userCircle) userCircle.setMap(null);
        userCircle = new naver.maps.Circle({
          map: map,
          center: center,
          radius: 3000,
          strokeColor: "#007BFF",
          strokeOpacity: 0.6,
          strokeWeight: 2,
          fillColor: "#007BFF",
          fillOpacity: 0.15,
        });

        // 기존 마커 제거
        requestMarkers.forEach((m) => m.setMap(null));
        requestMarkers = [];

        // 요청 정보 불러오기
        $.get(
          `/api/request/nearby?lat=${"${lat}"}&lng=${"${lng}"}&radius=3000`,
          function (requests) {
            $(".alert").remove();

            if (!requests || requests.length === 0) {
              const message = `
              <div class="text-center mt-3">
                <div class="alert alert-info" role="alert">
                  근처에 등록된 요청이 없습니다.
                </div>
              </div>`;
              $("#map").after(message);
              return;
            }

            // 마커 & InfoWindow 생성
            requests.forEach((req) => {
              if (!req.lat || !req.lng) return;

              const marker = new naver.maps.Marker({
                map: map,
                position: new naver.maps.LatLng(req.lat, req.lng),
              });

              const content = `
              <div style="padding:8px; font-size:13px;">
                <strong>요청 제목: ${"${req.title}"}</strong><br/>
                획득 포인트: ${"${req.point}"}pt<br/>
                현재 답변 수: ${"${req.visibleAnswerCount}"}개/3개<br/>
                <a href="javascript:goToDetail(${"${req.id}"})" class="btn btn-sm btn-primary mt-1">자세히 보기</a>
              </div>
            `;
              const infoWindow = new naver.maps.InfoWindow({
                content: content,
              });

              naver.maps.Event.addListener(marker, "click", function () {
                infoWindow.open(map, marker);
              });

              requestMarkers.push(marker);
            });
          }
        );
      }

      // 상세 페이지 이동 함수
      function goToDetail(id) {
        window.location.href = `/request/${"${id}"}`;
      }

      // 위치 기반 요청 마커 로드
      function getUserLocation() {
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(
            function (position) {
              loadRequestMarkers(
                position.coords.latitude,
                position.coords.longitude
              );
            },
            function () {
              alert("위치 정보를 불러올 수 없습니다.");
            },
            {
              enableHighAccuracy: true,
              timeout: 10000,
              maximumAge: 0,
            }
          );
        } else {
          alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
        }
      }

      // 초기 이벤트 바인딩
      $(document).ready(function () {
        getUserLocation();

        $("#myLocationButton").on("click", function () {
          getUserLocation();
        });

        $("#refreshNearbyButton").on("click", function () {
          if (map) {
            const center = map.getCenter();
            loadRequestMarkers(center.lat(), center.lng());
          }
        });
      });
    </script>
  </body>
</html>
