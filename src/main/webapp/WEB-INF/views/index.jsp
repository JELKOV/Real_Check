<%@ page contentType="text/html;charset=UTF-8" language="java" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>RealCheck - 실시간 현장 공유 서비스</title>
  <link rel="stylesheet" href="/css/style.css" />
  <link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    rel="stylesheet"
  />
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
  <%@ include file="common/header.jsp" %>

  <div class="container mt-5">
    <h2 class="text-center mb-4">실시간 현장 정보 공유 서비스</h2>

    <div class="row g-4">
      <!-- 1. 상태 등록 (지도 기반) -->
      <div class="col-md-4">
        <div class="card shadow-sm h-100">
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">상태 등록하기</h5>
            <p class="card-text flex-grow-1">
              지도를 보고 직접 대기 현황을 등록하세요.
            </p>
            <a href="/nearby" class="btn btn-primary mt-auto">
              등록하러 가기
            </a>
          </div>
        </div>
      </div>

      <!-- 2. 주변 대기 현황 보기 -->
      <div class="col-md-4">
        <div class="card shadow-sm h-100">
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">주변 대기 현황 보기</h5>
            <p class="card-text flex-grow-1">
              내 위치 기반으로 근처의 현장 정보를 확인해보세요.
            </p>
            <a href="/nearby" class="btn btn-outline-primary mt-auto">
              지도에서 확인
            </a>
          </div>
        </div>
      </div>

      <!-- 3. 내가 공유한 정보 보기 -->
      <div class="col-md-4">
        <div class="card shadow-sm h-100">
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">내가 공유한 정보</h5>
            <p class="card-text flex-grow-1">
              지금까지 내가 등록한 상태 정보를 확인하고 관리하세요.
            </p>
            <a href="/my-logs" class="btn btn-outline-success mt-auto">
              내 정보 페이지
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>

  <%@ include file="common/footer.jsp" %>
</body>
</html>