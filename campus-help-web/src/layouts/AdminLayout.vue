<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <h1 class="logo-text">校园帮管理后台</h1>
          </div>
        </div>
        
        <div class="header-right">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回前台
          </el-button>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="getAvatarUrl(userStore.userInfo?.avatar)">
                {{ userStore.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="nickname">{{ userStore.nickname || userStore.email }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <div class="admin-container">
      <!-- 左侧菜单 -->
      <aside class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/verification">
            <el-icon><DocumentChecked /></el-icon>
            <span>实名认证审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/content" disabled>
            <el-icon><Files /></el-icon>
            <span>信息审核</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">开发中</el-tag>
          </el-menu-item>
          <el-menu-item index="/admin/statistics" disabled>
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">开发中</el-tag>
          </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 主内容区 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tools, ArrowLeft, ArrowDown, User, SwitchButton,
  DataAnalysis, DocumentChecked, UserFilled, Files, TrendCharts
} from '@element-plus/icons-vue'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const goBack = () => {
  router.push('/home')
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('退出成功')
        router.push('/login')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.admin-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.header-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 var(--content-padding);
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-bg-white);
  box-shadow: 0 2px 8px rgba(30, 136, 229, 0.3);
}

.logo-text {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--color-bg-primary);
}

.nickname {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

/* 主容器 */
.admin-container {
  display: flex;
  flex: 1;
  min-height: calc(100vh - var(--header-height));
}

/* 左侧菜单 */
.admin-sidebar {
  width: 240px;
  background-color: var(--color-bg-white);
  border-right: 1px solid var(--color-border);
  overflow-y: auto;
}

.admin-menu {
  border-right: none;
  padding: var(--spacing-md) 0;
}

.admin-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px var(--spacing-md);
  border-radius: var(--radius-md);
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: var(--color-bg-primary);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

/* 主内容区 */
.admin-content {
  flex: 1;
  padding: var(--spacing-2xl);
  overflow-y: auto;
  background-color: var(--color-bg-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px;
  }
  
  .admin-content {
    padding: var(--spacing-lg);
  }
}
</style>

