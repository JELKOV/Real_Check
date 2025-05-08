<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 등록 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/register.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 700px">
      <h3 class="text-center mb-4">요청 등록</h3>

      <!-- 장소 선택 방식 -->
      <div class="mb-3">
        <label class="form-label">장소 선택 방식</label>
        <div class="btn-group w-100">
          <button type="button" class="btn btn-primary w-50" id="btnPlace">
            공식 장소
          </button>
          <button
            type="button"
            class="btn btn-outline-primary w-50"
            id="btnCustom"
          >
            사용자 지정 장소
          </button>
        </div>
      </div>

      <!-- 협력업체 장소 (Place) 선택 -->
      <div id="placeSection">
        <div class="mb-3">
          <label class="form-label">공식 장소 검색</label>
          <input
            type="text"
            class="form-control"
            id="placeSearch"
            placeholder="장소 이름 검색"
          />
          <ul id="placeSearchResults" class="list-group mt-2"></ul>
        </div>

        <div id="placeMap" class="map-container"></div>

        <div class="mb-3">
          <label class="form-label">선택된 장소 정보</label>
          <input
            type="text"
            class="form-control"
            id="selectedPlaceName"
            readonly
          />
          <input type="hidden" id="placeId" name="placeId" />
          <input type="hidden" id="lat" name="lat" />
          <input type="hidden" id="lng" name="lng" />
        </div>
      </div>

      <!-- 사용자 지정 장소 (Custom Place) 선택 -->
      <div id="customSection" style="display: none">
        <div class="mb-3">
          <label class="form-label">사용자 지정 장소 이름</label>
          <input
            type="text"
            class="form-control"
            id="customPlaceName"
            placeholder="예: 강남역 근처"
          />
        </div>

        <div id="customMap" class="map-container"></div>
        <input type="hidden" id="latCustom" name="lat" />
        <input type="hidden" id="lngCustom" name="lng" />
      </div>

      <!-- 요청 정보 -->
      <div class="mb-3">
        <label for="category" class="form-label">요청 카테고리</label>
        <select class="form-select" id="category" name="category" required>
          <option value="">카테고리를 선택하세요</option>
          <option value="PARKING">🅿️ 주차 가능 여부</option>
          <option value="WAITING_STATUS">⏳ 대기 상태</option>
          <option value="STREET_VENDOR">🥟 노점 현황</option>
          <option value="PHOTO_REQUEST">📸 현장 사진 요청</option>
          <option value="BUSINESS_STATUS">🏪 가게 영업 여부</option>
          <option value="OPEN_SEAT">💺 좌석 여유</option>
          <option value="BATHROOM">🚻 화장실 여부</option>
          <option value="WEATHER_LOCAL">☁️ 현장 날씨</option>
          <option value="NOISE_LEVEL">🔊 소음 여부</option>
          <option value="FOOD_MENU">🍔 메뉴/음식</option>
          <option value="CROWD_LEVEL">👥 혼잡도</option>
          <option value="ETC">❓ 기타</option>
        </select>
      </div>

      <div class="mb-3">
        <label for="title" class="form-label">요청 제목</label>
        <input
          type="text"
          class="form-control"
          id="title"
          name="title"
          required
        />
      </div>

      <div class="mb-3">
        <label for="content" class="form-label">요청 내용</label>
        <textarea
          class="form-control"
          id="content"
          rows="3"
          required
        ></textarea>
      </div>

      <div class="mb-3">
        <label for="point" class="form-label">포인트 설정</label>
        <input
          type="number"
          class="form-control"
          id="point"
          min="1"
          value="10"
          required
        />
      </div>

      <button type="submit" class="btn btn-primary w-100">요청 등록</button>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/register.js"></script>
  </body>
</html>
