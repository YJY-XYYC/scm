<template>
  <div class="product-container">
    <!-- 搜索栏 -->
    <div :inline="true" :model="queryParams" class="search-form" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); padding: 16px; border-radius: 4px; box-shadow: 0 10px 30px 0 rgba(0, 0, 0, 0.3); margin-bottom: 20px; display: block;">
        <!-- 确保三个输入框和按钮在同一行 -->
        <div style="display: flex; gap: 15px; align-items: flex-end; flex-wrap: wrap;">
          <el-form-item label="商品搜索" style="color: white;">
            <el-input 
              v-model="queryParams.searchTerm" 
              placeholder="请输入商品名称或商品编码" 
              clearable 
              @keyup.enter="handleQuery"
              prefix-icon="Search"
              style="width: 220px; background-color: #48494c;"
            />
          </el-form-item>
          <el-form-item label="分类" style="color: white;">
              <el-select v-model="queryParams.category" placeholder="请选择分类" clearable @change="handleQuery" style="width: 200px; background-color: #48494c;" popper-class="category-select-dropdown">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                  style="background-color: #48494c; color: white;"
                />
              </el-select>
            </el-form-item>
          <el-form-item label="状态" style="color: white;">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable @change="handleQuery" style="width: 200px; background-color: #48494c;" popper-class="status-select-dropdown">
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="parseInt(option.value)"
                style="background-color: #48494c; color: white;"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery" class="rounded-button">查询</el-button>
            <el-button type="primary" @click="resetQuery" class="rounded-button">重置</el-button>
            <el-button type="primary" @click="handleAdd" class="rounded-button">新增商品</el-button>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete" class="rounded-button">
              批量删除
            </el-button>
          </el-form-item>
        </div>
      </div>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="productList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="商品图片" width="100" align="center">
          <template #default="scope">
            <el-image
              :src="scope.row.image ? ('/api' + scope.row.image) : '/images/product-icon.svg'"
              fit="contain"
              style="width: 50px; height: 50px"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" />
        <el-table-column label="商品编码" prop="code" width="120" />
        <el-table-column label="分类" prop="category" width="100" />
        <el-table-column label="价格" width="120">
          <template #default="scope">
            {{ scope.row.price !== null ? '¥' + scope.row.price.toFixed(2) : '未定价' }}
          </template>
        </el-table-column>
        <el-table-column label="库存" prop="stock" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="handleStock(scope.row)">
              库存
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
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

    <!-- 商品表单对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            >
              <img v-if="productForm.image" :src="'/api' + productForm.image" class="avatar" />
              <img v-else src="/images/product-icon.svg" class="avatar" />
          </el-upload>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input 
            v-model="productForm.name" 
            placeholder="请输入商品名称" 
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="商品编码" prop="code">
          <el-input v-model="productForm.code" placeholder="请输入商品编码" />
        </el-form-item>
        <!-- <el-form-item label="库位编码">
          <el-input v-model="productForm.library_coding" placeholder="请输入库位编码" />
        </el-form-item> -->
        <el-form-item label="分类" prop="category">
          <el-input v-model="productForm.category" placeholder="请输入分类名称" />
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
        <el-form-item label="状态">
          <el-radio-group v-model="productForm.status">
            <el-radio
              v-for="option in statusOptions"
              :key="option.value"
              :value="parseInt(option.value)"
            >{{ option.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
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
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog
      title="库存调整"
      v-model="stockDialog.visible"
      width="500px"
    >
      <el-form
        ref="stockFormRef"
        :model="stockForm"
        :rules="stockRules"
        label-width="100px"
      >
        <el-form-item label="当前库存">
          <span class="stock-value">{{ stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="调整数量" prop="adjustAmount">
          <el-input-number
            v-model="stockForm.adjustAmount"
            :min="-stockForm.currentStock"
            placeholder="正数增加/负数减少"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="stockForm.remark"
            type="textarea"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stockDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="handleStockSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductList, addProduct, updateProduct, deleteProduct, updateProductStatus, updateProductStock, getProductCategories } from '@/api/product'


// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  searchTerm: '',
  category: undefined,
  status: undefined
})

// 商品列表数据
const productList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedIds = ref([])

// 分类选项
const categoryOptions = ref([])

// 状态字典选项
const statusOptions = ref([])

// 引入字典API
import { getDictItemByCode } from '@/api/dict'

// 库存相关代码已移除

// 表单对话框
const dialog = reactive({
  visible: false,
  title: '',
  type: ''
})

// 商品表单
const productFormRef = ref(null)
const productForm = reactive({
  id: undefined,
  name: '',
  code: '',
  library_coding: '',
  category: '',
  price: 0,
  stock: 0,
  status: 1,
  image: '',
  description: ''
})

// 表单校验规则
const productRules = {
  code: [
    { required: true, message: '请输入商品编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '商品编码只能包含字母和数字', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ]
}

// 库存调整对话框
const stockDialog = reactive({
  visible: false,
  productId: undefined
})

const stockFormRef = ref(null)
const stockForm = reactive({
  currentStock: 0,
  adjustAmount: 0,
  remark: ''
})

const stockRules = {
  adjustAmount: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    { type: 'number', message: '请输入数字', trigger: 'blur' }
  ]
}

// 上传相关
const uploadUrl = import.meta.env.VITE_API_URL + '/upload'
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  productForm.image = response.data
}

// 获取状态字典
const loadStatusDict = async () => {
  try {
    const res = await getDictItemByCode('product_status')
    if (res.code === 200) {
      statusOptions.value = res.data
    }
  } catch (error) {
    console.error('获取状态字典失败:', error)
  }
}

// 从商品列表中提取分类
const loadCategories = async () => {
  try {
    console.log('开始从商品数据中提取分类...')
    // 调用商品列表API获取所有商品数据
    const res = await getProductList({ pageNum: 1, pageSize: 1000 }) // 增大pageSize以获取更多数据
    
    console.log('商品列表API响应:', res)
    
    if (res && res.data && res.data.records && Array.isArray(res.data.records)) {
      console.log('商品数据数量:', res.data.records.length)
      
      // 从商品数据中提取所有不重复的分类值
      const categoriesSet = new Set()
      res.data.records.forEach(product => {
        if (product && product.category) {
          categoriesSet.add(product.category)
          console.log('找到分类:', product.category)
        }
      })
      
      // 将分类集合转换为选择器需要的格式
      const uniqueCategories = Array.from(categoriesSet)
      console.log('提取的唯一分类数量:', uniqueCategories.length)
      console.log('唯一分类列表:', uniqueCategories)
      
      if (uniqueCategories.length > 0) {
        categoryOptions.value = uniqueCategories.map(category => ({
          value: category,
          label: category
        }))
        console.log('分类选项设置完成:', categoryOptions.value)
        //ElMessage.success(`成功从商品数据中提取 ${uniqueCategories.length} 个分类`)
      } else {
        // 如果没有找到分类数据，使用默认分类
        console.log('没有找到分类数据，使用默认分类')
        setDefaultCategories()
      }
    } else {
      // 如果API返回数据格式不正确，使用默认分类
      console.log('API返回数据格式不正确，使用默认分类')
      setDefaultCategories()
    }
  } catch (error) {
    console.error('获取商品数据失败:', error)
    ElMessage.warning('获取商品分类失败，使用默认分类')
    // API调用失败时使用默认分类
    setDefaultCategories()
  }
}

// 设置默认分类
const setDefaultCategories = () => {
  categoryOptions.value = [
    { value: '电子产品', label: '电子产品' },
    { value: '服装', label: '服装' },
    { value: '食品', label: '食品' },
    { value: '图书', label: '图书' },
    { value: '家居', label: '家居' }
  ]
}

// 获取库存数据函数已移除

// 获取商品列表
const getList = async () => {
  try {
    loading.value = true
    
    // 重置列表数据
    productList.value = []
    total.value = 0
    
    // 创建搜索参数对象
    const searchParams = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      category: queryParams.category,
      status: queryParams.status,
      showAllStatus: true
    }
    
    // 根据搜索内容的特性决定如何设置搜索参数
    const searchTerm = queryParams.searchTerm && queryParams.searchTerm.trim()
    // if (searchTerm) {
    //   ElMessage.success(`正在搜索: 名称或编码包含 "${searchTerm}"`)
    // } else {
    //   ElMessage.info('正在加载所有商品...')
    // }
    
    console.log('搜索参数:', searchParams)
    const res = await getProductList(searchParams)
    console.log('API响应:', res.data)
    
    if (res.data && res.data.records) {
      let filteredRecords = res.data.records
      
      // 前端过滤逻辑
    // 1. 先过滤分类
    if (queryParams.category) {
      filteredRecords = filteredRecords.filter(item => 
        item && item.category && item.category === queryParams.category
      )
      console.log(`分类过滤后商品数量: ${filteredRecords.length}`)
    }
    
    // 2. 过滤状态
    if (queryParams.status !== undefined) {
      filteredRecords = filteredRecords.filter(item => 
        item && item.status !== undefined && item.status === queryParams.status
      )
      console.log(`状态过滤后商品数量: ${filteredRecords.length}`)
    }
    
    // 3. 再根据搜索词过滤商品名称和编码
    if (searchTerm) {
      filteredRecords = filteredRecords.filter(item => {
        const matchesName = item.name && item.name.toLowerCase().includes(searchTerm.toLowerCase())
        const matchesCode = item.code && item.code.toLowerCase().includes(searchTerm.toLowerCase())
        return matchesName || matchesCode
      })
      ElMessage.success(`搜索完成，找到 ${filteredRecords.length} 个匹配商品`)
    }
    
    // 显示筛选结果提示
    if (queryParams.status !== undefined && !searchTerm && !queryParams.category) {
      const statusText = queryParams.status === 1 ? '在售' : '下架'
      ElMessage.success(`已筛选 ${filteredRecords.length} 个${statusText}商品`)
    } else if (queryParams.category && !searchTerm) {
      ElMessage.success(`已筛选 ${filteredRecords.length} 个${queryParams.category}类商品`)
    }
      
      productList.value = filteredRecords
      total.value = filteredRecords.length
    } else {
      productList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    productList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.searchTerm = ''
  queryParams.category = undefined
  queryParams.status = undefined
  handleQuery()
}

// 选择框变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 新增商品
const handleAdd = () => {
  dialog.type = 'add'
  dialog.title = '新增商品'
  dialog.visible = true
  productForm.id = undefined
}

// 编辑商品
const handleEdit = (row) => {
  dialog.type = 'edit'
  dialog.title = '编辑商品'
  dialog.visible = true
  Object.assign(productForm, row)
}

// 提交表单
const handleSubmit = async () => {
  await productFormRef.value.validate()
  
  try {
    if (dialog.type === 'add') {
      await addProduct(productForm)
      ElMessage.success('新增成功')
    } else {
      await updateProduct(productForm)
      ElMessage.success('修改成功')
    }
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('操作失败：', error)
  }
}

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该商品吗？')
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败：', error)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个商品吗？`)
    await deleteProduct(selectedIds.value)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('批量删除失败：', error)
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 确保传递整数类型的status值
    const statusValue = parseInt(row.status)
    await updateProductStatus(row.id, statusValue)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败：', error)
    row.status = row.status === 1 ? 0 : 1
  }
}

// 库存调整
const handleStock = (row) => {
  stockDialog.productId = row.id
  stockForm.currentStock = row.stock
  stockForm.adjustAmount = 0
  stockForm.remark = ''
  stockDialog.visible = true
}

// 提交库存调整
const handleStockSubmit = async () => {
  await stockFormRef.value.validate()
  
  try {
    // 计算最终库存值：当前库存 + 调整数量
    const finalStock = stockForm.currentStock + stockForm.adjustAmount
    await updateProductStock(stockDialog.productId, finalStock, stockForm.remark)
    ElMessage.success('库存调整成功')
    stockDialog.visible = false
    getList()
  } catch (error) {
    console.error('库存调整失败：', error)
  }
}



// 处理商品选择变化函数已移除

// 对话框关闭
const handleDialogClose = () => {
  productFormRef.value?.resetFields()
  Object.assign(productForm, {
    id: undefined,
    name: '',
    code: '',
    category: '',
    price: 0,
    stock: 0,
    status: 1,
    image: '',
    description: ''
  })
}

// 分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

onMounted(() => {
  loadStatusDict()
  loadCategories()
  getList()
})
</script>

<style lang="scss" scoped>
.product-container {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
    box-shadow: none !important;
    border: none !important;
    padding: 0 !important;
    background: transparent !important;
  }
  
  /* 库存调整对话框中的当前库存文字样式 */
  .stock-value {
    color: white !important;
  }
  
  /* 确保对话框中的所有文字都为白色 */
  .el-dialog {
    .el-form-item__label,
    .el-form-item__content,
    .el-input-number,
    .el-input__wrapper,
    .el-input__inner,
    span {
      color: white !important;
    }
  }
  
  .search-card .el-card__body {
    padding: 0 !important;
    background: transparent !important;
  }
  
  form.search-form {
    background-color: white !important;
    padding: 16px;
    border-radius: 4px;
    box-shadow: 0 6px 24px 0 rgba(0, 0, 0, 0.2) !important;
    border: 1px solid #e4e7ed;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      
      &:hover {
        border-color: var(--el-color-primary);
      }
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }
    
    .avatar {
      width: 100px;
      height: 100px;
      display: block;
    }
  }
  
  /* 设置搜索栏中label的颜色为白色 */
  .search-form {
    :deep(.el-form-item__label) {
      color: white !important;
    }
    
    /* 设置输入框和选择框的背景颜色 */
    :deep(.el-input__wrapper),
    :deep(.el-select .el-input__wrapper),
    :deep(.el-select .el-input__inner) {
      background-color: #48494c !important;
      color: white !important;
      // box-shadow: none !important;
      border-color: #ffffff !important;
    }
  }
  
  /* 确保下拉菜单背景色为#48494c - 使用更强制的选择器 */
  :global(.el-select-dropdown),
  :global(.el-select-dropdown__wrap) {
    background-color: #48494c !important;
    border-color: #48494c !important;
  }
  
  :global(.el-select-dropdown__item) {
    color: white !important;
    background-color: #48494c !important;
  }
  
  :global(.el-select-dropdown__item:hover) {
    background-color: #5a5b5e !important;
    color: white !important;
  }
  
  :global(.el-select-dropdown__item.selected) {
    background-color: #6c6d70 !important;
    color: white !important;
  }
  
  /* 添加额外的覆盖样式 */
  :global(.el-select-dropdown__empty) {
    color: white !important;
    background-color: #48494c !important;
  }
  
  /* 确保下拉菜单的popper容器也应用正确的背景色 */
  :global(.el-popper) {
    background-color: #48494c !important;
  }
  
  /* 覆盖Element Plus的默认样式 */
  :global(.el-select__popper) {
    background-color: #48494c !important;
    border: none !important;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5) !important;
  }
  
  /* 自定义下拉菜单类 */
  :global(.custom-select-dropdown) {
    background-color: #48494c !important;
    border: none !important;
  }
  
  /* 分类下拉菜单专用样式 */
  :global(.category-select-dropdown) {
    background-color: #48494c !important;
    border: none !important;
    color: white !important;
  }
  
  :global(.category-select-dropdown .el-select-dropdown__wrap) {
    background-color: #48494c !important;
  }
  
  :global(.category-select-dropdown .el-select-dropdown__item) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  :global(.category-select-dropdown .el-select-dropdown__item:hover) {
    background-color: #5a5b5e !important;
  }
  
  :global(.category-select-dropdown .el-select-dropdown__item.selected) {
    background-color: #6c6d70 !important;
  }
  
  /* 状态下拉菜单专用样式 */
  :global(.status-select-dropdown) {
    background-color: #48494c !important;
    border: none !important;
    color: white !important;
  }
  
  :global(.status-select-dropdown .el-select-dropdown__wrap) {
    background-color: #48494c !important;
  }
  
  :global(.status-select-dropdown .el-select-dropdown__item) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  :global(.status-select-dropdown .el-select-dropdown__item:hover) {
    background-color: #5a5b5e !important;
  }
  
  :global(.status-select-dropdown .el-select-dropdown__item.selected) {
    background-color: #6c6d70 !important;
  }
  
  /* 下拉菜单输入框背景色和边框 - 确保在所有状态下都显示白色边框 */
  :global(.el-select__wrapper) {
    background-color: #48494c !important;
    border: 1px solid white !important;
    outline: none !important; /* 移除默认轮廓线 */
  }
  
  /* 确保点击、聚焦、激活时边框保持白色，不变成蓝色 */
  :global(.el-select__wrapper:active),
  :global(.el-select__wrapper:focus),
  :global(.el-select__wrapper:focus-within),
  :global(.el-select__wrapper:hover),
  :global(.el-select__wrapper.is-focus) {
    border-color: white !important;
    box-shadow: none !important;
    outline: none !important;
  }
  
  /* 针对特定的组件状态添加更具体的规则 */
  :global(.el-select__wrapper.el-tooltip__trigger) {
    border-color: white !important;
  }
  
  :global(.el-select__wrapper.el-tooltip__trigger:focus) {
    border-color: white !important;
    box-shadow: none !important;
    outline: none !important;
  }
  
  /* 搜索输入框边框样式 - 保持白色边框，包括点击和焦点状态 */
  :global(.el-input__wrapper) {
    border: 1px solid white !important;
  }
  
  /* 设置输入框文字颜色为白色 */
  :global(.el-input__inner) {
    color: white !important;
  }
  
  :global(.el-input__wrapper.is-focus),
  :global(.el-input__wrapper:focus-within),
  :global(.el-input__wrapper:hover) {
    border-color: white !important;
    box-shadow: none !important;
  }
  
  :global(.el-select__selection) {
    background-color: #48494c !important;
  }
  
  :global(.el-select__input-wrapper) {
    background-color: #48494c !important;
  }
  
  :global(.el-select__placeholder) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  :global(.el-select__caret) {
    color: white !important;
  }
  
  /* 确保样式优先级最高 */
  * :global(.el-select-dropdown),
  * :global(.el-select-dropdown__wrap),
  * :global(.el-select-dropdown__item) {
    background-color: #48494c !important;
    color: white !important;
    border-color: #48494c !important;
  }
  
  /* 修改下拉菜单三角形颜色 */
  * :global(.el-popper__arrow::before),
  * :global(.el-popper__arrow::after) {
    border-color: #48494c !important;
    background-color: #48494c !important;
  }
  
  /* 针对分类和状态下拉菜单的三角形颜色 */
  * :global(.custom-select-dropdown .el-popper__arrow::before),
  * :global(.custom-select-dropdown .el-popper__arrow::after),
  * :global(.status-select-dropdown .el-popper__arrow::before),
  * :global(.status-select-dropdown .el-popper__arrow::after) {
    border-color: #48494c !important;
  }
  
  /* 直接定位所有下拉菜单的箭头元素 */
  * :global(.el-select-dropdown__popper .el-popper__arrow::before),
  * :global(.el-select-dropdown__popper .el-popper__arrow::after) {
    border-color: #48494c !important;
  }
  
  /* 已移除自定义按钮样式，还原为Element Plus默认样式 */
  
  /* 对话框背景渐变颜色 */
  :global(.el-dialog) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 对话框标题文字颜色为白色 */
  :global(.el-dialog__title) {
    color: white !important;
  }
  
  /* 表单标签文字颜色为白色 */
  :global(.el-form-item__label) {
    color: white !important;
  }
  
  /* 上传图标背景颜色 */
  :global(.avatar-uploader-icon) {
    background-color: #48494c !important;
  }
  
  /* 确保上传区域在任何状态下都保持白色边框 */
  :global(.el-upload:hover),
  :global(.el-upload:focus) {
    border-color: white !important;
    box-shadow: none !important;
    outline: none !important;
  }
  
  /* 确保上传组件容器在悬停时也保持白色边框 */
  :global(.avatar-uploader:hover) {
    border-color: white !important;
    box-shadow: none !important;
  }
  
  /* 确保i标签在悬停时保持白色边框效果 */
  :global(i.el-icon.avatar-uploader-icon:hover),
  :global(i.el-icon.avatar-uploader-icon:focus) {
    border-color: white !important;
    box-shadow: none !important;
    outline: none !important;
  }
  
  /* 输入框包装器背景颜色 */
  :global(.el-input__wrapper) {
    background-color: #48494c !important;
  }
  
  /* 完全移除输入框包装器的所有悬停和焦点效果 */
  :global(.el-input__wrapper),
  :global(.el-input__wrapper:hover),
  :global(.el-input__wrapper:focus-within),
  :global(.el-input__wrapper.is-focus) {
    --el-input-border-color: #ffffff !important;
    --el-input-hover-border-color: #ffffff !important;
    --el-input-focus-border-color: #ffffff !important;
    box-shadow: none !important;
    border-color: #ffffff !important;
    outline: none !important;
    transition: none !important;
  }
  
  /* 确保内部输入框也没有任何悬停效果 */
  :global(.el-input__inner),
  :global(.el-input__inner:hover),
  :global(.el-input__inner:focus) {
    border-color: transparent !important;
    box-shadow: none !important;
    outline: none !important;
    transition: none !important;
  }
  
  /* 文本域背景颜色和文字颜色 */
  :global(.el-textarea__inner) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  /* 将radio标签文字颜色修改为白色 */
  :global(.el-radio__label) {
    color: white !important;
  }
  
  /* 设置radio按钮选中时的样式 */
  :global(.el-radio__input.is-checked .el-radio__inner) {
    background-color: #48494c !important;
    border-color: white !important;
  }
  
  /* 设置表格背景颜色为渐变 */
  :global(.el-table),
  :global(.el-table__inner-wrapper),
  :global(.el-table__header-wrapper),
  :global(.el-table__body-wrapper) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    background-color: transparent !important;
  }
  
  /* 确保表格行和单元格也有透明背景 */
  :global(.el-table__row),
  :global(.el-table__cell) {
    background-color: transparent !important;
    border-color: white !important; /* 将横线改为白色 */
    color: white !important; /* 将文字改为白色 */
  }
  
  /* 设置表头行的背景颜色为渐变 */
  :global(.el-table__header tr) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    color: white !important; /* 确保表头文字也是白色 */
  }
  
  /* 确保表头单元格文字也是白色 */
  :global(.el-table__header th) {
    color: white !important;
  }
  
  /* 使表格第一条横线加粗 */
  :global(.el-table__body) tbody tr:first-child td,
  :global(.el-table__header) thead tr:first-child th {
    border-top-width: 2px !important;
  }
  
  /* 使表格最后一条横线加粗 */
  :global(.el-table__body) tbody tr:last-child td {
    border-bottom-width: 2px !important;
  }
  
  /* 设置卡片内容区域背景颜色为渐变 */
  :global(.el-card__body) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 移除卡片容器的白色边框 */
  :global(.el-card) {
    border: none !important;
    box-shadow: none !important;
  }
  
  /* 将分页组件文字颜色修改为白色 */
  :global(.el-pagination__total) {
    color: white !important;
  }
  
  :global(.el-pagination__goto) {
    color: white !important;
  }
  
  /* 设置分页按钮和页码的样式 */
  :global(.btn-prev),
  :global(.btn-next),
  :global(.el-pagination__item) {
    background-color: #48494c !important;
    color: white !important;
    margin: 0 4px !important; /* 增加间隙 */
    border: 1px solid white !important; /* 添加白色边框 */
  }
  
  /* 设置活动页码的样式 */
  :global(.el-pagination__item.is-active),
  :global(.is-active.number) {
    background-color: #48494c !important;
    color: white !important;
    border: 1px solid white !important; /* 确保有明确的白色边框 */
  }
  
  /* 确保分页按钮中的图标也是白色 */
  :global(.btn-prev .el-icon),
  :global(.btn-next .el-icon) {
    color: white !important;
  }
  
  /* 为特定的四个按钮设置背景颜色 */
  .rounded-button {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important;
    transition: all 0.3s ease !important;
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
    color: #E4656C !important;
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
    font-weight: bold;
  }
  
  /* 增加分页组件中各元素的整体间隙 */
  :global(.el-pagination) {
    gap: 8px !important;
  }
  
  /* 设置开关按钮的背景颜色和白色边框 */
  :global(.el-switch__core) {
    // background-color: #409EFF !important;
    border: 1px solid white !important;
  }
  
  /* 确保开关按钮在激活状态下也有白色边框 */
  :global(.el-switch__core.is-checked) {
    border: 1px solid white !important;
  }
}
</style>