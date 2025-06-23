/**
 * 사용하는 곳
 * - request/register.js
 * - 1
 * - request/detail.js
 * - 2
 */

// [1] debounce 함수
export function debounce(func, delay) {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, arguments), delay);
  };
}


// [2] 입력 필드 값을 적절한 타입으로 파싱
export function parseFieldValue(input) {
  const val = input.val();
  if (val === "") return null;
  if (input.attr("type") === "number") return parseInt(val, 10);
  if (val === "true" || val === "false") return val === "true";
  return val;
}
