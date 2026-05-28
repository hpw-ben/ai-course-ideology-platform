import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 管理员登录
export function adminLogin(username, password) {
  return api.post('/admin/login', { username, password })
}

export function getPlatformStats() {
  return api.get('/admin/stats/platform')
}

// ========== 轮播图管理 ==========
export function getAllCarousels(page) {
  return api.get('/admin/carousel/list', { params: { page } })
}

export function getActiveCarousels(page) {
  return api.get('/admin/carousel/active', { params: { page } })
}

export function createCarousel(data) {
  return api.post('/admin/carousel/create', data)
}

export function updateCarousel(data) {
  return api.post('/admin/carousel/update', data)
}

export function deleteCarousel(id) {
  return api.delete(`/admin/carousel/${id}`)
}

// ========== 科学栏目管理 ==========
export function getAllScienceItems(module) {
  return api.get('/admin/science/list', { params: { module } })
}

export function getActiveScienceItems(module) {
  return api.get('/admin/science/active', { params: { module } })
}

export function createScienceItem(data) {
  return api.post('/admin/science/create', data)
}

export function updateScienceItem(data) {
  return api.post('/admin/science/update', data)
}

export function deleteScienceItem(id) {
  return api.delete(`/admin/science/${id}`)
}

// ========== 新闻管理 ==========
export function getAllNews() {
  return api.get('/admin/news/list')
}

export function getAllNewsByCategory(category) {
  return api.get('/admin/news/list', { params: { category } })
}

export function getPublishedNews() {
  return api.get('/admin/news/published')
}

export function getPublishedNewsByCategory(category) {
  return api.get('/admin/news/published', { params: { category } })
}

export function getLatestNews(limit = 5) {
  return api.get('/admin/news/latest', { params: { limit } })
}

export function getLatestNewsByCategory(limit = 5, category) {
  return api.get('/admin/news/latest', { params: { limit, category } })
}

export function getNewsById(id) {
  return api.get(`/admin/news/${id}`)
}

export function createNews(data) {
  return api.post('/admin/news/create', data)
}

export function updateNews(data) {
  return api.post('/admin/news/update', data)
}

export function deleteNews(id) {
  return api.delete(`/admin/news/${id}`)
}

// ========== 素材审核 ==========
export function getPendingMaterials() {
  return api.get('/admin/material/pending')
}

export function getAllMaterialsForAdmin() {
  return api.get('/admin/material/all')
}

export function approveMaterial(id) {
  return api.post(`/admin/material/approve/${id}`)
}

export function rejectMaterial(id, reason) {
  return api.post(`/admin/material/reject/${id}`, { reason })
}

export function revokeMaterial(id, reason) {
  return api.post(`/admin/material/revoke/${id}`, { reason })
}

export function setMaterialShelfStatus(id, shelfStatus) {
  return api.post(`/admin/material/shelf/${id}`, { shelfStatus })
}

export function updateMaterialCategory(id, category) {
  return api.post(`/admin/material/category/${id}`, { category })
}

export function getMaterialDetail(id) {
  return api.get(`/admin/material/detail/${id}`)
}

// ========== 人物事迹（新时代领头人）素材 ==========
export function createLeaderMaterial(data) {
  return api.post('/admin/leader-material/create', data)
}

export function getAllTags() {
  return api.get('/material/tags')
}

export function getAllTagsByCategory(category) {
  return api.get('/material/tags', { params: { category } })
}

export function createTag(name, category) {
  return api.post('/material/tag', { name, category })
}

export function updateTagCategory(id, category) {
  return api.post('/material/tag/category', { id, category })
}

// ========== 讨论评论管理 ==========
export function deleteDiscussionComment(id, reason) {
  return api.post(`/admin/discussion/comment/delete/${id}`, { reason })
}

// ========== 时事论坛话题管理 ==========
export function getAllTopics() {
  return api.get('/admin/topic/list')
}

export function getActiveTopics() {
  return api.get('/admin/topic/active')
}

export function getTopicById(id) {
  return api.get(`/admin/topic/${id}`)
}

export function getTopicByCode(code) {
  return api.get(`/admin/topic/code/${code}`)
}

export function createTopic(data) {
  return api.post('/admin/topic/create', data)
}

export function updateTopic(data) {
  return api.post('/admin/topic/update', data)
}

export function deleteTopic(id) {
  return api.delete(`/admin/topic/${id}`)
}

// ========== 话题评论 ==========
export function getTopicComments(topicId) {
  return api.get(`/admin/topic/${topicId}/comments`)
}

export function addTopicComment(data) {
  return api.post('/admin/topic/comment', data)
}

export function deleteTopicComment(id, reason) {
  return api.post(`/admin/topic/comment/delete/${id}`, { reason })
}

export function toggleTopicCommentPin(id, topicId) {
  return api.post(`/admin/topic/comment/${id}/pin`, null, { params: { topicId } })
}

// ========== 通知相关 ==========
export function getUserNotifications(userId, userType) {
  return api.get(`/admin/notification/${userId}/${userType}`)
}

export function getUnreadCount(userId, userType) {
  return api.get(`/admin/notification/unread/${userId}/${userType}`)
}

export function markNotificationAsRead(id) {
  return api.post(`/admin/notification/read/${id}`)
}

export function markAllNotificationsAsRead(userId, userType) {
  return api.post(`/admin/notification/readAll/${userId}/${userType}`)
}
