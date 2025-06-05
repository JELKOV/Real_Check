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
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"
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
          <i class="bi bi-search"></i>
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
            <th>상세보기</th>
            <th>조치</th>
          </tr>
        </thead>
        <tbody id="userTableBody">
          <!-- jQuery로 렌더링 -->
        </tbody>
      </table>
    </div>

    <!-- 사용자 상세정보 모달 -->
    <div class="modal fade" id="userDetailModal" tabindex="-1">
      <div class="modal-dialog modal-xl">
        <!-- ★ modal-xl 추가 -->
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">사용자 상세 정보</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>
          <div class="modal-body" id="userDetailModalBody">
            <!-- AJAX로 채워짐 -->
          </div>
        </div>
      </div>
    </div>

    <script src="/js/admin/users.js"></script>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
