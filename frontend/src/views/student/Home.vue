<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect" class="nav-menu">
          <el-menu-item index="home">首页</el-menu-item>
          <el-menu-item index="thought">思想</el-menu-item>
          <el-menu-item index="science">学习科学</el-menu-item>
          <el-menu-item index="leader">新时代领头人</el-menu-item>
          <el-menu-item index="forum">时事论坛</el-menu-item>
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
      <!-- 轮播图 -->
      <div class="carousel-section">
        <el-carousel height="400px" :interval="5000" v-if="banners.length > 0">
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

      <!-- 新闻资讯 -->
      <div class="news-section">
        <div class="section-header">
          <h3>新闻资讯</h3>
          <el-button type="text" @click="goNewsMore">更多 ></el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="16">
            <div class="news-list">
              <div class="news-item" v-for="news in newsWithCover" :key="news.id" @click="viewNews(news)">
                <div class="news-img">
                  <img :src="getNewsCover(news)" :alt="news.title" class="news-cover" @error="handleNewsCoverError" />
                </div>
                <div class="news-info">
                  <h4>{{ news.title }}</h4>
                  <p>{{ news.summary || (news.content ? (news.content.substring(0, 100) + '...') : '') }}</p>
                  <div class="news-meta">
                    <span class="news-author" v-if="news.author">{{ news.author }}</span>
                    <span class="news-date">{{ formatTime(news.createdAt) }}</span>
                    <span class="news-views">浏览 {{ news.viewCount || 0 }}</span>
                  </div>
                </div>
              </div>

              <div class="news-title-list" v-if="newsNoCover.length > 0">
                <div class="news-title-item" v-for="news in newsNoCover" :key="news.id" @click="viewNews(news)">
                  <span class="news-title">{{ news.title }}</span>
                  <span class="news-date">{{ formatTime(news.createdAt) }}</span>
                </div>
              </div>
              <div v-if="newsList.length === 0" class="no-news">
                <p>暂无新闻资讯</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="hot-topics">
              <div class="topic-header">热门话题</div>
              <div class="topic-list">
                <div class="topic-item" v-for="(topic, index) in hotTopics" :key="index" @click="viewHotTopic(topic)">
                  <span class="topic-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                  <span class="topic-title">{{ topic.title }}</span>
                </div>
              </div>
            </div>

            <div class="recommend-topics">
              <div class="topic-header">为你推荐</div>
              <div class="topic-list">
                <div class="topic-item" v-for="(m, index) in recommendedMaterials" :key="m.id" @click="viewMaterial(m)">
                  <span class="topic-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                  <span class="topic-title">{{ m.title }}</span>
                </div>
                <div v-if="recommendedMaterials.length === 0" class="no-recommend">
                  <p>暂无推荐</p>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bindStudent } from '../../api/bind'
import { getActiveCarousels, getLatestNews, getHotTopics } from '../../api/public'
import { getApprovedMaterials } from '../../api/material'

export default {
  name: 'StudentHome',
  setup() {
    const router = useRouter()

    const activeMenu = ref('home')
    const user = ref({})
    const codeInput = ref('')
    const banners = ref([])
    const newsList = ref([])
    const hotTopics = ref([])

    const recommendedMaterials = ref([])

    const newsWithCover = computed(() => (newsList.value || []).filter(n => n && n.coverImage))
    const newsNoCover = computed(() => (newsList.value || []).filter(n => n && !n.coverImage))

    const DEFAULT_NEWS_COVER = 'data:image/svg+xml;utf8,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360" viewBox="0 0 640 360"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#6a7cff"/><stop offset="1" stop-color="#7b4cc2"/></linearGradient></defs><rect width="640" height="360" fill="url(#g)"/><text x="50%" y="54%" dominant-baseline="middle" text-anchor="middle" font-family="Arial" font-size="32" fill="white" opacity="0.92">暂无封面</text></svg>')

    const getNewsCover = (news) => {
      if (news && news.coverImage) return news.coverImage
      return DEFAULT_NEWS_COVER
    }

    const handleNewsCoverError = (e) => {
      const img = e && e.target
      if (!img) return
      img.onerror = null
      img.src = DEFAULT_NEWS_COVER
    }

    const loadCarousels = async () => {
      try {
        const res = await getActiveCarousels('home')
        if (res.data.code === 200) {
          banners.value = (res.data.data || []).filter(b => b && b.title !== '思想页横幅')
        }
      } catch (e) {
        console.error('加载轮播图失败:', e)
      }
    }

    const loadNews = async () => {
      try {
        const res = await getLatestNews(10)
        if (res.data.code === 200) {
          newsList.value = res.data.data || []
        }
      } catch (e) {
        console.error('加载新闻失败:', e)
      }
    }

    const loadHotTopics = async () => {
      try {
        const res = await getHotTopics(10)
        if (res.data.code === 200) {
          hotTopics.value = res.data.data || []
        }
      } catch (e) {
        console.error('加载热门话题失败:', e)
      }
    }

    const normalizeText = (v) => String(v || '').trim().toLowerCase()

    const getMaterialText = (m) => {
      if (!m) return ''
      const title = normalizeText(m.title)
      const desc = normalizeText(m.description)
      const tags = Array.isArray(m.tags) ? m.tags.map(t => normalizeText(t && t.name)).join(' ') : ''
      return `${title} ${desc} ${tags}`.trim()
    }

    const isMajorMatch = (m, major) => {
      const mj = normalizeText(major)
      if (!mj) return false
      const text = getMaterialText(m)
      if (!text) return false
      if (text.includes(mj)) return true
      const tokens = mj.split(/[\s,，、/\\]+/).map(t => t.trim()).filter(t => t.length >= 2)
      return tokens.some(t => t && text.includes(t))
    }

    const scoreMaterial = (m, major) => {
      const mj = normalizeText(major)
      if (!mj) return 0
      const text = getMaterialText(m)
      if (!text) return 0

      let s = 0
      if (text.includes(mj)) s += 10

      const tokens = mj.split(/[\s,，、/\\]+/).map(t => t.trim()).filter(t => t.length >= 2)
      for (const t of tokens) {
        if (t && text.includes(t)) s += 4
      }
      return s
    }

    const loadRecommendedMaterials = async () => {
      try {
        const res = await getApprovedMaterials()
        if (res.data.code === 200) {
          const list = res.data.data || []
          const major = user.value && user.value.major ? user.value.major : ''

          const majorList = (list || []).filter(m => isMajorMatch(m, major))
          const otherList = (list || []).filter(m => !isMajorMatch(m, major))

          const majorScored = majorList.map(m => ({ m, s: scoreMaterial(m, major) }))
          majorScored.sort((a, b) => {
            if (b.s !== a.s) return b.s - a.s
            const bt = Date.parse(b.m && b.m.createdAt ? b.m.createdAt : 0) || 0
            const at = Date.parse(a.m && a.m.createdAt ? a.m.createdAt : 0) || 0
            return bt - at
          })

          const otherSorted = otherList.slice().sort((a, b) => {
            const bt = Date.parse(b && b.createdAt ? b.createdAt : 0) || 0
            const at = Date.parse(a && a.createdAt ? a.createdAt : 0) || 0
            return bt - at
          })

          const picked = majorScored.map(x => x.m).slice(0, 10)
          if (picked.length < 10) {
            const need = 10 - picked.length
            picked.push(...otherSorted.slice(0, need))
          }
          recommendedMaterials.value = picked
        }
      } catch (e) {
        console.error('加载推荐失败:', e)
      }
    }

    const viewMaterial = (m) => {
      if (!m || !m.id) return
      router.push(`/student/material/${m.id}`)
    }

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) {
        user.value = JSON.parse(userData)
      } else {
        router.push('/login')
      }

      loadCarousels()
      loadNews()
      loadHotTopics()
      loadRecommendedMaterials()
    })

    const viewHotTopic = (item) => {
      if (!item) return
      if (item.type === 'TOPIC' && item.code) {
        router.push(`/student/topic/${item.code}`)
      } else if (item.type === 'DISCUSSION') {
        if (item.code) router.push(`/student/discussion/${item.code}`)
      }
    }

    const handleMenuSelect = (index) => {
      activeMenu.value = index
      if (index === 'profile') {
        router.push('/student/profile')
      } else if (index === 'forum') {
        router.push('/student/forum')
      } else if (index === 'leader') {
        router.push('/student/leader')
      } else if (index === 'science') {
        router.push('/student/science')
      } else if (index === 'thought') {
        router.push('/student/thought')
      }
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const viewNews = (news) => {
      if (!news) return
      router.push(`/student/news/${news.id}`)
    }

    const handleBannerClick = (banner) => {
      if (banner && banner.linkUrl) {
        window.open(banner.linkUrl, '_blank')
      }
    }

    const formatTime = (time) => {
      if (!time) return ''
      return time.replace('T', ' ').substring(0, 10)
    }

    const handleCodeSubmit = async () => {
      if (!codeInput.value.trim()) return

      const code = codeInput.value.trim()
      if (code.startsWith('BD')) {
        try {
          const res = await bindStudent(user.value.id, code)
          if (res.data.code === 200) {
            user.value = res.data.data
            localStorage.setItem('user', JSON.stringify(user.value))
            ElMessage.success('绑定成功！')
            codeInput.value = ''
            loadRecommendedMaterials()
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

    const goNewsMore = () => {
      router.push('/student/news/list')
    }

    return {
      activeMenu,
      user,
      codeInput,
      banners,
      newsList,
      newsWithCover,
      newsNoCover,
      hotTopics,
      recommendedMaterials,
      handleMenuSelect,
      handleLogout,
      viewNews,
      viewHotTopic,
      viewMaterial,
      handleCodeSubmit,
      handleBannerClick,
      formatTime,
      getNewsCover,
      handleNewsCoverError,
      goNewsMore
    }
  }
}
</script>

<style scoped>
.home-container {
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
.code-input {
  margin-right: 20px;
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
  padding-top: 60px;
  max-width: 1200px;
  margin: 0 auto;
}
.carousel-section {
  margin: 20px 0;
}
.banner-item {
  height: 100%;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}
.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  padding: 30px;
  color: #fff;
}
.banner-overlay h2 {
  font-size: 28px;
  margin: 0;
}
.no-carousel {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}
.news-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #c41230;
  padding-bottom: 10px;
}
.section-header h3 {
  color: #333;
  margin: 0;
}
.news-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.news-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}
.news-item:hover {
  background: #f9f9f9;
}
.news-title-list {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.news-title-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.news-title-item:hover {
  background: #f7f7f7;
}
.news-title-item .news-title {
  flex: 1;
  padding-right: 12px;
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.news-title-item .news-date {
  flex-shrink: 0;
  font-size: 12px;
  color: #999;
}
.news-img {
  width: 200px;
  height: 120px;
  flex-shrink: 0;
}
.news-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}
.news-info {
  flex: 1;
}
.news-info h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}
.news-info p {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 10px 0;
}
.news-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}
.news-author {
  color: #666;
}
.news-date {
  color: #999;
}
.news-views {
  color: #999;
}
.no-news {
  padding: 40px;
  text-align: center;
  color: #999;
}
.hot-topics {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  padding-bottom: 28px;
  min-height: 360px;
}
.recommend-topics {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  padding-bottom: 28px;
  min-height: 360px;
  margin-top: 15px;
}
.no-recommend {
  padding: 10px 0;
  text-align: center;
  color: #999;
}
.material-detail .detail-row {
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}
.material-detail .label {
  color: #666;
}
.detail-content {
  margin-top: 12px;
}
.detail-img {
  width: 100%;
  border-radius: 6px;
}
.detail-video {
  width: 100%;
  border-radius: 6px;
}
.article-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #333;
}
.topic-header {
  font-weight: bold;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 2px solid #c41230;
  margin-bottom: 15px;
}
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.topic-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.topic-item:hover .topic-title {
  color: #c41230;
}
.topic-rank {
  width: 20px;
  height: 20px;
  background: #ddd;
  color: #666;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.topic-rank.top {
  background: #c41230;
  color: #fff;
}
.topic-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>