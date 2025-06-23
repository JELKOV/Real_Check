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
    html += `<select class="form-select" name="${config.name}" required>`;
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

// [2] 답변 카드에서 필드 렌더링 (표시 or 수정)
export function renderExtraAnswerFields(answer, isEditMode = false) {
  const config = categoryFieldMap[answer.category];
  if (!config) return "";

  const value = answer[config.name];
  if (value == null) return "";

  if (isEditMode) {
    if (config.boolean) {
      return `
        <div class="mb-2">
          <label>${config.label}</label>
          <select class="form-select edit-extra-input" data-field="${
            config.name
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
          <input type="text" class="form-control edit-extra-input" data-field="${config.name}" value="${value}">
        </div>
      `;
    }
  }

  // 일반 표시용
  if (config.boolean) {
    return `<div class="text-muted">${config.label}: ${
      value ? "있음" : "없음"
    }</div>`;
  }

  return `<div class="text-muted">${config.label}: ${value}${
    config.unit || ""
  }</div>`;
}

// [3] 답변 입력창 비활성화 관리
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
