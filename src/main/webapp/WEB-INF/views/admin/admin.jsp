<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>관리자 대시보드 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/admin.css" />
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h2 class="text-center fw-bold mb-5">관리자 대시보드</h2>

      <div class="row g-4">
        <!-- 통계 보기 -->
        <div class="col-md-4">
          <div class="card h-100 shadow-sm admin-card">
            <div class="card-body d-flex align-items-center">
              <div>
                <h5 class="card-title mb-1">통계 보기</h5>
                <p class="card-text text-muted small">
                  신고 수, 포인트 합계, 월별 로그 수
                </p>
                <a
                  href="/admin/stats"
                  class="btn btn-sm btn-outline-primary mt-2"
                  >바로가기</a
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 사용자 관리 -->
        <div class="col-md-4">
          <div class="card h-100 shadow-sm admin-card">
            <div class="card-body d-flex align-items-center">
              <div>
                <h5 class="card-title mb-1">사용자 관리</h5>
                <p class="card-text text-muted small">
                  차단 해제 및 사용자 검색
                </p>
                <a
                  href="/admin/users"
                  class="btn btn-sm btn-outline-success mt-2"
                  >바로가기</a
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 신고 관리 -->
        <div class="col-md-4">
          <div class="card h-100 shadow-sm admin-card">
            <div class="card-body d-flex align-items-center">
              <div>
                <h5 class="card-title mb-1">신고 관리</h5>
                <p class="card-text text-muted small">
                  전체 신고 내역 및 상세 통계
                </p>
                <a
                  href="/admin/reports"
                  class="btn btn-sm btn-outline-danger mt-2"
                  >바로가기</a
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 로그 관리 -->
        <div class="col-md-4">
          <div class="card h-100 shadow-sm admin-card">
            <div class="card-body d-flex align-items-center">
              <div>
                <h5 class="card-title mb-1">로그 관리</h5>
                <p class="card-text text-muted small">
                  관리자 행동 로그 조회 및 필터링
                </p>
                <a href="/admin/logs" class="btn btn-sm btn-outline-dark mt-2"
                  >바로가기</a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
