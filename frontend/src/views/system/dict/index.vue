<template>
    <div class="filter-container">
      <el-input
        v-model="searchParams.keyword"
        placeholder="请输入字典名称或编码"
        style="width: 250px; margin-right: 10px"
      ></el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="primary" @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      style="width: 100%; box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25); border-radius: 8px; margin: 20px 0; position: relative; z-index: 1; overflow: visible;">
      <el-table-column
        prop="dictName"
        label="字典名称"
        width="180">
      </el-table-column>
      <el-table-column
        prop="dictCode"
        label="字典编码"
        width="180">
      </el-table-column>
      <el-table-column
        prop="description"
        label="描述">
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
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="handleManageItems(scope.row)">管理字典项</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="form.dictName" placeholder="请输入字典名称"></el-input>
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="form.dictCode" placeholder="请输入字典编码"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDictList, addDict, updateDict, deleteDict } from '@/api/dict'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const loading = ref(false)
const list = ref([])
const allMenus = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const router = useRouter()

const searchParams = ref({
  keyword: ''
})

const form = ref({
  id: null,
  dictName: '',
  dictCode: '',
  description: '',
  status: 1
})

const rules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' }
  ],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' }
  ]
}

// 获取列表
const getList = async () => {
  try {
    loading.value = true
    const res = await getDictList()
    allMenus.value = res.data.records
    handleSearch() // 初始加载时执行一次搜索
  } catch (error) {
    console.error('获取字典列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  const keyword = searchParams.value.keyword.toLowerCase()
  if (!keyword.trim()) {
    list.value = allMenus.value
    return
  }
  
  list.value = allMenus.value.filter(item => {
    const nameMatch = item.dictName && item.dictName.toLowerCase().includes(keyword)
    const codeMatch = item.dictCode && item.dictCode.toLowerCase().includes(keyword)
    return nameMatch || codeMatch
  })
}

// 处理重置
const handleReset = () => {
  searchParams.value.keyword = ''
  list.value = allMenus.value
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增字典'
  form.value = {
    id: null,
    dictName: '',
    dictCode: '',
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑字典'
  form.value = { ...row }
  dialogVisible.value = true
}

// 处理管理字典项
const handleManageItems = (row) => {
  router.push(`/system/dict-item?dictId=${row.id}&dictName=${encodeURIComponent(row.dictName)}`)
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该字典吗？删除后相关的字典项也将被删除。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDict(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除字典失败:', error)
    }
  })
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    if (form.value.id) {
      await updateDict(form.value)
      ElMessage.success('更新成功')
    } else {
      await addDict(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 设置输入框背景颜色和选中状态边框颜色 */
:deep(.el-input__wrapper) {
  background-color: #48494c !important;
}

/* 设置输入框内文字颜色为白色 */
:deep(.el-input__inner) {
  color: #ffffff !important;
}

/* 设置输入框占位符文字颜色为白色 */
:deep(.el-input__inner)::placeholder {
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 修改输入框选中状态的边框颜色 */
:deep(.el-input__wrapper):focus-within {
  box-shadow: 0 0 0 1px #ffffff inset !important;
  border-color: #409eff !important;
}

/* 设置按钮样式与单选按钮一致 */
.filter-container :deep(.el-button) {
  background-color: #ffffff !important;
  color: #274151 !important;
  border: 1px solid #ffffff !important;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  outline: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 按钮悬停效果 */
.filter-container :deep(.el-button:hover) {
  background-color: #5a5b5f !important;
  border-color: #ffffff !important;
}

/* 按钮激活效果 */
.filter-container {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-container :deep(.el-button:active) {
  background-color: #ffffff !important;
  color: #274151 !important;
}

/* 移除表格相关样式，阴影和边框等样式已通过内联样式设置 */
:deep(.el-table) {
  overflow: visible;
}

:deep(.el-table__inner-wrapper) {
  overflow: hidden;
}

/* 设置表格整体渐变背景 */
:deep(.el-table),
:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper),
:deep(.el-table__footer-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格行和单元格也是透明的，让渐变背景透出来 */
:deep(.el-table__row),
:deep(.el-table__header tr),
:deep(.el-table__body tr),
:deep(.el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: #ffffff !important;
}

/* 移除scoped内的对话框样式，移到下方非scoped样式块 */
</style>

/* 非scoped样式块，用于全局样式 */
<style>
/* 移除最外层卡片的白色边框 */
.el-card,
.el-card.is-always-shadow {
  border: none !important;
  box-shadow: none !important;
}

/* 移除表格中的竖线（列分隔线） */
body .el-table--border,
body .el-table--group {
  border-right: none !important;
  border-left: none !important;
}

body .el-table--border td,
body .el-table--group td,
body .el-table--border th,
body .el-table--group th {
  border-right: none !important;
  border-left: none !important;
}

body .el-table--border:before,
body .el-table--group:before {
  display: none !important;
}

body .el-table__fixed-right-patch,
body .el-table__border-left-patch {
  display: none !important;
}

/* 为字典管理页面的表格容器设置渐变背景 */
.el-card__header,
.el-card__body,
body .filter-container,
body .el-table,
body .el-table__header-wrapper,
body .el-table__body-wrapper,
body .el-table__footer-wrapper {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 为卡片头部标题span设置白色文字颜色 */
.el-card__header span {
  color: #ffffff !important;
}

body .el-table__row,
body .el-table__header tr,
body .el-table__body tr,
body .el-table__cell {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: #ffffff !important;
}

/* 设置对话框的背景渐变 */
.el-dialog,
.el-dialog__body,
.el-dialog__header,
.el-dialog__footer {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: #ffffff !important;
}

/* 确保对话框标题颜色适配深色背景 */
.el-dialog__title {
  color: #ffffff !important;
}

/* 将对话框中的表单标签文字修改为白色 */
.el-dialog .el-form-item__label {
  color: #ffffff !important;
}

/* 将对话框中的单选按钮标签文字修改为白色 */
.el-dialog .el-radio__label {
  color: #ffffff !important;
}

/* 将对话框中的textarea输入框背景颜色修改为#48494c */
.el-dialog .el-textarea__inner {
  background-color: #48494c !important;
  color: #ffffff !important;
  border: 1px solid #ffffff !important;
}
/* 确保textarea输入框在被选中时边框颜色为白色 */
.el-dialog .el-textarea__inner:focus {
  border-color: #ffffff !important;
  box-shadow: 0 0 0 0.5px white !important;
}

/* 设置单选按钮选中时的样式 */
.el-dialog .el-radio__input.is-checked .el-radio__inner {
  background-color: #48494c !important;
  border-color: #ffffff !important;
}

/* 将对话框中的输入框内文字颜色修改为白色 */
.el-dialog .el-input__inner {
  color: #ffffff !important;
}
</style>