<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>사용자 관리 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center fw-bold mb-4">🙍‍♂️ 사용자 관리</h3>

      <!--검색창 -->
      <div class="input-group mb-3">
        <input
          type="text"
          id="searchInput"
          class="form-control"
          placeholder="이메일 또는 닉네임 검색"
        />
        <button id="searchBtn" type="button" class="btn btn-primary">
          검색
        </button>
      </div>

      <!--사용자 목록 테이블 -->
      <table class="table table-bordered text-center align-middle">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>이메일</th>
            <th>닉네임</th>
            <th>상태</th>
            <th>조치</th>
          </tr>
        </thead>
        <tbody id="userTableBody">
          <!-- jQuery로 렌더링 -->
        </tbody>
      </table>
    </div>

    <script>
      $(document).ready(function () {
        loadBlockedUsers();

        // 사용자 검색
        $("#searchBtn").on("click", function () {
          const keyword = $("#searchInput").val().trim();

          if (keyword.length > 0) {
            $.get(`/api/admin/users/search`, { keyword }, function (users) {
              renderUsers(users);
            });
          } else {
            loadBlockedUsers();
          }
        });

        // 차단 사용자 목록 불러오기
        function loadBlockedUsers() {
          $.get("/api/admin/users/blocked", function (users) {
            renderUsers(users);
          });
        }

        // 사용자 목록 렌더링
        function renderUsers(users) {
          const $tbody = $("#userTableBody");
          $tbody.empty();

          console.log("렌더링 대상 users", users);

          if (!Array.isArray(users) || users.length === 0) {
            $tbody.append(`<tr><td colspan="5">사용자가 없습니다.</td></tr>`);
            return;
          }

          users.forEach((user) => {
            const isActive =
              String(user.active) === "true" || user.active === true;
            const statusText = isActive ? "정상" : "차단됨";
            const unblockBtn = !isActive
              ? `<button class="btn btn-sm btn-success" onclick="unblockUser(${"${user.id}"})">해제</button>`
              : "-";

            const row = `
        <tr>
          <td>${"${user.id}"}</td>
          <td>${"${user.email}"}</td>
          <td>${"${user.nickname}"}</td>
          <td>${"${statusText}"}</td>
          <td>${"${unblockBtn}"}</td>
        </tr>
      `;
            $tbody.append(row);
          });
        }

        // 사용자 차단 해제
        window.unblockUser = function (userId) {
          if (!confirm("정말 차단 해제하시겠습니까?")) return;

          $.ajax({
            url: `/api/admin/users/${"${userId}"}/unblock`,
            type: "PATCH",
            success: function () {
              alert("차단 해제 완료!");
              loadBlockedUsers();
            },
            error: function () {
              alert("차단 해제 실패!");
            },
          });
        };
      });
    </script>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
