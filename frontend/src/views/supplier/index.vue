<template>
  <div class="supplier-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="供应商搜索">
          <el-input v-model="queryParams.searchTerm" placeholder="请输入供应商名称或编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px;" @change="handleQuery">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" class="rounded-button">查询</el-button>
          <el-button type="primary" @click="resetQuery" class="rounded-button">重置</el-button>
          <el-button type="primary" @click="handleAdd" class="rounded-button">新增供应商</el-button>
          <el-button type="danger" @click="handleBatchDelete" class="rounded-button">批量删除</el-button>
          <el-button type="success" @click="handleExport" class="rounded-button export-button">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮和表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="supplierList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="name" label="供应商名称" />
        <el-table-column prop="code" label="供应商编码" />
        <el-table-column prop="contact" label="联系人" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="status" label="状态" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="primary"
              link
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important; display: flex; align-items: center; justify-content: right; padding: 1px 0;">
        <!-- 总数和每页大小 -->
        <div style="margin-right: 20px;">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            layout="total, sizes"
            @size-change="handleSizeChange"
            style="background: transparent !important;"
          />
        </div>
        
        <!-- 前一页按钮 -->
        <div>
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            :total="total"
            layout="prev"
            @current-change="handleCurrentChange"
            style="background: transparent !important;"
          />
        </div>
        
        <!-- 空白间隔 - 前一页和页码之间 -->
        <div style="width: 10px;"></div>
        
        <!-- 页码 -->
        <div>
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            :total="total"
            layout="pager"
            @current-change="handleCurrentChange"
            style="background: transparent !important;"
          />
        </div>
        
        <!-- 空白间隔 - 页码和后一页之间 -->
        <div style="width: 10px;"></div>
        
        <!-- 后一页按钮 -->
        <div>
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            :total="total"
            layout="next"
            @current-change="handleCurrentChange"
            style="background: transparent !important;"
          />
        </div>
        
        <!-- 跳转功能 -->
        <div style="margin-left: 20px;">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            :total="total"
            layout="jumper"
            @current-change="handleCurrentChange"
            style="background: transparent !important;"
          />
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="supplierFormRef"
        :model="supplierForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierForm.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="供应商编码" prop="code">
          <el-input v-model="supplierForm.code" placeholder="请输入供应商编码" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="supplierForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="supplierForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱地址" prop="email">
          <el-input v-model="supplierForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="supplierForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="supplierForm.remark"
            type="textarea"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useRouter } from 'vue-router'
import { Download } from '@element-plus/icons-vue'
import { getSupplierList, addSupplier, updateSupplier, updateSupplierStatus, deleteSupplier, batchDeleteSuppliers, getSupplierDetail } from '@/api/supplier'
import { exportToExcel } from '@/utils/export'

// 路由实例
const router = useRouter()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  searchTerm: '',
  status: undefined
})

// 选中的行
const selectedRows = ref([])

// 处理选择变化
  const handleSelectionChange = (selection) => {
    selectedRows.value = selection
  }

// 供应商列表数据
const loading = ref(false)
const supplierList = ref([])
const total = ref(0)

// 对话框数据
const dialog = reactive({
  visible: false,
  title: '',
  type: 'add' // add or edit
})

// 表单数据
const supplierFormRef = ref()
const supplierForm = reactive({
  id: undefined,
  name: '',
  code: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  status: 1,
  remark: ''
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 获取供应商列表
const getList = async () => {
    try {
      loading.value = true
      // 构造请求参数
      const requestParams = {
        pageNum: queryParams.pageNum,
        pageSize: queryParams.pageSize,
        status: queryParams.status
      }
      
      // 根据搜索内容决定查询字段
      // 为了符合需求：输入供应商名字查询时从name字段查询，输入供应商编码时从code字段查询
      // 这里简化处理，前端统一传递参数，由后端根据参数智能判断
      if (queryParams.searchTerm) {
        // 前端传递搜索词到两个参数，但由后端决定使用哪个字段查询
        requestParams.name = queryParams.searchTerm
        requestParams.code = queryParams.searchTerm
      }
      
      const { data } = await getSupplierList(requestParams)
      supplierList.value = data.records
      total.value = data.total
    } catch (error) {
      console.error('获取供应商列表失败：', error)
      ElMessage.error('获取供应商列表失败')
    } finally {
      loading.value = false
    }
  }

// 查询按钮点击事件
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮点击事件
const resetQuery = () => {
  queryParams.searchTerm = ''
  queryParams.status = undefined
  handleQuery()
}

// 新增按钮点击事件
const handleAdd = () => {
  dialog.type = 'add'
  dialog.title = '新增供应商'
  dialog.visible = true
  Object.assign(supplierForm, {
    id: undefined,
    name: '',
    code: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    status: 1,
    remark: ''
  })
}

// 详情按钮点击事件
const handleDetail = (row) => {
  router.push(`/supplier/detail/${row.id}`)
}

// 编辑按钮点击事件
const handleEdit = (row) => {
  dialog.type = 'edit'
  dialog.title = '编辑供应商'
  dialog.visible = true
  Object.assign(supplierForm, row)
}

// 提交表单
const handleSubmit = async () => {
  await supplierFormRef.value.validate()
  try {
    console.log('提交表单数据:', { dialogType: dialog.type, formData: { ...supplierForm } })
    if (dialog.type === 'add') {
      await addSupplier(supplierForm)
      ElMessage.success('添加成功')
    } else {
      // 对于编辑操作，确保id存在
      if (!supplierForm.id) {
        throw new Error('供应商ID不存在')
      }
      await updateSupplier(supplierForm)
      ElMessage.success('修改成功')
    }
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('操作失败：', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 修改状态
const handleStatusChange = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要${row.status === 1 ? '禁用' : '启用'}该供应商吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    await updateSupplierStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    getList()
  } catch (error) {
    console.error('操作失败：', error)
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 单个删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除该供应商吗？',
      '警告',
      {
        type: 'warning'
      }
    )
    await deleteSupplier(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败：', error)
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请先选择要删除的供应商')
      return
    }
    
    await ElMessageBox.confirm(
      `确认要删除选中的${selectedRows.value.length}个供应商吗？`,
      '警告',
      {
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteSuppliers(ids)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败：', error)
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出
const handleExport = async () => {
  if (supplierList.value.length === 0) {
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
    
    // 批量获取所有供应商的完整详情（包含关联产品）
    const supplierDetails = await Promise.all(
      supplierList.value.map(supplier => getSupplierDetail(supplier.id))
    )
    
    // 转换数据格式，准备导出
    const exportData = []
    
    // 整合供应商和产品信息
    supplierDetails.forEach(detailRes => {
      const supplier = detailRes.data
      const supplierProducts = supplier.supplierProducts || []
      
      // 如果供应商没有关联产品，也要导出基本信息
      if (supplierProducts.length === 0) {
        exportData.push({
          供应商名称: supplier.name,
          供应商编码: supplier.code,
          联系人: supplier.contact,
          联系电话: supplier.phone,
          邮箱地址: supplier.email || '无',
          地址: supplier.address || '无',
          状态: supplier.status === 1 ? '启用' : '禁用',
          创建时间: supplier.createTime,
          备注: supplier.remark || '无',
          产品ID: '',
          产品名称: '',
          产品属性: '',
          产品创建时间: '',
          产品更新时间: ''
        })
      } else {
        // 有产品的情况，为每个产品创建一行数据
        supplierProducts.forEach(product => {
          exportData.push({
            供应商名称: supplier.name,
            供应商编码: supplier.code,
            联系人: supplier.contact,
            联系电话: supplier.phone,
            邮箱地址: supplier.email || '无',
            地址: supplier.address || '无',
            状态: supplier.status === 1 ? '启用' : '禁用',
            创建时间: supplier.createTime,
            备注: supplier.remark || '无',
            产品ID: product.id,
            产品名称: product.productName,
            产品属性: product.property === 1 ? '原料' : product.property === 2 ? '半成品' : '成品',
            产品创建时间: product.createTime || '',
            产品更新时间: product.updateTime || ''
          })
        })
      }
    })
    
    // 使用导出工具函数
    exportToExcel(exportData, '供应商数据')
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出供应商失败:', error)
    ElMessage.error('导出供应商失败')
  } finally {
    // 确保无论成功还是失败都关闭加载状态
    if (loadingInstance) {
      loadingInstance.close()
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 对话框关闭事件
const handleDialogClose = () => {
  supplierFormRef.value?.resetFields()
}

// 页面加载时获取列表数据
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.supplier-container {
  .search-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border-radius: 4px;
    border: 1px solid #ffffff !important;
  }

  .table-card {
    background: white !important;
    box-shadow: 0 10px 30px 0 rgba(0, 0, 0, 0.3) !important;
    border: 1px solid #e4e7ed !important;
    border-radius: 4px;
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
      background: white !important;
      padding: 16px;
      border-radius: 4px;
    }
  }
  
  // 为搜索表单设置渐变背景
  .search-form {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    padding: 16px;
    border-radius: 4px;
  }
  
  // 调整搜索卡片样式，移除白色边框
  .search-card {
    background: transparent !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  // 调整表格卡片样式，设置渐变背景并移除白色边框
  .table-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  // 调整el-card__body样式
  .search-card :deep(.el-card__body) {
    background: transparent !important;
    padding: 0 !important;
    border: none !important;
  }
  
  .table-card :deep(.el-card__body) {
    background: transparent !important;
    padding: 16px !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  /* 全局设置供应商容器的渐变背景 - 最外层容器 */
  .supplier-container {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-bg-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-bg-color-page: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-bg-color-overlay: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 为表格设置渐变背景 - 使用最高优先级 */
  :global(.supplier-container .el-table) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-table-bg-color: #464e58 !important;
    --el-table-header-bg-color: #464e58 !important;
    border: none !important;
    border-top: 1px solid #ffffff !important;
  }
  
  /* 直接为表格所有内部元素设置背景色 - 覆盖所有可能的白色背景 */
  :global(.supplier-container .el-table .el-table__inner-wrapper),
  :global(.supplier-container .el-table .el-table__header-wrapper),
  :global(.supplier-container .el-table .el-table__body-wrapper),
  :global(.supplier-container .el-table .el-table__header),
  :global(.supplier-container .el-table .el-table__body),
  :global(.supplier-container .el-table .el-table__header table),
  :global(.supplier-container .el-table .el-table__body table) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 表头和表体行样式 */
  :global(.supplier-container .el-table .el-table__header-row),
  :global(.supplier-container .el-table .el-table__row) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 表头单元格和普通单元格样式 */
  :global(.supplier-container .el-table .el-table__header-cell),
  :global(.supplier-container .el-table .el-table__cell) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
    border-bottom: 1px solid transparent !important;
    border-right: 1px solid transparent !important;
  }
  
  /* 单元格内容文字颜色 */
  :global(.supplier-container .el-table .el-table__cell > .cell) {
    color: #ffffff !important;
  }
  
  /* 隐藏所有分隔线和边框 */
  :global(.supplier-container .el-table::before),
  :global(.supplier-container .el-table::after),
  :global(.supplier-container .el-table .el-table__border-left-patch),
  :global(.supplier-container .el-table .el-table__border-right-patch) {
    display: none !important;
  }
  
  /* 导出按钮文字颜色设置 */
  :global(.el-button.rounded-button.export-button span) {
    color: #67C23A !important;
  }
  
  /* 分页组件样式 - 设置渐变背景 */
  .supplier-container .table-card .el-card__body .pagination {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    padding: 16px 0;
  }
  
  /* 分页组件内部元素样式 */
  .supplier-container .table-card .el-card__body .pagination .el-pagination {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  .supplier-container .table-card .el-card__body .pagination .el-pagination__total,
  .supplier-container .table-card .el-card__body .pagination .el-pagination__sizes,
  .supplier-container .table-card .el-card__body .pagination .el-pagination__jump {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
  }
  
  /* 分页按钮样式 */
  .supplier-container .table-card .el-card__body .pagination .btn-prev,
  .supplier-container .table-card .el-card__body .pagination .btn-next {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
    border: none !important;
  }
  
  .supplier-container .table-card .el-card__body .pagination .btn-prev:disabled,
  .supplier-container .table-card .el-card__body .pagination .btn-next:disabled {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: rgba(255, 255, 255, 0.5) !important;
  }
  
  /* 分页页码样式 */
  .supplier-container .table-card .el-card__body .pagination .el-pager li {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: #ffffff !important;
    border: none !important;
  }
  
  .supplier-container .table-card .el-card__body .pagination .el-pager li.is-active {
    background: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;
  }
  
  /* 确保卡片和内容区域也是渐变背景 */
  .supplier-container .table-card {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  .supplier-container .table-card .el-card__body {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    padding: 16px !important;
    border: none !important;
  }
  
  /* 覆盖Element Plus的默认表格样式 */
  .el-table {
    --el-table-header-bg-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    --el-table-body-row-hover-bg-color: rgba(255, 255, 255, 0.05) !important;
  }
  
  // 调整输入框样式 - 明确基础边框样式
  :deep(.el-input__wrapper) {
    background-color: #48494c !important;
    color: #ffffff !important;
    border: 1px solid #ffffff !important; /* 明确边框设置 */
    border-color: #ffffff !important;
    transition: none !important;
    box-shadow: none !important;
  }
  
  // 输入框选中时的边框样式 - 最高优先级
  .supplier-container :deep(.el-input__wrapper) {
    background-color: #48494c !important;
  }
  
  .supplier-container :deep(.el-input__wrapper:focus-within),
  .supplier-container :deep(.el-input__wrapper.is-focus),
  .supplier-container :deep(.el-input__wrapper:focus),
  .supplier-container :deep(.el-input__wrapper:focus-visible),
  .supplier-container :deep(.el-input__wrapper:hover) {
    border-color: #ffffff !important;
    border: 1px solid #ffffff !important; /* 明确白色边框 */
    box-shadow: none !important;
    outline: none !important;
    --el-input-focus-border: #ffffff !important;
  }
  
  :deep(.el-input__inner) {
    color: #ffffff !important;
    background: transparent !important;
    border: none !important;
  }
  
  // 确保输入框内部元素获得焦点时也保持白色边框
  :deep(.el-input__inner:focus) {
    outline: none !important;
    box-shadow: none !important;
    border-color: transparent !important;
  }
  
  // 为textarea输入框设置背景颜色和白色边框
  :deep(.el-textarea__inner) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
  
  // 确保textarea输入框获得焦点时也保持白色边框
  :deep(.el-textarea__inner:focus) {
    border-color: #ffffff !important;
    outline: none !important;
    box-shadow: none !important;
  }
  
  // 调整选择框样式 - 明确基础边框样式
  :deep(.el-select__wrapper) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important; /* 明确边框设置 */
    border-color: #ffffff !important;
    transition: none !important;
    box-shadow: none !important;
  }
  
  // 选择框选中时的边框样式 - 最高优先级
  .supplier-container :deep(.el-select__wrapper) {
    background-color: #48494c !important;
  }
  
  .supplier-container :deep(.el-select__wrapper:focus-within),
  .supplier-container :deep(.el-select__wrapper.is-focus),
  .supplier-container :deep(.el-select__wrapper:focus),
  .supplier-container :deep(.el-select__wrapper:focus-visible),
  .supplier-container :deep(.el-select__wrapper:hover) {
    border-color: #ffffff !important;
    border: 1px solid #ffffff !important; /* 明确白色边框 */
    box-shadow: none !important;
    outline: none !important;
    --el-select-focus-border: #ffffff !important;
  }
  
  :deep(.el-select__placeholder) {
    color: #cccccc !important;
  }
  
  /* 为新增/编辑对话框设置渐变背景 */
  :deep(.el-dialog) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保对话框内容区域也有渐变背景 */
  :deep(.el-dialog__body) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保对话框标题区域也有渐变背景 */
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保对话框底部区域也有渐变背景 */
  :deep(.el-dialog__footer) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保对话框标题文字颜色为白色 */
  :deep(.el-dialog__title) {
    color: #ffffff !important;
  }
  
  /* 确保单选按钮文字颜色为白色 */
  :deep(.el-radio__label) {
    color: #ffffff !important;
  }
  
  /* 单选按钮内部圆点默认状态 - 白色背景 */
  :deep(.el-radio__inner) {
    background-color: white !important;
    border: 1px solid #ffffff !important;
  }
  
  /* 单选按钮选中状态 - 背景颜色#48494c，白色边框 */
  :deep(.el-radio.is-checked .el-radio__inner) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important;
  }
  
  :deep(.el-select__caret) {
    color: #ffffff !important;
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
  
  // 调整表单标签颜色
  :deep(.el-form-item__label) {
    color: #ffffff !important;
  }
  
  // 分页组件中Total文字颜色设置为白色
  :global(.el-pagination__total) {
    color: #ffffff !important;
  }
  
  // 分页组件中Go to文字颜色设置为白色
  :global(.el-pagination__goto) {
    color: #ffffff !important;
  }
  
  // 上一页按钮背景颜色和边框设置
  :global(.btn-prev) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
  
  // 下一页按钮背景颜色和边框设置
  :global(.btn-next) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
  
  // 页码元素背景颜色和边框设置
  :global(.el-pager li) {
    background-color: #48494c !important;
    border: 1px solid #ffffff !important;
    color: #ffffff !important;
  }
}
</style>