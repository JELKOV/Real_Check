<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>RealCheck - 실시간 정보 요청/응답 플랫폼</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/index.css" />
    <link href="https://unpkg.com/aos@2.3.4/dist/aos.css" rel="stylesheet" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>

  <body>
    <%@ include file="common/header.jsp" %>

    <div class="container mt-4">
      <c:if test="${deletionRequested}">
        <div class="alert alert-success text-center mb-4">
          회원 탈퇴 요청이 완료되었습니다. 7일 뒤에 자동 삭제됩니다.<br />
          로그인하여 탈퇴 요청을 취소할 수 있습니다.
        </div>
      </c:if>
    </div>

    <!-- 섹션 1 -->
    <section class="py-5 bg-white">
      <div class="container" data-aos="fade-up">
        <h2 class="text-center emoji-title">🗨️</h2>
        <h3 class="text-center section-title">실시간 요청/답변 & 내 답변</h3>
        <p class="lead text-center mb-5">
          원하는 장소에 궁금한 질문을 등록하고 빠르게 답변을 받아보세요.<br />내가
          한 답변도 확인할 수 있습니다.
        </p>

        <div class="row g-4">
          <div class="col-lg-4 col-md-6 col-12">
            <div
              class="p-4 bg-light rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">📌</div>
              <h5 class="fw-bold">요청 등록</h5>
              <p class="text-muted flex-grow-1">
                궁금한 장소 정보를 요청해보세요. 포인트를 걸면 더 빠르게 답변
                받을 수 있어요.
              </p>
              <div class="mt-auto">
                <a href="/request/register" class="btn btn-primary btn-sm"
                  >요청 등록하기</a
                >
              </div>
            </div>
          </div>

          <div class="col-lg-4 col-md-6 col-12">
            <div
              class="p-4 bg-light rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">✅</div>
              <h5 class="fw-bold">요청 답변</h5>
              <p class="text-muted flex-grow-1">
                다른 유저의 요청에 답변하고 보상을 받아보세요.
              </p>
              <div class="mt-auto">
                <a href="/request/list" class="btn btn-success btn-sm"
                  >요청 답변하기</a
                >
              </div>
            </div>
          </div>

          <div class="col-lg-4 col-md-12 col-12">
            <div
              class="p-4 bg-light rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">👤</div>
              <h5 class="fw-bold">내 답변 내역</h5>
              <p class="text-muted flex-grow-1">
                내가 등록한 답변 관련된 내역을 확인할 수 있어요.
              </p>
              <div class="mt-auto">
                <a href="/my-logs" class="btn btn-secondary btn-sm"
                  >내 답변 내역</a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 섹션 2 -->
    <section class="py-5 bg-light">
      <div class="container" data-aos="fade-up">
        <h2 class="text-center emoji-title">🗺️</h2>
        <h3 class="text-center section-title">지도에서 요청/정보 탐색</h3>
        <p class="lead text-center mb-5">
          지도 기반으로 요청을 확인하고, 자발적인 정보도 공유해보세요.
        </p>

        <div class="row g-4">
          <div class="col-md-4 col-sm-6 col-12">
            <div
              class="p-4 bg-white rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">🗺️</div>
              <h5 class="fw-bold">주변 요청 보기</h5>
              <p class="text-muted flex-grow-1">
                지도에서 요청 위치를 확인하고 응답해보세요.
              </p>
              <div class="mt-auto">
                <a
                  href="/nearby/request-list"
                  class="btn btn-outline-primary btn-sm"
                  >지도에서 보기</a
                >
              </div>
            </div>
          </div>

          <div class="col-md-4 col-sm-6 col-12">
            <div
              class="p-4 bg-white rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">📍</div>
              <h5 class="fw-bold">주변 정보 보기</h5>
              <p class="text-muted flex-grow-1">
                내 위치 주변에 등록된 정보를 쉽게 확인할 수 있어요.
              </p>
              <div class="mt-auto">
                <a href="/nearby" class="btn btn-outline-info btn-sm"
                  >주변 정보 보기</a
                >
              </div>
            </div>
          </div>

          <div class="col-md-4 col-sm-12 col-12">
            <div
              class="p-4 bg-white rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">🌟</div>
              <h5 class="fw-bold">자발적 공유</h5>
              <p class="text-muted flex-grow-1">
                정보가 없어도 직접 현장을 공유해 다른 사람에게 도움을 줄 수
                있어요.
              </p>
              <div class="mt-auto">
                <a href="/map/free-share" class="btn btn-outline-warning btn-sm"
                  >공유 지도 열기</a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 섹션 3 -->
    <section class="py-5 bg-white">
      <div class="container" data-aos="fade-up">
        <h2 class="text-center emoji-title">🏛️</h2>
        <h3 class="text-center section-title">공식 장소 커뮤니티</h3>
        <p class="lead text-center mb-5">
          인증된 장소 정보를 검색하거나, 직접 등록해 운영해보세요.
        </p>

        <div class="row g-4">
          <div class="col-md-6 col-12">
            <div
              class="p-4 bg-light rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">🔍</div>
              <h5 class="fw-bold">공식 장소 검색</h5>
              <p class="text-muted flex-grow-1">
                등록된 공식 장소를 확인하고 커뮤니티를 둘러보세요.
              </p>
              <div class="mt-auto">
                <a href="/place/search" class="btn btn-outline-success btn-sm"
                  >장소 검색하기</a
                >
              </div>
            </div>
          </div>

          <div class="col-md-6 col-12">
            <div
              class="p-4 bg-light rounded shadow hover-shadow d-flex flex-column h-100 text-center"
            >
              <div class="fs-1 mb-2">➕</div>
              <h5 class="fw-bold">장소 등록하기</h5>
              <p class="text-muted flex-grow-1">
                직접 장소를 등록하고 공식 장소로 운영해보세요. (승인 필요)
              </p>
              <div class="mt-auto">
                <a href="/place/register" class="btn btn-outline-dark btn-sm"
                  >장소 등록하기</a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <%@ include file="common/footer.jsp" %>

    <!-- AOS 애니메이션 -->
    <script src="https://unpkg.com/aos@2.3.4/dist/aos.js"></script>
    <script>
      AOS.init({ duration: 800 });
    </script>
  </body>
</html>
