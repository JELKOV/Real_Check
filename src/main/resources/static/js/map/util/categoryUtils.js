/**
 * 사용하는 곳
 * - map/free-share.js
 * - 1
 * - map/nearby.js
 * - 1, 2
 */

// [0] 전역변수 카테고리 라벨 매핑 (카테고리 코드 → 텍스트)
const categoryLabelMap = {
  PARKING: "🅿️ 주차 가능 여부",
  WAITING_STATUS: "⏳ 대기 상태",
  STREET_VENDOR: "🥟 노점 현황",
  PHOTO_REQUEST: "📸 사진 요청",
  BUSINESS_STATUS: "🏪 가게 영업 여부",
  OPEN_SEAT: "💺 좌석 여유",
  BATHROOM: "🚻 화장실 여부",
  WEATHER_LOCAL: "☁️ 날씨 상태",
  NOISE_LEVEL: "🔊 소음 여부",
  FOOD_MENU: "🍔 메뉴/음식",
  CROWD_LEVEL: "👥 혼잡도",
  ETC: "❓ 기타",
};

// [0] 카테고리 필드 구성 정보 (동적 입력 필드용)
const categoryFieldMap = {
  PARKING: {
    label: "주차 가능 여부",
    name: "isParkingAvailable",
    type: "select",
    options: [
      { value: "true", text: "가능" },
      { value: "false", text: "불가능" },
    ],
  },
  WAITING_STATUS: {
    label: "대기 인원",
    name: "waitCount",
    type: "number",
  },
  CROWD_LEVEL: {
    label: "혼잡도",
    name: "crowdLevel",
    type: "number",
  },
  BATHROOM: {
    label: "화장실 여부",
    name: "hasBathroom",
    type: "select",
    options: [
      { value: "true", text: "있음" },
      { value: "false", text: "없음" },
    ],
  },
  FOOD_MENU: {
    label: "메뉴 정보",
    name: "menuInfo",
    type: "text",
  },
  WEATHER_LOCAL: {
    label: "날씨 상태",
    name: "weatherNote",
    type: "text",
  },
  STREET_VENDOR: {
    label: "노점 이름",
    name: "vendorName",
    type: "text",
  },
  PHOTO_REQUEST: {
    label: "사진 요청 메모",
    name: "photoNote",
    type: "text",
  },
  NOISE_LEVEL: {
    label: "소음 상태",
    name: "noiseNote",
    type: "text",
  },
  BUSINESS_STATUS: {
    label: "영업 여부",
    name: "isOpen",
    type: "select",
    options: [
      { value: "true", text: "영업 중" },
      { value: "false", text: "영업 종료" },
    ],
  },
  OPEN_SEAT: {
    label: "남은 좌석 수",
    name: "seatCount",
    type: "number",
  },
  ETC: {
    label: "기타 메모",
    name: "extra",
    type: "text",
  },
};

// [1] 카테고리 라벨 매핑 함수
export function getCategoryLabel(code) {
  return categoryLabelMap[code] || code;
}

// [2] 공지용 extraField 내용을 카테고리별로 포맷팅하여 출력
export function extractExtraFieldValueOnly(category, logDto) {
  const config = categoryFieldMap[category];
  if (!config) return "-";

  switch (config.type) {
    case "select":
      const val = logDto[config.name];
      return (
        config.options.find((opt) => opt.value === String(val))?.text || val
      );
    case "text":
      return logDto[config.name] || "-";
    case "boolean":
      return logDto[config.name] === true ? "예" : "아니오";
    default:
      return "-";
  }
}
