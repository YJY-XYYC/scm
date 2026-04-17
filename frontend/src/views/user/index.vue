<template>
  <div class="user-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="请选择角色" clearable>
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
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
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
          <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
            批量删除
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="userList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="用户名" prop="username" />
        <el-table-column label="姓名" prop="name" />
        <el-table-column label="手机号" prop="phone" />
        <!-- 角色列 - 确保总是可见 -->
        <el-table-column label="角色" prop="role" width="120" fixed show-overflow-tooltip>
          <template #default="scope">
            <div style="min-height: 20px; padding: 2px 0;">
              <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'info'" size="small" style="display: inline-block;">
                {{ scope.row.role || '未设置' }}
              </el-tag>
              <span style="font-size: 12px; color: #909399;">{{ scope.row.role ? (scope.row.role === 'admin' ? '管理员' : '普通用户') : '暂无角色' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email" />
        <el-table-column label="状态" width="100">
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 用户表单对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialog.type === 'add'">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <!-- 角色选择器 - 从sys_role表获取数据，绑定到sys_user表的role字段 -->
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <template v-if="roles && roles.length > 0">
              <el-option v-for="role in roles" :key="role.code" :label="role.name" :value="role.code" />
            </template>
            <template v-else>
              <!-- 默认角色选项作为降级方案 -->
              <el-option label="管理员" value="admin" />
              <el-option label="普通用户" value="user" />
            </template>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPwdDialog.visible"
      width="500px"
    >
      <el-form
        ref="resetPwdFormRef"
        :model="resetPwdForm"
        :rules="resetPwdRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPwdDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleResetPwdSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, updateUserStatus } from '@/api/user'
import { getAllActiveRoles } from '@/api/role'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  name: '',
  role: '',
  status: undefined
})

// 用户列表数据
const userList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedIds = ref([])

// 所有可用角色
const roles = ref([])

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    userList.value = res.data.records
    total.value = res.data.total
    // 添加调试信息，检查用户数据中是否包含role字段
    console.log('用户列表数据:', userList.value.map(item => ({id: item.id, username: item.username, role: item.role})))
  } catch (error) {
    console.error('获取用户列表失败：', error)
  }
  loading.value = false
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.username = ''
  queryParams.name = ''
  queryParams.role = ''
  queryParams.status = undefined
  handleQuery()
}

// 表格多选
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
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

// 用户表单对话框
const dialog = reactive({
  visible: false,
  title: '',
  type: ''
})

// 用户表单
const userFormRef = ref()
const userForm = reactive({
  id: undefined,
  username: '',
  name: '',
  password: '',
  phone: '',
  email: '',
  role: ''
})

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取所有可用角色
const loadRoles = async () => {
  try {
    console.log('开始加载角色数据...')
    const res = await getAllActiveRoles()
    console.log('角色数据响应：', res)
    
    // 确保数据格式正确
    if (res && res.data && Array.isArray(res.data)) {
      roles.value = res.data
      console.log('角色数据加载成功，共', roles.value.length, '个角色')
      
      // 如果没有角色数据，提供默认选项
      if (roles.value.length === 0) {
        roles.value = [
          { code: 'admin', name: '管理员' },
          { code: 'user', name: '普通用户' }
        ]
        console.log('没有从后端获取到角色数据，使用默认角色选项')
      }
    } else {
      console.error('角色数据格式错误:', res)
      // 使用默认角色选项
      roles.value = [
        { code: 'admin', name: '管理员' },
        { code: 'user', name: '普通用户' }
      ]
    }
  } catch (error) {
    console.error('获取角色列表失败：', error)
    // 出错时使用默认角色选项
    roles.value = [
      { code: 'admin', name: '管理员' },
      { code: 'user', name: '普通用户' }
    ]
  }
}

// 新增用户
const handleAdd = () => {
  dialog.type = 'add'
  dialog.title = '新增用户'
  dialog.visible = true
}

// 修改用户
const handleUpdate = (row) => {
  dialog.type = 'edit'
  dialog.title = '修改用户'
  dialog.visible = true
  // 复制用户数据到表单
  Object.assign(userForm, row)
  console.log('编辑用户：', userForm, '当前角色选项：', roles.value)
}

// 提交表单
const handleSubmit = async () => {
  await userFormRef.value.validate()
  try {
    if (dialog.type === 'add') {
      await addUser(userForm)
      ElMessage.success('新增成功')
    } else {
      await updateUser(userForm)
      ElMessage.success('修改成功')
    }
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('操作失败：', error)
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '警告', {
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败：', error)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个用户吗？`, '警告', {
      type: 'warning'
    })
    await deleteUser(selectedIds.value)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('批量删除失败：', error)
  }
}

// 状态改变
const handleStatusChange = async (row) => {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败：', error)
    row.status = row.status === 1 ? 0 : 1 // 恢复状态
  }
}

// 重置密码
const resetPwdDialog = reactive({
  visible: false,
  userId: undefined
})

const resetPwdFormRef = ref()
const resetPwdForm = reactive({
  newPassword: ''
})

const resetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleResetPassword = (row) => {
  resetPwdDialog.userId = row.id
  resetPwdDialog.visible = true
}

const handleResetPwdSubmit = async () => {
  await resetPwdFormRef.value.validate()
  try {
    await resetPassword(resetPwdDialog.userId, resetPwdForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPwdDialog.visible = false
  } catch (error) {
    console.error('密码重置失败：', error)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  userFormRef.value?.resetFields()
  Object.assign(userForm, {
    id: undefined,
    username: '',
    name: '',
    password: '',
    phone: '',
    email: '',
    role: ''
  })
}

onMounted(() => {
  getList()
  loadRoles()
})
</script>

<style lang="scss" scoped>
.user-container {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
    background: white !important;
    border-radius: 4px;
  }
  
  .table-card {
    background: white !important;
    border-radius: 4px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
      background: white !important;
      padding: 16px;
      border-radius: 4px;
    }
  }
  
  /* 确保卡片内容区域有白色背景 */
  :deep(.el-card__body) {
    background: white !important;
    padding: 16px !important;
    border: none !important;
  }
  
  /* 直接为el-pagination元素添加样式 */
  :deep(.el-pagination) {
    background: white !important;
    padding: 16px;
    border-radius: 4px;
  }
}
</style>