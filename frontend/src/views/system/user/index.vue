<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.username"
        placeholder="请输入用户名"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
        clearable
        @clear="handleQuery"
      />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button type="primary" @click="handleReset">重置</el-button>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <!-- 表格和分页合并容器 -->
    <div class="table-pagination-container">
      <!-- 表格 -->
      <el-table :data="tableData" style="width: 100%; border-radius: 4px 4px 0 0; border-bottom: none;">
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <!-- 修正头像URL构建逻辑，确保正确拼接/api前缀 -->
            <el-avatar :size="40" :src="scope.row.avatar ? (scope.row.avatar.startsWith('/api') ? scope.row.avatar : '/api' + scope.row.avatar) : undefined">
              <span v-if="!scope.row.avatar">{{ scope.row.realName?.charAt(0) || scope.row.username?.charAt(0) || '用' }}</span>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            {{ scope.row.role === 'ADMIN' ? 'admin' : scope.row.role === 'USER' ? 'user' : scope.row.role }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
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

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @current-change="handleCurrentChange"
          style="display: flex; align-items: center; gap: 10px; flex-wrap: nowrap;"
        />
      </div>
    </div>

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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-uploader">
                <el-avatar :size="100" :src="form.avatar ? (form.avatar.startsWith('/api') ? form.avatar : '/api' + form.avatar) : undefined">
                  <span v-if="!form.avatar">{{ form.realName?.charAt(0) || form.username?.charAt(0) || '用' }}</span>
                </el-avatar>
            <el-upload
              class="avatar-upload"
              action="/api/user/avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="small" style="margin-top: 10px;">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.realName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user'
import { getAllActiveRoles } from '@/api/role'
import { useUserStore } from '@/stores/user'

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const roles = ref([])

// 对话框样式
const dialogStyle = {
  background: 'linear-gradient(135deg, #464e58 0%, #434c55 100%)',
  backgroundColor: 'transparent',
  '--el-dialog-background-color': 'transparent',
  '--el-bg-color': 'transparent',
  color: 'white'
}

// 获取所有角色
const fetchRoles = async () => {
  try {
    const res = await getAllActiveRoles()
    if (res.code === 200) {
      roles.value = res.data
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const queryParams = reactive({
  username: ''
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

// 重置
const handleReset = () => {
  queryParams.username = ''
  pageNum.value = 1
  handleQuery()
}

// 查询
const handleQuery = async () => {
  try {
    // 检查搜索框是否为空，为空时也执行查询以刷新数据
    const res = await getUserList({
      username: queryParams.username || '',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
  Object.assign(form, {
      id: null,
      username: '',
      password: '',
      realName: '',
      email: '',
      phone: '',
      avatar: '',
      role: '',
      status: 1
    })
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
  Object.assign(form, row)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        handleQuery()
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = form.id ? updateUser : addUser
        const res = await api(form)
        if (res.code === 200) {
          // 如果编辑的是当前登录用户的信息，同步更新全局用户状态和localStorage
          // 确保小头像能显示最新的头像
          const currentUsername = localStorage.getItem('username') || ''
          
          // 只有当编辑的用户与当前登录用户一致时，才更新全局状态
          if (form.username && form.username === currentUsername) {
            // 为头像路径添加时间戳参数以避免浏览器缓存问题
            let avatarPathWithCacheBuster = form.avatar
            if (avatarPathWithCacheBuster) {
              // 确保路径包含/api前缀
              if (!avatarPathWithCacheBuster.startsWith('/api')) {
                avatarPathWithCacheBuster = '/api' + avatarPathWithCacheBuster
              }
              // 添加时间戳
              avatarPathWithCacheBuster = avatarPathWithCacheBuster + '?' + new Date().getTime()
            }
            
            // 立即更新全局用户状态，确保头像能实时显示
            const userStore = useUserStore()
            if (userStore && userStore.userInfo) {
              const updatedUserInfo = {
                ...userStore.userInfo,
                avatar: avatarPathWithCacheBuster,
                realName: form.realName, // 同时更新用户名等其他信息
                phone: form.phone,
                email: form.email
              }
              userStore.setUserInfo(updatedUserInfo)
              
              // 更新localStorage
              try {
                localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
                console.log('已在保存用户信息时更新全局状态和localStorage')
              } catch (e) {
                console.error('更新localStorage失败:', e)
              }
            }
          }
          
          ElMessage.success(form.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          handleQuery()
        }
      } catch (error) {
        console.error(form.id ? '更新用户失败:' : '添加用户失败:', error)
        ElMessage.error(form.id ? '更新用户失败' : '添加用户失败')
      }
    }
  })
}

// 分页
const handleCurrentChange = (val) => {
  pageNum.value = val
  handleQuery()
}

// 头像上传成功处理
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    // 保存头像路径并添加/api前缀以通过代理访问后端静态资源
    form.avatar = '/api' + response.data
    
    // 如果正在编辑的是当前登录用户的头像，同步更新全局用户状态和localStorage
    // 这样小头像也能实时更新
    const currentUsername = localStorage.getItem('username') || ''
    
    // 只有当编辑的用户与当前登录用户一致时，才更新全局状态
    if (form.username && form.username === currentUsername) {
      // 添加时间戳参数以避免浏览器缓存问题
      const avatarPathWithCacheBuster = form.avatar + '?' + new Date().getTime()
      
      // 立即更新全局用户状态，确保头像能实时显示
      const userStore = useUserStore()
      if (userStore && userStore.userInfo) {
        const updatedUserInfo = {
          ...userStore.userInfo,
          avatar: avatarPathWithCacheBuster
        }
        userStore.setUserInfo(updatedUserInfo)
        
        // 更新localStorage
        try {
          localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
          console.log('已在头像上传成功时更新全局状态和localStorage')
        } catch (e) {
          console.error('更新localStorage失败:', e)
        }
      }
    }
    
    ElMessage.success('头像上传成功')
  } else {
    console.error('头像上传失败:', response)
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('只支持 JPG、JPEG 和 PNG 格式！')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
  }
  return isJPG && isLt2M
}

// 关闭弹窗
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 初始化
onMounted(() => {
  fetchRoles()
  handleQuery()
})
</script>

<style>
/* 头像上传样式 */
.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-upload {
  margin-top: 10px;
}

/* 开关组件样式 - 全局生效确保最高优先级 */
.el-switch .el-switch__core {
  /* background-color: #409EFF !important; */
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
  outline: none !important;
}

.el-switch .el-switch__core .el-switch__action {
  background-color: white !important;
  border-color: white !important;
}

.el-switch .el-switch__core.is-checked {
  background-color: #48494c !important;
  border: 2px solid #ffffff !important;
  box-shadow: none !important;
  outline: none !important;
}

.el-switch .el-switch__core.is-checked .el-switch__action {
  background-color: white !important;
  border-color: white !important;
}

/* 直接针对span元素的全局样式 */
:global(.el-switch__core) {
  background-color: #48494c !important;
  border: 2px solid #ffffff !important;
  box-shadow: none !important;
  outline: none !important;
}

/* 直接针对span元素的全局样式，包含状态 */
:global(.el-switch__core.is-checked) {
  background-color: #48494c !important;
  border: 2px solid #ffffff !important;
  box-shadow: none !important;
  outline: none !important;
}

.el-switch__core .el-switch__action {
  background-color: white !important;
  border-color: white !important;
}
</style>

<style scoped>
.filter-container {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  color: white;
}

/* 表格和分页合并容器 */
.table-pagination-container {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 表格样式 - 修改为使用渐变背景 */
:deep(.el-table) {
  background: transparent !important;
  margin-bottom: 0;
  color: white !important;
}

/* 表格内部容器 - 应用渐变背景 */
:deep(.el-table__inner-wrapper),
:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 表格行和单元格 */
:deep(.el-table__row),
:deep(.el-table__cell) {
  background: transparent !important;
  color: white !important;
  border-color: white !important;
}

/* 表头样式 - 直接应用渐变背景 */
:deep(.el-table__header),
:deep(.el-table__header-wrapper),
:deep(.el-table__header th.el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  color: white !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

/* 表头和表格主体之间的边框 - 加粗第一条横线 */
:deep(.el-table__header-wrapper) {
  border-bottom: 2px solid white !important;
}

/* 表格最后一条横线 - 设置为白色 */
:deep(.el-table__body-wrapper) {
  border-bottom: 1px solid white !important;
}

/* 确保表格最后一行的底部边框也是白色 */
:deep(.el-table__row:last-child) {
  border-bottom-color: white !important;
}

/* 表头单元格内容 */
:deep(.el-table__header th.el-table__cell .cell) {
  color: white !important;
}

/* 单元格内容 */
:deep(.el-table__cell .cell) {
  color: white !important;
}

/* 滚动条容器 */
:deep(.el-scrollbar__wrap),
:deep(.el-scrollbar__view) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

/* 分页包装器样式 */
.pagination-wrapper {
  padding: 16px;
  border-top: 1px solid #ebeef5;
}

.filter-item {
  margin-right: 10px;
}

/* 输入框背景颜色设置 - 添加白色边框 */
:deep(.el-input__wrapper) {
  background-color: #48494c !important;
  box-shadow: none !important;
  border: 1px solid #ffffff !important; /* 添加白色边框 */
}

:deep(.el-input__inner) {
  background-color: #48494c !important;
  color: white !important;
  border: none !important;
}

/* 使用深度选择器强制覆盖对话框样式 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  color: white !important;
  --el-dialog-background-color: transparent !important;
  --el-bg-color: transparent !important;
}

/* 深度选择器覆盖标题区域 - 移除底部边框 */
:deep(.el-dialog__header) {
  background: transparent !important;
  border-bottom: none !important;
  box-shadow: none !important;
}

:deep(.el-dialog__title) {
  color: white !important;
}

/* 深度选择器覆盖内容区域 */
:deep(.el-dialog__body) {
  background: transparent !important;
  color: white !important;
}

/* 深度选择器覆盖底部区域 */
:deep(.el-dialog__footer) {
  background: transparent !important;
  border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
}

/* 使用深度选择器将表单标签文字颜色设置为白色 */
:deep(.el-form-item__label) {
  color: white !important;
}

/* 确保在对话框中的表单标签也为白色 */
:deep(.el-dialog .el-form-item__label) {
  color: white !important;
}

/* 弹窗内的输入框也应用相同样式 - 使用白色边框 */
.el-dialog :deep(.el-input__wrapper) {
  background-color: #48494c !important;
  color: white !important;
  border: 1px solid #ffffff !important; /* 使用白色边框 */
}

.el-dialog :deep(.el-input__inner) {
  background-color: #48494c !important;
  color: white !important;
  border: none !important;
}

/* 下拉选择器包装器样式 - 去掉边框 */
:deep(.el-select__wrapper) {
  background-color: #48494c !important;
  color: white !important;
  border: none !important;
  box-shadow: none !important;
}

/* 下拉选择器内容区域背景颜色 */
:deep(.el-select__selection),
:deep(.el-select__placeholder) {
  background-color: #48494c !important;
  color: white !important;
}

/* 下拉选择器placeholder文字颜色 - 与输入框保持一致 */
:deep(.el-select__placeholder) {
  color: white !important;
  opacity: 1 !important;
}

/* 下拉选择器中的span文字颜色 */
:deep(.el-select__selection span) {
  color: white !important;
}

/* 下拉菜单背景颜色 */
:deep(.el-select-dropdown) {
  background-color: #48494c !important;
  border: none !important;
  box-shadow: none !important;
}

/* 下拉菜单项背景颜色 */
:deep(.el-select-dropdown__item) {
  background-color: #48494c !important;
  color: white !important;
}

/* 下拉菜单项悬停状态 */
:deep(.el-select-dropdown__item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 下拉菜单包装器背景色 */
:deep(.el-select-dropdown__wrap) {
  background-color: #48494c !important;
}

/* 下拉菜单列表背景色 */
:deep(.el-select-dropdown__list) {
  background-color: #48494c !important;
}

/* popper容器背景色 */
:deep(.el-popper) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

/* popper内部内容背景色 */
:deep(.el-popper__inner) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

/* 下拉菜单箭头样式 - 彻底覆盖Element Plus默认箭头 */
:deep(.el-popper__arrow),
:deep(.el-popper__arrow::before),
:deep(.el-popper__arrow::after) {
  background-color: #48494c !important;
  border-color: #48494c !important;
  background-image: none !important;
  border-top-color: #48494c !important;
  border-right-color: #48494c !important;
  border-bottom-color: #48494c !important;
  border-left-color: #48494c !important;
}

/* 确保下拉菜单在对话框中也正确显示 */
.el-dialog :deep(.el-select-dropdown) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

/* 确保下拉菜单在对话框中的popper也正确显示 */
.el-dialog :deep(.el-popper) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

/* 对话框中下拉菜单箭头样式 */
.el-dialog :deep(.el-popper__arrow),
.el-dialog :deep(.el-popper__arrow::before),
.el-dialog :deep(.el-popper__arrow::after) {
  background-color: #48494c !important;
  border-color: #48494c !important;
  background-image: none !important;
  border-top-color: #48494c !important;
  border-right-color: #48494c !important;
  border-bottom-color: #48494c !important;
  border-left-color: #48494c !important;
}

/* 全局下拉菜单箭头样式 - 使用更高优先级确保所有情况都覆盖 */
:global(.el-popper__arrow),
:global(.el-popper__arrow::before),
:global(.el-popper__arrow::after) {
  background-color: #48494c !important;
  border-color: #48494c !important;
  background-image: none !important;
  border-top-color: #48494c !important;
  border-right-color: #48494c !important;
  border-bottom-color: #48494c !important;
  border-left-color: #48494c !important;
}

/* 移除冲突的开关样式 */

/* 下拉菜单选项文字颜色和背景色 - 设置为白色文字和#48494c背景 */
:deep(.el-select-dropdown__item) {
  color: #ffffff !important;
  background-color: #48494c !important;
  border: none !important;
}

/* 下拉菜单选中项文字颜色和背景色 */
:deep(.el-select-dropdown__item.selected) {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: none !important;
}

/* 下拉菜单悬停项文字颜色和背景色 */
:deep(.el-select-dropdown__item:hover) {
  color: #ffffff !important;
  background-color: #5a5b5f !important;
  border: none !important;
}

/* 对话框中下拉菜单文字颜色和背景色 */
.el-dialog :deep(.el-select-dropdown__item) {
  color: #ffffff !important;
  background-color: #48494c !important;
  border: none !important;
}
.el-dialog :deep(.el-select-dropdown__item.selected) {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: none !important;
}
.el-dialog :deep(.el-select-dropdown__item:hover) {
  color: #ffffff !important;
  background-color: #5a5b5f !important;
  border: none !important;
}

/* 全局下拉菜单文字颜色和背景色 - 确保所有情况都覆盖 */
:global(.el-select-dropdown__item) {
  color: #ffffff !important;
  background-color: #48494c !important;
  border: none !important;
}
:global(.el-select-dropdown__item.selected) {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
  border: none !important;
}
:global(.el-select-dropdown__item:hover) {
  color: #ffffff !important;
  background-color: #5a5b5f !important;
  border: none !important;
}

/* 更具体的下拉菜单选择器 */
:deep(.el-select-dropdown.is-multiple) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

/* 确保下拉菜单在body中也能正确显示 */
:global(.el-select-dropdown) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

:global(.el-select-dropdown__wrap) {
  background-color: #48494c !important;
  border: none !important;
}

:global(.el-select-dropdown__list) {
  background-color: #48494c !important;
  border: none !important;
}

/* 确保全局popper样式 */
:global(.el-popper) {
  background-color: #48494c !important;
  border: none !important;
  background-image: none !important;
}

:global(.el-popper__inner) {
  background-color: #48494c !important;
  border: '1px solid #ffffff' !important;
  background-image: none !important;
}

/* 分页组件样式 - 按钮和页码背景颜色 */
:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next),
:deep(.el-pagination .number),
:deep(.el-pagination .is-active.number) {
  background-color: #48494c !important;
  color: white !important;
  border: 1px solid #ffffff !important;
}

/* 禁用状态的按钮也应用相同背景 */
:deep(.el-pagination .btn-prev:disabled),
:deep(.el-pagination .btn-next:disabled) {
  background-color: #48494c !important;
  color: rgba(255, 255, 255, 0.6) !important;
  border: 1px solid #ffffff !important;
}

/* 鼠标悬停效果 - 只保留前后页按钮的悬停效果，移除数字按钮的悬停效果 */
:deep(.el-pagination .btn-prev:hover:not(:disabled)),
:deep(.el-pagination .btn-next:hover:not(:disabled)) {
  background-color: #3a3b40 !important;
  color: white !important;
  border-color: #3a3b40 !important;
}

/* 分页组件中的Go to文本设置为白色 */
:deep(.el-pagination__goto) {
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

/* 为选择角色的下拉框添加白色边框 */
.el-dialog .el-select :deep(.el-select__wrapper) {
  border: 1px solid #ffffff !important;
}

/* 分页组件总数显示文本样式 - 设置为白色 */
.pagination-wrapper :deep(.el-pagination__total) {
  color: white !important;
}
</style>