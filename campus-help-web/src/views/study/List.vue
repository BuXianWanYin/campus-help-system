<template>
  <div class="list-container">
    <div class="page-header">
      <h1 class="page-title">学习互助</h1>
      <el-button type="primary" @click="handlePublish">发布问题</el-button>
    </div>

    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索问题关键词（标题或描述）"
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
        <el-form-item label="分类：">
          <el-select v-model="filters.category" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="生物" value="BIOLOGY" />
            <el-option label="计算机" value="COMPUTER" />
            <el-option label="英语" value="ENGLISH" />
            <el-option label="文学" value="LITERATURE" />
            <el-option label="历史" value="HISTORY" />
            <el-option label="哲学" value="PHILOSOPHY" />
            <el-option label="经济" value="ECONOMICS" />
            <el-option label="管理" value="MANAGEMENT" />
            <el-option label="法律" value="LAW" />
            <el-option label="教育" value="EDUCATION" />
            <el-option label="艺术" value="ART" />
            <el-option label="工程" value="ENGINEERING" />
            <el-option label="医学" value="MEDICINE" />
            <el-option label="农学" value="AGRICULTURE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="待解答" value="PENDING_ANSWER" />
            <el-option label="已回答" value="ANSWERED" />
            <el-option label="已解决" value="SOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序：">
          <el-select v-model="filters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
            <el-option label="最新发布" value="latest" />
            <el-option label="回答最多" value="popular" />
            <el-option label="最新回答" value="recent_answer" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">筛选</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 问题列表 -->
    <div class="card-grid" v-loading="loading">
      <div v-for="item in questionList" :key="item.id" class="question-card" @click="goToDetail(item.id)">
        <div class="card-header">
          <el-tag :type="getCategoryTagType(item.category)" class="category-tag">
            {{ getCategoryName(item.category) }}
          </el-tag>
          <el-tag :type="getStatusTagType(item.status)" size="small" class="status-tag">
            {{ getStatusName(item.status) }}
          </el-tag>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-desc">{{ truncateText(item.description, 100) }}</p>
          <div v-if="item.images && parseImages(item.images).length > 0" class="card-images">
            <img 
              v-for="(img, idx) in parseImages(item.images).slice(0, 3)" 
              :key="idx"
              :src="getAvatarUrl(img)" 
              :alt="item.title"
              class="card-image"
            />
          </div>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }} 浏览
            </span>
            <span class="meta-item">
              <el-icon><ChatLineRound /></el-icon>
              {{ item.answerCount || 0 }} 回答
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.createTime) }}
            </span>
          </div>
        </div>
        <div class="card-footer">
          <div class="card-user">
            <el-avatar :size="24" :src="getAvatarUrl(item.userAvatar)">{{ item.userName?.charAt(0) || 'U' }}</el-avatar>
            <span>{{ item.userName || '未知用户' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && questionList.length === 0" description="暂无问题" />

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
 * 学习互助列表页
 * 展示学习问题列表，支持搜索、筛选、分页等功能
 */

<script setup>
import { ref, reactive, onMounted, onActivated, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, View, ChatLineRound, Clock, Money } from '@element-plus/icons-vue'
import { questionApi, searchHistoryApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

// 定义组件名称，用于keep-alive
defineOptions({
  name: 'QuestionList'
})

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const questionList = ref([])
const total = ref(0)

const searchKeyword = ref('')
const searchHistoryList = ref([])

const filters = reactive({
  category: '',
  status: '',
  sortBy: 'latest',
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 获取问题列表
 */
const fetchQuestionList = async () => {
  if (loading.value) {
    return
  }
  
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      category: filters.category || undefined,
      status: filters.status || undefined,
      sortBy: filters.sortBy || 'latest',
      keyword: filters.keyword || undefined
    }
    
    const response = await questionApi.getList(params)
    if (response.code === 200) {
      const pageData = response.data
      questionList.value = (pageData.records || []).map(item => ({
        ...item,
        images: parseImages(item.images),
        userAvatar: item.user?.avatar,
        userName: item.user?.nickname || '未知用户',
        userId: item.userId
      }))
      total.value = pageData.total || 0
    }
  } catch (error) {
    ElMessage.error('获取问题列表失败')
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
    return typeof imagesJson === 'string' ? JSON.parse(imagesJson) : imagesJson
  } catch {
    return []
  }
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
 * 截断文本
 */
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

/**
 * 获取分类名称
 */
const getCategoryName = (category) => {
  const categoryMap = {
    'MATH': '数学',
    'PHYSICS': '物理',
    'CHEMISTRY': '化学',
    'BIOLOGY': '生物',
    'COMPUTER': '计算机',
    'ENGLISH': '英语',
    'LITERATURE': '文学',
    'HISTORY': '历史',
    'PHILOSOPHY': '哲学',
    'ECONOMICS': '经济',
    'MANAGEMENT': '管理',
    'LAW': '法律',
    'EDUCATION': '教育',
    'ART': '艺术',
    'ENGINEERING': '工程',
    'MEDICINE': '医学',
    'AGRICULTURE': '农学',
    'OTHER': '其他'
  }
  return categoryMap[category] || category || '其他'
}

/**
 * 获取分类标签类型
 */
const getCategoryTagType = (category) => {
  const typeMap = {
    'MATH': 'warning',
    'PHYSICS': 'success',
    'CHEMISTRY': 'danger',
    'BIOLOGY': 'success',
    'COMPUTER': 'primary',
    'ENGLISH': 'info',
    'LITERATURE': 'warning',
    'HISTORY': 'info',
    'PHILOSOPHY': 'info',
    'ECONOMICS': 'success',
    'MANAGEMENT': 'primary',
    'LAW': 'danger',
    'EDUCATION': 'warning',
    'ART': 'success',
    'ENGINEERING': 'primary',
    'MEDICINE': 'danger',
    'AGRICULTURE': 'success',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

/**
 * 获取状态名称
 */
const getStatusName = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'PENDING_ANSWER': '待解答',
    'ANSWERED': '已回答',
    'SOLVED': '已解决',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝',
    'ADMIN_OFFSHELF': '已下架'
  }
  return statusMap[status] || status || '未知'
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status) => {
  const typeMap = {
    'PENDING_REVIEW': 'info',
    'PENDING_ANSWER': 'warning',
    'ANSWERED': 'primary',
    'SOLVED': 'success',
    'CANCELLED': 'info',
    'REJECTED': 'danger',
    'ADMIN_OFFSHELF': 'danger'
  }
  return typeMap[status] || ''
}

/**
 * 处理关键词搜索
 */
const handleKeywordSearch = () => {
  filters.keyword = searchKeyword.value.trim()
  pagination.pageNum = 1
  fetchQuestionList()
  fetchSearchHistory()
}

/**
 * 清空关键词
 */
const handleClearKeyword = () => {
  searchKeyword.value = ''
  filters.keyword = ''
  pagination.pageNum = 1
  fetchQuestionList()
}

/**
 * 选择搜索历史
 */
const handleSelectHistory = (keyword) => {
  searchKeyword.value = keyword
  filters.keyword = keyword
  pagination.pageNum = 1
  fetchQuestionList()
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
    await searchHistoryApi.clear('STUDY_QUESTION')
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
    const response = await searchHistoryApi.getList('STUDY_QUESTION', 10)
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
  fetchQuestionList()
}

/**
 * 处理发布
 */
const handlePublish = () => {
  router.push('/study/publish')
}

/**
 * 跳转详情
 */
const goToDetail = (id) => {
  router.push(`/study/detail/${id}`)
}

/**
 * 处理分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchQuestionList()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchQuestionList()
}

// 组件激活时（从其他页面返回时），刷新数据
onActivated(() => {
  questionList.value = []
  nextTick(() => {
    fetchQuestionList()
    fetchSearchHistory()
  })
})

onMounted(() => {
  questionList.value = []
  fetchQuestionList()
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
  margin: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.question-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.question-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-tag {
  font-weight: 500;
}

.status-tag {
  margin-left: auto;
}

.card-content {
  flex: 1;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.card-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #E0E0E0;
}

.card-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #909399;
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
}

.card-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
