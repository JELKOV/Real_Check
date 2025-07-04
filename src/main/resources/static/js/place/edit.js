// 모듈 import
import {
  renderAnswerFields,
  renderCategoryDropdown,
} from "./util/categoryUtils.js";

import {
  uploadImages,
  renderImagePreview,
  clearImagePreview,
} from "./util/imageUtils.js";

// 전역 변수
let uploadedImageUrls = [...(existingImageUrls || [])].filter(
  (url) => url && url !== "undefined" && url.trim() !== ""
);

$(document).ready(function () {
  renderCategoryDropdown($("#category"), [currentCategory], currentCategory);
  $("#category").prop("disabled", true);
  bindEventListeners();
  updateCharCount();
  renderPreviewWithDelete(); // 이미지 미리보기 렌더링
  renderAnswerFields($("#dynamicAnswerFields"), currentCategory, statusLogJson);
});

// 이벤트 바인딩
function bindEventListeners() {
  $("#selectImageBtn").on("click", () => $("#fileInput").click());
  $("#fileInput").on("change", handleFileUpload);
  $("#cancelImageBtn").on("click", clearAllImages);
  $("#content").on("input", updateCharCount);
  $("#category").on("change", function () {
    const selected = $(this).val();
    console.log("선택된 카테고리:", selected); // 값 제대로 찍히는지 확인
    renderAnswerFields($("#dynamicAnswerFields"), selected, {});
  });
  $("#editForm").on("submit", handleSubmit);

  // 이미지 클릭 시 모달로 보기
  $(document).on("click", "#uploadedPreview img", function () {
    const url = $(this).data("url");
    $("#modalImage").attr("src", url);
    $("#imageModal").modal("show");
  });
}

// 이미지 처리
function handleFileUpload() {
  const files = $("#fileInput")[0].files;
  if (!files.length) return;

  uploadImages(files)
    .then((urls) => {
      uploadedImageUrls.push(...urls);
      renderPreviewWithDelete();
      $("#fileInput").val(""); // 같은 파일 재업로드 가능하도록 초기화
    })
    .catch((err) => {
      alert("업로드 실패: " + err.responseText);
    });
}

function clearAllImages() {
  uploadedImageUrls = [];
  clearImagePreview("uploadedPreview");
  $("#fileInput").val(""); // input 초기화
  renderPreviewWithDelete(); // wrapper d-none 적용을 위해 호출
}

// 미리보기 렌더링 및 삭제 버튼 처리
function renderPreviewWithDelete() {
  renderImagePreview(uploadedImageUrls, "uploadedPreview", onDeleteImage);

  const $wrapper = $("#uploadedPreview").closest(".position-relative");
  if (uploadedImageUrls.length > 0) {
    $wrapper.removeClass("d-none");
  } else {
    $wrapper.addClass("d-none");
  }

  $("#cancelImageBtn").toggleClass("d-none", uploadedImageUrls.length === 0);
}

function onDeleteImage(index) {
  uploadedImageUrls.splice(index, 1);
  renderPreviewWithDelete();
}

// 글자 수 표시
function updateCharCount() {
  const len = $("#content").val().trim().length;
  $("#contentCount").text(`${len} / 300자`);
}

// 수정 요청 제출
function handleSubmit(e) {
  e.preventDefault();

  const category = currentCategory; // 고정된 카테고리 사용
  const body = {
    content: $("#content").val().trim(),
    category,
    imageUrls: uploadedImageUrls,
    waitCount: null,
    hasBathroom: null,
    menuInfo: null,
    weatherNote: null,
    vendorName: null,
    photoNote: null,
    noiseNote: null,
    isParkingAvailable: null,
    isOpen: null,
    seatCount: null,
    crowdLevel: null,
    extra: null,
  };

  // 카테고리에 따른 값만 채우기
  switch (category) {
    case "WAITING_STATUS": {
      const val = $("#waitCount").val();
      body.waitCount = val === "" ? null : parseInt(val);
      break;
    }
    case "FOOD_MENU":
      body.menuInfo = $("#menuInfo").val().trim() || null;
      break;
    case "BATHROOM": {
      const val = $("#hasBathroom").val();
      body.hasBathroom = val === "" ? null : val === "true";
      break;
    }
    case "WEATHER_LOCAL":
      body.weatherNote = $("#weatherNote").val().trim() || null;
      break;
    case "STREET_VENDOR":
      body.vendorName = $("#vendorName").val().trim() || null;
      break;
    case "PHOTO_REQUEST":
      body.photoNote = $("#photoNote").val().trim() || null;
      break;
    case "NOISE_LEVEL":
      body.noiseNote = $("#noiseNote").val().trim() || null;
      break;
    case "PARKING": {
      const val = $("#isParkingAvailable").val();
      body.isParkingAvailable = val === "" ? null : val === "true";
      break;
    }
    case "BUSINESS_STATUS": {
      const val = $("#isOpen").val();
      body.isOpen = val === "" ? null : val === "true";
      break;
    }
    case "OPEN_SEAT": {
      const val = $("#seatCount").val();
      body.seatCount = val === "" ? null : parseInt(val);
      break;
    }
    case "CROWD_LEVEL": {
      const val = $("#crowdLevel").val();
      body.crowdLevel = val === "" ? null : parseInt(val);
      break;
    }
    case "ETC":
      body.extra = $("#extra").val().trim() || null;
      break;
  }

  const logId = $("#logId").val();
  $.ajax({
    url: `/api/status/${logId}`,
    method: "PUT",
    contentType: "application/json",
    data: JSON.stringify(body),
    success: function () {
      alert("수정이 완료되었습니다.");
      window.location.href = `/place/community/${$("#placeId").val()}`;
    },
    error: function (xhr) {
      alert("수정 실패: " + xhr.responseText);
    },
  });
}
