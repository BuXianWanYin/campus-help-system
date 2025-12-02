<template>
  <div class="register-container">
    <!-- 左侧：插图和宣传文字 -->
    <div class="register-left">
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
    
    <!-- 右侧：注册表单 -->
    <div class="register-right">
      <div class="register-form-wrapper">
        <h1 class="form-title">创建账号</h1>
        <p class="form-subtitle">欢迎加入我们，请填写以下信息完成注册</p>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="至少8位，包含字母和数字"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="code">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入验证码"
              size="large"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
              <template #append>
                <el-button
                  :disabled="codeCountdown > 0"
                  @click="sendRegisterCode"
                >
                  {{ codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="agreePrivacy">
              我同意<el-link type="primary" :underline="false" style="margin: 0 4px;">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="registerLoading"
              :disabled="!agreePrivacy"
              size="large"
              style="width: 100%"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <el-link
              type="primary"
              :underline="false"
              @click="goToLogin"
            >
              已有账号？<span class="link-text">去登录</span>
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

const registerLoading = ref(false)
const codeCountdown = ref(0)
const registerFormRef = ref(null)
const agreePrivacy = ref(false)

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

// 发送注册验证码
const sendRegisterCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  if (codeCountdown.value > 0) {
    return
  }
  
  try {
    await authApi.sendCode('REGISTER', registerForm.email)
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

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  if (!agreePrivacy.value) {
    ElMessage.warning('请先同意隐私政策')
    return
  }
  
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

// 跳转到登录页
const goToLogin = () => {
  router.push({ path: '/login', query: route.query })
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background-color: #f7fbfc;
}

/* 左侧区域 */
.register-left {
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
.register-right {
  flex: 0 0 40%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #FFFFFF;
  padding: 40px;
}

.register-form-wrapper {
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

.register-form {
  margin-top: 32px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #b9d7ea inset;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

.register-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

.register-form :deep(.el-checkbox) {
  color: #606266;
}

.register-form :deep(.el-checkbox__label) {
  font-size: 14px;
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
  .register-container {
    flex-direction: column;
  }
  
  .register-left {
    flex: 0 0 auto;
    min-height: 400px;
    padding: 40px 20px;
  }
  
  .register-right {
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
