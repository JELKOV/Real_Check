<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>오픈된 요청 목록 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/request/list.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">오픈된 요청 목록</h3>

      <!-- 필터 영역 -->
      <div class="mb-3 text-center">
        <select id="categoryFilter" class="form-select w-auto d-inline-block">
          <option value="">전체 카테고리</option>
          <option value="PARKING">🅿️ 주차 가능 여부</option>
          <option value="WAITING_STATUS">⏳ 대기 상황</option>
          <option value="STREET_VENDOR">🥟 노점 현황</option>
          <option value="PHOTO_REQUEST">📸 사진 요청</option>
          <option value="BUSINESS_STATUS">🏪 업영 유무</option>
          <option value="OPEN_SEAT">💺 자석 유여</option>
          <option value="BATHROOM">🚻 화장실 유무</option>
          <option value="WEATHER_LOCAL">☁️ 현장 날씨</option>
          <option value="NOISE_LEVEL">🔊 소음 상황</option>
          <option value="FOOD_MENU">🍔 음식/메뉴</option>
          <option value="CROWD_LEVEL">👥 호장도</option>
          <option value="ETC">❓ 기타</option>
        </select>

        <select
          id="radiusFilter"
          class="form-select w-auto d-inline-block me-2"
        >
          <option value="1000">1km 이내(검색 위치 기준)</option>
          <option value="2000">2km 이내(검색 위치 기준)</option>
          <option value="3000" selected>3km 이내(검색 위치 기준)</option>
          <option value="5000">5km 이내(검색 위치 기준)</option>
          <option value="10000">10km 이내(검색 위치 기준)</option>
        </select>
      </div>

      <!-- 위치 검색 -->
      <div class="mb-3 text-center">
        <input
          type="text"
          id="locationInput"
          class="form-control w-50 d-inline-block me-2"
          placeholder="예: 강남역, 서울시청 등"
        />
        <button id="searchLocationBtn" class="btn btn-primary">
          위치 검색
        </button>
      </div>

      <!-- 지도 + 요청 리스트 2단 구조 -->
      <div class="row gx-3">
        <div class="col-md-6">
          <div id="map" class="map-area position-relative">
            <div
              id="myLocationButton"
              class="map-control-button"
              title="내 위치"
            >
              📍
            </div>
            <div
              id="refreshNearbyButton"
              class="map-control-button"
              title="다시 불러오기"
            >
              🔄
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div id="requestListContainer" class="scrollable-list border rounded">
            <div id="requestList" class="row g-3 p-2"></div>
            <div
              id="loadingIndicator"
              class="text-center my-3"
              style="display: none"
            >
              <div class="spinner-border text-primary" role="status"></div>
              <p class="mt-2">불러오는 중...</p>
            </div>

            <div
              id="endOfListMessage"
              class="text-center text-muted my-3"
              style="display: none"
            >
              더 이상 불러올 요청이 없습니다.
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 위치 검색 결과 목록 모니터 -->
    <div
      class="modal fade"
      id="searchResultModal"
      tabindex="-1"
      role="dialog"
      aria-hidden="true"
    >
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">검색 결과 선택</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body">
            <ul id="searchResultList" class="list-group"></ul>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/request/list.js"></script>
  </body>
</html>
