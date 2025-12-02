<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-content">
      <el-row :gutter="24">
        <!-- 左侧：用户信息卡片 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8">
          <div class="user-info-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="getAvatarUrl(userInfo.avatar)" class="user-avatar">
                {{ userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="getUploadHeaders()"
                :data="{ module: 'avatar' }"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button type="primary" size="small" text>更换头像</el-button>
              </el-upload>
            </div>

            <div class="user-basic-info">
              <h3 class="user-name">{{ userInfo.nickname || '未设置昵称' }}</h3>
              <p class="user-email">{{ userInfo.email }}</p>
              
              <div class="user-tags">
                <el-tag v-show="userInfo.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
                <template v-if="userInfo.role !== 'ADMIN'">
                  <el-tag v-show="userInfo.isVerified === 1" type="success" size="small">已认证</el-tag>
                  <el-tag v-show="userInfo.isVerified !== 1 && userInfo.isVerified !== undefined" type="info" size="small">未认证</el-tag>
                </template>
              </div>

              <el-button
                v-if="userInfo.isVerified !== 1 && userInfo.role !== 'ADMIN'"
                type="primary"
                size="small"
                style="margin-top: 16px;"
                @click="goToVerification"
              >
                前往实名认证
              </el-button>
            </div>
          </div>
        </el-col>

        <!-- 右侧：详细信息 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16">
          <div class="info-tabs-card">
            <el-tabs v-model="activeTab" class="profile-tabs">
              <el-tab-pane label="基本信息" name="basic">
                <div class="tab-content">
                  <el-form
                    ref="formRef"
                    :model="form"
                    :rules="rules"
                    label-width="100px"
                    class="profile-form"
                  >
                    <el-form-item label="昵称" prop="nickname">
                      <el-input 
                        v-model="form.nickname" 
                        placeholder="请输入昵称"
                        clearable
                      >
                        <template #prefix>
                          <el-icon><User /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>

                    <el-form-item label="性别" prop="gender">
                      <el-radio-group v-model="form.gender" class="gender-group">
                        <el-radio :label="1">男</el-radio>
                        <el-radio :label="2">女</el-radio>
                        <el-radio :label="0">保密</el-radio>
                      </el-radio-group>
                    </el-form-item>

                    <el-form-item label="年级" prop="grade">
                      <el-input 
                        v-model="form.grade" 
                        placeholder="请输入年级，如：2021级"
                        clearable
                      >
                        <template #prefix>
                          <el-icon><Calendar /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>

                    <el-form-item label="专业" prop="major">
                      <el-input 
                        v-model="form.major" 
                        placeholder="请输入专业"
                        clearable
                      >
                        <template #prefix>
                          <el-icon><Reading /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>

                    <el-form-item class="form-actions">
                      <el-button type="primary" :loading="loading" @click="handleSubmit">
                        保存修改
                      </el-button>
                      <el-button @click="handleReset">
                        <el-icon><RefreshLeft /></el-icon>
                        重置
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>

              <el-tab-pane label="账户信息" name="account">
                <div class="tab-content">
                  <div class="account-info-list">
                    <div class="info-item">
                      <div class="info-label">
                        <el-icon><Message /></el-icon>
                        <span>邮箱地址</span>
                      </div>
                      <div class="info-value">{{ userInfo.email }}</div>
                    </div>

                    <div class="info-item">
                      <div class="info-label">
                        <el-icon><Clock /></el-icon>
                        <span>注册时间</span>
                      </div>
                      <div class="info-value">{{ formatDate(userInfo.createTime) }}</div>
                    </div>

                    <div class="info-item">
                      <div class="info-label">
                        <el-icon><Timer /></el-icon>
                        <span>最后登录</span>
                      </div>
                      <div class="info-value">{{ formatDate(userInfo.lastLoginTime) || '从未登录' }}</div>
                    </div>

                    <div class="info-item">
                      <div class="info-label">
                        <el-icon><CircleCheck /></el-icon>
                        <span>账户状态</span>
                      </div>
                      <div class="info-value">
                        <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="large">
                          {{ userInfo.status === 1 ? '正常' : '已禁用' }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="更改密码" name="password">
                <div class="tab-content">
                  <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="120px"
                    class="profile-form"
                  >
                    <el-form-item label="当前密码" prop="currentPassword">
                      <el-input
                        v-model="passwordForm.currentPassword"
                        type="password"
                        placeholder="请输入当前密码"
                        show-password
                        clearable
                      />
                    </el-form-item>

                    <el-form-item label="新密码" prop="newPassword">
                      <el-input
                        v-model="passwordForm.newPassword"
                        type="password"
                        placeholder="请输入新密码（至少8位，包含字母和数字）"
                        show-password
                        clearable
                      />
                    </el-form-item>

                    <el-form-item label="确认新密码" prop="confirmPassword">
                      <el-input
                        v-model="passwordForm.confirmPassword"
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                        clearable
                      />
                    </el-form-item>

                    <el-form-item class="form-actions">
                      <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                        保存修改
                      </el-button>
                      <el-button @click="handleResetPassword">
                        <el-icon><RefreshLeft /></el-icon>
                        重置
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Camera, User, Check, Warning, Star, Document, 
  Message, Clock, Timer, CircleCheck, Calendar, Reading, RefreshLeft, Lock
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import { getToken } from '@/utils/auth'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const activeTab = ref('basic')
const loading = ref(false)
const passwordLoading = ref(false)
// 从 userStore 获取初始用户信息，避免刷新时抖动
const userInfo = ref(userStore.userInfo || {})

const form = reactive({
  nickname: '',
  gender: 0,
  grade: '',
  major: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
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

const uploadAction = `${import.meta.env.VITE_APP_BASE_API}/file/upload`
const getUploadHeaders = () => ({
  Authorization: `Bearer ${getToken()}`
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 如果 userStore 中已有用户信息，先使用它避免抖动
    if (userStore.userInfo && (!userInfo.value || Object.keys(userInfo.value).length === 0)) {
      userInfo.value = { ...userStore.userInfo }
      // 填充表单
      form.nickname = userInfo.value.nickname || ''
      form.gender = userInfo.value.gender || 0
      form.grade = userInfo.value.grade || ''
      form.major = userInfo.value.major || ''
    }
    
    const response = await userApi.getCurrentUser()
    if (response.code === 200) {
      userInfo.value = response.data
      // 填充表单
      form.nickname = userInfo.value.nickname || ''
      form.gender = userInfo.value.gender || 0
      form.grade = userInfo.value.grade || ''
      form.major = userInfo.value.major || ''
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户信息失败')
  }
}

// 头像上传成功
const handleAvatarSuccess = (response, file) => {
  if (response.code === 200) {
    userInfo.value.avatar = response.data
    ElMessage.success('头像上传成功')
    // 更新用户信息
    updateUserInfo({ avatar: response.data })
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 更新用户信息
const updateUserInfo = async (data) => {
  try {
    // 确保包含邮箱字段，避免后端验证失败
    const updateData = {
      ...data,
      email: userInfo.value.email || userStore.userInfo?.email
    }
    const response = await userApi.updateCurrentUser(updateData)
    if (response.code === 200) {
      userStore.setUserInfo(response.data)
      userInfo.value = response.data
      ElMessage.success('更新成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 确保包含邮箱字段，避免后端验证失败
        const formData = {
          ...form,
          email: userInfo.value.email || userStore.userInfo?.email
        }
        await updateUserInfo(formData)
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  form.nickname = userInfo.value.nickname || ''
  form.gender = userInfo.value.gender || 0
  form.grade = userInfo.value.grade || ''
  form.major = userInfo.value.major || ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 前往实名认证页面
const goToVerification = () => {
  router.push('/user/verification')
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        const response = await userApi.changePassword({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })
        if (response.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          handleResetPassword()
          // 延迟跳转到登录页
          setTimeout(() => {
            userStore.logout()
            router.push('/login')
          }, 1500)
        }
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 重置密码表单
const handleResetPassword = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--spacing-2xl);
  min-height: calc(100vh - var(--header-height));
  background-color: var(--color-bg-primary);
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 内容区域 */
.profile-content {
  margin-top: 24px;
}

/* 用户信息卡片 */
.user-info-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

/* 头像区域 */
.avatar-section {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
  padding-bottom: var(--spacing-2xl);
  border-bottom: 1px solid var(--color-border);
}

.user-avatar {
  background-color: var(--color-primary);
  font-size: 32px;
  font-weight: bold;
  color: var(--color-bg-white);
}

.avatar-uploader {
  margin-top: var(--spacing-md);
}

/* 用户基本信息 */
.user-basic-info {
  text-align: center;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.user-email {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-lg) 0;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

/* 信息标签页卡片 */
.info-tabs-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

/* 标签页样式 */
.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--spacing-2xl);
  border-bottom: 1px solid var(--color-border);
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-regular);
  padding: 0 var(--spacing-2xl);
  height: 40px;
  line-height: 40px;
}

.profile-tabs :deep(.el-tabs__item:hover) {
  color: var(--color-primary);
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: 600;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
  height: 2px;
}

.tab-content {
  padding: 8px 0;
}

/* 表单样式 */
.profile-form {
  max-width: 600px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  padding: 4px 12px;
  min-height: 32px;
  transition: all 0.3s;
}

.profile-form :deep(.el-input__inner) {
  height: 24px;
  line-height: 24px;
  font-size: 14px;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.profile-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px var(--color-primary) inset;
}

.profile-form :deep(.el-input__prefix) {
  color: #909399;
}

.gender-group {
  display: flex;
  gap: 12px;
}

.gender-group :deep(.el-radio.is-bordered) {
  border-radius: var(--radius-md);
  padding: var(--spacing-sm) var(--content-padding);
  border-color: var(--color-border);
}

.gender-group :deep(.el-radio.is-bordered.is-checked) {
  border-color: var(--color-primary);
  background-color: var(--color-bg-primary);
}

.form-actions {
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-2xl);
  border-top: 1px solid var(--color-border);
}

.form-actions :deep(.el-button) {
  border-radius: var(--radius-md);
}

/* 账户信息列表 */
.account-info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: 14px;
  color: var(--color-text-regular);
}

.info-label .el-icon {
  font-size: 16px;
  color: var(--color-text-secondary);
}

.info-value {
  font-size: 14px;
  color: var(--color-text-primary);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .profile-container {
    padding: 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .user-info-card,
  .info-tabs-card {
    padding: 24px;
  }

  .user-avatar {
    width: 100px !important;
    height: 100px !important;
    font-size: 40px;
  }

  .user-name {
    font-size: 20px;
  }

  .profile-form {
    max-width: 100%;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .info-value {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }

  .page-subtitle {
    font-size: 13px;
  }

  .user-info-card,
  .info-tabs-card {
    padding: 20px;
  }

  .profile-tabs :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 15px;
  }
}
</style>

