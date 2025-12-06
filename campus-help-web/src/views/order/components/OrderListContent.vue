<template>
  <div class="order-list-content">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="待付款" value="PENDING_PAYMENT" />
            <el-option label="已付款" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="待自提" value="PENDING_PICKUP" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易方式：">
          <el-select v-model="filters.tradeMethod" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="邮寄" value="MAIL" />
            <el-option label="自提" value="FACE_TO_FACE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词：">
          <el-input v-model="filters.keyword" placeholder="订单号、商品标题" style="width: 200px" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div v-for="order in orderList" :key="order.id" class="order-card" @click="goToDetail(order.id)">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
          </div>
          <div class="order-time">{{ formatDateTime(order.createTime) }}</div>
        </div>
        
        <div class="order-body">
          <div class="goods-info">
            <img :src="getGoodsImage(order.goods)" :alt="order.goods?.title" class="goods-image" />
            <div class="goods-details">
              <h4 class="goods-title">{{ order.goods?.title }}</h4>
              <div class="goods-meta">
                <span>数量：{{ order.quantity }}件</span>
                <span>单价：¥{{ order.price }}</span>
              </div>
            </div>
          </div>
          
          <div class="order-amount">
            <div class="amount-item">
              <span>商品金额：</span>
              <span>¥{{ order.totalAmount }}</span>
            </div>
            <div v-if="order.shippingFee > 0" class="amount-item">
              <span>邮费：</span>
              <span>¥{{ order.shippingFee }}</span>
            </div>
            <div class="amount-total">
              <span>合计：</span>
              <span class="total-price">¥{{ (parseFloat(order.totalAmount) + parseFloat(order.shippingFee || 0)).toFixed(2) }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="other-user">
            <el-avatar :size="24" :src="getAvatarUrl(getOtherUser(order)?.avatar)">
              {{ getOtherUser(order)?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span v-if="role === 'SELLER'" class="user-label">买家：</span>
            <span v-else class="user-label">卖家：</span>
            <span>{{ getOtherUser(order)?.nickname || '未知用户' }}</span>
          </div>
          <div class="order-actions">
            <el-button v-if="role === 'BUYER' && order.status === 'PENDING_PAYMENT'" type="primary" size="small" @click.stop="handlePay(order)">
              付款
            </el-button>
            <el-button v-if="role === 'SELLER' && order.status === 'PAID' && order.tradeMethod === 'MAIL'" type="primary" size="small" @click.stop="handleShip(order)">
              发货
            </el-button>
            <el-button v-if="role === 'BUYER' && (order.status === 'SHIPPED' || order.status === 'PENDING_PICKUP')" type="success" size="small" @click.stop="handleConfirmReceipt(order)">
              确认收货
            </el-button>
            <el-button v-if="order.status === 'PENDING_PAYMENT'" type="danger" size="small" @click.stop="handleCancel(order)">
              取消订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />

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

    <!-- 分页器 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

/**
 * 订单列表内容组件
 * 展示订单列表，支持筛选、分页、订单操作等功能
 */

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (value) => ['BUYER', 'SELLER'].includes(value)
  },
  filters: {
    type: Object,
    default: () => ({})
  }
})

const router = useRouter()

const loading = ref(false)
const orderList = ref([])
const total = ref(0)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 获取订单列表
 */
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      role: props.role,
      status: props.filters.status || undefined,
      tradeMethod: props.filters.tradeMethod || undefined,
      keyword: props.filters.keyword || undefined
    }
    
    const response = await orderApi.getList(params)
    if (response.code === 200) {
      const pageData = response.data
      orderList.value = pageData.records || []
      total.value = pageData.total || 0
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取商品图片
 */
const getGoodsImage = (goods) => {
  if (!goods) return 'https://via.placeholder.com/100x100?text=暂无图片'
  const images = parseImages(goods.images)
  if (images.length > 0) {
    return getAvatarUrl(images[0])
  }
  return 'https://via.placeholder.com/100x100?text=暂无图片'
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
 * 获取对方用户信息
 */
const getOtherUser = (order) => {
  if (props.role === 'BUYER') {
    return order.seller
  } else {
    return order.buyer
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
    'PENDING_PAYMENT': '待付款',
    'PAID': '已付款',
    'SHIPPED': '已发货',
    'PENDING_PICKUP': '待自提',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
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
    'CANCELLED': 'danger',
    'REFUNDED': 'info'
  }
  return typeMap[status] || ''
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.pageNum = 1
  fetchOrderList()
}

/**
 * 跳转详情
 */
const goToDetail = (id) => {
  router.push(`/order/detail/${id}`)
}

/**
 * 处理付款
 */
const handlePay = async (order) => {
  try {
    await orderApi.pay(order.id)
    ElMessage.success('付款成功')
    fetchOrderList()
  } catch (error) {
    ElMessage.error('付款失败')
  }
}

// 发货对话框相关
const shipDialogVisible = ref(false)
const currentShipOrder = ref(null)
const shipForm = reactive({
  logisticsCompany: '',
  trackingNumber: ''
})

/**
 * 处理发货
 */
const handleShip = (order) => {
  currentShipOrder.value = order
  shipForm.logisticsCompany = ''
  shipForm.trackingNumber = ''
  shipDialogVisible.value = true
}

/**
 * 确认发货
 */
const confirmShip = async () => {
  if (!shipForm.logisticsCompany || !shipForm.trackingNumber) {
    ElMessage.warning('请填写物流公司和快递单号')
    return
  }
  
  if (!currentShipOrder.value) return
  
  try {
    await orderApi.ship(currentShipOrder.value.id, {
      logisticsCompany: shipForm.logisticsCompany.trim(),
      trackingNumber: shipForm.trackingNumber.trim()
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchOrderList()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发货失败')
  }
}

/**
 * 处理确认收货
 */
const handleConfirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      type: 'warning'
    })
    await orderApi.confirmReceipt(order.id)
    ElMessage.success('确认收货成功')
    fetchOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

/**
 * 处理取消订单
 */
const handleCancel = async (order) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因'
    })
    
    await orderApi.cancel(order.id, reason)
    ElMessage.success('取消订单成功')
    fetchOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

/**
 * 处理分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchOrderList()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchOrderList()
}

onActivated(() => {
  fetchOrderList()
})

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.order-list-content {
  padding: 20px 0;
}

.filter-bar {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.order-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F0F0F0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  color: #909399;
}

.order-body {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.goods-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.goods-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.goods-details {
  flex: 1;
}

.goods-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.goods-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.order-amount {
  width: 200px;
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-item {
  font-size: 14px;
  color: #606266;
}

.amount-total {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  padding-top: 8px;
  border-top: 1px solid #F0F0F0;
}

.total-price {
  color: #F56C6C;
  font-size: 18px;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #F0F0F0;
}

.other-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.user-label {
  font-weight: 500;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>

