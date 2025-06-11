<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>공지 수정 - RealCheck</title>
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
      <h3>📢 공지 수정</h3>
      <p class="text-muted">기존 공지를 수정합니다.</p>

      <form id="editForm">
        <input type="hidden" id="logId" value="${statusLog.id}" />
        <input type="hidden" id="placeId" value="${statusLog.placeId}" />

        <!-- 공지 내용 -->
        <div class="mb-3">
          <label for="content" class="form-label">공지 내용</label>
          <textarea
            id="content"
            name="content"
            class="form-control"
            rows="5"
            maxlength="300"
            required
          >
            ${statusLog.content}</textarea
          >
          <div class="d-flex justify-content-end mt-1">
            <small id="contentCount" class="text-muted">0 / 300자</small>
          </div>
        </div>

        <!-- 카테고리 -->
        <div class="mb-3">
          <label for="category" class="form-label">카테고리</label>
          <select id="category" name="category" class="form-select"></select>
        </div>

        <!-- 동적 필드 -->
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

          <!-- 미리보기 + 전체 제거 버튼 -->
          <div class="border rounded p-2 position-relative">
            <div class="d-flex justify-content-end mb-2">
              <button
                type="button"
                class="btn btn-sm btn-outline-danger d-none"
                id="cancelImageBtn"
              >
                ❌ 전체 제거
              </button>
            </div>

            <div id="uploadedPreview" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>

        <!-- 버튼 -->
        <div class="mt-4 d-flex justify-content-end gap-2">
          <button type="submit" class="btn btn-primary">수정하기</button>
          <a href="/place/community/${place.id}" class="btn btn-secondary"
            >취소</a
          >
        </div>
      </form>
    </div>

    <!-- 이미지 모달 -->
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
      // 선택 가능한 카테고리 목록 (공식 장소마다 다를 수 있음)
      const allowedTypes = [
        <c:forEach
          var="type"
          items="${place.allowedRequestTypes}"
          varStatus="loop"
        >
          "${type}"<c:if test="${!loop.last}">,</c:if>
        </c:forEach>,
      ];
      // 기존 이미지 목록
      const existingImageUrls = [
        <c:forEach var="img" items="${statusLog.imageUrls}" varStatus="loop">
          "${img}"<c:if test="${!loop.last}">,</c:if>
        </c:forEach>,
      ];
      // 현재 공지의 카테고리 (예: FOOD_MENU)
      const currentCategory = "${statusLog.category}";
      // 전체 StatusLogDto 데이터를 JSON 형태로 전달 (동적 필드 렌더링용)
      const statusLogJson = "${statusLogJson}";
    </script>
    <script src="/js/place/edit.js"></script>
  </body>
</html>
