// 현재 수정 중인 로그의 ID
let editingId = null;
// 새로 업로드된 이미지의 URL
let uploadedImageUrl = null;
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
  if (log.type === "ANSWER") badges.push(`<span class="badge bg-primary me-1">요청답변</span>`);
  if (log.type === "FREE_SHARE") badges.push(`<span class="badge bg-secondary me-1">자발공유</span>`);

  if (log.selected)
    badges.push('<span class="badge bg-success me-1">✅ 채택됨</span>');
  if (log.hidden)
    badges.push('<span class="badge bg-secondary me-1">🚫 신고 처리</span>');
  if (log.requestClosed)
    badges.push('<span class="badge bg-warning text-dark me-1">🔒 마감됨</span>');

  // 카테고리 뱃지
  if (log.category) {
    const categoryLabel = categoryLabelMap[log.category] || log.category;
    badges.push(`<span class="badge bg-info text-dark">${categoryLabel}</span>`);
  }

  const imageHtml = log.imageUrl
    ? `<img src="${log.imageUrl}" class="img-fluid rounded border" style="max-height:150px;" />`
    : `<div class="text-muted small">이미지 없음</div>`;

  const relativeTime = getRelativeTime(log.createdAt);

  const requestInfoHtml =
    log.type === "ANSWER" && log.requestTitle && log.requestContent
      ? `<div class="bg-light p-2 rounded mb-2 small">
           <strong class="d-block">📌 요청 정보</strong>
           <div><strong>제목:</strong> ${log.requestTitle}</div>
           <div><strong>내용:</strong> ${log.requestContent}</div>
         </div>`
      : "";

  const categorySummary = getCategorySummary(log);

  return `
    <div class="col-12 mb-3" data-id="${log.id}">
      <div class="card shadow-sm">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div>${badges.join(" ")}</div>
            <small class="text-muted">${relativeTime}</small>
          </div>

          <h5 class="mb-2">📝 답변 내용</h5>
          <p class="mb-1">${log.content}</p>
          ${
            categorySummary
              ? `<p class="text-muted small mb-2">${categorySummary}</p>`
              : ""
          }

          ${requestInfoHtml}

          <div class="mb-3">${imageHtml}</div>

          <div class="text-muted small mb-3">
            작성자: ${log.nickname ?? "익명"} |
            장소: ${
              log.placeName
                ? `<span class="text-primary fw-bold">${log.placeName}</span>`
                : log.customPlaceName
                ? `<span class="text-muted">${log.customPlaceName}</span>`
                : "사용자 지정 위치"
            } 
            ${
              log.type === "FREE_SHARE"
                ? `| 조회수: ${log.viewCount ?? 0}`
                : ""
            }
          </div>

          <div class="text-end">
            ${
              log.selected
                ? `<span class="text-muted small">✅ 채택된 답변은 수정/삭제 불가</span>`
                : log.requestClosed
                ? `<span class="text-muted small">🔒 마감된 요청에 대한 답변은 수정/삭제 불가</span>`
                : `
                  <button class="btn btn-sm btn-outline-primary btn-edit me-2">수정</button>
                  <button class="btn btn-sm btn-outline-danger btn-delete">삭제</button>
                `
            }
          </div>
        </div>
      </div>
    </div>
  `;
}

// 카테고리 값 가져오기
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

// 상대 시간 포맷 함수
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
  uploadedImageUrl = null;
  $("#uploadedImage").empty();
  $("#dynamicFields").html(getDynamicFieldsHTML(log.category, log));

  const modal = new bootstrap.Modal(document.getElementById("editModal"));
  modal.show();
}

// 카테고리별 필드 동적 삽입 HTML 반환
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

// ─────────────────────────────────────
// [5] 이미지 업로드 처리
// ─────────────────────────────────────

function handleFileUpload() {
  const file = this.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  $.ajax({
    url: "/api/upload",
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function (url) {
      uploadedImageUrl = url;
      $("#uploadedImage").html(`<img src="${url}" style="max-width:100px;" />`);
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
    imageUrl: uploadedImageUrl,
  };

  switch (log.category) {
    case "WAITING_STATUS":
      updatedData.waitCount = parseInt($("#editWaitCount").val());
      break;
    case "BATHROOM":
      updatedData.hasBathroom = $("#editHasBathroom").val() === "true";
      break;
    case "FOOD_MENU":
      updatedData.menuInfo = $("#editMenuInfo").val();
      break;
    case "WEATHER_LOCAL":
      updatedData.weatherNote = $("#editWeatherNote").val();
      break;
    case "STREET_VENDOR":
      updatedData.vendorName = $("#editVendorName").val();
      break;
    case "PHOTO_REQUEST":
      updatedData.photoNote = $("#editPhotoNote").val();
      break;
    case "BUSINESS_STATUS":
      updatedData.isOpen = $("#editIsOpen").val() === "true";
      break;
    case "OPEN_SEAT":
      updatedData.seatCount = parseInt($("#editSeatCount").val());
      break;
    case "NOISE_LEVEL":
      updatedData.noiseNote = $("#editNoiseNote").val();
      break;
    case "PARKING":
      updatedData.isParkingAvailable =
        $("#editIsParkingAvailable").val() === "true";
      break;
    case "CROWD_LEVEL":
      updatedData.crowdLevel = parseInt($("#editCrowdLevel").val());
      break;
    case "ETC":
      updatedData.extra = $("#editExtra").val();
      break;
  }

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
