<template>
  <div class="product-detail">
    <el-card class="detail-card">
      <div class="card-header">
        <h2>产品详情</h2>
        <el-button type="primary" @click="goBack" class="rounded-button">返回列表</el-button>
      </div>
      <el-descriptions title="基本信息" :column="2" border style="margin-top: 20px;">
        <el-descriptions-item label="产品ID">{{ product.id }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ product.code }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ product.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ product.category }}</el-descriptions-item>
        <el-descriptions-item label="价格">{{ product.price !== null ? product.price + '元' : '未定价' }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ product.stock }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(product.status)">{{ getStatusText(product.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(product.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(product.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-card class="detail-section" style="margin-top: 20px;">
        <template #header>
          <div class="section-title">产品描述</div>
        </template>
        <div class="description-content">
          {{ product.description || '暂无描述' }}
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref({})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '下架',
    1: '上架'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success'
  }
  return typeMap[status] || 'info'
}

// 日期格式化函数
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 返回列表
const goBack = () => {
  router.push('/product/selection')
}

// 获取产品详情
const getDetail = async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error('获取产品详情失败:', error)
    ElMessage.error('获取产品详情失败')
  }
}

onMounted(() => {
  getDetail()
})
</script>

<style lang="scss" scoped>
.product-detail {
  padding: 20px;
}

// rounded-button 样式定义
.rounded-button {
  background-color: #ffffff !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  transition: all 0.3s ease !important;
}

.rounded-button:hover:not(.is-disabled) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
}

.rounded-button.is-disabled {
  border: 1px solid #ffffff !important;
  background-color: #ffffff !important;
}

// Element Plus 主按钮特殊处理
:global(.rounded-button.el-button--primary) {
  --el-button-text-color: #274151 !important;
}

:global(.rounded-button.el-button--primary span) {
  color: #274151 !important;
}

.detail-card {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border: 1px solid #ffffff !important;
  .el-card__body {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  h2 {
    color: #ffffff !important;
    margin: 0;
  }
}

// 基本信息描述样式
:deep(.el-descriptions) {
  .el-descriptions__title {
    color: #ffffff !important;
  }
  .el-descriptions__label.is-bordered-label {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
  }
  .el-descriptions__cell.el-descriptions__content.is-bordered-content {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
  }
}

// 详情区块样式
.detail-section {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border: 1px solid #ffffff !important;
  .el-card__header {
    border-bottom: 1px solid #ffffff !important;
  }
  .section-title {
    color: #ffffff !important;
  }
  .el-card__body {
    padding: 15px;
  }
}

.description-content {
  color: #ffffff !important;
  line-height: 1.6;
}
</style>