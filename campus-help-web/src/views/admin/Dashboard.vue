<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2 class="page-title">数据概览</h2>
      <p class="page-subtitle">系统整体数据统计</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-blue">
          <el-icon :size="32"><UserFilled /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">总用户数</p>
          <h3 class="stat-value">{{ stats.totalUsers || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-orange">
          <el-icon :size="32"><DocumentChecked /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">待审核认证</p>
          <h3 class="stat-value">{{ stats.pendingVerifications || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-green">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">已认证用户</p>
          <h3 class="stat-value">{{ stats.verifiedUsers || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-red">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">已封禁用户</p>
          <h3 class="stat-value">{{ stats.bannedUsers || 0 }}</h3>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-cards">
        <el-card class="action-card" @click="goToVerification">
          <div class="action-icon icon-orange">
            <el-icon :size="24"><DocumentChecked /></el-icon>
          </div>
          <div class="action-content">
            <h4>实名认证审核</h4>
            <p>审核用户提交的实名认证申请</p>
          </div>
        </el-card>
        <el-card class="action-card" @click="goToUsers">
          <div class="action-icon icon-blue">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="action-content">
            <h4>用户管理</h4>
            <p>管理用户账号，包括封禁和解封</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, DocumentChecked, CircleCheck, Warning } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const router = useRouter()
const stats = ref({
  totalUsers: 0,
  pendingVerifications: 0,
  verifiedUsers: 0,
  bannedUsers: 0
})

// 获取统计数据
const fetchStats = async () => {
  try {
    // TODO: 实现统计数据接口
    // const response = await adminApi.getStats()
    // if (response.code === 200) {
    //   stats.value = response.data
    // }
  } catch (error) {
    ElMessage.error(error.message || '获取统计数据失败')
  }
}

// 跳转到实名认证审核
const goToVerification = () => {
  router.push('/admin/verification')
}

// 跳转到用户管理
const goToUsers = () => {
  router.push('/admin/users')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-page {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.stat-card {
  background-color: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.stat-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.stat-icon.icon-green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.stat-icon.icon-red {
  background-color: rgba(244, 67, 54, 0.1);
  color: var(--color-danger);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-xs) 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0;
}

.quick-actions {
  margin-top: var(--spacing-2xl);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.action-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.action-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.action-content {
  flex: 1;
}

.action-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.action-content p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}
</style>

