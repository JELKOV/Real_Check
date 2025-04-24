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
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 480px">
      <h3 class="text-center mb-4">회원가입</h3>

      <!-- 에러 메시지 -->
      <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger" role="alert">${errorMsg}</div>
      </c:if>

      <form method="post" action="/register" id="registerForm">
        <div class="mb-3">
          <label for="email" class="form-label">이메일</label>
          <input
            type="email"
            class="form-control"
            id="email"
            name="email"
            required
          />
          <div class="form-text text-danger" id="emailError"></div>
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
          <div class="form-text text-danger" id="nicknameError"></div>
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

        <div class="mb-3">
          <label for="confirmPassword" class="form-label">비밀번호 확인</label>
          <input
            type="password"
            class="form-control"
            id="confirmPassword"
            name="confirmPassword"
            required
          />
          <div class="form-text text-danger" id="passwordMatchError"></div>
        </div>

        <button type="submit" class="btn btn-primary w-100">가입하기</button>
      </form>

      <div class="text-center mt-3">
        <a href="/login" class="text-decoration-none"
          >이미 계정이 있으신가요? 로그인</a
        >
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>

<script>
    $(document).ready(function () {
      // 이메일 중복 확인
      $("#email").on("blur", function () {
        const email = $(this).val();
        $.get("/api/user/check-email", { email }, function (exists) {
          if (exists) {
            $("#emailError")
              .text("이미 사용 중인 이메일입니다.")
              .css("color", "red");
          } else {
            $("#emailError")
              .text("사용 가능한 이메일입니다.")
              .css("color", "green");
          }
        });
      });
  
      // 닉네임 중복 확인
      $("#nickname").on("blur", function () {
        const nickname = $(this).val();
        $.get("/api/user/check-nickname", { nickname }, function (exists) {
          if (exists) {
            $("#nicknameError")
              .text("이미 사용 중인 닉네임입니다.")
              .css("color", "red");
          } else {
            $("#nicknameError")
              .text("사용 가능한 닉네임입니다.")
              .css("color", "green");
          }
        });
      });
  
      // 비밀번호 일치 여부 확인
      $("#confirmPassword, #password").on("keyup", function () {
        const pw = $("#password").val();
        const cpw = $("#confirmPassword").val();
        if (pw !== cpw) {
          $("#passwordMatchError")
            .text("비밀번호가 일치하지 않습니다.")
            .css("color", "red");
        } else {
          $("#passwordMatchError").text("").css("color", "");
        }
      });
  
      // 전체 폼 유효성 검사
      $("#registerForm").on("submit", function () {
        return (
          $("#emailError").css("color") !== "red" &&
          $("#nicknameError").css("color") !== "red" &&
          $("#passwordMatchError").text() === ""
        );
      });
    });
  </script>
