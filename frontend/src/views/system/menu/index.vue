<template>
    <div class="filter-container">
      <el-input 
        v-model="searchKeyword" 
        placeholder="请输入菜单名称" 
        style="width: 200px; margin-right: 10px;"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button type="primary" @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd" style="margin-left: 10px;">新增</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" style="width: 100%" row-key="id">
      <el-table-column prop="name" label="菜单名称" />
      <el-table-column prop="path" label="路由路径" />
      <el-table-column prop="component" label="组件路径" />
      <el-table-column prop="icon" label="图标">
        <template #default="scope">
          <el-icon v-if="scope.row.icon && iconMap[scope.row.icon]">
            <component :is="iconMap[scope.row.icon]" />
          </el-icon>
          <span v-else>{{ scope.row.icon || '无' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="hidden" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.hidden ? 'info' : 'success'">
            {{ scope.row.hidden ? '隐藏' : '显示' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

  <!-- 弹窗 -->
  <el-dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="500px"
    @close="handleDialogClose"
    :custom-class="'custom-dialog'"
    :style="dialogStyle"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入菜单名称" />
      </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标类名" />
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级菜单"
            clearable
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="是否隐藏">
          <el-switch v-model="form.hidden" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, Setting, Goods, List, Shop, Money, Box, DataAnalysis, Management, ShoppingCart } from '@element-plus/icons-vue'
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api/menu'

// 图标映射
const iconMap = {
  'House': House,
  'Setting': Setting,
  'Goods': Goods,
  'List': List,
  'Shop': Shop,
  'Money': Money,
  'Box': Box,
  'DataAnalysis': DataAnalysis,
  'Management': Management,
  'ShoppingCart': ShoppingCart
}

// 对话框样式
const dialogStyle = {
  background: 'linear-gradient(135deg, #464e58 0%, #434c55 100%)',
  backgroundColor: 'transparent',
  '--el-dialog-background-color': 'transparent',
  '--el-bg-color': 'transparent',
  //'--el-color-primary': 'blue',
  '--el-color-white': 'white',
  '--el-text-color-primary': 'white',
  '--el-text-color-regular': '#274151',
  '--el-text-color-secondary': 'white',
  color: 'white',
  // 确保所有子元素文字颜色
  '--dialog-text-color': 'white'
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  path: '',
  component: '',
  icon: '',
  parentId: null,
  sort: 0,
  hidden: false
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
}

const tableData = ref([])
const menuTree = ref([])
const searchKeyword = ref('')

// 获取列表数据
const getList = async () => {
  try {
    const res = await getMenuList()
    if (res.code === 200) {
      // 保存所有菜单数据
      const allMenus = res.data
      
      // 如果有搜索关键字，直接在所有菜单中搜索
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        // 简单直接的过滤，匹配所有菜单（包括子菜单）
        tableData.value = allMenus.filter(menu => 
          menu.name.toLowerCase().includes(keyword)
        )
      } else {
        // 无搜索时显示所有数据
        tableData.value = allMenus
      }
      
      // 为下拉选择框准备菜单树
      menuTree.value = [{ id: 0, name: '顶级菜单', children: allMenus }]
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    ElMessage.error('获取菜单列表失败')
  }
}

// 搜索
const handleSearch = () => {
  getList()
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
  getList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    name: '',
    path: '',
    component: '',
    icon: '',
    parentId: null,
    sort: 0,
    hidden: false
  })
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该菜单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMenu(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = form.id ? updateMenu : addMenu
        const res = await api(form)
        if (res.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error(form.id ? '更新菜单失败:' : '添加菜单失败:', error)
        ElMessage.error(form.id ? '更新菜单失败' : '添加菜单失败')
      }
    }
  })
}

// 关闭弹窗
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 初始化
getList()
</script>

<style scoped>
.filter-container {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    padding: 16px;
    border-radius: 4px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    border: none;
  }

.filter-container :deep(.el-input__wrapper) {
    background-color: #48494c !important;
    color: white !important;
    --el-input-bg-color: #48494c !important;
  }
  
  .filter-container :deep(.el-input__wrapper):focus-within {
    box-shadow: 0 0 0 0.7px white !important;
    border-color: white !important;
  }
  
  .filter-container :deep(.el-input__inner) {
    background-color: #48494c !important;
    color: white !important;
  }

/* 修改查询、重置、新增按钮样式 */
.filter-container :deep(.el-button) {
  background-color: white !important;
  color: #274151 !important;
  border: 1px solid #48494c !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  outline: none !important;
  cursor: pointer !important;
  transition: background-color 0.3s ease;
}

/* 为按钮添加适当的圆角 */
.filter-container :deep(.el-button:first-child) {
  border-top-left-radius: 4px !important;
  border-bottom-left-radius: 4px !important;
}

.filter-container :deep(.el-button:last-child) {
  border-top-right-radius: 4px !important;
  border-bottom-right-radius: 4px !important;
}

/* 按钮悬停效果 - 确保所有按钮都有统一的悬停样式 */
.filter-container :deep(.el-button) {
  transition: all 0.3s ease !important;
}

/* 为所有按钮添加统一的悬停效果 */
.filter-container :deep(.el-button:hover) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  box-shadow: 0 0 0 2px #ffffff !important;
}

/* 为primary类型按钮添加悬停效果 */
.filter-container :deep(.el-button.el-button--primary:hover) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  box-shadow: 0 0 0 0px #ffffff !important;
}

/* 按钮激活效果 */
.filter-container :deep(.el-button:active) {
  background-color: #f0f0f0 !important;
}

.el-table {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3) !important;
  border-radius: 8px !important;
  overflow: hidden !important;
  border: 0px solid #dcdfe6 !important;
  background-color: white !important;
}

/* 确保内部包装器也有阴影 */
.el-table__inner-wrapper {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3) !important;
  border-radius: 8px !important;
}

/* 对话框内的表单元素样式 */
:deep(.custom-dialog .el-form .el-input__wrapper) {
  background-color: #48494c !important;
  color: white !important;
  --el-input-bg-color: #48494c !important;
}

:deep(.custom-dialog .el-form .el-input__inner) {
  background-color: #48494c !important;
  color: white !important;
}

:deep(.custom-dialog .el-form .el-select__wrapper) {
  background-color: #48494c !important;
  color: white !important;
}

:deep(.custom-dialog .el-form .el-select__placeholder) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.custom-dialog .el-form .el-input-number__decrease),
:deep(.custom-dialog .el-form .el-input-number__increase) {
  background-color: #5a5b5f !important;
  color: white !important;
}

:deep(.custom-dialog .el-form .el-input-number__decrease:hover),
:deep(.custom-dialog .el-form .el-input-number__increase:hover) {
  background-color: #6a6b6f !important;
}

:deep(.custom-dialog .el-form-item__label) {
  color: white !important;
}

/* 对话框内的按钮样式 - 完全复制用户管理页面样式 */
:deep(.custom-dialog .el-button) {
  background-color: white !important;
  color: #274151 !important;
  border: 1px solid #48494c !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  outline: none !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
}

/* 按钮悬停效果 */
:deep(.custom-dialog .el-button:hover) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  box-shadow: 0 0 0 2px #ffffff !important;
}

/* 为primary类型按钮添加特定悬停效果 */
:deep(.custom-dialog .el-button.el-button--primary:hover) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  box-shadow: 0 0 0 0px #ffffff !important;
}

/* 按钮激活效果 */
:deep(.custom-dialog .el-button:active) {
  background-color: #f0f0f0 !important;
}

/* 确保对话框标题为白色 */
:deep(.el-dialog__title) {
  color: white !important;
}

/* 使用最高优先级选择器确保表单标签显示为白色 */
:deep(.custom-dialog .el-form-item__label),
:deep(.el-form-item__label[for^="el-id-"]) {
  color: white !important;
  font-weight: normal !important;
  opacity: 1 !important;
  filter: none !important;
  -webkit-text-fill-color: white !important;
}

/* 确保输入框和选择器的文字为白色并设置背景颜色 */
:deep(.custom-dialog .el-input__wrapper),
:deep(.custom-dialog .el-input__inner),
:deep(.custom-dialog .el-select__wrapper),
:deep(.custom-dialog .el-select__input) {
  background-color: #48494c !important;
  color: white !important;
  --el-input-bg-color: #48494c !important;
  --el-input-group-bg-color: #48494c !important;
}

/* 直接针对选中的div元素设置背景色 */
:deep(.el-input__wrapper) {
  background-color: #48494c !important;
  --el-input-bg-color: #48494c !important;
  --el-bg-color: #48494c !important;
  box-shadow: none !important;
  border-color: white !important;
  border-width: 1px !important;
  border-style: solid !important;
}

/* 确保输入框内部背景色 */
:deep(.el-input__wrapper > .el-input__inner) {
  background-color: #48494c !important;
  color: white !important;
  border: none !important;
}

/* 确保选择器wrapper背景色 - 最高优先级 */
:deep(.el-select__wrapper),
:deep(.el-select__wrapper.el-tooltip__trigger) {
  background-color: #48494c !important;
  --el-input-bg-color: #48494c !important;
  --el-bg-color: #48494c !important;
  box-shadow: none !important;
  border-color: white !important;
  border-width: 1px !important;
  border-style: solid !important;
}

/* 针对选中的div元素及其下拉菜单的背景色设置 - 最高优先级策略 */
/* 1. 直接覆盖选择器触发元素 */
:global(.el-select__wrapper),
:global(.el-select__wrapper.el-tooltip__trigger) {
  background-color: #48494c !important;
  --el-input-bg-color: #48494c !important;
  --el-bg-color: #48494c !important;
  box-shadow: none !important;
  border-color: white !important;
  border-width: 1px !important;
  border-style: solid !important;
}

/* 2. 覆盖所有下拉菜单容器 - 确保最高优先级 */
:global(.el-select-dropdown),
:global(.el-select-dropdown__menu),
:global(.el-dropdown-menu),
:global(.el-select-dropdown.is-multiple),
:global(.el-select-dropdown.is-empty) {
  background-color: #48494c !important;
  --el-bg-color: #48494c !important;
  --el-dropdown-menu-bg-color: #48494c !important;
  background-image: none !important;
  background-clip: padding-box !important;
  border-color: #666 !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5) !important;
  z-index: 9999 !important;
}

/* 3. 覆盖所有弹出层元素 */
:global(.el-popper),
:global(.el-popper.is-light),
:global(.el-popper[x-placement^="bottom"]),
:global(.el-popper[x-placement^="top"]),
:global(.el-popper[x-placement^="left"]),
:global(.el-popper[x-placement^="right"]),
:global(.el-popover) {
  background-color: #48494c !important;
  --el-bg-color: #48494c !important;
  --el-dropdown-menu-bg-color: #48494c !important;
  background-image: none !important;
  border-color: #666 !important;
}

/* 4. 覆盖下拉菜单内部所有容器 */
:global(.el-select-dropdown__wrap),
:global(.el-select-dropdown__list),
:global(.el-scrollbar__wrap),
:global(.el-scrollbar__view),
:global(.el-select-dropdown__content) {
  background-color: #48494c !important;
  --el-bg-color: #48494c !important;
}

/* 5. 覆盖所有选项元素 */
:global(.el-select-dropdown__item),
:global(.el-dropdown-menu__item) {
  background-color: #48494c !important;
  color: white !important;
  --el-dropdown-menu-item-hover-bg-color: #58595c !important;
}

/* 6. 覆盖悬停和选中状态 */
:global(.el-select-dropdown__item:hover),
:global(.el-dropdown-menu__item:hover) {
  background-color: #58595c !important;
  color: white !important;
}

:global(.el-select-dropdown__item.selected),
:global(.el-dropdown-menu__item.is-active),
:global(.el-dropdown-menu__item.is-disabled) {
  background-color: #58595c !important;
  color: white !important;
}

/* 6.1 确保下拉菜单占位符文字为白色 */
:global(.el-select__placeholder) {
  color: white !important;
  opacity: 1 !important;
}

/* 7. 使用ID和属性选择器确保最大覆盖范围 */
:global([id^="el-select-dropdown"]),
:global([role="listbox"]),
:global(.el-scrollbar) {
  background-color: #48494c !important;
  color: white !important;
}

/* 7.1 针对滚动条滑块设置颜色 */
:global(.el-scrollbar__thumb),
:global(.el-scrollbar__thumb:hover),
:global(.el-scrollbar__bar:hover .el-scrollbar__thumb) {
  background-color: #5a5b5f !important;
}

/* 7.2 针对滚动条轨道设置样式 */
:global(.el-scrollbar__bar) {
  background-color: transparent !important;
}

/* 7.3 针对垂直滚动条的特定样式 */
:global(.el-scrollbar__bar.is-vertical .el-scrollbar__thumb) {
  background-color: #5a5b5f !important;
}

/* 7.4 确保滑块在不同状态下的颜色 */
:global(.el-scrollbar__thumb:horizontal),
:global(.el-scrollbar__thumb:vertical) {
  background-color: #5a5b5f !important;
}

/* 8. 专门针对选中的ul元素及其树状结构设置背景色 */
:global(ul.el-scrollbar__view.el-select-dropdown__list),
:global(ul[role="listbox"]),
:global(ul.el-select-dropdown__list) {
  background-color: #48494c !important;
  --el-bg-color: #48494c !important;
  color: white !important;
}

/* 针对表格内部包装器设置渐变背景 - 使用deep选择器提高优先级 */
:deep(.el-table__inner-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 同时覆盖el-table的背景色和文字颜色 */
.el-table {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
}

/* 确保表头行也应用渐变背景和白色文字 */
:deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
}

:deep(.el-table__header th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
}

:deep(.el-table__header .el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
  border-bottom: 1.3px solid #ffffff !important;
}

/* 确保表格内容单元格文字为白色 */
:deep(.el-table__cell) {
  color: white !important;
}

/* 确保表格行内容文字为白色并设置高优先级 */
:deep(.el-table__row) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
  transition: none !important;
}

/* 彻底去除表格行鼠标悬停效果 - 使用更高优先级选择器 */
:deep(.el-table__row:hover),
:deep(.el-table__row.hover-row),
:deep(.el-table__row.current-row) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  transition: none !important;
}

/* 确保表格单元格悬停也不改变背景 */
:deep(.el-table__cell:hover),
:deep(.el-table__cell.hover-row-cell),
:deep(.el-table__cell.current-row-cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格主体包装器不影响悬停效果 */
:deep(.el-table__body-wrapper:hover) {
  background: transparent !important;
}

/* 针对Element UI默认样式的额外覆盖 */
.el-table :deep(.el-table__row:hover > td),
.el-table :deep(.el-table__row:hover > th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 使用:global选择器强制覆盖全局样式 */
:global(.el-table__row:hover) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}
:global(.el-table__row:hover > td) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 确保表格单元格内文本为白色 */
:deep(.el-table__cell .cell) {
  color: white !important;
}

/* 针对开关按钮设置背景色和白色边框 */
:global(.el-switch__core) {
  /* background-color: #409EFF !important; */
  border-color: white !important;
  border-width: 1px !important;
  border-style: solid !important;
}

/* 确保开关按钮激活状态下保持白色边框 */
:global(.el-switch.is-active .el-switch__core) {
  border-color: white !important;
  border-width: 1px !important;
  border-style: solid !important;
}

/* 9. 针对树状结构容器设置背景色 */
:global(.el-tree),
:global(.el-tree-node),
:global(.el-tree-node__content),
:global(.el-tree-node__children) {
  background-color: #48494c !important;
  --el-bg-color: #48494c !important;
  color: white !important;
}

/* 10. 确保树节点内容背景色 */
:global(.el-tree-node__content) {
  background-color: #48494c !important;
  color: white !important;
}

/* 11. 确保树节点展开/折叠图标容器背景色 */
:global(.el-tree-node__expand-icon) {
  background-color: #48494c !important;
  color: white !important;
}

/* 确保表单必填星号显示为红色 - 增强版样式 */
/* 方法1：针对伪元素星号 */
:deep(.custom-dialog .el-form-item.is-required:not(.is-no-asterisk) .el-form-item__label)::before {
  content: '*' !important;
  color: red !important;
  margin-right: 4px !important;
  font-size: 14px !important;
  font-weight: bold !important;
  opacity: 1 !important;
}

/* 方法2：针对独立星号元素 */
:deep(.custom-dialog .el-form-item.is-required:not(.is-no-asterisk) .el-form-item__label__asterisk) {
  color: red !important;
  margin-left: 4px !important;
  font-size: 14px !important;
  font-weight: bold !important;
  opacity: 1 !important;
  filter: none !important;
}

/* 方法3：使用!important和内联样式优先级覆盖 */
:deep(.el-form-item__label__asterisk),
:deep(.el-form-item__label::before) {
  color: #F2676E !important;
  color: #F2676E !important; /* RGB格式确保兼容性 */
  font-weight: bold !important;
  opacity: 1 !important;
  filter: none !important;
  -webkit-text-fill-color: #F2676E !important; /* 防止文字被填充其他颜色 */
}

/* 直接针对dialog中的所有星号元素 */
:deep(.custom-dialog .el-form-item__label__asterisk),
:deep(.custom-dialog .el-form-item__label::before) {
  color: #F2676E !important;
  color: #F2676E !important;
  font-weight: bold !important;
  opacity: 1 !important;
  filter: none !important;
  -webkit-text-fill-color: #F2676E !important;
}

</style>