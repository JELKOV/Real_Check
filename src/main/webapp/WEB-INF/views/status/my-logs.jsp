<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>내가 공유한 정보 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/status/my-logs.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 1000px">
      <h3 class="text-center mb-4">내가 공유한 정보</h3>

      <!-- 필터 영역 -->
      <div
        class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4"
      >
        <div class="filter-group">
          <label for="statusTypeFilter" class="form-label fw-semibold mb-1">
            📂 상태 유형</label
          >
          <!-- 숨겨진 기본 select (JS 연동용) -->
          <select id="statusTypeFilter" class="form-select d-none">
            <option value="">	🗂️ 전체</option>
            <option value="ANSWER">📥 요청 → 답변</option>
            <option value="FREE_SHARE">📢 정보 공유</option>
          </select>

          <!-- 커스텀 드롭다운 -->
          <div id="customStatusDropdown" class="custom-dropdown">
            <button
              type="button"
              id="statusDropdownToggle"
              class="dropdown-toggle-btn"
            >
              <span id="statusDropdownLabel">🗂️ 전체</span>
              <span class="dropdown-arrow">▼</span>
            </button>
            <ul id="statusDropdownList" class="dropdown-list"></ul>
          </div>
        </div>

        <!-- 신고 제외 체크박스 -->
        <div class="form-check form-switch mt-3 mt-md-4">
          <input
            class="form-check-input"
            type="checkbox"
            id="hideHiddenLogs"
            checked
          />
          <label
            class="form-check-label fw-semibold text-danger-emphasis"
            for="hideHiddenLogs"
          >
            🚫 신고처리 제외
          </label>
        </div>
      </div>

      <!-- 카드 기반 로그 출력 영역 -->
      <div id="logsBody" class="row gy-3"></div>

      <nav class="mt-4">
        <ul class="pagination justify-content-center" id="pagination"></ul>
      </nav>
    </div>

    <!-- 수정 모달 -->
    <div
      class="modal fade"
      id="editModal"
      tabindex="-1"
      aria-labelledby="editModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <form id="editForm">
            <div class="modal-header">
              <h5 class="modal-title" id="editModalLabel">상태 로그 수정</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="닫기"
              ></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label>내용</label>
                <input
                  type="text"
                  id="editContent"
                  class="form-control"
                  required
                />
              </div>

              <!-- 카테고리별 유동 필드 삽입 영역 -->
              <div id="dynamicFields"></div>

              <div class="mb-3">
                <label>이미지 업로드</label><br />
                <div class="d-flex align-items-center gap-2">
                  <button
                    type="button"
                    class="btn btn-outline-secondary btn-sm"
                    id="uploadBtn"
                  >
                    이미지 선택
                  </button>
                </div>

                <!-- 미리보기 영역 -->
                <input
                  type="file"
                  id="fileInput"
                  name="files"
                  multiple
                  style="display: none"
                />
                <div id="uploadedPreview" class="mt-2"></div>
              </div>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                닫기
              </button>
              <button type="submit" class="btn btn-primary">저장</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <%@ include file="../common/footer.jsp" %>
    <script src="/js/status/my-logs.js"></script>
  </body>
</html>
