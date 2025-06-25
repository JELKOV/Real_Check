/**
 * 사용하는 곳
 * - place/place-register.js
 * - 1
 * - place/place-edit.js
 * - 1
 * - place/place-search.js
 * - 2, 3
 */

// [1] 지도 초기화 및 클릭 시 위경도 + 주소 업데이트
export function initMapWithClickHandler(
  mapId = "map",
  defaultLat = 37.5665,
  defaultLng = 126.978
) {
  const center = new naver.maps.LatLng(defaultLat, defaultLng);
  const map = new naver.maps.Map(mapId, {
    center,
    zoom: 15,
  });

  const marker = new naver.maps.Marker({
    position: center,
    map,
  });

  naver.maps.Event.addListener(map, "click", function (e) {
    const lat = e.coord.lat();
    const lng = e.coord.lng();

    marker.setPosition(e.coord);
    updateLatLngFields(lat, lng);
    reverseGeocodeAndFillAddress(lat, lng);
  });

  return { map, marker };
}

// [2] 마커 추가
export function addPlaceMarker(map, place, markerArray, infoWindowRef) {
  const marker = new naver.maps.Marker({
    position: new naver.maps.LatLng(place.lat, place.lng),
    map,
    title: place.name,
  });

  const infoWindow = new naver.maps.InfoWindow({
    content: `<div style="padding:5px;">${place.name}</div>`,
  });

  naver.maps.Event.addListener(marker, "click", function () {
    if (infoWindowRef.open) infoWindowRef.open.close();
    infoWindow.open(map, marker);
    infoWindowRef.open = infoWindow;
  });

  markerArray.push(marker);
}

// [3] 마커 전체 제거
export function clearMarkers(markerArray, infoWindowRef) {
  markerArray.forEach((m) => m.setMap(null));
  markerArray.length = 0;

  if (infoWindowRef.open) {
    infoWindowRef.open.close();
    infoWindowRef.open = null;
  }
}

// ─────────────────────────────────────────────
// 내부 함수
// ─────────────────────────────────────────────

// [1] 위경도 입력 필드 업데이트
function updateLatLngFields(lat, lng) {
  $("#lat").val(lat);
  $("#lng").val(lng);
}

// [2] 주소 역지오코딩 후 address 필드 채우기
function reverseGeocodeAndFillAddress(lat, lng) {
  naver.maps.Service.reverseGeocode(
    {
      coords: new naver.maps.LatLng(lat, lng),
      orders: ["roadaddr", "addr"],
    },
    function (status, response) {
      if (status === naver.maps.Service.Status.OK) {
        const result = response.v2.address;
        $("#address").val(result.roadAddress || result.jibunAddress || "");
      }
    }
  );
}