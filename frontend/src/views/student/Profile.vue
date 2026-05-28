<template>
  <div class="profile-container">
    <!-- 顶部导航栏 -->
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="profile" class="nav-menu">
          <el-menu-item index="home" @click="goHome">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/student/thought')">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/student/science')">学习科学</el-menu-item>
          <el-menu-item index="leader" @click="$router.push('/student/leader')">新时代领头人</el-menu-item>
          <el-menu-item index="forum" @click="$router.push('/student/forum')">时事论坛</el-menu-item>
          <el-menu-item index="profile">个人中心</el-menu-item>
        </el-menu>
        <div class="code-input">
          <el-input v-model="codeInput" placeholder="请输入任务码/讨论码/绑定码" size="small" style="width: 200px" @keyup.enter="handleCodeSubmit">
            <template #append>
              <el-button @click="handleCodeSubmit">确定</el-button>
            </template>
          </el-input>
        </div>
        <div class="user-info">
          <img v-if="user.avatar" :src="user.avatar" class="user-avatar" />
          <span>学生 {{ user.username }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧菜单 -->
        <el-col :span="5">
          <div class="sidebar">
            <el-menu :default-active="activeTab" @select="handleTabSelect">
              <el-menu-item index="info">
                <el-icon><User /></el-icon>
                <span>个人信息</span>
              </el-menu-item>
              <el-menu-item index="record">
                <el-icon><Document /></el-icon>
                <span>学习记录</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-col>

        <!-- 右侧内容 -->
        <el-col :span="19">
          <!-- 个人信息 -->
          <div v-if="activeTab === 'info'" class="content-card">
            <h2>个人信息</h2>
            
            <!-- 头像修改 -->
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleAvatarChange"
                accept="image/*"
              >
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
                <div v-else class="avatar-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传头像</span>
                </div>
              </el-upload>
              <el-button type="primary" size="small" @click="saveAvatar" :loading="avatarLoading" style="margin-top: 10px">保存头像</el-button>
            </div>

            <el-form :model="profileForm" label-width="100px" class="profile-form">
              <el-form-item label="真实姓名">
                <el-input v-model="profileForm.realName" disabled />
              </el-form-item>
              <el-form-item label="学号">
                <el-input v-model="profileForm.studentNo" disabled />
              </el-form-item>
              <el-form-item label="账户昵称">
                <el-input v-model="profileForm.username" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
              <el-form-item label="所属专业">
                <el-input v-model="profileForm.major" disabled />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saveLoading" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>

            <!-- 绑定的教师 -->
            <el-divider />
            <h3>绑定的教师</h3>
            <div v-if="bindingTeachers.length > 0" class="teacher-list">
              <div v-for="binding in bindingTeachers" :key="binding.id" class="teacher-item">
                <div class="teacher-info">
                  <span class="teacher-name">{{ binding.teacherRealName || binding.teacherName }}</span>
                  <span class="teacher-major">{{ binding.teacherMajor }}</span>
                </div>
                <span class="bind-time">绑定时间：{{ formatTime(binding.bindTime) }}</span>
              </div>
            </div>
            <el-empty v-else description="暂未绑定教师，请输入绑定码进行绑定" />

            <!-- 密码重置 -->
            <el-divider />
            <h3>密码重置</h3>
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" :loading="resetLoading" @click="resetPassword">重置密码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 学习记录 -->
          <div v-if="activeTab === 'record'" class="content-card">
            <h2>学习记录</h2>
            
            <!-- 学习统计 -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ stats.recordCount }}</div>
                  <div class="stat-label">总评论数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ stats.discussionCount }}</div>
                  <div class="stat-label">参与讨论数</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ stats.taskCount }}</div>
                  <div class="stat-label">打卡任务数</div>
                </div>
              </el-col>
            </el-row>

            <div style="margin-top: 20px;">
              <h3>报告中心</h3>
              <div style="display: flex; gap: 12px; align-items: center; margin: 10px 0 15px 0; flex-wrap: wrap;">
                <el-select v-model="reportTypeFilter" placeholder="筛选类型" size="small" style="width: 160px">
                  <el-option label="全部" value="ALL" />
                  <el-option label="学习报告" value="TASK" />
                  <el-option label="讨论报告" value="DISCUSSION_REPORT" />
                  <el-option label="讨论内容" value="DISCUSSION_CONTENT" />
                </el-select>
                <el-input v-model="reportKeyword" placeholder="按标题/码搜索" size="small" style="width: 240px" clearable />
              </div>

              <el-table :data="displayedReportItems" stripe v-if="displayedReportItems.length > 0">
                <el-table-column label="类型" width="120">
                  <template #default="{ row }">
                    <el-tag
                      :type="row.reportKind === 'TASK' ? 'primary' : (row.reportKind === 'DISCUSSION_REPORT' ? 'success' : 'warning')"
                    >
                      {{ row.reportKind === 'TASK' ? '学习报告' : (row.reportKind === 'DISCUSSION_REPORT' ? '讨论报告' : '讨论内容') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" />
                <el-table-column prop="code" label="码" width="140" />
                <el-table-column label="时间" width="180">
                  <template #default="{ row }">{{ formatTime(row.time) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="140">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="downloadReportItem(row)">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无可下载报告" />
              <div v-if="filteredReportItems.length > 3" class="section-expand-wrapper">
                <el-button type="primary" link @click="toggleReportExpand">
                  <el-icon v-if="!reportExpanded"><ArrowDown /></el-icon>
                  <el-icon v-else><ArrowUp /></el-icon>
                  {{ reportExpanded ? '收起' : `展开全部 (还有${filteredReportItems.length - 3}条)` }}
                </el-button>
              </div>
            </div>

            <div style="margin-top: 20px;">
              <h3>任务完成情况</h3>
              <el-tabs v-model="taskStatusTab" style="margin-top: 10px;">
                <el-tab-pane label="待完成" name="IN_PROGRESS">
                  <el-table :data="taskInProgressList" stripe v-loading="taskListLoading" v-if="taskInProgressList.length > 0">
                    <el-table-column prop="taskTitle" label="任务" />
                    <el-table-column prop="taskCode" label="任务码" width="140" />
                    <el-table-column label="操作" width="120">
                      <template #default="{ row }">
                        <el-button type="primary" link @click="goTask(row.taskCode)">进入</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-empty v-if="!taskListLoading && taskInProgressList.length === 0" description="暂无待完成任务" />
                </el-tab-pane>
                <el-tab-pane label="已完成" name="COMPLETED">
                  <el-table :data="taskCompletedList" stripe v-loading="taskListLoading" v-if="taskCompletedList.length > 0">
                    <el-table-column prop="taskTitle" label="任务" />
                    <el-table-column prop="taskCode" label="任务码" width="140" />
                    <el-table-column prop="completedAt" label="完成时间" width="180">
                      <template #default="{ row }">{{ formatTime(row.completedAt) }}</template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                      <template #default="{ row }">
                        <el-button type="primary" link @click="goTask(row.taskCode)">查看</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-empty v-if="!taskListLoading && taskCompletedList.length === 0" description="暂无已完成任务" />
                </el-tab-pane>
              </el-tabs>
            </div>

            <div style="margin-top: 20px;">
              <h3>讨论参与</h3>
              <div style="display:flex; gap: 12px; align-items: center; margin: 10px 0 12px 0; flex-wrap: wrap;">
                <el-input v-model="discussionKeyword" placeholder="按讨论标题/讨论码搜索" size="small" style="width: 260px" clearable />
                <el-button size="small" :loading="discussionListLoading" @click="loadDiscussionParticipation">刷新</el-button>
              </div>
              <el-table :data="displayedDiscussionParticipation" stripe v-loading="discussionListLoading" v-if="displayedDiscussionParticipation.length > 0">
                <el-table-column prop="title" label="讨论" />
                <el-table-column prop="code" label="讨论码" width="140" />
                <el-table-column label="我的发言" width="120">
                  <template #default="{ row }">
                    {{ Number(row.myCommentCount || 0) + Number(row.myReplyCount || 0) }}
                  </template>
                </el-table-column>
                <el-table-column label="最近参与" width="180">
                  <template #default="{ row }">{{ formatTime(row.lastEnterTime) || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <div class="discussion-actions">
                      <el-button type="primary" link @click="goDiscussion(row.code)">进入</el-button>
                      <el-button type="success" link @click="exportMyDiscussionReportPdf(row.code)">导出我的报告</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无讨论参与记录" />
              <div v-if="filteredDiscussionParticipation.length > 3" class="section-expand-wrapper">
                <el-button type="primary" link @click="toggleDiscussionExpand">
                  <el-icon v-if="!discussionExpanded"><ArrowDown /></el-icon>
                  <el-icon v-else><ArrowUp /></el-icon>
                  {{ discussionExpanded ? '收起' : `展开全部 (还有${filteredDiscussionParticipation.length - 3}条)` }}
                </el-button>
              </div>
            </div>

            <div style="margin-top: 20px;">
              <h3>学习足迹（素材）</h3>
              <div style="display:flex; gap: 12px; align-items: center; margin: 10px 0 12px 0; flex-wrap: wrap;">
                <el-input v-model="materialKeyword" placeholder="按素材标题/任务码搜索" size="small" style="width: 260px" clearable />
                <el-select v-model="materialTypeFilter" placeholder="类型" size="small" style="width: 140px">
                  <el-option label="全部" value="ALL" />
                  <el-option label="文章" value="ARTICLE" />
                  <el-option label="图片" value="IMAGE" />
                  <el-option label="视频" value="VIDEO" />
                </el-select>
                <el-button size="small" :loading="materialFootprintsLoading" @click="loadMaterialFootprints">刷新</el-button>
              </div>
              <el-table :data="displayedMaterialFootprints" stripe v-loading="materialFootprintsLoading" v-if="displayedMaterialFootprints.length > 0">
                <el-table-column prop="materialTitle" label="素材" />
                <el-table-column label="类型" width="100">
                  <template #default="{ row }">
                    <el-tag size="small">
                      {{ row.materialType === 'IMAGE' ? '图片' : row.materialType === 'VIDEO' ? '视频' : '文章' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="taskCode" label="来源任务" width="140" />
                <el-table-column label="最近学习" width="180">
                  <template #default="{ row }">{{ formatTime(row.lastLearnTime) || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="goMaterial(row.materialId)">查看素材</el-button>
                    <el-button type="success" link v-if="row.taskCode" @click="goTask(row.taskCode)">查看任务</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无素材学习足迹" />
              <div v-if="filteredMaterialFootprints.length > 3" class="section-expand-wrapper">
                <el-button type="primary" link @click="toggleMaterialExpand">
                  <el-icon v-if="!materialExpanded"><ArrowDown /></el-icon>
                  <el-icon v-else><ArrowUp /></el-icon>
                  {{ materialExpanded ? '收起' : `展开全部 (还有${filteredMaterialFootprints.length - 3}条)` }}
                </el-button>
              </div>
            </div>

            <!-- 学习轨迹 -->
            <div class="track-header">
              <h3 style="margin-top: 20px">学习轨迹</h3>
              <span v-if="learningRecords.length > 3" class="track-count">共 {{ learningRecords.length }} 条记录</span>
            </div>
            <el-table :data="displayedRecords" stripe v-if="learningRecords.length > 0">
              <el-table-column label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.type === 'DISCUSSION' ? 'success' : 'primary'">
                    {{ row.type === 'DISCUSSION' ? '讨论' : '任务' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetTitle" label="标题" />
              <el-table-column prop="targetCode" label="码" width="140" />
              <el-table-column label="进入时间" width="180">
                <template #default="{ row }">{{ formatTime(row.enterTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button v-if="row.type === 'DISCUSSION'" type="success" link @click="exportDiscussionContentPdf(row.targetCode)">导出</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="learningRecords.length > 3" class="expand-btn-wrapper">
              <el-button type="primary" link @click="toggleExpand">
                <el-icon v-if="!isExpanded"><ArrowDown /></el-icon>
                <el-icon v-else><ArrowUp /></el-icon>
                {{ isExpanded ? '收起' : `展开全部 (还有${learningRecords.length - 3}条)` }}
              </el-button>
            </div>
            <el-empty v-if="learningRecords.length === 0" description="暂无学习轨迹，快去参与学习吧！" />
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Document, Plus, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { updateUsername, updatePassword, updateAvatar, updatePhone } from '../../api/auth'
import { getBindingTeachers, bindStudent } from '../../api/bind'
import { getLearningRecords, getLearningStats, getStudentTaskCompletions, downloadReportPdf, getStudentDiscussionParticipation, getStudentMaterialFootprints } from '../../api/learning'
import { downloadStudentDiscussionContentPdf, downloadStudentDiscussionReportPdf } from '../../api/discussion'
import { uploadResourceFile } from '../../utils/upload'

export default {
  name: 'StudentProfile',
  setup() {
    const router = useRouter()
    const user = ref({})
    const activeTab = ref('info')
    const codeInput = ref('')
    const avatarUrl = ref('')
    const avatarFile = ref(null)
    const avatarLoading = ref(false)
    const saveLoading = ref(false)
    const resetLoading = ref(false)
    const passwordFormRef = ref(null)
    const bindingTeachers = ref([])
    const learningRecords = ref([])
    const stats = ref({ totalDuration: 0, discussionCount: 0, taskCount: 0, recordCount: 0 })
    const isExpanded = ref(false)

    const reportExpanded = ref(false)
    const discussionExpanded = ref(false)
    const materialExpanded = ref(false)

    const reportTypeFilter = ref('ALL')
    const reportKeyword = ref('')

    const discussionParticipation = ref([])
    const discussionListLoading = ref(false)
    const discussionKeyword = ref('')

    const materialFootprints = ref([])
    const materialFootprintsLoading = ref(false)
    const materialKeyword = ref('')
    const materialTypeFilter = ref('ALL')

    const taskStatusTab = ref('IN_PROGRESS')
    const taskInProgressList = ref([])
    const taskCompletedList = ref([])
    const taskListLoading = ref(false)

    // 计算显示的记录（折叠时只显示3条）
    const displayedRecords = computed(() => {
      if (isExpanded.value || learningRecords.value.length <= 3) {
        return learningRecords.value
      }
      return learningRecords.value.slice(0, 3)
    })

    const filteredDiscussionParticipation = computed(() => {
      const kw = (discussionKeyword.value || '').trim().toLowerCase()
      const list = discussionParticipation.value || []
      if (!kw) return list
      return list.filter((x) => {
        const t = (x.title || '').toLowerCase()
        const c = (x.code || '').toLowerCase()
        return t.includes(kw) || c.includes(kw)
      })
    })

    const displayedDiscussionParticipation = computed(() => {
      const list = filteredDiscussionParticipation.value || []
      if (discussionExpanded.value || list.length <= 3) return list
      return list.slice(0, 3)
    })

    const filteredMaterialFootprints = computed(() => {
      const kw = (materialKeyword.value || '').trim().toLowerCase()
      const type = materialTypeFilter.value
      const list = materialFootprints.value || []
      return list.filter((x) => {
        if (!x) return false
        if (type && type !== 'ALL' && String(x.materialType || '').toUpperCase() !== String(type).toUpperCase()) return false
        if (!kw) return true
        const mt = (x.materialTitle || '').toLowerCase()
        const tc = (x.taskCode || '').toLowerCase()
        return mt.includes(kw) || tc.includes(kw)
      })
    })

    const displayedMaterialFootprints = computed(() => {
      const list = filteredMaterialFootprints.value || []
      if (materialExpanded.value || list.length <= 3) return list
      return list.slice(0, 3)
    })

    const toggleExpand = () => {
      isExpanded.value = !isExpanded.value
    }

    const toggleReportExpand = () => {
      reportExpanded.value = !reportExpanded.value
    }

    const toggleDiscussionExpand = () => {
      discussionExpanded.value = !discussionExpanded.value
    }

    const toggleMaterialExpand = () => {
      materialExpanded.value = !materialExpanded.value
    }

    const downloadPdfBlob = async (promise, filename, failText) => {
      try {
        const res = await promise
        const ct = (res && res.headers && (res.headers['content-type'] || res.headers['Content-Type'])) || ''
        if (!String(ct).toLowerCase().includes('application/pdf')) {
          const msg = res && res.data ? await res.data.text() : ''
          ElMessage.error(msg || failText)
          return
        }
        const blob = new Blob([res.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
      } catch (e) {
        const msg = e?.response?.data ? await e.response.data.text() : (e?.response?.data?.message || e?.message || failText)
        ElMessage.error(msg)
      }
    }

    const toStringSafe = (v) => (v === null || v === undefined) ? '' : String(v)

    const reportItems = computed(() => {
      const list = (learningRecords.value || []).filter(Boolean)

      const taskMap = new Map()
      const discussionMap = new Map()

      for (const r of list) {
        if (!r || !r.type) continue

        if (r.type === 'TASK') {
          const key = toStringSafe(r.targetId) || toStringSafe(r.targetCode) || toStringSafe(r.id)
          const prev = taskMap.get(key)
          const t1 = prev ? (prev.enterTime || prev.createdAt || prev.id || 0) : 0
          const t2 = r.enterTime || r.createdAt || r.id || 0
          if (!prev || t2 > t1) taskMap.set(key, r)
        }

        if (r.type === 'DISCUSSION') {
          const key = toStringSafe(r.targetCode) || toStringSafe(r.targetId) || toStringSafe(r.id)
          const prev = discussionMap.get(key)
          const t1 = prev ? (prev.enterTime || prev.createdAt || prev.id || 0) : 0
          const t2 = r.enterTime || r.createdAt || r.id || 0
          if (!prev || t2 > t1) discussionMap.set(key, r)
        }
      }

      const items = []

      for (const r of taskMap.values()) {
        // 学习报告按“已打卡”即可提供下载入口（下载时由后端生成PDF）；不要求提前生成过PDF
        if (!r || !r.checkedIn) continue
        items.push({
          reportKind: 'TASK',
          title: r.targetTitle,
          code: r.targetCode,
          time: r.reportGeneratedAt || r.checkinTime || r.enterTime || r.createdAt,
          recordId: r.id
        })
      }

      for (const r of discussionMap.values()) {
        items.push({
          reportKind: 'DISCUSSION_REPORT',
          title: r.targetTitle,
          code: r.targetCode,
          time: r.enterTime || r.createdAt,
          discussionCode: r.targetCode
        })
        items.push({
          reportKind: 'DISCUSSION_CONTENT',
          title: r.targetTitle,
          code: r.targetCode,
          time: r.enterTime || r.createdAt,
          discussionCode: r.targetCode
        })
      }

      items.sort((a, b) => {
        const ta = a && a.time ? new Date(a.time).getTime() : 0
        const tb = b && b.time ? new Date(b.time).getTime() : 0
        return tb - ta
      })

      return items
    })

    const filteredReportItems = computed(() => {
      const kw = (reportKeyword.value || '').trim().toLowerCase()
      const type = reportTypeFilter.value

      return reportItems.value.filter((x) => {
        if (!x) return false
        if (type && type !== 'ALL' && x.reportKind !== type) return false
        if (!kw) return true
        const t = (x.title || '').toLowerCase()
        const c = (x.code || '').toLowerCase()
        return t.includes(kw) || c.includes(kw)
      })
    })

    const displayedReportItems = computed(() => {
      const list = filteredReportItems.value || []
      if (reportExpanded.value || list.length <= 3) return list
      return list.slice(0, 3)
    })

    const downloadReportItem = async (item) => {
      if (!item) return
      if (!user.value || !user.value.id) {
        ElMessage.warning('请先登录')
        return
      }
      if (item.reportKind === 'TASK') {
        if (!item.recordId) {
          ElMessage.warning('记录不存在')
          return
        }
        await downloadPdfBlob(downloadReportPdf(item.recordId), `learning-report-${item.recordId}.pdf`, '生成PDF失败')
        await loadLearningRecords()
        return
      }

      if (!item.discussionCode) {
        ElMessage.warning('讨论码为空')
        return
      }

      if (item.reportKind === 'DISCUSSION_REPORT') {
        await downloadPdfBlob(
          downloadStudentDiscussionReportPdf(item.discussionCode, user.value.id),
          `discussion-report-${item.discussionCode}.pdf`,
          '导出失败'
        )
        return
      }

      if (item.reportKind === 'DISCUSSION_CONTENT') {
        await exportDiscussionContentPdf(item.discussionCode)
      }
    }

    const profileForm = reactive({
      username: '',
      realName: '',
      major: '',
      studentNo: '',
      phone: ''
    })

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const passwordRules = {
      oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
        profileForm.username = user.value.username
        profileForm.realName = user.value.realName
        profileForm.major = user.value.major
        profileForm.studentNo = user.value.studentNo || ''
        profileForm.phone = user.value.phone || ''
        avatarUrl.value = user.value.avatar || ''
        loadBindingTeachers()
        loadLearningRecords()
        loadDiscussionParticipation()
        loadMaterialFootprints()
        loadStats()
        loadTaskLists()
      } else {
        router.push('/login')
      }
    })

    const normalizeDiscussionItem = (x) => {
      if (!x) return null
      return {
        discussionId: x.discussionId,
        code: x.code,
        title: x.title,
        lastEnterTime: x.lastEnterTime,
        myCommentCount: x.myCommentCount,
        myReplyCount: x.myReplyCount,
        myLastCommentAt: x.myLastCommentAt
      }
    }

    const loadDiscussionParticipation = async () => {
      if (!user.value.id) return
      discussionListLoading.value = true
      try {
        const res = await getStudentDiscussionParticipation(user.value.id)
        if (res.data.code === 200) {
          discussionParticipation.value = (res.data.data || []).map(normalizeDiscussionItem).filter(Boolean)
        }
      } catch (e) {
        console.error(e)
      } finally {
        discussionListLoading.value = false
      }
    }

    const normalizeMaterialFootprint = (x) => {
      if (!x) return null
      return {
        materialId: x.materialId,
        materialTitle: x.materialTitle,
        materialType: x.materialType,
        materialCategory: x.materialCategory,
        taskId: x.taskId,
        taskCode: x.taskCode,
        taskTitle: x.taskTitle,
        lastLearnTime: x.lastLearnTime
      }
    }

    const loadMaterialFootprints = async () => {
      if (!user.value.id) return
      materialFootprintsLoading.value = true
      try {
        const res = await getStudentMaterialFootprints(user.value.id)
        if (res.data.code === 200) {
          materialFootprints.value = (res.data.data || []).map(normalizeMaterialFootprint).filter(Boolean)
        }
      } catch (e) {
        console.error(e)
      } finally {
        materialFootprintsLoading.value = false
      }
    }

    const loadBindingTeachers = async () => {
      try {
        const res = await getBindingTeachers(user.value.id)
        if (res.data.code === 200) {
          bindingTeachers.value = res.data.data
        }
      } catch (e) {
        console.error(e)
      }
    }

    const loadLearningRecords = async () => {
      try {
        const res = await getLearningRecords(user.value.id)
        if (res.data.code === 200) {
          learningRecords.value = res.data.data
        }
      } catch (e) {
        console.error(e)
      }
    }

    const loadTaskLists = async () => {
      if (!user.value.id) return
      taskListLoading.value = true
      try {
        const [a, b] = await Promise.all([
          getStudentTaskCompletions(user.value.id, 'IN_PROGRESS'),
          getStudentTaskCompletions(user.value.id, 'COMPLETED')
        ])
        if (a.data.code === 200) taskInProgressList.value = a.data.data || []
        if (b.data.code === 200) taskCompletedList.value = b.data.data || []
      } catch (e) {
        console.error(e)
      } finally {
        taskListLoading.value = false
      }
    }

    const goTask = (code) => {
      if (!code) return
      router.push('/student/task/' + code)
    }

    const goDiscussion = (code) => {
      if (!code) return
      router.push('/student/discussion/' + code)
    }

    const goMaterial = (id) => {
      if (!id) return
      router.push('/student/material/' + id)
    }

    const loadStats = async () => {
      try {
        const res = await getLearningStats(user.value.id)
        if (res.data.code === 200) {
          stats.value = res.data.data
        }
      } catch (e) {
        console.error(e)
      }
    }

    const handleTabSelect = (index) => {
      activeTab.value = index
      if (index === 'record') {
        loadTaskLists()
        loadDiscussionParticipation()
        loadMaterialFootprints()
      }
    }

    const goHome = () => {
      router.push('/student/home')
    }

    const handleCodeSubmit = async () => {
      if (!codeInput.value.trim()) return
      
      const code = codeInput.value.trim()
      // 判断是否为绑定码（以BD开头）
      if (code.startsWith('BD')) {
        try {
          const res = await bindStudent(user.value.id, code)
          if (res.data.code === 200) {
            user.value = res.data.data
            localStorage.setItem('user', JSON.stringify(user.value))
            ElMessage.success('绑定成功！')
            codeInput.value = ''
            loadBindingTeachers() // 刷新绑定教师列表
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (e) {
          ElMessage.error('绑定失败')
        }
      } else if (code.startsWith('DIS')) {
        // 讨论码，跳转到讨论页面
        router.push('/student/discussion/' + code)
      } else if (code.startsWith('TASK')) {
        // 任务码，跳转到任务页面
        router.push('/student/task/' + code)
      } else {
        ElMessage.warning('无效的码，请检查后重新输入')
      }
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const handleAvatarChange = (file) => {
      const rawFile = file && file.raw ? file.raw : null
      if (!rawFile) return
      if (!String(rawFile.type || '').startsWith('image/')) {
        ElMessage.warning('只能上传图片文件')
        return
      }
      if (Number(rawFile.size || 0) > 2 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过2MB')
        return
      }
      avatarUrl.value = URL.createObjectURL(rawFile)
      avatarFile.value = rawFile
    }

    const saveAvatar = async () => {
      if (!avatarFile.value && !avatarUrl.value) {
        ElMessage.warning('请先选择头像')
        return
      }
      avatarLoading.value = true
      try {
        const avatarValue = avatarFile.value
          ? await uploadResourceFile(avatarFile.value, 'avatar')
          : avatarUrl.value
        const res = await updateAvatar({
          userId: user.value.id,
          avatar: avatarValue,
          role: 'STUDENT'
        })
        if (res.data.code === 200) {
          user.value.avatar = avatarValue
          avatarUrl.value = avatarValue
          avatarFile.value = null
          localStorage.setItem('user', JSON.stringify(user.value))
          ElMessage.success('头像保存成功')
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        avatarLoading.value = false
      }
    }

    const saveProfile = async () => {
      if (!profileForm.username.trim()) {
        ElMessage.warning('昵称不能为空')
        return
      }

      if (!profileForm.phone || !/^\d{11}$/.test(String(profileForm.phone).trim())) {
        ElMessage.warning('手机号必须为11位数字')
        return
      }

      saveLoading.value = true
      try {
        const res = await updateUsername({
          userId: user.value.id,
          newUsername: profileForm.username,
          role: 'STUDENT'
        })
        if (res.data.code !== 200) {
          ElMessage.error(res.data.message)
          return
        }

        const phoneRes = await updatePhone({
          userId: user.value.id,
          phone: profileForm.phone,
          role: 'STUDENT'
        })
        if (phoneRes.data.code !== 200) {
          ElMessage.error(phoneRes.data.message)
          return
        }

        user.value.username = profileForm.username
        user.value.phone = profileForm.phone
        localStorage.setItem('user', JSON.stringify(user.value))
        ElMessage.success('保存成功')
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }

    const resetPassword = async () => {
      passwordFormRef.value.validate(async (valid) => {
        if (!valid) return
        resetLoading.value = true
        try {
          const res = await updatePassword({
            userId: user.value.id,
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword,
            role: 'STUDENT'
          })
          if (res.data.code === 200) {
            ElMessage.success('密码重置成功，请重新登录')
            localStorage.removeItem('user')
            router.push('/login')
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (e) {
          ElMessage.error('重置失败')
        } finally {
          resetLoading.value = false
        }
      })
    }

    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }

    const formatDuration = (seconds) => {
      if (!seconds || seconds === 0) return '0分钟'
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      if (hours > 0) {
        return `${hours}小时${minutes}分钟`
      }
      return `${minutes}分钟`
    }

    const exportDiscussionContentPdf = async (code) => {
      if (!code) return
      if (!user.value || !user.value.id) {
        ElMessage.warning('请先登录')
        return
      }
      try {
        const res = await downloadStudentDiscussionContentPdf(code, user.value.id)
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
        a.download = `discussion-content-${code}.pdf`
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
      } catch (e) {
        const msg = e?.response?.data ? await e.response.data.text() : (e?.response?.data?.message || e?.message || '导出失败')
        ElMessage.error(msg)
      }
    }

    const exportMyDiscussionReportPdf = async (code) => {
      if (!code) return
      if (!user.value || !user.value.id) {
        ElMessage.warning('请先登录')
        return
      }
      await downloadPdfBlob(
        downloadStudentDiscussionReportPdf(code, user.value.id),
        `discussion-report-${code}.pdf`,
        '导出失败'
      )
    }

    return {
      user,
      activeTab,
      codeInput,
      avatarUrl,
      avatarLoading,
      saveLoading,
      resetLoading,
      passwordFormRef,
      profileForm,
      passwordForm,
      passwordRules,
      bindingTeachers,
      learningRecords,
      displayedRecords,
      isExpanded,
      stats,
      reportExpanded,
      reportTypeFilter,
      reportKeyword,
      filteredReportItems,
      displayedReportItems,
      downloadReportItem,
      toggleReportExpand,
      discussionKeyword,
      discussionListLoading,
      filteredDiscussionParticipation,
      discussionExpanded,
      displayedDiscussionParticipation,
      toggleDiscussionExpand,
      materialKeyword,
      materialTypeFilter,
      materialFootprintsLoading,
      filteredMaterialFootprints,
      materialExpanded,
      displayedMaterialFootprints,
      toggleMaterialExpand,
      taskStatusTab,
      taskInProgressList,
      taskCompletedList,
      taskListLoading,
      handleTabSelect,
      goHome,
      handleCodeSubmit,
      handleLogout,
      goTask,
      goDiscussion,
      goMaterial,
      loadDiscussionParticipation,
      loadMaterialFootprints,
      handleAvatarChange,
      saveAvatar,
      saveProfile,
      resetPassword,
      formatTime,
      formatDuration,
      exportDiscussionContentPdf,
      exportMyDiscussionReportPdf,
      toggleExpand,
      User,
      Document,
      Plus,
      ArrowDown,
      ArrowUp
    }
  }
}
</script>


<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f5f5;
}
.navbar {
  background: #c41230;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  margin-right: 40px;
}
.nav-menu {
  flex: 1;
  background: transparent;
  border: none;
}
.nav-menu .el-menu-item {
  color: #fff;
  border-bottom: none;
}
.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.user-info {
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info .el-button {
  color: #fff;
}
.code-input {
  margin-right: 20px;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}
.main-content {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}
.sidebar {
  background: #fff;
  border-radius: 8px;
  padding: 10px 0;
}
.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 600px;
}
.content-card h2 {
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #c41230;
  color: #333;
}
.content-card h3 {
  margin: 0 0 15px 0;
  color: #333;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}
.avatar-uploader {
  cursor: pointer;
}
.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #f5f5f5;
  border: 2px dashed #ddd;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}
.profile-form, .password-form {
  max-width: 500px;
}
.teacher-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.teacher-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #f9f9f9;
  border-radius: 8px;
}
.teacher-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.teacher-name {
  font-weight: bold;
  color: #333;
}
.teacher-major {
  color: #666;
  font-size: 14px;
}
.bind-time {
  color: #999;
  font-size: 12px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  background: linear-gradient(135deg, #c41230 0%, #e74c3c 100%);
  color: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}
.stat-label {
  font-size: 14px;
  opacity: 0.9;
}
.track-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.track-header h3 {
  margin: 0;
}
.track-count {
  color: #909399;
  font-size: 14px;
}
.expand-btn-wrapper {
  text-align: center;
  margin-top: 15px;
  padding: 10px 0;
  border-top: 1px dashed #eee;
}
.expand-btn-wrapper .el-button {
  font-size: 14px;
}

.section-expand-wrapper {
  text-align: center;
  margin-top: 10px;
}

.discussion-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.discussion-actions .el-button {
  padding: 0;
  white-space: nowrap;
}

.discussion-actions .el-button + .el-button {
  margin-left: 0;
}
</style>
