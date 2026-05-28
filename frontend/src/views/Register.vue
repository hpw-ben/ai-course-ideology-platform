<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">教学平台注册</h2>
      
      <div class="role-switch">
        <el-radio-group v-model="form.role" size="large">
          <el-radio-button label="STUDENT">学生端</el-radio-button>
          <el-radio-button label="TEACHER">教师端</el-radio-button>
        </el-radio-group>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <!-- 头像上传 -->
        <el-form-item class="avatar-item">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
            <div v-else class="avatar-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传头像</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账户昵称" size="large" :prefix-icon="User"></el-input>
        </el-form-item>

        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" :prefix-icon="Postcard"></el-input>
        </el-form-item>

        <el-form-item v-if="form.role === 'STUDENT'" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入10位学号" size="large" :prefix-icon="Postcard"></el-input>
        </el-form-item>

        <el-form-item v-if="form.role === 'TEACHER'" prop="staffNo">
          <el-input v-model="form.staffNo" placeholder="请输入10位职工号" size="large" :prefix-icon="Postcard"></el-input>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" :prefix-icon="User"></el-input>
        </el-form-item>

        <el-form-item prop="major">
          <el-select v-model="form.major" placeholder="请选择所属专业" size="large" style="width: 100%">
            <el-option v-for="m in majorOptions" :key="m" :label="m" :value="m"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password :prefix-icon="Lock"></el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" size="large" show-password :prefix-icon="Lock"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Postcard, Plus } from '@element-plus/icons-vue'
import { register } from '../api/auth'
import { getAllTags } from '../api/material'
import { uploadResourceFile } from '../utils/upload'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const avatarUrl = ref('')
    const avatarFile = ref(null)
    const majorOptions = ref([])

    const form = reactive({
      username: '',
      realName: '',
      studentNo: '',
      staffNo: '',
      phone: '',
      password: '',
      confirmPassword: '',
      role: 'STUDENT',
      major: '',
      avatar: ''
    })

    watch(
      () => form.role,
      (r) => {
        if (r === 'STUDENT') {
          form.staffNo = ''
        } else if (r === 'TEACHER') {
          form.studentNo = ''
        }
      }
    )

    const loadMajorOptions = async () => {
      try {
        const res = await getAllTags()
        if (res.data.code === 200) {
          const names = (res.data.data || [])
            .map(t => (t && t.name ? String(t.name).trim() : ''))
            .filter(Boolean)
          majorOptions.value = Array.from(new Set(names)).sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'))
        }
      } catch (e) {
        console.error('加载专业选项失败:', e)
      }
    }

    onMounted(() => {
      loadMajorOptions()
    })

    const handleAvatarChange = (file) => {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.raw.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('只能上传图片文件')
        return
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB')
        return
      }

      avatarFile.value = file.raw
      avatarUrl.value = URL.createObjectURL(file.raw)
    }

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 8) {
        callback(new Error('密码长度不能少于8位'))
      } else if (value.length > 20) {
        callback(new Error('密码长度不能超过20位'))
      } else if (!/[A-Za-z]/.test(value)) {
        callback(new Error('密码必须包含字母'))
      } else if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
        callback(new Error('密码必须包含特殊符号'))
      } else {
        callback()
      }
    }

    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const validateStudentNo = (rule, value, callback) => {
      if (form.role !== 'STUDENT') return callback()
      if (!value) return callback(new Error('请输入10位学号'))
      if (!/^\d{10}$/.test(String(value).trim())) return callback(new Error('学号必须为10位数字'))
      callback()
    }

    const validateStaffNo = (rule, value, callback) => {
      if (form.role !== 'TEACHER') return callback()
      if (!value) return callback(new Error('请输入10位职工号'))
      if (!/^\d{10}$/.test(String(value).trim())) return callback(new Error('职工号必须为10位数字'))
      callback()
    }

    const validatePhone = (rule, value, callback) => {
      if (!value) return callback(new Error('请输入手机号'))
      if (!/^\d{11}$/.test(String(value).trim())) return callback(new Error('手机号必须为11位数字'))
      callback()
    }

    const rules = {
      username: [{ required: true, message: '请输入账户昵称', trigger: 'blur' }],
      realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
      studentNo: [{ required: true, validator: validateStudentNo, trigger: 'blur' }],
      staffNo: [{ required: true, validator: validateStaffNo, trigger: 'blur' }],
      phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
      major: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
      password: [{ required: true, validator: validatePass, trigger: 'blur' }],
      confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
    }

    const handleRegister = () => {
      formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            // 上传头像
            if (avatarFile.value) {
              form.avatar = await uploadResourceFile(avatarFile.value, 'avatar')
            }

            const res = await register({
              username: form.username,
              realName: form.realName,
              studentNo: form.studentNo,
              staffNo: form.staffNo,
              phone: form.phone,
              password: form.password,
              role: form.role,
              major: form.major,
              avatar: form.avatar
            })
            if (res.data.code === 200) {
              ElMessage.success('注册成功')
              router.push('/login')
            } else {
              ElMessage.error(res.data.message)
            }
          } catch (err) {
            ElMessage.error('注册失败，请稍后重试')
          } finally {
            loading.value = false
          }
        }
      })
    }

    return {
      formRef,
      form,
      rules,
      loading,
      avatarUrl,
      majorOptions,
      handleAvatarChange,
      handleRegister,
      User,
      Lock,
      Postcard,
      Plus
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color:#c91a23;
  padding: 20px 0;
}
.register-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}
.role-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.avatar-item {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.avatar-uploader {
  display: block;
  margin: 0 auto;
  cursor: pointer;
}
.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}
.avatar-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}
.footer-links {
  text-align: center;
  margin-top: 20px;
  color: #666;
}
.footer-links a {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
}
</style>