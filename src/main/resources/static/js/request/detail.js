let loginUserIdNum = null;
let requestId = null;

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
  const loginUserIdElement = document.getElementById("loginUserId");

  if (!loginUserIdElement) {
    console.error("로그인된 사용자 ID 요소를 찾을 수 없습니다.");
  } else {
    const loginUserId = loginUserIdElement.value;
    loginUserIdNum = Number(loginUserId);
  }

  requestId = document.getElementById("requestId").value;

  // 이벤트 바인딩
  bindEventListeners();

  // 초기 데이터 로드
  loadRequestDetail(requestId);
  loadAnswerList(requestId);
});

/**
 * [1] 이벤트 리스너 바인딩
 */
function bindEventListeners() {
  // 답변 채택 버튼 클릭
  $(document).on("click", ".select-answer-btn", handleSelectAnswer);

  // 답변 등록 처리
  $("#answerForm").on("submit", submitAnswer);

  // 요청 마감 버튼 클릭
  $(document).on("click", "#closeRequestBtn", function () {
    closeRequest();
  });

  // 답변 수정 버튼 클릭 (수정 모드 활성화)
  $(document).on("click", ".edit-answer-btn", activateEditMode);

  // 답변 수정 저장 버튼 클릭
  $(document).on("click", ".save-edit-btn", saveEditedAnswer);

  // 답변 수정 취소 버튼 클릭
  $(document).on("click", ".cancel-edit-btn", cancelEditMode);

  // 답변 삭제 버튼 클릭
  $(document).on("click", ".delete-answer-btn", deleteAnswer);

  // 신고 버튼 클릭 (답변 신고)
  $(document).on("click", ".report-answer-btn", handleReportButtonClick);
}

/**
 * [2] 요청 상세 정보 로드
 */

// [2-1] 상세 정보 관련 전체 로드 헬퍼 함수
function loadRequestDetail(requestId) {
  $.get(`/api/request/${requestId}`, function (request) {
    renderRequestDetail(request);

    // 답변 입력창 비활성화 조건 확인
    manageAnswerFormVisibility(request);

    // 지도 표시
    renderMap(request.lat, request.lng);
    renderAnswerFields(request.category);
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
    isRequester && !request.closed && request.answerCount === 0;

  const closedBadge = request.closed
    ? `<span class="badge bg-danger ms-2">🔒 마감</span>`
    : "";
  const categoryBadge = `<span class="badge bg-primary">${
    categoryLabelMap[request.category] || request.category
  }</span>`;

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

// [2-3] 지도 표시 함수
function renderMap(lat, lng) {
  if (lat && lng) {
    const map = new naver.maps.Map("map", {
      center: new naver.maps.LatLng(lat, lng),
      zoom: 16,
    });
    new naver.maps.Marker({
      position: new naver.maps.LatLng(lat, lng),
      map: map,
    });
  } else {
    $("#map").text("위치 정보가 없습니다.");
  }
}

// [2-4] 답변 입력창 비활성화 관리
function manageAnswerFormVisibility(request) {
  let disableReason = "";

  if (request.closed) {
    disableReason = "🔒 이 요청은 마감되었습니다.";
  } else if (request.answerCount >= 3) {
    disableReason = "🚫 최대 답변 수(3개)에 도달했습니다.";
  } else if (loginUserIdNum === request.requesterId) {
    disableReason = "🙋 요청 작성자는 답변을 등록할 수 없습니다.";
  }

  if (disableReason) {
    $("#answerFormSection").hide();
    $("#answerFormSection").before(`
      <div class="alert alert-warning mt-3">
        ${disableReason}
      </div>
    `);
  } else {
    $("#answerFormSection").show();
    $(".alert-warning").remove();
  }
}

// [2-5] 유틸 함수: 카테고리별 동적 필드 생성
function renderAnswerFields(category) {
  const container = $("#dynamicAnswerFields");
  container.empty();

  const fieldMap = {
    PARKING: {
      label: "주차 가능 여부",
      name: "isParkingAvailable",
      type: "select",
      options: [
        { value: "true", text: "가능" },
        { value: "false", text: "불가능" },
      ],
    },
    WAITING_STATUS: {
      label: "대기 인원",
      name: "waitCount",
      type: "number",
    },
    CROWD_LEVEL: {
      label: "혼잡도",
      name: "waitCount",
      type: "number",
    },
    BATHROOM: {
      label: "화장실 여부",
      name: "hasBathroom",
      type: "select",
      options: [
        { value: "true", text: "있음" },
        { value: "false", text: "없음" },
      ],
    },
    FOOD_MENU: {
      label: "메뉴 정보",
      name: "menuInfo",
      type: "text",
    },
    WEATHER_LOCAL: {
      label: "날씨 상태",
      name: "weatherNote",
      type: "text",
    },
    STREET_VENDOR: {
      label: "노점 이름",
      name: "vendorName",
      type: "text",
    },
    PHOTO_REQUEST: {
      label: "사진 요청 메모",
      name: "photoNote",
      type: "text",
    },
    NOISE_LEVEL: {
      label: "소음 상태",
      name: "noiseNote",
      type: "text",
    },
    BUSINESS_STATUS: {
      label: "영업 여부",
      name: "isOpen",
      type: "select",
      options: [
        { value: "true", text: "영업 중" },
        { value: "false", text: "영업 종료" },
      ],
    },
    OPEN_SEAT: {
      label: "남은 좌석 수",
      name: "seatCount",
      type: "number",
    },
    ETC: {
      label: "기타 메모",
      name: "extra",
      type: "text",
    },
  };

  const config = fieldMap[category];
  if (!config) return;

  let fieldHtml = `<div class="mb-3"><label>${config.label}</label>`;
  if (config.type === "select") {
    fieldHtml += `<select class="form-select" name="${config.name}" required>`;
    fieldHtml += `<option value="">선택하세요</option>`;
    config.options.forEach((opt) => {
      fieldHtml += `<option value="${opt.value}">${opt.text}</option>`;
    });
    fieldHtml += `</select>`;
  } else {
    fieldHtml += `<input type="${config.type}" class="form-control" name="${config.name}" required />`;
  }
  fieldHtml += `</div>`;

  container.append(fieldHtml);
}

/**
 * [3] 답변 리스트 함수
 */

// [3-1] 답변 리스트 로드
function loadAnswerList(requestId) {
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

    answers
      .sort((a, b) => (b.selected ? 1 : 0) - (a.selected ? 1 : 0))
      .forEach((answer) => {
        const row = generateAnswerRow(answer, hasSelected);
        $("#answerList").append(row);
      });
    updateAutoCloseNotice(answers.length);
  });
}

// [3-1-1] 답변 자동 마감 안내
function updateAutoCloseNotice(answerCount) {
  if (answerCount > 0) {
    $("#autoCloseNotice").html(`
      <div class="alert alert-warning mt-2">
        ⚠️ 답변이 등록된 이후 3시간 내에 채택되지 않으면 자동 마감되고 포인트가 답변자들에게 분배됩니다.
      </div>
    `);
  } else {
    $("#autoCloseNotice").empty();
  }
}

// [3-2] 답변 행 생성 함수 (버튼 위치 개선)
function generateAnswerRow(answer, hasSelected) {
  const imageHtml = answer.imageUrl
    ? `<img src="${answer.imageUrl}" style="max-width:100px;" class="mt-2" />`
    : "";
  const nickname = answer.nickname || "익명 사용자";
  const selectedBadge = answer.selected
    ? `<span class="badge bg-success ms-2">✅ 채택됨</span>`
    : "";

  // 신고 버튼 (본인 답변은 신고 불가)
  const reportButton = generateReportButton(answer);
  // 수정 삭제 버튼
  const actionButtons = generateActionButtons(answer, hasSelected);
  const formattedDate = new Date(answer.createdAt).toLocaleString("ko-KR");

  return `
    <li class="list-group-item answer-item" data-answer-data='${JSON.stringify(
      answer
    )}'>
      <strong>${nickname}</strong> ${selectedBadge} ${reportButton}
      <p id="answer-text-${answer.id}">${answer.content}</p>
      <small class="text-muted">신고 횟수: ${answer.reportCount}</small>
      <div class="dynamic-fields">
        ${renderExtraAnswerFields(answer)}
      </div>
      ${imageHtml}
      <br><small class="text-muted">${formattedDate}</small>
      ${actionButtons}
    </li>`;
}

/**
 * [3-3] 수정 / 삭제 관련 로직
 */

// [3-3-1] 답변 채택 가능 여부 판단 함수
function canSelectAnswer(answer, hasSelected) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (answer.selected) return false; // 이미 선택된 답변은 선택 불가
  if (hasSelected) return false; // 이미 다른 답변이 선택된 경우
  if (loginUserIdNum !== answer.requestOwnerId) return false; // 요청 작성자만 채택 가능
  return true;
}

// [3-3-2] 답변 수정/삭제 가능 여부 판단 함수
function canEditOrDeleteAnswer(answer) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (loginUserIdNum !== answer.userId) return false; // 답변 작성자만 수정/삭제 가능
  if (answer.selected) return false; // 채택된 답변은 수정/삭제 불가
  return true;
}

// [3-3-3] 수정/삭제 버튼 생성 함수
function generateActionButtons(answer, hasSelected) {
  const canSelect = canSelectAnswer(answer, hasSelected);
  const canEditOrDelete = canEditOrDeleteAnswer(answer);

  return `
    <div class="edit-delete-buttons">
      ${
        canSelect
          ? `<button class="btn btn-sm btn-outline-success select-answer-btn" data-id="${answer.id}">✅ 채택</button>`
          : ""
      }
      ${
        canEditOrDelete
          ? `
          <button class="btn btn-sm btn-warning edit-answer-btn" data-id="${answer.id}">✏️</button>
          <button class="btn btn-sm btn-danger delete-answer-btn" data-id="${answer.id}">🗑️</button>
        `
          : ""
      }
    </div>
    <div class="save-cancel-buttons" style="display:none;">
      <button class="btn btn-primary save-edit-btn" data-id="${
        answer.id
      }">저장</button>
      <button class="btn btn-secondary cancel-edit-btn" data-id="${
        answer.id
      }">취소</button>
    </div>
  `;
}

// [3-3-4] 답변 수정 모드 활성화
function activateEditMode() {
  const answerId = $(this).data("id");
  const answerRow = $(`li[data-answer-data*='"id":${answerId}']`);
  const answerTextElement = answerRow.find(`#answer-text-${answerId}`);
  const originalText = answerTextElement.text().trim();
  const answer = JSON.parse(answerRow.attr("data-answer-data"));

  // 수정 모드로 변경
  answerTextElement.html(`
    <textarea class="form-control edit-answer-input" data-id="${answerId}">${originalText}</textarea>
  `);

  // 동적 필드 수정 가능하게 렌더링
  answerRow.find(".dynamic-fields").html(renderExtraAnswerFields(answer, true));

  // 버튼 토글 (저장/취소 표시, 수정/삭제 숨기기)
  answerRow.find(".edit-delete-buttons").hide();
  answerRow.find(".save-cancel-buttons").show();
}
// [3-3-5] 수정 취소 처리
function cancelEditMode() {
  loadAnswerList(requestId); // 원래 답변 목록으로 되돌리기
}

// [3-3-6] 수정 저장 처리
function saveEditedAnswer() {
  const answerId = $(this).data("id");
  const answerRow = $(`li[data-answer-data*='"id":${answerId}']`);
  const newText = answerRow.find(".edit-answer-input").val().trim();
  const extraFields = {};

  // 동적 필드 값 수집
  answerRow.find(".edit-extra-input").each(function () {
    const field = $(this).data("field");
    extraFields[field] = $(this).val();
  });

  if (!newText) {
    alert("내용을 입력해주세요.");
    return;
  }

  // 수정 데이터 구성
  const dto = { content: newText, ...extraFields };

  $.ajax({
    url: `/api/status/${answerId}`,
    method: "PUT",
    contentType: "application/json",
    data: JSON.stringify(dto),
    success: function () {
      alert("답변이 수정되었습니다.");
      loadAnswerList(requestId); // 수정 후 목록 새로고침
    },
    error: function (xhr) {
      alert("수정 실패: " + xhr.responseText);
    },
  });
}

// [3-3-7] 답변 삭제 처리
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

/**
 * [3-4] 신고 버튼 관련 함수수
 */

// [3-4-1] 신고 버튼 생성 함수
function generateReportButton(answer) {
  const canReport = loginUserIdNum !== answer.userId; // 본인 신고 불가
  if (!canReport) return "";

  return answer.reportCount > 0
    ? `<button class="btn btn-sm btn-outline-danger" disabled>
        🚨 신고됨 (${answer.reportCount})
      </button>`
    : `<button class="btn btn-sm btn-danger report-answer-btn" data-id="${answer.id}">
        🚨 신고
      </button>`;
}

// [3-4-2] 신고 버튼 클릭 처리
function handleReportButtonClick() {
  const statusLogId = $(this).data("id");
  openReportModal(statusLogId);
}

// [3-4-3] 신고 모달 열기
function openReportModal(statusLogId) {
  const reason = prompt("🚨 신고 사유를 입력해주세요:");
  if (!reason) return;

  // 신고 API 호출
  submitReport(statusLogId, reason);
}

// [3-4-4] 신고 API 호출
function submitReport(statusLogId, reason) {
  $.ajax({
    url: `/api/report`,
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify({ statusLogId, reason }),
    success: function () {
      alert("신고가 접수되었습니다.");
      loadAnswerList(requestId); // 신고 후 목록 갱신
    },
    error: function (xhr) {
      alert("신고 실패: " + xhr.responseText);
    },
  });
}

// [3-5] 유틸 함수: 응답 필드 표시용 텍스트 생성 (수정 모드 지원)
function renderExtraAnswerFields(answer, isEditMode = false) {
  const fieldMap = {
    PARKING: {
      label: "주차 가능 여부",
      field: "isParkingAvailable",
      boolean: true,
    },
    WAITING_STATUS: { label: "대기 인원", field: "waitCount", unit: "명" },
    CROWD_LEVEL: { label: "혼잡도", field: "waitCount", unit: "명" },
    BATHROOM: { label: "화장실 여부", field: "hasBathroom", boolean: true },
    FOOD_MENU: { label: "메뉴 정보", field: "menuInfo" },
    WEATHER_LOCAL: { label: "날씨 상태", field: "weatherNote" },
    STREET_VENDOR: { label: "노점 이름", field: "vendorName" },
    PHOTO_REQUEST: { label: "사진 메모", field: "photoNote" },
    NOISE_LEVEL: { label: "소음 상태", field: "noiseNote" },
    BUSINESS_STATUS: { label: "영업 여부", field: "isOpen", boolean: true },
    OPEN_SEAT: { label: "남은 좌석 수", field: "seatCount", unit: "개" },
    ETC: { label: "기타 메모", field: "extra" },
  };

  const config = fieldMap[answer.category];
  if (!config) return "";

  const value = answer[config.field];
  if (value == null) return "";

  if (isEditMode) {
    // 수정 모드: 필드 수정 가능하게 표시
    if (config.boolean) {
      return `
        <div class="mb-2">
          <label>${config.label}</label>
          <select class="form-select edit-extra-input" data-field="${
            config.field
          }">
            <option value="true" ${value ? "selected" : ""}>있음</option>
            <option value="false" ${!value ? "selected" : ""}>없음</option>
          </select>
        </div>
      `;
    } else {
      return `
        <div class="mb-2">
          <label>${config.label}</label>
          <input type="text" class="form-control edit-extra-input" data-field="${config.field}" value="${value}">
        </div>
      `;
    }
  }

  // 일반 모드 (텍스트로 표시)
  if (config.boolean) {
    return `<div class="text-muted">${config.label}: ${
      value ? "있음" : "없음"
    }</div>`;
  }

  return `<div class="text-muted">${config.label}: ${value}${
    config.unit || ""
  }</div>`;
}

/**
 * [4] 답변 채택 버튼 처리
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
 * [5] 답변 제출 처리 (중복 방지)
 */
function submitAnswer(e) {
  e.preventDefault();
  const content = $("#answerContent").val();
  const dto = { content, requestId: requestId };

  // 유연 필드 동적 추가 (카테고리별 필드)
  $("#dynamicAnswerFields")
    .find("input, select")
    .each(function () {
      const name = $(this).attr("name");
      let value = $(this).val();

      if (value === "") return;

      if ($(this).attr("type") === "number") {
        value = parseInt(value);
      } else if (value === "true" || value === "false") {
        value = value === "true";
      }

      dto[name] = value;
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
        loadAnswerList(requestId); // 답변 목록 즉시 새로고침
        loadRequestDetail(requestId); // 답변 카운트 갱신
      },
      error: function (xhr) {
        alert("답변 등록 실패: " + xhr.responseText);
      },
    });
  });
}
