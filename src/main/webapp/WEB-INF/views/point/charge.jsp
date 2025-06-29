<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>포인트 충전 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="mb-4">포인트 충전</h3>

      <div class="card p-4 shadow-sm">
        <div class="mb-3">
          <label for="chargeAmount" class="form-label">충전 금액</label>
          <input
            type="number"
            id="chargeAmount"
            class="form-control"
            placeholder="예: 100"
            min="1"
          />
        </div>

        <button id="chargeBtn" class="btn btn-primary w-100">
          💳 테스트 충전하기
        </button>

        <div
          id="chargeMsg"
          class="mt-3 text-success fw-bold"
          style="display: none"
        ></div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/point/charge.js"></script>
  </body>
</html>
