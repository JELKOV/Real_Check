// ─────────────────────────────────────────────
// [0] 카테고리 한글 라벨 맵
// ─────────────────────────────────────────────
const categoryLabelMap = {
  PARKING: "🅿️ 주차 가능 여부",
  WAITING_STATUS: "⏳ 대기 상태",
  STREET_VENDOR: "🥟 노점 현황",
  PHOTO_REQUEST: "📸 사진 요청",
  BUSINESS_STATUS: "🏪 가게 영업 여부",
  OPEN_SEAT: "💺 좌석 여유",
  BATHROOM: "🚻 화장실 여부",
  WEATHER_LOCAL: "☁️ 날씨 상태",
  NOISE_LEVEL: "🔊 소음 여부",
  FOOD_MENU: "🍔 메뉴/음식",
  CROWD_LEVEL: "👥 혼잡도",
  ETC: "❓ 기타",
};

// ─────────────────────────────────────────────
// [1] 문서 준비 시 초기화
// ─────────────────────────────────────────────
$(document).ready(function () {
  initMap();
  renderCategoryLabels();
  bindSearchAddress();
  bindPlaceFormSubmit();
  bindCategorySelectAll();
});

// ─────────────────────────────────────────────
// [2] 지도 초기화 및 클릭 이벤트 바인딩
// ─────────────────────────────────────────────
let map, marker;

function initMap() {
  const defaultCenter = new naver.maps.LatLng(37.5665, 126.978);
  map = new naver.maps.Map("map", {
    center: defaultCenter,
    zoom: 15,
  });

  marker = new naver.maps.Marker({
    position: defaultCenter,
    map: map,
  });

  naver.maps.Event.addListener(map, "click", function (e) {
    const lat = e.coord.lat();
    const lng = e.coord.lng();
    marker.setPosition(e.coord);
    $("#lat").val(lat);
    $("#lng").val(lng);

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
  });
}

// ─────────────────────────────────────────────
// [3] 카테고리 라벨 렌더링
// ─────────────────────────────────────────────
function renderCategoryLabels() {
  $(".category-label").each(function () {
    const code = $(this).data("category");
    $(this).text(categoryLabelMap[code] || code);
  });
}

// ─────────────────────────────────────────────
// [4] 주소 검색 버튼 바인딩 + 엔터 방지 및 처리
// ─────────────────────────────────────────────
function bindSearchAddress() {
  $("#searchAddressBtn").click(searchAddress);

  // 주소 입력창에서 Enter 눌렀을 때 form 제출 막고 검색 실행
  $("#addressInput").on("keydown", function (e) {
    if (e.key === "Enter") {
      e.preventDefault();
      searchAddress();
    }
  });
}

function searchAddress() {
  const query = $("#addressInput").val();
  if (!query) return alert("주소를 입력하세요.");

  naver.maps.Service.geocode({ query }, function (status, response) {
    if (status !== naver.maps.Service.Status.OK)
      return alert("주소를 찾을 수 없습니다.");

    const item = response.v2.addresses[0];
    if (!item) return alert("주소 결과 없음");

    const latlng = new naver.maps.LatLng(item.y, item.x);
    map.setCenter(latlng);
    marker.setPosition(latlng);

    $("#address").val(item.roadAddress || item.jibunAddress);
    $("#lat").val(item.y);
    $("#lng").val(item.x);
  });
}

// ─────────────────────────────────────────────
// [5] 장소 등록 폼 제출 처리 (유효성 검사 포함)
// ─────────────────────────────────────────────
function bindPlaceFormSubmit() {
  $("#placeForm").on("submit", function (e) {
    e.preventDefault();

    const name = $("input[name='name']").val();
    const address = $("input[name='address']").val();
    const lat = $("#lat").val();
    const lng = $("#lng").val();
    const selectedCategories = $("input[name='categories']:checked")
      .map(function () {
        return this.value;
      })
      .get();

    // 유효성 검사
    if (!name) return alert("📌 장소 이름을 입력하세요.");
    if (!address || !lat || !lng) return alert("📌 주소를 검색하거나 지도를 클릭하세요.");
    if (selectedCategories.length === 0) return alert("📌 최소 하나 이상의 카테고리를 선택하세요.");

    const data = {
      name,
      address,
      lat: parseFloat(lat),
      lng: parseFloat(lng),
      categories: selectedCategories,
    };

    $.ajax({
      url: "/api/place",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(data),
      success: function () {
        alert("✅ 장소 등록 요청 완료");
        window.location.href = "/";
      },
      error: function (xhr) {
        alert("❌ 등록 실패: " + xhr.responseText);
      },
    });
  });
}

// ─────────────────────────────────────────────
// [6] 카테고리 전체 선택 기능
// ─────────────────────────────────────────────
function bindCategorySelectAll() {
  $("#selectAllCategoriesBtn").click(function () {
    $("input[name='categories']").prop("checked", true);
  });

  $("#deselectAllCategoriesBtn").click(function () {
    $("input[name='categories']").prop("checked", false);
  });
}
