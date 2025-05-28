let currentFocus = -1; // 현재 키보드로 포커싱된 검색 결과 인덱스
let isNavigatingByKey = false; // 키보드 탐색 중 여부 플래그
let mainMap = null; // 네이버 지도 인스턴스
let placeMarkers = []; // 지도에 표시된 마커 목록
let selectedPlaceId = null;

// DOM 로딩 후 초기 실행
$(document).ready(function () {
  initializeMap(); // [1] 지도 초기화
  bindMapControlEvents(); // [2] 지도 버튼 이벤트 연결 (내 위치, 새로고침)
  bindSearchInputEvents(); // [3] 검색창 입력 및 키보드 탐색 이벤트 연결
  bindSearchResultEvents(); // [4] 검색 결과 클릭, hover, 즐겨찾기 버튼 이벤트
});

// [1] 지도 초기화
function initializeMap() {
  mainMap = new naver.maps.Map("mainMap", {
    center: new naver.maps.LatLng(37.5665, 126.978), // 서울 기본
    zoom: 13,
  });

  getUserLocation();
}

// [2] 지도 컨트롤 버튼(내 위치 / 주변 새로고침) 핸들링
function bindMapControlEvents() {
  // 내 위치 버튼 클릭 시 현재 위치 가져오기
  $("#myLocationButton").on("click", getUserLocation);

  // 새로고침 버튼 클릭 시 현재 지도 중심 기준 주변 장소 재로드
  $("#refreshNearbyButton").on("click", function () {
    const center = mainMap.getCenter();
    loadNearbyPlaces(center.lat(), center.lng());
  });
}

// [3] 검색 입력창 이벤트 바인딩
function bindSearchInputEvents() {
  // 검색 입력 디바운스 처리
  $("#placeSearch").on("input", debounce(handlePlaceSearch, 300));

  // Enter 키 입력 시, 포커스가 없으면 첫 번째 항목 선택
  $("#placeSearch").on("keypress", function (e) {
    if (e.which === 13 && currentFocus === -1) {
      const firstItem = $("#placeSearchResults .place-item").first();
      if (firstItem.length) firstItem.click();
    }
  });

  // ↑ ↓ 키보드 탐색 및 Enter로 선택
  $("#placeSearch").on("keydown", function (e) {
    const items = $("#placeSearchResults .place-item");
    if (!items.length) return;

    if (["ArrowDown", "ArrowUp"].includes(e.key)) {
      isNavigatingByKey = true;
      setTimeout(() => (isNavigatingByKey = false), 500);
    }

    // ↓ 키: 다음 항목으로 이동
    if (e.key === "ArrowDown") {
      e.preventDefault();
      currentFocus = (currentFocus + 1) % items.length;
      setActive(items);
    }
    // ↑ 키: 이전 항목으로 이동
    else if (e.key === "ArrowUp") {
      e.preventDefault();
      currentFocus = (currentFocus - 1 + items.length) % items.length;
      setActive(items);
    }
    // Enter 키: 현재 포커싱된 항목 클릭
    else if (e.key === "Enter") {
      e.preventDefault();
      if (currentFocus >= 0 && currentFocus < items.length) {
        items.eq(currentFocus).click();
      }
    }
  });
}

// [4] 검색 결과 리스트 및 즐겨찾기 버튼 이벤트 바인딩
function bindSearchResultEvents() {
  // 마우스 클릭 시 장소 상세 정보 출력
  $("#placeSearchResults").on("click", ".place-item", function () {
    const placeId = $(this).data("id");
    const placeName = $(this).data("name");
    const placeAddress = $(this).data("address");
    const lat = parseFloat($(this).data("lat"));
    const lng = parseFloat($(this).data("lng"));

    renderSelectedPlaceInfo(placeId, placeName, placeAddress, lat, lng);
  });

  // 마우스 hover 시 포커싱 효과
  $("#placeSearchResults").on("mouseenter", ".place-item", function () {
    const items = $("#placeSearchResults .place-item");
    currentFocus = items.index(this);
    setActive(items);
  });

  // 즐겨찾기 버튼 이벤트 바인딩
  $("#favoriteBtn").on("click", function () {
    if (!isLoggedIn) {
      alert("즐겨찾기는 로그인 후 이용 가능합니다.");
      return;
    }

    if (!selectedPlaceId) {
      alert("장소를 먼저 선택해주세요.");
      return;
    }

    const btn = $(this);

    $.post(`/api/place/${selectedPlaceId}/favorite`)
      .done((msg) => {
        updateFavoriteButtonUI(btn);
        console.log(msg);
      })
      .fail(() => alert("즐겨찾기 요청 실패"));
  });
}

/**
 *  1. 검색 결과 탐색 및 렌더링 관련
 */

// [1] 키보드 포커스 시 하이라이트 처리
function setActive(items) {
  items.removeClass("active");
  if (currentFocus >= 0 && currentFocus < items.length) {
    const target = items.eq(currentFocus);
    target.addClass("active");
    target[0].scrollIntoView({ behavior: "smooth", block: "nearest" });
  }
}

// [2] 검색어 입력 시 호출되는 API 처리
function handlePlaceSearch() {
  if (isNavigatingByKey) return;

  const query = $("#placeSearch").val().trim();
  if (!query) {
    $("#placeSearchResults").empty().hide();
    $("#selectedPlaceInfo").addClass("d-none");
    clearMarkers();
    return;
  }

  $.get("/api/place/search", { query })
    .done(renderSearchResults)
    .fail(() => console.error("검색 실패"));
}

// [3] 검색 API 결과 렌더링
function renderSearchResults(places) {
  currentFocus = -1;
  clearMarkers();

  if (!places.length) {
    $("#placeSearchResults")
      .html('<li class="list-group-item text-muted">검색 결과 없음</li>')
      .show();
    return;
  }

  const keyword = $("#placeSearch").val().trim().toLowerCase();
  const escapedKeyword = escapeRegex(keyword);

  const html = places
    .map((place) => {
      const name = place.name.replace(
        new RegExp(`(${escapedKeyword})`, "gi"),
        "<mark>$1</mark>"
      );

      addPlaceMarker(place); // 검색 결과도 지도에 마커 표시

      return `<li class="list-group-item place-item" 
                 data-id="${place.id}" 
                 data-name="${place.name}" 
                 data-address="${place.address}" 
                 data-lat="${place.lat}" 
                 data-lng="${place.lng}">
                <strong>${name}</strong><br/>
                <small class="text-muted">${place.address}</small>
              </li>`;
    })
    .join("");

  $("#placeSearchResults").html(html).show();
}

/**
 *  2. 장소 선택 및 마커 관련
 */

// [4] 특정 장소를 선택했을 때 정보 표시 및 마커 이동
function renderSelectedPlaceInfo(id, name, address, lat, lng) {
  $("#placeName").text(name);
  $("#placeAddress").text("📍 " + address);
  $("#communityLink").attr("href", `/place/community/${id}`);
  $("#favoriteBtn")
    .removeClass("btn-warning")
    .addClass("btn-outline-warning")
    .text("☆ 즐겨찾기 추가");
  $("#selectedPlaceInfo").removeClass("d-none");

  // 선택된 장소 ID 저장
  selectedPlaceId = id;

  if (isLoggedIn) {
    $("#favoriteBtn").show(); // 로그인 되어 있으면 보임
  } else {
    $("#favoriteBtn").hide(); // 로그인 안 되어 있으면 숨김
  }

  // 지도 이동 및 마커 표시
  const latlng = new naver.maps.LatLng(lat, lng);
  mainMap.setCenter(latlng);
  clearMarkers();
  addPlaceMarker({ lat, lng, name });

  // 즐겨찾기 상태 확인 후 버튼 초기화
  if (isLoggedIn) {
    $.get(`/api/place/${id}/is-favorite`).done((isFavorite) => {
      const btn = $("#favoriteBtn");
      btn.toggleClass("btn-outline-warning", !isFavorite);
      btn.toggleClass("btn-warning", isFavorite);
      btn.text(isFavorite ? "⭐ 즐겨찾기 완료" : "☆ 즐겨찾기 추가");
    });
  }
}

// [5] 마커 추가
function addPlaceMarker(place) {
  const marker = new naver.maps.Marker({
    position: new naver.maps.LatLng(place.lat, place.lng),
    map: mainMap,
    title: place.name,
  });

  placeMarkers.push(marker);

  const infoWindow = new naver.maps.InfoWindow({
    content: `<div style="padding:5px;">${place.name}</div>`,
  });

  naver.maps.Event.addListener(marker, "click", function () {
    infoWindow.open(mainMap, marker);
  });
}

// [6] 마커 전체 제거
function clearMarkers() {
  placeMarkers.forEach((m) => m.setMap(null));
  placeMarkers = [];
}

// [7] 내 위치 받아오기 및 지도 중심 설정
function getUserLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      function (pos) {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        mainMap.setCenter(new naver.maps.LatLng(lat, lng));
        loadNearbyPlaces(lat, lng);
      },
      () => alert("위치 정보를 가져올 수 없습니다."),
      { enableHighAccuracy: true }
    );
  } else {
    alert("브라우저가 위치를 지원하지 않습니다.");
  }
}

// [8] 주변 장소 API 호출 및 마커 렌더링
function loadNearbyPlaces(lat, lng) {
  $.get("/api/place/nearby", { lat, lng, radiusMeters: 1000 }).done(
    (places) => {
      clearMarkers();

      places.forEach((place) => {
        const marker = new naver.maps.Marker({
          position: new naver.maps.LatLng(place.lat, place.lng),
          map: mainMap,
          title: place.name,
        });

        const infoWindow = new naver.maps.InfoWindow({
          content: `<div style="padding:5px;">${place.name}</div>`,
        });

        naver.maps.Event.addListener(marker, "click", () => {
          infoWindow.open(mainMap, marker);
        });

        placeMarkers.push(marker);
      });
    }
  );
}

/**
 * 3. 유틸리티 함수
 */

// [9] 정규식 특수문자 이스케이프 처리
function escapeRegex(text) {
  return text.replace(/[-\/\\^$*+?.()|[\]{}]/g, "\\$&");
}

// [10] 디바운스 유틸 함수
function debounce(fn, delay) {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(() => fn.apply(this, arguments), delay);
  };
}

// [11] 즐겨찾기 버튼 UI 상태 업데이트
function updateFavoriteButtonUI(btn) {
  const isNowFavorite = !btn.hasClass("btn-warning"); // 현재가 즐겨찾기 아님 → 등록되는 상태
  btn.toggleClass("btn-outline-warning", !isNowFavorite);
  btn.toggleClass("btn-warning", isNowFavorite);
  btn.text(isNowFavorite ? "⭐ 즐겨찾기 완료" : "☆ 즐겨찾기 추가");
}
