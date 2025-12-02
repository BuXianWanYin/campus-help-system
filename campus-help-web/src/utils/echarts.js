import * as echarts from 'echarts'

/**
 * 初始化 ECharts 实例
 * @param {HTMLElement} dom - DOM 元素
 * @param {Object} option - ECharts 配置项
 * @returns {echarts.ECharts} ECharts 实例
 */
export function initChart(dom, option) {
  const chart = echarts.init(dom)
  chart.setOption(option)
  return chart
}

/**
 * 响应式调整图表大小
 * @param {echarts.ECharts} chart - ECharts 实例
 */
export function resizeChart(chart) {
  if (chart) {
    chart.resize()
  }
}

