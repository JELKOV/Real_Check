$(document).ready(function () {
  const requestId = document.getElementById("requestId").value;
  const loginUserId = document.getElementById("loginUserId").value;

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

  // -------------------------
  // 1. 요청 상세 정보 로드
  // -------------------------
  $.get(`/api/request/${requestId}`, function (request) {
    const formattedDate = new Date(request.createdAt).toLocaleString();
    const nickname = request.requesterNickname || "익명";
    const location =
      request.placeName || request.customPlaceName || "장소 정보 없음";
    const closedBadge = request.closed
      ? `<span class="badge bg-danger ms-2">🔒 마감</span>`
      : "";

    const badge = `<span class="badge bg-primary"> ${
      categoryLabelMap[request.category] || request.category
    } </span> ${closedBadge}`;

    const html = `
            <div class="card">
              <div class="card-body">
                <h5 class="card-title d-flex justify-content-between align-items-center">${request.title} ${badge}</h5>
                <p class="card-text text-muted">${request.content}</p>
                <ul class="list-unstyled mt-3">
                  <li><strong>포인트:</strong> ${request.point}pt</li>
                  <li><strong>장소:</strong> ${location}</li>
                  <li><strong>작성자:</strong> ${nickname}</li>
                  <li><strong>작성일:</strong> ${formattedDate}</li>
                </ul>
              </div>
            </div>
          `;
    $("#requestDetail").html(html);

    // 답변 입력창 표시 여부 판단 (요청자거나, 답변이 이미 3개면 숨김)
    const loginUserIdNum = Number(loginUserId); // NaN 처리 포함
    if (
      !Number.isNaN(loginUserIdNum) &&
      (loginUserIdNum === request.requesterId || request.answerCount >= 3)
    ) {
      $("#answerFormSection").hide();
    }

    // 지도 표시
    if (request.lat && request.lng) {
      // 지도초기화
      const map = new naver.maps.Map("map", {
        center: new naver.maps.LatLng(request.lat, request.lng),
        zoom: 16,
      });

      // 마커표시
      new naver.maps.Marker({
        position: new naver.maps.LatLng(request.lat, request.lng),
        map: map,
      });
    } else {
      $("#map").text("위치 정보가 없습니다.");
    }

    // 카테고리에 맞는 추가 필드 렌더링
    renderAnswerFields(request.category);
  });

  // -------------------------
  // 2. 답변 리스트 렌더링
  // -------------------------
  $.get(`/api/status/by-request/${requestId}`, function (answers) {
    if (answers.length === 0) {
      $("#answerList").html(
        '<li class="list-group-item">등록된 답변이 없습니다.</li>'
      );
      return;
    }

    const hasSelected = answers.some((a) => a.selected);

    answers
      .sort((a, b) => (b.selected ? 1 : 0) - (a.selected ? 1 : 0)) // 채택된 답변 먼저
      .forEach((answer) => {
        const imageHtml = answer.imageUrl
          ? '<img src="' +
            answer.imageUrl +
            '" style="max-width:100px;" class="mt-2" />'
          : "";
        const nickname = answer.nickname ? answer.nickname : "익명 사용자";
        const isOwner = answer.requestOwnerId === parseInt(loginUserId);
        const canSelect = isOwner && !hasSelected && !answer.selected;
        const formattedDate = new Date(answer.createdAt).toLocaleString();

        const selectedBadge = answer.selected
          ? `<span class="badge bg-success ms-2">✅ 채택됨</span>`
          : "";

        const selectBtn = canSelect
          ? `<button class="btn btn-sm btn-outline-success select-answer-btn mt-2" data-id=${answer.id}>이 답변 채택</button>`
          : "";

        const row = `
                <li class="list-group-item">
                  <strong>${nickname}</strong> ${selectedBadge}
                  <p>${answer.content}</p>
                  ${renderExtraAnswerFields(answer)}
                  ${imageHtml}
                  <br><small class="text-muted">${formattedDate}</small>
                  ${selectBtn}
                </li>
              `;
        $("#answerList").append(row);
      });
  });

  // -------------------------
  // 3. 답변 채택 버튼 처리
  // -------------------------
  $(document).on("click", ".select-answer-btn", function () {
    const statusLogId = $(this).data("id");
    if (!confirm("이 답변을 채택하시겠습니까?")) return;

    $.post(`/api/status/select/${statusLogId}`)
      .done(() => {
        alert("답변이 채택되었습니다.");
        location.reload();
        //TODO: 포인트 지급 차감 처리
      })
      .fail((xhr) => {
        alert("채택 실패: " + xhr.responseText);
      });
  });

  // -------------------------
  // 4. 답변 등록 처리
  // -------------------------
  $("#answerForm").on("submit", function (e) {
    e.preventDefault();

    const content = $("#answerContent").val();
    const dto = { content };

    // 유연 필드 동적 추가
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

    $.ajax({
      url: `/api/answer/${requestId}`,
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(dto),
      success: function () {
        alert("답변이 등록되었습니다.");
        location.reload();
      },
      error: function (xhr) {
        alert("답변 등록 실패: " + xhr.responseText);
      },
    });
  });
});


// 유틸 함수: 카테고리별 동적 필드 생성
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

// 유틸 함수: 응답 필드 표시용 텍스트 생성
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
