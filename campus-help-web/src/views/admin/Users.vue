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
      <div class="filter-actions">
        <el-button type="primary" @click="handleAddUser">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <el-table
      :data="userList"
      v-loading="loading"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
    >
      <el-table-column prop="id" label="用户ID" min-width="80" sortable />
      <el-table-column label="头像" min-width="80">
        <template #default="{ row }">
          <el-avatar :size="40" :src="getAvatarUrl(row.avatar)">
            {{ row.nickname?.charAt(0) || row.email?.charAt(0) || 'U' }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="120" sortable>
        <template #default="{ row }">
          {{ row.nickname || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="gender" label="性别" min-width="80">
        <template #default="{ row }">
          <span v-if="row.gender === 1">男</span>
          <span v-else-if="row.gender === 2">女</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'primary'" size="small" effect="plain">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isVerified" label="实名认证" min-width="140">
        <template #default="{ row }">
          <template v-if="row.role === 'ADMIN'">
            <span class="text-muted">-</span>
          </template>
          <template v-else>
            <el-tag :type="row.isVerified === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.isVerified === 1 ? '已认证' : '未认证' }}
            </el-tag>
            <el-button
              v-if="row.isVerified === 1"
              type="primary"
              link
              size="small"
              style="margin-left: 8px;"
              @click="handleViewVerification(row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="账号状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.status === 1 ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="banReason" label="封禁原因" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="注册时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 1 && row.role !== 'ADMIN'"
              type="warning"
              size="small"
              :icon="Lock"
              @click="handleBan(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else-if="row.status === 2"
              type="success"
              size="small"
              :icon="Unlock"
              @click="handleUnban(row)"
            >
              解封
            </el-button>
            <el-button
              v-if="row.role !== 'ADMIN'"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
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

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="600px"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="editForm.grade" placeholder="请输入年级，如：2021级" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="editForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增用户"
      width="600px"
    >
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addForm.password"
            type="password"
            placeholder="请输入密码（至少8位，包含字母和数字）"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="addForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">保密</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="addForm.grade" placeholder="请输入年级，如：2021级" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="addForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd" :loading="submitting">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看认证信息对话框 -->
    <el-dialog
      v-model="verificationDialogVisible"
      title="认证信息"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="currentUser">
        <el-descriptions-item label="真实姓名">
          {{ currentUser.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ currentUser.studentId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">
          {{ maskIdCard(currentUser.idCard) }}
        </el-descriptions-item>
        <el-descriptions-item label="用户类型">
          {{ currentUser.userType || '学生' }}
        </el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="currentUser.isVerified === 1 ? 'success' : 'info'">
            {{ currentUser.isVerified === 1 ? '已认证' : '未认证' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认证证明" v-if="currentUser.verificationProof">
          <div class="proof-images">
            <el-image
              v-for="(img, index) in parseProofImages(currentUser.verificationProof)"
              :key="index"
              :src="img"
              :preview-src-list="parseProofImages(currentUser.verificationProof)"
              fit="cover"
              style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px;"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDate(currentUser.verificationSubmitTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">
          {{ formatDate(currentUser.verificationAuditTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="currentUser.verificationAuditReason">
          {{ currentUser.verificationAuditReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Lock, Unlock } from '@element-plus/icons-vue'
import { userApi, adminApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const loading = ref(false)
const submitting = ref(false)
const userList = ref([])
const banDialogVisible = ref(false)
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const verificationDialogVisible = ref(false)
const currentUser = ref(null)
const banFormRef = ref(null)
const editFormRef = ref(null)
const addFormRef = ref(null)

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

const editForm = reactive({
  id: null,
  nickname: '',
  email: '',
  gender: 0,
  grade: '',
  major: '',
  phone: '',
  role: 'USER'
})

const addForm = reactive({
  email: '',
  password: '',
  nickname: '',
  gender: 0,
  grade: '',
  major: '',
  phone: '',
  role: 'USER'
})

const banRules = {
  banType: [{ required: true, message: '请选择封禁类型', trigger: 'change' }],
  banDays: [{ required: true, message: '请输入封禁天数', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写封禁原因', trigger: 'blur' }]
}

const editRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ]
}

const addRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码至少8位，且包含字母和数字', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ]
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
      `确定要解封用户 "${row.nickname || row.email}" 吗？`,
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

// 编辑用户
const handleEdit = (row) => {
  currentUser.value = row
  editForm.id = row.id
  editForm.nickname = row.nickname || ''
  editForm.email = row.email || ''
  editForm.gender = row.gender || 0
  editForm.grade = row.grade || ''
  editForm.major = row.major || ''
  editForm.phone = row.phone || ''
  editForm.role = row.role || 'USER'
  editDialogVisible.value = true
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await userApi.updateUser(editForm.id, editForm)
        if (response.code === 200) {
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          fetchUserList()
        }
      } catch (error) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 新增用户
const handleAddUser = () => {
  addForm.email = ''
  addForm.password = ''
  addForm.nickname = ''
  addForm.gender = 0
  addForm.grade = ''
  addForm.major = ''
  addForm.phone = ''
  addForm.role = 'USER'
  addDialogVisible.value = true
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const response = await adminApi.createUser(addForm)
        if (response.code === 200) {
          ElMessage.success('创建成功')
          addDialogVisible.value = false
          fetchUserList()
        }
      } catch (error) {
        ElMessage.error(error.message || '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.nickname || row.email}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminApi.deleteUser(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 查看认证信息
const handleViewVerification = (row) => {
  currentUser.value = row
  verificationDialogVisible.value = true
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

// 身份证号脱敏
const maskIdCard = (idCard) => {
  if (!idCard) return '-'
  if (idCard.length <= 8) return idCard
  return idCard.substring(0, 4) + '****' + idCard.substring(idCard.length - 4)
}

// 解析认证证明图片
const parseProofImages = (proof) => {
  if (!proof) return []
  try {
    return JSON.parse(proof)
  } catch (e) {
    return []
  }
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

.filter-actions {
  margin-top: var(--spacing-md);
  display: flex;
  justify-content: flex-end;
}

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

