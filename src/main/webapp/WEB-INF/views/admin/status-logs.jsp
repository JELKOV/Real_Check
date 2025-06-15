<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>자발 공유 관리 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center fw-bold mb-4">🚨 자발 공유 로그 관리</h3>

      <!-- 📋 테이블 -->
      <table class="table table-bordered text-center align-middle">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>작성자</th>
            <th>내용</th>
            <th>조회수</th>
            <th>보상 여부</th>
            <th>상태</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody id="logTableBody">
          <tr>
            <td colspan="7">데이터를 불러오는 중...</td>
          </tr>
        </tbody>
      </table>
    </div>

    <script></script>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/admin/status-logs.js"></script>
  </body>
</html>
