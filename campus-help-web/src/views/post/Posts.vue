<template>
  <div class="posts-container">
    <div class="page-header">
      <h1 class="page-title">我的发布</h1>
      <el-button type="primary" @click="handlePublish">
        {{ activeTab === 'lost-found' ? '发布失物' : activeTab === 'goods' ? '发布商品' : '发布问题' }}
      </el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="tabs-container">
      <el-tab-pane label="失物招领" name="lost-found">
        <!-- 失物招领筛选栏 -->
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="类型：">
              <el-select v-model="lostFoundFilters.type" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="失物" value="LOST" />
                <el-option label="招领" value="FOUND" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类：">
              <el-select v-model="lostFoundFilters.category" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="证件类" value="证件类" />
                <el-option label="电子产品" value="电子产品" />
                <el-option label="生活用品" value="生活用品" />
                <el-option label="书籍" value="书籍" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态：">
              <el-select v-model="lostFoundFilters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="待审核" value="PENDING_REVIEW" />
                <el-option label="待认领" value="PENDING_CLAIM" />
                <el-option label="认领中" value="CLAIMING" />
                <el-option label="已认领" value="CLAIMED" />
                <el-option label="已关闭" value="CLOSED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序：">
              <el-select v-model="lostFoundFilters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
                <el-option label="最新" value="latest" />
                <el-option label="浏览最多" value="view" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      <el-tab-pane label="闲置商品" name="goods">
        <!-- 商品筛选栏 -->
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="分类：">
              <el-select v-model="goodsFilters.category" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
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
            <el-form-item label="状态：">
              <el-select v-model="goodsFilters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="待审核" value="PENDING_REVIEW" />
                <el-option label="在售" value="ON_SALE" />
                <el-option label="已下架" value="OFFSHELF" />
                <el-option label="已售完" value="SOLD_OUT" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序：">
              <el-select v-model="goodsFilters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
                <el-option label="最新" value="latest" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
                <el-option label="浏览量" value="view" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      <el-tab-pane label="学习互助" name="study">
        <!-- 学习互助筛选栏 -->
        <div class="filter-bar">
          <el-form :inline="true" class="filter-form">
            <el-form-item label="分类：">
              <el-select v-model="studyFilters.category" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="数学" value="MATH" />
                <el-option label="物理" value="PHYSICS" />
                <el-option label="化学" value="CHEMISTRY" />
                <el-option label="生物" value="BIOLOGY" />
                <el-option label="计算机" value="COMPUTER" />
                <el-option label="英语" value="ENGLISH" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态：">
              <el-select v-model="studyFilters.status" placeholder="全部" style="width: 120px" clearable @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="待审核" value="PENDING_REVIEW" />
                <el-option label="待解答" value="PENDING_ANSWER" />
                <el-option label="已回答" value="ANSWERED" />
                <el-option label="已解决" value="SOLVED" />
                <el-option label="已取消" value="CANCELLED" />
                <el-option label="已拒绝" value="REJECTED" />
                <el-option label="已下架" value="ADMIN_OFFSHELF" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序：">
              <el-select v-model="studyFilters.sortBy" placeholder="最新" style="width: 120px" @change="handleSearch">
                <el-option label="最新" value="latest" />
                <el-option label="回答最多" value="popular" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 失物列表 -->
    <div v-if="activeTab === 'lost-found'" class="card-grid" v-loading="loading">
      <div v-for="item in lostFoundList" :key="item.id" class="lost-found-card">
        <div class="card-image-wrapper" @click="goToLostFoundDetail(item.id)">
          <img :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
          <span class="card-badge" :class="item.type === 'LOST' ? 'badge-red' : 'badge-green'">
            {{ item.type === 'LOST' ? '失物' : '招领' }}
          </span>
          <span class="status-badge" :class="getLostFoundStatusClass(item.status)">
            {{ getLostFoundStatusText(item.status, item.type) }}
          </span>
        </div>
        <div class="card-content">
          <h3 class="card-title" @click="goToLostFoundDetail(item.id)">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Location /></el-icon>
              {{ item.lostLocation }}
            </span>
            <!-- 审核未通过（待审核、已拒绝）不显示发布日期和浏览量 -->
            <template v-if="item.status !== 'PENDING_REVIEW' && item.status !== 'REJECTED'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(item.createTime) }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.viewCount || 0 }}次浏览
              </span>
            </template>
            <template v-else-if="item.status === 'PENDING_REVIEW'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                待审核
              </span>
            </template>
            <template v-else-if="item.status === 'REJECTED'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                已拒绝
              </span>
            </template>
          </div>
          <div class="card-footer">
            <div class="card-info">
            </div>
            <div class="card-actions">
              <el-button size="small" @click.stop="goToLostFoundDetail(item.id)">查看详情</el-button>
              <el-button 
                v-if="canEditLostFound(item.status)" 
                size="small" 
                type="primary" 
                text 
                @click.stop="handleEditLostFound(item)">
                编辑
              </el-button>
              <el-button 
                v-if="canCloseLostFound(item.status)" 
                size="small" 
                type="warning" 
                text 
                @click.stop="handleCloseLostFound(item)">
                关闭
              </el-button>
              <el-button 
                v-if="canDeleteLostFound(item.status)" 
                size="small" 
                type="danger" 
                text 
                @click.stop="handleDeleteLostFound(item)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习互助列表 -->
    <div v-if="activeTab === 'study'" class="card-grid" v-loading="loading">
      <div v-for="item in studyList" :key="item.id" class="study-card">
        <div class="card-image-wrapper" @click="goToStudyDetail(item.id)">
          <img v-if="getFirstImage(item.images)" :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
          <div v-else class="card-image-placeholder">
            <el-icon><Document /></el-icon>
          </div>
          <span class="status-badge status-badge-left" :class="getStudyStatusClass(item.status)">
            {{ getStudyStatusText(item.status) }}
          </span>
        </div>
        <div class="card-content">
          <h3 class="card-title" @click="goToStudyDetail(item.id)">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Folder /></el-icon>
              {{ getStudyCategoryName(item.category) }}
            </span>
            <template v-if="item.status !== 'PENDING_REVIEW' && item.status !== 'REJECTED'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(item.createTime) }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.viewCount || 0 }}次浏览
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ item.answerCount || 0 }}个回答
              </span>
            </template>
            <template v-else-if="item.status === 'PENDING_REVIEW'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                待审核
              </span>
            </template>
            <template v-else-if="item.status === 'REJECTED'">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                已拒绝
              </span>
            </template>
          </div>
          <div class="card-footer">
            <div class="card-info">
            </div>
            <div class="card-actions">
              <el-button size="small" @click.stop="goToStudyDetail(item.id)">查看详情</el-button>
              <el-button 
                v-if="canEditStudy(item.status)" 
                size="small" 
                type="primary" 
                text 
                @click.stop="handleEditStudy(item)">
                编辑
              </el-button>
              <el-button 
                v-if="canCancelStudy(item.status)" 
                size="small" 
                type="warning" 
                text 
                @click.stop="handleCancelStudy(item)">
                取消
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-if="activeTab === 'goods'" class="card-grid" v-loading="loading">
      <div v-for="item in goodsList" :key="item.id" class="goods-card">
        <div class="card-image-wrapper" @click="goToGoodsDetail(item.id)">
          <img :src="getFirstImage(item.images)" :alt="item.title" class="card-image" />
          <span class="status-badge status-badge-left" :class="getGoodsStatusClass(item.status)">
            {{ getGoodsStatusText(item.status) }}
          </span>
          <span v-if="item.stock" class="stock-badge">
            库存：{{ item.stock }}件
          </span>
        </div>
        <div class="card-content">
          <h3 class="card-title" @click="goToGoodsDetail(item.id)">{{ item.title }}</h3>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Folder /></el-icon>
              {{ item.category }}
            </span>
            <span class="meta-item">
              <el-icon><Box /></el-icon>
              {{ item.condition }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ item.viewCount || 0 }}次浏览
            </span>
            <span v-if="item.tradeMethod" class="meta-item">
              <el-icon><Box /></el-icon>
              {{ item.tradeMethod === 'MAIL' ? '邮寄' : '自提' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.createTime) }}
            </span>
          </div>
          <div class="card-footer">
            <div class="card-info">
              <span class="price-text">¥{{ item.currentPrice }}</span>
            </div>
            <div class="card-actions">
              <el-button size="small" @click.stop="goToGoodsDetail(item.id)">查看详情</el-button>
              <el-button 
                v-if="canEditGoods(item.status)" 
                size="small" 
                type="primary" 
                text 
                @click.stop="handleEditGoods(item)">
                编辑
              </el-button>
              <el-button 
                v-if="canOffshelfGoods(item.status)" 
                size="small" 
                type="warning" 
                text 
                @click.stop="handleOffshelfGoods(item)">
                下架
              </el-button>
              <el-button 
                v-if="canReshelfGoods(item.status)" 
                size="small" 
                type="success" 
                text 
                @click.stop="handleReshelfGoods(item)">
                上架
              </el-button>
              <el-button 
                v-if="canDeleteGoods(item.status)" 
                size="small" 
                type="danger" 
                text 
                @click.stop="handleDeleteGoods(item)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-if="!loading && ((activeTab === 'lost-found' && lostFoundList.length === 0) || (activeTab === 'goods' && goodsList.length === 0) || (activeTab === 'study' && studyList.length === 0))" 
      :description="activeTab === 'lost-found' ? '您还没有发布任何失物信息' : activeTab === 'goods' ? '您还没有发布任何商品' : '您还没有发布任何问题'" 
    />

    <!-- 分页器 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

/**
 * 我的发布页面
 * 展示用户发布的所有内容，包括失物招领、闲置商品、学习问题
 */

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, View, Folder, Box, Document, ChatDotRound } from '@element-plus/icons-vue'
import { lostFoundApi, goodsApi, questionApi } from '@/api'
import { getAvatarUrl } from '@/utils/image'

const router = useRouter()

// 当前激活的标签页
const activeTab = ref('lost-found')
const loading = ref(false)
const lostFoundList = ref([])
const goodsList = ref([])
const studyList = ref([])
const total = ref(0)

// 失物招领筛选条件
const lostFoundFilters = reactive({
  type: '',
  category: '',
  status: '',
  sortBy: 'latest'
})

const goodsFilters = reactive({
  category: '',
  status: '',
  sortBy: 'latest'
})

const studyFilters = reactive({
  category: '',
  status: '',
  sortBy: 'latest'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 获取我的失物招领列表
 */
const fetchMyLostFoundPosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      type: lostFoundFilters.type || undefined,
      category: lostFoundFilters.category || undefined,
      status: lostFoundFilters.status || undefined,
      sortBy: lostFoundFilters.sortBy || 'latest'
    }
    
    const response = await lostFoundApi.getMyPosts(params)
    if (response.code === 200) {
      const pageData = response.data
      lostFoundList.value = (pageData.records || []).map(item => ({
        ...item,
        images: parseImages(item.images)
      }))
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取我的失物招领列表失败:', error)
    ElMessage.error('获取我的失物招领列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取我的商品列表
 */
const fetchMyGoodsPosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      category: goodsFilters.category || undefined,
      status: goodsFilters.status || undefined,
      sortBy: goodsFilters.sortBy || 'latest'
    }
    
    const response = await goodsApi.getMyPosts(params)
    if (response.code === 200) {
      const pageData = response.data
      goodsList.value = (pageData.records || []).map(item => ({
        ...item,
        images: parseImages(item.images)
      }))
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取我的商品列表失败:', error)
    ElMessage.error('获取我的商品列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取我的学习问题列表
 */
const fetchMyStudyPosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: studyFilters.status || undefined
    }
    
    const response = await questionApi.getMyPublished(params)
    if (response.code === 200) {
      const pageData = response.data
      // 前端筛选分类和排序
      let records = pageData.records || []
      
      // 分类筛选
      if (studyFilters.category) {
        records = records.filter(item => item.category === studyFilters.category)
      }
      
      // 排序
      if (studyFilters.sortBy === 'popular') {
        records.sort((a, b) => (b.answerCount || 0) - (a.answerCount || 0))
      } else {
        // latest - 默认按创建时间倒序
        records.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      
      studyList.value = records.map(item => ({
        ...item,
        images: parseImages(item.images)
      }))
      total.value = records.length
    }
  } catch (error) {
    console.error('获取我的问题列表失败:', error)
    ElMessage.error('获取我的问题列表失败')
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
    return JSON.parse(imagesJson)
  } catch {
    return []
  }
}

/**
 * 获取第一张图片
 */
const getFirstImage = (images) => {
  if (Array.isArray(images) && images.length > 0) {
    return getAvatarUrl(images[0])
  }
  return 'https://via.placeholder.com/300x200?text=暂无图片'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

/**
 * 处理标签页切换
 */
const handleTabChange = () => {
  pagination.pageNum = 1
  if (activeTab.value === 'lost-found') {
    fetchMyLostFoundPosts()
  } else if (activeTab.value === 'goods') {
    fetchMyGoodsPosts()
  } else if (activeTab.value === 'study') {
    fetchMyStudyPosts()
  }
}

/**
 * 获取失物招领状态文本
 */
const getLostFoundStatusText = (status, type) => {
  // 根据类型显示不同的状态文本
  if (type === 'LOST') {
    // 失物类型：寻找中、确认中、已找到
    const lostStatusMap = {
      'PENDING_REVIEW': '待审核',
      'PENDING_CLAIM': '寻找中',
      'CLAIMING': '确认中',  // 有人提供线索，正在确认
      'CLAIMED': '已找到',
      'CLOSED': '已关闭',
      'REJECTED': '已拒绝'
    }
    return lostStatusMap[status] || status
  } else {
    // 招领类型：等待失主、认领中、已认领
    const foundStatusMap = {
      'PENDING_REVIEW': '待审核',
      'PENDING_CLAIM': '等待失主',
      'CLAIMING': '认领中',
      'CLAIMED': '已认领',
      'CLOSED': '已关闭',
      'REJECTED': '已拒绝'
    }
    return foundStatusMap[status] || status
  }
}

/**
 * 获取失物招领状态样式类
 */
const getLostFoundStatusClass = (status) => {
  const classMap = {
    'PENDING_REVIEW': 'status-warning',
    'PENDING_CLAIM': 'status-info',
    'CLAIMING': 'status-primary',
    'CLAIMED': 'status-success',
    'CLOSED': 'status-default',
    'REJECTED': 'status-danger'
  }
  return classMap[status] || 'status-default'
}

/**
 * 获取商品状态文本
 */
const getGoodsStatusText = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'ON_SALE': '在售',
    'OFFSHELF': '已下架',
    'SOLD_OUT': '已售完',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

/**
 * 获取商品状态样式类
 */
const getGoodsStatusClass = (status) => {
  const classMap = {
    'PENDING_REVIEW': 'status-warning',
    'ON_SALE': 'status-success',
    'OFFSHELF': 'status-default',
    'SOLD_OUT': 'status-info',
    'REJECTED': 'status-danger'
  }
  return classMap[status] || 'status-default'
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.pageNum = 1
  if (activeTab.value === 'lost-found') {
    fetchMyLostFoundPosts()
  } else if (activeTab.value === 'goods') {
    fetchMyGoodsPosts()
  } else if (activeTab.value === 'study') {
    fetchMyStudyPosts()
  }
}

/**
 * 失物招领相关判断
 */
const canEditLostFound = (status) => {
  return status !== 'CLAIMED' && status !== 'CLOSED'
}

const canCloseLostFound = (status) => {
  return status !== 'CLOSED' && status !== 'CLAIMED'
}

const canDeleteLostFound = (status) => {
  return status !== 'CLAIMING' && status !== 'CLAIMED'
}

/**
 * 商品相关判断
 */
const canEditGoods = (status) => {
  return status === 'ON_SALE' || status === 'OFFSHELF' || status === 'REJECTED'
}

const canOffshelfGoods = (status) => {
  return status === 'ON_SALE'
}

const canReshelfGoods = (status) => {
  return status === 'OFFSHELF'
}

const canDeleteGoods = (status) => {
  return status !== 'SOLD_OUT'
}

/**
 * 处理发布
 */
const handlePublish = () => {
  if (activeTab.value === 'lost-found') {
    router.push('/lost-found/publish')
  } else if (activeTab.value === 'goods') {
    router.push('/goods/publish')
  } else if (activeTab.value === 'study') {
    router.push('/study/publish')
  }
}

/**
 * 失物招领相关操作
 */
const goToLostFoundDetail = (id) => {
  router.push(`/lost-found/detail/${id}`)
}

const handleEditLostFound = (item) => {
  router.push(`/lost-found/edit/${item.id}`)
}

const handleCloseLostFound = async (item) => {
  try {
    const typeText = item.type === 'LOST' ? '失物' : '招领'
    const closeTip = item.type === 'LOST' 
      ? '关闭后将不再显示，其他用户将无法看到此失物信息。' 
      : '关闭后将不可被认领，失主将无法申请认领该物品。'
    
    await ElMessageBox.confirm(
      `确定要关闭${typeText}《${item.title}》吗？${closeTip}`,
      `关闭${typeText}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await lostFoundApi.close(item.id)
    ElMessage.success('关闭成功')
    fetchMyLostFoundPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭失败:', error)
      ElMessage.error(error.response?.data?.message || '关闭失败')
    }
  }
}

const handleDeleteLostFound = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除失物《${item.title}》吗？删除后无法恢复。`,
      '删除失物',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await lostFoundApi.delete(item.id)
    ElMessage.success('删除成功')
    fetchMyLostFoundPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失物失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

/**
 * 商品相关操作
 */
const goToGoodsDetail = (id) => {
  router.push(`/goods/detail/${id}`)
}

const handleEditGoods = (item) => {
  router.push(`/goods/edit/${item.id}`)
}

const handleOffshelfGoods = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架商品《${item.title}》吗？下架后其他用户将无法看到此商品。`,
      '下架商品',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await goodsApi.offshelf(item.id)
    ElMessage.success('下架成功')
    fetchMyGoodsPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
      ElMessage.error(error.response?.data?.message || '下架失败')
    }
  }
}

const handleReshelfGoods = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新上架商品《${item.title}》吗？`,
      '上架商品',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await goodsApi.reshelf(item.id)
    ElMessage.success('上架成功')
    fetchMyGoodsPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('上架失败:', error)
      ElMessage.error(error.response?.data?.message || '上架失败')
    }
  }
}

const handleDeleteGoods = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品《${item.title}》吗？删除后无法恢复。`,
      '删除商品',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await goodsApi.delete(item.id)
    ElMessage.success('删除成功')
    fetchMyGoodsPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

/**
 * 学习互助相关操作
 */
const goToStudyDetail = (id) => {
  router.push(`/study/detail/${id}`)
}

const canEditStudy = (status) => {
  // 已解决、已取消的不允许编辑，其他状态都可以编辑
  return status !== 'SOLVED' && status !== 'CANCELLED'
}

const canCancelStudy = (status) => {
  // 只有待解答、已回答状态的问题可以取消
  return status === 'PENDING_ANSWER' || status === 'ANSWERED'
}

const handleEditStudy = (item) => {
  router.push(`/study/edit/${item.id}`)
}

const handleCancelStudy = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消问题《${item.title}》吗？取消后将无法恢复。`,
      '取消问题',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await questionApi.cancel(item.id)
    ElMessage.success('取消成功')
    fetchMyStudyPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

/**
 * 获取学习问题状态文本
 */
const getStudyStatusText = (status) => {
  const statusMap = {
    'PENDING_REVIEW': '待审核',
    'PENDING_ANSWER': '待解答',
    'ANSWERED': '已回答',
    'SOLVED': '已解决',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝',
    'ADMIN_OFFSHELF': '已下架'
  }
  return statusMap[status] || status
}

/**
 * 获取学习问题状态样式类
 */
const getStudyStatusClass = (status) => {
  const classMap = {
    'PENDING_REVIEW': 'status-warning',
    'PENDING_ANSWER': 'status-info',
    'ANSWERED': 'status-primary',
    'SOLVED': 'status-success',
    'CANCELLED': 'status-default',
    'REJECTED': 'status-danger',
    'ADMIN_OFFSHELF': 'status-default'
  }
  return classMap[status] || 'status-default'
}

/**
 * 获取学习问题分类名称
 */
const getStudyCategoryName = (category) => {
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
 * 处理分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  if (activeTab.value === 'lost-found') {
    fetchMyLostFoundPosts()
  } else if (activeTab.value === 'goods') {
    fetchMyGoodsPosts()
  } else if (activeTab.value === 'study') {
    fetchMyStudyPosts()
  }
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  if (activeTab.value === 'lost-found') {
    fetchMyLostFoundPosts()
  } else if (activeTab.value === 'goods') {
    fetchMyGoodsPosts()
  } else if (activeTab.value === 'study') {
    fetchMyStudyPosts()
  }
}

onMounted(() => {
  if (activeTab.value === 'lost-found') {
    fetchMyLostFoundPosts()
  } else if (activeTab.value === 'goods') {
    fetchMyGoodsPosts()
  } else if (activeTab.value === 'study') {
    fetchMyStudyPosts()
  }
})
</script>

<style scoped>
.posts-container {
  max-width: 1200px;
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

.filter-bar {
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.lost-found-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.lost-found-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #FFFFFF;
  font-weight: 500;
  z-index: 1;
}

.badge-red {
  background-color: #F56C6C;
}

.badge-green {
  background-color: #67C23A;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #FFFFFF;
  font-weight: 500;
  z-index: 1;
}

.status-badge-left {
  left: 8px;
  right: auto;
}

.stock-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #FFFFFF;
  font-weight: 500;
  background-color: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 1;
}

.status-warning {
  background-color: #E6A23C;
}

.status-info {
  background-color: #909399;
}

.status-primary {
  background-color: #409EFF;
}

.status-success {
  background-color: #67C23A;
}

.status-default {
  background-color: #C0C4CC;
}

.status-danger {
  background-color: #F56C6C;
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.5;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  white-space: normal;
}

.card-title:hover {
  color: #409EFF;
}

.card-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  white-space: normal;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #F0F0F0;
  margin-top: auto;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.tabs-container {
  margin-bottom: 20px;
}

.goods-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  cursor: pointer;
}

.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.price-text {
  font-size: 20px;
  color: #F56C6C;
  font-weight: bold;
}

.stock-text {
  font-size: 12px;
  color: #909399;
}

.study-card {
  background-color: #FFFFFF;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #E0E0E0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.study-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F7FA;
  color: #909399;
  font-size: 48px;
}
</style>

