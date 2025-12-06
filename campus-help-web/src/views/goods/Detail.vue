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
              <el-icon><Box /></el-icon>
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
          
          <!-- 地址表单对话框 -->
          <el-dialog
            v-model="addressDialogVisible"
            title="添加收货地址"
            width="600px"
            :close-on-click-modal="false"
          >
            <el-form
              ref="addressFormRef"
              :model="addressForm"
              :rules="addressRules"
              label-width="100px"
            >
              <el-form-item label="收货人姓名" prop="receiverName">
                <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" maxlength="50" />
              </el-form-item>
              <el-form-item label="收货人电话" prop="receiverPhone">
                <el-input v-model="addressForm.receiverPhone" placeholder="请输入收货人电话" maxlength="11" />
              </el-form-item>
              <el-form-item label="所在地区" prop="province">
                <el-row :gutter="12">
                  <el-col :span="8">
                    <el-input v-model="addressForm.province" placeholder="省份" />
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="addressForm.city" placeholder="城市" />
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="addressForm.district" placeholder="区县" />
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="详细地址" prop="detailAddress">
                <el-input 
                  v-model="addressForm.detailAddress" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入详细地址" 
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="邮政编码" prop="postalCode">
                <el-input v-model="addressForm.postalCode" placeholder="选填，6位数字" maxlength="6" />
              </el-form-item>
              <el-form-item label="默认地址">
                <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="addressDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSubmitAddress" :loading="addressSubmitting">确定</el-button>
            </template>
          </el-dialog>
          
          <!-- 购买对话框 -->
          <el-dialog
            v-model="buyDialogVisible"
            title="立即购买"
            width="600px"
            :close-on-click-modal="false"
          >
            <el-form :model="buyForm" :rules="buyRules" ref="buyFormRef" label-width="100px">
              <el-form-item label="商品信息">
                <div class="buy-goods-info">
                  <img :src="mainImage" :alt="goods.title" class="buy-goods-image" />
                  <div class="buy-goods-details">
                    <div class="buy-goods-title">{{ goods.title }}</div>
                    <div class="buy-goods-price">¥{{ goods.currentPrice }}</div>
                  </div>
                </div>
              </el-form-item>
              
              <el-form-item label="购买数量" prop="quantity">
                <el-input-number
                  v-model="buyForm.quantity"
                  :min="1"
                  :max="goods.stock"
                  :precision="0"
                  style="width: 200px"
                />
                <span class="stock-tip">库存：{{ goods.stock }}件</span>
              </el-form-item>
              
              <el-form-item label="交易方式" prop="tradeMethod">
                <el-radio-group v-model="buyForm.tradeMethod" :disabled="!!goods.tradeMethod">
                  <el-radio label="MAIL" :disabled="goods.tradeMethod === 'FACE_TO_FACE'">
                    邮寄
                    <span v-if="goods.shippingFee" class="fee-tip">（邮费：¥{{ goods.shippingFee }}）</span>
                  </el-radio>
                  <el-radio label="FACE_TO_FACE" :disabled="goods.tradeMethod === 'MAIL'">
                    自提
                    <span v-if="goods.tradeLocation" class="fee-tip">（地点：{{ goods.tradeLocation }}）</span>
                  </el-radio>
                </el-radio-group>
                <div v-if="goods.tradeMethod" class="trade-method-tip">
                  该商品仅支持{{ goods.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}方式
                </div>
              </el-form-item>
              
              <el-form-item
                v-if="buyForm.tradeMethod === 'MAIL'"
                label="收货地址"
                prop="addressId"
              >
                <el-select
                  v-model="buyForm.addressId"
                  placeholder="请选择收货地址"
                  style="width: 100%"
                  @change="handleAddressChange"
                >
                  <el-option
                    v-for="address in addressList"
                    :key="address.id"
                    :label="`${address.receiverName} ${address.receiverPhone} ${address.fullAddress}`"
                    :value="address.id"
                  >
                    <div class="address-option">
                      <div class="address-name-phone">
                        <span>{{ address.receiverName }}</span>
                        <span style="margin-left: 8px">{{ address.receiverPhone }}</span>
                        <el-tag v-if="address.isDefault === 1" type="success" size="small" style="margin-left: 8px">默认</el-tag>
                      </div>
                      <div class="address-detail">{{ address.fullAddress }}</div>
                    </div>
                  </el-option>
                </el-select>
                <el-button
                  type="text"
                  @click="handleAddAddress"
                  style="margin-top: 8px"
                >
                  + 添加新地址
                </el-button>
              </el-form-item>
              
              <el-form-item label="订单金额">
                <div class="order-amount">
                  <div class="amount-row">
                    <span>商品金额：</span>
                    <span>¥{{ (goods.currentPrice * buyForm.quantity).toFixed(2) }}</span>
                  </div>
                  <div v-if="buyForm.tradeMethod === 'MAIL' && goods.shippingFee" class="amount-row">
                    <span>邮费：</span>
                    <span>¥{{ goods.shippingFee.toFixed(2) }}</span>
                  </div>
                  <div class="amount-row total">
                    <span>合计：</span>
                    <span class="total-amount">
                      ¥{{ calculateTotalAmount().toFixed(2) }}
                    </span>
                  </div>
                </div>
              </el-form-item>
            </el-form>
            
            <template #footer>
              <el-button @click="buyDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSubmitBuy" :loading="buySubmitting">
                确认购买
              </el-button>
            </template>
          </el-dialog>
          
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

/**
 * 商品详情页
 * 展示商品详细信息，支持购买、联系卖家等功能
 */

<script setup>
import { ref, computed, onMounted, onActivated, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Folder, View, Box, ShoppingCart, Money } from '@element-plus/icons-vue'
import { goodsApi, chatApi, orderApi, addressApi } from '@/api'
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

// 购买相关
const buyDialogVisible = ref(false)
const buySubmitting = ref(false)
const buyFormRef = ref(null)
const buyForm = ref({
  quantity: 1,
  tradeMethod: '',
  addressId: null
})
const addressList = ref([])
// 地址警告标志，确保只弹一次
const addressWarningShown = ref(false)
// 地址表单弹窗
const addressDialogVisible = ref(false)
const addressFormRef = ref(null)
const addressSubmitting = ref(false)
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: 0
})
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入收货人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  postalCode: [
    { 
      validator: (rule, value, callback) => {
        if (value && value.trim() !== '') {
          if (!/^\d{6}$/.test(value)) {
            callback(new Error('邮政编码必须是6位数字'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const buyRules = {
  quantity: [
    { required: true, message: '请选择购买数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '购买数量至少为1', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (goods.value && value > goods.value.stock) {
          callback(new Error(`购买数量不能超过库存（当前库存：${goods.value.stock}）`))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  tradeMethod: [
    { required: true, message: '请选择交易方式', trigger: 'change' }
  ],
  addressId: [
    { required: true, message: '请选择收货地址', trigger: 'change' }
  ]
}

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
const handleBuy = async () => {
  // 检查实名认证
  if (!userStore.userInfo?.isVerified) {
    ElMessage.warning('购买商品需要实名认证，请先完成实名认证')
    router.push('/user/verification')
    return
  }
  
  // 检查商品状态
  if (goods.value.status !== 'ON_SALE') {
    ElMessage.warning('商品已下架或已售完')
    return
  }
  
  // 检查库存
  if (goods.value.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }
  
  // 初始化购买表单
  // 如果商品只支持一种交易方式，自动设置并禁用选择
  const tradeMethod = goods.value.tradeMethod || ''
  buyForm.value = {
    quantity: 1,
    tradeMethod: tradeMethod,
    addressId: null
  }
  
  // 重置警告标志
  addressWarningShown.value = false
  
  // 如果是邮寄方式，加载收货地址列表
  if (tradeMethod === 'MAIL') {
    await fetchAddressList()
  }
  
  buyDialogVisible.value = true
}

// 监听交易方式变化
watch(() => buyForm.value.tradeMethod, async (newMethod) => {
  if (newMethod === 'MAIL') {
    await fetchAddressList()
  } else {
    buyForm.value.addressId = null
  }
})

/**
 * 获取收货地址列表
 */
const fetchAddressList = async () => {
  try {
    const response = await addressApi.getList()
    if (response.code === 200) {
      addressList.value = response.data || []
      // 如果有默认地址，自动选择
      const defaultAddress = addressList.value.find(addr => addr.isDefault === 1)
      if (defaultAddress) {
        buyForm.value.addressId = defaultAddress.id
      } else if (addressList.value.length === 0 && !addressWarningShown.value) {
        // 如果没有地址，提示用户添加（只弹一次）
        addressWarningShown.value = true
        ElMessage.warning('您还没有收货地址，请先添加收货地址')
      }
    }
  } catch (error) {
    console.error('获取收货地址列表失败:', error)
    ElMessage.error('获取收货地址列表失败')
  }
}

/**
 * 处理地址变化
 */
const handleAddressChange = () => {
  // 地址变化时，重新计算订单总金额（如果需要）
  // 这里暂时不需要额外处理，因为订单金额是计算属性，会自动更新
}

/**
 * 添加新地址
 */
const handleAddAddress = () => {
  // 重置表单
  addressForm.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    postalCode: '',
    isDefault: addressList.value.length === 0 ? 1 : 0 // 如果没有地址，默认设为默认地址
  }
  addressDialogVisible.value = true
}

/**
 * 提交地址表单
 */
const handleSubmitAddress = async () => {
  if (!addressFormRef.value) return
  
  try {
    await addressFormRef.value.validate()
  } catch (error) {
    return
  }
  
  addressSubmitting.value = true
  try {
    const response = await addressApi.add(addressForm.value)
    if (response.code === 200) {
      ElMessage.success('地址添加成功')
      addressDialogVisible.value = false
      // 刷新地址列表
      await fetchAddressList()
      // 自动选择新添加的地址
      if (response.data) {
        buyForm.value.addressId = response.data
      } else {
        // 如果后端没有返回地址ID，重新获取列表并选择最后一个（最新添加的）
        const newResponse = await addressApi.getList()
        if (newResponse.code === 200 && newResponse.data && newResponse.data.length > 0) {
          const newAddress = newResponse.data[0] // 新添加的地址应该在列表最前面
          buyForm.value.addressId = newAddress.id
        }
      }
    } else {
      ElMessage.error(response.message || '地址添加失败')
    }
  } catch (error) {
    console.error('添加地址失败:', error)
    ElMessage.error(error.response?.data?.message || '地址添加失败，请稍后重试')
  } finally {
    addressSubmitting.value = false
  }
}

/**
 * 计算订单总金额
 */
const calculateTotalAmount = () => {
  if (!goods.value) return 0
  const goodsAmount = goods.value.currentPrice * buyForm.value.quantity
  const shippingFee = (buyForm.value.tradeMethod === 'MAIL' && goods.value.shippingFee) 
    ? goods.value.shippingFee 
    : 0
  return goodsAmount + shippingFee
}

/**
 * 提交购买
 */
const handleSubmitBuy = async () => {
  if (!buyFormRef.value) return
  
  try {
    await buyFormRef.value.validate()
  } catch (error) {
    return
  }
  
  // 验证邮寄方式必须选择地址
  if (buyForm.value.tradeMethod === 'MAIL' && !buyForm.value.addressId) {
    ElMessage.warning('邮寄方式必须选择收货地址')
    return
  }
  
  buySubmitting.value = true
  try {
    const orderData = {
      goodsId: goods.value.id,
      quantity: buyForm.value.quantity,
      tradeMethod: buyForm.value.tradeMethod,
      addressId: buyForm.value.tradeMethod === 'MAIL' ? buyForm.value.addressId : null
    }
    
    const response = await orderApi.create(orderData)
    if (response.code === 200) {
      ElMessage.success('订单创建成功')
      buyDialogVisible.value = false
      
      // 创建订单后，跳转到支付页面
      const orderId = response.data
      router.push(`/order/pay/${orderId}`)
    } else {
      ElMessage.error(response.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.response?.data?.message || '创建订单失败，请稍后重试')
  } finally {
    buySubmitting.value = false
  }
}

/**
 * 处理联系
 */
const handleContact = async () => {
  // 检查实名认证
  if (!userStore.userInfo?.isVerified) {
    ElMessage.warning('联系卖家需要实名认证，请先完成实名认证')
    router.push('/user/verification')
    return
  }
  
  if (!goods.value || !goods.value.userId) {
    ElMessage.warning('用户信息不存在')
    return
  }
  
  try {
    // 创建或获取会话
    const response = await chatApi.createOrGetSession({
      targetUserId: goods.value.userId,
      relatedType: 'GOODS',
      relatedId: goods.value.id
    })
    
    if (response.code === 200) {
      const sessionId = response.data.sessionId || response.data
      // 跳转到聊天页面，并传递会话ID
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

// 当从地址管理页面返回时，如果购买对话框打开，刷新地址列表（保留兼容性）
onActivated(() => {
  if (route.query.from === 'buy' && buyDialogVisible.value && buyForm.value.tradeMethod === 'MAIL') {
    addressWarningShown.value = false // 重置警告标志
    fetchAddressList()
  }
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

/* 购买对话框样式 */
.buy-goods-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #F5F7FA;
  border-radius: 8px;
}

.buy-goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.buy-goods-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.buy-goods-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.buy-goods-price {
  font-size: 18px;
  color: #F56C6C;
  font-weight: bold;
}

.stock-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.fee-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.trade-method-tip {
  font-size: 12px;
  color: #E6A23C;
  margin-top: 4px;
}

.address-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.address-name-phone {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #303133;
}

.address-detail {
  font-size: 12px;
  color: #909399;
}

.order-amount {
  padding: 12px;
  background-color: #F5F7FA;
  border-radius: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.amount-row.total {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #E0E0E0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.total-amount {
  font-size: 20px;
  color: #F56C6C;
  font-weight: bold;
}
</style>

