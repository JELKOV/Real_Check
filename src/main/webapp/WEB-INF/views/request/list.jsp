<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 목록 - RealCheck</title>
    <link rel="stylesheet" href="/css/style.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  </head>
  <body>
    <%@ include file="../common/header.jsp" %>

    <div class="container mt-5">
      <h3 class="text-center mb-4">전체 요청 목록</h3>

      <!-- 카테고리 필터 -->
      <div class="mb-4 text-center">
        <select id="categoryFilter" class="form-select w-auto d-inline-block">
          <option value="">전체 카테고리</option>
          <option value="PARKING">🅿️ 주차 가능 여부</option>
          <option value="WAITING_STATUS">⏳ 대기 상태</option>
          <option value="STREET_VENDOR">🥟 노점 현황</option>
          <option value="PHOTO_REQUEST">📸 사진 요청</option>
          <option value="BUSINESS_STATUS">🏪 영업 여부</option>
          <option value="OPEN_SEAT">💺 좌석 여유</option>
          <option value="BATHROOM">🚻 화장실 여부</option>
          <option value="WEATHER_LOCAL">☁️ 현장 날씨</option>
          <option value="NOISE_LEVEL">🔊 소음 상태</option>
          <option value="FOOD_MENU">🍔 음식/메뉴</option>
          <option value="CROWD_LEVEL">👥 혼잡도</option>
          <option value="ETC">❓ 기타</option>
        </select>
      </div>

      <div id="requestList" class="row g-3"></div>
    </div>

    <script>
      const categoryLabelMap = {
        PARKING: "🅿️ 주차",
        WAITING_STATUS: "⏳ 대기",
        STREET_VENDOR: "🥟 노점",
        PHOTO_REQUEST: "📸 사진",
        BUSINESS_STATUS: "🏪 영업",
        OPEN_SEAT: "💺 좌석",
        BATHROOM: "🚻 화장실",
        WEATHER_LOCAL: "☁️ 날씨",
        NOISE_LEVEL: "🔊 소음",
        FOOD_MENU: "🍔 메뉴",
        CROWD_LEVEL: "👥 혼잡",
        ETC: "❓ 기타",
      };

      function renderRequests(data) {
        if (data.length === 0) {
          $("#requestList").html(
            `<div class="text-center text-muted">요청이 없습니다.</div>`
          );
          return;
        }

        let html = "";
        data.forEach(function (req) {
          const badge = req.category
            ? `<span class="badge bg-secondary mb-2"> ${"${categoryLabelMap[req.category] || req.category}"} </span>`
            : "";

          html += `
            <div class="col-md-6">
              <div class="card h-100 shadow-sm">
                <div class="card-body">
                  ${"${badge}"}
                  <h5 class="card-title">${"${req.title}"}</h5>
                  <p class="card-text">${"${req.content.substring(0, 30)}"}...</p>
                  <small>포인트: ${"${req.point}"}</small><br/>
                  <a href="#" class="btn btn-outline-primary btn-sm mt-2 view-detail" data-id=${"${req.id}"}>상세보기</a>
                </div>
              </div>
            </div>
          `;
        });

        $("#requestList").html(html);
      }

      function loadRequests(category = "") {
        $.get("/api/request/open", function (data) {
          if (category) {
            data = data.filter((req) => req.category === category);
          }
          renderRequests(data);
        });
      }

      $(function () {
        loadRequests();

        $("#categoryFilter").on("change", function () {
          const selectedCategory = $(this).val();
          loadRequests(selectedCategory);
        });

        $(document).on("click", ".view-detail", function (e) {
          e.preventDefault();
          const id = $(this).data("id");
          window.location.href = `/request/${"${id}"}`;
        });
      });
    </script>

    <%@ include file="../common/footer.jsp" %>
  </body>
</html>
