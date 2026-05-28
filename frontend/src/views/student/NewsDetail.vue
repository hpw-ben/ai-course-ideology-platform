<template>
  <div class="news-detail-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-button type="text" style="color: #fff" @click="goBack">{{ isFromThought ? '返回思想页面' : '返回首页' }}</el-button>
      </div>
    </div>
    <div class="main-content">
      <div class="news-content" v-if="news">
        <h1 class="news-title">{{ news.title }}</h1>
        <div class="news-meta">
          <span v-if="news.author">作者：{{ news.author }}</span>
          <span>发布时间：{{ formatTime(news.createdAt) }}</span>
          <span>浏览量：{{ news.viewCount || 0 }}</span>
        </div>
        <div class="news-summary" v-if="news.summary">{{ news.summary }}</div>
        <div class="news-body" v-html="contentHtml"></div>
      </div>
      <div class="comment-section">
        <div class="comment-header"><h3>评论区 ({{ comments.length }})</h3></div>
        <div class="comment-input">
          <el-input v-model="commentContent" type="textarea" :rows="4" placeholder="发表你的看法..." maxlength="500" show-word-limit />
          <div style="margin-top: 10px; display:flex; align-items:center; gap: 10px;">
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleCommentImageChange">
              <el-button size="small">上传图片</el-button>
            </el-upload>
            <img v-if="commentImageUrl" :src="commentImageUrl" style="max-height: 60px; border-radius: 4px;" />
            <el-button v-if="commentImageUrl" size="small" type="danger" link @click="clearCommentImage">移除图片</el-button>
          </div>
          <el-button type="primary" @click="submitComment" :loading="submitting" style="margin-top: 10px">发表评论</el-button>
        </div>
        <div class="comment-list">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <div class="comment-avatar">
              <img v-if="comment.userAvatar" :src="comment.userAvatar" />
              <div v-else class="avatar-placeholder">{{ comment.userRealName ? comment.userRealName[0] : 'U' }}</div>
            </div>
            <div class="comment-content">
              <div class="comment-user">
                <span class="user-name">{{ comment.userRealName || comment.userName }}</span>
                <span class="user-type">{{ comment.userType === 'STUDENT' ? '学生' : '教师' }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div v-if="comment.imageUrl" style="margin-top: 8px;">
                <img :src="comment.imageUrl" style="max-width: 100%; max-height: 260px; border-radius: 6px;" />
              </div>
              <div class="comment-time" style="display:flex; gap: 10px; align-items:center;">
                <span>{{ formatTime(comment.createdAt) }}</span>
                <el-button type="text" size="small" :disabled="!user.id" @click="toggleLike(comment)">
                  {{ comment.liked ? '已赞' : '点赞' }} ({{ comment.likeCount || 0 }})
                </el-button>
                <el-button type="text" size="small" @click="showReplyInput(comment)">回复</el-button>
              </div>

              <div v-if="replyingTo === comment.id" class="reply-input" style="margin-top: 10px;">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + (comment.userRealName || comment.userName) + '...'" />
                <div style="margin-top: 8px; display:flex; align-items:center; gap: 10px;">
                  <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleReplyImageChange">
                    <el-button size="small">上传图片</el-button>
                  </el-upload>
                  <img v-if="replyImageUrl" :src="replyImageUrl" style="max-height: 50px; border-radius: 4px;" />
                  <el-button v-if="replyImageUrl" size="small" type="danger" link @click="clearReplyImage">移除图片</el-button>
                </div>
                <div style="margin-top: 8px; display:flex; justify-content:flex-end; gap: 8px;">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button type="primary" size="small" :loading="replyLoading" @click="submitReply(comment)">回复</el-button>
                </div>
              </div>

              <div v-if="comment.replies && comment.replies.length > 0" class="replies-list" style="margin-top: 10px; padding-left: 55px;">
                <div class="reply-item" v-for="r in comment.replies" :key="r.id" style="display:flex; gap: 10px; padding: 10px; background:#fff; border-radius:6px; margin-bottom:8px;">
                  <div style="width: 28px; height: 28px; flex-shrink:0;">
                    <img v-if="r.userAvatar" :src="r.userAvatar" style="width:28px; height:28px; border-radius:50%; object-fit:cover;" />
                    <div v-else style="width:28px; height:28px; border-radius:50%; background:#c41230; color:#fff; display:flex; align-items:center; justify-content:center; font-size:12px;">
                      {{ r.userRealName ? r.userRealName[0] : 'U' }}
                    </div>
                  </div>
                  <div style="flex:1;">
                    <div style="display:flex; align-items:center; gap: 8px; flex-wrap: wrap;">
                      <span style="font-weight:bold; color:#333; font-size:13px;">{{ r.userRealName || r.userName }}</span>
                      <span style="font-size:12px; color:#999;">{{ r.userType === 'STUDENT' ? '学生' : '教师' }}</span>
                      <span v-if="r.replyToUserRealName" style="font-size:12px; color:#999;">回复 <span style="color:#c41230;">{{ r.replyToUserRealName }}</span></span>
                      <span style="margin-left:auto; font-size:11px; color:#999;">{{ formatTime(r.createdAt) }}</span>
                      <el-button type="text" size="small" :disabled="!user.id" @click="toggleLike(r)">
                        {{ r.liked ? '已赞' : '点赞' }} ({{ r.likeCount || 0 }})
                      </el-button>
                      <el-button type="text" size="small" @click="showReplyToReply(comment, r)">回复</el-button>
                    </div>
                    <div style="margin-top: 6px; color:#666; line-height:1.6; white-space: pre-wrap; word-break: break-word;">{{ r.content }}</div>
                    <div v-if="r.imageUrl" style="margin-top: 6px;">
                      <img :src="r.imageUrl" style="max-width: 100%; max-height: 220px; border-radius: 6px;" />
                    </div>
                  </div>
                </div>

                <div v-if="replyingToReply && replyingToReply.parentId === comment.id" class="reply-input" style="margin-top: 10px; padding: 10px; background:#fff; border-radius: 6px;">
                  <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + (replyingToReply.userRealName || replyingToReply.userName) + '...'" />
                  <div style="margin-top: 8px; display:flex; align-items:center; gap: 10px;">
                    <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleReplyImageChange">
                      <el-button size="small">上传图片</el-button>
                    </el-upload>
                    <img v-if="replyImageUrl" :src="replyImageUrl" style="max-height: 50px; border-radius: 4px;" />
                    <el-button v-if="replyImageUrl" size="small" type="danger" link @click="clearReplyImage">移除图片</el-button>
                  </div>
                  <div style="margin-top: 8px; display:flex; justify-content:flex-end; gap: 8px;">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button type="primary" size="small" :loading="replyLoading" @click="submitReplyToReply(comment)">回复</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="comments.length === 0" class="no-comments"><p>暂无评论，快来发表第一条评论吧！</p></div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNewsById, getNewsCommentsWithUser, addNewsComment, toggleNewsCommentLike } from '../../api/public'
import { uploadResourceFile } from '../../utils/upload'
export default {
  name: 'StudentNewsDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const isFromThought = computed(() => (route.query && route.query.from) === 'thought')
    const news = ref(null)
    const contentHtml = computed(() => {
      const c = news.value && news.value.content ? news.value.content : ''
      return String(c)
    })
    const comments = ref([])
    const commentContent = ref('')
    const commentImageUrl = ref('')
    const submitting = ref(false)
    const user = ref({})
    const replyingTo = ref(null)
    const replyingToReply = ref(null)
    const replyContent = ref('')
    const replyLoading = ref(false)
    const replyImageUrl = ref('')
    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) { user.value = JSON.parse(userData) }
      loadNews()
      loadComments()
    })
    const loadNews = async () => {
      try {
        const newsId = route.params.id
        const res = await getNewsById(newsId)
        if (res.data.code === 200) { news.value = res.data.data }
        else { ElMessage.error('新闻不存在'); router.push(isFromThought.value ? '/student/thought' : '/student/home') }
      } catch (e) { ElMessage.error('加载新闻失败') }
    }
    const loadComments = async () => {
      try {
        const newsId = route.params.id
        const res = await getNewsCommentsWithUser(newsId, user.value.id, user.value.role)
        if (res.data.code === 200) { comments.value = res.data.data }
      } catch (e) { console.error('加载评论失败:', e) }
    }

    const handleCommentImageChange = (file) => {
      const raw = file?.raw
      if (!raw) return
      if (!String(raw.type || '').startsWith('image/')) { ElMessage.warning('仅支持图片'); return }
      if (raw.size > 2 * 1024 * 1024) { ElMessage.warning('图片过大，最大2MB'); return }
      commentImageUrl.value = ''
      uploadResourceFile(raw, 'news-comment').then((url) => {
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
      if (!String(raw.type || '').startsWith('image/')) { ElMessage.warning('仅支持图片'); return }
      if (raw.size > 2 * 1024 * 1024) { ElMessage.warning('图片过大，最大2MB'); return }
      replyImageUrl.value = ''
      uploadResourceFile(raw, 'news-reply').then((url) => {
        replyImageUrl.value = url
      }).catch((error) => {
        replyImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearReplyImage = () => {
      replyImageUrl.value = ''
    }
    const submitComment = async () => {
      if (!commentContent.value.trim() && !commentImageUrl.value) { ElMessage.warning('请输入评论内容'); return }
      submitting.value = true
      try {
        const res = await addNewsComment({ newsId: route.params.id, userId: user.value.id, userType: 'STUDENT', content: commentContent.value, imageUrl: commentImageUrl.value })
        if (res.data.code === 200) { ElMessage.success('评论成功'); commentContent.value = ''; commentImageUrl.value = ''; loadComments() }
        else { ElMessage.error(res.data.message) }
      } catch (e) { ElMessage.error('评论失败') }
      finally { submitting.value = false }
    }

    const toggleLike = async (comment) => {
      if (!comment || !comment.id) return
      if (!user.value.id) return
      try {
        const res = await toggleNewsCommentLike(comment.id, { userId: user.value.id, userType: user.value.role })
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
      replyImageUrl.value = ''
    }

    const showReplyToReply = (parentComment, reply) => {
      replyingTo.value = null
      replyingToReply.value = { ...reply, parentId: parentComment.id }
      replyContent.value = ''
      replyImageUrl.value = ''
    }

    const cancelReply = () => {
      replyingTo.value = null
      replyingToReply.value = null
      replyContent.value = ''
      replyImageUrl.value = ''
    }

    const submitReply = async (parentComment) => {
      if (!replyContent.value.trim() && !replyImageUrl.value) { ElMessage.warning('请输入回复内容'); return }
      replyLoading.value = true
      try {
        const res = await addNewsComment({
          newsId: route.params.id,
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
          loadComments()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('回复失败')
      } finally {
        replyLoading.value = false
      }
    }

    const submitReplyToReply = async (parentComment) => {
      if (!replyContent.value.trim() && !replyImageUrl.value) { ElMessage.warning('请输入回复内容'); return }
      replyLoading.value = true
      try {
        const res = await addNewsComment({
          newsId: route.params.id,
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
          loadComments()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('回复失败')
      } finally {
        replyLoading.value = false
      }
    }
    const goBack = () => { router.push(isFromThought.value ? '/student/thought' : '/student/home') }
    const formatTime = (time) => { if (!time) return ''; return time.replace('T', ' ').substring(0, 19) }
    return {
      news, contentHtml, comments,
      commentContent, commentImageUrl,
      submitting,
      replyingTo, replyingToReply, replyContent, replyLoading, replyImageUrl,
      loadComments,
      handleCommentImageChange, clearCommentImage,
      handleReplyImageChange, clearReplyImage,
      submitComment,
      toggleLike,
      showReplyInput, showReplyToReply, cancelReply, submitReply, submitReplyToReply,
      goBack, formatTime, isFromThought,
      user
    }
  }
}
</script>
<style scoped>
.news-detail-container{
  min-height:100vh;
  background:#f5f5f5
}
.navbar{
  background:#c41230;
  position:fixed;
  top:0;
  left:0;
  right:0;
  z-index:100;
  box-shadow:0 2px 8px rgba(0,0,0,0.15)
}
.nav-content{
  max-width:1200px;
  margin:0 auto;
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding:0 20px;
  height:60px
}
.logo{
  font-size:20px;
  font-weight:bold;
  color:#fff
}
.main-content{
  padding-top:80px;
  max-width:900px;
  margin:0 auto;
  padding-bottom:40px
}
.news-content{
  background:#fff;
  padding:40px;
  border-radius:8px;
  margin-bottom:20px
}
.news-title{
  font-size:32px;
  color:#333;
  margin:0 0 20px 0;
  line-height:1.4
}
.news-meta{
  display:flex;
  gap:20px;
  color:#999;
  font-size:14px;
  padding-bottom:20px;
  border-bottom:1px solid #eee;
  margin-bottom:20px
}
.news-summary{
  font-size:16px;
  color:#666;
  line-height:1.8;
  margin-bottom:20px;
  padding:15px;
  background:#f9f9f9;
  border-left:4px solid #c41230
}
.news-body{
  font-size:16px;
  color:#333;
  line-height:2;
  white-space:pre-wrap;
  overflow-wrap:anywhere;
  word-break:break-word}
.comment-section{
  background:#fff;
  padding:30px;
  border-radius:8px
}
.comment-header{
  border-bottom:2px solid #c41230;
  padding-bottom:15px;
  margin-bottom:20px
}
.comment-header h3{
  margin:0;
  color:#333
}
.comment-input{
  margin-bottom:30px
}
.comment-list{
  display:flex;
  flex-direction:column;
  gap:20px
}
.comment-item{
  display:flex;
  gap:15px;
  padding:15px;
  background:#f9f9f9;
  border-radius:8px
}
.comment-avatar{
  width:40px;
  height:40px;
  flex-shrink:0
}
.comment-avatar img{
  width:100%;
  height:100%;
  border-radius:50%;
  object-fit:cover
}
.avatar-placeholder{
  width:100%;
  height:100%;
  border-radius:50%;
  background:#c41230;
  color:#fff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:18px;
  font-weight:bold
}
.comment-content{
  flex:1
}
.comment-user{
  display:flex;
  align-items:center;
  gap:10px;
  margin-bottom:8px
}
.user-name{
  font-weight:bold;
  color:#333
}
.user-type{
  font-size:12px;
  color:#999;
  padding:2px 8px;
  background:#fff;
  border-radius:4px
}
.comment-text{
  color:#666;
  line-height:1.6;
  margin-bottom:8px
}
.comment-time{
  font-size:12px;
  color:#999
}
.no-comments{
  text-align:center;
  padding:40px;
  color:#999
}
</style>

