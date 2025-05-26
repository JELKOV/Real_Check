let uploadedImageUrls = [];

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
  renderCategoryOptions(allowedTypes);
  bindEventListeners();

  $(document).on("click", ".delete-image-btn", function () {
    const urlToRemove = $(this).data("url");
    uploadedImageUrls = uploadedImageUrls.filter((url) => url !== urlToRemove);
    renderImagePreview();
  });
});

function bindEventListeners() {
  bindContentLengthCounter();
  bindFileSelectButton();
  bindDragAndDropUpload();
  bindFileInputChange();
  bindImageCancel();
  bindCategoryChange();
  bindFormSubmit();
}

// [1] 글자 수 실시간 반영
function bindContentLengthCounter() {
  $("#content").on("input", function () {
    const len = $(this).val().length;
    $("#contentCount").text(`${len} / 300자`);
  });
}

// [2] 파일 선택 버튼 처리
function bindFileSelectButton() {
  $("#selectImageBtn").on("click", () => $("#fileInput").click());
}

// [3] 드레그 앤 드롭으로 이미지 처리
function bindDragAndDropUpload() {
  const dropArea = $("#dropArea");

  dropArea.on("dragover", (e) => {
    e.preventDefault();
    dropArea.addClass("dragover");
  });

  dropArea.on("dragleave", () => dropArea.removeClass("dragover"));

  dropArea.on("drop", (e) => {
    e.preventDefault();
    dropArea.removeClass("dragover");
    const files = e.originalEvent.dataTransfer.files;
    if (files.length > 0) {
      handleFileUpload(files);
    }
  });
}

// [4] 파일 Input으로 처리
function bindFileInputChange() {
  $("#fileInput").on("change", function () {
    const files = this.files;
    if (files.length > 0) {
      handleFileUpload(files);
    }
  });
}

// [5] 이미지 제거 버튼 클릭
function bindImageCancel() {
  $("#cancelImageBtn").on("click", function () {
    $("#fileInput").val("");
    $("#uploadedPreview").empty();
    $(this).addClass("d-none");
    uploadedImageUrls = [];
  });
}

// [6] 카테고리 변경 시 동적 입력 필드 렌더링
function bindCategoryChange() {
  $("#category").on("change", function () {
    const selected = $(this).val();
    renderAnswerFields(selected);
  });
}

// [7] 폼 제출 처리
function bindFormSubmit() {
  $("#registerForm").on("submit", function (e) {
    e.preventDefault();

    const content = $("#content").val().trim();
    if (!content) {
      alert("공지 내용을 입력해주세요.");
      return;
    }

    const data = {
      placeId: $("input[name='placeId']").val(),
      content,
      imageUrls: uploadedImageUrls,
      category: $("#category").val() || null,
    };

    // 동적으로 렌더링된 필드 값 수집
    $("#dynamicAnswerFields")
      .find("input, select")
      .each(function () {
        const name = $(this).attr("name");
        let value = $(this).val();

        if ($(this).attr("type") === "number") {
          value = parseInt(value);
        } else if (value === "true" || value === "false") {
          value = value === "true";
        }

        if (
          value !== "" &&
          value !== null &&
          value !== undefined &&
          !(typeof value === "number" && isNaN(value))
        ) {
          data[name] = value;
          console.log("🚀 최종 전송 데이터:", data);
        }
      });

    // 버튼 비활성화
    $("button[type='submit']").prop("disabled", true).text("등록 중...");

    // POST 요청
    $.ajax({
      url: "/api/status/register-public",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(data),
      success: function () {
        alert("공지 등록 완료!");
        location.href = "/place/community/" + data.placeId;
      },
      error: function (xhr) {
        alert("등록 실패: " + xhr.responseText);
        $("button[type='submit']").prop("disabled", false).text("등록하기");
      },
    });
  });
}

// [8] 동적 입력 필드 렌더링 함수
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

  let html = `<div class="mb-3"><label class="form-label">${config.label}</label>`;

  if (config.type === "select") {
    html += `<select name="${config.name}" class="form-select">`;
    html += `<option value="">선택하세요</option>`;
    config.options.forEach((opt) => {
      html += `<option value="${opt.value}">${opt.text}</option>`;
    });
    html += `</select>`;
  } else {
    html += `<input type="${config.type}" class="form-control" name="${config.name}" required />`;
  }

  html += `</div>`;
  container.append(html);
}

// [9] 카테고리 select 옵션 렌더링
function renderCategoryOptions(allowedTypes) {
  const $select = $("#category");
  $select.empty();
  $select.append(`<option value="">-- 선택하세요 --</option>`);

  allowedTypes.forEach((type) => {
    const label = categoryLabelMap[type] || type;
    $select.append(`<option value="${type}">${label}</option>`);
  });
}

// [10] 사진 미리보기 랜더링
function renderImagePreview() {
  const previewHtml = uploadedImageUrls
    .map(
      (url, idx) => `
        <div class="me-2 mb-2 position-relative d-inline-block">
          <img 
            src="${url}" 
            class="img-fluid border rounded" 
            style="max-height: 150px; max-width: 150px; object-fit: cover; cursor: pointer;" 
            data-url="${url}"
            onclick="openImageModal('${url}')"
          />
          <button 
            type="button" 
            class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn"
            style="background-color: rgba(0,0,0,0.6); color: white;" 
            data-url="${url}" 
            title="삭제"
          ></button>
        </div>
      `
    )
    .join("");
  $("#uploadedPreview").html(previewHtml);

  // 이미지가 하나라도 있으면 '이미지 제거' 버튼 보이기
  $("#cancelImageBtn").toggleClass("d-none", uploadedImageUrls.length === 0);
}

// [11] 모달 열기
function openImageModal(url) {
  $("#modalImage").attr("src", url);
  $("#imageModal").modal("show");
}

// [12] 이미지 업로드 처리
function handleFileUpload(files) {
  const formData = new FormData();
  // 여러 파일을 모두 FormData에 추가
  for (const file of files) {
    formData.append("files", file);
  }

  $.ajax({
    url: "/api/upload/multi", // 다중 업로드 endpoint로 변경
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function (res) {
      // 응답된 이미지 URL 배열 저장
      uploadedImageUrls = res;
      renderImagePreview(); // 미리보기 렌더링
      $("#cancelImageBtn").removeClass("d-none");
    },
    error: function (xhr) {
      alert("업로드 실패: " + xhr.responseText);
    },
  });
}
