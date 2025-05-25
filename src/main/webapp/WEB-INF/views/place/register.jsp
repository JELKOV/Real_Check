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
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 700px">
      <h3>📢 공지 등록</h3>
      <p class="text-muted">해당 장소의 공지를 등록합니다.</p>

      <form id="registerForm">
        <input type="hidden" name="placeId" value="${param.placeId}" />

        <div class="mb-3">
          <label for="content" class="form-label">공지 내용</label>
          <textarea
            id="content"
            name="content"
            class="form-control"
            rows="5"
            required
          ></textarea>
        </div>

        <div class="mb-3">
          <label for="fileInput" class="form-label">이미지 첨부 (선택)</label>
          <input type="file" class="form-control" id="fileInput" />
          <div id="uploadedPreview" class="mt-2"></div>
        </div>

        <button type="submit" class="btn btn-primary">등록하기</button>
        <a href="/place/community/${param.placeId}" class="btn btn-secondary"
          >취소</a
        >
      </form>
    </div>

    <script>
      let uploadedImageUrl = null;

      // 이미지 업로드 처리
      $("#fileInput").on("change", function () {
        const file = this.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        $.ajax({
          url: "/api/upload",
          method: "POST",
          data: formData,
          processData: false,
          contentType: false,
          success: function (url) {
            uploadedImageUrl = url;
            $("#uploadedPreview").html(
              `<img src="${url}" class="img-fluid border" style="max-height: 150px;" />`
            );
          },
          error: function (xhr) {
            alert("업로드 실패: " + xhr.responseText);
          },
        });
      });

      // 공지 등록 제출
      $("#registerForm").on("submit", function (e) {
        e.preventDefault();

        const data = {
          placeId: $("input[name='placeId']").val(),
          content: $("#content").val(),
          imageUrl: uploadedImageUrl,
        };

        $.ajax({
          url: "/api/status",
          method: "POST",
          contentType: "application/json",
          data: JSON.stringify(data),
          success: function () {
            alert("공지 등록 완료!");
            location.href = "/place/community/" + data.placeId;
          },
          error: function (xhr) {
            alert("등록 실패: " + xhr.responseText);
          },
        });
      });
    </script>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
