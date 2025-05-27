let uploadedImageUrls = [...existingImageUrls]; // 서버로부터 내려온 기존 이미지
const logId = $("#logId").val();
const placeId = $("#placeId").val();

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
  renderCategoryOptions();
  bindEventListeners();
  updateCharCount();
  renderImagePreviews();
  renderAnswerFields(currentCategory, statusLogJson);
});

// ─────────────────────────────────────
// 카테고리 옵션 렌더링
// ─────────────────────────────────────
function renderCategoryOptions() {
  const $select = $("#category");
  $select.empty();
  $select.append(`<option value="">-- 선택하세요 --</option>`);

  allowedTypes.forEach((cat) => {
    const label = categoryLabelMap[cat] || cat;
    const selected = cat === currentCategory ? "selected" : "";
    $select.append(`<option value="${cat}" ${selected}>${label}</option>`);
  });
}

// ─────────────────────────────────────
// 이벤트 바인딩
// ─────────────────────────────────────
function bindEventListeners() {
  $("#selectImageBtn").on("click", () => $("#fileInput").click());
  $("#fileInput").on("change", handleFileUpload);
  $("#cancelImageBtn").on("click", clearAllImages);
  $("#content").on("input", updateCharCount);
  $("#category").on("change", function () {
    const selected = $(this).val();
    renderAnswerFields(selected, {});
  });
  $("#editForm").on("submit", handleSubmit);
}

// ─────────────────────────────────────
// 이미지 처리
// ─────────────────────────────────────
function handleFileUpload() {
  const files = $("#fileInput")[0].files;
  if (!files.length) return;

  const formData = new FormData();
  for (const file of files) formData.append("files", file);

  $.ajax({
    url: "/api/upload/multi",
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function (urls) {
      uploadedImageUrls.push(...urls);
      renderImagePreviews();
    },
    error: function (xhr) {
      alert("업로드 실패: " + xhr.responseText);
    },
  });
}

function renderImagePreviews() {
  const $container = $("#uploadedPreview").empty();
  if (uploadedImageUrls.length > 0) {
    $("#cancelImageBtn").removeClass("d-none");
  } else {
    $("#cancelImageBtn").addClass("d-none");
  }

  uploadedImageUrls.forEach((url, index) => {
    const $block = $(`
      <div class="position-relative d-inline-block">
        <img src="${url}" class="img-thumbnail me-2 mb-2" style="max-width: 100px;" />
        <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0" data-index="${index}"></button>
      </div>
    `);
    $block.find("button").on("click", function () {
      uploadedImageUrls.splice($(this).data("index"), 1);
      renderImagePreviews();
    });
    $container.append($block);
  });
}

function clearAllImages() {
  uploadedImageUrls = [];
  renderImagePreviews();
}

// ─────────────────────────────────────
// 동적 카테고리 필드 렌더링
// ─────────────────────────────────────
function renderAnswerFields(category, data = {}) {
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
      name: "crowdLevel",
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

  let html = `<div class="mb-3"><label class="form-label">${config.label}</label>`;

  const currentValue = data[config.name];

  if (config.type === "select") {
    html += `<select name="${config.name}" class="form-select">`;
    html += `<option value="">선택하세요</option>`;
    config.options.forEach((opt) => {
      const selected = String(currentValue) === opt.value ? "selected" : "";
      html += `<option value="${opt.value}" ${selected}>${opt.text}</option>`;
    });
    html += `</select>`;
  } else {
    html += `<input type="${config.type}" class="form-control" name="${
      config.name
    }" value="${currentValue ?? ""}" />`;
  }

  html += `</div>`;
  container.append(html);
}

// ─────────────────────────────────────
// 글자 수 표시
// ─────────────────────────────────────
function updateCharCount() {
  const len = $("#content").val().trim().length;
  $("#contentCount").text(`${len} / 300자`);
}

// ─────────────────────────────────────
// 수정 요청 제출
// ─────────────────────────────────────
function handleSubmit(e) {
  e.preventDefault();

  const body = {
    content: $("#content").val().trim(),
    category: $("#category").val(),
    imageUrls: uploadedImageUrls,
  };

  switch (body.category) {
    case "WAITING_STATUS":
      body.waitCount = parseInt($("#waitCount").val());
      break;
    case "FOOD_MENU":
      body.menuInfo = $("#menuInfo").val();
      break;
    case "BATHROOM":
      body.hasBathroom = $("#hasBathroom").val() === "true";
      break;
    case "ETC":
      body.extra = $("#extra").val();
      break;
    case "CROWD_LEVEL":
      body.crowdLevel = parseInt($("#crowdLevel").val());
      break;
    case "WEATHER_LOCAL":
      body.weatherNote = $("#weatherNote").val();
      break;
    case "STREET_VENDOR":
      body.vendorName = $("#vendorName").val();
      break;
    case "PHOTO_REQUEST":
      body.photoNote = $("#photoNote").val();
      break;
    case "NOISE_LEVEL":
      body.noiseNote = $("#noiseNote").val();
      break;
    case "BUSINESS_STATUS":
      body.isOpen = $("#isOpen").val() === "true";
      break;
    case "OPEN_SEAT":
      body.seatCount = parseInt($("#seatCount").val());
      break;
  }

  $.ajax({
    url: `/api/status/${logId}`,
    method: "PUT",
    contentType: "application/json",
    data: JSON.stringify(body),
    success: function () {
      alert("수정 완료!");
      window.location.href = `/place/community/${placeId}`;
    },
    error: function (xhr) {
      alert("수정 실패: " + xhr.responseText);
    },
  });
}
