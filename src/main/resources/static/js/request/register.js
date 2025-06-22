// 지도 관련
import {
  initializeMap,
  setMainMarker,
  setCustomPlace,
  resetSelectedPlace,
} from "./util/mapUtils.js";

// 카테고리 & 플레이스 홀더 관련
import {
  applyPlaceholdersForCategory,
  updateCategoryDropdown,
  resetCategoryDropdown,
  resetPlaceholders,
} from "./util/categoryUtils.js";

// 키보드 조작 관련
import {
  bindKeyboardEvents,
  updateSelection,
  setCurrentFocus,
  isKeyNavigating,
  setSelectPlaceFn,
} from "./util/keyboardUtils.js";

// Debounce 함수 (입력 지연 제어)
import { debounce } from "../util/common.js";

// 전역변수 설정 - 공식장소 / 사용자장소
let isCustomMode = false;

// [1] 페이지 초기화 코드
$(document).ready(function () {
  initRequestPage();
  setSelectPlaceFn(selectPlace);
});

// [2] 페이지 초기화 관련 함수
function initRequestPage() {
  /**
   * 외부 바인딩
   */
  initializeMap();
  /**
   * 내부 바인딩
   */
  bindEventListeners();
  initializePage();
}

// [3] 이벤트 리스너 바인딩
function bindEventListeners() {
  /**
   * 외부 바인딩
   */
  // 키보드 이동 이벤트
  bindKeyboardEvents();

  /**
   * 내부 바인딩
   */
  // 장소 검색 이벤트
  bindSearchEvents();
  // 사용자 지정 장소 이벤트
  bindCustomPlaceEvents();
  // 카테고리 및 요청 등록 이벤트
  bindFormEvents();
  // 토글 버튼 이벤트 (검색/사용자 지정)
  bindToggleEvents();
  // 검색 결과 닫기 이벤트
  bindCloseEvents();
}

// [4] 페이지 초기화 (카테고리 및 기본 설정)
function initializePage() {
  initializeAccordionInstances();
  resetCategoryDropdown();
  resetPlaceholders();
  resetSelectedPlace();
}

// 아코디언 상태 함수 초기화
function initializeAccordionInstances() {
  const allSteps = ["step1", "step2", "step3"];
  allSteps.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;

    const instance = bootstrap.Collapse.getOrCreateInstance(el, {
      toggle: false,
    });

    // step1만 열고 나머지는 닫는다
    if (id === "step1") {
      instance.show(); // 첫 번째만 열림
    } else {
      instance.hide(); // 나머지는 무조건 닫힘 (기억됨)
    }
  });
}

// [5] 바인딩 함수화

// [5-1] 장소 검색 이벤트
function bindSearchEvents() {
  $("#placeSearch").on("input", debounce(handlePlaceSearch, 300));
  $("#placeSearchResults").on("mousedown", ".place-item", handlePlaceSelect);
  $("#placeSearchResults").on("mouseenter", ".place-item", highlightItem);
}

// [5-1-1] 장소 검색 (AJAX)
// 검색창에 입력된 문자열을 기준으로 서버에 장소 검색 요청을 보냄
function handlePlaceSearch() {
  // 현재 ↓ 키 탐색 중일 경우 (리스트 포커스 이동 중일 경우)

  // 검색 요청을 차단하여 리스트가 덮어써지는 현상을 방지
  if (isKeyNavigating()) return;

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

// [5-1-1-1] 검색 결과 렌더링
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
    setCurrentFocus(-1);
  } else {
    updateSelection($(".place-item"));
  }
}

// [5-1-1-1-1] 장소 관련 폼 만들기
function createPlaceItemHtml(place) {
  return `<li class="list-group-item place-item" 
            data-id="${place.id}" 
            data-lat="${place.lat}" 
            data-lng="${place.lng}" 
            data-name="${place.name}">
            ${place.name}
          </li>`;
}

// [5-1-2] 마우스 탐색 (클릭)
function handlePlaceSelect(e) {
  e.preventDefault();
  selectPlace($(e.target));
  bindCloseEvents();
}

// [5-1-3] 마우스 탐색 (호버)
function highlightItem() {
  $(".place-item").removeClass("selected");
  $(this).addClass("selected");
  setCurrentFocus($(".place-item").index($(this)));
}

// [5-2] 사용자 지정 장소 검색 이벤트
function bindCustomPlaceEvents() {
  // 사용자 지정 장소 주소 검색
  $("#searchAddressBtn").click(handleCustomAddressSearch);
  // 사용자 지정 주소 Enter 키 검색
  $("#customAddress").on("keydown", handleEnterKeyForCustomAddress);
}

// [5-2-1] 사용자 지정 주소 검색 (Naver Geocoder API 사용)
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

// [5-3-2] 사용자 지정 주소 엔터 키 검색 (함수화)
function handleEnterKeyForCustomAddress(e) {
  if (e.key === "Enter" && $("#customAddress").is(":focus")) {
    e.preventDefault(); // 기본 엔터 동작 막기 (폼 제출 방지)
    handleCustomAddressSearch(); // 검색 함수 실행
  }
}

// [5-4] 폼 이벤트
function bindFormEvents() {
  $("#category").on("change", updatePlaceholders);
  $("#requestForm").on("submit", submitRequest);
}

// [5-4-1] Placeholder 초기화 및 자동 설정
function updatePlaceholders() {
  applyPlaceholdersForCategory($("#category").val());
}

// [5-4-2] 요청 등록 (AJAX)
function submitRequest(e) {
  e.preventDefault();

  const requestData = {
    title: $("#title").val(),
    content: $("#content").val(),
    point: parseInt($("#point").val()),
    category: $("#category").val(),
    placeId: $("#placeId").val() || null,
    lat: $("#lat").val() ? parseFloat($("#lat").val()) : null,
    lng: $("#lng").val() ? parseFloat($("#lng").val()) : null,
    customPlaceName: isCustomMode ? $("#selectedPlaceName").val() : null,
  };

  // STEP1: 장소 정보 검증
  if (isCustomMode) {
    if (!requestData.lat || !requestData.lng || !requestData.customPlaceName) {
      alert("STEP1: 사용자 지정 장소 정보를 입력해주세요.");
      focusOnStep("step1");
      return;
    }
  } else {
    if (!requestData.placeId) {
      alert("STEP1: 공식 장소를 선택해주세요.");
      focusOnStep("step1");
      return;
    }
  }

  if (!requestData.title || !requestData.content || !requestData.category) {
    alert("STEP2: 요청 내용을 입력해주세요.");
    focusOnStep("step2");
    return;
  }
  if (!requestData.point || requestData.point < 10) {
    alert("STEP3: 요청은 최소 10포인트 이상이어야 합니다.");
    focusOnStep("step3");
    return;
  }

  $.ajax({
    url: "/api/request",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(requestData),
    success: () => {
      alert("요청이 등록되었습니다!");
      location.href = "/my-requests";
    },
    error: (xhr) => {
      alert("등록 실패: " + xhr.responseText);
    },
  });
}

// [5-4-2-1] 요청정보 유효성 검사 사용자 UI 개선 기능
function focusOnStep(stepId) {
  const allSteps = ["step1", "step2", "step3"];

  allSteps.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;

    const instance = bootstrap.Collapse.getInstance(el); // 이미 인스턴스가 있음
    if (id === stepId) {
      instance?.show();
    } else {
      instance?.hide();
    }
  });

  // 포커스 이동
  const targetInput = document.querySelector(
    `#${stepId} input, #${stepId} textarea, #${stepId} select`
  );
  if (targetInput) {
    targetInput.focus({ preventScroll: true });
  }
}

// [5-5] 토글 이벤트
function bindToggleEvents() {
  $("#btnPlace").click(() => togglePlaceMode("search"));
  $("#btnCustom").click(() => togglePlaceMode("custom"));
}

// [5-5-1] 토글 버튼 (검색/사용자 지정)
function togglePlaceMode(mode) {
  isCustomMode = mode === "custom";
  const isCustom = isCustomMode;

  $("#customPlaceSection").toggle(isCustom);
  $("#searchSection").toggle(!isCustom);

  resetSelectedPlace(isCustom);
  resetCategoryDropdown();

  const [activeBtn, inactiveBtn] = isCustom
    ? ["#btnCustom", "#btnPlace"]
    : ["#btnPlace", "#btnCustom"];

  toggleButtonStyles(activeBtn, inactiveBtn);
}

// [5-5-2] 버튼 스타일 전환 함수
function toggleButtonStyles(active, inactive) {
  $(active).addClass("btn-primary").removeClass("btn-outline-primary");
  $(inactive).addClass("btn-outline-primary").removeClass("btn-primary");
}

// [5-6] 외부 클릭으로 검색 결과 닫기 이벤트
function bindCloseEvents() {
  $(document).click(handleDocumentClick);
}

// [5-6-1] 외부 클릭으로 검색 결과 닫기 관련 함수
function handleDocumentClick(e) {
  if (!$(e.target).closest("#placeSearch, #placeSearchResults").length) {
    $("#placeSearchResults").hide();
  }
}

// ─────────────────────────────────────
// 공통 유틸 함수
// ─────────────────────────────────────

// [1] 장소 선택 함수
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
  $("#selectedPlaceName").val(placeName).show().addClass("selected");
  $("#selectedMarker").show(); // 마킹 활성화
  setMainMarker(lat, lng, placeId);

  $("#placeSearch").val(placeName);
  $("#lat").val(lat);
  $("#lng").val(lng);
  $("#placeId").val(placeId || "");

  if (placeId) {
    loadPlaceDetails(placeId);
  } else {
    $("#infoSection").hide();
    resetCategoryDropdown();
  }
}

// [2-1] 장소 세부 정보 로드
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
