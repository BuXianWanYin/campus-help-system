<template>
  <div class="pay-container" v-loading="loading">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">订单支付</h1>
    </div>

    <div v-if="order" class="pay-content">
      <!-- 订单状态卡片 -->
      <el-card class="status-card" shadow="never">
        <div class="status-info">
          <div class="status-text">
            <el-tag type="warning" size="large">待付款</el-tag>
          </div>
          <div class="order-no">订单号：{{ order.orderNo }}</div>
          <div class="order-time">创建时间：{{ formatDateTime(order.createTime) }}</div>
        </div>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="goods-card" shadow="never">
        <template #header>
          <div class="card-header">商品信息</div>
        </template>
        <div class="goods-info">
          <img :src="getGoodsImage(order.goods)" :alt="order.goods?.title" class="goods-image" />
          <div class="goods-details">
            <h3 class="goods-title" @click="goToGoodsDetail(order.goodsId)">{{ order.goods?.title }}</h3>
            <div class="goods-meta">
              <span>分类：{{ order.goods?.category }}</span>
              <span>成色：{{ order.goods?.condition }}</span>
            </div>
            <div class="goods-price">
              <span>单价：¥{{ order.price }}</span>
              <span>数量：{{ order.quantity }}件</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 交易信息 -->
      <el-card class="trade-card" shadow="never">
        <template #header>
          <div class="card-header">交易信息</div>
        </template>
        <div class="trade-info">
          <div class="info-row">
            <span class="label">交易方式：</span>
            <span class="value">{{ order.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}</span>
          </div>
          <div class="info-row">
            <span class="label">商品金额：</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
          <div v-if="order.shippingFee > 0" class="info-row">
            <span class="label">邮费：</span>
            <span class="value">¥{{ order.shippingFee }}</span>
          </div>
          <div class="info-row total-row">
            <span class="label">合计：</span>
            <span class="value total-price">¥{{ (parseFloat(order.totalAmount) + parseFloat(order.shippingFee || 0)).toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 收货地址（邮寄时显示） -->
      <el-card v-if="order.tradeMethod === 'MAIL' && order.receiverName" class="address-card" shadow="never">
        <template #header>
          <div class="card-header">收货地址</div>
        </template>
        <div class="address-info">
          <div class="info-row">
            <span class="label">收货人：</span>
            <span class="value">{{ order.receiverName }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话：</span>
            <span class="value">{{ order.receiverPhone }}</span>
          </div>
          <div class="info-row">
            <span class="label">收货地址：</span>
            <span class="value">{{ order.receiverAddress }}</span>
          </div>
        </div>
      </el-card>

      <!-- 自提地点（自提时显示） -->
      <el-card v-if="order.tradeMethod === 'FACE_TO_FACE' && order.pickupLocation" class="pickup-card" shadow="never">
        <template #header>
          <div class="card-header">自提地点</div>
        </template>
        <div class="pickup-info">
          <div class="info-row">
            <span class="label">自提地点：</span>
            <span class="value">{{ order.pickupLocation }}</span>
          </div>
        </div>
      </el-card>

      <!-- 卖家信息 -->
      <el-card class="seller-card" shadow="never">
        <template #header>
          <div class="card-header">卖家信息</div>
        </template>
        <div class="seller-info">
          <el-avatar :size="40" :src="getAvatarUrl(order.seller?.avatar)">
            {{ order.seller?.nickname?.charAt(0) || 'S' }}
          </el-avatar>
          <div class="seller-details">
            <div class="seller-name">{{ order.seller?.nickname || '未知用户' }}</div>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" size="large" @click="handlePay" :loading="paying">
          确认支付
        </el-button>
        <el-button type="danger" size="large" @click="handleCancel">
          取消订单
        </el-button>
        <el-button type="info" size="large" @click="handleContact">
          联系TA
        </el-button>
      </div>
    </div>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="500px">
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="取消原因" required>
          <el-input 
            v-model="cancelForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入取消原因" 
            maxlength="500"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { orderApi, chatApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const paying = ref(false)
const order = ref(null)
const cancelDialogVisible = ref(false)
const cancelForm = reactive({
  reason: ''
})

/**
 * 加载订单详情
 */
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = parseInt(route.params.id)
    const response = await orderApi.getDetail(orderId)
    if (response.code === 200) {
      order.value = response.data
      
      // 验证订单状态
      if (order.value.status !== 'PENDING_PAYMENT') {
        ElMessage.warning('订单状态已变更，无法支付')
        router.push(`/order/detail/${orderId}`)
        return
      }
    } else {
      ElMessage.error('获取订单详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

/**
 * 获取商品图片
 */
const getGoodsImage = (goods) => {
  if (!goods || !goods.images) return 'https://via.placeholder.com/200x200?text=暂无图片'
  try {
    const images = typeof goods.images === 'string' ? JSON.parse(goods.images) : goods.images
    if (Array.isArray(images) && images.length > 0) {
      return getAvatarUrl(images[0])
    }
  } catch {
    // 解析失败
  }
  return 'https://via.placeholder.com/200x200?text=暂无图片'
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
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 跳转到商品详情
 */
const goToGoodsDetail = (goodsId) => {
  router.push(`/goods/detail/${goodsId}`)
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

/**
 * 处理支付
 */
const handlePay = async () => {
  if (!order.value) return
  
  try {
    await ElMessageBox.confirm('确定要支付此订单吗？', '确认付款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    paying.value = true
    try {
      await orderApi.pay(order.value.id)
      ElMessage.success('付款成功')
      // 跳转到订单详情页
      router.push(`/order/detail/${order.value.id}`)
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '付款失败')
    } finally {
      paying.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  }
}

/**
 * 处理取消订单
 */
const handleCancel = () => {
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

/**
 * 确认取消订单
 */
const confirmCancel = async () => {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  
  try {
    await orderApi.cancel(order.value.id, cancelForm.reason.trim())
    ElMessage.success('取消订单成功')
    cancelDialogVisible.value = false
    // 跳转到订单详情页
    router.push(`/order/detail/${order.value.id}`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '取消订单失败')
  }
}

/**
 * 处理联系卖家
 */
const handleContact = async () => {
  if (!order.value) return
  
  try {
    // 创建或获取会话
    const response = await chatApi.createOrGetSession({
      targetUserId: order.value.sellerId,
      relatedType: 'GOODS',
      relatedId: order.value.goodsId
    })
    
    if (response.code === 200) {
      const sessionId = response.data.sessionId || response.data
      router.push({
        path: '/user/chat',
        query: { sessionId }
      })
    } else {
      ElMessage.error(response.message || '创建会话失败')
    }
  } catch (error) {
    console.error('联系卖家失败:', error)
    ElMessage.error('联系卖家失败，请稍后重试')
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.pay-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 200px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.pay-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-card,
.goods-card,
.trade-card,
.address-card,
.pickup-card,
.seller-card {
  border-radius: 8px;
  border: 1px solid #E0E0E0;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-text {
  font-size: 18px;
}

.order-no {
  font-size: 14px;
  color: #606266;
}

.order-time {
  font-size: 14px;
  color: #909399;
}

.goods-info {
  display: flex;
  gap: 16px;
}

.goods-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
  cursor: pointer;
}

.goods-details {
  flex: 1;
}

.goods-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 12px 0;
  cursor: pointer;
}

.goods-title:hover {
  color: #409EFF;
}

.goods-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.goods-price {
  display: flex;
  gap: 24px;
  font-size: 16px;
  color: #303133;
}

.trade-info,
.address-info,
.pickup-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.label {
  width: 100px;
  color: #909399;
}

.value {
  flex: 1;
  color: #303133;
}

.total-row {
  padding-top: 12px;
  border-top: 1px solid #F0F0F0;
  font-size: 18px;
  font-weight: bold;
}

.total-price {
  color: #F56C6C;
  font-size: 24px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-details {
  flex: 1;
}

.seller-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.action-bar {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 24px;
  background-color: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.action-bar .el-button {
  min-width: 120px;
}

@media (max-width: 768px) {
  .pay-container {
    padding: 16px;
  }
  
  .goods-info {
    flex-direction: column;
  }
  
  .goods-image {
    width: 100%;
    max-width: 300px;
  }
  
  .action-bar {
    flex-direction: column;
  }
  
  .action-bar .el-button {
    width: 100%;
  }
}
</style>

