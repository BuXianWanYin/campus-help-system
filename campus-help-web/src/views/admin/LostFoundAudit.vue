<template>
  <div class="lost-found-audit-page">
    <div class="page-header">
      <h2 class="page-title">失物招领审核</h2>
      <p class="page-subtitle">审核用户提交的失物招领信息</p>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="类型：">
          <el-select v-model="filters.type" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="失物" value="LOST" />
            <el-option label="招领" value="FOUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类：">
          <el-select v-model="filters.category" placeholder="全部" style="width: 150px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="证件" value="证件" />
            <el-option label="衣物" value="衣物" />
            <el-option label="书籍" value="书籍" />
            <el-option label="其他" value="其他" />
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
      :data="lostFoundList"
      v-loading="loading"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" min-width="80" sortable />
      <el-table-column prop="type" label="类型" min-width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 'LOST' ? 'danger' : 'success'" size="small">
            {{ row.type === 'LOST' ? '失物' : '招领' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="100" />
      <el-table-column prop="location" label="地点" min-width="150" show-overflow-tooltip />
      <el-table-column prop="user" label="发布者" min-width="120">
        <template #default="{ row }">
          {{ row.user?.nickname || '未知用户' }}
        </template>
      </el-table-column>
      <el-table-column prop="auditTriggerReason" label="触发原因" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.auditTriggerReason || '-' }}
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
            type="primary"
            size="small"
            :icon="DocumentChecked"
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button
            type="info"
            size="small"
            :icon="View"
            @click="handleViewDetail(row)"
          >
            查看详情
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

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="审核失物招领"
      width="700px"
    >
      <div v-if="currentItem" class="item-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">
            <el-tag :type="currentItem.type === 'LOST' ? 'danger' : 'success'">
              {{ currentItem.type === 'LOST' ? '失物' : '招领' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentItem.location }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="触发原因" :span="2">
            {{ currentItem.auditTriggerReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="物品描述" :span="2">
            {{ currentItem.description }}
          </el-descriptions-item>
          <el-descriptions-item label="物品图片" :span="2" v-if="itemImages && itemImages.length > 0">
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
      
      <el-form :model="auditForm" label-width="120px" style="margin-top: 20px;">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="0">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="auditForm.auditResult === 0"
          label="拒绝原因"
          required
        >
          <el-input
            v-model="auditForm.auditReason"
            type="textarea"
            :rows="3"
            placeholder="请填写拒绝原因"
            maxlength="500"
            show-word-limit
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
      title="失物详情"
      width="800px"
    >
      <div v-if="currentItem" class="item-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">
            <el-tag :type="currentItem.type === 'LOST' ? 'danger' : 'success'">
              {{ currentItem.type === 'LOST' ? '失物' : '招领' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentItem.location }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="触发原因" :span="2">
            {{ currentItem.auditTriggerReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="物品描述" :span="2">
            {{ currentItem.description }}
          </el-descriptions-item>
          <el-descriptions-item label="联系方式" v-if="currentItem.contactInfo">
            {{ currentItem.contactInfo }}
          </el-descriptions-item>
          <el-descriptions-item label="悬赏金额" v-if="currentItem.reward">
            ¥{{ currentItem.reward }}
          </el-descriptions-item>
          <el-descriptions-item label="物品图片" :span="2" v-if="itemImages && itemImages.length > 0">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentChecked, View } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { getAvatarUrl } from '@/utils'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const lostFoundList = ref([])
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const itemImages = ref([])

const filters = reactive({
  type: '',
  category: '',
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const auditForm = reactive({
  auditResult: 1,
  auditReason: ''
})

// 获取待审核列表
const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const response = await adminApi.getPendingLostFoundList({
      current: pagination.current,
      size: pagination.size,
      type: filters.type || undefined,
      category: filters.category || undefined,
      keyword: filters.keyword || undefined
    })
    if (response.code === 200) {
      lostFoundList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

// 处理审核
const handleAudit = (row) => {
  currentItem.value = row
  // 解析图片
  if (row.images) {
    try {
      itemImages.value = JSON.parse(row.images)
    } catch (e) {
      itemImages.value = []
    }
  } else {
    itemImages.value = []
  }
  auditForm.auditResult = 1
  auditForm.auditReason = ''
  auditDialogVisible.value = true
}

// 提交审核
const handleSubmitAudit = async () => {
  if (auditForm.auditResult === 0 && !auditForm.auditReason.trim()) {
    ElMessage.warning('拒绝审核必须填写拒绝原因')
    return
  }
  
  submitting.value = true
  try {
    const response = await adminApi.auditLostFound(currentItem.value.id, {
      auditResult: auditForm.auditResult,
      auditReason: auditForm.auditReason || null
    })
    if (response.code === 200) {
      ElMessage.success(auditForm.auditResult === 1 ? '审核通过' : '审核拒绝')
      auditDialogVisible.value = false
      fetchLostFoundList()
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
      itemImages.value = JSON.parse(row.images)
    } catch (e) {
      itemImages.value = []
    }
  } else {
    itemImages.value = []
  }
  detailDialogVisible.value = true
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  fetchLostFoundList()
}

// 重置
const handleReset = () => {
  filters.type = ''
  filters.category = ''
  filters.keyword = ''
  pagination.current = 1
  fetchLostFoundList()
}

// 分页
const handleSizeChange = () => {
  fetchLostFoundList()
}

const handlePageChange = () => {
  fetchLostFoundList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchLostFoundList()
})
</script>

<style scoped>
.lost-found-audit-page {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
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

.filter-form {
  margin: 0;
}

.pagination-wrapper {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
}

.item-detail {
  margin-bottom: var(--spacing-lg);
}

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>

