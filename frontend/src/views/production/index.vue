<template>
  <div class="production-container">
    <div class="search-area">
      <el-input v-model="searchParams.keyword" placeholder="请输入计划名称或产物名称" class="search-input" />
      <el-select v-model="searchParams.status" placeholder="计划状态" class="search-select">
        <el-option label="计划中" value="1" />
        <el-option label="生产中" value="2" />
        <el-option label="完成" value="3" />
        <el-option label="取消" value="4" />
      </el-select>
      <el-button type="primary" @click="search" class="rounded-button">查询</el-button>
      <el-button type="primary" @click="reset" class="rounded-button">重置</el-button>
      <el-button type="primary" @click="openAddDialog" class="rounded-button">新增生产计划</el-button>
      <el-button type="success" @click="handleExport" class="rounded-button export-button">
        <el-icon><Download /></el-icon>导出
      </el-button>
    </div>
    
    <el-card class="box-card">
      <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="productionPlans" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="计划ID" width="100" />
          <el-table-column prop="planName" label="计划名称" width="180" show-overflow-tooltip />
          <el-table-column prop="productName" label="产物名称" width="180" show-overflow-tooltip />
          <el-table-column prop="productQuantity" label="生产数量" width="120" />
          <el-table-column prop="productProperty" label="产物属性" width="120">
            <template #default="scope">
              <span :class="{
                'property-type': true,
                'property-semi': scope.row.productProperty === 2,
                'property-finished': scope.row.productProperty === 3
              }">
                {{ getProductPropertyText(scope.row.productProperty) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="requiredMaterials" label="所需材料" width="300" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ formatRequiredMaterials(scope.row.requiredMaterials) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productionTime" label="制作时间(小时)" width="150" />
          <el-table-column label="开始时间" width="180">
          <template #default="scope">
            <span>{{ formatTime(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
          <el-table-column prop="expectedEndTime" label="预计完成时间" width="180" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="status" label="计划状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="倒计时" width="150" align="center">
            <template #default="scope">
              <span v-if="(scope.row.status === 2 || scope.row.status === '2') && scope.row.expectedEndTime">
                {{ countdownMap[scope.row.id] || '计算中...' }}
              </span>
              <span v-else-if="scope.row.status === 3 || scope.row.status === '3'">
                已完成
              </span>
              <span v-else>
                - 
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="editPlan(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deletePlan(scope.row)">删除</el-button>
              <el-button 
                size="small" 
                :type="getNextStatusType(scope.row.status)" 
                @click="updateStatus(scope.row)"
              >
                {{ getNextStatusText(scope.row.status) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="产物名称" prop="productName">
          <el-select 
            v-model="formData.productName" 
            placeholder="请选择产物名称"
            filterable
            @change="handleProductChange"
          >
            <el-option 
              v-for="material in materials" 
              :key="material.id" 
              :label="material.materialName" 
              :value="material.materialName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产物属性" prop="productProperty">
          <el-select v-model="formData.productProperty" placeholder="请选择产物属性">
            <el-option label="半成品" :value="2" />
            <el-option label="成品" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="生产数量" prop="quantity">
          <el-input-number 
            v-model="formData.quantity" 
            :min="1" 
            :step="1" 
            :precision="0" 
            placeholder="请输入生产数量"
            @change="handleQuantityChange"
          />
        </el-form-item>
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="formData.planName" placeholder="自动生成" readonly />
        </el-form-item>
        <el-form-item label="所需材料" prop="requiredMaterials">
          <el-input 
            :value="formatRequiredMaterials(formData.requiredMaterials)" 
            placeholder="自动生成" 
            type="textarea"
            rows="3"
            readonly
          />
        </el-form-item>
        <el-form-item label="制作时间(小时)" prop="productionTime">
          <el-input-number 
            v-model="formData.productionTime" 
            :min="0.1" 
            :step="0.1" 
            :precision="2" 
            placeholder="自动生成"
            readonly
          />
        </el-form-item>
        <el-form-item label="开始生产时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="请选择开始生产时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="calculateExpectedEndTime"
          />
        </el-form-item>
        <el-form-item label="预计完成时间" prop="expectedEndTime">
          <el-date-picker
            v-model="formData.expectedEndTime"
            type="datetime"
            placeholder="自动计算"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            readonly
          />
        </el-form-item>
        <el-form-item label="计划状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择计划状态">
            <el-option label="计划中" :value="1" />
            <el-option label="生产中" :value="2" />
            <el-option label="完成" :value="3" />
            <el-option label="取消" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getDictItemByCode } from '@/api/dict'
import { exportToExcel } from '@/utils/export'
import {
  getProductionPlans,
  createProductionPlan,
  updateProductionPlan,
  deleteProductionPlan,
  updateProductionPlanStatus
} from '@/api/production'
import { getMaterials } from '@/api/material'

// 表格数据
const productionPlans = ref([])
const loading = ref(false)

// 搜索参数
const searchParams = reactive({
  keyword: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 状态字典选项
const statusOptions = ref([])
const statusTypeMap = ref({})

// 格式化时间，解决时区问题
const formatTime = (timeString) => {
  if (!timeString) return ''
  // 直接按原格式返回，避免JavaScript Date对象的时区转换
  return timeString
}

// 倒计时相关
const countdownTimer = ref(null)
const countdownUpdate = ref(0) // 用于触发倒计时更新的响应式变量
const countdownMap = ref({}) // 存储每个生产计划的倒计时结果

// 选择的数据
const selectedPlans = ref([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: '',
  planName: '',
  productName: '',
  productProperty: 3, // 默认值设为成品（3）
  quantity: 1,
  requiredMaterials: '',
  productionTime: 0,
  startTime: '',
  expectedEndTime: '',
  status: 1
})

// 物料列表
const materials = ref([])
const selectedMaterial = ref(null)

// 物料映射（用于将ID转换为名称）
const materialMap = ref({})

// 表单验证规则
const formRules = reactive({
  productName: [
    { required: true, message: '请选择产物名称', trigger: 'change' }
  ],
  productProperty: [
    { required: true, message: '请选择产物属性', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入生产数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '生产数量必须大于0', trigger: 'blur' }
  ],
  planName: [
    { required: true, message: '计划名称自动生成', trigger: 'blur' }
  ],
  requiredMaterials: [
    { required: true, message: '所需材料自动生成', trigger: 'blur' }
  ],
  productionTime: [
    { required: true, message: '制作时间自动生成', trigger: 'blur' },
    { type: 'number', min: 0.1, message: '制作时间必须大于0', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始生产时间', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择计划状态', trigger: 'change' }
  ]
})

// 对话框标题
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑生产计划' : '新增生产计划'
})

// 格式化所需材料，将物料ID转换为物料名称
const formatRequiredMaterials = (requiredMaterials) => {
  if (!requiredMaterials) return ''
  
  try {
    // 分割所需材料字符串，如 "1*2/2*1" => ["1*2", "2*1"]
    const materials = requiredMaterials.split('/')
    
    // 转换每个物料项
    const formattedMaterials = materials.map(item => {
      // 分割物料ID和数量，如 "1*2" => ["1", "2"]
      const [materialId, quantity] = item.split('*')
      
      // 获取物料名称，如果找不到则显示原始ID
      const materialName = materialMap.value[materialId] || `物料${materialId}`
      
      // 返回格式化后的字符串，如 "物品名称1*2"
      return `${materialName}*${quantity}`
    })
    
    // 重新组合为斜杠分隔的字符串
    return formattedMaterials.join('/')
  } catch (error) {
    console.error('格式化所需材料失败:', error)
    return requiredMaterials // 出错时返回原始字符串
  }
}

// 获取状态字典
const loadStatusDict = async () => {
  try {
    const res = await getDictItemByCode('production_status')
    if (res.code === 200) {
      statusOptions.value = res.data
      // 构建状态类型映射
      statusTypeMap.value = {
        '1': 'info',
        '2': 'warning',
        '3': 'success',
        '4': 'danger'
      }
    }
  } catch (error) {
    console.error('获取状态字典失败:', error)
  }
}

// 获取状态文本
const getStatusText = (status) => {
  // 确保状态是字符串类型
  const statusStr = String(status)
  const dictItem = statusOptions.value.find(item => item.value === statusStr)
  return dictItem ? dictItem.label : ''
}

// 获取状态类型
const getStatusType = (status) => {
  // 确保状态是字符串类型
  const statusStr = String(status)
  return statusTypeMap.value[statusStr] || 'info'
}

// 获取产物属性文本
const getProductPropertyText = (property) => {
  // 确保属性是数字类型
  const propertyNum = Number(property)
  const propertyMap = {
    '2': '半成品',
    '3': '成品'
  }
  return propertyMap[propertyNum] || propertyNum
}

// 获取下一个状态文本
const getNextStatusText = (status) => {
  // 确保状态是字符串类型
  const statusStr = String(status)
  const nextStatusMap = {
    '1': '开始生产',
    '2': '完成生产',
    '3': '重新计划',
    '4': '重新计划'
  }
  return nextStatusMap[statusStr] || ''
}

// 获取下一个状态类型
const getNextStatusType = (status) => {
  // 确保状态是字符串类型
  const statusStr = String(status)
  const nextTypeMap = {
    '1': 'warning',
    '2': 'success',
    '3': 'primary',
    '4': 'primary'
  }
  return nextTypeMap[statusStr] || 'primary'
}

// 倒计时函数：计算剩余时间
const getCountdown = (endTime, updateTrigger) => {
  if (!endTime) return '-'  
   
  console.log('getCountdown called with:', { endTime, updateTrigger })
  
  // 确保updateTrigger被使用，触发响应式更新
  // 通过将updateTrigger添加到now的计算中，确保Vue检测到变化
  const now = new Date().getTime() + updateTrigger
  let end = 0
  
  // 尝试解析endTime，处理不同格式
  try {
    // 首先尝试直接解析
    end = new Date(endTime).getTime()
    
    // 如果解析失败，尝试处理可能的格式问题
    if (isNaN(end)) {
      // 尝试将yyyy-MM-dd HH:mm:ss格式转换为ISO格式
      const isoString = endTime.replace(/ /g, 'T') + 'Z'
      end = new Date(isoString).getTime()
    }
    
    // 如果仍然解析失败，尝试手动解析yyyy-MM-dd HH:mm:ss格式
    if (isNaN(end)) {
      const parts = endTime.match(/(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/)
      if (parts) {
        const [, year, month, day, hour, minute, second] = parts
        end = new Date(year, month - 1, day, hour, minute, second).getTime()
      }
    }
    
    // 如果仍然解析失败，返回错误信息
    if (isNaN(end)) {
      console.error('无法解析结束时间:', endTime)
      return '时间格式错误'
    }
  } catch (error) {
    console.error('解析结束时间时出错:', error)
    return '时间格式错误'
  }
  
  const remaining = end - (now - updateTrigger) // 减去updateTrigger，恢复真实时间
  console.log('倒计时计算:', { now: now - updateTrigger, end, remaining })
  
  if (isNaN(remaining)) {
    return '时间格式错误'
  }
  
  if (remaining <= 0) {
    console.log('已超时，剩余时间:', remaining)
    return '已超时'
  }
  
  // 计算剩余时间
  const days = Math.floor(remaining / (1000 * 60 * 60 * 24))
  const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((remaining % (1000 * 60)) / 1000)
  
  const result = days > 0 
    ? `${days}天${hours}时${minutes}分${seconds}秒`
    : hours > 0
      ? `${hours}时${minutes}分${seconds}秒`
      : minutes > 0
        ? `${minutes}分${seconds}秒`
        : `${seconds}秒`
  
  console.log('倒计时结果:', result)
  return result
}

// 更新所有生产中计划的倒计时
const updateAllCountdowns = () => {
  const now = new Date().getTime()
  
  productionPlans.value.forEach(plan => {
    if ((plan.status === 2 || plan.status === '2') && plan.expectedEndTime) {
      try {
        // 尝试解析结束时间
        let end = new Date(plan.expectedEndTime).getTime()
        
        // 如果解析失败，尝试处理可能的格式问题
        if (isNaN(end)) {
          const isoString = plan.expectedEndTime.replace(/ /g, 'T') + 'Z'
          end = new Date(isoString).getTime()
        }
        
        // 如果仍然解析失败，尝试手动解析yyyy-MM-dd HH:mm:ss格式
        if (isNaN(end)) {
          const parts = plan.expectedEndTime.match(/(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/)
          if (parts) {
            const [, year, month, day, hour, minute, second] = parts
            end = new Date(year, month - 1, day, hour, minute, second).getTime()
          }
        }
        
        if (!isNaN(end)) {
          const remaining = end - now
          
          if (remaining <= 0) {
            countdownMap.value[plan.id] = '已超时'
          } else {
            // 计算剩余时间
            const days = Math.floor(remaining / (1000 * 60 * 60 * 24))
            const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
            const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))
            const seconds = Math.floor((remaining % (1000 * 60)) / 1000)
            
            countdownMap.value[plan.id] = days > 0 
              ? `${days}天${hours}时${minutes}分${seconds}秒`
              : hours > 0
                ? `${hours}时${minutes}分${seconds}秒`
                : minutes > 0
                  ? `${minutes}分${seconds}秒`
                  : `${seconds}秒`
          }
        } else {
          countdownMap.value[plan.id] = '时间格式错误'
        }
      } catch (error) {
        console.error(`更新生产计划 ${plan.id} 倒计时时出错:`, error)
        countdownMap.value[plan.id] = '时间格式错误'
      }
    }
  })
}

// 启动倒计时定时器
const startCountdownTimer = () => {
  // 清除现有的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 初始化倒计时
  updateAllCountdowns()
  // 初始检查一次是否有需要自动开始生产的计划
  checkAutoStartProduction()
  
  // 创建新的定时器，每秒更新一次
  countdownTimer.value = setInterval(() => {
    // 更新所有倒计时
    updateAllCountdowns()
    
    // 检查是否有计划中的任务需要自动开始生产
    checkAutoStartProduction()
    
    // 检查是否有生产中的计划已超时
    checkProductionTimeout()
  }, 1000)
}

// 检查是否有计划中的任务需要自动开始生产
const checkAutoStartProduction = () => {
  const now = new Date().getTime()
  
  productionPlans.value.forEach(plan => {
    // 同时处理数字和字符串类型的状态
    if ((plan.status === 1 || plan.status === '1') && plan.startTime) {
      try {
        // 尝试解析开始时间
        let startTime = new Date(plan.startTime).getTime()
        
        // 如果解析失败，尝试处理可能的格式问题
        if (isNaN(startTime)) {
          const isoString = plan.startTime.replace(/ /g, 'T') + 'Z'
          startTime = new Date(isoString).getTime()
        }
        
        // 如果仍然解析失败，尝试手动解析yyyy-MM-dd HH:mm:ss格式
        if (isNaN(startTime)) {
          const parts = plan.startTime.match(/(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/)
          if (parts) {
            const [, year, month, day, hour, minute, second] = parts
            startTime = new Date(year, month - 1, day, hour, minute, second).getTime()
          }
        }
        
        // 只有当startTime有效且当前时间已超过或等于开始时间时，才自动开始生产
        if (!isNaN(startTime) && now >= startTime) {
          console.log(`生产计划 ${plan.planName} 到达开始时间，当前时间: ${now}，开始时间: ${startTime}`)
          
          // 计算预计完成时间
          const productionTime = Number(plan.productionTime) || 0
          const productionTimeMs = productionTime * 60 * 60 * 1000
          const endTime = new Date(now + productionTimeMs)
          
          // 格式化预计完成时间
          const expectedEndTime = `${endTime.getFullYear()}-${String(endTime.getMonth() + 1).padStart(2, '0')}-${String(endTime.getDate()).padStart(2, '0')} ${String(endTime.getHours()).padStart(2, '0')}:${String(endTime.getMinutes()).padStart(2, '0')}:${String(endTime.getSeconds()).padStart(2, '0')}`
          
          // 更新生产计划，包括状态、开始时间和预计完成时间
          const updateData = {
            id: plan.id,
            status: 2, // 计划中 -> 生产中
            startTime: plan.startTime,
            expectedEndTime: expectedEndTime
          }
          
          // 自动开始生产
          updateProductionPlan(updateData).then(response => {
            if (response.code === 200) {
              ElMessage.success(`生产计划 ${plan.planName} 已自动开始生产`)
              loadProductionPlans() // 重新加载数据
            }
          }).catch(error => {
            console.error('自动更新生产计划状态失败:', error)
          })
        }
      } catch (error) {
        console.error(`检查生产计划 ${plan.planName} 开始时间时出错:`, error)
      }
    }
  })
}

// 检查生产中的计划是否已超时
const checkProductionTimeout = () => {
  const now = new Date().getTime()
  
  productionPlans.value.forEach(plan => {
    // 同时处理数字和字符串类型的状态
    if ((plan.status === 2 || plan.status === '2') && plan.expectedEndTime) {
      try {
        // 尝试解析结束时间，与getCountdown函数保持一致的解析逻辑
        let endTime = new Date(plan.expectedEndTime).getTime()
        
        // 如果解析失败，尝试处理可能的格式问题
        if (isNaN(endTime)) {
          const isoString = plan.expectedEndTime.replace(/ /g, 'T') + 'Z'
          endTime = new Date(isoString).getTime()
        }
        
        // 只有当endTime有效且确实超时，并且超时时间超过1秒时才更新状态
        if (!isNaN(endTime) && now >= endTime && (now - endTime) > 1000) {
          console.log(`生产计划 ${plan.planName} 已超时，当前时间: ${now}，结束时间: ${endTime}，超时时间: ${now - endTime}`)
          // 自动更新状态为完成
          updateProductionPlanStatus(plan.id, '3').then(response => {
            if (response.code === 200) {
              ElMessage.success(`生产计划 ${plan.planName} 已完成`)
              loadProductionPlans() // 重新加载数据
            }
          }).catch(error => {
            console.error('自动更新生产计划状态失败:', error)
          })
        }
      } catch (error) {
        console.error(`检查生产计划 ${plan.planName} 超时状态时出错:`, error)
      }
    }
  })
}

// 加载生产计划列表
const loadProductionPlans = () => {
  loading.value = true
  const params = {
    ...searchParams,
    page: pagination.currentPage,
    pageSize: pagination.pageSize
  }
  
  // 如果使用的是合并后的keyword，需要将其映射到后端期望的字段
  // 这里假设后端API支持同时搜索planName和productName
  // 如果后端API不支持，可能需要调整为分别传递
  const apiParams = {
    ...params,
    planName: params.keyword,
    productName: params.keyword
  }
  delete apiParams.keyword
  
  getProductionPlans(apiParams).then(response => {
    if (response.code === 200) {
      // 打印原始数据，查看时间格式
      console.log('原始生产计划数据:', response.data.records)
      productionPlans.value = response.data.records || []
      pagination.total = response.data.total || 0
      
      // 启动或重启定时器
      startCountdownTimer()
    } else {
      ElMessage.error('获取生产计划列表失败: ' + response.message)
    }
  }).catch(error => {
    ElMessage.error('获取生产计划列表失败: ' + error.message)
  }).finally(() => {
    loading.value = false
  })
}

// 查询
const search = () => {
  pagination.currentPage = 1
  loadProductionPlans()
}

// 重置
const reset = () => {
  searchParams.keyword = ''
  searchParams.status = ''
  pagination.currentPage = 1
  loadProductionPlans()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadProductionPlans()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadProductionPlans()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedPlans.value = selection
}

// 获取物料列表
const loadMaterials = () => {
  return new Promise((resolve, reject) => {
    getMaterials({ page: 1, pageSize: 100 }).then(response => {
      if (response.code === 200) {
        // 只保留物料类型为2(半成品)和3(成品)的物料
        materials.value = response.data.records.filter(material => 
          material.materialType === 2 || material.materialType === 3
        ) || []
        
        // 建立物料ID到名称的映射
        materialMap.value = {}
        materials.value.forEach(material => {
          materialMap.value[material.id] = material.materialName
        })
        
        // 加载所有物料用于映射（包括原料和半成品）
        loadAllMaterialsForMapping()
        resolve()
      } else {
        const errorMsg = '获取物料列表失败: ' + response.message
        ElMessage.error(errorMsg)
        reject(new Error(errorMsg))
      }
    }).catch(error => {
      const errorMsg = '获取物料列表失败: ' + error.message
      ElMessage.error(errorMsg)
      reject(error)
    })
  })
}

// 加载所有物料用于映射
const loadAllMaterialsForMapping = () => {
  getMaterials({ page: 1, pageSize: 1000 }).then(response => {
    if (response.code === 200) {
      const allMaterials = response.data.records || []
      // 更新物料映射
      allMaterials.forEach(material => {
        materialMap.value[material.id] = material.materialName
      })
    }
  }).catch(error => {
    console.error('加载所有物料失败:', error)
  })
}

// 打开新增对话框
const openAddDialog = async () => {
  isEdit.value = false
  resetForm()
  try {
    // 加载物料列表并等待完成
    await loadMaterials()
    // 打开对话框
    dialogVisible.value = true
  } catch (error) {
    console.error('打开新增对话框失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.id = ''
  formData.productName = ''
  formData.quantity = 1
  formData.productProperty = 3 // 默认设为成品
  formData.planName = ''
  formData.requiredMaterials = ''
  formData.productionTime = 0
  formData.startTime = ''
  formData.expectedEndTime = ''
  formData.status = 1
  selectedMaterial.value = null
}

// 处理产物选择变化
const handleProductChange = (value) => {
  if (!value) {
    selectedMaterial.value = null
    formData.requiredMaterials = ''
    formData.productionTime = 0
    generatePlanName()
    return
  }
  
  // 查找选中的物料
  selectedMaterial.value = materials.value.find(m => m.materialName === value)
  if (selectedMaterial.value) {
    // 计算所需材料（乘以数量）
    calculateRequiredMaterials()
    // 设置制作时间（乘以数量），保留两位小数
    formData.productionTime = parseFloat((selectedMaterial.value.requiredTime * formData.quantity).toFixed(2))
  }
  
  // 重新生成计划名称
  generatePlanName()
  // 重新计算预计完成时间
  calculateExpectedEndTime()
}

// 处理数量变化
const handleQuantityChange = (value) => {
  if (!value || !selectedMaterial.value) return
  
  // 重新计算所需材料
  calculateRequiredMaterials()
  // 重新计算制作时间，保留两位小数
  formData.productionTime = parseFloat((selectedMaterial.value.requiredTime * value).toFixed(2))
  // 重新生成计划名称
  generatePlanName()
  // 重新计算预计完成时间
  calculateExpectedEndTime()
}

// 计算所需材料
const calculateRequiredMaterials = () => {
  if (!selectedMaterial.value || !formData.quantity) return
  
  const baseMaterials = selectedMaterial.value.requiredMaterials
  if (!baseMaterials) {
    formData.requiredMaterials = ''
    return
  }
  
  // 解析基础材料并乘以数量
  const materialsArray = baseMaterials.split('/')
  const calculatedMaterials = materialsArray.map(material => {
    const [materialId, amount] = material.split('*')
    if (!materialId || !amount) return material
    return `${materialId}*${parseFloat(amount) * formData.quantity}`
  })
  
  formData.requiredMaterials = calculatedMaterials.join('/')
}

// 生成计划名称
const generatePlanName = () => {
  if (!formData.productName || !formData.quantity) {
    formData.planName = ''
    return
  }
  
  // 生成格式：产物名称-数量-年月日时分秒
  const now = new Date()
  const dateStr = now.getFullYear() + 
                  String(now.getMonth() + 1).padStart(2, '0') + 
                  String(now.getDate()).padStart(2, '0') + 
                  String(now.getHours()).padStart(2, '0') + 
                  String(now.getMinutes()).padStart(2, '0') + 
                  String(now.getSeconds()).padStart(2, '0')
  
  formData.planName = `${formData.productName}-${formData.quantity}-${dateStr}`
}

// 计算预计完成时间
const calculateExpectedEndTime = () => {
  if (!formData.startTime || !formData.productionTime) {
    formData.expectedEndTime = ''
    return
  }
  
  const startTime = new Date(formData.startTime)
  const endTime = new Date(startTime)
  // 确保productionTime是数字类型
  const productionTime = Number(formData.productionTime)
  
  // 将小时转换为毫秒并直接添加到时间戳上，避免精度丢失
  const productionTimeMs = productionTime * 60 * 60 * 1000
  endTime.setTime(endTime.getTime() + productionTimeMs)
  
  // 格式化为el-date-picker期望的格式：YYYY-MM-DD HH:mm:ss
  const year = endTime.getFullYear()
  const month = String(endTime.getMonth() + 1).padStart(2, '0')
  const day = String(endTime.getDate()).padStart(2, '0')
  const formattedHours = String(endTime.getHours()).padStart(2, '0')
  const formattedMinutes = String(endTime.getMinutes()).padStart(2, '0')
  const seconds = String(endTime.getSeconds()).padStart(2, '0')
  
  formData.expectedEndTime = `${year}-${month}-${day} ${formattedHours}:${formattedMinutes}:${seconds}`
}

// 编辑
const editPlan = async (row) => {
  isEdit.value = true
  // 复制行数据到表单
  Object.assign(formData, row)
  // 直接从后端的productQuantity字段获取生产数量，默认为1
  formData.quantity = row.productQuantity || 1
  // 确保productionTime是数字类型
  formData.productionTime = Number(formData.productionTime) || 0
  
  try {
    // 加载物料列表并等待完成
    await loadMaterials()
    // 物料加载完成后查找对应的物料
    selectedMaterial.value = materials.value.find(m => m.materialName === row.productName)
    // 重新计算所需材料
    calculateRequiredMaterials()
    // 重新计算预计完成时间
    calculateExpectedEndTime()
    // 打开对话框
    dialogVisible.value = true
  } catch (error) {
    console.error('编辑生产计划失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  if (!formRef.value) return
  
  formRef.value.validate((valid) => {
    if (valid) {
        const form = { ...formData }
        
        // 将前端的quantity字段映射到后端的productQuantity字段
        form.productQuantity = form.quantity
        // 移除前端不需要传递给后端的quantity字段
        delete form.quantity
        
        // 使用el-date-picker已经格式化好的日期格式，不需要再转换为ISO格式
      
      if (isEdit.value) {
        // 更新生产计划
        updateProductionPlan(form).then(response => {
          if (response.code === 200) {
            ElMessage.success('更新生产计划成功')
            dialogVisible.value = false
            loadProductionPlans()
          } else {
            ElMessage.error('更新生产计划失败: ' + response.message)
          }
        }).catch(error => {
          ElMessage.error('更新生产计划失败: ' + error.message)
        })
      } else {
        // 新增生产计划
        createProductionPlan(form).then(response => {
          if (response.code === 200) {
            ElMessage.success('新增生产计划成功')
            dialogVisible.value = false
            loadProductionPlans()
          } else {
            ElMessage.error('新增生产计划失败: ' + response.message)
          }
        }).catch(error => {
          ElMessage.error('新增生产计划失败: ' + error.message)
        })
      }
    }
  })
}

// 删除
const deletePlan = (row) => {
  ElMessageBox.confirm('确定要删除这个生产计划吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteProductionPlan(row.id).then(response => {
      if (response.code === 200) {
        ElMessage.success('删除生产计划成功')
        loadProductionPlans()
      } else {
        ElMessage.error('删除生产计划失败: ' + response.message)
      }
    }).catch(error => {
      ElMessage.error('删除生产计划失败: ' + error.message)
    })
  }).catch(() => {
    // 取消删除
  })
}

// 更新状态
const updateStatus = (row) => {
  let nextStatus = 0
  if (row.status === 1 || row.status === '1') {
    nextStatus = 2 // 计划中 -> 生产中
    
    // 当点击开始生产时，始终使用当前时间作为开始时间
    // 这样可以确保倒计时从当前时间开始计算，而不是从过去的时间开始计算
    const now = new Date()
    let startTime
    let endTime
    
    console.log('开始生产，当前时间:', now)
    console.log('生产计划原始数据:', row)
    
    // 强制使用当前时间作为开始时间
    startTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
    endTime = new Date(now)
    console.log('强制使用当前时间作为开始时间:', startTime)
    
    // 计算预计完成时间
    const productionTime = Number(row.productionTime) || 0
    console.log('生产时间(小时):', productionTime)
    
    // 将小时转换为毫秒并直接添加到时间戳上，避免精度丢失
    const productionTimeMs = productionTime * 60 * 60 * 1000
    console.log('生产时间(毫秒):', productionTimeMs)
    
    endTime.setTime(endTime.getTime() + productionTimeMs)
    console.log('预计完成时间(Date对象):', endTime)
    
    // 直接格式化日期为yyyy-MM-dd HH:mm:ss格式，使用当地时间
    const expectedEndTime = `${endTime.getFullYear()}-${String(endTime.getMonth() + 1).padStart(2, '0')}-${String(endTime.getDate()).padStart(2, '0')} ${String(endTime.getHours()).padStart(2, '0')}:${String(endTime.getMinutes()).padStart(2, '0')}:${String(endTime.getSeconds()).padStart(2, '0')}`
    console.log('预计完成时间(格式化):', expectedEndTime)
    
    // 更新生产计划，包括状态、开始时间和预计完成时间
    const updateData = {
      id: row.id,
      status: nextStatus,
      startTime: startTime,
      expectedEndTime: expectedEndTime
    }
    
    console.log('更新生产计划数据:', updateData)
    
    updateProductionPlan(updateData).then(response => {
      if (response.code === 200) {
        ElMessage.success('开始生产成功')
        loadProductionPlans()
      } else {
        ElMessage.error('开始生产失败: ' + response.message)
      }
    }).catch(error => {
      ElMessage.error('开始生产失败: ' + error.message)
    })
  } else if (row.status === 2 || row.status === '2') {
    nextStatus = 3 // 生产中 -> 完成
    
    // 只更新状态
    updateProductionPlanStatus(row.id, nextStatus).then(response => {
      if (response.code === 200) {
        ElMessage.success('完成生产成功')
        loadProductionPlans()
      } else {
        ElMessage.error('完成生产失败: ' + response.message)
      }
    }).catch(error => {
      ElMessage.error('完成生产失败: ' + error.message)
    })
  } else {
    nextStatus = 1 // 完成/取消 -> 计划中
    
    // 只更新状态
    updateProductionPlanStatus(row.id, nextStatus).then(response => {
      if (response.code === 200) {
        ElMessage.success('重新计划成功')
        loadProductionPlans()
      } else {
        ElMessage.error('重新计划失败: ' + response.message)
      }
    }).catch(error => {
      ElMessage.error('重新计划失败: ' + error.message)
    })
  }
}

// 初始化
onMounted(() => {
  loadStatusDict()
  loadProductionPlans()
  loadAllMaterialsForMapping()
})

// 组件销毁时清除定时器
onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
})

// 导出生产计划数据
const handleExport = () => {
  if (productionPlans.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  let loadingInstance
  try {
    loadingInstance = ElLoading.service({ message: '正在导出数据，请稍候...', lock: true })
    
    // 准备导出数据
    const exportData = productionPlans.value.map(plan => ({
      计划ID: plan.id,
      计划名称: plan.planName,
      产物名称: plan.productName,
      产物属性: getProductPropertyText(plan.productProperty),
      生产数量: plan.productQuantity,
      所需材料: formatRequiredMaterials(plan.requiredMaterials),
      制作时间: plan.productionTime,
      开始时间: plan.startTime,
      预计完成时间: plan.expectedEndTime,
      计划状态: getStatusText(plan.status)
    }))
    
    // 调用导出函数
    exportToExcel(exportData, '生产计划数据')
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    if (loadingInstance) {
      loadingInstance.close()
    }
  }
}
</script>

<style lang="scss" scoped>
.production-container {
  padding: 20px;
  
  .search-area {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 10px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    padding: 15px;
    border-radius: 8px;
    
    .search-input {
      width: 200px;
      
      // 修改搜索框背景颜色
      :deep(.el-input__wrapper) {
        background-color: #48494c !important;
        
        // 修改输入框内部背景颜色
        .el-input__inner {
          background-color: #48494c !important;
          color: #ffffff !important;
        }
        
        // 修改获得焦点时的边框颜色
        &:focus-within {
          box-shadow: 0 0 0 1px #ffffff inset !important;
          border-color: #ffffff !important;
        }
      }
    }
    
    .search-select {
      width: 180px;
      
      // 修改下拉选择框背景颜色
      :deep(.el-select__wrapper) {
        background-color: #48494c !important;
        
        // 修改选择框内部背景颜色
        .el-select__selection {
          background-color: #48494c !important;
        }
        
        // 修改选择框文字颜色
        .el-select__placeholder {
          color: #ffffff !important;
        }
        
        // 修改选择框输入文字颜色
        .el-select__input {
          color: #ffffff !important;
        }
        
        // 修改获得焦点时的边框颜色
        &:focus-within {
          box-shadow: 0 0 0 1px #ffffff inset !important;
          border-color: #ffffff !important;
        }
      }
      
      // 修改下拉选项的悬停背景颜色
      :deep(.el-select-dropdown__item) {
        &:hover {
          background-color: #5A5B5E !important;
        }
      }
    }
    
    /* 圆角按钮样式 */
    .rounded-button {
      background-color: #ffffff !important;
      color: #274151 !important;
      border-color: #ffffff !important;
      transition: all 0.3s ease !important;
      margin-left: 10px;
    }
    
    /* 为按钮添加鼠标悬停效果 */
    .rounded-button:hover:not(.is-disabled) {
      background-color: #5a5b5f !important;
      color: #274151 !important;
      border-color: #ffffff !important;
    }
    
    /* 确保禁用状态下也有白色边框和白色背景 */
    .rounded-button.is-disabled {
      border: 1px solid #ffffff !important;
      background-color: #ffffff !important;
    }
    
    /* 极高优先级的规则 - 确保主按钮文字颜色 */
    :global(.rounded-button.el-button--primary) {
      --el-button-text-color: #274151 !important;
    }
    
    :global(.rounded-button.el-button--primary span) {
      color: #274151 !important;
    }
    
    /* 导出按钮文字和图标颜色设置 */
    :global(.rounded-button.export-button.el-button--success) {
      --el-button-text-color: #67C23A !important;
    }
    
    :global(.rounded-button.export-button.el-button--success span) {
      color: #67C23A !important;
    }
    
    :global(.rounded-button.export-button.el-button--success .el-icon) {
      color: #67C23A !important;
    }
  }
  
  // 全局覆盖计划状态下拉选择器的悬停样式
  :global(.el-select-dropdown__item:hover) {
    background-color: #5A5B5E !important;
  }

  .box-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    border: none !important;
    
    .table-container {
      margin-bottom: 20px;
    }
    
    .pagination {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
      
      /* 修改分页组件的文字颜色为白色 */
      :deep(.el-pagination__total) {
        color: white !important;
      }
      
      /* 完全重置分页组件中select的所有样式 */
      :deep(.el-pagination__sizes) {
        /* 重置select容器 */
        .el-select {
          /* 重置select wrapper */
          .el-select__wrapper {
            background-color: #48494c !important;
            // border: 1px solid white !important;
            outline: none !important;
            
            /* 覆盖Element Plus的CSS变量 */
            --el-select-border-color: white !important;
            --el-select-border-color-hover: white !important;
            --el-select-input-focus-border-color: white !important;
            --el-border-color-hover: white !important;
            --el-border-color-focus: white !important;
            --el-border-color: white !important;
            
            /* 重置内部元素 */
            .el-select__placeholder {
              color: white !important;
            }
            
            .el-select__input {
              color: white !important;
            }
            
            /* 重置下拉箭头 */
            .el-select__caret {
              color: white !important;
            }
          }
          
          /* 确保所有状态下的边框都是白色 */
          &:focus,
          &.is-focus,
          &.is-active {
            .el-select__wrapper {
              // border: 1px solid white !important;
              box-shadow: 0 0 0 1px white !important;
              outline: none !important;
            }
          }
        }
        
        /* 直接重置select wrapper的所有状态 */
        .el-select__wrapper,
        .el-select__wrapper:focus,
        .el-select__wrapper.is-focus,
        .el-select__wrapper.is-active,
        .el-select__wrapper:hover {
          // border: 1px solid white !important;
          box-shadow: 0 0 0 1px white !important;
          outline: none !important;
        }
      }
      
      /* 修改分页数字按钮的颜色 */
      :deep(.el-pager li) {
        color: white !important;
      }
      
      /* 修改分页按钮的颜色 */
      :deep(.el-pagination button) {
        color: white !important;
      }
      
      /* 修改上一页按钮的背景颜色、间距和边框 */
      :deep(.btn-prev) {
        background-color: #48494c !important;
        margin: 0 5px;
        border: 1px solid white !important;
      }
      
      /* 修改当前页码项的背景颜色、间距和边框 */
      :deep(.el-pager li.is-active),
      :deep(.el-pager li.active),
      :deep(.el-pager li.is-active.number) {
        background-color: #48494c !important;
        margin: 0 5px;
        border: 1px solid white !important;
      }
      
      /* 修改下一页按钮的背景颜色、间距和边框 */
      :deep(.btn-next) {
        background-color: #48494c !important;
        margin: 0 5px;
        border: 1px solid white !important;
      }
      
      /* 修改"Go to"文字的颜色为白色 */
      :deep(.el-pagination__goto) {
        color: white !important;
      }
      
      /* 修改页码输入框容器的背景颜色为#48494c */
      :deep(.el-pagination .el-input__wrapper) {
        background-color: #48494c !important;
        // border: 1px solid white !important;
        // outline: none !important;
      }
      
      /* 修改页码输入框容器的焦点和点击状态 */
      :deep(.el-pagination .el-input__wrapper:focus),
      :deep(.el-pagination .el-input__wrapper.is-focus) {
        // border: 1px solid white !important;
        box-shadow: 0 0 0 1px white !important;
        // outline: none !important;
      }
      
      /* 修改页码输入框文字的颜色为白色 */
      :deep(.el-pagination .el-input__inner) {
        color: white !important;
      }
    }
  }
  
  /* 修改表格的背景颜色和文字颜色 */
  :deep(.el-table) {
    background: transparent !important;
    --el-table-row-hover-bg-color: transparent !important;
    --el-table-row-hover-color: white !important;
    --el-table-border-color: rgba(255, 255, 255, 0.1) !important;
    
    .el-table__header-wrapper {
      background: transparent !important;
    }
    
    .el-table__body-wrapper {
      background: transparent !important;
    }
    
    .el-table__row {
      background: transparent !important;
      
      /* 修改表格行的文字颜色为白色 */
      td:not(.el-table-fixed-column--right) {
        color: white !important;
        background: transparent !important;
      }
      
      /* 去掉鼠标悬停时的背景色变化 */
      &:hover {
        background: transparent !important;
        
        td:not(.el-table-fixed-column--right) {
          background: transparent !important;
        }
      }
    }
    
    /* 修改表头的背景颜色和文字颜色 */
    thead {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    th {
      background: transparent !important;
      color: white !important;
      
      /* 修改表头边框颜色 */
      border-bottom-color: rgba(255, 255, 255, 0.2) !important;
    }
    
    /* 修改表头行的背景颜色 */
    .el-table__header tr {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 修改表头单元格的背景颜色 */
    .el-table__header th {
      background: transparent !important;
    }
    
    /* 修改表格行的边框颜色 */
    td:not(.el-table-fixed-column--right) {
      border-bottom-color: rgba(255, 255, 255, 0.1) !important;
      background: transparent !important;
    }
    
    /* 为固定列的td和th设置渐变背景 */
    td.el-table-fixed-column--right,
    th.el-table-fixed-column--right {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      border-bottom-color: rgba(255, 255, 255, 0.1) !important;
    }
    
    /* 修改表格最后一条横线为白色并加粗 */
    .el-table__body tr:last-child td,
    .el-table__body tr:last-child td.el-table-fixed-column--right {
      border-bottom-color: #ffffff !important;
      border-bottom-width: 1px !important;
    }
    
    /* 修改表格的滚动条样式 */
    .el-scrollbar__wrap {
      background: transparent !important;
    }
    
    /* 去掉表格行的hover效果 - 排除固定列 */
    &:not(.el-table--enable-row-transition) .el-table__body tr:hover > td:not(.el-table-fixed-column--right) {
      background-color: transparent !important;
    }
    
    /* 去掉表格行的hover效果（针对启用了行过渡的表格） - 排除固定列 */
    .el-table--enable-row-transition .el-table__body tr:hover > td:not(.el-table-fixed-column--right) {
      background-color: transparent !important;
    }
    
    /* 去掉表格行的hover效果（针对固定列） */
    .el-table__fixed-body-wrapper .el-table__body tr:hover > td {
      background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 去掉表格行的hover效果（更全面的覆盖） - 排除固定列 */
    .el-table__body tr:hover > td:not(.el-table-fixed-column--right) {
      background-color: transparent !important;
    }
    
    /* 去掉表格行的hover效果（针对斑马纹表格） - 排除固定列 */
    .el-table--striped .el-table__body tr.el-table__row--striped:hover > td:not(.el-table-fixed-column--right) {
      background-color: transparent !important;
    }
    
    /* 去掉表格行的hover效果（针对展开行） */
    .el-table__expand-icon-cell:hover {
      background-color: transparent !important;
    }
    
    /* 确保表格单元格在所有状态下的背景色都是透明 - 排除固定列 */
    .el-table__cell:not(.el-table-fixed-column--right) {
      background-color: transparent !important;
      
      &:hover {
        background-color: transparent !important;
      }
      
      &.el-table__cell--row-hover {
        background-color: transparent !important;
      }
    }
    
    /* 为固定列的单元格设置渐变背景 */
    .el-table__cell.el-table-fixed-column--right {
      background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      
      &:hover {
        background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      }
      
      &.el-table__cell--row-hover {
        background-color: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      }
    }
    
    /* 确保固定列表头的单元格内容区域也显示渐变背景 */
    th.el-table-fixed-column--right .cell {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 修复固定列（操作列）的背景颜色 - 终极解决方案 */
    /* 1. 固定列最外层容器 */
    .el-table__fixed,
    .el-table__fixed-right {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      z-index: 10 !important;
      
      /* 覆盖Element Plus固定列的伪元素背景 */
      &::before {
        background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: -1;
      }
    }
    
    /* 2. 固定列内部的所有直接子元素 */
    .el-table__fixed > *,
    .el-table__fixed-right > * {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 3. 固定列表格相关容器 */
    .el-table__fixed .el-table__header-wrapper,
    .el-table__fixed-right .el-table__header-wrapper,
    .el-table__fixed .el-table__body-wrapper,
    .el-table__fixed-right .el-table__body-wrapper,
    .el-table__fixed .el-scrollbar,
    .el-table__fixed-right .el-scrollbar,
    .el-table__fixed .el-scrollbar__wrap,
    .el-table__fixed-right .el-scrollbar__wrap,
    .el-table__fixed .el-scrollbar__view,
    .el-table__fixed-right .el-scrollbar__view {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 4. 固定列的表格元素本身 */
    .el-table__fixed table,
    .el-table__fixed-right table,
    .el-table__fixed .el-table,
    .el-table__fixed-right .el-table,
    .el-table__fixed .el-table__header,
    .el-table__fixed-right .el-table__header,
    .el-table__fixed .el-table__body,
    .el-table__fixed-right .el-table__body {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 5. 固定列的行和单元格 */
    .el-table__fixed .el-table__header tr,
    .el-table__fixed-right .el-table__header tr,
    .el-table__fixed .el-table__body tr,
    .el-table__fixed-right .el-table__body tr {
      background: transparent !important;
    }
    
    .el-table__fixed .el-table__header th,
    .el-table__fixed-right .el-table__header th {
      background: transparent !important;
      color: white !important;
      
      /* 确保单元格边框颜色一致 */
      border-bottom-color: rgba(255, 255, 255, 0.1) !important;
    }
    
    /* 固定列的单元格使用渐变背景 */
    .el-table__fixed .el-table__body td,
    .el-table__fixed-right .el-table__body td,
    .el-table__fixed .el-table__cell,
    .el-table__fixed-right .el-table__cell {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      color: white !important;
      
      /* 确保单元格边框颜色一致 */
      border-bottom-color: rgba(255, 255, 255, 0.1) !important;
    }
    
    /* 6. 固定列单元格的hover状态 */
    .el-table__fixed .el-table__cell:hover,
    .el-table__fixed-right .el-table__cell:hover,
    .el-table__fixed .el-table__cell--row-hover,
    .el-table__fixed-right .el-table__cell--row-hover,
    .el-table__fixed .el-table__body tr:hover > td,
    .el-table__fixed-right .el-table__body tr:hover > td {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 7. 确保固定列的所有层级都继承背景 */
    .el-table__fixed .el-table__cell *,
    .el-table__fixed-right .el-table__cell * {
      background: transparent !important;
    }
    
    /* 8. 确保固定列单元格的内容区域也显示渐变背景 */
    .el-table__fixed .el-table__cell .cell,
    .el-table__fixed-right .el-table__cell .cell {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
    }
    
    /* 8. 覆盖Element Plus可能存在的其他固定列样式 */
    :deep(.el-table__fixed),
    :deep(.el-table__fixed-right) {
      background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      
      &::before {
        background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
      }
    }
    
    /* 9. 确保固定列的表头也应用正确样式 */
    :deep(.el-table__fixed .el-table__header th),
    :deep(.el-table__fixed-right .el-table__header th) {
      background: transparent !important;
      color: white !important;
    }
  }
  
  /* 修改表格中标签的样式 */
  // :deep(.el-tag) {
  //   background-color: rgba(255, 255, 255, 0.2) !important;
  //   color: white !important;
  //   border-color: rgba(255, 255, 255, 0.3) !important;
  // }
}

/* 属性类型样式 */
.property-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.property-semi {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.property-finished {
  background-color: #f0f9eb;
  color: #67c23a;
}

/* 新增生产计划对话框样式 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  border: none !important;
  
  .el-dialog__header {
    background: transparent !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    .el-dialog__title {
      color: white !important;
    }
    
    .el-dialog__headerbtn {
      color: white !important;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1) !important;
      }
    }
  }
  
  .el-dialog__body {
    background: transparent !important;
    color: white !important;
    
    /* 确保表单元素的背景颜色正确 */
    .el-select__wrapper,
    .el-input__wrapper {
      background-color: #48494c !important;
      
      .el-input__inner {
        background-color: #48494c !important;
        color: white !important;
      }
      
      .el-select__placeholder {
        color: rgba(255, 255, 255, 0.7) !important;
      }
      
      /* 点击时边框变为白色 */
      &:focus-within,
      &:hover {
        box-shadow: 0 0 0 1px #ffffff inset !important;
        border-color: #ffffff !important;
      }
    }
    
    .el-textarea__inner {
      background-color: #48494c !important;
      color: white !important;
      
      /* 点击时边框变为白色 */
      &:focus,
      &:hover {
        box-shadow: 0 0 0 1px #ffffff inset !important;
        border-color: #ffffff !important;
      }
    }
    
    /* 确保标签颜色正确 */
    .el-form-item__label {
      color: white !important;
    }
  }
  
  .el-dialog__footer {
    background: transparent !important;
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }
}

/* 时间选择器弹出层样式 - 终极解决方案 */
/* 直接覆盖所有与日期选择器相关的元素 - 使用最高优先级 */
/* 1. 覆盖最外层容器 */
:deep(.el-popper.el-date-picker__popper),
:deep(.el-popper.el-date-picker__popper.el-popper--light),
:deep(.el-date-picker__popper),
:deep(.el-popper__inner) {
  background-color: #48494c !important;
  background-image: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  overflow: hidden !important;
  min-height: 420px !important;
  height: auto !important;
}

/* 2. 覆盖所有子元素，包括深层嵌套的 */
:deep(.el-popper.el-date-picker__popper) *,
:deep(.el-popper.el-date-picker__popper.el-popper--light) *,
:deep(.el-date-picker__popper) *,
:deep(.el-popper__inner) * {
  background-color: #48494c !important;
  background-image: none !important;
}

/* 3. 直接覆盖Element Plus日期选择器的所有组件 */
:deep(.el-picker-panel),
:deep(.el-picker-panel *),
:deep(.el-date-picker),
:deep(.el-date-picker *),
:deep(.el-picker-panel__content),
:deep(.el-picker-panel__content *),
:deep(.el-picker-panel__body),
:deep(.el-picker-panel__body *),
:deep(.el-picker-panel__footer),
:deep(.el-picker-panel__footer *),
:deep(.el-picker-panel__content-wrapper),
:deep(.el-picker-panel__content-wrapper *),
:deep(.el-date-table),
:deep(.el-date-table *),
:deep(.el-time-panel),
:deep(.el-time-panel *),
:deep(.el-time-spinner),
:deep(.el-time-spinner *),
:deep(.el-picker-panel__header),
:deep(.el-picker-panel__header *),
:deep(.el-date-picker__shortcuts),
:deep(.el-date-picker__shortcuts *),
:deep(.el-date-picker__header-label),
:deep(.el-date-picker__header-label *),
:deep(.el-time-panel__footer),
:deep(.el-time-panel__footer *),
:deep(.el-time-editor),
:deep(.el-time-editor *),
:deep(.el-picker-panel__sidebar),
:deep(.el-picker-panel__sidebar *) {
  background-color: #48494c !important;
  background-image: none !important;
  border-color: white !important;
  color: white !important;
}

/* 4. 覆盖所有伪元素 */
:deep(.el-popper.el-date-picker__popper)::before,
:deep(.el-popper.el-date-picker__popper)::after,
:deep(.el-popper.el-date-picker__popper.el-popper--light)::before,
:deep(.el-popper.el-date-picker__popper.el-popper--light)::after,
:deep(.el-date-picker__popper)::before,
:deep(.el-date-picker__popper)::after,
:deep(.el-popper__inner)::before,
:deep(.el-popper__inner)::after,
:deep(.el-popper__arrow)::before,
:deep(.el-popper__arrow)::after {
  background-color: #48494c !important;
  background-image: none !important;
  border-color: #48494c !important;
}

/* 5. 直接针对底部白色区域的修复 */
:deep(.el-picker-panel__content-wrapper),
:deep(.el-picker-panel__content-wrapper *) {
  background-color: #48494c !important;
  background-image: none !important;
  height: 100% !important;
  min-height: 380px !important;
  overflow: hidden !important;
}

:deep(.el-picker-panel__body),
:deep(.el-picker-panel__body *) {
  background-color: #48494c !important;
  background-image: none !important;
  height: 100% !important;
  min-height: 380px !important;
  overflow: hidden !important;
}

:deep(.el-picker-panel__content),
:deep(.el-picker-panel__content *) {
  background-color: #48494c !important;
  background-image: none !important;
  height: 100% !important;
  min-height: 380px !important;
  overflow: hidden !important;
}

/* 6. 确保日期选择器的整个弹出层没有任何白色背景 */
:deep(.el-date-picker__popper) {
  background-color: #48494c !important;
  background-image: none !important;
  overflow: hidden !important;
  height: auto !important;
  min-height: 420px !important;
}

/* 7. 使用全局选择器确保覆盖所有可能的元素 */
:global(.el-popper.el-date-picker__popper),
:global(.el-popper.el-date-picker__popper *),
:global(.el-popper.el-date-picker__popper.el-popper--light),
:global(.el-popper.el-date-picker__popper.el-popper--light *),
:global(.el-date-picker__popper),
:global(.el-date-picker__popper *),
:global(.el-popper__inner),
:global(.el-popper__inner *) {
  background-color: #48494c !important;
  background-image: none !important;
  border-color: white !important;
  color: white !important;
}

/* 8. 终极终极解决方案：直接覆盖所有可能的白色区域 */
:global(.el-picker-panel),
:global(.el-picker-panel) *, 
:global(.el-date-picker),
:global(.el-date-picker) *, 
:global(.el-popper),
:global(.el-popper) *, 
:global(.el-popper__inner),
:global(.el-popper__inner) *, 
:global(.el-picker-panel__content),
:global(.el-picker-panel__content) *, 
:global(.el-picker-panel__body),
:global(.el-picker-panel__body) *, 
:global(.el-picker-panel__footer),
:global(.el-picker-panel__footer) *, 
:global(.el-picker-panel__content-wrapper),
:global(.el-picker-panel__content-wrapper) *, 
:global(.el-date-table),
:global(.el-date-table) *, 
:global(.el-time-panel),
:global(.el-time-panel) *, 
:global(.el-time-spinner),
:global(.el-time-spinner) *, 
:global(.el-picker-panel__header),
:global(.el-picker-panel__header) *, 
:global(.el-date-picker__shortcuts),
:global(.el-date-picker__shortcuts) *, 
:global(.el-date-picker__header-label),
:global(.el-date-picker__header-label) *, 
:global(.el-time-panel__footer),
:global(.el-time-panel__footer) *, 
:global(.el-time-editor),
:global(.el-time-editor) *, 
:global(.el-picker-panel__sidebar),
:global(.el-picker-panel__sidebar) * {
  background-color: #48494c !important;
  background-image: none !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 9. 确保日期选择器的整个弹出层高度足够，覆盖所有白色区域 */
:deep(.el-picker-panel) {
  background-color: #48494c !important;
  background-image: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  overflow: hidden !important;
  min-height: 420px !important;
  height: auto !important;
  
  /* 确保所有子元素都继承背景色 */
  * {
    background-color: #48494c !important;
  }
  
  /* 头部样式 */
  .el-picker-panel__header {
    background-color: #48494c !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    .el-picker-panel__icon-btn {
      color: white !important;
      
      &:hover {
        color: #ffffff !important;
        background-color: rgba(255, 255, 255, 0.1) !important;
      }
    }
    
    .el-picker-panel__title {
      color: white !important;
    }
    
    .el-month-table__btn,
    .el-year-table__btn {
      color: white !important;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1) !important;
      }
    }
  }
  
  /* 时间选择器主体样式 */
  .el-date-table {
    background-color: #48494c !important;
    
    th {
      color: rgba(255, 255, 255, 0.7) !important;
      
      &:first-child,
      &:last-child {
        color: rgba(255, 255, 255, 0.5) !important;
      }
    }
    
    td {
      color: white !important;
      
      .el-date-table__cell {
        color: white !important;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1) !important;
        }
        
        &.el-date-table__cell--today {
          color: #409eff !important;
        }
        
        &.el-date-table__cell--selected {
          background-color: #409eff !important;
          
          &:hover {
            background-color: #66b1ff !important;
          }
        }
      }
    }
  }
  
  /* 时间选择器底部样式 */
  .el-picker-panel__footer {
    background-color: #48494c !important;
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    /* 针对日期选择器底部的所有按钮，使用更精确的选择器 */
    > button,
    > .el-button {
      color: white !important;
      
      /* 明确禁用悬停效果，使用!important确保优先级 */
      &:hover,
      &:focus,
      &:active {
        background-color: transparent !important;
        border-color: transparent !important;
        color: white !important;
        box-shadow: none !important;
        transform: none !important;
      }
    }
    
    /* 针对主要按钮（"现在"按钮） */
    > .el-button--primary {
      background-color: #409eff !important;
      border-color: #409eff !important;
      
      /* 明确禁用悬停效果，保持原有样式 */
      &:hover,
      &:focus,
      &:active {
        background-color: #409eff !important;
        border-color: #409eff !important;
        color: white !important;
        box-shadow: none !important;
        transform: none !important;
      }
    }
  }
  
  /* 时间选择器侧边栏样式 */
  .el-picker-panel__sidebar {
    background-color: #48494c !important;
    border-right: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    .el-picker-panel__sidebar-btn {
      color: white !important;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1) !important;
      }
      
      &.is-active {
        background-color: rgba(255, 255, 255, 0.1) !important;
        color: #409eff !important;
      }
    }
  }
  
  /* 时间选择器时间面板样式 */
  .el-time-panel {
    background-color: #48494c !important;
    
    .el-time-spinner {
      background-color: #48494c !important;
      
      .el-time-spinner__item {
        color: white !important;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1) !important;
        }
        
        &.el-time-spinner__item--active {
          color: #409eff !important;
        }
      }
    }
    
    .el-time-panel__content::after,
    .el-time-panel__content::before {
      background-color: rgba(255, 255, 255, 0.1) !important;
    }
  }
  
  /* 时间选择器快捷选项样式 */
  .el-date-picker__header-label {
    color: white !important;
  }
  
  .el-date-picker__shortcuts {
    background-color: #48494c !important;
    border-right: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    .el-picker-panel__shortcut {
      color: white !important;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1) !important;
      }
    }
  }
  
  /* 额外的样式覆盖，确保所有元素都没有白色背景 */
  .el-picker-panel__content {
    background-color: #48494c !important;
  }
  
  .el-date-range-picker__content {
    background-color: #48494c !important;
  }
  
  .el-date-range-picker__header {
    background-color: #48494c !important;
  }
  
  .el-time-panel__footer {
    background-color: #48494c !important;
  }
  
  .el-time-panel__footer button {
    color: white !important;
  }
  
  /* 确保时间选择器的输入框背景也是深色 */
  .el-time-editor {
    background-color: #48494c !important;
    
    .el-input__inner {
      background-color: #48494c !important;
      color: white !important;
    }
  }
  
  /* 确保清除按钮区域也是深色 */
  .el-picker-panel__footer {
    background-color: #48494c !important;
  }
  
  /* 确保所有可能的容器都是深色 */
  .el-picker-panel__body {
    background-color: #48494c !important;
  }
  
  .el-picker-panel__content-wrapper {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的底部容器也是深色 */
  .el-picker-panel__footer {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的整个容器都是深色，包括可能的阴影区域 */
  &::before,
  &::after {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有面板都是深色 */
  .el-picker-panel__content,
  .el-picker-panel__content:first-child,
  .el-picker-panel__content:last-child {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的时间选择部分也是深色 */
  .el-time-panel {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的快捷选项区域也是深色 */
  .el-date-picker__shortcuts {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有按钮都是深色背景 */
  .el-button {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有输入框都是深色背景 */
  .el-input {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有下拉菜单都是深色背景 */
  .el-select-dropdown {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有选项都是深色背景 */
  .el-select-dropdown__item {
    background-color: #48494c !important;
    color: white !important;
  }
  
  /* 确保日期选择器的所有分割线都是浅色的 */
  .el-divider {
    background-color: rgba(255, 255, 255, 0.1) !important;
  }
  
  /* 确保日期选择器的所有边框都是浅色的 */
  * {
    border-color: rgba(255, 255, 255, 0.1) !important;
  }
  
  /* 确保日期选择器的底部白色区域被覆盖 */
  .el-picker-panel {
    background-color: #48494c !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的所有子面板都是深色 */
  .el-picker-panel__content {
    background-color: #48494c !important;
    height: 100% !important;
  }
  
  /* 直接针对日期选择器的底部白色区域 - 终极解决方案 */
  /* 覆盖所有与日期选择器相关的元素 */
  :deep(.el-picker-panel),
  :deep(.el-date-picker),
  :deep(.el-picker-panel__content),
  :deep(.el-picker-panel__body),
  :deep(.el-picker-panel__footer),
  :deep(.el-picker-panel__content-wrapper),
  :deep(.el-date-table),
  :deep(.el-time-panel),
  :deep(.el-time-spinner),
  :deep(.el-picker-panel__header),
  :deep(.el-date-picker__shortcuts),
  :deep(.el-date-picker__header-label),
  :deep(.el-time-panel__footer),
  :deep(.el-time-editor),
  :deep(.el-picker-panel__sidebar) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有子元素都是深色 */
  :deep(.el-picker-panel) *,
  :deep(.el-date-picker) *,
  :deep(.el-picker-panel__content) *,
  :deep(.el-picker-panel__body) *,
  :deep(.el-picker-panel__footer) *,
  :deep(.el-picker-panel__content-wrapper) *,
  :deep(.el-date-table) *,
  :deep(.el-time-panel) *,
  :deep(.el-time-spinner) *,
  :deep(.el-picker-panel__header) *,
  :deep(.el-date-picker__shortcuts) *,
  :deep(.el-date-picker__header-label) *,
  :deep(.el-time-panel__footer) *,
  :deep(.el-time-editor) *,
  :deep(.el-picker-panel__sidebar) * {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的背景色覆盖整个区域 */
  :deep(.el-picker-panel) {
    background-color: #48494c !important;
    background-image: none !important;
    overflow: hidden !important;
    
    /* 确保日期选择器的所有伪元素也是深色 */
    &::before,
    &::after {
      background-color: #48494c !important;
      background-image: none !important;
    }
  }
  
  /* 确保日期选择器的内容区域完全覆盖 */
  :deep(.el-picker-panel__content) {
    background-color: #48494c !important;
    background-image: none !important;
    min-height: 100% !important;
    height: auto !important;
  }
  
  /* 确保日期选择器的body区域完全覆盖 */
  :deep(.el-picker-panel__body) {
    background-color: #48494c !important;
    background-image: none !important;
    min-height: 100% !important;
    height: auto !important;
  }
  
  /* 确保日期选择器的底部区域完全覆盖 */
  :deep(.el-picker-panel__footer) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有边框都是浅色的 */
  :deep(.el-picker-panel) * {
    border-color: rgba(255, 255, 255, 0.1) !important;
  }
  
  /* 使用global选择器确保覆盖所有日期选择器元素 */
  :global(.el-picker-panel),
  :global(.el-date-picker),
  :global(.el-picker-panel__content),
  :global(.el-picker-panel__body),
  :global(.el-picker-panel__footer),
  :global(.el-picker-panel__content-wrapper),
  :global(.el-date-table),
  :global(.el-time-panel),
  :global(.el-time-spinner),
  :global(.el-picker-panel__header),
  :global(.el-date-picker__shortcuts),
  :global(.el-date-picker__header-label),
  :global(.el-time-panel__footer),
  :global(.el-time-editor),
  :global(.el-picker-panel__sidebar) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 使用global选择器确保覆盖所有日期选择器子元素 */
  :global(.el-picker-panel) *,
  :global(.el-date-picker) *,
  :global(.el-picker-panel__content) *,
  :global(.el-picker-panel__body) *,
  :global(.el-picker-panel__footer) *,
  :global(.el-picker-panel__content-wrapper) *,
  :global(.el-date-table) *,
  :global(.el-time-panel) *,
  :global(.el-time-spinner) *,
  :global(.el-picker-panel__header) *,
  :global(.el-date-picker__shortcuts) *,
  :global(.el-date-picker__header-label) *,
  :global(.el-time-panel__footer) *,
  :global(.el-time-editor) *,
  :global(.el-picker-panel__sidebar) * {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 使用global选择器确保覆盖日期选择器的伪元素 */
  :global(.el-picker-panel::before),
  :global(.el-picker-panel::after),
  :global(.el-date-picker::before),
  :global(.el-date-picker::after) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的下拉面板完全覆盖，包括底部白色区域 */
  :global(.el-popper__inner) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的整个弹出层都是深色 */
  :global(.el-popper) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有内部容器都是深色 */
  :global(.el-date-editor) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的时间选择部分完全覆盖 */
  :global(.el-time-panel__content) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的底部按钮区域完全覆盖 */
  :global(.el-time-panel__footer) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的清除按钮区域完全覆盖 */
  :global(.el-picker-panel__footer) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 终极解决方案：直接覆盖整个日期选择器的所有元素 */
  /* 使用 !important 确保最高优先级 */
  :global(.el-picker-panel),
  :global(.el-date-picker),
  :global(.el-picker-panel__content),
  :global(.el-picker-panel__body),
  :global(.el-picker-panel__footer),
  :global(.el-picker-panel__content-wrapper),
  :global(.el-date-table),
  :global(.el-time-panel),
  :global(.el-time-spinner),
  :global(.el-picker-panel__header),
  :global(.el-date-picker__shortcuts),
  :global(.el-date-picker__header-label),
  :global(.el-time-panel__footer),
  :global(.el-time-editor),
  :global(.el-picker-panel__sidebar),
  :global(.el-popper),
  :global(.el-popper__inner),
  :global(.el-popper__arrow),
  :global(.el-popper__arrow::before) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: #48494c !important;
    box-shadow: none !important;
    color: white !important;
  }
  
  /* 确保日期选择器的所有子元素都是深色 */
  :global(.el-picker-panel *),
  :global(.el-date-picker *),
  :global(.el-popper *),
  :global(.el-time-panel *),
  :global(.el-time-spinner *),
  :global(.el-date-table *) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
  }
  
  /* 确保日期选择器的整个弹出层高度足够，覆盖所有白色区域 */
  :global(.el-picker-panel) {
    min-height: 400px !important;
    height: auto !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的内容区域完全填充 */
  :global(.el-picker-panel__body) {
    min-height: 350px !important;
    height: auto !important;
  }
  
  /* 确保日期选择器的箭头也是深色 */
  :global(.el-popper__arrow::before) {
    border-color: #48494c !important;
  }
  
  /* 确保没有任何伪元素导致白色区域 */
  :global(.el-picker-panel::before),
  :global(.el-picker-panel::after),
  :global(.el-date-picker::before),
  :global(.el-date-picker::after),
  :global(.el-popper::before),
  :global(.el-popper::after) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: #48494c !important;
  }
  
  /* 确保时间选择器的滚动区域也是深色 */
  :global(.el-time-spinner__wrapper) {
    background-color: #48494c !important;
  }
  
  /* 确保时间选择器的上下箭头区域也是深色 */
  :global(.el-time-spinner__arrow) {
    background-color: #48494c !important;
  }
  
  /* 直接覆盖日期选择器底部的白色区域 - 最关键的修复 */
  /* 这个选择器直接针对Element Plus日期选择器的弹出层容器 */
  :global(.el-popper.el-popper--light.el-date-picker__popper) {
    background-color: #48494c !important;
    min-height: 400px !important;
    height: auto !important;
  }
  
  /* 确保日期选择器的内容区域完全覆盖底部 */
  :global(.el-date-picker__popper .el-picker-panel__content) {
    background-color: #48494c !important;
    height: 100% !important;
    min-height: 380px !important;
  }
  
  /* 确保日期选择器的整个弹出层都是深色，包括底部的白色区域 */
  :global(.el-date-picker__popper) {
    background-color: #48494c !important;
    background-image: none !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  /* 确保日期选择器的所有内部容器都是深色 */
  :global(.el-date-picker__popper *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 直接针对日期选择器底部白色区域的修复 */
  :global(.el-picker-panel__content-wrapper) {
    background-color: #48494c !important;
    background-image: none !important;
    min-height: 380px !important;
  }
  
  /* 确保日期选择器的下拉内容区域完全覆盖 */
  :global(.el-date-picker__content) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有可能容器都是深色 */
  :global(.el-picker-panel__body-wrapper) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 直接覆盖日期选择器的所有子元素，确保没有遗漏 */
  :global(.el-picker-panel *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的下拉面板内容区域 */
  :global(.el-select-dropdown__wrap) {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的时间选择器面板 */
  :global(.el-time-panel__content) {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的快捷选项区域 */
  :global(.el-date-picker__shortcuts) {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的整个弹出层结构 */
  :global(.el-date-picker__popper .el-popper__inner) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的年份月份选择面板 */
  :global(.el-year-table),
  :global(.el-month-table) {
    background-color: #48494c !important;
  }
  
  /* 确保日期选择器的所有可能的白色区域都被覆盖 */
  :global(.el-picker-panel__content),
  :global(.el-picker-panel__content:first-child),
  :global(.el-picker-panel__content:last-child) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
  }
  
  /* 直接覆盖日期选择器的整个组件 */
  :global(.el-date-picker) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有子元素，包括深层嵌套的 */
  :global(.el-date-picker *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的弹出层完全覆盖 */
  :global(.el-date-picker__popper) {
    background-color: #48494c !important;
    background-image: none !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的底部区域没有白色 */
  :global(.el-picker-panel__footer) {
    background-color: #48494c !important;
    background-image: none !important;
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
  }
  
  /* 确保日期选择器的快捷选项底部区域 */
  :global(.el-date-picker__shortcuts) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的时间选择器底部区域 */
  :global(.el-time-panel__footer) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的整个高度足够，覆盖所有白色区域 */
  :global(.el-picker-panel) {
    background-color: #48494c !important;
    background-image: none !important;
    height: auto !important;
    min-height: 420px !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的内容区域完全填充 */
  :global(.el-picker-panel__body) {
    background-color: #48494c !important;
    background-image: none !important;
    height: auto !important;
    min-height: 380px !important;
  }
  
  /* 终极解决方案：直接覆盖所有可能的白色区域 */
  :global(.el-popper),
  :global(.el-popper__inner),
  :global(.el-picker-panel),
  :global(.el-date-picker),
  :global(.el-picker-panel__content),
  :global(.el-picker-panel__body),
  :global(.el-picker-panel__footer),
  :global(.el-picker-panel__content-wrapper),
  :global(.el-date-table),
  :global(.el-time-panel),
  :global(.el-time-spinner),
  :global(.el-picker-panel__header),
  :global(.el-date-picker__shortcuts),
  :global(.el-date-picker__header-label),
  :global(.el-time-panel__footer),
  :global(.el-time-editor),
  :global(.el-picker-panel__sidebar),
  :global(.el-date-picker__popper),
  :global(.el-date-picker__popper *),
  :global(.el-picker-panel *),
  :global(.el-date-picker *) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
  }
  
  /* 最高优先级：直接覆盖日期选择器的所有元素，包括深层嵌套 */
  :deep(.el-picker-panel),
  :deep(.el-picker-panel *) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
  }
  
  /* 直接针对日期选择器底部白色区域的修复 - 最高优先级 */
  :deep(.el-picker-panel__content-wrapper),
  :deep(.el-picker-panel__body-wrapper) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的整个弹出层没有任何白色背景 */
  :deep(.el-date-picker__popper) {
    background-color: #48494c !important;
    background-image: none !important;
    overflow: hidden !important;
    height: auto !important;
    min-height: 420px !important;
  }
  
  /* 确保日期选择器的内容区域完全填充，没有底部白色 */
  :deep(.el-picker-panel__content) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的body区域完全填充 */
  :deep(.el-picker-panel__body) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 直接覆盖所有可能的白色区域 - 终极选择器 */
  :deep(.el-picker-panel),
  :deep(.el-picker-panel) > *, 
  :deep(.el-picker-panel) > * > *, 
  :deep(.el-picker-panel) > * > * > *, 
  :deep(.el-picker-panel) > * > * > * > *, 
  :deep(.el-picker-panel) > * > * > * > * > * {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
  }
  
  /* 直接覆盖Element Plus日期选择器的所有元素，包括深层嵌套 */
  :deep(.el-date-picker *),
  :deep(.el-picker-panel *),
  :deep(.el-popper *),
  :deep(.el-picker-panel__content *),
  :deep(.el-picker-panel__body *),
  :deep(.el-picker-panel__footer *),
  :deep(.el-picker-panel__content-wrapper *),
  :deep(.el-date-table *),
  :deep(.el-time-panel *),
  :deep(.el-time-spinner *),
  :deep(.el-picker-panel__header *),
  :deep(.el-date-picker__shortcuts *),
  :deep(.el-date-picker__header-label *),
  :deep(.el-time-panel__footer *),
  :deep(.el-time-editor *),
  :deep(.el-picker-panel__sidebar *),
  :deep(.el-date-picker__popper *),
  :deep(.el-date-picker__content *),
  :deep(.el-date-range-picker__content *),
  :deep(.el-select-dropdown *),
  :deep(.el-select-dropdown__wrap *),
  :deep(.el-select-dropdown__item *),
  :deep(.el-year-table *),
  :deep(.el-month-table *),
  :deep(.el-date-table *),
  :deep(.el-time-panel *),
  :deep(.el-time-spinner *),
  :deep(.el-date-picker__shortcuts *),
  :deep(.el-picker-panel__header *),
  :deep(.el-picker-panel__footer *) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: white !important;
  }
  
  /* 覆盖所有伪元素 */
  :deep(.el-picker-panel::before),
  :deep(.el-picker-panel::after),
  :deep(.el-date-picker::before),
  :deep(.el-date-picker::after),
  :deep(.el-popper::before),
  :deep(.el-popper::after),
  :deep(.el-popper__arrow::before),
  :deep(.el-popper__arrow::after),
  :deep(.el-picker-panel__content::before),
  :deep(.el-picker-panel__content::after),
  :deep(.el-picker-panel__body::before),
  :deep(.el-picker-panel__body::after),
  :deep(.el-picker-panel__footer::before),
  :deep(.el-picker-panel__footer::after),
  :deep(.el-picker-panel__content-wrapper::before),
  :deep(.el-picker-panel__content-wrapper::after) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: #48494c !important;
  }
  
  /* 直接针对日期选择器底部白色区域的修复 - 最高优先级 */
  :deep(.el-picker-panel__content-wrapper),
  :deep(.el-picker-panel__content-wrapper *) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的body区域完全填充 */
  :deep(.el-picker-panel__body),
  :deep(.el-picker-panel__body *) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 确保日期选择器的内容区域完全填充 */
  :deep(.el-picker-panel__content),
  :deep(.el-picker-panel__content *) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 380px !important;
    overflow: hidden !important;
  }
  
  /* 直接覆盖日期选择器的整个弹出层 */
  :deep(.el-date-picker__popper),
  :deep(.el-date-picker__popper *) {
    background-color: #48494c !important;
    background-image: none !important;
    height: 100% !important;
    min-height: 420px !important;
    overflow: hidden !important;
  }
  
  /* 直接覆盖Element Plus的弹出层 */
  :deep(.el-popper),
  :deep(.el-popper *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 直接覆盖Element Plus的弹出层内容 */
  :deep(.el-popper__inner),
  :deep(.el-popper__inner *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的所有可能容器都是深色 */
  :deep(.el-date-editor),
  :deep(.el-date-editor *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的时间选择器内容 */
  :deep(.el-time-spinner__wrapper),
  :deep(.el-time-spinner__wrapper *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 确保日期选择器的时间选择器箭头 */
  :deep(.el-time-spinner__arrow),
  :deep(.el-time-spinner__arrow *) {
    background-color: #48494c !important;
    background-image: none !important;
  }
  
  /* 专门修复日期选择器OK按钮背景颜色 */
  :deep(.el-picker-panel__link-btn),
  :deep(.el-picker-panel__link-btn *),
  :deep(.el-picker-panel__footer .el-button--small),
  :deep(.el-picker-panel__footer .el-button--small *) {
    background-color: #48494c !important;
    background-image: none !important;
    border-color: #48494c !important;
    color: white !important;
  }
  
  /* 计划状态下拉选择器悬停样式 */
  :global(.el-select-dropdown__item:hover) {
    background-color: #5A5B5E !important;
  }
  
  /* 全局样式覆盖，确保分页组件中的select边框始终为白色 */
  :deep(.el-pagination) {
    .el-select__wrapper,
    .el-select__wrapper:focus,
    .el-select__wrapper.is-focus,
    .el-select__wrapper.is-active,
    .el-select__wrapper:hover {
      // border: 1px solid white !important;
      box-shadow: 0 0 0 1px white !important;
      outline: none !important;
    }
  }
}
</style>