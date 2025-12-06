<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2 class="page-title">数据概览</h2>
      <div class="header-actions">
        <p class="page-subtitle">系统整体数据统计</p>
        <el-select v-model="statsPeriod" style="width: 120px" @change="handlePeriodChange">
          <el-option label="最近7天" value="7days" />
          <el-option label="最近30天" value="30days" />
          <el-option label="本学期" value="semester" />
          <el-option label="本学年" value="year" />
        </el-select>
      </div>
    </div>

    <!-- 系统统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-blue">
          <el-icon :size="32"><UserFilled /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">总用户数</p>
          <h3 class="stat-value">{{ stats.totalUsers || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-orange">
          <el-icon :size="32"><DocumentChecked /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">待审核认证</p>
          <h3 class="stat-value">{{ stats.pendingVerifications || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-green">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">已认证用户</p>
          <h3 class="stat-value">{{ stats.verifiedUsers || 0 }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-red">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <p class="stat-label">已封禁用户</p>
          <h3 class="stat-value">{{ stats.bannedUsers || 0 }}</h3>
        </div>
      </div>
    </div>

    <!-- 业务数据统计 -->
    <div class="business-stats-section">
      <h3 class="section-title">校园互助数据</h3>
      <div class="stats-cards">
        <div v-for="stat in statsData" :key="stat.id" class="stat-card">
          <div class="stat-card-content">
            <div>
              <p class="stat-label">{{ stat.label }}</p>
              <h3 class="stat-value">{{ stat.value }}</h3>
              <p class="stat-change" :class="stat.changeType">
                <el-icon><component :is="stat.changeIcon" /></el-icon>
                {{ stat.change }} 较上周
              </p>
            </div>
            <div class="stat-icon" :class="stat.colorClass">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
          </div>
        </div>
      </div>
      <div class="charts-grid">
        <div class="chart-card">
          <h3>互助类型分布</h3>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <h3>每日互助趋势</h3>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-cards">
        <el-card class="action-card" @click="goToVerification">
          <div class="action-icon icon-orange">
            <el-icon :size="24"><DocumentChecked /></el-icon>
          </div>
          <div class="action-content">
            <h4>实名认证审核</h4>
            <p>审核用户提交的实名认证申请</p>
          </div>
        </el-card>
        <el-card class="action-card" @click="goToUsers">
          <div class="action-icon icon-blue">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="action-content">
            <h4>用户管理</h4>
            <p>管理用户账号，包括封禁和解封</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, DocumentChecked, CircleCheck, Warning, ArrowUp, ArrowDown, Connection, User as UsersIcon, Clock } from '@element-plus/icons-vue'
import { initChart, resizeChart } from '@/utils/echarts'
import { adminApi } from '@/api'

const router = useRouter()
const statsPeriod = ref('7days')
const stats = ref({
  totalUsers: 0,
  pendingVerifications: 0,
  verifiedUsers: 0,
  bannedUsers: 0
})

// 业务统计数据（使用 markRaw 避免组件被响应式化）
const statsData = ref([
  { id: 1, label: '总互助次数', value: '0', change: '0%', changeType: 'change-up', changeIcon: markRaw(ArrowUp), icon: markRaw(Connection), colorClass: 'icon-blue' },
  { id: 2, label: '活跃用户', value: '0', change: '0%', changeType: 'change-up', changeIcon: markRaw(ArrowUp), icon: markRaw(UsersIcon), colorClass: 'icon-green' },
  { id: 3, label: '平均响应时间', value: '0分钟', change: '0%', changeType: 'change-down', changeIcon: markRaw(ArrowDown), icon: markRaw(Clock), colorClass: 'icon-orange' }
])

// 图表引用
const typeChartRef = ref(null)
const trendChartRef = ref(null)
let typeChartInstance = null
let trendChartInstance = null

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await adminApi.getDashboardStats(statsPeriod.value)
    if (response.code === 200) {
      const data = response.data
      stats.value = {
        totalUsers: data.totalUsers || 0,
        pendingVerifications: data.pendingVerifications || 0,
        verifiedUsers: data.verifiedUsers || 0,
        bannedUsers: data.bannedUsers || 0
      }
      
      // 更新业务统计数据
      statsData.value[0].value = formatNumber(data.totalAssistanceCount || 0)
      statsData.value[1].value = formatNumber(data.activeUsers || 0)
      // 平均响应时间暂时不计算，显示固定值
      
      // 更新图表数据
      updateCharts(data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error(error.message || '获取统计数据失败')
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 更新图表数据
const updateCharts = (data) => {
  // 更新类型分布图表
  if (typeChartInstance && data.typeDistribution) {
    const typeData = [
      { value: data.typeDistribution['失物招领'] || 0, name: '失物招领', itemStyle: { color: '#1E88E5' } },
      { value: data.typeDistribution['闲置交易'] || 0, name: '闲置交易', itemStyle: { color: '#4CAF50' } },
      { value: data.typeDistribution['学习互助'] || 0, name: '学习互助', itemStyle: { color: '#FF9800' } }
    ]
    typeChartInstance.setOption({
      series: [{ data: typeData }]
    })
  }
  
  // 更新每日趋势图表
  if (trendChartInstance && data.dailyTrends) {
    const dates = data.dailyTrends.map(t => {
      const date = new Date(t.date)
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return days[date.getDay()]
    })
    const lostFoundData = data.dailyTrends.map(t => t.lostFoundCount || 0)
    const goodsData = data.dailyTrends.map(t => t.goodsCount || 0)
    const studyData = data.dailyTrends.map(t => t.studyQuestionCount || 0)
    
    trendChartInstance.setOption({
      xAxis: { data: dates },
      series: [
        { data: lostFoundData },
        { data: goodsData },
        { data: studyData }
      ]
    })
  }
}

// 处理时间周期变化
const handlePeriodChange = () => {
  fetchStats()
  // 重新加载图表数据
  initCharts()
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    if (typeChartRef.value) {
      if (typeChartInstance) {
        typeChartInstance.dispose()
      }
      typeChartInstance = initChart(typeChartRef.value, {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        series: [
          {
            name: '互助类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 0, name: '失物招领', itemStyle: { color: '#1E88E5' } },
              { value: 0, name: '闲置交易', itemStyle: { color: '#4CAF50' } },
              { value: 0, name: '学习互助', itemStyle: { color: '#FF9800' } }
            ]
          }
        ]
      })
    }

    if (trendChartRef.value) {
      if (trendChartInstance) {
        trendChartInstance.dispose()
      }
      trendChartInstance = initChart(trendChartRef.value, {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['失物招领', '闲置交易', '学习互助'],
          top: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '失物招领',
            type: 'line',
            stack: 'Total',
            smooth: true,
            data: [0, 0, 0, 0, 0, 0, 0],
            itemStyle: { color: '#1E88E5' },
            areaStyle: { opacity: 0.3 }
          },
          {
            name: '闲置交易',
            type: 'line',
            stack: 'Total',
            smooth: true,
            data: [0, 0, 0, 0, 0, 0, 0],
            itemStyle: { color: '#4CAF50' },
            areaStyle: { opacity: 0.3 }
          },
          {
            name: '学习互助',
            type: 'line',
            stack: 'Total',
            smooth: true,
            data: [0, 0, 0, 0, 0, 0, 0],
            itemStyle: { color: '#FF9800' },
            areaStyle: { opacity: 0.3 }
          }
        ]
      })
    }
  })
}

const handleResize = () => {
  resizeChart(typeChartInstance)
  resizeChart(trendChartInstance)
}

// 跳转到实名认证审核
const goToVerification = () => {
  router.push('/admin/verification')
}

// 跳转到用户管理
const goToUsers = () => {
  router.push('/admin/users')
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (typeChartInstance) {
    typeChartInstance.dispose()
  }
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
})
</script>

<style scoped>
.dashboard-page {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
}

.page-header {
  margin-bottom: var(--spacing-2xl);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.stat-card {
  background-color: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.stat-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.stat-icon.icon-green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.stat-icon.icon-red {
  background-color: rgba(244, 67, 54, 0.1);
  color: var(--color-danger);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-xs) 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0;
}

.quick-actions {
  margin-top: var(--spacing-2xl);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.action-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.action-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.action-content {
  flex: 1;
}

.action-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.action-content p {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* 业务数据统计 */
.business-stats-section {
  margin-top: var(--spacing-2xl);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 var(--spacing-sm) 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.change-up {
  color: var(--color-success);
}

.stat-change.change-down {
  color: var(--color-danger);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.icon-blue {
  background-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.stat-icon.icon-green {
  background-color: rgba(76, 175, 80, 0.1);
  color: var(--color-success);
}

.stat-icon.icon-orange {
  background-color: rgba(255, 152, 0, 0.1);
  color: var(--color-secondary);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-top: var(--spacing-lg);
}

.chart-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.chart-container {
  width: 100%;
  height: 256px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>

