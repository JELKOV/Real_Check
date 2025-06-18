// 초기 진입점 등록
document.addEventListener("DOMContentLoaded", initDeletionCountdownTimer);

// 삭제 타이머 초기화 진입점
function initDeletionCountdownTimer() {
  const timerElement = document.getElementById("timer");
  const deletionScheduledAt = timerElement?.getAttribute("data-deletion");

  // [1] 삭제 예정 시간 확인
  if (!timerElement || !deletionScheduledAt) {
    timerElement.innerHTML =
      "<span class='text-danger'>삭제 예정 시간이 설정되지 않았습니다.</span>";
    return;
  }

  // [2] 삭제 예정 시간 파싱 (ISO 문자열 → Date 객체)
  const deletionDate = parseDeletionDate(deletionScheduledAt);
  if (!deletionDate) {
    timerElement.innerHTML =
      "<span class='text-danger'>잘못된 삭제 예정 시간 형식입니다.</span>";
    return;
  }

  // [3] 타이머 시작 (1초마다 갱신)
  updateCountdown(deletionDate);
  setInterval(() => updateCountdown(deletionDate), 1000);
}

// ISO 문자열 → Date 객체 변환
function parseDeletionDate(isoString) {
  const date = new Date(isoString);
  return isNaN(date.getTime()) ? null : date;
}

// 타이머 업데이트 함수 (남은 시간 계산 및 표시)
function updateCountdown(deletionDate) {
  // [1] 현재 시간과 삭제 예정 시간 차이 계산
  const now = new Date();
  const diff = deletionDate - now;

  // [2] 타이머 요소 참조
  const timerElement = document.getElementById("timer");
  const daysElement = document.getElementById("days");
  const hoursElement = document.getElementById("hours");
  const minutesElement = document.getElementById("minutes");
  const secondsElement = document.getElementById("seconds");

  // [3] 타이머 요소 확인 (존재하지 않으면 오류 로그)
  if (!daysElement || !hoursElement || !minutesElement || !secondsElement) {
    console.error("타이머 요소를 찾을 수 없습니다.");
    return;
  }

  // [4] 삭제 예정 시간이 이미 도래한 경우
  if (diff <= 0) {
    document.getElementById("timer").innerHTML =
      "<span class='text-danger'>삭제 예약 시간이 도래했습니다.</span>";
    return;
  }

  // [5] 남은 시간 계산 (일, 시, 분, 초)
  const { days, hours, minutes, seconds } = calculateTimeComponents(diff);

  // [6] 타이머 표시 업데이트
  daysElement.textContent = days;
  hoursElement.textContent = hours;
  minutesElement.textContent = minutes;
  secondsElement.textContent = seconds;
}

// 시간 차이 → 일, 시, 분, 초로 변환
function calculateTimeComponents(milliseconds) {
  const days = Math.floor(milliseconds / (1000 * 60 * 60 * 24));
  const hours = Math.floor(
    (milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
  );
  const minutes = Math.floor((milliseconds % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((milliseconds % (1000 * 60)) / 1000);
  return { days, hours, minutes, seconds };
}
