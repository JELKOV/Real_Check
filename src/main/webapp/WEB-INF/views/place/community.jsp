<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
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
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-4">
      <h2><c:out value="${place.name}" /></h2>
      <p class="text-muted"><c:out value="${place.address}" /></p>

      <!-- 장소 소유자의 공지 등록 버튼 -->
      <c:if test="${loginUser != null && loginUser.id == place.ownerId}">
        <div class="mb-3">
          <a
            href="/status/register?placeId=${place.id}"
            class="btn btn-success btn-sm"
          >
            <i class="bi bi-plus-circle"></i> 공지 등록
          </a>
        </div>
      </c:if>

      <!-- 최신 상태 한 줄 표시 (선택적) -->
      <c:if test="${not empty latestLog}">
        <div class="alert alert-primary">
          <strong>🚨 최신 정보:</strong> <c:out value="${latestLog.content}" />
          <span class="text-muted small">
            (<fmt:formatDate
              value="${latestLog.createdAt}"
              pattern="yyyy-MM-dd HH:mm"
            />)
          </span>
        </div>
      </c:if>

      <!-- 📢 공식 공지글 -->
      <h5 class="mt-4">📢 공식 공지글</h5>
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
                      등록일:
                      <fmt:formatDate
                        value="${log.createdAt}"
                        pattern="yyyy-MM-dd HH:mm"
                      />
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

      <!-- 📥 해당 장소에 대한 요청 목록 + 응답 -->
      <h5 class="mt-4">📥 해당 장소에 대한 요청 목록</h5>
      <c:if test="${not empty placeRequests}">
        <div class="list-group mb-4">
          <c:forEach var="req" items="${placeRequests}">
            <div
              class="list-group-item d-flex justify-content-between align-items-center"
            >
              <div>
                <strong><c:out value="${req.title}" /></strong><br />
                <span class="text-muted small">
                  <fmt:formatDate
                    value="${req.createdAt}"
                    pattern="yyyy-MM-dd HH:mm"
                  />
                </span>
              </div>
              <div>
                <a
                  href="/request/${req.id}"
                  class="btn btn-outline-primary btn-sm me-1"
                  >상세보기</a
                >
                <c:if test="${loginUser != null}">
                  <a
                    href="/status/answer?requestId=${req.id}"
                    class="btn btn-primary btn-sm"
                    >응답하기</a
                  >
                </c:if>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:if>
      <c:if test="${empty placeRequests}">
        <div class="text-muted">해당 장소에 대한 요청이 없습니다.</div>
      </c:if>

      <!-- 📡 최근 3시간 실시간 공유 -->
      <h5 class="mt-5">📡 최근 공유된 정보 (3시간 이내)</h5>
      <c:choose>
        <c:when test="${not empty recentLogs}">
          <ul class="list-group">
            <c:forEach var="log" items="${recentLogs}">
              <li class="list-group-item d-flex justify-content-between">
                <span><c:out value="${log.content}" /></span>
                <small class="text-muted">
                  <fmt:formatDate
                    value="${log.createdAt}"
                    pattern="yyyy-MM-dd HH:mm"
                  />
                </small>
              </li>
            </c:forEach>
          </ul>
        </c:when>
        <c:otherwise>
          <div class="text-muted">최근 공유된 정보가 없습니다.</div>
        </c:otherwise>
      </c:choose>
    </div>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
