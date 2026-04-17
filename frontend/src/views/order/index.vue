<template>
  <div class="order-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable ref="orderNoInput" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;" @change="handleQuery" ref="statusSelect">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="parseInt(option.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" class="rounded-button">查询</el-button>
          <el-button type="primary" @click="resetQuery" class="rounded-button">重置</el-button>
          <el-button type="primary" @click="handleAdd" class="rounded-button">新增订单</el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedOrders.length === 0" class="rounded-button">批量删除</el-button>
        <el-button type="success" @click="handleExport" class="rounded-button export-button">
          <el-icon><Download /></el-icon>导出
        </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="orderList"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
        ref="orderTableRef"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="订单编号" prop="orderNo" width="180" />
        <el-table-column label="收货人" prop="consignee" width="120" />
        <el-table-column label="联系电话" prop="phone" width="120" />
        <el-table-column label="总金额" prop="totalAmount" width="120">
          <template #default="scope">
            ￥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="success" 
              link 
              @click="handlePay(scope.row)"
            >
              支付
            </el-button>
            <el-button 
              v-if="scope.row.status === 1"
              type="warning" 
              link 
              @click="handleShip(scope.row)"
            >
              发货
            </el-button>
            <el-button 
              v-if="scope.row.status === 0"
              type="danger" 
              link 
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="scope.row.status === 2"
              type="success" 
              link 
              @click="handleComplete(scope.row)"
            >
              完成
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="订单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailDialog.data.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailDialog.data.status)">
            {{ getStatusText(detailDialog.data.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detailDialog.data.consignee }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailDialog.data.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detailDialog.data.address }}</el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">{{ detailDialog.data.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detailDialog.data.items || []" border style="margin-top: 20px">
        <el-table-column label="商品名称" prop="productName" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.productImage ? ('/api' + scope.row.productImage) : '/images/product-icon.svg'"
              fit="contain"
              style="width: 50px; height: 50px"
            />
          </template>
        </el-table-column>
        <el-table-column label="单价" prop="price" width="120">
          <template #default="scope">{{ scope.row.price !== null ? '￥' + scope.row.price : '未定价' }}</template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity" width="120" />
        <el-table-column label="小计" width="120">
          <template #default="scope">￥{{ scope.row.totalAmount }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑订单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
      :style="dialogStyle"
    >
      <el-form ref="orderFormRef" :model="orderForm" :rules="rules" label-width="100px">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="orderForm.orderNo" placeholder="请输入订单编号" />
        </el-form-item>
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="orderForm.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="orderForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="orderForm.address" placeholder="请输入收货地址" />
        </el-form-item>
        <el-form-item label="商品列表" prop="items">
          <div class="order-items">
            <div v-for="(item, index) in orderForm.items" :key="index" class="item-row">
              <el-select 
                v-model="item.productId" 
                placeholder="请选择商品"
                @change="(val) => handleProductChange(val, index)"
              >
                <el-option
                  v-for="product in productList.filter(p => p.status === 1)"
                  :key="product.id"
                  :label="product.name"
                  :value="product.id"
                />
              </el-select>
              <el-input-number
                v-model="item.quantity"
                :min="1"
                @change="() => calculateItemAmount(index)"
              />
              <el-input
                v-model="item.price"
                placeholder="单价"
                readonly
              >
                <template #suffix>/单价</template>
              </el-input>
              <el-input
                v-model="item.totalAmount"
                placeholder="小计"
                readonly
              >
                <template #suffix>/小计</template>
              </el-input>
              <el-button type="danger" @click="removeOrderItem(index)">删除</el-button>
            </div>
            <el-button type="primary" @click="addOrderItem" class="rounded-button">添加商品</el-button>
          </div>
        </el-form-item>
        <el-form-item label="订单总额">
          <el-input v-model="orderForm.totalAmount" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="dialog-cancel-btn" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getOrderList, getOrderDetail, payOrder, shipOrder, cancelOrder, completeOrder, addOrder, checkOrderNoExists, deleteOrder, batchDeleteOrders } from '@/api/order'
import { getProductList } from '@/api/product'
import { getDictItemByCode } from '@/api/dict'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { exportToExcel } from '@/utils/export'
import { Download } from '@element-plus/icons-vue'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  status: null,
  startTime: '',
  endTime: ''
})

// 元素引用
const orderNoInput = ref(null)

// 状态字典选项
const statusOptions = ref([])
const statusTypeMap = ref({})
const statusSelect = ref(null)

// 新增订单对话框渐变背景样式
const dialogStyle = {
  background: 'linear-gradient(135deg, #464e58 0%, #434c55 100%)',
  backgroundColor: 'transparent',
  '--el-dialog-background-color': 'transparent',
  '--el-bg-color': 'transparent',
  '--el-color-white': 'white',
  '--el-text-color-primary': 'white',
  '--el-text-color-regular': 'white',
  '--el-text-color-secondary': 'white',
  color: 'white'
}

// 强制设置背景颜色
const setBackgroundColors = () => {
  // 设置订单编号输入框背景
  if (orderNoInput.value) {
    const inputWrapper = orderNoInput.value.$el.querySelector('.el-input__wrapper')
    if (inputWrapper) {
      inputWrapper.style.backgroundColor = '#48494c'
      inputWrapper.style.borderColor = '#48494c'
      inputWrapper.style.backgroundImage = 'none'
      
      // 添加点击事件监听，设置边框为白色
      inputWrapper.addEventListener('click', function() {
        const self = this;
        // 使用setTimeout确保样式在其他可能的样式之后应用
        setTimeout(function() {
          // 完全重写样式，同时处理box-shadow
          self.setAttribute('style', 'background-color: #48494c !important; border-color: white !important; box-shadow: 0 0 0 1px white !important; background-image: none !important; transition: border-color 0.2s;');
        }, 0);
      })
      
      // 添加聚焦事件监听，确保聚焦时边框也是白色
      inputWrapper.addEventListener('focus', function() {
        // 使用setAttribute方法更强制地设置样式
        this.setAttribute('style', (this.getAttribute('style') || '') + '; border-color: white !important;')
      }, true)
      
      // 添加失焦事件监听，恢复原始边框颜色
      inputWrapper.addEventListener('blur', function() {
        this.style.borderColor = '#48494c'
      }, true)
      
      // 设置输入框内部元素
      const innerInput = inputWrapper.querySelector('.el-input__inner')
      if (innerInput) {
        innerInput.style.backgroundColor = '#48494c'
        innerInput.style.color = 'white'
      }
    }
  }
  
  // 设置状态选择器背景
  if (statusSelect.value) {
    const selectWrapper = statusSelect.value.$el.querySelector('.el-select__wrapper')
    if (selectWrapper) {
      selectWrapper.style.backgroundColor = '#48494c'
      selectWrapper.style.borderColor = '#48494c'
      selectWrapper.style.backgroundImage = 'none'
      
      // 添加点击事件监听，设置边框为白色
      selectWrapper.addEventListener('click', function() {
        const self = this;
        // 使用setTimeout确保样式在其他可能的样式之后应用
        setTimeout(function() {
          // 完全重写样式，同时处理box-shadow
          self.setAttribute('style', 'background-color: #48494c !important; border-color: white !important; box-shadow: 0 0 0 1px white !important; background-image: none !important; transition: border-color 0.2s;');
        }, 0);
      })
      
      // 添加聚焦事件监听，确保聚焦时边框也是白色
      selectWrapper.addEventListener('focus', function() {
        // 使用setAttribute方法更强制地设置样式
        this.setAttribute('style', (this.getAttribute('style') || '') + '; border-color: white !important;')
      }, true)
      
      // 添加失焦事件监听，恢复原始边框颜色
      selectWrapper.addEventListener('blur', function() {
        this.style.borderColor = '#48494c'
      }, true)
      
      // 设置选择器内部元素
      const innerInput = selectWrapper.querySelector('.el-input__inner')
      if (innerInput) {
        innerInput.style.backgroundColor = '#48494c'
        innerInput.style.color = 'white'
      }
    }
  }
  
  // 设置时间范围选择器背景
  const datePickerWrappers = document.querySelectorAll('.el-date-editor')
  datePickerWrappers.forEach(wrapper => {
    wrapper.style.backgroundColor = '#48494c'
    wrapper.style.borderColor = 'white'
    wrapper.style.borderWidth = '1px'
    wrapper.style.borderStyle = 'solid'
    wrapper.style.backgroundImage = 'none'
    wrapper.style.boxShadow = 'none'
    
    // 完全重写样式以确保优先级，添加完整的边框定义
    wrapper.setAttribute('style', 'background-color: #48494c !important; border: 1px solid white !important; background-image: none !important; box-shadow: none !important;');
    
    // 添加点击事件监听，设置边框为白色
    wrapper.addEventListener('click', function() {
      const self = this;
      // 使用setTimeout确保样式在其他可能的样式之后应用
      setTimeout(function() {
        // 完全重写样式，同时处理box-shadow
        self.setAttribute('style', 'background-color: #48494c !important; border: 1px solid white !important; box-shadow: 0 0 0 1px white !important; background-image: none !important; transition: border-color 0.2s;');
      }, 0);
    })
    
    // 添加聚焦事件监听，确保聚焦时边框也是白色
    wrapper.addEventListener('focus', function() {
      // 使用setAttribute方法更强制地设置样式
      this.setAttribute('style', 'background-color: #48494c !important; border: 1px solid white !important; background-image: none !important; box-shadow: none !important;');
    }, true)
    
    // 添加失焦事件监听，保持白色边框
    wrapper.addEventListener('blur', function() {
      this.setAttribute('style', 'background-color: #48494c !important; border: 1px solid white !important; background-image: none !important; box-shadow: none !important;');
    }, true)
    
    // 设置内部输入框元素
    const innerInputs = wrapper.querySelectorAll('.el-range-input')
    innerInputs.forEach(input => {
      input.style.backgroundColor = '#48494c'
      input.style.color = 'white'
      input.setAttribute('style', 'background-color: #48494c !important; color: white !important;')
    })
    
    // 设置分隔符样式
    const separators = wrapper.querySelectorAll('.el-range-separator')
    separators.forEach(separator => {
      separator.style.color = 'white'
      separator.setAttribute('style', 'color: white !important;')
    })
    
    // 设置图标样式
    const icons = wrapper.querySelectorAll('.el-icon')
    icons.forEach(icon => {
      icon.style.color = 'white'
      icon.setAttribute('style', 'color: white !important;')
    })
  })
}

// 组件挂载后执行
onMounted(() => {
  // 立即执行一次
  nextTick(() => setBackgroundColors())
  
  // 使用定时器确保样式应用，处理可能的异步渲染
  const timer = setInterval(() => {
    setBackgroundColors()
    // 检查是否已经成功应用样式
    const testWrapper = document.querySelector('.el-input__wrapper')
    if (testWrapper && testWrapper.style.backgroundColor === '#48494c') {
      clearInterval(timer)
    }
  }, 100)
  
  // 最多尝试10次
  setTimeout(() => clearInterval(timer), 1000)
})

// 日期范围
const dateRange = ref([])

// 数据列表
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const selectedOrders = ref([])

// 详情对话框
const detailDialog = reactive({
  visible: false,
  data: {}
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const orderFormRef = ref()
const productList = ref([])

// 表单数据
const orderForm = ref({
  orderNo: '',
  consignee: '',
  phone: '',
  address: '',
  items: [],
  totalAmount: 0
})

// 表单验证规则
const rules = {
  orderNo: [
    { required: true, message: '订单编号不能为空', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '订单编号只能包含字母和数字', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (!value) {
          return callback()
        }
        try {
          // 异步检查订单编号是否存在
          const res = await checkOrderNoExists(value)
          if (!res.data) {
            // 显示错误提示但不自动清空输入框
            ElMessage.error('订单编号重复，请重新输入')
            // 返回错误，阻止表单提交
            return callback(new Error('订单编号重复'))
          } else {
            // 订单编号可用
            return callback()
          }
        } catch (error) {
          console.error('检查订单编号失败:', error)
          // 后端接口不可用时，暂时允许提交，但给出警告
          ElMessage.warning('无法检查订单编号是否存在，请确保订单编号唯一')
          return callback()
        }
      },
      // 确保在失去焦点时立即触发验证
      trigger: ['blur', 'change']
    }
  ],
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ],
  items: [
    { required: true, message: '请添加商品', trigger: 'change' }
  ]
}

// 获取状态字典
const loadStatusDict = async () => {
  try {
    const res = await getDictItemByCode('order_status')
    if (res.code === 200) {
      statusOptions.value = res.data
      // 构建状态类型映射
      statusTypeMap.value = {
        '-1': 'danger',
        '0': 'info',
        '1': 'success',
        '2': 'warning',
        '3': 'primary'
      }
    }
  } catch (error) {
    console.error('获取状态字典失败:', error)
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusStr = String(status)
  const dictItem = statusOptions.value.find(item => item.value === statusStr)
  return dictItem ? dictItem.label : '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  return statusTypeMap.value[status] || ''
}

// 处理日期变化
const handleDateChange = (val) => {
  queryParams.startTime = val ? val[0] : ''
  queryParams.endTime = val ? val[1] : ''
  handleQuery() // 选择日期后自动触发查询
}

// 查询列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getOrderList(queryParams)
    orderList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.orderNo = ''
  queryParams.status = null
  dateRange.value = []
  queryParams.startTime = ''
  queryParams.endTime = ''
  handleQuery()
}

// 查看详情
const handleDetail = (row) => {
  router.push(`/order/detail/${row.id}`)
}

// 支付订单
const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？')
    await payOrder(row.id)
    ElMessage.success('支付成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

// 发货
const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm('确认发货吗？')
    await shipOrder(row.id)
    ElMessage.success('发货成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发货失败')
    }
  }
}

// 取消订单
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '警告', {
      type: 'warning'
    })
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

// 完成订单
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成该订单吗？')
    await completeOrder(row.id)
    ElMessage.success('订单已完成')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成订单失败')
    }
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 删除订单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该订单吗？', '警告', {
      type: 'warning'
    })
    await deleteOrder(row.id)
    ElMessage.success('订单已删除')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除订单失败')
    }
  }
}

// 批量删除订单
const handleBatchDelete = async () => {
  try {
    const ids = selectedOrders.value.map(item => item.id)
    if (ids.length === 0) {
      ElMessage.warning('请选择要删除的订单')
      return
    }
    
    await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 个订单吗？`, '警告', {
      type: 'warning'
    })
    
    await batchDeleteOrders(ids)
    ElMessage.success(`成功删除 ${ids.length} 个订单`)
    
    // 清空选择并刷新列表
    selectedOrders.value = []
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除订单失败')
    }
  }
}

// 导出订单数据
const handleExport = async () => {
  if (orderList.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  let loadingInstance
  try {
    // 显示加载状态
    loadingInstance = ElLoading.service({
      message: '正在导出数据，请稍候...',
      lock: true
    })
    
    // 批量获取所有订单的详情数据，处理单个订单详情获取失败的情况
    const orderDetails = await Promise.all(
      orderList.value.map(async (order) => {
        try {
          const res = await getOrderDetail(order.id)
          return res.data
        } catch (error) {
          console.error(`获取订单 ${order.id} 详情失败:`, error)
          // 如果获取详情失败，返回基本订单信息
          return {
            ...order,
            address: '获取失败',
            remark: '获取失败',
            items: []
          }
        }
      })
    )
    
    // 转换数据格式，包含订单详情和商品信息
    const exportData = orderDetails.map(order => {
      // 处理商品列表，转换为字符串格式
      const products = order.items ? order.items.map(item => 
        `${item.productName}: ${item.quantity}件 (单价: ￥${item.price}, 小计: ￥${item.totalAmount})`
      ).join('\n') : '无'
      
      return {
        订单编号: order.orderNo,
        收货人: order.consignee,
        联系电话: order.phone,
        收货地址: order.address || '无',
        订单备注: order.remark || '无',
        总金额: order.totalAmount,
        订单状态: getStatusText(order.status),
        创建时间: order.createTime,
        商品列表: products
      }
    })
    
    // 使用导出工具函数
    exportToExcel(exportData, '订单数据')
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出订单失败:', error)
    ElMessage.error('导出订单失败')
  } finally {
    // 确保无论成功还是失败都关闭加载状态
    if (loadingInstance) {
      loadingInstance.close()
    }
  }
}

// 处理分页
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  handleQuery()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  handleQuery()
}

// 获取商品列表
const loadProductList = async () => {
  try {
    const res = await getProductList({
      pageNum: 1,
      pageSize: 100,
      status: 1  // 只获取上架的商品
    })
    if (res.data && res.data.records) {
      productList.value = res.data.records
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  }
}

// 生成订单号
const generateOrderNo = () => {
  const timestamp = new Date().getTime()
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `ORD${timestamp}${random}`
}

// 新增订单
const handleAdd = async () => {
  dialogVisible.value = true
  dialogTitle.value = '新增订单'
  
  // 重置表单，订单编号默认为空，完全由用户输入
  orderForm.value = {
    orderNo: '',
    consignee: '',
    phone: '',
    address: '',
    items: [
      {
        productId: undefined,
        quantity: 1,
        price: 0,
        totalAmount: 0
      }
    ],
    totalAmount: 0
  }
  
  // 加载商品列表
  await loadProductList()
}

// 添加商品项
const addOrderItem = () => {
  orderForm.value.items.push({
    productId: undefined,
    quantity: 1,
    price: 0,
    totalAmount: 0
  })
}

// 移除商品项
const removeOrderItem = (index) => {
  orderForm.value.items.splice(index, 1)
  // 重新计算订单总金额
  orderForm.value.totalAmount = orderForm.value.items.reduce((sum, item) => {
    return sum + (item.totalAmount || 0)
  }, 0)
}

// 商品选择变化
const handleProductChange = (productId, index) => {
  const product = productList.value.find(item => item.id === productId)
  if (product) {
    orderForm.value.items[index].price = product.price
    calculateItemAmount(index)
  }
}

// 计算商品项金额
const calculateItemAmount = (index) => {
  const item = orderForm.value.items[index]
  item.totalAmount = item.price * item.quantity
  
  // 重新计算订单总金额
  orderForm.value.totalAmount = orderForm.value.items.reduce((sum, item) => {
    return sum + (item.totalAmount || 0)
  }, 0)
}

// 提交表单
const submitForm = async () => {
  if (!orderFormRef.value) return
  await orderFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addOrder(orderForm.value)
        ElMessage.success('添加成功')
        dialogVisible.value = false
        handleQuery()
      } catch (error) {
        console.error('添加订单失败:', error)
        // 检查是否为订单编号重复错误
        if (error.response && error.response.data && 
            (error.response.data.message.includes('Duplicate entry') || 
             error.response.data.message.includes('order_no'))) {
          ElMessage.error('订单编号重复，请输入其他订单编号')
          // 清空订单编号输入框
          orderForm.value.orderNo = ''
        } else {
          ElMessage.error('添加订单失败，请检查订单信息')
        }
      }
    }
  })
}

// 初始化
onMounted(() => {
  loadStatusDict()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.order-container {
  padding: 20px;

  .search-card {
      margin-bottom: 20px;
      border-radius: 4px;
      border: none !important;
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      --el-card-bg-color: transparent !important;
      --el-card-border-color: transparent !important;
      
      .el-card__body {
        padding: 16px !important;
        border: none !important;
        background: #48494c !important;
        color: white; /* 确保文字颜色在深色背景上清晰可见 */
      }
      
      .search-form {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
      }
      
      /* 针对所有可能的表单标签元素设置白色文字 */
      .search-card .el-form-item__label,
      .search-card label,
      .search-card div.el-form-item__label,
      .search-form .el-form-item__label,
      .order-container .el-form-item__label {
        color: white !important;
        -webkit-text-fill-color: white !important; /* 防止文字颜色被覆盖 */
        color-adjust: exact !important;
      }
      
      /* 确保直接覆盖Element Plus表单标签样式 */
      .el-form :deep(.el-form-item__label) {
        color: white !important;
      }
      
      /* 全局覆盖Element Plus相关CSS变量 */
      .search-card,
      .search-form {
        --el-form-item-label-color: white !important;
        --el-color-text-primary: white !important;
        --el-color-text-regular: white !important;
        --el-color-text-secondary: white !important;
      }
      
      .search-form .el-form-item {
        margin-right: 10px;
        margin-bottom: 0;
      }
      
      .search-form .el-form-item:last-child {
        margin-right: 0;
      }
      
      /* 订单编号输入框背景 - 最高优先级 */
      .el-input__wrapper,
      .el-input__wrapper[tabindex="-1"] {
        background-color: #48494c !important;
        --el-input-bg-color: #48494c !important;
        --el-input-group-bg-color: #48494c !important;
        box-shadow: none !important;
        border-color: #48494c !important;
        background-image: none !important;
      }
      
      /* 确保输入框内部元素也使用相同背景 */
      .search-card .el-input__inner {
        background-color: #48494c !important;
        color: white !important;
      }
      
      /* 状态选择器背景 - 最高优先级 - 设置为深色 */
      :deep(.el-select__wrapper),
      :deep(.el-select__wrapper.el-tooltip__trigger),
      :deep(.el-select__wrapper.el-tooltip__trigger.el-tooltip__trigger) {
        background-color: #48494c !important;
        --el-select-bg-color: #48494c !important;
        --el-bg-color: #48494c !important;
        box-shadow: 0 0 0 1px white !important;
        border-color: #48494c !important;
        background-image: none !important;
      }
      
      /* 确保状态选择器的文字颜色为白色 */
      :deep(.el-select__wrapper .el-select__placeholder) {
        color: white !important;
      }
      
      :deep(.el-select__wrapper .el-select__selection span) {
        color: white !important;
      }
      
      /* 确保状态选择器的图标颜色为白色 */
      :deep(.el-select__wrapper .el-select__icon) {
        color: white !important;
      }
      
      /* 确保选中后文字颜色为白色 */
      :deep(.el-select__wrapper .el-select__input) {
        color: white !important;
      }
      
      /* 确保select组件的文本颜色为白色 - 全局覆盖 */
      :deep(.el-select) {
        --el-color-primary: white !important;
        --el-text-color-primary: white !important;
        --el-text-color-regular: white !important;
      }
      
      /* 日期选择器背景 */
      .el-date-editor {
        background-color: #48494c !important;
        --el-date-editor-bg-color: #48494c !important;
      }
      
      /* 日期选择器下拉菜单白色背景 - 使用:deep()提高优先级 */
      :deep(.el-picker-panel) {
        --el-picker-panel-bg-color: white !important;
        background-color: white !important;
        background: white !important;
        --el-bg-color: white !important;
        --el-bg-color-overlay: white !important;
        border-color: #dcdfe6 !important;
      }
      
      :deep(.el-date-picker) {
        background-color: white !important;
        background: white !important;
      }
      
      :deep(.el-date-editor + .el-picker-panel) {
        background-color: white !important;
      }
      
      /* 确保下拉菜单内部元素也有白色背景 */
      :deep(.el-picker-panel__content) {
        background-color: white !important;
      }
      
      :deep(.el-date-editor__wrapper) {
        background-color: white !important;
      }
      
      :deep(.el-date-table) {
        background-color: white !important;
      }
      
      :deep(.el-date-table-header),
      :deep(.el-date-table__row) {
        background-color: white !important;
      }
      
      :deep(.el-time-panel) {
        background-color: white !important;
      }
      
      :deep(.el-time-spinner) {
        background-color: white !important;
      }
      
      /* 覆盖全局的下拉选择器样式 - 确保状态下拉选择框背景为白色 */
      :deep(.el-select-dropdown),
      :deep(.el-select-dropdown-menu),
      :deep(.el-select__popper),
      :deep(.el-popper),
      :deep(.el-popper__inner) {
        background-color: white !important;
        background: white !important;
        --el-select-dropdown-bg-color: white !important;
        --el-bg-color: white !important;
        --el-bg-color-overlay: white !important;
        border-color: #dcdfe6 !important;
      }
      
      /* 确保下拉菜单内部元素也有白色背景 */
      :deep(.el-select-dropdown__wrap),
      :deep(.el-select-dropdown__list) {
        background-color: white !important;
      }
      
      /* 确保下拉菜单项的文字颜色为黑色 */
      :deep(.el-select-dropdown__item) {
        color: #303133 !important;
        background-color: white !important;
      }
      
      :deep(.el-select-dropdown__item:hover) {
        background-color: #f5f7fa !important;
      }
      
      :deep(.el-select-dropdown__item.selected) {
        background-color: #ecf5ff !important;
        color: #409eff !important;
      }
    }

  /* 使用更高优先级的选择器确保背景渐变正确应用 */
  .table-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: #464e58 !important;
    border: none !important;
    box-shadow: none !important;
    
    /* 为卡片内的所有元素设置渐变背景 */
    & > * {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
  }
  
  /* 表格样式 - 重置所有表格相关CSS变量 */
  :deep(.el-table) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-table-bg-color: transparent !important;
    --el-table-header-bg-color: transparent !important;
    --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.05) !important;
    --el-bg-color: transparent !important;
    --el-card-bg-color: transparent !important;
    color: white !important; /* 设置表格文字为白色 */
    width: 100% !important; /* 确保表格完全适应容器 */
    table-layout: fixed !important; /* 使用固定布局确保列宽一致 */
  }
  
  /* 移除表格右侧空白区域 */
  :deep(.el-table--layout-fixed) {
    width: 100% !important;
    table-layout: fixed !important; /* 确保固定布局 */
  }
  
  /* 确保表格内部wrapper正确显示 */
  :deep(.el-table__inner-wrapper) {
    overflow-x: hidden !important;
  }
  
  /* 确保右侧固定列正确显示，没有多余空白 */
  :deep(.el-table__fixed-right) {
    right: 0 !important;
    height: calc(100% - 0px) !important;
  }
  
  /* 表格内部元素样式 - 重置背景色 */
  :deep(.el-table__inner-wrapper) {
    background: transparent !important;
  }
  
  /* 表格表头行（标题行）样式 - 最高优先级组合 */
  .table-card :deep(.el-table__header-wrapper),
  .table-card :deep(.el-table__header),
  .table-card :deep(.el-table__header-wrapper) table,
  .table-card :deep(.el-table__header) table {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: transparent !important;
    border: none !important;
    color: white !important; /* 设置表头文字为白色 */
    table-layout: fixed !important; /* 表头表格使用固定布局 */
    width: 100% !important;
  }
  
  /* 隐藏多余的左侧补丁div */
  .table-card :deep(.el-table__border-left-patch) {
    display: none !important;
  }
  
  /* 表头行及其单元格 - 最精确选择器 */
  .table-card :deep(.el-table__header-row) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: transparent !important;
    color: white !important; /* 设置表头行文字为白色 */
    width: 100% !important;
  }
  
  .table-card :deep(.el-table__header-cell) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: transparent !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important; /* 设置表头单元格文字为白色 */
    /* 确保表头单元格宽度与数据单元格一致 */
    box-sizing: border-box !important;
    min-width: 0 !important;
  }
  
  /* 确保表头行在各种状态下都保持渐变背景 */
  .table-card :deep(.el-table__header-row:hover),
  .table-card :deep(.el-table__header-row.hover) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: white !important;
  }
  
  .table-card :deep(.el-table__header-cell:hover),
  .table-card :deep(.el-table__header-cell.hover) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: white !important;
  }
  
  /* 全局覆盖样式 - 最高优先级 */
  :global(.el-table__header-wrapper),
  :global(.el-table__header),
  :global(.el-table__header-wrapper) table,
  :global(.el-table__header) table {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: white !important;
    table-layout: fixed !important;
    width: 100% !important;
  }
  
  :global(.el-table__header-row) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: white !important;
    width: 100% !important;
  }
  
  :global(.el-table__header-cell) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
    box-sizing: border-box !important;
    min-width: 0 !important;
  }
  
  /* 表格单元格样式 */
  :deep(.el-table__cell) {
    background: transparent !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important; /* 设置表格单元格文字为白色 */
    box-sizing: border-box !important;
    min-width: 0 !important;
  }
  
  /* 表格行样式 */
  :deep(.el-table__row) {
    color: white !important; /* 设置表格行文字为白色 */
    width: 100% !important;
  }
  
  /* 分页样式 */
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 全局表格相关组件样式 */
  :global(.el-table__inner-wrapper),
  :global(.el-table__header-wrapper),
  :global(.el-table__body-wrapper),
  :global(.el-table__header),
  :global(.el-table__body),
  :global(.el-table__row),
  :global(.el-table__row--striped) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保表格主体内容完全填充容器 */
  :deep(.el-table__body) {
    width: 100% !important;
    table-layout: fixed !important; /* 数据表格也使用固定布局 */
  }
  
  /* 处理表格滚动相关样式 */
  :deep(.el-table__body-wrapper.is-scrolling-left) {
    padding-bottom: 0 !important;
  }
  
  /* 隐藏可能导致右侧空白的右侧补丁 */
  :deep(.el-table__border-right-patch) {
    display: none !important;
  }
  
  /* 确保表头和数据列宽严格一致 */
  :deep(.el-table__header colgroup),
  :deep(.el-table__body colgroup) {
    table-layout: fixed !important;
  }
  
  /* 确保每个列的宽度严格遵循定义 */
  :deep(.el-table__header col),
  :deep(.el-table__body col) {
    width: inherit !important;
    min-width: 0 !important;
    box-sizing: border-box !important;
  }

  /* 新增订单对话框中输入框的背景颜色和白色边框 */
  :deep(.el-dialog .el-input__wrapper),
  :deep(.el-dialog .el-input__wrapper[tabindex="-1"]) {
    background-color: #48494c !important;
    --el-input-bg-color: #48494c !important;
    --el-input-group-bg-color: #48494c !important;
    box-shadow: none !important;
    border-color: #ffffff !important; /* 白色边框 */
    border-width: 1px !important; /* 确保边框宽度 */
    border-style: solid !important; /* 确保边框样式 */
    background-image: none !important;
  }

  /* 确保对话框中输入框内部元素也使用相同背景 */
  :deep(.el-dialog .el-input__inner) {
    background-color: #48494c !important;
    color: white !important;
    border: none !important; /* 移除内部边框，只保留外部边框 */
  }
  
  /* 新增订单对话框中商品列表的下拉菜单选择器背景颜色 */
  :deep(.el-dialog .el-select__wrapper),
  :deep(.el-dialog .el-select__wrapper.el-tooltip__trigger) {
    background-color: #48494c !important;
    --el-select-bg-color: #48494c !important;
    --el-bg-color: #48494c !important;
    box-shadow: none !important;
    border-color: #ffffff !important;
    border-width: 1px !important;
    border-style: solid !important;
    background-image: none !important;
  }
  
  /* 确保下拉选择器的文字颜色为白色 */
  :deep(.el-dialog .el-select__selection .el-select__placeholder) {
    color: white !important;
  }
  
  :deep(.el-dialog .el-select__selection .el-select__selected-item) {
    color: white !important;
  }
  
  /* 新增订单对话框中数字输入框（el-input-number）的加减号按钮背景颜色为白色 */
  :deep(.el-dialog .el-input-number .el-input-number__decrease),
  :deep(.el-dialog .el-input-number .el-input-number__increase) {
    color: #48494c !important; /* 修改文字颜色为深色，确保在白色背景下可见 */
    border-color: #ffffff !important;
    --el-border-color: #ffffff !important;
    --el-input-number-button-bg-color: #ffffff !important; /* 背景颜色为白色 */
    background-color: #ffffff !important; /* 直接设置背景颜色为白色 */
  }
  
  /* 确保按钮悬停状态下背景颜色仍为白色 */
  :deep(.el-dialog .el-input-number .el-input-number__decrease:hover:not(.is-disabled)),
  :deep(.el-dialog .el-input-number .el-input-number__increase:hover:not(.is-disabled)) {
    color: #48494c !important;
    background-color: #ffffff !important; /* 悬停时背景颜色保持白色 */
    border-color: #ffffff !important;
  }
  
  /* 确保按钮禁用状态的样式 */
  :deep(.el-dialog .el-input-number .el-input-number__decrease.is-disabled),
  :deep(.el-dialog .el-input-number .el-input-number__increase.is-disabled) {
    color: #c0c0c0 !important;
    border-color: #c0c0c0 !important;
    background-color: #f5f5f5 !important; /* 禁用状态使用浅灰色背景 */
  }

  /* 新增订单对话框中的取消按钮样式 - 默认状态文字颜色为#48494c */
  :deep(.dialog-cancel-btn) {
    color: #48494c !important;
  }
  
  /* 确保取消按钮内部span元素的文字颜色也正确设置 */
  :deep(.dialog-cancel-btn span) {
    color: #48494c !important;
  }
  
  /* 取消按钮鼠标悬停状态样式 - 文字颜色变为#409eff */
  :deep(.dialog-cancel-btn:hover) {
    color: #409eff !important;
  }
  
  /* 确保取消按钮悬停时内部span元素的文字颜色也正确设置 */
  :deep(.dialog-cancel-btn:hover span) {
    color: #409eff !important;
  }
  
  /* 新增订单对话框中的删除按钮样式 */
  :deep(.el-dialog .el-button--danger) {
    background-color: #ffffff !important;
    border-color: #ffffff !important;
    color: #274151 !important;
  }
  
  /* 删除按钮鼠标悬停状态 */
  :deep(.el-dialog .el-button--danger:hover:not(.is-disabled)) {
    background-color: #48494c !important;
    border-color: #ffffff !important;
    color: #E4656C !important;
  }
  
  /* 确保删除按钮内部文字的颜色也正确设置 */
  :deep(.el-dialog .el-button--danger span) {
    color: #E4656C !important;
  }
  
  :deep(.el-dialog .el-button--danger:hover:not(.is-disabled) span) {
    color: #E4656C !important;
  }
  
  /* 为按钮设置默认状态下的背景颜色和白色边框 */
  .rounded-button:not(.is-disabled):not(.el-button--danger) {
    background-color: white !important;
    color: #274151 !important;
    border: 1px solid #ffffff !important;
  }
  
  /* 批量删除按钮默认背景颜色为#c0c0c0并添加白色边框 */
  .rounded-button:not(.is-disabled).el-button--danger {
    background-color: #ffffff !important;
    border: 1px solid #ffffff !important;
  }
  
  /* 确保禁用状态下也有白色边框和白色背景 */
  .rounded-button.is-disabled {
    border: 1px solid #ffffff !important;
    background-color: #ffffff !important;
  }

  /* 为这四个按钮添加鼠标悬停效果 */
  .rounded-button:hover:not(.is-disabled) {
    background-color: #5a5b5f !important;
    color: #274151 !important;
    border-color: #ffffff !important;
  }
  
  /* 批量删除按钮特殊处理 - 直接设置文字颜色 */
  :global(.el-button.rounded-button.el-button--danger) {
    color: #E4656C !important;
  }
  
  /* 批量删除按钮内的span标签文字颜色 */
  :global(.el-button.rounded-button.el-button--danger span) {
    color: #E4656C !important;
  }
  
  /* 禁用状态的批量删除按钮文字颜色 */
  :global(.el-button.rounded-button.el-button--danger.is-disabled) {
    color: #c0c0c0 !important;
    opacity: 1 !important;
  }
  
  /* 禁用状态的批量删除按钮内span标签文字颜色 */
  :global(.el-button.rounded-button.el-button--danger.is-disabled span) {
    color: #c0c0c0 !important;
    opacity: 1 !important;
  }
  
  /* 极高优先级的规则 */
  :global(.rounded-button.el-button--danger) {
    --el-button-text-color: #E4656C !important;
  }
  
  :global(.rounded-button.el-button--danger span) {
    color: #E4656C !important;
    color: #E4656C !important; /* 重复声明以提高优先级 */
  }

  /* 导出按钮文字颜色设置 */
  :global(.el-button.rounded-button.export-button span) {
    color: #67C23A !important;
  }
  
  /* 分页组件中的span元素文字颜色设置为白色 */
  :global(.pagination) span {
    color: white !important;
  }
  
  /* 确保分页组件中所有span元素文字颜色为白色 */
  :global(.el-pagination) span {
    color: white !important;
  }
  
  /* 针对分页组件中的total元素 */
  :global(.el-pagination__total) {
    color: white !important;
  }
  
  :global(.el-pagination__total span) {
    color: white !important;
  }
  
  /* 针对分页组件中的Go to文字 */
  :global(.el-pagination__goto) {
    color: white !important;
  }
  
  :global(.el-pagination__goto span) {
    color: white !important;
  }
  
  /* 增加分页组件中按钮和页码元素之间的间隙 */
  :global(.el-pagination__prev),
  :global(.el-pagination__next),
  :global(.el-pagination__item),
  :global(.el-pagination__btn) {
    margin: 0 12px !important;
    padding: 0 4px !important;
  }
  
  /* 确保页码列表项之间的间距 */
  :global(.el-pagination li) {
    margin: 0 8px !important;
  }
  
  /* 为分页组件中的五个区域设置背景颜色、白色边框和白色文字，并移除鼠标悬停效果 */
  :global(.pagination .el-input__wrapper),
  :global(.pagination .el-input__wrapper[tabindex="-1"]),
  :global(.pagination .btn-prev),
  :global(.pagination .btn-next),
  :global(.pagination .el-pagination li),
  :global(.pagination .el-select__wrapper),
  :global(.pagination .el-select__wrapper.el-tooltip__trigger) {
    background-color: #48494c !important;
    --el-bg-color: #48494c !important;
    border: 1px solid #ffffff !important;
    box-shadow: none !important;
    background-image: none !important;
    color: white !important;
  }
  
  /* 确保输入框内的文字颜色为白色 */
  :global(.pagination .el-input__inner) {
    color: white !important;
  }
  
  /* 确保下拉选择器内的文字颜色为白色 */
  :global(.pagination .el-select__selected-item),
  :global(.pagination .el-select__placeholder) {
    color: white !important;
  }
  
  /* 确保下拉选择器内的span文字颜色为白色 */
  :global(.pagination .el-select__placeholder span),
  :global(.pagination .el-select__selected-item span) {
    color: white !important;
  }
  
  /* 确保按钮内的图标颜色为白色 */
  :global(.pagination .btn-prev i),
  :global(.pagination .btn-next i),
  :global(.pagination .btn-prev .el-icon),
  :global(.pagination .btn-next .el-icon) {
    color: white !important;
  }
  
  /* 确保页码li元素内的文字颜色为白色 */
  :global(.pagination .el-pagination li.is-active.number) {
    color: white !important;
  }
  
  /* 移除上一页和下一页按钮的悬停效果 */
  :global(.pagination .btn-prev:hover),
  :global(.pagination .btn-next:hover),
  :global(.pagination .el-pagination__prev:hover),
  :global(.pagination .el-pagination__next:hover),
  :global(.pagination .el-pagination__btn:hover) {
    background-color: #48494c !important;
    color: white !important;
    border-color: #ffffff !important;
  }
  
  /* 移除页码项的悬停效果 */
  :global(.pagination .el-pagination li:hover),
  :global(.pagination .el-pagination__item:hover) {
    background-color: #48494c !important;
    color: white !important;
    border-color: #ffffff !important;
  }
  
  /* 移除输入框的悬停效果 */
  :global(.pagination .el-input__wrapper:hover) {
    background-color: #48494c !important;
    --el-bg-color: #48494c !important;
    border-color: #ffffff !important;
    box-shadow: none !important;
  }
  
  /* 移除下拉选择器的悬停效果 */
  :global(.pagination .el-select__wrapper:hover) {
    background-color: #48494c !important;
    --el-bg-color: #48494c !important;
    border-color: #ffffff !important;
    box-shadow: none !important;
  }
}

.order-items {
  .item-row {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
    
    .el-select {
      width: 200px;
    }
    
    .el-input-number {
      width: 120px;
    }
    
    .el-input {
      width: 120px;
    }
  }
}
</style>