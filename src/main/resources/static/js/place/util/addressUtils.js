/**
 * 사용하는 곳
 * - place/place-register.js
 * - 1, 2
 * - place/place-edit.js
 * - 1, 2
 */

// [1] 주소 검색 바인딩 함수
// - 입력 필드에 "Enter" 키 이벤트를 바인딩하여 주소 검색을 트리거함
export function bindSearchAddress(inputSelector, onFound) {
  $(inputSelector).on("keydown", function (e) {
    if (e.key === "Enter") {
      e.preventDefault();
      const query = $(this).val();
      searchAddress(query, onFound);
    }
  });
}

// [2]  Naver Maps Geocode API를 이용해 주소 → 좌표 검색
// - 검색 성공 시 첫 번째 결과를 콜백 함수에 전달
export function searchAddress(query, callback) {
  if (!query) return alert("주소를 입력하세요.");
  naver.maps.Service.geocode({ query }, function (status, response) {
    if (status !== naver.maps.Service.Status.OK)
      return alert("주소를 찾을 수 없습니다.");
    const item = response.v2.addresses[0];
    if (!item) return alert("주소 결과 없음");
    callback(item);
  });
}
