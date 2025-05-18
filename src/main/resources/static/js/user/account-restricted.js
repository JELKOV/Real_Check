document.addEventListener("DOMContentLoaded", function () {
  const timerElement = document.getElementById("timer");
  const deletionScheduledAt = timerElement?.getAttribute("data-deletion");

  if (!deletionScheduledAt) {
    timerElement.innerHTML =
      "<span class='text-danger'>삭제 예정 시간이 설정되지 않았습니다.</span>";
    return;
  }

  const deletionDate = new Date(deletionScheduledAt);
  if (isNaN(deletionDate.getTime())) {
    timerElement.innerHTML =
      "<span class='text-danger'>잘못된 삭제 예정 시간 형식입니다.</span>";
    return;
  }

  updateCountdown(deletionDate);
  setInterval(() => updateCountdown(deletionDate), 1000);
});

function updateCountdown(deletionDate) {
  const now = new Date();
  const diff = deletionDate - now;

  const daysElement = document.getElementById("days");
  const hoursElement = document.getElementById("hours");
  const minutesElement = document.getElementById("minutes");
  const secondsElement = document.getElementById("seconds");

  if (!daysElement || !hoursElement || !minutesElement || !secondsElement) {
    console.error("타이머 요소를 찾을 수 없습니다.");
    return;
  }

  if (diff <= 0) {
    document.getElementById("timer").innerHTML =
      "<span class='text-danger'>삭제 예약 시간이 도래했습니다.</span>";
    return;
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);

  daysElement.textContent = days;
  hoursElement.textContent = hours;
  minutesElement.textContent = minutes;
  secondsElement.textContent = seconds;
}
