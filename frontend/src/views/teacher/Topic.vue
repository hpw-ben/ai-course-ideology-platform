<template>
  <div class="topic-container">
    <div class="header">
      <el-button @click="goBack"><el-icon><Back /></el-icon> 返回论坛</el-button>
      <h2>{{ topic.title }}</h2>
      <div class="code-box">
        话题码：<span class="code">{{ topic.code }}</span>
      </div>
    </div>

    <div class="content">
      <!-- 话题信息 -->
      <div class="info-card">
        <h3>话题信息</h3>
        <p><strong>描述：</strong>{{ topic.description || '暂无描述' }}</p>
        <p><strong>发布者：</strong>{{ topic.adminName }}</p>
        <p><strong>发布时间：</strong>{{ formatTime(topic.createdAt) }}</p>
        <p><strong>浏览量：</strong>{{ topic.viewCount }}</p>
      </div>

      <!-- 关联素材 -->
      <div class="material-card" v-if="topic.materials && topic.materials.length > 0">
        <h3>相关素材（{{ topic.materials.length }}个）</h3>
        <div class="materials-list">
          <div class="material-item" v-for="(mat, index) in topic.materials" :key="mat.id">
            <div class="material-header">
              <span class="material-index">素材 {{ index + 1 }}</span>
              <el-tag size="small">{{ mat.type === 'IMAGE' ? '图片' : mat.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
            </div>
            <p><strong>标题：</strong>{{ mat.title }}</p>
            <div v-if="mat.type === 'IMAGE'">
              <div v-if="mat.fileUrl">
                <img :src="mat.fileUrl" class="material-img" />
              </div>
              <div v-else-if="mat.loading" class="loading-tip">加载中...</div>
              <el-button v-else size="small" @click="loadMaterialFile(mat)">点击加载图片</el-button>
            </div>
            <div v-if="mat.type === 'VIDEO'">
              <div v-if="mat.fileUrl">
                <video :src="mat.playUrl || mat.fileUrl" controls class="material-video"></video>
              </div>
              <div v-else-if="mat.loading" class="loading-tip">加载中...</div>
              <el-button v-else size="small" @click="loadMaterialFile(mat)">点击加载视频</el-button>
            </div>
            <div v-if="mat.type === 'ARTICLE'" class="article-content">
              {{ mat.content }}
            </div>
          </div>
        </div>
      </div>

      <!-- 关联新闻 -->
      <div class="news-card" v-if="topic.relatedNews && topic.relatedNews.length > 0">
        <h3>相关新闻（{{ topic.relatedNews.length }}条）</h3>
        <div class="news-list">
          <div class="news-item" v-for="n in topic.relatedNews" :key="n.id">
            <el-link type="primary" :underline="false" @click="openNews(n.id)">{{ n.title }}</el-link>
            <span class="news-date">{{ formatDate(n.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 发表评论 -->
      <div class="comment-form-card" v-if="topic.status === 'ACTIVE'">
        <h3>发表评论</h3>
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="发表您的观点..." />
        <div style="margin-top: 10px; display:flex; align-items:center; gap: 10px;">
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleCommentImageChange">
            <el-button size="small">上传图片</el-button>
          </el-upload>
          <img v-if="commentImageUrl" :src="commentImageUrl" style="max-height: 60px; border-radius: 4px;" />
          <el-button v-if="commentImageUrl" size="small" type="danger" link @click="clearCommentImage">移除图片</el-button>
        </div>
        <el-button type="primary" :loading="submitLoading" @click="submitComment" style="margin-top: 10px">发表评论</el-button>
      </div>
      <div class="comment-form-card ended-tip" v-else>
        <p>该话题已关闭，无法发表评论</p>
      </div>

      <!-- 评论列表 -->
      <div class="comments-card">
        <h3>讨论区（{{ totalComments }}条评论）</h3>
        <div v-if="comments.length === 0" class="empty-tip">暂无评论，快来发表第一条评论吧！</div>
        <div v-else class="comment-list">
          <div class="comment-item" v-for="c in comments" :key="c.id" :class="{ pinned: c.isPinned }">
            <div class="comment-header">
              <el-tag v-if="c.isPinned" type="warning" size="small" style="margin-right: 5px">置顶</el-tag>
              <img v-if="c.userAvatar" :src="c.userAvatar" class="avatar" />
              <div v-else class="avatar-placeholder">{{ c.userRealName?.charAt(0) }}</div>
              <div class="user-info">
                <span class="name">{{ c.userRealName }}</span>
                <el-tag v-if="c.userType === 'ADMIN'" type="danger" size="small" style="margin-left: 5px">管理员</el-tag>
                <el-tag v-else-if="c.userType === 'TEACHER'" type="warning" size="small" style="margin-left: 5px">教师</el-tag>
                <span class="username">@{{ c.userName }}</span>
              </div>
              <span class="time">{{ formatTime(c.createdAt) }}</span>
              <el-button v-if="topic.status === 'ACTIVE'" type="text" size="small" @click="showReplyInput(c)">回复</el-button>
            </div>
            <div class="comment-content">{{ c.content }}</div>
            <div v-if="c.imageUrl" style="margin-top: 8px;">
              <img :src="c.imageUrl" style="max-width: 100%; max-height: 260px; border-radius: 6px;" />
            </div>

            <div style="margin-top: 8px; display:flex; gap: 10px; align-items:center;">
              <el-button type="text" size="small" :disabled="!user.id" @click="toggleLike(c)">
                {{ c.liked ? '已赞' : '点赞' }} ({{ c.likeCount || 0 }})
              </el-button>
            </div>
            
            <!-- 回复输入框 -->
            <div v-if="replyingTo === c.id && topic.status === 'ACTIVE'" class="reply-input">
              <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + c.userRealName + '...'" />
              <div style="margin-top: 8px; display:flex; align-items:center; gap: 10px;">
                <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleReplyImageChange">
                  <el-button size="small">上传图片</el-button>
                </el-upload>
                <img v-if="replyImageUrl" :src="replyImageUrl" style="max-height: 50px; border-radius: 4px;" />
                <el-button v-if="replyImageUrl" size="small" type="danger" link @click="clearReplyImage">移除图片</el-button>
              </div>
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button type="primary" size="small" :loading="replyLoading" @click="submitReply(c)">回复</el-button>
              </div>
            </div>
            
            <!-- 子回复列表 -->
            <div v-if="c.replies && c.replies.length > 0" class="replies-list">
              <div class="reply-item" v-for="r in c.replies" :key="r.id">
                <div class="reply-header">
                  <img v-if="r.userAvatar" :src="r.userAvatar" class="reply-avatar" />
                  <div v-else class="reply-avatar-placeholder">{{ r.userRealName?.charAt(0) }}</div>
                  <span class="reply-name">{{ r.userRealName }}</span>
                  <el-tag v-if="r.userType === 'ADMIN'" type="danger" size="small">管理员</el-tag>
                  <el-tag v-else-if="r.userType === 'TEACHER'" type="warning" size="small">教师</el-tag>
                  <span v-if="r.replyToUserRealName" class="reply-to">回复 <span class="reply-to-name">{{ r.replyToUserRealName }}</span></span>
                  <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
                  <el-button v-if="topic.status === 'ACTIVE'" type="text" size="small" @click="showReplyToReply(c, r)">回复</el-button>
                </div>
                <div class="reply-content">{{ r.content }}</div>
                <div v-if="r.imageUrl" style="margin-top: 6px;">
                  <img :src="r.imageUrl" style="max-width: 100%; max-height: 220px; border-radius: 6px;" />
                </div>
                <div style="margin-top: 6px; display:flex; gap: 10px; align-items:center;">
                  <el-button type="text" size="small" :disabled="!user.id" @click="toggleLike(r)">
                    {{ r.liked ? '已赞' : '点赞' }} ({{ r.likeCount || 0 }})
                  </el-button>
                </div>
              </div>
              
              <!-- 回复子评论的输入框 -->
              <div v-if="replyingToReply && replyingToReply.parentId === c.id && topic.status === 'ACTIVE'" class="reply-input">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + replyingToReply.userRealName + '...'" />
                <div style="margin-top: 8px; display:flex; align-items:center; gap: 10px;">
                  <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleReplyImageChange">
                    <el-button size="small">上传图片</el-button>
                  </el-upload>
                  <img v-if="replyImageUrl" :src="replyImageUrl" style="max-height: 50px; border-radius: 4px;" />
                  <el-button v-if="replyImageUrl" size="small" type="danger" link @click="clearReplyImage">移除图片</el-button>
                </div>
                <div class="reply-actions">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button type="primary" size="small" :loading="replyLoading" @click="submitReplyToReply(c)">回复</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { getTopicByCode, getTopicComments, addTopicComment, toggleTopicCommentLike, getMaterialFile, getMaterialFileLength, getMaterialFileChunk } from '../../api/public'
import { uploadResourceFile } from '../../utils/upload'

export default {
  name: 'TeacherTopic',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const topic = ref({})
    const comments = ref([])
    const user = ref({})
    const commentContent = ref('')
    const submitLoading = ref(false)
    const commentImageUrl = ref('')
    const replyingTo = ref(null)
    const replyingToReply = ref(null)
    const replyContent = ref('')
    const replyLoading = ref(false)
    const replyImageUrl = ref('')

    const totalComments = computed(() => {
      let count = comments.value.length
      comments.value.forEach(c => {
        if (c.replies) count += c.replies.length
      })
      return count
    })

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      }
      loadTopic()
    })

    const revokeMaterialObjectUrl = (mat) => {
      if (mat && mat.playUrl && typeof mat.playUrl === 'string' && mat.playUrl.startsWith('blob:')) {
        URL.revokeObjectURL(mat.playUrl)
      }
    }

    const ensurePlayableUrl = async (mat) => {
      if (!mat || !mat.fileUrl) return
      revokeMaterialObjectUrl(mat)
      // 大视频 base64(dataURL) 在部分浏览器下直接作为 src 会失败，转为 blob URL 更稳
      if (mat.type === 'VIDEO' && typeof mat.fileUrl === 'string' && mat.fileUrl.startsWith('data:')) {
        try {
          const resp = await fetch(mat.fileUrl)
          const blob = await resp.blob()
          mat.playUrl = URL.createObjectURL(blob)
        } catch (e) {
          mat.playUrl = mat.fileUrl
        }
      } else {
        mat.playUrl = mat.fileUrl
      }
    }

    onUnmounted(() => {
      if (topic.value && topic.value.materials) {
        topic.value.materials.forEach(revokeMaterialObjectUrl)
      }
    })

    const loadTopic = async () => {
      try {
        const res = await getTopicByCode(route.params.code)
        if (res.data.code === 200) {
          topic.value = res.data.data
          // 自动加载素材文件
          if (topic.value.materials && topic.value.materials.length > 0) {
            loadAllMaterialFiles()
          }
          loadComments()
        } else {
          ElMessage.error(res.data.message || '话题不存在')
          router.push('/teacher/forum')
        }
      } catch (e) {
        ElMessage.error('获取话题失败')
        router.push('/teacher/forum')
      }
    }

    // 自动加载所有素材文件
    const loadAllMaterialFiles = async () => {
      const materials = topic.value.materials
      for (const mat of materials) {
        if ((mat.type === 'IMAGE' || mat.type === 'VIDEO') && !mat.fileUrl) {
          loadMaterialFile(mat)
        }
      }
    }

    // 按需加载单个素材文件
    const loadMaterialFile = async (mat) => {
      if (mat.fileUrl || mat.loading) return
      mat.loading = true
      try {
        const res = await getMaterialFile(mat.id)
        if (res.data.code === 200 && res.data.data) {
          mat.fileUrl = res.data.data.fileUrl
          await ensurePlayableUrl(mat)
          return
        }

        if (res.data && res.data.message === 'FILE_URL_TOO_LARGE') {
          const lenRes = await getMaterialFileLength(mat.id)
          const totalLen = (lenRes.data && lenRes.data.code === 200) ? (lenRes.data.data || 0) : 0
          if (!totalLen) return

          if (totalLen > 30 * 1024 * 1024) {
            ElMessage.error('视频过大，当前数据库配置较小，建议改为文件存储或调大 max_allowed_packet')
            return
          }

          const CHUNK_SIZE = 1024 * 1024
          let offset = 1
          let content = ''
          while (offset <= totalLen) {
            const chunkRes = await getMaterialFileChunk(mat.id, offset)
            if (!chunkRes.data || chunkRes.data.code !== 200) break
            const chunk = chunkRes.data.data || ''
            if (!chunk) break
            content += chunk
            offset += CHUNK_SIZE
          }
          mat.fileUrl = content
          await ensurePlayableUrl(mat)
        }
      } catch (e) {
        console.error('加载素材失败', e)
      } finally {
        mat.loading = false
      }
    }

    const loadComments = async () => {
      if (!topic.value.id) return
      try {
        const res = await getTopicComments(topic.value.id, user.value.id, user.value.role)
        if (res.data.code === 200) {
          comments.value = res.data.data
        }
      } catch (e) { console.error(e) }
    }

    const handleCommentImageChange = (file) => {
      const raw = file?.raw
      if (!raw) return
      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片')
        return
      }
      if (raw.size > 2 * 1024 * 1024) {
        ElMessage.warning('图片过大，最大2MB')
        return
      }
      commentImageUrl.value = ''
      uploadResourceFile(raw, 'topic-comment').then((url) => {
        commentImageUrl.value = url
      }).catch((error) => {
        commentImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearCommentImage = () => {
      commentImageUrl.value = ''
    }

    const handleReplyImageChange = (file) => {
      const raw = file?.raw
      if (!raw) return
      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片')
        return
      }
      if (raw.size > 2 * 1024 * 1024) {
        ElMessage.warning('图片过大，最大2MB')
        return
      }
      replyImageUrl.value = ''
      uploadResourceFile(raw, 'topic-reply').then((url) => {
        replyImageUrl.value = url
      }).catch((error) => {
        replyImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearReplyImage = () => {
      replyImageUrl.value = ''
    }

    const goBack = () => { router.push('/teacher/forum') }
    const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 19) : ''
    const formatDate = (time) => time ? String(time).replace('T', ' ').substring(0, 10) : ''

    const openNews = (id) => {
      if (!id) return
      router.push(`/teacher/news/${id}`)
    }

    const submitComment = async () => {
      if (!commentContent.value.trim() && !commentImageUrl.value) {
        ElMessage.warning('请输入评论内容')
        return
      }
      submitLoading.value = true
      try {
        const res = await addTopicComment({
          topicId: topic.value.id,
          userId: user.value.id,
          userType: user.value.role,
          content: commentContent.value,
          imageUrl: commentImageUrl.value
        })
        if (res.data.code === 200) {
          ElMessage.success('评论发表成功')
          commentContent.value = ''
          commentImageUrl.value = ''
          loadComments()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) { ElMessage.error('发表失败') }
      finally { submitLoading.value = false }
    }

    const toggleLike = async (comment) => {
      if (!comment || !comment.id) return
      if (!user.value.id) return
      try {
        const res = await toggleTopicCommentLike(comment.id, { userId: user.value.id, userType: user.value.role })
        if (res.data.code === 200) {
          const liked = !!res.data.data?.liked
          comment.liked = liked
          const cur = Number(comment.likeCount || 0)
          comment.likeCount = liked ? (cur + 1) : Math.max(cur - 1, 0)
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('操作失败')
      }
    }

    const showReplyInput = (comment) => {
      replyingTo.value = comment.id
      replyingToReply.value = null
      replyContent.value = ''
    }

    const showReplyToReply = (parentComment, reply) => {
      replyingTo.value = null
      replyingToReply.value = { ...reply, parentId: parentComment.id }
      replyContent.value = ''
    }

    const cancelReply = () => {
      replyingTo.value = null
      replyingToReply.value = null
      replyContent.value = ''
      replyImageUrl.value = ''
    }

    const submitReply = async (parentComment) => {
      if (!replyContent.value.trim() && !replyImageUrl.value) {
        ElMessage.warning('请输入回复内容')
        return
      }
      replyLoading.value = true
      try {
        const res = await addTopicComment({
          topicId: topic.value.id,
          userId: user.value.id,
          userType: user.value.role,
          content: replyContent.value,
          imageUrl: replyImageUrl.value,
          parentId: parentComment.id,
          replyToUserId: parentComment.userId,
          replyToUserType: parentComment.userType
        })
        if (res.data.code === 200) {
          ElMessage.success('回复成功')
          cancelReply()
          replyImageUrl.value = ''
          loadComments()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) { ElMessage.error('回复失败') }
      finally { replyLoading.value = false }
    }

    const submitReplyToReply = async (parentComment) => {
      if (!replyContent.value.trim() && !replyImageUrl.value) {
        ElMessage.warning('请输入回复内容')
        return
      }
      replyLoading.value = true
      try {
        const res = await addTopicComment({
          topicId: topic.value.id,
          userId: user.value.id,
          userType: user.value.role,
          content: replyContent.value,
          imageUrl: replyImageUrl.value,
          parentId: parentComment.id,
          replyToUserId: replyingToReply.value.userId,
          replyToUserType: replyingToReply.value.userType
        })
        if (res.data.code === 200) {
          ElMessage.success('回复成功')
          cancelReply()
          replyImageUrl.value = ''
          loadComments()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) { ElMessage.error('回复失败') }
      finally { replyLoading.value = false }
    }

    return { 
      topic, comments, user, totalComments,
      commentContent, submitLoading,
      commentImageUrl, handleCommentImageChange, clearCommentImage,
      replyingTo, replyingToReply, replyContent, replyLoading,
      replyImageUrl, handleReplyImageChange, clearReplyImage,
      goBack, formatTime, submitComment, toggleLike,
      formatDate,
      openNews,
      loadMaterialFile,
      showReplyInput, showReplyToReply, cancelReply, submitReply, submitReplyToReply,
      Back 
    }
  }
}
</script>

<style scoped>
.topic-container { padding: 20px; max-width: 900px; margin: 0 auto; }
.header { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; }
.header h2 { flex: 1; margin: 0; }
.code-box { display: flex; align-items: center; gap: 10px; }
.code { font-size: 18px; font-weight: bold; color: #c41230; background: #fff1f0; padding: 5px 15px; border-radius: 4px; }
.content { display: flex; flex-direction: column; gap: 20px; }
.info-card, .material-card, .comment-form-card, .comments-card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.info-card h3, .material-card h3, .comment-form-card h3, .comments-card h3 { margin: 0 0 15px 0; color: #333; border-bottom: 2px solid #c41230; padding-bottom: 10px; }
.info-card p { margin: 10px 0; color: #666; overflow-wrap: anywhere; word-break: break-word; }
.material-img { max-width: 100%; max-height: 400px; border-radius: 4px; margin-top: 10px; }
.material-video { max-width: 100%; max-height: 400px; border-radius: 4px; margin-top: 10px; }
.article-content { margin-top: 10px; padding: 15px; background: #f5f7fa; border-radius: 4px; white-space: pre-wrap; max-height: 300px; overflow-y: auto; }
.materials-list { display: flex; flex-direction: column; gap: 20px; }
.material-item { padding: 15px; background: #f9f9f9; border-radius: 8px; border-left: 3px solid #c41230; }
.material-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.material-index { font-weight: bold; color: #c41230; }
.ended-tip { color: #909399; text-align: center; }
.empty-tip { color: #909399; text-align: center; padding: 40px; }
.comment-list { display: flex; flex-direction: column; gap: 15px; }
.comment-item { padding: 15px; background: #f9f9f9; border-radius: 8px; }
.comment-item.pinned { background: #fffbe6; border: 1px solid #ffe58f; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; }
.avatar-placeholder { width: 40px; height: 40px; border-radius: 50%; background: #c41230; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.user-info { flex: 1; }
.name { font-weight: bold; color: #333; }
.username { color: #909399; margin-left: 8px; font-size: 13px; }
.time { color: #909399; font-size: 12px; }
.comment-content { color: #333; line-height: 1.6; word-wrap: break-word; word-break: break-all; white-space: pre-wrap; }
.reply-input { margin-top: 10px; padding: 10px; background: #fff; border-radius: 4px; }
.reply-actions { margin-top: 8px; display: flex; justify-content: flex-end; gap: 8px; }
.replies-list { margin-top: 10px; padding-left: 50px; }
.reply-item { padding: 10px; background: #fff; border-radius: 4px; margin-bottom: 8px; border-left: 3px solid #c41230; }
.reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.reply-avatar { width: 28px; height: 28px; border-radius: 50%; }
.reply-avatar-placeholder { width: 28px; height: 28px; border-radius: 50%; background: #c41230; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.reply-name { font-weight: bold; color: #333; font-size: 13px; }
.reply-to { color: #909399; font-size: 12px; }
.reply-to-name { color: #c41230; }
.reply-time { color: #909399; font-size: 11px; margin-left: auto; }
.reply-content { color: #333; font-size: 13px; line-height: 1.5; word-wrap: break-word; word-break: break-all; white-space: pre-wrap; }
.loading-tip { color: #909399; padding: 20px; text-align: center; }
</style>
