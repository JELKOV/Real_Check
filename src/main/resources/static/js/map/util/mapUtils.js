/**
 * 사용하는 곳
 * - map/free-share.js
 * - 1, 2, 4, 5
 * - map/nearby.js
 * - 6
 * - map/request-list.js
 * - 1, 4
 */

// [1] 반경 원 생성
export function createUserCircle(
  map,
  center,
  radius = 3000,
  color = "#007BFF"
) {
  return new naver.maps.Circle({
    map,
    center,
    radius,
    strokeColor: color,
    strokeOpacity: 0.6,
    strokeWeight: 2,
    fillColor: color,
    fillOpacity: 0.15,
  });
}

// [2] 반경 원 업데이트
export function updateCircleStyle(circle, color = "#28a745", radius = 3000) {
  if (!circle) return;
  circle.setOptions({
    strokeColor: color,
    fillColor: color,
    strokeOpacity: 0.6,
    strokeWeight: 2,
    fillOpacity: 0.15,
  });
  circle.setRadius(radius);
}

// [3] 네이버 맵 마커 및 InfoWindow 생성
export function createMarkerWithInfoWindow(
  map,
  lat,
  lng,
  htmlContent,
  openedInfoWindows
) {
  const position = new naver.maps.LatLng(lat, lng);
  const marker = new naver.maps.Marker({ map, position });

  const infoWindow = new naver.maps.InfoWindow({ content: htmlContent });

  naver.maps.Event.addListener(marker, "click", () => {
    if (Array.isArray(openedInfoWindows)) {
      openedInfoWindows.forEach((iw) => iw.close());
      openedInfoWindows.length = 0;
      openedInfoWindows.push(infoWindow);
    }
    infoWindow.open(map, marker);
  });

  return { marker, infoWindow };
}

// [4] 맵이동 함수
export function moveMapTo(map, circle, lat, lng) {
  const latlng = new naver.maps.LatLng(lat, lng);
  map.setCenter(latlng);
  circle?.setCenter(latlng);
}

// [5] 지도에서 중심 좌표 추출
export function extractLatLngFromMap(map) {
  const center = map.getCenter();
  return {
    lat: center.lat(),
    lng: center.lng(),
  };
}

// [6] 전체 InfoWindows 닫기
export function closeAllInfoWindows(infoWindowArray) {
  infoWindowArray.forEach((iw) => iw.close());
  infoWindowArray.length = 0;
}