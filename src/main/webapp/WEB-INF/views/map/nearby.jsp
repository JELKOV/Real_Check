<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>현장 정보 보기 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link href="/css/map/nearby.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container-fluid mt-4">
      <h3 class="text-center mb-4">등록된 현장 정보 탐색</h3>
      <div class="row">
        <!-- 지도 영역 -->
        <div class="col-md-8">
          <div class="map-container-wrapper">
            <!-- 좌측 상단 필터 버튼 -->
            <div class="map-filter-group">
              <button
                class="btn btn-outline-primary btn-sm me-1 filter-btn"
                id="filterModeOfficial"
              >
                공식장소
              </button>
              <button
                class="btn btn-outline-success btn-sm filter-btn"
                id="filterModeUser"
              >
                사용자지정장소
              </button>
            </div>

            <!-- 우측 하단 제어 버튼 -->
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
              title="주변 정보"
            >
              🔄
            </div>

            <!-- 지도 표시 영역 -->
            <div id="statusMap" class="map-container"></div>
          </div>
        </div>

        <!-- 사이드 정보 패널 -->
        <div class="col-md-4">
          <div
            id="sidebarWrapper"
            class="bg-light border rounded shadow-sm p-3"
            style="height: 85vh; overflow-y: auto; display: block"
          >
            <!-- 1. 최상단 타이틀 -->
            <h4 class="fw-bold mb-3">📌 주변 응답 정보</h4>

            <!-- 2. 공식 장소 응답 -->
            <div id="sidebarOfficialContent" style="display: none">
              <h5 class="text-primary fw-bold">🏛 공식 장소 응답</h5>
              <div id="sidebarGroupedLogs"></div>
            </div>

            <!-- 3. 사용자 지정 응답 -->
            <div id="sidebarUserContent" style="display: none">
              <h5 class="text-success fw-bold">📍 사용자 지정 응답</h5>
              <div id="sidebarUserLogs"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/map/nearby.js"></script>
  </body>
</html>
