<template>
  <div class="login-container">
    <!-- 左侧：插图和宣传文字 -->
    <div class="login-left">
      <div class="illustration-wrapper">
        <!-- 装饰性几何图形 -->
        <div class="decoration-shapes">
          <div class="shape shape-circle"></div>
          <div class="shape shape-square"></div>
          <div class="shape shape-rect"></div>
        </div>
        <!-- 主要功能图标 -->
        <div class="main-illustration">
          <div class="feature-icons">
            <div class="icon-item icon-search">
              <div class="icon-bg">
                <el-icon :size="40"><Search /></el-icon>
              </div>
              <span class="icon-label">失物招领</span>
              <p class="icon-desc">发布失物信息<br/>寻找丢失物品</p>
            </div>
            <div class="icon-item icon-shopping">
              <div class="icon-bg">
                <el-icon :size="40"><ShoppingBag /></el-icon>
              </div>
              <span class="icon-label">闲置交易</span>
              <p class="icon-desc">买卖闲置物品<br/>让资源再利用</p>
            </div>
            <div class="icon-item icon-truck">
              <div class="icon-bg">
                <el-icon :size="40"><Box /></el-icon>
              </div>
              <span class="icon-label">跑腿服务</span>
              <p class="icon-desc">发布或接取任务<br/>互帮互助</p>
            </div>
          </div>
        </div>
      </div>
      <div class="promotion-text">
        <h2>连接校园，互助共享</h2>
        <p>失物招领、闲置交易、跑腿服务，一站式解决您的校园互助需求</p>
      </div>
    </div>
    
    <!-- 右侧：登录表单 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <h1 class="form-title">欢迎回来</h1>
        <p class="form-subtitle">输入您的邮箱和密码登录</p>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="loginForm.email"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password" v-if="loginType === 'password'">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="code" v-if="loginType === 'code'">
            <el-input
              v-model="loginForm.code"
              placeholder="请输入验证码"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
              <template #append>
                <el-button
                  :disabled="codeCountdown > 0"
                  @click="sendLoginCode"
                >
                  {{ codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          
          <div class="form-options">
            <el-link
              type="primary"
              :underline="false"
              @click="toggleLoginType"
              class="toggle-link"
            >
              {{ loginType === 'password' ? '使用验证码登录' : '使用密码登录' }}
            </el-link>
          </div>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loginLoading"
              size="large"
              style="width: 100%"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <el-link
              type="primary"
              :underline="false"
              @click="goToRegister"
            >
              还没有账号？<span class="link-text">注册</span>
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingBag, Box, Message, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import appConfig from '@/config'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginType = ref('password') // password 或 code
const loginLoading = ref(false)
const codeCountdown = ref(0)
const loginFormRef = ref(null)
let countdownTimer = null

const loginForm = reactive({
  email: '',
  password: '',
  code: ''
})

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
  
  if (codeCountdown.value > 0) {
    return
  }
  
  try {
    await authApi.sendCode('LOGIN', loginForm.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown(60)
  } catch (error) {
    // 如果后端返回了剩余秒数，使用该秒数
    const remainingSeconds = extractRemainingSeconds(error.message || '')
    if (remainingSeconds > 0) {
      startCountdown(remainingSeconds)
    } else {
      // 否则使用默认60秒
      startCountdown(60)
    }
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 从错误信息中提取剩余秒数
const extractRemainingSeconds = (message) => {
  const match = message.match(/(\d+)秒后重试/)
  return match ? parseInt(match[1]) : 0
}

// 开始倒计时
const startCountdown = (seconds) => {
  // 清除之前的定时器
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  codeCountdown.value = seconds
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

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

// 跳转到注册页
const goToRegister = () => {
  router.push({ path: '/register', query: route.query })
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background-color: #f7fbfc;
}

/* 左侧区域 */
.login-left {
  flex: 0 0 60%;
  background-color: #d6e6f2;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 80px;
  position: relative;
  overflow: hidden;
}

.illustration-wrapper {
  position: relative;
  width: 100%;
  max-width: 600px;
  margin-bottom: 60px;
}

.decoration-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.shape {
  position: absolute;
  background-color: rgba(118, 159, 205, 0.1);
  border-radius: 8px;
}

.shape-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  top: 10%;
  right: 15%;
  animation: float 6s ease-in-out infinite;
}

.shape-square {
  width: 60px;
  height: 60px;
  top: 60%;
  left: 10%;
  animation: float 8s ease-in-out infinite;
}

.shape-rect {
  width: 100px;
  height: 40px;
  border-radius: 20px;
  top: 30%;
  left: 5%;
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-25px) rotate(5deg);
    opacity: 0.8;
  }
}

.main-illustration {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.feature-icons {
  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  width: 100%;
  max-width: 550px;
  gap: 30px;
  z-index: 2;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 160px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.icon-item:hover {
  transform: translateY(-8px);
}

.icon-item:hover .icon-bg {
  box-shadow: 0 8px 24px rgba(118, 159, 205, 0.4);
  transform: scale(1.05);
}

.icon-bg {
  width: 80px;
  height: 80px;
  background-color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #769fcd;
  box-shadow: 0 4px 16px rgba(118, 159, 205, 0.3);
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.icon-item .icon-label {
  font-size: 16px;
  margin-bottom: 8px;
  color: #303133;
  white-space: nowrap;
  font-weight: 600;
  transition: color 0.3s ease;
}

.icon-item:hover .icon-label {
  color: #769fcd;
}

.icon-item .icon-desc {
  font-size: 13px;
  color: #606266;
  text-align: center;
  line-height: 1.6;
  margin: 0;
  transition: color 0.3s ease;
}

.icon-item:hover .icon-desc {
  color: #769fcd;
}


.promotion-text {
  text-align: center;
  color: #303133;
  max-width: 500px;
}

.promotion-text h2 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: #303133;
}

.promotion-text p {
  font-size: 16px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

/* 右侧区域 */
.login-right {
  flex: 0 0 40%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #FFFFFF;
  padding: 40px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
}

.form-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px 0;
}

.login-form {
  margin-top: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #b9d7ea inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

.login-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
}

.toggle-link {
  font-size: 14px;
  color: #769fcd;
}

.toggle-link:hover {
  color: #b9d7ea;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #606266;
}

.link-text {
  color: #769fcd;
  font-weight: 500;
}

.form-footer .el-link:hover .link-text {
  color: #b9d7ea;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-left {
    flex: 0 0 auto;
    min-height: 400px;
    padding: 40px 20px;
  }
  
  .login-right {
    flex: 0 0 auto;
    width: 100%;
  }
  
  .illustration-wrapper {
    margin-bottom: 30px;
  }
  
  .feature-icons {
    flex-direction: column;
    gap: 40px;
    align-items: center;
  }
  
  .icon-item {
    max-width: 200px;
  }
  
  .promotion-text h2 {
    font-size: 20px;
  }
  
  .promotion-text p {
    font-size: 14px;
  }
}
</style>
