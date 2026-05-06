import { http } from './http'

export async function getMe() {
  const { data } = await http.get('/auth/me')
  return data
}

export async function login(payload) {
  const { data } = await http.post('/auth/login', payload)
  return data
}

export async function register(payload) {
  const { data } = await http.post('/auth/register', payload)
  return data
}

export async function logout() {
  await http.post('/auth/logout')
}

export async function changePassword(currentPassword, newPassword) {
  const { data } = await http.put('/auth/me/password', { currentPassword, newPassword })
  return data
}

export async function updateName(fullName) {
  const { data } = await http.put('/auth/me/name', { fullName })
  return data
}

export async function updateAllergens(myAllergens) {
  const { data } = await http.put('/auth/me/allergens', { myAllergens })
  return data
}

export async function updateAvatar(avatarData) {
  const { data } = await http.put('/auth/me/avatar', { avatarData })
  return data
}

export async function getAllUsers() {
  const { data } = await http.get('/admin/users')
  return data
}

export async function deleteUser(userId) {
  const { data } = await http.delete(`/admin/users/${userId}`)
  return data
}

export async function updateUserAllergens(userId, myAllergens) {
  const { data } = await http.put(`/admin/users/${userId}/allergens`, { myAllergens })
  return data
}

export async function updateUserName(userId, fullName) {
  const { data } = await http.put(`/admin/users/${userId}/name`, { fullName })
  return data
}

export async function updateUserRole(userId, role) {
  const { data } = await http.put(`/admin/users/${userId}/role`, { role })
  return data
}

export async function resetUserPassword(userId) {
  const { data } = await http.put(`/admin/users/${userId}/reset-password`)
  return data
}

export async function setUserBlocked(userId, blocked) {
  const { data } = await http.put(`/admin/users/${userId}/blocked`, { blocked })
  return data
}

export async function getUserHistory(userId) {
  const { data } = await http.get(`/admin/users/${userId}/history`)
  return data
}

export async function clearUserHistory(userId) {
  const { data } = await http.delete(`/admin/users/${userId}/history`)
  return data
}

export async function deleteUserHistoryEntry(userId, historyId) {
  const { data } = await http.delete(`/admin/users/${userId}/history/${historyId}`)
  return data
}
