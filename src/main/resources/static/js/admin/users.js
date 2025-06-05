$(document).ready(function () {
  // 페이지 로딩 시 차단된 사용자 목록 먼저 불러옴
  loadBlockedUsers();

  // [1] 검색 버튼 클릭 시 검색 수행
  $("#searchBtn").on("click", function () {
    const keyword = $("#searchInput").val().trim();

    if (keyword.length > 0) {
      // 검색 키워드가 있을 경우 → 서버에 검색 요청
      $.get(`/api/admin/users/search`, { keyword }, function (users) {
        renderUsers(users); // 검색 결과 렌더링
      });
    } else {
      // 입력값이 없으면 차단 사용자 목록 다시 불러오기
      loadBlockedUsers();
    }
  });

  // Enter 키로 검색 가능하도록 이벤트 바인딩
  $("#searchInput").on("keydown", function (e) {
    if (e.key === "Enter") {
      $("#searchBtn").click(); // 기존 클릭 핸들러 재사용
    }
  });

  // [2] 동적으로 렌더링된 사용자 상세 버튼 클릭 처리
  $(document).on("click", ".detail-btn", function () {
    const userId = $(this).data("id");
    showUserDetail(userId); // 모달 열기
  });

  // 차단
  $(document).on("click", ".block-btn", function () {
    const userId = $(this).data("id");
    blockUser(userId);
  });

  // 해제
  $(document).on("click", ".unblock-btn", function () {
    const userId = $(this).data("id");
    unblockUser(userId);
  });

  $(document).on("click", ".toggle-log-btn", function () {
    const userId = $(this).data("id");
    const type = $(this).data("type");
    const $targetDiv = $(`#userLogsArea-${type}`);

    // 다른 영역은 닫기
    $(".log-section").not($targetDiv).addClass("d-none");

    // 같은 버튼 누르면 닫기
    if (!$targetDiv.hasClass("d-none")) {
      $targetDiv.addClass("d-none");
      return;
    }

    // 열기 + AJAX 요청
    $targetDiv.removeClass("d-none");
    loadUserLogs(userId, 0, type);
  });
});

// [A] 차단된 사용자 목록 불러오기
function loadBlockedUsers() {
  $.get("/api/admin/users/blocked", function (users) {
    renderUsers(users); // 목록 렌더링
  });
}

// [B] 사용자 목록 테이블 렌더링 함수
function renderUsers(users) {
  const $tbody = $("#userTableBody");
  $tbody.empty(); // 테이블 비우기

  console.log("렌더링 대상 users", users);

  if (!Array.isArray(users) || users.length === 0) {
    // 데이터가 없을 경우
    $tbody.append(`<tr><td colspan="5">사용자가 없습니다.</td></tr>`);
    return;
  }

  // 사용자 하나씩 테이블 행으로 생성
  users.forEach((user) => {
    const isActive = String(user.active) === "true" || user.active === true;
    const statusText = isActive ? "정상" : "차단됨";

    // 상태에 따라 버튼 구분
    const actionBtn = isActive
      ? `<button class="btn btn-sm btn-danger block-btn" data-id="${user.id}">차단</button>`
      : `<button class="btn btn-sm btn-success unblock-btn" data-id="${user.id}">해제</button>`;

    // 상세 보기 버튼: 항상 표시
    const detailBtn = `<button class="btn btn-sm btn-info detail-btn" data-id="${user.id}">상세보기</button>`;

    // 테이블 행 HTML 구성
    const row = `
        <tr>
          <td>${user.id}</td>
          <td>${user.email}</td>
          <td>${user.nickname}</td>
          <td>${statusText}</td>
          <td>${detailBtn}</td>
          <td>${actionBtn}</td>
        </tr>
      `;
    $tbody.append(row); // 테이블에 추가
  });
}

// [C] 사용자 상세 정보 표시 (모달 열기)
function showUserDetail(userId) {
  $.get(`/api/admin/users/${userId}`, function (data) {
    const u = data.user;

    const userInfoHtml = `
      <ul class="list-group text-start mb-3">
        <li class="list-group-item"><strong>ID:</strong> ${u.id}</li>
        <li class="list-group-item"><strong>이메일:</strong> ${u.email}</li>
        <li class="list-group-item"><strong>닉네임:</strong> ${u.nickname}</li>
        <li class="list-group-item"><strong>상태:</strong> ${
          u.active ? "정상" : "차단됨"
        }</li>
        <li class="list-group-item"><strong>가입일:</strong> ${formatDate(
          u.createdAt
        )}</li>
        <li class="list-group-item"><strong>포인트:</strong> ${u.points}</li>
        <li class="list-group-item"><strong>신고 횟수:</strong> ${
          u.reportCount
        }</li>
        <li class="list-group-item"><strong>등록 로그 수:</strong> ${
          data.totalLogs
        }</li>
      </ul>

      <div class="btn-group w-100 mb-2" role="group">
        <button class="btn btn-outline-primary w-33 toggle-log-btn" data-id="${
          u.id
        }" data-type="status">답변 내역</button>
        <button class="btn btn-outline-secondary w-33 toggle-log-btn" data-id="${
          u.id
        }" data-type="request">요청 내역</button>
        <button class="btn btn-outline-danger w-33 toggle-log-btn" data-id="${
          u.id
        }" data-type="report">신고 내역</button>
      </div>

      <div id="userLogsArea-status" class="log-section d-none"></div>
      <div id="userLogsArea-request" class="log-section d-none"></div>
      <div id="userLogsArea-report" class="log-section d-none"></div>
    `;

    $("#userDetailModalBody").html(userInfoHtml);
    $("#userDetailModal").modal("show");
  });
}

// [D] 날짜 포맷 함수
function formatDate(isoString) {
  if (!isoString) return "정보 없음";
  const d = new Date(isoString);
  return d.toLocaleString("ko-KR"); // 한국식 날짜로 변환
}

// [E] 사용자 차단 해제 처리
function unblockUser(userId) {
  if (!confirm("정말 차단 해제하시겠습니까?")) return;

  $.ajax({
    url: `/api/admin/users/${userId}/unblock`,
    type: "PATCH",
    success: function () {
      alert("차단 해제 완료!");
      loadBlockedUsers(); // 목록 새로고침
    },
    error: function () {
      alert("차단 해제 실패!");
    },
  });
}

// [F] 사용자 차단 처리
function blockUser(userId) {
  if (!confirm("정말 이 사용자를 차단하시겠습니까?")) return;

  $.ajax({
    url: `/api/admin/users/${userId}/block`,
    type: "PATCH",
    success: function () {
      alert("사용자 차단 완료!");
      loadBlockedUsers(); // 목록 새로고침
    },
    error: function () {
      alert("차단 실패!");
    },
  });
}

// [G] 사용자 활동 로그 불러오기
function loadUserLogs(userId, page = 0, type = "status") {
  let url;
  if (type === "status") {
    url = `/api/admin/users/${userId}/logs/status?page=${page}&size=5`;
  } else if (type === "request") {
    url = `/api/admin/users/${userId}/logs/requests?page=${page}&size=5`;
  } else if (type === "report") {
    url = `/api/admin/users/${userId}/logs/reports?page=${page}&size=5`;
  }

  $.get(url, function (data) {
    renderUserLogSection(
      type,
      data.content,
      data.currentPage,
      data.totalPages,
      userId
    );
  });
}

function renderUserLogSection(type, items, currentPage, totalPages, userId) {
  let html = "";

  if (type === "status") {
    html += `<h5>✅ 상태 로그 (${items.length})</h5><ul class="list-group mb-3">`;
    items.forEach((log) => {
      html += `
        <li class="list-group-item">
          <strong>#${log.id}</strong> | ${log.type} | ${formatDate(
        log.createdAt
      )}<br/>
          ${log.content}
        </li>`;
    });
  } else if (type === "request") {
    html += `<h5>📌 요청 (${items.length})</h5><ul class="list-group mb-3">`;
    items.forEach((req) => {
      html += `
        <li class="list-group-item">
          <strong>#${req.id}</strong> | ${req.title} | ${formatDate(
        req.createdAt
      )} | 포인트: ${req.point}
        </li>`;
    });
  } else if (type === "report") {
    html += `<h5>🚨 신고 (${items.length})</h5><ul class="list-group mb-3">`;
    items.forEach((r) => {
      html += `
        <li class="list-group-item">
          <strong>#${r.id}</strong> | 로그 ID: ${r.statusLogId} | 사유: ${
        r.reason
      } | ${formatDate(r.createdAt)}
        </li>`;
    });
  }

  html += "</ul>";
  html += renderPaginationButtons(currentPage, totalPages, userId, type);

  $(`#userLogsArea-${type}`).html(html);
}

function renderPaginationButtons(currentPage, totalPages, userId, type) {
  let html = `<div class="d-flex justify-content-center flex-wrap gap-1 mb-3">`;

  const navBtnClass = "btn btn-sm btn-outline-dark fw-bold"; // ← 화살표 버튼 스타일
  const pageBtnClass = "btn btn-sm"; // ← 숫자 버튼 스타일

  // « 맨 앞으로
  if (currentPage > 0) {
    html += `<button class="${navBtnClass}" onclick="loadUserLogs(${userId}, 0, '${type}')">&laquo;</button>`;
  }

  // ‹ 이전
  if (currentPage > 0) {
    html += `<button class="${navBtnClass}" onclick="loadUserLogs(${userId}, ${
      currentPage - 1
    }, '${type}')">&lsaquo;</button>`;
  }

  // 페이지 번호 (현재 페이지 기준 ±2)
  const start = Math.max(0, currentPage - 2);
  const end = Math.min(totalPages - 1, currentPage + 2);
  for (let i = start; i <= end; i++) {
    const activeClass =
      i === currentPage ? "btn-primary" : "btn-outline-secondary";
    html += `<button class="${pageBtnClass} ${activeClass}" onclick="loadUserLogs(${userId}, ${i}, '${type}')">${
      i + 1
    }</button>`;
  }

  // › 다음
  if (currentPage < totalPages - 1) {
    html += `<button class="${navBtnClass}" onclick="loadUserLogs(${userId}, ${
      currentPage + 1
    }, '${type}')">&rsaquo;</button>`;
  }

  // » 맨 끝으로
  if (currentPage < totalPages - 1) {
    html += `<button class="${navBtnClass}" onclick="loadUserLogs(${userId}, ${
      totalPages - 1
    }, '${type}')">&raquo;</button>`;
  }

  html += `</div>`;
  return html;
}
