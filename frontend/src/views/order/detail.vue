<template>
  <div class="order-detail">
    <el-descriptions title="订单信息" :column="2" border>
      <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="getStatusType(order.status)">
          {{ getStatusText(order.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="收货人">{{ order.consignee }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item>
      <el-descriptions-item label="收货地址" :span="2">{{ order.address }}</el-descriptions-item>
      <el-descriptions-item label="订单备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
    </el-descriptions>

    <el-card class="order-items" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>商品信息</span>
        </div>
      </template>
      <el-table :data="order.items" border>
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.productImage ? ('/api' + scope.row.productImage) : '/images/product-icon.svg'"
              fit="contain"
              style="width: 50px; height: 50px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="scope">{{ scope.row.price !== null ? '￥' + scope.row.price : '未定价' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="120" />
        <el-table-column label="小计" width="120">
          <template #default="scope">￥{{ scope.row.totalAmount }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="order-footer" style="margin-top: 20px; text-align: right">
      <span class="total-amount">
        订单总金额：<span class="amount">￥{{ order.totalAmount }}</span>
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const order = ref({})

const getStatusText = (status) => {
  const statusMap = {
    '-1': '已取消',
    '0': '待支付',
    '1': '已支付',
    '2': '已发货',
    '3': '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    '-1': 'danger',
    '0': 'info',
    '1': 'success',
    '2': 'warning',
    '3': 'primary'
  }
  return typeMap[status] || 'info'
}

const getDetail = async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

onMounted(() => {
  getDetail()
})
</script>

<style lang="scss" scoped>
.order-detail {
  padding: 20px;

  /* 为订单信息描述组件中的内容单元格添加背景渐变 */
  :deep(.el-descriptions__cell.el-descriptions__content.is-bordered-content) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
  
  /* 为订单信息描述组件中的标签单元格设置背景颜色 */
  :deep(.el-descriptions__cell.el-descriptions__label.is-bordered-label) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
  
  /* 为订单信息标题设置白色文字颜色 */
  :deep(.el-descriptions__title) {
    color: #ffffff !important;
  }

  .order-items {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: 1px solid #ffffff !important;
  }
  
  /* 为商品信息标题设置白色文字颜色 */
  .card-header span {
    color: #ffffff !important;
  }

  /* 为商品信息表格设置样式 */
  :deep(.el-table) {
    color: #ffffff !important;
  }

  :deep(.el-table th.el-table__cell) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border-color: #ffffff !important;
    color: #ffffff !important;
  }

  :deep(.el-table td.el-table__cell) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border-color: #ffffff !important;
    color: #ffffff !important;
  }

  .total-amount {
    font-size: 16px;
    color: #ffffff !important;
    
    .amount {
      color: #f56c6c;
      font-size: 20px;
      font-weight: bold;
    }
  }
}
</style>