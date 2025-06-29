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
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5 px-4">
      <div class="card shadow-sm p-4" style="max-width: 1200px; margin: 0 auto">
        <div class="row">
          <!-- [1] 내 정보 -->
          <div class="col-md-6 mb-4">
            <h4 class="mb-3">🙋‍♂️ 내 정보</h4>
            <div class="card p-3 shadow-sm">
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
                  class="btn btn-outline-primary btn-sm flex-grow-1"
                  >닉네임 변경</a
                >
                <a
                  href="/change-password"
                  class="btn btn-outline-secondary btn-sm flex-grow-1"
                  >비밀번호 변경</a
                >
                <button
                  class="btn btn-danger btn-sm flex-grow-1"
                  data-bs-toggle="modal"
                  data-bs-target="#deleteModal"
                >
                  회원 탈퇴
                </button>
              </div>
            </div>
          </div>

          <!-- 오른쪽 열 전체: 장소 관리 + 포인트 관리 -->
          <div class="col-md-6 mb-4">
            <!-- 장소 관리 -->
            <h4 class="mb-3">📍 장소 관리</h4>
            <div class="card p-3 shadow-sm mb-4">
              <p class="mb-2 text-muted small">
                내가 등록한 장소와 즐겨찾는 장소를 관리할 수 있습니다.
              </p>
              <div class="d-flex gap-2">
                <a
                  href="/place/my"
                  class="btn btn-outline-primary btn-sm flex-grow-1"
                  >내 소유 장소</a
                >
                <a
                  href="/my-favorites"
                  class="btn btn-outline-secondary btn-sm flex-grow-1"
                  >즐겨찾기 장소</a
                >
              </div>
            </div>

            <!-- 포인트 관리 -->
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h4 class="mb-0">🪙 포인트 관리</h4>
              <span class="text-muted small"
                >현재 보유:
                <strong class="text-primary"
                  >${loginUser.points}pt</strong
                ></span
              >
            </div>
            <div class="card p-3 shadow-sm">
              <p class="mb-2 text-muted small">
                부족한 포인트를 충전하거나, 포인트를 현금으로 환급해드립니다.
              </p>
              <div class="d-flex gap-2">
                <a
                  href="/point/charge"
                  class="btn btn-outline-primary btn-sm flex-grow-1"
                  >🔋 포인트 충전</a
                >
                <a
                  href="/point/cash"
                  class="btn btn-outline-secondary btn-sm flex-grow-1"
                  >💰 포인트 환급</a
                >
              </div>
            </div>
          </div>

          <div class="col-12">
            <!-- [4] 활동/포인트 탭 버튼 -->
            <ul class="nav nav-tabs mb-3" id="mypageTab" role="tablist">
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link active"
                  id="activity-tab"
                  data-bs-toggle="tab"
                  data-bs-target="#activitySection"
                  type="button"
                  role="tab"
                >
                  💬 최근 활동
                </button>
              </li>
              <li class="nav-item" role="presentation">
                <button
                  class="nav-link"
                  id="point-tab"
                  data-bs-toggle="tab"
                  data-bs-target="#pointSection"
                  type="button"
                  role="tab"
                >
                  📌 포인트 내역
                </button>
              </li>
            </ul>

            <!-- [4-1] 탭 콘텐츠 영역 -->
            <div class="tab-content">
              <!-- 최근 활동 -->
              <div
                class="tab-pane fade show active"
                id="activitySection"
                role="tabpanel"
              >
                <ul class="list-group mb-3">
                  <c:forEach var="activity" items="${recentActivities}">
                    <li class="list-group-item d-flex justify-content-between">
                      <div>
                        <span
                          class="badge ${activity.type == '요청' ? 'bg-info' : 'bg-primary'}"
                        >
                          ${activity.type}
                        </span>
                        <span class="fw-bold">
                          ${activity.title != null ? activity.title :
                          activity.requestTitle} </span
                        ><br />
                        <small class="text-muted"
                          >장소: ${activity.placeName}</small
                        >
                      </div>
                      <small class="text-muted">${activity.createdAt}</small>
                    </li>
                  </c:forEach>
                  <c:if test="${empty recentActivities}">
                    <li class="list-group-item text-center text-muted">
                      최근 활동 내역이 없습니다.
                    </li>
                  </c:if>
                </ul>
              </div>

              <!-- 포인트 내역 -->
              <div class="tab-pane fade" id="pointSection" role="tabpanel">
                <div class="card p-3 shadow-sm">
                  <h5 class="text-primary">${loginUser.points} 포인트</h5>
                  <p class="text-muted small">
                    리얼체크 활동을 통해 적립된 포인트입니다.
                  </p>
                  <div id="pointList">불러오는 중...</div>
                  <!-- 페이징 버튼 출력 영역 -->
                  <div
                    id="pointPagination"
                    class="mt-3 d-flex justify-content-center gap-1"
                  ></div>
                </div>
              </div>
            </div>
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
    <script src="/js/user/mypage.js"></script>
  </body>
</html>
