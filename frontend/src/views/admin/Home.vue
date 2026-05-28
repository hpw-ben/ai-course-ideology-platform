<template>
  <div class="admin-container">
    <!-- 顶部导航 -->
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">思政学习平台 - 管理后台</div>
        <div class="user-info">
          <span>管理员 {{ user.realName }}</span>
          <el-button type="text" style="color: #fff" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧菜单 -->
        <el-col :span="4">
          <div class="sidebar">
            <el-menu :default-active="activeTab" @select="handleTabSelect">
              <el-menu-item index="carousel">
                <el-icon><Picture /></el-icon>
                <span>轮播图管理</span>
              </el-menu-item>
              <el-menu-item index="analysis">
                <el-icon><DataAnalysis /></el-icon>
                <span>数据管理</span>
              </el-menu-item>
              <el-menu-item index="news">
                <el-icon><Document /></el-icon>
                <span>新闻资讯</span>
              </el-menu-item>
              <el-menu-item index="thought">
                <el-icon><Document /></el-icon>
                <span>思想栏目</span>
              </el-menu-item>
              <el-menu-item index="science">
                <el-icon><Document /></el-icon>
                <span>科学栏目</span>
              </el-menu-item>
              <el-menu-item index="material">
                <el-icon><Files /></el-icon>
                <span>素材审核</span>
              </el-menu-item>
              <el-menu-item index="topic">
                <el-icon><ChatDotRound /></el-icon>
                <span>时事论坛</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-col>

        <!-- 右侧内容 -->
        <el-col :span="20">
          <div v-if="activeTab === 'analysis'" class="content-card">
            <div class="card-header">
              <h2>平台数据管理</h2>
              <el-button type="primary" :loading="platformStatsLoading" @click="loadPlatformStats">刷新</el-button>
            </div>

            <el-row :gutter="20" class="platform-stats-grid">
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.userCount) }}</div>
                  <div class="platform-stat-label">注册用户数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.studentCount) }}</div>
                  <div class="platform-stat-label">学生数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.teacherCount) }}</div>
                  <div class="platform-stat-label">教师数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.materialCount) }}</div>
                  <div class="platform-stat-label">素材数量</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="platform-stats-grid">
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.taskCount) }}</div>
                  <div class="platform-stat-label">任务数量</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.discussionCount) }}</div>
                  <div class="platform-stat-label">讨论数量</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.newsCount) }}</div>
                  <div class="platform-stat-label">新闻数量</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.forumTopicCount) }}</div>
                  <div class="platform-stat-label">论坛话题数量</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="platform-stats-grid">
              <el-col :span="8">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.newsViewCount) }}</div>
                  <div class="platform-stat-label">新闻总浏览量</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.discussionViewCount) }}</div>
                  <div class="platform-stat-label">讨论总浏览量</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.forumTopicViewCount) }}</div>
                  <div class="platform-stat-label">论坛总浏览量</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="platform-stats-grid">
              <el-col :span="8">
                <div class="platform-stat-card">
                  <div class="platform-stat-value">{{ formatStat(platformStats.materialViewCount) }}</div>
                  <div class="platform-stat-label">素材总浏览量</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 轮播图管理 -->
          <div v-if="activeTab === 'carousel'" class="content-card">
            <div class="card-header">
              <h2>轮播图管理</h2>
              <el-button type="primary" @click="showCarouselDialog()">添加轮播图</el-button>
            </div>
            <el-table :data="carousels" stripe>
              <el-table-column label="图片" width="150">
                <template #default="{ row }">
                  <img :src="row.imageUrl" class="carousel-thumb" />
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" />
              <el-table-column label="显示页面" width="120">
                <template #default="{ row }">{{ formatCarouselPage(row) }}</template>
              </el-table-column>
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showCarouselDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeCarousel(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 思想栏目管理 -->
          <div v-if="activeTab === 'thought'" class="content-card">
            <div class="card-header">
              <h2>思想栏目管理</h2>
              <div style="display: flex; gap: 10px;">
                <el-button @click="showThoughtBannerDialog">设置思想页横幅</el-button>
                <el-button type="primary" @click="showNewsDialog(null, thoughtCategory)">发布思想文章</el-button>
              </div>
            </div>

            <div style="margin-bottom: 14px; display: flex; align-items: center; gap: 12px;">
              <span style="color: #666;">分类：</span>
              <el-radio-group v-model="thoughtCategory" @change="loadThoughtNews">
                <el-radio-button v-for="c in thoughtCategories" :key="c" :label="c">{{ c }}</el-radio-button>
              </el-radio-group>
            </div>

            <el-table :data="thoughtNewsList" stripe>
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="author" label="作者" width="100" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '已发布' : '草稿' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showNewsDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeNews(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 新闻资讯管理 -->
          <div v-if="activeTab === 'news'" class="content-card">
            <div class="card-header">
              <h2>新闻资讯管理</h2>
              <el-button type="primary" @click="showNewsDialog()">发布新闻</el-button>
            </div>
            <el-table :data="newsList" stripe>
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="author" label="作者" width="100" />
              <el-table-column prop="viewCount" label="浏览量" width="80" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '已发布' : '草稿' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showNewsDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeNews(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 科学栏目管理 -->
          <div v-if="activeTab === 'science'" class="content-card">
            <div class="card-header">
              <h2>科学栏目管理</h2>
              <el-button type="primary" @click="showScienceDialog()">添加内容</el-button>
            </div>

            <div style="margin-bottom: 14px; display: flex; align-items: center; gap: 12px;">
              <span style="color: #666;">模块：</span>
              <el-radio-group v-model="scienceModule" @change="loadScienceItems">
                <el-radio-button label="empower">科技赋能</el-radio-button>
                <el-radio-button label="news">科技新闻</el-radio-button>
                <el-radio-button label="knowledge">科普知识</el-radio-button>
              </el-radio-group>
            </div>

            <el-table :data="scienceItems" stripe>
              <el-table-column label="封面" width="150">
                <template #default="{ row }">
                  <video v-if="row.coverImage" :src="row.coverImage" class="science-thumb" muted controls />
                  <div v-else class="science-thumb placeholder"></div>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showScienceDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeScienceItem(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 素材审核 -->
          <div v-if="activeTab === 'material'" class="content-card">
            <div class="card-header">
              <h2>素材审核</h2>
              <div style="display: flex; align-items: center; gap: 12px">
                <el-button type="primary" @click="showLeaderMaterialDialog()">发布人物事迹</el-button>
              <el-radio-group v-model="materialFilter" @change="loadMaterials">
                <el-radio-button label="PENDING">待审核</el-radio-button>
                <el-radio-button label="ALL">全部</el-radio-button>
              </el-radio-group>
              </div>
            </div>
            <el-table :data="materials" stripe>
              <el-table-column prop="title" label="标题" />
              <el-table-column label="类型" width="80">
                <template #default="{ row }">
                  <el-tag>{{ row.type === 'IMAGE' ? '图片' : row.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
                    {{ row.status === 'APPROVED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="上架" width="90">
                <template #default="{ row }">
                  <el-switch
                    :model-value="row.shelfStatus === 'ON'"
                    :disabled="row.status !== 'APPROVED'"
                    @change="(val) => onMaterialShelfChange(row, val)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="分类" width="160">
                <template #default="{ row }">
                  <el-select
                    v-model="row.category"
                    placeholder="选择分类"
                    size="small"
                    clearable
                    :disabled="!row.id"
                    style="width: 140px"
                    @change="() => onMaterialCategoryChange(row)"
                  >
                    <el-option v-for="c in materialCategories" :key="c" :label="c" :value="c" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="上传时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showMaterialDetail(row)">详情</el-button>
                  <template v-if="row.status === 'PENDING'">
                    <el-button type="success" link @click="handleApprove(row)">通过</el-button>
                    <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
                  </template>
                  <template v-else-if="row.status === 'APPROVED'">
                    <el-button type="warning" link @click="handleRevoke(row)">撤回</el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 时事论坛话题管理 -->
          <div v-if="activeTab === 'topic'" class="content-card">
            <div class="card-header">
              <h2>时事论坛话题管理</h2>
              <el-button type="primary" @click="showTopicDialog()">发布话题</el-button>
            </div>
            <el-table :data="topics" stripe>
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="code" label="话题码" width="140" />
              <el-table-column prop="commentCount" label="评论数" width="80" />
              <el-table-column prop="viewCount" label="浏览量" width="80" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '进行中' : '已关闭' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showTopicDialog(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeTopic(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 轮播图编辑弹窗 -->
    <el-dialog v-model="carouselDialogVisible" :title="carouselForm.id ? '编辑轮播图' : '添加轮播图'" width="600px">
      <el-form :model="carouselForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="carouselForm.title" placeholder="轮播图标题（可选）" />
        </el-form-item>
        <el-form-item label="显示页面">
          <el-select v-model="carouselForm.page" placeholder="请选择显示页面" style="width: 100%">
            <el-option label="首页" value="home" />
            <el-option label="思想页" value="thought" />
            <el-option label="学习科学" value="science" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload class="carousel-uploader" action="#" :show-file-list="false" :auto-upload="false" :on-change="handleCarouselImageChange" accept="image/*">
            <img v-if="carouselForm.imageUrl" :src="carouselForm.imageUrl" class="carousel-preview" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
<!--        <el-form-item label="跳转链接">-->
<!--          <el-input v-model="carouselForm.linkUrl" placeholder="点击跳转的链接（可选）" />-->
<!--        </el-form-item>-->
        <el-form-item label="排序">
          <el-input-number v-model="carouselForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="carouselForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="carouselDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveCarousel">保存</el-button>
      </template>
    </el-dialog>

    <!-- 科学栏目编辑弹窗 -->
    <el-dialog v-model="scienceDialogVisible" :title="scienceForm.id ? '编辑内容' : '添加内容'" width="650px">
      <el-form :model="scienceForm" label-width="90px">
        <el-form-item label="所属模块" required>
          <el-select v-model="scienceForm.module" placeholder="请选择模块" style="width: 100%">
            <el-option label="科技赋能" value="empower" />
            <el-option label="科技新闻" value="news" />
            <el-option label="科普知识" value="knowledge" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="scienceForm.module === 'news' || scienceForm.module === 'knowledge'" label="内容形式" required>
          <el-radio-group v-model="scienceForm.contentType">
            <el-radio label="VIDEO">视频</el-radio>
            <el-radio label="ARTICLE">文章</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="scienceForm.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="scienceForm.description" type="textarea" :rows="3" placeholder="简介（可选）" />
        </el-form-item>
        <el-form-item v-if="scienceForm.module === 'empower' || scienceForm.contentType === 'VIDEO'" label="视频">
              <el-upload
                class="carousel-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleScienceImageChange"
                :accept="'.mp4,video/mp4'"
              >
                <video v-if="scienceForm.coverImage" :src="scienceForm.coverImage" class="carousel-preview" muted controls />
                <div v-else class="upload-placeholder">
                  <el-icon><Plus /></el-icon>
              <span>上传视频</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="(scienceForm.module === 'news' || scienceForm.module === 'knowledge') && scienceForm.contentType === 'ARTICLE'" label="正文" required>
          <el-input v-model="scienceForm.articleContent" type="textarea" :rows="10" placeholder="请输入文章正文" />
        </el-form-item>
        <el-form-item v-if="(scienceForm.module === 'knowledge' || scienceForm.module === 'news') && scienceForm.contentType === 'ARTICLE'" label="插入图片">
          <el-upload
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :on-change="handleScienceArticleImageChange"
          >
            <el-button type="primary" plain>上传图片并插入</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="scienceForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scienceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveScienceItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新闻编辑弹窗 -->
    <el-dialog v-model="newsDialogVisible" :title="newsForm.id ? '编辑新闻' : '发布新闻'" width="800px">
      <el-form :model="newsForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="newsForm.title" placeholder="新闻标题" />
        </el-form-item>
        <el-form-item label="分类" v-if="activeTab === 'thought'" required>
          <el-select v-model="newsForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in thoughtCategories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="newsForm.summary" type="textarea" :rows="2" placeholder="新闻摘要（可选）" />
        </el-form-item>
        <el-form-item label="封面">
          <div style="display:flex;flex-direction:column;gap:10px;width:100%">
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :accept="'image/*'"
              :on-change="handleNewsCoverChange"
            >
              <el-button type="primary" plain>上传封面图片</el-button>
            </el-upload>
            <img v-if="newsForm.coverImage" :src="newsForm.coverImage" style="max-width:100%;max-height:180px;border-radius:6px;object-fit:cover" />
          </div>
        </el-form-item>
        <el-form-item label="关联素材">
          <el-select v-model="newsForm.materialIds" multiple placeholder="选择关联素材" style="width: 100%">
            <el-option v-for="m in approvedMaterials" :key="m.id" :label="m.title" :value="m.id">
              <span>{{ m.title }}</span>
              <el-tag size="small" style="margin-left: 10px">{{ m.type === 'IMAGE' ? '图片' : m.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <div style="display:flex;flex-direction:column;gap:10px;width:100%">
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :accept="'video/*'"
              :on-change="handleNewsVideoInsertChange"
            >
              <el-button type="primary" plain>上传视频并插入</el-button>
            </el-upload>
            <el-input v-model="newsForm.content" type="textarea" :rows="10" placeholder="新闻内容" />
          </div>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="newsForm.author" placeholder="作者（可选）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="newsForm.status" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="newsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveNews">保存</el-button>
      </template>
    </el-dialog>

    <!-- 话题编辑弹窗 -->
    <el-dialog v-model="topicDialogVisible" :title="topicForm.id ? '编辑话题' : '发布话题'" width="800px">
      <el-form :model="topicForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="topicForm.title" placeholder="话题标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="topicForm.description" type="textarea" :rows="4" placeholder="话题描述" />
        </el-form-item>
        <el-form-item label="关联素材">
          <el-select v-model="topicForm.materialIds" multiple placeholder="选择关联素材" style="width: 100%">
            <el-option v-for="m in approvedMaterials" :key="m.id" :label="m.title" :value="m.id">
              <span>{{ m.title }}</span>
              <el-tag size="small" style="margin-left: 10px">{{ m.type === 'IMAGE' ? '图片' : m.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联新闻">
          <el-select v-model="topicForm.newsIds" multiple placeholder="选择关联新闻" style="width: 100%">
            <el-option v-for="n in newsOptions" :key="n.id" :label="n.title" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="topicForm.id">
          <el-radio-group v-model="topicForm.status">
            <el-radio label="ACTIVE">进行中</el-radio>
            <el-radio label="CLOSED">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveTopic">保存</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="saveLoading" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 素材详情弹窗 -->
    <el-dialog v-model="materialDetailDialogVisible" title="素材详情" width="800px">
      <div v-if="materialDetail" class="material-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ materialDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag>{{ materialDetail.type === 'IMAGE' ? '图片' : materialDetail.type === 'VIDEO' ? '视频' : '文章' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="materialDetail.status === 'APPROVED' ? 'success' : materialDetail.status === 'REJECTED' ? 'danger' : 'warning'">
              {{ materialDetail.status === 'APPROVED' ? '已通过' : materialDetail.status === 'REJECTED' ? '已拒绝' : '待审核' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatTime(materialDetail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ materialDetail.description || '无' }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <el-tag v-for="tag in materialDetail.tags" :key="tag.id" style="margin-right: 5px">{{ tag.name }}</el-tag>
            <span v-if="!materialDetail.tags || materialDetail.tags.length === 0">无</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 素材内容展示 -->
        <div class="material-content" style="margin-top: 20px">
          <h4>素材内容</h4>
          <div v-if="materialDetail.type === 'IMAGE'" class="image-preview">
            <img :src="materialDetail.fileUrl" alt="素材图片" style="max-width: 100%; border-radius: 4px" />
          </div>
          <div v-else-if="materialDetail.type === 'VIDEO'" class="video-preview">
            <video :src="materialDetail.fileUrl" controls style="max-width: 100%; border-radius: 4px"></video>
          </div>
          <div v-else-if="materialDetail.type === 'ARTICLE'" class="article-content">
            <div style="padding: 15px; background: #f5f5f5; border-radius: 4px; white-space: pre-wrap">{{ materialDetail.content }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>
            <template v-if="materialDetail && materialDetail.status === 'PENDING'">
              <el-button type="success" @click="approveInDetail">通过</el-button>
              <el-button type="danger" @click="rejectInDetail">拒绝</el-button>
            </template>
            <template v-else-if="materialDetail && materialDetail.status === 'APPROVED'">
              <el-button type="warning" @click="revokeInDetail">撤回</el-button>
            </template>
          </div>
          <el-button @click="materialDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 撤回原因弹窗 -->
    <el-dialog v-model="revokeDialogVisible" title="撤回原因" width="400px">
      <el-input v-model="revokeReason" type="textarea" :rows="3" placeholder="请输入撤回原因" />
      <template #footer>
        <el-button @click="revokeDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="saveLoading" @click="confirmRevoke">确认撤回</el-button>
      </template>
    </el-dialog>

    <!-- 发布人物事迹弹窗 -->
    <el-dialog v-model="leaderMaterialDialogVisible" title="发布人物事迹" width="820px">
      <el-form :model="leaderMaterialForm" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="leaderMaterialForm.title" placeholder="人物/事迹标题" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="leaderMaterialForm.description" type="textarea" :rows="2" placeholder="简要介绍（可选）" />
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="carousel-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleLeaderCoverChange"
            accept="image/*"
          >
            <img v-if="leaderMaterialForm.fileUrl" :src="leaderMaterialForm.fileUrl" class="carousel-preview" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标签">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px">
            <el-select v-model="tagFilterCategory" placeholder="大类" style="width: 140px">
              <el-option v-for="c in tagCategories" :key="c" :label="c" :value="c" />
            </el-select>
            <el-button @click="openTagManage">管理标签大类</el-button>
          </div>
          <el-select v-model="leaderMaterialForm.tagIds" multiple filterable placeholder="选择标签" style="width: 100%">
            <el-option v-for="t in leaderTagOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <div style="display: flex; align-items: center; gap: 10px; margin-top: 10px">
            <el-input v-model="newTagName" placeholder="新增标签" style="width: 240px" />
            <el-select v-model="newTagCategory" placeholder="大类" style="width: 140px">
              <el-option v-for="c in baseCategories" :key="c" :label="c" :value="c" />
            </el-select>
            <el-button @click="addNewTag">添加标签</el-button>
          </div>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="leaderMaterialForm.content" type="textarea" :rows="10" placeholder="人物事迹正文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="leaderMaterialDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveLeaderMaterial">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagManageDialogVisible" title="标签大类管理" width="620px">
      <el-table :data="allTags" stripe>
        <el-table-column prop="name" label="标签" />
        <el-table-column label="大类" width="220">
          <template #default="{ row }">
            <el-select v-model="row.category" placeholder="选择大类" style="width: 180px" @change="() => onTagCategoryChange(row)">
              <el-option v-for="c in baseCategories" :key="c" :label="c" :value="c" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="tagManageDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  // ...
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Document, Files, ChatDotRound, Plus, DataAnalysis } from '@element-plus/icons-vue'
import {
  getAllCarousels, createCarousel, updateCarousel, deleteCarousel,
  getAllScienceItems, createScienceItem, updateScienceItem, deleteScienceItem,
  getAllNews, getAllNewsByCategory, getNewsById, createNews, updateNews, deleteNews, getPublishedNews,
  getPendingMaterials, getAllMaterialsForAdmin, approveMaterial, rejectMaterial, revokeMaterial, getMaterialDetail, setMaterialShelfStatus, updateMaterialCategory,
  getAllTopics, getTopicById, createTopic, updateTopic, deleteTopic,
  createLeaderMaterial, getAllTags, createTag, updateTagCategory, getPlatformStats
} from '../../api/admin'
import { extractUploadRawFile, uploadResourceFile } from '../../utils/upload'

export default {
  name: 'AdminHome',
  components: { Picture, Document, Files, ChatDotRound, Plus, DataAnalysis },
  setup() {
    const router = useRouter()
    const user = ref({})
    const activeTab = ref('carousel')
    const saveLoading = ref(false)

    const platformStatsLoading = ref(false)
    const platformStats = reactive({
      userCount: 0,
      studentCount: 0,
      teacherCount: 0,
      materialCount: 0,
      taskCount: 0,
      discussionCount: 0,
      newsCount: 0,
      forumTopicCount: 0,
      newsViewCount: 0,
      discussionViewCount: 0,
      forumTopicViewCount: 0,
      materialViewCount: null
    })

    // 轮播图
    const carousels = ref([])
    const carouselDialogVisible = ref(false)
    const carouselForm = reactive({ id: null, title: '', page: 'home', imageUrl: '', linkUrl: '', sortOrder: 0, status: true })
    const carouselUploadFile = ref(null)

    // 新闻
    const newsList = ref([])
    const newsDialogVisible = ref(false)
    const newsForm = reactive({ id: null, title: '', category: '', summary: '', content: '', coverImage: '', author: '', status: true, materialIds: [] })
    const newsCoverUploadFile = ref(null)

    // 思想栏目
    const thoughtCategories = ['重要活动', '重要文章', '重要会议', '重要讲话', '出国访问', '批示指示', '十个明确', '十四个坚持', '十三个方面成就', '六个必须坚持']
    const thoughtCategory = ref(thoughtCategories[0])
    const thoughtNewsList = ref([])

    // 科学栏目
    const scienceModule = ref('empower')
    const scienceItems = ref([])
    const scienceDialogVisible = ref(false)
    const scienceForm = reactive({ id: null, module: 'empower', contentType: 'VIDEO', title: '', description: '', coverImage: '', articleContent: '', linkUrl: '', sortOrder: 0, status: true })
    const scienceCoverUploadFile = ref(null)

    // 素材
    const materials = ref([])
    const materialFilter = ref('PENDING')
    const selectMaterials = ref([])
    const rejectDialogVisible = ref(false)
    const rejectReason = ref('')
    const rejectingMaterial = ref(null)

    const revokeDialogVisible = ref(false)
    const revokeReason = ref('')
    const revokingMaterial = ref(null)

    const materialDetailDialogVisible = ref(false)
    const materialDetail = ref(null)

    const approvedMaterials = computed(() => (selectMaterials.value || []).filter(m => m && m.status === 'APPROVED'))

    // 话题
    const topics = ref([])
    const topicDialogVisible = ref(false)
    const topicForm = reactive({ id: null, title: '', description: '', materialIds: [], newsIds: [], status: 'ACTIVE' })

    const newsOptions = ref([])

    // 发布人物事迹
    const leaderMaterialDialogVisible = ref(false)
    const leaderMaterialForm = reactive({ title: '', description: '', content: '', fileUrl: '', tagIds: [] })
    const leaderCoverUploadFile = ref(null)
    const allTags = ref([])
    const newTagName = ref('')
    const tagCategories = ['全部', '理学', '工学', '农学', '医学', '其他']
    const baseCategories = ['理学', '工学', '农学', '医学', '其他']
    const materialCategories = ['专业思政案例', '政策解读', '安全类思政', '科技伦理', '课程关联思政']
    const tagFilterCategory = ref('全部')
    const newTagCategory = ref('')
    const tagManageDialogVisible = ref(false)

    const leaderTagOptions = computed(() => {
      const c = tagFilterCategory.value
      if (!c || c === '全部') return allTags.value || []
      return (allTags.value || []).filter(t => t && t.category === c)
    })

    const formatTime = (time) => {
      if (!time) return ''
      const s = String(time)
      return s.replace('T', ' ').substring(0, 19)
    }

    const formatCarouselPage = (row) => {
      if (row && !row.page && row.title === '思想页横幅') return '思想页'
      const p = (row && row.page) ? row.page : 'home'
      if (p === 'thought') return '思想页'
      if (p === 'science') return '学习科学'
      return '首页'
    }

    // ========= 登录态 =========
    onMounted(() => {
      const userData = localStorage.getItem('user')
      if (!userData) {
        router.push('/login')
        return
      }
      let parsed
      try {
        parsed = JSON.parse(userData)
      } catch (e) {
        localStorage.removeItem('user')
        router.push('/login')
        return
      }
      if (!parsed || parsed.role !== 'ADMIN') {
        localStorage.removeItem('user')
        router.push('/login')
        return
      }
      user.value = parsed
      loadCarousels()
    })

    const handleLogout = () => {
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    const handleTabSelect = (index) => {
      activeTab.value = index
      if (index === 'analysis') loadPlatformStats()
      if (index === 'carousel') loadCarousels()
      if (index === 'news') loadNews()
      if (index === 'thought') loadThoughtNews()
      if (index === 'science') loadScienceItems()
      if (index === 'material') loadMaterials()
      if (index === 'topic') loadTopics()
    }

    const formatStat = (v) => {
      if (v === null || v === undefined) return '-'
      const n = Number(v)
      if (!Number.isFinite(n)) return '-'
      return String(n)
    }

    const loadPlatformStats = async () => {
      platformStatsLoading.value = true
      try {
        const res = await getPlatformStats()
        if (res.data.code === 200) {
          Object.assign(platformStats, res.data.data || {})
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('获取统计失败')
      } finally {
        platformStatsLoading.value = false
      }
    }

    // ========= 轮播图 =========
    const loadCarousels = async () => {
      try {
        const res = await getAllCarousels()
        if (res.data.code === 200) carousels.value = res.data.data || []
      } catch (e) {
        console.error(e)
      }
    }

    const showCarouselDialog = (row = null) => {
      if (row) {
        Object.assign(carouselForm, { ...row })
      } else {
        Object.assign(carouselForm, { id: null, title: '', page: 'home', imageUrl: '', linkUrl: '', sortOrder: 0, status: true })
      }
      carouselUploadFile.value = null
      carouselDialogVisible.value = true
    }

    const showThoughtBannerDialog = () => {
      Object.assign(carouselForm, { id: null, title: '思想页横幅', page: 'thought', imageUrl: '', linkUrl: '', sortOrder: 0, status: true })
      carouselUploadFile.value = null
      carouselDialogVisible.value = true
    }

    const handleCarouselImageChange = (file) => {
      const raw = extractUploadRawFile(file)
      if (!raw) {
        ElMessage.error('读取图片失败，请重新选择图片')
        return
      }
      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片文件')
        return
      }
      carouselUploadFile.value = raw
      carouselForm.imageUrl = URL.createObjectURL(raw)
    }

    const handleNewsCoverChange = (uploadFile, uploadFiles) => {
      const raw = extractUploadRawFile(uploadFile, uploadFiles)

      if (!raw) {
        ElMessage.error('读取封面图片失败，请重新选择图片')
        return
      }

      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片文件')
        return
      }

      newsCoverUploadFile.value = raw
      newsForm.coverImage = URL.createObjectURL(raw)
    }

    const handleNewsVideoInsertChange = (uploadFile, uploadFiles) => {
      const raw = extractUploadRawFile(uploadFile, uploadFiles)

      if (!raw) {
        ElMessage.error('读取视频失败，请重新选择视频')
        return
      }

      if (!String(raw.type || '').startsWith('video/')) {
        ElMessage.warning('仅支持视频文件')
        return
      }

      if (raw.size && raw.size > 20 * 1024 * 1024) {
        ElMessage.warning('视频文件较大，可能导致上传失败或卡顿，建议使用较小视频')
      }

      uploadResourceFile(raw, 'news-video').then((result) => {
        const videoTag = `<video src="${result}" controls style="max-width:100%;height:auto;border-radius:6px;"></video>`
        const current = newsForm.content || ''
        newsForm.content = current ? `${current}\n${videoTag}\n` : `${videoTag}\n`
      }).catch((error) => {
        ElMessage.error(error && error.message ? error.message : '视频上传失败')
      })
    }

    const handleScienceArticleImageChange = (file) => {
      const raw = extractUploadRawFile(file)
      if (!raw) return
      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片文件')
        return
      }

      uploadResourceFile(raw, 'science-article').then((result) => {
        const imgTag = `<img src="${result}" style="max-width:100%;height:auto;" />`
        const current = scienceForm.articleContent || ''
        scienceForm.articleContent = current ? `${current}\n${imgTag}\n` : `${imgTag}\n`
      }).catch((error) => {
        ElMessage.error(error && error.message ? error.message : '图片上传失败')
      })
    }

    const saveCarousel = async () => {
      if (!carouselForm.imageUrl) { ElMessage.warning('请上传图片'); return }
      saveLoading.value = true
      try {
        const payload = { ...carouselForm }
        if (carouselUploadFile.value) {
          payload.imageUrl = await uploadResourceFile(carouselUploadFile.value, 'carousel')
        }
        const res = carouselForm.id ? await updateCarousel(payload) : await createCarousel(payload)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          carouselDialogVisible.value = false
          loadCarousels()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }

    const removeCarousel = async (row) => {
      try {
        await ElMessageBox.confirm('确定删除该轮播图吗？', '提示', { type: 'warning' })
        const res = await deleteCarousel(row.id)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          loadCarousels()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        // canceled
      }
    }

    // ========= 新闻/思想 =========
    const loadNews = async () => {
      try {
        const res = await getAllNews()
        if (res.data.code === 200) newsList.value = res.data.data || []
      } catch (e) {
        console.error(e)
      }
    }

    const loadThoughtNews = async () => {
      try {
        const res = await getAllNewsByCategory(thoughtCategory.value)
        if (res.data.code === 200) thoughtNewsList.value = res.data.data || []
      } catch (e) {
        console.error(e)
      }
    }

    const showNewsDialog = async (row = null, presetCategory = null) => {
      await ensureSelectMaterialsLoaded(true)
      if (row && row.id) {
        try {
          const res = await getNewsById(row.id)
          if (res.data.code === 200) Object.assign(newsForm, res.data.data)
          else Object.assign(newsForm, { ...row })
        } catch (e) {
          Object.assign(newsForm, { ...row })
        }
      } else {
        Object.assign(newsForm, { id: null, title: '', category: presetCategory || '', summary: '', content: '', coverImage: '', author: '', status: true, materialIds: [] })
      }
      newsCoverUploadFile.value = null
      newsDialogVisible.value = true
    }

    const saveNews = async () => {
      if (!newsForm.title || !newsForm.content) { ElMessage.warning('请完善标题和内容'); return }
      if (activeTab.value === 'thought' && !newsForm.category) { ElMessage.warning('请选择分类'); return }
      saveLoading.value = true
      try {
        const payload = { ...newsForm }
        if (newsCoverUploadFile.value) {
          payload.coverImage = await uploadResourceFile(newsCoverUploadFile.value, 'news-cover')
        }
        const res = newsForm.id ? await updateNews(payload) : await createNews(payload)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          newsDialogVisible.value = false
          if (activeTab.value === 'thought') loadThoughtNews()
          else loadNews()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }

    const removeNews = async (row) => {
      try {
        await ElMessageBox.confirm('确定删除该内容吗？', '提示', { type: 'warning' })
        const res = await deleteNews(row.id)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          if (activeTab.value === 'thought') loadThoughtNews()
          else loadNews()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        // canceled
      }
    }

    // ========= 科学栏目 =========
    const loadScienceItems = async () => {
      try {
        const res = await getAllScienceItems(scienceModule.value)
        if (res.data.code === 200) scienceItems.value = res.data.data || []
      } catch (e) {
        console.error(e)
      }
    }

    const showScienceDialog = (row = null) => {
      if (row) {
        Object.assign(scienceForm, {
          id: row.id,
          module: row.module || scienceModule.value || 'empower',
          contentType: row.contentType || 'VIDEO',
          title: row.title || '',
          description: row.description || '',
          coverImage: row.coverImage || '',
          articleContent: row.articleContent || '',
          linkUrl: row.linkUrl || '',
          sortOrder: row.sortOrder || 0,
          status: row.status !== false
        })
      } else {
        Object.assign(scienceForm, {
          id: null,
          module: scienceModule.value || 'empower',
          contentType: scienceModule.value === 'empower' ? 'VIDEO' : 'VIDEO',
          title: '',
          description: '',
          coverImage: '',
          articleContent: '',
          linkUrl: '',
          sortOrder: 0,
          status: true
        })
      }
      scienceCoverUploadFile.value = null
      scienceDialogVisible.value = true
    }

    const handleScienceImageChange = (file) => {
      const raw = extractUploadRawFile(file)
      if (!raw) return

      const name = (raw.name || '').toLowerCase()
      const isMp4 = name.endsWith('.mp4')
      if (!isMp4) {
        ElMessage.error('仅支持 mp4 格式视频')
        return
      }

      scienceCoverUploadFile.value = raw
      scienceForm.coverImage = URL.createObjectURL(raw)
    }

    const saveScienceItem = async () => {
      if (!scienceForm.module) { ElMessage.warning('请选择模块'); return }
      if (!scienceForm.title) { ElMessage.warning('请输入标题'); return }

      if ((scienceForm.module === 'news' || scienceForm.module === 'knowledge') && scienceForm.contentType === 'ARTICLE') {
        if (!scienceForm.articleContent) { ElMessage.warning('请输入文章正文'); return }
      }

      saveLoading.value = true
      try {
        const payload = { ...scienceForm, linkUrl: '', sortOrder: 0 }

        if (scienceCoverUploadFile.value) {
          payload.coverImage = await uploadResourceFile(scienceCoverUploadFile.value, 'science-video')
        }

        if (payload.module === 'empower') {
          payload.contentType = 'VIDEO'
          payload.articleContent = ''
        } else if (payload.contentType === 'ARTICLE') {
          payload.coverImage = ''
          payload.articleContent = payload.articleContent || ''
        } else {
          payload.contentType = 'VIDEO'
          payload.articleContent = ''
        }

        const res = scienceForm.id ? await updateScienceItem(payload) : await createScienceItem(payload)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          scienceDialogVisible.value = false
          loadScienceItems()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }

    const removeScienceItem = async (row) => {
      try {
        await ElMessageBox.confirm('确定删除该内容吗？', '提示', { type: 'warning' })
        const res = await deleteScienceItem(row.id)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          loadScienceItems()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        // canceled
      }
    }

    const loadAllTags = async () => {
      try {
        const res = await getAllTags()
        if (res.data.code === 200) allTags.value = res.data.data || []
      } catch (e) {
        // ignore
      }
    }

    const showLeaderMaterialDialog = () => {
      leaderMaterialForm.title = ''
      leaderMaterialForm.description = ''
      leaderMaterialForm.content = ''
      leaderMaterialForm.fileUrl = ''
      leaderMaterialForm.tagIds = []
      leaderCoverUploadFile.value = null
      loadAllTags()
      leaderMaterialDialogVisible.value = true
    }

    const handleLeaderCoverChange = (file) => {
      const raw = extractUploadRawFile(file)
      if (!raw) return
      if (!String(raw.type || '').startsWith('image/')) {
        ElMessage.warning('仅支持图片文件')
        return
      }
      leaderCoverUploadFile.value = raw
      leaderMaterialForm.fileUrl = URL.createObjectURL(raw)
    }

    const saveLeaderMaterial = async () => {
      if (!leaderMaterialForm.title || !leaderMaterialForm.content) { ElMessage.warning('请填写标题和内容'); return }
      saveLoading.value = true
      try {
        const payload = { ...leaderMaterialForm }
        if (leaderCoverUploadFile.value) {
          payload.fileUrl = await uploadResourceFile(leaderCoverUploadFile.value, 'leader-cover')
        }
        const res = await createLeaderMaterial(payload)
        if (res.data.code === 200) {
          ElMessage.success('发布成功')
          leaderMaterialDialogVisible.value = false
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('发布失败')
      } finally {
        saveLoading.value = false
      }
    }

    const addNewTag = async () => {
      if (!newTagName.value) { ElMessage.warning('请输入标签'); return }
      try {
        const res = await createTag(newTagName.value, newTagCategory.value)
        if (res.data.code === 200) {
          ElMessage.success('添加成功')
          newTagName.value = ''
          newTagCategory.value = ''
          const listRes = await getAllTags()
          if (listRes.data.code === 200) allTags.value = listRes.data.data || []
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('添加失败')
      }
    }

    const openTagManage = async () => {
      tagManageDialogVisible.value = true
      try {
        const res = await getAllTags()
        if (res.data.code === 200) allTags.value = res.data.data || []
      } catch (e) {
        // ignore
      }
    }

    const onTagCategoryChange = async (row) => {
      if (!row || !row.id) return
      try {
        const res = await updateTagCategory(row.id, row.category)
        if (res.data.code !== 200) ElMessage.error(res.data.message)
      } catch (e) {
        ElMessage.error('更新失败')
      }
    }

    const ensureSelectMaterialsLoaded = async (force = false) => {
      if (!force && selectMaterials.value.length > 0 && newsOptions.value.length > 0) {
        return
      }

      try {
        const [materialRes, newsRes] = await Promise.all([
          getAllMaterialsForAdmin(),
          getPublishedNews()
        ])

        if (materialRes.data.code === 200) {
          selectMaterials.value = materialRes.data.data || []
        }

        if (newsRes.data.code === 200) {
          newsOptions.value = newsRes.data.data || []
        }
      } catch (e) {
        console.error(e)
      }
    }

    const loadMaterials = async () => {
      try {
        const res = materialFilter.value === 'PENDING'
          ? await getPendingMaterials()
          : await getAllMaterialsForAdmin()

        if (res.data.code === 200) {
          materials.value = res.data.data || []
        } else {
          ElMessage.error(res.data.message)
        }

        await ensureSelectMaterialsLoaded(true)
      } catch (e) {
        ElMessage.error('加载素材失败')
      }
    }

    const showMaterialDetail = async (row) => {
      if (!row || !row.id) return
      try {
        const res = await getMaterialDetail(row.id)
        if (res.data.code === 200) {
          materialDetail.value = res.data.data || null
          materialDetailDialogVisible.value = true
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('加载素材详情失败')
      }
    }

    const handleApprove = async (row) => {
      if (!row || !row.id) return
      try {
        const res = await approveMaterial(row.id)
        if (res.data.code === 200) {
          ElMessage.success('审核通过')
          await loadMaterials()
          if (materialDetail.value && materialDetail.value.id === row.id) {
            await showMaterialDetail(row)
          }
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('审核失败')
      }
    }

    const handleReject = (row) => {
      rejectingMaterial.value = row || null
      rejectReason.value = ''
      rejectDialogVisible.value = true
    }

    const confirmReject = async () => {
      if (!rejectingMaterial.value || !rejectingMaterial.value.id) return
      saveLoading.value = true
      try {
        const res = await rejectMaterial(rejectingMaterial.value.id, rejectReason.value)
        if (res.data.code === 200) {
          ElMessage.success('已拒绝')
          rejectDialogVisible.value = false
          await loadMaterials()
          if (materialDetail.value && materialDetail.value.id === rejectingMaterial.value.id) {
            materialDetailDialogVisible.value = false
          }
          rejectingMaterial.value = null
          rejectReason.value = ''
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        saveLoading.value = false
      }
    }

    const handleRevoke = (row) => {
      revokingMaterial.value = row || null
      revokeReason.value = ''
      revokeDialogVisible.value = true
    }

    const confirmRevoke = async () => {
      if (!revokingMaterial.value || !revokingMaterial.value.id) return
      saveLoading.value = true
      try {
        const res = await revokeMaterial(revokingMaterial.value.id, revokeReason.value)
        if (res.data.code === 200) {
          ElMessage.success('已撤回')
          revokeDialogVisible.value = false
          await loadMaterials()
          if (materialDetail.value && materialDetail.value.id === revokingMaterial.value.id) {
            materialDetailDialogVisible.value = false
          }
          revokingMaterial.value = null
          revokeReason.value = ''
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        saveLoading.value = false
      }
    }

    const approveInDetail = async () => {
      if (!materialDetail.value || !materialDetail.value.id) return
      await handleApprove(materialDetail.value)
    }

    const rejectInDetail = () => {
      if (!materialDetail.value || !materialDetail.value.id) return
      handleReject(materialDetail.value)
    }

    const revokeInDetail = () => {
      if (!materialDetail.value || !materialDetail.value.id) return
      handleRevoke(materialDetail.value)
    }

    const onMaterialShelfChange = async (row, enabled) => {
      if (!row || !row.id) return
      const shelfStatus = enabled ? 'ON' : 'OFF'
      try {
        const res = await setMaterialShelfStatus(row.id, shelfStatus)
        if (res.data.code === 200) {
          row.shelfStatus = shelfStatus
          ElMessage.success('上架状态已更新')
          await ensureSelectMaterialsLoaded(true)
        } else {
          ElMessage.error(res.data.message)
          row.shelfStatus = shelfStatus === 'ON' ? 'OFF' : 'ON'
        }
      } catch (e) {
        row.shelfStatus = shelfStatus === 'ON' ? 'OFF' : 'ON'
        ElMessage.error('更新上架状态失败')
      }
    }

    const onMaterialCategoryChange = async (row) => {
      if (!row || !row.id) return
      try {
        const res = await updateMaterialCategory(row.id, row.category || '')
        if (res.data.code === 200) {
          ElMessage.success('分类已更新')
          await ensureSelectMaterialsLoaded(true)
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('更新分类失败')
      }
    }

    const loadTopics = async () => {
      try {
        const res = await getAllTopics()
        if (res.data.code === 200) {
          topics.value = res.data.data || []
        } else {
          ElMessage.error(res.data.message)
        }
        await ensureSelectMaterialsLoaded(true)
      } catch (e) {
        ElMessage.error('加载话题失败')
      }
    }

    const showTopicDialog = async (row = null) => {
      await ensureSelectMaterialsLoaded(true)

      if (row && row.id) {
        try {
          const res = await getTopicById(row.id)
          if (res.data.code === 200) {
            const topicData = res.data.data || {}
            Object.assign(topicForm, {
              id: topicData.id || row.id,
              title: topicData.title || '',
              description: topicData.description || '',
              materialIds: topicData.materialIds || (topicData.materials || []).map(item => item.id),
              newsIds: topicData.newsIds || (topicData.newsList || []).map(item => item.id),
              status: topicData.status || 'ACTIVE'
            })
          } else {
            ElMessage.error(res.data.message)
            return
          }
        } catch (e) {
          ElMessage.error('加载话题失败')
          return
        }
      } else {
        Object.assign(topicForm, { id: null, title: '', description: '', materialIds: [], newsIds: [], status: 'ACTIVE' })
      }

      topicDialogVisible.value = true
    }

    const saveTopic = async () => {
      if (!String(topicForm.title || '').trim()) {
        ElMessage.warning('请输入话题标题')
        return
      }

      saveLoading.value = true
      try {
        const payload = {
          ...topicForm,
          materialIds: topicForm.materialIds || [],
          newsIds: topicForm.newsIds || []
        }

        const res = topicForm.id ? await updateTopic(payload) : await createTopic(payload)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          topicDialogVisible.value = false
          await loadTopics()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }

    const removeTopic = async (row) => {
      if (!row || !row.id) return
      try {
        await ElMessageBox.confirm('确定删除该话题吗？', '提示', { type: 'warning' })
        const res = await deleteTopic(row.id)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          await loadTopics()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        // canceled
      }
    }

    return {
      Picture, Document, Files, ChatDotRound, Plus, DataAnalysis,
      user,
      activeTab,
      saveLoading,
      handleTabSelect,
      handleLogout,
      formatTime,
      platformStats,
      platformStatsLoading,
      loadPlatformStats,
      formatStat,
      // 轮播图
      carousels,
      carouselDialogVisible,
      carouselForm,
      showCarouselDialog,
      showThoughtBannerDialog,
      handleCarouselImageChange,
      saveCarousel,
      removeCarousel,
      formatCarouselPage,
      // 新闻
      newsList,
      newsDialogVisible,
      newsForm,
      showNewsDialog,
      saveNews,
      removeNews,
      handleNewsCoverChange,
      handleNewsVideoInsertChange,
      // 思想栏目
      thoughtCategories,
      thoughtCategory,
      thoughtNewsList,
      loadThoughtNews,
      // 科学栏目
      scienceModule,
      scienceItems,
      scienceDialogVisible,
      scienceForm,
      loadScienceItems,
      showScienceDialog,
      handleScienceImageChange,
      handleScienceArticleImageChange,
      saveScienceItem,
      removeScienceItem,
      // 素材
      materials,
      materialFilter,
      loadMaterials,
      showMaterialDetail,
      handleApprove,
      handleReject,
      handleRevoke,
      rejectDialogVisible,
      rejectReason,
      confirmReject,
      revokeDialogVisible,
      revokeReason,
      confirmRevoke,
      materialDetailDialogVisible,
      materialDetail,
      approveInDetail,
      rejectInDetail,
      revokeInDetail,
      approvedMaterials,
      newsOptions,
      onMaterialShelfChange,
      onMaterialCategoryChange,
      materialCategories,
      // 话题
      topics,
      topicDialogVisible,
      topicForm,
      showTopicDialog,
      saveTopic,
      removeTopic,
      // 人物事迹 & 标签
      leaderMaterialDialogVisible,
      leaderMaterialForm,
      showLeaderMaterialDialog,
      handleLeaderCoverChange,
      saveLeaderMaterial,
      allTags,
      leaderTagOptions,
      newTagName,
      tagCategories,
      baseCategories,
      tagFilterCategory,
      newTagCategory,
      tagManageDialogVisible,
      openTagManage,
      addNewTag,
      onTagCategoryChange
    }
  }
}
</script>

<style scoped>
  /* ... */
  .science-thumb {
    width: 120px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
    background: #f5f5f5;
  }
  .science-thumb.placeholder {
    border: 1px dashed #ddd;
  }

.admin-container {
  min-height: 100vh;
  background: #f0f2f5;
}
.navbar {
  background: #001529;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}
.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}
.user-info {
  color: #fff;
  display: flex;
  align-items: center;
  gap: 15px;
}
.main-content {
  padding-top: 80px;
  max-width: 1400px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
}
.sidebar {
  background: #fff;
  border-radius: 8px;
}
.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 600px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #001529;
  padding-bottom: 15px;
}
.card-header h2 {
  margin: 0;
  color: #333;
}
.carousel-thumb {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
.science-thumb {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  background: #f5f5f5;
}
.science-thumb.placeholder {
  border: 1px dashed #ddd;
}
.carousel-uploader {
  width: 300px;
}
.carousel-preview {
  width: 300px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}
.upload-placeholder {
  width: 300px;
  height: 150px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
}
.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}
.text-muted {
  color: #999;
}

.platform-stats-grid {
  margin-bottom: 16px;
}
.platform-stat-card {
  background: #f5f7fa;
  padding: 18px;
  border-radius: 8px;
}
.platform-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  text-align: center;
  line-height: 1.2;
}
.platform-stat-label {
  margin-top: 8px;
  text-align: center;
  color: #606266;
}
</style>
