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

  // [3] 차단 해제 버튼 클릭 처리 (동적 이벤트)
  $(document).on("click", ".unblock-btn", function () {
    const userId = $(this).data("id");
    unblockUser(userId); // 해제 요청
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

    // 차단 해제 버튼: 차단된 사용자만 표시
    const unblockBtn = !isActive
      ? `<button class="btn btn-sm btn-success unblock-btn" data-id="${user.id}">해제</button>`
      : "-";

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
          <td>${unblockBtn}</td>
        </tr>
      `;
    $tbody.append(row); // 테이블에 추가
  });
}

// [C] 사용자 상세 정보 표시 (모달 열기)
function showUserDetail(userId) {
  $.get(`/api/admin/users/${userId}`, function (data) {
    const u = data.user;

    // 상세 정보 HTML 생성
    const html = `
        <ul class="list-group text-start">
          <li class="list-group-item"><strong>ID:</strong> ${u.id}</li>
          <li class="list-group-item"><strong>이메일:</strong> ${u.email}</li>
          <li class="list-group-item"><strong>닉네임:</strong> ${
            u.nickname
          }</li>
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
      `;
    $("#userDetailModalBody").html(html); // 모달 내용 삽입
    $("#userDetailModal").modal("show"); // 모달 열기
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
