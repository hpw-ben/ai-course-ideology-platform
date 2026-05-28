<template>
  <div class="science-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="science" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/teacher/home')">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/teacher/thought')">思想</el-menu-item>
          <el-menu-item index="science">学习科学</el-menu-item>
          <el-menu-item index="leader" @click="$router.push('/teacher/leader')">新时代领头人</el-menu-item>
          <el-menu-item index="forum" @click="$router.push('/teacher/forum')">时事论坛</el-menu-item>
          <el-menu-item index="manage" @click="$router.push('/teacher/manage')">管理中心</el-menu-item>
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
          <span>教师 {{ user.username }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="page-header">
        <div class="page-title">学习科学</div>
      </div>

      <div class="carousel-section">
        <el-carousel height="360px" :interval="5000" v-if="banners.length > 0">
          <el-carousel-item v-for="item in banners" :key="item.id">
            <div class="banner-item" @click="handleBannerClick(item)">
              <img :src="item.imageUrl" :alt="item.title" class="banner-image" />
              <div class="banner-overlay" v-if="item.title">
                <h2>{{ item.title }}</h2>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        <div v-else class="no-carousel">
          <p>暂无轮播图</p>
        </div>
      </div>

      <div class="modules">
        <div class="module-card">
          <div class="module-header">
            <div class="module-title">科技赋能</div>
            <el-button type="text" class="more-btn" @click="handleMore('empower')">更多 &gt;</el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in empowerList" :key="item.id">
              <div class="item-card">
                <video v-if="item.coverImage" class="empower-video" controls muted preload="metadata">
                  <source :src="item.coverImage" type="video/mp4" />
                </video>
                <div class="item-title">{{ item.title }}</div>
                <div class="item-desc">{{ item.description || '' }}</div>
                <el-button v-if="item.linkUrl" type="text" class="more-btn" @click="openLink(item.linkUrl)">查看链接</el-button>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="module-card">
          <div class="module-header">
            <div class="module-title">科技新闻</div>
            <el-button type="text" class="more-btn" @click="handleMore('news')">更多 &gt;</el-button>
          </div>
          <div class="list">
            <div class="list-item" v-for="item in newsList" :key="item.id" @click="openScienceDetail('news', item)">
              <div class="list-title">{{ item.title }}</div>
            </div>
          </div>
        </div>

        <div class="module-card">
          <div class="module-header">
            <div class="module-title">科普知识</div>
            <el-button type="text" class="more-btn" @click="handleMore('knowledge')">更多 &gt;</el-button>
          </div>
          <div class="list">
            <div class="list-item" v-for="item in knowledgeList" :key="item.id" @click="openScienceDetail('knowledge', item)">
              <div class="list-title">{{ item.title }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActiveCarousels, getActiveScienceItems } from '../../api/public'
import { getTaskByCode } from '../../api/task'
import { getDiscussionByCode } from '../../api/discussion'

export default {
  name: 'TeacherScience',
  setup() {
    const router = useRouter()
    const user = ref({})
    const codeInput = ref('')
    const banners = ref([])

    const empowerList = ref([])
    const newsList = ref([])
    const knowledgeList = ref([])

    const loadCarousels = async () => {
      try {
        const res = await getActiveCarousels('science')
        if (res.data.code === 200) {
          banners.value = res.data.data || []
        }
      } catch (e) {
        console.error('加载轮播图失败:', e)
      }
    }

    const loadEmpower = async () => {
      try {
        const res = await getActiveScienceItems('empower')
        if (res.data.code === 200) {
          empowerList.value = (res.data.data || []).slice(0, 3)
        }
      } catch (e) {
        console.error('加载科技赋能失败:', e)
      }
    }

    const loadScienceNews = async () => {
      try {
        const res = await getActiveScienceItems('news')
        if (res.data.code === 200) {
          newsList.value = (res.data.data || []).slice(0, 3)
        }
      } catch (e) {
        console.error('加载科技新闻失败:', e)
      }
    }

    const loadScienceKnowledge = async () => {
      try {
        const res = await getActiveScienceItems('knowledge')
        if (res.data.code === 200) {
          knowledgeList.value = (res.data.data || []).slice(0, 3)
        }
      } catch (e) {
        console.error('加载科普知识失败:', e)
      }
    }

    const handleBannerClick = (banner) => {
      if (banner && banner.linkUrl) {
        window.open(banner.linkUrl, '_blank')
      }
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const handleCodeSubmit = async () => {
      const raw = codeInput.value ? String(codeInput.value).trim() : ''
      if (!raw) return

      const code = raw.toUpperCase()

      const tryTask = async () => {
        try {
          const res = await getTaskByCode(code)
          if (res.data.code === 200 && res.data.data && res.data.data.id) {
            router.push(`/teacher/task/${res.data.data.id}`)
            codeInput.value = ''
            return true
          }
        } catch (e) {
          // ignore
        }
        return false
      }

      const tryDiscussion = async () => {
        try {
          const res = await getDiscussionByCode(code)
          if (res.data.code === 200 && res.data.data && res.data.data.id) {
            router.push(`/teacher/discussion/${res.data.data.id}`)
            codeInput.value = ''
            return true
          }
        } catch (e) {
          // ignore
        }
        return false
      }

      if (code.startsWith('TASK')) {
        const ok = await tryTask()
        if (!ok) ElMessage.error('任务码无效或无权限查看')
        return
      }
      if (code.startsWith('DIS')) {
        const ok = await tryDiscussion()
        if (!ok) ElMessage.error('讨论码无效或无权限查看')
        return
      }

      const ok1 = await tryTask()
      if (ok1) return
      const ok2 = await tryDiscussion()
      if (ok2) return
      ElMessage.warning('无效的码，请检查后重新输入')
    }

    const openLink = (url) => {
      if (url) window.open(url, '_blank')
    }

    const openScienceDetail = (module, item) => {
      if (!item || !item.id) return
      if (module === 'news') {
        router.push(`/teacher/science/news/${item.id}`)
        return
      }
      if (module === 'knowledge') {
        router.push(`/teacher/science/knowledge/${item.id}`)
      }
    }

    const handleMore = (module) => {
      if (module === 'empower') {
        router.push('/teacher/science/empower/list')
        return
      }
      if (module === 'news') {
        router.push('/teacher/science/news/list')
        return
      }
      if (module === 'knowledge') {
        router.push('/teacher/science/knowledge/list')
        return
      }
      ElMessage.info('功能开发中')
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      } else {
        router.push('/login')
        return
      }
      loadCarousels()
      loadEmpower()
      loadScienceNews()
      loadScienceKnowledge()
    })

    return {
      user,
      codeInput,
      handleCodeSubmit,
      banners,
      empowerList,
      newsList,
      knowledgeList,
      handleBannerClick,
      handleLogout,
      handleMore,
      openLink,
      openScienceDetail
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
 .code-input { margin-right: 20px; }
.user-info { color: #fff; display: flex; align-items: center; gap: 10px; }
.user-info .el-button { color: #fff; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; }

.main-content { padding-top: 80px; max-width: 1200px; margin: 0 auto; padding-left: 20px; padding-right: 20px; }
.page-header { padding: 10px 0 12px; }
.page-title { font-size: 22px; font-weight: 900; color: #333; }

.carousel-section { margin: 12px 0 16px; }
.banner-item { height: 100%; position: relative; cursor: pointer; overflow: hidden; border-radius: 10px; }
.banner-image { width: 100%; height: 100%; object-fit: cover; }
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  padding: 20px;
  color: #fff;
}
.banner-overlay h2 { font-size: 22px; margin: 0; }
.no-carousel {
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 10px;
  color: #999;
}

.modules { display: flex; flex-direction: column; gap: 14px; }
.module-card { background: #fff; border-radius: 10px; padding: 16px; }
.module-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.module-title { font-size: 18px; font-weight: 900; color: #333; }
.more-btn { padding: 0; }

.item-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
  background: #fafafa;
}
.empower-video {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 8px;
  background: #000;
}
.item-title {
  font-size: 14px;
  font-weight: 800;
  color: #222;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.list { display: flex; flex-direction: column; gap: 10px; }
.list-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 12px; border: 1px solid #eee; border-radius: 10px; background: #fafafa; cursor: pointer; }
.list-title { flex: 1; min-width: 0; font-size: 14px; font-weight: 700; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.list-meta { font-size: 12px; color: #999; flex-shrink: 0; }
</style>
