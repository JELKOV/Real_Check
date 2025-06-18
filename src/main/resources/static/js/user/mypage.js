// 현재 포인트 페이지 번호 (0부터 시작)
let currentPointPage = 0;
// 한 페이지당 항목 수
const pageSize = 10;

// ─────────────────────────────────────
// [1] 초기 로딩 및 탭 이벤트 바인딩
// ─────────────────────────────────────
$(document).ready(function () {
  loadPointPage(0); // 페이지 로드 시 0페이지 포인트 내역 불러오기

  // 마이페이지 탭 변경 시 포인트 탭이면 데이터 재로딩
  $('#mypageTab button[data-bs-toggle="tab"]').on("shown.bs.tab", function (e) {
    const targetId = $(e.target).attr("data-bs-target");
    if (targetId === "#pointSection") {
      loadPointPage(currentPointPage); // 현재 페이지 기준으로 다시 불러오기
    }
  });
});

// ─────────────────────────────────────
// [2] 포인트 내역 불러오기
// ─────────────────────────────────────
function loadPointPage(page) {
  $.get(`/api/point/my?page=${page}&size=${pageSize}`, function (resp) {
    const { content, totalPages, number } = resp;

    // 포인트 내역이 없을 경우 안내 메시지 표시
    if (!content || content.length === 0) {
      $("#pointList").html(
        `<div class="text-muted">포인트 내역이 없습니다.</div>`
      );
      $("#pointPagination").empty();
      return;
    }

    // 각 포인트 항목 렌더링
    const html = content
      .map((p) => {
        const amountColor = p.amount > 0 ? "text-success" : "text-danger"; // 색상 구분
        const amountText = p.amount > 0 ? `+${p.amount}` : `${p.amount}`; // + 또는 -
        return `
        <div class="point-entry border-bottom py-1">
          <div><strong>${p.reason}</strong></div>
          <div class="text-muted small">
            ${p.type} | <span class="${amountColor} fw-bold">${amountText}pt</span> | ${p.earnedAt}
          </div>
        </div>`;
      })
      .join("");

    $("#pointList").html(html); // 리스트 영역에 HTML 삽입
    renderPagination(totalPages, number); // 페이지네이션 렌더링
    currentPointPage = number; // 현재 페이지 업데이트
  });
}

// ─────────────────────────────────────
// [3] 페이지네이션 렌더링
// ─────────────────────────────────────
function renderPagination(totalPages, currentPage) {
  let pagingHtml = "";

  // 전체 페이지 수만큼 버튼 생성
  for (let i = 0; i < totalPages; i++) {
    const active = i === currentPage ? "btn-primary" : "btn-outline-primary";
    pagingHtml += `<button class="btn btn-sm ${active}" onclick="loadPointPage(${i})">${
      i + 1
    }</button> `;
  }

  $("#pointPagination").html(pagingHtml); // 페이지네이션 영역에 버튼 삽입
}
