<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--TODO : 입력폼 변화 / 지도 연동 / 카테고리화 입력폼변화 / 추가필드 답변 -->
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
      src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=fyljbu3cv5"
    ></script>
  </head>

  <body>
    <%@ include file="../common/header.jsp" %>

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

    <script>
      const requestId = location.pathname.split("/").pop();
      const loginUserId = "${loginUser != null ? loginUser.id : ''}";

      $(document).ready(function () {
        // 요청 정보 가져오기
        $.get(`/api/request/${"${requestId}"}`, function (request) {
          const formattedDate = new Date(request.createdAt).toLocaleString();
          const nickname = request.requesterNickname || "익명";
          const location =
            request.placeName || request.customPlaceName || "장소 정보 없음";
          const html = `
            <div class="card">
              <div class="card-body">
                <h5 class="card-title">${"${request.title}"}</h5>
                <p class="card-text text-muted">${"${request.content}"}</p>
                <ul class="list-unstyled mt-3">
                  <li><strong>포인트:</strong> ${"${request.point}"}pt</li>
                  <li><strong>장소:</strong> ${"${location}"}</li>
                  <li><strong>작성자:</strong> ${"${nickname}"}</li>
                  <li><strong>작성일:</strong> ${"${formattedDate}"}</li>
                </ul>
              </div>
            </div>
          `;
          $("#requestDetail").html(html);

          // 로그인 유저 확인 및 답변 수 제한을 통한 입력창 숨김 처리
          if (
            parseInt(loginUserId) === request.requesterId ||
            request.answerCount >= 3
          ) {
            $("#answerFormSection").hide();
          }

          // 지도 초기화
          const map = new naver.maps.Map("map", {
            center: new naver.maps.LatLng(request.lat, request.lng),
            zoom: 16,
          });

          // 마커 표시
          new naver.maps.Marker({
            position: new naver.maps.LatLng(request.lat, request.lng),
            map: map,
          });
        });

        // 답변 리스트 불러오기
        $.get(
          `/api/status/by-request/${"${requestId}"}`,
          function (answers) {
            if (answers.length === 0) {
              $("#answerList").html(
                '<li class="list-group-item">등록된 답변이 없습니다.</li>'
              );
              return;
            }

            const hasSelected = answers.some((a) => a.selected);

            answers
              .sort((a, b) => (b.selected ? 1 : 0) - (a.selected ? 1 : 0))
              .forEach((answer) => {
                const imageHtml = answer.imageUrl
                  ? '<img src="' +
                    answer.imageUrl +
                    '" style="max-width:100px;" class="mt-2" />'
                  : "";
                const nickname = answer.nickname
                  ? answer.nickname
                  : "익명 사용자";
                const isOwner = answer.requestOwnerId === parseInt(loginUserId);
                const canSelect = isOwner && !hasSelected && !answer.selected;
                const formattedDate = new Date(
                  answer.createdAt
                ).toLocaleString();

                const selectedBadge = answer.selected
                  ? `<span class="badge bg-success ms-2">✅ 채택됨</span>`
                  : "";

                const selectBtn = canSelect
                  ? `<button class="btn btn-sm btn-outline-success select-answer-btn mt-2" data-id=${"${answer.id}"}>이 답변 채택</button>`
                  : "";

                const row = `
                <li class="list-group-item">
                  <strong>${"${nickname}"}</strong> ${"${selectedBadge}"}
                  <p>${"${answer.content}"}</p>
                  ${"${imageHtml}"}
                  <br><small class="text-muted">${"${formattedDate}"}</small>
                  ${"${selectBtn}"}
                </li>
              `;
                $("#answerList").append(row);
              });
          }
        );

        // 채택 처리
        $(document).on("click", ".select-answer-btn", function () {
          const statusLogId = $(this).data("id");
          if (!confirm("이 답변을 채택하시겠습니까?")) return;

          $.post(`/api/status/select/${"${statusLogId}"}`)
            .done(() => {
              alert("답변이 채택되었습니다.");
              location.reload();
              //TODO: 포인트 지급 차감 처리
            })
            .fail((xhr) => {
              alert("채택 실패: " + xhr.responseText);
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
          };

          $.ajax({
            url: `/api/answer/${"${requestId}"}`,
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