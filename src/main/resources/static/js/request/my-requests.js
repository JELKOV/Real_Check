let currentPage = 0;
const pageSize = 10;

// 카테고리 라벨 매핑
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
  FOOD_MENU: "🍔 메뉴 정보",
  CROWD_LEVEL: "👥 혼잡도",
  ETC: "기타",
};

// [1] 초기 진입
$(document).ready(function () {
  initMyRequestPage();
});

// [1-1] 초기화 통합 함수
function initMyRequestPage() {
  bindEventListeners();
  initializeCategoryFilter();
  loadRequestList();
}

// [2] 이벤트 리스너 바인딩
function bindEventListeners() {
  $("#filterBtn").click(handleFilterClick);
  $(document).on("click", ".view-detail", handleViewDetailClick);
  $(document).on("click", ".page-link", handlePageClick);
}

// 검색 버튼
function handleFilterClick() {
  currentPage = 0;
  loadRequestList();
}

// 상세보기
function handleViewDetailClick(e) {
  e.preventDefault();
  const id = $(this).data("id");
  location.href = `/request/${id}`;
}

// 페이지네이션 클릭
function handlePageClick(e) {
  e.preventDefault();
  const page = $(this).data("page");
  if (page !== undefined && page !== currentPage) {
    currentPage = page;
    loadRequestList();
  }
}

// [3] 요청 목록 불러오기
function loadRequestList() {
  const category = $("#categoryFilter").val();
  const keyword = $("#searchKeyword").val();

  $.get("/api/request/my", {
    category,
    keyword,
    page: currentPage,
    size: pageSize,
  }).done(function (resp) {
    const { content, totalPages } = resp;

    if (!content || content.length === 0) {
      showEmptyRequestMessage();
      return;
    }

    const html = `
      ${generateRequestTable(content)}
      ${generatePagination(totalPages, currentPage)}
    `;
    $("#requestTableContainer").html(html);
  });
}

// [3-1] 응답 없을때 처리 함수
function showEmptyRequestMessage() {
  $("#requestTableContainer").html(`
    <div class="alert alert-secondary">등록한 요청이 없습니다.</div>
  `);
}

// [4] 요청 테이블 생성
function generateRequestTable(data) {
  let html = `
    <table class="table table-bordered align-middle text-center">
      <thead class="table-light">
        <tr>
          <th>제목</th>
          <th>카테고리</th>
          <th>답변 수</th>
          <th>상태</th>
          <th>포인트</th>
          <th>등록일</th>
          <th>관리</th>
        </tr>
      </thead>
      <tbody>
  `;

  data.forEach((req) => {
    html += generateRequestRow(req);
  });

  html += "</tbody></table>";
  return html;
}

// [5] 개별 행 생성
function generateRequestRow(req) {
  const status = req.closed ? "✅ 마감됨" : "🟢 진행 중";
  const alertText =
    !req.closed && req.answerCount === 3
      ? `<div class='text-info small'>⚠️ 채택 필요</div>`
      : "";
  const categoryLabel = categoryLabelMap[req.category] || req.category;
  const formattedDate = formatDate(req.createdAt);
  const pointDisplay = formatPoint(req);

  return `
    <tr>
      <td>${req.title}</td>
      <td>${categoryLabel}</td>
      <td>${req.visibleAnswerCount}</td>
      <td>${status}${alertText}</td>
      <td>${pointDisplay}</td>
      <td>${formattedDate}</td>
      <td>
        <a href="#" class="btn btn-outline-primary btn-sm view-detail" data-id="${req.id}">상세보기</a>
      </td>
    </tr>
  `;
}

// [5-1] 날짜 보조 함수
function formatDate(dateStr) {
  return new Date(dateStr).toLocaleString();
}

// [5-2] 포인트 보조 함수
function formatPoint(req) {
  let point = `🪙 ${req.point} 포인트`;
  if (!req.closed) {
    point += `<div class="text-secondary small">(예치 중)</div>`;
  } else if (req.refundProcessed) {
    point += `<div class="text-success small">(환불됨)</div>`;
  }
  return point;
}

// [6] 페이지네이션 렌더링
function generatePagination(totalPages, currentPage) {
  if (totalPages <= 1) return "";

  let html = `<nav><ul class="pagination justify-content-center">`;
  for (let i = 0; i < totalPages; i++) {
    html += `
      <li class="page-item ${i === currentPage ? "active" : ""}">
        <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
      </li>
    `;
  }
  html += `</ul></nav>`;
  return html;
}

// [7] 카테고리 필터 옵션 로딩 (라벨 적용)
function initializeCategoryFilter() {
  $.get("/api/request/categories", function (categories) {
    let options = `<option value="">전체 카테고리</option>`;
    categories.forEach((category) => {
      const label = categoryLabelMap[category] || category;
      options += `<option value="${category}">${label}</option>`;
    });
    $("#categoryFilter").html(options);
  });
}
