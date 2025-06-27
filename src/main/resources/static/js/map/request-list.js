import { groupByLocation } from "./util/groupUtils.js";
import { getCurrentPosition } from "./util/locationUtils.js";
import { createUserCircle, moveMapTo } from "./util/mapUtils.js";

let map = null;
let userCircle = null;
let requestMarkers = [];
let markerMap = {};
let openedInfoWindow = null;

let currentPage = 0;
let pageSize = 5;
let hasNextPage = true;

let lastLat = null;
let lastLng = null;

// 버튼 이벤트 바인딩
$(document).ready(function () {
  getCurrentPosition((pos) => {
    currentPage = 0;
    hasNextPage = true;
    loadRequests(pos.coords.latitude, pos.coords.longitude);
  });

  bindSidebarCardClick();

  // 지도 밖 클릭 시 InfoWindow 닫기
  $(document).on("click", function (e) {
    // InfoWindow나 마커, 사이드바 카드를 클릭한 게 아니라면 닫기
    if (
      !$(e.target).closest(".custom-infowindow").length &&
      !$(e.target).closest(".sidebar-card").length
    ) {
      if (openedInfoWindow) {
        openedInfoWindow.close();
        openedInfoWindow = null;
      }
    }
  });

  $("#myLocationButton").on("click", () => {
    getCurrentPosition((pos) => {
      currentPage = 0;
      hasNextPage = true;
      loadRequests(pos.coords.latitude, pos.coords.longitude);
    });
  });

  $("#refreshNearbyButton").on("click", function () {
    if (map) {
      const center = map.getCenter();
      currentPage = 0;
      hasNextPage = true;
      loadRequests(center.lat(), center.lng());
    }
  });

  $(document).on("click", ".toggle-group", function () {
    const $group = $(this).closest(".sidebar-card").find(".grouped-requests");
    const isOpen = !$group.hasClass("d-none");

    $group.toggleClass("d-none");
    const count = $group.children().length;
    $(this).text(isOpen ? `+ 더 보기 (${count})` : "▲ 접기");
  });

  $(document).on("click", "#prevPageBtn", function () {
    if (currentPage > 0) {
      currentPage--;
      loadRequests(lastLat, lastLng);
    }
  });

  $(document).on("click", "#nextPageBtn", function () {
    currentPage++;
    loadRequests(lastLat, lastLng);
  });
});

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
    moveMapTo(map, userCircle, lat, lng);
  }

  if (userCircle) userCircle.setMap(null);
  userCircle = createUserCircle(map, center, 3000, "#007BFF");
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
    const rep = group[0];
    const others = group.slice(1);

    // 날짜 포맷 (예: 2025.06.27 14:32)
    const createdAt = new Date(rep.createdAt).toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });

    // 장소 이름
    const placeName = rep.placeName || rep.customPlaceName || "장소 정보 없음";

    // 장소 타입 표시
    const placeTypeBadge = rep.placeName
      ? `<span class="badge bg-success ms-1">공식 장소</span>`
      : `<span class="badge bg-secondary ms-1">일반 장소</span>`;

    const otherHtml = others
      .map((req) => {
        const created = new Date(req.createdAt).toLocaleString("ko-KR");
        const place = req.placeName || req.customPlaceName || "장소 없음";
        const isOfficial = !!req.placeName;
        const typeBadge = isOfficial
          ? `<span class="badge bg-success ms-1">공식 장소</span>`
          : `<span class="badge bg-secondary ms-1">일반 장소</span>`;

        return `
        <div class="border-top pt-2 mt-2">
          <div class="text-muted small">${req.title}</div>
          <div class="text-muted small">📍 ${place} ${typeBadge}</div>
          <div class="text-muted small">🕒 ${created}</div>
          <div class="text-muted small">응답: ${req.visibleAnswerCount}/3 / 포인트: ${req.point}</div>
          <a href="/request/${req.id}" target="_blank" class="btn btn-sm btn-outline-secondary mt-1">자세히 보기</a>
        </div>`;
      })
      .join("");

    const groupHtml = `
      <div class="sidebar-card mb-3 p-2 border rounded" data-marker-key="${key}">
        <div class="mb-2">
          <div class="fw-bold">${rep.title}</div>
          <div class="text-muted small">📍 ${placeName} ${placeTypeBadge}</div>
          <div class="text-muted small">🕒 ${createdAt}</div>
          <div class="text-muted small">응답: ${
            rep.visibleAnswerCount
          }/3 / 포인트: ${rep.point}</div>
          <a href="/request/${
            rep.id
          }" target="_blank" class="btn btn-sm btn-outline-primary mt-1">자세히 보기</a>
        </div>
        ${
          others.length > 0
            ? `
          <div class="mt-2">
            <span class="badge bg-info text-dark toggle-group" style="cursor: pointer;">
              + 더 보기 (${others.length})
            </span>
          </div>
          <div class="grouped-requests d-none">${otherHtml}</div>`
            : ""
        }
      </div>`;

    $sidebar.append(groupHtml);
  }
}

function bindSidebarCardClick() {
  $(document).on("click", ".sidebar-card", function (e) {
    if ($(e.target).hasClass("toggle-group")) return;

    const key = $(this).data("marker-key");
    if (key) {
      focusMarker(key);
    }
  });
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
  lastLat = lat;
  lastLng = lng;

  if (currentPage === 0 && !map) {
    loadMap(lat, lng);
  }

  $.get(`/api/request/nearby`, {
    lat: lat,
    lng: lng,
    radius: 3000,
    page: currentPage,
    size: pageSize,
  }).done((response) => {
    const requests = response.content;
    hasNextPage = !response.last;

    if (!requests || requests.length === 0) {
      $("#sidebarRequests").html(`
        <div class="alert alert-info text-center">
          근처에 요청이 없습니다.
        </div>
      `);
      $("#sidebarPagination").remove();
      return;
    }

    // 마커와 사이드바 초기화
    requestMarkers.forEach((m) => m.setMap(null));
    requestMarkers = [];
    markerMap = {};
    $("#sidebarRequests").empty();

    const grouped = groupByLocation(requests, "lat", "lng", 5);
    renderMarkers(grouped);
    renderSidebar(grouped);
    renderPaginationControls();
  });
}

// 페이지네이션 버튼 렌더링
function renderPaginationControls() {
  const $pagination = $("#sidebarPagination");
  $pagination.remove(); // 기존 제거

  const controlsHtml = `
    <div id="sidebarPagination" class="text-center my-3">
      <button id="prevPageBtn" class="btn btn-outline-secondary btn-sm me-2" ${
        currentPage === 0 ? "disabled" : ""
      }>이전</button>
      <button id="nextPageBtn" class="btn btn-outline-primary btn-sm" ${
        !hasNextPage ? "disabled" : ""
      }>다음</button>
    </div>
  `;

  $("#sidebarRequests").append(controlsHtml);
}
