<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>校园帮助系统</h2>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="80px"
          >
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="loginForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password" v-if="loginType === 'password'">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item label="验证码" prop="code" v-if="loginType === 'code'">
              <el-input
                v-model="loginForm.code"
                placeholder="请输入验证码"
                prefix-icon="Key"
                @keyup.enter="handleLogin"
              >
                <template #append>
                  <el-button
                    :disabled="codeCountdown > 0"
                    @click="sendLoginCode"
                  >
                    {{ codeCountdown > 0 ? `${codeCountdown}秒后重试` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-link
                type="primary"
                :underline="false"
                @click="toggleLoginType"
                style="margin-bottom: 10px;"
              >
                {{ loginType === 'password' ? '使用验证码登录' : '使用密码登录' }}
              </el-link>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loginLoading"
                style="width: 100%"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="100px"
          >
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少8位，包含字母和数字"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <el-input
                v-model="registerForm.code"
                placeholder="请输入验证码"
                prefix-icon="Key"
              >
                <template #append>
                  <el-button
                    :disabled="codeCountdown > 0"
                    @click="sendRegisterCode"
                  >
                    {{ codeCountdown > 0 ? `${codeCountdown}秒后重试` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="registerLoading"
                style="width: 100%"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loginType = ref('password') // password 或 code
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeCountdown = ref(0)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  email: '',
  password: '',
  code: ''
})

const registerForm = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  code: ''
})

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码至少8位，且包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 切换登录方式
const toggleLoginType = () => {
  loginType.value = loginType.value === 'password' ? 'code' : 'password'
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

// 发送登录验证码
const sendLoginCode = async () => {
  if (!loginForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  try {
    await authApi.sendCode('LOGIN', loginForm.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 发送注册验证码
const sendRegisterCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  try {
    await authApi.sendCode('REGISTER', registerForm.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 开始倒计时
const startCountdown = () => {
  codeCountdown.value = 60
  const timer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginLoading.value = true
      try {
        const email = loginForm.email
        const password = loginType.value === 'password' ? loginForm.password : null
        const code = loginType.value === 'code' ? loginForm.code : null
        
        await userStore.login(email, password, code)
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loginLoading.value = false
      }
    }
  })
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        const user = {
          email: registerForm.email,
          password: registerForm.password
        }
        await userStore.register(user, registerForm.code)
        ElMessage.success('注册成功')
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// 切换标签页
const handleTabChange = (tabName) => {
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
  loginType.value = 'password'
  codeCountdown.value = 0
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f7fbfc;
}

.login-card {
  width: 450px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #769fcd;
}
</style>
