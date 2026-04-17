<template>
  <div class="sales-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="rounded-button">查询</el-button>
          <el-button type="primary" @click="handleReset" class="rounded-button">重置</el-button>
          <el-button type="success" @click="handleExport" class="rounded-button export-button">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card class="stat-card sales-card" shadow="hover">
          <div class="stat-item">
            <div class="icon-wrapper">
              <i class="icon icon-sales"></i>
            </div>
            <div class="label">总销售额</div>
            <div class="value">¥{{ totalAmount.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card orders-card" shadow="hover">
          <div class="stat-item">
            <div class="icon-wrapper">
              <i class="icon icon-orders"></i>
            </div>
            <div class="label">总订单数</div>
            <div class="value">{{ totalOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card average-card" shadow="hover">
          <div class="stat-item">
            <div class="icon-wrapper">
              <i class="icon icon-average"></i>
            </div>
            <div class="label">平均客单价</div>
            <div class="value">¥{{ averageAmount.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 销售明细表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>销售明细</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        :summary-method="getSummary"
        show-summary
      >
        <el-table-column prop="date" label="日期" width="180" />
        <el-table-column prop="orderCount" label="订单数" align="right" />
        <el-table-column prop="totalAmount" label="销售额" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="averageAmount" label="客单价" align="right">
          <template #default="{ row }">
            ¥{{ row.averageAmount.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSalesList } from '@/api/sales'
import { exportToExcel } from '@/utils/export'

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 搜索条件
const dateRange = ref([])
const loading = ref(false)

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length > 0) {
    console.log('通过watch捕获到日期范围变化:', newVal)
    handleSearch()
  }
}, { immediate: false })

// 表格数据
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算统计数据
const totalAmount = computed(() => {
  return tableData.value.reduce((sum, item) => sum + item.totalAmount, 0)
})

const totalOrders = computed(() => {
  return tableData.value.reduce((sum, item) => sum + item.orderCount, 0)
})

const averageAmount = computed(() => {
  return totalOrders.value ? totalAmount.value / totalOrders.value : 0
})

// 表格合计行
const getSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    const values = data.map(item => Number(item[column.property]))
    if (!values.every(value => isNaN(value))) {
      sums[index] = values.reduce((prev, curr) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        } else {
          return prev
        }
      }, 0)
      if (column.property === 'totalAmount' || column.property === 'averageAmount') {
        sums[index] = '¥' + sums[index].toFixed(2)
      }
    } else {
      sums[index] = '--'
    }
  })
  return sums
}

// 加载表格数据
const loadTableData = async () => {
  console.log('开始加载表格数据...')
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value || []
    console.log('发送请求参数:', { startDate, endDate, pageNum: currentPage.value, pageSize: pageSize.value })
    const res = await getSalesList({
      startDate,
      endDate,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取销售明细失败:', error)
    ElMessage.error('获取销售明细失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  console.log('执行查询，日期范围:', dateRange.value)
  currentPage.value = 1
  loadTableData()
}

// 重置
const handleReset = () => {
  dateRange.value = []
  handleSearch()
}

// 导出
const handleExport = async () => {
  try {
    const [startDate, endDate] = dateRange.value || []
    const data = await getSalesList({
      startDate,
      endDate,
      pageSize: total.value
    })
    if (data.data?.records) {
      const exportData = data.data.records.map(item => ({
        日期: item.date,
        订单数: item.orderCount,
        销售额: `¥${item.totalAmount.toFixed(2)}`,
        客单价: `¥${item.averageAmount.toFixed(2)}`
      }))
      exportToExcel(exportData, '销售明细')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadTableData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTableData()
}

// 初始化
handleSearch()
</script>

<style lang="scss" scoped>
.sales-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: none;
  }

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.3s ease;
      position: relative;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .stat-item {
        text-align: center;
        padding: 30px 20px;
        position: relative;
        z-index: 2;

        .icon-wrapper {
          width: 60px;
          height: 60px;
          margin: 0 auto 20px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 255, 255, 0.9);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          position: relative;
          
          &::before {
            content: '';
            position: absolute;
            top: -2px;
            left: -2px;
            right: -2px;
            bottom: -2px;
            border-radius: 50%;
            background: linear-gradient(45deg, transparent, transparent 40%, rgba(255, 255, 255, 0.3));
            z-index: -1;
          }
          
          .icon {
            width: 30px;
            height: 30px;
            display: inline-block;
            background-size: contain;
            background-repeat: no-repeat;
            background-position: center;
          }
          
          .icon-sales::before {
            content: '💰';
            font-size: 24px;
          }
          
          .icon-orders::before {
            content: '📋';
            font-size: 24px;
          }
          
          .icon-average::before {
            content: '🎯';
            font-size: 24px;
          }
        }

        .label {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.9);
          margin-bottom: 10px;
          font-weight: 500;
        }

        .value {
          font-size: 28px;
          font-weight: bold;
          color: #ffffff;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          transition: transform 0.3s ease;
        }
        
        &:hover .value {
          transform: scale(1.05);
        }
      }
      
      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, transparent 0%, rgba(255, 255, 255, 0.1) 100%);
        pointer-events: none;
      }
    }
    
    .sales-card {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
      border: none;
    }
    
    .orders-card {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
      border: none;
    }
    
    .average-card {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
      border: none;
    }
  }

  .table-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    border: none;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      /* 将标题文字设置为白色 */
      span {
        color: white !important;
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
      
      /* 为分页组件内部元素添加间隙 */
      ::v-deep .el-pagination {
        display: flex;
        gap: 10px;
      }
      
      /* 为按钮和页码元素添加直接的边距 */
      // ::v-deep .btn-prev,
      // ::v-deep .btn-next,
      // ::v-deep .number {
      //   margin: 0 5px;
      // }
      
      /* 修改分页相关元素的背景颜色和边框 */
      ::v-deep .el-select__wrapper {
        background-color: #48494c !important;
        border: 1px solid white !important;
        outline: none !important;
        box-shadow: none !important;
      }
      
      /* 彻底移除选择器所有状态下的蓝色边框 */
      ::v-deep .el-select__wrapper:focus,
      ::v-deep .el-select__wrapper:focus-within,
      ::v-deep .el-select__wrapper.is-focus,
      ::v-deep .el-select__wrapper:hover {
        outline: none !important;
        box-shadow: none !important;
        border-color: white !important;
        border: 1px solid white !important;
      }
      
      /* 为选择器中的文字设置白色 */
      ::v-deep .el-select__placeholder {
        color: white !important;
      }
      
      /* 将分页中的Total和Go to文字修改为白色 */
      ::v-deep .el-pagination__total,
      ::v-deep .el-pagination__goto {
        color: white !important;
      }
      
      ::v-deep .btn-prev,
      ::v-deep .btn-next {
        background-color: #48494c !important;
        border: 1px solid white !important;
        color: white !important;
        outline: none !important;
        box-shadow: none !important;
      }
      
      /* 移除按钮点击时的蓝色边框 */
      ::v-deep .btn-prev:focus,
      ::v-deep .btn-next:focus,
      ::v-deep .btn-prev:hover,
      ::v-deep .btn-next:hover {
        outline: none !important;
        box-shadow: none !important;
        border-color: white !important;
      }
      
      ::v-deep .number {
        background-color: #48494c !important;
        border: 1px solid white !important;
        color: white !important;
        outline: none !important;
        box-shadow: none !important;
      }
      
      /* 移除页码按钮点击时的蓝色边框 */
      ::v-deep .number:focus,
      ::v-deep .number:hover {
        outline: none !important;
        box-shadow: none !important;
        border-color: white !important;
      }
      
      ::v-deep .el-input__wrapper {
        background-color: #48494c !important;
        border: 1px solid white !important;
        outline: none !important;
        box-shadow: none !important;
      }
      
      /* 彻底移除输入框容器所有状态下的蓝色边框 */
      ::v-deep .el-input__wrapper:focus,
      ::v-deep .el-input__wrapper:focus-within,
      ::v-deep .el-input__wrapper:hover {
        outline: none !important;
        box-shadow: none !important;
        border-color: white !important;
      }
      
      /* 为输入框内的文字设置白色并移除边框 */
      ::v-deep .el-input__inner {
        color: white !important;
        background-color: #48494c !important;
        // border: 1px solid white !important;
        outline: none !important;
        box-shadow: none !important;
      }
      
      /* 彻底移除输入框本身所有状态下的蓝色边框 */
      ::v-deep .el-input__inner:focus,
      ::v-deep .el-input__inner:hover {
        outline: none !important;
        box-shadow: none !important;
        border-color: white !important;
      }
      
      /* 额外覆盖可能的全局焦点样式 */
      ::v-deep *:focus {
        outline: none !important;
        box-shadow: none !important;
      }
    }
    
    /* 为表格内部的div区域设置背景渐变 */
    ::v-deep .el-table {
      background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    }
    
    ::v-deep .el-table__inner-wrapper {
      background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    }
    
    ::v-deep .el-table__header-wrapper,
    ::v-deep .el-table__body-wrapper,
    ::v-deep .el-table__footer-wrapper {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    }
    
    /* 确保表格单元格也使用相同的背景 */
    ::v-deep .el-table__cell {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      color: white;
    }
    
    /* 确保表头文字颜色为白色 */
    ::v-deep .el-table__header th {
      color: white !important;
    }
  }
  
  /* 使用深度选择器将时间范围标签文字修改为白色 */
  .search-card {
    ::v-deep .el-form-item__label {
      color: white !important;
    }
    
    /* 修改时间范围选择器的背景颜色 */
    ::v-deep .el-date-editor {
      background-color: #48494c !important;
      border-color: #ffffff !important;
    }
    
    /* 修改输入框的文字颜色 */
    ::v-deep .el-range-input {
      color: white !important;
    }
    
    /* 修改分隔符的颜色 */
    ::v-deep .el-range-separator {
      color: white !important;
    }
    
    /* 修改鼠标点击时的边框颜色 */
    ::v-deep .el-date-editor:focus-within {
      border-color: white !important;
      box-shadow: 0 0 0 1px white inset !important;
    }
  }
  
  /* 为导出按钮设置特定文字颜色 - 提高优先级 */
  // :global(.el-button.export-button) {
  //   color: #67C23A !important;
  // }
  
  /* 直接针对按钮内的span元素设置颜色 */
  :global(.el-button.export-button span) {
    color: #67C23A !important;
  }
  
  /* 全局样式，使用更强的选择器确保样式生效 */
  /* 为按钮设置背景颜色 - 使用全局选择器确保样式生效 */
  :global(.rounded-button) {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important;
    transition: all 0.3s ease !important;
  }
  
  /* 为按钮添加鼠标悬停效果 */
  :global(.rounded-button:hover:not(.is-disabled)) {
    background-color: #5a5b5f !important;
    color: #274151 !important;
    border-color: #ffffff !important;
  }
  
  :global(.el-picker-panel__sidebar) {
    background-color: #48494c !important;
    border-right-color: #5a5b5f !important;
  }
  
  :global(.el-picker-panel__shortcut) {
    background-color: #48494c !important;
    color: #e4b23d !important;
  }
  
  :global(.el-picker-panel__shortcut:hover) {
      background-color: #5a5b5f !important;
    }
    
    /* 为按钮设置背景颜色 */
    .rounded-button {
      background-color: #ffffff !important;
      color: #274151 !important;
      border-color: #ffffff !important;
      transition: all 0.3s ease !important;
    }
    
    /* 为按钮添加鼠标悬停效果 */
    .rounded-button:hover:not(.is-disabled) {
      background-color: #5a5b5f !important;
      color: #274151 !important;
      border-color: #ffffff !important;
    }
  
  /* 覆盖更多可能影响的样式 */
  :global(.el-date-editor .el-input__wrapper) {
    background-color: #48494c !important;
  }
  
  /* 确保弹出的面板样式 */
  :global(.el-picker-panel) {
    background-color: #48494c !important;
  }
  
  :global(.el-picker-panel .el-picker-panel__sidebar) {
    background-color: #48494c !important;
  }
  
  /* 添加额外的选择器来确保样式覆盖 */
  :global(.el-calendar) {
    background-color: #48494c !important;
  }
  
  :global(.el-calendar__header) {
    background-color: #48494c !important;
  }
}
</style>