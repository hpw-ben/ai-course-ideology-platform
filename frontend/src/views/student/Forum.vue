<template>
  <div class="forum-container">
    <!-- 顶部导航栏 -->
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="forum" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/student/home')">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/student/thought')">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/student/science')">学习科学</el-menu-item>
          <el-menu-item index="leader" @click="$router.push('/student/leader')">新时代领头人</el-menu-item>
          <el-menu-item index="forum">时事论坛</el-menu-item>
          <el-menu-item index="profile" @click="$router.push('/student/profile')">个人中心</el-menu-item>
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
      <div class="page-header">
        <h1>时事论坛</h1>
        <p>关注时事热点，参与话题讨论</p>
      </div>

      <div class="topics-list" v-if="topics.length > 0">
        <div class="topic-card" v-for="topic in topics" :key="topic.id" @click="goToTopic(topic)">
          <div class="topic-info">
            <h3>{{ topic.title }}</h3>
            <p class="topic-desc">{{ topic.description || '暂无描述' }}</p>
            <div class="topic-meta">
              <span><el-icon><ChatDotRound /></el-icon> {{ topic.commentCount || 0 }} 评论</span>
              <span><el-icon><View /></el-icon> {{ topic.viewCount || 0 }} 浏览</span>
              <span><el-icon><Clock /></el-icon> {{ formatTime(topic.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无话题，敬请期待" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, View, Clock } from '@element-plus/icons-vue'
import { bindStudent } from '../../api/bind'
import { getActiveTopics } from '../../api/public'

export default {
  name: 'StudentForum',
  setup() {
    const router = useRouter()
    const user = ref({})
    const codeInput = ref('')
    const topics = ref([])

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      }
      loadTopics()
    })

    const loadTopics = async () => {
      try {
        const res = await getActiveTopics()
        if (res.data.code === 200) {
          topics.value = res.data.data
        }
      } catch (e) { console.error(e) }
    }

    const goToTopic = (topic) => {
      router.push('/student/topic/' + topic.code)
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const handleCodeSubmit = async () => {
      if (!codeInput.value.trim()) return

      const code = codeInput.value.trim()
      if (code.startsWith('BD')) {
        if (!user.value || !user.value.id) {
          router.push('/login')
          return
        }
        try {
          const res = await bindStudent(user.value.id, code)
          if (res.data.code === 200) {
            user.value = res.data.data
            localStorage.setItem('user', JSON.stringify(user.value))
            ElMessage.success('绑定成功！您的专业已更新为：' + user.value.major)
            codeInput.value = ''
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (e) {
          ElMessage.error('绑定失败')
        }
      } else if (code.startsWith('DIS')) {
        router.push('/student/discussion/' + code)
      } else if (code.startsWith('TASK')) {
        router.push('/student/task/' + code)
      } else {
        ElMessage.warning('无效的码，请检查后重新输入')
      }
    }

    const formatTime = (time) => {
      if (!time) return ''
      return time.replace('T', ' ').substring(0, 16)
    }

    return { user, codeInput, handleCodeSubmit, topics, goToTopic, handleLogout, formatTime, ChatDotRound, View, Clock }
  }
}
</script>

<style scoped>
.forum-container {
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
 .code-input {
   margin-right: 20px;
 }
.user-info {
  color: #fff;
  display: flex;
  align-items: center; gap: 10px;
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
  max-width: 1000px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}
.page-header {
  text-align: center;
  padding: 30px 0;
}
.page-header h1 {
  margin: 0 0 10px 0;
  color: #333;
}
.page-header p {
  margin: 0;
  color: #666;
}
.topics-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.topic-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.topic-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}
.topic-info {
  flex: 1;
}
.topic-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}
.topic-desc {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.topic-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
}
.topic-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}
.topic-materials {
  display: flex;
  gap: 5px;
  align-items: center;
}
.more-tag {
  color: #999;
  font-size: 12px;
}
</style>
