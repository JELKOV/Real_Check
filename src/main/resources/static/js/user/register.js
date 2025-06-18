let emailChecked = false;
let nicknameChecked = false;
document.addEventListener("DOMContentLoaded", initRegisterForm);

function initRegisterForm() {
  bindEvents();
  clearForm();
}

function bindEvents() {
  $("#loginLink").on("click", handleLoginLinkClick);
  $("#email").on("input", validateEmailInput);
  $("#email").on("blur", checkEmailDuplicate);
  $("#nickname").on("input", validateNicknameInput);
  $("#nickname").on("blur", checkNicknameDuplicate);
  $("#password, #confirmPassword").on("input", validatePasswordMatch);
  $("#registerForm").on("submit", handleFormSubmit);
}

// 입력 폼 초기화 함수
function clearForm() {
  $("#email, #nickname, #password, #confirmPassword")
    .val("")
    .attr("autocomplete", "off");
  $("#emailError, #nicknameError, #passwordMatchError").text("").hide();
  emailChecked = false;
  nicknameChecked = false;
}

// 로그인 링크 클릭 시 폼 초기화 후 이동
function handleLoginLinkClick(e) {
  e.preventDefault();
  clearForm();
  // 페이지 강제 이동 (history.replace로 이동)
  setTimeout(() => window.location.replace("/login"), 50);
}

// 이메일 유효성 검사 (입력 중에도 검사)
function validateEmailInput() {
  const email = $("#email").val().trim();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (email === "") {
    clearError("#emailError");
    emailChecked = false;
    return;
  }

  if (!emailRegex.test(email)) {
    showError("#emailError", "유효하지 않은 이메일 형식입니다.");
    emailChecked = false;
  } else {
    clearError("#emailError");
    emailChecked = false; // 중복확인 전 false 유지
  }
}

// 이메일 중복 확인 (입력 종료 후 - blur)
function checkEmailDuplicate() {
  const email = $(this).val().trim();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  // 1. 형식이 올바른 경우에만 중복 확인
  if (!emailRegex.test(email)) return;

  // 2. 중복 확인 AJAX 요청
  $.get("/api/user/check-email", { email }, function (exists) {
    if (exists) {
      showError("#emailError", "이미 사용 중인 이메일입니다.");
      emailChecked = false;
    } else {
      showSuccess("#emailError", "사용 가능한 이메일입니다.");
      emailChecked = true;
    }
  });
}

// 닉네임 실시간 유효성 검사 (2~20자 한글, 영문, 숫자)
function validateNicknameInput() {
  const nickname = $(this).val().trim();
  const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,20}$/;

  if (!nicknameRegex.test(nickname)) {
    showError("#nicknameError", "2~20자 한글, 영문, 숫자만 사용 가능합니다.");
    nicknameChecked = false;
    return;
  }
  clearError("#nicknameError");
  nicknameChecked = false; // 유효성 통과 후에도 중복 확인 전까지는 false
}

// 닉네임 중복 확인 (입력 종료 후 - blur)
function checkNicknameDuplicate() {
  const nickname = $(this).val().trim();
  const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,20}$/;

  // 형식이 올바른 경우에만 중복 확인
  if (!nicknameRegex.test(nickname)) return;

  $.get("/api/user/check-nickname", { nickname }, function (exists) {
    if (exists) {
      showError("#nicknameError", "이미 사용 중인 닉네임입니다.");
      nicknameChecked = false;
    } else {
      showSuccess("#nicknameError", "사용 가능한 닉네임입니다.");
      nicknameChecked = true;
    }
  });
}

// 비밀번호 실시간 유효성 및 일치 검사
function validatePasswordMatch() {
  const password = $("#password").val().trim();
  const confirmPassword = $("#confirmPassword").val().trim();
  if (password === "" && confirmPassword === "") {
    clearError("#passwordMatchError");
    return;
  }

  if (!isPasswordValid(password)) {
    showError(
      "#passwordMatchError",
      "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
    );
    return;
  }

  if (password !== confirmPassword) {
    showError("#passwordMatchError", "비밀번호가 일치하지 않습니다.");
    return;
  }

  // 모두 통과할 경우
  showSuccess("#passwordMatchError", "비밀번호가 유효하고 일치합니다.");
}

// 비밀번호 정규화
function isPasswordValid(password) {
  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_=+[\]{};:,.<>/?]).{8,20}$/;
  return passwordRegex.test(password);
}

function handleFormSubmit(event) {
  const password = $("#password").val();
  if (
    !emailChecked ||
    !nicknameChecked ||
    !passwordRegex.test(password) ||
    $("#passwordMatchError").hasClass("text-danger")
  ) {
    event.preventDefault();
    showToast("입력 정보를 확인해주세요.");
  }
}

// 에러 메시지 공통 유틸
function showError(selector, message) {
  $(selector)
    .text(message)
    .css("display", "block")
    .removeClass("text-success")
    .addClass("text-danger");
}

function showSuccess(selector, message) {
  $(selector)
    .text(message)
    .css("display", "block")
    .removeClass("text-danger")
    .addClass("text-success");
}

function clearError(selector) {
  $(selector).text("").hide().removeClass("text-danger text-success");
}

// Toast 표시 함수 (에러용)
function showToast(message) {
  $("#toastMessage").text(message);
  const toastElement = new bootstrap.Toast($("#errorToast")[0], {
    delay: 3000, // 자동 사라짐 시간 (3초)
    autohide: true,
  });
  toastElement.show();
}