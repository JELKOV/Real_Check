<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>신고 관리 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">🚨 신고 관리</h3>

      <!-- ─────────────────────────────────────────────
           버튼 영역: 전체 신고 목록 / 숨김 처리된 상태 로그 조회
      ───────────────────────────────────────────── -->
      <div class="mb-3">
        <button id="btnAllReports" class="btn btn-primary btn-sm me-2">
          전체 신고 목록 보기
        </button>
        <button id="btnHiddenLogs" class="btn btn-warning btn-sm">
          숨김 상태 로그 조회
        </button>
      </div>

      <!-- ─────────────────────────────────────────────
           테이블 영역: 모드에 따라 헤더/바디를 전환
      ───────────────────────────────────────────── -->
      <table class="table table-bordered text-center align-middle">
        <thead class="table-light">
          <!-- [헤더1] 전체 신고 모드용 (원하는 순서대로 재배치) -->
          <tr class="header-all">
            <th>상태 로그 ID</th>
            <th>작성자(답변자)</th>
            <th>답변 내용</th>
            <th>관련 질문(제목/내용)</th>
            <th>신고자</th>
            <th>신고 사유</th>
            <th>날짜</th>
            <th>관리자 기능</th>
          </tr>

          <!-- [헤더2] 숨김 처리된 상태 로그 모드용 -->
          <tr class="header-hidden" style="display: none">
            <th>상태 로그 ID</th>
            <th>작성자(답변자)</th>
            <th>메시지</th>
            <th>숨김 여부</th>
            <th>숨김 시각</th>
            <th>기능</th>
          </tr>
        </thead>
        <tbody id="reportTableBody"></tbody>
      </table>
    </div>

    <!-- ─────────────────────────────────────────────
         선택: 상태 로그 상세 보기용 Modal (optional)
    ───────────────────────────────────────────── -->
    <div
      class="modal fade"
      id="statusLogDetailModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">상태 로그 상세</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <div class="modal-body" id="statusLogDetailModalBody">
            <!-- AJAX로 채워질 신고 내역 리스트 -->
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <%@ include file="../common/footer.jsp" %>
    <script src="/js/admin/reports.js"></script>
  </body>
</html>
