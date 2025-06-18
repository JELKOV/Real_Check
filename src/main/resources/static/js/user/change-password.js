// ─────────────────────────────────────
// 비밀번호 변경 로직
// ─────────────────────────────────────
$(document).ready(function () {
  initPasswordForm();
});

// ─────────────────────────────────────
// [1] 초기화 함수
// ─────────────────────────────────────
function initPasswordForm() {
  bindPasswordInputValidation();
  bindPasswordFormSubmit();
}
// ─────────────────────────────────────
// [2] 정규식 및 유틸 상수
// ─────────────────────────────────────

// 비밀번호 정규식 (영문 대소문자, 숫자, 특수문자 포함 - 8~20자)
const passwordRegex =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_=+\[\]{};:,.<>/?]).{8,20}$/;

// ─────────────────────────────────────
// [3] 비밀번호 입력 유효성 검사 바인딩
// ─────────────────────────────────────

// 비밀번호 유효성 및 일치 검사 (입력 시)
function bindPasswordInputValidation() {
  $("#confirmPassword, #newPassword").on("keyup", validatePasswordMatch);
}

// 비밀번호 유효성 검사 유틸 함수
function validatePasswordMatch() {
  const pw = $("#newPassword").val();
  const cpw = $("#confirmPassword").val();

  // [1] 비밀번호 유효성 검사
  if (!passwordRegex.test(pw)) {
    showPasswordError(
      "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
    );
    return;
  } else {
    clearPasswordError();
  }

  // [2] 비밀번호 일치 확인
  if (pw !== cpw) {
    showPasswordError("비밀번호가 일치하지 않습니다.");
  } else {
    showPasswordSuccess("비밀번호가 유효하고 일치합니다.");
  }
}

// ─────────────────────────────────────
// [4] 메시지 표시 유틸
// ─────────────────────────────────────
function showPasswordError(msg) {
  $("#matchError")
    .text(msg)
    .removeClass("text-success")
    .addClass("text-danger");
}

function showPasswordSuccess(msg) {
  $("#matchError")
    .text(msg)
    .removeClass("text-danger")
    .addClass("text-success");
}

function clearPasswordError() {
  $("#matchError").text("").removeClass("text-danger text-success");
}

// ─────────────────────────────────────
// [5] 폼 제출 처리
// ─────────────────────────────────────

function bindPasswordFormSubmit() {
  $("#passwordForm").on("submit", function (e) {
    e.preventDefault();

    // [1] 입력된 값 가져오기
    const currentPassword = $("#currentPassword").val();
    const newPassword = $("#newPassword").val();
    const confirmPassword = $("#confirmPassword").val();

    // [2] 비밀번호 유효성 최종 검사 (서버로 전송 전)
    if (!passwordRegex.test(newPassword)) {
      alert(
        "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
      );
      return;
    }

    // [3] 비밀번호 일치 여부 확인
    if (newPassword !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    // [4] 비밀번호 변경 요청 (AJAX)
    $.ajax({
      url: "/api/user/password",
      method: "PUT",
      contentType: "application/json",
      data: JSON.stringify({ currentPassword, newPassword }),
      success: function () {
        alert("비밀번호가 변경되었습니다.");
        location.href = "/mypage";
      },
      error: function (xhr) {
        alert(xhr.responseText || "변경 실패");
      },
    });
  });
}