<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>공식 장소 검색 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/place/place-search.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 800px">
      <h3 class="mb-4 text-center">🏢 공식 장소 검색</h3>

      <div class="mb-3">
        <input
          type="text"
          class="form-control"
          id="placeSearch"
          placeholder="장소 이름을 입력하세요"
        />
      </div>

      <ul id="placeSearchResults" class="list-group mb-3"></ul>

      <!-- 지도 영역 -->
      <div
        id="mainMap"
        style="height: 400px; margin-bottom: 2rem; position: relative"
      >
        <!-- 지도 제어 버튼들 -->
        <div class="map-control-group">
          <button class="map-btn" id="myLocationButton" title="내 위치">
            📍
          </button>
          <button
            class="map-btn"
            id="refreshNearbyButton"
            title="주변 새로고침"
          >
            🔄
          </button>
        </div>
      </div>

      <div id="selectedPlaceInfo" class="d-none">
        <div class="card">
          <div class="card-body">
            <h5 id="placeName" class="card-title">장소 이름</h5>
            <p id="placeAddress" class="card-text text-muted">주소 정보</p>
            <div class="d-flex justify-content-between align-items-center">
              <a
                id="communityLink"
                href="#"
                class="btn btn-sm btn-outline-primary"
              >
                커뮤니티 페이지 이동
              </a>
              <button id="favoriteBtn" class="btn btn-sm btn-outline-warning">
                ☆ 즐겨찾기 추가
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/place/place-search.js"></script>
  </body>
</html>
