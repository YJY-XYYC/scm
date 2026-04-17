<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ dictName }} - 字典项管理</span>
        </div>
      </template>

      <!-- 操作列上方的新增按钮 -->
      <div style="margin-bottom: 10px; text-align: right;">
        <el-button type="primary" @click="handleAdd" class="dict-item-add-btn">新增字典项</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="list"
        border
        style="width: 100%;">
        <el-table-column
          prop="label"
          label="字典项名称"
          width="297">
        </el-table-column>
        <el-table-column
          prop="value"
          label="字典项值"
          width="250">
        </el-table-column>
        <el-table-column
          prop="sort"
          label="排序"
          width="250">
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="250">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="300">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
        <el-form-item label="字典项名称" prop="label">
          <el-input v-model="form.label" placeholder="请输入字典项名称"></el-input>
        </el-form-item>
        <el-form-item label="字典项值" prop="value">
          <el-input v-model="form.value" placeholder="请输入字典项值"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" placeholder="排序号"></el-input-number>
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getDictItemList, addDictItem, updateDictItem, deleteDictItem } from '@/api/dict'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const dictId = ref('')
const dictName = ref('')

const form = ref({
  id: null,
  dictId: '',
  label: '',
  value: '',
  sort: 0,
  status: 1
})

const rules = {
  label: [
    { required: true, message: '请输入字典项名称', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入字典项值', trigger: 'blur' }
  ]
}

// 获取字典项列表
const getList = async () => {
  if (!dictId.value) return
  
  try {
    loading.value = true
    const res = await getDictItemList(dictId.value)
    list.value = res.data || []
  } catch (error) {
    console.error('获取字典项列表失败:', error)
    ElMessage.error('获取字典项列表失败')
  } finally {
    loading.value = false
  }
}

// 处理新增
const handleAdd = () => {
  dialogTitle.value = '新增字典项'
  form.value = {
    id: null,
    dictId: dictId.value,
    label: '',
    value: '',
    sort: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑字典项'
  form.value = { ...row }
  dialogVisible.value = true
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该字典项吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictItem(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除字典项失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    if (form.value.id) {
      await updateDictItem(form.value)
      ElMessage.success('更新成功')
    } else {
      await addDictItem(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    }
  }
}

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  if (newQuery.dictId) {
    dictId.value = newQuery.dictId
    dictName.value = newQuery.dictName ? decodeURIComponent(newQuery.dictName) : '字典项管理'
    getList()
  }
}, { immediate: true })
</script>

<style scoped>
.app-container {
  padding: 20px;
}

/* 修改卡片背景为线性渐变 */
:deep(.el-card) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #ffffff;
  border: none !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 新增字典项按钮样式 - 与用户管理页面查询按钮样式一致 */
:deep(.dict-item-add-btn) {
  background-color: white !important;
  color: #274151 !important;
  border: 1px solid #48494c !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  outline: none !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  border-radius: 4px !important;
}

/* 新增字典项按钮悬停效果 */
:deep(.dict-item-add-btn:hover) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  /* box-shadow: 0 0 0 2px #ffffff !important; */
}

/* 新增字典项按钮激活效果 */
:deep(.dict-item-add-btn:active) {
  background-color: #f0f0f0 !important;
}

/* 修改表格内部包装器背景为线性渐变 */
:deep(.el-table__inner-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  /* border-left: 1px solid #ebeef5 !important; */
  /* border-right: 1px solid #ebeef5 !important; */
}

/* 确保表格容器背景透明，显示渐变 */
:deep(.el-table) {
  background: transparent !important;
  margin-top: 10px;
}

/* 调整表格列间距，增加单元格内边距，并设置透明背景 */
:deep(.el-table__cell) {
  padding: 12px 16px !important; /* 增加上下和左右内边距 */
  border-right: none !important; /* 删除单元格右侧边框 */
  background: transparent !important;
  color: #ffffff !important;
}

/* 调整表头单元格样式，并设置透明背景 */
:deep(.el-table__header .el-table__cell) {
  padding: 14px 16px !important; /* 表头可以稍大一点 */
  border-right: none !important; /* 删除表头单元格右侧边框 */
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: #ffffff !important;
}

/* 为最后一列表头添加白色右边框 */
:deep(.el-table__header .el-table__cell:last-child) {
  border-right: 1px solid #ffffff !important;
}

/* 为最后一列单元格添加白色右边框 */
:deep(.el-table__body .el-table__cell:last-child) {
  border-right: 1px solid #ffffff !important;
}

/* 保留表格左边框，删除右边框 */
:deep(.el-table--border) {
  border-right: none !important;
  border-left: 1px solid #ebeef5 !important; /* 添加左边框线 */
  border-right: 1px solid #ebeef5 !important;
  background: transparent !important;
}

/* 确保表头和表体区域背景透明 */
:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  border-left: 1px solid #ebeef5 !important;
  border-right: 1px solid #ebeef5 !important;
  background: transparent !important;
}

/* 确保表格行背景透明 */
:deep(.el-table__row) {
  background: transparent !important;
}

/* 确保表格背景透明 */
:deep(.el-table__body) {
  background: transparent !important;
}

/* 确保表头背景透明 */
:deep(.el-table__header) {
  background: transparent !important;
}

/* 修改对话框背景为线性渐变 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #ffffff;
}

/* 确保对话框标题文字颜色为白色 */
:deep(.el-dialog__title) {
  color: #ffffff !important;
}

/* 确保对话框内容区域背景透明 */
:deep(.el-dialog__body) {
  background: transparent !important;
  color: #ffffff;
}

/* 确保对话框底部区域背景透明 */
:deep(.el-dialog__footer) {
  background: transparent !important;
}

/* 确保表单标签文字颜色为白色 */
:deep(.el-form-item__label) {
  color: #ffffff !important;
}

/* 确保单选按钮标签文字颜色为白色 */
:deep(.el-radio__label) {
  color: #ffffff !important;
}

/* 调整卡片容器大小，使其完全贴合表格，去掉右侧空白 */
.el-card__body {
  padding: 20px !important;
  width: 100%;
  min-width: 710px;
  margin: 0 auto;
  overflow: hidden;
}

/* 删除固定列右侧的补丁元素 */
:deep(.el-table__fixed-right-patch) {
  display: none !important;
}

/* radio按钮使用默认的Element Plus鼠标悬停效果 */

/* 修改输入框的背景颜色 */
:deep(.el-input__wrapper) {
  background-color: #48494c !important;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgb(255, 255, 255) inset !important;
}
:deep(.el-input__inner) {
  background-color: #48494c !important;
  color: #fff !important;
  border-color: #48494c !important;
}
:deep(.el-input__inner:focus) {
  border-color: white !important;
  /* box-shadow: 0 0 0 1px white !important; */
}
</style>