export function formatKST(isoString) {
  return new Date(isoString).toLocaleString("ko-KR", {
    timeZone: "Asia/Seoul",
  });
}
