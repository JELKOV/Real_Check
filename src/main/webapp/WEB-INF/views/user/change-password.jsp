<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>비밀번호 변경 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 480px">
      <h3 class="text-center mb-4">비밀번호 변경</h3>

      <form id="passwordForm">
        <div class="mb-3">
          <label for="currentPassword" class="form-label">현재 비밀번호</label>
          <input
            type="password"
            class="form-control"
            id="currentPassword"
            name="currentPassword"
            required
          />
        </div>

        <div class="mb-3">
          <label for="newPassword" class="form-label">새 비밀번호</label>
          <input
            type="password"
            class="form-control"
            id="newPassword"
            name="newPassword"
            required
          />
        </div>

        <div class="mb-3">
          <label for="confirmPassword" class="form-label">비밀번호 확인</label>
          <input
            type="password"
            class="form-control"
            id="confirmPassword"
            required
          />
          <div class="form-text text-danger" id="matchError"></div>
        </div>

        <button type="submit" class="btn btn-primary w-100">변경하기</button>
      </form>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/user/change-password.js"></script>
  </body>
</html>
