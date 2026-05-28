<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">教学平台登录</h2>
      
      <div class="role-switch">
        <el-radio-group v-model="form.role" size="large">
          <el-radio-button label="STUDENT">学生端</el-radio-button>
          <el-radio-button label="TEACHER">教师端</el-radio-button>
          <el-radio-button label="ADMIN">管理员</el-radio-button>
        </el-radio-group>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账户名称" size="large" :prefix-icon="User"></el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password :prefix-icon="Lock"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links" v-if="form.role === 'STUDENT' || form.role === 'TEACHER'">
        <el-button type="text" class="forgot-link" @click="openForgotDialog">忘记密码？</el-button>
      </div>

      <div class="footer-links" v-if="form.role !== 'ADMIN'">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>

      <el-dialog v-model="forgotDialogVisible" title="忘记密码" width="420px">
        <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef" label-width="80px">
          <el-form-item :label="form.role === 'TEACHER' ? '工号' : '学号'" prop="accountNo">
            <el-input v-model="forgotForm.accountNo" :placeholder="form.role === 'TEACHER' ? '请输入10位工号' : '请输入10位学号'" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="请输入新密码(至少6位)" />
          </el-form-item>
          <el-form-item label="确认" prop="confirmPassword">
            <el-input v-model="forgotForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="forgotDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="forgotLoading" @click="submitForgot">确定重置</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, forgotPassword, forgotPasswordTeacher } from '../api/auth'
import { adminLogin } from '../api/admin'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)

    const forgotDialogVisible = ref(false)
    const forgotFormRef = ref(null)
    const forgotLoading = ref(false)

    const forgotForm = reactive({
      accountNo: '',
      newPassword: '',
      confirmPassword: ''
    })

    const form = reactive({
      username: '',
      password: '',
      role: 'STUDENT'
    })

    const rules = {
      username: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    const forgotRules = {
      accountNo: [
        {
          validator: (rule, value, callback) => {
            const v = value == null ? '' : String(value).trim()
            if (!v) {
              callback(new Error(form.role === 'TEACHER' ? '请输入工号' : '请输入学号'))
              return
            }
            if (!/^\d{10}$/.test(v)) {
              callback(new Error(form.role === 'TEACHER' ? '工号必须为10位数字' : '学号必须为10位数字'))
              return
            }
            callback()
          },
          trigger: 'blur'
        }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== forgotForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    const openForgotDialog = () => {
      forgotForm.accountNo = ''
      forgotForm.newPassword = ''
      forgotForm.confirmPassword = ''
      forgotDialogVisible.value = true
    }

    const submitForgot = async () => {
      if (!forgotFormRef.value) return
      forgotFormRef.value.validate(async (valid) => {
        if (!valid) return
        forgotLoading.value = true
        try {
          let res
          if (form.role === 'TEACHER') {
            res = await forgotPasswordTeacher({
              staffNo: forgotForm.accountNo,
              newPassword: forgotForm.newPassword
            })
          } else {
            res = await forgotPassword({
              studentNo: forgotForm.accountNo,
              newPassword: forgotForm.newPassword
            })
          }
          if (res.data.code === 200) {
            ElMessage.success('密码重置成功，请使用新密码登录')
            forgotDialogVisible.value = false
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (e) {
          ElMessage.error('重置失败，请稍后重试')
        } finally {
          forgotLoading.value = false
        }
      })
    }

    const handleLogin = () => {
      formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            let res
            if (form.role === 'ADMIN') {
              res = await adminLogin(form.username, form.password)
            } else {
              res = await login({
                username: form.username,
                password: form.password,
                role: form.role
              })
            }
            if (res.data.code === 200) {
              const user = res.data.data
              localStorage.setItem('user', JSON.stringify(user))
              ElMessage.success('登录成功')
              if (user.role === 'STUDENT') {
                router.push('/student/home')
              } else if (user.role === 'TEACHER') {
                router.push('/teacher/home')
              } else if (user.role === 'ADMIN') {
                router.push('/admin/home')
              }
            } else {
              ElMessage.error(res.data.message)
            }
          } catch (err) {
            ElMessage.error('登录失败，请稍后重试')
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
      handleLogin,
      forgotDialogVisible,
      forgotForm,
      forgotRules,
      forgotFormRef,
      forgotLoading,
      openForgotDialog,
      submitForgot,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color:#c91a23;
}
.login-box {
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
  margin-bottom: 30px;
}
.footer-links {
  text-align: center;
  margin-top: 20px;
  color: #666;
}
.forgot-link {
  padding: 0;
}
.footer-links a {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
}
</style>