<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索角色名称或编码"
        style="width: 250px; margin-right: 10px"
        @keyup.enter="handleSearch"
        :clearable="false"
      />
      <el-button class="el-radio-button__inner" @click="handleSearch">查询</el-button>
      <el-button class="el-radio-button__inner" @click="handleRefresh">重置</el-button>
      <el-button class="el-radio-button__inner" @click="handleAdd">新增</el-button>
    </div>

    <!-- 表格和分页合并容器 -->
    <div class="table-pagination-container">
      <el-table
        v-loading="loading"
        :data="list"
        border
        style="width: 100%; border-bottom: none; border-radius: 4px 4px 0 0;">
        <el-table-column
          prop="name"
          label="角色名称"
          width="180">
        </el-table-column>
        <el-table-column
          prop="code"
          label="角色编码"
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
          width="240">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="handleAssignMenus(scope.row)">权限分配</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
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
      </div>
    </div>

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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入描述" />
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

    <!-- 权限分配对话框 -->
    <el-dialog
      :title="'权限分配 - ' + currentRoleName"
      v-model="menuDialogVisible"
      width="500px">
      <div style="max-height: 400px; overflow-y: auto;">
        <el-tree
          ref="menuTree"
          :data="menuTreeData"
          show-checkbox
          node-key="id"
          :default-expand-all="true"
          :props="{
            children: 'children',
            label: 'name'
          }"
        />
      </div>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getRoleList, addRole, updateRole, deleteRole, assignMenus, getMenuIdsByRoleId } from '@/api/role'
import { getMenuList } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 权限分配相关
const menuDialogVisible = ref(false)
const currentRoleId = ref(null)
const currentRoleName = ref('')
const menuTree = ref(null)
const menuTreeData = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const form = reactive({
  id: undefined,
  name: '',
  code: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    // loading.value = true
    const { data } = await getRoleList(queryParams.pageNum, queryParams.pageSize, queryParams.keyword)
    list.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取菜单树数据
const getMenuTree = async () => {
  try {
    console.log('开始获取菜单树数据...')
    // 从后端获取完整的菜单树
    const res = await getMenuList()
    console.log('菜单树响应:', res)
    
    // 处理菜单数据，确保格式适配el-tree组件
    // 检查响应格式并提取数据
    if (res && res.code === 200 && res.data) {
      // 确保menuTreeData是数组
      const menus = Array.isArray(res.data) ? res.data : [res.data]
      menuTreeData.value = menus
      console.log('菜单树数据设置成功:', menus)
    } else {
      console.warn('菜单树数据格式异常:', res)
      menuTreeData.value = []
    }
    
    // 如果当前正在分配权限，重新设置选中状态
    if (currentRoleId.value && menuTree.value) {
      console.log('获取角色ID为', currentRoleId.value, '的菜单权限')
      const roleRes = await getMenuIdsByRoleId(currentRoleId.value)
      console.log('角色菜单权限响应:', roleRes)
      
      if (roleRes && roleRes.code === 200 && roleRes.data) {
        // 确保nextTick后再设置选中状态，保证DOM已渲染
        nextTick(() => {
          if (menuTree.value) {
            // 确保menuIds是数组
            const menuIds = Array.isArray(roleRes.data) ? roleRes.data : [roleRes.data]
            console.log('设置选中的菜单ID:', menuIds)
            menuTree.value.setCheckedKeys(menuIds)
          }
        })
      }
    }
  } catch (error) {
    console.error('获取菜单树失败:', error)
    ElMessage.error('获取菜单树失败：' + (error.message || '未知错误'))
    // 即使出错也设置空数组，避免树组件错误
    menuTreeData.value = []
  }
}

// 处理权限分配
const handleAssignMenus = async (row) => {
  console.log('打开权限分配对话框，角色:', row.name, 'ID:', row.id)
  currentRoleId.value = row.id
  currentRoleName.value = row.name
  
  // 先显示对话框，确保菜单树组件已被创建
  menuDialogVisible.value = true
  
  try {
    // 使用nextTick确保对话框DOM已渲染，菜单树组件已创建
    await nextTick()
    // 再获取最新的菜单树数据（会自动设置当前角色的选中状态）
    await getMenuTree()
    console.log('菜单树数据获取完成并设置了选中状态')
  } catch (error) {
    console.error('获取角色菜单权限失败:', error)
  }
}

// 保存菜单权限
const handleSaveMenus = async () => {
  if (!menuTree.value || !currentRoleId.value) {
    ElMessage.warning('数据不完整，无法保存权限')
    return
  }
  
  try {
    // 获取选中的菜单ID并确保为数值类型
    const selectedMenuIds = menuTree.value.getCheckedKeys().map(id => {
      // 转换为数值并确保是数字类型
      return +id;
    })
    
    // 显示加载状态
    loading.value = true
    
    // 创建一个新对象，确保类型正确
    const assignData = {
      roleId: +currentRoleId.value,
      menuIds: selectedMenuIds
    }
    
    // 调用assignMenus接口
    const res = await assignMenus(assignData)
    
    ElMessage.success('权限分配成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('权限分配失败:', error)
    ElMessage.error(error.message || '权限分配失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 处理添加
const handleAdd = () => {
  dialogTitle.value = '添加角色'
  form.id = undefined
  form.name = ''
  form.code = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该角色吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除角色失败:', error)
      if (error.response && error.response.data && error.response.data.msg) {
        ElMessage.error(error.response.data.msg)
      } else {
        ElMessage.error('删除失败')
      }
    }
  })
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateRole(form)
      ElMessage.success('更新成功')
    } else {
      await addRole(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 监听搜索关键词变化
const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

// 处理刷新操作
const handleRefresh = () => {
  // 重置搜索条件
  queryParams.keyword = ''
  queryParams.pageNum = 1
  // 重新获取数据
  getList()
}

onMounted(() => {
  getList()
  getMenuTree()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 添加边框阴影 */
}

/* 修改输入框背景颜色和边框 */
.filter-container .el-input {
  --el-input-bg-color: #48494c;
}
.filter-container .el-input__wrapper {
  background-color: #48494c !important;
  box-shadow: none !important;
  border: 1px solid white !important;
}
.filter-container :deep(.el-input__wrapper) {
  background-color: #48494c !important;
  box-shadow: none !important;
  border: 1px solid white !important;
}
.filter-container :deep(.el-input__inner) {
  background-color: #48494c !important;
  color: white;
  border: none;
}

/* 表格和分页合并容器 */
.table-pagination-container {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 统一的边框阴影 */
  overflow: hidden;
  margin-bottom: 0;
}

/* 表格样式 - 使用深度选择器确保优先级 */
:deep(.el-table) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  margin-bottom: 0;
  border-bottom: none;
}

/* 确保表格内容区域背景色一致 */
:deep(.el-table__inner-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格滚动容器背景色 */
:deep(.el-table__inner-wrapper .el-scrollbar) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格滚动视图背景色 */
:deep(.el-table__inner-wrapper .el-scrollbar__wrap) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格滚动内容视图背景色 */
:deep(.el-table__inner-wrapper .el-scrollbar__view) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 表格头部背景 */
:deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 表格表头行背景 */
:deep(.el-table__header tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表头单元格背景一致 */
:deep(.el-table__header tr th.el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

/* 表格主体背景 */
:deep(.el-table__body-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格行背景透明，显示渐变背景 */
:deep(.el-table__row) {
  background: transparent !important;
}

/* 表格单元格样式 */
:deep(.el-table__cell) {
  background: transparent !important;
  color: white !important;
}

/* 移除表格行鼠标悬停效果 */
:deep(.el-table__row:hover) {
  background: transparent !important;
}

/* 移除表格单元格鼠标悬停效果 */
:deep(.el-table__cell:hover) {
  background: transparent !important;
  color: white !important;
}

/* 确保Element UI默认的悬停效果被完全覆盖 */
:deep(.el-table--enable-row-hover .el-table__body tr:hover>td) {
  background: transparent !important;
}

/* 移除表格行背景色变化 */
:deep(.el-table__body tr:hover) {
  background: transparent !important;
}

/* 表格头部单元格 */
:deep(.el-table__header .el-table__cell) {
  background: transparent !important;
  color: white !important;
}

/* 表格边框颜色 */
:deep(.el-table) {
  --el-table-border-color: rgba(255, 255, 255, 0.1) !important;
}

/* 移除表格竖线，保留横线 */
:deep(.el-table__cell) {
  border-right: none !important;
  border-left: none !important;
  border-bottom: 1px solid #ffffff !important;
}

/* 确保表头单元格也没有竖线，并将底部边框加粗作为第一条横线 */
:deep(.el-table__header tr th.el-table__cell) {
  border-right: none !important;
  border-left: none !important;
  border-bottom: 1.5px solid #ffffff !important;
}

/* 移除表格左侧边框补丁 */
:deep(.el-table__border-left-patch) {
  display: none !important;
}

/* 分页包装器样式 */
.pagination-wrapper {
  padding: 0 16px 16px 16px;
  border-top: 1px solid #ffffff;
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

/* 分页控件样式 */
.el-pagination {
  background-color: transparent; /* 继承父容器背景色 */
  padding: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  min-height: 60px; /* 确保有足够的高度来实现垂直居中 */
  border-top: none; /* 移除顶部边框，由pagination-wrapper统一管理 */
  color: white !important; /* 设置分页控件文字颜色为白色 */
}

/* 确保分页控件内部所有子元素垂直居中 */
.el-pagination > * {
  display: flex;
  align-items: center;
  color: white !important; /* 确保所有子元素文字颜色为白色 */
}

/* 确保分页总数文字为白色 */
:deep(.el-pagination__total) {
  color: white !important;
}

/* 为分页控件中的上一页和下一页按钮设置背景颜色 */
:deep(.btn-prev),
:deep(.btn-next) {
  background-color: #48494c !important;
  border: 1px solid white !important;
  border-radius: 4px;
}

/* 确保分页按钮中的图标颜色 */
:deep(.btn-prev .el-icon),
:deep(.btn-next .el-icon) {
  color: white !important;
}

/* 确保禁用状态下的按钮背景色 */
:deep(.btn-prev:disabled),
:deep(.btn-next:disabled) {
  background-color: #48494c !important;
  border: 1px solid white !important;
}

/* 为分页控件中当前激活的页码li元素设置背景颜色和文字颜色 */
:deep(.is-active.number) {
  background-color: #48494c !important;
  border: 1px solid white !important;
  color: white !important;
}

/* 为分页控件中的选择器设置背景颜色 */
:deep(.el-select__wrapper) {
  background-color: #48494c !important;
  border: 1px solid white !important;
}

/* 确保选择器内部元素背景色一致 */
:deep(.el-select__selection) {
  background-color: #48494c !important;
}

/* 确保选择器占位符文字颜色 */
:deep(.el-select__placeholder) {
  color: white !important;
}

/* 通用el-input__wrapper样式 */
:deep(.el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid white !important;
  box-shadow: none !important;
  color: white !important;
}

/* 确保输入框内部文字颜色为白色 */
:deep(.el-input__inner) {
  color: white !important;
}

/* 为按钮添加圆角处理和白色边框 */
.filter-container .el-button.el-radio-button__inner {
  border-radius: 4px;
  border: 1px solid #ffffff;
}

/* 为按钮添加与el-radio-button__inner一致的鼠标悬停效果 */
.filter-container .el-button.el-radio-button__inner:hover {
  color: #274151;
  background-color: #5a5b5f;
  border-color: #ffffff;
}

/* 为添加角色对话框设置背景颜色 - 使用深度选择器提高优先级 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

/* 确保对话框内容区域背景正确 */
:deep(.el-dialog__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: white;
  padding: 20px;
}

/* 确保对话框头部背景正确 */
:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: white;
  padding: 20px;
  border-bottom: none;
}

/* 确保对话框底部背景正确 */
:deep(.el-dialog__footer) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: white;
  padding: 10px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 对话框标题颜色 */
:deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

/* 适配深色背景的表单元素 - 使用深度选择器 */
:deep(.el-dialog__body .el-form-item__label) {
  color: white;
}

:deep(.el-dialog__body .el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

:deep(.el-dialog__body .el-input__inner) {
  background-color: #48494c !important;
  color: white !important;
}

:deep(.el-dialog__body .el-textarea__inner) {
  background-color: #48494c !important;
  color: white !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

/* 确保所有textarea元素都有白色边框 */
:deep(.el-textarea__inner) {
  border: 1px solid #ffffff !important;
}

:deep(.el-dialog__body .el-radio__label) {
  color: white;
}

/* 修改单选按钮选中时的颜色和边框 */
:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #48494c !important;
  border-color: #ffffff !important;
}

/* 对话框内容区域背景色 */
:deep(.el-dialog__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 权限分配对话框中包裹el-tree的div背景色 */
:deep(.el-dialog__body > div) {
  background: #48494c !important;
  border: 1px solid white !important;
  color: white;
  border-radius: 6px;
}

/* el-tree组件背景色和文字颜色 */
:deep(.el-tree) {
  background: transparent;
  color: white;
}

/* 确保el-tree节点文字颜色为白色 */
:deep(.el-tree-node__label) {
  color: white;
}

/* 为每个权限项（el-tree节点）添加鼠标悬停效果 */
:deep(.el-tree-node__content) {
  &:hover {
    background-color: #5A5B5F;
  }
}

/* 对话框按钮样式 */
:deep(.el-dialog__footer .el-button) {
  background-color: white;
  border: 1px solid white;
  color:#274151;
}

:deep(.el-dialog__footer .el-button:hover) {
  background-color: white;
  color: #409eff;
}

/* 按钮文字选中时变为蓝色 */
:deep(.el-dialog__footer .el-button span) {
  user-select: text;
}

:deep(.el-dialog__footer .el-button:focus span),
:deep(.el-dialog__footer .el-button:active span),
:deep(.el-dialog__footer .el-button span::selection) {
  color: #409eff !important;
}

:deep(.el-dialog__footer .el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: white;
}
</style>