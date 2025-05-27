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
    <link rel="stylesheet" href="/css/request/register.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">요청 등록</h3>

      <!-- 지도 + 폼 2단 구성 -->
      <div class="row">
        <!-- 왼쪽 지도 -->
        <div class="col-md-6 mb-4">
          <div id="mainMap" class="map-container"></div>
        </div>

        <!-- 오른쪽 입력 폼 -->
        <div class="col-md-6">
          <form id="requestForm">
            <!-- 장소 선택 방식 -->
            <div class="mb-3">
              <label class="form-label" for="btnPlace">장소 선택 방식</label>
              <div class="btn-group w-100">
                <button
                  type="button"
                  class="btn btn-primary w-50"
                  id="btnPlace"
                >
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

            <!-- 장소 검색 / 사용자 지정 -->
            <div id="placeSection">
              <!-- 공식 장소 검색 -->
              <div class="mb-3" id="searchSection">
                <label for="placeSearch" class="form-label"
                  >공식 장소 검색</label
                >
                <input
                  type="text"
                  class="form-control"
                  id="placeSearch"
                  placeholder="장소 이름 검색"
                />
                <ul id="placeSearchResults" class="list-group mt-2"></ul>
              </div>

              <!-- 사용자 지정 장소 -->
              <div id="customPlaceSection" style="display: none">
                <label for="customAddress" class="form-label"
                  >사용자 지정 주소</label
                >
                <div class="input-group mb-2">
                  <input
                    type="text"
                    class="form-control"
                    id="customAddress"
                    placeholder="주소를 입력하세요"
                  />
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    id="searchAddressBtn"
                  >
                    주소 검색
                  </button>
                </div>
                <p class="text-muted">
                  지도를 클릭하거나 도로명 주소/지번을 입력하세요.
                </p>
              </div>

              <!-- 선택된 장소 정보 -->
              <div class="mb-3 mt-3">
                <label for="selectedPlaceName" class="form-label"
                  >선택된 장소 정보</label
                >
                <div class="position-relative">
                  <input
                    type="text"
                    class="form-control selected-place-input"
                    id="selectedPlaceName"
                    readonly
                    placeholder="선택된 장소가 없습니다."
                  />
                  <span
                    class="selected-marker"
                    id="selectedMarker"
                    style="display: none"
                    >✔️</span
                  >
                </div>
                <div
                  id="infoSection"
                  class="info-card mt-3"
                  style="display: none"
                >
                  <h5 class="info-title">📌 공식 장소 추가 정보</h5>
                  <div class="info-content">
                    <p>
                      <strong>📍 주소:</strong>
                      <span id="infoAddress">정보 없음</span>
                    </p>
                    <p>
                      <strong>📢 관리자 공지:</strong>
                      <span id="infoRecent">정보 없음</span>
                    </p>
                    <a
                      href="#"
                      target="_blank"
                      id="infoLink"
                      class="btn btn-outline-info w-100 mt-2 d-flex align-items-center justify-content-center gap-2"
                      style="font-weight: 500"
                    >
                      🌐 <span>커뮤니티 페이지</span>
                    </a>
                  </div>
                </div>

                <input type="hidden" id="placeId" name="placeId" />
                <input type="hidden" id="lat" name="lat" />
                <input type="hidden" id="lng" name="lng" />
              </div>
            </div>

            <!-- 나머지 요청 정보 입력 -->
            <div class="mb-3">
              <label for="category" class="form-label">요청 카테고리</label>
              <select
                class="form-select"
                id="category"
                name="category"
                required
              >
                <option value="">카테고리를 선택하세요</option>
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
                name="content"
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
                name="point"
                min="1"
                value="10"
                required
              />
            </div>

            <button type="submit" class="btn btn-primary w-100">
              요청 등록
            </button>
          </form>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/request/register.js"></script>
  </body>
</html>
