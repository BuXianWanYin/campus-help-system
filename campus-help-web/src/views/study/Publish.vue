<template>
  <div class="publish-container">
    <div class="publish-header">
      <h1 class="page-title">发布学习问题</h1>
      <el-button type="text" @click="goBack">返回</el-button>
    </div>

    <div class="publish-content">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="top"
      >
        <!-- 问题分类 -->
        <el-form-item label="问题分类" prop="category" class="form-item-block">
          <el-select v-model="form.category" placeholder="请选择问题分类" style="width: 100%">
            <el-option label="数学" value="MATH" />
            <el-option label="物理" value="PHYSICS" />
            <el-option label="化学" value="CHEMISTRY" />
            <el-option label="生物" value="BIOLOGY" />
            <el-option label="计算机" value="COMPUTER" />
            <el-option label="英语" value="ENGLISH" />
            <el-option label="文学" value="LITERATURE" />
            <el-option label="历史" value="HISTORY" />
            <el-option label="哲学" value="PHILOSOPHY" />
            <el-option label="经济" value="ECONOMICS" />
            <el-option label="管理" value="MANAGEMENT" />
            <el-option label="法律" value="LAW" />
            <el-option label="教育" value="EDUCATION" />
            <el-option label="艺术" value="ART" />
            <el-option label="工程" value="ENGINEERING" />
            <el-option label="医学" value="MEDICINE" />
            <el-option label="农学" value="AGRICULTURE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <!-- 问题标题 -->
        <el-form-item label="问题标题" prop="title" class="form-item-block">
          <el-input
            v-model="form.title"
            placeholder="请输入问题标题，简洁明了地描述您的问题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 问题描述 -->
        <el-form-item label="问题描述" prop="description" class="form-item-block">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="请详细描述您的问题，包括具体的内容、您的疑问等，至少10个字"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- 问题图片 -->
        <el-form-item label="问题图片" prop="images" class="form-item-block">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            accept="image/*"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">最多上传3张图片，支持jpg、png格式，单张图片不超过10MB</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-item-block">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            发布问题
          </el-button>
          <el-button size="large" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img :src="previewImageUrl" alt="预览图片" style="width: 100%" />
    </el-dialog>
  </div>
</template>

/**
 * 发布学习问题页面
 * 用户发布学习互助问题
 */

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { questionApi, fileApi } from '@/api'

const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')
const imageList = ref([])

// 发布表单数据
const form = reactive({
  category: '',
  title: '',
  description: ''
})

const rules = {
  category: [
    { required: true, message: '请选择问题分类', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在1到200个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述至少10个字，最多2000个字', trigger: 'blur' }
  ]
}

/**
 * 图片上传前检查
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB！')
    return false
  }
  return false // 阻止自动上传
}

/**
 * 预览图片
 */
const handlePreview = (file) => {
  previewImageUrl.value = file.url || URL.createObjectURL(file.raw)
  previewVisible.value = true
}

/**
 * 移除图片
 */
const handleRemove = (file) => {
  const index = imageList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    imageList.value.splice(index, 1)
  }
}

/**
 * 超出限制
 */
const handleExceed = () => {
  ElMessage.warning('最多只能上传3张图片！')
}

/**
 * 上传图片
 */
const uploadImages = async () => {
  const uploadedUrls = []
  
  for (const file of imageList.value) {
    if (file.raw) {
      try {
        const response = await fileApi.upload(file.raw, 'study')
        if (response.code === 200) {
          uploadedUrls.push(response.data.url || response.data)
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        throw new Error(`图片上传失败：${file.name}`)
      }
    } else if (file.url) {
      // 如果已经有URL，直接使用
      uploadedUrls.push(file.url)
    }
  }
  
  return uploadedUrls
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    // 上传图片
    submitting.value = true
    let imageUrls = []
    
    if (imageList.value.length > 0) {
      try {
        imageUrls = await uploadImages()
      } catch (error) {
        ElMessage.error(error.message || '图片上传失败')
        submitting.value = false
        return
      }
    }
    
    // 准备提交数据
    const submitData = {
      category: form.category,
      title: form.title,
      description: form.description,
      images: imageUrls.length > 0 ? imageUrls : null
    }
    
    // 提交到后端
    const response = await questionApi.publish(submitData)
    
    if (response.code === 200) {
      ElMessage.success('发布成功！审核通过后将显示在学习互助列表中')
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push(`/study/detail/${response.data}`)
      }, 1000)
    }
  } catch (error) {
    console.error('发布失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('发布失败，请检查表单信息')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有填写的内容将被清空。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formRef.value?.resetFields()
    imageList.value = []
  }).catch(() => {})
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.publish-content {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.form-item-block {
  margin-bottom: 24px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>

