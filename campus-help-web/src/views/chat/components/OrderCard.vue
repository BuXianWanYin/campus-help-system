<template>
  <div class="order-card-compact" v-if="order">
    <div class="order-card-content">
      <!-- 商品图片 -->
      <div class="order-goods-image" @click="goToGoodsDetail">
        <img :src="getGoodsImage(order.goods)" :alt="order.goods?.title" />
      </div>
      
      <!-- 订单信息 -->
      <div class="order-info">
        <div class="order-header">
          <div class="order-title-text">{{ order.goods?.title || '商品' }}</div>
          <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
        </div>
        
        <div class="order-details">
          <div class="order-detail-item">
            <span class="detail-label">共{{ order.quantity }}件商品</span>
            <span class="detail-value">合计 ¥{{ (parseFloat(order.totalAmount) + parseFloat(order.shippingFee || 0)).toFixed(2) }}</span>
          </div>
          <div class="order-detail-item" v-if="order.orderNo">
            <span class="detail-label">订单号：</span>
            <span class="detail-value order-no">{{ order.orderNo }}</span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮区域 -->
      <div class="order-actions">
        <!-- 发送订单按钮（买家可见） -->
        <el-button 
          v-if="isBuyer && !isInMessage"
          type="primary" 
          size="small" 
          @click="handleSendOrder"
          class="send-order-btn">
          <el-icon><Promotion /></el-icon>
          <span>发送订单</span>
        </el-button>
        
        <!-- 卖家操作按钮 -->
        <template v-if="isSeller">
          <!-- 改价按钮（待付款状态） -->
          <el-button 
            v-if="order.status === 'PENDING_PAYMENT'"
            type="warning" 
            size="small" 
            @click="handleUpdatePrice"
            class="action-btn">
            <el-icon><Edit /></el-icon>
            <span>改价</span>
          </el-button>
          
          <!-- 发货按钮（已付款且邮寄方式） -->
          <el-button 
            v-if="order.status === 'PAID' && order.tradeMethod === 'MAIL'"
            type="primary" 
            size="small" 
            @click="handleShip"
            class="action-btn">
            <el-icon><Box /></el-icon>
            <span>发货</span>
          </el-button>
        </template>
        
        <!-- 买家操作按钮 -->
        <template v-if="isBuyer">
          <!-- 付款按钮 -->
          <el-button 
            v-if="order.status === 'PENDING_PAYMENT'"
            type="primary" 
            size="small" 
            @click="handlePay"
            class="action-btn">
            <el-icon><Wallet /></el-icon>
            <span>立即付款</span>
          </el-button>
          
          <!-- 确认收货按钮 -->
          <el-button 
            v-if="order.status === 'SHIPPED' || order.status === 'PENDING_PICKUP'"
            type="success" 
            size="small" 
            @click="handleConfirmReceipt"
            class="action-btn">
            <el-icon><CircleCheck /></el-icon>
            <span>确认收货</span>
          </el-button>
        </template>
        
        <!-- 查看详情按钮 -->
        <el-button 
          type="text" 
          size="small" 
          @click="goToOrderDetail"
          class="detail-btn">
          查看详情
        </el-button>
      </div>
    </div>
    
    <!-- 改价对话框 -->
    <el-dialog v-model="priceDialogVisible" title="修改订单价格" width="400px">
      <el-form :model="priceForm" label-width="100px">
        <el-form-item label="原价">
          <el-input :value="`¥${order.price}`" disabled />
        </el-form-item>
        <el-form-item label="新价格" required>
          <el-input-number 
            v-model="priceForm.newPrice" 
            :min="0.01" 
            :precision="2"
            :step="1"
            style="width: 100%"
            placeholder="请输入新价格" />
        </el-form-item>
        <el-form-item label="改价原因">
          <el-input 
            v-model="priceForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入改价原因（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdatePrice">确定</el-button>
      </template>
    </el-dialog>
    
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, Edit, Box, Wallet, CircleCheck } from '@element-plus/icons-vue'
import { orderApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  order: {
    type: Object,
    default: null
  },
  // 是否在消息中显示（用于区分是固定卡片还是消息中的卡片）
  isInMessage: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update-order', 'send-order'])

const router = useRouter()
const userStore = useUserStore()

const priceDialogVisible = ref(false)
const priceForm = ref({
  newPrice: null,
  reason: ''
})

const shipDialogVisible = ref(false)
const shipForm = ref({
  logisticsCompany: '',
  trackingNumber: ''
})

const isBuyer = computed(() => {
  if (!props.order || !userStore.userInfo?.id) {
    return false
  }
  // 确保类型一致（都转换为数字进行比较）
  const currentUserId = Number(userStore.userInfo.id)
  const buyerId = props.order.buyerId ? Number(props.order.buyerId) : null
  if (buyerId === null) {
    return false
  }
  return currentUserId === buyerId
})

const isSeller = computed(() => {
  if (!props.order || !userStore.userInfo?.id) {
    return false
  }
  // 确保类型一致（都转换为数字进行比较）
  const currentUserId = Number(userStore.userInfo.id)
  const sellerId = props.order.sellerId ? Number(props.order.sellerId) : null
  if (sellerId === null) {
    return false
  }
  const result = currentUserId === sellerId
  // 调试日志（生产环境可移除）
  if (process.env.NODE_ENV === 'development') {
    console.log('OrderCard isSeller check:', {
      currentUserId,
      sellerId,
      orderBuyerId: props.order.buyerId,
      orderSellerId: props.order.sellerId,
      orderStatus: props.order.status,
      isInMessage: props.isInMessage,
      result,
      userInfo: userStore.userInfo
    })
  }
  return result
})

/**
 * 获取商品图片
 */
const getGoodsImage = (goods) => {
  if (!goods || !goods.images) return 'https://via.placeholder.com/80x80?text=暂无图片'
  try {
    const images = JSON.parse(goods.images)
    if (Array.isArray(images) && images.length > 0) {
      return getAvatarUrl(images[0])
    }
  } catch {
    // 解析失败
  }
  return 'https://via.placeholder.com/80x80?text=暂无图片'
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
 * 跳转商品详情
 */
const goToGoodsDetail = () => {
  if (props.order?.goodsId) {
    router.push(`/goods/detail/${props.order.goodsId}`)
  }
}

/**
 * 跳转订单详情
 */
const goToOrderDetail = () => {
  if (props.order?.id) {
    router.push(`/order/detail/${props.order.id}`)
  }
}

/**
 * 发送订单
 */
const handleSendOrder = () => {
  emit('send-order', props.order)
}

/**
 * 处理改价
 */
const handleUpdatePrice = () => {
  priceForm.value = {
    newPrice: parseFloat(props.order.price),
    reason: ''
  }
  priceDialogVisible.value = true
}

/**
 * 确认改价
 */
const confirmUpdatePrice = async () => {
  if (!priceForm.value.newPrice || priceForm.value.newPrice <= 0) {
    ElMessage.warning('请输入有效的新价格')
    return
  }
  
  if (priceForm.value.newPrice === parseFloat(props.order.price)) {
    ElMessage.warning('新价格不能与原价相同')
    return
  }
  
  try {
    await orderApi.updatePrice(props.order.id, {
      newPrice: priceForm.value.newPrice,
      reason: priceForm.value.reason
    })
    ElMessage.success('改价成功')
    priceDialogVisible.value = false
    emit('update-order')
  } catch (error) {
    ElMessage.error(error.message || '改价失败')
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
    
    await orderApi.pay(props.order.id)
    ElMessage.success('付款成功')
    emit('update-order')
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
    await orderApi.ship(props.order.id, shipForm.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    emit('update-order')
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
    
    await orderApi.confirmReceipt(props.order.id)
    ElMessage.success('确认收货成功')
    emit('update-order')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}
</script>

<style scoped>
.order-card-compact {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 10px 12px;
  margin-bottom: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
}

.order-card-compact:hover {
  background-color: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12), 0 2px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.order-card-content {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.order-goods-image {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background-color: var(--color-bg-primary);
}

.order-goods-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.order-title-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.order-detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  line-height: 1.3;
}

.detail-label {
  color: var(--color-text-secondary);
}

.detail-value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.detail-value.order-no {
  font-family: monospace;
  font-size: 11px;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex-shrink: 0;
  align-items: flex-end;
}

.send-order-btn,
.action-btn {
  width: 90px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0 8px;
  font-size: 12px;
}

.send-order-btn :deep(.el-icon),
.action-btn :deep(.el-icon) {
  font-size: 13px;
  margin-right: 0;
}

.send-order-btn span,
.action-btn span {
  font-size: 11px;
  line-height: 1;
}

.detail-btn {
  padding: 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.detail-btn:hover {
  color: var(--color-primary);
}

/* 响应式 */
@media (max-width: 768px) {
  .order-card-content {
    flex-wrap: wrap;
  }
  
  .order-actions {
    flex-direction: row;
    width: 100%;
    justify-content: flex-end;
  }
  
  .send-order-btn,
  .action-btn {
    flex: 1;
    min-width: 0;
  }
}
</style>
