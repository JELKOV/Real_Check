<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="/css/header.css" />
<!-- Bootstrap Icons (아이콘용) -->
<link
  rel="stylesheet"
  href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"
/>
<script>
  const isLoggedIn = (
    <c:out value="${not empty sessionScope.loginUser}" default="false" />
  );
</script>

<header class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm mb-4">
  <div
    class="container-fluid px-4 d-flex justify-content-between align-items-center"
  >
    <!-- 로고 -->
    <a class="navbar-brand fw-bold fs-4 hover-glow" href="/">RealCheck</a>

    <!-- 햄버거 버튼 -->
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navbarContent"
      aria-controls="navbarContent"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <span class="navbar-toggler-icon"></span>
    </button>

    <!-- 메뉴 -->
    <div class="collapse navbar-collapse" id="navbarContent">
      <ul
        class="navbar-nav ms-auto mb-2 mb-lg-0 d-flex align-items-lg-center text-center text-lg-start"
      >
        <li class="nav-item">
          <a class="nav-link hover-glow" href="/"
            ><i class="bi bi-house"></i> 홈</a
          >
        </li>

        <c:if test="${not empty loginUser and loginUser.hasPlace}">
          <li class="nav-item">
            <a class="nav-link hover-glow" href="/place/my">
              <i class="bi bi-geo-alt"></i> 내 장소 관리
            </a>
          </li>
        </c:if>

        <c:choose>
          <c:when test="${not empty sessionScope.loginUser}">
            <!-- 내 활동 드롭다운 -->
            <li class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle hover-glow"
                href="#"
                role="button"
                data-bs-toggle="dropdown"
              >
                <i class="bi bi-person-lines-fill"></i> 내 활동
              </a>
              <ul class="dropdown-menu dropdown-menu-dark">
                <li>
                  <a class="dropdown-item" href="/my-requests">내 요청</a>
                </li>
                <li><a class="dropdown-item" href="/my-logs">내 답변</a></li>
                <li>
                  <a class="dropdown-item" href="/my-favorites">즐겨찾기</a>
                </li>
              </ul>
            </li>

            <!-- 포인트 드롭다운 -->
            <li class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle hover-glow"
                href="#"
                role="button"
                data-bs-toggle="dropdown"
              >
                <i class="bi bi-currency-dollar"></i> 포인트
              </a>
              <ul class="dropdown-menu dropdown-menu-dark">
                <li>
                  <a class="dropdown-item" href="/point/charge">포인트 충전</a>
                </li>
                <li>
                  <a class="dropdown-item" href="/point/cash">포인트 환전</a>
                </li>
              </ul>
            </li>

            <li class="nav-item">
              <a class="nav-link hover-glow" href="/mypage">
                <i class="bi bi-person-circle"></i> 마이페이지
              </a>
            </li>

            <c:if test="${loginUser.role == 'ADMIN'}">
              <li class="nav-item">
                <a class="nav-link text-warning nav-link-style" href="/admin">
                  <i class="bi bi-shield-lock"></i> 관리자페이지
                </a>
              </li>
            </c:if>

            <li class="nav-item">
              <form action="/logout" method="post" class="d-inline">
                <button
                  type="submit"
                  class="btn btn-link nav-link text-white hover-glow p-0"
                >
                  <i class="bi bi-box-arrow-right"></i> 로그아웃
                </button>
              </form>
            </li>
          </c:when>
          <c:otherwise>
            <li class="nav-item">
              <a class="nav-link hover-glow" href="/login">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link hover-glow" href="/register">
                <i class="bi bi-person-plus"></i> 회원가입
              </a>
            </li>
          </c:otherwise>
        </c:choose>
      </ul>
    </div>
  </div>
</header>
