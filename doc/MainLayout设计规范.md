# MainLayout 设计规范（2025年标准）

## 一、概述

`MainLayout.vue` 是整个系统的主布局组件，所有功能页面都基于此布局进行开发。本文档定义了基于 MainLayout 的统一设计标准。

## 二、配色方案

### 2.1 主渐变色

**标准渐变**：`linear-gradient(135deg, #769fcd 0%, #409eff 100%)`

**使用场景**：
- 主要按钮
- Logo 图标
- 功能卡片图标
- 头像背景
- 导航栏激活状态

### 2.2 辅助渐变色

**绿色渐变**：`linear-gradient(135deg, #67c23a 0%, #85ce61 100%)`
- 用于：成功状态、闲置交易模块

**橙色渐变**：`linear-gradient(135deg, #ff9800 0%, #ffb74d 100%)`
- 用于：警告状态、跑腿服务模块

### 2.3 背景色

- 页面主背景：`#f7fbfc`
- 卡片背景：`#FFFFFF`
- 卡片渐变背景：`linear-gradient(135deg, #FFFFFF 0%, #f7fbfc 100%)`
- 区块背景：`#d6e6f2`

### 2.4 边框和文字色

- 边框：`#b9d7ea`
- 主文字：`#303133`
- 常规文字：`#606266`
- 次要文字：`#909399`

## 三、功能模块卡片设计

### 3.1 布局规范

**网格布局**：
```css
.module-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
```

**响应式**：
- 移动端：`grid-template-columns: 1fr;`（单列）

### 3.2 卡片样式

```css
.module-card {
  background: linear-gradient(135deg, #FFFFFF 0%, #f7fbfc 100%);
  border-radius: 12px;
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid #b9d7ea;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}
```

### 3.3 图标设计

**尺寸**：64px × 64px

**样式**：
```css
.module-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.module-icon.icon-blue {
  background: linear-gradient(135deg, #769fcd 0%, #409eff 100%);
  color: #FFFFFF;
}

.module-icon.icon-green {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #FFFFFF;
}

.module-icon.icon-orange {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  color: #FFFFFF;
}
```

### 3.4 文字内容

**标题**：
- 字体大小：18px
- 字重：bold
- 颜色：`#303133`
- 间距：margin-bottom: 8px

**描述**：
- 字体大小：14px
- 颜色：`#606266`
- 行高：1.5

## 四、顶部导航栏

### 4.1 Logo 设计

**尺寸**：40px × 40px

**样式**：
```css
.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #769fcd 0%, #409eff 100%);
  color: #FFFFFF;
}
```

### 4.2 菜单项

**激活状态**：
```css
.el-menu--horizontal .el-menu-item.is-active {
  color: #769fcd;
  border-bottom: 2px solid #769fcd;
}
```

## 五、按钮设计

### 5.1 主要按钮

```css
.el-button--primary {
  background: linear-gradient(135deg, #769fcd 0%, #409eff 100%);
  border: none;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}
```

### 5.2 圆角规范

- 主要按钮：20px
- 卡片：8px-12px
- Logo/图标：8px（方形）或 50%（圆形）

## 六、欢迎横幅

### 6.1 背景渐变

```css
.welcome-banner {
  background: linear-gradient(135deg, #769fcd 0%, #409eff 100%);
  border-radius: 12px;
  padding: 32px;
  color: #FFFFFF;
}
```

### 6.2 统计卡片

```css
.stat-item {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 16px 20px;
  text-align: center;
}
```

## 七、响应式设计

### 7.1 断点

- 桌面端：> 1024px
- 平板端：768px - 1024px
- 移动端：< 768px

### 7.2 移动端适配

**功能模块卡片**：
```css
@media (max-width: 768px) {
  .module-grid {
    grid-template-columns: 1fr;
  }
  
  .module-card {
    padding: 24px 20px;
  }
  
  .module-icon {
    width: 56px;
    height: 56px;
  }
  
  .module-title {
    font-size: 16px;
  }
  
  .module-desc {
    font-size: 13px;
  }
}
```

## 八、阴影规范

### 8.1 卡片阴影

- 默认：`0 2px 12px rgba(0, 0, 0, 0.05)`
- 悬停：`0 8px 24px rgba(0, 0, 0, 0.12)`

### 8.2 按钮阴影

- 默认：`0 2px 8px rgba(64, 158, 255, 0.3)`
- 悬停：`0 4px 12px rgba(64, 158, 255, 0.4)`

### 8.3 图标阴影

- 功能图标：`0 4px 12px rgba(0, 0, 0, 0.1)`

## 九、动画效果

### 9.1 卡片悬停

```css
.module-card {
  transition: all 0.3s ease;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}
```

### 9.2 按钮悬停

```css
.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}
```

## 十、开发检查清单

### 10.1 配色检查

- [ ] 主要按钮使用标准渐变：`linear-gradient(135deg, #769fcd 0%, #409eff 100%)`
- [ ] 功能图标使用对应渐变色
- [ ] 页面背景使用 `#f7fbfc`
- [ ] 卡片背景使用白色或浅色渐变
- [ ] 边框使用 `#b9d7ea`

### 10.2 布局检查

- [ ] 功能模块使用 3 列网格布局（桌面端）
- [ ] 移动端切换为单列布局
- [ ] 卡片内容左对齐，图标在左侧
- [ ] 间距符合规范（gap: 24px）

### 10.3 交互检查

- [ ] 卡片有悬停效果（上浮 + 阴影加深）
- [ ] 按钮有悬停效果（上浮 + 阴影加深）
- [ ] 所有交互元素有 transition 过渡
- [ ] 点击卡片可跳转到对应页面

### 10.4 响应式检查

- [ ] 桌面端显示 3 列
- [ ] 移动端显示 1 列
- [ ] 图标和文字大小适配
- [ ] 间距和内边距适配

## 十一、与其他页面的集成

### 11.1 个人中心页面

个人中心页面应使用相同的配色方案：
- 头像背景：`linear-gradient(135deg, #769fcd 0%, #409eff 100%)`
- 按钮背景：`linear-gradient(135deg, #769fcd 0%, #409eff 100%)`
- 卡片边框：`#b9d7ea`

### 11.2 列表页面

列表页面（失物招领、闲置交易、跑腿服务）应：
- 使用相同的卡片样式
- 使用相同的悬停效果
- 使用相同的配色方案

### 11.3 详情页面

详情页面应：
- 使用相同的按钮样式
- 使用相同的标签样式
- 使用相同的信息展示卡片

## 十二、核心功能模块

### 12.1 失物招领

- 主色：蓝色渐变
- 图标：Search
- 路径：`/lost-found/list`

### 12.2 闲置交易

- 主色：绿色渐变
- 图标：ShoppingBag
- 路径：`/goods/list`

### 12.3 跑腿服务

- 主色：橙色渐变
- 图标：TrendCharts
- 路径：`/task/list`

## 十三、总结

所有页面开发必须遵循 MainLayout 的设计标准，确保：
1. 配色统一（使用标准渐变）
2. 布局一致（3列网格 → 单列）
3. 交互统一（悬停效果）
4. 响应式完整（桌面 + 移动）

