<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>로그인 - RealCheck</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/style.css" />
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 480px">
      <h3 class="text-center mb-4">로그인</h3>

      <!-- 성공 메시지 -->
      <c:if test="${not empty successMsg}">
        <div class="alert alert-success" role="alert">${successMsg}</div>
      </c:if>

      <!-- 에러 메시지 -->
      <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger" role="alert">${errorMsg}</div>
      </c:if>

      <form method="post" action="/login">
        <div class="mb-3">
          <label for="email" class="form-label">이메일</label>
          <input
            type="email"
            class="form-control"
            id="email"
            name="email"
            required
          />
        </div>

        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input
            type="password"
            class="form-control"
            id="password"
            name="password"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary w-100">로그인</button>
      </form>

      <div class="text-center mt-3">
        <a href="/register" class="text-decoration-none">회원가입</a>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
