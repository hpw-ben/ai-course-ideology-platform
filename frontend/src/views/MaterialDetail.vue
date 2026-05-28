<template>
  <div class="material-detail-page">
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台</div>
        <el-button type="text" style="color: #fff" @click="goBack">返回</el-button>
      </div>
    </div>

    <div class="main-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!material" class="empty">素材不存在或已下架</div>

      <div v-else class="card">
        <div class="row"><span class="label">标题：</span>{{ material.title }}</div>
        <div class="row">
          <span class="label">类型：</span>
          {{ material.type === 'IMAGE' ? '图片' : material.type === 'VIDEO' ? '视频' : '文章' }}
        </div>
        <div class="row"><span class="label">介绍：</span>{{ material.description || '无' }}</div>

        <div v-if="material.type === 'IMAGE' && material.fileUrl" class="content">
          <img :src="material.fileUrl" class="img" />
        </div>
        <div v-else-if="material.type === 'VIDEO' && material.fileUrl" class="content">
          <video :src="material.fileUrl" controls class="video"></video>
        </div>
        <div v-else-if="material.type === 'ARTICLE'" class="content">
          <div class="article">{{ material.content || '' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMaterialById } from '../api/material'

export default {
  name: 'MaterialDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()

    const material = ref(null)
    const loading = ref(false)

    const goBack = () => {
      const p = String(route.path || '')
      if (p.startsWith('/teacher/')) {
        router.push('/teacher/home')
      } else if (p.startsWith('/student/')) {
        router.push('/student/home')
      } else {
        router.back()
      }
    }

    const load = async () => {
      const id = route.params.id
      if (!id) {
        material.value = null
        return
      }
      loading.value = true
      try {
        const res = await getMaterialById(id)
        if (res.data.code === 200) {
          material.value = res.data.data || null
        } else {
          material.value = null
          ElMessage.error(res.data.message || '加载失败')
        }
      } catch (e) {
        material.value = null
        ElMessage.error('加载失败')
      } finally {
        loading.value = false
      }
    }

    const resetScroll = async () => {
      await nextTick()
      try {
        const el = document.querySelector('.material-detail-page')
        if (el) el.scrollTop = 0
      } catch (e) {
      }
      try {
        window.scrollTo(0, 0)
      } catch (e) {
      }
    }

    onMounted(() => {
      try {
        document.body.classList.remove('el-popup-parent--hidden')
        document.body.style.overflow = 'auto'
        document.documentElement.style.overflow = 'auto'
      } catch (e) {
      }
      load()
      resetScroll()
    })

    watch(
      () => route.params.id,
      () => {
        load()
        resetScroll()
      }
    )

    return {
      material,
      loading,
      goBack
    }
  }
}
</script>

<style scoped>
.material-detail-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f5f5f5;
  overflow-y: auto;
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
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}
.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 80px 20px 20px;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}
.row {
  margin-bottom: 12px;
  color: #333;
}
.label {
  color: #666;
  display: inline-block;
  min-width: 72px;
}
.content {
  margin-top: 16px;
}
.img {
  width: 100%;
  border-radius: 8px;
  display: block;
}
.video {
  width: 100%;
  border-radius: 8px;
}
.article {
  color: #333;
  line-height: 1.9;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-word;
}
.loading, .empty {
  text-align: center;
  color: #909399;
  padding: 60px 0;
}
</style>
