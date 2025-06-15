$(document).ready(function () {
  loadLogs();

  function loadLogs() {
    $.get("/api/admin/status-logs", function (logs) {
      const logList = logs.content;
      const $tbody = $("#logTableBody");
      $tbody.empty();

      if (!logList || logList.length === 0) {
        $tbody.append('<tr><td colspan="7">데이터가 없습니다</td></tr>');
        return;
      }

      logList.forEach((log) => {
        console.log("log.nickname: ", log.nickname, typeof log.nickname);
        const rewarded = log.rewarded ? "✅" : "❌";
        const hidden = log.hidden ? "🔒 차단됨" : "✅ 공개";
        const button = log.hidden
          ? `<button class="btn btn-success btn-sm unblock-btn" data-id="${log.id}">차단 해제</button>`
          : `<button class="btn btn-danger btn-sm block-btn" data-id="${log.id}">차단</button>`;

        $tbody.append(`
                <tr>
                <td>${log.id}</td>
                <td>${log.nickname || "미확인"} (#${log.userId})</td>
                <td class="text-truncate" style="max-width: 200px;">${
                  log.content
                }</td>
                <td>${log.viewCount}</td>
                <td>${rewarded}</td>
                <td>${hidden}</td>
                <td>${button}</td>
                </tr>
            `);
      });

      bindBlockButtons();
    });
  }

  function bindBlockButtons() {
    $(".block-btn").on("click", function () {
      const id = $(this).data("id");
      $.post(`/api/admin/status-logs/${id}/block`, function () {
        alert("차단 처리 완료");
        loadLogs();
      });
    });

    $(".unblock-btn").on("click", function () {
      const id = $(this).data("id");
      $.post(`/api/admin/status-logs/${id}/unblock`, function () {
        alert("차단 해제 완료");
        loadLogs();
      });
    });
  }
});
