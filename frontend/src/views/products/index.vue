<template>
  <div class="product-container">
    <!-- 搜索和操作栏 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;">
        <el-form-item label="商品名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入商品编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operation-container">
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增商品
        </el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="productList"
        border
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="商品编码" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">
            {{ scope.row.price !== null ? '¥' + scope.row.price.toFixed(2) : '未定价' }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
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
              {{ scope.row.status === 1 ? '下架' : '上架' }}
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

    <!-- 商品表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productFormRules"
        label-width="80px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品编码" prop="code">
          <el-input v-model="productForm.code" placeholder="请输入商品编码" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="productForm.price" 
            :precision="2" 
            :step="0.1" 
            :min="0"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number 
            v-model="productForm.stock" 
            :min="0" 
            :precision="0"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
        <el-radio-group v-model="productForm.status">
          <el-radio label="1">上架</el-radio>
          <el-radio label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select
          v-model="productForm.category"
          placeholder="请选择或输入分类"
          filterable
          allow-create
          default-first-option
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="option in categoryOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入商品描述"
          />
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductCategories } from '@/api/product'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  code: '',
  status: undefined
})

// 商品列表数据
const productList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('')
const dialogTitle = ref('')
const productFormRef = ref(null)

// 分类数据
const categoryOptions = ref([])

// 表单数据
const productForm = reactive({
  id: undefined,
  name: '',
  code: '',
  price: 0,
  stock: 0,
  status: 1,
  category: '',
  description: ''
})

// 表单校验规则
const productFormRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入商品编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '商品编码只能包含字母和数字', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择或输入商品分类', trigger: 'blur' }
  ]
}

// 获取商品列表
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用获取商品列表接口
    // const res = await getProductList(queryParams)
    // productList.value = res.data.records
    // total.value = res.data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询和重置
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.code = ''
  queryParams.status = undefined
  handleQuery()
}

// 分页方法
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 新增商品
const handleAdd = () => {
  dialogType.value = 'add'
  dialogTitle.value = '新增商品'
  dialogVisible.value = true
  // 重置表单
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
  Object.assign(productForm, {
    id: undefined,
    name: '',
    code: '',
    price: 0,
    stock: 0,
    status: 1,
    category: '',
    description: ''
  })
  loadCategories()
}



// 提交表单
const submitForm = async () => {
  if (!productFormRef.value) return
  
  await productFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // TODO: 调用新增商品接口
          // await createProduct(productForm)
        } else {
          // TODO: 调用更新商品接口
          // await updateProduct(productForm.id, productForm)
        }
        ElMessage.success(`${dialogType.value === 'add' ? '新增' : '更新'}成功`)
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('提交失败:', error)
      }
    }
  })
}

// 删除商品
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该商品吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除商品接口
      // await deleteProduct(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 更改商品状态
const handleStatusChange = async (row) => {
  try {
    // TODO: 调用更新商品状态接口
    // await updateProductStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    getList()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await getProductCategories()
    if (res.code === 200 && res.data && Array.isArray(res.data)) {
      categoryOptions.value = res.data.map(item => ({
        value: item.name || item,
        label: item.name || item
      }))
    }
  } catch (error) {
    console.error('获取商品分类失败:', error)
    // 设置默认分类
    categoryOptions.value = [
      { value: '电子产品', label: '电子产品' },
      { value: '服装鞋帽', label: '服装鞋帽' },
      { value: '食品饮料', label: '食品饮料' },
      { value: '家居用品', label: '家居用品' },
      { value: '图书文具', label: '图书文具' }
    ]
  }
}

// 编辑商品
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑商品'
  dialogVisible.value = true
  // 重置表单
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
  // 填充表单数据
  nextTick(() => {
    Object.assign(productForm, row)
    // 加载分类数据
    loadCategories()
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.product-container {
  .filter-container {
    margin-bottom: 20px;
    
    .operation-container {
      margin-top: 20px;
    }
  }
  
  .demo-form-inline {
    padding: 16px;
    border-radius: 4px;
    /* 直接设置渐变背景 */
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: #464e58 !important;
  }
  
  /* 直接针对el-form元素 */
  .el-form.demo-form-inline {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: #464e58 !important;
  }
  
  .table-container {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}

/* 使用:deep确保样式穿透到子组件 */
:deep(.el-form.demo-form-inline) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: #464e58 !important;
}

/* 针对filter-container中的form元素 */
.product-container .filter-container .el-form.demo-form-inline {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}
</style>