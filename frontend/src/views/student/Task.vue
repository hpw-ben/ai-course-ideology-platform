<template>
  <div class="task-container">
    <div class="header">
      <el-button @click="goBack"><el-icon><Back /></el-icon> 返回首页</el-button>
      <h2>{{ task.title }}</h2>
      <div class="code-box">
        任务码：<span class="code">{{ task.code }}</span>
      </div>
    </div>

    <div class="content">
      <!-- 任务信息 -->
      <div class="info-card">
        <h3>任务信息</h3>
        <p><strong>说明：</strong>{{ task.description || '无' }}</p>
        <p><strong>状态：</strong>
          <el-tag :type="task.status === 'IN_PROGRESS' ? 'success' : task.status === 'ENDED' ? 'info' : 'warning'">
            {{ task.status === 'IN_PROGRESS' ? '进行中' : task.status === 'ENDED' ? '已结束' : '未开始' }}
          </el-tag>
        </p>
        <p><strong>创建时间：</strong>{{ formatTime(task.createdAt) }}</p>
        <p v-if="task.endTime"><strong>截止时间：</strong>{{ formatTime(task.endTime) }}</p>
      </div>

      <!-- 关联素材 -->
      <div class="material-card" v-if="task.materials && task.materials.length > 0">
        <h3>学习素材（{{ task.materials.length }}个）</h3>
        <div v-if="taskProgress" style="margin: 10px 0; color:#606266;">
          素材进度：{{ taskProgress.completedMaterials }}/{{ taskProgress.totalMaterials }}
          <span v-if="taskProgress.status === 'COMPLETED'" style="margin-left:8px; color:#67c23a;">任务已完成</span>
        </div>
        <div class="materials-list">
          <div class="material-item" v-for="(mat, index) in task.materials" :key="mat.id">
            <div class="material-header">
              <span class="material-index">素材 {{ index + 1 }}</span>
              <el-tag size="small">{{ mat.type === 'IMAGE' ? '图片' : mat.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
              <el-tag v-if="isMaterialCompleted(mat.id)" type="success" size="small">已学完</el-tag>
              <el-tag v-else type="info" size="small">学习中</el-tag>
              <span v-if="!isMaterialCompleted(mat.id) && mat.type === 'ARTICLE'" style="color:#909399; font-size:12px;">滚动到底自动完成</span>
              <span v-if="!isMaterialCompleted(mat.id) && mat.type === 'VIDEO'" style="color:#909399; font-size:12px;">播放完成自动完成</span>
              <span v-if="!isMaterialCompleted(mat.id) && mat.type === 'IMAGE'" style="color:#909399; font-size:12px;">点击查看自动完成</span>
            </div>
            <p><strong>标题：</strong>{{ mat.title }}</p>
            <div v-if="mat.type === 'IMAGE' && mat.fileUrl">
              <img :src="mat.fileUrl" class="material-img" @click="onImageViewed(mat.id)" />
            </div>
            <div v-if="mat.type === 'VIDEO' && mat.fileUrl">
              <video :src="mat.fileUrl" controls class="material-video" @ended="onVideoEnded(mat.id)"></video>
            </div>
            <div v-if="mat.type === 'ARTICLE'" class="article-content" @scroll="onArticleScroll($event, mat.id)" @click="onArticleScroll($event, mat.id)">
              {{ mat.content }}
            </div>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="task.newsList && task.newsList.length > 0">
        <h3>新闻资讯（{{ task.newsList.length }}条）</h3>
        <div>
          <div v-for="n in task.newsList" :key="n.id" style="margin: 6px 0;">
            <el-link type="primary" @click="openNews(n.id)">{{ n.title }}</el-link>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="quizTotalCount > 0">
        <h3>测验题（{{ quizTotalCount }}题）</h3>
        <div v-if="quizMode === 'PER_MATERIAL'">
          <div v-for="(m, mIdx) in quizMaterials" :key="mIdx" style="margin-bottom: 14px;">
            <div style="font-weight: 700; margin-bottom: 8px;">素材：{{ m.materialTitle || m.materialId }}</div>
            <div class="quiz-list">
              <div class="quiz-item" v-for="(q, idx) in (m.questions || [])" :key="idx">
                <div class="quiz-stem">{{ idx + 1 }}. {{ q.stem }}</div>
                <el-radio-group v-model="quizAnswers[m.materialId][idx]" class="quiz-answer">
                  <el-radio label="A">A. {{ q.options?.A }}</el-radio>
                  <el-radio label="B">B. {{ q.options?.B }}</el-radio>
                  <el-radio label="C">C. {{ q.options?.C }}</el-radio>
                  <el-radio label="D">D. {{ q.options?.D }}</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="quiz-list">
          <div class="quiz-item" v-for="(q, idx) in quizQuestions" :key="idx">
            <div class="quiz-stem">{{ idx + 1 }}. {{ q.stem }}</div>
            <el-radio-group v-model="quizAnswers[idx]" class="quiz-answer">
              <el-radio label="A">A. {{ q.options?.A }}</el-radio>
              <el-radio label="B">B. {{ q.options?.B }}</el-radio>
              <el-radio label="C">C. {{ q.options?.C }}</el-radio>
              <el-radio label="D">D. {{ q.options?.D }}</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
          <el-button type="primary" :loading="quizSubmitting" @click="submitQuiz">提交答题</el-button>
          <span v-if="quizDone" style="color: #67c23a;">正确率：{{ quizAccuracyText }}</span>
        </div>
      </div>

      <div class="info-card" v-if="viewpointOptions.length > 0">
        <h3>观点打卡 & 短笔记</h3>
        <div>
          <div style="margin-bottom: 10px; color:#606266;">请选择一个观点（可选）：</div>
          <el-radio-group v-model="viewpointChoice" style="display:flex; flex-direction: column; gap: 8px;">
            <el-radio v-for="(opt, idx) in viewpointOptions" :key="idx" :label="opt">{{ opt }}</el-radio>
          </el-radio-group>
        </div>
        <div style="margin-top: 12px;">
          <div style="margin-bottom: 8px; color:#606266;">短笔记（50字内，可选，写入学习报告）：</div>
          <el-input v-model="shortNote" type="textarea" :rows="2" maxlength="50" show-word-limit placeholder="请输入短笔记(最多50字)" />
        </div>
        <div style="margin-top: 12px; display:flex; gap: 10px; align-items:center;">
          <el-button type="primary" :loading="viewpointSaving" :disabled="viewpointSaving" @click="saveViewpointAndNote">保存</el-button>
          <span v-if="viewpointSaved" style="color:#67c23a;">已保存</span>
          <span v-if="!recordId" style="color:#909399;">学习记录创建后才可保存</span>
        </div>
      </div>

      <div class="info-card" v-if="task.id">
        <h3>任务打卡</h3>
        <p v-if="checkinRequiredSeconds > 0"><strong>要求：</strong>学习满 {{ checkinRequiredMinutes }} 分钟后可打卡</p>
        <p v-else><strong>要求：</strong>无需等待，可直接打卡</p>
        <div style="display:flex; gap: 10px; align-items:center;">
          <el-button type="primary" :loading="checkinLoading" :disabled="checkinDisabled" @click="doCheckin">
            {{ checkedIn ? '已打卡' : '打卡' }}
          </el-button>
          <el-button type="success" :disabled="!canDownloadPdf" @click="downloadPdf">一键生成PDF学习报告</el-button>
          <span v-if="!checkedIn && remainingSeconds > 0" style="color:#909399;">还需 {{ remainingSeconds }} 秒</span>
          <span v-if="checkedIn" style="color:#67c23a;">打卡成功</span>
        </div>
      </div>

      <!-- 兼容旧数据：单素材 -->
      <div class="material-card" v-if="(!task.materials || task.materials.length === 0) && task.material">
        <h3>学习素材</h3>
        <div v-if="taskProgress" style="margin: 10px 0; color:#606266;">
          素材进度：{{ taskProgress.completedMaterials }}/{{ taskProgress.totalMaterials }}
          <span v-if="taskProgress.status === 'COMPLETED'" style="margin-left:8px; color:#67c23a;">任务已完成</span>
        </div>
        <div class="material-info">
          <p><strong>标题：</strong>{{ task.material.title }}</p>
          <p><strong>类型：</strong>{{ task.material.type === 'IMAGE' ? '图片' : task.material.type === 'VIDEO' ? '视频' : '文章' }}</p>
          <div style="margin-top: 8px;">
            <el-tag v-if="isMaterialCompleted(task.material.id)" type="success" size="small">已学完</el-tag>
            <el-tag v-else type="info" size="small">学习中</el-tag>
            <span v-if="!isMaterialCompleted(task.material.id) && task.material.type === 'ARTICLE'" style="margin-left:8px; color:#909399; font-size:12px;">滚动到底自动完成</span>
            <span v-if="!isMaterialCompleted(task.material.id) && task.material.type === 'VIDEO'" style="margin-left:8px; color:#909399; font-size:12px;">播放完成自动完成</span>
            <span v-if="!isMaterialCompleted(task.material.id) && task.material.type === 'IMAGE'" style="margin-left:8px; color:#909399; font-size:12px;">点击查看自动完成</span>
          </div>
          <div v-if="task.material.type === 'IMAGE' && task.material.fileUrl">
            <img :src="task.material.fileUrl" class="material-img" @click="onImageViewed(task.material.id)" />
          </div>
          <div v-if="task.material.type === 'VIDEO' && task.material.fileUrl">
            <video :src="task.material.fileUrl" controls class="material-video" @ended="onVideoEnded(task.material.id)"></video>
          </div>
          <div v-if="task.material.type === 'ARTICLE'" class="article-content" @scroll="onArticleScroll($event, task.material.id)" @click="onArticleScroll($event, task.material.id)">
            {{ task.material.content }}
          </div>
        </div>
      </div>

      <!-- 学生发表评论 -->
      <div class="comment-form-card" ref="interactionAnchor" v-if="task.status !== 'ENDED'">
        <h3>发表评论</h3>
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="发表您的学习心得..." @input="markInteractionActive" />
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
        <p>该任务已结束，无法发表评论</p>
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
                <span class="username">@{{ c.userName }}</span>
              </div>
              <span class="time">{{ formatTime(c.createdAt) }}</span>
              <el-button v-if="task.status !== 'ENDED'" type="text" size="small" @click="showReplyInput(c)">回复</el-button>
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
            <div v-if="replyingTo === c.id && task.status !== 'ENDED'" class="reply-input">
              <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + c.userRealName + '...'" @input="markInteractionActive" />
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
                  <span v-if="r.replyToUserRealName" class="reply-to">回复 <span class="reply-to-name">{{ r.replyToUserRealName }}</span></span>
                  <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
                  <el-button v-if="task.status !== 'ENDED'" type="text" size="small" @click="showReplyToReply(c, r)">回复</el-button>
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
              <div v-if="replyingToReply && replyingToReply.parentId === c.id && task.status !== 'ENDED'" class="reply-input">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 ' + replyingToReply.userRealName + '...'" @input="markInteractionActive" />
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { getTaskByCode, getTaskCommentsWithUser, addTaskComment, toggleTaskCommentLike } from '../../api/task'
import { recordEnter, recordLeave, recordHeartbeat, submitTaskQuiz, getTaskCheckinStatus, checkinTask, saveTaskViewpointAndNote, getLearningRecordDetail, markTaskMaterialCompleted, getTaskProgress, downloadReportPdf } from '../../api/learning'
import { getMaterialFileUrl } from '../../api/material'
import { uploadResourceFile } from '../../utils/upload'

export default {
  name: 'StudentTask',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const task = ref({})
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

    const recordId = ref(null)
    const duration = ref(0)
    const materialDuration = ref(0)
    const interactionDuration = ref(0)
    const activeZone = ref('MATERIAL')
    const interactionAnchor = ref(null)
    const durationTimer = ref(null)
    const heartbeatTimer = ref(null)
    const quizQuestions = ref([])
    const quizMaterials = ref([])
    const quizMode = ref('FLAT')
    const quizAnswers = ref({})
    const quizSubmitting = ref(false)
    const quizDone = ref(false)
    const quizAccuracy = ref(0)

    const taskProgress = ref(null)
    const materialMarkingId = ref(null)

    const autoMarked = new Set()

    const viewpointOptions = ref([])
    const viewpointChoice = ref('')
    const shortNote = ref('')
    const viewpointSaving = ref(false)
    const viewpointSaved = ref(false)

    const checkedIn = ref(false)
    const checkinLoading = ref(false)

    const totalComments = computed(() => {
      let count = comments.value.length
      comments.value.forEach(c => {
        if (c.replies) count += c.replies.length
      })
      return count
    })

    const markMaterialActive = () => {
      activeZone.value = 'MATERIAL'
    }

    const markInteractionActive = () => {
      activeZone.value = 'INTERACTION'
    }

    const handleWindowScroll = () => {
      const el = interactionAnchor.value
      if (!el) return
      const rect = el.getBoundingClientRect()
      if (rect.top <= window.innerHeight * 0.6) {
        markInteractionActive()
      } else {
        markMaterialActive()
      }
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      }
      loadTask()
      window.addEventListener('scroll', handleWindowScroll, { passive: true })
    })

    onBeforeUnmount(async () => {
      window.removeEventListener('scroll', handleWindowScroll)
      await stopTimers()
      if (recordId.value) {
        try {
          await recordLeave({
            recordId: recordId.value,
            duration: duration.value,
            materialDuration: materialDuration.value,
            interactionDuration: interactionDuration.value
          })
        } catch (e) {
          // ignore
        }
      }
    })

    const parseViewpointOptions = () => {
      viewpointOptions.value = []
      try {
        if (task.value.viewpointOptionsJson) {
          const arr = JSON.parse(task.value.viewpointOptionsJson)
          viewpointOptions.value = Array.isArray(arr)
            ? Array.from(new Set(arr
              .map(v => String(v == null ? '' : v).trim())
              .filter(v => !!v)))
            : []
        }
      } catch (e) {
        viewpointOptions.value = []
      }
    }

    const loadTask = async () => {
      try {
        const res = await getTaskByCode(route.params.code)
        if (res.data.code === 200) {
          task.value = res.data.data
          parseViewpointOptions()
          // 为每个素材加载文件URL
          await loadMaterialFileUrls()
          loadComments()
          parseQuiz()
          await loadCheckinStatus()
          await loadTaskProgress()
          await recordLearningEnter()
        } else {
          ElMessage.error(res.data.message || '任务不存在')
          router.push('/student/home')
        }
      } catch (e) {
        ElMessage.error('获取任务失败')
        router.push('/student/home')
      }
    }

    const loadCheckinStatus = async () => {
      if (!user.value.id || !task.value.id) return
      try {
        const res = await getTaskCheckinStatus(user.value.id, task.value.id)
        if (res.data.code === 200) {
          checkedIn.value = !!res.data.data?.checkedIn
        }
      } catch (e) {
        console.error(e)
      }
    }

    const parseQuiz = () => {
      quizQuestions.value = []
      quizMaterials.value = []
      quizMode.value = 'FLAT'
      quizAnswers.value = {}
      quizDone.value = false
      quizAccuracy.value = 0
      try {
        if (task.value.quizJson) {
          const obj = JSON.parse(task.value.quizJson)
          const mode = obj && obj.mode ? String(obj.mode).toUpperCase() : ''
          const materials = obj && obj.materials ? obj.materials : []
          if (mode === 'PER_MATERIAL' && Array.isArray(materials)) {
            quizMode.value = 'PER_MATERIAL'
            quizMaterials.value = materials.map(m => ({
              materialId: String(m.materialId),
              materialTitle: m.materialTitle || '',
              materialType: m.materialType || '',
              questions: Array.isArray(m.questions) ? m.questions : []
            }))
            const ans = {}
            quizMaterials.value.forEach(m => {
              ans[m.materialId] = {}
            })
            quizAnswers.value = ans
            return
          }
          const qs = obj && obj.questions ? obj.questions : []
          quizQuestions.value = Array.isArray(qs) ? qs : []
        }
      } catch (e) {
        quizQuestions.value = []
        quizMaterials.value = []
        quizMode.value = 'FLAT'
      }
    }

    const quizTotalCount = computed(() => {
      if (quizMode.value === 'PER_MATERIAL') {
        return (quizMaterials.value || []).reduce((sum, m) => sum + ((m && m.questions && m.questions.length) ? m.questions.length : 0), 0)
      }
      return quizQuestions.value.length
    })

    const recordLearningEnter = async () => {
      if (!user.value.id) {
        ElMessage.warning('请先登录')
        return
      }
      if (!task.value.id) return
      try {
        const r = await recordEnter({
          studentId: user.value.id,
          type: 'TASK',
          targetId: task.value.id,
          targetCode: task.value.code,
          targetTitle: task.value.title
        })
        if (r.data.code === 200 && r.data.data && r.data.data.id) {
          recordId.value = r.data.data.id
          await loadRecordDetail()
          startTimers()
        } else {
          ElMessage.error(r.data.message || '创建学习记录失败')
        }
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '创建学习记录失败'
        ElMessage.error(msg)
      }
    }

    const loadRecordDetail = async () => {
      if (!recordId.value) return
      try {
        const res = await getLearningRecordDetail(recordId.value)
        if (res.data.code === 200) {
          const r = res.data.data || {}
          viewpointChoice.value = r.viewpointChoice || ''
          shortNote.value = r.shortNote || ''
        }
      } catch (e) {
        console.error(e)
      }
    }

    const loadTaskProgress = async () => {
      if (!user.value.id || !task.value.id) return
      try {
        const res = await getTaskProgress(user.value.id, task.value.id)
        if (res.data.code === 200) {
          taskProgress.value = res.data.data || null
        }
      } catch (e) {
        console.error(e)
      }
    }

    const isMaterialCompleted = (materialId) => {
      if (!taskProgress.value) return false
      const ids = taskProgress.value.completedMaterialIds || []
      return Array.isArray(ids) && ids.includes(materialId)
    }

    const markMaterialDone = async (materialId) => {
      if (!user.value.id || !task.value.id || !materialId) return
      if (isMaterialCompleted(materialId)) return
      if (autoMarked.has(materialId)) return
      autoMarked.add(materialId)
      materialMarkingId.value = materialId
      try {
        const res = await markTaskMaterialCompleted({
          studentId: user.value.id,
          taskId: task.value.id,
          materialId
        })
        if (res.data.code === 200) {
          taskProgress.value = res.data.data || taskProgress.value
          ElMessage.success('素材学习已完成')
        } else {
          ElMessage.error(res.data.message)
          autoMarked.delete(materialId)
        }
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '标记失败'
        ElMessage.error(msg)
        autoMarked.delete(materialId)
      } finally {
        materialMarkingId.value = null
      }
    }

    const onVideoEnded = async (materialId) => {
      markMaterialActive()
      await markMaterialDone(materialId)
    }

    const onImageViewed = async (materialId) => {
      markMaterialActive()
      await markMaterialDone(materialId)
    }

    const onArticleScroll = async (evt, materialId) => {
      markMaterialActive()
      if (!evt || !evt.target) return
      const el = evt.target
      if (!el || typeof el.scrollTop !== 'number') return
      const threshold = 6
      // 若内容不足以产生滚动条，认为“已阅读到末尾”（需要用户至少点击一次触发）
      if (el.scrollHeight <= el.clientHeight + threshold) {
        await markMaterialDone(materialId)
        return
      }
      const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - threshold
      if (nearBottom) {
        await markMaterialDone(materialId)
      }
    }

    const saveViewpointAndNote = async () => {
      if (!recordId.value) {
        await recordLearningEnter()
      }
      if (!recordId.value) {
        ElMessage.error('学习记录未创建，请刷新页面后重试')
        return
      }
      markMaterialActive()
      viewpointSaving.value = true
      try {
        const res = await saveTaskViewpointAndNote({
          recordId: recordId.value,
          studentId: user.value.id,
          viewpointChoice: viewpointChoice.value,
          shortNote: shortNote.value
        })
        if (res.data.code === 200) {
          viewpointSaved.value = true
          ElMessage.success('保存成功')
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '保存失败'
        ElMessage.error(msg)
      } finally {
        viewpointSaving.value = false
        setTimeout(() => { viewpointSaved.value = false }, 1500)
      }
    }

    const startTimers = () => {
      stopTimers()
      duration.value = 0
      materialDuration.value = 0
      interactionDuration.value = 0
      activeZone.value = 'MATERIAL'
      durationTimer.value = setInterval(() => {
        if (document.hidden) return
        duration.value += 1
        if (activeZone.value === 'INTERACTION') {
          interactionDuration.value += 1
        } else {
          materialDuration.value += 1
        }
      }, 1000)
      heartbeatTimer.value = setInterval(async () => {
        if (!recordId.value) return
        try {
          await recordHeartbeat({
            recordId: recordId.value,
            duration: duration.value,
            materialDuration: materialDuration.value,
            interactionDuration: interactionDuration.value
          })
        } catch (e) {
          console.error(e)
        }
      }, 20000)
    }

    const checkinRequiredSeconds = computed(() => {
      const v = task.value && task.value.checkinRequiredSeconds != null ? Number(task.value.checkinRequiredSeconds) : 0
      return Number.isFinite(v) && v > 0 ? Math.floor(v) : 0
    })

    const checkinRequiredMinutes = computed(() => {
      return Math.ceil(checkinRequiredSeconds.value / 60)
    })

    const remainingSeconds = computed(() => {
      if (checkedIn.value) return 0
      const req = checkinRequiredSeconds.value
      if (req <= 0) return 0
      return Math.max(req - duration.value, 0)
    })

    const checkinDisabled = computed(() => {
      if (checkedIn.value) return true
      return remainingSeconds.value > 0 || !recordId.value
    })

    const doCheckin = async () => {
      if (!recordId.value) { ElMessage.error('学习记录未创建'); return }
      markMaterialActive()
      checkinLoading.value = true
      try {
        const res = await checkinTask({
          recordId: recordId.value,
          taskId: task.value.id,
          duration: duration.value,
          materialDuration: materialDuration.value,
          interactionDuration: interactionDuration.value
        })
        if (res.data.code === 200) {
          checkedIn.value = true
          ElMessage.success('打卡成功')
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '打卡失败'
        ElMessage.error(msg)
        if (String(msg).includes('已打卡')) checkedIn.value = true
      } finally {
        checkinLoading.value = false
      }
    }

    const stopTimers = async () => {
      if (durationTimer.value) {
        clearInterval(durationTimer.value)
        durationTimer.value = null
      }
      if (heartbeatTimer.value) {
        clearInterval(heartbeatTimer.value)
        heartbeatTimer.value = null
      }
    }

    const submitQuiz = async () => {
      if (!recordId.value) { ElMessage.error('学习记录未创建'); return }
      if (quizTotalCount.value === 0) { ElMessage.warning('该任务未配置测验题'); return }
      markMaterialActive()
      if (quizMode.value === 'PER_MATERIAL') {
        const unanswered = (quizMaterials.value || []).some(m => {
          const mid = m && m.materialId ? String(m.materialId) : ''
          if (!mid) return true
          const mAns = quizAnswers.value[mid] || {}
          const qs = m.questions || []
          return qs.some((_, idx) => !mAns[idx])
        })
        if (unanswered) { ElMessage.warning('请完成所有题目再提交'); return }
      } else {
        const unanswered = quizQuestions.value.some((_, idx) => !quizAnswers.value[idx])
        if (unanswered) { ElMessage.warning('请完成所有题目再提交'); return }
      }
      quizSubmitting.value = true
      try {
        const res = await submitTaskQuiz({
          recordId: recordId.value,
          taskId: task.value.id,
          taskCode: task.value.code,
          answers: quizAnswers.value
        })
        if (res.data.code === 200) {
          quizDone.value = true
          quizAccuracy.value = res.data.data?.accuracy || 0
          ElMessage.success('提交成功')
          await loadTaskProgress()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('提交失败')
      } finally {
        quizSubmitting.value = false
      }
    }

    const downloadPdf = async () => {
      if (!recordId.value) return
      if (!checkedIn.value) {
        ElMessage.warning('请先完成打卡再生成学习报告')
        return
      }
      if (quizTotalCount.value > 0 && !quizDone.value) {
        ElMessage.warning('请先提交答题再生成学习报告')
        return
      }
      try {
        const res = await downloadReportPdf(recordId.value)
        const ct = (res && res.headers && (res.headers['content-type'] || res.headers['Content-Type'])) || ''
        if (!String(ct).toLowerCase().includes('application/pdf')) {
          const msg = res && res.data ? await res.data.text() : ''
          ElMessage.error(msg || '生成PDF失败')
          return
        }
        const blob = new Blob([res.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `learning-report-${recordId.value}.pdf`
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
      } catch (e) {
        const msg = e?.response?.data ? await e.response.data.text() : (e?.response?.data?.message || e?.message || '生成PDF失败')
        ElMessage.error(msg)
      }
    }

    const quizAccuracyText = computed(() => {
      return `${(quizAccuracy.value * 100).toFixed(2)}%`
    })

    const canDownloadPdf = computed(() => {
      return !!checkedIn.value && (quizTotalCount.value === 0 || !!quizDone.value)
    })
    
    // 单独加载素材的文件URL
    const loadMaterialFileUrls = async () => {
      if (task.value.materials && task.value.materials.length > 0) {
        for (const mat of task.value.materials) {
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
      if (task.value.material && (task.value.material.type === 'IMAGE' || task.value.material.type === 'VIDEO') && !task.value.material.fileUrl) {
        try {
          const res = await getMaterialFileUrl(task.value.material.id)
          if (res.data.code === 200) {
            task.value.material.fileUrl = res.data.data
          }
        } catch (e) { console.error('加载素材文件失败:', e) }
      }
    }

    const loadComments = async () => {
      if (!task.value.id) return
      try {
        const res = await getTaskCommentsWithUser(task.value.id, user.value.id, user.value.role)
        if (res.data.code === 200) {
          comments.value = res.data.data
        }
      } catch (e) { console.error(e) }
    }

    const handleCommentImageChange = (file) => {
      markInteractionActive()
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
      uploadResourceFile(raw, 'task-comment').then((url) => {
        commentImageUrl.value = url
      }).catch((error) => {
        commentImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearCommentImage = () => {
      markInteractionActive()
      commentImageUrl.value = ''
    }

    const handleReplyImageChange = (file) => {
      markInteractionActive()
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
      uploadResourceFile(raw, 'task-reply').then((url) => {
        replyImageUrl.value = url
      }).catch((error) => {
        replyImageUrl.value = ''
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const clearReplyImage = () => {
      markInteractionActive()
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

    const submitComment = async () => {
      markInteractionActive()
      if (!commentContent.value.trim() && !commentImageUrl.value) {
        ElMessage.warning('请输入评论内容')
        return
      }
      submitLoading.value = true
      try {
        const res = await addTaskComment({
          taskId: task.value.id,
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
        const res = await toggleTaskCommentLike(comment.id, { userId: user.value.id, userType: user.value.role })
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
      markInteractionActive()
      replyingTo.value = comment.id
      replyingToReply.value = null
      replyContent.value = ''
    }

    const showReplyToReply = (parentComment, reply) => {
      markInteractionActive()
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
      markInteractionActive()
      if (!replyContent.value.trim() && !replyImageUrl.value) {
        ElMessage.warning('请输入回复内容')
        return
      }
      replyLoading.value = true
      try {
        const res = await addTaskComment({
          taskId: task.value.id,
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
      } catch (e) { ElMessage.error('回复失败') }
      finally { replyLoading.value = false }
    }

    const submitReplyToReply = async (parentComment) => {
      markInteractionActive()
      if (!replyContent.value.trim() && !replyImageUrl.value) {
        ElMessage.warning('请输入回复内容')
        return
      }
      replyLoading.value = true
      try {
        const res = await addTaskComment({
          taskId: task.value.id,
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
      } catch (e) { ElMessage.error('回复失败') }
      finally { replyLoading.value = false }
    }

    return { 
      task, comments, user, totalComments,
      commentContent, submitLoading,
      commentImageUrl, handleCommentImageChange, clearCommentImage,
      replyingTo, replyingToReply, replyContent, replyLoading,
      replyImageUrl, handleReplyImageChange, clearReplyImage,
      interactionAnchor,
      markInteractionActive,
      quizQuestions, quizMaterials, quizMode, quizTotalCount, quizAnswers, quizSubmitting, quizDone, quizAccuracyText,
      canDownloadPdf,
      taskProgress, materialMarkingId, isMaterialCompleted,
      onVideoEnded, onImageViewed, onArticleScroll,
      viewpointOptions, viewpointChoice, shortNote, viewpointSaving, viewpointSaved, saveViewpointAndNote,
      checkedIn,
      checkinLoading,
      checkinRequiredSeconds,
      checkinRequiredMinutes,
      remainingSeconds,
      checkinDisabled,
      doCheckin,
      goBack,
      openNews,
      formatTime,
      submitQuiz,
      downloadPdf,
      submitComment,
      toggleLike,
      showReplyInput, showReplyToReply, cancelReply, submitReply, submitReplyToReply,
      Back 
    }
  }
}
</script>

<style scoped>
.task-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
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
}
.code-box {
  display: flex;
  align-items: center;
  gap: 10px;
}
.code {
  font-size: 18px;
  font-weight: bold;
  color: #c41230;
  background: #fff1f0;
  padding: 5px 15px;
  border-radius: 4px;
}
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-card, .material-card, .comment-form-card, .comments-card {
  background: #fff; padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.info-card h3, .material-card h3, .comment-form-card h3, .comments-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  border-bottom: 2px solid #c41230;
  padding-bottom: 10px;
}
.info-card p {
  margin: 10px 0;
  color: #666;
}
.material-img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
  margin-top: 10px; }
.material-video {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
  margin-top: 10px;
}
.article-content {
  margin-top: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
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
.ended-tip {
  color: #909399;
  text-align: center;
}
.empty-tip {
  color: #909399;
  text-align: center;
  padding: 40px;
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
  font-size: 16px; }
.user-info {
  flex: 1;
}
.name {
  font-weight: bold;
  color: #333;
}
.username {
  color: #909399;
  margin-left: 8px;
  font-size: 13px;
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
  background: #c41230;
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
.quiz-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quiz-item {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 3px solid #c41230;
}
.quiz-stem {
  font-weight: 600;
  margin-bottom: 8px;
}
.quiz-answer {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
