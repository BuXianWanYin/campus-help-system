<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用{{ appConfig.title || '校园帮系统' }}</span>
        </div>
      </template>
      <div class="welcome-content">
        <p>您好，{{ userStore.nickname || userStore.email }}！</p>
        <p>欢迎来到{{ appConfig.title || '校园帮系统' }}，这里可以帮您解决失物招领、闲置交易、跑腿服务等校园互助需求。</p>
      </div>
    </el-card>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="feature-card" @click="goToLostFound">
          <div class="feature-content">
            <el-icon class="feature-icon" :size="48"><Search /></el-icon>
            <h3>失物招领</h3>
            <p>发布失物信息，寻找丢失物品</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="feature-card" @click="goToGoods">
          <div class="feature-content">
            <el-icon class="feature-icon" :size="48"><ShoppingBag /></el-icon>
            <h3>闲置交易</h3>
            <p>买卖闲置物品，让资源再利用</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="feature-card" @click="goToTask">
          <div class="feature-content">
            <el-icon class="feature-icon" :size="48"><Box /></el-icon>
            <h3>跑腿服务</h3>
            <p>发布或接取跑腿任务，互帮互助</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search, ShoppingBag, Box } from '@element-plus/icons-vue'
import appConfig from '@/config'

const router = useRouter()
const userStore = useUserStore()

const goToLostFound = () => {
  router.push('/lost-found/list')
}

const goToGoods = () => {
  router.push('/goods/list')
}

const goToTask = () => {
  router.push('/task/list')
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  background-color: #FFFFFF;
  border: 1px solid #b9d7ea;
  box-shadow: 0 2px 12px 0 rgba(118, 159, 205, 0.1);
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-content p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.feature-card {
  background-color: #FFFFFF;
  border: 1px solid #b9d7ea;
  box-shadow: 0 2px 12px 0 rgba(118, 159, 205, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.feature-card:hover {
  border-color: #769fcd;
  box-shadow: 0 4px 16px 0 rgba(118, 159, 205, 0.2);
  transform: translateY(-2px);
}

.feature-content {
  text-align: center;
  padding: 20px;
}

.feature-icon {
  color: #769fcd;
  margin-bottom: 16px;
}

.feature-content h3 {
  margin: 12px 0;
  font-size: 18px;
  color: #303133;
}

.feature-content p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}
</style>
