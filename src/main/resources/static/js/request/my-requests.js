$(document).ready(function () {
  initializeRequestList();
  bindEventListeners();
});

// [1] 초기화 함수
function initializeRequestList() {
  loadRequestList();
}

// [2] 이벤트 리스너 바인딩
function bindEventListeners() {
  //   // 카테고리 필터링
  //   $("#categoryFilter").on("change", function () {
  //     const selectedCategory = $(this).val();
  //     loadRequestList(selectedCategory);
  //   });

  // 상세보기 버튼 클릭
  $(document).on("click", ".view-detail", function (e) {
    e.preventDefault();
    const id = $(this).data("id");
    window.location.href = `/request/${id}`;
  });
}

// [3] 요청 목록 로드 함수 (카테고리 필터 적용)
function loadRequestList() {
  $.get("/api/request/my", function (data) {
    if (data.length === 0) {
      $("#requestTableContainer").html(`
        <div class="alert alert-secondary">
          등록한 요청이 없습니다.
        </div>
      `);
      return;
    }

    const html = generateRequestTable(data);
    $("#requestTableContainer").html(html);
  });
}

// [4] 요청 테이블 생성 함수 (동적 HTML 생성)
function generateRequestTable(data) {
  let html = `
    <table class="table table-bordered align-middle text-center">
      <thead class="table-light">
        <tr>
          <th>제목</th>
          <th>내용</th>
          <th>답변 수</th>
          <th>상태</th>
          <th>포인트</th>
          <th>등록일</th>
          <th>관리</th>
        </tr>
      </thead>
      <tbody>
  `;

  data.forEach(function (req) {
    html += generateRequestRow(req);
  });

  html += `</tbody></table>`;
  return html;
}

// [5] 개별 요청 행 생성 함수 (동적 HTML)
function generateRequestRow(req) {
  const status = req.closed ? "✅ 마감됨" : "🟢 진행 중";
  const isAlertNeeded = !req.closed && req.answerCount === 3;
  const alertText = isAlertNeeded
    ? `<div class='text-info small'>⚠️ 채택 필요</div>`
    : "";
  const formattedDate = new Date(req.createdAt).toLocaleString();
  const pointDisplay = req.closed
    ? `🪙 ${req.point} 포인트`
    : `🪙 ${req.point} 포인트 <div class="text-secondary small">(예치 중)</div>`;

  return `
    <tr>
      <td>${req.title}</td>
      <td>${req.content}</td>
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

// // [7] 카테고리 필터 초기화 함수
// function initializeCategoryFilter() {
//   $.get("/api/request/categories", function (categories) {
//     let options = `<option value="">전체</option>`;
//     categories.forEach((category) => {
//       options += `<option value="${category}">${category}</option>`;
//     });
//     $("#categoryFilter").html(options);
//   });
// }
