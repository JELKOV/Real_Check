import { getCurrentPosition, isWithinRadius } from "./util/locationUtils.js";
import {
  createUserCircle,
  updateCircleStyle,
  moveMapTo,
  extractLatLngFromMap,
} from "./util/mapUtils.js";

import { debounce } from "../util/common.js";

import { groupByLocation } from "./util/groupUtils.js";
import { getCategoryLabel } from "./util/categoryUtils.js";

// ─────────────────────────────────────────────
// [0] 전역 변수 정의
// ─────────────────────────────────────────────
let map = null; // 네이버 맵 인스턴스
let userCircle = null; // 사용자 중심 3km 원
let userLocation = null; // 사용자 현재 위치
let uploadedImageUrls = []; // 업로드된 이미지 URL 배열
let dataMarkers = []; // 현재 지도 위 마커 리스트
let initialUserLocation = null;
let currentPage = 1;
let isLoading = false;
let hasNextPage = true;
let isRegisterMode = false;

// ─────────────────────────────────────────────
// [1] 초기화: 문서 준비 완료 시 실행
// ─────────────────────────────────────────────
$(document).ready(function () {
  // 사용자 위치를 가져와 지도 초기화 및 리스트 로드 수행
  getUserLocation();

  // 등록/정보 보기 토글 버튼 초기 텍스트 설정
  toggleRegisterUI(isRegisterMode);

  // ───────────── 버튼 및 이벤트 바인딩 ─────────────

  // 내 위치로 이동 버튼 클릭 시 → 위치 재탐색
  $("#myLocationButton").on("click", getUserLocation);
  // 근처 정보 새로고침 버튼 → 리스트 초기화 및 다시 로드
  $("#refreshNearbyButton").on("click", resetAndLoadFreeShareList);
  // 이미지 업로드 input 변경 시 → 업로드 및 미리보기 렌더링
  $("#imageInput").on("change", handleImageFileChange);
  // 카테고리 선택 시 → 해당하는 동적 필드 렌더링
  $("#categorySelect").on("change", renderDynamicFields);
  // 등록 폼 제출 시 → 등록 처리 수행
  $("#registerForm").on("submit", handleSubmit);
  // 일수 필터 변경 시 → 리스트 재로딩
  $("#daysSelect").on("change", resetAndLoadFreeShareList);

  // 반경 필터 변경 시 원 반영
  $("#radiusSelect").on("change", function () {
    const radius = parseInt($(this).val());

    if (userCircle) {
      userCircle.setRadius(radius); // UI 반영 (원 크기)
    }

    resetAndLoadFreeShareList(); // 데이터 다시 불러오기
  });

  // 더보기 버튼 클릭 시 → 다음 페이지 데이터 불러오기
  $("#loadMoreBtn").on("click", function () {
    loadFreeShareList(true); // append = true (기존에 이어붙임)
  });
  // 이미지 미리보기 영역의 삭제 버튼 클릭 시 → 해당 이미지 제거
  $(document).on("click", ".delete-image-btn", function () {
    const url = $(this).data("url");
    uploadedImageUrls = uploadedImageUrls.filter((u) => u !== url);
    renderImagePreview();
  });
  // 주소 검색 버튼 클릭 시 → 지도 이동 및 리스트 로드
  $("#searchAddressBtn").click(() => {
    if (isRegisterMode) {
      alert("등록 모드에서는 주소 검색을 사용할 수 없습니다.");
      return;
    }

    const query = $("#addressInput").val();
    if (!query) return alert("주소를 입력하세요");

    naver.maps.Service.geocode({ query }, (status, response) => {
      if (status !== naver.maps.Service.Status.OK) {
        return alert("주소를 찾을 수 없습니다");
      }

      const item = response.v2.addresses[0];
      if (!item) return alert("주소 결과 없음");

      const latlng = new naver.maps.LatLng(item.y, item.x);
      map.setCenter(latlng); // 지도 중심 이동
      userLocation = extractLatLngFromMap(map); // 위치 갱신
      userCircle.setCenter(latlng); // 원도 이동
      resetAndLoadFreeShareList(); // 리스트 재로딩
    });
  });

  // 주소 입력창에서 엔터 키 입력 시 → 검색 실행
  $("#addressInput").on("keydown", function (e) {
    if (e.key === "Enter") {
      e.preventDefault();
      $("#searchAddressBtn").click();
    }
  });

  // 등록/정보 보기 토글 버튼 클릭 시
  $("#registerToggleButton").on("click", function () {
    if (isRegisterMode) {
      exitRegisterMode();
    } else {
      enterRegisterMode();
    }

    // 지도 위치 재조정
    if (userLocation && map) {
      moveMapTo(map, userCircle, userLocation.lat, userLocation.lng);
    }
  });
});

function resetAndLoadFreeShareList() {
  if (isRegisterMode) return; // 등록 모드일 땐 리스트 로딩 금지
  currentPage = 1;
  hasNextPage = true;
  loadFreeShareList(false);
}

// ─────────────────────────────────────────────
// [2] 사용자 위치 가져오기 → 지도 초기화 및 리스트 로드
// ─────────────────────────────────────────────
function getUserLocation() {
  getCurrentPosition((pos) => {
    userLocation = { lat: pos.coords.latitude, lng: pos.coords.longitude };
    if (!initialUserLocation) {
      initialUserLocation = { ...userLocation };
    }
    initMap(userLocation.lat, userLocation.lng);
    if (!isRegisterMode) {
      resetAndLoadFreeShareList();
    }
  });
}

// ─────────────────────────────────────────────
// [3] 지도 초기화 + 사용자 반경 표시 + 지도 클릭 등록
// ─────────────────────────────────────────────
function initMap(lat, lng) {
  const center = new naver.maps.LatLng(lat, lng);

  map = new naver.maps.Map("freeShareMap", {
    center: center,
    zoom: 14,
  });

  dataMarkers.forEach((m) => m.setMap(null));
  dataMarkers = [];

  if (userCircle) userCircle.setMap(null);

  userCircle = createUserCircle(
    map,
    center,
    3000,
    isRegisterMode ? "#007bff" : "#28a745"
  );

  // 지도 클릭 시 등록 폼 띄우기
  naver.maps.Event.addListener(map, "click", function (e) {
    if (!isRegisterMode) return; // 등록 모드 아닐 땐 무시

    const lat = e.coord.lat();
    const lng = e.coord.lng();

    // 반경 검사
    if (!isWithinRadius(userLocation.lat, userLocation.lng, lat, lng, 3000)) {
      alert("3km 이내에서만 등록할 수 있습니다.");
      return;
    }

    // 위치 설정 후 모달 열기
    $("#registerForm input[name='lat']").val(lat);
    $("#registerForm input[name='lng']").val(lng);
    new bootstrap.Modal(document.getElementById("registerModal")).show();
  });

  // 드래그 후 0.3초 동안 아무 동작 없을 때만 실행
  const handleDragEnd = debounce(() => {
    if (isRegisterMode) return;

    const center = map.getCenter();
    userLocation = extractLatLngFromMap(map);
    userCircle.setCenter(center);
    resetAndLoadFreeShareList();
  }, 300);

  // 드래그 이동 후 중심 좌표 업데이트 및 로그 재로딩
  naver.maps.Event.addListener(map, "dragend", handleDragEnd);
}

// ─────────────────────────────────────────────
// [4] 자발적 공유 정보 목록 불러오기 + 클릭 시 포커싱
// ─────────────────────────────────────────────

function loadFreeShareList(append = false) {
  if (!userLocation || isLoading || !hasNextPage) {
    $("#loadMoreContainer").hide();
    return;
  }

  isLoading = true;
  $("#loadingSpinner").show();

  const days = parseInt($("#daysSelect").val()) || 7;
  const radius = parseInt($("#radiusSelect").val()) || 3000;

  $.get(
    "/api/status/free-share",
    {
      lat: userLocation.lat,
      lng: userLocation.lng,
      radiusMeters: radius,
      days,
      page: currentPage,
      size: 10,
    },
    function (res) {
      isLoading = false;
      $("#loadingSpinner").hide();

      const logs = res.content || [];

      if (!append) $("#freeShareList").empty();

      if (logs.length === 0) {
        hasNextPage = false;
        $("#loadMoreContainer").hide();
        return;
      }

      // 그룹화 후 대표만 렌더링
      const grouped = groupByLocation(logs, "lat", "lng", 5);

      for (const key in grouped) {
        const group = grouped[key];
        const representative = group[0];
        renderLogItem(representative, group.length, group);
      }

      if (currentPage >= res.totalPages) {
        hasNextPage = false;
        $("#loadMoreContainer").hide();
      } else {
        currentPage++;
        $("#loadMoreContainer").show();
      }
    }
  );
}

function renderLogItem(log, groupSize = 1, group = []) {
  const label = getCategoryLabel(log.category) || "정보 공유";
  const shortContent =
    log.content.length > 50 ? log.content.slice(0, 50) + "..." : log.content;

  const groupMeta =
    groupSize > 1
      ? `<div class="group-meta text-end">
          <span class="badge bg-secondary mb-1">+${
            groupSize - 1
          }개 동일 위치</span><br/>
          <button
            class="btn btn-sm btn-outline-primary view-group-btn mt-1"
            data-group='${JSON.stringify(group)}'
            title="같은 위치 다른 공유 보기">
            🔍 모두 보기
          </button>
        </div>`
      : "";

  const item = $(`
    <div class="card mb-2 free-share-item" data-log-id="${log.id}">
      <div class="card-body">
        <div class="d-flex justify-content-between flex-wrap align-items-start">
          <h6 class="card-title mb-1">${label}</h6>
          ${groupMeta}
        </div>
        <p class="card-text text-truncate mt-2">${shortContent}</p>
        <div class="text-muted mt-2">
          조회수: ${log.viewCount} · ${new Date(log.createdAt).toLocaleString()}
        </div>
      </div>
    </div>
  `);

  item.on("click", () => {
    showLogDetail(log);
    focusOnLog(log);
  });

  // 모두 보기 버튼 클릭 이벤트
  item.find(".view-group-btn").on("click", (e) => {
    e.stopPropagation(); // 부모 클릭 이벤트 막기
    const groupLogs = JSON.parse($(e.currentTarget).attr("data-group"));
    showGroupDetailModal(groupLogs);
  });

  $("#freeShareList").append(item);
}

// ─────────────────────────────────────────────
// [5] 선택된 로그를 지도에 띄우고 중심 이동
// ─────────────────────────────────────────────
function focusOnLog(log) {
  if (!log.lat || !log.lng) return alert("위치 정보가 없습니다.");
  dataMarkers.forEach((m) => m.setMap(null));
  dataMarkers = [];

  const position = new naver.maps.LatLng(log.lat, log.lng);
  const marker = new naver.maps.Marker({ position, map });
  const infoWindow = new naver.maps.InfoWindow({
    content: `<div style="padding:5px; max-width:250px;"><strong>${
      log.categoryLabel || "정보 공유"
    }</strong><br/>${log.content}<br/><small>${new Date(
      log.createdAt
    ).toLocaleString()}</small></div>`,
  });
  infoWindow.open(map, marker);
  map.setCenter(position);
  dataMarkers.push(marker);
}

// ─────────────────────────────────────────────
// [6] 로그 상세 정보 모달 표시
// ─────────────────────────────────────────────

function showLogDetail(log) {
  if (!$("#logDetailModal").hasClass("show")) {
    new bootstrap.Modal(document.getElementById("logDetailModal")).show();
  }
  renderLogDetail(log);

  // Redis에 반영된 최신 로그 정보 다시 요청
  $.get(`/api/status/free-share/view/${log.id}`)
    .done((updatedLog) => {
      renderLogDetail(updatedLog);
    })
    .fail((xhr) => {
      if (xhr.status === 429) {
        alert("너무 자주 조회하고 있습니다. 잠시 후 다시 시도해주세요.");
      } else {
        alert("상세 정보를 불러오지 못했습니다.");
      }
    });
}

// 동일 위치 여러개 정보 그룹 보기
function showGroupDetailModal(groupLogs) {
  const container = $("#groupDetailContent");
  container.empty();

  groupLogs.forEach((log) => {
    const label = getCategoryLabel(log.category);
    const shortContent =
      log.content.length > 50 ? log.content.slice(0, 50) + "..." : log.content;

    const logItem = $(`
      <div class="border-bottom mb-3 pb-2">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <strong>${label}</strong><br/>
            <span>${shortContent}</span>
            <div class="text-muted small">${new Date(
              log.createdAt
            ).toLocaleString()}</div>
          </div>
          <button class="btn btn-sm btn-outline-secondary view-detail-btn">상세 보기</button>
        </div>
      </div>
    `);

    logItem.find(".view-detail-btn").on("click", () => {
      $("#groupDetailModal").modal("hide");
      showLogDetail(log);
    });

    container.append(logItem);
  });

  new bootstrap.Modal(document.getElementById("groupDetailModal")).show();
}

// 분리된 렌더 함수
function renderLogDetail(log) {
  const label = getCategoryLabel(log.category) || "정보 공유";

  const html = `
    <div class="mb-2"><strong>카테고리:</strong> ${label}</div>
    <div class="mb-2"><strong>내용:</strong><br/>${log.content || ""}</div>

    ${
      Array.isArray(log.imageUrls) && log.imageUrls.length
        ? log.imageUrls
            .map(
              (url) =>
                `<img src="${url}" class="img-thumbnail me-2 mb-2" style="max-width: 150px;" />`
            )
            .join("")
        : ""
    }

    ${
      log.waitCount != null
        ? `<div><strong>대기 인원:</strong> ${log.waitCount}명</div>`
        : ""
    }

    ${
      typeof log.menuInfo === "string" && log.menuInfo.trim() !== ""
        ? `<div><strong>메뉴 정보:</strong> ${log.menuInfo}</div>`
        : ""
    }

    ${
      log.hasBathroom != null
        ? `<div><strong>화장실:</strong> ${
            log.hasBathroom ? "있음" : "없음"
          }</div>`
        : ""
    }

    ${
      typeof log.weatherNote === "string" && log.weatherNote.trim() !== ""
        ? `<div><strong>날씨 상태:</strong> ${log.weatherNote}</div>`
        : ""
    }

    ${
      typeof log.vendorName === "string" && log.vendorName.trim() !== ""
        ? `<div><strong>노점 이름:</strong> ${log.vendorName}</div>`
        : ""
    }

    ${
      typeof log.photoNote === "string" && log.photoNote.trim() !== ""
        ? `<div><strong>요청 내용:</strong> ${log.photoNote}</div>`
        : ""
    }

    ${
      typeof log.noiseNote === "string" && log.noiseNote.trim() !== ""
        ? `<div><strong>소음 상태:</strong> ${log.noiseNote}</div>`
        : ""
    }

    ${
      log.isParkingAvailable != null
        ? `<div><strong>주차 가능:</strong> ${
            log.isParkingAvailable ? "가능" : "불가능"
          }</div>`
        : ""
    }

    ${
      log.isOpen != null
        ? `<div><strong>영업 상태:</strong> ${
            log.isOpen ? "영업 중" : "영업 종료"
          }</div>`
        : ""
    }

    ${
      log.seatCount != null
        ? `<div><strong>남은 좌석 수:</strong> ${log.seatCount}개</div>`
        : ""
    }

    ${
      log.crowdLevel != null
        ? `<div><strong>혼잡도:</strong> ${log.crowdLevel}단계</div>`
        : ""
    }

    ${
      typeof log.extra === "string" && log.extra.trim() !== ""
        ? `<div><strong>기타 정보:</strong> ${log.extra}</div>`
        : ""
    }

    <div class="mb-2"><strong>조회수:</strong> ${log.viewCount}</div>
    <div class="text-muted mt-2">${new Date(
      log.createdAt
    ).toLocaleString()}</div>
  `;

  $("#logDetailContent").html(html);
}

// ─────────────────────────────────────────────
// [7] 카테고리에 따른 동적 입력 필드 렌더링
// ─────────────────────────────────────────────
function renderDynamicFields() {
  const selected = $(this).val();
  const $container = $("#categoryDynamicFields");
  $container.empty();

  const fieldMap = {
    PARKING: {
      label: "주차 가능 여부",
      html: `<select name='isParkingAvailable' class='form-select'><option value=''>선택</option><option value='true'>가능</option><option value='false'>불가능</option></select>`,
    },
    WAITING_STATUS: {
      label: "대기 인원 수",
      html: `<input name='waitCount' type='number' class='form-control' />`,
    },
    CROWD_LEVEL: {
      label: "혼잡도",
      html: `<input name='crowdLevel' type='number' class='form-control' />`,
    },
    BATHROOM: {
      label: "화장실 유무",
      html: `<select name='hasBathroom' class='form-select'><option value='true'>있음</option><option value='false'>없음</option></select>`,
    },
    FOOD_MENU: {
      label: "메뉴 정보",
      html: `<input name='menuInfo' type='text' class='form-control' />`,
    },
    WEATHER_LOCAL: {
      label: "날씨 상태",
      html: `<input name='weatherNote' type='text' class='form-control' />`,
    },
    STREET_VENDOR: {
      label: "노점 이름",
      html: `<input name='vendorName' type='text' class='form-control' />`,
    },
    PHOTO_REQUEST: {
      label: "사진 요청 메모",
      html: `<input name='photoNote' type='text' class='form-control' />`,
    },
    NOISE_LEVEL: {
      label: "소음 상태",
      html: `<input name='noiseNote' type='text' class='form-control' />`,
    },
    BUSINESS_STATUS: {
      label: "영업 여부",
      html: `<select name='isOpen' class='form-select'><option value='true'>영업 중</option><option value='false'>영업 종료</option></select>`,
    },
    OPEN_SEAT: {
      label: "남은 좌석 수",
      html: `<input name='seatCount' type='number' class='form-control' />`,
    },
    ETC: {
      label: "기타 메모",
      html: `<input name='extra' type='text' class='form-control' />`,
    },
  };

  if (fieldMap[selected]) {
    $container.append(
      `<div class='mb-3'><label>${fieldMap[selected].label}</label>${fieldMap[selected].html}</div>`
    );
  }
}

// ─────────────────────────────────────────────
// [8] 등록 폼 제출 핸들러
// ─────────────────────────────────────────────
function handleSubmit(e) {
  e.preventDefault();
  const $form = $(this);
  const lat = parseFloat($form.find("input[name='lat']").val());
  const lng = parseFloat($form.find("input[name='lng']").val());
  const category = $form.find("select[name='category']").val();

  if (!isWithinRadius(userLocation.lat, userLocation.lng, lat, lng, 3000)) {
    return alert("3km 이내에서만 등록할 수 있습니다.");
  }

  const dto = {
    content: $form.find("textarea[name='content']").val(),
    category,
    lat,
    lng,
    imageUrls: uploadedImageUrls,
  };

  // 카테고리별 추가 필드 처리
  switch (category) {
    case "WAITING_STATUS":
      dto.waitCount =
        parseInt($form.find("input[name='waitCount']").val()) || null;
      break;
    case "CROWD_LEVEL":
      dto.crowdLevel =
        parseInt($form.find("input[name='crowdLevel']").val()) || null;
      break;
    case "BATHROOM":
      dto.hasBathroom = $form.find("select[name='hasBathroom']").val();
      break;
    case "FOOD_MENU":
      dto.menuInfo = $form.find("input[name='menuInfo']").val() || null;
      break;
    case "WEATHER_LOCAL":
      dto.weatherNote = $form.find("input[name='weatherNote']").val() || null;
      break;
    case "STREET_VENDOR":
      dto.vendorName = $form.find("input[name='vendorName']").val() || null;
      break;
    case "PHOTO_REQUEST":
      dto.photoNote = $form.find("input[name='photoNote']").val() || null;
      break;
    case "NOISE_LEVEL":
      dto.noiseNote = $form.find("input[name='noiseNote']").val() || null;
      break;
    case "PARKING":
      dto.isParkingAvailable = $form
        .find("select[name='isParkingAvailable']")
        .val();
      break;
    case "BUSINESS_STATUS":
      dto.isOpen = $form.find("select[name='isOpen']").val();
      break;
    case "OPEN_SEAT":
      dto.seatCount =
        parseInt($form.find("input[name='seatCount']").val()) || null;
      break;
    case "ETC":
      dto.extra = $form.find("input[name='extra']").val() || null;
      break;
  }

  $.ajax({
    url: "/api/status/free-share",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(dto),
    success: () => {
      alert("등록 완료");
      // 등록 모드 해제
      exitRegisterMode();

      bootstrap.Modal.getInstance(
        document.getElementById("registerModal")
      ).hide();

      if (initialUserLocation) {
        moveMapTo(
          map,
          userCircle,
          initialUserLocation.lat,
          initialUserLocation.lng
        );
        updateUserCircleStyle("#28a745", 3000);
      }
      resetAndLoadFreeShareList();
    },
    error: (xhr) => alert("등록 실패: " + xhr.responseText),
  });
}

// ─────────────────────────────────────────────
// [10] 이미지 업로드 핸들러
// ─────────────────────────────────────────────
function handleImageFileChange(e) {
  const files = e.target.files;
  if (files.length === 0) return;

  const formData = new FormData();
  for (let i = 0; i < files.length; i++) formData.append("files", files[i]);

  $.ajax({
    url: "/api/upload/multi",
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: (res) => {
      uploadedImageUrls = res;
      renderImagePreview();
    },
    error: (xhr) => alert("이미지 업로드 실패: " + xhr.responseText),
  });
}

// ─────────────────────────────────────────────
// [11] 이미지 미리보기 렌더링
// ─────────────────────────────────────────────
function renderImagePreview() {
  const html = uploadedImageUrls
    .map(
      (url) => `
    <div class="position-relative d-inline-block">
      <img src="${url}" data-url="${url}" class="me-2 mb-2 img-thumbnail" style="max-width:100px;" />
      <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn" style="background-color: rgba(0,0,0,0.6); color: white;" title="삭제" data-url="${url}"></button>
    </div>
  `
    )
    .join("");
  $("#uploadedPreview").html(html);
}

// ─────────────────────────────────────────────
// [12] 내부 메서드
// ─────────────────────────────────────────────

// updateUserCircleStyle(color, radius) 함수
function updateUserCircleStyle(color = "#28a745", radius = 3000) {
  updateCircleStyle(userCircle, color, radius);
}

// 필터 비활성화/활성화 함수
function setFilterDisabledState(disabled) {
  $("#radiusSelect").prop("disabled", disabled);
  $("#daysSelect").prop("disabled", disabled);
  $("#addressInput").prop("disabled", disabled);
  $("#searchAddressBtn").prop("disabled", disabled);
}

// 토글 UI 공통함수
function toggleRegisterUI(isRegister) {
  setFilterDisabledState(isRegister);

  $("#registerInstructionArea")
    .toggle(isRegister)
    .toggleClass("d-none", !isRegister);
  $("#freeShareList").toggle(!isRegister).toggleClass("d-none", isRegister);
  $("#loadMoreContainer").toggle(!isRegister);

  const icon = isRegister ? "❌" : "📝";
  const label = isRegister ? "등록 취소" : "정보 등록";
  $("#registerToggleButton").html(`${icon}<br /><span>${label}</span>`);
}

// 등록 모드 진입 함수
function enterRegisterMode() {
  isRegisterMode = true;
  const fixed = initialUserLocation || userLocation;
  userLocation = { ...fixed };
  moveMapTo(map, userCircle, fixed.lat, fixed.lng);
  updateUserCircleStyle("#007bff", 3000);

  $("#radiusSelect").val("3000");
  toggleRegisterUI(true);
}

// 정보 모드 복귀 함수
function exitRegisterMode() {
  isRegisterMode = false;
  updateUserCircleStyle("#28a745");

  toggleRegisterUI(false);

  resetAndLoadFreeShareList();
}
