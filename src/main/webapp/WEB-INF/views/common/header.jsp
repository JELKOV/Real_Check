<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="bg-dark text-white p-3 mb-4">
  <div class="container d-flex justify-content-between align-items-center">
    <h1 class="h4 mb-0">RealCheck</h1>
    <nav>
      <a href="/" class="text-white mx-2 text-decoration-none">홈</a>
      <c:choose>
        <c:when test="${not empty sessionScope.loginUser}">
          <a href="/mypage" class="text-white mx-2 text-decoration-none">마이페이지</a>
          <c:if test="${loginUser.role == 'ADMIN'}">
            <a href="/admin" class="text-warning mx-2 text-decoration-none">관리자페이지</a>
          </c:if>
          <a href="/logout" class="text-white mx-2 text-decoration-none">로그아웃</a>
        </c:when>
        <c:otherwise>
          <a href="/login" class="text-white mx-2 text-decoration-none">로그인</a>
          <a href="/register" class="text-white mx-2 text-decoration-none">회원가입</a>
        </c:otherwise>
      </c:choose>
    </nav>
  </div>
</header>
