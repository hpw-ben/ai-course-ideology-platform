import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export function getBindCode(teacherId) {
  return api.get(`/bind/code/${teacherId}`)
}

export function regenerateBindCode(teacherId) {
  return api.post(`/bind/code/regenerate/${teacherId}`)
}

export function bindStudent(studentId, code) {
  return api.post('/bind/bindStudent', { studentId, code })
}

export function getBindingStudents(teacherId) {
  return api.get(`/bind/students/${teacherId}`)
}

export function getBindingCount(teacherId) {
  return api.get(`/bind/count/${teacherId}`)
}

export function getBindingTeachers(studentId) {
  return api.get(`/bind/teachers/${studentId}`)
}

export function unbindStudent(teacherId, studentId) {
  return api.post('/bind/unbind', { teacherId, studentId })
}
