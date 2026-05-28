<template>
  <div class="discussion-container">
    <div class="header">
      <el-button @click="goBack"><el-icon><Back /></el-icon> 返回首页</el-button>
      <h2>{{ discussion.title }}</h2>
    </div>

    <div class="content">
      <!-- 讨论信息 -->
      <div class="info-card">
        <p><strong>讨论说明：</strong>{{ discussion.description || '无' }}</p>
        <p><strong>发起教师：</strong>{{ discussion.teacherName }}</p>
        <p v-if="discussion.endTime">
          <strong>截止时间：</strong>{{ formatTime(discussion.endTime) }}
          <el-tag v-if="isExpired" type="danger" size="small" style="margin-left: 10px">已截止</el-tag>
          <el-tag v-else type="success" size="small" style="margin-left: 10px">进行中</el-tag>
        </p>
        <div style="margin-top: 8px;">
          <el-button v-if="isExpired || discussion.status === 'ENDED'" type="success" size="small" :loading="exporting" @click="exportMyReportPdf">导出我的报告PDF</el-button>
        </div>
      </div>

      <!-- 关联素材 -->
      <div class="material-card" v-if="discussion.materials && discussion.materials.length > 0">
        <h3>学习素材（{{ discussion.materials.length }}个）</h3>
        <div class="materials-list">
          <div class="material-item" v-for="(mat, index) in discussion.materials" :key="mat.id">
            <div class="material-header">
              <span class="material-index">素材 {{ index + 1 }}</span>
              <span class="material-type-tag">{{ mat.type === 'IMAGE' ? '图片' : mat.type === 'VIDEO' ? '视频' : '文章' }}</span>
            </div>
            <p class="material-title">{{ mat.title }}</p>
            <p class="material-desc" v-if="mat.description">{{ mat.description }}</p>
            <div v-if="mat.type === 'IMAGE' && mat.fileUrl">
              <img :src="mat.fileUrl" class="material-img" />
            </div>
            <div v-if="mat.type === 'VIDEO' && mat.fileUrl">
              <video :src="mat.fileUrl" controls class="material-video"></video>
            </div>
            <div v-if="mat.type === 'ARTICLE'" class="article-content">
              {{ mat.content }}
            </div>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="discussion.newsList && discussion.newsList.length > 0">
        <h3>新闻资讯（{{ discussion.newsList.length }}条）</h3>
        <div>
          <div v-for="n in discussion.newsList" :key="n.id" style="margin: 6px 0;">
            <el-link type="primary" @click="openNews(n.id)">{{ n.title }}</el-link>
          </div>
        </div>
      </div>
      <!-- 兼容旧数据：单素材 -->
      <div class="material-card" v-else-if="discussion.material">
        <h3>学习素材</h3>
        <div class="material-info">
          <p class="material-title">{{ discussion.material.title }}</p>
          <p class="material-desc" v-if="discussion.material.description">{{ discussion.material.description }}</p>
          <div v-if="discussion.material.type === 'IMAGE' && discussion.material.fileUrl">
            <img :src="discussion.material.fileUrl" class="material-img" />
          </div>
          <div v-if="discussion.material.type === 'VIDEO' && discussion.material.fileUrl">
            <video :src="discussion.material.fileUrl" controls class="material-video"></video>
          </div>
          <div v-if="discussion.material.type === 'ARTICLE'" class="article-content">
            {{ discussion.material.content }}
          </div>
        </div>
      </div>

      <!-- 发表评论 -->
      <div class="comment-form-card">
        <h3>发表评论</h3>
        <div v-if="isExpired" class="expired-tip">
          <el-alert title="讨论已截止，无法发表评论" type="warning" :closable="false" />
        </div>
        <template v-else>
          <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="请输入您的观点和看法..." />
          <div style="margin-top: 10px; display:flex; align-items:center; gap: 10px;">
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleCommentImageChange">
              <el-button size="small">上传图片</el-button>
            </el-upload>
            <img v-if="commentImageUrl" :src="commentImageUrl" style="max-height: 60px; border-radius: 4px;" />
            <el-button v-if="commentImageUrl" size="small" type="danger" link @click="clearCommentImage">移除图片</el-button>
          </div>
          <el-button type="primary" :loading="submitLoading" @click="submitComment" style="margin-top: 10px">发表评论</el-button>
        </template>
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
                <el-tag v-if="c.userType === 'TEACHER'" type="danger" size="small" style="margin-left: 5px">教师</el-tag>
                <span class="is-me" v-if="c.userId === user.id && c.userType === user.role">（我）</span>
              </div>
              <span class="time">{{ formatTime(c.createdAt) }}</span>
              <el-button v-if="!isExpired" type="text" size="small" @click="showReplyInput(c)">回复</el-button>
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
            <div v-if="replyingTo === c.id && !isExpired" class="reply-input">
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
                  <el-tag v-if="r.userType === 'TEACHER'" type="danger" size="small">教师</el-tag>
                  <span class="is-me" v-if="r.userId === user.id && r.userType === user.role">（我）</span>
                  <span v-if="r.replyToUserRealName" class="reply-to">回复 <span class="reply-to-name">{{ r.replyToUserRealName }}</span></span>
                  <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
                  <el-button v-if="!isExpired" type="text" size="small" @click="showReplyToReply(c, r)">回复</el-button>
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
              <div v-if="replyingToReply && replyingToReply.parentId === c.id && !isExpired" class="reply-input">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { getDiscussionByCode, getComments, addComment, toggleLikeComment, downloadStudentDiscussionReportPdf } from '../../api/discussion'
import { recordEnter } from '../../api/learning'
import { getMaterialFileUrl } from '../../api/material'
import { uploadResourceFile } from '../../utils/upload'

export default {
  name: 'StudentDiscussion',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const discussion = ref({})
    const comments = ref([])
    const commentContent = ref('')
    const submitLoading = ref(false)
    const commentImageUrl = ref('')
    const user = ref({})
    const replyingTo = ref(null)
    const replyingToReply = ref(null)
    const replyContent = ref('')
    const replyLoading = ref(false)
    const replyImageUrl = ref('')
    const exporting = ref(false)

    // 计算是否已截止
    const isExpired = computed(() => {
      if (!discussion.value.endTime) return false
      return new Date(discussion.value.endTime) < new Date()
    })

    // 计算总评论数
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
      loadDiscussion()
    })

    // 记录进入学习轨迹
    const recordLearningEnter = async () => {
      if (!user.value.id || !discussion.value.id) return
      try {
        await recordEnter({
          studentId: user.value.id,
          type: 'DISCUSSION',
          targetId: discussion.value.id,
          targetCode: discussion.value.code,
          targetTitle: discussion.value.title
        })
      } catch (e) {
        console.error(e)
      }
    }
    
    // 单独加载素材的文件URL
    const loadMaterialFileUrls = async () => {
      if (discussion.value.materials && discussion.value.materials.length > 0) {
        for (const mat of discussion.value.materials) {
          if ((mat.type === 'IMAGE' || mat.type === 'VIDEO') && !mat.fileUrl) {
            try {
              const res = await getMaterialFileUrl(mat.id)
              if (res.data.code === 200) {
                mat.fileUrl = res.data.data
              }
            } catch (e) { console.error('加载素材文件失败:', e) }
          }
        }
      }
      // 兼容旧数据
      if (discussion.value.material && (discussion.value.material.type === 'IMAGE' || discussion.value.material.type === 'VIDEO') && !discussion.value.material.fileUrl) {
        try {
          const res = await getMaterialFileUrl(discussion.value.material.id)
          if (res.data.code === 200) {
            discussion.value.material.fileUrl = res.data.data
          }
        } catch (e) { console.error('加载素材文件失败:', e) }
      }
    }

    const loadDiscussion = async () => {
      try {
        const res = await getDiscussionByCode(route.params.code)
        if (res.data.code === 200) {
          discussion.value = res.data.data
          // 为每个素材加载文件URL
          await loadMaterialFileUrls()
          loadComments()
          recordLearningEnter()
        } else {
          ElMessage.error(res.data.message)
          router.push('/student/home')
        }
      } catch (e) {
        ElMessage.error('讨论不存在')
        router.push('/student/home')
      }
    }

    const loadComments = async () => {
      try {
        const res = await getComments(discussion.value.id, user.value.id, user.value.role)
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
      uploadResourceFile(raw, 'discussion-comment').then((url) => {
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
      uploadResourceFile(raw, 'discussion-reply').then((url) => {
        replyImageUrl.value = url
      }).catch((error) => {
        replyImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearReplyImage = () => {
      replyImageUrl.value = ''
    }

    const goBack = () => { router.push('/student/home') }

    const openNews = (id) => {
      if (!id) return
      router.push(`/student/news/${id}`)
    }

    const formatTime = (time) => {
      if (!time) return ''
      return time.replace('T', ' ').substring(0, 19)
    }

    const exportMyReportPdf = async () => {
      if (!user.value || !user.value.id) {
        ElMessage.warning('请先登录')
        return
      }
      if (!discussion.value || !discussion.value.code) return
      exporting.value = true
      try {
        const res = await downloadStudentDiscussionReportPdf(discussion.value.code, user.value.id)
        const ct = (res && res.headers && (res.headers['content-type'] || res.headers['Content-Type'])) || ''
        if (!String(ct).toLowerCase().includes('application/pdf')) {
          const msg = res && res.data ? await res.data.text() : ''
          ElMessage.error(msg || '导出失败')
          return
        }
        const blob = new Blob([res.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `discussion-report-student-${user.value.id}-${discussion.value.code}.pdf`
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '导出失败'
        ElMessage.error(msg)
      } finally {
        exporting.value = false
      }
    }

    const submitComment = async () => {
      if (!commentContent.value.trim() && !commentImageUrl.value) {
        ElMessage.warning('请输入评论内容')
        return
      }
      submitLoading.value = true
      try {
        const res = await addComment({
          discussionId: discussion.value.id,
          userId: user.value.id,
          userType: 'STUDENT',
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
        const res = await toggleLikeComment(comment.id, { userId: user.value.id, userType: user.value.role })
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
        const res = await addComment({
          discussionId: discussion.value.id,
          userId: user.value.id,
          userType: 'STUDENT',
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
        const res = await addComment({
          discussionId: discussion.value.id,
          userId: user.value.id,
          userType: 'STUDENT',
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
      discussion,
      comments,
      user,
      commentContent,
      submitLoading,
      commentImageUrl,
      replyingTo,
      replyingToReply,
      replyContent,
      replyLoading,
      replyImageUrl,
      isExpired,
      totalComments,
      goBack,
      openNews,
      formatTime,
      exporting,
      exportMyReportPdf,
      loadDiscussion,
      loadComments,
      submitComment,
      toggleLike,
      showReplyInput,
      showReplyToReply,
      cancelReply,
      submitReply,
      submitReplyToReply,
      Back,
      handleCommentImageChange,
      clearCommentImage,
      handleReplyImageChange,
      clearReplyImage
    }
  }
}
</script>

<style scoped>
.discussion-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.header h2 {
  flex: 1;
  margin: 0;
  color: #c41230;
}
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-card, .material-card, .comment-form-card, .comments-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.info-card p {
  margin: 8px 0;
  color: #666;
}
.material-card h3, .comment-form-card h3, .comments-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  border-bottom: 2px solid #c41230;
  padding-bottom: 10px;
}
.material-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}
.material-desc {
  color: #666;
  margin-bottom: 15px;
}
.material-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
}
.material-video {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
}
.article-content {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.8;
}
.materials-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.material-item {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 3px solid #c41230;
}
.material-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.material-index {
  font-weight: bold;
  color: #c41230;
}
.material-type-tag {
  color: #909399;
  font-size: 13px;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}
.empty-tip {
  color: #909399;
  text-align: center;
  padding: 40px;
}
.expired-tip {
  margin-bottom: 10px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.comment-item {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}
.comment-item.pinned {
  background: #fffbe6;
  border: 1px solid #ffe58f;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}
.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #c41230;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.user-info {
  flex: 1;
}
.name {
  font-weight: bold;
  color: #333;
}
.is-me {
  color: #c41230;
  font-size: 12px;
  margin-left: 5px;
}
.time {
  color: #909399;
  font-size: 12px;
}
.comment-content {
  color: #333;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
.reply-input {
  margin-top: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}
.reply-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.replies-list {
  margin-top: 10px;
  padding-left: 50px;
}
.reply-item {
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 8px;
  border-left: 3px solid #c41230;
}
.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}
.reply-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.reply-name {
  font-weight: bold;
  color: #333;
  font-size: 13px;
}
.reply-to {
  color: #909399;
  font-size: 12px;
}
.reply-to-name {
  color: #c41230;
}
.reply-time {
  color: #909399;
  font-size: 11px;
  margin-left: auto;
}
.reply-content {
  color: #333;
  font-size: 13px;
  line-height: 1.5;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>

