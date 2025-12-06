/**
 * ECharts图表工具
 * 封装ECharts的常用操作
 */

import * as echarts from 'echarts'

/**
 * 初始化ECharts实例
 * @param {HTMLElement} dom - DOM元素
 * @param {Object} option - ECharts配置项
 * @returns {echarts.ECharts} ECharts实例
 */
export function initChart(dom, option) {
  const chart = echarts.init(dom)
  chart.setOption(option)
  return chart
}

/**
 * 响应式调整图表大小
 * @param {echarts.ECharts} chart - ECharts实例
 */
export function resizeChart(chart) {
  if (chart) {
    chart.resize()
  }
}

