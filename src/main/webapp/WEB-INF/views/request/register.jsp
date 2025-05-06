<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>요청 등록 - RealCheck</title>
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
      <h3 class="text-center mb-4">요청 등록</h3>

      <!-- 지도 영역 -->
      <div
        id="map"
        style="
          width: 100%;
          height: 400px;
          margin-bottom: 20px;
          border: 1px solid #ccc;
        "
      >
        <!-- 향후 Kakao 지도 연동 예정 -->
      </div>

      <!-- 요청 등록 폼 -->
      <form id="requestForm">
        <div class="mb-3">
          <label for="category" class="form-label">요청 카테고리</label>
          <select class="form-select" id="category" name="category" required>
            <option value="">카테고리를 선택하세요</option>
            <option value="PARKING">🅿️ 주차 가능 여부</option>
            <option value="WAITING_STATUS">⏳ 대기 상태</option>
            <option value="STREET_VENDOR">🥟 노점 현황</option>
            <option value="PHOTO_REQUEST">📸 현장 사진 요청</option>
            <option value="BUSINESS_STATUS">🏪 가게 영업 여부</option>
            <option value="OPEN_SEAT">💺 좌석 여유</option>
            <option value="BATHROOM">🚻 화장실 여부</option>
            <option value="WEATHER_LOCAL">☁️ 현장 날씨</option>
            <option value="NOISE_LEVEL">🔊 소음 여부</option>
            <option value="FOOD_MENU">🍔 메뉴/음식</option>
            <option value="CROWD_LEVEL">👥 혼잡도</option>
            <option value="ETC">❓ 기타</option>
          </select>
        </div>

        <div class="mb-3">
          <label for="title" class="form-label">요청 제목</label>
          <input
            type="text"
            class="form-control"
            id="title"
            name="title"
            required
          />
        </div>

        <div class="mb-3">
          <label for="content" class="form-label">요청 내용</label>
          <textarea
            class="form-control"
            id="content"
            rows="3"
            required
          ></textarea>
        </div>

        <div class="mb-3">
          <label for="point" class="form-label">포인트 설정</label>
          <input
            type="number"
            class="form-control"
            id="point"
            min="1"
            value="10"
            required
          />
        </div>

        <!-- 장소 정보 -->
        <div class="mb-3">
          <label class="form-label">선택된 위치</label>
          <input
            type="text"
            class="form-control"
            id="placeId"
            name="placeId"
            readonly
            required
          />
          <input type="hidden" id="lat" name="lat" />
          <input type="hidden" id="lng" name="lng" />
        </div>

        <button type="submit" class="btn btn-primary w-100">요청 등록</button>
      </form>
    </div>

    <%@ include file="../common/footer.jsp" %>

    <script>
      $(document).ready(function () {
        // TODO: Kakao 지도 API 연동 시 마커 클릭 시 아래 값들을 설정
        $("#map").text("🗺 지도는 추후 Kakao Maps API로 연동 예정");

        const titlePlaceholderMap = {
          PARKING: "주차 가능한 공간이 있나요?",
          WAITING_STATUS: "대기 줄이 긴가요?",
          STREET_VENDOR: "붕어빵 노점 지금 하나요?",
          PHOTO_REQUEST: "현장 사진 부탁드릴게요!",
          BUSINESS_STATUS: "가게 문 열었나요?",
          OPEN_SEAT: "자리 여유 있나요?",
          BATHROOM: "화장실 이용 가능한가요?",
          WEATHER_LOCAL: "지금 비 오나요?",
          NOISE_LEVEL: "조용한 곳인가요?",
          FOOD_MENU: "오늘 점심 뭐 나와요?",
          CROWD_LEVEL: "많이 붐비나요?",
          ETC: "궁금한 현장의 정보를 자유롭게 요청하세요!",
        };

        const contentPlaceholderMap = {
          PARKING: "EX) 압구정 로데오 공영주차장에 지금 주차할 수 있나요?",
          WAITING_STATUS: "EX) 강남역 갓덴스시 현재 대기 줄 몇 명 정도인가요?",
          STREET_VENDOR: "EX) 테헤란로 농협 앞 붕어빵집 오늘도 운영하나요?",
          PHOTO_REQUEST:
            "EX) 부산 해운대 근처 날씨 확인 가능한 사진 부탁드려요!",
          BUSINESS_STATUS: "EX) 공휴일인데 오늘 가게 문 열었는지 궁금해요.",
          OPEN_SEAT: "EX) 스타벅스 서울대입구점 지금 좌석 여유 있나요?",
          BATHROOM: "EX) OO공원 근처에 화장실 이용 가능한 곳 있나요?",
          WEATHER_LOCAL: "EX) 홍대 앞 지금 비 오고 있나요?",
          NOISE_LEVEL:
            "EX) 신촌역 근처 조용한 카페 찾고 있어요. 주변 소음 어떤가요?",
          FOOD_MENU: "EX) 학교식당 오늘 점심 메뉴 아시는 분 계신가요?",
          CROWD_LEVEL: "EX) 석촌호수 산책로 지금 사람 많은가요?",
          ETC: "EX) 오늘 여의도 불꽃축제 사람들 이동 상황 어떤가요?",
        };

        // 카테고리 선택 시 placeholder 자동 설정
        $("#category").on("change", function () {
          const cat = $(this).val();
          $("#title").attr(
            "placeholder",
            titlePlaceholderMap[cat] || "예: 궁금한 점을 입력하세요"
          );
          $("#content").attr(
            "placeholder",
            contentPlaceholderMap[cat] || "요청 내용을 입력하세요"
          );
        });

        // 요청 등록 처리
        $("#requestForm").on("submit", function (e) {
          e.preventDefault();

          const requestData = {
            title: $("#title").val(),
            content: $("#content").val(),
            point: parseInt($("#point").val()),
            category: $("#category").val(),
            placeId: $("#placeId").val() || null,
            lat: $("#lat").val() ? parseFloat($("#lat").val()) : null,
            lng: $("#lng").val() ? parseFloat($("#lng").val()) : null,
          };

          $.ajax({
            url: "/api/request",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(requestData),
            success: function () {
              alert("요청이 등록되었습니다!");
              location.href = "/request/list";
            },
            error: function (xhr) {
              alert("등록 실패: " + xhr.responseText);
            },
          });
        });
      });
    </script>
  </body>
</html>
