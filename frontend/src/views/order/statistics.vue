<template>
  <div class="order-statistics-container">
    <div class="page-header">
      <h2>订单统计</h2>
    </div>
    
    <el-card class="mb-4">
      <div class="stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ totalSales }}</div>
              <div class="stat-label">总销售额</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ pendingOrders }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ completedOrders }}</div>
              <div class="stat-label">已完成订单</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-card class="mb-4">
      <template #header>
        <div class="card-header">
          <span>销售趋势</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateRangeChange"
          />
        </div>
      </template>
      <div class="chart-container">
        <!-- 这里可以集成图表组件 -->
        <div class="placeholder-chart">销售趋势图表区域</div>
      </div>
    </el-card>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>热门商品销售</span>
        </div>
      </template>
      <el-table :data="topProduct" style="width: 100%">
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="销售数量" width="120" />
        <el-table-column prop="revenue" label="销售额" width="120" />
        <el-table-column prop="percentage" label="占比" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const dateRange = ref([])
const totalOrders = ref(0)
const totalSales = ref(0)
const pendingOrders = ref(0)
const completedOrders = ref(0)
const topProduct = ref([])

// 模拟数据
const mockTopProduct = [
  { productId: 'P001', productName: '智能手机A', quantity: 156, revenue: 46800, percentage: '28%' },
  { productId: 'P002', productName: '笔记本电脑B', quantity: 89, revenue: 44500, percentage: '22%' },
  { productId: 'P003', productName: '平板电脑C', quantity: 123, revenue: 36900, percentage: '18%' },
  { productId: 'P004', productName: '智能手表D', quantity: 210, revenue: 21000, percentage: '10%' },
  { productId: 'P005', productName: '无线耳机E', quantity: 345, revenue: 17250, percentage: '8%' }
]

// 生命周期
onMounted(() => {
  loadStatistics()
})

// 方法
const loadStatistics = () => {
  // 模拟从API加载统计数据
  setTimeout(() => {
    totalOrders.value = 1286
    totalSales.value = 258000
    pendingOrders.value = 156
    completedOrders.value = 1024
    topProduct.value = mockTopProduct
  }, 500)
}

const handleDateRangeChange = (value) => {
  if (value) {
    // 根据日期范围重新加载数据
    loadStatistics()
    ElMessage.success('已更新日期范围内的统计数据')
  }
}
</script>

<style scoped>
.order-statistics-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.stats-overview {
  padding: 20px 0;
}

.stat-card {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.placeholder-chart {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  border-radius: 4px;
  color: #909399;
  font-size: 16px;
}
</style>