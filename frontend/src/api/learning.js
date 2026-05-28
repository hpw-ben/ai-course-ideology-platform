import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 记录进入学习
export function recordEnter(data) {
  return api.post('/learning-record/enter', data)
}

// 记录离开学习
export function recordLeave(data) {
  return api.post('/learning-record/leave', data)
}

export function recordHeartbeat(data) {
  return api.post('/learning-record/heartbeat', data)
}

export function submitTaskQuiz(data) {
  return api.post('/learning-record/task/submit-quiz', data)
}

export function getTaskCheckinStatus(studentId, taskId) {
  return api.get(`/learning-record/task/checkin-status?studentId=${studentId}&taskId=${taskId}`)
}

export function checkinTask(data) {
  return api.post('/learning-record/task/checkin', data)
}

export function saveTaskViewpointAndNote(data) {
  return api.post('/learning-record/task/viewpoint-note', data)
}

export function getLearningRecordDetail(id) {
  return api.get(`/learning-record/detail/${id}`)
}

export function downloadReportPdf(recordId) {
  return api.get(`/learning-record/report/${recordId}/pdf`, { responseType: 'blob' })
}

export function markTaskMaterialCompleted(data) {
  return api.post('/learning-record/task/material/complete', data)
}

export function getTaskProgress(studentId, taskId) {
  return api.get(`/learning-record/task/progress?studentId=${studentId}&taskId=${taskId}`)
}

export function getStudentTaskCompletions(studentId, status) {
  const s = status ? String(status) : ''
  const qs = s ? `?studentId=${studentId}&status=${encodeURIComponent(s)}` : `?studentId=${studentId}`
  return api.get(`/learning-record/task/completions${qs}`)
}

// 获取学生学习记录
export function getLearningRecords(studentId) {
  return api.get(`/learning-record/student/${studentId}`)
}

// 获取学生学习统计
export function getLearningStats(studentId) {
  return api.get(`/learning-record/stats/${studentId}`)
}

export function getStudentDiscussionParticipation(studentId) {
  return api.get(`/learning-record/student/${studentId}/discussion-participation`)
}

export function getStudentMaterialFootprints(studentId) {
  return api.get(`/learning-record/student/${studentId}/material-footprints`)
}

export function getTeacherBoundParticipation(params) {
  return api.get('/learning-record/teacher/bound-participation', { params })
}
