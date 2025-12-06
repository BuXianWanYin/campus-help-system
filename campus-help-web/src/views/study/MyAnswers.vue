<template>
  <div class="my-answers-page">
    <div class="page-header">
      <h1 class="page-title">我的回答</h1>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="问题状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 150px" clearable @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="待回答" value="PENDING_ANSWER" />
            <el-option label="已回答" value="ANSWERED" />
            <el-option label="已解决" value="SOLVED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 问题列表 -->
    <div class="question-list" v-loading="loading">
      <div v-if="questionList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无回答" />
      </div>
      <div v-else class="question-items">
        <div v-for="item in questionList" :key="item.id" class="question-item">
          <div class="question-header">
            <h3 class="question-title" @click="goToDetail(item.id)">{{ item.title }}</h3>
            <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
          </div>
          <div class="question-meta">
            <span class="meta-item">
              <el-icon><Collection /></el-icon>
              {{ getCategoryName(item.category) }}
            </span>
            <span class="meta-item" v-if="item.reward && item.reward > 0">
              <el-icon><Money /></el-icon>
              悬赏 ¥{{ item.reward }}
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ item.answerCount || 0 }} 个回答
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }} 次浏览
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(item.createTime) }}
            </span>
          </div>
          <div class="question-actions">
            <el-button type="primary" text size="small" @click="goToDetail(item.id)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
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
import { ElMessage } from 'element-plus'
import { Collection, Money, ChatDotRound, View, Clock } from '@element-plus/icons-vue'
import { questionApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const questionList = ref([])

const filters = reactive({
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取分类名称
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
  return categoryMap[category] || category || '-'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'PENDING_ANSWER': '待回答',
    'ANSWERED': '已回答',
    'SOLVED': '已解决',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝',
    'ADMIN_OFFSHELF': '已下架'
  }
  return statusMap[status] || status || '-'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING_REVIEW': 'info',
    'PENDING_ANSWER': 'warning',
    'ANSWERED': 'success',
    'SOLVED': 'success',
    'CANCELLED': 'info',
    'REJECTED': 'danger',
    'ADMIN_OFFSHELF': 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取问题列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    if (filters.status) {
      params.status = filters.status
    }
    
    const response = await questionApi.getMyAnswered(params)
    if (response.code === 200) {
      const pageData = response.data
      questionList.value = pageData.records || []
      pagination.total = pageData.total || 0
    } else {
      ElMessage.error(response.message || '获取问题列表失败')
    }
  } catch (error) {
    console.error('获取问题列表失败:', error)
    ElMessage.error('获取问题列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  fetchQuestionList()
}

// 重置
const handleReset = () => {
  filters.status = ''
  pagination.current = 1
  fetchQuestionList()
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchQuestionList()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchQuestionList()
}

// 查看详情
const goToDetail = (id) => {
  router.push(`/study/detail/${id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchQuestionList()
})
</script>

<style scoped>
.my-answers-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.question-list {
  margin-bottom: 20px;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.question-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  cursor: pointer;
  flex: 1;
  margin-right: 12px;
}

.question-title:hover {
  color: var(--color-primary);
}

.question-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

