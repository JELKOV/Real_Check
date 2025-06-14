let map;
let markers = [];
let radiusCircle = null;
let currentPage = 1;
let isLoading = false;
let hasMore = true;
let searchCenter = null;
let activeInfoWindow = null;

// 초기화
$(document).ready(function () {
  initMap();
  initEventHandlers();

  $("#myLocationButton").on("click", function () {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function (pos) {
        const latlng = new naver.maps.LatLng(
          pos.coords.latitude,
          pos.coords.longitude
        );
        map.setCenter(latlng);
        drawRadiusCircle(latlng, parseInt($("#radiusFilter").val()));
        resetAndLoad();
      });
    } else {
      alert("위치 정보를 지원하지 않는 브라우저입니다.");
    }
  });

  $("#refreshNearbyButton").on("click", function () {
    resetAndLoad();
  });
});

// 카테고리 라벨 매핑
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

// 네이버 지도 초기화
function initMap() {
  map = new naver.maps.Map("map", {
    center: new naver.maps.LatLng(37.5665, 126.978),
    zoom: 13,
  });

  // 지도 초기 중심 설정
  searchCenter = map.getCenter();
  loadRequests();

  // 지도 드래그 종료 시 새로운 위치로 요청 로드
  naver.maps.Event.addListener(map, "dragend", function () {
    searchCenter = map.getCenter();
    resetAndLoad();
  });
}

// 각종 이벤트 핸들러 초기화
function initEventHandlers() {
  // 카테고리 필터 및 지도 필터 변경 시 다시 로드
  $("#radiusFilter, #categoryFilter").on("change", resetAndLoad);

  // 위치 검색 버튼
  $("#searchLocationBtn").on("click", searchLocation);
  $("#locationInput").on("keydown", function (e) {
    if (e.key === "Enter") searchLocation();
  });

  // 무한 스크롤 이벤트
  $(window).on("scroll", function () {
    if (
      $(window).scrollTop() + $(window).height() >=
      $(document).height() - 100
    ) {
      if (!isLoading && hasMore) loadRequests();
    }
  });
}

// 위치 검색
function searchLocation() {
  const query = $("#locationInput").val();
  if (!query) return alert("검색어를 입력하세요.");

  // 서버를 통한 위치 검색 (도로명 주소 검색) - 네이버 검색 API
  $.ajax({
    url: "/api/naver/search",
    type: "GET",
    data: { query },
    success: function (response) {
      const parsed =
        typeof response === "string" ? JSON.parse(response) : response;
      const items = parsed.items;

      if (items.length === 0) {
        alert("검색 결과가 없습니다.");
        return;
      }

      // 검색 결과 목록 모달에 표시
      $("#searchResultList").empty();
      items.forEach((item, index) => {
        $("#searchResultList").append(`
            <li class="list-group-item" data-index="${index}">
              <strong>${item.title}</strong><br>
              <small>${item.roadAddress}</small>
            </li>
          `);
      });

      // 모달 열기
      $("#searchResultModal").modal("show");

      // 모달에서 도로명 주소 선택 시 지도 이동
      $("#searchResultList")
        .off("click")
        .on("click", "li", function () {
          const index = $(this).data("index");
          const selected = items[index];
          const roadAddress = selected.roadAddress;

          if (!roadAddress) {
            alert("도로명 주소가 없습니다.");
            return;
          }

          searchByRoadAddress(roadAddress);
          $("#searchResultModal").modal("hide");
        });
    },
    error: function () {
      alert("검색 API 요청 중 오류가 발생했습니다.");
    },
  });
}

// 도로명 주소로 지도 검색 (geocode 사용)
function searchByRoadAddress(roadAddress) {
  naver.maps.Service.geocode(
    { query: roadAddress },
    function (status, response) {
      if (status !== naver.maps.Service.Status.OK) {
        alert("도로명 주소 검색 실패");
        return;
      }

      const result = response.v2.addresses[0];
      if (!result) {
        alert("해당 도로명 주소로 검색된 좌표가 없습니다.");
        return;
      }

      // 지도 중심 이동
      const { y: lat, x: lng } = result;

      searchCenter = new naver.maps.LatLng(parseFloat(lat), parseFloat(lng));
      map.setCenter(searchCenter);
      resetAndLoad();
    }
  );
}

// 요청 로드 (무한 스크롤 + 지도 이동)
function loadRequests() {
  if (isLoading) return;
  isLoading = true;
  $("#loadingIndicator").show();

  // 필터 파라미터 설정
  const category = $("#categoryFilter").val() || null;
  const radius = parseInt($("#radiusFilter").val(), 10);
  const centerLat = searchCenter.lat();
  const centerLng = searchCenter.lng();

  drawRadiusCircle(searchCenter, radius);

  // API 호출하여 요청 목록 로드
  $.get(
    "/api/request/open",
    {
      page: currentPage,
      size: 10,
      lat: centerLat,
      lng: centerLng,
      radius: radius,
      category: category,
    },
    function (data) {
      if (currentPage === 1) {
        clearMarkers();
        $("#requestList").empty();
      }

      renderRequests(data);
      currentPage++;

      hasMore = data.length === 10;
      isLoading = false;
      $("#loadingIndicator").hide();
    }
  ).fail(() => {
    isLoading = false;
    $("#loadingIndicator").hide();
    alert("요청 로드 중 오류");
  });
}

// 요청 목록 렌더링
function renderRequests(data) {
  const container = $("#requestList");

  data.forEach(function (req) {
    if ($(`[data-request-id="${req.id}"]`).length > 0) return;

    // 카테고리 벳지 렌더링
    const badge = req.category
      ? `<span class="badge bg-secondary mb-2"> ${
          categoryLabelMap[req.category] || req.category
        } </span>`
      : "";

    // 카드 렌더링
    const cardHtml = `
        <div class="col-md-6" data-request-id="${req.id}">
          <div class="card h-100 shadow-sm request-card" data-request-id="${
            req.id
          }">
            <div class="card-body">
              ${badge}
              <h5 class="card-title">${req.title}</h5>
              <p class="card-text">${(req.content || "").substring(
                0,
                30
              )}...</p>
              <small>포인트: ${req.point}</small><br/>
              <a href="/request/${
                req.id
              }" class="btn btn-outline-primary btn-sm mt-2 view-detail">상세보기</a>
            </div>
          </div>
        </div>
      `;

    container.append(cardHtml);

    // 지도 마커 생성
    if (req.lat && req.lng) {
      const marker = new naver.maps.Marker({
        map: map,
        position: new naver.maps.LatLng(req.lat, req.lng),
      });

      // 마커 클릭 시 정보창
      const infoWindow = new naver.maps.InfoWindow({
        content: `
            <div style="padding: 10px; max-width: 200px;">
              <strong>${req.title}</strong><br/>
              ${req.content}<br/>
              <small>${categoryLabelMap[req.category] || req.category}</small>
            </div>
          `,
        maxWidth: 200,
      });

      // 마커 클릭 핸들러
      naver.maps.Event.addListener(marker, "click", function () {
        if (activeInfoWindow) activeInfoWindow.close();
        infoWindow.open(map, marker);
        activeInfoWindow = infoWindow;
        map.panTo(marker.getPosition());
      });

      // 마커 저장 (req.id 기준)
      markers.push({ id: req.id, marker, infoWindow });
    }
  });

  // 카드 클릭 시 마커 이동
  $(".request-card")
    .off("click")
    .on("click", function () {
      const requestId = $(this).data("request-id");
      const markerInfo = markers.find((m) => m.id === requestId);
      if (markerInfo) {
        map.panTo(markerInfo.marker.getPosition());
        if (activeInfoWindow) activeInfoWindow.close();
        markerInfo.infoWindow.open(map, markerInfo.marker);
        activeInfoWindow = markerInfo.infoWindow;
      }
    });
}
// 지도 드래그 / 검색 / 필터 변경 시 데이터 초기화 + 로드
function resetAndLoad() {
  searchCenter = map.getCenter(); // 항상 최신 좌표로 설정
  console.log("resetAndLoad() - 현재 중심 좌표:", searchCenter);

  currentPage = 1;
  hasMore = true;
  clearMarkers();
  loadRequests();
}

// 마커 초기화
function clearMarkers() {
  markers.forEach((m) => m.marker.setMap(null));
  markers = [];
  if (activeInfoWindow) activeInfoWindow.close();
}

// 거리 계산 함수
function getDistance(lat1, lng1, lat2, lng2) {
  const R = 6371e3;
  const toRad = (deg) => deg * (Math.PI / 180);
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
}

// 반경 원 그리기
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
