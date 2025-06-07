<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>통계 보기 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center fw-bold mb-4">📊 관리자 통계</h3>

      <!-- ─────────── 탭 네비게이션 ─────────── -->
      <ul class="nav nav-tabs" id="statsTab" role="tablist">
        <li class="nav-item" role="presentation">
          <button
            class="nav-link active"
            id="overview-tab"
            data-bs-toggle="tab"
            data-bs-target="#overview"
            type="button"
            role="tab"
            aria-controls="overview"
            aria-selected="true"
          >
            개요 (Overview)
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            id="log-stats-tab"
            data-bs-toggle="tab"
            data-bs-target="#log-stats"
            type="button"
            role="tab"
            aria-controls="log-stats"
            aria-selected="false"
          >
            로그 통계
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            id="user-stats-tab"
            data-bs-toggle="tab"
            data-bs-target="#user-stats"
            type="button"
            role="tab"
            aria-controls="user-stats"
            aria-selected="false"
          >
            사용자 통계
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            id="point-stats-tab"
            data-bs-toggle="tab"
            data-bs-target="#point-stats"
            type="button"
            role="tab"
            aria-controls="point-stats"
            aria-selected="false"
          >
            포인트 통계
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            class="nav-link"
            id="top-users-tab"
            data-bs-toggle="tab"
            data-bs-target="#top-users"
            type="button"
            role="tab"
            aria-controls="top-users"
            aria-selected="false"
          >
            Top 사용자
          </button>
        </li>
      </ul>

      <!-- ─────────── 탭 콘텐츠 ─────────── -->
      <div class="tab-content mt-4" id="statsTabContent">
        <!-- 1) 개요(Overview) 탭 -->
        <div
          class="tab-pane fade show active"
          id="overview"
          role="tabpanel"
          aria-labelledby="overview-tab"
        >
          <div class="row g-4">
            <div class="col-md-4">
              <div class="card text-center shadow-sm h-100">
                <div class="card-body">
                  <h6 class="card-title">총 신고 수</h6>
                  <p class="display-6" id="reportCount">-</p>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center shadow-sm h-100">
                <div class="card-body">
                  <h6 class="card-title">포인트 총 발행량</h6>
                  <p class="display-6" id="pointTotal">-</p>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center shadow-sm h-100">
                <div class="card-body">
                  <h6 class="card-title">카테고리별 상태로그 수</h6>
                  <p class="display-6" id="totalLogsByCategory">-</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 2) 로그 통계 탭 -->
        <div
          class="tab-pane fade"
          id="log-stats"
          role="tabpanel"
          aria-labelledby="log-stats-tab"
        >
          <div class="row g-4">
            <div class="col-md-6">
              <div class="card shadow-sm h-100">
                <div class="card-header text-center">
                  <h6 class="mb-0">📈 월별 상태로그 등록 수</h6>
                </div>
                <div class="card-body">
                  <canvas id="logChart" style="height: 200px"></canvas>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="card shadow-sm h-100">
                <div class="card-header text-center">
                  <h6 class="mb-0">📊 카테고리별 상태로그 수</h6>
                </div>
                <div class="card-body d-flex justify-content-center">
                  <canvas
                    id="categoryChart"
                    style="width: 150px; height: 150px"
                  ></canvas>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 3) 사용자 통계 탭 -->
        <div
          class="tab-pane fade"
          id="user-stats"
          role="tabpanel"
          aria-labelledby="user-stats-tab"
        >
          <div class="row g-4">
            <div class="col-12">
              <div class="card shadow-sm">
                <div class="card-header text-center">
                  <h6 class="mb-0">👥 월별 사용자 가입 통계</h6>
                </div>
                <div class="card-body">
                  <canvas id="userSignUpChart" style="height: 200px"></canvas>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 4) 포인트 통계 탭 -->
        <div
          class="tab-pane fade"
          id="point-stats"
          role="tabpanel"
          aria-labelledby="point-stats-tab"
        >
          <div class="row g-4">
            <!-- 카드 1: 일별 흐름 -->
            <div class="col-md-6">
              <div class="card h-100 shadow-sm">
                <div class="card-header text-center">
                  <h6 class="mb-0">💰 일별 포인트 흐름</h6>
                </div>
                <div class="card-body">
                  <canvas id="pointFlowChart" style="height: 200px"></canvas>
                </div>
              </div>
            </div>

            <!-- 카드 2: 분포도 & 0원 비율 -->
            <div class="col-md-6">
              <div class="card h-100 shadow-sm">
                <div class="card-header text-center">
                  <h6 class="mb-0">📊 잔액 분포 & 0원 비율</h6>
                </div>
                <div class="card-body d-flex flex-column align-items-center">
                  <canvas
                    id="pointDistributionChart"
                    style="width: 150px; height: 150px"
                  ></canvas>
                  <p class="mt-3 mb-0">
                    0원 사용자: <span id="zeroBalanceRatio">-</span>
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div class="row g-4 mt-3">
            <!-- 카드 3: 최근 내역 -->
            <div class="col-lg-6">
              <div class="card h-100 shadow-sm">
                <div class="card-header">
                  <h6 class="mb-0">📋 최근 포인트 내역 (최신 10건)</h6>
                </div>
                <div class="card-body p-2">
                  <div
                    class="table-responsive"
                    style="max-height: 300px; overflow: auto"
                  >
                    <table class="table table-sm mb-0">
                      <thead class="table-light">
                        <tr>
                          <th>날짜</th>
                          <th>유저</th>
                          <th>금액</th>
                          <th>사유</th>
                        </tr>
                      </thead>
                      <tbody id="recentPointHistory"></tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>

            <!-- 카드 4: 상위 사용자 -->
            <div class="col-lg-6">
              <div class="card h-100 shadow-sm">
                <div class="card-header">
                  <h6 class="mb-0">🥇 잔액 Top 10 사용자</h6>
                </div>
                <div class="card-body p-2">
                  <div
                    class="table-responsive"
                    style="max-height: 300px; overflow: auto"
                  >
                    <table class="table table-sm mb-0">
                      <thead class="table-light">
                        <tr>
                          <th>#</th>
                          <th>유저</th>
                          <th>잔액</th>
                        </tr>
                      </thead>
                      <tbody id="topPointUsers"></tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 5) Top 사용자 탭 -->
        <div
          class="tab-pane fade"
          id="top-users"
          role="tabpanel"
          aria-labelledby="top-users-tab"
        >
          <div class="row g-4">
            <div class="col-md-6">
              <div class="card shadow-sm h-100">
                <div class="card-header">
                  <h6 class="mb-0">🚨 신고 Top 10 유저</h6>
                </div>
                <div class="card-body p-2">
                  <div
                    class="table-responsive"
                    style="max-height: 300px; overflow: auto"
                  >
                    <table class="table table-sm mb-0">
                      <thead class="table-light">
                        <tr>
                          <th>#</th>
                          <th>유저 ID</th>
                          <th>닉네임</th>
                          <th>건수</th>
                        </tr>
                      </thead>
                      <tbody id="topReportedUsers"></tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-md-6">
              <div class="card shadow-sm h-100">
                <div class="card-header">
                  <h6 class="mb-0">🥇 기여 Top 10 유저</h6>
                </div>
                <div class="card-body p-2">
                  <div
                    class="table-responsive"
                    style="max-height: 300px; overflow: auto"
                  >
                    <table class="table table-sm mb-0">
                      <thead class="table-light">
                        <tr>
                          <th>#</th>
                          <th>유저 ID</th>
                          <th>닉네임</th>
                          <th>로그 수</th>
                        </tr>
                      </thead>
                      <tbody id="topContributingUsers"></tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script src="/js/admin/stats.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  </body>
</html>
