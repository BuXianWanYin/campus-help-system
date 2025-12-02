<template>
  <div class="forgot-password-container">
    <div class="forgot-password-left">
      <div class="illustration-wrapper">
        <div class="feature-cards">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Search /></el-icon>
            </div>
            <div class="feature-title">失物招领</div>
            <div class="feature-desc">发布失物信息<br/>寻找丢失物品</div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><ShoppingBag /></el-icon>
            </div>
            <div class="feature-title">闲置交易</div>
            <div class="feature-desc">买卖闲置物品<br/>让资源再利用</div>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Box /></el-icon>
            </div>
            <div class="feature-title">跑腿服务</div>
            <div class="feature-desc">发布或接取任务<br/>互帮互助</div>
          </div>
        </div>
        <div class="promo-text">
          <h1>连接校园，互助共享</h1>
          <p>失物招领、闲置交易、跑腿服务，一站式解决您的校园互助需求</p>
        </div>
      </div>
    </div>
    
    <div class="forgot-password-right">
      <div class="forgot-password-form-wrapper">
        <h2>重置密码</h2>
        <p class="form-desc">请输入您的邮箱，我们将发送验证码到您的邮箱</p>
        
        <el-form
          ref="forgotPasswordFormRef"
          :model="forgotPasswordForm"
          :rules="forgotPasswordRules"
          class="forgot-password-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="forgotPasswordForm.email"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="code" v-if="step === 2">
            <el-input
              v-model="forgotPasswordForm.code"
              placeholder="请输入验证码"
              size="large"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
              <template #append>
                <el-button
                  :disabled="codeCountdown > 0 || codeSending"
                  :loading="codeSending"
                  @click="sendResetCode"
                >
                  {{ codeSending ? '发送中' : (codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码') }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="newPassword" v-if="step === 2">
            <el-input
              v-model="forgotPasswordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword" v-if="step === 2">
            <el-input
              v-model="forgotPasswordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="forgotPasswordLoading"
              @click="handleForgotPassword"
              style="width: 100%"
            >
              {{ step === 1 ? '发送验证码' : '重置密码' }}
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <el-link
              type="primary"
              @click="goToLogin"
            >
              返回登录
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
import { authApi } from '@/api'
import appConfig from '@/config'

const router = useRouter()
const route = useRoute()

const step = ref(1) // 1: 输入邮箱, 2: 输入验证码和新密码
const forgotPasswordLoading = ref(false)
const codeCountdown = ref(0)
const codeSending = ref(false)
const forgotPasswordFormRef = ref(null)
let countdownTimer = null

const forgotPasswordForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== forgotPasswordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码至少8位，且包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送重置密码验证码
const sendResetCode = async () => {
  if (!forgotPasswordForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  if (codeCountdown.value > 0 || codeSending.value) {
    return
  }
  
  codeSending.value = true
  try {
    await authApi.sendCode('RESET_PASSWORD', forgotPasswordForm.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown(60)
  } catch (error) {
    const remainingSeconds = extractRemainingSeconds(error.message || '')
    if (remainingSeconds > 0) {
      startCountdown(remainingSeconds)
    }
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    codeSending.value = false
  }
}

// 从错误信息中提取剩余秒数
const extractRemainingSeconds = (message) => {
  const match = message.match(/(\d+)秒后重试/)
  return match ? parseInt(match[1]) : 0
}

// 开始倒计时
const startCountdown = (seconds) => {
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

// 处理忘记密码
const handleForgotPassword = async () => {
  if (!forgotPasswordFormRef.value) return
  
  if (step.value === 1) {
    // 第一步：验证邮箱并发送验证码
    await forgotPasswordFormRef.value.validateField('email', async (valid) => {
      if (valid) {
        // 检查邮箱是否已注册
        try {
          const response = await authApi.checkEmail(forgotPasswordForm.email)
          if (response.data === false) {
            ElMessage.warning('该邮箱未注册，请先注册')
            return
          }
          // 发送验证码
          await sendResetCode()
          step.value = 2
        } catch (error) {
          ElMessage.error(error.message || '检查邮箱失败')
        }
      }
    })
  } else {
    // 第二步：重置密码
    await forgotPasswordFormRef.value.validate(async (valid) => {
      if (valid) {
        forgotPasswordLoading.value = true
        try {
          await authApi.resetPassword(
            forgotPasswordForm.email,
            forgotPasswordForm.code,
            forgotPasswordForm.newPassword
          )
          ElMessage.success('密码重置成功，请使用新密码登录')
          router.push({ path: '/login', query: { email: forgotPasswordForm.email } })
        } catch (error) {
          ElMessage.error(error.message || '重置密码失败')
        } finally {
          forgotPasswordLoading.value = false
        }
      }
    })
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push({ path: '/login', query: route.query })
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  min-height: 100vh;
  background-color: #f7fbfc;
}

/* 左侧区域 */
.forgot-password-left {
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
  z-index: 1;
}

.feature-cards {
  display: flex;
  justify-content: space-around;
  gap: 30px;
  margin-bottom: 60px;
}

.feature-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.feature-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.feature-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.promo-text {
  text-align: center;
}

.promo-text h1 {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.promo-text p {
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
}

/* 右侧区域 */
.forgot-password-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background-color: #ffffff;
}

.forgot-password-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.forgot-password-form-wrapper h2 {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  text-align: center;
}

.form-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
  text-align: center;
}

.forgot-password-form {
  margin-top: 30px;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.toggle-link {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .forgot-password-container {
    flex-direction: column;
  }
  
  .forgot-password-left {
    flex: 0 0 auto;
    padding: 40px 20px;
  }
  
  .feature-cards {
    flex-direction: column;
    gap: 20px;
  }
  
  .promo-text h1 {
    font-size: 28px;
  }
  
  .forgot-password-right {
    padding: 20px;
  }
}
</style>

