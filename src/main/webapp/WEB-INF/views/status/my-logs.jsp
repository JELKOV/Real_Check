<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>내가 공유한 정보 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5" style="max-width: 800px">
      <h3 class="text-center mb-4">내가 공유한 정보</h3>
      <table class="table table-striped text-center align-middle">
        <thead>
          <tr>
            <th>번호</th>
            <th>내용</th>
            <th>대기 인원</th>
            <th>이미지</th>
            <th>장소 ID</th>
            <th>등록일</th>
            <th>수정/삭제</th>
          </tr>
        </thead>
        <tbody id="logsBody"></tbody>
      </table>
      <div class="text-end mt-3">
        <a href="/nearby" class="btn btn-outline-secondary"
          >지도에서 등록하기</a
        >
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <!-- 수정 모달 (hidden) -->
    <div class="modal" tabindex="-1" id="editModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <form id="editForm">
            <div class="modal-header">
              <h5 class="modal-title">상태 수정</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
              ></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label>내용</label>
                <input
                  type="text"
                  id="editContent"
                  class="form-control"
                  required
                />
              </div>
              <div class="mb-3">
                <label>대기 인원</label>
                <input
                  type="number"
                  id="editWaitCount"
                  class="form-control"
                  required
                />
              </div>
              <div class="mb-3">
                <label>이미지 업로드</label><br />
                <button
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  id="uploadBtn"
                >
                  이미지 선택
                </button>
                <input
                  type="file"
                  id="fileInput"
                  name="file"
                  style="display: none"
                />
                <div id="uploadedImage" class="mt-2"></div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary">저장</button>
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                닫기
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <script>
      let editingId = null;
      let uploadedImageUrl = null;

      $(document).ready(function () {
        loadMyLogs();

        // 내 상태 로그 불러오기
        function loadMyLogs() {
          $.getJSON("/api/status/my", function (list) {
            var $tbody = $("#logsBody");
            $tbody.empty();
            list.forEach(function (log, index) {
              var imageHtml = log.imageUrl
                ? '<img src="' +
                  log.imageUrl +
                  '" alt="status image" style="max-width:60px; max-height:60px;"/>'
                : "-";
              var row =
                '<tr data-id="' +
                log.id +
                '">' +
                "<td>" +
                (index + 1) +
                "</td>" +
                "<td>" +
                log.content +
                "</td>" +
                "<td>" +
                log.waitCount +
                "</td>" +
                "<td>" +
                imageHtml +
                "</td>" +
                "<td>" +
                log.placeId +
                "</td>" +
                "<td>" +
                new Date(log.createdAt).toLocaleString("ko-KR", {
                  year: "numeric",
                  month: "2-digit",
                  day: "2-digit",
                  hour: "2-digit",
                  minute: "2-digit",
                }) +
                "</td>" +
                "<td>" +
                '<button class="btn btn-sm btn-primary btn-edit me-2">수정</button>' +
                '<button class="btn btn-sm btn-danger btn-delete">삭제</button>' +
                "</td>" +
                "</tr>";
              $tbody.append(row);
            });
          });
        }

        // 수정 버튼 클릭
        $("#logsBody").on("click", ".btn-edit", function () {
          const $tr = $(this).closest("tr");
          editingId = $tr.data("id");
          const content = $tr.find("td:eq(1)").text();
          const waitCount = $tr.find("td:eq(2)").text();

          $("#editContent").val(content);
          $("#editWaitCount").val(waitCount);
          $("#uploadedImage").empty();
          uploadedImageUrl = null;

          const modal = new bootstrap.Modal(
            document.getElementById("editModal")
          );
          modal.show();
        });

        // 이미지 업로드 버튼 클릭 시
        $("#uploadBtn").on("click", function () {
          $("#fileInput").click();
        });

        // 파일 선택하면 서버 업로드
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
              alert("업로드 성공! URL: " + url);
              uploadedImageUrl = url;
              $("#uploadedImage").html(
                `<img src="${url}" style="max-width:100px;"/>`
              );
            },
            error: function (xhr) {
              alert("업로드 실패: " + xhr.responseText);
            },
          });
        });

        // 수정 완료 버튼 클릭
        // 수정 폼 제출
        $("#editForm").on("submit", function (e) {
          e.preventDefault();

          if (!editingId) {
            alert("수정할 항목이 없습니다.");
            return;
          }

          const updatedData = {
            content: $("#editContent").val(),
            waitCount: parseInt($("#editWaitCount").val()),
            imageUrl: uploadedImageUrl, // 이게 새로 업로드한 URL
          };

          $.ajax({
            url: `/api/status/${editingId}`,
            method: "PUT",
            contentType: "application/json",
            data: JSON.stringify(updatedData),
            success: function () {
              alert("수정 완료");
              location.reload();
            },
            error: function (xhr) {
              alert("수정 실패: " + xhr.responseText);
            },
          });
        });

        // 삭제 버튼 클릭
        $("#logsBody").on("click", ".btn-delete", function () {
          var $tr = $(this).closest("tr");
          var id = $tr.data("id");
          if (confirm("정말 삭제하시겠습니까?")) {
            $.ajax({
              url: "/api/status/" + id,
              method: "DELETE",
              success: function () {
                $tr.remove();
              },
              error: function (xhr) {
                alert("삭제 실패: " + xhr.responseText);
              },
            });
          }
        });
      });
    </script>
  </body>
</html>
