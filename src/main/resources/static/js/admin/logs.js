let currentPage = 0;
const pageSize = 10;

// TargetType별 허용 ActionType 목록
const actionTypeOptions = {
  USER: ["BLOCK", "UNBLOCK"],
  PLACE: ["APPROVE", "REJECT"],
  REPORT: ["REJECT"],
  STATUS_LOG: ["BLOCK", "UNBLOCK"],
};

// 초기 실행
$(document).ready(function () {
  init();
});

// 초기화 함수
function init() {
  bindSearchEvent();
  bindTargetTypeChangeEvent();
  loadAdminList();
  loadLogs();
}

// 검색 버튼 이벤트 바인딩
function bindSearchEvent() {
  $("#searchBtn").on("click", function () {
    currentPage = 0;
    loadLogs();
  });
}

// 대상 유형(targetType) 변경 시 작업 유형(actionType) 필터링
function bindTargetTypeChangeEvent() {
  $("#targetType").on("change", function () {
    const selectedTarget = $(this).val();
    const $actionType = $("#actionType");
    $actionType.empty();
    $actionType.append('<option value="">전체</option>');

    const types = actionTypeOptions[selectedTarget];
    const allTypes = ["BLOCK", "UNBLOCK", "APPROVE", "REJECT"];

    const listToUse = types || allTypes;
    listToUse.forEach((type) => {
      $actionType.append(`<option value="${type}">${type}</option>`);
    });
  });
}

// 관리자 목록 비동기 로딩
function loadAdminList() {
  $.get("/api/admin/users/list", function (admins) {
    const select = $("#adminId");
    admins.forEach((a) => {
      select.append(
        `<option value="${a.id}">${a.nickname} (${a.email})</option>`
      );
    });
  });
}

// 로그 데이터 로드
function loadLogs() {
  const adminId = $("#adminId").val();
  const actionType = $("#actionType").val();
  const targetType = $("#targetType").val();

  const params = {
    page: currentPage,
    size: pageSize,
  };
  if (adminId) params.adminId = adminId;
  if (actionType) params.actionType = actionType;
  if (targetType) params.targetType = targetType;

  $.ajax({
    url: "/api/admin/logs",
    method: "GET",
    data: params,
    success: function (response) {
      renderTable(response.content);
      renderPagination(response.totalPages);
    },
    error: function (xhr) {
      alert("로그 불러오기 실패: " + xhr.responseText);
    },
  });
}

// 테이블 렌더링
function renderTable(logs) {
  const tbody = $("#logTableBody");
  tbody.empty();

  if (logs.length === 0) {
    tbody.append(
      '<tr><td colspan="5" class="text-center">데이터가 없습니다</td></tr>'
    );
    return;
  }

  logs.forEach((log) => {
    const row = `
      <tr>
        <td>${log.id}</td>
        <td>${log.adminNickname || "미확인"} (#${log.adminId})</td>
        <td>${log.actionType}</td>
        <td>${log.targetType} (${log.targetId})</td>
        <td>${log.description || "-"}</td>
      </tr>
    `;
    tbody.append(row);
  });
}

// 페이지네이션 렌더링
function renderPagination(totalPages) {
  const container = $("#pagination");
  container.empty();

  if (totalPages <= 1) return;

  for (let i = 0; i < totalPages; i++) {
    const button = $("<button>")
      .text(i + 1)
      .addClass("btn btn-sm me-1")
      .toggleClass("btn-primary", i === currentPage)
      .toggleClass("btn-outline-secondary", i !== currentPage)
      .on("click", function () {
        currentPage = i;
        loadLogs();
      });

    container.append(button);
  }
}
