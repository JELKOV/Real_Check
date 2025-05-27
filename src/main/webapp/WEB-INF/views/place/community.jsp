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
      <h2><c:out value="${place.name}" /></h2>
      <p class="text-muted"><c:out value="${place.address}" /></p>

      <!-- 장소 지도 -->
      <h5 class="mt-4">📍 장소 위치</h5>
      <div
        id="placeMap"
        style="height: 300px"
        class="mb-4 border rounded"
      ></div>

      <c:if test="${not empty latestLog}">
        <div class="alert alert-info mt-3">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="fw-bold">📢 최근 공지</div>
              <div class="mt-1">${latestLog.content}</div>

              <!-- 카테고리 요약 정보 -->
              <c:if test="${not empty latestLog.category}">
                <div
                  class="mt-2 small text-muted category-summary"
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
                class="carousel slide ms-3"
                style="width: 180px; max-height: 200px"
                data-bs-ride="carousel"
              >
                <div
                  class="carousel-inner border rounded"
                  style="max-height: 200px; overflow: hidden"
                >
                  <c:forEach
                    var="img"
                    items="${latestLog.imageUrls}"
                    varStatus="status"
                  >
                    <div class="carousel-item ${status.first ? 'active' : ''}">
                      <img
                        src="${img}"
                        class="d-block w-100 log-image"
                        style="object-fit: contain; height: 200px"
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
          <div class="text-muted small mt-2">
            🕒 <span data-created-at="${latestLog.createdAt}"></span>
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
                <div class="card">
                  <div class="card-body">
                    <h5><c:out value="${log.content}" /></h5>

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

                    <c:if test="${not empty log.imageUrls}">
                      <div
                        id="carousel-${log.id}"
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
                                alt="공지 이미지"
                                data-bs-toggle="modal"
                                data-bs-target="#imageModal"
                                data-img="${img}"
                                style="object-fit: contain; height: 250px"
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

                    <div class="text-muted small mt-2">
                      등록일: <span data-created-at="${log.createdAt}"></span>
                    </div>

                    <c:if
                      test="${loginUser != null && loginUser.id == log.userId}"
                    >
                      <div class="d-flex justify-content-end gap-2 mt-3">
                        <a
                          href="/status/edit?logId=${log.id}"
                          class="btn btn-sm btn-outline-secondary"
                        >
                          ✏️ 수정
                        </a>
                        <button
                          type="button"
                          class="btn btn-sm btn-outline-danger"
                          onclick="confirmDelete('${log.id}', '${place.id}')"
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
                <div class="card shadow-sm rounded-3 position-relative">
                  <div class="card-body p-4">
                    <!-- 응답 내용 블록 -->
                    <div class="mb-3">
                      <div
                        class="bg-light rounded p-3 border position-relative"
                      >
                        <div
                          class="d-flex justify-content-between align-items-start"
                        >
                          <div class="mb-1 fw-semibold text-secondary">
                            💬 응답 내용
                          </div>

                          <!-- 카테고리 뱃지 (응답 블록 내부 오른쪽 상단) -->
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
                    </div>

                    <!-- 이미지 (있는 경우) -->
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
                                style="object-fit: contain; height: 250px"
                              />
                            </div>
                          </c:forEach>
                        </div>

                        <!-- 좌우 이동 버튼 (ID 맞춰야 함) -->
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

                    <!-- 하단: 작성자 / 날짜 / 상세보기 -->
                    <div
                      class="d-flex justify-content-between align-items-center pt-3 border-top"
                    >
                      <div class="text-muted small">
                        👤
                        <span
                          >${log.nickname != null ? log.nickname : "익명"}</span
                        ><br /> 🕒
                        <span data-created-at="${log.createdAt}"></span>
                      </div>
                      <div>
                        <c:choose>
                          <c:when test="${not empty log.requestId}">
                            <!-- 요청 연결된 응답 -->
                            <a
                              href="/request/${log.requestId}"
                              class="btn btn-outline-primary btn-sm"
                            >
                              상세보러 가기
                            </a>
                          </c:when>
                          <c:when test="${log.type == 'REGISTER'}">
                            <!-- 공지글인 경우 -->
                            <span class="badge bg-success">📢 공지</span>
                          </c:when>
                          <c:when test="${log.type == 'FREE_SHARE'}">
                            <!-- 자발 공유인 경우 -->
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
    <script src="/js/place/community.js"></script>
  </body>
</html>
