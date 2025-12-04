<template>
  <div class="list-container">
    <div class="page-header">
      <h1 class="page-title">我的交易</h1>
    </div>

    <!-- 标签页：我买到的 / 我卖出的 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="我买到的" name="BUYER">
        <OrderListContent :role="'BUYER'" :filters="filters" />
      </el-tab-pane>
      <el-tab-pane label="我卖出的" name="SELLER">
        <OrderListContent :role="'SELLER'" :filters="filters" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import OrderListContent from './components/OrderListContent.vue'

defineOptions({
  name: 'OrderList'
})

const activeTab = ref('BUYER')

const filters = reactive({
  status: '',
  tradeMethod: '',
  keyword: ''
})

const handleTabChange = () => {
  filters.status = ''
  filters.tradeMethod = ''
  filters.keyword = ''
}
</script>

<style scoped>
.list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}
</style>

