import { getCategoryLabel } from "./util/categoryUtils.js";

import { formatDateTime } from "./util/dateUtils.js";

// ─────────────────────────────────────────────
// [1] 준비 시 실행되는 초기 함수들
// ─────────────────────────────────────────────
$(document).ready(function () {
  // 장소 지도 렌더링
  initMap();
  // 등록일 텍스트 치환
  formatCreatedAt();
  // 카테고리 설명 텍스트 삽입
  renderCategorySummaries();
  // 카테고리 뱃지 텍스트 처리
  renderCategoryBadges();

  // 이미지 클릭 시 확대 모달 띄우기
  $(document).on("click", ".log-image", function () {
    const src = $(this).data("img");
    $("#modalImage").attr("src", src);
  });

  // community.js
  $(document).on("click", ".delete-log-btn", function () {
    const logId = $(this).data("log-id");
    const placeId = $(this).data("place-id");
    confirmDelete(logId, placeId);
  });
});

// ─────────────────────────────────────────────
// [2] 장소 지도 초기화
// ─────────────────────────────────────────────
function initMap() {
  const lat = parseFloat($("#placeLat").val());
  const lng = parseFloat($("#placeLng").val());
  const placeName = $("#placeName").val();
  const latestLogContent = $("#latestLogContent").val();
  const latestLogTime = $("#latestLogTime").val();

  if (!isNaN(lat) && !isNaN(lng) && typeof naver !== "undefined") {
    const map = new naver.maps.Map("placeMap", {
      center: new naver.maps.LatLng(lat, lng),
      zoom: 16,
    });

    const marker = new naver.maps.Marker({
      position: new naver.maps.LatLng(lat, lng),
      map: map,
    });

    const infoHtml = `
      <div style="padding:10px; max-width:250px; font-size:14px; line-height:1.5;">
        <div><strong>📍 ${placeName}</strong></div>
        ${
          latestLogContent
            ? `<div style="margin-top:4px;">🪑 ${latestLogContent}</div>
               <div style="color:gray; font-size:12px; margin-top:2px;">
               🕒 ${formatDateTime(latestLogTime)}</div>`
            : `<div style="margin-top:4px; color:gray;">등록된 최신 공지가 없습니다.</div>`
        }
      </div>
    `;

    new naver.maps.InfoWindow({
      content: infoHtml,
    }).open(map, marker);
  }
}
// ─────────────────────────────────────────────
// [3] 날짜 표기 처리 (data-created-at)
// ─────────────────────────────────────────────
function formatCreatedAt() {
  document.querySelectorAll("[data-created-at]").forEach((el) => {
    const raw = el.getAttribute("data-created-at");
    if (raw) el.textContent = formatDateTime(raw);
  });
}

// ─────────────────────────────────────────────
// [4] 카테고리 뱃지 텍스트 렌더링 (응답)
// - .category-badge 요소들의 텍스트를 치환
// ─────────────────────────────────────────────
function renderCategoryBadges() {
  document.querySelectorAll(".category-badge").forEach((el) => {
    const cat = el.getAttribute("data-category");
    if (cat) el.textContent = getCategoryLabel(cat);
  });
}

// ─────────────────────────────────────────────
// [5] 카테고리별 텍스트 렌더링
// - .category-summary 요소 대상
// ─────────────────────────────────────────────
function renderCategorySummaries() {
  document.querySelectorAll(".category-summary").forEach((el) => {
    renderCategorySummaryText(el);
  });
}

// 텍스트 생성 로직 (개별 항목 처리)
function renderCategorySummaryText(el) {
  const category = el.dataset.category;

  switch (category) {
    case "WAITING_STATUS":
      el.textContent = `현재 대기 인원: ${el.dataset.waitCount || "-"}`;
      break;
    case "FOOD_MENU":
      el.textContent = `오늘의 메뉴: ${el.dataset.menuInfo || "정보 없음"}`;
      break;
    case "BATHROOM":
      el.textContent = `화장실 있음 여부: ${
        el.dataset.hasBathroom === "true" ? "있음" : "없음"
      }`;
      break;
    case "PARKING":
      el.textContent = `주차 가능 여부: ${
        el.dataset.isParkingAvailable === "true" ? "가능" : "불가능"
      }`;
      break;
    case "NOISE_LEVEL":
      el.textContent = `소음 상태: ${el.dataset.noiseNote || "정보 없음"}`;
      break;
    case "CROWD_LEVEL":
      el.textContent = `혼잡도: ${el.dataset.crowdLevel || "-"} / 10`;
      break;
    case "WEATHER_LOCAL":
      el.textContent = `날씨 상태: ${el.dataset.weatherNote || "정보 없음"}`;
      break;
    case "STREET_VENDOR":
      el.textContent = `노점 이름: ${el.dataset.vendorName || "정보 없음"}`;
      break;
    case "PHOTO_REQUEST":
      el.textContent = `사진 메모: ${el.dataset.photoNote || "없음"}`;
      break;
    case "BUSINESS_STATUS":
      el.textContent = `영업 여부: ${
        el.dataset.isOpen === "true" ? "영업 중" : "영업 안 함"
      }`;
      break;
    case "OPEN_SEAT":
      el.textContent = `남은 좌석 수: ${el.dataset.seatCount || "-"}석`;
      break;
    case "ETC":
      el.textContent = `기타 정보: ${el.dataset.extra || "없음"}`;
      break;
    default:
      el.textContent = "";
  }
}

// ─────────────────────────────────────────────
// [6] 공지 삭제 API 호출
// ─────────────────────────────────────────────
function confirmDelete(logId, placeId) {
  if (!confirm("정말 삭제하시겠습니까?")) return;

  $.ajax({
    url: `/api/status/${logId}`,
    method: "DELETE",
    success: function () {
      alert("삭제가 완료되었습니다.");
      location.href = "/place/community/" + placeId;
    },
    error: function (xhr) {
      alert("삭제 실패: " + xhr.responseText);
    },
  });
}
