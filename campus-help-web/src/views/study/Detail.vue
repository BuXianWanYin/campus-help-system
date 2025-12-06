<template>
  <div class="detail-container" v-loading="loading">
    <el-empty v-if="!loading && !questionDetail" description="问题不存在" />
    
    <div v-if="questionDetail && questionDetail.question" class="detail-content">
      <!-- 返回按钮 -->
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      
      <!-- 问题详情卡片 -->
      <div class="question-card">
        <div class="question-header">
          <div class="question-tags">
            <el-tag :type="getCategoryTagType(questionDetail.question.category)" class="category-tag">
              {{ getCategoryName(questionDetail.question.category) }}
            </el-tag>
            <el-tag :type="getStatusTagType(questionDetail.question.status)" size="small">
              {{ getStatusName(questionDetail.question.status) }}
            </el-tag>
          </div>
          <h1 class="question-title">{{ questionDetail.question.title }}</h1>
        </div>
        
        <div class="question-meta">
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ questionDetail.question.viewCount || 0 }} 浏览</span>
          </div>
          <div class="meta-item">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ questionDetail.question.answerCount || 0 }} 回答</span>
          </div>
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>发布于 {{ formatDateTime(questionDetail.question.createTime) }}</span>
          </div>
        </div>
        
        <!-- 问题图片 -->
        <div v-if="questionImages.length > 0" class="question-images">
          <el-image
            v-for="(img, index) in questionImages"
            :key="index"
            :src="getAvatarUrl(img)"
            :preview-src-list="questionImages.map(i => getAvatarUrl(i))"
            fit="cover"
            class="question-image"
            preview-teleported
          />
        </div>
        
        <div class="question-description">
          <h3>问题描述</h3>
          <div class="description-content" v-html="formatContent(questionDetail.question.description)"></div>
        </div>
        
        <div v-if="questionDetail.question.reward && questionDetail.question.reward > 0" class="reward-section">
          <el-icon><Money /></el-icon>
          <span class="reward-text">悬赏金额：¥{{ questionDetail.question.reward }}</span>
        </div>
        
        <!-- 发布者信息 -->
        <div class="publisher-section">
          <div class="publisher-info">
            <el-avatar :size="48" :src="getAvatarUrl(questionDetail.question.user?.avatar)">
              {{ questionDetail.question.user?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="publisher-details">
              <div class="publisher-name">{{ questionDetail.question.user?.nickname || '未知用户' }}</div>
              <div class="publish-time">发布于 {{ formatDateTime(questionDetail.question.createTime) }}</div>
            </div>
          </div>
          <el-button 
            v-if="!isPublisher && questionDetail.question.status !== 'SOLVED' && questionDetail.question.status !== 'CANCELLED'"
            type="primary" 
            @click="showAnswerDialog = true"
          >
            回答
          </el-button>
          <el-button 
            v-if="isPublisher && questionDetail.question.status !== 'SOLVED' && questionDetail.question.status !== 'CANCELLED'"
            type="warning" 
            @click="handleCancelQuestion"
          >
            取消问题
          </el-button>
        </div>
      </div>
      
      <!-- 回答列表 -->
      <div class="answers-section">
        <div class="answers-header">
          <h2>回答（{{ answers.length }}）</h2>
        </div>
        
        <div v-if="answers.length === 0" class="no-answers">
          <el-empty description="暂无回答，快来回答吧！" />
        </div>
        
        <div v-for="answer in answers" :key="answer.id" class="answer-item" :class="{ 'accepted': answer.isAccepted }">
          <div class="answer-header">
            <div class="answer-user">
              <el-avatar :size="40" :src="getAvatarUrl(answer.user?.avatar)">
                {{ answer.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="answer-user-info">
                <div class="answer-user-name">{{ answer.user?.nickname || '未知用户' }}</div>
                <div class="answer-time">{{ formatDateTime(answer.createTime) }}</div>
              </div>
            </div>
            <div class="answer-badges">
              <el-tag v-if="answer.isAccepted" type="success" size="small">
                <el-icon><Check /></el-icon>
                已采纳
              </el-tag>
            </div>
          </div>
          
          <div class="answer-content">
            <div class="answer-text" v-html="formatContent(answer.content)"></div>
            
            <!-- 回答图片 -->
            <div v-if="answer.images && parseImages(answer.images).length > 0" class="answer-images">
              <el-image
                v-for="(img, index) in parseImages(answer.images)"
                :key="index"
                :src="getAvatarUrl(img)"
                :preview-src-list="parseImages(answer.images).map(i => getAvatarUrl(i))"
                fit="cover"
                class="answer-image"
                preview-teleported
              />
            </div>
          </div>
          
          <div class="answer-actions">
            <el-button 
              v-if="isPublisher && !answer.isAccepted && questionDetail.question.status !== 'SOLVED'"
              type="success" 
              size="small"
              @click="handleAcceptAnswer(answer.id)"
            >
              采纳答案
            </el-button>
            <el-button 
              v-if="isPublisher && answer.isAccepted"
              type="info" 
              size="small"
              disabled
            >
              已采纳
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 回答对话框 -->
      <el-dialog
        v-model="showAnswerDialog"
        title="回答问题"
        width="800px"
        :before-close="handleCloseAnswerDialog"
      >
        <el-form ref="answerFormRef" :model="answerForm" :rules="answerRules" label-width="80px">
          <el-form-item label="回答内容" prop="content">
            <el-input
              v-model="answerForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入您的回答，详细解答问题..."
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="回答图片" prop="images">
            <el-upload
              v-model:file-list="answerImageList"
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
            <div class="form-tip">最多上传3张图片</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="handleCloseAnswerDialog">取消</el-button>
          <el-button type="primary" @click="handleSubmitAnswer" :loading="answering">
            提交回答
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, View, ChatLineRound, Clock, Money, Check, Plus } from '@element-plus/icons-vue'
import { questionApi, fileApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const questionDetail = ref(null)
const questionId = computed(() => Number(route.params.id))
const isPublisher = computed(() => {
  if (!questionDetail.value?.question || !userStore.userInfo) return false
  return questionDetail.value.question.userId === userStore.userInfo.id
})

const answers = computed(() => {
  return questionDetail.value?.answers || []
})

const questionImages = computed(() => {
  if (!questionDetail.value?.question?.images) return []
  return parseImages(questionDetail.value.question.images)
})

const showAnswerDialog = ref(false)
const answering = ref(false)
const answerFormRef = ref(null)
const answerForm = ref({
  content: '',
  images: []
})
const answerImageList = ref([])

const answerRules = {
  content: [
    { required: true, message: '请输入回答内容', trigger: 'blur' },
    { min: 1, max: 2000, message: '回答内容长度在1到2000个字符之间', trigger: 'blur' }
  ]
}

/**
 * 获取问题详情
 */
const fetchQuestionDetail = async () => {
  loading.value = true
  try {
    const response = await questionApi.getDetail(questionId.value)
    if (response.code === 200) {
      questionDetail.value = response.data
    } else {
      ElMessage.error(response.message || '获取问题详情失败')
    }
  } catch (error) {
    ElMessage.error('获取问题详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 解析图片JSON
 */
const parseImages = (imagesJson) => {
  if (!imagesJson) return []
  try {
    return typeof imagesJson === 'string' ? JSON.parse(imagesJson) : imagesJson
  } catch {
    return []
  }
}

/**
 * 格式化时间
 */
const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 格式化内容（换行处理）
 */
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

/**
 * 获取分类名称
 */
const getCategoryName = (category) => {
  const categoryMap = {
    'MATH': '数学',
    'PHYSICS': '物理',
    'CHEMISTRY': '化学',
    'BIOLOGY': '生物',
    'COMPUTER': '计算机',
    'ENGLISH': '英语',
    'LITERATURE': '文学',
    'HISTORY': '历史',
    'PHILOSOPHY': '哲学',
    'ECONOMICS': '经济',
    'MANAGEMENT': '管理',
    'LAW': '法律',
    'EDUCATION': '教育',
    'ART': '艺术',
    'ENGINEERING': '工程',
    'MEDICINE': '医学',
    'AGRICULTURE': '农学',
    'OTHER': '其他'
  }
  return categoryMap[category] || category || '其他'
}

/**
 * 获取分类标签类型
 */
const getCategoryTagType = (category) => {
  const typeMap = {
    'MATH': 'warning',
    'PHYSICS': 'success',
    'CHEMISTRY': 'danger',
    'BIOLOGY': 'success',
    'COMPUTER': 'primary',
    'ENGLISH': 'info',
    'LITERATURE': 'warning',
    'HISTORY': 'info',
    'PHILOSOPHY': 'info',
    'ECONOMICS': 'success',
    'MANAGEMENT': 'primary',
    'LAW': 'danger',
    'EDUCATION': 'warning',
    'ART': 'success',
    'ENGINEERING': 'primary',
    'MEDICINE': 'danger',
    'AGRICULTURE': 'success',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

/**
 * 获取状态名称
 */
const getStatusName = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'PENDING_ANSWER': '待解答',
    'ANSWERED': '已回答',
    'SOLVED': '已解决',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝',
    'ADMIN_OFFSHELF': '已下架'
  }
  return statusMap[status] || status || '未知'
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status) => {
  const typeMap = {
    'PENDING_REVIEW': 'info',
    'PENDING_ANSWER': 'warning',
    'ANSWERED': 'primary',
    'SOLVED': 'success',
    'CANCELLED': 'info',
    'REJECTED': 'danger',
    'ADMIN_OFFSHELF': 'danger'
  }
  return typeMap[status] || ''
}

/**
 * 提交回答
 */
const handleSubmitAnswer = async () => {
  if (!answerFormRef.value) return
  
  await answerFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 上传图片
    const imageUrls = []
    if (answerImageList.value.length > 0) {
      try {
        for (const file of answerImageList.value) {
          if (file.raw) {
            const uploadResponse = await fileApi.upload(file.raw, 'study')
            if (uploadResponse.code === 200) {
              imageUrls.push(uploadResponse.data.url || uploadResponse.data)
            }
          } else if (file.url) {
            imageUrls.push(file.url)
          }
        }
      } catch (error) {
        ElMessage.error('图片上传失败')
        return
      }
    }
    
    answering.value = true
    try {
      const response = await questionApi.answer(questionId.value, {
        content: answerForm.content,
        images: imageUrls
      })
      
      if (response.code === 200) {
        ElMessage.success('回答成功')
        showAnswerDialog.value = false
        handleCloseAnswerDialog()
        fetchQuestionDetail()
      } else {
        ElMessage.error(response.message || '回答失败')
      }
    } catch (error) {
      ElMessage.error('回答失败')
    } finally {
      answering.value = false
    }
  })
}

/**
 * 采纳答案
 */
const handleAcceptAnswer = async (answerId) => {
  try {
    await ElMessageBox.confirm('确定要采纳这个答案吗？采纳后将无法更改。', '确认采纳', {
      type: 'warning'
    })
    
    const response = await questionApi.acceptAnswer(questionId.value, {
      answerId: answerId
    })
    
    if (response.code === 200) {
      ElMessage.success('采纳成功')
      fetchQuestionDetail()
    } else {
      ElMessage.error(response.message || '采纳失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('采纳失败')
    }
  }
}

/**
 * 取消问题
 */
const handleCancelQuestion = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个问题吗？', '确认取消', {
      type: 'warning'
    })
    
    const response = await questionApi.cancel(questionId.value)
    
    if (response.code === 200) {
      ElMessage.success('取消成功')
      fetchQuestionDetail()
    } else {
      ElMessage.error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

/**
 * 关闭回答对话框
 */
const handleCloseAnswerDialog = () => {
  showAnswerDialog.value = false
  answerForm.value = {
    content: '',
    images: []
  }
  answerImageList.value = []
  if (answerFormRef.value) {
    answerFormRef.value.resetFields()
  }
}

/**
 * 图片上传相关
 */
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return false // 阻止自动上传
}

const handlePreview = (file) => {
  // 预览功能由 el-image 的 preview-src-list 提供
}

const handleRemove = () => {
  // 移除文件
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传3张图片')
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchQuestionDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 200px);
}

.back-btn {
  margin-bottom: 20px;
  font-size: 14px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.question-header {
  margin-bottom: 16px;
}

.question-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.question-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  line-height: 1.5;
}

.question-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F0F0F0;
  font-size: 14px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.question-image {
  width: 200px;
  height: 200px;
  border-radius: 4px;
  border: 1px solid #E0E0E0;
  cursor: pointer;
}

.question-description {
  margin-bottom: 20px;
}

.question-description h3 {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.description-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.reward-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #FDF6EC;
  border-radius: 4px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
  color: #E6A23C;
}

.publisher-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #F0F0F0;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publisher-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.publish-time {
  font-size: 14px;
  color: #909399;
}

.answers-section {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.answers-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F0F0F0;
}

.answers-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.no-answers {
  padding: 40px 0;
}

.answer-item {
  padding: 20px 0;
  border-bottom: 1px solid #F0F0F0;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-item.accepted {
  background-color: #F0F9FF;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #B3D8FF;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.answer-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.answer-user-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.answer-time {
  font-size: 13px;
  color: #909399;
}

.answer-content {
  margin-bottom: 12px;
}

.answer-text {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.answer-image {
  width: 150px;
  height: 150px;
  border-radius: 4px;
  border: 1px solid #E0E0E0;
  cursor: pointer;
}

.answer-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .question-title {
    font-size: 20px;
  }
  
  .question-image {
    width: 100%;
    height: auto;
  }
  
  .answer-image {
    width: 100%;
    height: auto;
  }
}
</style>

