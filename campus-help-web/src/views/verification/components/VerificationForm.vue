<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="120px"
    style="max-width: 700px;"
  >
    <el-form-item label="真实姓名" prop="realName">
      <el-input
        v-model="form.realName"
        placeholder="请输入真实姓名（2-10个中文字符）"
        maxlength="10"
        show-word-limit
        clearable
      />
    </el-form-item>
    
    <el-form-item label="身份证号" prop="idCard">
      <el-input
        v-model="form.idCard"
        placeholder="请输入18位身份证号"
        maxlength="18"
        show-word-limit
        clearable
      />
    </el-form-item>
    
    <el-form-item label="学号/工号" prop="studentId">
      <el-input
        v-model="form.studentId"
        placeholder="请输入学号或工号（6-20个字符）"
        maxlength="20"
        show-word-limit
        clearable
      />
    </el-form-item>
    
    <el-form-item label="用户类型" prop="userType">
      <div class="user-type-wrapper">
        <el-radio-group v-model="form.userType">
          <el-radio label="学生">学生</el-radio>
          <el-radio label="教师">教师</el-radio>
        </el-radio-group>
        <div v-if="form.userType" class="user-type-tip">
          <span v-if="form.userType === '学生'">学生：请上传学生证照片、校园卡照片或教务系统截图</span>
          <span v-else-if="form.userType === '教师'">教师：请上传教师证照片、工作证照片或校园系统截图</span>
        </div>
      </div>
    </el-form-item>
    
    <el-form-item label="证明文件" prop="proofImages">
      <div class="upload-wrapper">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          :action="uploadAction"
          :headers="uploadHeaders"
          :limit="3"
          :on-exceed="handleExceed"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-remove="handleRemove"
          :before-upload="beforeUpload"
          list-type="picture-card"
          accept="image/jpeg,image/jpg,image/png"
          :disabled="loading"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">
          <p>支持格式：jpg、jpeg、png</p>
          <p>单张图片不超过5MB</p>
          <p>至少上传1张，最多3张</p>
        </div>
        <div v-if="formErrors.proofImages" class="form-error">
          {{ formErrors.proofImages }}
        </div>
      </div>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        提交认证
      </el-button>
      <el-button v-if="showCancel" @click="handleCancel">取消</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi, fileApi } from '@/api'
import { getToken } from '@/utils/auth'

const props = defineProps({
  userInfo: {
    type: Object,
    default: () => ({})
  },
  showCancel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit-success', 'cancel'])

const userStore = useUserStore()
const formRef = ref(null)
const uploadRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const formErrors = reactive({
  proofImages: ''
})

const form = reactive({
  realName: '',
  idCard: '',
  studentId: '',
  userType: '',
  proofImages: []
})

// 上传配置
const uploadAction = computed(() => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseURL}/api/file/upload?module=verification`
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    'Authorization': `Bearer ${token}`
  }
})

// 监听 userInfo 变化，填充表单（用于重新提交）
watch(() => props.userInfo, (newVal) => {
  if (newVal && newVal.realName) {
    form.realName = newVal.realName || ''
    form.idCard = newVal.idCard || ''
    form.studentId = newVal.studentId || ''
    form.userType = newVal.userType || '学生'
    
    // 如果有已上传的证明文件，加载到 fileList
    if (newVal.verificationProof) {
      try {
        const proofUrls = JSON.parse(newVal.verificationProof)
        if (Array.isArray(proofUrls) && proofUrls.length > 0) {
          fileList.value = proofUrls.map((url, index) => ({
            uid: `existing-${index}`,
            name: `证明文件${index + 1}`,
            url: url.startsWith('http') ? url : `${import.meta.env.VITE_API_BASE_URL || ''}${url}`,
            status: 'success'
          }))
          form.proofImages = proofUrls
        }
      } catch (e) {
        console.error('解析证明文件失败', e)
      }
    }
  }
}, { immediate: true })

// 身份证号验证规则
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/.test(value)) {
    callback(new Error('身份证号格式不正确，请输入18位身份证号'))
  } else {
    callback()
  }
}

// 真实姓名验证（2-10个中文字符）
const validateRealName = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入真实姓名'))
  } else if (!/^[\u4e00-\u9fa5]{2,10}$/.test(value)) {
    callback(new Error('真实姓名必须是2-10个中文字符'))
  } else {
    callback()
  }
}

// 学号验证（6-20个字符）
const validateStudentId = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入学号/工号'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('学号/工号长度必须在6-20个字符之间'))
  } else {
    callback()
  }
}

const rules = {
  realName: [
    { required: true, validator: validateRealName, trigger: 'blur' }
  ],
  idCard: [
    { required: true, validator: validateIdCard, trigger: 'blur' }
  ],
  studentId: [
    { required: true, validator: validateStudentId, trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
}

// 文件上传前验证
const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isValidType) {
    ElMessage.error('只能上传 jpg/jpeg/png 格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 文件上传成功
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    const url = response.data
    if (!form.proofImages.includes(url)) {
      form.proofImages.push(url)
    }
    formErrors.proofImages = ''
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
    // 从 fileList 中移除失败的文件
    const index = fileList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 文件上传失败
const handleUploadError = (error, file) => {
  ElMessage.error('文件上传失败：' + (error.message || '未知错误'))
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

// 文件移除
const handleRemove = (file) => {
  if (file.url) {
    const index = form.proofImages.indexOf(file.url)
    if (index > -1) {
      form.proofImages.splice(index, 1)
    }
  }
  formErrors.proofImages = ''
}

// 文件数量超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张图片')
}

// 提交认证
const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 验证证明文件
  if (form.proofImages.length === 0) {
    formErrors.proofImages = '请至少上传1张证明文件'
    ElMessage.error('请至少上传1张证明文件')
    return
  }
  if (form.proofImages.length > 3) {
    formErrors.proofImages = '最多只能上传3张证明文件'
    ElMessage.error('最多只能上传3张证明文件')
    return
  }
  formErrors.proofImages = ''
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await userApi.submitVerification({
          realName: form.realName,
          idCard: form.idCard,
          studentId: form.studentId,
          userType: form.userType,
          proofImages: form.proofImages
        })
        if (response.code === 200) {
          ElMessage.success('认证信息提交成功，请等待管理员审核')
          emit('submit-success', response.data)
        }
      } catch (error) {
        ElMessage.error(error.message || '提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 重置表单
const handleReset = () => {
  form.realName = ''
  form.idCard = ''
  form.studentId = ''
  form.userType = ''
  form.proofImages = []
  fileList.value = []
  formErrors.proofImages = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}
</script>

<style scoped>
.user-type-wrapper {
  width: 100%;
}

.user-type-tip {
  font-size: 13px;
  color: #606266;
  margin-top: 12px;
  padding: 10px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  line-height: 1.6;
}

.upload-wrapper {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
  line-height: 1.8;
  padding: 0;
}

.upload-tip p {
  margin: 4px 0;
}

.form-error {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 8px;
  line-height: 1.5;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
  line-height: 1.5;
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 24px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
}

:deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  margin: 0;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  object-fit: cover;
}

/* 确保错误提示不会遮挡其他内容 */
:deep(.el-form-item.is-error) {
  margin-bottom: 28px;
}

:deep(.el-form-item.is-error .el-form-item__error) {
  position: relative;
  top: 0;
  left: 0;
  padding-top: 4px;
}
</style>

