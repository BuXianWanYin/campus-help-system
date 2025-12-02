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
        <!-- 主要插图 -->
        <div class="main-illustration">
          <div class="laptop-icon">
            <div class="laptop-screen">
              <div class="folder-icon">
                <el-icon :size="32"><Folder /></el-icon>
              </div>
            </div>
            <div class="laptop-base"></div>
          </div>
          <div class="floating-folder">
            <el-icon :size="40"><FolderChecked /></el-icon>
          </div>
          <div class="floating-docs">
            <div class="doc doc-1"></div>
            <div class="doc doc-2"></div>
            <div class="doc doc-3"></div>
          </div>
        </div>
      </div>
      <div class="promotion-text">
        <h2>一款兼具设计美学与高效开发的后台系统</h2>
        <p>美观实用的界面，经过视觉优化，确保卓越的用户体验</p>
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
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Folder, FolderChecked, Message, Lock, Key } from '@element-plus/icons-vue'
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
  max-width: 500px;
  height: 400px;
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
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.main-illustration {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.laptop-icon {
  position: relative;
  z-index: 2;
}

.laptop-screen {
  width: 200px;
  height: 140px;
  background-color: #769fcd;
  border-radius: 8px 8px 0 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(118, 159, 205, 0.3);
}

.laptop-screen .folder-icon {
  color: #FFFFFF;
}

.laptop-base {
  width: 240px;
  height: 12px;
  background-color: #b9d7ea;
  border-radius: 0 0 4px 4px;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(118, 159, 205, 0.2);
}

.floating-folder {
  position: absolute;
  top: 20%;
  right: 15%;
  color: #769fcd;
  animation: float 5s ease-in-out infinite;
  z-index: 1;
}

.floating-docs {
  position: absolute;
  top: 10%;
  left: 10%;
  z-index: 1;
}

.doc {
  width: 40px;
  height: 50px;
  background-color: rgba(185, 215, 234, 0.6);
  border: 2px solid #b9d7ea;
  border-radius: 4px;
  position: absolute;
}

.doc-1 {
  top: 0;
  left: 0;
  transform: rotate(-10deg);
  animation: float 6s ease-in-out infinite;
}

.doc-2 {
  top: 20px;
  left: 30px;
  transform: rotate(5deg);
  animation: float 7s ease-in-out infinite 0.5s;
}

.doc-3 {
  top: 40px;
  left: 60px;
  transform: rotate(-8deg);
  animation: float 8s ease-in-out infinite 1s;
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
    min-height: 300px;
    padding: 40px 20px;
  }
  
  .register-right {
    flex: 0 0 auto;
    width: 100%;
  }
  
  .illustration-wrapper {
    height: 250px;
    margin-bottom: 30px;
  }
  
  .promotion-text h2 {
    font-size: 20px;
  }
  
  .promotion-text p {
    font-size: 14px;
  }
}
</style>
