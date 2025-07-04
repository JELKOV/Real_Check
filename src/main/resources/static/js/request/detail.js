// 카테고리 관련 import
import { getCategoryLabel } from "./util/categoryUtils.js";

// 이미지 관련 import
import {
  handleImageFileChange,
  renderImagePreview,
  handleImageRemoveBtn,
  handleImagePreviewModal,
} from "./util/imageUtils.js";

// 신고 관련 import
import {
  updateReportButton,
  handleReportToggle,
  handleSubmitReportReason,
} from "./util/reportUtils.js";

// 파싱함수 import
import { parseFieldValue } from "../util/common.js";

// 폼 관련련 import
import {
  renderAnswerFields,
  bindCustomSelectEvents,
  renderExtraAnswerFields,
  manageAnswerFormVisibility,
} from "./util/formUtils.js";

// 지도 렌더링
import { renderMap } from "./util/mapUtils.js";

// ─────────────────────────────────────────────────────────────
// [0] 전역 변수 및 유틸 함수
// ─────────────────────────────────────────────────────────────
let loginUserIdNum = null;
let requestId = null;
let editingId = null;
let uploadedImageUrls = [];
let requestLat = null;
let requestLng = null;
let placeId = null;

// ─────────────────────────────────────────────────────────────
// [1] 문서 준비 및 이벤트 바인딩
// ─────────────────────────────────────────────────────────────
$(document).ready(function () {
  // [1-1] 로그인 사용자 ID 파싱
  const loginUserIdElement = document.getElementById("loginUserId");

  if (!loginUserIdElement) {
    console.error("로그인된 사용자 ID 요소를 찾을 수 없습니다.");
  } else {
    const loginUserId = loginUserIdElement.value;
    loginUserIdNum = Number(loginUserId);
  }

  // [1-2] 요청 ID 파싱
  requestId = document.getElementById("requestId").value;

  // [1-3] 이벤트 바인딩
  bindEventListeners();

  // [1-4] 커스텀 셀렉트 이벤트 바인딩 (한 번만)
  bindCustomSelectEvents();

  // [1-5] 요청 상세 및 답변 리스트 초기 로드
  loadRequestDetail(requestId, function (request) {
    loadAnswerList(requestId, request);
  });
});

// 이벤트 리스너 바인딩 함수
function bindEventListeners() {
  // (1) 등록 / 제출 관련

  // 답변 등록 처리
  $("#answerForm").on("submit", submitAnswer);

  // 사진 올리기 (파일선택) 클릭
  $(document).on("change", "#fileInput", (e) => {
    handleImageFileChange(e, uploadedImageUrls, () => {
      // 이미지 미리보기 처리
      renderImagePreview("#uploadedPreview", uploadedImageUrls);
    });
  });

  // 수정용 이미지 업로드 핸들러
  $(document).on("change", ".edit-file-input", handleEditImageUpload);

  // (2) 수정 관련

  // 답변 수정 버튼 클릭 (수정 모드 활성화)
  $(document).on("click", ".edit-answer-btn", activateEditMode);
  // 답변 수정 저장 버튼 클릭
  $(document).on("click", ".save-edit-btn", saveEditedAnswer);
  // 답변 수정 취소 버튼 클릭
  $(document).on("click", ".cancel-edit-btn", cancelEditMode);
  // 답변 삭제 버튼 클릭
  $(document).on("click", ".delete-answer-btn", deleteAnswer);

  // (3) 채택 및 마감

  // 답변 채택 버튼 클릭
  $(document).on("click", ".select-answer-btn", handleSelectAnswer);

  //(4) 신고 관련

  // 신고 버튼 클릭 (toggle 방식으로 대체)
  $(document).on("click", ".report-toggle-btn", function () {
    handleReportToggle.call(this, loadAnswerList, requestId);
  });

  // 신고 사유 선택 후 전송 버튼 클릭
  $(document).on("click", "#submitReportBtn", function () {
    handleSubmitReportReason(loadAnswerList, requestId);
  });

  // (5) 이미지 관련

  // 수정 이미지 삭제 버튼 클릭 + 초기 업로드 삭제도 포함
  $(document).on("click", ".delete-image-btn", (e) =>
    handleImageRemoveBtn(e, uploadedImageUrls)
  );
  // 이미지 미리보기 (확대) 핸들러
  $(document).on(
    "click",
    ".carousel-image, .img-thumbnail",
    handleImagePreviewModal
  );
}

// ─────────────────────────────────────────────────────────────
// [2] 요청 상세 정보 로딩 및 렌더링
// ─────────────────────────────────────────────────────────────

// [2-1] 상세 정보 관련 전체 로드 헬퍼 함수
function loadRequestDetail(requestId, callback) {
  $.get(`/api/request/${requestId}`, function (request) {
    renderRequestDetail(request);
    // 답변 입력창 비활성화 조건 확인
    manageAnswerFormVisibility(request, loginUserIdNum);
    // 지도 표시
    renderMap(
      request.lat,
      request.lng,
      request.title,
      request.placeName || request.customPlaceName || ""
    );
    renderAnswerFields(request.category);

    // 사용자지정 요청인 경우에만 위경도 저장
    if (!request.placeId && request.lat && request.lng) {
      requestLat = request.lat;
      requestLng = request.lng;
    }
    // placeId도 전역 변수에 저장 (공식 장소 여부 판별용)
    placeId = request.placeId;

    // request 정보 전달
    if (typeof callback === "function") {
      callback(request);
    }
  });
}

// [2-2] 요청 상세 정보 렌더링
function renderRequestDetail(request) {
  const formattedDate = new Date(request.createdAt).toLocaleString();
  const nickname = request.requesterNickname || "익명";
  const location =
    request.placeName || request.customPlaceName || "장소 정보 없음";
  const isRequester = loginUserIdNum === request.requesterId;
  const canCloseManually =
    isRequester && !request.closed && request.visibleAnswerCount === 0;

  const closedBadge = request.closed
    ? `<span class="badge bg-danger ms-2">🔒 마감</span>`
    : "";
  const categoryBadge = `<span class="badge bg-primary">${getCategoryLabel(
    request.category
  )}</span>`;

  const html = `
    <div class="card mt-3 ${
      request.closed ? "bg-light text-muted border-start border-danger" : ""
    }">
      <div class="card-body position-relative">
        <div class="d-flex justify-content-between align-items-start">
          <h4 class="card-title">${request.title} ${closedBadge}</h4>
          <div class="badge-container position-absolute top-0 end-0 mt-2 me-2">
            ${categoryBadge}
          </div>
        </div>
        <p class="card-text mt-2">${request.content}</p>

        <hr class="my-3">

        <ul class="list-unstyled mt-2">
          <li class="mb-2 d-flex align-items-center">
            <i class="bi bi-currency-exchange me-2 text-primary"></i>
            <strong>포인트:</strong> <span class="ms-2">${
              request.point
            }pt</span>
          </li>
          <li class="mb-2 d-flex align-items-center">
            <i class="bi bi-geo-alt-fill me-2 text-primary"></i>
            <strong>장소:</strong> <span class="ms-2">${location}</span>
          </li>
          <li class="mb-2 d-flex align-items-center">
            <i class="bi bi-person-fill me-2 text-primary"></i>
            <strong>작성자:</strong> <span class="ms-2">${nickname}</span>
          </li>
          <li class="d-flex align-items-center">
            <i class="bi bi-calendar-check-fill me-2 text-primary"></i>
            <strong>작성일:</strong> <span class="ms-2">${formattedDate}</span>
          </li>
        </ul>

        <div class="position-absolute bottom-0 end-0 mb-2 me-2">
          ${
            canCloseManually
              ? '<button id="closeRequestBtn" class="btn btn-danger">마감하기</button>'
              : ""
          }
        </div>
      </div>
    </div>
  `;

  $("#requestDetail").html(html);

  // 마감하기 버튼 클릭 이벤트 바인딩
  if (canCloseManually) {
    $("#closeRequestBtn").on("click", function () {
      closeRequestManually(requestId);
    });
  }
}

// [2-2-1] 요청 수동 마감 처리
function closeRequestManually(requestId) {
  if (!confirm("이 요청을 마감하시겠습니까?")) return;

  $.ajax({
    url: `/api/request/${requestId}/close`,
    method: "PATCH",
    // 사용자 ID 전달
    data: { userId: loginUserIdNum },
    success: function () {
      alert("요청이 마감되었습니다.");
      // 마감 상태 갱신
      loadRequestDetail(requestId);
      loadAnswerList(requestId);
    },
    error: function (xhr) {
      alert("마감 처리에 실패했습니다: " + xhr.responseText);
    },
  });
}

// ─────────────────────────────────────────────────────────────
// [3] 답변 관련 기능
// ─────────────────────────────────────────────────────────────

// [3-1] 답변 리스트 로드
function loadAnswerList(requestId, request = null) {
  $.get(`/api/status/by-request/${requestId}`, function (answers) {
    $("#answerList").empty();
    if (answers.length === 0) {
      $("#answerList").html(
        '<li class="list-group-item">등록된 답변이 없습니다.</li>'
      );
      $("#autoCloseNotice").empty();
      return;
    }

    const hasSelected = answers.some((a) => a.selected);

    const visibleAnswers = answers
      .filter((answer) => !answer.hidden)
      .sort((a, b) => (b.selected ? 1 : 0) - (a.selected ? 1 : 0));

    visibleAnswers.forEach((answer) => {
      const row = generateAnswerRow(answer, hasSelected, false);
      $("#answerList").append(row);
    });

    visibleAnswers.forEach((answer) => {
      const canReport =
        loginUserIdNum !== null &&
        loginUserIdNum !== answer.userId &&
        !answer.selected &&
        !answer.requestClosed;

      if (canReport) {
        updateReportButton(answer.id, answer.reportCount);
      }
    });

    // request 정보 전달되었을 경우만 마감 안내 표시
    if (request && !request.closed && loginUserIdNum === request.requesterId) {
      const oldestAnswer = answers.reduce((oldest, current) => {
        return new Date(current.createdAt) < new Date(oldest.createdAt)
          ? current
          : oldest;
      }, answers[0]);

      updateAutoCloseNotice(answers.length, oldestAnswer?.createdAt);
    }
  });
}

// [3-2] 답변 자동 마감 안내
function updateAutoCloseNotice(visibleAnswerCount, createdAtStr) {
  const $notice = $("#autoCloseNotice");
  $notice.empty();

  if (visibleAnswerCount === 0 || !createdAtStr) return;

  const createdAt = new Date(createdAtStr);
  if (isNaN(createdAt.getTime())) return;

  const expireTime = new Date(createdAt.getTime() + 3 * 60 * 60 * 1000);
  const now = new Date();
  const msRemaining = expireTime - now;

  if (msRemaining <= 0) {
    $notice.html(`
      <div class="alert alert-danger mt-3">
        ⌛ 답변 마감 시간이 지났습니다. 포인트가 자동 분배되었을 수 있습니다.
      </div>
    `);
    return;
  }

  const totalMinutes = Math.floor(msRemaining / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  $notice.html(`
    <div class="alert alert-warning mt-3">
      ⏳ <strong>${hours}시간 ${minutes}분</strong> 후 이 요청이 자동 마감되며,
      <span class="text-danger fw-bold">포인트가 답변자들에게 자동 분배</span>됩니다.
    </div>
  `);
}

// [3-3] 답변 행 생성 함수
function generateAnswerRow(answer, hasSelected, isEditing = false) {
  const nickname = answer.nickname || "익명 사용자";
  const selectedBadge = answer.selected
    ? `<span class="badge bg-success ms-2">✅ 채택됨</span>`
    : "";

  // 신고 수 뱃지
  const reportBadge =
    answer.reportCount > 0
      ? `<span class="badge bg-danger ms-2">🚨 ${answer.reportCount}회 신고</span>`
      : "";

  // 날짜 표시
  const formattedDate = new Date(answer.createdAt).toLocaleString("ko-KR");

  // 수정 삭제 채택 신고 버튼
  const actionButtons = generateActionButtons(answer, hasSelected, isEditing);

  // 텍스트 영역: 수정 모드 여부에 따라 분기
  const textHtml = isEditing
    ? `<textarea class="form-control edit-answer-input" data-id="${answer.id}">${answer.content}</textarea>`
    : `<p id="answer-text-${answer.id}">${answer.content}</p>`;

  // 동적 필드
  const extraFieldsHtml = renderExtraAnswerFields(answer, isEditing);

  // 이미지 영역
  let imageHtml = "";
  if (isEditing) {
    const uploaded = (answer.imageUrls || [])
      .map(
        (url) => `
        <div class="position-relative d-inline-block">
          <img src="${url}" data-old="true" data-url="${url}" class="me-2 mb-2 img-thumbnail" style="max-width:100px;" />
          <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn"
                  style="background-color: rgba(0,0,0,0.6); color: white;" title="삭제"></button>
        </div>
      `
      )
      .join("");

    imageHtml = `
      <label class="mt-2">이미지 수정</label>
      <input type="file" class="form-control edit-file-input" accept="image/*" multiple />
      <div class="edit-uploaded-preview mt-2 d-flex flex-wrap gap-2">
        ${uploaded}
      </div>
    `;
  } else if (Array.isArray(answer.imageUrls) && answer.imageUrls.length > 0) {
    const carouselId = `carousel-${answer.id}`;
    const indicators = answer.imageUrls
      .map(
        (_, i) =>
          `<button type="button" data-bs-target="#${carouselId}" data-bs-slide-to="${i}" ${
            i === 0 ? "class='active'" : ""
          } aria-label="Slide ${i + 1}"></button>`
      )
      .join("");

    const slides = answer.imageUrls
      .map(
        (url, i) =>
          `<div class="carousel-item ${i === 0 ? "active" : ""}">
            <img src="${url}" class="d-block w-100 img-thumbnail carousel-image" style="max-height: 200px; object-fit: contain;" data-url="${url}" />
          </div>`
      )
      .join("");

    imageHtml = `
      <div id="${carouselId}" class="carousel slide mt-2" data-bs-ride="carousel">
        <div class="carousel-indicators">${indicators}</div>
        <div class="carousel-inner">${slides}</div>
        ${
          answer.imageUrls.length > 1
            ? `
          <button class="carousel-control-prev" type="button" data-bs-target="#${carouselId}" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#${carouselId}" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
          </button>`
            : ""
        }
      </div>
    `;
  }

  return `
    <li class="list-group-item answer-item" data-answer-data='${JSON.stringify(
      answer
    )}'>
      <strong>${nickname}</strong> ${selectedBadge} ${reportBadge}
      ${textHtml}
      <div class="dynamic-fields">
        ${extraFieldsHtml}
        ${imageHtml}
      </div>
      <br><small class="text-muted">${formattedDate}</small>
      ${actionButtons}
    </li>
  `;
}

// [3-4] 답변 채택 가능 여부 판단 함수
function canSelectAnswer(answer, hasSelected) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (answer.selected) return false; // 이미 선택된 답변은 선택 불가
  if (hasSelected) return false; // 이미 다른 답변이 선택된 경우
  if (loginUserIdNum !== answer.requestOwnerId) return false; // 요청 작성자만 채택 가능
  if (answer.requestClosed) return false; // 요청 마감된 경우 불가
  return true;
}

// [3-5] 답변 수정/삭제 가능 여부 판단 함수
function canEditOrDeleteAnswer(answer) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (loginUserIdNum !== answer.userId) return false; // 답변 작성자만 수정/삭제 가능
  if (answer.selected) return false; // 채택된 답변은 수정/삭제 불가
  if (answer.requestClosed) return false; // 마감된 요청이면 불가
  return true;
}

// [3-6] 수정/삭제/신고 버튼 생성 함수
function generateActionButtons(answer, hasSelected, isEditing = false) {
  if (isEditing) {
    return `
      <div class="save-cancel-buttons mt-2 d-flex gap-2">
        <button class="btn btn-primary save-edit-btn" data-id="${answer.id}">저장</button>
        <button class="btn btn-secondary cancel-edit-btn" data-id="${answer.id}">취소</button>
      </div>
    `;
  }
  const canSelect = canSelectAnswer(answer, hasSelected);
  const canEditOrDelete = canEditOrDeleteAnswer(answer);
  const canReport =
    loginUserIdNum !== null &&
    loginUserIdNum !== answer.userId &&
    !answer.selected &&
    !answer.requestClosed;

  const selectButton = canSelect
    ? `<button class="btn btn-sm btn-outline-success select-answer-btn" data-id="${answer.id}">✅ 채택</button>`
    : "";

  const editDeleteButtons = canEditOrDelete
    ? `
      <button class="btn btn-sm btn-warning edit-answer-btn" data-id="${answer.id}">✏️</button>
      <button class="btn btn-sm btn-danger delete-answer-btn" data-id="${answer.id}">🗑️</button>
    `
    : "";

  const reportButton = canReport
    ? `<button class="btn btn-sm btn-secondary report-toggle-btn" data-id="${answer.id}" disabled>🚨 신고</button>`
    : "";

  return `
    <div class="edit-delete-buttons d-flex flex-wrap gap-2 mt-2">
      ${selectButton}
      ${editDeleteButtons}
      ${reportButton}
    </div>
  `;
}

// [3-7] 수정 - 이미지 업로드 로직
function handleEditImageUpload() {
  const files = this.files;
  const $preview = $(this).closest("li").find(".edit-uploaded-preview");
  if (files.length === 0) return;

  const formData = new FormData();
  for (let i = 0; i < files.length; i++) {
    formData.append("files", files[i]);
  }

  $.ajax({
    url: "/api/upload/multi",
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function (res) {
      res.forEach((url) => {
        $preview.append(`
          <div class="position-relative d-inline-block">
            <img src="${url}" data-url="${url}" class="img-thumbnail me-2 mb-2" style="max-width:100px;" />
            <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn"
                    style="background-color: rgba(0,0,0,0.6); color: white;" title="삭제"></button>
          </div>
        `);
      });
    },
    error: function (xhr) {
      alert("이미지 업로드 실패: " + xhr.responseText);
    },
  });
}

// [3-8] 답변 수정 모드 활성화
function activateEditMode() {
  const answerId = $(this).data("id");
  editingId = answerId;

  // 기존 답변 리스트 → 해당 답변만 다시 렌더링
  $.get(`/api/status/by-request/${requestId}`, function (answers) {
    const hasSelected = answers.some((a) => a.selected);
    const visibleAnswers = answers.filter((a) => !a.hidden);

    // answerList 전체 비우고 다시 그리되, 수정 중인 항목만 수정모드로
    $("#answerList").empty();
    visibleAnswers.forEach((answer) => {
      const rowHtml =
        answer.id === editingId
          ? generateAnswerRow(answer, hasSelected, true) // 수정모드 표시
          : generateAnswerRow(answer, hasSelected, false); // 일반모드
      $("#answerList").append(rowHtml);
    });

    // 신고 버튼 상태 갱신
    visibleAnswers.forEach((answer) => {
      const canReport =
        loginUserIdNum !== null &&
        loginUserIdNum !== answer.userId &&
        !answer.selected &&
        !answer.requestClosed;
      if (canReport) {
        updateReportButton(answer.id, answer.reportCount);
      }
    });
  });
}

// [3-9] 수정 취소 처리
function cancelEditMode() {
  editingId = null;

  // 원래 answer 다시 렌더링
  loadAnswerList(requestId);
}

// [3-10] 수정 저장 처리
function saveEditedAnswer() {
  const answerId = $(this).data("id");
  const answerRow = $(`li[data-answer-data*='"id":${answerId}']`);
  const newText = answerRow.find(".edit-answer-input").val().trim();
  const extraFields = {};

  // 1. 동적 필드 값 수집
  answerRow.find(".edit-extra-input").each(function () {
    const input = $(this);
    const field = input.data("field");
    const value = parseFieldValue(input);
    if (value !== null) extraFields[field] = value;
  });

  // 2. 본문 텍스트 검증
  if (!newText) {
    alert("내용을 입력해주세요.");
    return;
  }

  // 3. 이미지 URL 수집 (최신 DOM 기준)
  const imageUrls = answerRow
    .find(".edit-uploaded-preview img")
    .map(function () {
      return $(this).attr("data-url"); // 주의: .data("url") 말고 attr() 사용 추천
    })
    .get();

  // 4. 전송 DTO 구성
  const dto = {
    content: newText,
    imageUrls,
    ...extraFields,
  };

  // 5. 서버에 PUT 요청
  $.ajax({
    url: `/api/status/${answerId}`,
    method: "PUT",
    contentType: "application/json",
    data: JSON.stringify(dto),
    success: function () {
      alert("답변이 수정되었습니다.");
      loadAnswerList(requestId); // 수정 후 목록 다시 로드
    },
    error: function (xhr) {
      alert("수정 실패: " + xhr.responseText);
    },
  });
}

// [3-11] 답변 삭제 처리
function deleteAnswer() {
  const answerId = $(this).data("id");
  if (!confirm("정말로 이 답변을 삭제하시겠습니까?")) return;

  $.ajax({
    url: `/api/status/${answerId}`,
    method: "DELETE",
    success: function () {
      alert("답변이 삭제되었습니다.");
      loadAnswerList(requestId);
    },
    error: function (xhr) {
      alert("답변 삭제 실패: " + xhr.responseText);
    },
  });
}

// ─────────────────────────────────────────────────────────────
// [4] 응답 필드 표시용
// ─────────────────────────────────────────────────────────────

/**
 * [4-1] 답변 채택 버튼 처리
 */
function handleSelectAnswer() {
  const statusLogId = $(this).data("id");
  if (!confirm("이 답변을 채택하시겠습니까?")) return;

  // UI 즉시 반응 (새로고침 없이)
  loadAnswerList(requestId);

  // 서버 요청 (비동기)
  $.post(`/api/status/select/${statusLogId}`)
    .done(() => {
      loadRequestDetail(requestId); // 요청 상태 갱신 (마감)
      loadAnswerList(requestId); // 답변 목록 갱신
    })
    .fail((xhr) => {
      alert("채택 실패: " + xhr.responseText);
    });
}

/**
 * [4-2] 답변 제출 처리 (중복 방지)
 */
function submitAnswer(e) {
  e.preventDefault();
  const content = $("#answerContent").val();
  const dto = {
    content,
    requestId,
    imageUrls: uploadedImageUrls,
  };

  // 사용자지정 요청이라면 위도/경도 포함
  if (!placeId && requestLat && requestLng) {
    dto.lat = requestLat;
    dto.lng = requestLng;
  }

  // 유연 필드 동적 추가 (카테고리별 필드)
  $("#dynamicAnswerFields")
    .find("input, select")
    .each(function () {
      const input = $(this);
      const name = input.attr("name");
      const value = parseFieldValue(input);
      if (value !== null) dto[name] = value;
    });

  // 사용자 중복 답변 확인
  $.get(`/api/status/by-request/${requestId}`, function (answers) {
    const hasAnswered = answers.some((a) => a.userId === loginUserIdNum);

    if (hasAnswered) {
      alert("이미 이 요청에 답변을 등록하셨습니다.");
      return;
    }

    // 중복이 아니라면 답변 등록
    $.ajax({
      url: `/api/answer/${requestId}`,
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(dto),
      success: function () {
        alert("답변이 등록되었습니다.");
        $("#answerContent").val(""); // 입력 필드 초기화
        $("#fileInput").val(""); // 파일 input 초기화
        $("#uploadedPreview").empty(); // 미리보기 제거
        uploadedImageUrls = []; // 사진 배열 초기화

        loadRequestDetail(requestId, function (request) {
          loadAnswerList(requestId, request); // 안내 문구 포함됨
        }); // 답변 카운트 갱신
      },
      error: function (xhr) {
        alert("답변 등록 실패: " + xhr.responseText);
      },
    });
  });
}
