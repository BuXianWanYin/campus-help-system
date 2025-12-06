<template>
  <div class="register-container">
    <!-- 左侧：插图和宣传文字 -->
    <div class="register-left">
      <div class="illustration-wrapper">
        <div class="system-title">{{ appConfig.title }}</div>
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
            <div class="feature-title">学习互助</div>
            <div class="feature-desc">发布或接取任务<br/>互帮互助</div>
          </div>
        </div>
        <div class="promo-text">
          <h1>连接校园，互助共享</h1>
          <p>失物招领、闲置交易、学习互助，一站式解决您的校园互助需求</p>
        </div>
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
                  :disabled="codeCountdown > 0 || codeSending"
                  :loading="codeSending"
                  @click="sendRegisterCode"
                >
                  {{ codeSending ? '发送中' : (codeCountdown > 0 ? `${codeCountdown}秒` : '获取验证码') }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          
            <el-form-item>
             <el-checkbox v-model="agreePrivacy">
               <span style="vertical-align: middle;">我同意</span>
               <el-link type="primary" :underline="false" style="vertical-align: middle; margin: 0 4px;">《隐私政策》</el-link>
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

/**
 * 注册页面
 * 用户注册功能，支持邮箱验证码注册
 */

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingBag, Box, Message, Lock, Key, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import appConfig from '@/config'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const registerLoading = ref(false)
const codeCountdown = ref(0)
const codeSending = ref(false)
const registerFormRef = ref(null)
const agreePrivacy = ref(false)
let countdownTimer = null

// 注册表单数据
const registerForm = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  code: ''
})

/**
 * 验证确认密码
 * @param {Object} rule - 验证规则
 * @param {string} value - 输入值
 * @param {Function} callback - 回调函数
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

/**
 * 验证邮箱是否已注册
 * @param {Object} rule - 验证规则
 * @param {string} value - 输入值
 * @param {Function} callback - 回调函数
 */
const validateEmail = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  
  // 检查邮箱是否已注册
  try {
    const response = await authApi.checkEmail(value)
    if (response.data === true) {
      callback(new Error('该邮箱已被注册，请直接登录'))
    } else {
      callback()
    }
  } catch (error) {
    // 如果接口调用失败，只验证格式，不阻止提交
    // 后端注册时会再次检查
    callback()
  }
}

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
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

/**
 * 发送注册验证码
 */
const sendRegisterCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  if (codeCountdown.value > 0 || codeSending.value) {
    return
  }
  
  codeSending.value = true
  try {
    await authApi.sendCode('REGISTER', registerForm.email)
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown(60)
  } catch (error) {
    // 如果后端返回了剩余秒数，使用该秒数
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
  z-index: 1;
}

.system-title {
  font-size: 42px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 50px;
  letter-spacing: 2px;
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
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
}

.register-form :deep(.el-checkbox__label span),
.register-form :deep(.el-checkbox__label .el-link) {
  vertical-align: middle;
  line-height: 1.5;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #606266;
}

.link-text {
  color: #409eff;
  font-weight: 500;
}

.form-footer .el-link:hover .link-text {
  color: #66b1ff;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .register-container {
    flex-direction: column;
  }
  
  .register-left {
    flex: 0 0 auto;
    padding: 40px 20px;
  }
  
  .feature-cards {
    flex-direction: column;
    gap: 20px;
  }
  
  .system-title {
    font-size: 32px;
    margin-bottom: 30px;
  }
  
  .promo-text h1 {
    font-size: 28px;
  }
  
  .register-right {
    padding: 20px;
  }
}
</style>
