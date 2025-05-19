// 비밀번호 정규식 (영문 대소문자, 숫자, 특수문자 포함 - 8~20자)
const passwordRegex =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_=+[\]{};:,.<>/?]).{8,20}$/;

// 비밀번호 유효성 및 일치 검사 (입력 시)
$("#confirmPassword, #newPassword").on("keyup", function () {
  const pw = $("#newPassword").val();
  const cpw = $("#confirmPassword").val();

  // [1] 비밀번호 유효성 검사
  if (!passwordRegex.test(pw)) {
    $("#matchError")
      .text(
        "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
      )
      .removeClass("text-success")
      .addClass("text-danger");
    return; // 유효하지 않으면 이후 검사 중단
  } else {
    $("#matchError").text("").removeClass("text-danger");
  }

  // [2] 비밀번호 일치 확인
  if (pw !== cpw) {
    $("#matchError")
      .text("비밀번호가 일치하지 않습니다.")
      .removeClass("text-success")
      .addClass("text-danger");
  } else {
    $("#matchError")
      .text("비밀번호가 유효하고 일치합니다.")
      .removeClass("text-danger")
      .addClass("text-success");
  }
});

// 비밀번호 변경 폼 제출
$("#passwordForm").on("submit", function (e) {
  e.preventDefault();

  // [1] 입력된 값 가져오기
  const currentPassword = $("#currentPassword").val();
  const newPassword = $("#newPassword").val();
  const confirmPassword = $("#confirmPassword").val();

  // [2] 비밀번호 유효성 최종 검사 (서버로 전송 전)
  if (!passwordRegex.test(newPassword)) {
    alert("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.");
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
    data: JSON.stringify({
      currentPassword, // 현재 비밀번호
      newPassword, // 새 비밀번호
    }),
    success: function () {
      // [5] 변경 성공 시 알림 및 페이지 이동
      alert("비밀번호가 변경되었습니다.");
      location.href = "/mypage";
    },
    error: function (xhr) {
      // [6] 변경 실패 시 오류 메시지 출력
      alert(xhr.responseText || "변경 실패");
    },
  });
});
