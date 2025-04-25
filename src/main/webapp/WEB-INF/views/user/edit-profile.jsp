<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>프로필 수정 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    />
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 480px">
      <h3 class="text-center mb-4">닉네임 수정</h3>

      <form id="updateForm">
        <div class="mb-3">
          <label class="form-label">현재 닉네임</label>
          <input
            type="text"
            class="form-control"
            value="${loginUser.nickname}"
            disabled
          />
        </div>

        <div class="mb-3">
          <label for="nickname" class="form-label">새 닉네임</label>
          <input
            type="text"
            class="form-control"
            id="nickname"
            name="nickname"
            required
          />
          <div class="form-text text-danger" id="nicknameError"></div>
        </div>

        <button type="submit" class="btn btn-primary w-100">수정하기</button>
      </form>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
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

      $("#updateForm").on("submit", function (e) {
        e.preventDefault();
        const nickname = $("#nickname").val();

        $.ajax({
          url: "/api/user/update",
          method: "PUT",
          contentType: "application/json",
          data: JSON.stringify({ nickname }),
          success: function () {
            alert("닉네임이 성공적으로 수정되었습니다.");
            location.href = "/mypage";
          },
          error: function () {
            alert("수정에 실패했습니다.");
          },
        });
      });
    </script>
  </body>
</html>
