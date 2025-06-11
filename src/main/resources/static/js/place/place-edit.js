// ─────────────────────────────────────────────
// [0] 카테고리 라벨 맵
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
// [1] 문서 초기화
// ─────────────────────────────────────────────
$(document).ready(function () {
  initMap();
  renderCategoryLabels();
  bindSearchAddress();
  bindFormSubmit();
  bindCategorySelectAll();
});

// ─────────────────────────────────────────────
// [2] 지도 로딩 + 클릭 이벤트
// ─────────────────────────────────────────────
let map, marker;

function initMap() {
  const lat = parseFloat($("#lat").val()) || 37.5665;
  const lng = parseFloat($("#lng").val()) || 126.978;
  const center = new naver.maps.LatLng(lat, lng);

  map = new naver.maps.Map("map", {
    center: center,
    zoom: 15,
  });

  marker = new naver.maps.Marker({
    position: center,
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
// [4] 주소 검색
// ─────────────────────────────────────────────
function bindSearchAddress() {
  $("#searchAddressBtn").click(searchAddress);
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

// [5] 수정 or 재등록 요청 제출
function bindFormSubmit() {
  $("#placeForm").on("submit", function (e) {
    e.preventDefault();

    // ✅ JSP에서 선언된 전역 변수 사용 (placeId, isRejected)
    const name = $("input[name='name']").val();
    const address = $("input[name='address']").val();
    const lat = $("#lat").val();
    const lng = $("#lng").val();
    const selectedCategories = $("input[name='categories']:checked")
      .map(function () {
        return this.value;
      })
      .get();

    if (!name) return alert("📌 장소 이름을 입력하세요.");
    if (!address || !lat || !lng)
      return alert("📌 주소를 입력하거나 지도를 클릭하세요.");
    if (selectedCategories.length === 0)
      return alert("📌 하나 이상의 카테고리를 선택하세요.");

    const data = {
      name,
      address,
      lat: parseFloat(lat),
      lng: parseFloat(lng),
      categories: selectedCategories,
    };

    console.log("[디버그] placeId:", placeId);
    console.log("[디버그] isRejected:", isRejected);
    console.log("[디버그] data:", data);

    $.ajax({
      url: `/api/place/${placeId}`,
      method: "PATCH",
      contentType: "application/json",
      data: JSON.stringify(data),
      success: function () {
        if (isRejected) {
          alert("📨 재등록 요청이 접수되었습니다. 관리자 승인을 기다려주세요.");
        } else {
          alert("✅ 장소 정보가 수정되었습니다.");
        }
        window.location.href = "/place/my";
      },
      error: function (xhr) {
        alert("❌ 요청 실패: " + xhr.responseText);
      },
    });
  });
}

// ─────────────────────────────────────────────
// [6] 카테고리 전체 선택 / 해제
// ─────────────────────────────────────────────
function bindCategorySelectAll() {
  $("#selectAllCategoriesBtn").click(function () {
    $("input[name='categories']").prop("checked", true);
  });

  $("#deselectAllCategoriesBtn").click(function () {
    $("input[name='categories']").prop("checked", false);
  });
}
