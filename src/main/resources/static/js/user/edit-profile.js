$(document).ready(function () {
  let nicknameChecked = false;
  let currentAjax = null;

  // 닉네임 실시간 유효성 및 중복 검사
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
      $("#updateForm button[type='submit']").prop("disabled", true);
      return;
    }

    // 중복 검사 전에 기존 요청 취소
    if (currentAjax) {
      currentAjax.abort();
    }

    // 중복 검사 (유효성 통과시)
    currentAjax = $.get(
      "/api/user/check-nickname",
      { nickname },
      function (exists) {
        if (exists) {
          $("#nicknameError")
            .text("이미 사용 중인 닉네임입니다.")
            .css("display", "block")
            .removeClass("text-success")
            .addClass("text-danger");
          nicknameChecked = false;
          $("#updateForm button[type='submit']").prop("disabled", true);
        } else {
          $("#nicknameError")
            .text("사용 가능한 닉네임입니다.")
            .css("display", "block")
            .removeClass("text-danger")
            .addClass("text-success");
          nicknameChecked = true;
          $("#updateForm button[type='submit']").prop("disabled", false);
        }
      }
    ).always(function () {
      currentAjax = null; // 요청 완료되면 초기화
    });
  });

  // 닉네임 수정 폼 제출
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
    $("#updateForm button[type='submit']").prop("disabled", true);

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
        $("#updateForm button[type='submit']").prop("disabled", false);
      },
    });
  });
});
