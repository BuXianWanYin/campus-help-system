<template>
  <div class="users-management-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <p class="page-subtitle">管理系统用户，包括封禁和解封操作</p>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="邮箱：">
          <el-input
            v-model="filters.email"
            placeholder="请输入邮箱"
            clearable
            style="width: 200px"
            @keyup.enter="handleFilter"
          />
        </el-form-item>
        <el-form-item label="昵称：">
          <el-input
            v-model="filters.nickname"
            placeholder="请输入昵称"
            clearable
            style="width: 200px"
            @keyup.enter="handleFilter"
          />
        </el-form-item>
        <el-form-item label="角色：">
          <el-select v-model="filters.role" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态：">
          <el-select v-model="filters.status" placeholder="全部" style="width: 120px" @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="正常" :value="1" />
            <el-option label="已封禁" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      :data="userList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'primary'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isVerified" label="实名认证" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isVerified === 1 ? 'success' : 'info'" size="small">
            {{ row.isVerified === 1 ? '已认证' : '未认证' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="账号状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="banReason" label="封禁原因" width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1 && row.role !== 'ADMIN'"
            type="danger"
            size="small"
            @click="handleBan(row)"
          >
            封禁
          </el-button>
          <el-button
            v-else-if="row.status === 2"
            type="success"
            size="small"
            @click="handleUnban(row)"
          >
            解封
          </el-button>
          <span v-else class="text-muted">-</span>
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

    <!-- 封禁对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      title="封禁用户"
      width="500px"
    >
      <el-form :model="banForm" :rules="banRules" ref="banFormRef" label-width="100px">
        <el-form-item label="用户信息">
          <div class="user-info-display">
            <p><strong>昵称：</strong>{{ currentUser?.nickname }}</p>
            <p><strong>邮箱：</strong>{{ currentUser?.email }}</p>
          </div>
        </el-form-item>
        <el-form-item label="封禁类型" prop="banType" required>
          <el-radio-group v-model="banForm.banType">
            <el-radio label="TEMPORARY">临时封禁</el-radio>
            <el-radio label="PERMANENT">永久封禁</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="banForm.banType === 'TEMPORARY'"
          label="封禁天数"
          prop="banDays"
          required
        >
          <el-input-number
            v-model="banForm.banDays"
            :min="1"
            :max="365"
            style="width: 200px"
          />
          <span style="margin-left: 8px; color: var(--color-text-secondary);">天</span>
        </el-form-item>
        <el-form-item label="封禁原因" prop="reason" required>
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请填写封禁原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSubmitBan" :loading="submitting">
          确认封禁
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, adminApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const userList = ref([])
const banDialogVisible = ref(false)
const currentUser = ref(null)
const banFormRef = ref(null)

const filters = reactive({
  email: '',
  nickname: '',
  role: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const banForm = reactive({
  banType: 'TEMPORARY',
  banDays: 7,
  reason: ''
})

const banRules = {
  banType: [{ required: true, message: '请选择封禁类型', trigger: 'change' }],
  banDays: [{ required: true, message: '请输入封禁天数', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写封禁原因', trigger: 'blur' }]
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const response = await userApi.getUserPage({
      current: pagination.current,
      size: pagination.size,
      email: filters.email || undefined,
      nickname: filters.nickname || undefined,
      role: filters.role || undefined,
      status: filters.status || undefined
    })
    if (response.code === 200) {
      userList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

// 封禁用户
const handleBan = (row) => {
  currentUser.value = row
  banForm.banType = 'TEMPORARY'
  banForm.banDays = 7
  banForm.reason = ''
  banDialogVisible.value = true
}

// 提交封禁
const handleSubmitBan = async () => {
  if (!banFormRef.value) return
  
  await banFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await adminApi.banUser({
          userId: currentUser.value.id,
          banType: banForm.banType,
          reason: banForm.reason,
          banDays: banForm.banType === 'TEMPORARY' ? banForm.banDays : null
        })
        if (response.code === 200) {
          ElMessage.success('封禁成功')
          banDialogVisible.value = false
          fetchUserList()
        }
      } catch (error) {
        ElMessage.error(error.message || '封禁失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 解封用户
const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户 "${row.nickname}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminApi.unbanUser(row.id)
    if (response.code === 200) {
      ElMessage.success('解封成功')
      fetchUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解封失败')
    }
  }
}

// 筛选
const handleFilter = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  filters.email = ''
  filters.nickname = ''
  filters.role = ''
  filters.status = ''
  pagination.current = 1
  fetchUserList()
}

// 分页
const handleSizeChange = () => {
  fetchUserList()
}

const handlePageChange = () => {
  fetchUserList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.users-management-page {
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

.text-muted {
  color: var(--color-text-secondary);
  font-size: 14px;
}
</style>

