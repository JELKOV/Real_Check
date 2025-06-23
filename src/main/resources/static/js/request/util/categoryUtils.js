/**
 * 사용하는 곳
 * - request/register.js
 * - 1, 2, 3, 4
 * - request/lists.js
 * - 5
 */

// 기본 카테고리 설정
const allCategories = [
  { value: "PARKING", text: "🅿️ 주차 가능 여부" },
  { value: "WAITING_STATUS", text: "⏳ 대기 상태" },
  { value: "STREET_VENDOR", text: "🥟 노점 현황" },
  { value: "PHOTO_REQUEST", text: "📸 현장 사진 요청" },
  { value: "BUSINESS_STATUS", text: "🏪 가게 영업 여부" },
  { value: "OPEN_SEAT", text: "💺 좌석 여유" },
  { value: "BATHROOM", text: "🚻 화장실 여부" },
  { value: "WEATHER_LOCAL", text: "☁️ 현장 날씨" },
  { value: "NOISE_LEVEL", text: "🔊 소음 여부" },
  { value: "FOOD_MENU", text: "🍔 메뉴/음식" },
  { value: "CROWD_LEVEL", text: "👥 혼잡도" },
  { value: "ETC", text: "❓ 기타" },
];

// 카테고리별 타이틀 플레이스홀더
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

// 카테고리별 본문 플레이스홀더
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

// [1] 카테고리 드롭다운 초기화 함수
export function resetCategoryDropdown() {
  updateCategoryDropdown(allCategories.map((cat) => cat.value));
}

// [2] 카테고리 및 Placeholder 설정
export function updateCategoryDropdown(allowedCategories) {
  const categoryDropdown = $("#category").empty();
  categoryDropdown.append('<option value="">카테고리를 선택하세요</option>');
  allCategories.forEach((cat) => {
    if (allowedCategories.includes(cat.value)) {
      categoryDropdown.append(
        `<option value="${cat.value}">${cat.text}</option>`
      );
    }
  });
  resetPlaceholders();
}

// [3] Placeholder 초기화
export function resetPlaceholders() {
  const defaultTitle = "예: 궁금한 점을 입력하세요";
  const defaultContent = "요청 내용을 입력하세요";
  $("#title").attr("placeholder", defaultTitle);
  $("#content").attr("placeholder", defaultContent);
}

// [4] Placeholder 초기화 및 자동 설정
export function applyPlaceholdersForCategory(category) {
  $("#title").attr(
    "placeholder",
    titlePlaceholderMap[category] || "예: 궁금한 점을 입력하세요"
  );
  $("#content").attr(
    "placeholder",
    contentPlaceholderMap[category] || "요청 내용을 입력하세요"
  );
}


// 기본 카테고리 뱃지지
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


// [5] 카테고리 코드에 해당하는 라벨 반환
export function getCategoryLabel(code) {
  return categoryLabelMap[code] || code;
}
