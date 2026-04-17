<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索分类名称"
        style="width: 250px; margin-right: 10px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleAdd">新增分类</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="categoryList"
      border
      style="width: 100%">
      <el-table-column
        prop="name"
        label="分类名称"
        width="180">
      </el-table-column>
      <el-table-column
        prop="description"
        label="分类描述">
      </el-table-column>
      <el-table-column
        prop="status"
        label="状态"
        width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="200">
        <template #default="scope">
          <el-button link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-show="total > 0"
      :current-page="queryParams.pageNum"
      :page-sizes="[10, 20, 30, 50]"
      :page-size="queryParams.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange">
    </el-pagination>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const categoryList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = reactive({
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
})

// 获取分类列表
const getCategoryList = async () => {
  loading.value = true
  try {
    // 这里可以替换为实际的API调用
    // const { data } = await getCategoryListAPI(queryParams)
    // 模拟数据
    const mockData = {
      records: [
        { id: 1, name: '电子产品', description: '手机、电脑等电子产品', status: 1 },
        { id: 2, name: '服装鞋帽', description: '各类服装和鞋靴', status: 1 },
        { id: 3, name: '食品饮料', description: '各类食品和饮料', status: 1 }
      ],
      total: 3
    }
    categoryList.value = mockData.records
    total.value = mockData.total
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  getCategoryList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.id = null
  form.name = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除分类「${row.name}」吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里可以替换为实际的API调用
      // await deleteCategoryAPI(row.id)
      ElMessage.success('删除成功')
      getCategoryList()
    } catch (error) {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    // 这里可以替换为实际的API调用
    // if (form.id) {
    //   await updateCategoryAPI(form)
    // } else {
    //   await addCategoryAPI(form)
    // }
    ElMessage.success(form.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    getCategoryList()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getCategoryList()
}

const handleCurrentChange = (current) => {
  queryParams.pageNum = current
  getCategoryList()
}

// 初始化
onMounted(() => {
  getCategoryList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}
</style>