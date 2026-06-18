/** 金额格式化：小于1万显示原值，大于等于1万显示"万" */
export function formatMoney(val) {
  const num = Number(val) || 0
  if (num === 0) return '0.00'
  if (Math.abs(num) < 10000) return num.toFixed(2)
  return (num / 10000).toFixed(2) + '万'
}
