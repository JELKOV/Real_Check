/**
 * 사용하는 곳
 * - request/detail.js
 */

// [1] 신고버튼 UI 업데이트 토글 형식
export function updateReportButton(statusLogId, reportCount) {
  const btn = $(`.report-toggle-btn[data-id=${statusLogId}]`);
  if (btn.length === 0) {
    // 버튼이 아직 DOM에 없으면 100ms 후 재시도
    return setTimeout(() => updateReportButton(statusLogId, reportCount), 100);
  }

  $.get(`/api/report/check?statusLogId=${statusLogId}`, function (isReported) {
    if (isReported) {
      btn
        .removeClass("btn-secondary btn-danger")
        .addClass("btn-outline-danger")
        .text(`🚨 신고됨 (${reportCount})`)
        .prop("disabled", false);
    } else {
      btn
        .removeClass("btn-secondary btn-outline-danger")
        .addClass("btn-danger")
        .text(`🚨 신고 (${reportCount})`)
        .prop("disabled", false);
    }
  });
}

// [2] 신고 버튼 클릭 처리 : 모달 생성
export function handleReportToggle(loadAnswerList, requestId) {
  const statusLogId = $(this).data("id");
  const btn = $(this);
  const isAlreadyReported = btn.hasClass("btn-outline-danger");

  if (isAlreadyReported) {
    // 신고 취소
    if (!confirm("🚨 신고를 취소하시겠습니까?")) return;

    $.ajax({
      url: `/api/report?statusLogId=${statusLogId}`,
      method: "DELETE",
      success: function () {
        alert("신고가 취소되었습니다.");
        loadAnswerList(requestId);
      },
      error: function (xhr) {
        alert("신고 취소 실패: " + xhr.responseText);
      },
    });
  } else {
    // 신고 모달 표시
    $("#reportTargetStatusLogId").val(statusLogId);
    $("#reportReasonSelect").val(""); // 초기화
    const modal = new bootstrap.Modal(
      document.getElementById("reportReasonModal")
    );
    modal.show();
  }
}

// [3] 신고 처리 하기
export function handleSubmitReportReason(loadAnswerList, requestId) {
  const statusLogId = $("#reportTargetStatusLogId").val();
  const reason = $("#reportReasonSelect").val();
  const modal = bootstrap.Modal.getInstance(
    document.getElementById("reportReasonModal")
  );

  if (!reason) {
    alert("🚨 신고 사유를 선택해주세요.");
    return;
  }

  $.ajax({
    url: `/api/report`,
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify({ statusLogId, reason }),
    success: function () {
      alert("신고가 접수되었습니다.");
      modal.hide();
      loadAnswerList(requestId);
    },
    error: function (xhr) {
      alert("신고 실패: " + xhr.responseText);
    },
  });
}
