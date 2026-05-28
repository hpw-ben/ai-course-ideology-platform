<template>
  <div class="thought-list-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="thought" class="nav-menu">
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
      <div class="content-area">
        <div class="header-row">
          <el-button @click="goBack">返回</el-button>
          <div class="page-title">思想栏目 · {{ thoughtCategory }}</div>
          <div class="spacer"></div>
        </div>

        <div class="list-card">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else>
            <div v-if="pagedList.length === 0" class="empty">暂无内容</div>
            <div v-else class="list">
              <div v-for="n in pagedList" :key="n.id" class="row">
                <el-link type="primary" :underline="false" @click="openNews(n.id)">
                  {{ n.title }}
                </el-link>
                <div class="date">{{ formatDate(n.createdAt) }}</div>
              </div>
            </div>

            <div v-if="total > pageSize" class="pager">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                :current-page="page"
                @current-change="page = $event"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublishedNews } from '../../api/public'

export default {
  name: 'TeacherThoughtList',
  setup() {
    const router = useRouter()
    const route = useRoute()

    const user = ref({})
    const loading = ref(false)

    const thoughtCategory = computed(() => (route.query.category || '').toString() || '重要活动')

    const list = ref([])
    const page = ref(1)
    const pageSize = 10

    const total = computed(() => list.value.length)
    const pagedList = computed(() => {
      const start = (page.value - 1) * pageSize
      return list.value.slice(start, start + pageSize)
    })

    const loadList = async () => {
      loading.value = true
      try {
        const cat = thoughtCategory.value
        if (cat === '批示指示') {
          const res = await Promise.all([getPublishedNews('批示指示'), getPublishedNews('指示批示')])
          const a = res[0].data.code === 200 ? (res[0].data.data || []) : []
          const b = res[1].data.code === 200 ? (res[1].data.data || []) : []
          const map = new Map()
          ;[...a, ...b].forEach(x => map.set(x.id, x))
          list.value = Array.from(map.values())
        } else {
          const res = await getPublishedNews(cat)
          if (res.data.code === 200) list.value = res.data.data || []
          else list.value = []
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('获取列表失败')
        list.value = []
      } finally {
        loading.value = false
      }
    }

    const openNews = (id) => {
      if (!id) return
      router.push({ path: `/teacher/news/${id}`, query: { from: 'thought' } })
    }

    const goBack = () => {
      if (window.history.length > 1) router.back()
      else router.push('/teacher/thought')
    }

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const formatDate = (v) => {
      if (!v) return ''
      const d = new Date(v)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    }

    watch(thoughtCategory, () => {
      page.value = 1
      loadList()
    })

    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (userData) user.value = JSON.parse(userData)
      loadList()
    })

    return {
      user,
      loading,
      thoughtCategory,
      page,
      pageSize,
      total,
      pagedList,
      openNews,
      goBack,
      handleLogout,
      formatDate
    }
  }
}
</script>

<style scoped>
.thought-list-container { min-height: 100vh; background: #f5f5f5; }
.navbar { background: #c41230; position: fixed; top: 0; left: 0; right: 0; z-index: 100; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.nav-content { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; }
.logo { font-size: 20px; font-weight: bold; color: #fff; margin-right: 40px; }
.nav-menu { flex: 1; background: transparent; border: none; }
.nav-menu .el-menu-item { color: #fff; border-bottom: none; }
.nav-menu .el-menu-item:hover, .nav-menu .el-menu-item.is-active { background: rgba(255,255,255,0.2); color: #fff; }
.user-info { color: #fff; display: flex; align-items: center; gap: 10px; }
.user-info .el-button { color: #fff; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; }

.main-content { padding-top: 80px; }
.content-area { max-width: 1200px; margin: 0 auto; padding: 18px 20px; }

.header-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 700; color: #333; }
.spacer { flex: 1; }

.list-card { background: #fff; border-radius: 8px; padding: 14px 16px; min-width: 0; }
.list { display: flex; flex-direction: column; gap: 10px; }
.row { display: flex; align-items: center; gap: 12px; min-width: 0; }
.row :deep(.el-link) { display: block; flex: 1; min-width: 0; }
.row :deep(.el-link__inner) {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.date { flex: none; color: #999; font-size: 12px; }

.pager { margin-top: 14px; display: flex; justify-content: flex-end; }
.loading { color: #666; padding: 12px 0; }
.empty { color: #999; font-size: 13px; padding: 12px 0; }
</style>
