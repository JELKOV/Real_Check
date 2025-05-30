<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>RealCheck - 세상의 궁금증을 대신 해결해드립니다</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>

  <body>
    <%@ include file="common/header.jsp" %>

    <div class="container mt-5">
      <!-- 회원 탈퇴 성공 메시지 (Flash Attribute) -->
      <c:if test="${deletionRequested}">
        <div class="alert alert-success text-center mb-4">
          회원 탈퇴 요청이 완료되었습니다. 7일 뒤에 자동 삭제됩니다.<br />
          로그인하여 탈퇴 요청을 취소할 수 있습니다.
        </div>
      </c:if>

      <h2 class="text-center mb-5">실시간 현장 정보 요청/답변 서비스</h2>

      <div class="row g-4">
        <!-- 1. 요청 등록하기 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">요청 등록</h5>
              <p class="card-text flex-grow-1">
                지도에서 궁금한 장소를 선택하고, 요청을 등록하세요.<br />
                포인트를 걸고 빠르게 답변을 받아보세요.
              </p>
              <a
                href="/request/register"
                class="btn btn-outline-primary mt-auto"
                >요청 등록하기</a
              >
            </div>
          </div>
        </div>

        <!-- 2. 요청 답변하기 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">요청 답변하기</h5>
              <p class="card-text flex-grow-1">
                근처에서 답하지 못한 요청을 확인하고 답변을 등록하세요.<br />
                추가 포인트를 드립니다. 정확한 답변으로 포인트를 획득하세요.
              </p>
              <a href="/request/list" class="btn btn-outline-primary mt-auto"
                >요청 확인하기</a
              >
            </div>
          </div>
        </div>

        <!-- 2.5. 지도에서 요청 보기 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">지도에서 요청 확인</h5>
              <p class="card-text flex-grow-1">
                지도를 통해 요청 위치를 한눈에 확인하세요.<br />
                근처 요청을 직접 보고 응답할 수 있어요.
              </p>
              <a
                href="/nearby/request-list"
                class="btn btn-outline-info mt-auto"
                >지도 요청 보기</a
              >
            </div>
          </div>
        </div>

        <!-- 3. 주변 현황(정보) 탐색하기 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">내 주변 정보 탐색</h5>
              <p class="card-text flex-grow-1">
                내 주변에 등록된 정보를 지도에서 탐색하세요.<br />
                다양한 질문과 답변을 확인할 수 있습니다.
              </p>
              <a href="/nearby" class="btn btn-outline-info mt-auto"
                >주변 정보 보기</a
              >
            </div>
          </div>
        </div>

        <!-- 4. 내 요청/답변 관리 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">내 활동 관리</h5>
              <p class="card-text flex-grow-1">
                내가 등록한 요청과 답변을 확인하고<br />
                포인트 내역을 관리하세요.
              </p>
              <a href="/my-logs" class="btn btn-outline-secondary mt-auto"
                >내 정보 관리</a
              >
            </div>
          </div>
        </div>

        <!-- 5. 공식 장소 검색 -->
        <div class="col-md-3">
          <div class="card shadow-sm h-100">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title">공식 장소 검색</h5>
              <p class="card-text flex-grow-1">
                인증된 장소를 검색하여 커뮤니티 페이지를 확인하세요.<br />
                공지와 답변을 한눈에 볼 수 있습니다.
              </p>
              <a href="/place/search" class="btn btn-outline-success mt-auto">
                장소 검색하기
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="common/footer.jsp" %>
  </body>
</html>
