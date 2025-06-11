<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>장소 수정</title>
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
      <h3 class="mb-4 text-center">✏️ 장소 수정</h3>
      <div class="row">
        <!-- 지도 영역 -->
        <div class="col-md-6 mb-4">
          <div id="map" style="width: 100%; height: 400px"></div>
        </div>

        <!-- 수정 폼 -->
        <div class="col-md-6">
          <form id="placeForm" class="card p-4 shadow-sm">
            <div class="mb-3">
              <label class="form-label">장소 이름</label>
              <input
                type="text"
                name="name"
                class="form-control"
                value="${place.name}"
                required
              />
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
                value="${place.address}"
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
                  value="${place.lat}"
                />
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">경도</label>
                <input
                  type="text"
                  name="lng"
                  id="lng"
                  class="form-control"
                  value="${place.lng}"
                />
              </div>
            </div>

            <!-- 카테고리 선택 -->
            <div class="mb-3">
              <label class="form-label">허용 요청 카테고리</label>
              <div
                class="d-flex justify-content-between align-items-center mb-2"
              >
                <span class="text-muted small">* 요청 유형을 선택해주세요</span>
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
                      <input class="form-check-input" type="checkbox"
                      name="categories" value="${category.name()}"
                      id="cat-${category.name()}" ${place.allowedRequestTypes !=
                      null &&
                      place.allowedRequestTypes.contains(category.name()) ?
                      'checked="checked"' : ''} />
                      <label
                        class="form-check-label category-label"
                        for="cat-${category.name()}"
                        data-category="${category.name()}"
                      ></label>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>

            <div class="text-end">
              <c:choose>
                <c:when test="${place.rejected}">
                  <button type="submit" class="btn btn-danger">
                    재등록 신청
                  </button>
                </c:when>
                <c:otherwise>
                  <button type="submit" class="btn btn-primary">
                    수정 완료
                  </button>
                </c:otherwise>
              </c:choose>
            </div>
          </form>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      const placeId = "${place.id}";
      const isRejected = "${place.rejected}" === "true";
    </script>
    <script src="/js/place/place-edit.js"></script>
  </body>
</html>
