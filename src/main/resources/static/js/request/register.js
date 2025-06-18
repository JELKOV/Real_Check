// [0] 전역 변수 설정
let mainMap;
let mainMarker = null;
// 사용자 지정 장소 마커
let customMarker = null;
let currentFocus = -1;
// ↓ 키 탐색 중에는 자동 검색(debounce AJAX)을 막기 위한 플래그
let isNavigatingByKey = false;

// 기본 카테고리 설정
const allCategories = [
  { value: "PARKING", text: "🅿️ 주차 가능 여부" },
  { value: "WAITING_STATUS", text: "⏳ 대기 상태" },
  { value: "STREET_VENDOR", text: "🥟 노점 현황" },
  { value: "PHOTO_REQUEST", text: "📸 현장 사진 요청" },
  { value: "BUSINESS_STATUS", text: "🏪 가게 영업 여부" },
  { value: "OPEN_SEAT", text: "💺 좌석 여유" },
  { value: "BATHROOM", text: "🚻 화장실 여부" },
  { value: "WEATHER_LOCAL", text: "☁️ 현장 날씨" },
  { value: "NOISE_LEVEL", text: "🔊 소음 여부" },
  { value: "FOOD_MENU", text: "🍔 메뉴/음식" },
  { value: "CROWD_LEVEL", text: "👥 혼잡도" },
  { value: "ETC", text: "❓ 기타" },
];
const titlePlaceholderMap = {
  PARKING: "주차 가능한 공간이 있나요?",
  WAITING_STATUS: "대기 줄이 긴가요?",
  STREET_VENDOR: "붕어빵 노점 지금 하나요?",
  PHOTO_REQUEST: "현장 사진 부탁드릴게요!",
  BUSINESS_STATUS: "가게 문 열었나요?",
  OPEN_SEAT: "자리 여유 있나요?",
  BATHROOM: "화장실 이용 가능한가요?",
  WEATHER_LOCAL: "지금 비 오나요?",
  NOISE_LEVEL: "조용한 곳인가요?",
  FOOD_MENU: "오늘 점심 뭐 나와요?",
  CROWD_LEVEL: "많이 붐비나요?",
  ETC: "궁금한 현장의 정보를 자유롭게 요청하세요!",
};
const contentPlaceholderMap = {
  PARKING: "EX) 압구정 로데오 공영주차장에 지금 주차할 수 있나요?",
  WAITING_STATUS: "EX) 강남역 갓덴스시 현재 대기 줄 몇 명 정도인가요?",
  STREET_VENDOR: "EX) 테헤란로 농협 앞 붕어빵집 오늘도 운영하나요?",
  PHOTO_REQUEST: "EX) 부산 해운대 근처 날씨 확인 가능한 사진 부탁드려요!",
  BUSINESS_STATUS: "EX) 공휴일인데 오늘 가게 문 열었는지 궁금해요.",
  OPEN_SEAT: "EX) 스타벅스 서울대입구점 지금 좌석 여유 있나요?",
  BATHROOM: "EX) OO공원 근처에 화장실 이용 가능한 곳 있나요?",
  WEATHER_LOCAL: "EX) 홍대 앞 지금 비 오고 있나요?",
  NOISE_LEVEL: "EX) 신촌역 근처 조용한 카페 찾고 있어요. 주변 소음 어떤가요?",
  FOOD_MENU: "EX) 학교식당 오늘 점심 메뉴 아시는 분 계신가요?",
  CROWD_LEVEL: "EX) 석촌호수 산책로 지금 사람 많은가요?",
  ETC: "EX) 오늘 여의도 불꽃축제 사람들 이동 상황 어떤가요?",
};

// [1] 페이지 초기화 코드
$(document).ready(function () {
  initRequestPage();
});

// [2] 페이지 초기화 관련 함수
function initRequestPage() {
  initializeMap();
  bindEventListeners();
  initializePage();
}

// [3] 지도 초기화
function initializeMap() {
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

// [4] 이벤트 리스너 바인딩
function bindEventListeners() {
  // 장소 검색 이벤트
  bindSearchEvents();
  // 키보드 이동 이벤트
  bindKeyboardEvents();
  // 사용자 지정 장소 이벤트
  bindCustomPlaceEvents();
  // 카테고리 및 요청 등록 이벤트
  bindFormEvents();
  // 토글 버튼 이벤트 (검색/사용자 지정)
  bindToggleEvents();
  // 검색 결과 닫기 이벤트
  bindCloseEvents();
}

// [5] 페이지 초기화 (카테고리 및 기본 설정)
function initializePage() {
  updateCategoryDropdown(allCategories.map((cat) => cat.value));
  resetPlaceholders();
  resetSelectedPlace();
}

// [5-1] 카테고리 및 Placeholder 설정
function updateCategoryDropdown(allowedCategories) {
  const categoryDropdown = $("#category").empty();
  categoryDropdown.append('<option value="">카테고리를 선택하세요</option>');
  allCategories.forEach((cat) => {
    if (allowedCategories.includes(cat.value)) {
      categoryDropdown.append(
        `<option value="${cat.value}">${cat.text}</option>`
      );
    }
  });
  resetPlaceholders();
}

// [5-2] Placeholder 초기화
function resetPlaceholders() {
  const defaultTitle = "예: 궁금한 점을 입력하세요";
  const defaultContent = "요청 내용을 입력하세요";
  $("#title").attr("placeholder", defaultTitle);
  $("#content").attr("placeholder", defaultContent);
}

// [5-3] 장소 초기화
function resetSelectedPlace(isCustom = false) {
  // 입력 필드 및 선택된 UI 초기화
  $("#selectedPlaceName").val("").removeClass("selected");
  $("#selectedMarker").hide();
  $("#placeId").val("");
  $("#lat").val("");
  $("#lng").val("");
  $(".place-item").removeClass("selected"); // 검색 결과 UI 초기화

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

  // 장소 정보 숨기기 (infoSection)
  $("#infoSection").hide();

  // 사용자 지정 장소일 경우 주소창도 초기화
  if (isCustom) {
    $("#customAddress").val("");
  }
}

// [6] 바인딩 함수화

// [6-1] 장소 검색 이벤트
function bindSearchEvents() {
  $("#placeSearch").on("input", debounce(handlePlaceSearch, 300));
  $("#placeSearchResults").on("mousedown", ".place-item", handlePlaceSelect);
  $("#placeSearchResults").on("mouseenter", ".place-item", highlightItem);
}

// [6-1-1] 장소 검색 (AJAX)
// 검색창에 입력된 문자열을 기준으로 서버에 장소 검색 요청을 보냄
function handlePlaceSearch() {
  // 현재 ↓ 키 탐색 중일 경우 (리스트 포커스 이동 중일 경우)

  // 검색 요청을 차단하여 리스트가 덮어써지는 현상을 방지
  if (isNavigatingByKey) {
    return;
  }

  // 검색어 가져오기 (앞뒤 공백 제거)
  const query = $("#placeSearch").val().trim();

  // 검색어가 비어있으면: 검색 결과 숨기고 초기화
  if (!query) {
    $("#placeSearchResults").empty().hide();
    // 선택된 장소 초기화
    resetSelectedPlace();
    // 카테고리 드롭다운 전체 복원
    resetCategoryDropdown();
    return;
  }

  // 정상적인 검색어가 존재하는 경우: 서버에 GET 요청
  $.get("/api/place/search", { query })
    // 검색 결과는 renderSearchResults() 함수로 렌더링
    .done(renderSearchResults)
    .fail(() => console.error("장소 검색 실패"));
}

// [6-1-1-1] 검색 결과 렌더링
// 서버에서 받은 장소 목록(places)을 <li>로 변환하여 화면에 표시
// 키보드 포커스 상태 유지도 함께 처리
function renderSearchResults(places) {
  // 장소가 존재할 경우 → 리스트 아이템 HTML 생성
  // 장소가 없을 경우 → "검색 결과 없음" 메시지 출력
  const resultsHtml = places.length
    ? places.map(createPlaceItemHtml).join("")
    : '<li class="list-group-item">검색 결과가 없습니다.</li>';
  // 결과 리스트를 DOM에 삽입하고 보이도록 설정
  $("#placeSearchResults").html(resultsHtml).show();

  // 포커스 초기화 여부 판단
  // - 새롭게 그려진 리스트 중에 .selected가 하나도 없다면 → 포커스를 초기화 (-1)
  // - 만약 selected가 유지된 항목이 있다면 → 스크롤 위치 유지
  if (!$(".place-item.selected").length) {
    currentFocus = -1;
  } else {
    updateSelection($(".place-item"));
  }
}

// [6-1-1-1-1] 장소 관련 폼 만들기
function createPlaceItemHtml(place) {
  return `<li class="list-group-item place-item" 
            data-id="${place.id}" 
            data-lat="${place.lat}" 
            data-lng="${place.lng}" 
            data-name="${place.name}">
            ${place.name}
          </li>`;
}

// [6-1-2] 마우스 탐색 (클릭)
function handlePlaceSelect(e) {
  e.preventDefault();
  selectPlace($(e.target));
}

// [6-1-3] 마우스 탐색 (호버)
function highlightItem() {
  $(".place-item").removeClass("selected");
  $(this).addClass("selected");
  currentFocus = $(".place-item").index($(this));
}

// [6-2] 키보드 이벤트  ↓ / ↑ / Enter 누를 때: 포커스 이동 + 자동 검색 차단
function bindKeyboardEvents() {
  $("#placeSearch").on("keydown", handlePlaceSearchKeyDown);
  $(document).on("keydown", handleGlobalKeyDown);
}

// [6-2-1] 키보드 다운 업 버튼 이벤트Z
function handlePlaceSearchKeyDown(e) {
  // ↓키 등으로 리스트 탐색 중이라는 상태를 true로 설정
  if (["ArrowDown", "ArrowUp", "Enter"].includes(e.key)) {
    isNavigatingByKey = true;
    // 항목 선택 / 포커스 처리 함수 실행
    handleKeyboardNavigation(e);
    setTimeout(() => {
      isNavigatingByKey = false;
    }, 500);
  }
}

// [6-2-1-1] 키보드 탐색 (Arrow + Enter)
function handleKeyboardNavigation(e) {
  const items = $(".place-item");
  if (items.length === 0) return;

  if (e.key === "ArrowDown") {
    e.preventDefault();
    currentFocus = (currentFocus + 1) % items.length;
  } else if (e.key === "ArrowUp") {
    e.preventDefault();
    currentFocus = (currentFocus - 1 + items.length) % items.length;
  } else if (e.key === "Enter" && currentFocus >= 0) {
    e.preventDefault();
    selectPlace($(items[currentFocus]));
  }
  updateSelection(items);
}

// [6-2-2] ESC 키로 검색 결과 닫기
function handleGlobalKeyDown(e) {
  if (e.key === "Escape") {
    $("#placeSearchResults").hide();
  }
}

// [6-3] 사용자 지정 장소 검색 이벤트
function bindCustomPlaceEvents() {
  // 사용자 지정 장소 주소 검색
  $("#searchAddressBtn").click(handleCustomAddressSearch);
  // 사용자 지정 주소 Enter 키 검색
  $("#customAddress").on("keydown", handleEnterKeyForCustomAddress);
}

// [6-3-1] 사용자 지정 주소 검색 (Naver Geocoder API 사용)
function handleCustomAddressSearch() {
  const query = $("#customAddress").val().trim();
  if (!query) {
    alert("주소를 입력하세요.");
    return;
  }

  naver.maps.Service.geocode({ query }, function (status, response) {
    console.log("Geocode 응답:", response);

    if (
      status !== naver.maps.Service.Status.OK ||
      !response.v2.addresses.length
    ) {
      alert("주소 검색에 실패했습니다.");
      return;
    }

    const result = response.v2.addresses[0];
    if (!result) {
      alert("검색된 주소가 없습니다.");
      return;
    }

    const lat = parseFloat(result.y);
    const lng = parseFloat(result.x);
    if (isNaN(lat) || isNaN(lng)) {
      alert("검색된 주소의 좌표를 읽을 수 없습니다.");
      return;
    }

    // 주소 정보를 직접 설정
    setCustomPlace(lat, lng);
  });
}

// [6-3-2] 사용자 지정 주소 엔터 키 검색 (함수화)
function handleEnterKeyForCustomAddress(e) {
  if (e.key === "Enter" && $("#customAddress").is(":focus")) {
    e.preventDefault(); // 기본 엔터 동작 막기 (폼 제출 방지)
    handleCustomAddressSearch(); // 검색 함수 실행
  }
}

// [6-4] 폼 이벤트
function bindFormEvents() {
  $("#category").on("change", updatePlaceholders);
  $("#requestForm").on("submit", submitRequest);
}

// [6-4-1] Placeholder 초기화 및 자동 설정
function updatePlaceholders() {
  const cat = $("#category").val();
  $("#title").attr(
    "placeholder",
    titlePlaceholderMap[cat] || "예: 궁금한 점을 입력하세요"
  );
  $("#content").attr(
    "placeholder",
    contentPlaceholderMap[cat] || "요청 내용을 입력하세요"
  );
}

// [6-4-2] 요청 등록 (AJAX)
function submitRequest(e) {
  console.log("클릭됨");
  e.preventDefault();

  const isCustomPlace = $("#customPlaceSection").is(":visible");
  console.log(isCustomPlace);

  const requestData = {
    title: $("#title").val(),
    content: $("#content").val(),
    point: parseInt($("#point").val()),
    category: $("#category").val(),
    placeId: $("#placeId").val() || null,
    lat: $("#lat").val() ? parseFloat($("#lat").val()) : null,
    lng: $("#lng").val() ? parseFloat($("#lng").val()) : null,
    customPlaceName: isCustomPlace ? $("#selectedPlaceName").val() : null,
  };

  // 요청 포인트 검사
  if (!requestData.point || requestData.point < 10) {
    alert("요청은 최소 10포인트 이상이어야 합니다.");
    return;
  }

  // 공통 필수 항목 검사
  if (!requestData.title || !requestData.content || !requestData.category) {
    alert("필수 입력 항목을 확인해주세요.");
    return;
  }

  // 사용자 지정 장소일 경우: 주소/좌표/이름 필수
  if (
    isCustomPlace &&
    (!requestData.lat || !requestData.lng || !requestData.customPlaceName)
  ) {
    alert("사용자 지정 장소의 주소 또는 좌표가 정확하지 않습니다.");
    return;
  }

  // 공식 장소일 경우: placeId 반드시 있어야 함
  if (!isCustomPlace && !requestData.placeId) {
    alert("공식 장소를 선택해주세요.");
    return;
  }

  $.ajax({
    url: "/api/request", // 실제 API 경로에 맞게 수정
    method: "POST",
    contentType: "application/json", // JSON 형식으로 전송
    data: JSON.stringify(requestData), // JSON 문자열로 변환
    success: function () {
      alert("요청이 등록되었습니다!");
      location.href = "/my-requests";
    },
    error: function (xhr) {
      alert("등록 실패: " + xhr.responseText);
    },
  });
}

// [6-5] 토글 이벤트
function bindToggleEvents() {
  $("#btnPlace").click(() => togglePlaceMode("search"));
  $("#btnCustom").click(() => togglePlaceMode("custom"));
}

// [6-5-1] 토글 버튼 (검색/사용자 지정)
function togglePlaceMode(mode) {
  const isCustom = mode === "custom";

  $("#customPlaceSection").toggle(isCustom);
  $("#searchSection").toggle(!isCustom);

  resetSelectedPlace(isCustom);
  resetCategoryDropdown();

  const [activeBtn, inactiveBtn] = isCustom
    ? ["#btnCustom", "#btnPlace"]
    : ["#btnPlace", "#btnCustom"];

  toggleButtonStyles(activeBtn, inactiveBtn);
}

// [6-5-2] 버튼 스타일 전환 함수
function toggleButtonStyles(active, inactive) {
  $(active).addClass("btn-primary").removeClass("btn-outline-primary");
  $(inactive).addClass("btn-outline-primary").removeClass("btn-primary");
}

// [6-6] 외부 클릭으로 검색 결과 닫기 이벤트
function bindCloseEvents() {
  $(document).click(handleDocumentClick);
}

// [6-6-1] 외부 클릭으로 검색 결과 닫기 관련 함수
function handleDocumentClick(e) {
  if (!$(e.target).closest("#placeSearch, #placeSearchResults").length) {
    $("#placeSearchResults").hide();
  }
}

// ─────────────────────────────────────
// 공통 유틸 함수
// ─────────────────────────────────────

// [1] Debounce 함수 (입력 지연 제어)
function debounce(func, delay) {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, arguments), delay);
  };
}

// [2] 카테고리 드롭다운 초기화 함수
function resetCategoryDropdown() {
  updateCategoryDropdown(allCategories.map((cat) => cat.value));
}

// [3] 선택된 항목 강조 (키보드 탐색)
function updateSelection(items) {
  items.removeClass("selected");
  if (currentFocus >= 0 && currentFocus < items.length) {
    $(items[currentFocus]).addClass("selected");
    $(items[currentFocus])[0].scrollIntoView({
      behavior: "smooth",
      block: "nearest",
    });
  }
}

// [4] 사용자 지정 장소 설정 (도로명 주소 자동 표시)
function setCustomPlace(lat, lng) {
  createOrUpdateMarker("custom", lat, lng);
  mainMap.setCenter(new naver.maps.LatLng(lat, lng));

  // 위도/경도 값 저장
  $("#lat").val(lat);
  $("#lng").val(lng);

  $("#customAddress").val("");

  // 도로명 주소 자동 검색 (handleCustomAddressSearch 로직 사용)
  reverseGeocodeByLatLng(lat, lng);
}

// [4-1] 도로명 주소 조회 (좌표 → 주소 변환)
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

// [4-1-1] 주소 추출 함수
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

// [5] 장소 선택 함수
function selectPlace(item) {
  const lat = parseFloat(item.data("lat"));
  const lng = parseFloat(item.data("lng"));
  const placeName = item.data("name");
  const placeId = item.data("id");

  // 기존 선택 초기화
  resetSelectedPlace();
  $(".place-item").removeClass("selected");
  item.addClass("selected");

  // 선택된 장소 정보 표시
  $("#selectedPlaceName").val(placeName).show();
  $("#selectedPlaceName").addClass("selected");
  $("#selectedMarker").show(); // 마킹 활성화

  setMainMarker(lat, lng, placeName, placeId);
  $("#placeSearch").val(placeName);
  $("#lat").val(lat);
  $("#lng").val(lng);
  $("#placeId").val(placeId || "");

  if (placeId) {
    loadPlaceDetails(placeId);
  } else {
    $("#infoSection").hide();
    updateCategoryDropdown(allCategories.map((cat) => cat.value));
  }

  $("#placeSearch").val(placeName);
}

// [5-1] 장소 세부 정보 로드
function loadPlaceDetails(placeId) {
  $.get(`/api/place/${placeId}/details`, function (data) {
    $("#infoSection").show();
    $("#infoAddress").text(data.address || "주소 정보 없음");
    $("#infoRecent").text(data.recentInfo || "최근 정보 없음");
    // 커뮤니티 링크 설정
    if (data.communityLink) {
      $("#infoLink")
        .attr("href", data.communityLink)
        .attr("target", "_blank") // 새 창으로 열기 (선택)
        .text("커뮤니티 페이지");
    } else {
      $("#infoLink").hide();
    }

    // 카테고리 필터링 적용
    if (data.allowedRequestTypes) {
      updateCategoryDropdown(data.allowedRequestTypes);
    }
  }).fail(function (xhr) {
    console.error("API Error:", xhr.responseText);
  });
}

// [5-2] 장소 마커 설정 (공식 장소)
function setMainMarker(lat, lng, id = "") {
  createOrUpdateMarker("main", lat, lng);
  mainMap.setCenter(new naver.maps.LatLng(lat, lng));

  // 장소 정보 설정 (lat, lng, placeId)
  $("#lat").val(lat);
  $("#lng").val(lng);
  $("#placeId").val(id || "");

  // 사용자 지정 마커 숨기기
  if (customMarker) customMarker.setMap(null);
}

// [6] 마커 생성/갱신 공통 함수
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
