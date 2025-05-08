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

      <!-- 답변 리스트 -->
      <h4 class="mt-5 mb-3">등록된 답변</h4>
      <ul id="answerList" class="list-group mb-4"></ul>

      <!-- 답변 등록 폼 -->
      <div id="answerFormSection">
        <h5>답변 등록</h5>
        <form id="answerForm" enctype="multipart/form-data">
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

          <!-- 카테고리별 추가 입력 필드: JavaScript로 동적 생성 -->
          <div id="dynamicAnswerFields"></div>

          <div class="mb-3">
            <input type="file" name="file" class="form-control" />
          </div>
          <button type="submit" class="btn btn-success w-100">
            답변 등록하기
          </button>
        </form>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <script src="/js/detail.js"></script>
  </body>
</html>
