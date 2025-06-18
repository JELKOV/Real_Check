let nicknameChecked = false;
let currentAjax = null;

$(document).ready(function () {
  initNicknameUpdateForm();
});

// ─────────────────────────────────────
// [1] 초기화 함수
// ─────────────────────────────────────
function initNicknameUpdateForm() {
  bindNicknameInputHandler();
  bindNicknameSubmitHandler();
}

// ─────────────────────────────────────
// [2] 닉네임 입력 이벤트 처리
// ─────────────────────────────────────

// 닉네임 실시간 유효성 및 중복 검사
function bindNicknameInputHandler() {
  $("#nickname").on("input", function () {
    const nickname = $(this).val().trim();
    const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,20}$/;

    if (!nicknameRegex.test(nickname)) {
      showNicknameError("2~20자 한글, 영문, 숫자만 사용 가능합니다.");
      nicknameChecked = false;
      disableSubmit();
      return;
    }

    if (currentAjax) currentAjax.abort();

    currentAjax = $.get(
      "/api/user/check-nickname",
      { nickname },
      function (exists) {
        if (exists) {
          showNicknameError("이미 사용 중인 닉네임입니다.");
          nicknameChecked = false;
          disableSubmit();
        } else {
          showNicknameSuccess("사용 가능한 닉네임입니다.");
          nicknameChecked = true;
          enableSubmit();
        }
      }
    ).always(() => {
      currentAjax = null;
    });
  });
}

// ─────────────────────────────────────
// [3] 에러/성공 메시지 출력
// ─────────────────────────────────────

function showNicknameError(message) {
  $("#nicknameError")
    .text(message)
    .css("display", "block")
    .removeClass("text-success")
    .addClass("text-danger");
}

function showNicknameSuccess(message) {
  $("#nicknameError")
    .text(message)
    .css("display", "block")
    .removeClass("text-danger")
    .addClass("text-success");
}

// ─────────────────────────────────────
// [4] 버튼 제어 함수
// ─────────────────────────────────────
function disableSubmit() {
  $("#updateForm button[type='submit']").prop("disabled", true);
}

function enableSubmit() {
  $("#updateForm button[type='submit']").prop("disabled", false);
}
// ─────────────────────────────────────
// [5] 폼 제출 처리
// ─────────────────────────────────────
function bindNicknameSubmitHandler() {
  $("#updateForm").on("submit", function (e) {
    e.preventDefault();

    const nickname = $("#nickname").val().trim();
    const nicknameError = $("#nicknameError").text();

    // 닉네임 중복 여부 최종 확인
    if (!nicknameChecked || nicknameError.includes("이미 사용 중인")) {
      alert("닉네임을 확인해주세요.");
      return;
    }

    // 버튼 비활성화 (중복 클릭 방지)
    disableSubmit();

    $.ajax({
      url: "/api/user/update",
      method: "PUT",
      contentType: "application/json",
      data: JSON.stringify({ nickname }),
      success: function () {
        alert("닉네임이 성공적으로 수정되었습니다.");
        location.href = "/login";
      },
      error: function (xhr) {
        alert(xhr.responseText);
        // 오류 발생 시 버튼 다시 활성화
        enableSubmit();
      },
    });
  });
}
