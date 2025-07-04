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
              <div class="mb-4">
                <h3 class="fw-bold">📢 공지 등록</h3>
                <p class="text-muted small">해당 장소의 공지를 등록합니다.</p>
              </div>

              <form id="registerForm">
                <input type="hidden" name="placeId" value="${param.placeId}" />

                <!-- 공지 내용 -->
                <div class="form-floating mb-3">
                  <textarea
                    class="form-control"
                    placeholder="공지 내용을 입력하세요"
                    id="content"
                    name="content"
                    style="height: 140px"
                    maxlength="300"
                    required
                  ></textarea>
                  <label for="content">공지 내용</label>
                </div>
                <div class="d-flex justify-content-end mt-1">
                  <small id="contentCount" class="text-muted">0 / 300자</small>
                </div>

                <!-- 공지 카테고리 선택 -->
                <div class="mb-3">
                  <label for="category" class="form-label">카테고리</label>

                  <!-- 숨겨진 select (폼 전송용) -->
                  <select
                    id="category"
                    name="category"
                    class="form-select d-none"
                    required
                  ></select>

                  <!-- 커스텀 드롭다운 -->
                  <div class="custom-dropdown" id="categoryDropdown">
                    <button
                      type="button"
                      class="dropdown-toggle-btn"
                      id="categoryToggle"
                    >
                      <span class="label">선택하세요</span>
                      <span class="dropdown-arrow">▼</span>
                    </button>
                    <ul class="dropdown-list" id="categoryList"></ul>
                  </div>
                </div>

                <!-- 카테고리별 필드 자동 렌더링 영역 -->
                <div id="dynamicAnswerFields"></div>

                <!-- 이미지 업로드 -->
                <div class="mb-3">
                  <label class="form-label">이미지 첨부 (선택)</label>

                  <div id="dropArea" class="mb-2">
                    <i class="bi bi-image"></i>
                    <!-- 아이콘 -->
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

                  <!-- 미리보기 + 전체 제거 버튼을 묶는 박스 -->
                  <div class="border rounded p-2 position-relative d-none">
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
                    <div
                      id="uploadedPreview"
                      class="d-flex flex-wrap gap-2"
                    ></div>
                  </div>
                </div>

                <!-- 버튼 -->
                <div class="d-flex justify-content-end gap-2 mt-4">
                  <a
                    href="/place/community/${param.placeId}"
                    class="btn btn-outline-secondary"
                  >
                    취소
                  </a>
                  <button type="submit" class="btn btn-primary px-4">
                    등록하기
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
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
    <script type="module" src="/js/place/register.js"></script>
  </body>
</html>
