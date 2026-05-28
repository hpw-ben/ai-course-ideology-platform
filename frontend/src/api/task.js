import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 添加响应拦截器用于调试
api.interceptors.response.use(
  response => {
    console.log('task API 响应:', response)
    return response
  },
  error => {
    console.error('task API 错误:', error)
    return Promise.reject(error)
  }
)

export function createTask(data) {
  console.log('调用 createTask API, 数据:', data)
  return api.post('/task/create', data)
}

export function getTasksByTeacher(teacherId) {
  return api.get(`/task/teacher/${teacherId}`)
}

export function getTaskById(id) {
  return api.get(`/task/detail/${id}`)
}

export function getTaskByCode(code) {
  return api.get(`/task/code/${code}`)
}

export function deleteTask(id) {
  return api.delete(`/task/${id}`)
}

export function getTaskStats(teacherId) {
  return api.get(`/task/stats/${teacherId}`)
}

// 评论相关API
export function addTaskComment(data) {
  return api.post('/task/comment', data)
}

export function getTaskComments(taskId) {
  return api.get(`/task/${taskId}/comments`)
}

export function getTaskCommentsWithUser(taskId, userId, userType) {
  let qs = ''
  if (userId && userType) {
    qs = `?userId=${userId}&userType=${encodeURIComponent(userType)}`
  }
  return api.get(`/task/${taskId}/comments${qs}`)
}

// 点赞/取消点赞
export function toggleTaskCommentLike(commentId, data) {
  return api.post(`/task/comment/${commentId}/like`, data)
}

export function deleteTaskComment(id) {
  return api.delete(`/task/comment/${id}`)
}

export function togglePinTaskComment(id, taskId) {
  return api.post(`/task/comment/${id}/pin?taskId=${taskId}`)
}
