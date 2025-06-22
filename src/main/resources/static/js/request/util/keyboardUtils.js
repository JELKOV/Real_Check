/**
 * 사용하는 곳
 * - request/register.js
 */

let currentFocus = -1;
// ↓ 키 탐색 중에는 자동 검색(debounce AJAX)을 막기 위한 플래그
let isNavigatingByKey = false;
// 외부에서 주입받는 함수
let externalSelectPlaceFn;

// [1] 키보드 이벤트  ↓ / ↑ / Enter 누를 때: 포커스 이동 + 자동 검색 차단
export function bindKeyboardEvents() {
  $("#placeSearch").on("keydown", handlePlaceSearchKeyDown);
  $(document).on("keydown", handleGlobalKeyDown);
}

// [2] 선택된 항목 강조 (키보드 탐색)
export function updateSelection(items) {
  items.removeClass("selected");
  const focus = getCurrentFocus();
  if (focus >= 0 && focus < items.length) {
    $(items[focus]).addClass("selected");
    $(items[focus])[0].scrollIntoView({
      behavior: "smooth",
      block: "nearest",
    });
  }
}

// 외부에서 접근 가능한 변수/함수 
export function getCurrentFocus() {
  return currentFocus;
}
export function setCurrentFocus(val) {
  currentFocus = val;
}
export function isKeyNavigating() {
  return isNavigatingByKey;
}
export function setSelectPlaceFn(fn) {
  externalSelectPlaceFn = fn;
}

// ─────────────────────────────────────────────────────────────
// 내부 함수
// ─────────────────────────────────────────────────────────────

// [1] 키보드 다운 업 버튼 이벤트Z
function handlePlaceSearchKeyDown(e) {
  // ↓키 등으로 리스트 탐색 중이라는 상태를 true로 설정
  if (["ArrowDown", "ArrowUp", "Enter"].includes(e.key)) {
    isNavigatingByKey = true;
    // 항목 선택 / 포커스 처리 함수 실행
    handleKeyboardNavigation(e);
    setTimeout(() => {
      isNavigatingByKey = false;
    }, 500);
  }
}

// [2] 키보드 탐색 (Arrow + Enter)
function handleKeyboardNavigation(e) {
  const items = $(".place-item");
  if (items.length === 0) return;

  if (e.key === "ArrowDown") {
    e.preventDefault();
    setCurrentFocus((getCurrentFocus() + 1) % items.length);
  } else if (e.key === "ArrowUp") {
    e.preventDefault();
    setCurrentFocus((getCurrentFocus() - 1 + items.length) % items.length);
  } else if (e.key === "Enter" && getCurrentFocus() >= 0) {
    e.preventDefault();
    externalSelectPlaceFn($(items[getCurrentFocus()]));
    $("#placeSearchResults").hide();
  }

  updateSelection(items);
}

// [3] ESC 키로 검색 결과 닫기
function handleGlobalKeyDown(e) {
  if (e.key === "Escape") {
    $("#placeSearchResults").hide();
  }
}
