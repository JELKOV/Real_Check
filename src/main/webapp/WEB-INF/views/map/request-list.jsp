<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 정보 보기 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link href="/css/map/request-list.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container-fluid mt-4">
      <h3 class="text-center mb-4">내 주변 요청 정보</h3>
      <div class="row">
        <!-- 좌측: 사이드바 -->
        <div class="col-md-4">
          <div
            id="sidebarWrapper"
            class="bg-light border rounded shadow-sm p-3 d-flex flex-column"
            style="height: 85vh"
          >
            <h5 class="fw-bold mb-3">💬 주변 요청 목록</h5>
            <div id="sidebarRequests" class="flex-grow-1 overflow-auto"></div>
          </div>
        </div>

        <!-- 우측: 지도 영역 -->
        <div class="col-md-8">
          <div
            class="map-container-wrapper position-relative"
            style="height: 85vh"
          >
            <!-- 지도 -->
            <div id="map" class="map-container" style="height: 100%"></div>

            <!-- 제어 버튼 -->
            <div
              class="map-control-button"
              id="myLocationButton"
              title="내 위치"
            >
              📍
            </div>
            <div
              class="map-control-button"
              id="refreshNearbyButton"
              title="요청 조회"
            >
              🔄
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/map/request-list.js"></script>
  </body>
</html>
