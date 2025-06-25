/**
 * 사용하는 곳
 * - place/community.js
 */

// [1] 날짜 포맷 유틸 (ISO 문자열 → 보기 쉬운 형식)
export function formatDateTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}
