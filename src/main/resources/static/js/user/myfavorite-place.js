$(document).ready(function () {
  loadFavoritePlaces();
});

function loadFavoritePlaces() {
  $.get("/api/place/favorites", function (favorites) {
    const $list = $("#favoritePlaceList");
    const $empty = $("#emptyMessage");

    $list.empty(); // 초기화

    if (!favorites || favorites.length === 0) {
      $empty.show();
      return;
    }

    $empty.hide(); // 목록이 있으니 메시지 숨김

    favorites.forEach((p) => {
      const badge = p.approved
        ? '<span class="badge bg-success">승인됨</span>'
        : '<span class="badge bg-secondary">심사 중</span>';

      const communityBtn = p.approved
        ? `<a href="/place/community/${p.placeId}" class="btn btn-sm btn-outline-primary me-2">커뮤니티 보기</a>`
        : "";

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
    alert("즐겨찾기 정보를 불러오는 데 실패했습니다.");
  });
}

function unfavorite(placeId) {
  $.post(`/api/place/${placeId}/favorite`, function (msg) {
    alert(msg); // "즐겨찾기 해제 완료"
    loadFavoritePlaces(); // 다시 목록 갱신
  });
}
