<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>자발적 정보 공유 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link href="/css/map/free-share.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div id="freeShareLayout">
      <div id="freeSharePage" class="d-flex flex-column flex-md-row">
        <!-- 왼쪽: 공유 정보 목록 -->
        <div id="freeShareListContainer" class="p-2 border-end bg-white">
          <div class="input-group mt-2">
            <input
              type="text"
              id="addressInput"
              class="form-control"
              placeholder="지역명을 입력하세요"
            />
            <button
              type="button"
              class="btn btn-outline-primary"
              id="searchAddressBtn"
            >
              🔍 지역 검색
            </button>
          </div>
          <div class="p-3">
            <h5>📋 공유 정보 목록</h5>

            <div class="row g-2 mb-3">
              <div class="col-md-6">
                <label for="daysSelect" class="form-label">조회 기간</label>
                <!-- 실제 작동할 원본 select (숨김) -->
                <select id="daysSelect" class="form-select d-none">
                  <option value="1">1일 이내</option>
                  <option value="3">3일 이내</option>
                  <option value="7" selected>7일 이내</option>
                  <option value="30">한 달 이내</option>
                </select>

                <!-- 커스텀 UI용 드롭다운 -->
                <div class="custom-select-wrapper" id="daysDropdown">
                  <div class="custom-select-toggle">
                    <span class="label">7일 이내</span>
                    <span class="caret-icon">▾</span>
                  </div>
                  <ul class="custom-select-options">
                    <li data-value="1">1일 이내</li>
                    <li data-value="3">3일 이내</li>
                    <li data-value="7" class="selected">7일 이내</li>
                    <li data-value="30">한 달 이내</li>
                  </ul>
                </div>
              </div>

              <div class="col-md-6">
                <label for="radiusSelect" class="form-label">반경 거리</label>
                <select id="radiusSelect" class="form-select d-none">
                  <option value="1000">1km</option>
                  <option value="3000" selected>3km</option>
                  <option value="5000">5km</option>
                </select>

                <div class="custom-select-wrapper" id="radiusDropdown">
                  <div class="custom-select-toggle">
                    <span class="label">3km</span>
                    <span class="caret-icon">▾</span>
                  </div>
                  <ul class="custom-select-options">
                    <li data-value="1000">1km</li>
                    <li data-value="3000" class="selected">3km</li>
                    <li data-value="5000">5km</li>
                  </ul>
                </div>
              </div>

              <div id="registerInstructionArea" style="display: none">
                <div class="alert alert-info mt-3">
                  📝 <strong>등록 모드입니다.</strong><br />
                  지도를 클릭하여 <strong>위치를 선택</strong>하면 정보를 등록할
                  수 있습니다.<br />
                  등록은
                  <span class="fw-bold text-primary">3km 이내</span>에서만
                  가능합니다.
                </div>
              </div>
            </div>

            <div id="freeShareList"></div>
            <div
              class="text-center my-3"
              id="loadMoreContainer"
              style="display: none"
            >
              <button id="loadMoreBtn" class="btn btn-outline-primary">
                더보기
              </button>
            </div>
            <div
              id="loadingSpinner"
              class="text-center py-3"
              style="display: none"
            >
              <div class="spinner-border text-secondary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 지도 영역 -->
        <div id="freeShareMap" class="position-relative">
          <div class="map-control-container">
            <div
              class="map-control-button"
              id="registerToggleButton"
              title="등록 모드 전환"
            >
              📝<br /><span id="registerBtnLabel">정보 등록</span>
            </div>
            <div
              class="map-control-button"
              id="myLocationButton"
              title="내 위치"
            >
              📍<br /><span>내위치</span>
            </div>
            <div
              class="map-control-button"
              id="refreshNearbyButton"
              title="주변 새로고침"
            >
              🔄<br /><span>새로고침</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 정보 등록용 모달 -->
      <div
        class="modal fade"
        id="registerModal"
        tabindex="-1"
        aria-hidden="true"
      >
        <div class="modal-dialog">
          <form id="registerForm" class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">자발적 정보 등록</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="닫기"
              ></button>
            </div>
            <div class="modal-body">
              <input type="hidden" name="lat" />
              <input type="hidden" name="lng" />

              <div class="mb-3">
                <label class="form-label">내용</label>
                <textarea
                  name="content"
                  class="form-control"
                  rows="3"
                  required
                ></textarea>
              </div>

              <div class="mb-3">
                <label class="form-label">카테고리</label>
                <select name="category" class="form-select" id="categorySelect">
                  <option value="">선택 없음</option>
                  <option value="PARKING">🅿️ 주차 가능 여부</option>
                  <option value="WAITING_STATUS">⏳ 대기 상태</option>
                  <option value="STREET_VENDOR">🥟 노점 현황</option>
                  <option value="PHOTO_REQUEST">📸 사진 요청</option>
                  <option value="BUSINESS_STATUS">🏪 가게 영업 여부</option>
                  <option value="OPEN_SEAT">💺 좌석 여유</option>
                  <option value="BATHROOM">🚻 화장실 여부</option>
                  <option value="WEATHER_LOCAL">☁️ 날씨 상태</option>
                  <option value="NOISE_LEVEL">🔊 소음 여부</option>
                  <option value="FOOD_MENU">🍔 메뉴/음식</option>
                  <option value="CROWD_LEVEL">👥 혼잡도</option>
                  <option value="ETC">❓ 기타</option>
                </select>
              </div>

              <div id="categoryDynamicFields"></div>

              <div class="mb-3">
                <label class="form-label">이미지 (선택)</label>
                <input
                  type="file"
                  class="form-control"
                  id="imageInput"
                  multiple
                />
                <div id="uploadedPreview" class="mt-2"></div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary">등록</button>
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 상세 정보 보기 모달 -->
      <div
        class="modal fade"
        id="logDetailModal"
        tabindex="-1"
        aria-hidden="true"
      >
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">상세 정보 보기</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="닫기"
              ></button>
            </div>
            <div class="modal-body" id="logDetailContent">
              <!-- JavaScript로 렌더링 -->
            </div>
          </div>
        </div>
      </div>

      <!-- 같은 위치의 여러 정보 보기 모달 -->
      <div class="modal fade" id="groupDetailModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">같은 위치의 공유 정보들</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <div class="modal-body" id="groupDetailContent"></div>
          </div>
        </div>
      </div>
    </div>
    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/map/free-share.js"></script>
  </body>
</html>
