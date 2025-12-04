<template>
  <div class="detail-container" v-loading="loading">
    <el-empty v-if="!loading && !lostFound" description="失物信息不存在" />
    
    <div v-if="lostFound" class="detail-content">
      <!-- 返回按钮 -->
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      
      <!-- 失物详情卡片 -->
      <div class="detail-card">
        <!-- 图片区域 -->
        <div class="image-section">
          <el-image
            v-if="mainImage"
            :src="mainImage"
            :preview-src-list="imageList"
            fit="cover"
            class="main-image"
            preview-teleported
          />
          <div v-else class="no-image">暂无图片</div>
          
          <!-- 图片缩略图 -->
          <div v-if="imageList.length > 1" class="image-thumbs">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="thumb-item"
              :class="{ active: mainImage === img }"
              @click="mainImage = img"
            >
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>
        
        <!-- 信息区域 -->
        <div class="info-section">
          <div class="detail-header">
            <span class="type-badge" :class="lostFound.type === 'LOST' ? 'badge-red' : 'badge-green'">
              {{ lostFound.type === 'LOST' ? '失物' : '招领' }}
            </span>
            <h1 class="detail-title">{{ lostFound.title }}</h1>
          </div>
          
          <div class="detail-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>地点：{{ lostFound.lostLocation }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>时间：{{ formatDateTime(lostFound.lostTime) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Folder /></el-icon>
              <span>分类：{{ lostFound.category }}</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span>浏览：{{ lostFound.viewCount || 0 }}次</span>
            </div>
          </div>
          
          <div class="detail-description">
            <h3>物品描述</h3>
            <p>{{ lostFound.description }}</p>
          </div>
          
          <div v-if="lostFound.reward > 0" class="reward-section">
            <span class="reward-label">悬赏金额：</span>
            <span class="reward-value">¥{{ lostFound.reward }}</span>
          </div>
          
          <!-- 联系方式 -->
          <div v-if="lostFound.contactInfo" class="contact-section">
            <div class="contact-label">
              <span>联系方式：</span>
            </div>
            <div class="contact-value">{{ lostFound.contactInfo }}</div>
          </div>
          
          <!-- 发布者信息 -->
          <div class="publisher-section">
            <div class="publisher-info">
              <el-avatar :size="48" :src="getAvatarUrl(lostFound.userAvatar)">
                {{ lostFound.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="publisher-details">
                <div class="publisher-name">{{ lostFound.userName || '未知用户' }}</div>
                <div class="publish-time">发布于 {{ formatDateTime(lostFound.createTime) }}</div>
              </div>
            </div>
            <el-button v-if="!isPublisher" type="primary" @click="handleContact">联系TA</el-button>
          </div>
          
          <!-- 认领按钮（仅非发布者可见，且未提交申请） -->
          <div v-if="!isPublisher && !myClaimRecord && (lostFound.status === 'PENDING_CLAIM' || lostFound.status === 'CLAIMING')" class="action-section">
            <el-button type="primary" size="large" @click="showClaimDialog = true">
              {{ lostFound.type === 'LOST' ? '我捡到了' : '这是我的' }}
            </el-button>
            <div class="action-tip" v-if="lostFound.type === 'LOST'">
              如果您捡到了这个物品或能提供相关线索，请点击按钮提交申请
            </div>
          </div>
          
          <!-- 我的申请（仅非发布者可见，且已提交申请） -->
          <div v-if="!isPublisher" class="my-claim-section" v-loading="myClaimLoading">
            <div v-if="myClaimRecord" class="claim-records-section">
              <h3 class="section-title">我的申请</h3>
              <div class="claim-record-item">
                <div class="claim-header">
                  <div class="claim-user-info">
                    <el-avatar :size="32" :src="getAvatarUrl(myClaimRecord.user?.avatar)">
                      {{ myClaimRecord.user?.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="claim-user-details">
                      <div class="claim-user-name">{{ myClaimRecord.user?.nickname || '我' }}</div>
                      <div class="claim-time">{{ formatDateTime(myClaimRecord.createTime) }}</div>
                    </div>
                  </div>
                  <div class="claim-status">
                    <el-tag v-if="myClaimRecord.status === 'PENDING'" type="warning">待处理</el-tag>
                    <el-tag v-else-if="myClaimRecord.status === 'CONFIRMED'" type="success">已确认</el-tag>
                    <el-tag v-else-if="myClaimRecord.status === 'REJECTED'" type="danger">已拒绝</el-tag>
                  </div>
                </div>
                <div class="claim-content">
                  <div class="claim-desc">
                    <strong>{{ lostFound.type === 'LOST' ? '拾取信息：' : '物品特征：' }}</strong>{{ myClaimRecord.description }}
                  </div>
                  <div v-if="myClaimRecord.lostTime" class="claim-meta">
                    <el-icon><Clock /></el-icon>
                    <span>{{ lostFound.type === 'LOST' ? '拾取时间：' : '丢失时间：' }}{{ formatDateTime(myClaimRecord.lostTime) }}</span>
                  </div>
                  <div v-if="myClaimRecord.otherInfo" class="claim-meta">
                    <span>其他信息：{{ myClaimRecord.otherInfo }}</span>
                  </div>
                  <div v-if="myClaimRecord.proofImages && parseImages(myClaimRecord.proofImages).length > 0" class="claim-proof">
                    <strong>证明文件：</strong>
                    <div class="proof-images">
                      <div
                        v-for="(img, index) in parseImages(myClaimRecord.proofImages)"
                        :key="index"
                        class="proof-image-wrapper"
                      >
                        <el-image
                          v-if="!isBlobUrl(img)"
                          :src="getAvatarUrl(img)"
                          :preview-src-list="parseImages(myClaimRecord.proofImages).filter(i => !isBlobUrl(i)).map(i => getAvatarUrl(i))"
                          fit="cover"
                          class="proof-image"
                          preview-teleported
                        >
                          <template #error>
                            <div class="image-error">图片加载失败</div>
                          </template>
                        </el-image>
                        <div v-else class="image-error">图片已失效（请重新提交）</div>
                      </div>
                    </div>
                  </div>
                  <div v-if="myClaimRecord.status === 'REJECTED' && myClaimRecord.rejectReason" class="claim-reject-reason">
                    <strong>拒绝原因：</strong>{{ myClaimRecord.rejectReason }}
                  </div>
                </div>
                <div v-if="myClaimRecord.status === 'PENDING'" class="claim-actions">
                  <el-button type="primary" size="small" @click="openEditClaimDialog">
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDeleteClaim">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 认领记录列表（仅发布者可见） -->
          <div v-if="isPublisher" class="claim-records-section">
            <h3 class="section-title">
              {{ lostFound.type === 'LOST' ? '线索提供' : '失主认领申请' }}
            </h3>
            <div class="section-tip">
              <template v-if="lostFound.type === 'LOST'">
                您丢失了物品，以下是其他用户提供的线索，请仔细核对信息后处理。
              </template>
              <template v-else>
                您拾到了物品，以下是失主申请认领该物品，请仔细核对信息后确认认领。
              </template>
            </div>
            <div v-loading="claimRecordsLoading">
              <div v-if="claimRecords.length === 0" class="empty-claims">
                <el-empty 
                  :description="lostFound.type === 'LOST' ? '暂无线索提供' : '暂无失主认领申请'" 
                  :image-size="100" 
                />
              </div>
              <div v-else class="claim-records-list">
                <div v-for="record in claimRecords" :key="record.id" class="claim-record-item">
                  <div class="claim-header">
                    <div class="claim-user-info">
                      <el-avatar :size="32" :src="getAvatarUrl(record.user?.avatar)">
                        {{ record.user?.nickname?.charAt(0) || 'U' }}
                      </el-avatar>
                      <div class="claim-user-details">
                        <div class="claim-user-name">{{ record.user?.nickname || '未知用户' }}</div>
                        <div class="claim-time">{{ formatDateTime(record.createTime) }}</div>
                      </div>
                    </div>
                    <div class="claim-status">
                      <template v-if="lostFound.type === 'LOST'">
                        <!-- 丢失物品：拾取人提供线索 -->
                        <el-tag v-if="record.status === 'PENDING'" type="warning">待确认</el-tag>
                        <el-tag v-else-if="record.status === 'CONFIRMED'" type="success">已找到</el-tag>
                        <el-tag v-else-if="record.status === 'REJECTED'" type="info">已忽略</el-tag>
                      </template>
                      <template v-else>
                        <!-- 招领物品：失主申请认领 -->
                        <el-tag v-if="record.status === 'PENDING'" type="warning">待处理</el-tag>
                        <el-tag v-else-if="record.status === 'CONFIRMED'" type="success">已确认</el-tag>
                        <el-tag v-else-if="record.status === 'REJECTED'" type="danger">已拒绝</el-tag>
                      </template>
                    </div>
                  </div>
                  <div class="claim-content">
                    <div class="claim-desc">
                      <strong>物品特征：</strong>{{ record.description }}
                    </div>
                    <div v-if="record.lostTime" class="claim-meta">
                      <el-icon><Clock /></el-icon>
                      <span>{{ lostFound.type === 'LOST' ? '拾取时间：' : '丢失时间：' }}{{ formatDateTime(record.lostTime) }}</span>
                    </div>
                    <div v-if="record.otherInfo" class="claim-meta">
                      <span>其他信息：{{ record.otherInfo }}</span>
                    </div>
                    <div v-if="record.proofImages && parseImages(record.proofImages).length > 0" class="claim-proof">
                      <strong>证明文件：</strong>
                      <div class="proof-images">
                        <div
                          v-for="(img, index) in parseImages(record.proofImages)"
                          :key="index"
                          class="proof-image-wrapper"
                        >
                          <el-image
                            v-if="!isBlobUrl(img)"
                            :src="getAvatarUrl(img)"
                            :preview-src-list="parseImages(record.proofImages).filter(i => !isBlobUrl(i)).map(i => getAvatarUrl(i))"
                            fit="cover"
                            class="proof-image"
                            preview-teleported
                          >
                            <template #error>
                              <div class="image-error">图片加载失败</div>
                            </template>
                          </el-image>
                          <div v-else class="image-error">图片已失效（请重新提交）</div>
                        </div>
                      </div>
                    </div>
                    <div v-if="record.status === 'REJECTED' && record.rejectReason" class="claim-reject-reason">
                      <strong>拒绝原因：</strong>{{ record.rejectReason }}
                    </div>
                  </div>
                  <div class="claim-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      :icon="Message"
                      @click="handleContactClaimer(record)"
                    >
                      联系TA
                    </el-button>
                    <el-button 
                      v-if="lostFound.type === 'FOUND' && record.status === 'PENDING'" 
                      type="success" 
                      size="small" 
                      @click="handleConfirmClaim(record.id)"
                    >
                      确认认领
                    </el-button>
                    <el-button 
                      v-if="lostFound.type === 'FOUND' && record.status === 'PENDING'" 
                      type="danger" 
                      size="small" 
                      @click="handleRejectClaim(record.id)"
                    >
                      拒绝
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 状态提示 -->
          <div v-if="lostFound.status === 'CLAIMED'" class="status-tip">
            <el-alert type="success" :closable="false">该失物已被认领</el-alert>
          </div>
          <div v-if="lostFound.status === 'CLOSED'" class="status-tip">
            <el-alert type="info" :closable="false">该失物已关闭</el-alert>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 认领对话框 -->
    <el-dialog
      v-model="showClaimDialog"
      :title="lostFound?.type === 'LOST' ? '我捡到了' : '申请认领'"
      width="600px"
      @close="resetClaimForm"
    >
      <el-form :model="claimForm" label-width="100px">
        <el-form-item :label="lostFound?.type === 'LOST' ? '拾取信息' : '物品特征'" required>
          <el-input
            v-model="claimForm.description"
            type="textarea"
            :rows="4"
            :placeholder="lostFound?.type === 'LOST' ? '请详细描述您在哪里捡到的、物品的状况等信息，以便发布者确认' : '请详细描述物品的特征，以便发布者确认'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="lostFound?.type === 'LOST' ? '拾取时间' : '丢失时间'">
          <el-date-picker
            v-model="claimForm.lostTime"
            type="datetime"
            :placeholder="lostFound?.type === 'LOST' ? '选择拾取时间' : '选择丢失时间'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="其他信息">
          <el-input
            v-model="claimForm.otherInfo"
            type="textarea"
            :rows="3"
            :placeholder="lostFound?.type === 'LOST' ? '其他相关信息，如联系方式、可以归还的时间和地点等' : '其他能够证明物品归属的信息'"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="lostFound?.type === 'LOST' ? '物品照片' : '证明文件'">
          <el-upload
            v-model:file-list="claimForm.proofImages"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip" v-if="lostFound?.type === 'LOST'">
            请上传您捡到的物品照片，以便发布者确认
          </div>
          <div class="form-tip" v-else>
            请上传能够证明物品归属的文件（可选）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClaimDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitClaim" :loading="claimLoading">提交</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑申请对话框 -->
    <el-dialog
      v-model="showEditClaimDialog"
      :title="lostFound?.type === 'LOST' ? '编辑申请' : '编辑认领申请'"
      width="600px"
      @close="resetEditClaimForm"
    >
      <el-form :model="editClaimForm" label-width="100px">
        <el-form-item :label="lostFound?.type === 'LOST' ? '拾取信息' : '物品特征'" required>
          <el-input
            v-model="editClaimForm.description"
            type="textarea"
            :rows="4"
            :placeholder="lostFound?.type === 'LOST' ? '请详细描述您在哪里捡到的、物品的状况等信息，以便发布者确认' : '请详细描述物品的特征，以便发布者确认'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="lostFound?.type === 'LOST' ? '拾取时间' : '丢失时间'">
          <el-date-picker
            v-model="editClaimForm.lostTime"
            type="datetime"
            :placeholder="lostFound?.type === 'LOST' ? '选择拾取时间' : '选择丢失时间'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="其他信息">
          <el-input
            v-model="editClaimForm.otherInfo"
            type="textarea"
            :rows="3"
            :placeholder="lostFound?.type === 'LOST' ? '其他相关信息，如联系方式、可以归还的时间和地点等' : '其他能够证明物品归属的信息'"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="lostFound?.type === 'LOST' ? '物品照片' : '证明文件'">
          <el-upload
            v-model:file-list="editClaimForm.proofImages"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip" v-if="lostFound?.type === 'LOST'">
            请上传您捡到的物品照片，以便发布者确认
          </div>
          <div class="form-tip" v-else>
            请上传能够证明物品归属的文件（可选）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditClaimDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEditClaim" :loading="editClaimLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Clock, Folder, View, Plus, Message } from '@element-plus/icons-vue'
import { lostFoundApi, chatApi, fileApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const lostFound = ref(null)
const showClaimDialog = ref(false)
const claimLoading = ref(false)
const claimRecords = ref([])
const claimRecordsLoading = ref(false)
const myClaimRecord = ref(null)
const myClaimLoading = ref(false)
const showEditClaimDialog = ref(false)
const editClaimLoading = ref(false)

// 判断是否为发布者
const isPublisher = computed(() => {
  if (!lostFound.value || !userStore.userInfo) return false
  return lostFound.value.userId === userStore.userInfo.id
})

const claimForm = ref({
  description: '',
  lostTime: null,
  otherInfo: '',
  proofImages: []
})

const imageList = computed(() => {
  if (!lostFound.value?.images) return []
  const images = typeof lostFound.value.images === 'string' 
    ? JSON.parse(lostFound.value.images) 
    : lostFound.value.images
  return images.map(img => getAvatarUrl(img))
})

const mainImage = ref('')

/**
 * 获取失物详情
 */
const fetchDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('失物ID不存在')
    return
  }
  
  loading.value = true
  try {
    const response = await lostFoundApi.getDetail(id)
    if (response.code === 200) {
      lostFound.value = response.data
      const images = lostFound.value.images 
        ? (typeof lostFound.value.images === 'string' ? JSON.parse(lostFound.value.images) : lostFound.value.images)
        : []
      lostFound.value.images = images
      lostFound.value.userAvatar = lostFound.value.user?.avatar
      lostFound.value.userName = lostFound.value.user?.nickname || '未知用户'
      
      // 设置主图
      if (images.length > 0) {
        mainImage.value = getAvatarUrl(images[0])
      }
      
      // 如果是发布者，获取认领记录列表
      if (lostFound.value && userStore.userInfo && lostFound.value.userId === userStore.userInfo.id) {
        fetchClaimRecords()
      }
      
      // 如果不是发布者，获取我的申请
      if (lostFound.value && userStore.userInfo && lostFound.value.userId !== userStore.userInfo.id) {
        fetchMyClaim()
      }
    } else {
      console.error('[Detail.vue] API 返回错误:', response)
      ElMessage.error(response.message || '获取失物详情失败')
    }
  } catch (error) {
    console.error('[Detail.vue] 获取失物详情失败:', error)
    console.error('[Detail.vue] 错误详情:', error.message, error.stack)
    ElMessage.error('获取失物详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取认领记录列表
 */
const fetchClaimRecords = async () => {
  if (!lostFound.value?.id) return
  
  claimRecordsLoading.value = true
  try {
    const response = await lostFoundApi.getClaimRecords(lostFound.value.id)
    if (response.code === 200) {
      claimRecords.value = response.data || []
    }
  } catch (error) {
    console.error('获取认领记录失败:', error)
    ElMessage.error('获取认领记录失败')
  } finally {
    claimRecordsLoading.value = false
  }
}

/**
 * 解析图片JSON
 */
const parseImages = (imagesJson) => {
  if (!imagesJson) return []
  try {
    if (typeof imagesJson === 'string') {
      return JSON.parse(imagesJson)
    }
    return imagesJson
  } catch {
    return []
  }
}

/**
 * 判断是否为blob URL
 */
const isBlobUrl = (url) => {
  if (!url) return false
  return url.startsWith('blob:')
}

/**
 * 格式化日期时间
 */
const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 处理联系（联系发布者）
 */
const handleContact = async () => {
  if (!lostFound.value.userId) {
    ElMessage.warning('用户信息不存在')
    return
  }
  
  try {
    // 创建或获取会话
    const response = await chatApi.createOrGetSession({
      targetUserId: lostFound.value.userId,
      relatedType: 'LOST_FOUND',
      relatedId: lostFound.value.id
    })
    
    if (response.code === 200) {
      const sessionId = response.data.sessionId
      // 跳转到聊天页面，并传递会话ID
      router.push({
        path: '/user/chat',
        query: { sessionId }
      })
    } else {
      ElMessage.error(response.message || '创建会话失败')
    }
  } catch (error) {
    console.error('联系TA失败:', error)
    ElMessage.error('联系TA失败，请稍后重试')
  }
}

/**
 * 处理联系申请者（提供线索的人）
 */
const handleContactClaimer = async (record) => {
  // 获取申请者用户ID，优先使用user.id，如果没有则使用claimerId
  const claimerId = record.user?.id || record.claimerId
  
  if (!record || !claimerId) {
    ElMessage.warning('用户信息不存在')
    return
  }
  
  try {
    // 创建或获取会话
    const response = await chatApi.createOrGetSession({
      targetUserId: claimerId,
      relatedType: 'LOST_FOUND',
      relatedId: lostFound.value.id
    })
    
    if (response.code === 200) {
      const sessionId = response.data.sessionId
      // 跳转到聊天页面，并传递会话ID
      router.push({
        path: '/user/chat',
        query: { sessionId }
      })
    } else {
      ElMessage.error(response.message || '创建会话失败')
    }
  } catch (error) {
    console.error('联系TA失败:', error)
    ElMessage.error('联系TA失败，请稍后重试')
  }
}

/**
 * 上传证明图片
 */
const uploadProofImages = async () => {
  const uploadedUrls = []
  
  for (const file of claimForm.value.proofImages) {
    if (file.raw) {
      // 需要上传的新文件
      try {
        const response = await fileApi.upload(file.raw, 'lost-found')
        if (response.code === 200) {
          uploadedUrls.push(response.data.url || response.data)
        } else {
          throw new Error(response.message || '图片上传失败')
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        throw new Error(`图片上传失败：${file.name}`)
      }
    } else if (file.url) {
      // 检查是否是服务器URL（不是blob URL）
      if (file.url.startsWith('http://') || file.url.startsWith('https://') || file.url.startsWith('/')) {
        // 已经是服务器URL，直接使用
        uploadedUrls.push(file.url)
      } else if (file.url.startsWith('blob:')) {
        // blob URL需要先上传
        ElMessage.warning(`图片 ${file.name} 需要重新上传`)
        continue
      } else {
        // 其他情况，尝试使用
        uploadedUrls.push(file.url)
      }
    } else if (file.response?.data?.url) {
      // 如果有response中的URL，使用它
      uploadedUrls.push(file.response.data.url)
    }
  }
  
  return uploadedUrls
}

/**
 * 处理提交认领
 */
const handleSubmitClaim = async () => {
  if (!claimForm.value.description.trim()) {
    const warningText = lostFound.value?.type === 'LOST' 
      ? '请填写拾取信息' 
      : '请填写物品特征描述'
    ElMessage.warning(warningText)
    return
  }
  
  claimLoading.value = true
  try {
    // 先上传图片到服务器
    let proofImageUrls = []
    if (claimForm.value.proofImages && claimForm.value.proofImages.length > 0) {
      try {
        proofImageUrls = await uploadProofImages()
      } catch (error) {
        ElMessage.error(error.message || '图片上传失败，请重试')
        claimLoading.value = false
        return
      }
    }
    
    const data = {
      lostFoundId: lostFound.value.id,
      description: claimForm.value.description,
      lostTime: claimForm.value.lostTime,
      otherInfo: claimForm.value.otherInfo,
      proofImages: proofImageUrls.length > 0 ? proofImageUrls : null
    }
    
    const response = await lostFoundApi.applyClaim(data)
    if (response.code === 200) {
      const successText = lostFound.value?.type === 'LOST' 
        ? '申请已提交，发布者会尽快与您联系' 
        : '认领申请已提交'
      ElMessage.success(successText)
      showClaimDialog.value = false
      resetClaimForm()
      // 刷新认领记录列表
      if (isPublisher.value) {
        fetchClaimRecords()
      } else {
        // 刷新我的申请
        await fetchMyClaim()
      }
    }
  } catch (error) {
    console.error('提交认领失败:', error)
    ElMessage.error('提交认领失败')
  } finally {
    claimLoading.value = false
  }
}

/**
 * 重置认领表单
 */
const resetClaimForm = () => {
  claimForm.value = {
    description: '',
    lostTime: null,
    otherInfo: '',
    proofImages: []
  }
}

/**
 * 确认认领
 */
const handleConfirmClaim = async (claimRecordId) => {
  try {
    const confirmMessage = lostFound.value.type === 'LOST' 
      ? '确认要同意该申请吗？' 
      : '确认要同意该失主认领申请吗？'
    await ElMessageBox.confirm(confirmMessage, '确认认领', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await lostFoundApi.confirmClaim(claimRecordId)
    if (response.code === 200) {
      ElMessage.success('认领已确认')
      // 重新获取详情和认领记录
      await fetchDetail()
      await fetchClaimRecords()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认认领失败:', error)
      ElMessage.error(error.message || '确认认领失败')
    }
  }
}

/**
 * 拒绝认领
 */
const handleRejectClaim = async (claimRecordId) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝认领', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入拒绝原因',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '请输入拒绝原因'
        }
        if (value.length > 200) {
          return '拒绝原因不能超过200个字符'
        }
        return true
      }
    })
    
    const response = await lostFoundApi.rejectClaim(claimRecordId, reason.trim())
    if (response.code === 200) {
      ElMessage.success('认领已拒绝')
      // 重新获取认领记录
      await fetchClaimRecords()
      // 如果失物状态需要更新，重新获取详情
      await fetchDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝认领失败:', error)
      ElMessage.error(error.message || '拒绝认领失败')
    }
  }
}

/**
 * 获取我的申请
 */
const fetchMyClaim = async () => {
  if (!lostFound.value?.id) return
  
  myClaimLoading.value = true
  try {
    const response = await lostFoundApi.getMyClaim(lostFound.value.id)
    if (response.code === 200) {
      myClaimRecord.value = response.data || null
    }
  } catch (error) {
    console.error('获取我的申请失败:', error)
    // 如果接口返回404或申请不存在，设置为null
    myClaimRecord.value = null
  } finally {
    myClaimLoading.value = false
  }
}

/**
 * 打开编辑申请对话框
 */
const openEditClaimDialog = () => {
  if (!myClaimRecord.value) return
  
  // 填充编辑表单
  editClaimForm.value = {
    description: myClaimRecord.value.description || '',
    lostTime: myClaimRecord.value.lostTime ? new Date(myClaimRecord.value.lostTime) : null,
    otherInfo: myClaimRecord.value.otherInfo || '',
    proofImages: []
  }
  
  // 处理图片
  if (myClaimRecord.value.proofImages) {
    const images = parseImages(myClaimRecord.value.proofImages)
    editClaimForm.value.proofImages = images.map((img, index) => {
      if (typeof img === 'string' && !img.startsWith('blob:')) {
        // 构建完整的图片URL用于显示
        const fullUrl = getAvatarUrl(img)
        // 保存原始路径用于提交
        return {
          uid: `existing-${index}-${Date.now()}`,
          name: `image-${index}.jpg`,
          url: fullUrl,
          originalPath: img, // 保存原始路径
          status: 'success'
        }
      }
      return null
    }).filter(Boolean)
  }
  
  showEditClaimDialog.value = true
}

/**
 * 编辑申请表单
 */
const editClaimForm = ref({
  description: '',
  lostTime: null,
  otherInfo: '',
  proofImages: []
})

/**
 * 上传编辑申请中的证明图片
 */
const uploadEditProofImages = async () => {
  const uploadedUrls = []
  
  for (const file of editClaimForm.value.proofImages) {
    if (file.raw) {
      // 需要上传的新文件
      try {
        const response = await fileApi.upload(file.raw, 'lost-found')
        if (response.code === 200) {
          uploadedUrls.push(response.data.url || response.data)
        } else {
          throw new Error(response.message || '图片上传失败')
        }
      } catch (error) {
        console.error('图片上传失败:', error)
        throw new Error(`图片上传失败：${file.name}`)
      }
    } else if (file.url) {
      // 如果文件有originalPath（从服务器加载的现有图片），直接使用原始路径
      if (file.originalPath) {
        uploadedUrls.push(file.originalPath)
      } else if (file.url.startsWith('http://') || file.url.startsWith('https://')) {
        // 完整的HTTP URL，提取路径部分
        try {
          const urlObj = new URL(file.url)
          uploadedUrls.push(urlObj.pathname + (urlObj.search || ''))
        } catch {
          // 解析失败，尝试直接使用
          uploadedUrls.push(file.url)
        }
      } else if (file.url.startsWith('/')) {
        // 已经是相对路径，直接使用
        uploadedUrls.push(file.url)
      } else if (file.url.startsWith('blob:')) {
        // blob URL需要先上传
        ElMessage.warning(`图片 ${file.name} 需要重新上传`)
        continue
      } else {
        // 其他情况，尝试使用
        uploadedUrls.push(file.url)
      }
    }
  }
  
  return uploadedUrls
}

/**
 * 提交编辑申请
 */
const handleSubmitEditClaim = async () => {
  if (!editClaimForm.value.description.trim()) {
    const warningText = lostFound.value?.type === 'LOST' 
      ? '请填写拾取信息' 
      : '请填写物品特征描述'
    ElMessage.warning(warningText)
    return
  }
  
  if (!myClaimRecord.value) {
    ElMessage.error('申请记录不存在')
    return
  }
  
  editClaimLoading.value = true
  try {
    // 先上传图片到服务器
    let proofImageUrls = []
    if (editClaimForm.value.proofImages && editClaimForm.value.proofImages.length > 0) {
      try {
        proofImageUrls = await uploadEditProofImages()
      } catch (error) {
        ElMessage.error(error.message || '图片上传失败，请重试')
        editClaimLoading.value = false
        return
      }
    }
    
    const data = {
      description: editClaimForm.value.description,
      lostTime: editClaimForm.value.lostTime,
      otherInfo: editClaimForm.value.otherInfo,
      proofImages: proofImageUrls.length > 0 ? proofImageUrls : null
    }
    
    const response = await lostFoundApi.updateClaim(myClaimRecord.value.id, data)
    if (response.code === 200) {
      ElMessage.success('申请已更新')
      showEditClaimDialog.value = false
      // 重新获取我的申请
      await fetchMyClaim()
    }
  } catch (error) {
    console.error('更新申请失败:', error)
    ElMessage.error(error.response?.data?.message || '更新申请失败')
  } finally {
    editClaimLoading.value = false
  }
}

/**
 * 删除申请
 */
const handleDeleteClaim = async () => {
  if (!myClaimRecord.value) {
    ElMessage.error('申请记录不存在')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这条申请吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await lostFoundApi.deleteClaim(myClaimRecord.value.id)
    if (response.code === 200) {
      ElMessage.success('申请已删除')
      myClaimRecord.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除申请失败:', error)
      ElMessage.error(error.response?.data?.message || '删除申请失败')
    }
  }
}

/**
 * 重置编辑申请表单
 */
const resetEditClaimForm = () => {
  editClaimForm.value = {
    description: '',
    lostTime: null,
    otherInfo: '',
    proofImages: []
  }
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  margin-bottom: 20px;
  font-size: 14px;
}

.detail-content {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.detail-card {
  display: flex;
  gap: 24px;
  padding: 24px;
}

.image-section {
  flex: 0 0 500px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.no-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  background-color: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  margin-bottom: 12px;
}

.image-thumbs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.thumb-item.active {
  border-color: #409EFF;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section {
  flex: 1;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
}

.badge-red {
  background-color: #F56C6C;
}

.badge-green {
  background-color: #67C23A;
}

.detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #F0F0F0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.detail-description {
  margin-bottom: 24px;
}

.detail-description h3 {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 12px 0;
}

.detail-description p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.reward-section {
  padding: 16px;
  background-color: #FFF7E6;
  border-radius: 8px;
  margin-bottom: 24px;
}

.reward-label {
  font-size: 14px;
  color: #606266;
}

.reward-value {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
  margin-left: 8px;
}

.contact-section {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background-color: #F0F9FF;
  border: 1px solid #B3D8FF;
  border-radius: 8px;
  margin-bottom: 24px;
}

.contact-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  flex-shrink: 0;
}

.contact-value {
  font-size: 14px;
  color: #409EFF;
  word-break: break-all;
  flex: 1;
}

.publisher-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #F5F5F5;
  border-radius: 8px;
  margin-bottom: 24px;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.publisher-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.action-section {
  margin-bottom: 24px;
}

.action-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.status-tip {
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .detail-card {
    flex-direction: column;
  }
  
  .image-section {
    flex: 1;
  }
  
  .main-image {
    height: 300px;
  }
  
  .publisher-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

.claim-records-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #F0F0F0;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.section-tip {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  line-height: 1.6;
}

.empty-claims {
  padding: 40px 0;
}

.claim-records-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.claim-record-item {
  background-color: #F9F9F9;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #E0E0E0;
}

.claim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.claim-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.claim-user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.claim-user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.claim-time {
  font-size: 12px;
  color: #909399;
}

.claim-status {
  flex-shrink: 0;
}

.claim-content {
  margin-bottom: 12px;
}

.claim-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.claim-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.claim-proof {
  margin-top: 8px;
}

.claim-proof strong {
  font-size: 13px;
  color: #606266;
  display: block;
  margin-bottom: 8px;
}

.proof-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.proof-image-wrapper {
  position: relative;
}

.proof-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.image-error {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  background-color: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 11px;
  text-align: center;
  padding: 4px;
  border: 1px solid #E0E0E0;
  box-sizing: border-box;
}

.claim-reject-reason {
  margin-top: 8px;
  padding: 8px;
  background-color: #FEF0F0;
  border-radius: 4px;
  font-size: 13px;
  color: #F56C6C;
}

.claim-reject-reason strong {
  color: #F56C6C;
}

.claim-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #E0E0E0;
}

.my-claim-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #F0F0F0;
}
</style>

