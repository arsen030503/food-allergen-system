import axios from 'axios'

const apiBase =
  document.querySelector('meta[name="api-base"]')?.content || '/api'

export const http = axios.create({
  baseURL: apiBase,
  withCredentials: true,
})

