/**
 * 사용하는 곳
 * - map/free-share.js
 * - 1, 2
 * - map/nearby.js
 * - 1
 * - map/request-list.js
 * - 1
 */

// [1] 현재 위치 가져오기
export function getCurrentPosition(successCallback, errorCallback = null) {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      successCallback,
      errorCallback || (() => alert("위치 정보를 가져올 수 없습니다.")),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  } else {
    alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
  }
}

// [2] 위치 반경 계산 함수 (하버사인 공식)
export function isWithinRadius(lat1, lng1, lat2, lng2, radiusMeters) {
  const R = 6371000;
  const toRad = deg => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c <= radiusMeters;
}