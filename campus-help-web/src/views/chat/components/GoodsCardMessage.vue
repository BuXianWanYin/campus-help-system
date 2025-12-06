<template>
  <div class="goods-card-message" @click="goToGoodsDetail">
    <div v-if="goodsLoading" class="goods-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-else-if="goods" class="goods-card-content">
      <img :src="getGoodsImage(goods.images)" :alt="goods.title" class="goods-card-image" />
      <div class="goods-card-info">
        <div class="goods-card-title">{{ goods.title }}</div>
        <div class="goods-card-price">¥{{ goods.currentPrice }}</div>
        <div class="goods-card-meta">
          <el-tag size="small" type="info">{{ goods.category }}</el-tag>
          <span v-if="goods.condition" class="goods-card-condition">{{ goods.condition }}</span>
        </div>
      </div>
    </div>
    <div v-else class="goods-card-error">
      <el-icon><Warning /></el-icon>
      <span>商品信息加载失败</span>
    </div>
  </div>
</template>

/**
 * 商品卡片消息组件
 * 在聊天消息中展示商品信息卡片
 */

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Warning } from '@element-plus/icons-vue'
import { goodsApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const goods = ref(null)
const goodsLoading = ref(false)

/**
 * 解析商品ID
 */
const parseGoodsId = () => {
  try {
    const content = JSON.parse(props.message.content)
    return content.goodsId
  } catch (error) {
    console.error('解析商品ID失败:', error)
    return null
  }
}

/**
 * 获取商品信息
 */
const fetchGoods = async () => {
  const goodsId = parseGoodsId()
  if (!goodsId) {
    return
  }
  
  goodsLoading.value = true
  try {
    const response = await goodsApi.getDetail(goodsId)
    if (response.code === 200) {
      goods.value = response.data
    }
  } catch (error) {
    console.error('获取商品信息失败:', error)
  } finally {
    goodsLoading.value = false
  }
}

/**
 * 获取商品图片
 */
const getGoodsImage = (imagesJson) => {
  if (!imagesJson) return 'https://via.placeholder.com/200x200?text=暂无图片'
  
  let imageArray = []
  if (typeof imagesJson === 'string') {
    try {
      imageArray = JSON.parse(imagesJson)
    } catch (e) {
      return 'https://via.placeholder.com/200x200?text=暂无图片'
    }
  } else if (Array.isArray(imagesJson)) {
    imageArray = imagesJson
  }
  
  if (imageArray.length > 0) {
    return getAvatarUrl(imageArray[0])
  }
  return 'https://via.placeholder.com/200x200?text=暂无图片'
}

/**
 * 跳转到商品详情
 */
const goToGoodsDetail = () => {
  const goodsId = parseGoodsId()
  if (goodsId) {
    router.push(`/goods/detail/${goodsId}`)
  }
}

onMounted(() => {
  fetchGoods()
})
</script>

<style scoped>
.goods-card-message {
  max-width: 300px;
  background-color: #FFFFFF;
  border: 1px solid #E0E0E0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.goods-card-message:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.goods-loading,
.goods-card-error {
  padding: 20px;
  text-align: center;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.goods-card-content {
  display: flex;
  flex-direction: column;
}

.goods-card-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.goods-card-info {
  padding: 12px;
}

.goods-card-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  min-height: 40px;
}

.goods-card-price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
  margin-bottom: 8px;
}

.goods-card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.goods-card-condition {
  font-size: 12px;
}
</style>

