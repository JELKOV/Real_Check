<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>회원가입 – RealCheck</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/style.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 480px">
      <h3 class="text-center mb-4">회원가입</h3>

      <!-- 에러 메시지 -->
      <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger" role="alert">${errorMsg}</div>
      </c:if>

      <!-- Toast 알림 영역 -->
      <div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1050">
        <div
          id="errorToast"
          class="toast align-items-center text-bg-danger border-0"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <div class="d-flex">
            <div class="toast-body" id="toastMessage">
              입력 정보를 확인해주세요.
            </div>
            <button
              type="button"
              class="btn-close btn-close-white me-2 m-auto"
              data-bs-dismiss="toast"
              aria-label="Close"
            ></button>
          </div>
        </div>
      </div>

      <form method="post" action="/register" id="registerForm">
        <div class="mb-3">
          <label for="email" class="form-label">이메일</label>
          <input
            type="email"
            class="form-control"
            id="email"
            name="email"
            required
            autocomplete="off"
            value=""
          />
          <div class="form-text" id="emailError"></div>
        </div>

        <div class="mb-3">
          <label for="nickname" class="form-label">닉네임</label>
          <input
            type="text"
            class="form-control"
            id="nickname"
            name="nickname"
            required
          />
          <div class="form-text" id="nicknameError"></div>
        </div>

        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input
            type="password"
            class="form-control"
            id="password"
            name="password"
            required
            autocomplete="new-password"
            value=""
          />
        </div>

        <div class="mb-3">
          <label for="confirmPassword" class="form-label">비밀번호 확인</label>
          <input
            type="password"
            class="form-control"
            id="confirmPassword"
            name="confirmPassword"
            required
          />
          <div class="form-text" id="passwordMatchError"></div>
        </div>

        <button type="submit" class="btn btn-primary w-100">가입하기</button>
      </form>

      <div class="text-center mt-3">
        <a href="/login" class="text-decoration-none" id="loginLink"
          >이미 계정이 있으신가요? 로그인</a
        >
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/user/register.js"></script>
  </body>
</html>
