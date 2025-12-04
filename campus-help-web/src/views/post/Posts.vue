<template>
  <div class="posts-container">
    <div class="page-header">
      <h1 class="page-title">我的发布</h1>
      <el-button type="primary" @click="handlePublish">发布失物</el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="类型：">
          <el-select v-model="filters.type" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="失物" value="LOST" />
            <el-option label="招领" value="FOUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类：">
          <el-select v-model="filters.category" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="证件类" value="证件类" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="书籍" value="书籍" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="待认领" value="PENDING_CLAIM" />
            <el-option label="认领中" value="CLAIMING" />
            <el-option label="已认领" value="CLAIMED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序：">
          <el-select v-model="filters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
            <el-option label="最新" value="latest" />
            <el-option label="浏览最多" value="view" />
            <el-option label="悬赏最高" value="reward" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 失物列表 -->
    <div class="card-grid" v-loading="loading">
      <div v-for="item in lostFoundList" :key="item.id" class="lost-found-card">
        <div class="card-image-wrapper" @click="goToDetail(item.id)">
          <img :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
          <span class="card-badge" :class="item.type === 'LOST' ? 'badge-red' : 'badge-green'">
            {{ item.type === 'LOST' ? '失物' : '招领' }}
          </span>
          <span class="status-badge" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status, item.type) }}
          </span>
        </div>
        <div class="card-content">
          <h3 class="card-title" @click="goToDetail(item.id)">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Location /></el-icon>
              {{ item.lostLocation }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.createTime) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }}次浏览
            </span>
          </div>
          <div class="card-footer">
            <div class="card-info">
              <span v-if="item.reward > 0" class="reward-text">悬赏 ¥{{ item.reward }}</span>
            </div>
            <div class="card-actions">
              <el-button size="small" @click.stop="goToDetail(item.id)">查看详情</el-button>
              <el-button 
                v-if="canEdit(item.status)" 
                size="small" 
                type="primary" 
                text 
                @click.stop="handleEdit(item)">
                编辑
              </el-button>
              <el-button 
                v-if="canClose(item.status)" 
                size="small" 
                type="warning" 
                text 
                @click.stop="handleClose(item)">
                关闭
              </el-button>
              <el-button 
                v-if="canDelete(item.status)" 
                size="small" 
                type="danger" 
                text 
                @click.stop="handleDelete(item)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && lostFoundList.length === 0" description="您还没有发布任何失物信息" />

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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, View } from '@element-plus/icons-vue'
import { lostFoundApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()

const loading = ref(false)
const lostFoundList = ref([])
const total = ref(0)

const filters = reactive({
  type: '',
  category: '',
  status: '',
  sortBy: 'latest'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 获取我的发布列表
 */
const fetchMyPosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      type: filters.type || undefined,
      category: filters.category || undefined,
      status: filters.status || undefined,
      sortBy: filters.sortBy || 'latest'
    }
    
    const response = await lostFoundApi.getMyPosts(params)
    if (response.code === 200) {
      const pageData = response.data
      lostFoundList.value = (pageData.records || []).map(item => ({
        ...item,
        images: parseImages(item.images)
      }))
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取我的发布列表失败:', error)
    ElMessage.error('获取我的发布列表失败')
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
 * 获取第一张图片
 */
const getFirstImage = (images) => {
  if (Array.isArray(images) && images.length > 0) {
    return getAvatarUrl(images[0])
  }
  return 'https://via.placeholder.com/300x200?text=暂无图片'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status, type) => {
  // 根据类型显示不同的状态文本
  if (type === 'LOST') {
    // 失物类型：寻找中、确认中、已找到
    const lostStatusMap = {
      'PENDING_REVIEW': '待审核',
      'PENDING_CLAIM': '寻找中',
      'CLAIMING': '确认中',  // 有人提供线索，正在确认
      'CLAIMED': '已找到',
      'CLOSED': '已关闭',
      'REJECTED': '已拒绝'
    }
    return lostStatusMap[status] || status
  } else {
    // 招领类型：等待失主、认领中、已认领
    const foundStatusMap = {
      'PENDING_REVIEW': '待审核',
      'PENDING_CLAIM': '等待失主',
      'CLAIMING': '认领中',
      'CLAIMED': '已认领',
      'CLOSED': '已关闭',
      'REJECTED': '已拒绝'
    }
    return foundStatusMap[status] || status
  }
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    'PENDING_REVIEW': 'status-warning',
    'PENDING_CLAIM': 'status-info',
    'CLAIMING': 'status-primary',
    'CLAIMED': 'status-success',
    'CLOSED': 'status-default',
    'REJECTED': 'status-danger'
  }
  return classMap[status] || 'status-default'
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.pageNum = 1
  fetchMyPosts()
}

/**
 * 判断是否可以编辑
 */
const canEdit = (status) => {
  return status !== 'CLAIMED' && status !== 'CLOSED'
}

/**
 * 判断是否可以关闭
 */
const canClose = (status) => {
  return status !== 'CLOSED' && status !== 'CLAIMED'
}

/**
 * 判断是否可以删除
 */
const canDelete = (status) => {
  return status !== 'CLAIMING' && status !== 'CLAIMED'
}

/**
 * 处理发布
 */
const handlePublish = () => {
  router.push('/lost-found/publish')
}

/**
 * 跳转详情
 */
const goToDetail = (id) => {
  router.push(`/lost-found/detail/${id}`)
}

/**
 * 处理编辑
 */
const handleEdit = (item) => {
  router.push(`/lost-found/edit/${item.id}`)
}

/**
 * 处理关闭
 */
const handleClose = async (item) => {
  try {
    const typeText = item.type === 'LOST' ? '失物' : '招领'
    const closeTip = item.type === 'LOST' 
      ? '关闭后将不再显示，其他用户将无法看到此失物信息。' 
      : '关闭后将不可被认领，失主将无法申请认领该物品。'
    
    await ElMessageBox.confirm(
      `确定要关闭${typeText}《${item.title}》吗？${closeTip}`,
      `关闭${typeText}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await lostFoundApi.close(item.id)
    ElMessage.success('关闭成功')
    fetchMyPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭失败:', error)
      ElMessage.error(error.response?.data?.message || '关闭失败')
    }
  }
}

/**
 * 处理删除
 */
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除失物《${item.title}》吗？删除后无法恢复。`,
      '删除失物',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await lostFoundApi.delete(item.id)
    ElMessage.success('删除成功')
    fetchMyPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失物失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

/**
 * 处理分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchMyPosts()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchMyPosts()
}

onMounted(() => {
  fetchMyPosts()
})
</script>

<style scoped>
.posts-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.lost-found-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.lost-found-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #FFFFFF;
  font-weight: 500;
  z-index: 1;
}

.badge-red {
  background-color: #F56C6C;
}

.badge-green {
  background-color: #67C23A;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #FFFFFF;
  font-weight: 500;
  z-index: 1;
}

.status-warning {
  background-color: #E6A23C;
}

.status-info {
  background-color: #909399;
}

.status-primary {
  background-color: #409EFF;
}

.status-success {
  background-color: #67C23A;
}

.status-default {
  background-color: #C0C4CC;
}

.status-danger {
  background-color: #F56C6C;
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.5;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-title:hover {
  color: #409EFF;
}

.card-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #F0F0F0;
  margin-top: auto;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reward-text {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>

