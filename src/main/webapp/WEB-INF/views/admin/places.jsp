<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>장소 관리 - 관리자</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center fw-bold mb-4">📍 장소 관리</h3>

      <!-- 검색/필터 폼 -->
      <form id="filterForm" class="row g-3 mb-4">
        <div class="col-sm-6 col-md-4">
          <input
            type="text"
            id="q"
            class="form-control form-control-sm"
            placeholder="장소명 검색"
          />
        </div>
        <div class="col-sm-4 col-md-3">
          <select id="statusFilter" class="form-select form-select-sm">
            <option value="">전체</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">승인</option>
            <option value="REJECTED">반려</option>
          </select>
        </div>

        <div class="col-auto">
          <button type="submit" class="btn btn-primary">검색</button>
        </div>
      </form>

      <!-- 결과 테이블 -->
      <div class="table-responsive">
        <table class="table table-striped table-hover table-sm align-middle">
          <thead class="table-light">
            <tr>
              <th class="text-center">#</th>
              <th>이름</th>
              <th class="d-none d-md-table-cell">주소</th>
              <th class="text-center d-none d-sm-table-cell">등록자</th>
              <th class="text-center">상태</th>
              <th class="d-none d-lg-table-cell">생성일</th>
              <th class="d-none d-lg-table-cell">수정일</th>
              <th class="text-center">승인/반려</th>
              <th class="text-center">삭제</th>
              <th class="text-center">상세</th>
            </tr>
          </thead>
          <tbody id="placeTableBody">
            <!-- JS로 채워집니다 -->
          </tbody>
        </table>
      </div>

      <!-- 페이징 컨트롤 -->
      <nav>
        <ul class="pagination justify-content-center" id="pagination">
          <!-- JS로 채워집니다 -->
        </ul>
      </nav>
    </div>

    <!-- 장소 상세보기 모달 -->
    <div
      class="modal fade"
      id="placeDetailModal"
      tabindex="-1"
      aria-labelledby="placeDetailLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="placeDetailLabel">장소 상세정보</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="닫기"
            ></button>
          </div>
          <div class="modal-body">
            <dl class="row">
              <dt class="col-sm-3">이름</dt>
              <dd class="col-sm-9" id="detailName"></dd>

              <dt class="col-sm-3">주소</dt>
              <dd class="col-sm-9" id="detailAddress"></dd>

              <dt class="col-sm-3">등록자</dt>
              <dd class="col-sm-9" id="detailOwner"></dd>

              <dt class="col-sm-3">상태</dt>
              <dd class="col-sm-9" id="detailStatus"></dd>

              <dt class="col-sm-3">위치 (lat, lng)</dt>
              <dd class="col-sm-9" id="detailLocation"></dd>

              <dt class="col-sm-3">생성일</dt>
              <dd class="col-sm-9" id="detailCreatedAt"></dd>

              <dt class="col-sm-3">수정일</dt>
              <dd class="col-sm-9" id="detailUpdatedAt"></dd>

              <dt class="col-sm-3">허용된 요청 타입</dt>
              <dd class="col-sm-9" id="detailAllowedTypes"></dd>

              <dt class="col-sm-3">최근 공지</dt>
              <dd class="col-sm-9" id="detailRecentInfo"></dd>

              <dt class="col-sm-3">즐겨찾기 수</dt>
              <dd class="col-sm-9" id="detailFavoriteCount"></dd>

              <dt class="col-sm-3">요청 수</dt>
              <dd class="col-sm-9" id="detailRequestCount"></dd>

              <dt class="col-sm-3">로그 수</dt>
              <dd class="col-sm-9" id="detailStatusLogCount"></dd>
            </dl>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/admin/places.js"></script>
  </body>
</html>
