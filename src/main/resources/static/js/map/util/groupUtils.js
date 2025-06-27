/**
 * 사용하는 곳
 * - map/free-share.js
 * - 1
 * - map/request-list.js
 * - 1
 */

// [1] 중복된 좌표기준 묶기
export function groupByLocation(
  items,
  latKey = "lat",
  lngKey = "lng",
  precision = 5
) {
  const grouped = {};
  items.forEach((item) => {
    const lat = item[latKey].toFixed(precision);
    const lng = item[lngKey].toFixed(precision);
    const key = `${lat}_${lng}`;
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(item);
  });
  return grouped;
}
