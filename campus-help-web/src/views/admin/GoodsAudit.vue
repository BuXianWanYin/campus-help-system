<template>
  <div class="goods-audit-page">
    <div class="page-header">
      <h2 class="page-title">商品审核</h2>
      <p class="page-subtitle">审核用户提交的闲置商品信息，可查看所有审核历史</p>
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
          <el-select v-model="filters.category" placeholder="全部" style="width: 150px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="数码产品" value="数码产品" />
            <el-option label="图书教材" value="图书教材" />
            <el-option label="服装鞋包" value="服装鞋包" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="运动健身" value="运动健身" />
            <el-option label="乐器" value="乐器" />
            <el-option label="文创用品" value="文创用品" />
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
      :data="goodsList"
      v-loading="loading"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
    >
      <el-table-column prop="id" label="ID" min-width="80" sortable />
      <el-table-column prop="title" label="商品标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="100" />
      <el-table-column prop="currentPrice" label="价格" min-width="100">
        <template #default="{ row }">
          ¥{{ row.currentPrice }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" min-width="80" />
      <el-table-column prop="user" label="发布者" min-width="120">
        <template #default="{ row }">
          {{ row.user?.nickname || '未知用户' }}
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
      title="审核商品"
      width="900px"
      class="audit-dialog"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="商品标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ currentItem.currentPrice }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentItem.stock }}件</el-descriptions-item>
          <el-descriptions-item label="成色">{{ currentItem.condition }}</el-descriptions-item>
          <el-descriptions-item label="交易方式">{{ currentItem.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentItem.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ currentItem.auditTime ? formatDate(currentItem.auditTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="触发原因" :span="2">
            {{ currentItem.auditTriggerReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentItem.auditStatus === 'REJECTED' && currentItem.auditReason" label="拒绝原因" :span="2">
            <el-alert :title="currentItem.auditReason" type="error" :closable="false" />
          </el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">
            {{ currentItem.description }}
          </el-descriptions-item>
          <el-descriptions-item label="商品图片" :span="2" v-if="itemImages && itemImages.length > 0">
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
      
      <el-form :model="auditForm" label-width="120px" style="margin-top: 20px;" v-if="currentItem && currentItem.auditStatus === 'PENDING'">
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
      <div v-else-if="currentItem && currentItem.auditStatus !== 'PENDING'" style="margin-top: 20px; padding: 20px; background-color: #f5f7fa; border-radius: 8px;">
        <el-alert
          :title="currentItem.auditStatus === 'APPROVED' ? '该商品已审核通过' : '该商品已审核拒绝'"
          :type="currentItem.auditStatus === 'APPROVED' ? 'success' : 'error'"
          :closable="false"
        />
        <div v-if="currentItem.auditStatus === 'REJECTED' && currentItem.auditReason" style="margin-top: 10px;">
          <p style="color: #606266; font-size: 14px;"><strong>拒绝原因：</strong>{{ currentItem.auditReason }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="auditDialogVisible = false">{{ currentItem && currentItem.auditStatus === 'PENDING' ? '取消' : '关闭' }}</el-button>
        <el-button v-if="currentItem && currentItem.auditStatus === 'PENDING'" type="primary" @click="handleSubmitAudit" :loading="submitting">
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="1000px"
      class="detail-dialog"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="商品标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ currentItem.currentPrice }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentItem.stock }}件</el-descriptions-item>
          <el-descriptions-item label="成色">{{ currentItem.condition }}</el-descriptions-item>
          <el-descriptions-item label="交易方式">{{ currentItem.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentItem.auditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentItem.auditStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ currentItem.auditTime ? formatDate(currentItem.auditTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="触发原因" :span="2">
            {{ currentItem.auditTriggerReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentItem.auditStatus === 'REJECTED' && currentItem.auditReason" label="拒绝原因" :span="2">
            <el-alert :title="currentItem.auditReason" type="error" :closable="false" />
          </el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">
            {{ currentItem.description }}
          </el-descriptions-item>
          <el-descriptions-item label="商品图片" :span="2" v-if="itemImages && itemImages.length > 0">
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
import { getAvatarUrl } from '@/utils/image'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const goodsList = ref([])
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const itemImages = ref([])

const filters = reactive({
  auditStatus: 'PENDING', // 默认显示待审核
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

// 获取审核列表
const fetchGoodsList = async () => {
  loading.value = true
  try {
    const response = await adminApi.getPendingGoodsList({
      current: pagination.current,
      size: pagination.size,
      auditStatus: filters.auditStatus || 'ALL',
      category: filters.category || undefined,
      keyword: filters.keyword || undefined
    })
    if (response.code === 200) {
      const records = response.data.records || []
      goodsList.value = records
      pagination.total = response.data.total || 0
      
      // 如果当前筛选的是待审核，但没有数据，自动切换到全部
      if (filters.auditStatus === 'PENDING' && records.length === 0 && pagination.current === 1) {
        filters.auditStatus = 'ALL'
        // 重新获取全部数据
        fetchGoodsList()
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
  // 只有待审核状态的才能审核
  if (row.auditStatus !== 'PENDING') {
    ElMessage.warning('该商品不在待审核状态')
    return
  }
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
    const response = await adminApi.auditGoods(currentItem.value.id, {
      auditResult: auditForm.auditResult,
      auditReason: auditForm.auditReason || null
    })
    if (response.code === 200) {
      ElMessage.success(auditForm.auditResult === 1 ? '审核通过' : '审核拒绝')
      auditDialogVisible.value = false
      fetchGoodsList()
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
  fetchGoodsList()
}

// 重置
const handleReset = () => {
  filters.auditStatus = 'PENDING'
  filters.category = ''
  filters.keyword = ''
  pagination.current = 1
  fetchGoodsList()
}

// 分页
const handleSizeChange = () => {
  fetchGoodsList()
}

const handlePageChange = () => {
  fetchGoodsList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchGoodsList()
})
</script>

<style scoped>
.goods-audit-page {
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

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 对话框美化 */
.audit-dialog :deep(.el-dialog__body),
.detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.item-detail-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.item-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  background-color: #fafafa;
  white-space: nowrap;
  min-width: 120px;
}

.item-descriptions :deep(.el-descriptions__content) {
  color: #303133;
  word-break: break-word;
  white-space: normal;
}

.item-descriptions :deep(.el-descriptions__table) {
  border-radius: 4px;
  overflow: hidden;
}

.item-descriptions :deep(.el-descriptions__table td),
.item-descriptions :deep(.el-descriptions__table th) {
  padding: 12px 16px;
}
</style>

