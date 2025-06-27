import { getCurrentPosition } from "./util/locationUtils.js";
import { closeAllInfoWindows } from "./util/mapUtils.js";
import {
  getCategoryLabel,
  extractExtraFieldValueOnly,
} from "./util/categoryUtils.js";
import { formatDate } from "./util/dateUtils.js";

// 전역변수
let map = null;
let dataMarkers = [];
let userCircle = null;
let currentLat = null;
let currentLng = null;
let currentFilterMode = "official";
let openedInfoWindows = [];
let markerMap = {};
let currentPage = 0;
const pageSize = 6;

$(document).ready(function () {
  // [1] 페이지 로드 시 사용자 위치 기반으로 지도 + 사이드바 초기화
  getUserLocation();

  // [2] 사이드바 카드 클릭 시 해당 마커로 지도 이동 이벤트 바인딩
  bindSidebarCardClick();

  // [3] 내 위치 버튼 클릭 → 현재 위치 기준으로 지도 및 응답 새로 로드
  $("#myLocationButton").on("click", function () {
    getUserLocation();
  });

  // [4] 주변 새로고침 버튼 클릭 → 현재 좌표 기준으로 새로 요청
  $("#refreshNearbyButton").on("click", function () {
    if (currentLat && currentLng) {
      loadMapAndSidebar(currentLat, currentLng);
    }
  });

  // [5] 필터 버튼 클릭 (공식 장소 모드) → 공식 장소 응답 로드
  $("#filterModeOfficial").on("click", function () {
    currentFilterMode = "official";
    loadMapAndSidebar(currentLat, currentLng);
  });

  // [6] 필터 버튼 클릭 (사용자 장소 모드) → 사용자 지정 응답 로드
  $("#filterModeUser").on("click", function () {
    currentFilterMode = "user";
    loadMapAndSidebar(currentLat, currentLng);
  });

  // [7] 공지 접기/펼치기 버튼 클릭 시 collapse toggle
  $(document).on("click", ".toggle-register", function (e) {
    e.stopPropagation(); // 지도 이동 방지
    const targetId = $(this).data("bs-target");
    $(targetId).collapse("toggle");
  });

  // [8] collapse 전환 완료 시 버튼 텍스트를 "내용 펼치기 / 숨기기"로 토글
  $(document).on("shown.bs.collapse hidden.bs.collapse", function (e) {
    const $collapse = $(e.target);
    const isShown = e.type === "shown";
    const $button = $(`[data-bs-target="#${$collapse.attr("id")}"]`);

    $button.text(isShown ? "내용 숨기기" : "내용 펼치기");
  });

  // [9] "질문 더보기" 버튼 클릭 → 숨겨진 질문 카드 토글 표시
  $(document).on("click", "[id^='toggleMoreQuestions_']", function (e) {
    e.stopPropagation(); // 지도 이동 방지

    const card = $(this).closest(".sidebar-card");
    const hiddenQuestions = card.find(".extra-question");

    if (hiddenQuestions.hasClass("d-none")) {
      hiddenQuestions.removeClass("d-none");
      $(this).text("질문 숨기기");
    } else {
      hiddenQuestions.addClass("d-none");
      $(this).text("질문 더보기");
    }
  });
});

// 지도 로딩
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

// 마커 지우기
function clearMarkers() {
  dataMarkers.forEach((m) => m.setMap(null));
  dataMarkers = [];
  markerMap = {};
  closeAllInfoWindows(openedInfoWindows);
  openedInfoWindows = [];
}

// 마커 클릭 시 InfoWindow 열기 이벤트 바인딩
function attachMarkerEvent(marker, infoWindow) {
  naver.maps.Event.addListener(marker, "click", () => {
    closeAllInfoWindows(openedInfoWindows);
    infoWindow.open(map, marker);
    openedInfoWindows = [infoWindow];
  });
}

// 사이드바 로딩
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
    loadUserLogsPaginated(lat, lng, 0);
  }
}

function loadUserLogsPaginated(lat, lng, page = 0) {
  clearMarkers(); // 마커 제거

  $.get("/api/status/nearby/user-locations", {
    lat,
    lng,
    radiusMeters: 3000,
    page,
    size: pageSize,
  }).done((response) => {
    currentPage = page;

    // 사이드바 내용 비우고 리스트 렌더링
    const $container = $("#sidebarUserLogs");
    $container.empty();
    renderSidebarUserLogs(response.content);

    // 페이지네이션 렌더링 (마지막 페이지 여부 전달)
    renderPaginationControls(!response.last);

    // 마커도 현재 페이지 기준으로만 렌더링
    renderUserMarkers(response.content);
  });
}

// ─────────────────────────────────────────────
// 일반 장소 페이지네이션 관련
// ─────────────────────────────────────────────

function renderPaginationControls(hasNext) {
  const $container = $("#sidebarUserLogs");

  // 페이지네이션 버튼 영역 추가
  let html = `<div class="d-flex justify-content-between mt-2 px-1">`;

  if (currentPage > 0) {
    html += `<button class="btn btn-sm btn-primary" id="prevUserPage">이전</button>`;
  } else {
    html += `<div></div>`; // 공간 유지를 위한 빈 div
  }

  if (hasNext) {
    html += `<button class="btn btn-sm btn-primary" id="nextUserPage">다음</button>`;
  }

  html += `</div>`;
  $container.append(html);

  // 핸들러 중복 방지를 위해 .off().on() 사용
  $(document)
    .off("click", "#prevUserPage")
    .on("click", "#prevUserPage", () => {
      if (currentPage > 0) {
        loadUserLogsPaginated(currentLat, currentLng, currentPage - 1);
      }
    });

  $(document)
    .off("click", "#nextUserPage")
    .on("click", "#nextUserPage", () => {
      loadUserLogsPaginated(currentLat, currentLng, currentPage + 1);
    });
}

// ─────────────────────────────────────────────
// 공식 장소 관련
// ─────────────────────────────────────────────

// 공식 장소 그룹에 대한 마커 랜더링 - REGISTER 없을 경우 첫 ANSWER 기준 좌표 사용
function renderGroupedMarkers(groups) {
  groups.forEach((group) => {
    let lat = group.latestRegister?.lat;
    let lng = group.latestRegister?.lng;

    // 공지 좌표가 없으면 → 첫 answerLogs 중 좌표 있는 항목 fallback
    if (!lat || !lng) {
      const fallback = group.answerLogs.find((ans) => ans.lat && ans.lng);
      if (fallback) {
        lat = fallback.lat;
        lng = fallback.lng;
      }
    }

    // 여전히 좌표 없으면 마커 생성 생략
    if (!lat || !lng) return;

    const position = new naver.maps.LatLng(lat, lng);
    const marker = new naver.maps.Marker({ map, position });

    const infoWindow = new naver.maps.InfoWindow({
      content: getInfoWindowHtml(group),
    });

    attachMarkerEvent(marker, infoWindow);
    dataMarkers.push(marker);
    markerMap[`place-${group.placeId}`] = marker;
  });
}

// InfoWindow에 표시할 HTML 템플릿 생성 - 장소 이름, 공지 내용, 시간, 주소 포함
function getInfoWindowHtml(group) {
  const { placeName, address, latestRegister, answerLogs } = group;

  // 공지 기준
  if (latestRegister) {
    const createdAt = new Date(latestRegister.createdAt).toLocaleString();
    return `
      <div class="map-info-window">
        <strong>${placeName}</strong><br/>
        <span>📢 ${latestRegister.content}</span><br/>
        <small>${createdAt}</small><br/>
        <span class="text-muted small">${address}</span>
      </div>`;
  }

  // 공지가 없고 답변만 있는 경우
  const fallback = answerLogs.find((ans) => ans.content && ans.createdAt);
  if (fallback) {
    const createdAt = new Date(fallback.createdAt).toLocaleString();
    return `
      <div class="map-info-window">
        <strong>${placeName}</strong><br/>
        <span>💬 ${fallback.content}</span><br/>
        <small>${createdAt}</small><br/>
        <span class="text-muted small">${address}</span>
      </div>`;
  }

  // 둘 다 없으면 간단한 장소 정보만
  return `
    <div class="map-info-window">
      <strong>${placeName}</strong><br/>
      <span class="text-muted">등록된 정보가 없습니다</span><br/>
      <span class="text-muted small">${address}</span>
    </div>`;
}

// 공식 장소 사이드 랜더링
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

  groups.forEach((group, index) => {
    const placeId = group.placeId;
    const placeKey = `place-${placeId}`;
    const noticeHtml = renderRegisterLog(group.latestRegister);

    const groupedAnswers = group.answerLogs.reduce((acc, answer) => {
      const key = answer.requestId;
      if (!acc[key]) {
        acc[key] = {
          requestTitle: answer.requestTitle,
          requestId: answer.requestId,
          answers: [],
        };
      }
      acc[key].answers.push(answer);
      return acc;
    }, {});

    const answerGroups = Object.values(groupedAnswers);

    const groupedHtml = answerGroups
      .map((grp, idx) => {
        const answerItems = grp.answers
          .map((ans) => `<li class="mb-1">💬 ${ans.content}</li>`)
          .join("");

        const isHidden = idx >= 2 ? "d-none extra-question" : "";

        return `
        <div class="card p-3 mb-2 ${isHidden}">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="fw-semibold text-dark">📝 ${grp.requestTitle}</div>
            <div class="text-end">
              <span class="badge bg-primary d-inline-block mb-1">${getCategoryLabel(
                grp.answers[0].category
              )}</span>
              <a href="/request/${
                grp.requestId
              }" class="btn btn-outline-secondary">📎 상세보기</a>
            </div>
          </div>
          <div class="fw-bold mb-1">답변</div>
          <ul class="mb-1">${answerItems}</ul>
        </div>
      `;
      })
      .join("");

    // 카드 상단 테두리는 첫 카드에만 적용
    const borderClass =
      index === 0 ? "border-top border-bottom pt-3" : "border-bottom";

    const toggleButton =
      answerGroups.length > 2
        ? `<button class="btn btn-outline-secondary" id="toggleMoreQuestions_${placeId}">질문 더보기</button>`
        : "";

    const cardHtml = `
    <div class="mb-4 ${borderClass} pb-3 sidebar-card" data-marker-key="${placeKey}">
      <div class="d-flex justify-content-between align-items-start mb-2">
        <div>
          <div class="fw-bold fs-6">${group.placeName}</div>
          <div class="text-muted small">${group.address}</div>
        </div>
        <a href="/place/community/${placeId}" target="_blank" class="btn btn-outline-primary">커뮤니티</a>
      </div>

      ${noticeHtml}

      <div class="answer-section">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h6 class="text-primary fw-semibold fs-6 mb-0">💬 요청 응답</h6>
          ${toggleButton}
        </div>
        ${groupedHtml}
      </div>
    </div>
  `;

    $container.append(cardHtml);
  });
}

// 공식장소 공지 관련 렌더링 내부 함수
function renderRegisterLog(log) {
  if (!log) {
    return `
      <div class="mb-3 p-2 border rounded bg-light text-muted">
        📢 <strong>공지:</strong> 등록된 공지가 없습니다.
      </div>
    `;
  }

  const categoryLabel = getCategoryLabel(log.category);
  const extraValueOnly = extractExtraFieldValueOnly(log.category, log);
  const time = formatDate(log.createdAt);
  const content = log.content;

  const collapseId = `collapseRegister_${log.id || "latest"}`;

  return `
    <div class="mb-3 p-3 border rounded bg-light">
      <div class="d-flex justify-content-between align-items-start gap-3">
        <div>
          📢 <strong>공식 공지:</strong> ${content}
        </div>
        <div class="text-end">
          <span class="badge bg-primary d-inline-block mb-1">${categoryLabel}</span>
          <button class="btn btn-outline-secondary toggle-register"
            data-bs-toggle="collapse"
            data-bs-target="#${collapseId}">
            내용 펼치기
          </button>
        </div>
      </div>
      <div class="collapse mt-2" id="${collapseId}">
        <div class="border-top pt-2">
          📝 상태: ${extraValueOnly}<br/>
          ⏰ <span class="text-muted small">${time}</span>
        </div>
      </div>
    </div>
  `;
}

// ─────────────────────────────────────────────
// 일반 장소 관련
// ─────────────────────────────────────────────

// 일반 장소 마커 랜더링
function renderUserMarkers(logs) {
  logs.forEach((log) => {
    if (!log.lat || !log.lng) return;

    const position = new naver.maps.LatLng(log.lat, log.lng);
    const marker = new naver.maps.Marker({ map, position });

    const infoWindow = new naver.maps.InfoWindow({
      content: getUserLogInfoWindowHtml(log),
    });

    attachMarkerEvent(marker, infoWindow);

    dataMarkers.push(marker);
    markerMap[`log-${log.id}`] = marker;
  });
}

// 일반 장소 응답용 InfoWindow 템플릿
function getUserLogInfoWindowHtml(log) {
  const createdAt = new Date(log.createdAt).toLocaleString();
  return `
    <div class="map-info-window">
      <strong>📍 일반 장소 응답</strong><br/>
      <span>${log.content}</span><br/>
      <small>${createdAt}</small>
    </div>`;
}

// 일반 장소 사이드바 랜더링
function renderSidebarUserLogs(logs) {
  const $container = $("#sidebarUserLogs");
  $("#sidebarUserContent").show();
  $container.empty();

  if (logs.length === 0) {
    $container.append(`
      <div class="text-muted small ms-2">주변에 일반 장소 응답이 없습니다.</div>
    `);
    return;
  }

  logs.forEach((log, index) => {
    const logKey = `log-${log.id}`;
    const time = formatDate(log.createdAt);

    const borderClass =
      index === 0 ? "border-top border-bottom pt-3" : "border-bottom";

    $container.append(`
    <div class="mb-3 ${borderClass} pb-2 sidebar-card" data-marker-key="${logKey}">
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
      <div><small class="text-muted">${time}</small></div>
    </div>
  `);
  });
}

// ─────────────────────────────────────────────
// 유틸 함수
// ─────────────────────────────────────────────

// 사이드바 카드 클릭 시 해당 마커로 지도 이동 이벤트 바인딩
function bindSidebarCardClick() {
  $(document).on("click", ".sidebar-card", function () {
    const key = $(this).data("marker-key");
    if (key) {
      focusMarker(key);
    }
  });
}

// 마커 포커싱
function focusMarker(key) {
  const marker = markerMap[key];
  if (marker) {
    map.panTo(marker.getPosition());
    const infoWindow = new naver.maps.InfoWindow({
      content: `<div style='padding: 5px;'>
              해당 위치입니다.<br/>
              👉 마커를 클릭하면 자세한 정보를 볼 수 있어요!
            </div>`,
    });
    infoWindow.open(map, marker);
    closeAllInfoWindows(openedInfoWindows);
    openedInfoWindows = [infoWindow];
  }
}

function getUserLocation() {
  getCurrentPosition((pos) => {
    loadMapAndSidebar(pos.coords.latitude, pos.coords.longitude);
  });
}
