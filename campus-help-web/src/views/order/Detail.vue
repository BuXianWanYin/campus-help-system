<template>
  <div class="detail-container" v-loading="loading">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">订单详情</h1>
    </div>

    <div v-if="order" class="order-detail">
      <!-- 订单状态卡片 -->
      <el-card class="status-card" shadow="never">
        <div class="status-info">
          <div class="status-text">
            <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag>
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

      <!-- 物流信息（已发货时显示） -->
      <el-card v-if="order.status === 'SHIPPED' && order.trackingNumber" class="logistics-card" shadow="never">
        <template #header>
          <div class="card-header">物流信息</div>
        </template>
        <div class="logistics-info">
          <div class="info-row">
            <span class="label">物流公司：</span>
            <span class="value">{{ order.logisticsCompany || '未填写' }}</span>
          </div>
          <div class="info-row">
            <span class="label">快递单号：</span>
            <span class="value">{{ order.trackingNumber }}</span>
          </div>
          <div v-if="order.shipTime" class="info-row">
            <span class="label">发货时间：</span>
            <span class="value">{{ formatDateTime(order.shipTime) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 对方信息 -->
      <el-card class="user-card" shadow="never">
        <template #header>
          <div class="card-header">{{ isBuyer ? '卖家信息' : '买家信息' }}</div>
        </template>
        <div class="user-info">
          <el-avatar :size="60" :src="getAvatarUrl(getOtherUser()?.avatar)">
            {{ getOtherUser()?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-details">
            <div class="user-name">
              {{ getOtherUser()?.nickname || '未知用户' }}
              <el-tag v-if="getOtherUser()?.isVerified" type="success" size="small">已认证</el-tag>
            </div>
            <el-button type="primary" size="small" @click="goToChat">联系对方</el-button>
          </div>
        </div>
      </el-card>

      <!-- 时间线 -->
      <el-card class="timeline-card" shadow="never">
        <template #header>
          <div class="card-header">订单时间线</div>
        </template>
        <el-timeline>
          <el-timeline-item timestamp="创建订单" :icon="Clock">
            <p>{{ formatDateTime(order.createTime) }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="order.payTime" timestamp="付款时间" :icon="Money">
            <p>{{ formatDateTime(order.payTime) }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="order.shipTime" timestamp="发货时间" :icon="Truck">
            <p>{{ formatDateTime(order.shipTime) }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="order.completeTime" timestamp="完成时间" :icon="CircleCheck">
            <p>{{ formatDateTime(order.completeTime) }}</p>
          </el-timeline-item>
          <el-timeline-item v-if="order.cancelTime" timestamp="取消时间" :icon="Close">
            <p>{{ formatDateTime(order.cancelTime) }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-if="isBuyer && order.status === 'PENDING_PAYMENT'" type="primary" size="large" @click="handlePay">
          立即付款
        </el-button>
        <el-button 
          v-if="isSeller && order.status === 'PAID' && order.tradeMethod === 'MAIL'" 
          type="primary" 
          size="large" 
          @click="handleShip">
          发货
        </el-button>
        <el-button 
          v-if="isBuyer && (order.status === 'SHIPPED' || order.status === 'PENDING_PICKUP')" 
          type="success" 
          size="large" 
          @click="handleConfirmReceipt">
          确认收货
        </el-button>
        <el-button 
          v-if="order.status === 'PENDING_PAYMENT'" 
          type="danger" 
          size="large" 
          @click="handleCancel">
          取消订单
        </el-button>
      </div>
    </div>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="发货" width="500px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司" required>
          <el-input v-model="shipForm.logisticsCompany" placeholder="请输入物流公司名称" />
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Clock, Money, CircleCheck, Close } from '@element-plus/icons-vue'
import { orderApi, chatApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

defineOptions({
  name: 'OrderDetail'
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const order = ref(null)
const shipDialogVisible = ref(false)
const shipForm = ref({
  logisticsCompany: '',
  trackingNumber: ''
})

const isBuyer = computed(() => {
  return order.value && userStore.userInfo?.id === order.value.buyerId
})

/**
 * 获取订单详情
 */
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const response = await orderApi.getDetail(id)
    if (response.code === 200) {
      order.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
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
    const images = JSON.parse(goods.images)
    if (Array.isArray(images) && images.length > 0) {
      return getAvatarUrl(images[0])
    }
  } catch {
    // 解析失败
  }
  return 'https://via.placeholder.com/200x200?text=暂无图片'
}

/**
 * 获取对方用户信息
 */
const getOtherUser = () => {
  if (!order.value) return null
  if (isBuyer.value) {
    return order.value.seller || order.value.sellerUser
  } else {
    return order.value.buyer || order.value.buyerUser
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'PENDING_PAYMENT': '待付款',
    'PAID': '已付款',
    'SHIPPED': '已发货',
    'PENDING_PICKUP': '待自提',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

/**
 * 获取状态类型
 */
const getStatusType = (status) => {
  const typeMap = {
    'PENDING_PAYMENT': 'warning',
    'PAID': 'info',
    'SHIPPED': 'primary',
    'PENDING_PICKUP': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 格式化日期时间
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
 * 返回
 */
const goBack = () => {
  router.back()
}

/**
 * 跳转商品详情
 */
const goToGoodsDetail = (goodsId) => {
  router.push(`/goods/detail/${goodsId}`)
}

/**
 * 跳转聊天
 */
const goToChat = async () => {
  if (!order.value) {
    ElMessage.warning('订单信息不存在')
    return
  }
  
  // 如果订单有sessionId，直接使用
  if (order.value.sessionId) {
    router.push({
      path: '/user/chat',
      query: { sessionId: order.value.sessionId }
    })
    return
  }
  
  // 如果没有sessionId，需要创建或获取会话
  try {
    const otherUserId = isBuyer.value ? order.value.sellerId : order.value.buyerId
    if (!otherUserId) {
      ElMessage.warning('无法获取对方用户信息')
      return
    }
    
    const response = await chatApi.createOrGetSession({
      targetUserId: otherUserId,
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
    console.error('联系对方失败:', error)
    ElMessage.error('联系对方失败，请稍后重试')
  }
}

/**
 * 处理付款
 */
const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确定要支付此订单吗？', '确认付款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await orderApi.pay(order.value.id)
    ElMessage.success('付款成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('付款失败')
    }
  }
}

/**
 * 处理发货
 */
const handleShip = () => {
  shipForm.value = {
    logisticsCompany: '',
    trackingNumber: ''
  }
  shipDialogVisible.value = true
}

/**
 * 确认发货
 */
const confirmShip = async () => {
  if (!shipForm.value.logisticsCompany || !shipForm.value.trackingNumber) {
    ElMessage.warning('请填写物流公司和快递单号')
    return
  }
  
  try {
    await orderApi.ship(order.value.id, shipForm.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchOrderDetail()
  } catch (error) {
    ElMessage.error('发货失败')
  }
}

/**
 * 处理确认收货
 */
const handleConfirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认收货后，订单将完成，请确保已收到商品。', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await orderApi.confirmReceipt(order.value.id)
    ElMessage.success('确认收货成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

/**
 * 处理取消订单
 */
const handleCancel = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因'
    })
    
    await orderApi.cancel(order.value.id, reason)
    ElMessage.success('取消订单成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-card {
  background: linear-gradient(135deg, #1E88E5 0%, #42A5F5 100%);
  color: #FFFFFF;
}

.status-card :deep(.el-card__body) {
  color: #FFFFFF;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-text {
  font-size: 20px;
  font-weight: bold;
}

.order-no {
  font-size: 14px;
  opacity: 0.9;
}

.order-time {
  font-size: 14px;
  opacity: 0.9;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.goods-card {
  margin-top: 20px;
}

.goods-info {
  display: flex;
  gap: 20px;
}

.goods-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.goods-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goods-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
  cursor: pointer;
}

.goods-title:hover {
  color: #409EFF;
}

.goods-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.goods-price {
  display: flex;
  gap: 20px;
  font-size: 16px;
  color: #F56C6C;
  font-weight: 500;
}

.trade-info,
.address-info,
.pickup-info,
.logistics-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.label {
  color: #909399;
  width: 100px;
  flex-shrink: 0;
}

.value {
  color: #303133;
  flex: 1;
}

.total-row {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #E0E0E0;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-details {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}

.timeline-card {
  margin-top: 20px;
}
</style>

