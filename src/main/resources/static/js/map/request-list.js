let map = null;
let userCircle = null;
let requestMarkers = [];
let markerMap = {};
let openedInfoWindow = null;

// 지도 초기화
function loadMap(lat, lng) {
  const center = new naver.maps.LatLng(lat, lng);

  if (!map) {
    map = new naver.maps.Map("map", {
      center: center,
      zoom: 14,
      draggable: false,
      pinchZoom: false,
      scrollWheel: false,
      disableDoubleClickZoom: true,
    });
  } else {
    map.setCenter(center);
  }

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
}

// 요청 데이터를 위치 기반으로 묶기
function groupRequestsByLocation(requests) {
  const grouped = {};

  requests.forEach((req) => {
    const key = `${req.lat.toFixed(5)}_${req.lng.toFixed(5)}`;
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(req);
  });

  return grouped;
}

// 요청 마커 렌더링
function renderMarkers(grouped) {
  // 마커 초기화
  requestMarkers.forEach((m) => m.setMap(null));
  requestMarkers = [];
  markerMap = {};

  for (const key in grouped) {
    const group = grouped[key];
    const { lat, lng } = group[0];
    const position = new naver.maps.LatLng(lat, lng);

    const marker = new naver.maps.Marker({
      position,
      map,
    });

    const content = group
      .map(
        (req) => `
        <div class="custom-infowindow">
        <strong>${req.title}</strong><br/>
        포인트: ${req.point}pt / 응답: ${req.visibleAnswerCount}/3<br/>
        <a href="/request/${req.id}" class="btn btn-sm btn-primary mt-2">상세 보기</a>
        </div>`
      )
      .join("<hr style='margin:6px 0;'/>");

    const infoWindow = new naver.maps.InfoWindow({ content });

    naver.maps.Event.addListener(marker, "click", function () {
      if (openedInfoWindow) openedInfoWindow.close();
      infoWindow.open(map, marker);
      openedInfoWindow = infoWindow;
    });

    requestMarkers.push(marker);
    markerMap[key] = { marker, infoWindow };
  }
}

// 사이드바 렌더링
function renderSidebar(grouped) {
  const $sidebar = $("#sidebarRequests");
  $sidebar.empty();

  for (const key in grouped) {
    const group = grouped[key];
    const { lat, lng } = group[0];

    const html = group
      .map(
        (req) => `
      <div class="mb-2">
        <div class="fw-bold">${req.title}</div>
        <div class="text-muted small">응답: ${req.visibleAnswerCount}/3 / 포인트: ${req.point}</div>
        <a href="/request/${req.id}" target="_blank" class="btn btn-sm btn-outline-secondary mt-1">자세히 보기</a>
      </div>`
      )
      .join("<hr style='margin:6px 0;'/>");

    $sidebar.append(`
      <div class="sidebar-card mb-3 p-2 border rounded" style="cursor:pointer" onclick="focusMarker('${key}')">
        ${html}
      </div>
    `);
  }
}

// 마커 포커스 (사이드바 클릭 시)
function focusMarker(key) {
  const data = markerMap[key];
  if (!data) return;
  map.panTo(data.marker.getPosition());
  if (openedInfoWindow) openedInfoWindow.close();
  data.infoWindow.open(map, data.marker);
  openedInfoWindow = data.infoWindow;
}

// 요청 데이터 로딩
function loadRequests(lat, lng) {
  loadMap(lat, lng);

  $.get(`/api/request/nearby?lat=${lat}&lng=${lng}&radius=3000`, (requests) => {
    if (!requests || requests.length === 0) {
      $("#sidebarRequests").html(`
        <div class="alert alert-info text-center">
          근처에 요청이 없습니다.
        </div>
      `);
      return;
    }

    const grouped = groupRequestsByLocation(requests);
    renderMarkers(grouped);
    renderSidebar(grouped);
  });
}

// 사용자 위치 확인
function getUserLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        loadRequests(pos.coords.latitude, pos.coords.longitude);
      },
      () => alert("위치 정보를 가져올 수 없습니다."),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  } else {
    alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
  }
}

// 버튼 이벤트 바인딩
$(document).ready(function () {
  getUserLocation();

  $("#myLocationButton").on("click", getUserLocation);

  $("#refreshNearbyButton").on("click", function () {
    if (map) {
      const center = map.getCenter();
      loadRequests(center.lat(), center.lng());
    }
  });
});
