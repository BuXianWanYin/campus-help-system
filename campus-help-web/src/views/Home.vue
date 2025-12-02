<template>
  <div class="home">
    <el-container>
      <el-header>
        <h1>校园帮助系统</h1>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>欢迎使用校园帮助系统</span>
                </div>
              </template>
              <p>这是一个基于 Vue 3 + Vite + Element Plus 的项目</p>
              <el-button type="primary" @click="testApi">测试 API 连接</el-button>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>ECharts 示例</span>
                </div>
              </template>
              <ChartExample />
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useApiStore } from '../stores/api'
import ChartExample from '../components/ChartExample.vue'

const apiStore = useApiStore()

const testApi = async () => {
  try {
    const response = await apiStore.testConnection()
    ElMessage.success('API 连接成功！')
    console.log(response)
  } catch (error) {
    ElMessage.error('API 连接失败：' + error.message)
  }
}
</script>

<style scoped>
.home {
  width: 100%;
  height: 100vh;
}

.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-header h1 {
  margin: 0;
}

.el-main {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

