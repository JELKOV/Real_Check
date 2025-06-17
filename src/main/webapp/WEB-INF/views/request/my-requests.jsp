<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>내 요청 목록 - RealCheck</title>
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
      <h3 class="mb-4">내 요청 목록</h3>

      <!-- 필터 영역 -->
      <div class="row mb-4">
        <div class="col-md-3">
          <select id="categoryFilter" class="form-select">
            <option value="">전체 카테고리</option>
            <!-- 동적으로 항목 추가 -->
          </select>
        </div>
        <div class="col-md-6">
          <input
            type="text"
            id="searchKeyword"
            class="form-control"
            placeholder="제목 또는 내용 검색"
          />
        </div>
        <div class="col-md-3">
          <button id="filterBtn" class="btn btn-primary w-100">검색</button>
        </div>
      </div>

      <div id="requestTableContainer"></div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/request/my-requests.js"></script>
  </body>
</html>
