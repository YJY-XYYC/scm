<template>
  <div class="order-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="orderList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="订单编号" prop="orderNo" />
        <el-table-column label="总金额" prop="totalAmount" />
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleDetail(scope.row)">详情</el-button>
            <el-button type="danger" link @click="handleCancel(scope.row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

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
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { getOrderList, cancelOrder } from '@/api/order'
import { ElMessage } from 'element-plus'

export default {
  name: 'OrderIndex',
  setup() {
    const loading = ref(false)
    const orderList = ref([])
    const total = ref(0)
    const queryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      orderNo: '',
      status: null
    })

    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待支付'
        case 1: return '已支付'
        case 2: return '已发货'
        case 3: return '已完成'
        case -1: return '已取消'
        default: return '未知'
      }
    }

    const getStatusTagType = (status) => {
      switch (status) {
        case 0: return 'info'
        case 1: return 'success'
        case 2: return 'warning'
        case 3: return 'primary'
        case -1: return 'danger'
        default: return ''
      }
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const { data } = await getOrderList(queryParams)
        orderList.value = data.records
        total.value = data.total
      } catch (error) {
        ElMessage.error('获取订单列表失败')
      } finally {
        loading.value = false
      }
    }

    const resetQuery = () => {
      queryParams.orderNo = ''
      queryParams.status = null
      handleQuery()
    }

    const handleDetail = (order) => {
      // 跳转到订单详情页
    }

    const handleCancel = async (order) => {
      try {
        await cancelOrder(order.id)
        ElMessage.success('订单取消成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('取消订单失败')
      }
    }

    const handleSelectionChange = (selection) => {
      // 处理选择变化
    }

    const handleSizeChange = (size) => {
      queryParams.pageSize = size
      handleQuery()
    }

    const handleCurrentChange = (page) => {
      queryParams.pageNum = page
      handleQuery()
    }

    onMounted(() => {
      handleQuery()
    })

    return {
      loading,
      orderList,
      total,
      queryParams,
      getStatusText,
      getStatusTagType,
      handleQuery,
      resetQuery,
      handleDetail,
      handleCancel,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.order-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
  background-color: white !important;
  border-radius: 4px;
  
  .search-form {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .el-card__body {
    padding: 16px !important;
    border: none !important;
    background-color: white !important;
  }
}

.table-card {
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>