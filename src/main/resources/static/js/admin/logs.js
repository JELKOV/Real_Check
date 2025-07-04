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
  setupCustomDropdown("adminId", "adminList", "adminToggle");
  setupCustomDropdown("targetType", "targetList", "targetToggle");
  setupCustomDropdown("actionType", "actionList", "actionToggle");
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

    // 옵션 변경 함수 실행
    renderOptionsForCustom("actionType", "actionList", "actionToggle");
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

    // 옵션 변경 함수 실행
    renderOptionsForCustom("adminId", "adminList", "adminToggle");
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

// 드롭다운 셋업 함수 추가
function setupCustomDropdown(selectId, listId, toggleId) {
  const $select = $(`#${selectId}`);
  const $list = $(`#${listId}`);
  const $toggle = $(`#${toggleId}`);
  const $label = $toggle.find(".label");

  function renderOptions() {
    $list.empty();
    $select.find("option").each(function () {
      const val = $(this).val();
      const text = $(this).text();
      $list.append(`<li data-value="${val}">${text}</li>`);
    });
  }

  renderOptions();

  $toggle.on("click", function () {
    $list.toggle();
  });

  $list.on("click", "li", function () {
    const value = $(this).data("value");
    const label = $(this).text();
    $label.text(label);

    $list.find("li").removeClass("selected animate-flash");
    $(this).addClass("selected animate-flash");

    $list.hide();
    $select.val(value).trigger("change");

    // 애니메이션 재적용 가능하도록 제거 후 재추가
    setTimeout(() => $(this).removeClass("animate-flash"), 500);
  });

  // 외부 클릭 시 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest(`#${toggleId}, #${listId}`).length) {
      $list.hide();
    }
  });
}

// 커스텀 드롭다운 옵션 리렌더링 (동적 변경 대응)
function renderOptionsForCustom(selectId, listId, toggleId) {
  const $select = $(`#${selectId}`);
  const $list = $(`#${listId}`);
  const $toggle = $(`#${toggleId}`);
  const $label = $toggle.find(".label");

  $list.empty();
  $select.find("option").each(function () {
    const val = $(this).val();
    const text = $(this).text();
    $list.append(`<li data-value="${val}">${text}</li>`);
  });

  const currentText = $select.find("option:selected").text() || "전체";
  $label.text(currentText);
}
