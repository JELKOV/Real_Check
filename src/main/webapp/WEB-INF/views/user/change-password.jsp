<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>비밀번호 변경 - RealCheck</title>
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    />
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

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
      $("#confirmPassword, #newPassword").on("keyup", function () {
        const pw = $("#newPassword").val();
        const cpw = $("#confirmPassword").val();
        if (pw !== cpw) {
          $("#matchError").text("비밀번호가 일치하지 않습니다.");
        } else {
          $("#matchError").text("");
        }
      });

      $("#passwordForm").on("submit", function (e) {
        e.preventDefault();

        const currentPassword = $("#currentPassword").val();
        const newPassword = $("#newPassword").val();

        $.ajax({
          url: "/api/user/password",
          method: "PUT",
          contentType: "application/json",
          data: JSON.stringify({
            currentPassword,
            newPassword,
          }),
          success: function () {
            alert("비밀번호가 변경되었습니다.");
            location.href = "/mypage";
          },
          error: function (xhr) {
            alert(xhr.responseText || "변경 실패");
          },
        });
      });
    </script>
  </body>
</html>
