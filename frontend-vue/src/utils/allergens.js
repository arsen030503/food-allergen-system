export const allergenEmojis = {
  Gluten: '🌾',
  Wheat: '🌾',
  Crustaceans: '🦐',
  Eggs: '🥚',
  Fish: '🐟',
  Peanuts: '🥜',
  Soybeans: '🌱',
  Milk: '🥛',
  Nuts: '🌰',
  'Tree Nuts': '🌰',
  Celery: '🥬',
  Mustard: '🌼',
  Sesame: '🌻',
  'Sesame Seeds': '🌻',
  Sulphites: '⚗️',
  Lupin: '🌿',
  Molluscs: '🦑',
  Shellfish: '🦪',
}

export function getAllergenEmoji(name) {
  return allergenEmojis[name] || '⚠️'
}

export function parseUserAllergens(csv) {
  return (csv || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

export function toCsv(list) {
  return list.join(',')
}

