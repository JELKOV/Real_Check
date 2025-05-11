$(document).ready(function () {
  initializeMap();
  bindEventListeners();
  initializePage();
});

// [1] 지도 초기화
let mainMap;
let mainMarker = null;
let currentFocus = -1;

function initializeMap() {
  mainMap = new naver.maps.Map("mainMap", {
    center: new naver.maps.LatLng(37.5665, 126.978),
    zoom: 13,
  });

  // 사용자 지정 장소 - 지도 클릭 시 마커 설정
  naver.maps.Event.addListener(mainMap, "click", function (e) {
    if ($("#searchSection").is(":visible")) return;
    setMainMarker(e.coord.lat(), e.coord.lng(), "사용자 지정 장소");
  });
}

// [2] 페이지 초기화 (카테고리 및 기본 설정)
function initializePage() {
  updateCategoryDropdown(allCategories.map((cat) => cat.value));
  resetPlaceholders();
  resetSelectedPlace();
}

// [3] 이벤트 리스너 바인딩
function bindEventListeners() {
  // 장소 검색
  $("#placeSearch").on("input", debounce(handlePlaceSearch, 300));
  $("#placeSearch").on("keydown", handleKeyboardNavigation);
  $("#placeSearchResults").on("mousedown", ".place-item", handlePlaceSelect);
  $("#placeSearchResults").on("mouseenter", ".place-item", highlightItem);
  $(document).on("keydown", handleGlobalKeyDown);
  $(document).click(handleDocumentClick);

  // 카테고리 및 요청 등록
  $("#category").on("change", updatePlaceholders);
  $("#requestForm").on("submit", submitRequest);

  // 토글 버튼 (검색/사용자 지정)
  $("#btnPlace").click(() => togglePlaceMode("search"));
  $("#btnCustom").click(() => togglePlaceMode("custom"));
}

// [4] 장소 검색 (AJAX)
function handlePlaceSearch() {
  const query = $("#placeSearch").val().trim();
  if (!query) {
    $("#placeSearchResults").empty().hide();
    resetSelectedPlace();
    resetCategoryDropdown();
    return;
  }

  $.get("/api/place/search", { query })
    .done(renderSearchResults)
    .fail(() => console.error("장소 검색 실패"));
}

// [5] 검색 결과 렌더링
function renderSearchResults(places) {
  const resultsHtml = places.length
    ? places.map(createPlaceItemHtml).join("")
    : '<li class="list-group-item">검색 결과가 없습니다.</li>';
  $("#placeSearchResults").html(resultsHtml).show();
  currentFocus = -1;
}

function createPlaceItemHtml(place) {
  return `<li class="list-group-item place-item" 
            data-id="${place.id}" 
            data-lat="${place.lat}" 
            data-lng="${place.lng}" 
            data-name="${place.name}">
            ${place.name}
          </li>`;
}

// [6] 키보드 탐색 (Arrow + Enter)
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

// [7] 선택된 항목 강조 (키보드 탐색)
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

// [8] 마우스 탐색 (클릭)
function handlePlaceSelect(e) {
  e.preventDefault();
  selectPlace($(e.target));
}

// [8] 마우스 탐색 (호버)
function highlightItem() {
  $(".place-item").removeClass("selected");
  $(this).addClass("selected");
  currentFocus = $(".place-item").index($(this));
}

// [9] ESC 키로 검색 결과 닫기
function handleGlobalKeyDown(e) {
  if (e.key === "Escape") {
    $("#placeSearchResults").hide();
  }
}

// [9] 외부 클릭으로 검색 결과 닫기
function handleDocumentClick(e) {
  if (!$(e.target).closest("#placeSearch, #placeSearchResults").length) {
    $("#placeSearchResults").hide();
  }
}

// [10] 장소 선택 함수
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

// [11] 장소 세부 정보 로드
function loadPlaceDetails(placeId) {
  $.get(`/api/place/${placeId}/details`, function (data) {
    $("#infoSection").show();
    $("#infoAddress").text(data.address || "주소 정보 없음");
    $("#infoRecent").text(data.recentInfo || "최근 정보 없음");
    $("#infoLink").attr("href", data.communityLink).text("커뮤니티 페이지");

    // 카테고리 필터링 적용
    if (data.allowedRequestTypes) {
      updateCategoryDropdown(data.allowedRequestTypes);
    }
  }).fail(function (xhr) {
    console.error("API Error:", xhr.responseText);
  });
}

// [12] 장소 초기화
function resetSelectedPlace() {
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

  // 장소 정보 숨기기 (infoSection)
  $("#infoSection").hide();
}

// [13] 장소 마커 설정
function setMainMarker(lat, lng, id = "") {
  if (!mainMarker) {
    mainMarker = new naver.maps.Marker({
      map: mainMap,
      position: new naver.maps.LatLng(lat, lng),
    });
  } else {
    mainMarker.setPosition(new naver.maps.LatLng(lat, lng));
    mainMarker.setMap(mainMap); // 마커 다시 표시
  }

  mainMap.setCenter(new naver.maps.LatLng(lat, lng));

  // 장소 정보 설정 (lat, lng, placeId)
  $("#lat").val(lat);
  $("#lng").val(lng);
  $("#placeId").val(id || "");
}

// [14] 카테고리 및 Placeholder 설정
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

// [15] 카테고리 드롭다운 초기화 함수
function resetCategoryDropdown() {
  updateCategoryDropdown(allCategories.map((cat) => cat.value));
}

// [16] Placeholder 초기화
function resetPlaceholders() {
  const defaultTitle = "예: 궁금한 점을 입력하세요";
  const defaultContent = "요청 내용을 입력하세요";
  $("#title").attr("placeholder", defaultTitle);
  $("#content").attr("placeholder", defaultContent);
}

// [17] 토글 버튼 (검색/사용자 지정)
function togglePlaceMode(mode) {
  if (mode === "search") {
    $("#searchSection").show();
    resetSelectedPlace();
    resetPlaceholders();
    toggleButtonStyles("#btnPlace", "#btnCustom");
  } else {
    $("#searchSection").hide();
    resetSelectedPlace();
    resetPlaceholders();
    toggleButtonStyles("#btnCustom", "#btnPlace");
  }
}

// [18] 버튼 스타일 전환 함수
function toggleButtonStyles(active, inactive) {
  $(active).addClass("btn-primary").removeClass("btn-outline-primary");
  $(inactive).addClass("btn-outline-primary").removeClass("btn-primary");
}

// [19] Placeholder 초기화 및 자동 설정
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

// [20] 요청 등록 (AJAX)
function submitRequest(e) {
  console.log("클릭됨");
  e.preventDefault();

  const requestData = {
    title: $("#title").val(),
    content: $("#content").val(),
    point: parseInt($("#point").val()),
    category: $("#category").val(),
    placeId: $("#placeId").val() || null,
    lat: $("#lat").val() ? parseFloat($("#lat").val()) : null,
    lng: $("#lng").val() ? parseFloat($("#lng").val()) : null,
  };

  // 요청 유효성 검사
  if (!requestData.title || !requestData.content || !requestData.category) {
    alert("필수 입력 항목을 확인해주세요.");
    return;
  }

  $.ajax({
    url: "/api/request",  // 실제 API 경로에 맞게 수정
    method: "POST",
    contentType: "application/json",  // JSON 형식으로 전송
    data: JSON.stringify(requestData),  // JSON 문자열로 변환
    success: function () {
      alert("요청이 등록되었습니다!");
      location.href = "/request/list";
    },
    error: function (xhr) {
      alert("등록 실패: " + xhr.responseText);
    },
  });
}

// [21] Debounce 함수 (입력 지연 제어)
function debounce(func, delay) {
  let timeout;
  return function () {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, arguments), delay);
  };
}

// [22] 기본 카테고리 설정
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
