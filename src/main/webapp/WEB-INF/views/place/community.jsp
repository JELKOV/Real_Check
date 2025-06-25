<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fn"
uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <link rel="stylesheet" href="/css/place/community.css" />
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
      <div class="mb-4">
        <h2 class="fw-bold display-6"><c:out value="${place.name}" /></h2>
        <p class="text-muted small">
          <i class="bi bi-geo-alt"></i> <c:out value="${place.address}" />
        </p>
      </div>

      <!-- 장소 지도 -->
      <h5 class="mt-4">📍 장소 위치</h5>
      <div
        id="placeMap"
        style="height: 300px"
        class="mb-4 border rounded"
      ></div>

      <c:if test="${not empty latestLog}">
        <div class="card border-info shadow-sm mb-4">
          <div
            class="card-body d-flex flex-column flex-md-row justify-content-between align-items-start gap-3"
          >
            <div class="flex-grow-1">
              <div class="d-flex align-items-center mb-2">
                <span class="badge bg-info text-dark me-2">📢 최근 공지</span>
                <small
                  class="text-muted"
                  data-created-at="${latestLog.createdAt}"
                ></small>
              </div>

              <p class="fs-6 mb-2"><c:out value="${latestLog.content}" /></p>

              <!-- 카테고리 요약 정보 -->
              <c:if test="${not empty latestLog.category}">
                <div
                  class="small text-muted category-summary"
                  data-category="${latestLog.category}"
                  data-wait-count="${latestLog.waitCount}"
                  data-has-bathroom="${latestLog.hasBathroom}"
                  data-menu-info="${latestLog.menuInfo}"
                  data-weather-note="${latestLog.weatherNote}"
                  data-vendor-name="${latestLog.vendorName}"
                  data-photo-note="${latestLog.photoNote}"
                  data-noise-note="${latestLog.noiseNote}"
                  data-is-parking-available="${latestLog.isParkingAvailable}"
                  data-is-open="${latestLog.isOpen}"
                  data-seat-count="${latestLog.seatCount}"
                  data-crowd-level="${latestLog.crowdLevel}"
                  data-extra="${latestLog.extra}"
                ></div>
              </c:if>
            </div>

            <!-- 캐러셀 (있을 경우) -->
            <c:if test="${not empty latestLog.imageUrls}">
              <div
                id="carousel-latest-log"
                class="carousel slide"
                style="width: 200px"
                data-bs-ride="carousel"
              >
                <div class="carousel-inner rounded border">
                  <c:forEach
                    var="img"
                    items="${latestLog.imageUrls}"
                    varStatus="status"
                  >
                    <div class="carousel-item ${status.first ? 'active' : ''}">
                      <img
                        src="${img}"
                        class="d-block w-100 log-image"
                        style="
                          object-fit: contain;
                          height: 250px;
                          background-color: #f8f9fa;
                        "
                        data-bs-toggle="modal"
                        data-bs-target="#imageModal"
                        data-img="${img}"
                      />
                    </div>
                  </c:forEach>
                </div>

                <c:if test="${fn:length(latestLog.imageUrls) > 1}">
                  <button
                    class="carousel-control-prev"
                    type="button"
                    data-bs-target="#carousel-latest-log"
                    data-bs-slide="prev"
                  >
                    <span class="carousel-control-prev-icon"></span>
                  </button>
                  <button
                    class="carousel-control-next"
                    type="button"
                    data-bs-target="#carousel-latest-log"
                    data-bs-slide="next"
                  >
                    <span class="carousel-control-next-icon"></span>
                  </button>
                </c:if>
              </div>
            </c:if>
          </div>
        </div>
      </c:if>

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
        <c:when test="${not empty pagedNotices.content}">
          <div class="row gy-3">
            <c:forEach var="log" items="${pagedNotices.content}">
              <div class="col-12">
                <div class="card notice-card shadow-sm rounded-3">
                  <div class="card-body">
                    <!-- 공지 내용 -->
                    <h6 class="fw-semibold mb-2">
                      <c:out value="${log.content}" />
                    </h6>

                    <!-- 카테고리 요약 -->
                    <c:if test="${not empty log.category}">
                      <div
                        class="small text-muted category-summary mb-2"
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

                    <!-- 이미지 캐러셀 -->
                    <c:if test="${not empty log.imageUrls}">
                      <div
                        id="carousel-${log.id}"
                        class="carousel slide my-3"
                        data-bs-ride="carousel"
                      >
                        <div
                          class="carousel-inner rounded border"
                          style="max-height: 250px; overflow: hidden"
                        >
                          <c:forEach
                            var="img"
                            items="${log.imageUrls}"
                            varStatus="status"
                          >
                            <div
                              class="carousel-item ${status.first ? 'active' : ''}"
                            >
                              <img
                                src="${img}"
                                class="d-block w-100 log-image"
                                style="
                                  object-fit: contain;
                                  height: 250px;
                                  background-color: #f8f9fa;
                                "
                                data-bs-toggle="modal"
                                data-bs-target="#imageModal"
                                data-img="${img}"
                              />
                            </div>
                          </c:forEach>
                        </div>
                        <c:if test="${fn:length(log.imageUrls) > 1}">
                          <button
                            class="carousel-control-prev"
                            type="button"
                            data-bs-target="#carousel-${log.id}"
                            data-bs-slide="prev"
                          >
                            <span class="carousel-control-prev-icon"></span>
                          </button>
                          <button
                            class="carousel-control-next"
                            type="button"
                            data-bs-target="#carousel-${log.id}"
                            data-bs-slide="next"
                          >
                            <span class="carousel-control-next-icon"></span>
                          </button>
                        </c:if>
                      </div>
                    </c:if>

                    <!-- 하단 메타 정보 -->
                    <div class="text-muted small mt-2">
                      🕒 <span data-created-at="${log.createdAt}"></span>
                    </div>

                    <!-- 수정/삭제 버튼 -->
                    <c:if
                      test="${loginUser != null && loginUser.id == log.userId}"
                    >
                      <div class="d-flex justify-content-end gap-2 mt-3">
                        <a
                          href="/status/edit?logId=${log.id}"
                          class="btn btn-sm btn-outline-secondary"
                          >✏️ 수정</a
                        >
                        <button
                          type="button"
                          class="btn btn-sm btn-outline-danger delete-log-btn"
                          data-log-id="${log.id}"
                          data-place-id="${place.id}"
                        >
                          🗑️ 삭제
                        </button>
                      </div>
                    </c:if>
                  </div>
                </div>
              </div>
            </c:forEach>
          </div>

          <!-- 페이지네이션 버튼 -->
          <div class="mt-3 text-center">
            <c:forEach var="i" begin="1" end="${pagedNotices.totalPages}">
              <a
                href="?page=${i}"
                class="btn btn-sm ${pagedNotices.currentPage == i ? 'btn-primary' : 'btn-outline-primary'} mx-1"
              >
                ${i}
              </a>
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
        <div class="list-group mb-4 shadow-sm rounded overflow-hidden">
          <c:forEach var="req" items="${placeRequests}">
            <div class="list-group-item py-3">
              <div
                class="d-flex justify-content-between align-items-center flex-column flex-md-row gap-2"
              >
                <!-- 왼쪽: 제목 + 상태 + 시간 -->
                <div class="flex-grow-1">
                  <div class="fw-semibold fs-6 mb-1">
                    <c:out value="${req.title}" />
                    <c:if test="${req.closed || req.hasSelectedAnswer}">
                      <span class="badge bg-secondary ms-2">🔒 종료</span>
                    </c:if>
                  </div>
                  <div class="text-muted small">
                    🕒 <span data-created-at="${req.createdAt}"></span>
                  </div>
                </div>

                <!-- 오른쪽 버튼 -->
                <div>
                  <c:choose>
                    <c:when
                      test="${loginUser != null && !req.closed && !req.hasSelectedAnswer}"
                    >
                      <a
                        href="/request/${req.id}"
                        class="btn btn-sm btn-primary"
                      >
                        응답하러 가기
                      </a>
                    </c:when>
                    <c:otherwise>
                      <a
                        href="/request/${req.id}"
                        class="btn btn-sm btn-outline-primary"
                      >
                        상세보기
                      </a>
                    </c:otherwise>
                  </c:choose>
                </div>
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
                <div class="card shared-log-card shadow-sm rounded-3">
                  <div class="card-body p-4">
                    <!-- 응답 내용 -->
                    <div class="mb-3">
                      <div
                        class="d-flex justify-content-between align-items-start mb-2"
                      >
                        <span class="fw-semibold text-secondary"
                          >💬 응답 내용</span
                        >

                        <c:if test="${not empty log.category}">
                          <span
                            class="badge bg-info text-dark category-badge ms-2"
                            data-category="${log.category}"
                          >
                            ${log.category}
                          </span>
                        </c:if>
                      </div>

                      <div class="fs-6 text-dark">
                        <c:out value="${log.content}" />
                      </div>

                      <!-- 카테고리 상세 요약 -->
                      <c:if test="${not empty log.category}">
                        <div
                          class="mt-2 small text-muted category-summary"
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
                    </div>

                    <!-- 이미지 캐러셀 -->
                    <c:if test="${not empty log.imageUrls}">
                      <div
                        id="carousel-recent-${log.id}"
                        class="carousel slide mb-3"
                        data-bs-ride="carousel"
                      >
                        <div
                          class="carousel-inner rounded border"
                          style="max-height: 250px; overflow: hidden"
                        >
                          <c:forEach
                            var="img"
                            items="${log.imageUrls}"
                            varStatus="status"
                          >
                            <div
                              class="carousel-item ${status.first ? 'active' : ''}"
                            >
                              <img
                                src="${img}"
                                class="d-block w-100 log-image"
                                alt="첨부 이미지"
                                data-bs-toggle="modal"
                                data-bs-target="#imageModal"
                                data-img="${img}"
                                style="
                                  object-fit: contain;
                                  height: 250px;
                                  background-color: #f8f9fa;
                                "
                              />
                            </div>
                          </c:forEach>
                        </div>
                        <c:if test="${fn:length(log.imageUrls) > 1}">
                          <button
                            class="carousel-control-prev"
                            type="button"
                            data-bs-target="#carousel-recent-${log.id}"
                            data-bs-slide="prev"
                          >
                            <span class="carousel-control-prev-icon"></span>
                          </button>
                          <button
                            class="carousel-control-next"
                            type="button"
                            data-bs-target="#carousel-recent-${log.id}"
                            data-bs-slide="next"
                          >
                            <span class="carousel-control-next-icon"></span>
                          </button>
                        </c:if>
                      </div>
                    </c:if>
                    <!-- 작성자/날짜/버튼 -->
                    <div
                      class="d-flex justify-content-between align-items-center pt-3 border-top"
                    >
                      <div class="text-muted small">
                        👤
                        <c:out
                          value="${log.nickname != null ? log.nickname : '익명'}"
                        /><br /> 🕒
                        <span data-created-at="${log.createdAt}"></span>
                      </div>

                      <div>
                        <c:choose>
                          <c:when test="${not empty log.requestId}">
                            <a
                              href="/request/${log.requestId}"
                              class="btn btn-outline-primary btn-sm"
                            >
                              상세보기
                            </a>
                          </c:when>
                          <c:when test="${log.type == 'REGISTER'}">
                            <span class="badge bg-success">📢 공지</span>
                          </c:when>
                          <c:when test="${log.type == 'FREE_SHARE'}">
                            <span class="badge bg-secondary"
                              >📡 자발적 공유</span
                            >
                          </c:when>
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

    <!-- 이미지 모달 -->
    <div class="modal fade" id="imageModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-body p-0">
            <img id="modalImage" src="" class="img-fluid w-100" />
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script type="module" src="/js/place/community.js"></script>
  </body>
</html>
