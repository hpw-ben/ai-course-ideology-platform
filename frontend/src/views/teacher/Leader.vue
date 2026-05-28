<template>
  <div class="leader-container">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-menu mode="horizontal" default-active="leader" class="nav-menu">
          <el-menu-item index="home" @click="$router.push('/teacher/home')">首页</el-menu-item>
          <el-menu-item index="thought" @click="$router.push('/teacher/thought')">思想</el-menu-item>
          <el-menu-item index="science" @click="$router.push('/teacher/science')">学习科学</el-menu-item>
          <el-menu-item index="leader">新时代领头人</el-menu-item>
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
        <h1>新时代领头人</h1>
        <p>理工科人物事迹 · 可按标签/关键词筛选</p>
      </div>

      <div class="filters">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索人物/事迹标题/关键词"
          class="search-input"
        />
        <div v-if="tagsExpanded" class="tag-categories">
          <el-radio-group v-model="activeCategory" size="small">
            <el-radio-button v-for="c in tagCategories" :key="c" :label="c">{{ c }}</el-radio-button>
          </el-radio-group>
        </div>
        <div class="tag-bar">
          <el-tag
            v-for="t in displayTagOptions"
            :key="t"
            :type="selectedTags.includes(t) ? 'danger' : 'info'"
            class="tag"
            effect="plain"
            @click="toggleTag(t)"
          >
            {{ t }}
          </el-tag>
          <el-button v-if="tagOptions.length > 10" type="text" @click="toggleTagsExpanded">{{ tagsExpanded ? '收起' : '展开' }}</el-button>
          <el-button v-if="selectedTags.length" type="text" @click="clearTags">清空标签</el-button>
        </div>
      </div>

      <div class="list">
        <el-card v-for="item in filteredList" :key="item.id" class="card" shadow="hover">
          <div class="card-body">
            <div v-if="item.fileUrl" class="cover-wrap">
              <img :src="item.fileUrl" class="cover-img" />
            </div>
            <div class="info">
              <div class="card-header">
                <div class="title">{{ item.title }}</div>
                <el-button type="primary" link @click="openDetail(item)">查看详情</el-button>
              </div>
              <div class="meta">
                <el-tag v-for="t in (item.tags || [])" :key="t.id" size="small" class="tag-mini">{{ t.name }}</el-tag>
              </div>
              <div class="time">发布时间：{{ formatTime(item.createdAt || item.created_at) }}</div>
              <div class="summary">{{ item.description || '' }}</div>
            </div>
          </div>
        </el-card>

        <el-empty v-if="filteredList.length === 0" description="暂无匹配的人物事迹" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLeaderTags, getLeaderList } from '../../api/public'
import { getTaskByCode } from '../../api/task'
import { getDiscussionByCode } from '../../api/discussion'

export default {
  name: 'TeacherLeader',
  setup() {
    const router = useRouter()
    const user = ref({})
    const codeInput = ref('')

    const keyword = ref('')
    const selectedTags = ref([])
    const tagsExpanded = ref(false)
    const activeCategory = ref('全部')
    const tagCategories = ['全部', '理学', '工学', '农学', '医学', '其他']

    const materials = ref([])
    const tagsFromServer = ref([])

    const tagOptions = computed(() => {
      return (tagsFromServer.value || []).map(t => t.name)
    })

    const getTagCategory = (name) => {
      const n = (name || '').trim()
      if (!n) return '其他'

      const includesAny = (text, keywords) => keywords.some(k => text.includes(k))

      const medKeys = ['医学', '临床', '护理', '药学', '公共卫生', '预防医学', '卫生', '流行病', '检验']
      const agriKeys = ['农学', '农业', '林学', '水产', '畜', '兽', '园艺', '作物', '植保']
      const engKeys = [
        '工学', '工程', '计算机', '软件', '网络', '信息安全', '人工智能', '大数据', '物联网',
        '电子', '通信', '机械', '材料', '土木', '建筑', '化工', '航天', '航空', '电气',
        '自动化', '控制', '测控', '仪器', '交通', '能源', '环境工程', '海洋科学',
        '地质', '潜艇', '力学', '测绘'
      ]
      const sciKeys = ['理学', '数学', '统计', '物理', '化学', '生物', '天文', '地理', '地球', '海洋']

      if (includesAny(n, medKeys)) return '医学'
      if (includesAny(n, agriKeys)) return '农学'
      if (includesAny(n, engKeys)) return '工学'
      if (includesAny(n, sciKeys)) return '理学'
      return '其他'
    }

    const displayTagOptions = computed(() => {
      const all = tagOptions.value || []
      if (!tagsExpanded.value) return all.slice(0, 10)
      if (activeCategory.value === '全部') return all
      return all.filter(t => getTagCategory(t) === activeCategory.value)
    })

    const filteredList = computed(() => {
      const kw = keyword.value.trim().toLowerCase()
      const tags = selectedTags.value

      return materials.value.filter(s => {
        const tagNames = (s.tags || []).map(t => t.name).join(' ')
        const text = `${s.title || ''} ${s.description || ''} ${s.content || ''} ${tagNames}`.toLowerCase()
        const hitKw = !kw || text.includes(kw)
        const hitTag = tags.length === 0 || tags.every(t => (s.tags || []).some(x => x.name === t))
        return hitKw && hitTag
      })
    })

    const toggleTag = (t) => {
      const idx = selectedTags.value.indexOf(t)
      if (idx >= 0) selectedTags.value.splice(idx, 1)
      else selectedTags.value.push(t)
    }

    const clearTags = () => {
      selectedTags.value = []
    }

    const toggleTagsExpanded = () => {
      tagsExpanded.value = !tagsExpanded.value
      if (!tagsExpanded.value) {
        activeCategory.value = '全部'
      }
    }

    const openDetail = (item) => {
      router.push(`/teacher/leader/${item.id}`)
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

    const loadTags = async () => {
      try {
        const res = await getLeaderTags()
        if (res.data.code === 200) {
          tagsFromServer.value = res.data.data || []
        }
      } catch (e) {
        console.error('加载标签失败:', e)
      }
    }

    const loadList = async () => {
      try {
        const res = await getLeaderList()
        if (res.data.code === 200) {
          materials.value = res.data.data || []
        }
      } catch (e) {
        console.error('加载人物事迹失败:', e)
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
      loadTags()
      loadList()
    })

    return {
      user,
      codeInput,
      handleCodeSubmit,
      keyword,
      selectedTags,
      tagsExpanded,
      activeCategory,
      tagCategories,
      tagOptions,
      displayTagOptions,
      filteredList,
      toggleTag,
      toggleTagsExpanded,
      clearTags,
      openDetail,
      formatTime,
      handleLogout
    }
  }
}
</script>

<style scoped>
.leader-container { min-height: 100vh; background: #f5f5f5; }
.navbar { background: #c41230; position: fixed; top: 0; left: 0; right: 0; z-index: 100; box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.nav-content { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; }
.logo { font-size: 20px; font-weight: bold; color: #fff; margin-right: 40px; }
.nav-menu { flex: 1; background: transparent; border: none; }
.nav-menu .el-menu-item { color: #fff; border-bottom: none; }
.nav-menu .el-menu-item:hover, .nav-menu .el-menu-item.is-active { background: rgba(255,255,255,0.2); color: #fff; }
 .code-input { margin-right: 20px; }
.user-info { color: #fff; display: flex; align-items: center; gap: 10px; }
.user-info .el-button { color: #fff; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; margin-right: 8px; }

.main-content { padding-top: 80px; max-width: 1200px; margin: 0 auto; padding-left: 20px; padding-right: 20px; }
.page-header { background: #fff; border-radius: 8px; padding: 18px 18px; margin-bottom: 16px; }
.page-header h1 { margin: 0 0 6px 0; color: #333; font-size: 22px; }
.page-header p { margin: 0; color: #666; }

.filters { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
.search-input { width: 100%; }
.tag-categories { margin-top: 12px; }
.tag-bar { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.tag { cursor: pointer; }

.list { display: flex; flex-direction: column; gap: 14px; }
.card { border-radius: 10px; }
.card-body { display: flex; gap: 14px; }
.cover-wrap { flex: 0 0 240px; width: 240px; height: 140px; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
.cover-img { width: 100%; height: 100%; object-fit: cover; object-position: center 20%; display: block; background: #f5f5f5; }
.cover-empty { width: 100%; height: 100%; background: #f5f5f5; }
.info { flex: 1; min-width: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.title { font-size: 16px; font-weight: 600; color: #333; }
.meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.tag-mini { margin-right: 0; }
.time { color: #999; font-size: 13px; margin-bottom: 8px; }
.summary { color: #666; line-height: 1.8; overflow-wrap: anywhere; word-break: break-word; }
</style>
