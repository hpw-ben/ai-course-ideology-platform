<template>
  <div class="thought-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="thought" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/teacher/home')">首页</el-menu-item>
          <el-menu-item index="thought">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/teacher/science')">学习科学</el-menu-item>
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
      <div class="thought-hero">
        <div class="hero-bg" :style="bannerUrl ? { backgroundImage: `url(${bannerUrl})` } : {}">
          <div class="hero-mask"></div>
        </div>
        <div class="hero-nav">
          <div class="hero-nav-inner">
            <div v-for="c in topCategories" :key="c" class="hero-nav-item" @click="goTopCategory(c)">
              {{ c }}
            </div>
          </div>
        </div>
      </div>

      <div class="content-area">
        <div class="sections">
          <div v-for="(sec, i) in sections" :key="sec" class="section">
            <div class="section-header">
              <div class="section-title">{{ sec }}</div>
              <div
                v-if="getSectionNews(i).length > 4"
                class="section-more section-more-click"
                @click="goSection(sec)"
              >
                更多
              </div>
            </div>
            <div class="section-list">
              <div v-for="n in getDisplayNews(i)" :key="n.id" class="list-item">
                <el-link type="primary" :underline="false" @click="openNews(n.id)">
                  {{ n.title }}
                </el-link>
              </div>
              <div v-if="getSectionNews(i).length === 0" class="empty">暂无内容</div>
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
import { getActiveCarousels, getPublishedNews } from '../../api/public'
import { getTaskByCode } from '../../api/task'
import { getDiscussionByCode } from '../../api/discussion'

export default {
  name: 'TeacherThought',
  setup() {
    const router = useRouter()
    const user = ref({})
    const codeInput = ref('')

    const topCategories = [
      '十个明确',
      '十四个坚持',
      '十三个方面成就',
      '六个必须坚持'
    ]

    const bannerUrl = ref('')

    const sections = [
      '重要活动',
      '重要会议',
      '重要讲话',
      '重要文章',
      '出国访问',
      '批示指示'
    ]

    const sectionNewsMap = ref({})

    const loadNews = async () => {
      try {
        const results = await Promise.all(
          sections.map(async (sec) => {
            if (sec === '批示指示') {
              const res = await Promise.all([getPublishedNews('批示指示'), getPublishedNews('指示批示')])
              const a = res[0].data.code === 200 ? (res[0].data.data || []) : []
              const b = res[1].data.code === 200 ? (res[1].data.data || []) : []
              const map = new Map()
              ;[...a, ...b].forEach(x => map.set(x.id, x))
              return [sec, Array.from(map.values())]
            }
            const res = await getPublishedNews(sec)
            const list = res.data.code === 200 ? (res.data.data || []) : []
            return [sec, list]
          })
        )
        const next = {}
        results.forEach(([k, v]) => { next[k] = v })
        sectionNewsMap.value = next
      } catch (e) {
        console.error(e)
      }
    }

    const loadBanner = async () => {
      try {
        const res = await getActiveCarousels('thought')
        if (res.data.code === 200) {
          const items = res.data.data || []
          const found = items.find(x => x && x.imageUrl)
          bannerUrl.value = found ? found.imageUrl : ''
        }
      } catch (e) {
        console.error(e)
      }

      if (bannerUrl.value) return

      try {
        const res2 = await getActiveCarousels()
        if (res2.data.code === 200) {
          const items2 = res2.data.data || []
          const legacy = items2.find(x => x && x.title === '思想页横幅' && x.imageUrl)
          bannerUrl.value = legacy ? legacy.imageUrl : ''
        }
      } catch (e2) {
        console.error(e2)
      }
    }

    const getSectionNews = (sectionIndex) => {
      const sec = sections[sectionIndex]
      if (!sec) return []
      const list = sectionNewsMap.value[sec] || []
      return list.slice(0, 9)
    }

    const getDisplayNews = (sectionIndex) => {
      const list = getSectionNews(sectionIndex)
      return list.slice(0, 4)
    }

    const openNews = (id) => {
      if (!id) return
      router.push({ path: `/teacher/news/${id}`, query: { from: 'thought' } })
    }

    const goSection = (sec) => {
      router.push({ path: '/teacher/thought/list', query: { category: sec } })
    }

    const goTopCategory = async (sec) => {
      if (!sec) return
      try {
        const res = await getPublishedNews(sec)
        const list = res.data.code === 200 ? (res.data.data || []) : []
        if (!list.length) {
          ElMessage.warning('暂无内容')
          return
        }

        const sorted = [...list].sort((a, b) => {
          const ta = new Date(a && a.createdAt ? a.createdAt : 0).getTime()
          const tb = new Date(b && b.createdAt ? b.createdAt : 0).getTime()
          return tb - ta
        })

        const id = sorted[0] && sorted[0].id
        if (!id) {
          ElMessage.warning('暂无内容')
          return
        }

        router.push({ path: `/teacher/news/${id}`, query: { from: 'thought' } })
      } catch (e) {
        console.error(e)
        ElMessage.error('加载失败')
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

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) user.value = JSON.parse(userData)
      loadBanner()
      loadNews()
    })

    return {
      user,
      codeInput,
      handleCodeSubmit,
      topCategories,
      goTopCategory,
      bannerUrl,
      sections,
      getSectionNews,
      getDisplayNews,
      goSection,
      openNews,
      handleLogout
    }
  }
}
</script>

<style scoped>
.thought-container { min-height: 100vh; background: #f5f5f5; }
.navbar { background: #c41230; position: fixed; top: 0; left: 0; right: 0; z-index: 100; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.nav-content { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; }
.logo { font-size: 20px; font-weight: bold; color: #fff; margin-right: 40px; }
.nav-menu { flex: 1; background: transparent; border: none; }
.nav-menu .el-menu-item { color: #fff; border-bottom: none; }
.nav-menu .el-menu-item:hover, .nav-menu .el-menu-item.is-active { background: rgba(255,255,255,0.2); color: #fff; }
 .code-input { margin-right: 20px; }
.user-info { color: #fff; display: flex; align-items: center; gap: 10px; }
.user-info .el-button { color: #fff; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; }

.main-content { padding-top: 80px; }

.thought-hero { background: #fff; }
.hero-bg {
  position: relative;
  height: 240px;
  background-color: #b10e1b;
  background-size: cover;
  background-position: center;
}
.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.15) 0%, rgba(0,0,0,0.25) 100%);
}
.hero-nav { background: #8f0c13; }
.hero-nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.hero-nav-item {
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f7e7b2;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  user-select: none;
}
.hero-nav-item:hover { background: rgba(255,255,255,0.08); }

.content-area { max-width: 1400px; margin: 0 auto; padding: 14px 10px 12px; }

.sections { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 22px; }
.section { background: #fff; border-radius: 12px; padding: 18px 20px; min-width: 0; box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.section-title {
  font-size: 20px;
  font-weight: 800;
  color: #c41230;
  padding-left: 10px;
  border-left: 4px solid #c41230;
}
.section-more { color: #999; font-size: 13px; }
.section-more-click { cursor: pointer; user-select: none; }
.section-more-click:hover { color: #666; }
.section-list { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.list-item { width: 100%; min-width: 0; }
.list-item :deep(.el-link) { display: block; width: 100%; }
.list-item :deep(.el-link__inner) {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.8;
}
.empty { color: #999; font-size: 13px; padding: 8px 0; }

@media (max-width: 1000px) {
  .sections { grid-template-columns: 1fr; }
  .hero-bg { height: 180px; }
  .hero-nav-inner { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
