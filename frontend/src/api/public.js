import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 统一文件上传接口
export function uploadFile(file, category = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  return api.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取启用的轮播图
export function getActiveCarousels(page) {
  return api.get('/public/carousel/active', { params: { page } })
}

// 获取启用的科学栏目内容
export function getActiveScienceItems(module) {
  return api.get('/public/science/active', { params: { module } })
}

export function getScienceItemById(id) {
  return api.get(`/public/science/${id}`)
}

// 获取已发布的新闻
export function getPublishedNews(category) {
  return api.get('/public/news/published', { params: { category } })
}

// 获取最新的新闻
export function getLatestNews(limit = 5, category) {
  return api.get('/public/news/latest', { params: { limit, category } })
}

// 获取新闻详情
export function getNewsById(id) {
  return api.get(`/public/news/${id}`)
}

// 获取新闻评论列表
export function getNewsComments(newsId) {
  return api.get(`/public/news/${newsId}/comments`)
}

export function getNewsCommentsWithUser(newsId, userId, userType) {
  let qs = ''
  if (userId && userType) {
    qs = `?userId=${userId}&userType=${encodeURIComponent(userType)}`
  }
  return api.get(`/public/news/${newsId}/comments${qs}`)
}

// 新闻评论点赞/取消点赞
export function toggleNewsCommentLike(commentId, data) {
  return api.post(`/public/news/comment/${commentId}/like`, data)
}

// 添加新闻评论
export function addNewsComment(data) {
  return api.post('/public/news/comment', data)
}

// 获取新闻评论数
export function getNewsCommentCount(newsId) {
  return api.get(`/public/news/${newsId}/comment-count`)
}

// 获取活跃的论坛话题
export function getActiveTopics() {
  return api.get('/public/topic/active')
}

// 获取热门话题（按浏览量排序，合并教师讨论与管理员话题）
export function getHotTopics(limit = 5) {
  return api.get('/public/hot-topics', { params: { limit } })
}

// 新时代领头人：获取标签
export function getLeaderTags() {
  return api.get('/public/leader/tags')
}

// 新时代领头人：获取人物事迹列表（可选 tagName 过滤）
export function getLeaderList(tag) {
  const params = {}
  if (tag != null && String(tag).trim() !== '') params.tag = tag
  return api.get('/public/leader/list', { params })
}

// 新时代领头人：获取人物事迹详情
export function getLeaderDetail(id) {
  return api.get(`/public/leader/${id}`)
}

// 根据话题码获取话题详情
export function getTopicByCode(code) {
  return api.get(`/public/topic/code/${code}`)
}

// 获取话题评论
export function getTopicComments(topicId, userId, userType) {
  let qs = ''
  if (userId && userType) {
    qs = `?userId=${userId}&userType=${encodeURIComponent(userType)}`
  }
  return api.get(`/public/topic/${topicId}/comments${qs}`)
}

// 话题评论点赞/取消点赞
export function toggleTopicCommentLike(commentId, data) {
  return api.post(`/public/topic/comment/${commentId}/like`, data)
}

// 添加话题评论
export function addTopicComment(data) {
  return api.post('/public/topic/comment', data)
}

// 获取单个素材文件URL（按需加载）
export function getMaterialFile(id) {
  return api.get(`/public/material/${id}/file`)
}

export function getMaterialFileLength(id) {
  return api.get(`/public/material/${id}/file/length`)
}

export function getMaterialFileChunk(id, offset) {
  return api.get(`/public/material/${id}/file/chunk`, { params: { offset } })
}

// 批量获取素材文件URL（最多10个）
export function getMaterialFiles(ids) {
  return api.post('/public/material/files', { ids })
}
