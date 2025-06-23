/**
 * 사용하는 곳
 * - request/detail.js
 */

// [1] 파일 선택 시 실행될 핸들러
export function handleImageFileChange(e, uploadedImageUrlsRef, callback) {
  const files = e.target.files;
  if (files.length > 0) {
    handleFileUpload(files, uploadedImageUrlsRef, callback);
  }
}

// [2] 이미지 미리보기 렌더링
export function renderImagePreview(containerSelector, imageUrls) {
  const html = imageUrls
    .map(
      (url) => `
      <div class="position-relative d-inline-block">
        <img src="${url}" data-url="${url}" class="me-2 mb-2 img-thumbnail" style="max-width:100px;" />
        <button type="button" class="btn btn-sm btn-close position-absolute top-0 end-0 delete-image-btn"
                style="background-color: rgba(0,0,0,0.6); color: white;" title="삭제" data-url="${url}"></button>
      </div>
    `
    )
    .join("");
  $(containerSelector).html(html);
}

// [3] 이미지 삭제 핸들러
export function handleImageRemoveBtn(e, uploadedImageUrlsRef) {
  const $btn = $(e.target);
  const url = $btn.closest(".position-relative").find("img").data("url");

  const index = uploadedImageUrlsRef.findIndex((u) => u === url);
  if (index !== -1) uploadedImageUrlsRef.splice(index, 1);

  $btn.closest(".position-relative").remove();
}

// [4] 이미지 모달 보기
export function handleImagePreviewModal(e) {
  const imageUrl = $(e.target).data("url");
  $("#modalImage").attr("src", imageUrl);
  const modal = new bootstrap.Modal(document.getElementById("imageModal"));
  modal.show();
}

// ─────────────────────────────────────────────────────────────
// 내부 함수
// ─────────────────────────────────────────────────────────────

// [1] 이미지 업로드 처리
function handleFileUpload(files, uploadedImageUrlsRef, callback) {
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
      uploadedImageUrlsRef.length = 0;
      uploadedImageUrlsRef.push(...res);
      if (callback) callback(res);
    },
    error: function (xhr) {
      alert("이미지 업로드 실패: " + xhr.responseText);
    },
  });
}