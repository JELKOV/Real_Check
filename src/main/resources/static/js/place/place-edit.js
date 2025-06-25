import { getCategoryLabel } from "./util/categoryUtils.js";
import { bindSearchAddress, searchAddress } from "./util/addressUtils.js";
import {
  bindCategorySelectAll,
  getSelectedCategoryValues,
} from "./util/formUtils.js";
import { initMapWithClickHandler } from "./util/mapUtils.js";

// ─────────────────────────────────────────────
// [1] 문서 초기화
// ─────────────────────────────────────────────
$(document).ready(function () {
  initMap();

  // 버튼 클릭 → 수동으로 직접 처리
  $("#searchAddressBtn").click(() => {
    const query = $("#addressInput").val();
    if (!query) return alert("주소를 입력하세요.");
    searchAddress(query, handleAddressResult);
  });

  // 엔터 입력 → 유틸 사용
  bindSearchAddress("#addressInput", handleAddressResult);

  renderCategoryLabels();
  bindFormSubmit();
  bindCategorySelectAll(
    "#selectAllCategoriesBtn",
    "#deselectAllCategoriesBtn",
    "input[name='categories']"
  );
});

// ─────────────────────────────────────────────
// [2] 지도 로딩 + 클릭 이벤트
// ─────────────────────────────────────────────
let map, marker;

function initMap() {
  const lat = parseFloat($("#lat").val()) || 37.5665;
  const lng = parseFloat($("#lng").val()) || 126.978;

  const result = initMapWithClickHandler("map", lat, lng);
  map = result.map;
  marker = result.marker;
}

// ─────────────────────────────────────────────
// [3] 카테고리 라벨 렌더링
// ─────────────────────────────────────────────
function renderCategoryLabels() {
  $(".category-label").each(function () {
    const code = $(this).data("category");
    $(this).text(getCategoryLabel(code));
  });
}

// ─────────────────────────────────────────────
// [4] 주소 검색
// ─────────────────────────────────────────────
function handleAddressResult(item) {
  const latlng = new naver.maps.LatLng(item.y, item.x);
  map.setCenter(latlng);
  marker.setPosition(latlng);

  $("#address").val(item.roadAddress || item.jibunAddress);
  $("#lat").val(item.y);
  $("#lng").val(item.x);
}

// [5] 수정 or 재등록 요청 제출
function bindFormSubmit() {
  $("#placeForm").on("submit", function (e) {
    e.preventDefault();

    // JSP에서 선언된 전역 변수 사용 (placeId, isRejected)
    const name = $("input[name='name']").val();
    const address = $("input[name='address']").val();
    const lat = $("#lat").val();
    const lng = $("#lng").val();
    const selectedCategories = getSelectedCategoryValues(
      "input[name='categories']"
    );

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

    // console.log("[디버그] placeId:", placeId);
    // console.log("[디버그] isRejected:", isRejected);
    // console.log("[디버그] data:", data);

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
