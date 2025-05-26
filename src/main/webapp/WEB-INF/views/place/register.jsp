<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>공지 등록 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="/css/place/register.css" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 700px">
      <h3>📢 공지 등록</h3>
      <p class="text-muted">해당 장소의 공지를 등록합니다.</p>

      <form id="registerForm">
        <input type="hidden" name="placeId" value="${param.placeId}" />

        <!-- 공지 내용 -->
        <div class="mb-3">
          <label for="content" class="form-label">공지 내용</label>

          <!-- 텍스트박스 -->
          <textarea
            id="content"
            name="content"
            class="form-control"
            rows="5"
            maxlength="300"
            required
          ></textarea>

          <!-- 글자 수 -->
          <div class="d-flex justify-content-end mt-1">
            <small id="contentCount" class="text-muted">0 / 300자</small>
          </div>
        </div>

        <!-- 공지 카테고리 선택 -->
        <div class="mb-3">
          <label for="category" class="form-label">카테고리</label>
          <select id="category" name="category" class="form-select"></select>
        </div>

        <!-- 카테고리별 필드 자동 렌더링 영역 -->
        <div id="dynamicAnswerFields"></div>

        <!-- 이미지 업로드 -->
        <div class="mb-3">
          <label class="form-label">이미지 첨부 (선택)</label>

          <div id="dropArea" class="mb-2">
            <p class="mb-1">📎 이미지를 드래그 / 파일 선택</p>
            <button
              type="button"
              id="selectImageBtn"
              class="btn btn-sm btn-outline-primary"
            >
              파일 선택
            </button>
            <input
              type="file"
              id="fileInput"
              accept="image/*"
              multiple
              style="display: none"
            />
          </div>

          <!-- 미리보기 + 전체 제거 버튼을 묶는 박스 -->
          <div class="border rounded p-2 position-relative">
            <!-- 전체 제거 버튼: 미리보기 블록 '바깥' 우측 상단 -->
            <div class="d-flex justify-content-end mb-2">
              <button
                type="button"
                class="btn btn-sm btn-outline-danger d-none"
                id="cancelImageBtn"
              >
                ❌ 전체 제거
              </button>
            </div>

            <!-- 실제 미리보기 이미지 출력 영역 -->
            <div id="uploadedPreview" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>

        <!-- 버튼 -->
        <div class="d-flex justify-content-end gap-2">
          <button type="submit" class="btn btn-primary">등록하기</button>
          <a href="/place/community/${param.placeId}" class="btn btn-secondary"
            >취소</a
          >
        </div>
      </form>
    </div>

    <!-- 사진 모달 -->
    <div class="modal fade" id="imageModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content bg-dark">
          <div class="modal-body text-center">
            <img id="modalImage" class="img-fluid rounded" />
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script>
      const allowedTypes = [
        <c:forEach
          var="type"
          items="${place.allowedRequestTypes}"
          varStatus="loop"
        >
          "${type}"<c:if test="${!loop.last}">,</c:if>
        </c:forEach>,
      ];
    </script>
    <script src="/js/place/register.js"></script>
  </body>
</html>
