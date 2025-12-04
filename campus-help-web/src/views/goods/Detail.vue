<template>
  <div class="detail-container" v-loading="loading">
    <el-empty v-if="!loading && !goods" description="商品不存在" />
    
    <div v-if="goods" class="detail-content">
      <!-- 返回按钮 -->
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      
      <!-- 商品详情卡片 -->
      <div class="detail-card">
        <!-- 图片区域 -->
        <div class="image-section">
          <el-image
            v-if="mainImage"
            :src="mainImage"
            :preview-src-list="imageList"
            fit="cover"
            class="main-image"
            preview-teleported
          />
          <div v-else class="no-image">暂无图片</div>
          
          <!-- 图片缩略图 -->
          <div v-if="imageList.length > 1" class="image-thumbs">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="thumb-item"
              :class="{ active: mainImage === img }"
              @click="mainImage = img"
            >
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>
        
        <!-- 信息区域 -->
        <div class="info-section">
          <div class="detail-header">
            <span class="status-badge" :class="getStatusClass(goods.status)">
              {{ getStatusText(goods.status) }}
            </span>
            <h1 class="detail-title">{{ goods.title }}</h1>
          </div>
          
          <div class="price-section">
            <span class="price-label">售价：</span>
            <span class="price-value">¥{{ goods.currentPrice }}</span>
          </div>
          
          <div class="detail-meta">
            <div class="meta-item">
              <el-icon><Folder /></el-icon>
              <span>分类：{{ goods.category }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Box /></el-icon>
              <span>成色：{{ goods.condition }}</span>
            </div>
            <div class="meta-item">
              <el-icon><ShoppingCart /></el-icon>
              <span>库存：{{ goods.stock }}件</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span>浏览：{{ goods.viewCount || 0 }}次</span>
            </div>
            <div class="meta-item">
              <el-icon><Truck /></el-icon>
              <span>交易方式：{{ goods.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}</span>
            </div>
            <div v-if="goods.tradeMethod === 'MAIL' && goods.shippingFee" class="meta-item">
              <el-icon><Money /></el-icon>
              <span>邮费：¥{{ goods.shippingFee }}</span>
            </div>
            <div v-if="goods.tradeMethod === 'FACE_TO_FACE' && goods.tradeLocation" class="meta-item">
              <el-icon><Location /></el-icon>
              <span>自提地点：{{ goods.tradeLocation }}</span>
            </div>
          </div>
          
          <div class="detail-description">
            <h3>商品描述</h3>
            <p>{{ goods.description }}</p>
          </div>
          
          <!-- 发布者信息 -->
          <div class="publisher-section">
            <div class="publisher-info">
              <el-avatar :size="48" :src="getAvatarUrl(goods.userAvatar)">
                {{ goods.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="publisher-details">
                <div class="publisher-name">
                  {{ goods.userName || '未知用户' }}
                  <el-tag v-if="goods.userIsVerified" type="success" size="small" style="margin-left: 8px">已认证</el-tag>
                </div>
                <div class="publish-time">发布于 {{ formatDateTime(goods.createTime) }}</div>
              </div>
            </div>
            <el-button v-if="!isSeller" type="primary" @click="handleContact">联系卖家</el-button>
          </div>
          
          <!-- 操作按钮 -->
          <div v-if="!isSeller && goods.status === 'ON_SALE'" class="action-section">
            <el-button type="primary" size="large" @click="handleBuy">
              立即购买
            </el-button>
            <div class="action-tip">
              购买前请确认商品信息和交易方式
            </div>
          </div>
          
          <!-- 发布者操作 -->
          <div v-if="isSeller" class="seller-actions">
            <el-button type="primary" @click="handleEdit">编辑商品</el-button>
            <el-button v-if="goods.status === 'ON_SALE'" @click="handleOffshelf">下架商品</el-button>
            <el-button v-if="goods.status === 'CLOSED'" @click="handleReshelf">重新上架</el-button>
            <el-button type="danger" @click="handleDelete">删除商品</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Folder, View, Box, ShoppingCart, Truck, Money } from '@element-plus/icons-vue'
import { goodsApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

defineOptions({
  name: 'GoodsDetail'
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const goods = ref(null)
const mainImage = ref('')
const imageList = ref([])

const isSeller = computed(() => {
  return goods.value && userStore.userInfo?.id === goods.value.userId
})

/**
 * 获取商品详情
 */
const fetchGoodsDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const response = await goodsApi.getDetail(id)
    if (response.code === 200) {
      goods.value = response.data
      const images = parseImages(goods.value.images)
      imageList.value = images.map(img => getAvatarUrl(img))
      if (imageList.value.length > 0) {
        mainImage.value = imageList.value[0]
      }
      
      // 填充用户信息
      if (goods.value.user) {
        goods.value.userAvatar = goods.value.user.avatar
        goods.value.userName = goods.value.user.nickname
        goods.value.userIsVerified = goods.value.user.isVerified
      }
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 解析图片JSON
 */
const parseImages = (imagesJson) => {
  if (!imagesJson) return []
  try {
    return JSON.parse(imagesJson)
  } catch {
    return []
  }
}

/**
 * 格式化时间
 */
const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'ON_SALE': '在售',
    'SOLD_OUT': '已售完',
    'CLOSED': '已关闭',
    'REJECTED': '已拒绝',
    'ADMIN_OFFSHELF': '已下架'
  }
  return statusMap[status] || status
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    'PENDING_REVIEW': 'badge-warning',
    'ON_SALE': 'badge-success',
    'SOLD_OUT': 'badge-info',
    'CLOSED': 'badge-info',
    'REJECTED': 'badge-danger',
    'ADMIN_OFFSHELF': 'badge-danger'
  }
  return classMap[status] || ''
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

/**
 * 处理购买
 */
const handleBuy = () => {
  // 检查实名认证
  if (!userStore.userInfo?.isVerified) {
    ElMessage.warning('购买商品需要实名认证，请先完成实名认证')
    router.push('/user/verification')
    return
  }
  // 跳转到购买页面或显示购买对话框
  ElMessage.info('购买功能开发中')
}

/**
 * 处理联系
 */
const handleContact = () => {
  // 跳转到聊天页面
  router.push('/user/chat')
}

/**
 * 处理编辑
 */
const handleEdit = () => {
  router.push(`/goods/edit/${goods.value.id}`)
}

/**
 * 处理下架
 */
const handleOffshelf = async () => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
      type: 'warning'
    })
    await goodsApi.offshelf(goods.value.id)
    ElMessage.success('下架成功')
    fetchGoodsDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
    }
  }
}

/**
 * 处理重新上架
 */
const handleReshelf = async () => {
  try {
    await goodsApi.reshelf(goods.value.id)
    ElMessage.success('上架成功')
    fetchGoodsDetail()
  } catch (error) {
    ElMessage.error('上架失败')
  }
}

/**
 * 处理删除
 */
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复', '提示', {
      type: 'warning'
    })
    await goodsApi.delete(goods.value.id)
    ElMessage.success('删除成功')
    router.push('/goods/list')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchGoodsDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  margin-bottom: 16px;
}

.detail-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  display: flex;
  gap: 24px;
}

.image-section {
  flex: 0 0 400px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  cursor: pointer;
}

.no-image {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F7FA;
  border-radius: 8px;
  color: #909399;
}

.image-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  overflow-x: auto;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.thumb-item.active {
  border-color: #409EFF;
}

.info-section {
  flex: 1;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
}

.badge-warning {
  background-color: #E6A23C;
}

.badge-success {
  background-color: #67C23A;
}

.badge-info {
  background-color: #909399;
}

.badge-danger {
  background-color: #F56C6C;
}

.detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  flex: 1;
}

.price-section {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #F5F7FA;
  border-radius: 8px;
}

.price-label {
  font-size: 16px;
  color: #606266;
}

.price-value {
  font-size: 32px;
  color: #F56C6C;
  font-weight: bold;
  margin-left: 8px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #F0F0F0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.detail-description {
  margin-bottom: 24px;
}

.detail-description h3 {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 12px 0;
}

.detail-description p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

.publisher-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #F5F7FA;
  border-radius: 8px;
  margin-bottom: 24px;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.publisher-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.action-section {
  text-align: center;
  padding: 24px;
  background-color: #F5F7FA;
  border-radius: 8px;
}

.action-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.seller-actions {
  display: flex;
  gap: 12px;
  padding: 24px;
  background-color: #F5F7FA;
  border-radius: 8px;
}
</style>

