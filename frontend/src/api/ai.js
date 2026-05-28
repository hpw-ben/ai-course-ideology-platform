import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' }
})

export function generateTaskQuiz(data) {
  return api.post('/ai/quiz/generate', data)
}

export function generateMaterialQuizzes(data) {
  return api.post('/ai/quiz/generate-material', data)
}
