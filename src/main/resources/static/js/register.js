$(document).ready(function () {
  let placeMap = initializeMap("placeMap");
  let customMap = initializeMap("customMap");
  let placeMarker = null;
  let customMarker = null;
  let currentFocus = -1;

  function initializeMap(target, lat = 37.5665, lng = 126.978, zoom = 13) {
    return new naver.maps.Map(target, {
      center: new naver.maps.LatLng(lat, lng),
      zoom: zoom,
    });
  }

  // 버튼 스타일 전환 함수
  function toggleButtonStyles(active, inactive) {
    $(active).addClass("btn-primary").removeClass("btn-outline-primary");
    $(inactive).addClass("btn-outline-primary").removeClass("btn-primary");
  }

  // 선택된 장소 초기화
  function resetSelectedPlace() {
    $("#selectedPlaceName").val("");
    $("#placeId").val("");
    $("#lat").val("");
    $("#lng").val("");
    $(".place-item").removeClass("selected");
    currentFocus = -1;
  }

  // 장소 선택 방식 전환
  $("#btnPlace").click(() => {
    $("#placeSection").show();
    $("#customSection").hide();
    if (customMarker) customMarker.setMap(null);
    // resizeMap(placeMap);
    resetSelectedPlace();
    toggleButtonStyles("#btnPlace", "#btnCustom");
  });

  $("#btnCustom").click(() => {
    $("#placeSection").hide();
    $("#customSection").show();
    if (placeMarker) placeMarker.setMap(null);
    // resizeMap(customMap);
    resetSelectedPlace();
    toggleButtonStyles("#btnCustom", "#btnPlace");
  });

  // 마커 설정 (공식 장소)
  function setPlaceMarker(lat, lng, name = "", id = "") {
    if (!placeMarker) {
      placeMarker = new naver.maps.Marker({
        map: placeMap,
        position: new naver.maps.LatLng(lat, lng),
      });
    } else {
      placeMarker.setPosition(new naver.maps.LatLng(lat, lng));
    }

    placeMap.setCenter(new naver.maps.LatLng(lat, lng));

    // 선택된 장소 정보 설정
    $("#selectedPlaceName").val(name);
    $("#placeId").val(id);
    $("#lat").val(lat);
    $("#lng").val(lng);
    $("#placeSearch").val(name); // 검색창에 선택된 이름 자동 표시
  }

  // 장소 검색
  $("#placeSearch").on("input", function () {
    const query = $(this).val().trim();
    if (!query) {
      $("#placeSearchResults").empty().hide();
      resetSelectedPlace();
      return;
    }

    $.get("/api/place/search", { query }, function (places) {
      $("#placeSearchResults").empty().show();
      if (places.length === 0) {
        $("#placeSearchResults").append(
          `<li class="list-group-item">검색 결과가 없습니다.</li>`
        );
        return;
      }

      places.forEach((place) => {
        $("#placeSearchResults").append(`
          <li class="list-group-item place-item" 
              data-id="${place.id}" 
              data-lat="${place.lat}" 
              data-lng="${place.lng}"
              data-name="${place.name}">
            ${place.name}
          </li>
        `);
      });

      // 클릭 이벤트 바인딩 (중복 방지)
      $(".place-item")
        .off("click")
        .on("click", function () {
          selectPlace($(this));
        });

      // 마우스 호버 시 하이라이트 적용
      $(".place-item")
        .off("mouseenter")
        .on("mouseenter", function () {
          highlightItem($(this));
        });
    });
  });

  // 장소 선택 함수 (검색 목록에서)
  function selectPlace(item) {
    const lat = parseFloat(item.data("lat"));
    const lng = parseFloat(item.data("lng"));
    const placeName = item.data("name");
    const placeId = item.data("id");

    $(".place-item").removeClass("selected flash");
    item.addClass("selected flash");

    setPlaceMarker(lat, lng, placeName, placeId);

    // 검색 결과 자동 닫기
    $("#placeSearchResults").hide();
    $("#placeSearch").val(placeName);
  }

  // 강조된 아이템 하이라이트 (키보드 탐색 + 마우스)
  function highlightItem(item) {
    $(".place-item").removeClass("selected");
    item.addClass("selected");
  }

  // 키보드 탐색 (위/아래 + 엔터)
  $("#placeSearch").on("keydown", function (e) {
    const items = $(".place-item");
    if (items.length === 0) return;

    if (e.key === "ArrowDown") {
      e.preventDefault();
      currentFocus = (currentFocus + 1) % items.length;
      highlightItem($(items[currentFocus]));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      currentFocus = (currentFocus - 1 + items.length) % items.length;
      highlightItem($(items[currentFocus]));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (currentFocus >= 0) {
        selectPlace($(items[currentFocus]));
      }
    }
  });

  // 검색 결과 외부 클릭 시 자동 닫기
  $(document).click(function (e) {
    if (!$(e.target).closest("#placeSearch, #placeSearchResults").length) {
      $("#placeSearchResults").hide();
    }
  });

  // 사용자 지정 장소 - 지도 클릭 시 마커 설정
  naver.maps.Event.addListener(customMap, "click", function (e) {
    if (!$("#customSection").is(":visible")) return;
    const lat = e.coord.lat();
    const lng = e.coord.lng();

    if (!customMarker) {
      customMarker = new naver.maps.Marker({
        map: customMap,
        position: new naver.maps.LatLng(lat, lng),
      });
    } else {
      customMarker.setPosition(new naver.maps.LatLng(lat, lng));
    }

    $("#selectedPlaceName").val("사용자 지정 장소");
    $("#placeId").val("");
    $("#lat").val(lat);
    $("#lng").val(lng);
    $("#placeSearch").val("사용자 지정 장소");
    $("#placeSearchResults").hide(); // 사용자 지정 선택 시 검색 결과 닫기
  });

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
    PHOTO_REQUEST: "EX) 부산 해운대 근처 날씨 확인 가능한 사진 부탁드려요!",
    BUSINESS_STATUS: "EX) 공휴일인데 오늘 가게 문 열었는지 궁금해요.",
    OPEN_SEAT: "EX) 스타벅스 서울대입구점 지금 좌석 여유 있나요?",
    BATHROOM: "EX) OO공원 근처에 화장실 이용 가능한 곳 있나요?",
    WEATHER_LOCAL: "EX) 홍대 앞 지금 비 오고 있나요?",
    NOISE_LEVEL: "EX) 신촌역 근처 조용한 카페 찾고 있어요. 주변 소음 어떤가요?",
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
