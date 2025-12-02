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
              <div class="avatar-wrapper">
                <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
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
                  <el-button type="primary" size="small" class="change-avatar-btn">
                    <el-icon><Camera /></el-icon>
                    更换头像
                  </el-button>
                </el-upload>
              </div>
            </div>

            <div class="user-basic-info">
              <h3 class="user-name">{{ userInfo.nickname || '未设置昵称' }}</h3>
              <p class="user-email">{{ userInfo.email }}</p>
              
              <div class="user-tags">
                <span v-if="userInfo.isVerified === 1" class="tag tag-success">
                  <el-icon><Check /></el-icon>
                  已认证
                </span>
                <span v-else class="tag tag-info">
                  <el-icon><Warning /></el-icon>
                  未认证
                </span>
                <span v-if="userInfo.role === 'ADMIN'" class="tag tag-warning">
                  <el-icon><Star /></el-icon>
                  管理员
                </span>
              </div>

              <el-button
                v-if="userInfo.isVerified !== 1"
                type="primary"
                class="verify-btn"
                @click="goToVerification"
              >
                <el-icon><Document /></el-icon>
                前往实名认证
              </el-button>
            </div>

            <!-- 信用积分卡片 -->
            <div class="credit-info">
              <div class="credit-header">
                <el-icon class="credit-icon"><TrendCharts /></el-icon>
                <span>信用积分</span>
              </div>
              <div class="credit-score">
                <span class="score-value">{{ userInfo.creditScore || 0 }}</span>
                <span class="score-label">分</span>
              </div>
              <div class="credit-level">
                <el-tag :type="getCreditLevelType(userInfo.creditLevel)" size="large">
                  {{ userInfo.creditLevel || '暂无等级' }}
                </el-tag>
              </div>
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
                        <el-radio :label="0" border>未知</el-radio>
                        <el-radio :label="1" border>男</el-radio>
                        <el-radio :label="2" border>女</el-radio>
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
                        <el-icon><Check /></el-icon>
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
                        <el-icon><TrendCharts /></el-icon>
                        <span>信用积分</span>
                      </div>
                      <div class="info-value">
                        <el-tag :type="getCreditLevelType(userInfo.creditLevel)" size="large">
                          {{ userInfo.creditScore }}分 - {{ userInfo.creditLevel }}
                        </el-tag>
                      </div>
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
  Camera, User, Check, Warning, Star, Document, TrendCharts, 
  Message, Clock, Timer, CircleCheck, Calendar, Reading, RefreshLeft 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import { getToken } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const activeTab = ref('basic')
const loading = ref(false)
const userInfo = ref({})

const form = reactive({
  nickname: '',
  gender: 0,
  grade: '',
  major: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ]
}

const uploadAction = `${import.meta.env.VITE_APP_BASE_API}/file/upload`
const getUploadHeaders = () => ({
  Authorization: `Bearer ${getToken()}`
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
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
    const response = await userApi.updateCurrentUser(data)
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
        await updateUserInfo(form)
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

// 获取信用等级类型
const getCreditLevelType = (level) => {
  const levelMap = {
    '优秀': 'success',
    '良好': 'primary',
    '一般': 'info',
    '较差': 'warning',
    '差': 'danger'
  }
  return levelMap[level] || 'info'
}

// 前往实名认证页面
const goToVerification = () => {
  router.push('/user/verification')
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
  border-radius: var(--radius-lg);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}

.user-info-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

/* 头像区域 */
.avatar-section {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e8f4f8;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  background-color: var(--color-primary);
  font-size: 48px;
  font-weight: bold;
  color: var(--color-bg-white);
  border: 4px solid var(--color-primary-lighter);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.2);
}

.avatar-uploader {
  margin-top: 16px;
}

.change-avatar-btn {
  border-radius: var(--radius-xl);
  padding: var(--spacing-sm) var(--content-padding);
  background-color: var(--color-primary);
  border: none;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.change-avatar-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
  background-color: var(--color-primary-hover);
}

/* 用户基本信息 */
.user-basic-info {
  text-align: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #e8f4f8;
  margin-bottom: 24px;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
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
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.tag-success {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.tag-info {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.tag-warning {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.verify-btn {
  width: 100%;
  border-radius: var(--radius-md);
  padding: 10px var(--content-padding);
  background-color: var(--color-primary);
  border: none;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.verify-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
  background-color: var(--color-primary-hover);
}

/* 信用积分卡片 */
.credit-info {
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, var(--color-primary-lighter) 100%);
  border-radius: var(--radius-lg);
  padding: var(--content-padding);
  text-align: center;
}

.credit-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #606266;
}

.credit-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.credit-score {
  margin-bottom: var(--spacing-md);
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: var(--color-primary);
  line-height: 1;
}

.score-label {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin-left: var(--spacing-xs);
}

.credit-level {
  display: flex;
  justify-content: center;
}

/* 信息标签页卡片 */
.info-tabs-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
  min-height: 500px;
}

.info-tabs-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}

/* 标签页样式 */
.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 32px;
  border-bottom: 2px solid #e8f4f8;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
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
  height: 3px;
}

.tab-content {
  padding: 8px 0;
}

/* 表单样式 */
.profile-form {
  max-width: 600px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 28px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  padding: var(--spacing-sm) 15px;
  transition: all 0.3s;
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
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e8f4f8;
}

.form-actions :deep(.el-button) {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
}

.form-actions :deep(.el-button--primary) {
  background-color: var(--color-primary);
  border: none;
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.form-actions :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
  background-color: var(--color-primary-hover);
}

/* 账户信息列表 */
.account-info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--content-padding);
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-primary-lighter);
  transition: all 0.3s ease;
}

.info-item:hover {
  background-color: var(--color-primary-lighter);
  border-color: var(--color-primary);
  transform: translateX(4px);
}

.info-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-regular);
}

.info-label .el-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.info-value {
  font-size: 15px;
  color: var(--color-text-primary);
  font-weight: 500;
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

  .score-value {
    font-size: 40px;
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

