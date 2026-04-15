import { http } from './http'

export async function analyze(payload) {
  const { data } = await http.post('/analyze', payload)
  return data
}

export async function getAllergens() {
  const { data } = await http.get('/allergens')
  return data
}

export async function getHistory() {
  const { data } = await http.get('/history')
  return data
}

export async function clearHistory() {
  const { data } = await http.delete('/history')
  return data
}

