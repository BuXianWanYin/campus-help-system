<template>
  <div class="content-management-page">
    <div class="page-header">
      <h2 class="page-title">失物招领管理</h2>
      <p class="page-subtitle">管理已审核通过的失物招领信息，可进行下架等操作</p>
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
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="待认领" value="PENDING_CLAIM" />
            <el-option label="认领中" value="CLAIMING" />
            <el-option label="已认领" value="CLAIMED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点：">
          <el-input
            v-model="filters.location"
            placeholder="搜索地点"
            style="width: 150px"
            clearable
            @keyup.enter="handleFilter"
          />
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
      <el-table-column prop="type" label="类型" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'LOST' ? 'danger' : 'success'" size="small" style="white-space: nowrap;">
            {{ row.type === 'LOST' ? '失物' : '招领' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" min-width="100" />
      <el-table-column prop="lostLocation" label="地点" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.lostLocation || '-' }}
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
          <el-tag v-if="row.status === 'PENDING_CLAIM'" type="info" size="small">待认领</el-tag>
          <el-tag v-else-if="row.status === 'CLAIMING'" type="warning" size="small">认领中</el-tag>
          <el-tag v-else-if="row.status === 'CLAIMED'" type="success" size="small">已认领</el-tag>
          <el-tag v-else-if="row.status === 'CLOSED'" size="small">已关闭</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" min-width="100" sortable />
      <el-table-column prop="createTime" label="发布时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180" fixed="right">
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
            v-if="row.status !== 'CLOSED'"
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
      title="失物详情"
      width="1000px"
      class="detail-dialog"
    >
      <div v-if="currentItem" class="item-detail-card">
        <el-descriptions :column="2" border class="item-descriptions">
          <el-descriptions-item label="类型">
            <el-tag :type="currentItem.type === 'LOST' ? 'danger' : 'success'">
              {{ currentItem.type === 'LOST' ? '失物' : '招领' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentItem.lostLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentItem.status === 'PENDING_CLAIM'" type="info">待认领</el-tag>
            <el-tag v-else-if="currentItem.status === 'CLAIMING'" type="warning">认领中</el-tag>
            <el-tag v-else-if="currentItem.status === 'CLAIMED'" type="success">已认领</el-tag>
            <el-tag v-else-if="currentItem.status === 'CLOSED'" size="small">已关闭</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentItem.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.user?.nickname || '未知用户' }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentItem.viewCount || 0 }}</el-descriptions-item>
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
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

/**
 * 失物招领管理页面
 * 管理已审核通过的失物招领信息，可进行下架等操作
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { lostFoundApi, adminApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()

const loading = ref(false)
const lostFoundList = ref([])
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const itemImages = ref([])

// 筛选条件
const filters = reactive({
  type: '',
  category: '',
  status: '',
  location: '',
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取列表（只查询已审核通过的）
const fetchLostFoundList = async () => {
  loading.value = true
  try {
    const response = await lostFoundApi.getList({
      pageNum: pagination.current,
      pageSize: pagination.size,
      type: filters.type || undefined,
      category: filters.category || undefined,
      status: filters.status || undefined,
      location: filters.location || undefined,
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
      lostFoundList.value = records
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

// 下架
const handleOffshelf = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入下架原因',
      '下架失物',
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
    const response = await adminApi.offshelfLostFound(row.id, { reason: reason.trim() })
    if (response.code === 200) {
      ElMessage.success('下架成功')
      fetchLostFoundList()
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
  fetchLostFoundList()
}

// 重置
const handleReset = () => {
  filters.type = ''
  filters.category = ''
  filters.status = ''
  filters.location = ''
  filters.keyword = ''
  pagination.current = 1
  fetchLostFoundList()
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchLostFoundList()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchLostFoundList()
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
  fetchLostFoundList()
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

