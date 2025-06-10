<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>장소 등록</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}&submodules=geocoder"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="mb-4 text-center">장소 등록</h3>
      <div class="row">
        <div class="col-md-6 mb-4">
          <div id="map" style="width: 100%; height: 400px"></div>
        </div>

        <div class="col-md-6">
          <form id="placeForm" class="card p-4 shadow-sm">
            <div class="mb-3">
              <label class="form-label">장소 이름</label>
              <input type="text" name="name" class="form-control" required />
            </div>

            <div class="mb-3">
              <label class="form-label">주소 검색</label>
              <div class="input-group">
                <input
                  type="text"
                  id="addressInput"
                  class="form-control"
                  placeholder="도로명 또는 지번 주소 입력"
                />
                <button
                  type="button"
                  id="searchAddressBtn"
                  class="btn btn-outline-secondary"
                >
                  주소 검색
                </button>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">주소</label>
              <input
                type="text"
                name="address"
                id="address"
                class="form-control"
                readonly
              />
            </div>

            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">위도</label>
                <input
                  type="text"
                  name="lat"
                  id="lat"
                  class="form-control"
                  readonly
                />
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">경도</label>
                <input
                  type="text"
                  name="lng"
                  id="lng"
                  class="form-control"
                  readonly
                />
              </div>
            </div>

            <!-- 카테고리 선택 영역 개선 -->
            <div class="mb-3">
              <label class="form-label">허용 요청 카테고리</label>

              <div
                class="d-flex justify-content-between align-items-center mb-2"
              >
                <span class="text-muted small"
                  >* 필요한 요청 유형을 선택해주세요</span
                >
                <div class="btn-group btn-group-sm">
                  <button
                    type="button"
                    id="selectAllCategoriesBtn"
                    class="btn btn-outline-secondary"
                  >
                    전체 선택
                  </button>
                  <button
                    type="button"
                    id="deselectAllCategoriesBtn"
                    class="btn btn-outline-secondary"
                  >
                    전체 취소
                  </button>
                </div>
              </div>

              <div class="row row-cols-2 row-cols-md-3 g-2">
                <c:forEach var="category" items="${requestCategories}">
                  <div class="col">
                    <div class="form-check">
                      <input
                        class="form-check-input"
                        type="checkbox"
                        name="categories"
                        value="${category.name()}"
                        id="cat-${category.name()}"
                      />
                      <label
                        class="form-check-label category-label"
                        for="cat-${category.name()}"
                        data-category="${category.name()}"
                      >
                      </label>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>

            <div class="text-end">
              <button type="submit" class="btn btn-primary">장소 등록</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script src="/js/place/place-register.js"></script>
  </body>
</html>
