<template>
  <div class="publish-container">
    <div class="publish-header">
      <h1 class="page-title">发布商品</h1>
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
        <!-- 商品标题 -->
        <el-form-item label="商品标题" prop="title" class="form-item-block">
          <el-input
            v-model="form.title"
            placeholder="请输入商品标题，如：二手iPhone 12、高等数学教材等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 商品分类 -->
        <el-form-item label="商品分类" prop="category" class="form-item-block">
          <el-select v-model="form.category" placeholder="请选择商品分类" style="width: 100%">
            <el-option label="数码产品" value="数码产品" />
            <el-option label="图书教材" value="图书教材" />
            <el-option label="服装鞋包" value="服装鞋包" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="运动健身" value="运动健身" />
            <el-option label="乐器" value="乐器" />
            <el-option label="文创用品" value="文创用品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 商品描述 -->
        <el-form-item label="商品描述" prop="description" class="form-item-block">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述商品的品牌、型号、使用情况等信息，至少10个字"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 售价 -->
        <el-form-item label="售价（元）" prop="price" class="form-item-block">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            placeholder="请输入售价"
            style="width: 200px"
          />
          <div class="form-tip">售价必须大于0</div>
        </el-form-item>

        <!-- 商品成色 -->
        <el-form-item label="商品成色" prop="condition" class="form-item-block">
          <el-radio-group v-model="form.condition">
            <el-radio label="全新">全新</el-radio>
            <el-radio label="几乎全新">几乎全新</el-radio>
            <el-radio label="轻微使用痕迹">轻微使用痕迹</el-radio>
            <el-radio label="明显使用痕迹">明显使用痕迹</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 库存数量 -->
        <el-form-item label="库存数量" prop="stock" class="form-item-block">
          <el-input-number
            v-model="form.stock"
            :min="1"
            :precision="0"
            placeholder="请输入库存数量"
            style="width: 200px"
          />
          <div class="form-tip">库存数量至少1件</div>
        </el-form-item>

        <!-- 商品图片 -->
        <el-form-item label="商品图片" prop="images" class="form-item-block">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="9"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :on-change="handleImageChange"
            accept="image/*"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">至少上传1张，最多9张图片，支持jpg、png格式，单张图片不超过5MB</div>
        </el-form-item>

        <!-- 交易方式 -->
        <el-form-item label="交易方式" prop="tradeMethod" class="form-item-block">
          <el-radio-group v-model="form.tradeMethod" @change="handleTradeMethodChange">
            <el-radio label="MAIL">邮寄</el-radio>
            <el-radio label="FACE_TO_FACE">自提</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 邮寄费用（邮寄时） -->
        <el-form-item 
          v-if="form.tradeMethod === 'MAIL'" 
          label="邮寄费用（元）" 
          prop="shippingFee" 
          class="form-item-block"
        >
          <el-input-number
            v-model="form.shippingFee"
            :min="0"
            :precision="2"
            placeholder="选填，默认0元"
            style="width: 200px"
          />
          <div class="form-tip">选填，默认0元</div>
        </el-form-item>

        <!-- 自提地点（自提时） -->
        <el-form-item 
          v-if="form.tradeMethod === 'FACE_TO_FACE'" 
          label="自提地点" 
          prop="tradeLocation" 
          class="form-item-block"
        >
          <el-input
            v-model="form.tradeLocation"
            placeholder="请输入自提地点，如：图书馆门口、教学楼A302等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="form-item-block">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            发布
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
 * 发布商品页面
 * 用户发布闲置商品信息
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { goodsApi, fileApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')
const imageList = ref([])

// 发布表单数据
const form = reactive({
  title: '',
  category: '',
  description: '',
  price: null,
  condition: '',
  stock: 1,
  images: [],
  tradeMethod: 'MAIL',
  shippingFee: 0,
  tradeLocation: ''
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在1到200个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述至少10个字，最多500个字', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '售价必须大于0', trigger: 'blur' }
  ],
  condition: [
    { required: true, message: '请选择商品成色', trigger: 'change' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '库存数量至少1件', trigger: 'blur' }
  ],
  images: [
    { 
      validator: (rule, value, callback) => {
        if (imageList.value.length === 0) {
          callback(new Error('请至少上传1张商品图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  tradeMethod: [
    { required: true, message: '请选择交易方式', trigger: 'change' }
  ],
  tradeLocation: [
    { 
      validator: (rule, value, callback) => {
        if (form.tradeMethod === 'FACE_TO_FACE' && !value) {
          callback(new Error('自提方式必须填写自提地点'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

/**
 * 检查实名认证
 */
onMounted(() => {
  if (!userStore.userInfo?.isVerified) {
    ElMessage.warning('发布商品需要实名认证，请先完成实名认证')
    router.push('/user/verification')
  }
})

/**
 * 处理交易方式变化
 */
const handleTradeMethodChange = () => {
  if (form.tradeMethod === 'MAIL') {
    form.tradeLocation = ''
  } else {
    form.shippingFee = 0
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
  return true
}

/**
 * 处理图片预览
 */
const handlePreview = (file) => {
  previewImageUrl.value = file.url || URL.createObjectURL(file.raw)
  previewVisible.value = true
}

/**
 * 处理图片变化（添加或移除）
 */
const handleImageChange = (file, fileList) => {
  // 同步更新 imageList
  imageList.value = fileList
  // 更新 form.images 以触发验证
  updateImagesField()
  // 手动触发验证
  if (formRef.value) {
    formRef.value.validateField('images')
  }
}

/**
 * 处理图片移除
 */
const handleRemove = () => {
  updateImagesField()
  // 手动触发验证
  if (formRef.value) {
    formRef.value.validateField('images')
  }
}

/**
 * 处理超出限制
 */
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

/**
 * 更新图片字段
 */
const updateImagesField = () => {
  form.images = imageList.value.map(item => item.url || item.response?.data?.url).filter(Boolean)
}

/**
 * 处理提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 检查实名认证
    if (!userStore.userInfo?.isVerified) {
      ElMessage.warning('发布商品需要实名认证，请先完成实名认证')
      router.push('/user/verification')
      return
    }
    
    // 检查图片
    if (imageList.value.length === 0) {
      ElMessage.warning('请至少上传1张商品图片')
      return
    }
    
    submitting.value = true
    
    // 上传图片
    const imageUrls = []
    for (const fileItem of imageList.value) {
      if (fileItem.raw) {
        // 直接传入文件对象和模块名称
        const uploadResponse = await fileApi.upload(fileItem.raw, 'goods')
        if (uploadResponse.code === 200) {
          imageUrls.push(uploadResponse.data)
        }
      } else if (fileItem.url) {
        imageUrls.push(fileItem.url)
      }
    }
    
    if (imageUrls.length === 0) {
      ElMessage.error('图片上传失败，请重试')
      submitting.value = false
      return
    }
    
    // 提交表单
    const submitData = {
      ...form,
      images: imageUrls
    }
    
    const response = await goodsApi.publish(submitData)
    if (response.code === 200) {
      ElMessage.success('发布成功')
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push(`/goods/detail/${response.data}`)
      }, 1000)
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 处理重置
 */
const handleReset = () => {
  formRef.value?.resetFields()
  imageList.value = []
  form.shippingFee = 0
  form.stock = 1
  form.tradeMethod = 'MAIL'
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
  margin-top: 4px;
}
</style>

