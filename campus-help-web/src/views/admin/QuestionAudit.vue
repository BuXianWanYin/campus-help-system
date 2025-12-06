<template>
  <div class="question-audit-page">
    <div class="page-header">
      <h2 class="page-title">学习问题审核</h2>
      <p class="page-subtitle">审核用户提交的学习问题，可查看所有审核历史</p>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审核状态：">
          <el-select v-model="filters.auditStatus" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="ALL" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类：">
          <el-select v-model="filters.category" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="生物" value="BIOLOGY" />
            <el-option label="计算机" value="COMPUTER" />
            <el-option label="英语" value="ENGLISH" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词：">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题或描述"
            style="width: 200px"
            clearable
            @keyup.enter="handleFilter"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      :data="questionList"
      v-loading="loading"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'createTime', order: 'ascending' }"
    >
      <el-table-column prop="id" label="ID" min-width="80" sortable />
      <el-table-column prop="title" label="问题标题" min-width="250" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="100">
        <template #default="{ row }">
          {{ getCategoryName(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="user" label="发布者" min-width="120">
        <template #default="{ row }">
          {{ row.user?.nickname || '未知用户' }}
        </template>
      </el-table-column>
      <el-table-column prop="reward" label="悬赏" min-width="100">
        <template #default="{ row }">
          <span v-if="row.reward && row.reward > 0" style="color: #E6A23C; font-weight: bold">
            ¥{{ row.reward }}
          </span>
          <span v-else style="color: #909399">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" min-width="100">
        <template #default="{ row }">
          <el-tag v-if="row.auditStatus === 'PENDING'" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="row.auditStatus === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="auditTriggerReason" label="触发原因" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.auditTriggerReason || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="auditTime" label="审核时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ row.auditTime ? formatDate(row.auditTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.auditStatus === 'PENDING'"
            type="primary"
            size="small"
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button
            v-else
            type="info"
            size="small"
            disabled
          >
            已审核
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleViewDetail(row)"
          >
            详情
          </el-button>
          <el-button
            v-if="row.auditStatus === 'APPROVED' && row.status !== 'ADMIN_OFFSHELF' && row.status !== 'SOLVED' && row.status !== 'CANCELLED'"
            type="warning"
            size="small"
            @click="handleOffshelf(row)"
          >
            下架
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="审核学习问题"
      width="900px"
      class="audit-dialog"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="分类">
            <el-tag :type="getCategoryTagType(currentItem.category)">
              {{ getCategoryName(currentItem.category) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="悬赏金额">
            <span v-if="currentItem.reward && currentItem.reward > 0" style="color: #E6A23C; font-weight: bold">
              ¥{{ currentItem.reward }}
            </span>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="触发原因">
            {{ currentItem.auditTriggerReason || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="item-description">
          <h4>问题描述</h4>
          <div class="description-content" v-html="formatContent(currentItem.description)"></div>
        </div>

        <div v-if="itemImages.length > 0" class="item-images">
          <h4>问题图片</h4>
          <div class="images-grid">
            <el-image
              v-for="(img, index) in itemImages"
              :key="index"
              :src="getAvatarUrl(img)"
              :preview-src-list="itemImages.map(i => getAvatarUrl(i))"
              fit="cover"
              class="preview-image"
              preview-teleported
            />
          </div>
        </div>
      </div>

      <el-divider />

      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="拒绝原因"
          :rules="[{ required: !auditForm.approved, message: '拒绝时必须填写原因' }]"
        >
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="3"
            placeholder="拒绝时必须填写拒绝原因"
            :disabled="auditForm.approved"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAudit" :loading="submitting">
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="问题详情"
      width="900px"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="分类">
            <el-tag :type="getCategoryTagType(currentItem.category)">
              {{ getCategoryName(currentItem.category) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentItem.status)" size="small">
              {{ getStatusName(currentItem.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentItem.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="悬赏金额">
            <span v-if="currentItem.reward && currentItem.reward > 0" style="color: #E6A23C; font-weight: bold">
              ¥{{ currentItem.reward }}
            </span>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ currentItem.auditTime ? formatDate(currentItem.auditTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentItem.auditReason" label="审核原因" :span="2">
            {{ currentItem.auditReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentItem.auditTriggerReason" label="触发原因" :span="2">
            {{ currentItem.auditTriggerReason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="item-description">
          <h4>问题描述</h4>
          <div class="description-content" v-html="formatContent(currentItem.description)"></div>
        </div>

        <div v-if="itemImages.length > 0" class="item-images">
          <h4>问题图片</h4>
          <div class="images-grid">
            <el-image
              v-for="(img, index) in itemImages"
              :key="index"
              :src="getAvatarUrl(img)"
              :preview-src-list="itemImages.map(i => getAvatarUrl(i))"
              fit="cover"
              class="preview-image"
              preview-teleported
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminQuestionApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const loading = ref(false)
const questionList = ref([])
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const submitting = ref(false)
const currentItem = ref(null)
const itemImages = ref([])

const filters = reactive({
  auditStatus: 'PENDING', // 默认显示待审核
  category: '',
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  approved: true,
  reason: ''
})

// 获取审核列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      auditStatus: filters.auditStatus || 'ALL',
      category: filters.category || undefined,
      keyword: filters.keyword || undefined
    }
    
    const response = await adminQuestionApi.getPendingList(params)
    if (response.code === 200) {
      const records = response.data.records || []
      questionList.value = records
      pagination.total = response.data.total || 0
      
      // 如果当前筛选的是待审核，但没有数据，自动切换到全部
      if (filters.auditStatus === 'PENDING' && records.length === 0 && pagination.pageNum === 1) {
        filters.auditStatus = 'ALL'
        // 重新获取全部数据
        fetchQuestionList()
        return
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

// 处理审核
const handleAudit = (row) => {
  if (row.auditStatus !== 'PENDING') {
    ElMessage.warning('该问题不在待审核状态')
    return
  }
  currentItem.value = row
  // 解析图片
  if (row.images) {
    try {
      itemImages.value = typeof row.images === 'string' ? JSON.parse(row.images) : row.images
    } catch (e) {
      itemImages.value = []
    }
  } else {
    itemImages.value = []
  }
  auditForm.approved = true
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const handleSubmitAudit = async () => {
  if (!auditForm.approved && !auditForm.reason.trim()) {
    ElMessage.warning('拒绝审核必须填写拒绝原因')
    return
  }
  
  submitting.value = true
  try {
    const response = await adminQuestionApi.audit(currentItem.value.id, {
      approved: auditForm.approved,
      reason: auditForm.reason || null
    })
    if (response.code === 200) {
      ElMessage.success(auditForm.approved ? '审核通过' : '审核拒绝')
      auditDialogVisible.value = false
      fetchQuestionList()
    }
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentItem.value = row
  // 解析图片
  if (row.images) {
    try {
      itemImages.value = typeof row.images === 'string' ? JSON.parse(row.images) : row.images
    } catch (e) {
      itemImages.value = []
    }
  } else {
    itemImages.value = []
  }
  detailDialogVisible.value = true
}

// 处理筛选
const handleFilter = () => {
  pagination.pageNum = 1
  fetchQuestionList()
}

// 重置筛选
const handleReset = () => {
  filters.auditStatus = 'PENDING'
  filters.category = ''
  filters.keyword = ''
  pagination.pageNum = 1
  fetchQuestionList()
}

// 下架问题
const handleOffshelf = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入下架原因', '下架问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入下架原因',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '下架原因不能为空'
        }
        if (value.trim().length > 500) {
          return '下架原因不能超过500个字符'
        }
        return true
      }
    })
    
    if (!reason || !reason.trim()) {
      return
    }
    
    const response = await adminQuestionApi.offshelf(row.id, reason.trim())
    if (response.code === 200) {
      ElMessage.success('下架成功')
      fetchQuestionList()
    } else {
      ElMessage.error(response.message || '下架失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '下架失败')
    }
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchQuestionList()
}

// 处理页码变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchQuestionList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化内容
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

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
  return categoryMap[category] || category || '其他'
}

// 获取分类标签类型
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

// 获取状态名称
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

// 获取状态标签类型
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

onMounted(() => {
  fetchQuestionList()
})
</script>

<style scoped>
.question-audit-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
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
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.item-detail-card {
  padding: 20px;
}

.item-descriptions {
  margin-bottom: 20px;
}

.item-description {
  margin-bottom: 20px;
}

.item-description h4 {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 12px 0;
}

.description-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.item-images {
  margin-top: 20px;
}

.item-images h4 {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 12px 0;
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview-image {
  width: 200px;
  height: 200px;
  border-radius: 4px;
  border: 1px solid #E0E0E0;
  cursor: pointer;
}
</style>

