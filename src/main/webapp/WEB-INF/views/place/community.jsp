<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title><c:out value="${place.name}" /> 커뮤니티 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>
    <!-- JSTL로 데이터만 숨겨 전달 -->
    <input type="hidden" id="placeName" value="${place.name}" />
    <input type="hidden" id="placeLat" value="${place.lat}" />
    <input type="hidden" id="placeLng" value="${place.lng}" />
    <c:choose>
      <c:when test="${not empty latestLog}">
        <input
          type="hidden"
          id="latestLogContent"
          value="${latestLog.content}"
        />
        <input
          type="hidden"
          id="latestLogTime"
          value="${latestLog.createdAt}"
        />
      </c:when>
      <c:otherwise>
        <input type="hidden" id="latestLogContent" value="" />
        <input type="hidden" id="latestLogTime" value="" />
      </c:otherwise>
    </c:choose>

    <div class="container mt-4">
      <h2><c:out value="${place.name}" /></h2>
      <p class="text-muted"><c:out value="${place.address}" /></p>

      <!-- 장소 지도 -->
      <h5 class="mt-4">📍 장소 위치</h5>
      <div
        id="placeMap"
        style="height: 300px"
        class="mb-4 border rounded"
      ></div>

      <!-- 공식 공지글 -->
      <div class="d-flex justify-content-between align-items-center mt-4 mb-2">
        <h5 class="mb-0">📢 공식 공지글</h5>
        <c:if test="${loginUser != null && loginUser.id == place.ownerId}">
          <a
            href="/status/register?placeId=${place.id}"
            class="btn btn-outline-primary btn-sm"
          >
            <i class="bi bi-plus-circle"></i> 공지 등록
          </a>
        </c:if>
      </div>

      <c:choose>
        <c:when test="${not empty registerLogs}">
          <div class="row gy-3">
            <c:forEach var="log" items="${registerLogs}">
              <div class="col-12">
                <div class="card">
                  <div class="card-body">
                    <h5><c:out value="${log.content}" /></h5>
                    <c:if test="${not empty log.imageUrl}">
                      <img
                        src="<c:out value='${log.imageUrl}' />"
                        class="img-fluid"
                        style="max-height: 150px"
                      />
                    </c:if>
                    <div class="text-muted small mt-2">
                      등록일: <span data-created-at="${log.createdAt}"></span>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>
          </div>
        </c:when>
        <c:otherwise>
          <div class="alert alert-info">등록된 공지글이 없습니다.</div>
        </c:otherwise>
      </c:choose>

      <!-- 해당 장소에 대한 요청 목록 -->
      <h5 class="mt-4">📥 요청 목록</h5>
      <c:if test="${not empty placeRequests}">
        <div class="list-group mb-4">
          <c:forEach var="req" items="${placeRequests}">
            <div
              class="list-group-item d-flex justify-content-between align-items-center"
            >
              <div>
                <strong><c:out value="${req.title}" /></strong>
                <c:if test="${req.closed || req.hasSelectedAnswer}">
                  <span class="badge bg-secondary ms-2">🔒 종료</span>
                </c:if>
                <br />
                <span
                  class="text-muted small"
                  data-created-at="${req.createdAt}"
                ></span>
              </div>
              <div>
                <c:choose>
                  <c:when
                    test="${loginUser != null && !req.closed && !req.hasSelectedAnswer}"
                  >
                    <a href="/request/${req.id}" class="btn btn-primary btn-sm"
                      >응답하러 가기</a
                    >
                  </c:when>
                  <c:otherwise>
                    <a
                      href="/request/${req.id}"
                      class="btn btn-outline-primary btn-sm"
                      >상세보러 가기</a
                    >
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:if>
      <c:if test="${empty placeRequests}">
        <div class="text-muted">요청이 없습니다.</div>
      </c:if>

      <!-- 최근 공유 정보 -->
      <h5 class="mt-5">📡 최근 공유된 정보 (3시간 이내)</h5>
      <c:choose>
        <c:when test="${not empty recentLogs}">
          <div class="row gy-3">
            <c:forEach var="log" items="${recentLogs}">
              <div class="col-12">
                <div class="card shadow-sm rounded-3">
                  <div class="card-body position-relative p-4">
                    <!-- 카테고리 뱃지 (우측 상단) -->
                    <c:if test="${not empty log.category}">
                      <span
                        class="badge bg-info text-dark category-badge position-absolute top-0 end-0 m-3"
                        data-category="${log.category}"
                      >
                        ${log.category}
                      </span>
                    </c:if>

                    <!-- 요청 제목 -->
                    <c:if test="${not empty log.requestTitle}">
                      <div class="mb-2">
                        <span class="text-muted">📝 요청 제목:</span>
                        <span class="fw-semibold">${log.requestTitle}</span>
                      </div>
                    </c:if>

                    <!-- 응답 내용 -->
                    <div class="mb-3">
                      <div class="bg-light rounded p-3 border">
                        <div class="mb-1 fw-semibold text-secondary">
                          💬 응답 내용
                        </div>
                        <div class="fs-6 text-dark">
                          <c:out value="${log.content}" />
                        </div>
                      </div>
                    </div>

                    <!-- 카테고리 상세 요약 -->
                    <c:if test="${not empty log.category}">
                      <div
                        class="text-muted small border-start ps-3 mb-2 category-summary"
                        data-category="${log.category}"
                        data-wait-count="${log.waitCount}"
                        data-has-bathroom="${log.hasBathroom}"
                        data-menu-info="${log.menuInfo}"
                        data-weather-note="${log.weatherNote}"
                        data-vendor-name="${log.vendorName}"
                        data-photo-note="${log.photoNote}"
                        data-noise-note="${log.noiseNote}"
                        data-is-parking-available="${log.isParkingAvailable}"
                        data-is-open="${log.isOpen}"
                        data-seat-count="${log.seatCount}"
                        data-crowd-level="${log.crowdLevel}"
                        data-extra="${log.extra}"
                      ></div>
                    </c:if>

                    <!-- 이미지 -->
                    <c:if test="${not empty log.imageUrl}">
                      <div class="mb-2">
                        <img
                          src="${log.imageUrl}"
                          alt="첨부 이미지"
                          class="img-fluid rounded-3 border ms-1"
                          style="max-height: 180px"
                        />
                      </div>
                    </c:if>

                    <!-- 작성자, 작성일, 상세보기 버튼 -->
                    <div
                      class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top"
                    >
                      <div class="text-muted small">
                        👤 작성자:
                        <span
                          >${log.nickname != null ? log.nickname : "익명"}</span
                        ><br />
                        🕒 작성일:
                        <span data-created-at="${log.createdAt}"></span>
                      </div>
                      <div>
                        <c:choose>
                          <c:when test="${not empty log.requestId}">
                            <a
                              href="/request/${log.requestId}"
                              class="btn btn-outline-primary btn-sm"
                            >
                              상세보러 가기
                            </a>
                          </c:when>
                          <c:otherwise>
                            <button
                              class="btn btn-outline-primary btn-sm"
                              disabled
                            >
                              상세보러 가기
                            </button>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </c:forEach>
          </div>
        </c:when>
        <c:otherwise>
          <div class="alert alert-info mt-2">최근 공유된 정보가 없습니다.</div>
        </c:otherwise>
      </c:choose>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/place/community.js"></script>
  </body>
</html>
