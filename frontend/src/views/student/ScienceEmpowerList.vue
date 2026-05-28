<template>
  <div class="science-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="science" class="nav-menu">
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
        <div class="page-title">科技赋能</div>
        <el-button type="text" class="back-btn" @click="router.back()">返回</el-button>
      </div>

      <div class="content-card" v-loading="loading">
        <div v-if="items.length" class="title-list">
          <div v-for="item in items" :key="item.id" class="title-row" @click="openWatch(item)">
            <span class="title-text">{{ item.title }}</span>
          </div>
        </div>
        <div v-else class="empty">暂无内容</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActiveScienceItems } from '../../api/public'

export default {
  name: 'StudentScienceEmpowerList',
  setup() {
    const router = useRouter()
    const user = ref({})
    const loading = ref(false)
    const items = ref([])

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const load = async () => {
      loading.value = true
      try {
        const res = await getActiveScienceItems('empower')
        if (res.data.code === 200) {
          items.value = res.data.data || []
        } else {
          items.value = []
        }
      } catch (e) {
        items.value = []
      } finally {
        loading.value = false
      }
    }

    const openWatch = (item) => {
      if (!item || !item.id) return
      router.push(`/student/science/empower/${item.id}`)
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
      router,
      user,
      loading,
      items,
      openWatch,
      handleLogout
    }
  }
}
</script>

<style scoped>
.science-container {
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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
}

.main-content {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}
.page-header {
  padding: 10px 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-size: 22px;
  font-weight: 900;
  color: #333;
}
.back-btn {
  padding: 0;
}

.content-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}
.title-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.title-row {
  padding: 12px 14px;
  border: 1px solid #eee;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
}
.title-row:hover {
  border-color: #c41230;
  background: #fff5f6;
}
.title-text {
  font-size: 14px;
  font-weight: 700;
  color: #222;
}
.empty {
  padding: 30px 0;
  text-align: center;
  color: #999;
}
</style>
