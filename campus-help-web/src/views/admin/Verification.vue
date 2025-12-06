<template>
  <div class="verification-audit-page">
    <div class="page-header">
      <h2 class="page-title">实名认证审核</h2>
      <p class="page-subtitle">审核用户提交的实名认证申请</p>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审核状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 150px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="VERIFIED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词：">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索昵称、邮箱、真实姓名或学号"
            style="width: 250px"
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
      :data="verificationList"
      v-loading="loading"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'verificationSubmitTime', order: 'descending' }"
    >
      <el-table-column prop="id" label="用户ID" min-width="80" sortable />
      <el-table-column label="用户" min-width="180" sortable>
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="40" :src="getAvatarUrl(row.avatar)">
              {{ (row.nickname || row.email || 'U').charAt(0) }}
            </el-avatar>
            <span class="user-name">{{ row.nickname || row.email || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="realName" label="真实姓名" min-width="120" sortable>
        <template #default="{ row }">
          {{ row.realName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="studentId" label="学号" min-width="150" sortable>
        <template #default="{ row }">
          {{ row.studentId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="userType" label="用户类型" min-width="100">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.userType || '学生' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="verificationStatus" label="审核状态" min-width="120" sortable>
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.verificationStatus)"
            size="small"
            effect="plain"
          >
            {{ getStatusText(row.verificationStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="verificationSubmitTime" label="提交时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.verificationSubmitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.verificationStatus === 'PENDING'"
            type="primary"
            size="small"
            :icon="DocumentChecked"
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            :icon="View"
            @click="handleViewDetail(row)"
          >
            详情
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
      title="审核实名认证"
      width="600px"
    >
      <el-form :model="auditForm" label-width="120px">
        <el-form-item label="用户信息">
          <div class="user-info-display">
            <p><strong>昵称：</strong>{{ currentUser?.nickname }}</p>
            <p><strong>邮箱：</strong>{{ currentUser?.email }}</p>
            <p><strong>真实姓名：</strong>{{ currentUser?.realName }}</p>
            <p><strong>学号：</strong>{{ currentUser?.studentId }}</p>
            <p><strong>身份证号：</strong>{{ maskIdCard(currentUser?.idCard) }}</p>
            <p><strong>用户类型：</strong>{{ currentUser?.userType || '学生' }}</p>
          </div>
        </el-form-item>
        <el-form-item label="认证证明">
          <div v-if="proofImages && proofImages.length > 0" class="proof-images">
            <el-image
              v-for="(img, index) in proofImages"
              :key="index"
              :src="img"
              :preview-src-list="proofImages"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px;"
            />
          </div>
          <span v-else class="text-muted">未上传证明文件</span>
        </el-form-item>
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
  </div>
</template>

/**
 * 实名认证审核页面
 * 管理员审核用户提交的实名认证申请
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentChecked, View } from '@element-plus/icons-vue'
import { adminApi, userApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const verificationList = ref([])
const auditDialogVisible = ref(false)
const currentUser = ref(null)
const proofImages = ref([])

// 筛选条件
const filters = reactive({
  status: 'PENDING', // 默认显示待审核
  keyword: '' // 关键词搜索
})

// 分页信息
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
const fetchVerificationList = async () => {
  loading.value = true
  try {
    const response = await adminApi.getVerificationList({
      current: pagination.current,
      size: pagination.size,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined
    })
    if (response.code === 200) {
      const records = response.data.records || []
      verificationList.value = records
      pagination.total = response.data.total || 0
      
      // 如果当前筛选的是待审核，但没有数据，自动切换到全部
      if (filters.status === 'PENDING' && records.length === 0 && pagination.current === 1) {
        filters.status = ''
        // 重新获取全部数据
        fetchVerificationList()
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
  currentUser.value = row
  // 解析认证证明文件
  if (row.verificationProof) {
    try {
      proofImages.value = JSON.parse(row.verificationProof)
    } catch (e) {
      proofImages.value = []
    }
  } else {
    proofImages.value = []
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
    const response = await userApi.auditVerification({
      userId: currentUser.value.id,
      auditResult: auditForm.auditResult,
      auditReason: auditForm.auditReason || null
    })
    if (response.code === 200) {
      ElMessage.success(auditForm.auditResult === 1 ? '审核通过' : '审核拒绝')
      auditDialogVisible.value = false
      fetchVerificationList()
    }
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentUser.value = row
  if (row.verificationProof) {
    try {
      proofImages.value = JSON.parse(row.verificationProof)
    } catch (e) {
      proofImages.value = []
    }
  } else {
    proofImages.value = []
  }
  auditDialogVisible.value = true
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  fetchVerificationList()
}

// 重置
const handleReset = () => {
  filters.status = 'PENDING'
  pagination.current = 1
  fetchVerificationList()
}

// 分页
const handleSizeChange = () => {
  fetchVerificationList()
}

const handlePageChange = () => {
  fetchVerificationList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'VERIFIED': 'success',
    'REJECTED': 'danger',
    'NOT_VERIFIED': 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'VERIFIED': '已通过',
    'REJECTED': '已拒绝',
    'NOT_VERIFIED': '未认证'
  }
  return map[status] || '未知'
}

// 身份证号脱敏
const maskIdCard = (idCard) => {
  if (!idCard) return '-'
  if (idCard.length <= 8) return idCard
  return idCard.substring(0, 4) + '****' + idCard.substring(idCard.length - 4)
}

onMounted(() => {
  fetchVerificationList()
})
</script>

<style scoped>
.verification-audit-page {
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

.user-info-display {
  background-color: var(--color-bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.user-info-display p {
  margin: var(--spacing-xs) 0;
  font-size: 14px;
}

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.text-muted {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: var(--color-text-primary);
}
</style>

