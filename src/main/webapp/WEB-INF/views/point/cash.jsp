<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>포인트 환전</title>
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
      <h3 class="mb-4">포인트 환전</h3>
      <div class="card p-4 shadow-sm">
        <div class="mb-3">
          <label for="cashAmount" class="form-label">환전할 포인트 금액</label>
          <input
            type="number"
            id="cashAmount"
            class="form-control"
            placeholder="예: 500"
            min="100"
          />
        </div>
        <button id="cashBtn" class="btn btn-danger w-100">💸 환전 신청</button>

        <div
          id="cashMsg"
          class="mt-3 text-success fw-bold"
          style="display: none"
        ></div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/point/cash.js"></script>
  </body>
</html>
