<template>
  <div class="manage-page">
    <div class="topbar">
      <div class="topbar-title">管理中心</div>
      <el-button type="text" class="home-back-btn" @click="goHome">
        <el-icon><Back /></el-icon> 返回首页
      </el-button>
    </div>

    <div class="layout-body">
      <div class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect" class="sidebar-menu">
          <el-menu-item index="dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="material">
            <el-icon><Document /></el-icon>
            <span>思政素材管理</span>
          </el-menu-item>
          <el-menu-item index="task">
            <el-icon><List /></el-icon>
            <span>学习任务管理</span>
          </el-menu-item>
          <el-menu-item index="discussion">
            <el-icon><ChatDotRound /></el-icon>
            <span>主题讨论管理</span>
          </el-menu-item>
          <el-menu-item index="student">
            <el-icon><UserFilled /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="main-area">
        <div v-if="activeMenu === 'dashboard'" class="content-section">
          <h2>工作台</h2>
          <el-row :gutter="20" class="stat-cards">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ stats.taskCount }}</div>
                <div class="stat-label">已创建任务数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ stats.discussionCount }}</div>
                <div class="stat-label">已创建讨论数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value">{{ stats.studentCount }}</div>
                <div class="stat-label">绑定学生总数</div>
              </div>
            </el-col>
          </el-row>

          <div class="quick-actions">
            <h3>快速操作</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-button type="primary" size="large" class="action-btn" @click="openTaskDialog">
                  <el-icon><Plus /></el-icon> 创建学习任务
                </el-button>
              </el-col>
              <el-col :span="12">
                <el-button type="success" size="large" class="action-btn" @click="openDiscussionDialog">
                  <el-icon><Plus /></el-icon> 创建话题
                </el-button>
              </el-col>
            </el-row>
          </div>

          <div class="recent-tasks">
            <div class="section-header">
              <h3>按任务码/讨论码查询参与明细</h3>
            </div>

            <div style="display:flex; flex-wrap:wrap; gap: 10px; align-items:center; margin-bottom: 12px;">
              <el-input v-model="participationQuery.code" placeholder="请输入任务码/讨论码" style="width: 240px" @keyup.enter="searchParticipation" />
              <el-select v-model="participationQuery.type" placeholder="类型(可选)" style="width: 160px">
                <el-option label="自动识别" value="" />
                <el-option label="任务" value="TASK" />
                <el-option label="讨论" value="DISCUSSION" />
              </el-select>
              <el-button type="primary" :loading="participationLoading" @click="searchParticipation">查询</el-button>
              <el-button @click="resetParticipation">重置</el-button>
              <div v-if="participationMeta.targetTitle" style="color:#606266;">
                {{ participationMeta.type === 'TASK' ? '任务' : participationMeta.type === 'DISCUSSION' ? '讨论' : '' }}：{{ participationMeta.targetTitle }}
              </div>
            </div>

            <el-row v-if="participationMeta.type === 'DISCUSSION' && participationSummary" :gutter="12" style="margin-bottom: 12px;">
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.boundStudentParticipantCount || 0 }}/{{ participationSummary.boundStudentCount || 0 }}</div>
                  <div class="stat-label">已参与学生/绑定学生</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.boundTotalCommentCount || 0 }}</div>
                  <div class="stat-label">绑定学生发言总数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.allStats?.studentParticipantCount || 0 }}</div>
                  <div class="stat-label">全体学生参与人数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.allStats?.totalCommentCount || 0 }}</div>
                  <div class="stat-label">讨论区总发言数</div>
                </div>
              </el-col>
            </el-row>

            <el-row v-if="participationMeta.type === 'TASK' && participationSummary" :gutter="12" style="margin-bottom: 12px;">
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.boundStudentParticipantCount || 0 }}/{{ participationSummary.boundStudentCount || 0 }}</div>
                  <div class="stat-label">已参与学生/绑定学生</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.boundStudentCompletedCount || 0 }}</div>
                  <div class="stat-label">绑定学生已完成数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationSummary.taskTotalMaterials || 0 }}</div>
                  <div class="stat-label">任务素材数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-value">{{ participationTotal || 0 }}</div>
                  <div class="stat-label">绑定学生总数</div>
                </div>
              </el-col>
            </el-row>

            <el-table :data="participationList" stripe v-loading="participationLoading">
              <el-table-column prop="studentRealName" label="学生姓名" width="140" />
              <el-table-column prop="studentName" label="账号" width="160" />
              <el-table-column prop="studentMajor" label="专业" />
              <el-table-column label="参与状态" width="110">
                <template #default="{ row }">
                  <el-tag v-if="participationMeta.type === 'DISCUSSION'" :type="(Number(row.discussionCommentCount || 0) + Number(row.discussionReplyCount || 0)) > 0 ? 'success' : 'info'">
                    {{ (Number(row.discussionCommentCount || 0) + Number(row.discussionReplyCount || 0)) > 0 ? '已参与' : '未参与' }}
                  </el-tag>
                  <el-tag v-else :type="formatTaskParticipation(row).tagType">{{ formatTaskParticipation(row).text }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column v-if="participationMeta.type === 'DISCUSSION'" label="发言数" width="100">
                <template #default="{ row }">{{ row.discussionCommentCount || 0 }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'DISCUSSION'" label="回复数" width="100">
                <template #default="{ row }">{{ row.discussionReplyCount || 0 }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'DISCUSSION'" label="最后发言时间" width="180">
                <template #default="{ row }">{{ formatTime(row.discussionLastCommentAt) || '-' }}</template>
              </el-table-column>

              <el-table-column v-if="participationMeta.type === 'TASK'" label="学习时长" width="120">
                <template #default="{ row }">{{ formatDuration(row.duration) || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'TASK'" label="素材进度" width="120">
                <template #default="{ row }">{{ formatMaterialProgress(row) }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'TASK'" label="打卡" width="90">
                <template #default="{ row }">{{ row.checkedIn ? '已打卡' : '-' }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'TASK'" label="测验" width="110">
                <template #default="{ row }">{{ formatQuiz(row.quizCorrect, row.quizTotal) }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'TASK'" label="进入时间" width="180">
                <template #default="{ row }">{{ formatTime(row.enterTime) || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="participationMeta.type === 'TASK'" label="离开时间" width="180">
                <template #default="{ row }">{{ formatTime(row.leaveTime) || '-' }}</template>
              </el-table-column>
              <el-table-column label="绑定时间" width="180">
                <template #default="{ row }">{{ formatTime(row.bindTime) || '-' }}</template>
              </el-table-column>
            </el-table>

            <div style="display:flex; justify-content:flex-end; margin-top: 12px;">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next"
                :total="participationTotal"
                :current-page="participationPage"
                :page-size="participationPageSize"
                :page-sizes="[10, 20, 50, 100]"
                @current-change="handleParticipationPageChange"
                @size-change="handleParticipationPageSizeChange"
              />
            </div>
          </div>

          <div class="recent-tasks" v-if="discussionList.length > 0">
            <div class="section-header">
              <h3>我的讨论（{{ discussionList.length }}个）</h3>
              <el-button v-if="discussionList.length > 5" type="text" @click="discussionExpanded = !discussionExpanded">
                {{ discussionExpanded ? '收起' : '展开全部' }}
                <el-icon><ArrowUp v-if="discussionExpanded" /><ArrowDown v-else /></el-icon>
              </el-button>
            </div>
            <el-table :data="displayDiscussionList" stripe>
              <el-table-column prop="title" label="讨论标题" />
              <el-table-column prop="code" label="讨论码" width="140" />
              <el-table-column label="绑定/参与" width="140">
                <template #default="{ row }">{{ (row.boundStudentParticipantCount || 0) }}/{{ (row.boundStudentCount || 0) }}</template>
              </el-table-column>
              <el-table-column label="绑定发言" width="110">
                <template #default="{ row }">{{ row.boundStudentCommentCount || 0 }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'IN_PROGRESS' ? 'success' : row.status === 'ENDED' ? 'info' : 'warning'">
                    {{ row.status === 'IN_PROGRESS' ? '进行中' : row.status === 'ENDED' ? '已结束' : '未开始' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="text" @click="viewDiscussion(row)">详情</el-button>
                  <el-button type="text" class="danger" @click="removeDiscussion(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="recent-tasks" v-if="taskList.length > 0">
            <div class="section-header">
              <h3>我的任务（{{ taskList.length }}个）</h3>
              <el-button v-if="taskList.length > 5" type="text" @click="taskExpanded = !taskExpanded">
                {{ taskExpanded ? '收起' : '展开全部' }}
                <el-icon><ArrowUp v-if="taskExpanded" /><ArrowDown v-else /></el-icon>
              </el-button>
            </div>
            <el-table :data="displayTaskList" stripe>
              <el-table-column prop="title" label="任务标题" />
              <el-table-column prop="code" label="任务码" width="140" />
              <el-table-column label="绑定/参与" width="140">
                <template #default="{ row }">{{ (row.participantCount || 0) }}/{{ (row.boundStudentCount || 0) }}</template>
              </el-table-column>
              <el-table-column label="完成数" width="110">
                <template #default="{ row }">{{ row.completedCount || 0 }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'IN_PROGRESS' ? 'success' : row.status === 'ENDED' ? 'info' : 'warning'">
                    {{ row.status === 'IN_PROGRESS' ? '进行中' : row.status === 'ENDED' ? '已结束' : '未开始' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="截止时间" width="180">
                <template #default="{ row }">{{ formatTime(row.endTime) || '无' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="text" @click="viewTask(row)">详情</el-button>
                  <el-button type="text" class="danger" @click="removeTask(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div v-if="activeMenu === 'profile'" class="content-section">
          <h2>个人信息</h2>
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleAvatarChange"
              accept="image/*"
            >
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
              <div v-else class="avatar-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传头像</span>
              </div>
            </el-upload>
            <el-button type="primary" size="small" @click="saveAvatar" :loading="avatarLoading" style="margin-top: 10px">保存头像</el-button>
          </div>

          <el-form :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="真实姓名">
              <el-input v-model="profileForm.realName" disabled />
            </el-form-item>
            <el-form-item label="职工号">
              <el-input v-model="profileForm.staffNo" disabled />
            </el-form-item>
            <el-form-item label="账户昵称">
              <el-input v-model="profileForm.username" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
            <el-form-item label="所属专业">
              <el-input v-model="profileForm.major" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saveLoading" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>

          <el-divider />
          <h3>密码重置</h3>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="resetLoading" @click="resetPassword">重置密码</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="activeMenu === 'material'" class="content-section">
          <h2>素材管理</h2>
          <el-tabs v-model="materialTab">
            <el-tab-pane label="上传素材" name="upload">
              <el-form :model="materialForm" label-width="100px" class="material-form">
                <el-form-item label="素材类型">
                  <el-radio-group v-model="materialForm.type">
                    <el-radio label="IMAGE">图片</el-radio>
                    <el-radio label="VIDEO">视频</el-radio>
                    <el-radio label="ARTICLE">文章</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="素材标题">
                  <el-input v-model="materialForm.title" placeholder="请输入素材标题" />
                </el-form-item>

                <el-form-item label="上传文件" v-if="materialForm.type !== 'ARTICLE'">
                  <el-upload
                    action="#"
                    :show-file-list="false"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    :accept="materialForm.type === 'IMAGE' ? 'image/*' : 'video/*'"
                  >
                    <el-button type="primary">选择{{ materialForm.type === 'IMAGE' ? '图片' : '视频' }}</el-button>
                  </el-upload>
                  <div v-if="materialForm.fileUrl" class="file-preview">
                    <img v-if="materialForm.type === 'IMAGE'" :src="materialForm.fileUrl" class="preview-img" />
                    <video v-else :src="materialForm.fileUrl" controls class="preview-video"></video>
                  </div>
                </el-form-item>

                <el-form-item label="文章内容" v-if="materialForm.type === 'ARTICLE'">
                  <el-input v-model="materialForm.content" type="textarea" :rows="8" placeholder="请输入文章内容" />
                </el-form-item>

                <el-form-item label="素材介绍">
                  <el-input v-model="materialForm.description" type="textarea" :rows="3" placeholder="请输入素材介绍" />
                </el-form-item>

                <el-form-item label="选择标签">
                  <el-select v-model="tagFilterCategory" placeholder="大类" style="width: 140px">
                    <el-option v-for="c in tagCategories" :key="c" :label="c" :value="c" />
                  </el-select>
                  <el-select v-model="materialForm.tagIds" multiple placeholder="选择标签" style="width: 460px; margin-left: 10px">
                    <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="tag.id" />
                  </el-select>
                </el-form-item>

                <el-form-item label="新增标签">
                  <el-input v-model="newTagName" placeholder="请输入新标签名称" style="width: 220px" />
                  <el-select v-model="newTagCategory" placeholder="大类" style="width: 160px; margin-left: 10px">
                    <el-option v-for="c in baseCategories" :key="c" :label="c" :value="c" />
                  </el-select>
                  <el-button type="success" style="margin-left: 10px" @click="addNewTag">添加</el-button>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="uploadLoading" @click="submitMaterial">提交素材</el-button>
                  <el-button @click="resetMaterialForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="我的素材" name="my-materials">
              <el-table :data="myMaterials" stripe>
                <el-table-column prop="title" label="素材标题" />
                <el-table-column prop="type" label="素材类型" width="100">
                  <template #default="{ row }">{{ row.type === 'IMAGE' ? '图片' : row.type === 'VIDEO' ? '视频' : '文章' }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
                      {{ row.status === 'APPROVED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="上传时间" width="180">
                  <template #default="{ row }">{{ formatTime(row.createdAt || row.createTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button type="text" @click="viewMaterial(row)">详情</el-button>
                    <el-button type="text" class="danger" @click="removeMaterial(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>

          <el-dialog v-model="materialDetailVisible" title="素材详情" width="720px">
            <div class="material-detail">
              <div class="detail-row"><span class="label">标题：</span>{{ materialDetail.title }}</div>
              <div class="detail-row"><span class="label">类型：</span>{{ materialDetail.type === 'IMAGE' ? '图片' : materialDetail.type === 'VIDEO' ? '视频' : '文章' }}</div>
              <div class="detail-row"><span class="label">介绍：</span>{{ materialDetail.description || '无' }}</div>
              <div v-if="materialDetail.type === 'IMAGE' && materialDetail.fileUrl" class="detail-content">
                <img :src="materialDetail.fileUrl" class="detail-img" />
              </div>
              <div v-if="materialDetail.type === 'VIDEO' && materialDetail.fileUrl" class="detail-content">
                <video :src="materialDetail.fileUrl" controls class="detail-video"></video>
              </div>
              <div v-if="materialDetail.type === 'ARTICLE'" class="detail-content">
                <div class="article-content">{{ materialDetail.content }}</div>
              </div>
            </div>
          </el-dialog>
        </div>

        <div v-if="activeMenu === 'task'" class="content-section">
          <h2>任务管理</h2>
          <div class="table-toolbar">
            <el-button type="primary" @click="openTaskDialog"><el-icon><Plus /></el-icon> 创建任务</el-button>
            <el-button @click="loadTasks"><el-icon><Refresh /></el-icon> 刷新</el-button>
          </div>
          <el-table :data="taskList" stripe>
            <el-table-column prop="title" label="任务标题" />
            <el-table-column prop="code" label="任务码" width="140" />
            <el-table-column label="绑定/参与" width="140">
              <template #default="{ row }">{{ (row.participantCount || 0) }}/{{ (row.boundStudentCount || 0) }}</template>
            </el-table-column>
            <el-table-column label="完成数" width="110">
              <template #default="{ row }">{{ row.completedCount || 0 }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 'IN_PROGRESS' ? 'success' : row.status === 'ENDED' ? 'info' : 'warning'">
                  {{ row.status === 'IN_PROGRESS' ? '进行中' : row.status === 'ENDED' ? '已结束' : '未开始' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="截止时间" width="180">
              <template #default="{ row }">{{ formatTime(row.endTime) || '无' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button type="text" @click="viewTask(row)">详情</el-button>
                <el-button type="text" @click="copyText(row.code)">复制任务码</el-button>
                <el-button type="text" class="danger" @click="removeTask(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-dialog v-model="taskCreateVisible" title="创建学习任务" width="720px">
            <el-form :model="taskCreateForm" label-width="90px">
              <el-form-item label="任务标题" required>
                <el-input v-model="taskCreateForm.title" placeholder="请输入任务标题" />
              </el-form-item>
              <el-form-item label="任务说明">
                <el-input v-model="taskCreateForm.description" type="textarea" :rows="3" placeholder="请输入任务说明" />
              </el-form-item>

              <el-form-item label="教师素材">
                <el-select v-model="taskCreateForm.teacherMaterialIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%">
                  <el-option v-for="m in myMaterials" :key="m.id" :label="m.title" :value="m.id" />
                </el-select>
              </el-form-item>

              <el-form-item label="人物事迹">
                <el-select v-model="taskCreateForm.leaderMaterialIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%" :loading="discussionSourceLoading">
                  <el-option v-for="m in leaderMaterials" :key="m.id" :label="m.title" :value="m.id" />
                </el-select>
              </el-form-item>

              <el-form-item label="新闻资讯">
                <el-select v-model="taskCreateForm.newsIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%" :loading="discussionSourceLoading">
                  <el-option v-for="n in publishedNews" :key="n.id" :label="n.title" :value="n.id" />
                </el-select>
              </el-form-item>

              <el-form-item label="截止时间">
                <el-date-picker
                  v-model="taskCreateForm.endTime"
                  type="datetime"
                  placeholder="可选" style="width: 100%"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>

              <el-divider />

              <el-form-item label="AI题目">
                <div style="display:flex; flex-wrap:wrap; gap: 10px; align-items:center; width:100%;">
                  <el-input-number v-model="taskCreateForm.quizCount" :min="3" :max="5" :step="1" />
                  <el-button type="primary" :loading="taskQuizGenerating" @click="generateTaskQuizForCreate('FLAT')">AI生成题目</el-button>
                  <el-button type="success" :loading="taskQuizGenerating" @click="generateTaskQuizForCreate('PER_MATERIAL')">按素材生成题目</el-button>
                  <el-button v-if="taskCreateForm.quizJson" @click="taskCreateForm.quizJson = ''">清空</el-button>
                </div>
              </el-form-item>
              <el-form-item v-if="taskCreateForm.quizJson" label="题目JSON">
                <el-input v-model="taskCreateForm.quizJson" type="textarea" :rows="6" />
              </el-form-item>

              <el-form-item v-if="taskQuizPreview" label="题目预览">
                <div style="width: 100%;">
                  <div v-if="taskQuizPreview.mode === 'PER_MATERIAL'">
                    <el-collapse>
                      <el-collapse-item v-for="m in taskQuizPreview.materials" :key="m.materialId" :title="m.materialTitle || ('素材 ' + m.materialId)">
                        <div v-for="(q, idx) in (m.questions || [])" :key="idx" style="padding: 6px 0; border-bottom: 1px solid #ebeef5;">
                          <div style="font-weight: 600; line-height: 1.7;">{{ idx + 1 }}. {{ q.stem }}</div>
                          <div style="padding-left: 12px; line-height: 1.7;">
                            <div v-for="(val, key) in (q.options || {})" :key="key">{{ key }}. {{ val }}</div>
                          </div>
                          <div style="margin-top: 6px; color: #303133;">答案：{{ q.answer || '-' }}</div>
                          <div v-if="q.analysis" style="margin-top: 4px; color: #606266; white-space: pre-wrap;">解析：{{ q.analysis }}</div>
                        </div>
                      </el-collapse-item>
                    </el-collapse>
                  </div>
                  <div v-else>
                    <el-collapse>
                      <el-collapse-item v-for="(q, idx) in (taskQuizPreview.questions || [])" :key="idx" :title="(idx + 1) + '. ' + (q.stem || '')">
                        <div style="line-height: 1.7;">
                          <div style="font-weight: 600;">题干</div>
                          <div style="white-space: pre-wrap;">{{ q.stem }}</div>
                          <div style="margin-top: 8px; font-weight: 600;">选项</div>
                          <div style="padding-left: 12px;">
                            <div v-for="(val, key) in (q.options || {})" :key="key">{{ key }}. {{ val }}</div>
                          </div>
                          <div style="margin-top: 8px;">答案：{{ q.answer || '-' }}</div>
                          <div v-if="q.analysis" style="margin-top: 6px; color: #606266; white-space: pre-wrap;">解析：{{ q.analysis }}</div>
                        </div>
                      </el-collapse-item>
                    </el-collapse>
                  </div>
                </div>
              </el-form-item>

              <el-divider />

              <el-form-item label="观点选项">
                <el-input v-model="taskCreateForm.viewpointText" type="textarea" :rows="4" placeholder="每行一个观点选项（学生学习时需选择其一）" />
              </el-form-item>
              <el-form-item label="打卡时长">
                <div style="display:flex; align-items:center; gap: 10px;">
                  <el-input-number v-model="taskCreateForm.checkinRequiredMinutes" :min="0" :max="600" :step="1" />
                  <span style="color:#606266;">分钟（0 表示不要求打卡）</span>
                </div>
              </el-form-item>

              <div class="form-tip">至少选择一个素材/人物/新闻，截止时间到后不可再学习</div>
            </el-form>
            <template #footer>
              <el-button @click="taskCreateVisible = false">取消</el-button>
              <el-button type="primary" :loading="taskCreating" @click="submitCreateTask">创建</el-button>
            </template>
          </el-dialog>
        </div>

        <div v-if="activeMenu === 'discussion'" class="content-section">
          <h2>讨论管理</h2>
          <div class="table-toolbar">
            <el-button type="success" @click="openDiscussionDialog"><el-icon><Plus /></el-icon> 创建讨论</el-button>
            <el-button @click="loadDiscussions"><el-icon><Refresh /></el-icon> 刷新</el-button>
          </div>

          <el-table :data="discussionList" stripe>
            <el-table-column prop="title" label="讨论标题" min-width="200" />
            <el-table-column prop="code" label="讨论码" width="140" />
            <el-table-column label="绑定发言" width="110">
              <template #default="{ row }">{{ row.boundStudentCommentCount || 0 }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 'IN_PROGRESS' ? 'success' : row.status === 'ENDED' ? 'info' : 'warning'">
                  {{ row.status === 'IN_PROGRESS' ? '进行中' : row.status === 'ENDED' ? '已结束' : '未开始' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="高频词" min-width="220">
              <template #default="{ row }">
                <div v-if="row.status === 'ENDED'">
                  <el-tag v-for="w in (discussionHotWords[row.id] || [])" :key="w.word" size="small" style="margin-right:6px; margin-bottom:6px;">{{ w.word }}</el-tag>
                  <span v-if="(discussionHotWords[row.id] || []).length === 0" style="color:#909399;">无</span>
                </div>
                <span v-else style="color:#909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="280">
              <template #default="{ row }">
                <div class="action-cell">
                  <el-button type="text" @click="viewDiscussion(row)">详情</el-button>
                  <el-button type="text" @click="copyText(row.code)">复制讨论码</el-button>
                  <el-button v-if="row.status === 'ENDED'" type="text" @click="exportTeacherSpeechPdf(row)">导出教师发言PDF</el-button>
                  <el-button type="text" class="danger" @click="removeDiscussion(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-dialog v-model="discussionCreateVisible" title="创建讨论" width="720px">
            <el-form :model="discussionCreateForm" label-width="90px">
              <el-form-item label="讨论标题" required>
                <el-input v-model="discussionCreateForm.title" placeholder="请输入讨论标题" />
              </el-form-item>
              <el-form-item label="讨论简介">
                <el-input v-model="discussionCreateForm.description" type="textarea" :rows="3" placeholder="请输入讨论简介" />
              </el-form-item>
              <el-form-item label="教师素材">
                <el-select v-model="discussionCreateForm.teacherMaterialIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%">
                  <el-option v-for="m in myMaterials" :key="m.id" :label="m.title" :value="m.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="人物事迹">
                <el-select v-model="discussionCreateForm.leaderMaterialIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%" :loading="discussionSourceLoading">
                  <el-option v-for="m in leaderMaterials" :key="m.id" :label="m.title" :value="m.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="新闻资讯">
                <el-select v-model="discussionCreateForm.newsIds" multiple filterable collapse-tags :max-collapse-tags="3" placeholder="可选" style="width: 100%" :loading="discussionSourceLoading">
                  <el-option v-for="n in publishedNews" :key="n.id" :label="n.title" :value="n.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="截止时间">
                <el-date-picker
                  v-model="discussionCreateForm.endTime"
                  type="datetime"
                  placeholder="可选" style="width: 100%"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
              <div class="form-tip">至少选择一个素材/人物/新闻，截止时间到后不可再评论</div>
            </el-form>
            <template #footer>
              <el-button @click="discussionCreateVisible = false">取消</el-button>
              <el-button type="primary" :loading="discussionCreating" @click="submitCreateDiscussion">创建</el-button>
            </template>
          </el-dialog>
        </div>

        <div v-if="activeMenu === 'student'" class="content-section">
          <h2>学生管理</h2>

          <div class="bind-code-section">
            <h3>绑定码</h3>
            <div class="bind-code-box">
              <span class="bind-code">{{ bindCode || '-' }}</span>
              <el-button size="small" type="primary" @click="copyText(bindCode)" :disabled="!bindCode">复制</el-button>
              <el-button size="small" @click="regenerateCode">重新生成</el-button>
            </div>
            <div class="bind-tip">学生输入绑定码即可绑定到你</div>
          </div>

          <el-table :data="bindingStudents" stripe>
            <el-table-column prop="studentRealName" label="学生姓名" width="140" />
            <el-table-column prop="studentName" label="账号" width="160" />
            <el-table-column prop="studentMajor" label="专业" />
            <el-table-column label="绑定时间" width="180">
              <template #default="{ row }">{{ formatTime(row.bindTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  plain
                  size="small"
                  :loading="String(unbindingStudentId) === String(row.studentId)"
                  :disabled="!row || !row.studentId || !user?.id"
                  @click="handleUnbindStudent(row)"
                >解绑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  ArrowDown,
  ArrowUp,
  Back,
  ChatDotRound,
  DataAnalysis,
  Document,
  List,
  Plus,
  Refresh,
  User,
  UserFilled
} from '@element-plus/icons-vue'

import { updateAvatar, updatePassword, updateUsername, updatePhone } from '../../api/auth'
import { getBindCode, getBindingCount, getBindingStudents, regenerateBindCode, unbindStudent } from '../../api/bind'
import { createDiscussion, deleteDiscussion, getDiscussionsByTeacher, getDiscussionStats, getDiscussionHotWords, downloadTeacherSpeechOnlyPdf } from '../../api/discussion'
import { getTeacherBoundParticipation } from '../../api/learning'
import { createMaterial, createTag, deleteMaterial, getAllTags, getMaterialById, getMaterialsByTeacher } from '../../api/material'
import { generateMaterialQuizzes, generateTaskQuiz } from '../../api/ai'
import { createTask, deleteTask, getTasksByTeacher, getTaskStats } from '../../api/task'
import { getLeaderList, getPublishedNews } from '../../api/public'
import { uploadResourceFile } from '../../utils/upload'


const router = useRouter()

const user = ref({})

const taskCreateVisible = ref(false)
const taskCreating = ref(false)
const taskQuizGenerating = ref(false)
const taskCreateForm = reactive({
  title: '',
  description: '',
  endTime: '',
  teacherMaterialIds: [],
  leaderMaterialIds: [],
  newsIds: [],
  quizJson: '',
  quizCount: 4,
  viewpointText: '',
  checkinRequiredMinutes: 0
})
const activeMenu = ref('dashboard')

const stats = reactive({ taskCount: 0, discussionCount: 0, studentCount: 0 })

const taskList = ref([])
const discussionList = ref([])
const discussionHotWords = reactive({})
const discussionExpanded = ref(false)
const taskExpanded = ref(false)

const displayTaskList = computed(() => (taskExpanded.value ? taskList.value : taskList.value.slice(0, 5)))
const displayDiscussionList = computed(() => (discussionExpanded.value ? discussionList.value : discussionList.value.slice(0, 5)))

const bindCode = ref('')
const bindingStudents = ref([])
const unbindingStudentId = ref(null)

const participationQuery = reactive({ code: '', type: '' })
const participationLoading = ref(false)
const participationList = ref([])
const participationTotal = ref(0)
const participationPage = ref(1)
const participationPageSize = ref(20)
const participationMeta = reactive({ targetTitle: '', type: '' })
const participationSummary = ref(null)

const materialTab = ref('upload')
const myMaterials = ref([])
const materialDetailVisible = ref(false)
const materialDetail = reactive({})

const allTags = ref([])
const tagCategories = ref([])
const baseCategories = ref(['思政', '工程伦理', '法治', '安全', '创新', '其他'])
const tagFilterCategory = ref('')
const newTagName = ref('')
const newTagCategory = ref('')

const tagOptions = computed(() => {
  if (!tagFilterCategory.value) return allTags.value
  return allTags.value.filter((t) => String(t.category || '') === String(tagFilterCategory.value))
})

const materialForm = reactive({
  type: 'IMAGE',
  title: '',
  description: '',
  content: '',
  tagIds: [],
  fileUrl: '',
  uploadFile: null
})
const uploadLoading = ref(false)

const avatarUrl = ref('')
const avatarFile = ref(null)
const avatarLoading = ref(false)

const profileForm = reactive({ username: '', realName: '', major: '', staffNo: '', phone: '' })
const saveLoading = ref(false)

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordFormRef = ref(null)
const resetLoading = ref(false)
const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) return callback()
        if (value !== passwordForm.newPassword) return callback(new Error('两次输入的密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
})

const discussionCreateVisible = ref(false)
const discussionCreating = ref(false)
const discussionCreateForm = reactive({
  title: '',
  description: '',
  endTime: '',
  teacherMaterialIds: [],
  leaderMaterialIds: [],
  newsIds: []
})

const discussionSourceLoading = ref(false)
const publishedNews = ref([])
const leaderMaterials = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return String(time).replace('T', ' ').substring(0, 19)
}

const formatDuration = (seconds) => {
  const s = Number(seconds || 0)
  if (!Number.isFinite(s) || s <= 0) return ''
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const r = Math.floor(s % 60)
  if (h > 0) return `${h}h${m}m${r}s`
  if (m > 0) return `${m}m${r}s`
  return `${r}s`
}

const formatQuiz = (correct, total) => {
  const t = Number(total || 0)
  const c = Number(correct || 0)
  if (!Number.isFinite(t) || t <= 0) return '-'
  return `${c}/${t}`
}

const formatMaterialProgress = (row) => {
  const total = Number(row?.taskTotalMaterials || 0)
  const done = Number(row?.taskCompletedMaterials || 0)
  if (!Number.isFinite(total) || total <= 0) return '-'
  return `${done}/${total}`
}

const formatTaskParticipation = (row) => {
  if (!row) return { text: '-', tagType: 'info' }
  if (row.checkedIn) return { text: '已打卡', tagType: 'success' }
  if (row.enterTime) return { text: '已参与', tagType: 'warning' }
  return { text: '未参与', tagType: 'info' }
}

const copyTextByExecCommand = (text) => {
  const temporaryTextarea = document.createElement('textarea')
  temporaryTextarea.value = String(text)
  temporaryTextarea.setAttribute('readonly', 'readonly')
  temporaryTextarea.style.position = 'fixed'
  temporaryTextarea.style.top = '-9999px'
  temporaryTextarea.style.left = '-9999px'
  document.body.appendChild(temporaryTextarea)
  temporaryTextarea.focus()
  temporaryTextarea.select()
  temporaryTextarea.setSelectionRange(0, temporaryTextarea.value.length)
  try {
    return document.execCommand('copy')
  } finally {
    document.body.removeChild(temporaryTextarea)
  }
}

const copyText = async (text) => {
  if (!text) {
    ElMessage.warning('无可复制内容')
    return
  }
  try {
    const textToCopy = String(text)
    if (window.isSecureContext && navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
      await navigator.clipboard.writeText(textToCopy)
    } else {
      const copied = copyTextByExecCommand(textToCopy)
      if (!copied) {
        throw new Error('copy failed')
      }
    }
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

const goHome = () => {
  router.push('/teacher/home')
}

const extractCount = (val) => {
  if (val == null) return 0
  if (typeof val === 'number') return Number.isFinite(val) ? val : 0
  if (typeof val === 'string') {
    const n = Number(val)
    return Number.isFinite(n) ? n : 0
  }
  if (typeof val === 'object') {
    const obj = val
    const keys = ['count', 'total', 'taskCount', 'discussionCount', 'studentCount', 'bindingCount']
    for (const k of keys) {
      if (obj[k] != null) return extractCount(obj[k])
    }
  }
  return 0
}

const loadTags = async () => {
  try {
    const res = await getAllTags()
    if (res?.data?.code === 200) {
      const list = (res.data.data || []).map((t) => ({ ...t, category: t.category || '其他' }))
      allTags.value = list
      tagCategories.value = Array.from(new Set(list.map((t) => t.category))).filter(Boolean)
    }
  } catch {
    allTags.value = []
    tagCategories.value = []
  }
}

const addNewTag = async () => {
  const name = String(newTagName.value || '').trim()
  const cat = String(newTagCategory.value || '').trim()
  if (!name) {
    ElMessage.warning('请输入新标签')
    return
  }
  if (!cat) {
    ElMessage.warning('请选择标签大类')
    return
  }
  try {
    const res = await createTag(name, cat)
    if (res?.data?.code === 200) {
      ElMessage.success('标签添加成功')
      newTagName.value = ''
      newTagCategory.value = ''
      await loadTags()
    } else {
      ElMessage.error(res?.data?.message || '添加失败')
    }
  } catch {
    ElMessage.error('添加失败')
  }
}

const handleFileChange = (file) => {
  const f = file && file.raw
  if (!f) return
  const expectedPrefix = materialForm.type === 'IMAGE' ? 'image/' : 'video/'
  const invalidMessage = materialForm.type === 'IMAGE' ? '请选择图片文件' : '请选择视频文件'
  if (!String(f.type || '').startsWith(expectedPrefix)) {
    ElMessage.warning(invalidMessage)
    return
  }
  materialForm.fileUrl = URL.createObjectURL(f)
  materialForm.uploadFile = f
}

const resetMaterialForm = () => {
  materialForm.type = 'IMAGE'
  materialForm.title = ''
  materialForm.description = ''
  materialForm.content = ''
  materialForm.tagIds = []
  materialForm.fileUrl = ''
  materialForm.uploadFile = null
}

const submitMaterial = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  if (!String(materialForm.title || '').trim()) {
    ElMessage.warning('请输入素材标题')
    return
  }
  if (materialForm.type !== 'ARTICLE' && !materialForm.uploadFile && !materialForm.fileUrl) {
    ElMessage.warning('请上传文件')
    return
  }
  if (materialForm.type === 'ARTICLE' && !String(materialForm.content || '').trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  uploadLoading.value = true
  try {
    const payload = { ...materialForm, teacherId: user.value.id }
    if (materialForm.type !== 'ARTICLE' && materialForm.uploadFile) {
      payload.fileUrl = await uploadResourceFile(materialForm.uploadFile, 'material')
    }
    delete payload.uploadFile

    const res = await createMaterial(payload)
    if (res?.data?.code === 200) {
      ElMessage.success('提交成功')
      resetMaterialForm()
      await loadMaterials()
    } else {
      ElMessage.error(res?.data?.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    uploadLoading.value = false
  }
}

const loadMaterials = async () => {
  if (!user.value || !user.value.id) return
  try {
    const res = await getMaterialsByTeacher(user.value.id)
    if (res?.data?.code === 200) myMaterials.value = res.data.data || []
  } catch {
    myMaterials.value = []
  }
}

const viewMaterial = async (row) => {
  try {
    const res = await getMaterialById(row.id)
    if (res?.data?.code === 200) {
      Object.assign(materialDetail, res.data.data || {})
      materialDetailVisible.value = true
    } else {
      ElMessage.error(res?.data?.message || '加载失败')
    }
  } catch {
    ElMessage.error('加载失败')
  }
}

const removeMaterial = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该素材？', '提示', { type: 'warning' })
    const res = await deleteMaterial(row.id)
    if (res?.data?.code === 200) {
      ElMessage.success('删除成功')
      await loadMaterials()
    } else {
      ElMessage.error(res?.data?.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const openTaskDialog = () => {
  activeMenu.value = 'task'
  taskCreateForm.title = ''
  taskCreateForm.description = ''
  taskCreateForm.endTime = ''
  taskCreateForm.teacherMaterialIds = []
  taskCreateForm.leaderMaterialIds = []
  taskCreateForm.newsIds = []
  taskCreateForm.quizJson = ''
  taskCreateForm.quizCount = 4
  taskCreateForm.viewpointText = ''
  taskCreateForm.checkinRequiredMinutes = 0
  taskCreateVisible.value = true
  Promise.all([loadMaterials(), loadDiscussionSources()])
}

const normalizeQuizCount = () => {
  const n = Number(taskCreateForm.quizCount)
  if (!Number.isFinite(n)) return 4
  if (n < 3) return 3
  if (n > 5) return 5
  return Math.floor(n)
}

const generateTaskQuizForCreate = async (mode) => {
  const materialIds = Array.from(
    new Set([...(taskCreateForm.teacherMaterialIds || []), ...(taskCreateForm.leaderMaterialIds || [])])
  )
  const newsIds = (taskCreateForm.newsIds || []).slice()
  if (materialIds.length === 0 && newsIds.length === 0) {
    ElMessage.warning('请至少选择一个素材或新闻')
    return
  }
  taskQuizGenerating.value = true
  try {
    const count = normalizeQuizCount()
    if (mode === 'PER_MATERIAL') {
      if (materialIds.length === 0) {
        ElMessage.warning('按素材生成题目需要至少选择一个素材')
        return
      }
      const res = await generateMaterialQuizzes({
        title: String(taskCreateForm.title || '').trim(),
        description: taskCreateForm.description || '',
        materialIds,
        count
      })
      if (res?.data?.code === 200) {
        taskCreateForm.quizJson = JSON.stringify(res.data.data || {}, null, 2)
        ElMessage.success('题目已生成')
      } else {
        ElMessage.error(res?.data?.message || '生成题目失败')
      }
    } else {
      const res = await generateTaskQuiz({
        title: String(taskCreateForm.title || '').trim(),
        description: taskCreateForm.description || '',
        materialIds,
        newsIds,
        count
      })
      if (res?.data?.code === 200) {
        const payload = res.data.data || {}
        if (payload && !payload.mode) payload.mode = 'FLAT'
        taskCreateForm.quizJson = JSON.stringify(payload, null, 2)
        ElMessage.success('题目已生成')
      } else {
        ElMessage.error(res?.data?.message || '生成题目失败')
      }
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '生成题目失败')
  } finally {
    taskQuizGenerating.value = false
  }
}

const parseViewpointOptions = () => {
  const raw = String(taskCreateForm.viewpointText || '')
  const list = raw
    .split(/\r?\n/)
    .map((s) => String(s || '').trim())
    .filter((s) => !!s)
  return Array.from(new Set(list))
}

const taskQuizPreview = computed(() => {
  const raw = String(taskCreateForm.quizJson || '').trim()
  if (!raw) return null
  try {
    const obj = JSON.parse(raw)
    const mode = obj && obj.mode ? String(obj.mode).toUpperCase() : 'FLAT'
    if (mode === 'PER_MATERIAL') {
      const materials = Array.isArray(obj.materials) ? obj.materials : []
      return {
        mode: 'PER_MATERIAL',
        materials: materials.map((m) => ({
          materialId: m && m.materialId != null ? String(m.materialId) : '',
          materialTitle: m && m.materialTitle ? m.materialTitle : '',
          questions: Array.isArray(m && m.questions ? m.questions : []) ? m.questions : []
        }))
      }
    }
    const questions = Array.isArray(obj && obj.questions ? obj.questions : []) ? obj.questions : []
    return { mode: 'FLAT', questions }
  } catch {
    return null
  }
})

const submitCreateTask = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  if (!taskCreateForm.title || !String(taskCreateForm.title).trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }

  const materialIds = Array.from(
    new Set([...(taskCreateForm.teacherMaterialIds || []), ...(taskCreateForm.leaderMaterialIds || [])])
  )
  const newsIds = (taskCreateForm.newsIds || []).slice()
  if (materialIds.length === 0 && newsIds.length === 0) {
    ElMessage.warning('请至少选择一个素材或新闻')
    return
  }

  taskCreating.value = true
  try {
    const payload = {
      teacherId: user.value.id,
      title: String(taskCreateForm.title).trim(),
      description: taskCreateForm.description || ''
    }
    if (taskCreateForm.endTime) payload.endTime = taskCreateForm.endTime
    if (materialIds.length > 0) payload.materialIds = materialIds
    if (newsIds.length > 0) payload.newsIds = newsIds

    if (taskCreateForm.quizJson && String(taskCreateForm.quizJson).trim()) {
      payload.quizJson = String(taskCreateForm.quizJson)
    }
    const vps = parseViewpointOptions()
    if (vps.length > 0) {
      payload.viewpointOptionsJson = JSON.stringify(vps)
    }
    const mins = Number(taskCreateForm.checkinRequiredMinutes || 0)
    if (Number.isFinite(mins) && mins > 0) {
      payload.checkinRequiredSeconds = Math.floor(mins * 60)
    }

    const res = await createTask(payload)
    if (res?.data?.code === 200) {
      ElMessage.success('创建成功')
      taskCreateVisible.value = false
      await loadTasks()
      await loadStats()
    } else {
      ElMessage.error(res?.data?.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '创建失败')
  } finally {
    taskCreating.value = false
  }
}

const loadDiscussionSources = async () => {
  discussionSourceLoading.value = true
  try {
    const [newsRes, leaderRes] = await Promise.all([getPublishedNews(), getLeaderList()])
    publishedNews.value = newsRes?.data?.code === 200 ? newsRes.data.data || [] : []
    leaderMaterials.value = leaderRes?.data?.code === 200 ? leaderRes.data.data || [] : []
  } catch {
    publishedNews.value = []
    leaderMaterials.value = []
  } finally {
    discussionSourceLoading.value = false
  }
}

const openDiscussionDialog = async () => {
  activeMenu.value = 'discussion'
  discussionCreateForm.title = ''
  discussionCreateForm.description = ''
  discussionCreateForm.endTime = ''
  discussionCreateForm.teacherMaterialIds = []
  discussionCreateForm.leaderMaterialIds = []
  discussionCreateForm.newsIds = []
  discussionCreateVisible.value = true
  await Promise.all([loadMaterials(), loadDiscussionSources()])
}

const submitCreateDiscussion = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  if (!discussionCreateForm.title || !String(discussionCreateForm.title).trim()) {
    ElMessage.warning('请输入讨论标题')
    return
  }

  const materialIds = Array.from(
    new Set([...(discussionCreateForm.teacherMaterialIds || []), ...(discussionCreateForm.leaderMaterialIds || [])])
  )
  const newsIds = (discussionCreateForm.newsIds || []).slice()
  if (materialIds.length === 0 && newsIds.length === 0) {
    ElMessage.warning('请至少选择一个素材或新闻')
    return
  }

  discussionCreating.value = true
  try {
    const payload = {
      teacherId: user.value.id,
      title: String(discussionCreateForm.title).trim(),
      description: discussionCreateForm.description || ''
    }
    if (discussionCreateForm.endTime) payload.endTime = discussionCreateForm.endTime
    if (materialIds.length > 0) payload.materialIds = materialIds
    if (newsIds.length > 0) payload.newsIds = newsIds

    const res = await createDiscussion(payload)
    if (res?.data?.code === 200) {
      ElMessage.success('创建成功')
      discussionCreateVisible.value = false
      const created = res?.data?.data
      if (created && created.id) {
        discussionList.value = [created, ...(discussionList.value || [])]
      }
      await loadDiscussions()
      await loadStats()
    } else {
      ElMessage.error(res?.data?.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '创建失败')
  } finally {
    discussionCreating.value = false
  }
}

const loadTasks = async () => {
  if (!user.value || !user.value.id) return
  try {
    const res = await getTasksByTeacher(user.value.id)
    if (res?.data?.code === 200) taskList.value = res.data.data || []
  } catch {
    taskList.value = []
  }
}

const loadDiscussionHotWords = async () => {
  if (!user.value || !user.value.id) return
  const ended = (discussionList.value || []).filter((d) => d && d.id && d.status === 'ENDED')
  if (ended.length === 0) return
  try {
    await Promise.all(
      ended.map(async (d) => {
        const res = await getDiscussionHotWords(user.value.id, d.id, 10)
        if (res?.data?.code === 200) discussionHotWords[d.id] = res.data.data || []
        else discussionHotWords[d.id] = []
      })
    )
  } catch {
  }
}

const loadDiscussions = async () => {
  if (!user.value || !user.value.id) return
  try {
    const res = await getDiscussionsByTeacher(user.value.id)
    if (res?.data?.code === 200) {
      discussionList.value = res.data.data || []
      await loadDiscussionHotWords()
    } else {
      discussionList.value = []
      ElMessage.error(res?.data?.message || '获取讨论列表失败')
    }
  } catch {
    discussionList.value = []
    ElMessage.error('获取讨论列表失败')
  }
}

const loadStudents = async () => {
  if (!user.value || !user.value.id) return
  try {
    const res = await getBindingStudents(user.value.id)
    if (res?.data?.code === 200) bindingStudents.value = res.data.data || []
  } catch {
    bindingStudents.value = []
  }
}

const loadBindCode = async () => {
  if (!user.value || !user.value.id) return
  try {
    const res = await getBindCode(user.value.id)
    if (res?.data?.code === 200) bindCode.value = res.data.data || ''
  } catch {
    bindCode.value = ''
  }
}

const loadStats = async () => {
  if (!user.value || !user.value.id) return
  try {
    const [taskRes, discRes, stuRes] = await Promise.all([getTaskStats(user.value.id), getDiscussionStats(user.value.id), getBindingCount(user.value.id)])
    if (taskRes?.data?.code === 200) stats.taskCount = extractCount(taskRes.data.data)
    if (discRes?.data?.code === 200) stats.discussionCount = extractCount(discRes.data.data)
    if (stuRes?.data?.code === 200) stats.studentCount = extractCount(stuRes.data.data)
  } catch {
  }
}

const refreshAll = async () => {
  await Promise.all([loadStats(), loadTasks(), loadDiscussions(), loadMaterials(), loadTags(), loadStudents(), loadBindCode()])
}

const searchParticipation = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  if (!String(participationQuery.code || '').trim()) {
    ElMessage.warning('请输入任务码/讨论码')
    return
  }
  participationLoading.value = true
  try {
    const res = await getTeacherBoundParticipation({
      teacherId: user.value.id,
      code: String(participationQuery.code).trim(),
      type: participationQuery.type || undefined,
      page: participationPage.value,
      pageSize: participationPageSize.value
    })
    if (res?.data?.code === 200) {
      const data = res.data.data || {}
      participationMeta.type = data.type || ''
      participationMeta.targetTitle = data.targetTitle || ''
      participationTotal.value = Number(data.total || 0)
      participationList.value = data.list || []
      participationSummary.value = data.summary || null
    } else {
      ElMessage.error(res?.data?.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '查询失败')
  } finally {
    participationLoading.value = false
  }
}

const resetParticipation = async () => {
  participationQuery.code = ''
  participationQuery.type = ''
  participationPage.value = 1
  participationList.value = []
  participationTotal.value = 0
  participationMeta.type = ''
  participationMeta.targetTitle = ''
  participationSummary.value = null
}

const handleParticipationPageChange = async (p) => {
  participationPage.value = p
  await searchParticipation()
}

const handleParticipationPageSizeChange = async (ps) => {
  participationPageSize.value = ps
  participationPage.value = 1
  await searchParticipation()
}

const viewTask = (row) => router.push(`/teacher/task/${row.id}`)
const viewDiscussion = (row) => router.push(`/teacher/discussion/${row.id}`)

const removeTask = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
    const res = await deleteTask(row.id)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await refreshAll()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const removeDiscussion = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该讨论？', '提示', { type: 'warning' })
    const res = await deleteDiscussion(row.id)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await refreshAll()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const exportTeacherSpeechPdf = async (row) => {
  if (!row || !row.id) return
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await downloadTeacherSpeechOnlyPdf(user.value.id, row.id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `discussion-teacher-speech-${row.id}.pdf`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '导出失败'
    ElMessage.error(msg)
  }
}

const regenerateCode = async () => {
  if (!user.value.id) return
  try {
    const res = await regenerateBindCode(user.value.id)
    if (res.data.code === 200) {
      bindCode.value = res.data.data
      ElMessage.success('已生成新绑定码')
      await loadStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch {
    ElMessage.error('生成失败')
  }
}

const handleUnbindStudent = async (row) => {
  if (!row || !row.studentId) {
    ElMessage.warning('学生信息不完整')
    return
  }
  if (!user.value || !user.value.id) {
    ElMessage.warning('请先登录')
    return
  }

  const studentName = row.studentRealName || row.studentName || ''
  try {
    await ElMessageBox.confirm(
      `确定解绑${studentName ? '该学生：' + studentName : '该学生'}？`,
      '提示',
      { type: 'warning' }
    )

    unbindingStudentId.value = row.studentId
    const res = await unbindStudent(user.value.id, row.studentId)
    if (res?.data?.code === 200) {
      ElMessage.success('解绑鎴愬姛')
      await loadStudents()
      await loadStats()
    } else {
      ElMessage.error(res?.data?.message || '解绑澶辫触')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('解绑澶辫触')
    }
  } finally {
    if (String(unbindingStudentId.value) === String(row.studentId)) unbindingStudentId.value = null
  }
}

const handleMenuSelect = async (index) => {
  activeMenu.value = index
  if (index === 'material') await loadMaterials()
  if (index === 'task') await loadTasks()
  if (index === 'discussion') await loadDiscussions()
  if (index === 'student') {
    await loadStudents()
    await loadBindCode()
  }
  if (index === 'dashboard') await loadStats()
}

const handleAvatarChange = (file) => {
  const rawFile = file && file.raw ? file.raw : null
  if (!rawFile) return
  if (!String(rawFile.type || '').startsWith('image/')) {
    ElMessage.warning('只能上传图片文件')
    return
  }
  if (Number(rawFile.size || 0) > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过2MB')
    return
  }
  avatarFile.value = rawFile
  avatarUrl.value = URL.createObjectURL(rawFile)
}

const saveAvatar = async () => {
  if (!avatarFile.value && !avatarUrl.value) {
    ElMessage.warning('请先选择头像')
    return
  }
  avatarLoading.value = true
  try {
    const avatarValue = avatarFile.value
      ? await uploadResourceFile(avatarFile.value, 'avatar')
      : avatarUrl.value
    const res = await updateAvatar({ userId: user.value.id, avatar: avatarValue, role: 'TEACHER' })
    if (res.data.code === 200) {
      ElMessage.success('头像修改成功')
      user.value.avatar = avatarValue
      avatarUrl.value = avatarValue
      avatarFile.value = null
      localStorage.setItem('user', JSON.stringify(user.value))
    } else {
      ElMessage.error(res.data.message)
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    avatarLoading.value = false
  }
}

const saveProfile = async () => {
  if (!profileForm.username.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }

  if (!profileForm.phone || !/^\d{11}$/.test(String(profileForm.phone).trim())) {
    ElMessage.warning('手机号必须为11位数字')
    return
  }

  saveLoading.value = true
  try {
    const res = await updateUsername({ userId: user.value.id, newUsername: profileForm.username, role: 'TEACHER' })
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message)
      return
    }

    const phoneRes = await updatePhone({ userId: user.value.id, phone: profileForm.phone, role: 'TEACHER' })
    if (phoneRes.data.code !== 200) {
      ElMessage.error(phoneRes.data.message)
      return
    }

    ElMessage.success('保存成功')
    user.value.username = profileForm.username
    user.value.phone = profileForm.phone
    localStorage.setItem('user', JSON.stringify(user.value))
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const resetPassword = async () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetLoading.value = true
    try {
      const res = await updatePassword({
        userId: user.value.id,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        role: 'TEACHER'
      })
      if (res.data.code === 200) {
        ElMessage.success('密码重置成功，请重新登录')
        localStorage.removeItem('user')
        router.push('/login')
      } else {
        ElMessage.error(res.data.message)
      }
    } catch {
      ElMessage.error('重置失败')
    } finally {
      resetLoading.value = false
    }
  })
}

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    profileForm.username = user.value.username || ''
    profileForm.realName = user.value.realName || user.value.real_name || ''
    profileForm.major = user.value.major || ''
    profileForm.staffNo = user.value.staffNo || ''
    profileForm.phone = user.value.phone || ''
    avatarUrl.value = user.value.avatar || ''
  } else {
    router.push('/login')
    return
  }

  refreshAll()
})


</script>

<style scoped>
  .manage-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  .topbar {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    background: #001529;
    color: #fff;
  }

  .topbar-title {
    color: #fff;
    font-size: 18px;
    font-weight: 600;
  }

  .home-back-btn {
    color: rgba(255, 255, 255, 0.9) !important;
  }

  .home-back-btn :deep(.el-icon) {
    color: rgba(255, 255, 255, 0.9) !important;
  }

  .home-back-btn:hover { color: #bae0ff !important; }

  .layout-body { display: flex; flex: 1; min-height: 0; }
  .sidebar {
    width: 220px;
    flex: 0 0 220px;
    background: #001529;
    border-right: 1px solid rgba(255, 255, 255, 0.2);
    overflow: auto;
  }
  .sidebar-menu {
    height: 100%;
    border-right: none;
    background: transparent;
    --el-menu-bg-color: transparent;
    --el-menu-text-color: rgba(255, 255, 255, 0.9);
    --el-menu-active-color: #ffffff;
    --el-menu-hover-bg-color: rgba(255, 255, 255, 0.14);
  }
  .sidebar-menu :deep(.el-menu-item) { color: rgba(255, 255, 255, 0.9); }
  .sidebar-menu :deep(.el-menu-item .el-icon) { color: rgba(255, 255, 255, 0.9); }
  .sidebar-menu :deep(.el-menu-item.is-active) {
    background: rgba(255, 255, 255, 0.18);
    color: #fff;
  }
  .sidebar-menu :deep(.el-menu-item.is-active .el-icon) { color: #fff; }

  .main-area { flex: 1; min-width: 0; padding: 20px; overflow: auto; }

  @media (max-width: 768px) {
    .sidebar { width: 180px; flex-basis: 180px; }
    .main-area { padding: 12px; }
  }

  @media (max-width: 480px) {
    .sidebar { width: 160px; flex-basis: 160px; }
    .main-area { padding: 10px; }
  }

  .content-section { background: #fff; padding: 20px; border-radius: 8px; }
  .content-section h2 { margin: 0 0 20px 0; color: #333; border-bottom: 2px solid #c41230; padding-bottom: 10px; }
  .content-section h3 { margin: 20px 0 15px 0; color: #666; }

  .stat-cards { margin-bottom: 30px; }
  .stat-card { background: #f0f9ff; padding: 20px; border-radius: 8px; text-align: center; }
  .stat-card.warning { background: #fff7e6; }
  .stat-card.warning .stat-value { color: #fa8c16; }

  .quick-actions { margin-bottom: 30px; }
  .action-btn { width: 100%; height: 60px; font-size: 16px; }
  .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
  .section-header h3 { margin: 0; color: #666; }
  .profile-form, .password-form, .setting-form { max-width: 500px; }
  .avatar-section { text-align: center; margin-bottom: 30px; }
  .avatar-uploader { cursor: pointer; display: inline-block; }
  .avatar-preview { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; }
  .avatar-placeholder { width: 100px; height: 100px; border-radius: 50%; background: #f5f7fa; border: 2px dashed #dcdfe6; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; font-size: 12px; }
  .avatar-placeholder .el-icon { font-size: 24px; margin-bottom: 5px; }
  .material-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
  .material-card { cursor: pointer; }
  .material-card h4 { margin: 0 0 10px 0; }
  .material-card p { color: #666; font-size: 14px; }
  .card-footer { margin-top: 15px; display: flex; justify-content: space-between; }
  .table-toolbar { margin-bottom: 15px; }
  .search-bar { margin-bottom: 20px; }
  .bind-code-section { margin-bottom: 20px; }
  .bind-code-box { display: flex; align-items: center; gap: 10px; margin: 10px 0; }
  .bind-code { font-size: 24px; font-weight: bold; color: #409eff; background: #ecf5ff; padding: 10px 20px; border-radius: 4px; letter-spacing: 2px; }
  .bind-tip { color: #909399; font-size: 13px; }
  .danger { color: #f56c6c; }
  .action-cell { display: flex; flex-wrap: wrap; gap: 6px 12px; align-items: center; }
  .material-form { max-width: 700px; }
  .file-preview { margin-top: 10px; }
  .preview-img { max-width: 300px; max-height: 200px; border-radius: 4px; }
  .preview-video { max-width: 400px; max-height: 250px; border-radius: 4px; }
  .upload-tip { color: #909399; font-size: 12px; margin-top: 5px; }
  .form-tip { color: #909399; font-size: 12px; margin-top: 5px; }
  .material-detail .detail-row { margin-bottom: 15px; }
  .material-detail .label { font-weight: bold; color: #606266; margin-right: 10px; }
  .material-detail .detail-content { margin-top: 15px; }
  .material-detail .article-content { margin-top: 10px; padding: 15px; background: #f5f7fa; border-radius: 4px; white-space: pre-wrap; max-height: 300px; overflow-y: auto; }
  .material-detail .detail-img { max-width: 100%; max-height: 400px; margin-top: 10px; border-radius: 4px; }
  .material-detail .detail-video { max-width: 100%; max-height: 400px; margin-top: 10px; border-radius: 4px; }
  .selected-materials { display: flex; flex-wrap: wrap; padding: 10px; background: #f5f7fa; border-radius: 4px; min-height: 40px; }
</style>






