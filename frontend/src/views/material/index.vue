<template>
    <div class="material-management">
      <div class="search-area">
      <el-input v-model="searchParams.searchText" placeholder="请输入物料名称或物料编码" class="search-input" @input="handleSearchInput" />
      <el-select v-model="searchParams.materialType" placeholder="物料类型" class="search-select" @change="search">
        <el-option label="原料" value="1" />
        <el-option label="半成品" value="2" />
        <el-option label="成品" value="3" />
      </el-select>
      <el-select v-model="searchParams.status" placeholder="状态" class="search-select" @change="search">
        <el-option label="启用" value="1" />
        <el-option label="禁用" value="0" />
      </el-select>
      <el-button type="primary" @click="search" class="rounded-button">查询</el-button>
      <el-button type="primary" @click="reset" class="rounded-button">重置</el-button>
      <el-button type="primary" @click="openAddDialog" class="rounded-button">新增物料</el-button>
      <el-button type="success" @click="handleExport" class="rounded-button export-button">
        <el-icon><Download /></el-icon>导出
      </el-button>
    </div>
    
    <el-card style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); border: none;">
      <el-table :data="materialsData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="materialCode" label="物料编码" width="150" />
        <el-table-column prop="materialName" label="物料名称" width="180" />
        <el-table-column prop="materialType" label="物料类型" width="100">
          <template #default="scope">
            <span class="property-type" :class="{
              'property-raw': scope.row.materialType === 1,
              'property-semi': scope.row.materialType === 2,
              'property-finished': scope.row.materialType === 3
            }">{{ getMaterialTypeText(scope.row.materialType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requiredMaterials" label="所需材料" width="250" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ formatRequiredMaterials(scope.row.requiredMaterials) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requiredTime" label="所需时间(小时)" width="120" />
        <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right" class-name="operation-column">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editMaterial(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
        <el-form-item label="物料编码" prop="materialCode" v-if="!isEdit">
          <el-input v-model="formData.materialCode" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName" required>
          <el-input v-model="formData.materialName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="物料类型" prop="materialType" required>
          <el-select v-model="formData.materialType" placeholder="请选择物料类型">
            <el-option label="原料" value="1" />
            <el-option label="半成品" value="2" />
            <el-option label="成品" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="所需材料" prop="selectedMaterials">
          <div v-if="selectedMaterials.length > 0" class="selected-materials-list">
            <div v-for="(material, index) in selectedMaterials" :key="material.id" class="material-item">
              <span class="material-name">{{ material.name }}</span>
              <el-input-number 
                v-model="material.quantity" 
                :min="1" 
                :step="1" 
                size="small"
                placeholder="数量"
              />
              <el-button 
                type="danger" 
                size="small" 
                @click="removeMaterial(index)"
              >删除</el-button>
            </div>
          </div>
          <el-select 
            v-model="tempSelectedMaterialId" 
            placeholder="请选择物料" 
            filterable
            clearable
            @change="addSelectedMaterial"
            class="add-material-select"
          >
            <el-option 
              v-for="(name, id) in filteredMaterialMap" 
              :key="id" 
              :label="name" 
              :value="id"
              :disabled="isMaterialSelected(id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所需时间(小时)" prop="requiredTime">
          <el-input-number v-model="formData.requiredTime" :min="0" :step="0.5" placeholder="请输入所需时间" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            placeholder="请输入描述" 
            type="textarea"
            rows="3"
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElLoading } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { exportToExcel } from '@/utils/export'
import { getMaterials, getMaterialById, createMaterial, updateMaterial, updateMaterialStatus, deleteMaterial } from '@/api/material'

export default {
  name: 'MaterialManagement',
  data() {
      return {
        searchParams: {
          searchText: '',
          materialType: '',
          status: ''
        },
        materialsData: [],
        loading: false,
        pagination: {
          currentPage: 1,
          pageSize: 10,
          total: 0
        },
        dialogVisible: false,
        dialogTitle: '新增物料',
        isEdit: false,
        formData: {
          id: '',
          materialCode: '',
          materialName: '',
          materialType: '',
          requiredMaterials: '',
          requiredTime: 0,
          description: '',
          unit: '',
          status: '1'
        },
        selectedMaterials: [], // 存储选中的物料及其数量
        tempSelectedMaterialId: '', // 临时选中的物料ID
        materialMap: {}, // 存储物料ID和名称的映射（用于表格显示）
        nameToIdMap: {}, // 存储物料名称到ID的映射（用于表格显示）
        filteredMaterialMap: {}, // 专门用于对话框选择器的过滤后的物料映射
        formRules: {
          materialCode: [
            { required: true, message: '请输入物料编码', trigger: 'blur' },
            { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
          ],
          materialName: [
            { required: true, message: '请输入物料名称', trigger: 'blur' },
            { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
          ],
          materialType: [
            { required: true, message: '请选择物料类型', trigger: 'change' }
          ],
          selectedMaterials: [],
          requiredTime: [
            { type: 'number', min: 0, message: '所需时间必须大于或等于0', trigger: 'blur' }
          ]
        }
    }
  },
  created() {
    this.loadAllMaterialsForMapping()
    this.loadMaterials()
  },
  beforeUnmount() {
    // 组件销毁时清除定时器
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },
  methods: {
    getMaterialTypeText(type) {
      const typeMap = {
        1: '原料',
        2: '半成品',
        3: '成品'
      }
      return typeMap[type] || type
    },
    loadMaterials() {
      this.loading = true
      // 创建搜索参数，同时将searchText赋给materialCode和materialName字段
      // 这样后端可以分别对编码和名称进行模糊查询
      const params = {
        materialCode: this.searchParams.searchText,
        materialName: this.searchParams.searchText,
        materialType: this.searchParams.materialType,
        status: this.searchParams.status,
        page: this.pagination.currentPage,
        pageSize: this.pagination.pageSize
      }
      
      // 调用API获取数据
      getMaterials(params).then(res => {
        if (res.code === 200) {
          this.materialsData = res.data.records || []
          this.pagination.total = res.data.total
        } else {
          ElMessage.error(res.msg || '获取物料列表失败')
        }
        this.loading = false
      }).catch(err => {
        console.error('获取物料列表失败:', err)
        ElMessage.error('获取物料列表失败')
        this.loading = false
      })
    },
    search() {
      this.pagination.currentPage = 1
      this.loadMaterials()
    },
    handleSearchInput() {
      // 防抖处理，避免频繁请求
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      this.searchTimer = setTimeout(() => {
        this.search()
      }, 300)
    },
    reset() {
      this.searchParams = {
        searchText: '',
        materialType: '',
        status: ''
      }
      this.pagination.currentPage = 1
      this.loadMaterials()
    },
    openAddDialog() {
      this.dialogTitle = '新增物料'
      this.isEdit = false
      this.$refs.formRef?.resetFields()
      this.formData = {
        id: '',
        materialCode: '',
        materialName: '',
        materialType: '',
        requiredMaterials: '',
        requiredTime: 0,
        description: ''
      }
      this.selectedMaterials = []
      this.tempSelectedMaterialId = ''
      this.dialogVisible = true
    },
    viewMaterial(row) {
      this.dialogTitle = '查看物料'
      this.isEdit = false
      this.formData = { ...row }
      this.dialogVisible = true
    },
    editMaterial(row) {
      this.dialogTitle = '编辑物料'
      this.isEdit = true
      // 深拷贝数据
      const materialData = { ...row }
      // 将materialType转换为字符串类型，确保与选择器选项值类型一致
      materialData.materialType = String(materialData.materialType)
      this.formData = materialData
      
      // 解析requiredMaterials字符串为selectedMaterials数组
      this.selectedMaterials = []
      this.tempSelectedMaterialId = ''
      
      if (materialData.requiredMaterials) {
        try {
          const materials = materialData.requiredMaterials.split('/')
          materials.forEach(item => {
            const [materialId, quantity] = item.split('*')
            if (materialId && quantity && this.materialMap[materialId]) {
              this.selectedMaterials.push({
                id: materialId,
                name: this.materialMap[materialId],
                quantity: parseInt(quantity)
              })
            }
          })
        } catch (error) {
          console.error('解析所需材料失败:', error)
        }
      }
      this.dialogVisible = true
    },
    toggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      this.$confirm(`确定要${statusText}物料「${row.materialName}」吗？`, '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用API更新状态
        updateMaterialStatus(row.id, newStatus).then(res => {
          if (res.code === 200) {
            ElMessage.success(`${statusText}成功`)
            this.loadMaterials()
          } else {
            ElMessage.error(res.msg || `${statusText}失败`)
          }
        }).catch(() => {
          ElMessage.error(`${statusText}失败`)
        })
      }).catch(() => {
        ElMessage.info('已取消操作')
      })
    },
    confirmDialog() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          // 深拷贝formData，避免直接修改原始数据
          const data = { ...this.formData };
          
          // 将selectedMaterials数组转换为后端所需的字符串格式: "id*quantity/id*quantity"
          if (this.selectedMaterials && this.selectedMaterials.length > 0) {
            data.requiredMaterials = this.selectedMaterials
              .map(item => `${item.id}*${item.quantity}`)
              .join('/');
          } else {
            data.requiredMaterials = '';
          }
          
          if (this.isEdit) {
            // 调用API更新数据
            updateMaterial(data).then(res => {
              if (res.code === 200) {
                ElMessage.success('更新成功')
                this.dialogVisible = false
                this.loadMaterials()
              } else {
                ElMessage.error(res.msg || '更新失败')
              }
            }).catch(() => {
              ElMessage.error('更新失败')
            })
          } else {
            // 调用API创建数据
            createMaterial(data).then(res => {
              if (res.code === 200) {
                ElMessage.success('创建成功')
                this.dialogVisible = false
                this.loadMaterials()
              } else {
                ElMessage.error(res.msg || '创建失败')
              }
            }).catch(() => {
              ElMessage.error('创建失败')
            })
          }
        }
      })
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadMaterials()
    },
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadMaterials()
    },
    handleDelete(row) {
      this.$confirm('确定要删除该物料吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用API删除数据
        deleteMaterial(row.id).then(res => {
          if (res.code === 200) {
            ElMessage.success('删除成功')
            this.loadMaterials()
            this.loadAllMaterialsForMapping() // 重新加载物料映射
          } else {
            ElMessage.error(res.msg || '删除失败')
          }
        }).catch(() => {
          ElMessage.error('删除失败')
        })
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    },
    
    // 加载所有物料用于建立ID到名称的映射
    loadAllMaterialsForMapping() {
      // 这里使用获取所有物料的API（不进行分页）
      const params = {
        page: 1,
        pageSize: 1000, // 假设最大数量为1000，实际应根据系统情况调整
        materialCode: '',
        materialName: '',
        materialType: '',
        status: ''
      }
      
      getMaterials(params).then(res => {
        if (res.code === 200) {
          const materials = res.data.records || []
          // 建立完整的ID到名称的映射和名称到ID的映射（用于表格显示）
          this.materialMap = {}
          this.nameToIdMap = {}
          // 创建专门用于对话框选择器的过滤后的物料映射
          this.filteredMaterialMap = {}
          
          materials.forEach(material => {
            // 为表格显示建立完整映射
            this.materialMap[material.id] = material.materialName
            this.nameToIdMap[material.materialName] = material.id
            
            // 只将符合条件的物料添加到对话框选择器的映射中
            // 条件：material_type为1(原料)和2(半成品)且status为1，同时避免循环依赖
            if ((material.materialType === 1 || material.materialType === 2) && 
                material.status === 1 && 
                (!this.isEdit || material.id !== this.formData.id)) {
              this.filteredMaterialMap[material.id] = material.materialName
            }
          })
        }
      }).catch(err => {
        console.error('加载物料映射失败:', err)
      })
    },
    
    // 添加选中的物料
    addSelectedMaterial() {
      if (!this.tempSelectedMaterialId) return
      
      const materialName = this.materialMap[this.tempSelectedMaterialId]
      if (materialName && !this.isMaterialSelected(this.tempSelectedMaterialId)) {
        this.selectedMaterials.push({
          id: this.tempSelectedMaterialId,
          name: materialName,
          quantity: 1 // 默认数量为1
        })
      }
      
      // 清空临时选择
      this.tempSelectedMaterialId = ''
    },
    
    // 移除选中的物料
    removeMaterial(index) {
      this.selectedMaterials.splice(index, 1)
    },
    
    // 检查物料是否已被选中
    isMaterialSelected(materialId) {
      return this.selectedMaterials.some(item => item.id === materialId)
    },
    
    // 将物料名称格式转换为物料ID格式
    formatMaterialsToIds(materialsString) {
      if (!materialsString) return '';
      try {
        // 按斜杠分割多个物料项
        const materialItems = materialsString.split('/');
        
        // 转换每个物料项：物料名称*数量 -> 物料ID*数量
        const formattedMaterials = materialItems.map(item => {
          // 按星号分割物料名称和数量
          const [name, quantity] = item.split('*');
          if (!name || !quantity) return item;
          
          // 获取对应的物料ID
          const materialId = this.nameToIdMap[name.trim()];
          
          // 如果找不到对应的ID，则使用原始名称（作为容错处理）
          return materialId ? `${materialId}*${quantity}` : item;
        });
        
        // 重新组合为斜杠分隔的字符串
        return formattedMaterials.join('/');
      } catch (error) {
        console.error('转换物料名称到ID失败:', error);
        return materialsString; // 出错时返回原始字符串
      }
    },
    
    // 格式化所需材料显示，将物料ID转换为物料名称
    formatRequiredMaterials(requiredMaterials) {
      if (!requiredMaterials) return ''
      
      try {
        // 分割所需材料字符串，如 "1*2/2*1" => ["1*2", "2*1"]
        const materials = requiredMaterials.split('/')
        
        // 转换每个物料项
        const formattedMaterials = materials.map(item => {
          // 分割物料ID和数量，如 "1*2" => ["1", "2"]
          const [materialId, quantity] = item.split('*')
          
          // 获取物料名称，如果找不到则显示原始ID
          const materialName = this.materialMap[materialId] || `物料${materialId}`
          
          // 返回格式化后的字符串，如 "物品名称1*2"
          return `${materialName}*${quantity}`
        })
        
        // 重新组合为斜杠分隔的字符串
        return formattedMaterials.join('/')
      } catch (error) {
        console.error('格式化所需材料失败:', error)
        return requiredMaterials // 出错时返回原始字符串
      }
    },
    
    // 导出物料数据
    handleExport() {
      if (this.materialsData.length === 0) {
        ElMessage.warning('没有可导出的数据')
        return
      }
      
      let loadingInstance
      try {
        loadingInstance = ElLoading.service({ message: '正在导出数据，请稍候...', lock: true })
        
        // 准备导出数据
        const exportData = this.materialsData.map(material => ({
          物料编码: material.materialCode,
          物料名称: material.materialName,
          物料类型: this.getMaterialTypeText(material.materialType),
          所需材料: this.formatRequiredMaterials(material.requiredMaterials),
          '所需时间(小时)': material.requiredTime,
          单位: material.unit,
          描述: material.description || '无',
          状态: material.status === 1 ? '启用' : '禁用',
          创建时间: material.createTime,
          更新时间: material.updateTime
        }))
        
        // 调用导出函数
        exportToExcel(exportData, '物料数据')
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
  }
}
</script>

<style scoped>
.material-management {
  padding: 20px;
}

/* 为表格相关容器添加渐变背景 - 提高优先级并覆盖所有表格元素 */
.material-management :global(.el-table),
.material-management :global(.el-table__inner-wrapper),
.material-management :global(.el-table__header-wrapper),
.material-management :global(.el-table__body-wrapper),
.material-management :global(.el-table__header),
.material-management :global(.el-table__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  background-image: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  --el-table-bg-color: transparent !important;
}

/* 确保表格主体行背景透明 */
.material-management :global(.el-table__row),
.material-management :global(.el-table__body-row) {
  background-color: transparent !important;
}

/* 为表头行添加渐变背景 */
.material-management :global(.el-table__header-row) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-image: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
}

/* 确保表头单元格有渐变背景 */
.material-management :global(.el-table__header-wrapper .el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-image: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 为表格表头th标签添加渐变背景 */
.material-management :global(.el-table__header th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-image: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 为固定列表头th标签添加渐变背景 */
.material-management :global(.el-table-fixed-column--right th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-image: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 确保表格主体单元格背景透明并设置文字颜色为白色 */
.material-management :global(.el-table__body-wrapper .el-table__cell) {
  background-color: transparent !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

/* 确保表格中所有文本元素颜色为白色 */
.material-management :global(.el-table__body-wrapper .el-table__cell .cell) {
  color: white !important;
}

/* 确保表格中标签文字颜色为白色 */
.material-management :global(.el-table__body-wrapper) {
  color: white !important;
}

.search-area {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    align-items: center;
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    padding: 15px;
    border-radius: 4px;
  }
  
  /* 搜索区域所有文本颜色设置为白色 */
  .search-area :deep(.el-input__inner),
  .search-area :deep(.el-select__placeholder),
  .search-area :deep(.el-select__text),
  .search-area :deep(.el-select__selected-item),
  .search-area :deep(.el-select__selected-item span),
  .search-area :deep(.el-select__input),
  .search-area :deep(.el-select__caret),
  .search-area :deep(.el-icon),
  .search-area :deep(.el-select__icon svg path),
  .search-area .el-button {
    color: white !important;
    fill: white !important;
  }
  
  /* 确保placeholder也显示为白色 */
  .search-area :deep(.el-input__inner)::placeholder,
  .search-area :deep(.el-select__input)::placeholder {
    color: rgba(255, 255, 255, 0.8) !important;
  }
  
  /* 确保选中状态的文本颜色 */
  .search-area :deep(.el-select__wrapper.is-focused .el-select__placeholder),
  .search-area :deep(.el-select__wrapper.is-focused .el-select__selected-item) {
    color: white !important;
  }
  
  /* 搜索区域所有输入框和选择器的背景颜色 */
  .search-area :deep(.el-input__wrapper),
  .search-area :deep(.el-select__wrapper) {
    background-color: #48494c !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    --el-input-bg-color: #48494c !important;
    --el-select-bg-color: #48494c !important;
  }
  
  /* 悬浮状态 */
  .search-area :deep(.el-input__wrapper:hover),
  .search-area :deep(.el-select__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.3) !important;
  }
  
  /* 聚焦状态 - 确保点击时边框颜色为白色 */
  .search-area :deep(.el-input__wrapper.is-focused),
  .search-area :deep(.el-select__wrapper.is-focused) {
    border-color: white !important;
    box-shadow: 0 0 0 1px white inset !important;
  }
  
  /* 兼容:focus-within伪类 */
  .search-area :deep(.el-input__wrapper):focus-within,
  .search-area :deep(.el-select__wrapper):focus-within {
    border-color: white !important;
    box-shadow: 0 0 0 1px white inset !important;
  }
  
  /* 确保选择器内部元素也有相同背景 */
  .search-area :deep(.el-select__selection),
  .search-area :deep(.el-select__input-wrapper),
  .search-area :deep(.el-select__placeholder) {
    background-color: #48494c !important;
  }

.search-input {
  width: 200px;
}

.search-select {
    width: 120px;
  }

  /* 圆角按钮样式 */
  .rounded-button {
    background-color: #ffffff !important;
    color: #274151 !important;
    border-color: #ffffff !important;
    transition: all 0.3s ease !important;
  }
  
  /* 导出按钮文字和图标颜色设置 */
  :global(.el-button.rounded-button.export-button span),
  :global(.el-button.rounded-button.export-button .el-icon) {
    color: #67C23A !important;
  }
  
  /* 选中物料列表样式 */
  .selected-materials-list {
    margin-bottom: 10px;
  }
  
  .material-item {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    padding: 8px;
    background-color: #48494c;
    border-radius: 4px;
    border: 1px solid #ffffff;
  }
  
  .material-name {
    flex: 1;
    margin-right: 10px;
    color: #ffffff;
  }
  
  .add-material-select {
    width: 100%;
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
  
  /* 导出按钮样式 */
  :global(.el-button.rounded-button.export-button span),
  :global(.el-button.rounded-button.export-button .el-icon) {
    color: #67C23A !important;
  }
  
  /* 新增物料对话框的线性渐变背景 */
  :global(.el-dialog) {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  }
  
  /* 确保对话框内的文字颜色与深色背景协调 */
  :global(.el-dialog .el-dialog__title) {
    color: white !important;
  }
  
  :global(.el-dialog .el-form-item__label) {
    color: #e0e0e0 !important;
  }
  
  /* 为对话框中的输入框和选择器添加背景颜色 */
  :global(.el-dialog .el-input__wrapper) {
    background-color: #48494c !important;
  }
  
  /* 输入框聚焦时的边框颜色 */
  :global(.el-dialog .el-input__wrapper:focus-within) {
    box-shadow: 0 0 0 1px #ffffff inset !important;
    border-color: #ffffff !important;
  }
  
  :global(.el-dialog .el-select__wrapper) {
    background-color: #48494c !important;
  }
  
  /* 选择器聚焦时的边框颜色 */
  :global(.el-dialog .el-select__wrapper:focus-within) {
    box-shadow: 0 0 0 1px #ffffff inset !important;
    border-color: #ffffff !important;
  }

  /* 选择器下拉菜单背景和选项悬停样式 */
  :global(.el-select-dropdown) {
    background-color: #48494c !important;
  }

  :global(.el-select-dropdown__item) {
    color: white !important;
  }

  :global(.el-select-dropdown__item:hover) {
    background-color: #5A5B5E !important;
    color: white !important;
  }
  
  :global(.el-dialog .el-textarea__inner) {
    background-color: #48494c !important;
    color: white !important;
  }
  
  /* 文本框聚焦时的边框颜色 */
  :global(.el-dialog .el-textarea__inner:focus) {
    border-color: #ffffff !important;
    box-shadow: 0 0 0 1px #ffffff !important;
  }
  
  /* 确保输入框和选择器内的文字颜色可见 */
  :global(.el-dialog .el-input__inner) {
    color: white !important;
  }
  
  :global(.el-dialog .el-select__placeholder) {
    color: white !important;
  }

  /* 确保对话框中选择器选中项的文字颜色为白色 */
  :global(.el-dialog .el-select__selection .el-select__selected-item) {
    color: white !important;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  /* 分页组件文字颜色设置为白色 */
  .pagination :deep(.el-pagination__total),
  .pagination :deep(.el-pagination__goto),
  .pagination :deep(.el-pagination__sizes),
  .pagination :deep(.el-select__label),
  .pagination :deep(.el-select__text),
  .pagination :deep(.el-pager li),
  .pagination :deep(.el-pagination__jump-prev),
  .pagination :deep(.el-pagination__jump-next),
  .pagination :deep(.el-pagination__prev),
  .pagination :deep(.el-pagination__next) {
    color: white !important;
  }

  /* 确保分页输入框文字也是白色 */
  .pagination :deep(.el-input__inner) {
    color: white !important;
  }

  /* 确保分页组件所有相关文字和图标颜色为白色 */
  /* 选择器placeholder文字 */
  .pagination :deep(.el-select__placeholder) {
    color: white !important;
  }
  
  /* 按钮图标颜色 */
  .pagination :deep(.el-icon) {
    color: white !important;
  }
  
  /* 按钮图标SVG颜色 */
  .pagination :deep(.el-icon svg path) {
    fill: white !important;
  }
  
  /* 页码输入框placeholder文字 */
  .pagination :deep(.el-input__inner)::placeholder {
    color: rgba(255, 255, 255, 0.8) !important;
  }

  /* 分页组件五个特定区域的背景颜色设置为#48494c */
  /* 1. 分页选择器（10/page） */
  .pagination :deep(.el-select__wrapper) {
    background-color: #48494c !important;
  }

  /* 分页选择器聚焦时的白色边框 */
  .pagination :deep(.el-select__wrapper:focus-within) {
    border: 1px solid white !important;
    box-shadow: 0 0 0 1px #ffffff !important;
  }

  /* 2. 上一页和下一页按钮 */
  .pagination :deep(.btn-prev),
  .pagination :deep(.btn-next) {
    background-color: #48494c !important;
    border: 1px solid white !important;
    margin: 0 8px !important;
  }

  /* 上一页和下一页按钮聚焦时的白色边框 */
  .pagination :deep(.btn-prev:focus),
  .pagination :deep(.btn-next:focus) {
    border: 1px solid white !important;
    box-shadow: 0 0 0 1px #ffffff !important;
    outline: none !important;
  }

  /* 所有页码项（包括选中和未选中） */
  .pagination :deep(.el-pager li) {
    background-color: #48494c !important;
    border: 1px solid white !important;
    margin: 0 8px !important;
  }

  /* 页码项聚焦时的白色边框 */
  .pagination :deep(.el-pager li:focus) {
    border: 1px solid white !important;
    box-shadow: 0 0 0 1px #ffffff !important;
    outline: none !important;
  }

  /* 4. 页码输入框 */
  .pagination :deep(.el-input__wrapper) {
    background-color: #48494c !important;
  }

  /* 页码输入框聚焦时的白色边框 */
  .pagination :deep(.el-input__wrapper:focus-within) {
    border: 1px solid white !important;
    box-shadow: 0 0 0 1px #ffffff !important;
  }

/* 表格整列th标签样式 */
.el-table__header th {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  color: white !important;
  border-right: 1px solid rgba(255, 255, 255, 0.1) !important;
}

/* 固定列th标签样式 */
.el-table-fixed-column--right th {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
  color: white !important;
}

/* 确保表头行背景透明，避免覆盖th标签样式 */
.el-table__header-wrapper .el-table__header {
  background-color: transparent !important;
}

@media (max-width: 768px) {
  .search-area {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .search-select {
    width: 100%;
  }
}

/* 操作列样式 */
:global(.operation-column) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
}

/* 固定右列单元格样式 */
:global(.el-table-fixed-column--right .el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
}

/* 固定列容器背景 */
:global(.el-table-fixed-right) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
}

/* 属性类型样式 */
.property-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.property-raw {
  background-color: #ecf5ff;
  color: #409eff;
}

.property-semi {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.property-finished {
  background-color: #f0f9eb;
  color: #67c23a;
}
</style>