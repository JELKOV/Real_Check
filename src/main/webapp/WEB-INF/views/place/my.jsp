<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>내 장소 목록</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>

  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 1100px">
      <h3 class="mb-4 text-center">📍 내가 등록한 장소</h3>

      <c:if test="${empty myPlaces}">
        <div class="alert alert-secondary text-center">
          아직 등록된 장소가 없습니다.
        </div>
      </c:if>

      <div class="row g-4">
        <!-- [1] 승인된 장소 -->
        <div class="col-md-4">
          <c:forEach var="place" items="${myPlaces}">
            <c:if test="${place.approved}">
              <div class="card shadow-sm mb-3">
                <div class="card-body">
                  <h5 class="card-title">${place.name}</h5>
                  <p class="card-text text-muted">${place.address}</p>
                  <span class="badge bg-success">승인됨</span>
                  <div class="d-flex justify-content-between mt-2">
                    <a
                      href="/place/place-edit/${place.id}"
                      class="btn btn-outline-primary btn-sm"
                      >✏️ 수정하기</a
                    >
                    <button
                      class="btn btn-outline-danger btn-sm"
                      onclick="deletePlace('${place.id}')"
                    >
                      🗑️ 등록 취소하기
                    </button>
                  </div>
                </div>
              </div>
            </c:if>
          </c:forEach>
        </div>

        <!-- [2] 반려된 장소 -->
        <div class="col-md-4">
          <c:forEach var="place" items="${myPlaces}">
            <c:if test="${place.rejected}">
              <div class="card shadow-sm mb-3 border-danger">
                <div class="card-body">
                  <h5 class="card-title">${place.name}</h5>
                  <p class="card-text text-muted">${place.address}</p>
                  <p class="text-danger fw-semibold small mb-2">
                    🚫 반려 사유: ${place.rejectionReason}
                  </p>
                  <span class="badge bg-danger">반려됨</span>
                  <div class="d-flex justify-content-between mt-2">
                    <a
                      href="/place/place-edit/${place.id}"
                      class="btn btn-outline-primary btn-sm"
                      >🔄 재등록 신청하기</a
                    >
                    <button
                      class="btn btn-outline-danger btn-sm"
                      onclick="deletePlace('${place.id}')"
                    >
                      🗑️ 등록 취소하기
                    </button>
                  </div>
                </div>
              </div>
            </c:if>
          </c:forEach>
        </div>

        <!-- [3] 심사중 장소 -->
        <div class="col-md-4">
          <c:forEach var="place" items="${myPlaces}">
            <c:if test="${!place.approved && !place.rejected}">
              <div class="card shadow-sm mb-3 border-secondary">
                <div class="card-body">
                  <h5 class="card-title">${place.name}</h5>
                  <p class="card-text text-muted">${place.address}</p>
                  <p class="text-secondary small">
                    🕓 현재 심사 중입니다. 관리자 승인까지 기다려주세요.
                  </p>
                  <span class="badge bg-secondary">심사중</span>
                  <div class="d-flex justify-content-end mt-2">
                    <button
                      class="btn btn-outline-danger btn-sm"
                      onclick="deletePlace('${place.id}')"
                    >
                      🗑️ 등록 취소하기
                    </button>
                  </div>
                </div>
              </div>
            </c:if>
          </c:forEach>
        </div>
      
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      function deletePlace(placeId) {
        if (!confirm("정말로 이 장소 등록을 취소하시겠습니까?")) return;
        $.ajax({
          url: "/api/admin/places/" + placeId,
          method: "DELETE",
        })
          .done(() => {
            alert("✅ 장소가 삭제되었습니다.");
            location.reload();
          })
          .fail((xhr) => {
            alert("❌ 삭제 실패: " + xhr.responseText);
          });
      }
    </script>
  </body>
</html>
