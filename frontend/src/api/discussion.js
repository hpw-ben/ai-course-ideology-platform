import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 创建讨论
export function createDiscussion(data) {
  return api.post('/discussion/create', data)
}

// 获取教师的讨论列表
export function getDiscussionsByTeacher(teacherId) {
  return api.get(`/discussion/teacher/${teacherId}`)
}

// 根据ID获取讨论
export function getDiscussionById(id) {
  return api.get(`/discussion/${id}`)
}

// 根据讨论码获取讨论
export function getDiscussionByCode(code) {
  return api.get(`/discussion/code/${code}`)
}

// 更新讨论
export function updateDiscussion(data) {
  return api.post('/discussion/update', data)
}

// 删除讨论
export function deleteDiscussion(id) {
  return api.delete(`/discussion/${id}`)
}

// 获取统计数据
export function getDiscussionStats(teacherId) {
  return api.get(`/discussion/stats/${teacherId}`)
}

// 添加评论
export function addComment(data) {
  return api.post('/discussion/comment', data)
}

// 获取讨论的评论列表
export function getComments(discussionId, userId, userType) {
  let qs = ''
  if (userId && userType) {
    qs = `?userId=${userId}&userType=${encodeURIComponent(userType)}`
  }
  return api.get(`/discussion/${discussionId}/comments${qs}`)
}

// 点赞/取消点赞
export function toggleLikeComment(commentId, data) {
  return api.post(`/discussion/comment/${commentId}/like`, data)
}

// 删除评论
export function deleteComment(id) {
  return api.delete(`/discussion/comment/${id}`)
}

// 置顶/取消置顶评论
export function togglePinComment(id, discussionId) {
  return api.post(`/discussion/comment/${id}/pin?discussionId=${discussionId}`)
}

export function getTeacherDiscussionReport(teacherId, discussionId) {
  return api.get(`/discussion/report/teacher?teacherId=${teacherId}&discussionId=${discussionId}`)
}

export function downloadTeacherDiscussionReportPdf(teacherId, discussionId) {
  return api.get(`/discussion/report/teacher/pdf?teacherId=${teacherId}&discussionId=${discussionId}`, { responseType: 'blob' })
}

export function getDiscussionHotWords(teacherId, discussionId, limit = 10) {
  return api.get(`/discussion/hotwords?teacherId=${teacherId}&discussionId=${discussionId}&limit=${limit}`)
}

export function downloadTeacherSpeechOnlyPdf(teacherId, discussionId) {
  return api.get(`/discussion/report/teacher-speech/pdf?teacherId=${teacherId}&discussionId=${discussionId}`, { responseType: 'blob' })
}

export function getStudentDiscussionReport(code, studentId) {
  return api.get(`/discussion/report/student?code=${encodeURIComponent(code)}&studentId=${studentId}`)
}

export function downloadStudentDiscussionReportPdf(code, studentId) {
  return api.get(`/discussion/report/student/pdf?code=${encodeURIComponent(code)}&studentId=${studentId}`, { responseType: 'blob' })
}

export function downloadStudentDiscussionContentPdf(code, studentId) {
  return api.get(`/discussion/content/student/pdf?code=${encodeURIComponent(code)}&studentId=${studentId}`, { responseType: 'blob' })
}
