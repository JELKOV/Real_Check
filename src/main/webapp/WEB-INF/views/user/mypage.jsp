<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>마이페이지 - RealCheck</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link rel="stylesheet" href="/css/style.css" />
</head>
<body>
<%@ include file="../common/header.jsp" %>

<div class="container mt-5" style="max-width: 600px;">
  <h3 class="text-center mb-4">내 정보</h3>

  <c:if test="${not empty loginUser}">
    <div class="card">
      <div class="card-body">
        <p><strong>이메일:</strong> ${loginUser.email}</p>
        <p><strong>닉네임:</strong> ${loginUser.nickname}</p>
        <p><strong>상태:</strong> 
          <c:choose>
            <c:when test="${loginUser.active}">정상</c:when>
            <c:otherwise>차단됨</c:otherwise>
          </c:choose>
        </p>

        <div class="mt-4">
          <a href="/edit-profile" class="btn btn-outline-primary me-2">정보 수정</a>
          <a href="/change-password" class="btn btn-outline-secondary">비밀번호 변경</a>
        </div>
      </div>
    </div>
  </c:if>

  <c:if test="${empty loginUser}">
    <div class="alert alert-warning text-center">
      로그인이 필요합니다.
      <a href="/login" class="btn btn-sm btn-primary ms-2">로그인하기</a>
    </div>
  </c:if>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
