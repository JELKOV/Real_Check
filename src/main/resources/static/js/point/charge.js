$(document).ready(function () {
  $("#chargeBtn").on("click", function () {
    const amount = parseInt($("#chargeAmount").val());

    if (isNaN(amount) || amount <= 0) {
      alert("1 이상의 숫자를 입력해주세요.");
      return;
    }

    $.post("/api/point/charge", { amount })
      .done(() => {
        $("#chargeMsg").text("충전이 완료되었습니다!").show();
        $("#chargeAmount").val(""); // 입력창 초기화
      })
      .fail((res) => {
        const msg = res.responseText || "충전에 실패했습니다.";
        alert("에러: " + msg);
      });
  });
});
