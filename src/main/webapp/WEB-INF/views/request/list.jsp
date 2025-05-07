<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 목록 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=fyljbu3cv5&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">전체 요청 목록</h3>

      <!-- 지도 표시 -->
      <div id="map" style="height: 300px" class="mb-3"></div>

      <!-- 필터 영역 -->
      <div class="mb-3 text-center">
        <select
          id="radiusFilter"
          class="form-select w-auto d-inline-block me-2"
        >
          <option value="1000">1km 이내(검색 위치 기준)</option>
          <option value="2000">2km 이내(검색 위치 기준)</option>
          <option value="3000" selected>3km 이내(검색 위치 기준)</option>
          <option value="5000">5km 이내(검색 위치 기준)</option>
          <option value="10000">10km 이내(검색 위치 기준)</option>
        </select>

        <select id="categoryFilter" class="form-select w-auto d-inline-block">
          <option value="">전체 카테고리</option>
          <option value="PARKING">🅿️ 주차 가능 여부</option>
          <option value="WAITING_STATUS">⏳ 대기 상태</option>
          <option value="STREET_VENDOR">🥟 노점 현황</option>
          <option value="PHOTO_REQUEST">📸 사진 요청</option>
          <option value="BUSINESS_STATUS">🏪 영업 여부</option>
          <option value="OPEN_SEAT">💺 좌석 여유</option>
          <option value="BATHROOM">🚻 화장실 여부</option>
          <option value="WEATHER_LOCAL">☁️ 현장 날씨</option>
          <option value="NOISE_LEVEL">🔊 소음 상태</option>
          <option value="FOOD_MENU">🍔 음식/메뉴</option>
          <option value="CROWD_LEVEL">👥 혼잡도</option>
          <option value="ETC">❓ 기타</option>
        </select>
      </div>

      <!-- 위치 검색 -->
      <div class="mb-3 text-center">
        <input
          type="text"
          id="locationInput"
          class="form-control w-50 d-inline-block me-2"
          placeholder="예: 강남역, 서울시청 등"
        />
        <button id="searchLocationBtn" class="btn btn-primary">
          위치 검색
        </button>
      </div>

      <div id="requestList" class="row g-3"></div>
    </div>

    <script>
      const categoryLabelMap = {
        PARKING: "🅿️ 주차",
        WAITING_STATUS: "⏳ 대기",
        STREET_VENDOR: "🥟 노점",
        PHOTO_REQUEST: "📸 사진",
        BUSINESS_STATUS: "🏪 영업",
        OPEN_SEAT: "💺 좌석",
        BATHROOM: "🚻 화장실",
        WEATHER_LOCAL: "☁️ 날씨",
        NOISE_LEVEL: "🔊 소음",
        FOOD_MENU: "🍔 메뉴",
        CROWD_LEVEL: "👥 혼잡",
        ETC: "❓ 기타",
      };

      let map;
      let markers = [];
      let radiusCircle = null;

      function getDistance(lat1, lng1, lat2, lng2) {
        const R = 6371e3;
        const toRad = (deg) => deg * (Math.PI / 180);
        const dLat = toRad(lat2 - lat1);
        const dLng = toRad(lng2 - lng1);
        const a =
          Math.sin(dLat / 2) ** 2 +
          Math.cos(toRad(lat1)) *
            Math.cos(toRad(lat2)) *
            Math.sin(dLng / 2) ** 2;
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
      }

      function renderRequests(data) {
        const container = $("#requestList");
        container.empty();

        // 마커 및 인포윈도우 초기화
        markers.forEach((m) => m.setMap(null));
        markers = [];

        if (data.length === 0) {
          container.html(
            '<div class="text-center text-muted">요청이 없습니다.</div>'
          );
          return;
        }

        data.forEach(function (req, index) {
          const badge = req.category
            ? `<span class="badge bg-secondary mb-2"> ${"${categoryLabelMap[req.category] || req.category}"} </span>`
            : "";

          const cardHtml = `
            <div class="col-md-6">
              <div class="card h-100 shadow-sm request-card" data-marker-index="${"${index}"}">
                <div class="card-body">
                  ${"${badge}"}
                  <h5 class="card-title">${"${req.title}"}</h5>
                  <p class="card-text">${"${(req.content || '').substring(0, 30)}"}...</p>
                  <small>포인트: ${"${req.point}"}</small><br/>
                  <a href="#" class="btn btn-outline-primary btn-sm mt-2 view-detail" data-id=${"${req.id}"}>상세보기</a>
                </div>
              </div>
            </div>
          `;

          container.append(cardHtml);

          // 마커 생성
          if (req.lat && req.lng) {
            const marker = new naver.maps.Marker({
              map: map,
              position: new naver.maps.LatLng(req.lat, req.lng),
            });

            const infoWindow = new naver.maps.InfoWindow({
              content: `
              <div style="padding: 5px; max-width: 200px;">
                <strong>${"${req.title}"}</strong><br/>
                ${"${req.content}"}<br/>
                <small>${"${categoryLabelMap[req.category] || req.category}"}</small>
              </div>`,
            });

            naver.maps.Event.addListener(marker, "click", function () {
              infoWindow.open(map, marker);
            });

            markers.push(marker);
          }
        });
        // 카드 클릭 시 해당 마커 중심 이동 + 인포윈도우 열기
        $(".request-card").on("click", function () {
          const idx = $(this).data("marker-index");
          const marker = markers[idx];
          if (marker) {
            map.panTo(marker.getPosition());
            new naver.maps.InfoWindow({
              content: "<div style='padding:5px;'>마커 정보 보기</div>",
            }).open(map, marker);
          }
        });
      }

      function drawRadiusCircle(center, radius) {
        if (radiusCircle) radiusCircle.setMap(null);
        radiusCircle = new naver.maps.Circle({
          map: map,
          center: center,
          radius: radius,
          strokeColor: "#007BFF",
          strokeOpacity: 0.6,
          strokeWeight: 2,
          fillColor: "#007BFF",
          fillOpacity: 0.15,
        });
      }

      function loadRequests() {
        const category = $("#categoryFilter").val();
        const radius = parseInt($("#radiusFilter").val(), 10);
        const center = map.getCenter();
        const centerLat = center.lat();
        const centerLng = center.lng();

        // 지도 반경 원 그리기
        drawRadiusCircle(center, radius);

        $.get("/api/request/open", function (data) {
          let filtered = data.filter((req) => {
            if (!req.lat || !req.lng) return false;
            if (category && req.category !== category) return false;

            const dist = getDistance(centerLat, centerLng, req.lat, req.lng);
            return dist <= radius;
          });

          renderRequests(filtered);
        });
      }

      $(function () {
        map = new naver.maps.Map("map", {
          center: new naver.maps.LatLng(37.5665, 126.978), // 서울
          zoom: 13,
        });

        loadRequests();

        $("#categoryFilter, #radiusFilter").on("change", loadRequests);
        naver.maps.Event.addListener(map, "dragend", loadRequests);

        $(document).on("click", ".view-detail", function (e) {
          e.preventDefault();
          const id = $(this).data("id");
          window.location.href = `/request/${"${id}"}`;
        });

        $("#locationInput").on("keydown", function (e) {
          if (e.key === "Enter") {
            e.preventDefault();
            $("#searchLocationBtn").click();
          }
        });

        $("#searchLocationBtn").on("click", function () {
          console.log("위치 검색 버튼 클릭됨");
          const query = $("#locationInput").val();
          if (!query) return;

          naver.maps.Service.geocode({ query }, function (status, response) {
            console.log("🛰️ geocode status:", status, response);

            if (status !== naver.maps.Service.Status.OK) {
              alert("위치 검색 실패");
              return;
            }

            const result = response.v2.addresses[0];
            const lat = parseFloat(result.y);
            const lng = parseFloat(result.x);
            const newCenter = new naver.maps.LatLng(lat, lng);

            map.setCenter(newCenter);
            loadRequests();
          });
        });
      });
    </script>
    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
