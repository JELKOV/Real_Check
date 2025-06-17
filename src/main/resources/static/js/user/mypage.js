let currentPointPage = 0;
const pageSize = 10;

$(document).ready(function () {
  loadPointPage(0); // 초기 로딩

  // 탭 전환
  $('#mypageTab button[data-bs-toggle="tab"]').on("shown.bs.tab", function (e) {
    const targetId = $(e.target).attr("data-bs-target");
    if (targetId === "#pointSection") {
      loadPointPage(currentPointPage);
    }
  });
});

function loadPointPage(page) {
  $.get(`/api/point/my?page=${page}&size=${pageSize}`, function (resp) {
    const { content, totalPages, number } = resp;

    if (!content || content.length === 0) {
      $("#pointList").html(
        `<div class="text-muted">포인트 내역이 없습니다.</div>`
      );
      $("#pointPagination").empty();
      return;
    }

    const html = content
      .map((p, idx) => {
        const amountColor = p.amount > 0 ? "text-success" : "text-danger";
        const amountText = p.amount > 0 ? `+${p.amount}` : `${p.amount}`;
        return `
        <div class="point-entry border-bottom py-1">
          <div><strong>${p.reason}</strong></div>
          <div class="text-muted small">
            ${p.type} | <span class="${amountColor} fw-bold">${amountText}pt</span> | ${p.earnedAt}
          </div>
        </div>`;
      })
      .join("");

    $("#pointList").html(html);
    renderPagination(totalPages, number);
    currentPointPage = number;
  });
}

function renderPagination(totalPages, currentPage) {
  let pagingHtml = "";
  for (let i = 0; i < totalPages; i++) {
    const active = i === currentPage ? "btn-primary" : "btn-outline-primary";
    pagingHtml += `<button class="btn btn-sm ${active}" onclick="loadPointPage(${i})">${
      i + 1
    }</button> `;
  }
  $("#pointPagination").html(pagingHtml);
}
