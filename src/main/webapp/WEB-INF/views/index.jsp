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
    <div class="container">
      <h2 class="text-center mb-4">실시간 현장 정보 공유 서비스</h2>

      <div class="row">
        <div class="col-md-6">
          <div class="card shadow-sm mb-4">
            <div class="card-body">
              <h5 class="card-title">주변 대기 현황 보기</h5>
              <p class="card-text">
                내 위치 기반으로 근처의 현장 정보를 확인해보세요.
              </p>
              <a href="/nearby" class="btn btn-primary">지도에서 확인</a>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card shadow-sm mb-4">
            <div class="card-body">
              <h5 class="card-title">내가 공유한 정보 보기</h5>
              <p class="card-text">
                지금까지 내가 등록한 상태 정보를 확인하고 관리하세요.
              </p>
              <a href="/my-logs" class="btn btn-outline-primary">내정보페이지</a>
            </div>
          </div>
        </div>
      </div>
    </div>
    <%@ include file="common/footer.jsp" %>
  </body>
</html>
