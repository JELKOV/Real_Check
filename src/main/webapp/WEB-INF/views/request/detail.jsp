<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 상세 보기 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/request/detail.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script
      type="text/javascript"
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${naverMapClientId}"
    ></script>
  </head>

  <body>
    <%@ include file="../common/header.jsp" %>

    <!-- 히든 필드로 서버 값 전달 -->
    <input type="hidden" id="requestId" value="${requestId}" />
    <input type="hidden" id="loginUserId" value="${loginUser.id}" />

    <div class="container mt-5" style="max-width: 800px">
      <h2 class="mb-4 text-center">🔍 요청 상세 보기</h2>

      <!-- 요청 상세 정보 -->
      <div id="requestDetail" class="mb-5 border rounded p-4 bg-light"></div>

      <!-- 지도 표시 -->
      <div id="map" style="height: 300px" class="mb-5"></div>

      <!-- 자동 마감 안내 -->
      <div id="autoCloseNotice"></div>

      <!-- 답변 리스트 -->
      <h4 class="mt-5 mb-3">등록된 답변</h4>
      <ul id="answerList" class="list-group mb-4"></ul>

      <!-- 답변 등록 폼 -->
      <div id="answerFormSection">
        <h5>답변 등록</h5>
        <form id="answerForm" enctype="multipart/form-data">
          <!-- 카테고리별 추가 입력 필드: JavaScript로 동적 생성 -->
          <div id="dynamicAnswerFields"></div>

          <div class="mb-3">
            <textarea
              class="form-control"
              name="content"
              id="answerContent"
              rows="3"
              placeholder="답변 내용을 입력하세요"
              required
            ></textarea>
          </div>

          <!-- 이미지 업로드 필드 -->
          <div class="mb-3">
            <label class="form-label">이미지 첨부 (선택)</label>
            <input
              type="file"
              name="files"
              id="fileInput"
              class="form-control"
              multiple
            />

            <!-- 미리보기 영역 -->
            <div id="uploadedPreview" class="mt-2"></div>
          </div>

          <button type="submit" class="btn btn-success w-100">
            답변 등록하기
          </button>
        </form>
      </div>
    </div>

    <!-- 신고 사유 선택 모달 -->
    <div
      class="modal fade"
      id="reportReasonModal"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">🚨 신고 사유 선택</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="닫기"
            ></button>
          </div>
          <div class="modal-body">
            <select id="reportReasonSelect" class="form-select">
              <option value="">사유를 선택해주세요</option>
              <option value="정보가 틀려요">📌 정보가 틀려요</option>
              <option value="불쾌한 내용이에요">😡 불쾌한 내용이에요</option>
              <option value="스팸/홍보예요">📢 스팸/홍보예요</option>
              <option value="기타">❓ 기타</option>
            </select>
            <input type="hidden" id="reportTargetStatusLogId" />
          </div>
          <div class="modal-footer">
            <button id="submitReportBtn" type="button" class="btn btn-danger">
              신고
            </button>
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 이미지 확대 모달 -->
    <div class="modal fade" id="imageModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content bg-dark">
          <div class="modal-body text-center p-0">
            <img
              id="modalImage"
              src=""
              class="img-fluid rounded"
              style="max-height: 90vh"
            />
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/request/detail.js"></script>
  </body>
</html>
