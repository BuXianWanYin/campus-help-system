# 校园帮助系统 - 前端项目

基于 Vue 3 + Vite + Element Plus 构建的前端项目

## 技术栈

- **框架**: Vue 3.x
- **构建工具**: Vite
- **UI 框架**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4.x
- **HTTP 客户端**: Axios
- **图表**: ECharts

## 项目结构

```
campus-help-web/
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 组件
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia 状态管理
│   ├── utils/           # 工具函数
│   ├── views/           # 页面视图
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
└── package.json         # 项目配置
```

## 安装依赖

```bash
npm install
```

## 开发

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

## 构建

```bash
npm run build
```

## API 代理配置

项目已配置 API 代理，所有 `/api` 开头的请求会被代理到 `http://localhost:8080`

例如：
- 前端请求：`/api/user/login`
- 实际请求：`http://localhost:8080/user/login`

## 使用示例

### 使用 Axios 发送请求

```javascript
import { useApiStore } from '@/stores/api'

const apiStore = useApiStore()
const data = await apiStore.testConnection()
```

### 使用 ECharts

```javascript
import { initChart } from '@/utils/echarts'

const chart = initChart(this.$refs.chartDom, {
  title: { text: '示例图表' },
  series: [{ data: [1, 2, 3, 4, 5] }]
})
```

