/**
 * Vue3 自定义指令
 */

/**
 * 点击外部关闭指令
 * 使用方式：v-click-outside="handler"
 * handler 是一个函数，当点击元素外部时会被调用
 */
export const clickOutside = {
  mounted(el, binding) {
    el._clickOutside = (event) => {
      // 获取排除的元素（如按钮）
      const excludeSelector = binding.arg || '.notification-btn-wrapper'
      const excludeEl = document.querySelector(excludeSelector)
      
      // 如果点击的元素在指令绑定的元素内部，则不触发
      // 如果点击的元素在排除的元素内部，也不触发
      if (
        !(el === event.target || el.contains(event.target)) &&
        !(excludeEl && (excludeEl === event.target || excludeEl.contains(event.target)))
      ) {
        // 如果绑定的值是函数，则调用它
        if (typeof binding.value === 'function') {
          binding.value(event)
        }
      }
    }
    // 使用 setTimeout 确保在下一个事件循环后添加事件监听，避免立即触发
    setTimeout(() => {
      document.addEventListener('click', el._clickOutside)
    }, 0)
  },
  unmounted(el) {
    // 组件卸载时移除事件监听
    if (el._clickOutside) {
      document.removeEventListener('click', el._clickOutside)
      delete el._clickOutside
    }
  }
}

