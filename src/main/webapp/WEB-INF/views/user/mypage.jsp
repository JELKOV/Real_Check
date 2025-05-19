<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>마이페이지 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/user/mypage.css" />
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 1000px">
      <div class="card shadow-sm p-4">
        <div class="row">
          <!-- [1] 내 정보 카드 -->
          <div class="col-md-6 mb-3">
            <h4 class="mb-3">내 정보</h4>
            <div class="card mb-3 p-3 shadow-sm">
              <div class="info-item mb-2">
                <strong>이메일:</strong>
                <p class="text-muted mb-1">${loginUser.email}</p>
              </div>
              <div class="info-item mb-2">
                <strong>닉네임:</strong>
                <p class="text-muted mb-1">${loginUser.nickname}</p>
              </div>
              <div class="info-item mb-2">
                <strong>상태:</strong>
                <p>
                  <span
                    class="badge ${loginUser.active ? 'bg-success' : 'bg-danger'}"
                  >
                    ${loginUser.active ? '정상' : '차단됨'}
                  </span>
                </p>
              </div>
              <div class="info-item mb-2">
                <strong>가입일:</strong>
                <p class="text-muted mb-1">${loginUser.createdAtFormatted}</p>
              </div>
              <div class="info-item mb-2">
                <strong>최근 로그인:</strong>
                <p class="text-muted mb-1">${loginUser.lastLoginFormatted}</p>
              </div>
              <div class="d-flex gap-2 mt-3">
                <a
                  href="/edit-profile"
                  class="btn btn-primary btn-sm flex-grow-1"
                >
                  <i class="bi bi-pencil"></i> 닉네임 변경
                </a>
                <a
                  href="/change-password"
                  class="btn btn-primary btn-sm flex-grow-1"
                >
                  <i class="bi bi-key"></i> 비밀번호 변경
                </a>

                <button
                  class="btn btn-danger btn-sm flex-grow-1"
                  data-bs-toggle="modal"
                  data-bs-target="#deleteModal"
                >
                  <i class="bi bi-trash"></i> 회원 탈퇴
                </button>
              </div>
            </div>
          </div>

          <!-- [2] 포인트 및 활동 내역 -->
          <div class="col-md-6">
            <h4 class="mb-3">내 포인트</h4>
            <div class="points-box mb-3 text-center">
              <h2 class="text-primary">${loginUser.points} 포인트</h2>
              <p class="text-muted">리얼체크 활동으로 포인트를 획득하세요!</p>
            </div>

            <h4 class="mb-3">최근 활동</h4>
            <ul class="list-group mb-4">
              <c:forEach var="activity" items="${recentActivities}">
                <li class="list-group-item">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <span
                        class="badge ${activity.type == '요청' ? 'bg-info' : 'bg-primary'}"
                      >
                        ${activity.type}
                      </span>
                      <span class="fw-bold"
                        >${activity.title != null ? activity.title :
                        activity.requestTitle}</span
                      >
                      <br />
                      <small class="text-muted"
                        >장소: ${activity.placeName}</small
                      >
                    </div>
                    <small class="text-muted">${activity.createdAt}</small>
                  </div>
                </li>
              </c:forEach>

              <c:if test="${empty recentActivities}">
                <li class="list-group-item text-center text-muted">
                  최근 활동 내역이 없습니다.
                </li>
              </c:if>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 회원 탈퇴 모달 -->
    <div
      class="modal fade"
      id="deleteModal"
      tabindex="-1"
      aria-labelledby="deleteModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered modal-md">
        <div class="modal-content rounded-3 shadow-md">
          <div class="modal-header border-0">
            <h5 class="modal-title fw-bold" id="deleteModalLabel">
              회원 탈퇴 요청
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body text-center">
            <p class="mb-3 text-content">
              회원 탈퇴 요청을 하시면, <strong>7일 뒤</strong>에 모든 정보가
              자동 삭제됩니다.<br />
              <span class="d-block mt-2"
                >이 기간 동안 로그인하여 <strong>탈퇴 요청을 취소</strong>할 수
                있습니다.</span
              >
              <span class="d-block mt-2">계속하시겠습니까?</span>
            </p>
          </div>
          <div
            class="modal-footer border-0 d-flex justify-content-center gap-2"
          >
            <button
              type="button"
              class="btn btn-light px-4"
              data-bs-dismiss="modal"
            >
              취소
            </button>
            <a href="/delete-account" class="btn btn-danger px-4">탈퇴 요청</a>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
