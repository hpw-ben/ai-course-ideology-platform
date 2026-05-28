import { uploadFile } from '../api/public'

export function extractUploadRawFile(uploadEvent, uploadFiles) {
  if (uploadEvent && uploadEvent.raw) {
    return uploadEvent.raw
  }

  if (Array.isArray(uploadFiles) && uploadFiles.length > 0 && uploadFiles[0] && uploadFiles[0].raw) {
    return uploadFiles[0].raw
  }

  return null
}

export function ensureFileType(rawFile, expectedPrefix, message) {
  if (!rawFile) {
    throw new Error('文件不存在')
  }

  if (expectedPrefix && !String(rawFile.type || '').startsWith(expectedPrefix)) {
    throw new Error(message)
  }
}

export function ensureFileSize(rawFile, maxSizeBytes, message) {
  if (!rawFile) {
    throw new Error('文件不存在')
  }

  if (maxSizeBytes > 0 && Number(rawFile.size || 0) > maxSizeBytes) {
    throw new Error(message)
  }
}

export function createLocalPreviewUrl(rawFile) {
  if (!rawFile) {
    return ''
  }

  return URL.createObjectURL(rawFile)
}

export async function uploadResourceFile(rawFile, category) {
  if (!rawFile) {
    throw new Error('文件不存在')
  }

  const response = await uploadFile(rawFile, category)
  const result = response && response.data ? response.data : null

  if (!result || result.code !== 200 || !result.data) {
    throw new Error(result && result.message ? result.message : '上传失败')
  }

  return result.data
}
