<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>관리자 행동 로그</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"
    />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center fw-bold mb-4">📝 관리자 행동 로그</h3>

      <!-- 🔍 필터 영역 -->
      <div class="row g-3 align-items-end mb-4">
        <div class="col-md-3">
          <label for="adminId" class="form-label">관리자</label>
          <select id="adminId" class="form-select">
            <option value="">전체</option>
            <!-- 여기에 관리자 목록이 동적으로 추가됨 -->
          </select>
        </div>
        <div class="col-md-3">
          <label for="actionType" class="form-label">작업 유형</label>
          <select id="actionType" class="form-select">
            <option value="">전체</option>
            <option value="BLOCK">BLOCK</option>
            <option value="UNBLOCK">UNBLOCK</option>
            <option value="APPROVE">APPROVE</option>
            <option value="REJECT">REJECT</option>
          </select>
        </div>
        <div class="col-md-3">
          <label for="targetType" class="form-label">대상 유형</label>
          <select id="targetType" class="form-select">
            <option value="">전체</option>
            <option value="USER">USER</option>
            <option value="PLACE">PLACE</option>
            <option value="REPORT">REPORT</option>
            <option value="STATUS_LOG">STATUS_LOG</option>
          </select>
        </div>
        <div class="col-md-3 d-grid">
          <button id="searchBtn" class="btn btn-primary">
            <i class="bi bi-search"></i> 검색
          </button>
        </div>
      </div>

      <!-- 📋 로그 테이블 -->
      <table class="table table-bordered text-center align-middle">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>관리자 ID</th>
            <th>작업</th>
            <th>대상</th>
            <th>설명</th>
          </tr>
        </thead>
        <tbody id="logTableBody">
          <!-- jQuery로 채워짐 -->
        </tbody>
      </table>

      <!-- 페이지네이션 -->
      <div id="pagination" class="d-flex justify-content-center mt-4"></div>
    </div>

    <script src="/js/admin/logs.js"></script>
    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
