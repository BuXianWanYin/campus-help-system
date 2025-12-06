<template>
  <div class="edit-container">
    <div class="edit-header">
      <h1 class="page-title">编辑失物信息</h1>
      <el-button type="text" @click="goBack">返回</el-button>
    </div>

    <div class="edit-content" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="top"
      >
        <!-- 类型选择 -->
        <el-form-item label="类型" prop="type" class="form-item-block">
          <div class="type-select-wrapper">
            <el-radio-group v-model="form.type">
              <el-radio label="LOST">失物</el-radio>
              <el-radio label="FOUND">招领</el-radio>
            </el-radio-group>
            <div class="form-tip type-tip">选择您是要寻找失物，还是要发布拾到的物品</div>
          </div>
        </el-form-item>

        <!-- 物品名称 -->
        <el-form-item label="物品名称" prop="title" class="form-item-block">
          <el-input
            v-model="form.title"
            placeholder="请输入物品名称，如：黑色钱包、小米手机等"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <!-- 物品分类 -->
        <el-form-item label="物品分类" prop="category" class="form-item-block">
          <el-select v-model="form.category" placeholder="请选择物品分类" style="width: 100%">
            <el-option label="证件类" value="证件类" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="书籍" value="书籍" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 物品描述 -->
        <el-form-item label="物品描述" prop="description" class="form-item-block">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述物品的特征、外观、品牌等信息，至少10个字"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 丢失时间 -->
        <el-form-item label="丢失/拾取时间" prop="lostTime" class="form-item-block">
          <el-date-picker
            v-model="form.lostTime"
            type="datetime"
            placeholder="选择丢失或拾取的时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <!-- 丢失地点 -->
        <el-form-item label="丢失/拾取地点" prop="lostLocation" class="form-item-block">
          <el-input
            v-model="form.lostLocation"
            placeholder="请输入具体地点，如：图书馆二楼、教学楼A302等"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <!-- 联系方式 -->
        <el-form-item label="联系方式" prop="contactInfo" class="form-item-block">
          <el-input
            v-model="form.contactInfo"
            placeholder="选填，如需公开联系方式请填写（手机号、QQ等），否则将通过站内私信联系"
            maxlength="50"
          />
          <div class="form-tip">不填写则默认通过站内私信联系</div>
        </el-form-item>

        <!-- 物品图片 -->
        <el-form-item label="物品图片" prop="images" class="form-item-block">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="9"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            accept="image/*"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">最多上传9张图片，支持jpg、png格式，单张图片不超过5MB</div>
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
          <div class="form-tip">选填，范围为0-10000元</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-item-block">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            {{ submitButtonText }}
          </el-button>
          <el-button size="large" @click="goBack">取消</el-button>
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
 * 编辑失物信息页面
 * 编辑已发布的失物招领信息
 */

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { lostFoundApi, fileApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')
const imageList = ref([])
const lostFoundId = ref(null)
// 保存原始状态，用于判断是否为被拒绝状态
const originalStatus = ref(null)

// 计算按钮文本
const submitButtonText = computed(() => {
  return originalStatus.value === 'REJECTED' ? '重新发布' : '保存修改'
})

const form = reactive({
  type: 'LOST',
  title: '',
  category: '',
  description: '',
  lostTime: null,
  lostLocation: '',
  contactInfo: '',
  reward: null
})

const rules = {
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在1到50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物品分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入物品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述至少10个字，最多500个字', trigger: 'blur' }
  ],
  lostTime: [
    { required: true, message: '请选择丢失/拾取时间', trigger: 'change' }
  ],
  lostLocation: [
    { required: true, message: '请输入丢失/拾取地点', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在1到100个字符', trigger: 'blur' }
  ]
}

/**
 * 加载失物详情
 */
const loadLostFoundDetail = async () => {
  loading.value = true
  try {
    const id = parseInt(route.params.id)
    lostFoundId.value = id
    
    const response = await lostFoundApi.getDetail(id)
    if (response.code === 200) {
      const data = response.data
      
      // 保存原始状态，用于判断是否为被拒绝状态
      originalStatus.value = data.status || null
      
      // 填充表单
      form.type = data.type || 'LOST'
      form.title = data.title || ''
      form.category = data.category || ''
      form.description = data.description || ''
      // 处理时间格式：如果后端返回的时间包含T（ISO格式），则转换为空格格式
      if (data.lostTime) {
        form.lostTime = data.lostTime.replace('T', ' ')
      } else {
        form.lostTime = null
      }
      form.lostLocation = data.lostLocation || ''
      form.contactInfo = data.contactInfo || ''
      form.reward = data.reward || null
      
      // 加载图片
      if (data.images) {
        try {
          const images = typeof data.images === 'string' ? JSON.parse(data.images) : data.images
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
    console.error('加载失物详情失败:', error)
    ElMessage.error('加载失物详情失败')
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
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
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
  ElMessage.warning('最多只能上传9张图片！')
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
        const response = await fileApi.upload(file.raw, 'lost-found')
        if (response.code === 200) {
          uploadedUrls.push(response.data.url || response.data)
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        throw new Error(`图片上传失败：${file.name}`)
      }
    } else if (file.url) {
      // 已有的图片，提取URL
      const url = file.url.replace(/^.*\/uploads\//, '/uploads/')
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
      type: form.type,
      title: form.title,
      category: form.category,
      description: form.description,
      lostTime: form.lostTime,
      lostLocation: form.lostLocation,
      contactInfo: form.contactInfo || null,
      images: imageUrls.length > 0 ? imageUrls : null,
      reward: form.reward && form.reward > 0 ? form.reward : null
    }
    
    // 提交到后端
    const response = await lostFoundApi.update(lostFoundId.value, submitData)
    
    if (response.code === 200) {
      const message = originalStatus.value === 'REJECTED' ? '重新发布成功，等待审核！' : '编辑成功！'
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
  loadLostFoundDetail()
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

.type-select-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-tip {
  margin-top: 0;
  padding-left: 0;
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

