$(function () {
  const $filterForm = $("#filterForm");
  const $tableBody = $("#placeTableBody");
  const $pagination = $("#pagination");
  let currentPage = 0;

  // 초기화
  bindFilterForm();
  bindPagination();
  loadPlaces();
  renderOptionsForCustom("statusFilter", "statusList", "statusToggle");

  // ─────────────────────────────────────────────
  // [1] 장소 리스트 조회 및 렌더링
  // ─────────────────────────────────────────────
  function loadPlaces() {
    const q = $("#q").val();
    const status = $("#statusFilter").val();
    $.getJSON("/api/admin/places", {
      page: currentPage,
      size: 10,
      q,
      status,
    }).done((resp) => {
      renderTable(resp.content);
      renderPagination(resp.totalPages);
      bindPlaceTableEvents();
    });
  }

  function renderTable(places) {
    $tableBody.empty();
    places.forEach((p) => {
      const statusLabel = p.approved
        ? '<span class="badge bg-success">승인</span>'
        : p.rejected
        ? '<span class="badge bg-danger">반려</span>'
        : '<span class="badge bg-secondary">Pending</span>';

      let approveRejectButtons = "";
      if (!p.approved && !p.rejected) {
        approveRejectButtons = `
          <div class="d-inline-flex gap-1">
            <button class="btn btn-sm border border-purple text-purple bg-white approve-btn" data-id="${p.id}">✔️</button>
            <button class="btn btn-sm border border-purple text-purple bg-white reject-btn" data-id="${p.id}">✖️</button>
          </div>
        `;
      }

      const deleteButton = `
        <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${p.id}">삭제</button>
      `;
      const detailButton = `
        <button class="btn btn-sm btn-outline-info detail-btn" data-id="${p.id}">상세</button>
      `;

      $tableBody.append(`
        <tr>
          <td class="text-center">${p.id}</td>
          <td>${p.name}</td>
          <td class="d-none d-md-table-cell">${p.address}</td>
          <td class="text-center d-none d-sm-table-cell">${
            p.ownerName || "-"
          }</td>
          <td class="text-center">${statusLabel}</td>
          <td class="d-none d-lg-table-cell">${new Date(
            p.createdAt
          ).toLocaleString()}</td>
          <td class="d-none d-lg-table-cell">${new Date(
            p.updatedAt
          ).toLocaleString()}</td>
          <td class="text-center">${approveRejectButtons}</td>
          <td class="text-center">${deleteButton}</td>
          <td class="text-center">${detailButton}</td>
        </tr>
      `);
    });
  }

  function renderPagination(totalPages) {
    $pagination.empty();
    for (let i = 0; i < totalPages; i++) {
      $pagination.append(`
        <li class="page-item ${i === currentPage ? "active" : ""}">
          <a class="page-link" href="#">${i + 1}</a>
        </li>
      `);
    }
  }

  // ─────────────────────────────────────────────
  // [2] 테이블 내부 버튼 이벤트 바인딩
  // ─────────────────────────────────────────────
  function bindPlaceTableEvents() {
    $tableBody
      .off("click")
      .on("click", ".approve-btn", function () {
        handleApprove($(this).data("id"));
      })
      .on("click", ".reject-btn", function () {
        handleReject($(this).data("id"));
      })
      .on("click", ".delete-btn", function () {
        handleDelete($(this).data("id"));
      })
      .on("click", ".detail-btn", function () {
        handleDetail($(this).data("id"));
      });
  }

  // ─────────────────────────────────────────────
  // [3] 개별 버튼 핸들러 함수들
  // ─────────────────────────────────────────────
  function handleApprove(id) {
    if (!confirm("정말 승인하시겠습니까?")) return;
    $.post(`/api/admin/places/${id}/approve`)
      .done(() => loadPlaces())
      .fail((xhr) => alert("승인 실패: " + xhr.responseText));
  }

  function handleReject(id) {
    const reason = prompt("반려 사유를 입력하세요:");
    if (!reason) {
      alert("반려 사유는 필수입니다.");
      return;
    }

    $.post(`/api/admin/places/${id}/reject`, { reason })
      .done(() => {
        alert("반려 처리 완료");
        loadPlaces();
      })
      .fail((xhr) => {
        alert("반려 실패: " + xhr.responseText);
      });
  }

  function handleDelete(id) {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    $.ajax({
      url: `/api/admin/places/${id}`,
      method: "DELETE",
    })
      .done(() => loadPlaces())
      .fail((xhr) => alert("삭제 실패: " + xhr.responseText));
  }

  function handleDetail(id) {
    $.getJSON(`/api/admin/places/${id}`).done((dto) => openDetailModal(dto));
  }

  // ─────────────────────────────────────────────
  // [4] 상세 모달 렌더링
  // ─────────────────────────────────────────────
  function openDetailModal(dto) {
    $("#detailName").text(dto.name);
    $("#detailAddress").text(dto.address);
    $("#detailOwner").text(dto.ownerName || "-");
    const state = dto.approved ? "승인" : dto.rejected ? "반려" : "Pending";
    $("#detailStatus").text(state);
    $("#detailLocation").text(`${dto.lat}, ${dto.lng}`);
    $("#detailCreatedAt").text(new Date(dto.createdAt).toLocaleString());
    $("#detailUpdatedAt").text(new Date(dto.updatedAt).toLocaleString());
    $("#detailAllowedTypes").text(dto.allowedRequestTypes.join(", ") || "-");
    $("#detailRecentInfo").text(dto.recentInfo || "-");
    $("#detailFavoriteCount").text(dto.favoriteCount);
    $("#detailRequestCount").text(dto.requestCount);
    $("#detailStatusLogCount").text(dto.statusLogCount);

    const modal = new bootstrap.Modal(
      document.getElementById("placeDetailModal")
    );
    modal.show();
  }

  // ─────────────────────────────────────────────
  // [5] 필터 폼 / 페이지네이션 바인딩
  // ─────────────────────────────────────────────
  function bindFilterForm() {
    $filterForm.on("submit", function (e) {
      e.preventDefault();
      currentPage = 0;
      loadPlaces();
    });
  }

  function bindPagination() {
    $pagination.on("click", ".page-link", function (e) {
      e.preventDefault();
      currentPage = parseInt($(this).text(), 10) - 1;
      loadPlaces();
    });
  }
});

// ─────────────────────────────────────────────
// [6] 커스텀 드랍다운 랜더링
// ─────────────────────────────────────────────
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

  // 기본 선택 표시
  const currentText = $select.find("option:selected").text() || "전체";
  $label.text(currentText);

  // 클릭 시 드롭다운 열기/닫기
  $toggle.off("click").on("click", function () {
    $list.toggle();
  });

  // 옵션 클릭 시 선택 처리
  $list.off("click").on("click", "li", function () {
    const value = $(this).data("value");
    const text = $(this).text();

    // set selected value to hidden select
    $select.val(value);
    $label.text(text);

    // 시각적 피드백
    $list.find("li").removeClass("selected");
    $(this).addClass("selected animate-flash");
    setTimeout(() => $(this).removeClass("animate-flash"), 500);

    $list.hide();
  });

  // 외부 클릭 시 드롭다운 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest(`#${toggleId}, #${listId}`).length) {
      $list.hide();
    }
  });
}
