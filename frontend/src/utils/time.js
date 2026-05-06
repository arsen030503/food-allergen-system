export function getTimeAgo(value, t) {
  if (!value) return t('common.unknownTime')
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return t('common.unknownTime')

  const diff = Date.now() - date.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return t('time.justNow')
  if (mins < 60) return t('time.minutesAgo', { count: mins })
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return t('time.hoursAgo', { count: hrs })
  return t('time.daysAgo', { count: Math.floor(hrs / 24) })
}

