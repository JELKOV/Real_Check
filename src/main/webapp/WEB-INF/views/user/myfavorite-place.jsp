<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>내 즐겨찾기 장소 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-4">
      <h3 class="mb-4">⭐ 내가 즐겨찾기한 장소</h3>

      <div id="favoritePlaceList" class="row g-4">
        <%-- JS로 렌더링될 영역 --%>
      </div>

      <div
        id="emptyMessage"
        class="text-center text-muted mt-4"
        style="display: none"
      >
        즐겨찾기한 장소가 없습니다.
      </div>
    </div>
    
    <%@ include file="../common/footer.jsp" %>
    <script src="/js/user/myfavorite-place.js"></script>
  </body>
</html>
