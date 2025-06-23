/**
 * 사용하는 곳
 * - request/register.js
 * - 1 ,2 ,3, 4
 * - request/lists.js
 * - 5
 * - request/detail.js
 * - 6
 */

// [1] 지도 초기화
let mainMap;
export function initializeMap() {
  mainMap = new naver.maps.Map("mainMap", {
    center: new naver.maps.LatLng(37.5665, 126.978),
    zoom: 13,
  });

  // 사용자 지정 장소 - 지도 클릭 시 마커 설정
  naver.maps.Event.addListener(mainMap, "click", function (e) {
    if ($("#customPlaceSection").is(":visible")) {
      setCustomPlace(e.coord.lat(), e.coord.lng());
    }
  });
}

// [2] 장소 마커 설정 (공식 장소)
export function setMainMarker(lat, lng, id = "") {
  createOrUpdateMarker("main", lat, lng);
  mainMap.setCenter(new naver.maps.LatLng(lat, lng));

  // 장소 정보 설정 (lat, lng, placeId)
  $("#lat").val(lat);
  $("#lng").val(lng);
  $("#placeId").val(id || "");

  // 사용자 지정 마커 숨기기
  if (customMarker) customMarker.setMap(null);
}

// [3] 사용자 지정 장소 설정 (도로명 주소 자동 표시)
export function setCustomPlace(lat, lng) {
  createOrUpdateMarker("custom", lat, lng);
  mainMap.setCenter(new naver.maps.LatLng(lat, lng));

  // 위도/경도 값 저장
  $("#lat").val(lat);
  $("#lng").val(lng);

  $("#customAddress").val("");

  // 도로명 주소 자동 검색 (handleCustomAddressSearch 로직 사용)
  reverseGeocodeByLatLng(lat, lng);
}

// [4] 장소 초기화
export function resetSelectedPlace(isCustom = false) {
  // 입력 필드 및 선택된 UI 초기화
  $("#selectedPlaceName").val("").removeClass("selected");
  $("#selectedMarker").hide();
  $("#placeSearchResults").hide();
  $("#placeId, #lat, #lng").val("");
  $(".place-item").removeClass("selected"); // 검색 결과 UI 초기화
  resetMarkers();
  // 장소 정보 숨기기 (infoSection)
  $("#infoSection").hide();
}

// [5] 반경 원 표시
let radiusCircle = null;

export function drawRadiusCircle(map, center, radius) {
  if (radiusCircle) radiusCircle.setMap(null);
  radiusCircle = new naver.maps.Circle({
    map,
    center,
    radius,
    strokeColor: "#007BFF",
    strokeOpacity: 0.6,
    strokeWeight: 2,
    fillColor: "#007BFF",
    fillOpacity: 0.15,
  });
}

// [6] 지도 표시 함수
export function renderMap(
  lat,
  lng,
  requestTitle = "요청 위치",
  placeName = ""
) {
  const position = new naver.maps.LatLng(lat, lng);

  const map = new naver.maps.Map("map", {
    center: position,
    zoom: 16,
  });

  const marker = new naver.maps.Marker({
    position: position,
    map: map,
  });

  // InfoWindow 추가
  const infoWindow = new naver.maps.InfoWindow({
    content: `
      <div style="padding:8px; max-width:200px;">
        <strong>📌 ${requestTitle}</strong><br />
        <span style="font-size:0.9rem;">${placeName}</span>
      </div>
    `,
    backgroundColor: "#fff",
    borderColor: "#666",
    borderWidth: 1,
    anchorSize: new naver.maps.Size(10, 10),
  });

  infoWindow.open(map, marker);
}

// ─────────────────────────────────────────────────────────────
// 내부 함수
// ─────────────────────────────────────────────────────────────

// 장소 마커
let mainMarker = null;
// 사용자 지정 장소 마커
let customMarker = null;

// [1] 마커 생성/갱신 공통 함수
function createOrUpdateMarker(type, lat, lng) {
  let marker = type === "main" ? mainMarker : customMarker;
  if (!marker) {
    marker = new naver.maps.Marker({
      map: mainMap,
      position: new naver.maps.LatLng(lat, lng),
    });

    if (type === "main") {
      mainMarker = marker;
    } else {
      customMarker = marker;
    }
  } else {
    marker.setPosition(new naver.maps.LatLng(lat, lng));
  }
}

// [2] 도로명 주소 조회 (좌표 → 주소 변환)
function reverseGeocodeByLatLng(lat, lng) {
  // 서버에서 프록시된 API 경로로 요청
  $.ajax({
    url: `/api/reverse-geocode?lat=${lat}&lng=${lng}`,
    method: "GET",
    success: function (response) {
      const data =
        typeof response === "string" ? JSON.parse(response) : response;
      console.log("Reverse Geocode 응답:", data);

      if (!data.results || data.results.length === 0) {
        alert("도로명 주소를 찾을 수 없습니다.");
        $("#selectedPlaceName").val(
          `사용자 지정 위치 (${lat.toFixed(6)}, ${lng.toFixed(6)})`
        );
        $("#customAddress").val("");
        return;
      }

      const selectedAddress = extractReadableAddress(data);

      if (!selectedAddress) {
        alert("도로명 주소를 찾을 수 없습니다.");
        $("#selectedPlaceName").val(
          `사용자 지정 위치 (${lat.toFixed(6)}, ${lng.toFixed(6)})`
        );
        $("#customAddress").val("");
        return;
      }

      // 도로명 주소 또는 지번 주소 자동 적용
      $("#selectedPlaceName").val(selectedAddress);
      $("#selectedPlaceName").addClass("selected");
      $("#selectedMarker").show(); // 마킹 활성화
    },
    error: function () {
      alert("도로명 주소 조회 중 오류가 발생했습니다.");
      $("#selectedPlaceName").val(
        `사용자 지정 위치 (${lat.toFixed(6)}, ${lng.toFixed(6)})`
      );
      $("#customAddress").val("");
    },
  });
}

// [3] 주소 추출 함수
function extractReadableAddress(data) {
  const roadResult = data.results.find((r) => r.name === "roadaddr");
  const addrResult = data.results.find((r) => r.name === "addr");

  if (roadResult) {
    return `${roadResult.region.area1.name} ${roadResult.region.area2.name} ${
      roadResult.region.area3.name
    } ${roadResult.land?.name || ""} ${roadResult.land?.number1 || ""}`.trim();
  } else if (addrResult) {
    return `${addrResult.region.area1.name} ${addrResult.region.area2.name} ${addrResult.region.area3.name}`;
  }
  return null;
}

// [4] 마커 초기화
export function resetMarkers() {
  // 마커 초기화 (숨기기)
  if (mainMarker) {
    mainMarker.setMap(null);
    mainMarker = null;
  }
  // 사용자 지정 마커 숨기기
  if (customMarker) {
    customMarker.setMap(null);
    customMarker = null;
  }
}
