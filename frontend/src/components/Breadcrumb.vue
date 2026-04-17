<template>
  <el-tabs
    v-model="activeTab"
    class="demo-tabs"
    @tab-click="handleClick"
    closable
    @tab-remove="removeTab"
  >
    <el-tab-pane
      v-for="item in visitedViews"
      :key="item.path"
      :label="item.title"
      :name="item.path"
    >
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.path)
// 初始化为空数组，不默认添加首页
const visitedViews = ref([])

// 添加访问过的页面
const addVisitedView = (route) => {
  const { meta, path } = route
  // 跳过空白页面，不添加到访问记录
  if (path === '/blank') {
    return
  }
  if (meta && meta.title) {
    const view = {
      title: meta.title,
      path: path
    }
    if (!visitedViews.value.some(v => v.path === path)) {
      visitedViews.value.push(view)
    }
  }
}

// 处理标签页点击
const handleClick = (tab) => {
  router.push(tab.props.name)
}

// 移除标签页
const removeTab = (targetName) => {
  const tabs = visitedViews.value
  let activeName = activeTab.value
  
  // 过滤掉要移除的标签页
  const updatedTabs = tabs.filter(tab => tab.path !== targetName)
  
  // 更新访问记录
  visitedViews.value = updatedTabs
  
  if (activeName === targetName) {
    // 如果关闭的是当前活跃标签页，需要找到新的活跃标签页
    if (updatedTabs.length > 0) {
      // 如果还有其他标签页，跳转到第一个标签页
      activeName = updatedTabs[0].path
    } else {
      // 如果没有其他标签页，跳转到空白页面
      activeName = '/blank'
    }
  }
  
  // 更新活跃标签页并跳转
  activeTab.value = activeName
  router.push(activeName)
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  addVisitedView(route)
  activeTab.value = newPath
}, { immediate: true })
</script>

<style lang="scss" scoped>
.demo-tabs {
  margin: 0; // 移除margin，使标签页紧贴header
  height: 100%; // 设置高度与header一致
  
  :deep(.el-tabs__header) {
    margin: 0;
    height: 100%; // 设置标签头高度
    
    .el-tabs__nav-wrap {
      height: 100%; // 设置导航容器高度
    }
    
    .el-tabs__nav-scroll {
      height: 100%; // 设置滚动容器高度
    }
    
    .el-tabs__nav {
      height: 100%; // 设置导航项容器高度
      
      .el-tabs__item {
          height: 100%; // 设置标签项高度
          display:flex;
          align-items: center; // 垂直居中
        }
    }
  }
}
</style>