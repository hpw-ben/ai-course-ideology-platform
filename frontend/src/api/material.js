import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

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

export function getMaterialsByTeacher(teacherId) {
  return api.get(`/material/list/${teacherId}`)
}

export function getAllMaterials() {
  return api.get('/material/all')
}

export function getApprovedMaterials() {
  return api.get('/material/approved')
}

export function getMaterialById(id) {
  return api.get(`/material/detail/${id}`)
}

// 单独获取素材文件URL（用于下载/查看大文件）
export function getMaterialFileUrl(id) {
  return api.get(`/material/file/${id}`)
}

export function createMaterial(data) {
  return api.post('/material/create', data)
}

export function updateMaterial(data) {
  return api.post('/material/update', data)
}

export function deleteMaterial(id) {
  return api.delete(`/material/${id}`)
}
