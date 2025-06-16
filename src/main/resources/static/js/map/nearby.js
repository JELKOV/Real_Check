let map = null;
let dataMarkers = [];
let userCircle = null;
let currentLat = null;
let currentLng = null;
let currentFilterMode = "official";
let openedInfoWindows = [];
let markerMap = {}; // 마커 매핑

function loadMap(lat, lng) {
  const center = new naver.maps.LatLng(lat, lng);
  currentLat = lat;
  currentLng = lng;

  if (!map) {
    map = new naver.maps.Map("statusMap", {
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

function clearMarkers() {
  dataMarkers.forEach((m) => m.setMap(null));
  dataMarkers = [];
  markerMap = {};
  openedInfoWindows.forEach((iw) => iw.close());
  openedInfoWindows = [];
}

function loadMapAndSidebar(lat, lng) {
  clearMarkers();
  $("#sidebarGroupedLogs").empty();
  $("#sidebarUserLogs").empty();

  $("#sidebarOfficialContent").hide();
  $("#sidebarUserContent").hide();

  loadMap(lat, lng);

  if (currentFilterMode === "official") {
    $.get(
      `/api/status/nearby/grouped?lat=${lat}&lng=${lng}`,
      function (groups) {
        renderSidebarGrouped(groups);
        renderGroupedMarkers(groups);
      }
    );
  } else {
    $.get(
      `/api/status/nearby/user-locations?lat=${lat}&lng=${lng}`,
      function (logs) {
        renderSidebarUserLogs(logs);
        renderUserMarkers(logs);
      }
    );
  }
}

function renderGroupedMarkers(groups) {
  groups.forEach((group) => {
    if (
      !group.latestRegister ||
      !group.latestRegister.lat ||
      !group.latestRegister.lng
    )
      return;

    const marker = new naver.maps.Marker({
      map: map,
      position: new naver.maps.LatLng(
        group.latestRegister.lat,
        group.latestRegister.lng
      ),
    });

    const infoWindow = new naver.maps.InfoWindow({
      content: `
        <div style="padding: 5px; max-width: 220px;">
          <strong>${group.placeName}</strong><br/>
          <span>📢 ${group.latestRegister.content}</span><br/>
          <small>${new Date(
            group.latestRegister.createdAt
          ).toLocaleString()}</small><br/>
          <span class='text-muted small'>${group.address}</span>
        </div>`,
    });

    naver.maps.Event.addListener(marker, "click", function () {
      openedInfoWindows.forEach((iw) => iw.close());
      infoWindow.open(map, marker);
      openedInfoWindows = [infoWindow];
    });

    dataMarkers.push(marker);
    markerMap[`place-${group.placeId}`] = marker;
  });
}

function renderSidebarGrouped(groups) {
  const $container = $("#sidebarGroupedLogs");
  $("#sidebarOfficialContent").show();
  $container.empty();

  if (groups.length === 0) {
    $container.append(`
      <div class="text-muted small ms-2">주변에 공식 장소 응답이 없습니다.</div>
    `);
    return;
  }

  groups.forEach((group) => {
    const placeId = group.placeId;
    const placeKey = `place-${placeId}`;
    const noticeText = group.latestRegister?.content ?? "공지 없음";

    const noticeHtml = `
        <div class="mb-2">
            <div class="notice-wrapper">
            <span class="notice-marquee">📢 ${noticeText}</span>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-1">
            <div class="fw-bold fs-6">${group.placeName}</div>
            <a href="/place/community/${placeId}" target="_blank" class="btn btn-sm btn-outline-primary">커뮤니티</a>
            </div>
            <div class="text-muted small mb-2">${group.address}</div>
        </div>
        `;

    const answersHtml = group.answerLogs
      .map(
        (ans) => `
      <li class="mb-2 d-flex justify-content-between align-items-center">
        <div>
          <span class="text-dark">🔹 ${ans.content}</span>
          ${
            ans.category
              ? `<span class="badge bg-info text-dark ms-1">${ans.category}</span>`
              : ""
          }
          ${
            ans.requestTitle
              ? `<div class="text-muted small">📝 ${ans.requestTitle}</div>`
              : ""
          }
          <small class="text-muted">${new Date(
            ans.createdAt
          ).toLocaleTimeString()}</small>
        </div>
        <a href="/request/${
          ans.requestId
        }" target="_blank" class="btn btn-sm btn-outline-secondary">상세보기</a>
      </li>
    `
      )
      .join("");

    $container.append(`
        <div class="mb-4 border-bottom pb-3 sidebar-card" onclick="focusMarker('${placeKey}')" style="cursor: pointer;">
            <h6 class="text-primary fw-semibold small">📢 공지</h6>
            ${noticeHtml}
            
            <h6 class="text-primary fw-semibold small mt-3">💬 요청 응답</h6>
            <ul class="mt-1">${answersHtml}</ul>
        </div>
        `);
  });
}

function renderUserMarkers(logs) {
  logs.forEach((log) => {
    if (!log.lat || !log.lng) return;

    const marker = new naver.maps.Marker({
      map: map,
      position: new naver.maps.LatLng(log.lat, log.lng),
    });

    const infoWindow = new naver.maps.InfoWindow({
      content: `
        <div style="padding: 5px; max-width: 220px;">
          <strong>📍 사용자 응답</strong><br/>
          <span>${log.content}</span><br/>
          <small>${new Date(log.createdAt).toLocaleString()}</small><br/>
        </div>`,
    });

    naver.maps.Event.addListener(marker, "click", function () {
      openedInfoWindows.forEach((iw) => iw.close());
      infoWindow.open(map, marker);
      openedInfoWindows = [infoWindow];
    });

    dataMarkers.push(marker);
    markerMap[`log-${log.id}`] = marker;
  });
}

function renderSidebarUserLogs(logs) {
  const $container = $("#sidebarUserLogs");
  $("#sidebarUserContent").show();
  $container.empty();

  if (logs.length === 0) {
    $container.append(`
      <div class="text-muted small ms-2">주변에 사용자 지정 응답이 없습니다.</div>
    `);
    return;
  }

  logs.forEach((log) => {
    const logKey = `log-${log.id}`;

    $container.append(`
      <div class="mb-3 border-bottom pb-2 sidebar-card" onclick="focusMarker('${logKey}')" style="cursor: pointer;">
        <div class="d-flex justify-content-between align-items-center mb-1">
          <div class="fw-bold">${log.customPlaceName ?? "응답"}</div>
          <a href="/request/${
            log.requestId
          }" target="_blank" class="btn btn-sm btn-outline-secondary">상세보기</a>
        </div>
        <div>${log.content}</div>
        ${
          log.requestTitle
            ? `<div class="text-muted small">📝 ${log.requestTitle}</div>`
            : ""
        }
        <div><small class="text-muted">${new Date(
          log.createdAt
        ).toLocaleTimeString()}</small></div>
      </div>
    `);
  });
}

function focusMarker(key) {
  const marker = markerMap[key];
  if (marker) {
    map.panTo(marker.getPosition());
    const infoWindow = new naver.maps.InfoWindow({
      content: `<div style='padding: 5px;'>해당 위치입니다.</div>`,
    });
    infoWindow.open(map, marker);
    openedInfoWindows.forEach((iw) => iw.close());
    openedInfoWindows = [infoWindow];
  }
}

function getUserLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      function (pos) {
        loadMapAndSidebar(pos.coords.latitude, pos.coords.longitude);
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
    if (currentLat && currentLng) {
      loadMapAndSidebar(currentLat, currentLng);
    }
  });

  $("#filterModeOfficial").on("click", function () {
    currentFilterMode = "official";
    loadMapAndSidebar(currentLat, currentLng);
  });

  $("#filterModeUser").on("click", function () {
    currentFilterMode = "user";
    loadMapAndSidebar(currentLat, currentLng);
  });
});
