<template>
  <div class="list-container">
    <div class="page-header">
      <h1 class="page-title">收货地址</h1>
      <el-button type="primary" @click="showAddDialog = true">添加地址</el-button>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <div v-for="address in addressList" :key="address.id" class="address-card">
        <div class="address-header">
          <div class="address-info">
            <span class="receiver-name">{{ address.receiverName }}</span>
            <span class="receiver-phone">{{ address.receiverPhone }}</span>
            <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
          </div>
          <div class="address-actions">
            <el-button type="primary" text size="small" @click="handleEdit(address)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(address)">删除</el-button>
          </div>
        </div>
        <div class="address-content">
          <div class="address-text">{{ address.fullAddress }}</div>
          <div v-if="address.postalCode" class="postal-code">邮编：{{ address.postalCode }}</div>
        </div>
        <div class="address-footer">
          <el-button 
            v-if="address.isDefault !== 1" 
            type="primary" 
            text 
            size="small" 
            @click="handleSetDefault(address)">
            设为默认
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && addressList.length === 0" description="暂无收货地址" />

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingAddress ? '编辑地址' : '添加地址'" 
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="收货人姓名" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="收货人电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入收货人电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <el-row :gutter="12">
            <el-col :span="8">
              <el-input v-model="form.province" placeholder="省份" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="form.city" placeholder="城市" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="form.district" placeholder="区县" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="form.detailAddress" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入详细地址" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="邮政编码" prop="postalCode">
          <el-input v-model="form.postalCode" placeholder="选填，6位数字" maxlength="6" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi } from '@/api'

defineOptions({
  name: 'AddressList'
})

const loading = ref(false)
const submitting = ref(false)
const addressList = ref([])
const showAddDialog = ref(false)
const editingAddress = ref(null)
const formRef = ref(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: 0
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入收货人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  postalCode: [
    { pattern: /^\d{6}$/, message: '邮政编码必须是6位数字', trigger: 'blur' }
  ]
}

/**
 * 获取地址列表
 */
const fetchAddressList = async () => {
  loading.value = true
  try {
    const response = await addressApi.getList()
    if (response.code === 200) {
      addressList.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理编辑
 */
const handleEdit = (address) => {
  editingAddress.value = address
  form.receiverName = address.receiverName
  form.receiverPhone = address.receiverPhone
  form.province = address.province
  form.city = address.city
  form.district = address.district
  form.detailAddress = address.detailAddress
  form.postalCode = address.postalCode || ''
  form.isDefault = address.isDefault || 0
  showAddDialog.value = true
}

/**
 * 处理删除
 */
const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      type: 'warning'
    })
    await addressApi.delete(address.id)
    ElMessage.success('删除成功')
    fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 处理设置默认
 */
const handleSetDefault = async (address) => {
  try {
    await addressApi.setDefault(address.id)
    ElMessage.success('设置成功')
    fetchAddressList()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

/**
 * 处理提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (editingAddress.value) {
      // 编辑
      await addressApi.update(editingAddress.value.id, form)
      ElMessage.success('更新成功')
    } else {
      // 添加
      await addressApi.add(form)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    fetchAddressList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 处理对话框关闭
 */
const handleDialogClose = () => {
  editingAddress.value = null
  formRef.value?.resetFields()
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.postalCode = ''
  form.isDefault = 0
}

onMounted(() => {
  fetchAddressList()
})
</script>

<style scoped>
.list-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.receiver-phone {
  font-size: 14px;
  color: #606266;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-content {
  margin-bottom: 12px;
}

.address-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 4px;
}

.postal-code {
  font-size: 12px;
  color: #909399;
}

.address-footer {
  display: flex;
  justify-content: flex-end;
}
</style>

