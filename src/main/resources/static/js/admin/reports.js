// ──────────────────────────────────────────────
// [0] 페이지 로드 시 바인딩 및 초기 화면 설정
// ──────────────────────────────────────────────
$(document).ready(function () {
  // 초기에는 “전체 신고 목록”을 띄움
  loadAllReports();

  $("#btnAllReports").click(function () {
    loadAllReports();
  });
  $("#btnHiddenLogs").click(function () {
    loadHiddenLogs();
  });
});

// ──────────────────────────────────────────────
// [1] 전체 신고 목록 로드 (열 순서 수정 버전)
// ──────────────────────────────────────────────
function loadAllReports() {
  $(".header-all").show();
  $(".header-hidden").hide();

  $.get("/api/admin/report/all", function (reports) {
    const rows = reports
      .map((r) => {
        // 1) 상태 로그 ID
        const statusLogId = r.statusLogId != null ? r.statusLogId : "-";
        // 2) 작성자(답변자) 이메일
        const writerEmail = r.writerEmail || "-";
        // 3) 답변 내용(= 상태 로그 본문)
        const statusLogContent = r.statusLogContent
          ? r.statusLogContent.replace(/\n/g, "<br/>")
          : "-";
        // 4) 관련 질문 제목/내용 (답변형 로그라면)
        let relatedQuestion = "<em>질문 없음</em>";
        if (r.requestId && r.requestTitle) {
          // 질문 제목+내용을 두 줄로 표시
          relatedQuestion = `
            <strong>${r.requestTitle}</strong><br/>
            <small>${r.requestContent || ""}</small>
          `;
        }
        // 5) 신고자 이메일
        const reporterEmail = r.reporterEmail || "-";
        // 6) 신고 사유
        const reason = r.reason || "-";
        // 7) 신고 날짜
        const createdAt = r.createdAt || "-";

        return `
          <tr>
            <!-- 1. 상태 로그 ID -->
            <td>${statusLogId}</td>
            <!-- 2. 작성자(답변자) -->
            <td>${writerEmail}</td>
            <!-- 3. 답변 내용 -->
            <td>${statusLogContent}</td>
            <!-- 4. 관련 질문(제목/내용) -->
            <td>${relatedQuestion}</td>
            <!-- 5. 신고자 -->
            <td>${reporterEmail}</td>
            <!-- 6. 신고 사유 -->
            <td>${reason}</td>
            <!-- 7. 날짜 -->
            <td>${createdAt}</td>
            <!-- 8. 관리자 기능 버튼 -->
            <td>
              <!-- 신고 횟수 조회 -->
              <button class="btn btn-sm btn-outline-purple me-1"
                      onclick="getReportCount(${statusLogId})">
                신고 횟수
              </button>
              <!-- 오탐 신고 취소(삭제) -->
              <button class="btn btn-sm btn-outline-purple"
                      onclick="deleteReport(${r.id})">
                신고 취소
              </button>
            </td>
          </tr>
        `;
      })
      .join("");

    $("#reportTableBody").html(rows);
  }).fail(function () {
    alert("전체 신고 목록을 불러오는 데 실패했습니다.");
  });
}
// ──────────────────────────────────────────────
// [2] 오탐 신고(Report) 수동 삭제
//    DELETE /api/admin/report/{reportId}
// ──────────────────────────────────────────────
function deleteReport(reportId) {
  if (!confirm("이 신고를 정말 삭제하시겠습니까?")) {
    return;
  }

  $.ajax({
    url: `/api/admin/report/${reportId}`,
    type: "DELETE",
    success: function (response) {
      alert("삭제되었습니다.");
      loadAllReports(); // 목록 다시 로드
    },
    error: function (xhr) {
      if (xhr.status === 404) {
        alert("해당 신고를 찾을 수 없습니다.");
      } else if (xhr.status === 403) {
        alert("관리자 권한이 필요합니다.");
      } else {
        alert("삭제 중 오류가 발생했습니다.");
      }
    },
  });
}

// ──────────────────────────────────────────────
// [3] 기존 함수: 특정 상태 로그 신고 횟수 조회 (변경 없음)
// ──────────────────────────────────────────────
function getReportCount(statusLogId) {
  if (!statusLogId) {
    alert("유효한 로그 ID가 아닙니다.");
    return;
  }
  $.get(`/api/admin/report/count?statusLogId=${statusLogId}`, function (data) {
    alert(`이 상태 로그에 대한 신고 수: ${data.count}`);
  }).fail(function () {
    alert("신고 횟수를 불러오는 데 실패했습니다.");
  });
}

// ──────────────────────────────────────────────
// [4] 숨김 처리된 상태 로그 목록 로드 (기존 로직 그대로)
// ──────────────────────────────────────────────
function loadHiddenLogs() {
  $(".header-all").hide();
  $(".header-hidden").show();

  $.get("/api/admin/report/logs", function (hiddenLogs) {
    const rows = hiddenLogs
      .map(
        (log) => `
        <tr>
          <td>${log.id || "-"}</td>
          <td>${log.nickname || "-"}</td>
          <td>${log.content || "-"}</td>
          <td>${log.hidden ? "예" : "아니오"}</td>
          <td>${log.updatedAt || "-"}</td>
          <td>
            <button class="btn btn-sm btn-outline-purple" onclick="showStatusLogDetail(${
              log.id
            })">
              🔎 신고내역
            </button>
          </td>
        </tr>
      `
      )
      .join("");

    $("#reportTableBody").html(rows);
  }).fail(function () {
    alert("숨김 처리된 상태 로그 목록을 불러오는 데 실패했습니다.");
  });
}

// ──────────────────────────────────────────────
// [5] 상태 로그 신고 내역 보기 (Modal 활용, 변경 없음)
// ──────────────────────────────────────────────
function showStatusLogDetail(statusLogId) {
  if (!statusLogId) {
    alert("유효한 상태 로그 ID가 아닙니다.");
    return;
  }
  $.get(
    `/api/admin/report/statuslog/${statusLogId}/reports`,
    function (reports) {
      let html = "<ul class='list-group'>";
      reports.forEach((r) => {
        html += `
        <li class="list-group-item">
          <b>신고자:</b> ${r.reporterEmail || "-"}<br/>
          <b>로그 작성자:</b> ${r.writerEmail || "-"}<br/>
          <b>사유:</b> ${r.reason || "-"}<br/>
          <b>신고 시각:</b> ${r.createdAt || "-"}
        </li>
      `;
      });
      html += "</ul>";
      $("#statusLogDetailModalBody").html(html);
      $("#statusLogDetailModal").modal("show");
    }
  ).fail(function () {
    alert("신고 내역을 불러오는 데 실패했습니다.");
  });
}
