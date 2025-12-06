<template>
  <div class="sensitive-word-page">
    <div class="page-header">
      <h2 class="page-title">敏感词配置</h2>
      <p class="page-subtitle">管理系统敏感词库，用于内容审核</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索敏感词"
          style="width: 250px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加敏感词
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="handleReload">
          <el-icon><Refresh /></el-icon>
          重新加载词库
        </el-button>
      </div>
    </div>

    <el-table
      :data="sensitiveWordList"
      v-loading="loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" min-width="80" sortable />
      <el-table-column prop="word" label="敏感词" min-width="200" />
      <el-table-column prop="level" label="严重程度" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.level === 'SEVERE'" type="danger" size="small">严重</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="160" sortable>
        <template #default="{ row }">
          {{ formatDate(row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input
            v-model="form.word"
            placeholder="请输入敏感词，支持多个词用逗号分隔批量添加"
            maxlength="100"
            show-word-limit
            clearable
          />
          <div class="form-tip">
            <el-text type="info" size="small">
              支持批量添加，多个敏感词用逗号（,）分隔
            </el-text>
          </div>
        </el-form-item>
        <el-form-item label="严重程度" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio label="NORMAL">普通</el-radio>
            <el-radio label="SEVERE">严重</el-radio>
          </el-radio-group>
          <div class="form-tip">
            <el-text type="info" size="small">
              普通：触发人工审核；严重：直接拒绝
            </el-text>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

/**
 * 敏感词配置页面
 * 管理系统敏感词库，用于内容审核
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const sensitiveWordList = ref([])
const searchKeyword = ref('')
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加敏感词')
const formRef = ref(null)

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表单
const form = reactive({
  id: null,
  word: '',
  level: 'NORMAL'
})

// 表单验证规则
const rules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { min: 1, max: 50, message: '敏感词长度在1到50个字符之间', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择严重程度', trigger: 'change' }
  ]
}

// 获取敏感词列表
const fetchSensitiveWordList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const response = await adminApi.getSensitiveWordList(params)
    if (response.code === 200) {
      sensitiveWordList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取敏感词列表失败')
    }
  } catch (error) {
    console.error('获取敏感词列表失败:', error)
    ElMessage.error('获取敏感词列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchSensitiveWordList()
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
  pagination.current = 1
  fetchSensitiveWordList()
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '添加敏感词'
  form.id = null
  form.word = ''
  form.level = 'NORMAL'
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑敏感词'
  form.id = row.id
  form.word = row.word
  form.level = row.level
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (form.id) {
        // 更新
        const response = await adminApi.updateSensitiveWord(form.id, {
          word: form.word.trim(),
          level: form.level
        })
        if (response.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          fetchSensitiveWordList()
        } else {
          ElMessage.error(response.message || '更新失败')
        }
      } else {
        // 添加
        const response = await adminApi.addSensitiveWord({
          word: form.word.trim(),
          level: form.level
        })
        if (response.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          fetchSensitiveWordList()
        } else {
          ElMessage.error(response.message || '添加失败')
        }
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除敏感词"${row.word}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminApi.deleteSensitiveWord(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchSensitiveWordList()
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

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的敏感词')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 个敏感词吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminApi.batchDeleteSensitiveWords(selectedIds.value)
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      fetchSensitiveWordList()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 重新加载词库
const handleReload = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重新加载敏感词库吗？这将刷新内存中的敏感词缓存。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await adminApi.reloadSensitiveWords()
    if (response.code === 200) {
      ElMessage.success('重新加载成功')
    } else {
      ElMessage.error(response.message || '重新加载失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新加载失败:', error)
      ElMessage.error('重新加载失败')
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchSensitiveWordList()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchSensitiveWordList()
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
    minute: '2-digit',
    second: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchSensitiveWordList()
})
</script>

<style scoped>
.sensitive-word-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 100px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header .page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
}

.page-header .page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.toolbar .toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar .toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.level-option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-tip {
  margin-top: 0;
  line-height: 1.5;
}
</style>

