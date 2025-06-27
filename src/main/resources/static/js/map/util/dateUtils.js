/**
 * 사용하는 곳
 * - map/nearby.js
 * - 1
 */

// [1] 날짜 포멧팅 함수
export function formatDate(datetimeStr) {
  const date = new Date(datetimeStr);
  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}
