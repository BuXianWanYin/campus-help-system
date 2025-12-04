<template>
  <div class="order-card" v-if="order">
    <div class="order-card-header">
      <div class="order-title">
        <el-icon><ShoppingBag /></el-icon>
        <span>订单信息</span>
      </div>
      <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
    </div>
    
    <div class="order-card-body">
      <!-- 订单号 -->
      <div class="order-info-row">
        <span class="label">订单号：</span>
        <span class="value">{{ order.orderNo }}</span>
      </div>
      
      <!-- 商品信息 -->
      <div class="goods-info" @click="goToGoodsDetail">
        <img :src="getGoodsImage(order.goods)" :alt="order.goods?.title" class="goods-image" />
        <div class="goods-details">
          <div class="goods-title">{{ order.goods?.title }}</div>
          <div class="goods-meta">
            <span>数量：{{ order.quantity }}件</span>
            <span>单价：¥{{ order.price }}</span>
          </div>
        </div>
      </div>
      
      <!-- 交易方式 -->
      <div class="order-info-row">
        <span class="label">交易方式：</span>
        <span class="value">{{ order.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}</span>
      </div>
      
      <!-- 邮寄信息（邮寄时显示） -->
      <div v-if="order.tradeMethod === 'MAIL' && order.receiverName" class="order-info-row">
        <span class="label">收货地址：</span>
        <span class="value">{{ order.receiverAddress }}</span>
      </div>
      
      <!-- 自提信息（自提时显示） -->
      <div v-if="order.tradeMethod === 'FACE_TO_FACE' && order.pickupLocation" class="order-info-row">
        <span class="label">自提地点：</span>
        <span class="value">{{ order.pickupLocation }}</span>
      </div>
      
      <!-- 物流信息（已发货时显示） -->
      <div v-if="order.status === 'SHIPPED' && order.trackingNumber" class="order-info-row">
        <span class="label">物流单号：</span>
        <span class="value">{{ order.trackingNumber }}</span>
        <span v-if="order.logisticsCompany" class="value">（{{ order.logisticsCompany }}）</span>
      </div>
      
      <!-- 订单金额 -->
      <div class="order-amount">
        <div class="amount-row">
          <span>商品金额：</span>
          <span>¥{{ order.totalAmount }}</span>
        </div>
        <div v-if="order.shippingFee > 0" class="amount-row">
          <span>邮费：</span>
          <span>¥{{ order.shippingFee }}</span>
        </div>
        <div class="amount-total">
          <span>合计：</span>
          <span class="total-price">¥{{ (parseFloat(order.totalAmount) + parseFloat(order.shippingFee || 0)).toFixed(2) }}</span>
        </div>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="order-card-actions">
      <el-button 
        v-if="isBuyer && order.status === 'PENDING_PAYMENT'" 
        type="primary" 
        size="small" 
        @click="handlePay">
        立即付款
      </el-button>
      <el-button 
        v-if="isSeller && order.status === 'PAID' && order.tradeMethod === 'MAIL'" 
        type="primary" 
        size="small" 
        @click="handleShip">
        发货
      </el-button>
      <el-button 
        v-if="isBuyer && (order.status === 'SHIPPED' || order.status === 'PENDING_PICKUP')" 
        type="success" 
        size="small" 
        @click="handleConfirmReceipt">
        确认收货
      </el-button>
      <el-button 
        v-if="order.status === 'PENDING_PAYMENT'" 
        type="danger" 
        size="small" 
        @click="handleCancel">
        取消订单
      </el-button>
      <el-button 
        type="text" 
        size="small" 
        @click="goToOrderDetail">
        查看详情
      </el-button>
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingBag } from '@element-plus/icons-vue'
import { orderApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update-order'])

const router = useRouter()
const userStore = useUserStore()

const shipDialogVisible = ref(false)
const shipForm = ref({
  logisticsCompany: '',
  trackingNumber: ''
})

const isBuyer = computed(() => {
  return props.order && userStore.userInfo?.id === props.order.buyerId
})

const isSeller = computed(() => {
  return props.order && userStore.userInfo?.id === props.order.sellerId
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
    
    await orderApi.cancel(props.order.id, reason)
    ElMessage.success('取消订单成功')
    emit('update-order')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}
</script>

<style scoped>
.order-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #E0E0E0;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F0F0F0;
}

.order-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.order-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.value {
  color: #303133;
  flex: 1;
}

.goods-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #F5F5F5;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.goods-info:hover {
  background-color: #EEEEEE;
}

.goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.goods-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.goods-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.order-amount {
  padding: 12px;
  background-color: #F9F9F9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.amount-total {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  padding-top: 8px;
  border-top: 1px solid #E0E0E0;
}

.total-price {
  color: #F56C6C;
  font-size: 18px;
  font-weight: bold;
}

.order-card-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #F0F0F0;
  flex-wrap: wrap;
}
</style>

