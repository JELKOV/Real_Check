<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>지도에서 등록 – RealCheck</n  <link rel="stylesheet" href="/css/style.css" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <style>#map { width: 100%; height: 70vh; }</style>
  <!-- 카카오 지도 JS SDK -->
  <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakao_js_key}&libraries=services"></script>
</head>
<body>
  <%@ include file="../common/header.jsp" %>
  <div class="container mt-4">
    <h3 class="mb-3">지도에서 대기 현황 등록</h3>
    <div id="map"></div>
  </div>

  <!-- 등록용 모달 -->
  <div class="modal fade" id="registerModal" tabindex="-1">
    <div class="modal-dialog">
      <form id="registerForm" class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">상태 로그 등록</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <input type="hidden" id="placeId" name="placeId" />
          <div class="mb-3">
            <label for="content" class="form-label">내용</label>
            <textarea id="content" name="content" class="form-control" required></textarea>
          </div>
          <div class="mb-3">
            <label for="waitCount" class="form-label">대기 인원</label>
            <input id="waitCount" name="waitCount" type="number" min="0" class="form-control" required/>
          </div>
          <div class="mb-3">
            <label for="imageUrl" class="form-label">이미지 URL</label>
            <input id="imageUrl" name="imageUrl" type="url" class="form-control"/>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
          <button type="submit" class="btn btn-primary">등록</button>
        </div>
      </form>
    </div>
  </div>

  <%@ include file="../common/footer.jsp" %>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
  $(function(){
    // 1) 지도 초기화
    var mapContainer = document.getElementById('map');
    var mapOption = { center: new kakao.maps.LatLng(37.566826, 126.9786567), level: 3 };
    var map = new kakao.maps.Map(mapContainer, mapOption);

    // 2) 현재 위치 마커
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos){
        var lat = pos.coords.latitude, lng = pos.coords.longitude;
        var locPosition = new kakao.maps.LatLng(lat, lng);
        map.setCenter(locPosition);
        new kakao.maps.Marker({ map: map, position: locPosition }).setMap(map);
      });
    }

    // 3) 장소 검색 및 마커 표시 (키워드 검색 예시)
    var ps = new kakao.maps.services.Places();
    ps.keywordSearch('', placesSearchCallback);

    function placesSearchCallback(data, status) {
      if (status === kakao.maps.services.Status.OK) {
        data.forEach(place => displayMarker(place));
      }
    }

    function displayMarker(place) {
      var marker = new kakao.maps.Marker({ map: map, position: new kakao.maps.LatLng(place.y, place.x) });
      kakao.maps.event.addListener(marker, 'click', function() {
        openRegister(place.id);
      });
    }

    // 4) 등록 폼 열기
    window.openRegister = function(placeId) {
      $('#placeId').val(placeId);
      new bootstrap.Modal($('#registerModal')).show();
    };

    // 5) 폼 제출 → API 호출
    $('#registerForm').submit(function(e) {
      e.preventDefault();
      var data = {
        placeId: +$('#placeId').val(),
        content: $('#content').val(),
        waitCount: +$('#waitCount').val(),
        imageUrl: $('#imageUrl').val() || null
      };
      $.ajax({
        url: '/api/status',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function() { alert('등록 성공!'); location.reload(); },
        error: function(xhr) { alert('등록 실패: ' + xhr.responseText); }
      });
    });
  });
  </script>
</body>
</html>
