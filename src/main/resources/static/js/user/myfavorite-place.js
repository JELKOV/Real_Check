// ─────────────────────────────────────
// [1] 페이지 로딩 시 즐겨찾기 목록 불러오기
// ─────────────────────────────────────
$(document).ready(function () {
  loadFavoritePlaces(); // 첫 진입 시 자동 로딩
});

// ─────────────────────────────────────
// [2] 즐겨찾기 장소 목록 불러오기
// ─────────────────────────────────────
function loadFavoritePlaces() {
  $.get("/api/place/favorites", function (favorites) {
    const $list = $("#favoritePlaceList"); // 목록을 담을 컨테이너
    const $empty = $("#emptyMessage"); // 비어있을 때 보여줄 메시지

    $list.empty(); // 이전 목록 초기화

    // 즐겨찾기 내역이 없을 경우
    if (!favorites || favorites.length === 0) {
      $empty.show(); // "즐겨찾기한 장소가 없습니다" 메시지 출력
      return;
    }

    $empty.hide(); // 데이터가 있으므로 안내 메시지 숨김

    // 각 장소에 대해 카드 형식으로 렌더링
    favorites.forEach((p) => {
      // 승인 여부 뱃지
      const badge = p.approved
        ? '<span class="badge bg-success">승인됨</span>'
        : '<span class="badge bg-secondary">심사 중</span>';

      // 승인된 장소만 커뮤니티 버튼 제공
      const communityBtn = p.approved
        ? `<a href="/place/community/${p.placeId}" class="btn btn-sm btn-outline-primary me-2">커뮤니티 보기</a>`
        : "";

      // 카드 HTML 구성 및 추가
      $list.append(`
        <div class="col-md-6">
          <div class="card shadow-sm">
            <div class="card-body">
              <h5 class="card-title">${p.placeName} ${badge}</h5>
              <p class="card-text">${p.address}</p>
              <div class="d-flex justify-content-end">
                ${communityBtn}
                <button class="btn btn-sm btn-outline-danger" onclick="unfavorite(${p.placeId})">즐겨찾기 해제</button>
              </div>
            </div>
          </div>
        </div>
      `);
    });
  }).fail(() => {
    // AJAX 실패 시 알림
    alert("즐겨찾기 정보를 불러오는 데 실패했습니다.");
  });
}

// ─────────────────────────────────────
// [3] 즐겨찾기 해제 요청
// ─────────────────────────────────────
function unfavorite(placeId) {
  $.post(`/api/place/${placeId}/favorite`, function (msg) {
    alert(msg); // 서버 응답 메시지 출력 (예: "즐겨찾기 해제 완료")
    loadFavoritePlaces(); // 해제 후 목록 다시 갱신
  });
}
