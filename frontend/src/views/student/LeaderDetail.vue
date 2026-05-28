<template>
  <div class="leader-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="leader" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/student/home')">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/student/thought')">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/student/science')">学习科学</el-menu-item>
          <el-menu-item index="leader" @click="$router.push('/student/leader')">新时代领头人</el-menu-item>
          <el-menu-item index="forum" @click="$router.push('/student/forum')">时事论坛</el-menu-item>
          <el-menu-item index="profile" @click="$router.push('/student/profile')">个人中心</el-menu-item>
        </el-menu>
        <div class="user-info">
          <img v-if="user.avatar" :src="user.avatar" class="user-avatar" />
          <span>学生 {{ user.username }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="page-header">
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px">
          <div>
            <h1 style="margin: 0 0 6px 0">{{ detail?.title || '人物事迹详情' }}</h1>
            <div class="meta" v-if="detail">
              <el-tag v-for="t in (detail.tags || [])" :key="t.id" size="small" class="tag-mini">{{ t.name }}</el-tag>
            </div>
            <div v-if="detail" class="time">发布时间：{{ formatTime(detail.createdAt || detail.created_at) }}</div>
          </div>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </div>

      <el-card class="detail-card" shadow="never">
        <div v-if="loading" class="text-muted">加载中...</div>
        <el-empty v-else-if="!detail" description="未找到人物事迹" />
        <div v-else>
          <div v-if="detail.fileUrl" class="cover">
            <img :src="detail.fileUrl" class="cover-img" />
          </div>
          <div class="detail-content">{{ detail.content || '' }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLeaderDetail } from '../../api/public'

export default {
  name: 'StudentLeaderDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const user = ref({})
    const loading = ref(false)
    const detail = ref(null)

    const loadDetail = async () => {
      const id = route.params.id
      if (!id) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        const res = await getLeaderDetail(id)
        if (res.data.code === 200) {
          detail.value = res.data.data
        } else {
          detail.value = null
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        detail.value = null
        ElMessage.error('获取详情失败')
      } finally {
        loading.value = false
      }
    }

    const goBack = () => {
      router.push('/student/leader')
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const formatTime = (val) => {
      if (!val) return ''
      if (Array.isArray(val)) {
        const [y, m, d, hh, mm, ss] = val
        const pad = (n) => String(n ?? '').padStart(2, '0')
        return `${y}-${pad(m)}-${pad(d)} ${pad(hh)}:${pad(mm)}:${pad(ss)}`.trim()
      }
      const s = String(val)
      return s.includes('T') ? s.replace('T', ' ').substring(0, 19) : s.substring(0, 19)
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) user.value = JSON.parse(userData)
      loadDetail()
    })

    watch(() => route.params.id, () => {
      loadDetail()
    })

    return {
      user,
      loading,
      detail,
      formatTime,
      goBack,
      handleLogout
    }
  }
}
</script>

<style scoped>
.leader-container {
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
.nav-menu .el-menu-item:hover, .nav-menu .el-menu-item.is-active {
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
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.main-content {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}
.page-header {
  background: #fff;
  border-radius: 8px;
  padding: 18px 18px;
  margin-bottom: 16px;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-mini {
  margin-right: 0;
}
.time {
  color: #999;
  font-size: 13px;
  margin-top: 8px;
}
.detail-card {
  border-radius: 10px;
}
.cover {
  margin-bottom: 12px;
}
.cover-img {
  width: 100%;
  height: auto;
  max-height: 520px;
  object-fit: contain;
  border-radius: 8px;
  display: block;
  background: #f5f5f5;
}
.detail-content {
  line-height: 1.9;
  color: #333;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-word;
}
.text-muted {
  color: #999;
}
</style>
