/**
 * 사용하는 곳
 * - place/place-register.js
 * - 1, 2
 * - place/place-edit.js
 * - 1, 2
 * - place/place-search.js
 * - 3
 */

// [1] 카테고리 전체 선택 / 해제
export function bindCategorySelectAll(
  selectAllBtn,
  deselectAllBtn,
  checkboxSelector
) {
  $(selectAllBtn).click(function () {
    $(checkboxSelector).prop("checked", true);
  });

  $(deselectAllBtn).click(function () {
    $(checkboxSelector).prop("checked", false);
  });
}

// [2] 선택된 카테고리 값 배열 반환
export function getSelectedCategoryValues(checkboxSelector) {
  return $(checkboxSelector)
    .filter(":checked")
    .map(function () {
      return this.value;
    })
    .get();
}

// [3] 키보드 포커스 시 하이라이트 처리
export function setActive(items, focusIndex) {
  items.removeClass("active");
  if (focusIndex >= 0 && focusIndex < items.length) {
    const target = items.eq(focusIndex);
    target.addClass("active");
    target[0].scrollIntoView({ behavior: "smooth", block: "nearest" });
  }
}
