<template>
  <div class="list-container">
    <div class="page-header">
      <h1 class="page-title">失物招领</h1>
      <el-button type="primary" @click="handlePublish">发布失物</el-button>
    </div>

    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索失物关键词（标题或描述）"
        clearable
        @keyup.enter="handleKeywordSearch"
        @clear="handleClearKeyword"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleKeywordSearch">搜索</el-button>
        </template>
      </el-input>
      
      <!-- 搜索历史 -->
      <div v-if="searchHistoryList.length > 0" class="search-history">
        <div class="history-header">
          <span class="history-title">搜索历史</span>
          <el-button text type="primary" size="small" @click="handleClearHistory">清空</el-button>
        </div>
        <div class="history-tags">
          <el-tag
            v-for="item in searchHistoryList"
            :key="item.id"
            closable
            @close="handleDeleteHistory(item.id)"
            @click="handleSelectHistory(item.keyword)"
            class="history-tag"
          >
            {{ item.keyword }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="类型：">
          <div class="tab-buttons">
            <el-button :type="filters.type === 'LOST' || !filters.type ? 'primary' : ''" size="small" @click="handleTypeChange('LOST')">失物</el-button>
            <el-button :type="filters.type === 'FOUND' ? 'primary' : ''" size="small" @click="handleTypeChange('FOUND')">招领</el-button>
          </div>
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
            <el-option :label="filters.type === 'LOST' ? '寻找中' : '等待失主'" value="PENDING_CLAIM" />
            <el-option :label="filters.type === 'LOST' ? '确认中' : '认领中'" value="CLAIMING" />
            <el-option :label="filters.type === 'LOST' ? '已找到' : '已认领'" value="CLAIMED" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点：">
          <el-input v-model="filters.location" placeholder="输入地点" style="width: 150px" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="排序：">
          <el-select v-model="filters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
            <el-option label="最新" value="latest" />
            <el-option label="浏览最多" value="view" />
            <el-option label="悬赏最高" value="reward" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">筛选</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 失物列表 -->
    <div class="card-grid" v-loading="loading">
      <div v-for="item in lostFoundList" :key="item.id" class="lost-found-card" @click="goToDetail(item.id)">
        <div class="card-image-wrapper">
          <img :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
          <span class="card-badge" :class="item.type === 'LOST' ? 'badge-red' : 'badge-green'">
            {{ item.type === 'LOST' ? '失物' : '招领' }}
          </span>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
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
              <el-icon><Folder /></el-icon>
              {{ item.category }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }}
            </span>
          </div>
          <div class="card-footer">
            <div class="card-user">
              <el-avatar :size="24" :src="getAvatarUrl(item.userAvatar)">{{ item.userName?.charAt(0) || 'U' }}</el-avatar>
              <span>{{ item.userName || '未知用户' }}</span>
            </div>
            <div class="card-actions">
              <span v-if="item.reward > 0" class="reward-text">悬赏 ¥{{ item.reward }}</span>
              <el-button 
                v-if="item.userId && item.userId !== userStore.userInfo?.id" 
                type="primary" 
                size="small" 
                text 
                @click.stop="handleContact(item)">
                联系TA
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && lostFoundList.length === 0" description="暂无失物信息" />

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
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Clock, Folder, View, Search } from '@element-plus/icons-vue'
import { lostFoundApi, searchHistoryApi } from '@/api'

// 定义组件名称，用于 keep-alive
defineOptions({
  name: 'LostFoundList'
})
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const lostFoundList = ref([])
const total = ref(0)

const searchKeyword = ref('')
const searchHistoryList = ref([])

const filters = reactive({
  type: '',
  category: '',
  status: '',
  location: '',
  sortBy: 'latest',
  keyword: '' // 添加关键词字段
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 获取失物列表
 */
const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      type: filters.type || undefined,
      category: filters.category || undefined,
      status: filters.status || undefined,
      location: filters.location || undefined,
      sortBy: filters.sortBy || 'latest',
      keyword: filters.keyword || undefined
    }
    
    const response = await lostFoundApi.getList(params)
    if (response.code === 200) {
      const pageData = response.data
      lostFoundList.value = (pageData.records || []).map(item => ({
        ...item,
        images: parseImages(item.images),
        userAvatar: item.user?.avatar,
        userName: item.user?.nickname || '未知用户',
        userId: item.userId
      }))
      total.value = pageData.total || 0
    }
  } catch (error) {
    ElMessage.error('获取失物列表失败')
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
 * 处理类型切换
 */
const handleTypeChange = (type) => {
  filters.type = filters.type === type ? '' : type
  pagination.pageNum = 1
  handleSearch()
}

/**
 * 处理关键词搜索
 */
const handleKeywordSearch = () => {
  filters.keyword = searchKeyword.value.trim()
  pagination.pageNum = 1
  fetchLostFoundList()
  fetchSearchHistory()
}

/**
 * 清空关键词
 */
const handleClearKeyword = () => {
  searchKeyword.value = ''
  filters.keyword = ''
  pagination.pageNum = 1
  fetchLostFoundList()
}

/**
 * 选择搜索历史
 */
const handleSelectHistory = (keyword) => {
  searchKeyword.value = keyword
  filters.keyword = keyword
  pagination.pageNum = 1
  fetchLostFoundList()
}

/**
 * 删除搜索历史
 */
const handleDeleteHistory = async (id) => {
  try {
    await searchHistoryApi.delete(id)
    ElMessage.success('删除成功')
    fetchSearchHistory()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

/**
 * 清空搜索历史
 */
const handleClearHistory = async () => {
  try {
    await searchHistoryApi.clear('LOST_FOUND')
    ElMessage.success('清空成功')
    searchHistoryList.value = []
  } catch (error) {
    ElMessage.error('清空失败')
  }
}

/**
 * 获取搜索历史
 */
const fetchSearchHistory = async () => {
  try {
    const response = await searchHistoryApi.getList('LOST_FOUND', 10)
    if (response.code === 200) {
      searchHistoryList.value = response.data || []
    }
  } catch (error) {
    // 未登录用户不显示搜索历史
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.pageNum = 1
  fetchLostFoundList()
}

/**
 * 处理发布
 */
const handlePublish = () => {
  router.push('/lost-found/publish')
}

/**
 * 处理联系
 */
const handleContact = (item) => {
  ElMessage.info(`联系 ${item.userName}`)
}

/**
 * 跳转详情
 */
const goToDetail = (id) => {
  router.push(`/lost-found/detail/${id}`)
}

/**
 * 处理分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchLostFoundList()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchLostFoundList()
}

// 组件激活时（从其他页面返回时），刷新数据
onActivated(() => {
  fetchLostFoundList()
  fetchSearchHistory()
})

onMounted(() => {
  // 首次加载时获取数据
  fetchLostFoundList()
  fetchSearchHistory()
})
</script>

<style scoped>
.list-container {
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

.search-bar {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.search-input {
  margin-bottom: 12px;
}

.search-history {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #E0E0E0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.history-tag:hover {
  background-color: #409EFF;
  color: #FFFFFF;
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

.tab-buttons {
  display: flex;
  border: 1px solid #E0E0E0;
  border-radius: 4px;
  overflow: hidden;
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
  cursor: pointer;
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
}

.badge-red {
  background-color: #F56C6C;
}

.badge-green {
  background-color: #67C23A;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 48px;
  max-height: 48px;
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
  min-height: 42px;
  max-height: 42px;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
  align-items: center;
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
  flex-shrink: 0;
}

.card-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reward-text {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
