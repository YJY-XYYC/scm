<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in gaugeCards" :key="index">
        <el-card 
          :class="[
            'stat-card', 
            index === 0 ? 'user-card' : 
            index === 1 ? 'order-card' : 
            index === 2 ? 'product-card' : 'sales-card'
          ]" 
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ item.title }}</span>
              <div class="icon-circle">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
            </div>
          </template>
          <div class="gauge-chart" :ref="el => gaugeRefs[item.ref] = el"></div>
          <div class="compare" :class="{ 'is-up': statistics[item.increase] > 0 }">
            较上周 {{ Math.abs(statistics[item.increase]) }}%
            <el-icon>
              <component :is="statistics[item.increase] > 0 ? ArrowUp : ArrowDown" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 销售趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <div class="chart-actions">
            <el-radio-group v-model="salesTrendType" size="small" @change="handleSalesTrendTypeChange" id="sales-trend-radio-group">
              <el-radio-button value="week">周</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
            </el-radio-group>
            <!-- 周模式和月模式都显示日期范围选择器 -->
            <el-date-picker
              v-model="salesDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="margin-left: 10px"
              @change="handleDateRangeChange"
              :picker-options="salesTrendType === 'week' ? datePickerOptions : null"
            />
          </div>
            </div>
          </template>
          <div ref="salesChartRef" class="chart"></div>
        </el-card>
      </el-col>

      <!-- 订单趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
              <div class="chart-actions">
                <el-radio-group v-model="orderTrendType" size="small" id="sales-trend-radio-group">
                  <el-radio-button value="week">周</el-radio-button>
                  <el-radio-button value="month">月</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="orderTrendChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品分类分布 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">商品分类分布</span>
        </div>
      </template>
      <div class="chart-container">
        <div class="chart-wrapper">
          <h4 class="chart-title">柱状图视图</h4>
          <div ref="categoryChartRef" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <h4 class="chart-title">旭日图视图</h4>
          <div ref="categorySunburstChartRef" class="sunburst-chart"></div>
        </div>
      </div>
    </el-card>

    <!-- 热销商品TOP10 -->
    <el-card class="rank-card">
      <template #header>
        <div class="card-header">
          <span>热销商品TOP10</span>
          <div class="chart-actions">
              <div class="custom-radio-group" style="display: flex; margin-top: 20px;">
                <button 
                  @click="topProductType = 'week'; handleTopProductTypeChange('week')"
                  @mouseenter="hoveredButton = 'week'"
                  @mouseleave="hoveredButton = null"
                  :style="{
                    padding: '8px 16px',
                    border: '1px solid #ffffff',
                    outline: 'none',
                    backgroundColor: topProductType === 'week' ? (hoveredButton === 'week' ? '#5a5b5f' : '#ffffff') : (hoveredButton === 'week' ? '#5a5b5f' : '#48494c'),
                    color: topProductType === 'week' ? '#274151' : '#ffffff',
                    cursor: 'pointer',
                    fontSize: '14px',
                    borderRight: '1px solid #ffffff',
                    borderTopLeftRadius: '4px',
                    borderBottomLeftRadius: '4px',
                    boxShadow: 'none',
                    position: (topProductType === 'week' || hoveredButton === 'week') ? 'relative' : 'static',
                    zIndex: (topProductType === 'week' || hoveredButton === 'week') ? '1' : 'auto'
                  }"
                >周</button>
                <button 
                  @click="topProductType = 'month'; handleTopProductTypeChange('month')"
                  @mouseenter="hoveredButton = 'month'"
                  @mouseleave="hoveredButton = null"
                  :style="{
                    padding: '8px 16px',
                    border: '1px solid #ffffff',
                    outline: 'none',
                    backgroundColor: topProductType === 'month' ? (hoveredButton === 'month' ? '#5a5b5f' : '#ffffff') : (hoveredButton === 'month' ? '#5a5b5f' : '#48494c'),
                    color: topProductType === 'month' ? '#274151' : '#ffffff',
                    cursor: 'pointer',
                    fontSize: '14px',
                    borderRight: '1px solid #ffffff',
                    marginLeft: '-1px',
                    boxShadow: 'none',
                    position: (topProductType === 'month' || hoveredButton === 'month') ? 'relative' : 'static',
                    zIndex: (topProductType === 'month' || hoveredButton === 'month') ? '1' : 'auto'
                  }"
                >月</button>
                <button 
                  @click="topProductType = 'year'; handleTopProductTypeChange('year')"
                  @mouseenter="hoveredButton = 'year'"
                  @mouseleave="hoveredButton = null"
                  :style="{
                    padding: '8px 16px',
                    border: '1px solid #ffffff',
                    outline: 'none',
                    backgroundColor: topProductType === 'year' ? (hoveredButton === 'year' ? '#5a5b5f' : '#ffffff') : (hoveredButton === 'year' ? '#5a5b5f' : '#48494c'),
                    color: topProductType === 'year' ? '#274151' : '#ffffff',
                    cursor: 'pointer',
                    fontSize: '14px',
                    borderTopRightRadius: '4px',
                    borderBottomRightRadius: '4px',
                    marginLeft: '-1px',
                    boxShadow: 'none',
                    position: (topProductType === 'year' || hoveredButton === 'year') ? 'relative' : 'static',
                    zIndex: (topProductType === 'year' || hoveredButton === 'year') ? '1' : 'auto'
                  }"
                >年</button>
              </div>
            </div>
        </div>
      </template>
      <el-table :data="statistics.topProduct" stripe :row-class-name="tableRowClassName" @sort-change="handleSortChange" style="max-height: 600px; overflow-y: auto;">
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="salesCount" label="销售数量" align="right" sortable="custom" />
        <el-table-column prop="salesAmount" label="销售金额" align="right" sortable="custom">
          <template #default="{ row }">
            ¥{{ row.salesAmount ? row.salesAmount.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 最近活动 -->
    <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
          :type="getActivityType(activity.type)"
          :timestamp="formatTime(activity.time)"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { User, List, Goods, Money, ShoppingCart, Refresh, Download, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatistics, getOrderTrend, getUserDistribution, getCategoryDistribution, getRecentActivities, getSalesTrend } from '@/api/dashboard'
import { exportToExcel } from '@/utils/export'
import { ElMessage } from 'element-plus'
import CountTo from '@/components/CountTo.vue'

// 加载状态
const loading = ref(false)

// 定时器
const activitiesTimer = ref(null)
const refreshTimer = ref(null)

// 自动刷新间隔（秒）
const refreshInterval = ref(30)

// 图表实例和引用
const salesChartRef = ref(null)
const orderTrendChartRef = ref(null)
const categoryChartRef = ref(null)
const categorySunburstChartRef = ref(null)
const salesChart = ref(null)
const orderTrendChart = ref(null)
const categoryChart = ref(null)
const categorySunburstChart = ref(null)

// 图表类型
const salesTrendType = ref('week')
const orderTrendType = ref('week')
const topProductType = ref('week')
const hoveredButton = ref(null)

// 热销商品日期范围


// 统计数据
const statistics = ref({
  userCount: 0,
  userIncrease: 0,
  orderCount: 0,
  orderIncrease: 0,
  productCount: 0,
  productIncrease: 0,
  totalSales: 0,
  salesIncrease: 0,
  topProduct: []
})

// 原始排序前的商品数据备份
const originalTopProduct = ref([])

// 处理表格排序（保持空行在最后）
const handleSortChange = ({ prop, order }) => {
  if (!statistics.value.topProduct.length) return
  
  if (!originalTopProduct.value.length) {
    // 首次排序时备份原始数据
    originalTopProduct.value = [...statistics.value.topProduct]
  }
  
  // 分离有数据的行和空行
  const hasDataRows = statistics.value.topProduct.filter(row => 
    row.productName || row.salesCount > 0 || row.salesAmount > 0
  )
  const emptyRows = statistics.value.topProduct.filter(row => 
    !row.productName && row.salesCount === 0 && row.salesAmount === 0
  )
  
  let sortedData
  
  if (order === 'ascending') {
    // 对有数据的行进行排序
    const sortedHasData = [...hasDataRows].sort((a, b) => Number(a[prop]) - Number(b[prop]))
    // 合并排序后的数据行和空行
    sortedData = [...sortedHasData, ...emptyRows]
  } else if (order === 'descending') {
    // 对有数据的行进行排序
    const sortedHasData = [...hasDataRows].sort((a, b) => Number(b[prop]) - Number(a[prop]))
    // 合并排序后的数据行和空行
    sortedData = [...sortedHasData, ...emptyRows]
  } else {
    // 如果取消排序，恢复原始数据
    sortedData = [...originalTopProduct.value]
    
    // 确保恢复后仍然有10行
    if (sortedData.length < 10) {
      const emptyRows = 10 - sortedData.length
      for (let i = 0; i < emptyRows; i++) {
        sortedData.push({
          productName: '',
          salesCount: 0,
          salesAmount: 0
        })
      }
    }
  }
  
  statistics.value.topProduct = sortedData
}

// 最近活动
const recentActivities = ref([])

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 使用默认参数，让后端根据类型返回相应时间范围的数据
    const res = await getStatistics(topProductType.value)
    if (res.data) {
      // 字段映射：将后端返回的字段转换为前端表格所需的字段
      let topProduct = res.data.topProduct?.map(item => ({
        productName: item.name || '',
        salesCount: item.quantity || 0,
        salesAmount: item.amount || 0,
        // 保留原始字段，确保不会丢失信息
        ...item
      })) || []
      
      // 从所有数据中挑出前十个
      topProduct = topProduct.slice(0, 10)
      
      // 确保始终显示10行，不足则补充空对象
      if (topProduct.length < 10) {
        const emptyRows = 10 - topProduct.length
        for (let i = 0; i < emptyRows; i++) {
          topProduct.push({
            productName: '',
            salesCount: 0,
            salesAmount: 0
          })
        }
      }
      
      const mappedData = {
        ...res.data,
        topProduct
      }
      
      statistics.value = {
        ...statistics.value,
        ...mappedData
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 加载最近活动
const loadRecentActivities = async () => {
  try {
    const res = await getRecentActivities()
    if (res.data) {
      recentActivities.value = res.data
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
  }
}

// 初始化图表
const initCharts = async () => {
  await Promise.all([
    initSalesChart(),
    initOrderTrendChart(),
    initCategoryChart()
  ])
}

// 定义响应式状态
const salesDateRange = ref([])

// 日期选择器选项
const datePickerOptions = {
  // 周模式下允许选择任意连续的7天
  onPick: ({ maxDate, minDate }) => {
    if (minDate && !maxDate) {
      // 当选择开始日期后，限制只能选择开始日期当天及之后的6天作为结束日期（共7天）
      // 允许往前或往后选择，只要求必须是连续的7天
      const sixDaysLater = new Date(minDate)
      sixDaysLater.setDate(sixDaysLater.getDate() + 6)
      datePickerOptions.disabledDate = (time) => {
        // 只允许选择开始日期当天及之后的6天
        return time.getTime() < minDate.getTime() || time.getTime() > sixDaysLater.getTime()
      }
    }
    if (maxDate) {
      // 清除限制
      datePickerOptions.disabledDate = null
    }
  }
}

// 初始化销售趋势图
const initSalesChart = async () => {
  try {
    // 添加调试信息
    console.log('开始初始化销售趋势图，类型:', salesTrendType.value)
    
    if (!salesChartRef.value) {
      console.error('销售图表容器不存在')
      return
    }
    
    // 获取数据并打印API返回结果
    let res
    if (salesDateRange.value.length === 2) {
      // 无论周模式还是月模式，只要有日期范围就使用
      const [start, end] = salesDateRange.value
      res = await getSalesTrend(salesTrendType.value, start, end)
      console.log('使用自定义日期范围获取数据:', { start, end, type: salesTrendType.value })
    } else {
      // 没有选择日期范围时，月模式获取全年数据，周模式调用默认参数（让后端处理本周数据）
      if (salesTrendType.value === 'month') {
        // 计算本年第一天和本年最后一天
        const currentDate = new Date()
        const currentYear = currentDate.getFullYear()
        const yearStart = new Date(currentYear, 0, 1) // 本年1月1日
        const yearEnd = new Date(currentYear, 11, 31) // 本年12月31日
        
        // 格式化日期为YYYY-MM-DD
        const formatDate = (date) => {
          const year = date.getFullYear()
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          return `${year}-${month}-${day}`
        }
        
        const startDate = formatDate(yearStart)
        const endDate = formatDate(yearEnd)
        
        res = await getSalesTrend(salesTrendType.value, startDate, endDate)
        console.log('月模式默认获取全年数据:', { startDate, endDate })
      } else {
        // 周模式调用默认参数，让后端返回本周数据
        res = await getSalesTrend(salesTrendType.value)
        console.log('周模式默认获取本周数据')
      }
    }
    console.log('API返回数据:', res)
    
    // 如果实例已存在，先销毁
    if (salesChart.value) {
      salesChart.value.dispose()
    }
    salesChart.value = echarts.init(salesChartRef.value)
    
    // 处理API返回的真实数据
    let xAxisData = []
    let seriesData = []
    
    // 辅助函数：获取数据中的字段值
    const getFieldValue = (item, fieldNames, defaultValue = null) => {
      for (const field of fieldNames) {
        if (item && item.hasOwnProperty(field) && item[field] !== null && item[field] !== undefined) {
          return item[field]
        }
      }
      return defaultValue
    }
    
    // 辅助函数：解析日期
    const parseDate = (dateStr) => {
      const date = new Date(dateStr)
      return isNaN(date.getTime()) ? null : date
    }
    
    // 增强的数据处理逻辑
    try {
      if (res && res.code === 200) {
        // 统一的数据结构检查
        let chartData = []
        const dataSources = [res.data, res.data?.result, res.data?.list]
        
        for (const source of dataSources) {
          if (Array.isArray(source) && source.length > 0) {
            chartData = source
            break
          }
        }
        
        if (chartData.length > 0) {
          // 统一获取销售额数据
          seriesData = chartData.map(item => {
            const value = getFieldValue(item, ['sales', 'amount', 'value', 'total', 'count'], 0)
            return typeof value === 'number' ? value : Number(value) || 0
          })
          
          // 根据模式处理X轴数据
          if (salesTrendType.value === 'week') {
            // 周数据处理优化
            xAxisData = chartData.map((item, index) => {
              const dayData = getFieldValue(item, ['date', 'day', 'time'], index)
              const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
              
              if (typeof dayData === 'number') {
                return weekdays[dayData % 7] || `第${dayData}天`
              } else if (typeof dayData === 'string') {
                // 增强的日期解析
                const date = parseDate(dayData)
                if (date) {
                  return weekdays[date.getDay()]
                }
                // 处理可能的其他格式，如"Monday"或"周一"
                const dayStr = dayData.toLowerCase()
                const enWeekdays = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday']
                const enIndex = enWeekdays.indexOf(dayStr)
                if (enIndex !== -1) {
                  return weekdays[enIndex]
                }
                return dayData
              }
              return String(dayData)
            })
          } else {
            // 月数据处理优化 - 按1-12月顺序显示
            // 先提取并处理每个数据项的月份信息
            const monthDataArray = chartData.map((item, index) => {
              // 支持更多可能的字段名，优先获取month字段
              const monthData = getFieldValue(item, ['month', 'date', 'time'])
              const value = seriesData[index]
              let monthNum = 0
              
              // 解析月份数字
              if (typeof monthData === 'string') {
                // 处理YYYY-MM-DD格式，提取月
                if (monthData.match(/^\d{4}-(\d{2})/)) {
                  monthNum = parseInt(monthData.split('-')[1])
                }
                // 处理YYYY-MM格式，作为整月显示
                else if (monthData.match(/^\d{4}-\d{2}$/)) {
                  monthNum = parseInt(monthData.split('-')[1])
                }
                // 处理"X月"格式
                else if (monthData.match(/^(\d+)月$/)) {
                  monthNum = parseInt(monthData.match(/^(\d+)月$/)[1])
                }
              } else {
                monthNum = parseInt(monthData)
              }
              
              return {
                monthNum: isNaN(monthNum) ? 0 : monthNum,
                monthLabel: `${monthNum}月`,
                value: value
              }
            })
            
            // 按月份数字排序（1-12月）
            monthDataArray.sort((a, b) => a.monthNum - b.monthNum)
            
            // 生成排序后的X轴数据和系列数据
            xAxisData = monthDataArray.map(item => item.monthLabel)
            seriesData = monthDataArray.map(item => item.value)
          }
          
          console.log('处理后的数据:', { xAxisData, seriesData })
          
          // 增强的数据有效性检查
          const hasValidData = seriesData.some(v => v > 0 && !isNaN(v))
          if (!hasValidData || xAxisData.length === 0) {
            console.warn('数据无效或全为0，使用默认演示数据')
            throw new Error('Invalid or empty data')
          }
        } else {
          console.warn('API返回空数据数组，使用默认数据')
          throw new Error('Empty data array')
        }
      } else {
        console.warn('API返回格式异常:', res)
        throw new Error('Invalid API response')
      }
    } catch (dataError) {
      console.info('使用默认测试数据显示图表:', dataError.message)
      // 生成更真实的默认数据
      if (salesTrendType.value === 'week') {
        // 模拟一周的销售波动数据
        xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        // 基于真实业务场景的合理波动
        seriesData = [180, 210, 230, 200, 250, 320, 260]
      } else {
          // 模拟月度销售趋势数据 - 只关注月份
          xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          // 生成基于月份的模拟数据
          seriesData = []
          for (let i = 0; i < 12; i++) {
            // 生成有季节波动的月度数据
            const baseValue = 10000
            const seasonalFactor = 1 + Math.sin((i - 2) * Math.PI / 6) * 0.3 // 季节波动
            const trendFactor = 1 + i * 0.05 // 增长趋势
            const randomFactor = 1 + (Math.random() - 0.5) * 0.2 // 随机波动
            seriesData.push(Math.floor(baseValue * seasonalFactor * trendFactor * randomFactor))
          }
      }
    }
    
    // 最终的安全检查
    if (xAxisData.length === 0 || seriesData.length === 0) {
      console.error('数据数组为空，使用默认数据')
      if (salesTrendType.value === 'week') {
        xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        seriesData = [150, 230, 224, 218, 135, 147, 260]
      } else {
        // 只关注月份
        xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
        seriesData = [12000, 15000, 18000, 14000, 16000, 19000, 21000]
      }
    }
    
    const option = {
      title: {
        text: '销售趋势',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'normal',
          color: '#ffffff'  // 标题文字改为白色
        }
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(64, 64, 64, 0.8)',  // 提示框背景色
        borderColor: 'transparent',  // 移除提示框边框
        textStyle: {
          color: '#ffffff'  // 提示框文字颜色
        },
        formatter: function(params) {
          if (!params || !params[0]) return '无数据';
          return `${params[0].name}<br/>销售额: ¥${params[0].value ? params[0].value.toFixed(2) : '0.00'}`;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',  // 增加底部空间，避免标签被截断
        top: '15%',     // 增加顶部空间
        containLabel: true,
        backgroundColor: 'transparent'  // 网格背景透明
      },
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLabel: {
          interval: 0,  // 显示所有标签
          rotate: 0,    // 水平显示
          color: '#ffffff'  // X轴标签文字改为白色
        },
        axisLine: {
          lineStyle: {
            color: '#ffffff'  // 移除X轴线
          }
        },
        axisTick: {
          lineStyle: {
            color: '#ffffff'  // 移除X轴刻度线
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '销售额(元)',
        nameTextStyle: {
          color: '#ffffff'  // Y轴名称文字颜色改为白色
        },
        axisLabel: {
          color: '#ffffff',  // Y轴标签文字改为白色
          formatter: function(value) {
            return '¥' + value;
          }
        },
        axisLine: {
          lineStyle: {
            color: '#ffffff'  // 移除Y轴线
          }
        },
        axisTick: {
          lineStyle: {
            color: '#ffffff'  // 移除Y轴刻度线
          }
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(255, 255, 255, 0.2)'  // 网格线颜色改为半透明白色
          }
        }
      },
      series: [{
        name: '销售额',
        type: 'line',
        data: seriesData,
        smooth: true,
        symbol: 'circle',       // 显示数据点
        symbolSize: 6,          // 数据点大小
        itemStyle: {
          color: '#ffffff'  // 数据点颜色改为白色
        },
        lineStyle: {
          color: '#ffffff'  // 线条颜色改为白色
        },
        label: {
          show: true,           // 显示数值标签
          position: 'top',      // 标签位置
          formatter: function(params) {
            return '¥' + (params.value ? params.value.toFixed(0) : '0');
          },
          fontSize: 10,
          color: '#ffffff'  // 标签文字颜色改为白色
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(255, 255, 255, 0.3)'  // 区域填充上部颜色改为半透明白色
            }, {
              offset: 1, color: 'rgba(255, 255, 255, 0)'  // 区域填充下部颜色改为透明
            }]
          }
        }
      }]
    }
    
    // 设置图表配置
    salesChart.value.setOption(option)
    
    // 添加窗口大小变化时的重新渲染
    window.addEventListener('resize', handleResize)
    
    console.log('销售趋势图初始化完成')
  } catch (error) {
    console.error('初始化销售趋势图表失败:', error)
    // 即使出错也创建一个基本图表，避免空白
    if (salesChartRef.value && !salesChart.value) {
      try {
        salesChart.value = echarts.init(salesChartRef.value)
        salesChart.value.setOption({
          title: {
            text: '数据加载失败',
            left: 'center'
          },
          tooltip: {},
          xAxis: { data: ['暂无数据'] },
          yAxis: {},
          series: [{ type: 'line', data: [0] }]
        })
      } catch (initError) {
        console.error('创建基础图表失败:', initError)
      }
    }
  }
}

// 初始化订单趋势图
const initOrderTrendChart = async () => {
  try {
    // 使用当前的模式类型，而不是硬编码的'week'
    const res = await getOrderTrend(orderTrendType.value)
    if (res.code === 200) {
      // 根据模式类型设置不同的日期标签
      const xAxisData = orderTrendType.value === 'week'
        ? ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        : ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

      // 设置颜色
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8E44AD', '#2ECC71']
      
      // 处理数据，确保即使没有数据也有默认显示
      let chartData = []
      if (res.data && res.data.length > 0) {
        chartData = res.data.map((item, index) => {
          let name = '未知'
          
          if (orderTrendType.value === 'week' && item.date) {
            // 将日期字符串转换为星期几
            const date = new Date(item.date)
            // 调整：JavaScript中getDay()返回0-6，0是周日，需要转换索引
            const dayIndex = date.getDay() // 0=周日, 1=周一, ..., 6=周六
            name = xAxisData[dayIndex] || item.date
          } else if (orderTrendType.value === 'month') {
            // 优化月模式数据处理，支持多种可能的字段名和格式
            let monthValue = null;
            
            // 尝试不同的可能的月份字段名
            if (item.month) {
              monthValue = item.month;
            } else if (item.date) {
              monthValue = item.date;
            } else if (item.name) {
              monthValue = item.name;
            }
            
            if (monthValue) {
              // 处理不同格式的月份数据
              let monthNumber = null;
              
              // 尝试从'YYYY-MM'格式中提取
              if (typeof monthValue === 'string' && monthValue.includes('-')) {
                const parts = monthValue.split('-');
                if (parts.length >= 2) {
                  monthNumber = parseInt(parts[1]);
                }
              }
              // 尝试直接转换为数字
              else if (!isNaN(monthValue)) {
                monthNumber = parseInt(monthValue);
              }
              // 尝试从中文月份格式中提取（如'9月'）
              else if (typeof monthValue === 'string' && monthValue.includes('月')) {
                const match = monthValue.match(/(\d+)月/);
                if (match && match[1]) {
                  monthNumber = parseInt(match[1]);
                }
              }
              
              // 如果成功提取到有效的月份数字
              if (monthNumber && monthNumber >= 1 && monthNumber <= 12) {
                name = `${monthNumber}月`;
              } else {
                // 如果无法解析为标准月份格式，直接使用原始值作为名称
                // 确保不会因为格式问题丢失数据
                name = String(monthValue);
              }
            }
          }
          
          // 获取数据值，支持多种可能的字段名
          let dataValue = 0;
          if (item.count !== undefined) {
            dataValue = item.count;
          } else if (item.value !== undefined) {
            dataValue = item.value;
          } else if (item.number !== undefined) {
            dataValue = item.number;
          } else if (item.quantity !== undefined) {
            dataValue = item.quantity;
          }
          
          // 确保值为数字类型
          dataValue = parseFloat(dataValue) || 0;
          
          return {
            name,
            value: dataValue,
            itemStyle: {
              color: colors[index % colors.length]
            }
          }
        })
      } else {
        // 如果没有数据，显示空状态，value设置为0
        chartData = [{ name: '暂无数据', value: 0, itemStyle: { color: '#ccc' } }]
      }

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          textStyle: {
            color: '#ffffff' // 设置提示框文字颜色为白色
          },
          backgroundColor: '#48494c' // 设置提示框背景颜色为主题色
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          textStyle: {
            color: '#ffffff' // 设置图例文字颜色为白色
          }
        },
        series: [
          {
            name: '订单数量',
            type: 'pie',
            radius: ['50%', '70%'],
            center: ['40%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: 'transparent',  // 移除白色边框
              borderWidth: 0  // 设置边框宽度为0
            },
            label: {
              show: true,
              formatter: '{b}: {c}单',
              color: '#ffffff' // 设置标签文字颜色为白色
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '16',
                fontWeight: 'bold',
                color: '#ffffff' // 设置悬停时标签文字颜色为白色
              }
            },
            labelLine: {
              show: true
            },
            data: chartData
          }
        ]
      }

      // 如果实例已存在，先销毁
      if (orderTrendChart.value) {
        orderTrendChart.value.dispose()
      }
      
      // 确保DOM元素存在
      if (orderTrendChartRef.value) {
        orderTrendChart.value = echarts.init(orderTrendChartRef.value)
        orderTrendChart.value.setOption(option)
        
        // 添加窗口大小变化时的重绘
        window.addEventListener('resize', handleOrderTrendChartResize)
      }
    }
  } catch (error) {
    console.error('初始化订单趋势图表失败:', error)
    ElMessage.error('初始化订单趋势图表失败')
  }
}

// 初始化商品分类分布柱状图
const initCategoryChart = async () => {
  try {
    const res = await getCategoryDistribution()
    if (!res.data || !Array.isArray(res.data)) {
      console.error('商品分类数据格式错误:', res.data)
      return
    }

    // 设置颜色
    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
    
    // 计算总数量用于计算占比
    const totalCount = res.data.reduce((sum, item) => sum + item.count, 0)
    
    // 计算每个分类的占比（百分比）
    const percentageData = res.data.map(item => (item.count / totalCount * 100).toFixed(1))

    const option = {
      textStyle: {
        color: '#ffffff'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#ffffff',
            borderColor: '#ffffff',
            borderWidth: 1
          }
        },
        backgroundColor: '#48494c',
        borderColor: 'transparent',
        textStyle: {
          color: '#ffffff'
        },
        formatter: function(params) {
          let result = params[0].name + '<br/>'
          params.forEach(param => {
            if (param.seriesName === '商品数量') {
              result += param.marker + param.seriesName + ': ' + param.value + '个<br/>'
            } else {
              result += param.marker + param.seriesName + ': ' + param.value + '%<br/>'
            }
          })
          return result
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: res.data.map(item => item.category),
        axisPointer: {
          type: 'shadow',
          label: {
            color: '#ffffff',
            backgroundColor: '#48494c',
            borderColor: '#ffffff',
            borderWidth: 1
          }
        },
        axisLabel: {
          interval: 0,
          rotate: 30,
          color: '#ffffff'
        },
        axisLine: {
          lineStyle: {
            color: '#48494c'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#ffffff'
          }
        }
      },
      yAxis: [
        {
          type: 'value',
          name: '商品数量',
          min: 0,
          nameTextStyle: {
            color: '#ffffff'
          },
          axisLabel: {
            formatter: '{value}个',
            color: '#ffffff'
          },
          axisLine: {
            lineStyle: {
              color: '#48494c'
            }
          },
          axisTick: {
            lineStyle: {
              color: '#ffffff'
            }
          },
          axisPointer: {
            label: {
              color: '#ffffff',
              backgroundColor: '#48494c',
              borderColor: '#ffffff',
              borderWidth: 1
            }
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.3)'
            }
          }
        },
        {
          type: 'value',
          name: '占比(%)',
          min: 0,
          max: 100,
          nameTextStyle: {
            color: '#ffffff'
          },
          axisLabel: {
            formatter: '{value}%',
            color: '#ffffff'
          },
          axisLine: {
            lineStyle: {
              color: '#48494c'
            }
          },
          axisTick: {
            lineStyle: {
              color: '#ffffff'
            }
          },
          axisPointer: {
            label: {
              color: '#ffffff',
              backgroundColor: '#48494c',
              borderColor: '#ffffff',
              borderWidth: 1
            }
          },
          splitLine: {
            show: false
          }
        }
      ],
      series: [
        {
          name: '商品数量',
          type: 'bar',
          data: res.data.map((item, index) => ({
            value: item.count,
            itemStyle: {
              color: colors[index % colors.length]
            }
          })),
          barWidth: '40%',
          itemStyle: {
            borderRadius: [5, 5, 0, 0]
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}个',
            color: '#ffffff'
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
              borderColor: '#ffffff',
              borderWidth: 2
            }
          }
        },
        {
          name: '占比',
          type: 'line',
          yAxisIndex: 1,
          data: percentageData,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            color: '#ffffff',
            width: 3
          },
          itemStyle: {
            color: '#ffffff',
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}%',
            color: '#ffffff'
          }
        }
      ]
    }

    // 如果实例已存在，先销毁
    if (categoryChart.value) {
      categoryChart.value.dispose()
    }
    categoryChart.value = echarts.init(categoryChartRef.value)
    categoryChart.value.setOption(option)
  } catch (error) {
    console.error('初始化商品分类分布图表失败:', error)
  }
}

// 初始化商品分类分布旭日图
const initCategorySunburstChart = async () => {
  try {
    const res = await getCategoryDistribution()
    if (!res.data || !Array.isArray(res.data)) {
      console.error('商品分类数据格式错误:', res.data)
      return
    }

    // 预设颜色数组，提供更丰富的视觉效果
    const colors = [
      '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
      '#FF9F40', '#8AC847', '#DDA0DD', '#6495ED', '#FF7F50'
    ]

    // 转换数据为旭日图格式，并为每个分类分配颜色
    const sunburstData = {
      name: '商品分类',
      value: res.data.reduce((sum, item) => sum + item.count, 0),
      children: res.data.map((item, index) => ({
        name: item.category,
        value: item.count,
        itemStyle: {
          color: colors[index % colors.length]
        }
      }))
    }

    // 计算总数用于百分比显示
    const totalCount = sunburstData.value

    const option = {
      textStyle: {
        color: '#ffffff'
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          if (params.data) {
            const percent = totalCount > 0 ? ((params.value / totalCount) * 100).toFixed(1) : '0.0'
            return `${params.data.name}<br/>数量: ${params.value} 个<br/>占比: ${percent}%`
          }
          return params.name
        },
        backgroundColor: '#48494c',
        borderColor: 'transparent',
        borderWidth: 0,
        textStyle: {
          color: '#ffffff'
        }
      },
      series: [
        {
          type: 'sunburst',
          data: [sunburstData],
          radius: ['25%', '75%'], // 扩大展示范围
          center: ['50%', '55%'], // 稍微向下调整，为中心标签留出空间,
          label: {
            show: true,
            color: '#ffffff'
          },
          emphasis: {
            focus: 'self',
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(0, 0, 0, 0.4)'
            },
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#ffffff'
            }
          },
          levels: [
            {
              // 中心层配置
              r0: '0%',
              r: '25%',
              itemStyle: {
                color: '#48494c',
                borderWidth: 0
              },
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold',
                color: '#ffffff',
                formatter: '总分类\n{sum}'
              }
            },
            {
              // 中间层配置 - 填充中心和最外层之间的区域
              r0: '25%',
              r: '50%',
              itemStyle: {
                borderWidth: 0,
                borderColor: 'transparent',
                // 使用半透明灰色填充中间区域
                color: 'rgba(200, 200, 200, 0.2)'
              },
              label: {
                show: true,
                rotate: 0,
                fontSize: 12,
                fontWeight: 'normal',
                color: '#333333',
                formatter: function(params) {
                  return params.data.name
                }
              }
            },
            {
              // 分类层配置（保持整体大小不变）
              r0: '50%',
              r: '75%',
              itemStyle: {
                borderWidth: 0,
                borderColor: 'transparent',
                // 添加渐变效果
                color: function(params) {
                  const colorList = [
                    ['#FF6384', '#FF8FA3'],
                    ['#36A2EB', '#69C0FF'],
                    ['#FFCE56', '#FFEAA7'],
                    ['#4BC0C0', '#7EE8E8'],
                    ['#9966FF', '#B39DDB'],
                    ['#FF9F40', '#FFB74D'],
                    ['#8AC847', '#AED959'],
                    ['#DDA0DD', '#E6B8E6'],
                    ['#6495ED', '#83BFF6'],
                    ['#FF7F50', '#FFA07A']
                  ]
                  const color = colorList[params.dataIndex % colorList.length]
                  return new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                    { offset: 0, color: color[0] },
                    { offset: 1, color: color[1] }
                  ])
                }
              },
              label: {
                show: true,
                rotate: 'tangential',
                fontSize: 12,
                fontWeight: 'normal',
                color: '#ffffff',
                formatter: function(params) {
                  // 只显示名称，不显示百分比
                  return params.data.name
                },
                // 文本阴影，提高可读性
                textShadowBlur: 2,
                textShadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          ],
          // 添加动画效果
          animation: true,
          animationDuration: 1000,
          animationEasing: 'cubicOut',
          // 添加鼠标悬停时的额外提示
          select: {
            itemStyle: {
              borderColor: '#1890FF',
              borderWidth: 3
            }
          },
          // 全局无边框配置
          lineStyle: {
            width: 0,
            color: 'transparent'
          }
        }
      ]
    }

    // 如果实例已存在，先销毁
    if (categorySunburstChart.value) {
      categorySunburstChart.value.dispose()
    }
    
    // 初始化图表前确保容器样式正确
    const chartContainer = categorySunburstChartRef.value
    if (chartContainer) {
      chartContainer.style.backgroundColor = 'transparent'
      chartContainer.style.border = 'none'
      chartContainer.style.outline = 'none'
    }
    
    categorySunburstChart.value = echarts.init(categorySunburstChartRef.value)
    // 强制设置所有文本样式为白色和无边框
    const whiteTextOption = {
      ...option,
      textStyle: { color: '#ffffff' },
      series: option.series.map(s => ({
        ...s,
        // 确保series级别无边框
        itemStyle: {
          ...s.itemStyle,
          borderWidth: 0,
          borderColor: 'transparent',
          stroke: 'transparent',
          strokeWidth: 0
        },
        label: { color: '#ffffff' },
        emphasis: {
          ...s.emphasis,
          label: { color: '#ffffff' },
          // 确保强调状态也无边框
          itemStyle: {
            ...s.emphasis.itemStyle,
            borderWidth: 0,
            borderColor: 'transparent',
            stroke: 'transparent',
            strokeWidth: 0
          }
        },
        // 再次确保select状态无边框
        select: {
          itemStyle: {
            borderWidth: 0,
            borderColor: 'transparent',
            stroke: 'transparent',
            strokeWidth: 0
          }
        },
        levels: s.levels.map(level => ({
          ...level,
          itemStyle: {
            ...level.itemStyle,
            borderWidth: 0,
            borderColor: 'transparent',
            stroke: 'transparent',
            strokeWidth: 0
          },
          label: { ...level.label, color: '#ffffff' }
        }))
      }))
    }
    categorySunburstChart.value.setOption(whiteTextOption)
  } catch (error) {
    console.error('初始化商品分类分布旭日图失败:', error)
  }
}

// 处理窗口大小变化
const handleResize = () => {
  try {
    if (salesChart.value) {
      salesChart.value.resize()
    }
    if (orderTrendChart.value) {
      orderTrendChart.value.resize()
    }
    if (categoryChart.value) {
      categoryChart.value.resize()
    }
    if (categorySunburstChart.value) {
      categorySunburstChart.value.resize()
    }
    // 处理仪表盘实例的大小变化
    Object.values(gaugeInstances.value).forEach(instance => {
      if (instance) {
        instance.resize()
      }
    })
  } catch (error) {
    console.error('图表 resize 失败:', error)
  }
}

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStatistics(),
      loadRecentActivities(),
      initCharts()
    ])
  } catch (error) {
    console.error('初始化数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  if (loading.value) return
  await initData()
}

// 监听图表类型变化
watch(salesTrendType, () => {
  initSalesChart()
})

watch(orderTrendType, () => {
  initOrderTrendChart()
})

// 监听刷新间隔变化
watch(refreshInterval, (newVal) => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
  if (newVal > 0) {
    refreshTimer.value = setInterval(refreshData, newVal * 1000)
  }
})

// 生命周期钩子
onMounted(async () => {
  await initData()
  await nextTick()
  initGaugeCharts() // 初始化仪表盘
  await initCategorySunburstChart() // 初始化旭日图
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (salesChart.value) {
    salesChart.value.dispose()
  }
  if (orderTrendChart.value) {
    orderTrendChart.value.dispose()
  }
  if (categoryChart.value) {
    categoryChart.value.dispose()
  }
  if (categorySunburstChart.value) {
    categorySunburstChart.value.dispose()
  }
})

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) { // 小于1分钟
    return '刚刚'
  } else if (diff < 3600000) { // 小于1小时
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 小于24小时
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return date.toLocaleDateString()
  }
}

// 获取活动类型对应的样式
const getActivityType = (type) => {
  const typeMap = {
    order: 'success',
    product: 'warning',
    user: 'primary'
  }
  return typeMap[type] || 'info'
}

// 为表格行添加样式，突出显示前三个商品（只对有数据的行应用）
const tableRowClassName = ({ row, rowIndex }) => {
  // 只对有实际数据的行应用前三名样式
  if (row.productName || row.salesCount > 0 || row.salesAmount > 0) {
    if (rowIndex === 0) {
      return 'gold-medal'
    } else if (rowIndex === 1) {
      return 'silver-medal'
    } else if (rowIndex === 2) {
      return 'bronze-medal'
    }
  }
  return ''
}

// 修改仪表盘相关代码
const gaugeRefs = ref({})
const gaugeInstances = ref({})

// 初始化仪表盘
const initGaugeCharts = () => {
  const gaugeOption = (value, max, title, color) => {
    // 根据不同的仪表盘计算合适的刻度间隔
    const interval = max / 8;
    const splitNumber = 8;
    
    // 计算合适的最大值，确保当前值在合理范围内
    const calculatedMax = value > max ? Math.ceil(value / interval) * interval : max;
    
    return {
      series: [{
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: calculatedMax,
        radius: '100%',
        splitNumber,
        axisLine: {
          lineStyle: {
            width: 8,
            color: [
              [0, '#00c853'],      // 绿色
              [0.05, '#17a948'],   // 绿色-深绿过渡
              [0.1, '#2e7d32'],    // 深绿
              [0.15, '#3a9e44'],   // 深绿-中绿过渡
              [0.2, '#4caf50'],    // 中绿
              [0.25, '#5abb5a'],   // 中绿-浅绿过渡
              [0.3, '#66bb6a'],    // 浅绿
              [0.35, '#81c77f'],   // 浅绿-黄绿过渡
              [0.4, '#9ccc65'],    // 黄绿
              [0.45, '#b5d64f'],   // 黄绿-亮黄过渡
              [0.5, '#cddc39'],    // 亮黄
              [0.55, '#f1d534'],   // 亮黄-黄色过渡
              [0.6, '#ffca28'],    // 黄色
              [0.65, '#ffb427'],   // 黄色-橙黄过渡
              [0.7, '#ffa726'],    // 橙黄
              [0.75, '#ff8d35'],   // 橙黄-橙色过渡
              [0.8, '#ff7043'],    // 橙色
              [0.825, '#f4614a'],  // 橙色-红橙过渡
              [0.85, '#ef5350'],   // 红橙
              [0.875, '#e24642'],  // 红橙-红色过渡
              [0.9, '#e53935'],    // 红色
              [0.925, '#d32c4a'],  // 红色-深红过渡
              [0.95, '#c2185b'],   // 深红
              [0.975, '#a91e68'],  // 深红-紫色过渡
              [1, '#9c27b0']       // 紫色
            ]
          }
        },
        pointer: {
          show: true,
          length: '50%',
          width: 3,
          itemStyle: {
            color: '#409EFF'
          }
        },
        axisTick: {
          show: true,
          splitNumber: 5,
          lineStyle: {
            color: '#FFFFFF',
            width: 1
          },
          length: 6
        },
        splitLine: {
          show: true,
          length: 10,
          lineStyle: {
            color: '#FFFFFF',
            width: 2
          }
        },
        axisLabel: {
          show: true,
          distance: 25,
          color: '#FFFFFF',
          fontSize: 12,
          formatter: function(value) {
            if (title === '总销售额') {
              return '¥' + (value || 0);
            }
            return (value || 0);
          }
        },
        detail: {
          valueAnimation: true,
          fontSize: 36,
          fontWeight: 'bold',
          color: '#FFFFFF',
          offsetCenter: [0, '60%'],
          formatter: function(value) {
            if (title === '总销售额') {
              return '¥' + (value ? value.toFixed(2) : '0.00');
            }
            return (value ? value.toFixed(0) : '0');
          }
        },
        data: [{
          value: value,
          name: ''
        }],
        title: {
          show: false
        },
        anchor: {
          show: true,
          size: 15,
          itemStyle: {
            color: '#409EFF'
          }
        }
      }]
    }
  }

  // 确保 DOM 已经渲染完成
  nextTick(() => {
    try {
      // 初始化用户仪表盘
      if (gaugeRefs.value.userGaugeRef) {
        const userCount = statistics.value.userCount;
        const userMax = Math.max(16, Math.ceil(userCount * 1.5)); // 确保最大值至少是当前值的1.5倍
        // 如果实例已存在，先销毁
        if (gaugeInstances.value.userGauge) {
          gaugeInstances.value.userGauge.dispose()
        }
        gaugeInstances.value.userGauge = echarts.init(gaugeRefs.value.userGaugeRef)
        gaugeInstances.value.userGauge.setOption(
          gaugeOption(userCount, userMax, '总用户数', '#67C23A')
        )
      }

      // 初始化订单仪表盘
      if (gaugeRefs.value.orderGaugeRef) {
        const orderCount = statistics.value.orderCount;
        const orderMax = Math.max(32, Math.ceil(orderCount * 1.5));
        // 如果实例已存在，先销毁
        if (gaugeInstances.value.orderGauge) {
          gaugeInstances.value.orderGauge.dispose()
        }
        gaugeInstances.value.orderGauge = echarts.init(gaugeRefs.value.orderGaugeRef)
        gaugeInstances.value.orderGauge.setOption(
          gaugeOption(orderCount, orderMax, '总订单数', '#409EFF')
        )
      }

      // 初始化商品仪表盘
      if (gaugeRefs.value.productGaugeRef) {
        const productCount = statistics.value.productCount;
        const productMax = Math.max(24, Math.ceil(productCount * 1.5));
        // 如果实例已存在，先销毁
        if (gaugeInstances.value.productGauge) {
          gaugeInstances.value.productGauge.dispose()
        }
        gaugeInstances.value.productGauge = echarts.init(gaugeRefs.value.productGaugeRef)
        gaugeInstances.value.productGauge.setOption(
          gaugeOption(productCount, productMax, '商品总数', '#E6A23C')
        )
      }

      // 初始化销售额仪表盘
      if (gaugeRefs.value.salesGaugeRef) {
        const totalSales = statistics.value.totalSales;
        const salesMax = Math.max(10000, Math.ceil(totalSales * 1.5 / 1000) * 1000);
        // 如果实例已存在，先销毁
        if (gaugeInstances.value.salesGauge) {
          gaugeInstances.value.salesGauge.dispose()
        }
        gaugeInstances.value.salesGauge = echarts.init(gaugeRefs.value.salesGaugeRef)
        gaugeInstances.value.salesGauge.setOption(
          gaugeOption(totalSales, salesMax, '总销售额', '#F56C6C')
        )
      }
    } catch (error) {
      console.error('初始化仪表盘失败:', error)
    }
  })
}

// 更新仪表盘数据
watch(() => statistics.value, (newVal) => {
  nextTick(() => {
    const { userGauge, orderGauge, productGauge, salesGauge } = gaugeInstances.value
    if (userGauge) {
      userGauge.setOption({
        series: [{ data: [{ value: newVal.userCount }] }]
      })
    }
    if (orderGauge) {
      orderGauge.setOption({
        series: [{ data: [{ value: newVal.orderCount }] }]
      })
    }
    if (productGauge) {
      productGauge.setOption({
        series: [{ data: [{ value: newVal.productCount }] }]
      })
    }
    if (salesGauge) {
      salesGauge.setOption({
        series: [{ data: [{ value: newVal.totalSales }] }]
      })
    }
  })
}, { deep: true })

// 仪表盘卡片配置
const gaugeCards = [
  {
    title: '总用户数',
    icon: 'User',
    ref: 'userGaugeRef',
    increase: 'userIncrease'
  },
  {
    title: '总订单数',
    icon: 'List',
    ref: 'orderGaugeRef',
    increase: 'orderIncrease'
  },
  {
    title: '商品总数',
    icon: 'Goods',
    ref: 'productGaugeRef',
    increase: 'productIncrease'
  },
  {
    title: '总销售额',
    icon: 'Money',
    ref: 'salesGaugeRef',
    increase: 'salesIncrease'
  }
]

// 切换销售趋势时间范围
const handleSalesTrendTypeChange = async (type) => {
  try {
    // 重新初始化图表 - 不再清除日期范围，保留用户的选择
    await initSalesChart()
  } catch (error) {
    console.error('切换销售趋势失败:', error)
  }
}

// 处理日期范围变化
const handleDateRangeChange = async () => {
  try {
    if (salesDateRange.value.length === 2) {
      // 周模式下严格限制只能选择连续的7天
      if (salesTrendType.value === 'week') {
        const [start, end] = salesDateRange.value
        const startDate = new Date(start)
        const endDate = new Date(end)
        
        // 计算两个日期之间的天数差
        const timeDiff = endDate.getTime() - startDate.getTime()
        const dayDiff = Math.ceil(timeDiff / (1000 * 3600 * 24))
        
        // 如果不是7天，显示警告并清空选择
        if (dayDiff !== 6) {
          ElMessage.warning('周模式请选择连续的7天')
          // 清空当前选择，让用户重新选择
          salesDateRange.value = []
          return
        }
      }
      
      // 重新初始化图表
      await initSalesChart()
    }
  } catch (error) {
    console.error('日期范围变更失败:', error)
  }
}

// 处理热销商品时间范围类型变化
const handleTopProductTypeChange = () => {
  // 重新加载统计数据
  loadStatistics()
}

// 订单趋势图表窗口大小变化处理
const handleOrderTrendChartResize = () => {
  if (orderTrendChart.value) {
    orderTrendChart.value.resize()
  }
}

// 切换订单趋势时间范围
const handleOrderTrendTypeChange = async (type) => {
  try {
    // 更新当前模式类型
    orderTrendType.value = type
    // 重新初始化图表以显示对应模式的数据
    await initOrderTrendChart()
  } catch (error) {
    console.error('切换订单趋势失败:', error)
    ElMessage.error('切换订单趋势失败')
  }
}
</script>
  
 <style lang="scss">
/* 修改单选按钮文字颜色 */
.el-radio-button__inner {
  color: #274151 !important; /* 设置文字颜色 */
}

/* 前三名商品的样式 - 使用最高优先级 */
.el-table__row.gold-medal {
  background-color: #fff9e6 !important;
}

.el-table__row.gold-medal > td {
  background-color: #fff9e6 !important;
  color: #e6a23c !important;
  font-weight: bold !important;
}

.el-table__row.silver-medal {
  background-color: #f0f9ff !important;
}

.el-table__row.silver-medal > td {
  background-color: #f0f9ff !important;
  color: #c0c0c0 !important;
  font-weight: bold !important;
}

.el-table__row.bronze-medal {
  background-color: #fdf6ec !important;
}

.el-table__row.bronze-medal > td {
  background-color: #fdf6ec !important;
  color: #e67700 !important;
  font-weight: bold !important;
}

/* 局部样式 */
.dashboard-container {
  padding: 20px;

  .stat-card {
    margin-bottom: 20px;
    transition: all 0.3s ease;
    border-radius: 12px;
    overflow: hidden;
    border: none;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      border-bottom: none;
      background: transparent;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .icon-circle {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 20px;
    }

    .gauge-chart {
      height: 280px;
      padding: 20px;
      transition: all 0.3s ease;
      &:hover {
        transform: scale(1.03);
      }
    }

    .compare {
      text-align: center;
      margin-top: -40px;
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      padding-bottom: 20px;

      &.is-up {
        color: #fff;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }

      &:not(.is-up) {
        color: #fff;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }
  }

  /* 每个卡片的渐变背景 */
  .user-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  }

  .order-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  }

  .product-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  }

  .sales-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  }

  .chart-card {
    margin-bottom: 20px;
    border: none; // 移除卡片默认边框
    box-shadow: none; // 移除卡片默认阴影

    .el-card__header {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%); // 添加背景颜色
      padding: 15px 20px;
      border-bottom: none; // 移除头部下边框
    }
    
    .el-card__body {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%); // 覆盖Element Plus默认白色背景
      padding: 0;
      border: none; // 确保内容区域无边框
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        color: #ffffff; /* 将标题文字设置为白色 */
      }

      .chart-actions {
        display: flex;
        gap: 10px;
      }
    }
    
    /* 完全重置销售趋势图单选按钮样式，彻底去除所有默认蓝色效果 */
    #sales-trend-radio-group {
      --button-bg: #48494c;
      --button-hover-bg: #5a5b5f;
      --button-active-bg: #ffffff;
      --button-color: #ffffff;
      --button-active-color: #274151;
      --border-color: #48494c;
      --border-radius: 4px;
    }
    
    /* 重置所有按钮样式 */
    #sales-trend-radio-group .el-radio-button {
      position: relative;
    }
    
    /* 重置按钮内部样式 - 添加白色边框 */
    #sales-trend-radio-group .el-radio-button__inner {
      background-color: var(--button-bg) !important;
      color: var(--button-color) !important;
      border: 1px solid #ffffff !important; /* 修改为白色边框 */
      box-shadow: none !important;
      transition: all 0.3s;
    }
    
    /* 移除所有默认的active状态蓝色边框 - 使用白色边框 */
    #sales-trend-radio-group .el-radio-button.is-active .el-radio-button__inner {
      background-color: var(--button-active-bg) !important;
      color: var(--button-active-color) !important;
      border-color: #ffffff !important; /* 修改为白色边框 */
      box-shadow: none !important;
    }
    
    /* 悬停状态 - 使用白色边框 */
    #sales-trend-radio-group .el-radio-button:hover .el-radio-button__inner {
      background-color: var(--button-hover-bg) !important;
      border-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 圆角设置 */
    #sales-trend-radio-group .el-radio-button:first-child .el-radio-button__inner {
      border-radius: var(--border-radius) 0 0 var(--border-radius) !important;
    }
    
    #sales-trend-radio-group .el-radio-button:last-child .el-radio-button__inner {
      border-radius: 0 var(--border-radius) var(--border-radius) 0 !important;
    }
    
    /* 彻底消除相邻按钮之间的边框问题 - 使用margin-left负值覆盖 */
    #sales-trend-radio-group .el-radio-button:not(:first-child) .el-radio-button__inner {
      margin-left: -1px !important;
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 确保选中按钮的左侧边框也是白色 */
    #sales-trend-radio-group .el-radio-button.is-active .el-radio-button__inner {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
      border-right-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 确保选中按钮旁边的按钮边框也是白色 */
    #sales-trend-radio-group .el-radio-button.is-active + .el-radio-button .el-radio-button__inner {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 移除focus状态的蓝色轮廓 */
    #sales-trend-radio-group .el-radio-button:focus {
      outline: none !important;
    }
    
    #sales-trend-radio-group .el-radio-button__inner:focus {
      outline: none !important;
      box-shadow: none !important;
    }
    
    /* 隐藏原生radio按钮 */
     #sales-trend-radio-group .el-radio-button__original-radio {
       opacity: 0;
       width: 0;
       height: 0;
     }
    
    #sales-trend-radio-group .el-radio-button.is-active + .el-radio-button .el-radio-button__inner {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 关键修复：确保选中按钮的左侧边框是白色 */
    #sales-trend-radio-group .el-radio-button.is-active .el-radio-button__inner {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 修复第二个按钮（月）选中时左侧的竖线 - 使用白色边框 */
    #sales-trend-radio-group .el-radio-button:nth-child(2).is-active .el-radio-button__inner {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 直接针对销售趋势图单选按钮的span元素 - 最高优先级 */
    #sales-trend-radio-group .el-radio-button.is-active .el-radio-button__inner span {
      color: #274151 !important; /* 与选中按钮文字颜色一致 */
      font-weight: 500 !important;
      display: inline-block;
    }
    
    /* 使用:deep确保穿透到组件内部 - 使用白色边框 */
    #sales-trend-radio-group :deep(.el-radio-button .el-radio-button__inner) {
      background-color: #48494c !important;
      color: #ffffff !important;
      border-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    #sales-trend-radio-group :deep(.el-radio-button.is-active .el-radio-button__inner) {
      background-color: #ffffff !important;
      color: #274151 !important;
      border-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* :deep相邻按钮边框样式 - 使用白色边框 */
    #sales-trend-radio-group :deep(.el-radio-button + .el-radio-button .el-radio-button__inner) {
      border-left-color: #ffffff !important; /* 修改为白色边框 */
    }
    
    /* 热销商品TOP10 按钮组样式 - 添加白色边框 */
    .custom-radio-group button {
      border: 1px solid #ffffff !important;
      box-shadow: none !important;
    }
    
    /* 热销商品TOP10 按钮组悬停样式 */
    .custom-radio-group button:hover {
      background-color: #5a5b5f !important;
      color: #ffffff !important;
      border-color: #ffffff !important;
    }
    
    /* 确保选中状态下的悬停样式也正确 - 保持白色边框 */
    .custom-radio-group button:hover:not([style*="backgroundColor: #ffffff"]) {
      background-color: #5a5b5f !important;
      border-color: #ffffff !important;
    }
    
    /* 确保第一个按钮有正确的圆角和边框 */
    .custom-radio-group button:first-child {
      border-top-left-radius: 4px !important;
      border-bottom-left-radius: 4px !important;
      border-right: 1px solid #ffffff !important;
    }
    
    /* 确保最后一个按钮有正确的圆角和边框 */
    .custom-radio-group button:last-child {
      border-top-right-radius: 4px !important;
      border-bottom-right-radius: 4px !important;
      border-left: 1px solid #ffffff !important;
    }
    
    /* 确保中间按钮有正确的边框 */
    .custom-radio-group button:not(:first-child):not(:last-child) {
      border-left: 1px solid #ffffff !important;
      border-right: 1px solid #ffffff !important;
    }
    
    /* 选中状态下确保白色边框清晰可见 */
    .custom-radio-group button[style*="backgroundColor: #ffffff"] {
      border: 1px solid #ffffff !important;
      z-index: 1 !important;
    }

    .chart {
    height: 350px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 半透明白色背景 */
  }
  
  .chart-container {
    display: flex;
    gap: 20px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 半透明白色背景 */
    padding: 15px;
    border-radius: 8px;
    color: #ffffff;
  }
  
  .chart-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    color: #ffffff;
  }
  
  .chart-title {
    margin: 0 0 10px 0;
    font-size: 14px;
    color: #ffffff;
  }
  
  .sunburst-chart {
    height: 400px; /* 增加高度以更好地展示旭日图 */
    width: 100%;
    min-width: 300px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 白色背景 */
    border-radius: 8px;
    padding: 10px;
  }
  
  @media (max-width: 768px) {
    .chart-container {
      flex-direction: column;
    }
    
    .sunburst-chart {
      min-width: auto;
    }
  }
  }

  .rank-card {
    margin-bottom: 20px;
    border: none !important; /* 去掉最外边的白色边框线 */
    
    .el-card__header {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 与chart-card头部背景色一致 */
      padding: 15px 20px;
      border-bottom: none;
    }
    
    .el-card__body {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important; /* 与头部相同的背景色，添加!important确保覆盖 */
    }
    
    .card-header span {
      color: white !important;
      font-weight: bold !important;
    }
    
    .el-card__body {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 与头部背景色一致 */
      border: none; /* 确保内容区域无边框 */
    }
    
    /* 设置表格及其所有子元素的背景色与头部一致，增加优先级 */
    .el-table {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    .el-table__inner-wrapper {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    .el-table__header-wrapper,
    .el-table__body-wrapper {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    .el-table__header {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important; /* 表头行添加背景色 */
    }
    .el-table__body {
      background: transparent !important;
    }
    /* 确保表头行和单元格也有背景色 */
    .el-table__header-wrapper thead tr,
    .el-table__header-wrapper thead th {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      color: #ffffff !important;
    }
    
    /* 设置表头单元格内文字为白色 */
    .el-table__header-wrapper thead th .cell {
      color: #ffffff !important;
    }
    
    /* 确保表格行的背景色透明，让父元素的渐变背景显示 */
    .el-table__row,
    .el-table__row.el-table__row--striped {
      background-color: transparent !important;
    }
    
    /* 确保表格单元格也透明 */
    .el-table__cell {
      background-color: transparent !important;
      --el-table-cell-bg-color: transparent !important;
    }
    
    /* 确保前三名的特殊样式也应用透明背景 */
    .el-table__row.gold-medal,
    .el-table__row.silver-medal,
    .el-table__row.bronze-medal {
      background-color: transparent !important;
    }
    
    /* 为除前三名外的所有行设置白色文字 */
    .el-table__row:not(.gold-medal):not(.silver-medal):not(.bronze-medal) .el-table__cell {
      color: white !important;
    }
    
    /* 设置表格边框为白色，完全移除所有竖线边框 */
    .el-table {
      border: 1px solid white !important;
      border-right: none !important;
      border-left: none !important;
      border-top: 2px solid white !important; /* 最上面的线加粗 */
      //border-bottom: 2px solid white !important; /* 最下面的横线加粗 */
    }
    .el-table th,
    .el-table td {
      border: none !important; /* 先清除所有边框 */
      border-bottom: 1px solid white !important; /* 只保留横线边框 */
    }
    .el-table--border th,
    .el-table--border td {
      border-right: none !important; /* 确保没有竖线边框 */
      border-left: none !important; /* 确保没有左侧竖线边框 */
      border-bottom: 1px solid white !important; /* 只保留横线边框 */
    }
    
    /* 最上面下面的一条线加粗（表头底部边框） */
    .el-table__header-wrapper thead th {
      border-bottom: 2px solid white !important;
    }
    /* 覆盖Element UI可能的其他边框样式 */
    .el-table__inner-wrapper {
      border-right: none !important;
    }
    .el-table__header-wrapper,
    .el-table__body-wrapper {
      border-right: none !important;
    }
    .el-table__header,
    .el-table__body {
      border-right: none !important;
    }
    /* 确保固定列没有边框 */
    .el-table--border.is-scrolling-left .el-table__fixed-left-patch {
      border-right: none !important;
    }
    .el-table__fixed-right,
    .el-table__fixed-left {
      border: none !important;
    }
  }

  .activity-card {
    margin-bottom: 20px;
    border: none !important; /* 移除卡片边框 */
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important; /* 确保整个卡片有背景色 */
    
    .el-card__header {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%); /* 与chart-card头部背景色一致 */
      padding: 15px 20px;
      border-bottom: none;
    }
    
    .card-header span {
      color: white !important;
      font-weight: bold !important;
    }

    .el-timeline {
      padding: 20px;
    }
    
    /* 设置时间线内容文字颜色为白色 */
    .el-timeline-item__content {
      color: white !important;
    }
  }
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    
    span {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
    }
    
    .el-icon {
      font-size: 20px;
      color: #909399;
    }
  }

  .gauge-chart {
    height: 200px;
    margin: 0;
  }

  .compare {
    text-align: center;
    padding: 10px 0;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;

    &.is-up {
      color: #67c23a;
    }

    &:not(.is-up) {
      color: #f56c6c;
    }

    .el-icon {
      font-size: 16px;
    }
  }
}

  /* 日期范围选择器样式 */
  .el-date-editor--daterange {
    background-color: #48494c !important;
    border-color: #ffffff !important;
    color: #ffffff !important;
    border-width: 1px !important; /* 添加白色边框 */
    box-shadow: none !important; /* 移除阴影 */
  }
  
  /* 针对带有el-input__wrapper类的元素 */
  .el-date-editor--daterange.el-input__wrapper {
    background-color: #48494c !important;
    border-color: #ffffff !important;
    border-width: 1px !important; /* 添加白色边框 */
    box-shadow: none !important; /* 移除阴影 */
  }
  
  .el-date-editor--daterange .el-range-input {
    background-color: transparent !important;
    color: #ffffff !important;
    border: none !important; /* 移除输入框边框 */
  }
  
  .el-date-editor--daterange .el-range-separator {
    color: #ffffff !important;
  }
  
  /* 确保图标也是白色 */
  .el-date-editor--daterange .el-icon {
    color: #ffffff !important;
  }
  
  /* 确保el-range-editor也有白色边框 */
  .el-range-editor {
    border: 1px solid #ffffff !important;
    box-shadow: none !important;
  }
  
  /* 订单趋势图radio按钮样式 - 与销售趋势图保持一致 - 使用白色边框 */
  #sales-trend-radio-group .el-radio-button .el-radio-button__inner {
    background-color: #48494c !important;
    color: #ffffff !important;
    border-color: #ffffff !important; /* 修改为白色边框 */
  }
  
  #sales-trend-radio-group .el-radio-button.is-active .el-radio-button__inner {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important; /* 修改为白色边框 */
  }
  
  /* 确保span按钮样式完全一致 */
  #sales-trend-radio-group .el-radio-button__inner {
    display: inline-block;
    padding: 0 20px;
    height: 32px;
    line-height: 30px;
    white-space: nowrap;
    vertical-align: middle;
    border: 1px solid #dcdfe6;
    cursor: pointer;
    box-sizing: border-box;
    outline: none;
    transition: all 0.3s;
  }
  
  /* 订单趋势图文字颜色设置为白色 */
  .chart-container,
  .chart-wrapper,
  .chart {
    color: #ffffff !important;
  }
  
  /* 确保图表中的文字和标签都是白色 */
  .chart text,
  .chart svg text,
  canvas {
    color: #ffffff !important;
    fill: #ffffff !important;
    stroke: transparent !important; /* 移除所有画布元素的描边 */
  }
</style>