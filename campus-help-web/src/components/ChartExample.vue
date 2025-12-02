<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { initChart, resizeChart } from '../utils/echarts'

const chartRef = ref(null)
let chartInstance = null

onMounted(() => {
  if (chartRef.value) {
    chartInstance = initChart(chartRef.value, {
      title: {
        text: '示例图表'
      },
      tooltip: {},
      xAxis: {
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      },
      yAxis: {},
      series: [
        {
          name: '销量',
          type: 'bar',
          data: [20, 36, 10, 10, 20, 20, 10]
        }
      ]
    })

    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  resizeChart(chartInstance)
}
</script>

