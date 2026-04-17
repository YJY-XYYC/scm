<template>
  <router-view />
</template>

<script setup>
// import { useRouter } from 'vue-router'
// import { onMounted } from 'vue'

// const router = useRouter()

// onMounted(() => {
//   // 确保首次访问时重定向到正确的路径
//   const path = window.location.pathname
//   if (path === '/scm' || path === '/scm/') {
//     router.push('/dashboard')
//   }
// })
</script>

<style>
/* 最高优先级的全局样式 - 确保下拉选择器背景为#48494c */
:root {
  --el-select-dropdown-bg-color: #48494c !important;
}

/* 直接应用到下拉选择器相关类名，但排除时间选择器 */
.el-select-dropdown,
.el-select-dropdown-menu,
.el-select__popper:not(.el-date-picker),
.el-popper:not(.el-date-picker),
.el-popper__inner:not(.el-date-picker__content),
.el-select-dropdown__content {
  background-color: #48494c !important;
  background: #48494c !important;
  --el-select-dropdown-bg-color: #48494c !important;
  border-color: transparent !important;
  background-image: none !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5) !important;
}

/* 确保时间选择器保持默认背景色 */
.el-date-picker,
.el-date-picker__header,
.el-date-picker__body,
.el-date-picker__content,
.el-picker-panel,
.el-picker-panel__content {
  background-color: unset !important;
  background: unset !important;
  --el-bg-color: unset !important;
  --el-bg-color-overlay: unset !important;
  --el-background-color-base: unset !important;
  --el-background-color-overlay: unset !important;
}

/* 确保下拉菜单项也有正确的背景色 */
.el-select-dropdown__item {
  background-color: #48494c !important;
  color: white !important;
}

/* 专门针对头像下拉菜单的样式 */
.el-dropdown-menu {
  background-color: #48494c !important;
}

.el-dropdown-menu__item {
  background-color: #48494c !important;
  color: white !important;
}

.el-dropdown-menu__item:hover {
  background-color: #5a5b5f !important;
  color: white !important;
}

.el-select-dropdown__item:hover,
.el-select-dropdown__item.selected {
  background-color: #4a5568 !important;
  color: white !important;
}

/* 完全隐藏所有下拉选择器的三角形箭头 */
.el-select__popper::before,
.el-select__popper::after,
.el-select-dropdown__arrow,
.el-select-dropdown__arrow::before,
.el-select-dropdown__arrow::after,
/* 增加可能的Element UI相关选择器 */
.el-popper[x-placement^="bottom"] .popper__arrow::after,
.el-popper[x-placement^="bottom"] .popper__arrow,
.el-popper[x-placement^="top"] .popper__arrow::after,
.el-popper[x-placement^="top"] .popper__arrow,
.el-picker-panel__arrow,
.el-picker-panel__arrow::before,
.el-picker-panel__arrow::after {
  display: none !important;
  opacity: 0 !important;
  visibility: hidden !important;
  width: 0 !important;
  height: 0 !important;
  border: none !important;
  background: none !important;
  /* 确保最高优先级 */
  all: unset !important;
  position: absolute !important;
  clip: rect(0, 0, 0, 0) !important;
  pointer-events: none !important;
}

/* 保留其他popper的透明设置 */
.el-popper:not(.el-select__popper)::before,
.el-popper:not(.el-select__popper)::after {
  border-color: transparent !important;
  background-color: transparent !important;
}

/* 超级强制全局重置 - 日期选择器文字为白色，但排除头部标签 */
*[class*="el-date"] *:not(.el-date-picker__header-label):not(.el-date-picker__header-title):not(.el-date-picker__header-label > *):not(.el-date-picker__header-title > *),
*[class*="el-picker"] *:not(.el-date-picker__header-label):not(.el-date-picker__header-title):not(.el-date-picker__header-label > *):not(.el-date-picker__header-title > *) {
  color: #e4b23d !important;
  text-shadow: none !important;
}

/* 直接针对日期选择器容器 */
.el-date-picker,
.el-picker-panel {
  color: white !important;
  --el-text-color-primary: white !important;
  --el-text-color-regular: white !important;
  --el-text-color-secondary: white !important;
  --el-color-on-surface: white !important;
  --el-color-text-primary: white !important;
  --el-color-text-regular: white !important;
  --el-color-text-secondary: white !important;
}

/* 表格元素 - 最高优先级 */
.el-date-table th,
.el-date-table td,
.el-date-table td > span,
.el-month-table td,
.el-month-table td > span,
.el-year-table td,
.el-year-table td > span {
  color: #e4b23d !important;
}

/* 非本月日期颜色设置为浅色，便于区分 - 提高优先级 */
.el-date-table tr.el-date-table__row td.prev-month,
.el-date-table tr.el-date-table__row td.next-month,
.el-date-table tr.el-date-table__row td.prev-month > div.el-date-table-cell > span.el-date-table-cell__text,
.el-date-table tr.el-date-table__row td.next-month > div.el-date-table-cell > span.el-date-table-cell__text {
  color: rgba(228, 178, 61, 0.3) !important;
}

/* 按钮和标签 */
.el-time-panel__btn,
.el-time-panel__btn > span,
.el-picker-panel__footer button,
.el-picker-panel__footer button > span {
  color: white !important;
}
/* 日期选择器头部年月标签 - 最高优先级 */
.el-date-picker__header .el-date-picker__header-label,
.el-date-picker__header .el-date-picker__header-label > span,
.el-date-picker__header .el-date-picker__header-title,
.el-date-picker__header .el-date-picker__header-title > span,
.el-picker-panel__content .el-date-picker__header-label,
.el-picker-panel__content .el-date-picker__header-label > span,
.el-picker-panel__content .el-date-picker__header-title,
.el-picker-panel__content .el-date-picker__header-title > span {
  color: #e4b23d !important;
  color: #e4b23d !important; /* 重复声明，确保最高优先级 */
}

/* 时间选择器 */
.el-time-spinner__item {
  color: white !important;
}

/* 图标元素 */
.el-date-picker .el-icon,
.el-picker-panel .el-icon,
.el-date-picker svg,
.el-picker-panel svg {
  color: white !important;
  fill: white !important;
}

/* 覆盖可能的内联样式，但排除非本月日期和头部标签 */
.el-date-picker :not(.prev-month):not(.next-month):not(.el-date-picker__header-label):not(.el-date-picker__header-title) [style*="color"],
.el-picker-panel :not(.prev-month):not(.next-month):not(.el-date-picker__header-label):not(.el-date-picker__header-title) [style*="color"] {
  color: white !important;
}

/* 覆盖悬停状态 - 保持非本月日期的浅色 */
.el-date-table td:not(.prev-month):not(.next-month):hover,
.el-date-table td:not(.prev-month):not(.next-month):hover > span,
.el-month-table td:hover,
.el-month-table td:hover > span,
.el-year-table td:hover,
.el-year-table td:hover > span,
.el-time-spinner__item:hover {
  color: white !important;
  background-color: #5A5B5E !important;
}
/* 非本月日期的悬停状态 - 保持浅色 */
.el-date-table td.prev-month:hover,
.el-date-table td.next-month:hover,
.el-date-table td.prev-month:hover > span,
.el-date-table td.next-month:hover > span {
  color: rgba(228, 178, 61, 0.5) !important;
  background-color: #5A5B5E !important;
}

/* 移除全局日期选择器样式，避免与组件内样式冲突 */

/* 全局设置日期选择器底部按钮样式 */
.el-picker-panel__footer {
  > button,
  > .el-button,
  > .el-button--small,
  > .el-picker-panel__link-btn {
    background-color: #48494c !important;
    border-color: #48494c !important;
    color: white !important;
    
    &:hover,
    &:focus,
    &:active {
      background-color: #48494c !important;
      border-color: #48494c !important;
      color: white !important;
      box-shadow: none !important;
      transform: none !important;
    }
  }
  
  > .el-button--primary {
    &:hover,
    &:focus,
    &:active {
      background-color: #409eff !important;
      border-color: #409eff !important;
      color: white !important;
      box-shadow: none !important;
      transform: none !important;
    }
  }
}
</style>