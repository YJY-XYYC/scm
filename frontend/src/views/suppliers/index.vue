<template>
  <div class="suppliers-container">
    <!-- 搜索和操作栏 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="供应商名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入供应商名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="供应商编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入供应商编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operation-container">
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增供应商
        </el-button>
      </div>
    </el-card>

    <!-- 供应商列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="supplierList"
        border
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="供应商名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="供应商编码" width="120" />
        <el-table-column prop="contact" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 供应商表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="supplierFormRef"
        :model="supplierForm"
        :rules="supplierFormRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="supplierForm.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="supplierForm.code" placeholder="请输入供应商编码" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="supplierForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="supplierForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="supplierForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" type="textarea" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="supplierForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getSuppliers, 
  addSupplier, 
  updateSupplier, 
  updateSupplierStatus, 
  deleteSupplier 
} from '@/api/supplier'

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  name: '',
  code: '',
  status: undefined
})

// 供应商列表数据
const supplierList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('')
const dialogTitle = ref('')
const supplierFormRef = ref(null)
const supplierForm = ref({
  id: undefined,
  name: '',
  code: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  remark: '',
  status: 1
})

// 表单校验规则
const supplierFormRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入供应商编码', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

// 获取供应商列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getSuppliers(queryParams.value)
    supplierList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败')
  } finally {
    loading.value = false
  }
}

// 查询方法
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    name: '',
    code: '',
    status: undefined
  }
  handleQuery()
}

// 分页方法
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

// 新增供应商
const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '新增供应商'
  dialogVisible.value = true
  // 重置表单
  if (supplierFormRef.value) {
    supplierFormRef.value.resetFields()
  }
  supplierForm.value = {
    id: undefined,
    name: '',
    code: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    remark: '',
    status: 1
  }
}

// 编辑供应商
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑供应商'
  dialogVisible.value = true
  // 填充表单数据
  supplierForm.value = { ...row }
}

// 提交表单
const submitForm = async () => {
  if (!supplierFormRef.value) return
  
  await supplierFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await addSupplier(supplierForm.value)
          ElMessage.success('添加成功')
        } else {
          await updateSupplier(supplierForm.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      }
    }
  })
}

// 更改供应商状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateSupplierStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    getList()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 删除供应商
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该供应商吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSupplier(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.suppliers-container {
  .filter-container {
    margin-bottom: 20px;
    
    .operation-container {
      margin-top: 20px;
    }
  }
  
  .table-container {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 