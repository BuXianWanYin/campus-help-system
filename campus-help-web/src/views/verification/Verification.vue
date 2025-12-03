<template>
  <div class="verification-container">
    <div class="verification-card">
      <h1 class="page-title">实名认证</h1>
      
      <!-- 已认证状态（非管理员） -->
      <div v-if="userInfo.isVerified === 1 && userInfo.role !== 'ADMIN'" class="verified-status">
        <el-result icon="success" title="认证成功" sub-title="您已完成实名认证">
          <template #extra>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="学号/工号">{{ userInfo.studentId }}</el-descriptions-item>
              <el-descriptions-item label="用户类型">{{ userInfo.userType || '学生' }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ maskIdCard(userInfo.idCard) }}
              </el-descriptions-item>
              <el-descriptions-item label="认证时间">
                {{ formatDate(userInfo.verificationAuditTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag type="success">已认证</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="接单权限">
                <el-tag v-if="userInfo.canAcceptTask === 1" type="success">可接单</el-tag>
                <el-tag v-else type="info">不可接单</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 20px;">
              <p style="color: var(--color-text-regular); font-size: 14px;">
                您现在可以发布闲置商品和跑腿任务<br>
                您现在可以参与商品交易和接单跑腿
              </p>
            </div>
          </template>
        </el-result>
      </div>
      
      <!-- 管理员提示 -->
      <div v-else-if="userInfo.role === 'ADMIN'" class="admin-status">
        <el-result icon="info" title="管理员无需认证" sub-title="管理员账号已自动获得所有权限，无需进行实名认证">
        </el-result>
      </div>
      
      <!-- 认证中状态 -->
      <div v-else-if="userInfo.verificationStatus === 'PENDING'" class="pending-status">
        <el-result icon="warning" title="认证审核中" sub-title="您的实名认证正在审核中">
          <template #extra>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="学号/工号">{{ userInfo.studentId }}</el-descriptions-item>
              <el-descriptions-item label="用户类型">{{ userInfo.userType || '学生' }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">
                {{ formatDate(userInfo.verificationSubmitTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="审核状态">
                <el-tag type="warning">待审核</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 20px; padding: 16px; background-color: #f0f9ff; border-radius: 8px;">
              <p style="color: var(--color-text-regular); font-size: 14px; margin: 0;">
                预计审核时间：1-3个工作日<br>
                审核结果将通过站内消息和邮件通知您<br>
                请耐心等待管理员审核
              </p>
            </div>
          </template>
        </el-result>
      </div>
      
      <!-- 已拒绝状态 -->
      <div v-else-if="userInfo.verificationStatus === 'REJECTED'" class="rejected-status">
        <el-result icon="error" title="认证未通过" sub-title="您的实名认证未通过审核">
          <template #extra>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="学号/工号">{{ userInfo.studentId }}</el-descriptions-item>
              <el-descriptions-item label="用户类型">{{ userInfo.userType || '学生' }}</el-descriptions-item>
              <el-descriptions-item label="审核时间">
                {{ formatDate(userInfo.verificationAuditTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="拒绝原因">
                <el-alert
                  :title="userInfo.verificationAuditReason || '未提供拒绝原因'"
                  type="error"
                  :closable="false"
                  show-icon
                />
              </el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="showForm = true">
                修改信息后重新提交
              </el-button>
            </div>
          </template>
        </el-result>
        
        <!-- 重新提交表单 -->
        <div v-if="showForm" style="margin-top: 30px;">
          <verification-form
            :user-info="userInfo"
            @submit-success="handleSubmitSuccess"
            @cancel="showForm = false"
          />
        </div>
      </div>
      
      <!-- 未认证状态（显示认证表单） -->
      <div v-else>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #title>
            <div>
              <p style="font-weight: 500; margin-bottom: 8px;">实名认证说明</p>
              <ul style="margin: 0; padding-left: 20px;">
                <li>实名认证后可以发布闲置商品和跑腿任务</li>
                <li>实名认证后可以参与商品交易和接单跑腿</li>
                <li>未认证用户只能使用失物招领功能</li>
                <li>认证信息仅用于平台安全管理，不会泄露给其他用户</li>
                <li>提交后，管理员将在1-3个工作日内完成审核</li>
              </ul>
            </div>
          </template>
        </el-alert>
        
        <verification-form
          :user-info="userInfo"
          @submit-success="handleSubmitSuccess"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import VerificationForm from './components/VerificationForm.vue'

const userStore = useUserStore()
const userInfo = ref({})
const showForm = ref(false)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response.code === 200) {
      userInfo.value = response.data
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户信息失败')
  }
}

// 提交成功后的处理
const handleSubmitSuccess = async (updatedUser) => {
  userStore.setUserInfo(updatedUser)
  userInfo.value = updatedUser
  showForm.value = false
  await fetchUserInfo() // 重新获取最新信息
}

// 身份证号脱敏显示
const maskIdCard = (idCard) => {
  if (!idCard) return ''
  if (idCard.length !== 18) return idCard
  return idCard.substring(0, 6) + '********' + idCard.substring(14)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.verification-container {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-2xl);
  min-height: calc(100vh - var(--header-height));
  background-color: var(--color-bg-primary);
}

.verification-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-3xl) 0;
}

.verified-status,
.pending-status,
.rejected-status,
.admin-status {
  padding: 20px 0;
}

:deep(.el-alert) {
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-2xl);
}

:deep(.el-result__title) {
  color: var(--color-text-primary);
  font-size: 20px;
}

:deep(.el-result__subtitle) {
  color: var(--color-text-regular);
  font-size: 14px;
}

:deep(.el-descriptions) {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .verification-container {
    padding: 16px;
  }
  
  .verification-card {
    padding: 24px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
}
</style>
