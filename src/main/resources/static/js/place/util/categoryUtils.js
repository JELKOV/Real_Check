/**
 * 사용하는 곳
 * - place/community.js       : [1] 카테고리명 렌더링
 * - place/register.js        : [2] 드롭다운 렌더링, [3] 동적 필드 렌더링
 * - place/edit.js            : [2] 드롭다운 렌더링, [3] 동적 필드 렌더링
 * - place/place-register.js  : [1] 카테고리명 라벨 사용
 * - place/place-edit.js      : [1] 카테고리명 라벨 사용
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
// - 각 카테고리별로 사용자 입력받을 필드의 라벨, 이름, 타입, 옵션 정의
export const categoryFieldMap = {
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

// [1] 카테고리 코드에 대응하는 라벨 반환 함수
export function getCategoryLabel(code) {
  return categoryLabelMap[code] || code;
}

// [2] 커스텀 드롭다운 렌더링 함수 (select + ul li 구조)
// - 숨겨진 select 요소와 함께 사용하며, li 클릭 시 값 반영됨
export function renderCategoryDropdown($select, allowedTypes, current = null) {
  // 내부 전용 옵션 렌더링 함수
  function renderOptions(selectElement, types, currentValue) {
    selectElement.empty().append(`<option value="">-- 선택하세요 --</option>`);
    types.forEach((type) => {
      const selected = type === currentValue ? "selected" : "";
      selectElement.append(
        `<option value="${type}" ${selected}>${categoryLabelMap[type]}</option>`
      );
    });
  }

  renderOptions($select, allowedTypes, current);

  const $ul = $("#categoryList");
  const $label = $("#categoryToggle .label");

  // select 다시 초기화 (form 전송용)
  $select.empty().append(`<option value="">선택하세요</option>`);

  // 커스텀 드롭다운 초기화
  $ul.empty();

  allowedTypes.forEach((type) => {
    const label = getCategoryLabel(type);
    const isSelected = type === current;

    $select.append(
      `<option value="${type}" ${isSelected ? "selected" : ""}>${label}</option>`
    );
    $ul.append(`<li class="dropdown-item" data-value="${type}">${label}</li>`);

    if (isSelected) {
      $label.text(label);
    }
  });

  // 드롭다운 항목 클릭 이벤트
  $ul.off("click").on("click", "li", function () {
    const val = $(this).data("value");
    const label = $(this).text();

    $label.text(label);
    $select.val(val).trigger("change");
    $ul.hide();
  });

  // 토글 버튼 클릭
  $("#categoryToggle")
    .off("click")
    .on("click", function () {
      $ul.toggle();
    });

  // 외부 클릭 시 드롭다운 닫기
  $(document).on("click", function (e) {
    if (!$(e.target).closest("#categoryDropdown").length) {
      $ul.hide();
    }
  });
}

// [3] 카테고리 선택 시 보여줄 입력 필드 렌더링 함수
// - select 타입인 경우 커스텀 드롭다운으로 렌더링
// - 그 외 text, number 등은 기본 input 사용
export function renderAnswerFields(container, category, data = {}) {
  container.empty();

  const config = categoryFieldMap[category];
  if (!config || !config.name) {
    console.warn("지원하지 않는 카테고리거나 config.name 없음:", category);
    return;
  }

  const currentValue = data[config.name] ?? "";
  let html = `<div class="mb-3">`;

  if (config.type === "select") {
    // form 전송용 숨김 input
    html += `
      <label class="form-label d-block">${config.label}</label>
      <input type="hidden" name="${config.name}" id="${
      config.name
    }" value="${currentValue}">
      <div class="dropdown" id="dropdown-${config.name}">
        <button type="button"
          class="btn btn-outline-secondary dropdown-toggle w-100 d-flex justify-content-between align-items-center"
          id="toggle-${config.name}">
          <span class="label">${
            getOptionText(config, currentValue) || "선택하세요"
          }</span>
        </button>
        <ul class="dropdown-menu w-100" id="list-${config.name}">
          ${config.options
            .map(
              (opt) =>
                `<li class="dropdown-item" data-value="${opt.value}">${opt.text}</li>`
            )
            .join("")}
        </ul>
      </div>
    `;
  } else {
    html += `
      <label class="form-label" for="${config.name}">${config.label}</label>
      <input
        type="${config.type}"
        class="form-control"
        id="${config.name}"
        name="${config.name}"
        value="${currentValue}"
        required
      />
    `;
  }

  html += `</div>`;
  container.append(html);

  if (config.type === "select") bindCustomSelect(config.name);
}

// 선택된 값의 텍스트 반환 (예: 'true' → '가능')
function getOptionText(config, value) {
  const opt = config.options.find((o) => o.value === String(value));
  return opt ? opt.text : "";
}

// 커스텀 드롭다운 동작 바인딩
function bindCustomSelect(fieldName) {
  const $input = $(`#${fieldName}`);
  const $label = $(`#toggle-${fieldName} .label`);
  const $ul = $(`#list-${fieldName}`);

  $(`#toggle-${fieldName}`).on("click", function () {
    $ul.toggle();
  });

  $ul.on("click", "li", function () {
    const val = $(this).data("value");
    const label = $(this).text();
    $input.val(val);
    $label.text(label);
    $ul.hide();
  });

  $(document).on("click", function (e) {
    if (!$(e.target).closest(`#dropdown-${fieldName}`).length) {
      $ul.hide();
    }
  });
}
