<template>
  <div class="supplier-detail">
    <el-descriptions title="供应商信息" :column="2" border>

      <el-descriptions-item label="供应商名称">{{ supplier.name }}</el-descriptions-item>
      <el-descriptions-item label="供应商编码">{{ supplier.code }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ supplier.phone }}</el-descriptions-item>
      <el-descriptions-item label="状态" :span="2">
        <el-tag :type="getStatusType(supplier.status)">
          {{ getStatusText(supplier.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="邮箱地址" :span="2">{{ supplier.email || '无' }}</el-descriptions-item>
      <el-descriptions-item label="地址" :span="2">{{ supplier.address || '无' }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ supplier.remark || '无' }}</el-descriptions-item>
    </el-descriptions>

    <el-card class="supplier-items" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>关联产品信息</span>
          <el-button type="primary" @click="handleAddProduct" class="rounded-button">新增产品</el-button>
        </div>
      </template>
      <el-table :data="supplier.supplierProducts || []" border>
        <el-table-column prop="id" label="产品ID" width="200" />
        <el-table-column prop="productName" label="产品名称" width="200" />
        <el-table-column prop="property" label="属性" width="200">
          <template #default="scope">
            <span :class="[
                'property-type',
                scope.row.property === 1 ? 'property-raw' :
                scope.row.property === 2 ? 'property-semi' :
                'property-finished'
              ]">
              {{ scope.row.property === 1 ? '原料' :
                 scope.row.property === 2 ? '半成品' :
                 '成品' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="240">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="240">
          <template #default="scope">
            {{ formatDate(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <!-- 操作列 -->
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑产品对话框 -->
    <el-dialog title="编辑产品" v-model="editDialogVisible" width="600px" @close="resetForm">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="产品名称">
          <el-input v-model="editForm.productName" placeholder="请输入产品名称" />
        </el-form-item>

        <el-form-item label="属性">
        <el-select v-model="editForm.property" placeholder="选择属性" class="custom-bg-select">
            <el-option label="原料" :value="1" />
            <el-option label="半成品" :value="2" />
          </el-select></el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetForm">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增产品对话框 -->
    <el-dialog title="新增产品" v-model="addDialogVisible" width="600px" @close="resetAddForm">
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="120px">
        <el-form-item label="产品名称">
          <el-input v-model="addForm.productName" placeholder="请输入产品名称" />
        </el-form-item>

        <el-form-item label="属性">
        <el-select v-model="addForm.property" placeholder="选择属性" class="custom-bg-select">
            <el-option label="原料" :value="1" />
            <el-option label="半成品" :value="2" />
          </el-select></el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetAddForm">取消</el-button>
          <el-button type="primary" @click="submitAddProduct">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSupplierDetail, updateSupplierItem, addSupplierItem } from '@/api/supplier'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const supplier = ref({ supplierProducts: [] })
// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({})
// 新增产品对话框
const addDialogVisible = ref(false)
const addForm = ref({})
const addFormRef = ref(null)
// 新增表单验证规则
const addFormRules = ref({
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  property: [
    { required: true, message: '请选择属性', trigger: 'change' }
  ]
})

const getStatusText = (status) => {
  const statusMap = {
    0: '禁用',
    1: '启用'
  }
  return statusMap[status] || '未知'
}

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

// 编辑产品
const handleEdit = (row) => {
  console.log('编辑产品:', row)
  // 打开编辑对话框并填充表单数据
  editForm.value = { ...row }
  editDialogVisible.value = true
}

// 删除产品
const handleDelete = (row) => {
  console.log('删除产品:', row)
  // 这里可以调用删除产品的API，例如：
  // deleteProduct(row.productId).then(() => {
  //   ElMessage.success('删除产品成功')
  //   getDetail() // 刷新数据
  // })
}

// 新增产品
const handleAddProduct = () => {
  console.log('新增产品');
  // 打开新增对话框并填充表单数据
  addDialogVisible.value = true;
  addForm.value = { supplierId: supplier.value.id };
};

// 重置新增表单
const resetAddForm = () => {
  addDialogVisible.value = false
  addForm.value = {}
  addFormRef.value?.resetFields()
};

// 提交编辑
const submitEdit = async () => {
  try {
    const itemToUpdate = {
      id: editForm.value.id,
      productName: editForm.value.productName,
      property: editForm.value.property
    }
    await updateSupplierItem(itemToUpdate)
    ElMessage.success('产品编辑成功')
    editDialogVisible.value = false
    getDetail() // 刷新数据
  } catch (error) {
    console.error('产品编辑失败:', error)
    ElMessage.error('产品编辑失败')
  }
}

// 提交新增产品
const submitAddProduct = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const itemToAdd = {
          supplierId: addForm.value.supplierId,
          productName: addForm.value.productName,
          property: Number(addForm.value.property)
        }
        await addSupplierItem(itemToAdd)
        ElMessage.success('新增产品成功')
        addDialogVisible.value = false
        getDetail() // 刷新数据
      } catch (error) {
        ElMessage.error('新增产品失败：' + error.message)
      }
    } else {
      ElMessage.warning('请完成表单验证')
    }
  })
}

// 重置表单
const resetForm = () => {
  editForm.value = {}
  editDialogVisible.value = false
}

const getDetail = async () => {
  try {
    const res = await getSupplierDetail(route.params.id)
    console.log('API响应:', res)
    console.log('响应数据:', res.data)
    console.log('supplierProducts:', res.data.supplierProducts)
    supplier.value = res.data
    console.log('更新后的supplier:', supplier.value)
  } catch (error) {
    console.error('获取供应商详情失败:', error)
    ElMessage.error('获取供应商详情失败')
  }
}

onMounted(() => {
  getDetail()
})
</script>

<style lang="scss" scoped>
.supplier-detail {
  padding: 20px;
}

/* 修改供应商信息描述列表的标签背景颜色 */
:deep(.el-descriptions__label.is-bordered-label) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #fff;
}
/* 修改供应商信息描述列表的内容单元格文字颜色 */
:deep(.el-descriptions__cell.el-descriptions__content.is-bordered-content) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #fff;
}
/* 修改供应商信息描述列表的标题文字颜色 */
:deep(.el-descriptions__title) {
  color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 为关联产品信息卡片设置背景 */
.supplier-items {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border: 1px solid #ffffff !important;
}

/* 修改卡片body的背景 */
:deep(.supplier-items .el-card__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  padding: 16px;
}

/* 为卡片标题设置白色文字颜色 */
.card-header span {
  color: #ffffff !important;
}

/* 为关联产品信息表格设置统一背景 */
:deep(.el-table),
:deep(.el-table__inner-wrapper),
:deep(.el-table__body-wrapper),
:deep(.el-table__body),
:deep(.el-table__empty-block),
:deep(.el-table th.el-table__cell),
:deep(.el-table td.el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border-color: #ffffff !important;
  color: #ffffff !important;
}

/* 确保空数据块充满整个表格区域 */
:deep(.el-table__empty-block) {
  height: 100%;
  width: 100%;
}

/* 圆角按钮样式 */
.rounded-button {
  background-color: white !important;
  color: #274151 !important;
  border: 1px solid #ffffff !important;
  transition: all 0.3s ease !important;
}

/* 确保禁用状态下也有白色边框和白色背景 */
.rounded-button.is-disabled {
  border: 1px solid #ffffff !important;
  background-color: #ffffff !important;
}

/* 为按钮添加鼠标悬停效果 */
.rounded-button:hover:not(.is-disabled) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
}

/* 极高优先级的规则 - 确保主按钮文字颜色 */
:global(.rounded-button.el-button--primary) {
  --el-button-text-color: #274151 !important;
}

:global(.rounded-button.el-button--primary span) {
  color: #274151 !important;
}

/* 新增产品对话框的背景颜色 */
:global(.el-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保对话框内的文字颜色与深色背景协调 */
:global(.el-dialog .el-dialog__title) {
  color: white !important;
}

:global(.el-dialog .el-form-item__label) {
  color: white !important;
}

/* 为对话框中的输入框和选择器添加背景颜色 */
:global(.el-dialog .el-input__wrapper),
:global(.el-dialog .el-select__wrapper) {
  background-color: #48494c !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
}

/* 设置输入框和选择器聚焦/点击时的边框颜色为白色 */
:global(.el-dialog .el-input__wrapper.is-focus),
:global(.el-dialog .el-input__wrapper:hover),
:global(.el-dialog .el-select__wrapper.is-focus),
:global(.el-dialog .el-select__wrapper:hover) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset !important;
}

:global(.el-dialog .el-input__inner),
:global(.el-dialog .el-select__placeholder) {
  color: #ffffff !important;
}

/* 直接针对所有下拉选择器的全局样式，确保能够覆盖默认样式 */
:global(.el-select-dropdown) {
  background-color: #48494c !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  z-index: 9999 !important;
}

/* 设置所有下拉选项的默认样式 */
:global(.el-select-dropdown__item) {
  color: #ffffff !important;
  background-color: #48494c !important;
  transition: background-color 0.2s ease !important;
}

/* 设置所有下拉选项的悬停样式 */
:global(.el-select-dropdown__item:hover),
:global(.el-select-dropdown__item.is-hovering),
:global(.el-select-dropdown__item.selected),
:global(.el-select-dropdown__item:focus) {
  background-color: #5A5B5F !important;
  color: #ffffff !important;
}

/* 属性类型样式 */
.property-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.property-raw {
  background-color: #ecf5ff;
  color: #409eff;
}

.property-semi {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.property-finished {
  background-color: #f0f9eb;
  color: #67c23a;
}
</style>