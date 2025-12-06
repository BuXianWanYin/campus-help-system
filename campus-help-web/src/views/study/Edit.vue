<template>
  <div class="edit-container">
    <div class="edit-header">
      <h1 class="page-title">编辑问题</h1>
      <el-button type="text" @click="goBack">返回</el-button>
    </div>

    <div class="edit-content" v-loading="loading">
      <!-- 如果是被拒绝状态，显示提示信息 -->
      <el-alert
        v-if="originalStatus === 'REJECTED'"
        title="您的问题已被拒绝，修改后将重新提交审核"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 24px;"
      >
        <template #default>
          <div>
            <p>拒绝原因：{{ questionDetail?.question?.auditReason || '无' }}</p>
            <p style="margin-top: 8px; margin-bottom: 0;">修改问题内容后，将重新提交审核，请确保内容符合规范。</p>
          </div>
        </template>
      </el-alert>

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

        <!-- 悬赏金额 -->
        <el-form-item label="悬赏金额（元）" prop="reward" class="form-item-block">
          <el-input-number
            v-model="form.reward"
            :min="0"
            :max="10000"
            :precision="2"
            placeholder="选填，如愿意支付悬赏可填写金额"
            style="width: 200px"
          />
          <div class="form-tip">选填，范围为0-10000元。设置悬赏可以吸引更多人回答您的问题</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-item-block">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            {{ originalStatus === 'REJECTED' ? '重新提交审核' : '保存修改' }}
          </el-button>
          <el-button size="large" @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="预览图片" width="800px">
      <img :src="previewImageUrl" style="width: 100%" alt="预览图片" />
    </el-dialog>
  </div>
</template>

/**
 * 编辑学习问题页面
 * 编辑已发布的学习问题
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { questionApi, fileApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const questionId = ref(null)
const originalStatus = ref(null)
const questionDetail = ref(null)
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 编辑表单数据
const form = reactive({
  category: '',
  title: '',
  description: '',
  reward: null
})

const imageList = ref([])

const rules = {
  category: [
    { required: true, message: '请选择问题分类', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在5到200个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度在10到2000个字符之间', trigger: 'blur' }
  ],
  reward: [
    { type: 'number', min: 0, max: 10000, message: '悬赏金额范围为0-10000元', trigger: 'blur' }
  ]
}

/**
 * 加载问题详情
 */
const loadQuestionDetail = async () => {
  loading.value = true
  try {
    const id = parseInt(route.params.id)
    questionId.value = id
    
    const response = await questionApi.getDetail(id)
    if (response.code === 200) {
      const data = response.data
      questionDetail.value = data
      
      // 保存原始状态，用于判断是否为被拒绝状态
      originalStatus.value = data.question?.status || null
      
      // 填充表单
      form.category = data.question?.category || ''
      form.title = data.question?.title || ''
      form.description = data.question?.description || ''
      form.reward = data.question?.reward || null
      
      // 加载图片
      if (data.question?.images) {
        try {
          const images = typeof data.question.images === 'string' ? JSON.parse(data.question.images) : data.question.images
          if (Array.isArray(images) && images.length > 0) {
            imageList.value = images.map((url, index) => ({
              uid: `existing-${index}`,
              name: `image-${index}.jpg`,
              url: getAvatarUrl(url),
              status: 'success'
            }))
          }
        } catch (e) {
          console.error('解析图片失败:', e)
        }
      }
    }
  } catch (error) {
    console.error('加载问题详情失败:', error)
    ElMessage.error('加载问题详情失败')
    router.back()
  } finally {
    loading.value = false
  }
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
      // 新上传的图片
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
        // 已有的图片，提取URL
        // 如果是完整URL，提取相对路径；如果是相对路径，直接使用
        let url = file.url
        if (url.startsWith('http://') || url.startsWith('https://')) {
          // 从完整URL中提取相对路径
          const match = url.match(/\/uploads\/.+$/)
          if (match) {
            url = match[0]
          }
        } else if (!url.startsWith('/uploads/')) {
          // 确保是相对路径
          url = url.startsWith('/') ? url : `/uploads/${url}`
        }
        uploadedUrls.push(url)
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
      images: imageUrls.length > 0 ? imageUrls : null,
      reward: form.reward && form.reward > 0 ? form.reward : null
    }
    
    // 提交到后端
    const response = await questionApi.update(questionId.value, submitData)
    
    if (response.code === 200) {
      const message = originalStatus.value === 'REJECTED' ? '重新提交成功，等待审核！' : '编辑成功！'
      ElMessage.success(message)
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/user/posts')
      }, 1500)
    }
  } catch (error) {
    console.error('编辑失败:', error)
    ElMessage.error(error.response?.data?.message || '编辑失败，请检查表单信息')
  } finally {
    submitting.value = false
  }
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadQuestionDetail()
})
</script>

<style scoped>
.edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.edit-header {
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

.edit-content {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.form-item-block {
  margin-bottom: 24px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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

@media (max-width: 768px) {
  .edit-container {
    padding: 16px;
  }
  
  .edit-content {
    padding: 20px;
  }
}
</style>

