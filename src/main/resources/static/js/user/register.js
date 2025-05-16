document.addEventListener("DOMContentLoaded", function () {
  let emailChecked = false;
  let nicknameChecked = false;

  // 로그인 링크 클릭 시 폼 초기화 후 이동
  $("#loginLink").on("click", function (e) {
    e.preventDefault();
    clearForm();
    setTimeout(() => {
      // 페이지 강제 이동 (history.replace로 이동)
      window.location.replace("/login");
    }, 50); // 짧은 딜레이로 초기화 후 이동
  });

  // 입력 폼 초기화 함수
  function clearForm() {
    $("#email, #nickname, #password, #confirmPassword")
      .val("")
      .attr("autocomplete", "off");
    $("#emailError, #nicknameError, #passwordMatchError").text("").hide();
    emailChecked = false;
    nicknameChecked = false;
  }

  // 이메일 유효성 검사 (입력 중에도 검사)
  $("#email").on("input", function () {
    const email = $(this).val().trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (email === "") {
      $("#emailError").text("").removeClass("text-danger text-success");
      emailChecked = false;
      return;
    }

    if (!emailRegex.test(email)) {
      $("#emailError")
        .text("유효하지 않은 이메일 형식입니다.")
        .css("display", "block")
        .removeClass("text-success")
        .addClass("text-danger");
      emailChecked = false;
    } else {
      $("#emailError").text("").removeClass("text-danger");
      emailChecked = false; // 형식이 맞아도 중복 확인 전까지는 false
    }
  });

  // 이메일 중복 확인 (입력 종료 후 - blur)
  $("#email").on("blur", function () {
    const email = $(this).val().trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // 1. 형식이 올바른 경우에만 중복 확인
    if (!emailRegex.test(email)) {
      return;
    }

    // 2. 중복 확인 AJAX 요청
    $.get("/api/user/check-email", { email }, function (exists) {
      if (exists) {
        $("#emailError")
          .text("이미 사용 중인 이메일입니다.")
          .css("display", "block")
          .removeClass("text-success")
          .addClass("text-danger");
        emailChecked = false;
      } else {
        $("#emailError")
          .text("사용 가능한 이메일입니다.")
          .removeClass("text-danger")
          .addClass("text-success");
        emailChecked = true;
      }
    });
  });

  // 닉네임 실시간 유효성 검사 (2~20자 한글, 영문, 숫자)
  $("#nickname").on("input", function () {
    const nickname = $(this).val().trim();
    const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,20}$/;

    if (!nicknameRegex.test(nickname)) {
      $("#nicknameError")
        .text("2~20자 한글, 영문, 숫자만 사용 가능합니다.")
        .css("display", "block")
        .removeClass("text-success")
        .addClass("text-danger");
      nicknameChecked = false;
      return;
    } else {
      $("#nicknameError").text("").removeClass("text-danger");
      nicknameChecked = false; // 유효성 통과 후에도 중복 확인 전까지는 false
    }
  });

  // 닉네임 중복 확인 (입력 종료 후 - blur)
  $("#nickname").on("blur", function () {
    const nickname = $(this).val().trim();
    const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,20}$/;

    // 형식이 올바른 경우에만 중복 확인
    if (!nicknameRegex.test(nickname)) {
      return;
    }

    $.get("/api/user/check-nickname", { nickname }, function (exists) {
      if (exists) {
        $("#nicknameError")
          .text("이미 사용 중인 닉네임입니다.")
          .css("display", "block")
          .removeClass("text-success")
          .addClass("text-danger");
        nicknameChecked = false;
      } else {
        $("#nicknameError")
          .text("사용 가능한 닉네임입니다.")
          .removeClass("text-danger")
          .addClass("text-success");
        nicknameChecked = true;
      }
    });
  });

  // 비밀번호 일치 실시간 확인
  $("#password, #confirmPassword").on("input", function () {
    const pw = $("#password").val();
    const cpw = $("#confirmPassword").val();
    if (pw !== cpw) {
      $("#passwordMatchError")
        .text("비밀번호가 일치하지 않습니다.")
        .css("display", "block")
        .removeClass("text-success")
        .addClass("text-danger");
    } else {
      $("#passwordMatchError").text("").removeClass("text-danger");
    }
  });

  // 전체 폼 유효성 검사 (제출 시점)
  $("#registerForm").on("submit", function (event) {
    if (
      !emailChecked ||
      !nicknameChecked ||
      $("#passwordMatchError").text() !== ""
    ) {
      event.preventDefault();
      showToast("입력 정보를 확인해주세요.");
    }
  });

  // Toast 표시 함수 (에러용)
  function showToast(message) {
    $("#toastMessage").text(message);
    const toastElement = new bootstrap.Toast($("#errorToast")[0], {
      delay: 3000, // 자동 사라짐 시간 (3초)
      autohide: true,
    });
    toastElement.show();
  }
});
