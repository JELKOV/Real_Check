// 모듈 import
import {
  renderAnswerFields, // 카테고리 선택에 따라 동적 필드 렌더링
  renderCategoryOptions, // 카테고리 select 옵션 렌더링
} from "./util/categoryUtils.js";

import {
  uploadImages, // 이미지 업로드 처리 (서버에 POST)
  renderImagePreview, // 이미지 미리보기 렌더링
  clearImagePreview, // 이미지 미리보기 초기화
} from "./util/imageUtils.js";

// 전역 변수
let uploadedImageUrls = []; // 업로드된 이미지 URL 목록 (폼 전송에 포함됨)

// 초기 렌더링 및 이벤트 바인딩] 
$(document).ready(function () {
  renderCategoryOptions($("#category"), allowedTypes); // 카테고리 셀렉트 박스 렌더링
  bindEventListeners(); // 모든 이벤트 핸들러 바인딩

  // 이미지 클릭 시 모달로 확대 보기
  $(document).on("click", "#uploadedPreview img", function () {
    const url = $(this).data("url");
    $("#modalImage").attr("src", url);
    $("#imageModal").modal("show");
  });
});

// 전체 이벤트 바인딩
function bindEventListeners() {
  bindContentLengthCounter();
  bindFileSelectButton();
  bindDragAndDropUpload();
  bindFileInputChange();
  bindImageCancel();
  bindCategoryChange();
  bindFormSubmit();
}

// [1] 실시간 글자 수 카운터
function bindContentLengthCounter() {
  $("#content").on("input", function () {
    const len = $(this).val().length;
    $("#contentCount").text(`${len} / 300자`);
  });
}

// [2] 이미지 선택 버튼 클릭 시 input[type="file"] 트리거
function bindFileSelectButton() {
  $("#selectImageBtn").on("click", () => $("#fileInput").click());
}

// [3] 드래그 앤 드롭으로 이미지 파일 업로드
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

// [4] 파일 input으로 선택된 파일 업로드
function bindFileInputChange() {
  $("#fileInput").on("change", function () {
    const files = this.files;
    if (files.length > 0) {
      handleFileUpload(files);
    }
  });
}

// [5] 이미지 전체 제거 버튼
function bindImageCancel() {
  $("#cancelImageBtn").on("click", function () {
    uploadedImageUrls = [];
    clearImagePreview("uploadedPreview");
    $("#fileInput").val("");
    renderPreviewWithDelete();
  });
}

// [6] 카테고리 선택 시 동적 입력 필드 렌더링
function bindCategoryChange() {
  $("#category").on("change", function () {
    const selected = $(this).val();
    renderAnswerFields($("#dynamicAnswerFields"), selected);
  });
}

// [7] 폼 제출 시 입력값 수집 및 API 요청
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

    // 동적 입력 필드 수집
    $("#dynamicAnswerFields")
      .find("input, select")
      .each(function () {
        const name = $(this).attr("name");
        let value = $(this).val();

        // 타입 캐스팅 처리
        if ($(this).attr("type") === "number") {
          value = parseInt(value);
        } else if (value === "true" || value === "false") {
          value = value === "true";
        }

        // 유효값만 추가
        if (
          value !== "" &&
          value !== null &&
          value !== undefined &&
          !(typeof value === "number" && isNaN(value))
        ) {
          data[name] = value;
        }
      });

    // 버튼 비활성화 및 텍스트 변경
    $("button[type='submit']").prop("disabled", true).text("등록 중...");

    // 서버에 등록 요청
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

// [8] 이미지 파일 업로드 처리
function handleFileUpload(files) {
  uploadImages(files)
    .then((urls) => {
      uploadedImageUrls.push(...urls);  // 기존 리스트에 추가
      renderPreviewWithDelete();
      $("#fileInput").val(""); 
    })
    .catch((err) => {
      alert("업로드 실패: " + err.responseText);
    });
}

// [9] 이미지 미리보기 + 삭제 버튼 바인딩
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

// [9-1] 삭제 버튼 클릭 시 실행되는 콜백
function onDeleteImage(index) {
  uploadedImageUrls.splice(index, 1);
  renderPreviewWithDelete(); // 갱신
  $("#cancelImageBtn").toggleClass("d-none", uploadedImageUrls.length === 0);
}
