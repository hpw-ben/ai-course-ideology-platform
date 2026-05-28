import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export function register(data) {
  return api.post('/auth/register', data)
}

export function login(data) {
  return api.post('/auth/login', data)
}

export function updateUsername(data) {
  return api.post('/auth/updateUsername', data)
}

export function updatePassword(data) {
  return api.post('/auth/updatePassword', data)
}

export function forgotPassword(data) {
  return api.post('/auth/forgotPassword', data)
}

export function forgotPasswordTeacher(data) {
  return api.post('/auth/forgotPasswordTeacher', data)
}

export function updateAvatar(data) {
  return api.post('/auth/updateAvatar', data)
}

export function updatePhone(data) {
  return api.post('/auth/updatePhone', data)
}