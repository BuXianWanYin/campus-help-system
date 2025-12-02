<template>
  <div class="detail-container" v-loading="loading">
    <el-empty v-if="!loading && !lostFound" description="失物信息不存在" />
    
    <div v-if="lostFound" class="detail-content">
      <!-- 返回按钮 -->
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      
      <!-- 失物详情卡片 -->
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
            <span class="type-badge" :class="lostFound.type === 'LOST' ? 'badge-red' : 'badge-green'">
              {{ lostFound.type === 'LOST' ? '失物' : '招领' }}
            </span>
            <h1 class="detail-title">{{ lostFound.title }}</h1>
          </div>
          
          <div class="detail-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>地点：{{ lostFound.lostLocation }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>时间：{{ formatDateTime(lostFound.lostTime) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Category /></el-icon>
              <span>分类：{{ lostFound.category }}</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span>浏览：{{ lostFound.viewCount || 0 }}次</span>
            </div>
          </div>
          
          <div class="detail-description">
            <h3>物品描述</h3>
            <p>{{ lostFound.description }}</p>
          </div>
          
          <div v-if="lostFound.reward > 0" class="reward-section">
            <span class="reward-label">悬赏金额：</span>
            <span class="reward-value">¥{{ lostFound.reward }}</span>
          </div>
          
          <!-- 发布者信息 -->
          <div class="publisher-section">
            <div class="publisher-info">
              <el-avatar :size="48" :src="getAvatarUrl(lostFound.userAvatar)">
                {{ lostFound.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="publisher-details">
                <div class="publisher-name">{{ lostFound.userName || '未知用户' }}</div>
                <div class="publish-time">发布于 {{ formatDateTime(lostFound.createTime) }}</div>
              </div>
            </div>
            <el-button type="primary" @click="handleContact">联系TA</el-button>
          </div>
          
          <!-- 认领按钮 -->
          <div v-if="lostFound.status === 'PENDING_CLAIM' || lostFound.status === 'CLAIMING'" class="action-section">
            <el-button type="primary" size="large" @click="showClaimDialog = true">
              {{ lostFound.type === 'LOST' ? '我要认领' : '这是我的' }}
            </el-button>
          </div>
          
          <!-- 状态提示 -->
          <div v-if="lostFound.status === 'CLAIMED'" class="status-tip">
            <el-alert type="success" :closable="false">该失物已被认领</el-alert>
          </div>
          <div v-if="lostFound.status === 'CLOSED'" class="status-tip">
            <el-alert type="info" :closable="false">该失物已关闭</el-alert>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 认领对话框 -->
    <el-dialog
      v-model="showClaimDialog"
      title="申请认领"
      width="600px"
      @close="resetClaimForm"
    >
      <el-form :model="claimForm" label-width="100px">
        <el-form-item label="物品特征" required>
          <el-input
            v-model="claimForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述物品的特征，以便发布者确认"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="丢失时间">
          <el-date-picker
            v-model="claimForm.lostTime"
            type="datetime"
            placeholder="选择丢失时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="其他信息">
          <el-input
            v-model="claimForm.otherInfo"
            type="textarea"
            :rows="3"
            placeholder="其他能够证明物品归属的信息"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="证明文件">
          <el-upload
            v-model:file-list="claimForm.proofImages"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClaimDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitClaim" :loading="claimLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Category, View, Plus } from '@element-plus/icons-vue'
import { lostFoundApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const lostFound = ref(null)
const showClaimDialog = ref(false)
const claimLoading = ref(false)

const claimForm = ref({
  description: '',
  lostTime: null,
  otherInfo: '',
  proofImages: []
})

const imageList = computed(() => {
  if (!lostFound.value?.images) return []
  const images = typeof lostFound.value.images === 'string' 
    ? JSON.parse(lostFound.value.images) 
    : lostFound.value.images
  return images.map(img => getAvatarUrl(img))
})

const mainImage = ref('')

/**
 * 获取失物详情
 */
const fetchDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('失物ID不存在')
    return
  }
  
  loading.value = true
  try {
    const response = await lostFoundApi.getDetail(id)
    if (response.code === 200) {
      lostFound.value = response.data
      const images = lostFound.value.images 
        ? (typeof lostFound.value.images === 'string' ? JSON.parse(lostFound.value.images) : lostFound.value.images)
        : []
      lostFound.value.images = images
      lostFound.value.userAvatar = lostFound.value.user?.avatar
      lostFound.value.userName = lostFound.value.user?.nickname || '未知用户'
      
      // 设置主图
      if (images.length > 0) {
        mainImage.value = getAvatarUrl(images[0])
      }
    }
  } catch (error) {
    console.error('获取失物详情失败:', error)
    ElMessage.error('获取失物详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化日期时间
 */
const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 处理联系
 */
const handleContact = () => {
  ElMessage.info(`联系 ${lostFound.value.userName}`)
}

/**
 * 处理提交认领
 */
const handleSubmitClaim = async () => {
  if (!claimForm.value.description.trim()) {
    ElMessage.warning('请填写物品特征描述')
    return
  }
  
  claimLoading.value = true
  try {
    const data = {
      lostFoundId: lostFound.value.id,
      description: claimForm.value.description,
      lostTime: claimForm.value.lostTime,
      otherInfo: claimForm.value.otherInfo,
      proofImages: claimForm.value.proofImages.map(file => file.url || file.response?.data?.url)
    }
    
    const response = await lostFoundApi.applyClaim(data)
    if (response.code === 200) {
      ElMessage.success('认领申请已提交')
      showClaimDialog.value = false
      resetClaimForm()
    }
  } catch (error) {
    console.error('提交认领失败:', error)
    ElMessage.error('提交认领失败')
  } finally {
    claimLoading.value = false
  }
}

/**
 * 重置认领表单
 */
const resetClaimForm = () => {
  claimForm.value = {
    description: '',
    lostTime: null,
    otherInfo: '',
    proofImages: []
  }
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  margin-bottom: 20px;
  font-size: 14px;
}

.detail-content {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.detail-card {
  display: flex;
  gap: 24px;
  padding: 24px;
}

.image-section {
  flex: 0 0 500px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.no-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  background-color: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  margin-bottom: 12px;
}

.image-thumbs {
  display: flex;
  gap: 8px;
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

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section {
  flex: 1;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
}

.badge-red {
  background-color: #F56C6C;
}

.badge-green {
  background-color: #67C23A;
}

.detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #F0F0F0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
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
  margin: 0;
}

.reward-section {
  padding: 16px;
  background-color: #FFF7E6;
  border-radius: 8px;
  margin-bottom: 24px;
}

.reward-label {
  font-size: 14px;
  color: #606266;
}

.reward-value {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
  margin-left: 8px;
}

.publisher-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #F5F5F5;
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
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.action-section {
  margin-bottom: 24px;
}

.status-tip {
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .detail-card {
    flex-direction: column;
  }
  
  .image-section {
    flex: 1;
  }
  
  .main-image {
    height: 300px;
  }
  
  .publisher-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

