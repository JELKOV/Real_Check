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
  </head>

  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 800px">
      <h2 class="mb-4 text-center">🔍 요청 상세 보기</h2>

      <!-- 요청 상세 정보 -->
      <div id="requestDetail" class="mb-5 border rounded p-4 bg-light"></div>

      <!-- 답변 리스트 -->
      <h4 class="mt-5 mb-3">등록된 답변</h4>
      <ul id="answerList" class="list-group mb-4"></ul>

      <!-- 답변 등록 폼 -->
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
        <div class="mb-3">
          <input type="file" name="file" class="form-control" />
        </div>
        <button type="submit" class="btn btn-success w-100">
          답변 등록하기
        </button>
      </form>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      const requestId = location.pathname.split("/").pop();

      $(document).ready(function () {
        // 요청 정보 가져오기
        $.get("/api/request/" + requestId, function (request) {
          let html =
            "<h5>" +
            request.title +
            "</h5>" +
            "<p class='text-muted'>" +
            request.content +
            "</p>" +
            "<p><strong>포인트:</strong> " +
            request.point +
            "</p>" +
            "<p><strong>장소:</strong> " +
            request.placeId +
            " (" +
            request.lat +
            ", " +
            request.lng +
            ")</p>";

          $("#requestDetail").html(html);
        });

        // 답변 리스트 불러오기
        $.get(`/api/status/by-request/${requestId}`, function (answers) {
          if (answers.length === 0) {
            $("#answerList").html(
              '<li class="list-group-item">등록된 답변이 없습니다.</li>'
            );
            return;
          }

          answers.forEach((answer) => {
            const imageHtml = answer.imageUrl
              ? '<img src="' +
                answer.imageUrl +
                '" style="max-width:100px;" class="mt-2" />'
              : "";
            const nickname = answer.nickname ? answer.nickname : "익명 사용자";
            const row =
              '<li class="list-group-item">' +
              "<strong>" +
              nickname +
              "</strong>" +
              "<p>" +
              answer.content +
              "</p>" +
              imageHtml +
              '<br><small class="text-muted">' +
              new Date(answer.createdAt).toLocaleString() +
              "</small>" +
              "</li>";

            $("#answerList").append(row);
          });
        });

        // 답변 등록 처리
        $("#answerForm").on("submit", function (e) {
          e.preventDefault();

          const content = $("#answerContent").val();
          const dto = {
            content: content,
            waitCount: 0,
            imageUrl: null, // TODO: 이미지 업로드 후 URL 연결
            placeId: 1, // 테스트용 (실제론 지도에서 선택한 값이 들어가야 함)
          };

          $.ajax({
            url: `/api/answer/${requestId}`,
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(dto),
            success: function () {
              alert("답변이 등록되었습니다.");
              location.reload();
            },
            error: function (xhr) {
              alert("답변 등록 실패: " + xhr.responseText);
            },
          });
        });
      });
    </script>
  </body>
</html>
