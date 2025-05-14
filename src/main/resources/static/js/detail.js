// 전역 변수로 loginUserIdNum, requestId 선언
let loginUserIdNum = null;
let requestId = null;

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

// [1] 이벤트 리스너 바인딩
function bindEventListeners() {
  // 답변 채택 버튼 클릭
  $(document).on("click", ".select-answer-btn", handleSelectAnswer);

  // 답변 등록 처리
  $("#answerForm").on("submit", submitAnswer);

  // 요청 마감 버튼 클릭
  $(document).on("click", "#closeRequestBtn", function () {
    closeRequest();
  });
}

// [2] 요청 상세 정보 로드
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

// [2-1] 요청 상세 정보 렌더링
function renderRequestDetail(request) {
  const formattedDate = new Date(request.createdAt).toLocaleString();
  const nickname = request.requesterNickname || "익명";
  const location =
    request.placeName || request.customPlaceName || "장소 정보 없음";
  const closedBadge = request.closed
    ? `<span class="badge bg-danger ms-2">🔒 마감</span>`
    : "";
  const categoryBadge = `<span class="badge bg-primary"> ${
    categoryLabelMap[request.category] || request.category
  } </span>`;

  const isRequester = loginUserIdNum === request.requesterId;
  const canCloseManually =
    isRequester && !request.closed && request.answerCount === 0;

  const html = `
    <div class="card mt-3 ${
      request.closed ? "bg-light text-muted border-start border-danger" : ""
    }">
      <div class="card-body">
        <h5 class="card-title d-flex justify-content-between align-items-center">
          ${request.title} ${categoryBadge} ${closedBadge}
        </h5>
        <p class="card-text text-muted">${request.content}</p>
        <ul class="list-unstyled mt-3">
          <li><strong>포인트:</strong> ${request.point}pt</li>
          <li><strong>장소:</strong> ${location}</li>
          <li><strong>작성자:</strong> ${nickname}</li>
          <li><strong>작성일:</strong> ${formattedDate}</li>
        </ul>
         ${
           canCloseManually
             ? '<button id="closeRequestBtn" class="btn btn-danger mt-2">마감하기</button>'
             : ""
         }
      </div>
    </div>`;

  $("#requestDetail").html(html);
  // 마감하기 버튼 클릭 이벤트 바인딩
  if (canCloseManually) {
    $("#closeRequestBtn").on("click", function () {
      closeRequestManually(requestId);
    });
  }
}

// [2-2] 지도 표시 함수
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

// [2-3] 답변 입력창 비활성화 관리
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

// [2-4] 유틸 함수: 카테고리별 동적 필드 생성
function renderAnswerFields(category) {
  const container = $("#dynamicAnswerFields");
  container.empty();

  const fieldMap = {
    WAITING_STATUS: {
      label: "대기 인원",
      name: "waitCount",
      type: "number",
    },
    CROWD_LEVEL: { label: "혼잡도", name: "waitCount", type: "number" },
    BATHROOM: {
      label: "화장실 여부",
      name: "hasBathroom",
      type: "select",
      options: [
        { value: "true", text: "있음" },
        { value: "false", text: "없음" },
      ],
    },
    FOOD_MENU: { label: "메뉴 정보", name: "menuInfo", type: "text" },
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
    NOISE_LEVEL: { label: "소음 상태", name: "noiseNote", type: "text" },
    PARKING: {
      label: "주차 가능 여부",
      name: "isParkingAvailable",
      type: "select",
      options: [
        { value: "true", text: "가능" },
        { value: "false", text: "불가능" },
      ],
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

// [3] 답변 리스트 로드
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

// [3-1] 답변 자동 마감 안내
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

// [3-2] 답변 행 생성 함수
function generateAnswerRow(answer, hasSelected) {
  const imageHtml = answer.imageUrl
    ? `<img src="${answer.imageUrl}" style="max-width:100px;" class="mt-2" />`
    : "";
  const nickname = answer.nickname || "익명 사용자";
  const selectedBadge = answer.selected
    ? `<span class="badge bg-success ms-2">✅ 채택됨</span>`
    : "";
  const canSelect = canSelectAnswer(answer, hasSelected);
  const canEditOrDelete = canEditOrDeleteAnswer(answer);
  console.log(answer.userId)

  const actionButtons = `
    ${
      canSelect
        ? `<button class="btn btn-sm btn-outline-success select-answer-btn mt-2" data-id="${answer.id}">이 답변 채택</button>`
        : ""
    }
    ${
      canEditOrDelete
        ? `
      <button class="btn btn-sm btn-warning mt-2 edit-answer-btn" data-id="${answer.id}">✏️</button>
      <button class="btn btn-sm btn-danger mt-2 delete-answer-btn" data-id="${answer.id}">🗑️</button>
    `
        : ""
    }
  `;

  const formattedDate = new Date(answer.createdAt).toLocaleString();

  return `
    <li class="list-group-item ${
      answer.selected ? "list-group-item-success" : ""
    }">
      <strong>${nickname}</strong> ${selectedBadge}
      <p>${answer.content}</p>
      ${renderExtraAnswerFields(answer)}
      ${imageHtml}
      <br><small class="text-muted">${formattedDate}</small>
      ${actionButtons}
    </li>`;
}

// [3-2-1] 답변 채택 가능 여부 판단 함수
function canSelectAnswer(answer, hasSelected) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (answer.selected) return false; // 이미 선택된 답변은 선택 불가
  if (hasSelected) return false; // 이미 다른 답변이 선택된 경우
  if (loginUserIdNum !== answer.requestOwnerId) return false; // 요청 작성자만 채택 가능
  return true;
}

// [3-2-2] 답변 수정/삭제 가능 여부 판단 함수
function canEditOrDeleteAnswer(answer) {
  if (!loginUserIdNum) return false; // 로그인 여부 확인
  if (loginUserIdNum !== answer.userId) return false; // 답변 작성자만 수정/삭제 가능
  if (answer.selected) return false; // 채택된 답변은 수정/삭제 불가
  return true;
}

// [3-3] 답변 수정 모드 활성화
$(document).on("click", ".edit-answer-btn", function () {
  const answerId = $(this).data("id");
  const answerTextElement = $(`#answer-text-${answerId}`);
  const originalText = answerTextElement.text().trim();

  // 수정 모드로 변경 (텍스트를 입력 필드로 변경)
  answerTextElement.html(`
    <textarea class="form-control edit-answer-input" data-id="${answerId}">${originalText}</textarea>
    <button class="btn btn-primary mt-2 save-edit-btn" data-id="${answerId}">저장</button>
    <button class="btn btn-secondary mt-2 cancel-edit-btn" data-id="${answerId}">취소</button>
  `);
});

// [3-3-1] 수정 취소 처리
$(document).on("click", ".cancel-edit-btn", function () {
  loadAnswerList(requestId); // 원래 답변 목록으로 되돌리기
});

// [3-3-2] 수정 저장 처리
$(document).on("click", ".save-edit-btn", function () {
  const answerId = $(this).data("id");
  const newContent = $(`textarea.edit-answer-input[data-id="${answerId}"]`)
    .val()
    .trim();

  if (!newContent) {
    alert("답변 내용을 입력해주세요.");
    return;
  }

  // 서버로 수정 요청 (PATCH)
  $.ajax({
    url: `/api/status/${answerId}`,
    method: "PATCH",
    contentType: "application/json",
    data: JSON.stringify({ content: newContent }),
    success: function () {
      alert("답변이 수정되었습니다.");
      loadAnswerList(requestId);
    },
    error: function (xhr) {
      alert("답변 수정 실패: " + xhr.responseText);
    },
  });
});

// [3-3-3] 답변 삭제 처리
$(document).on("click", ".delete-answer-btn", function () {
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
});

// [3-2-2] 유틸 함수: 응답 필드 표시용 텍스트 생성
function renderExtraAnswerFields(answer) {
  const category = answer.category;
  const fields = [];

  switch (category) {
    case "WAITING_STATUS":
    case "CROWD_LEVEL":
      if (answer.waitCount != null) {
        fields.push(`대기 인원: ${answer.waitCount}명`);
      }
      break;

    case "BATHROOM":
      if (answer.hasBathroom != null) {
        fields.push(`화장실 여부: ${answer.hasBathroom ? "있음" : "없음"}`);
      }
      break;

    case "FOOD_MENU":
      if (answer.menuInfo) {
        fields.push(`메뉴 정보: ${answer.menuInfo}`);
      }
      break;

    case "WEATHER_LOCAL":
      if (answer.weatherNote) {
        fields.push(`날씨 상태: ${answer.weatherNote}`);
      }
      break;

    case "STREET_VENDOR":
      if (answer.vendorName) {
        fields.push(`노점 이름: ${answer.vendorName}`);
      }
      break;

    case "PHOTO_REQUEST":
      if (answer.photoNote) {
        fields.push(`사진 메모: ${answer.photoNote}`);
      }
      break;

    case "NOISE_LEVEL":
      if (answer.noiseNote) {
        fields.push(`소음 상태: ${answer.noiseNote}`);
      }
      break;

    case "PARKING":
      if (answer.isParkingAvailable != null) {
        fields.push(
          `주차 가능 여부: ${answer.isParkingAvailable ? "가능" : "불가능"}`
        );
      }
      break;

    case "BUSINESS_STATUS":
      if (answer.isOpen != null) {
        fields.push(`영업 여부: ${answer.isOpen ? "영업 중" : "영업 종료"}`);
      }
      break;

    case "OPEN_SEAT":
      if (answer.seatCount != null) {
        fields.push(`남은 좌석 수: ${answer.seatCount}`);
      }
      break;
  }

  return fields.map((f) => `<div class="text-muted">${f}</div>`).join("");
}

// [4] 요청 수동 마감 처리
function closeRequestManually(requestId) {
  if (!confirm("이 요청을 마감하시겠습니까?")) return;

  $.ajax({
    url: `/api/request/${requestId}/close`,
    method: "PATCH",
    data: { userId: loginUserIdNum }, // 사용자 ID 전달
    success: function () {
      alert("요청이 마감되었습니다.");
      loadRequestDetail(requestId); // 마감 상태 갱신
      loadAnswerList(requestId);
    },
    error: function (xhr) {
      alert("마감 처리에 실패했습니다: " + xhr.responseText);
    },
  });
}

// [4] 답변 채택 버튼 처리
function handleSelectAnswer() {
  const statusLogId = $(this).data("id");
  if (!confirm("이 답변을 채택하시겠습니까?")) return;

  // UI 즉시 반응 (새로고침 없이)
  loadAnswerList(requestId);

  // 서버 요청 (비동기)
  $.post(`/api/status/select/${statusLogId}`)
    .done(() => {
      loadRequestDetail(requestId); // 요청 상태 갱신 (마감)
    })
    .fail((xhr) => {
      alert("채택 실패: " + xhr.responseText);
    });
}

// [5] 답변 제출 처리 (중복 방지)
function submitAnswer(e) {
  e.preventDefault();
  const content = $("#answerContent").val();
  const dto = { content };

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
