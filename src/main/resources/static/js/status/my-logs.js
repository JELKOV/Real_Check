// 현재 수정 중인 로그의 ID
let editingId = null;
// 새로 업로드된 이미지의 URL
let uploadedImageUrls = [];
// 현재 페이지 번호 전역 변수
let currentPage = 1;
// 답변목록 담을 배열
let logsList = [];

// 카테고리 코드 → 라벨 매핑 (배지 및 필터용)
const categoryLabelMap = {
  PARKING: "🅿️ 주차 가능 여부",
  WAITING_STATUS: "⏳ 대기 상태",
  STREET_VENDOR: "🥟 노점 현황",
  PHOTO_REQUEST: "📸 사진 요청",
  BUSINESS_STATUS: "🏪 가게 영업 여부",
  OPEN_SEAT: "💺 좌석 여유",
  BATHROOM: "🚻 화장실 여부",
  WEATHER_LOCAL: "☁️ 날씨 상태",
  NOISE_LEVEL: "🔊 소음 여부",
  FOOD_MENU: "🍔 메뉴/음식",
  CROWD_LEVEL: "👥 혼잡도",
  ETC: "❓ 기타",
};

$(document).ready(function () {
  bindEventListeners();
  loadMyLogs(1);
});

// ─────────────────────────────────────
// [1] 이벤트 바인딩
// ─────────────────────────────────────
function bindEventListeners() {
  $("#statusTypeFilter").on("change", () => loadMyLogs(1));
  $("#hideHiddenLogs").on("change", () => loadMyLogs(1));
  $("#uploadBtn").on("click", () => $("#fileInput").click());
  $("#fileInput").on("change", handleFileUpload);
  $("#editForm").on("submit", submitEdit);
  $("#logsBody").on("click", ".btn-edit", openEditModal);
  $("#logsBody").on("click", ".btn-delete", deleteLog);

  $(document).on("click", ".delete-image-btn", function () {
    const url = $(this).siblings("img").data("url");
    uploadedImageUrls = uploadedImageUrls.filter((u) => u !== url);
    $(this).closest(".position-relative").remove();
  });
}

// ─────────────────────────────────────
// [2] 로그 불러오기 / 필터링
// ─────────────────────────────────────

// 서버로부터 사용자 로그 리스트 받아오기
function loadMyLogs(page = 1) {
  currentPage = page;

  const selectedType = $("#statusTypeFilter").val();
  const hideHidden = $("#hideHiddenLogs").is(":checked");

  $.getJSON(
    `/api/status/my`,
    {
      page: page,
      size: 10,
      type: selectedType,
      hideHidden: hideHidden,
    },
    function (response) {
      renderLogs(response.content);
      renderPagination(response.totalPages, response.currentPage);
    }
  );
}

// ─────────────────────────────────────
// [3] 렌더링
// ─────────────────────────────────────

// 필터링된 로그 리스트 DOM에 출력
function renderLogs(logs) {
  logsList = logs;
  const $container = $("#logsBody").empty();
  logs.forEach((log) => {
    $container.append(renderLog(log));
  });
}

// 단일 로그 카드 HTML 생성
function renderLog(log) {
  const badges = [];

  // 기본 뱃지
  if (log.type === "ANSWER")
    badges.push(`<span class="badge bg-primary me-1">요청답변</span>`);
  if (log.type === "FREE_SHARE")
    badges.push(`<span class="badge bg-secondary me-1">자발공유</span>`);

  if (log.selected)
    badges.push('<span class="badge bg-success me-1">✅ 채택됨</span>');
  if (log.hidden)
    badges.push('<span class="badge bg-secondary me-1">🚫 신고 처리</span>');
  if (log.requestClosed)
    badges.push(
      '<span class="badge bg-warning text-dark me-1">🔒 마감됨</span>'
    );

  // 카테고리 뱃지
  if (log.category) {
    const categoryLabel = categoryLabelMap[log.category] || log.category;
    badges.push(
      `<span class="badge bg-info text-dark">${categoryLabel}</span>`
    );
  }

  const relativeTime = getRelativeTime(log.createdAt);

  // 답변 블록 (내가 쓴 답변)
  const mainContentHtml = getMainContentHtml(log);

  // 이미지 HTML
  const imageHtml = getImageCarouselHtml(log);

  // 요청 정보 블록
  const requestInfoHtml = getRequestInfoHtml(log);
  // 수정 삭제 버튼
  const actionButtons = getActionButtons(log);

  return `
    <div class="col-12 mb-3" data-id="${log.id}">
      <div class="card shadow-sm">
        <div class="card-body">
          
          <!-- 상단 뱃지 + 시간 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div>${badges.join(" ")}</div>
            <small class="text-muted">${relativeTime}</small>
          </div>

          <!-- 내가 쓴 답변 -->
          ${mainContentHtml}

          <!-- 이미지 -->
          <div class="mb-3">${imageHtml}</div>

          <!-- 관련 요청 내용 -->
          ${requestInfoHtml}

          <!-- 버튼 -->
          <div class="mt-3 text-end">${actionButtons}</div>
        </div>
      </div>
    </div>
  `;
}

// 페이지 네이션 랜더링
function renderPagination(totalPages, currentPage) {
  const $pagination = $("#pagination").empty();
  for (let i = 1; i <= totalPages; i++) {
    const isActive = i === currentPage ? "active" : "";
    $pagination.append(`
      <li class="page-item ${isActive}">
        <button class="page-link" data-page="${i}">${i}</button>
      </li>
    `);
  }

  // 이벤트 바인딩 (재등록)
  $pagination.find("button").on("click", function () {
    const selectedPage = parseInt($(this).data("page"));
    if (selectedPage !== currentPage) {
      loadMyLogs(selectedPage);
    }
  });
}

// ─────────────────────────────────────
// [4] 수정 모달 관련
// ─────────────────────────────────────

function openEditModal() {
  const $cardWrapper = $(this).closest("[data-id]");
  editingId = $cardWrapper.data("id");
  // logsList에서 해당 로그 정보 찾기
  const log = logsList.find((l) => l.id === editingId);
  if (!log) return;

  $("#editContent").val(log.content);

  // [1] 동적 필드 렌더링
  $("#dynamicFields").html(getDynamicFieldsHTML(log.category, log));
  // [2] 기존 이미지 배열 저장
  uploadedImageUrls = [...(log.imageUrls || [])];
  // [3] 기존 이미지 미리보기 렌더링
  $("#uploadedPreview").html(renderUploadedImages(uploadedImageUrls));

  const modal = new bootstrap.Modal(document.getElementById("editModal"));
  modal.show();
}

// ─────────────────────────────────────
// [5] 이미지 업로드 처리
// ─────────────────────────────────────

function handleFileUpload() {
  const files = this.files;
  if (!files.length) return;

  const formData = new FormData();
  for (const file of files) {
    formData.append("files", file); // 복수 업로드 지원
  }

  $.ajax({
    url: "/api/upload/multi", // 다중 업로드용 엔드포인트
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function (urls) {
      uploadedImageUrls.push(...urls);

      $("#uploadedPreview").append(renderUploadedImages(urls));
    },
    error: function (xhr) {
      alert("업로드 실패: " + xhr.responseText);
    },
  });
}

// ─────────────────────────────────────
// [6] 수정 제출 처리
// ─────────────────────────────────────

// 수정 완료 시 서버에 PUT 요청
function submitEdit(e) {
  e.preventDefault();
  const log = logsList.find((l) => l.id === editingId);
  if (!log) return;

  const updatedData = {
    content: $("#editContent").val(),
    imageUrls: uploadedImageUrls,
    ...extractCategoryFields(log.category),
  };

  $.ajax({
    url: `/api/status/${editingId}`,
    method: "PUT",
    contentType: "application/json",
    data: JSON.stringify(updatedData),
    success: function () {
      alert("수정 완료");
      location.reload();
    },
    error: function (xhr) {
      alert("수정 실패: " + xhr.responseText);
    },
  });
}

// ─────────────────────────────────────
// [7] 삭제 처리
// ─────────────────────────────────────

// 삭제 버튼 클릭 시 삭제 요청
function deleteLog() {
  const $cardWrapper = $(this).closest("[data-id]");
  const id = $cardWrapper.data("id");

  if (confirm("정말 삭제하시겠습니까?")) {
    $.ajax({
      url: `/api/status/${id}`,
      method: "DELETE",
      success: function () {
        $cardWrapper.remove();
      },
      error: function (xhr) {
        alert("삭제 실패: " + xhr.responseText);
      },
    });
  }
}

// ─────────────────────────────────────
// [8] 함수화
// ─────────────────────────────────────

/**
 * 카테고리 필드 값 추출 (수정 제출 시)
 * - submitEdit(e)
 */
function extractCategoryFields(category) {
  const val = (id) => $(`#${id}`).val(); // 간단 유틸
  switch (category) {
    case "WAITING_STATUS":
      return { waitCount: parseInt(val("editWaitCount")) };
    case "BATHROOM":
      return { hasBathroom: val("editHasBathroom") === "true" };
    case "FOOD_MENU":
      return { menuInfo: val("editMenuInfo") };
    case "WEATHER_LOCAL":
      return { weatherNote: val("editWeatherNote") };
    case "STREET_VENDOR":
      return { vendorName: val("editVendorName") };
    case "PHOTO_REQUEST":
      return { photoNote: val("editPhotoNote") };
    case "BUSINESS_STATUS":
      return { isOpen: val("editIsOpen") === "true" };
    case "OPEN_SEAT":
      return { seatCount: parseInt(val("editSeatCount")) };
    case "NOISE_LEVEL":
      return { noiseNote: val("editNoiseNote") };
    case "PARKING":
      return { isParkingAvailable: val("editIsParkingAvailable") === "true" };
    case "CROWD_LEVEL":
      return { crowdLevel: parseInt(val("editCrowdLevel")) };
    case "ETC":
      return { extra: val("editExtra") };
    default:
      return {};
  }
}

/**
 * 상대 시간 포맷 함수
 * - renderLog()
 */
function getRelativeTime(createdAt) {
  const now = new Date();
  const created = new Date(createdAt);
  const diffMs = now - created;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffSec < 60) return "방금 전";
  if (diffMin < 60) return `${diffMin}분 전`;
  if (diffHour < 24) return `${diffHour}시간 전`;
  if (diffDay < 7) return `${diffDay}일 전`;
  return created.toLocaleDateString("ko-KR");
}

/**
 * 수정 삭제 버튼 만들기
 * - renderLog()
 */
function getActionButtons(log) {
  if (log.selected)
    return `<span class="text-muted small">✅ 채택된 답변은 수정/삭제 불가</span>`;
  if (log.requestClosed)
    return `<span class="text-muted small">🔒 마감된 요청에 대한 답변은 수정/삭제 불가</span>`;
  return `
    <button class="btn btn-sm btn-outline-primary btn-edit me-2">수정</button>
    <button class="btn btn-sm btn-outline-danger btn-delete">삭제</button>`;
}

/**
 * 요청 정보 불러오기
 * - renderLog()
 */
function getRequestInfoHtml(log) {
  if (log.type !== "ANSWER" || !log.requestTitle || !log.requestContent)
    return "";
  return `
    <h6 class="fw-bold mt-4 mb-2">📌 관련 요청 내용</h6>
    <div class="bg-light border rounded p-3 mb-2 small">
      <div class="mb-2"><strong>📍 요청 제목:</strong> ${log.requestTitle}</div>
      <div><strong>📄 요청 내용:</strong> ${log.requestContent}</div>
    </div>`;
}

/**
 * 이미지 캐러셀
 * - renderLog()
 */
function getImageCarouselHtml(log) {
  if (!Array.isArray(log.imageUrls) || log.imageUrls.length === 0)
    return `<div class="text-muted small">이미지 없음</div>`;

  const carouselId = `carousel-${log.id}`;
  const indicators =
    log.imageUrls.length > 1
      ? `<div class="carousel-indicators">
        ${log.imageUrls
          .map(
            (_, i) =>
              `<button type="button" data-bs-target="#${carouselId}" data-bs-slide-to="${i}" ${
                i === 0 ? 'class="active"' : ""
              }></button>`
          )
          .join("")}
      </div>`
      : "";

  const slides = log.imageUrls
    .map(
      (url, i) => `
      <div class="carousel-item ${i === 0 ? "active" : ""}">
        <img src="${url}" class="d-block w-100 rounded" style="max-height:200px; object-fit:contain;" />
      </div>
  `
    )
    .join("");

  const controls =
    log.imageUrls.length > 1
      ? `<button class="carousel-control-prev" type="button" data-bs-target="#${carouselId}" data-bs-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </button>
      <button class="carousel-control-next" type="button" data-bs-target="#${carouselId}" data-bs-slide="next">
        <span class="carousel-control-next-icon"></span>
      </button>`
      : "";

  return `<div id="${carouselId}" class="carousel slide" data-bs-ride="carousel">
    ${indicators}
    <div class="carousel-inner">${slides}</div>
    ${controls}
  </div>`;
}

/**
 * 메인 컨텐츠 가져오기
 * - renderLog()
 */
function getMainContentHtml(log) {
  const categorySummary = getCategorySummary(log);
  const placeNameHtml = log.placeName
    ? `<span class="text-primary fw-bold">${log.placeName}</span>`
    : log.customPlaceName
    ? `<span class="text-muted">${log.customPlaceName}</span>`
    : "사용자 지정 위치";

  return `
    <h5 class="fw-bold mt-2 mb-3">✍️ 내가 쓴 답변</h5>
    <div class="bg-light border rounded p-3 mb-3 small">
      <div class="mb-2"><strong>📝 답변 내용:</strong> ${log.content}</div>
      ${
        categorySummary
          ? `<div class="mb-2"><strong>📂 상세 정보:</strong> ${categorySummary}</div>`
          : ""
      }
      <div class="d-flex justify-content-end text-muted small mt-3">
        <div class="text-end">
          <div><strong>장소:</strong> ${placeNameHtml}</div>
          <div><strong>작성자:</strong> ${log.nickname ?? "익명"}</div>
          ${
            log.type === "FREE_SHARE"
              ? `<div><strong>조회수:</strong> ${log.viewCount ?? 0}</div>`
              : ""
          }
        </div>
      </div>
    </div>`;
}

/**
 * 카테고리 값 가져오기
 * - getMainContentHtml()
 */
function getCategorySummary(log) {
  switch (log.category) {
    case "WAITING_STATUS":
      return `현재 대기 인원: ${log.waitCount ?? "-"}명`;
    case "FOOD_MENU":
      return `오늘의 메뉴: ${log.menuInfo ?? "정보 없음"}`;
    case "BATHROOM":
      return `화장실 있음 여부: ${log.hasBathroom ? "있음" : "없음"}`;
    case "PARKING":
      return `주차 가능 여부: ${log.isParkingAvailable ? "가능" : "불가능"}`;
    case "NOISE_LEVEL":
      return `소음 상태: ${log.noiseNote ?? "정보 없음"}`;
    case "CROWD_LEVEL":
      return `혼잡도: ${log.crowdLevel ?? "-"} / 10`;
    case "WEATHER_LOCAL":
      return `날씨 메모: ${log.weatherNote ?? "정보 없음"}`;
    case "STREET_VENDOR":
      return `노점 이름: ${log.vendorName ?? "정보 없음"}`;
    case "PHOTO_REQUEST":
      return `요청 메모: ${log.photoNote ?? "없음"}`;
    case "BUSINESS_STATUS":
      return `영업 여부: ${log.isOpen ? "영업 중" : "영업 안 함"}`;
    case "OPEN_SEAT":
      return `남은 좌석 수: ${log.seatCount ?? "-"}석`;
    case "ETC":
      return `기타 정보: ${log.extra ?? "없음"}`;
    default:
      return "";
  }
}

/**
 * 업로드 이미지 미리보기 블록 렌더링
 * - openEditModal() / handleFileUpload()
 */
function renderUploadedImages(urls) {
  return urls
    .map(
      (url) => `
      <div class="position-relative d-inline-block">
        <img src="${url}" data-url="${url}" class="me-2 mb-2 img-thumbnail" style="max-width:100px;" />
        <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn"
                style="background-color: rgba(0,0,0,0.6); color: white;" title="삭제"></button>
      </div>`
    )
    .join("");
}

/**
 * 카테고리별 필드 동적 삽입 HTML 반환
 * - openEditModal()
 */
function getDynamicFieldsHTML(category, log) {
  switch (category) {
    case "WAITING_STATUS":
      return `
        <div class="mb-3">
          <label>대기 인원</label>
          <input type="number" id="editWaitCount" class="form-control" value="${
            log.waitCount ?? ""
          }" />
        </div>`;
    case "BATHROOM":
      return `
        <div class="mb-3">
          <label>화장실 있음 여부</label>
          <select id="editHasBathroom" class="form-select">
            <option value="true" ${
              log.hasBathroom ? "selected" : ""
            }>있음</option>
            <option value="false" ${
              log.hasBathroom === false ? "selected" : ""
            }>없음</option>
          </select>
        </div>`;
    case "FOOD_MENU":
      return `
        <div class="mb-3">
          <label>메뉴 정보</label>
          <input type="text" id="editMenuInfo" class="form-control" value="${
            log.menuInfo ?? ""
          }" />
        </div>`;
    case "WEATHER_LOCAL":
      return `
        <div class="mb-3">
          <label>날씨 메모</label>
          <input type="text" id="editWeatherNote" class="form-control" value="${
            log.weatherNote ?? ""
          }" />
        </div>`;
    case "STREET_VENDOR":
      return `
        <div class="mb-3">
          <label>노점 이름</label>
          <input type="text" id="editVendorName" class="form-control" value="${
            log.vendorName ?? ""
          }" />
        </div>`;
    case "PHOTO_REQUEST":
      return `
        <div class="mb-3">
          <label>사진 요청 메모</label>
          <input type="text" id="editPhotoNote" class="form-control" value="${
            log.photoNote ?? ""
          }" />
        </div>`;
    case "BUSINESS_STATUS":
      return `
        <div class="mb-3">
          <label>영업 여부</label>
          <select id="editIsOpen" class="form-select">
            <option value="true" ${
              log.isOpen ? "selected" : ""
            }>영업 중</option>
            <option value="false" ${
              log.isOpen === false ? "selected" : ""
            }>영업 안 함</option>
          </select>
        </div>`;
    case "OPEN_SEAT":
      return `
        <div class="mb-3">
          <label>남은 좌석 수</label>
          <input type="number" id="editSeatCount" class="form-control" value="${
            log.seatCount ?? ""
          }" />
        </div>`;
    case "NOISE_LEVEL":
      return `
        <div class="mb-3">
          <label>소음 상태</label>
          <input type="text" id="editNoiseNote" class="form-control" value="${
            log.noiseNote ?? ""
          }" />
        </div>`;
    case "PARKING":
      return `
        <div class="mb-3">
          <label>주차 가능 여부</label>
          <select id="editIsParkingAvailable" class="form-select">
            <option value="true" ${
              log.isParkingAvailable ? "selected" : ""
            }>가능</option>
            <option value="false" ${
              log.isParkingAvailable === false ? "selected" : ""
            }>불가능</option>
          </select>
        </div>`;
    case "CROWD_LEVEL":
      return `
        <div class="mb-3">
          <label>혼잡도</label>
          <input type="number" id="editCrowdLevel" class="form-control" value="${
            log.crowdLevel ?? ""
          }" />
        </div>`;
    case "ETC":
      return `
        <div class="mb-3">
          <label>기타 정보</label>
          <textarea id="editExtra" class="form-control">${
            log.extra ?? ""
          }</textarea>
        </div>`;
    default:
      return "";
  }
}
