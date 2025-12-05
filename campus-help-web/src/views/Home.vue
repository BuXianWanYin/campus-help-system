<template>
  <div class="home-container">
    <div class="welcome-card">
      <h1 class="welcome-title">欢迎使用{{ appConfig.title || '校园帮系统' }}</h1>
      <div class="welcome-content">
        <p class="welcome-greeting">您好，{{ userStore.nickname || userStore.email }}！</p>
        <p class="welcome-desc">欢迎来到{{ appConfig.title || '校园帮系统' }}，这里可以帮您解决失物招领、闲置交易、学习互助等校园互助需求。</p>
      </div>
    </div>
    
    <div class="feature-cards">
      <div class="feature-card" @click="goToLostFound">
        <div class="feature-icon">
          <el-icon :size="40"><Search /></el-icon>
        </div>
        <div class="feature-title">失物招领</div>
        <div class="feature-desc">发布失物信息<br/>寻找丢失物品</div>
      </div>
      <div class="feature-card" @click="goToGoods">
        <div class="feature-icon">
          <el-icon :size="40"><ShoppingBag /></el-icon>
        </div>
        <div class="feature-title">闲置交易</div>
        <div class="feature-desc">买卖闲置物品<br/>让资源再利用</div>
      </div>
      <div class="feature-card" @click="goToStudy">
        <div class="feature-icon">
          <el-icon :size="40"><Box /></el-icon>
        </div>
        <div class="feature-title">学习互助</div>
        <div class="feature-desc">发布学习问题<br/>互帮互助</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search, ShoppingBag, Box } from '@element-plus/icons-vue'
import appConfig from '@/config'

// 定义组件名称，用于 keep-alive
defineOptions({
  name: 'Home'
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 组件激活时（从其他页面返回时），通知 MainLayout 刷新数据
onActivated(() => {
  // 延迟一下确保页面已渲染
  nextTick(() => {
    // 通知 MainLayout 刷新首页的失物招领数据
    window.dispatchEvent(new CustomEvent('refresh-home-data'))
  })
})

onMounted(() => {
  // 首次加载时也通知刷新
  nextTick(() => {
    window.dispatchEvent(new CustomEvent('refresh-home-data'))
  })
})

const goToLostFound = () => {
  router.push('/lost-found/list')
}

const goToGoods = () => {
  router.push('/goods/list')
}

const goToStudy = () => {
  router.push('/study/list')
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #b9d7ea;
}

.welcome-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 20px 0;
}

.welcome-content {
  padding: 0;
}

.welcome-greeting {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.welcome-desc {
  font-size: 16px;
  color: #606266;
  margin: 0;
  line-height: 1.8;
}

.feature-cards {
  display: flex;
  justify-content: space-around;
  gap: 30px;
  flex-wrap: wrap;
}

.feature-card {
  flex: 1;
  min-width: 280px;
  max-width: 360px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  border: 1px solid #b9d7ea;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: #769fcd;
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.feature-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.feature-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .home-container {
    padding: 16px;
  }
  
  .welcome-card {
    padding: 24px;
    margin-bottom: 30px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .feature-cards {
    flex-direction: column;
    gap: 20px;
  }
  
  .feature-card {
    max-width: 100%;
  }
}
</style>
