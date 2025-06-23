/**
 * 사용하는 곳
 * - request/list.js
 */

// [1] 문자열 자르기
export function shortenText(text, length = 30) {
  return text && text.length > length
    ? text.substring(0, length) + "..."
    : text;
}

// [2] 포인트 정보 출력
export function formatPointInfo(point) {
  return `<small>포인트: ${point}</small>`;
}

