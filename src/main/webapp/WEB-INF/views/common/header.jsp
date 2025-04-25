<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="/css/header.css" />
<header class="bg-dark text-white py-3 mb-4 shadow-sm">
  <div class="container d-flex justify-content-between align-items-center">
    <!-- 로고 버튼화 -->
    <a href="/" class="text-white text-decoration-none fw-bold fs-4 hover-glow">
      RealCheck
    </a>

    <!-- 네비게이션 메뉴 -->
    <nav class="d-flex align-items-center">
      <a href="/" class="text-white mx-2 text-decoration-none nav-link-style">홈</a>

      <c:choose>
        <c:when test="${not empty sessionScope.loginUser}">
          <a href="/mypage" class="text-white mx-2 text-decoration-none nav-link-style">마이페이지</a>
          <c:if test="${loginUser.role == 'ADMIN'}">
            <a href="/admin" class="text-warning mx-2 text-decoration-none nav-link-style">관리자페이지</a>
          </c:if>
          <a href="/logout" class="text-white mx-2 text-decoration-none nav-link-style">로그아웃</a>
        </c:when>
        <c:otherwise>
          <a href="/login" class="text-white mx-2 text-decoration-none nav-link-style">로그인</a>
          <a href="/register" class="text-white mx-2 text-decoration-none nav-link-style">회원가입</a>
        </c:otherwise>
      </c:choose>
    </nav>
  </div>
</header>