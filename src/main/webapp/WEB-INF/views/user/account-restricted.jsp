<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <title>서비스 접근 제한 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/user/account-restricted.css" />
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>
    <div class="container restricted-container text-center">
      <h2>🔒 서비스 접근 제한</h2>
      <p class="mt-3">
        현재 회원님은 <strong class="text-danger">탈퇴 예약 상태</strong>로
        서비스 이용이 제한되었습니다.
      </p>
      <p class="text-muted mb-4">
        계정을 유지하려면 아래 버튼을 클릭하여 탈퇴를 취소하세요.
      </p>

      <!-- 삭제 예정 시간 표시 -->
      <div class="deletion-warning">
        계정이 삭제되기까지 남은 시간:
        <div
          class="timer d-flex justify-content-center gap-2 mt-2"
          id="timer"
          data-deletion="${deletionScheduledAt}"
        >
          <span><strong id="days">0</strong><small>일</small></span>
          <span><strong id="hours">0</strong><small>시간</small></span>
          <span><strong id="minutes">0</strong><small>분</small></span>
          <span><strong id="seconds">0</strong><small>초</small></span>
        </div>
      </div>

      <div class="d-flex justify-content-center gap-3 mt-3">
        <a href="/cancel-account-deletion" class="btn btn-primary">탈퇴 취소</a>
        <form action="/logout" method="post" style="display: inline">
          <button type="submit" class="btn btn-secondary">로그아웃</button>
        </form>
      </div>
    </div>
    <%@ include file="../common/footer.jsp" %>
    <script src="/js/user/account-restricted.js"></script>
  </body>
</html>
