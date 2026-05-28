<template>
  <div class="science-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="science" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/teacher/home')">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/teacher/thought')">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/teacher/science')">学习科学</el-menu-item>
          <el-menu-item index="leader" @click="$router.push('/teacher/leader')">新时代领头人</el-menu-item>
          <el-menu-item index="forum" @click="$router.push('/teacher/forum')">时事论坛</el-menu-item>
          <el-menu-item index="manage" @click="$router.push('/teacher/manage')">管理中心</el-menu-item>
        </el-menu>
        <div class="user-info">
          <img v-if="user.avatar" :src="user.avatar" class="user-avatar" />
          <span>教师 {{ user.username }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="page-header">
        <div class="page-title">{{ item.title || '视频播放' }}</div>
        <el-button type="text" class="back-btn" @click="router.back()">返回</el-button>
      </div>

      <div class="content-card" v-loading="loading">
        <video v-if="item.coverImage" class="video-player" controls preload="metadata">
          <source :src="item.coverImage" type="video/mp4" />
        </video>
        <div v-else class="empty">暂无视频</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScienceItemById } from '../../api/public'

export default {
  name: 'TeacherScienceEmpowerWatch',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const user = ref({})
    const loading = ref(false)
    const item = ref({})

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const load = async () => {
      const id = route.params.id
      if (!id) {
        ElMessage.error('参数错误')
        router.back()
        return
      }

      loading.value = true
      try {
        const res = await getScienceItemById(id)
        if (res.data.code === 200 && res.data.data) {
          item.value = res.data.data
        } else {
          ElMessage.error(res.data.message || '内容不存在')
          router.back()
        }
      } catch (e) {
        ElMessage.error('加载失败')
        router.back()
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      } else {
        router.push('/login')
        return
      }
      load()
    })

    return {
      route,
      router,
      user,
      loading,
      item,
      handleLogout
    }
  }
}
</script>

<style scoped>
.science-container { min-height: 100vh; background: #f5f5f5; }
.navbar {
  background: #c41230;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.logo { font-size: 20px; font-weight: bold; color: #fff; margin-right: 40px; }
.nav-menu { flex: 1; background: transparent; border: none; }
.nav-menu .el-menu-item { color: #fff; border-bottom: none; }
.nav-menu .el-menu-item:hover, .nav-menu .el-menu-item.is-active { background: rgba(255,255,255,0.2); color: #fff; }
.user-info { color: #fff; display: flex; align-items: center; gap: 10px; }
.user-info .el-button { color: #fff; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; }

.main-content { padding-top: 80px; max-width: 1200px; margin: 0 auto; padding-left: 20px; padding-right: 20px; }
.page-header { padding: 10px 0 12px; display: flex; align-items: center; justify-content: space-between; }
.page-title { font-size: 22px; font-weight: 900; color: #333; }
.back-btn { padding: 0; }

.content-card { background: #fff; border-radius: 10px; padding: 16px; }
.video-player {
  width: 100%;
  max-height: 70vh;
  border-radius: 10px;
  background: #000;
}
.empty { padding: 30px 0; text-align: center; color: #999; }
</style>
