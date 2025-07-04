/**
 * 사용하는 곳
 * - request/detail.js
 */
import { categoryFieldMap } from "./categoryUtils.js";

// [1] 유틸 함수: 카테고리별 동적 필드 생성
export function renderAnswerFields(category) {
  const container = $("#dynamicAnswerFields");
  container.empty();

  const config = categoryFieldMap[category];
  if (!config) return;

  let html = `<div class="mb-3"><label>${config.label}</label>`;
  if (config.type === "select") {
    html += `
      <div class="custom-select-wrapper mb-2" data-name="${config.name}">
        <div class="custom-select-box">
          <span class="selected-text">선택하세요</span>
          <span class="custom-arrow">▾</span>
        </div>
        <ul class="custom-select-options">
          ${config.options
            .map(
              (opt) =>
                `<div class="custom-option" data-value="${opt.value}">${opt.text}</div>`
            )
            .join("")}
        </ul>
        <input type="hidden" name="${config.name}" required />
      </div>
    `;
  } else {
    html += `<input type="${config.type}" class="form-control" name="${config.name}" required />`;
  }
  html += `</div>`;

  container.append(html);
}

// [2] 커스텀 설렉트바인딩 함수
export function bindCustomSelectEvents() {
  // 1. 커스텀 박스 클릭 → 열고 닫기
  $(document).on("click", ".custom-select-box", function () {
    const $wrapper = $(this).closest(".custom-select-wrapper");
    const $options = $wrapper.find(".custom-select-options");

    const isOpen = $wrapper.hasClass("open");

    $(".custom-select-wrapper").removeClass("open");
    $(".custom-select-options").hide();

    if (!isOpen) {
      $wrapper.addClass("open");
      $options.show();
    }
  });

  // 2. 옵션 클릭 → 선택값 설정
  $(document).on("click", ".custom-option", function () {
    const value = $(this).data("value");
    const text = $(this).text();
    const $wrapper = $(this).closest(".custom-select-wrapper");

    $wrapper.find(".selected-text").text(text);
    $wrapper.find("input[type='hidden']").val(value).trigger("change");
    $wrapper.removeClass("open");
    $wrapper.find(".custom-select-options").hide();
  });

  // 3. 외부 클릭 → 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest(".custom-select-wrapper").length) {
      $(".custom-select-wrapper").removeClass("open");
      $(".custom-select-options").hide();
    }
  });
}

// [3] 답변 카드에서 필드 렌더링 (표시 or 수정)
export function renderExtraAnswerFields(answer, isEditMode = false) {
  const config = categoryFieldMap[answer.category];
  if (!config) return "";

  const value = answer[config.name];
  if (value == null) return "";

  if (isEditMode) {
    if (config.boolean) {
      const selectedOption = config.options.find(
        (opt) => opt.value.toString() === value.toString()
      );
      const selectedText = selectedOption ? selectedOption.text : "선택하세요";

      return `
      <div class="mb-2">
        <label>${config.label}</label>
        <div class="custom-select-wrapper edit-extra mb-2" data-name="${
          config.name
        }">
          <div class="custom-select-box">
            <span class="selected-text">${selectedText}</span>
            <span class="custom-arrow">▾</span>
          </div>
          <ul class="custom-select-options">
            ${config.options
              .map(
                (opt) =>
                  `<div class="custom-option" data-value="${opt.value}">${opt.text}</div>`
              )
              .join("")}
          </ul>
          <input type="hidden" 
                 name="${config.name}" 
                 value="${value}" 
                 required 
                 class="edit-extra-input"
                 data-field="${config.name}" />
        </div>
      </div>
    `;
    }
  }

  // 일반 표시용
  if (config.boolean) {
    const selectedOption = config.options.find(
      (opt) => opt.value.toString() === value.toString()
    );
    const displayText = selectedOption
      ? selectedOption.text
      : value
      ? "있음"
      : "없음";

    return `<div class="text-muted">${config.label}: ${displayText}</div>`;
  }

  return `<div class="text-muted">${config.label}: ${value}${
    config.unit || ""
  }</div>`;
}

// [4] 답변 입력창 비활성화 관리
export function manageAnswerFormVisibility(request, loginUserIdNum) {
  let disableReason = "";

  if (request.closed) {
    disableReason = "🔒 이 요청은 마감되었습니다.";
  } else if (request.visibleAnswerCount >= 3) {
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
