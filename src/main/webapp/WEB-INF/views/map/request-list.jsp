<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 지도 보기 - RealCheck</title>
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
        <!-- 지도 영역 -->
        <div class="col-md-8 position-relative">
          <div id="map" class="map-container position-relative">
            <div class="map-control-container">
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

        <!-- 사이드바 -->
        <div class="col-md-4">
          <div
            id="sidebarWrapper"
            class="bg-light border rounded shadow-sm p-3"
            style="height: 85vh; overflow-y: auto"
          >
            <h6 class="mb-3 fw-bold">💬 주변 요청 목록</h6>
            <div id="sidebarRequests"></div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/map/request-list.js"></script>
  </body>
</html>
