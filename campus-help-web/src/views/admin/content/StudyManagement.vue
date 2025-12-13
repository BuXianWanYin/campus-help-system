<template>
  <div class="content-management-page">
    <div class="page-header">
      <h2 class="page-title">学习互助管理</h2>
      <p class="page-subtitle">管理已审核通过的学习问题，可进行下架等操作</p>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
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
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="待回答" value="PENDING_ANSWER" />
            <el-option label="已回答" value="ANSWERED" />
            <el-option label="已解决" value="SOLVED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已下架" value="ADMIN_OFFSHELF" />
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
      :default-sort="{ prop: 'createTime', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" min-width="80" sortable />
      <el-table-column prop="title" label="问题标题" min-width="250" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="100">
        <template #default="{ row }">
          {{ getCategoryName(row.category) }}
        </template>
      </el-table-column>
      <el-table-column label="发布者" min-width="150">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-avatar :size="32" :src="getAvatarUrl(row.user?.avatar)">
              {{ row.user?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span>{{ row.user?.nickname || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'PENDING_ANSWER'" type="info" size="small">待回答</el-tag>
          <el-tag v-else-if="row.status === 'ANSWERED'" type="warning" size="small">已回答</el-tag>
          <el-tag v-else-if="row.status === 'SOLVED'" type="success" size="small">已解决</el-tag>
          <el-tag v-else-if="row.status === 'CANCELLED'" size="small">已取消</el-tag>
          <el-tag v-else-if="row.status === 'ADMIN_OFFSHELF'" type="warning" size="small">已下架</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="answerCount" label="回答数" min-width="80" sortable />
      <el-table-column prop="viewCount" label="浏览量" min-width="100" sortable />
      <el-table-column prop="createTime" label="发布时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <el-button
            type="success"
            size="small"
            :icon="View"
            @click="handleViewDetail(row)"
          >
            详情
          </el-button>
          <el-button
            type="primary"
            size="small"
            :icon="ChatDotRound"
            @click="handleManageAnswers(row)"
          >
            回答管理
          </el-button>
          <el-button
            v-if="row.status !== 'ADMIN_OFFSHELF' && row.status !== 'SOLVED' && row.status !== 'CANCELLED'"
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
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="问题详情"
      width="1000px"
      class="detail-dialog"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="问题标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ getCategoryName(currentItem.category) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentItem.status === 'PENDING_ANSWER'" type="info">待回答</el-tag>
            <el-tag v-else-if="currentItem.status === 'ANSWERED'" type="warning">已回答</el-tag>
            <el-tag v-else-if="currentItem.status === 'SOLVED'" type="success">已解决</el-tag>
            <el-tag v-else-if="currentItem.status === 'CANCELLED'" size="small">已取消</el-tag>
            <el-tag v-else-if="currentItem.status === 'ADMIN_OFFSHELF'" type="warning">已下架</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="回答数">{{ currentItem.answerCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentItem.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">
            {{ currentItem.description }}
          </el-descriptions-item>
          <el-descriptions-item label="问题图片" :span="2" v-if="itemImages && itemImages.length > 0">
            <div class="proof-images">
              <el-image
                v-for="(img, index) in itemImages"
                :key="index"
                :src="getAvatarUrl(img)"
                :preview-src-list="itemImages.map(i => getAvatarUrl(i))"
                fit="cover"
                style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px;"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 回答管理对话框 -->
    <el-dialog
      v-model="answerDialogVisible"
      :title="`回答管理 - ${currentQuestion?.title || ''}`"
      width="1200px"
      class="answer-dialog"
    >
      <div v-loading="answerLoading">
        <div v-if="answerList.length === 0 && !answerLoading" class="empty-state">
          <el-empty description="暂无回答" />
        </div>
        <el-table v-else :data="answerList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="回答者" width="150">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-avatar :size="32" :src="getAvatarUrl(row.user?.avatar)">
                  {{ row.user?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span>{{ row.user?.nickname || '未知用户' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="回答内容" min-width="300" show-overflow-tooltip />
          <el-table-column label="审核状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.auditStatus === 'PENDING'" type="warning" size="small">待审核</el-tag>
              <el-tag v-else-if="row.auditStatus === 'APPROVED'" type="success" size="small">已通过</el-tag>
              <el-tag v-else-if="row.auditStatus === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="是否采纳" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isAccepted === 1" type="success" size="small">已采纳</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="回答时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.auditStatus === 'PENDING'"
                type="success"
                size="small"
                :icon="Check"
                @click="handleAuditAnswer(row, true)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.auditStatus === 'PENDING'"
                type="danger"
                size="small"
                :icon="Close"
                @click="handleAuditAnswer(row, false)"
              >
                拒绝
              </el-button>
              <el-button
                v-if="row.auditStatus === 'APPROVED' || row.auditStatus === 'REJECTED'"
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDeleteAnswer(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="answerDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核回答对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.approved ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!auditForm.approved" label="拒绝原因" required>
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAuditAnswer">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除回答对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除回答"
      width="500px"
    >
      <el-form :model="deleteForm" label-width="100px">
        <el-form-item label="删除原因" required>
          <el-input
            v-model="deleteForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入删除原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteAnswer">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

/**
 * 学习互助管理页面
 * 管理已审核通过的学习问题，可进行下架等操作，支持回答管理
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Check, Close, Delete } from '@element-plus/icons-vue'
import { questionApi, adminQuestionApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const loading = ref(false)
const questionList = ref([])
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const itemImages = ref([])

// 回答管理相关
const answerDialogVisible = ref(false)
const currentQuestion = ref(null)
const answerList = ref([])
const answerLoading = ref(false)
const auditDialogVisible = ref(false)
const currentAnswer = ref(null)
const auditForm = reactive({
  approved: true,
  reason: ''
})
const deleteDialogVisible = ref(false)
const deleteForm = reactive({
  reason: ''
})

const filters = reactive({
  category: '',
  status: '',
  keyword: ''
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
    'OTHER': '其他'
  }
  return categoryMap[category] || category || '-'
}

// 获取列表（只查询已审核通过的）
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const response = await questionApi.getList({
      pageNum: pagination.current,
      pageSize: pagination.size,
      category: filters.category || undefined,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined
    })
    if (response.code === 200) {
      const pageData = response.data
      let records = []
      if (Array.isArray(pageData)) {
        records = pageData
      } else if (pageData.records) {
        records = pageData.records
      } else if (pageData.list) {
        records = pageData.list
      }
      questionList.value = records
      pagination.total = pageData.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '获取列表失败')
  } finally {
    loading.value = false
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

// 回答管理
const handleManageAnswers = async (row) => {
  currentQuestion.value = row
  answerDialogVisible.value = true
  answerList.value = []
  answerLoading.value = true
  
  try {
    const response = await adminQuestionApi.getAnswersByQuestionId(row.id)
    if (response.code === 200) {
      answerList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取回答列表失败')
    }
  } catch (error) {
    console.error('获取回答列表失败:', error)
    ElMessage.error('获取回答列表失败')
  } finally {
    answerLoading.value = false
  }
}

// 审核回答
const handleAuditAnswer = (row, approved) => {
  currentAnswer.value = row
  auditForm.approved = approved
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 确认审核回答
const confirmAuditAnswer = async () => {
  if (!auditForm.approved && (!auditForm.reason || auditForm.reason.trim().length === 0)) {
    ElMessage.warning('拒绝时必须填写拒绝原因')
    return
  }
  
  try {
    const response = await adminQuestionApi.auditAnswer(currentAnswer.value.id, {
      approved: auditForm.approved,
      reason: auditForm.reason.trim()
    })
    if (response.code === 200) {
      ElMessage.success(auditForm.approved ? '审核通过' : '审核拒绝')
      auditDialogVisible.value = false
      // 刷新回答列表
      handleManageAnswers(currentQuestion.value)
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 删除回答
const handleDeleteAnswer = (row) => {
  currentAnswer.value = row
  deleteForm.reason = ''
  deleteDialogVisible.value = true
}

// 确认删除回答
const confirmDeleteAnswer = async () => {
  if (!deleteForm.reason || deleteForm.reason.trim().length === 0) {
    ElMessage.warning('请填写删除原因')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除该回答吗？删除后将发送通知给回答者。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminQuestionApi.deleteAnswer(currentAnswer.value.id, deleteForm.reason.trim())
    if (response.code === 200) {
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      // 刷新回答列表
      handleManageAnswers(currentQuestion.value)
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 下架
const handleOffshelf = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入下架原因',
      '下架问题',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请填写下架原因',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '下架原因不能为空'
          }
          return true
        }
      }
    )
    
    // 调用下架接口
    const response = await adminQuestionApi.offshelf(row.id, reason.trim())
    if (response.code === 200) {
      ElMessage.success('下架成功')
      fetchQuestionList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '下架失败')
    }
  }
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  fetchQuestionList()
}

// 重置
const handleReset = () => {
  filters.category = ''
  filters.status = ''
  filters.keyword = ''
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
.content-management-page {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.filter-bar {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg);
  background-color: var(--color-bg-primary);
  border-radius: var(--radius-md);
}

.pagination-wrapper {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
}

.item-detail-card {
  padding: var(--spacing-lg);
}

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}
</style>

