import { shortenText, formatPointInfo } from "./util/commonUtils.js";
import { drawRadiusCircle } from "./util/mapUtils.js";
import { getCategoryLabel } from "./util/categoryUtils.js";

let map;
let markers = [];
let currentPage = 1;
let isLoading = false;
let hasMore = true;
let searchCenter = null;
let activeInfoWindow = null;

// 초기화
$(document).ready(function () {
  initMap();
  bindUIEvents();
  renderCustomCategoryDropdown();
  renderCustomRadiusDropdown();
});

// 네이버 지도 초기화
function initMap() {
  map = new naver.maps.Map("map", {
    center: new naver.maps.LatLng(37.5665, 126.978),
    zoom: 13,
  });

  // 지도 초기 중심 설정
  searchCenter = map.getCenter();
  loadRequests();

  // 지도 드래그 종료 시 새로운 위치로 요청 로드
  naver.maps.Event.addListener(map, "dragend", function () {
    searchCenter = map.getCenter();
    resetAndLoad();
  });
}

// 이벤트 핸들러 바인딩 함수
function bindUIEvents() {
  // 현재 위치로 지도 이동 버튼
  $("#myLocationButton").on("click", moveToCurrentLocation);

  // "내 주변 요청 새로고침" 버튼
  $("#refreshNearbyButton").on("click", resetAndLoad);

  // 반경 필터 또는 카테고리 변경 시 목록 새로 로드
  $("#radiusFilter, #categoryFilter").on("change", resetAndLoad);

  // 위치 검색 버튼 클릭 시 검색 실행
  $("#searchLocationBtn").on("click", searchLocation);

  // 위치 검색 입력창에서 Enter 키 입력 시 검색 실행
  $("#locationInput").on("keyup", (e) => e.key === "Enter" && searchLocation());

  // 무한 스크롤 이벤트
  // 스크롤이 페이지 하단에 근접하면 다음 페이지 요청 자동 로드
  $("#requestListContainer").on("scroll", function () {
    const $this = $(this);
    const scrollTop = $this.scrollTop();
    const scrollHeight = $this[0].scrollHeight;
    const clientHeight = $this.outerHeight();

    const nearBottom = scrollTop + clientHeight >= scrollHeight - 150;

    // console.log("스크롤 이벤트 확인", {
    //   scrollTop,
    //   scrollHeight,
    //   clientHeight,
    //   nearBottom,
    //   isLoading,
    //   hasMore,
    //   currentPage,
    // });

    if (nearBottom && !isLoading && hasMore) {
      // console.log("무한스크롤 조건 만족 → loadRequests 호출");
      isLoading = true;
      loadRequests();
    }
  });
}

// 위치 정보 가져오기
function moveToCurrentLocation() {
  if (!navigator.geolocation)
    return alert("위치 정보를 지원하지 않는 브라우저입니다.");

  navigator.geolocation.getCurrentPosition((pos) => {
    const latlng = new naver.maps.LatLng(
      pos.coords.latitude,
      pos.coords.longitude
    );
    map.setCenter(latlng);
    drawRadiusCircle(map, latlng, parseInt($("#radiusFilter").val()));
    resetAndLoad();
  });
}

// 지도 드래그 / 검색 / 필터 변경 시 데이터 초기화 + 로드
function resetAndLoad() {
  searchCenter = map.getCenter(); // 항상 최신 좌표로 설정
  console.log("resetAndLoad() - 현재 중심 좌표:", searchCenter);

  currentPage = 1;
  hasMore = true;
  clearMarkers();
  loadRequests();

  $("#endOfListMessage").hide();
}

// 마커 초기화
function clearMarkers() {
  markers.forEach((m) => m.marker.setMap(null));
  markers = [];
  if (activeInfoWindow) activeInfoWindow.close();
}

// 위치 검색
function searchLocation() {
  const query = $("#locationInput").val();
  if (!query) return alert("검색어를 입력하세요.");

  // 서버를 통한 위치 검색 (도로명 주소 검색) - 네이버 검색 API
  $.ajax({
    url: "/api/naver/search",
    type: "GET",
    data: { query },
    success: function (response) {
      const parsed =
        typeof response === "string" ? JSON.parse(response) : response;
      const items = parsed.items;

      if (items.length === 0) {
        alert("검색 결과가 없습니다.");
        return;
      }

      // 검색 결과 목록 모달에 표시
      $("#searchResultList").empty();
      items.forEach((item, index) => {
        $("#searchResultList").append(`
            <li class="list-group-item" data-index="${index}">
              <strong>${item.title}</strong><br>
              <small>${item.roadAddress}</small>
            </li>
          `);
      });

      // 모달 열기
      $("#searchResultModal").modal("show");

      // 모달에서 도로명 주소 선택 시 지도 이동
      $("#searchResultList")
        .off("click")
        .on("click", "li", function () {
          const index = $(this).data("index");
          const selected = items[index];
          const roadAddress = selected.roadAddress;

          if (!roadAddress) {
            alert("도로명 주소가 없습니다.");
            return;
          }

          searchByRoadAddress(roadAddress);
          $("#searchResultModal").modal("hide");
        });
    },
    error: function () {
      alert("검색 API 요청 중 오류가 발생했습니다.");
    },
  });
}

// 도로명 주소로 지도 검색 (geocode 사용)
function searchByRoadAddress(roadAddress) {
  naver.maps.Service.geocode(
    { query: roadAddress },
    function (status, response) {
      if (status !== naver.maps.Service.Status.OK) {
        alert("도로명 주소 검색 실패");
        return;
      }

      const result = response.v2.addresses[0];
      if (!result) {
        alert("해당 도로명 주소로 검색된 좌표가 없습니다.");
        return;
      }

      // 지도 중심 이동
      const { y: lat, x: lng } = result;

      searchCenter = new naver.maps.LatLng(parseFloat(lat), parseFloat(lng));
      map.setCenter(searchCenter);
      resetAndLoad();
    }
  );
}

// 요청 로드 (무한 스크롤 + 지도 이동)
function loadRequests() {
  // console.log("loadRequests() 실행 - currentPage:", currentPage);

  $("#loadingIndicator").show();

  const category = $("#categoryFilter").val() || null;
  const radius = parseInt($("#radiusFilter").val(), 10);
  const centerLat = searchCenter.lat();
  const centerLng = searchCenter.lng();

  drawRadiusCircle(map, searchCenter, radius);

  const pageSize = currentPage === 1 ? 10 : 5;

  $.get(
    "/api/request/open",
    {
      page: currentPage,
      size: pageSize,
      lat: centerLat,
      lng: centerLng,
      radius: radius,
      category: category,
    },
    function (data) {
      // console.log("서버 응답 데이터:", data);

      if (currentPage === 1) {
        clearMarkers();
        $("#requestList").empty();
        $("#endOfListMessage").hide();
      }

      // console.log(
      //   "현재 페이지:",
      //   currentPage,
      //   "받은 개수:",
      //   data.length,
      //   "hasMore:",
      //   hasMore
      // );

      renderRequests(data);
      currentPage++;

      hasMore = data.length === pageSize;

      if (!hasMore) {
        $("#endOfListMessage").show();
      }
      // console.log(`페이지 ${currentPage - 1} 완료. hasMore: ${hasMore}`);
    }
  )
    .fail((err) => {
      // console.error("요청 실패", err);
    })
    .always(() => {
      isLoading = false;
      $("#loadingIndicator").hide();
    });
}

// 요청 목록 렌더링
function renderRequests(data) {
  const container = $("#requestList");

  // console.log("카드 렌더링 시작, 현재 목록 길이:", container.children().length);

  data.forEach(function (req, i) {
    if ($(`[data-request-id="${req.id}"]`).length > 0) {
      // console.log(`중복 요청(${req.id}) 생략`);
      return;
    }

    // console.log(`${i + 1}. 요청 추가 → ${req.title} (ID: ${req.id})`);

    // 카드 HTML 생성 후 추가
    const cardHtml = generateRequestCard(req);
    container.append(cardHtml);

    // 지도 마커 생성
    if (req.lat && req.lng) {
      const markerInfo = createMarkerWithInfo(req);
      markers.push(markerInfo);
    }
  });

  // console.log("렌더링 완료, 총 카드 수:", container.children().length);

  // 카드 클릭 시 마커 이동
  $(".request-card")
    .off("click")
    .on("click", function () {
      const requestId = $(this).data("request-id");
      const markerInfo = markers.find((m) => m.id === requestId);
      if (markerInfo) {
        map.panTo(markerInfo.marker.getPosition());
        if (activeInfoWindow) activeInfoWindow.close();
        markerInfo.infoWindow.open(map, markerInfo.marker);
        activeInfoWindow = markerInfo.infoWindow;
      }
    });
}

// 요청 카드 HTML 생성 함수
function generateRequestCard(req) {
  // 카테고리 뱃지 HTML
  const badge = req.category
    ? `<span class="badge bg-secondary mb-2">${getCategoryLabel(
        req.category
      )}</span>`
    : "";

  // 요청 정보 카드 템플릿
  return `
    <div class="col-md-6" data-request-id="${req.id}">
      <div class="card h-100 shadow-sm request-card" data-request-id="${
        req.id
      }">
        <div class="card-body">
          ${badge}
          <h5 class="card-title">${req.title}</h5>
          <p class="card-text">${shortenText(req.content)}</p>
          ${formatPointInfo(req.point)}<br/>
          <a href="/request/${
            req.id
          }" class="btn btn-outline-primary btn-sm mt-2 view-detail">상세보기</a>
        </div>
      </div>
    </div>`;
}

// 요청에 대한 마커 + 정보창 생성 함수
function createMarkerWithInfo(req) {
  const position = new naver.maps.LatLng(req.lat, req.lng);
  // 마커 생성
  const marker = new naver.maps.Marker({ map, position });
  // 마커 클릭 시 정보창
  const infoWindow = new naver.maps.InfoWindow({
    content: `<div style="padding: 10px; max-width: 200px;"><strong>${
      req.title
    }</strong><br/>${req.content}<br/><small>${getCategoryLabel(
      req.category
    )}</small></div>`,
    maxWidth: 200,
  });

  // 마커 클릭 핸들러
  naver.maps.Event.addListener(marker, "click", () => {
    if (activeInfoWindow) activeInfoWindow.close();
    infoWindow.open(map, marker);
    activeInfoWindow = infoWindow;
    map.panTo(marker.getPosition());
  });

  // markers 배열에 저장할 객체 반환
  return { id: req.id, marker, infoWindow };
}

// 카테고리 드랍다운 이벤트 바인딩
function renderCustomCategoryDropdown() {
  const $dropdownList = $("#dropdownList");
  const $dropdownLabel = $("#dropdownLabel");
  const $categorySelect = $("#categoryFilter");

  // 1. 기존 select의 옵션을 기반으로 커스텀 UI 구성
  $dropdownList.empty();
  $categorySelect.find("option").each(function () {
    const value = $(this).val();
    const text = $(this).text();
    $dropdownList.append(
      `<li class="dropdown-item" data-value="${value}">${text}</li>`
    );
  });

  // 2. 토글 버튼 → 드롭다운 열기/닫기
  $("#dropdownToggle").on("click", function () {
    $dropdownList.toggleClass("show");
  });

  // 3. 외부 클릭 시 드롭다운 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest("#customCategoryDropdown").length) {
      $dropdownList.removeClass("show");
    }
  });

  // 4. 항목 선택 시 → 실제 select 변경 + change 이벤트 발생
  $dropdownList.on("click", ".dropdown-item", function () {
    const value = $(this).data("value");
    const label = $(this).text();

    $dropdownLabel.text(label);
    $categorySelect.val(value).trigger("change");
    $dropdownList.removeClass("show");
  });
}

// 원 반경 드랍다운 이벤트 바인딩
function renderCustomRadiusDropdown() {
  const $dropdownList = $("#radiusDropdownList");
  const $dropdownLabel = $("#radiusDropdownLabel");
  const $radiusSelect = $("#radiusFilter");

  // 1. 기존 select의 옵션을 기반으로 커스텀 UI 구성
  $dropdownList.empty();
  $radiusSelect.find("option").each(function () {
    const value = $(this).val();
    const text = $(this).text();
    $dropdownList.append(
      `<li class="dropdown-item" data-value="${value}">${text}</li>`
    );
  });

  // 2. 토글 버튼 → 드롭다운 열기/닫기
  $("#radiusDropdownToggle").on("click", function () {
    $dropdownList.toggleClass("show");
  });

  // 3. 외부 클릭 시 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest("#customRadiusDropdown").length) {
      $dropdownList.removeClass("show");
    }
  });

  // 4. 항목 선택 시 반영
  $dropdownList.on("click", ".dropdown-item", function () {
    const value = $(this).data("value");
    const label = $(this).text();

    $radiusSelect.val(value).trigger("change");
    $dropdownLabel.text(label);
    $dropdownList.removeClass("show");
  });
}
