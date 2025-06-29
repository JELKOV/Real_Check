$(document).ready(function () {
  $("#cashBtn").on("click", function () {
    const amount = parseInt($("#cashAmount").val());
    if (isNaN(amount) || amount < 100) {
      alert("최소 100 포인트 이상 입력해주세요.");
      return;
    }

    $.post("/api/point/cash", { amount })
      .done(() => {
        $("#cashMsg").text("환전이 완료되었습니다!").show();
        $("#cashAmount").val("");
      })
      .fail((res) => {
        alert(" " + (res.responseText || "환전에 실패했습니다."));
      });
  });
});
