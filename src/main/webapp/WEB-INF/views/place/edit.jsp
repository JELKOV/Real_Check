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
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container my-5">
      <div class="row justify-content-center">
        <div class="col-12 col-md-10 col-lg-8">
          <div class="card shadow-sm border-0 notice-form-wrapper">
            <div class="card-body">
              <!-- 제목 영역 -->
              <div class="mb-4">
                <h3 class="fw-bold">📢 공지 수정</h3>
                <p class="text-muted small">기존 공지를 수정합니다.</p>
              </div>

              <form id="editForm">
                <input type="hidden" id="logId" value="${statusLog.id}" />
                <input
                  type="hidden"
                  id="placeId"
                  value="${statusLog.placeId}"
                />

                <!-- 공지 내용 -->
                <div class="form-floating mb-3">
                  <textarea
                    id="content"
                    name="content"
                    class="form-control"
                    style="height: 140px"
                    maxlength="300"
                    required
                  >
${statusLog.content}</textarea
                  >
                  <label for="content">공지 내용</label>
                </div>
                <div class="d-flex justify-content-end mt-1">
                  <small id="contentCount" class="text-muted">0 / 300자</small>
                </div>

                <!-- 카테고리 -->
                <div class="form-floating mb-3">
                  <select
                    id="category"
                    name="category"
                    class="form-select"
                    disabled
                  >
                    <option value="">선택하세요</option>
                  </select>
                  <label for="category">카테고리</label>
                </div>

                <!-- 동적 필드 -->
                <div id="dynamicAnswerFields"></div>

                <!-- 이미지 업로드 -->
                <div class="mb-3">
                  <label class="form-label">이미지 첨부 (선택)</label>
                  <div id="dropArea" class="mb-2">
                    <i class="bi bi-image"></i>
                    <p class="mb-1">📎 이미지를 드래그하거나 선택하세요</p>
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

                  <!-- 미리보기 영역 -->
                  <div class="border rounded p-2 position-relative d-none">
                    <div class="d-flex justify-content-end mb-2">
                      <button
                        type="button"
                        class="btn btn-sm btn-outline-danger d-none"
                        id="cancelImageBtn"
                      >
                        ❌ 전체 제거
                      </button>
                    </div>
                    <div
                      id="uploadedPreview"
                      class="d-flex flex-wrap gap-2"
                    ></div>
                  </div>
                </div>

                <!-- 버튼 영역 -->
                <div class="d-flex justify-content-end gap-2 mt-4">
                  <a
                    href="/place/community/${place.id}"
                    class="btn btn-outline-secondary"
                  >
                    취소
                  </a>
                  <button type="submit" class="btn btn-primary px-4">
                    수정하기
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
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
      const existingImageUrls = [
        <c:forEach var="img" items="${statusLog.imageUrls}" varStatus="loop">
          <c:if test="${not empty img}">
            "${img}"<c:if test="${!loop.last}">,</c:if>
          </c:if>
        </c:forEach>,
      ];
      const currentCategory = "${statusLog.category}";
      const statusLogJson = "${statusLogJson}";
    </script>
    <script type="module" src="/js/place/edit.js"></script>
  </body>
</html>
