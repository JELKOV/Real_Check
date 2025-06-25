/**
 * 사용하는 곳
 * - place/register.js
 * - 1, 2, 3
 * - place/edit.js
 * - 1, 2, 3
 */

// [1] 이미지 업로드 처리 (다중 파일 업로드)
export function uploadImages(files) {
  const formData = new FormData();
  for (const file of files) {
    formData.append("files", file);
  }

  return new Promise((resolve, reject) => {
    $.ajax({
      url: "/api/upload/multi",
      method: "POST",
      data: formData,
      processData: false,
      contentType: false,
      success: function (res) {
        resolve(res); // string[] 형태의 이미지 URL 목록
      },
      error: function (xhr) {
        reject(xhr);
      },
    });
  });
}

// [2] 이미지 미리보기 렌더링
export function renderImagePreview(urls, containerId, deleteCallback) {
  const $container = $("#" + containerId).empty();
  const $wrapper = $container.closest(".position-relative");

  // 유효 이미지만 필터링
  const validUrls = (urls || []).filter((u) => u && u !== "undefined");
  if (validUrls.length === 0) return;

  validUrls.forEach((url, index) => {
    const $block = $(`
      <div class="position-relative d-inline-block me-2 mb-2">
        <img 
          src="${url}"
          class="img-fluid border rounded"
          style="max-height: 150px; max-width: 150px; object-fit: cover; cursor: pointer;"
          data-url="${url}"
        />
        <button 
          type="button" 
          class="btn btn-sm btn-close position-absolute top-0 end-0"
          style="background-color: rgba(38, 22, 22, 0.6); color: white;" 
          data-index="${index}" 
          title="삭제"
        ></button>
      </div>
    `);

    $block.find("button").on("click", () => {
      deleteCallback(index);
    });

    $container.append($block);
  });
}

// [3] 이미지 전체 제거
export function clearImagePreview(containerId) {
  $("#" + containerId).empty();
}
