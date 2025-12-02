<template>
  <div class="profile-container">
    <div class="profile-card">
      <h1 class="page-title">个人中心</h1>
      
      <el-row :gutter="20">
        <!-- 左侧：用户信息 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8">
          <div class="user-info-section">
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="userInfo.avatar">
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
                <el-button type="primary" size="small">
                  <el-icon><Camera /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>
            <div class="user-basic-info">
              <h3>{{ userInfo.nickname || '未设置昵称' }}</h3>
              <p class="email">{{ userInfo.email }}</p>
              <div class="tags">
                <el-tag v-if="userInfo.isVerified === 1" type="success" size="small">已认证</el-tag>
                <el-tag v-else type="info" size="small">未认证</el-tag>
                <el-tag v-if="userInfo.role === 'ADMIN'" type="warning" size="small" style="margin-left: 8px;">管理员</el-tag>
              </div>
              <el-button
                v-if="userInfo.isVerified !== 1"
                type="primary"
                size="small"
                style="margin-top: 12px;"
                @click="goToVerification"
              >
                前往实名认证
              </el-button>
            </div>
          </div>
        </el-col>
        
        <!-- 右侧：详细信息 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-width="100px"
                style="max-width: 600px;"
              >
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio :label="0">未知</el-radio>
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="年级" prop="grade">
                  <el-input v-model="form.grade" placeholder="请输入年级，如：2021级" />
                </el-form-item>
                <el-form-item label="专业" prop="major">
                  <el-input v-model="form.major" placeholder="请输入专业" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="账户信息" name="account">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="最后登录">{{ formatDate(userInfo.lastLoginTime) || '从未登录' }}</el-descriptions-item>
                <el-descriptions-item label="信用积分">
                  <el-tag :type="getCreditLevelType(userInfo.creditLevel)">
                    {{ userInfo.creditScore }}分 - {{ userInfo.creditLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="账户状态">
                  <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                    {{ userInfo.status === 1 ? '正常' : '已禁用' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #b9d7ea;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 32px 0;
}

.user-info-section {
  text-align: center;
  padding: 20px;
  background-color: #f7fbfc;
  border-radius: 8px;
}

.avatar-wrapper {
  margin-bottom: 24px;
}

.avatar-uploader {
  margin-top: 16px;
}

.user-basic-info h3 {
  margin: 20px 0 8px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.user-basic-info .email {
  margin: 8px 0 16px;
  color: #606266;
  font-size: 14px;
}

.user-basic-info .tags {
  margin-bottom: 12px;
}

/* 优化表单样式 */
:deep(.el-form) {
  padding: 20px 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #b9d7ea inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #769fcd inset;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  color: #606266;
}

:deep(.el-tabs__item.is-active) {
  color: #769fcd;
}

:deep(.el-tabs__active-bar) {
  background-color: #769fcd;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .profile-container {
    padding: 16px;
  }
  
  .profile-card {
    padding: 24px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
}
</style>

