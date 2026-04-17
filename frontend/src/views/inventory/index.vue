<template>
  <div class="inventory-container">
    <!-- 新建容器，包含搜索区域和批量操作按钮 -->
    <div class="top-operations-container">
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="queryParams.keyword"
              placeholder="请输入物品名称或编码"
              clearable
              @keyup.enter="handleQuery"
              class="custom-bg-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>

          <el-col :span="18" class="search-controls">
            <el-select
              v-model="queryParams.property"
              placeholder="筛选属性"
              clearable
              style="width: 120px; margin-right: 15px;"
              class="custom-bg-select"
              @change="handleQuery"
            >
              <el-option label="原料" value="1" />
              <el-option label="半成品" value="2" />
              <el-option label="成品" value="3" />
            </el-select>
            <el-select
              v-model="queryParams.forbidden"
              placeholder="筛选状态"
              clearable
              style="width: 140px; margin-right: 15px;"
              class="custom-bg-select"
              @change="handleQuery"
              value-key="value"
            >
              <el-option label="启用" :value="1" /> <!-- 确保值为数字类型 -->
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleQuery" class="rounded-button" style="margin-right: 15px;">搜索</el-button>
            <el-button @click="resetQuery" class="rounded-button" style="margin-right: 15px;">重置</el-button>
            <el-button type="primary" @click="openBatchAdjustDialog" class="rounded-button" style="margin-right: 15px;">
              <el-icon><Plus /></el-icon>批量调整库存
            </el-button>
            <el-button type="primary" @click="openAddItemDialog" class="rounded-button" style="margin-right: 15px;">
              <el-icon><Plus /></el-icon>新增物品
            </el-button>
            <el-button type="success" @click="handleExport" class="rounded-button export-button">
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-button type="primary" @click="viewInventoryLogs" class="rounded-button">
              <el-icon><Document /></el-icon>查看库存日志
            </el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 库存统计卡片 -->
    <div class="stats-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="never" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); color: white; border: none;">
            <div class="stat-content">
              <div class="stat-number" style="color: white;">{{ statistics.totalProducts || 0 }}</div>
              <div class="stat-label" style="color: white;">物品类别</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); color: white; border: none;">
            <div class="stat-content">
              <div class="stat-number" style="color: white;">{{ statistics.totalStock || 0 }}</div>
              <div class="stat-label" style="color: white;">总库存量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card low-stock" shadow="never" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); color: white; border: none;">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.lowStockCount || 0 }}</div>
              <div class="stat-label" style="color: white;">低库存物品</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card zero-stock" shadow="never" style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); color: white; border: none;">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.zeroStockCount || 0 }}</div>
              <div class="stat-label" style="color: white;">零库存物品</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 库存表格 -->
    <el-card style="background: linear-gradient(135deg, #464e58 0%, #434c55 100%); border: none;">
      <el-table
        v-loading="loading"
        :data="inventoryList"
        style="width: 100%; background: transparent !important;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="物品ID" width="80" />
        <el-table-column label="物品图片" width="80">
          <template #default="scope">
            <el-image
              v-if="getImageUrl(scope.row.image)"
              :src="getImageUrl(scope.row.image)"
              class="product-image"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="物品名称" min-width="120">
          <template #default="scope">
            <span class="product-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="物品分类" width="120" />
        <el-table-column prop="code" label="物品编码" width="120" />
        <el-table-column prop="property" label="属性" width="100">
          <template #default="scope">
            <span :class="{
              'property-type': true,
              'property-raw': scope.row.property === 1,
              'property-semi': scope.row.property === 2,
              'property-finished': scope.row.property === 3
            }">
              {{ scope.row.property === 1 ? '原料' : scope.row.property === 2 ? '半成品' : scope.row.property === 3 ? '成品' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="150">
          <template #default="scope">
            {{ scope.row.price !== null ? '¥' + scope.row.price : '未定价' }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存数量" width="120">
          <template #default="scope">
            <span
              :class="{
                'stock-low': scope.row.stock < 50,
                'stock-zero': scope.row.stock === 0,
                'stock-normal': scope.row.stock >= 50
              }"
            >
              {{ scope.row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="上架状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="物品描述" min-width="180">
          <template #default="scope">
            <el-tooltip :content="scope.row.description || ''" placement="top">
              <div class="description-text">{{ scope.row.description || '-' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>


        <el-table-column prop="library_coding" label="库位编码" width="150" />
        <el-table-column prop="forbidden" label="启用状态" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.forbidden"
              :active-value="1"
              :inactive-value="0"
              @change="handleForbiddenChange(scope.row)"
              style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column width="180" fixed="right">
          <template #header>
            <span style="margin-right: 10px;">操作</span>
            <el-checkbox
              v-model="queryParams.lowStock"
              label="仅显示低库存"
              size="small"
              @change="handleQuery"
              class="low-stock-checkbox"
            >仅显示低库存</el-checkbox>
          </template>
          <template #default="scope">
            <div style="display: inline-flex; gap: 7.8px;">
              <el-button
                type="primary"
                @click="openAdjustDialog(scope.row)"
                class="rounded-button"
              >
                调整库存
              </el-button>
              <el-button
                v-if="[2, 3].includes(scope.row.property) && scope.row.stock < 50"
                type="success"
                @click="openProductionDialog(scope.row)"
                class="rounded-button"
              >生产</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialog.visible"
      title="调整库存"
      width="500px"
      @close="resetAdjustForm"
      class="custom-dialog"
    >
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="物品图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccessSingle"
          >
            <el-image
              v-if="adjustForm.image"
              :src="getImageUrl(adjustForm.image)"
              fit="contain"
              style="width: 50px; height: 50px; cursor: pointer"
            />
            <el-icon v-else class="avatar-uploader-icon" style="font-size: 50px; height: 50px; width: 50px; line-height: 50px"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="adjustForm.productName" readonly class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="物品分类" prop="category">
          <el-input v-model="adjustForm.category" placeholder="物品分类" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="物品编码" prop="code">
          <el-input v-model="adjustForm.code" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="属性" prop="property">
          <el-select v-model="adjustForm.property" placeholder="选择属性" class="custom-bg-select">
            <el-option label="原料" :value="1" />
            <el-option label="半成品" :value="2" />
            <el-option label="成品" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model.number="adjustForm.price"
            :min="0"
            :step="0.01"
            placeholder="单价"
            class="custom-bg-input"
          />
        </el-form-item>
        <el-form-item label="当前库存" prop="currentStock">
          <el-input v-model="adjustForm.currentStock" readonly class="custom-bg-input" />
        </el-form-item>
        <!-- 供应商调整列表 -->
        <el-form-item v-if="[1, 2].includes(adjustForm.property)">
          <div class="supplier-adjustments-container">
            <div class="supplier-adjustment-header">
              <span>供应商调整列表</span>
              <el-button type="primary" size="small" @click="addSupplierAdjustment" class="rounded-button">
                <el-icon><Plus /></el-icon>添加供应商
              </el-button>
            </div>
            <div 
              v-for="(item, index) in adjustForm.supplierAdjustments" 
              :key="index"
              class="supplier-adjustment-item"
            >
              <el-row :gutter="20" style="align-items: center;">
                <el-col :span="10">
                  <el-select 
                    v-model="item.supplierId" 
                    placeholder="请选择供应商" 
                    class="custom-bg-select"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="supplier in supplierList"
                      :key="supplier.id"
                      :label="supplier.name"
                      :value="supplier.id"
                    />
                  </el-select>
                </el-col>
                <el-col :span="8">
                  <el-input-number
                    v-model.number="item.adjustAmount"
                    :min="-adjustForm.currentStock"
                    :step="1"
                    placeholder="调整数量"
                    class="custom-bg-input"
                    style="width: 100%"
                  />
                </el-col>
                <el-col :span="4">
                  <el-button 
                    type="danger" 
                    size="small"
                    @click="removeSupplierAdjustment(index)"
                    class="rounded-button"
                  >
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </el-col>
              </el-row>
            </div>
            <div v-if="(adjustForm.supplierAdjustments || []).length === 0" class="no-supplier-adjustments">
              暂无供应商调整项，点击"添加供应商"按钮添加
            </div>
          </div>
        </el-form-item>
        <!-- 非原料和半成品类型的调整数量 -->
        <el-form-item label="调整数量" prop="adjustAmount" v-else>
          <el-input-number
            v-model.number="adjustForm.adjustAmount"
            :min="-adjustForm.currentStock"
            :step="1"
            placeholder="请输入调整数量"
            class="custom-bg-input"
          />
        </el-form-item>
        <el-form-item label="调整后库存" prop="afterStock">
          <el-input :value="newStockComputed" readonly class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="上架状态" prop="status">
          <el-switch
            v-model="adjustForm.status"
            :active-value="1"
            :inactive-value="0"
            style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
          />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="adjustForm.description" type="textarea" rows="2" placeholder="商品描述" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="库位编码" prop="library_coding">
          <el-input v-model="adjustForm.library_coding" placeholder="库位编码" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="启用状态" prop="forbidden">
          <el-switch
            v-model="adjustForm.forbidden"
            :active-value="1"
            :inactive-value="0"
            style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
          />
        </el-form-item>
        <el-form-item label="调整备注" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" rows="3" placeholder="请输入调整备注" class="custom-bg-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="adjustDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleAdjust">确认调整</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量调整对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      title="批量调整库存及属性"
      width="1000px"
      @close="resetBatchForm"
      class="custom-dialog"
    >
      <div class="batch-tip">请选择要调整的物品（已选择 {{ selectedRows.length }} 件）</div>
      <el-table
        v-loading="batchLoading"
        :data="selectedRows"
        style="width: 100%"
        height="400"
      >
        <el-table-column prop="image" label="物品图片" width="120" :cell-style="{ paddingRight: '20px' }">
          <template #default="scope">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="(response) => handleUploadSuccess(response, scope.row)"
            >
              <el-image
                v-if="scope.row.image"
                :src="getImageUrl(scope.row.image)"
                fit="contain"
                style="width: 50px; height: 50px; cursor: pointer"
              />
              <el-icon v-else class="avatar-uploader-icon" style="font-size: 50px; height: 50px; width: 50px; line-height: 50px"><Plus /></el-icon>
            </el-upload>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="物品名称" min-width="150">
          <template #default="scope">
            <el-input v-model="scope.row.name" placeholder="物品名称" />
          </template>
        </el-table-column>
        <el-table-column prop="category" label="物品分类" width="120">
          <template #default="scope">
            <el-input v-model="scope.row.category" placeholder="物品分类" />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="物品编码" width="120">
          <template #default="scope">
            <el-input v-model="scope.row.code" placeholder="物品编码" />
          </template>
        </el-table-column>
        <el-table-column prop="property" label="属性" width="120">
          <template #default="scope">
            <el-select v-model="scope.row.property" placeholder="选择属性" class="custom-bg-select">
              <el-option label="原料" :value="1" />
              <el-option label="半成品" :value="2" />
              <el-option label="成品" :value="3" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="调整单价" width="190">
          <template #default="scope">
            <el-input-number
              v-model.number="scope.row.price"
              :min="0"
              :step="0.01"
              placeholder="单价"
            />
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="100">
          <template #default="scope">
            <span
              :class="{
                'stock-low': scope.row.currentStock < 50,
                'stock-zero': scope.row.currentStock === 0,
                'stock-normal': scope.row.currentStock >= 50
              }"
            >
              {{ scope.row.currentStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="调整数量" width="500">
          <template #default="scope">
            <!-- 对于原料和半成品，显示供应商调整列表 -->
            <div v-if="[1, 2].includes(scope.row.property)">
              <div class="supplier-adjustments-container" style="margin-bottom: 10px;">
                <div class="supplier-adjustment-header" style="margin-bottom: 5px;">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="addBatchSupplierAdjustment(scope.row)"
                    class="rounded-button"
                  >
                    <el-icon><Plus /></el-icon>添加供应商
                  </el-button>
                </div>
                <div 
                  v-for="(item, index) in scope.row.supplierAdjustments" 
                  :key="index"
                  class="supplier-adjustment-item"
                  style="margin-bottom: 5px;"
                >
                  <el-row :gutter="10" style="align-items: center; font-size: 12px;">
                    <el-col :span="9">
                      <el-select 
                        v-model="item.supplierId" 
                        placeholder="请选择供应商" 
                        style="width: 100%; font-size: 12px;"
                      >
                        <el-option
                          v-for="supplier in scope.row.supplierList"
                          :key="supplier.id"
                          :label="supplier.name"
                          :value="supplier.id"
                        />
                      </el-select>
                    </el-col>
                    <el-col :span="10">
                      <el-input-number
                        v-model.number="item.adjustAmount"
                        :min="-scope.row.currentStock"
                        :step="1"
                        placeholder="调整数量"
                        style="width: 100%; font-size: 12px;"
                      />
                    </el-col>
                    <el-col :span="4" style="text-align: right;">
                      <el-button 
                        type="danger" 
                        size="small"
                        @click="removeBatchSupplierAdjustment(scope.row, index)"
                        class="rounded-button"
                      >
                        <el-icon><Delete /></el-icon>删除
                      </el-button>
                    </el-col>
                  </el-row>
                </div>
                <div v-if="(scope.row.supplierAdjustments || []).length === 0" style="font-size: 12px; color: #909399;">
                  暂无供应商调整项，点击"添加供应商"按钮添加
                </div>
              </div>
            </div>
            <!-- 对于成品，直接输入调整数量 -->
            <el-input-number
              v-else
              v-model.number="scope.row.adjustAmount"
              :min="-scope.row.currentStock"
              :step="1"
              placeholder="调整数量"
              style="width: 200px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="上架状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="商品描述" min-width="150">
          <template #default="scope">
            <el-input v-model="scope.row.description" placeholder="商品描述" />
          </template>
        </el-table-column>
        <el-table-column prop="library_coding" label="库位编码" width="120">
          <template #default="scope">
            <el-input v-model="scope.row.library_coding" placeholder="库位编码" />
          </template>
        </el-table-column>
        <el-table-column prop="forbidden" label="启用状态" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.forbidden"
              :active-value="1"
              :inactive-value="0"
              style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="调整备注" min-width="150">
          <template #default="scope">
            <el-input v-model="scope.row.remark" placeholder="请输入备注" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchAdjust">确认批量调整</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增物品对话框 -->
    <el-dialog
      v-model="addItemDialog.visible"
      title="新增物品"
      width="500px"
      @close="resetAddItemForm"
      class="custom-dialog"
    >
      <el-form ref="addItemFormRef" :model="addItemForm" :rules="addItemRules" label-width="100px">
        <el-form-item label="物品图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccessAddItem"
          >
            <el-image
              v-if="addItemForm.image"
              :src="getImageUrl(addItemForm.image)"
              fit="contain"
              style="width: 50px; height: 50px; cursor: pointer"
            />
            <el-icon v-else class="avatar-uploader-icon" style="font-size: 50px; height: 50px; width: 50px; line-height: 50px"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="物品名称" prop="name">
          <el-select v-model="addItemForm.name" placeholder="选择物品名称" class="custom-bg-select" @change="handleItemChange">
            <el-option
              v-for="productName in productNameList"
              :key="productName"
              :label="productName"
              :value="productName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物品分类" prop="category">
          <el-input v-model="addItemForm.category" placeholder="物品分类" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="物品编码" prop="code">
          <el-input v-model="addItemForm.code" placeholder="物品编码" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="属性" prop="property">
          <span :class="{
                  'property-type': true,
                  'property-raw': addItemForm.property === 1,
                  'property-semi': addItemForm.property === 2,
                  'property-finished': addItemForm.property === 3
                }">
            {{ addItemForm.property === 1 ? '原料' : addItemForm.property === 2 ? '半成品' : '成品' }}</span>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model.number="addItemForm.price"
            :min="0"
            :step="0.01"
            placeholder="单价"
            class="custom-bg-input"
          />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model.number="addItemForm.stock"
            :min="0"
            :step="1"
            placeholder="库存数量"
            class="custom-bg-input"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId" v-if="[1, 2].includes(addItemForm.property)">
          <el-select v-model="addItemForm.supplierId" placeholder="请选择供应商" class="custom-bg-select">
            <el-option
              v-for="supplier in supplierList"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上架状态" prop="status">
          <el-switch
            v-model="addItemForm.status"
            :active-value="1"
            :inactive-value="0"
            style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
          />
        </el-form-item>
        <el-form-item label="物品描述" prop="description">
          <el-input v-model="addItemForm.description" placeholder="物品描述" type="textarea" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="库位编码" prop="library_coding">
          <el-input v-model="addItemForm.library_coding" placeholder="库位编码" class="custom-bg-input" />
        </el-form-item>
        <el-form-item label="启用状态" prop="forbidden">
          <el-switch
            v-model="addItemForm.forbidden"
            :active-value="1"
            :inactive-value="0"
            style="--el-switch-on-color: #67c23a; --el-switch-off-color: #c0c0c0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addItemDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleAddItem">确认新增</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增生产计划对话框 -->
    <el-dialog
      v-model="productionDialog.visible"
      title="新增生产计划"
      width="600px"
      class="custom-dialog"
    >
      <el-form ref="productionFormRef" :model="productionForm" :rules="productionRules" label-width="120px">
        <el-form-item label="产物名称" prop="productName">
          <el-input 
            v-model="productionForm.productName" 
            placeholder="自动生成"
            readonly
          />
        </el-form-item>
        <el-form-item label="产物属性" prop="productProperty">
          <el-input 
            :value="productionForm.productProperty === 2 ? '半成品' : '成品'"
            placeholder="自动生成"
            readonly
          />
        </el-form-item>
        <el-form-item label="生产数量" prop="quantity">
          <el-input-number 
            v-model="productionForm.quantity" 
            :min="1" 
            :step="1" 
            :precision="0" 
            placeholder="请输入生产数量"
            @change="handleQuantityChange"
          />
        </el-form-item>
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="productionForm.planName" placeholder="自动生成" readonly />
        </el-form-item>
        <el-form-item label="所需材料" prop="requiredMaterials">
          <el-input 
            :value="formatRequiredMaterials(productionForm.requiredMaterials)" 
            placeholder="自动生成" 
            type="textarea"
            rows="3"
            readonly
            class="custom-bg-input"
          />
        </el-form-item>
        <el-form-item label="制作时间(小时)" prop="productionTime">
          <el-input-number 
            v-model="productionForm.productionTime" 
            :min="0.1" 
            :step="0.1" 
            :precision="2" 
            placeholder="自动生成"
            readonly
          />
        </el-form-item>
        <el-form-item label="开始生产时间" prop="startTime">
          <el-date-picker
            v-model="productionForm.startTime"
            type="datetime"
            placeholder="请选择开始生产时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="calculateExpectedEndTime"
          />
        </el-form-item>
        <el-form-item label="预计完成时间" prop="expectedEndTime">
          <el-date-picker
            v-model="productionForm.expectedEndTime"
            type="datetime"
            placeholder="自动计算"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            readonly
          />
        </el-form-item>
        <el-form-item label="计划状态" prop="status">
          <el-select v-model="productionForm.status" placeholder="请选择计划状态">
            <el-option label="计划中" :value="1" />
            <el-option label="生产中" :value="2" />
            <el-option label="完成" :value="3" />
            <el-option label="取消" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="productionDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitProductionForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 库存日志查看对话框 -->
    <el-dialog
      v-model="logDialog.visible"
      title="库存调整日志"
      width="1000px"
      class="custom-dialog"
    >
      <div class="log-content-container">
        <div v-if="logLoading" class="log-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载日志中...</span>
        </div>
        <div v-else-if="logContent.length === 0" class="log-empty">
          <el-icon><DocumentRemove /></el-icon>
          <span>暂无库存调整日志</span>
        </div>
        <pre v-else class="log-content">{{ logContent }}</pre>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="logDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="downloadLogs">下载日志</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Delete, Document, Loading, DocumentRemove } from '@element-plus/icons-vue'
import { getInventoryList, adjustStock, batchAdjustStock, getInventoryStatistics, updateProductForbiddenStatus, getInventoryLogs } from '@/api/inventory'
import { updateProduct, addProduct } from '@/api/product'
import { getSupplierList, getSuppliersByProductName, getProductNames, getPropertyByProductName } from '@/api/supplier'
import { createProductionPlan } from '@/api/production'
import { getMaterials } from '@/api/material'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  lowStock: false,
  property: '',
  forbidden: ''
})

// 库存列表数据
const inventoryList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref([])

// 统计数据
const statistics = ref({})

// 供应商列表
const supplierList = ref([])

// 产品名称列表
const productNameList = ref([])

// 调整对话框
const adjustDialog = reactive({
  visible: false,
  title: ''
})

// 批量调整对话框
const batchDialog = reactive({
  visible: false
})
const batchLoading = ref(false)

// 新增物品对话框
const addItemDialog = reactive({
  visible: false
})

// 新增物品表单
const addItemFormRef = ref(null)
const addItemForm = reactive({
  name: '',
  category: '',
  code: '',
  property: 1,
  price: 0,
  stock: 0,
  supplierId: undefined,
  status: 0,
  description: '',
  library_coding: '',
  forbidden: 1,
  image: ''
})

// 生产计划对话框
const productionDialog = reactive({
  visible: false
})

// 生产计划表单
const productionFormRef = ref(null)
const productionForm = reactive({
  id: '',
  planName: '',
  productName: '',
  productProperty: 3,
  quantity: 1,
  requiredMaterials: '',
  productionTime: 0,
  startTime: '',
  expectedEndTime: '',
  status: 1
})

// 库存日志对话框
const logDialog = reactive({
  visible: false
})

// 日志内容
const logContent = ref('')
const logLoading = ref(false)

// 获取库存调整日志
const viewInventoryLogs = async () => {
  logLoading.value = true
  logContent.value = ''
  logDialog.visible = true
  
  try {
    // 调用后端API获取日志
    const res = await getInventoryLogs()
    logContent.value = res.data
    logLoading.value = false
  } catch (error) {
    ElMessage.error('加载日志失败：' + (error.message || '未知错误'))
    logLoading.value = false
  }
}

// 下载日志
const downloadLogs = () => {
  if (logContent.value.length === 0) {
    ElMessage.warning('暂无日志可下载')
    return
  }
  
  try {
    const blob = new Blob([logContent.value], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `inventory-logs-${new Date().toISOString().slice(0, 10)}.log`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('日志下载成功')
  } catch (error) {
    ElMessage.error('日志下载失败：' + (error.message || '未知错误'))
  }
}

// 物料列表
const materials = ref([])
const selectedMaterial = ref(null)

// 物料映射（用于将ID转换为名称）
const materialMap = ref({})

// 新增物品表单校验规则
const addItemRules = {
  name: [
    { required: true, message: '请输入物品名称', trigger: 'change' }
  ],
  category: [
    { required: false, message: '请输入物品分类', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请输入物品编码', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入单价', trigger: 'change' },
    { type: 'number', min: 0, message: '单价不能为负数', trigger: 'change' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'change' },
    { type: 'number', min: 0, message: '库存数量不能为负数', trigger: 'change' }
  ]
}

// 生产计划表单校验规则
const productionRules = {
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
}

// 调整表单
const adjustFormRef = ref(null)
const adjustForm = reactive({
  productId: undefined,
  productName: '',
  category: '',
  code: '',
  property: 1,
  price: 0,
  currentStock: 0,
  supplierAdjustments: [], // 供应商调整数组
  newStock: 0,
  status: 1,
  description: '',
  library_coding: '',
  forbidden: 0,
  remark: '',
  image: ''
})

// 计算总调整数量
const totalAdjustAmount = computed(() => {
  return adjustForm.supplierAdjustments.reduce((total, item) => {
    return total + (item.adjustAmount || 0)
  }, 0)
})

// 计算新库存
const newStockComputed = computed(() => {
  return adjustForm.currentStock + totalAdjustAmount.value
})

// 监听调整数量变化，更新新库存
import { watch } from 'vue'
watch(() => adjustForm.supplierAdjustments, () => {
  adjustForm.newStock = adjustForm.currentStock + totalAdjustAmount.value
}, { deep: true })

// 添加供应商调整项
const addSupplierAdjustment = () => {
  adjustForm.supplierAdjustments.push({
    supplierId: undefined,
    adjustAmount: 0
  })
}

// 删除供应商调整项
const removeSupplierAdjustment = (index) => {
  adjustForm.supplierAdjustments.splice(index, 1)
}

// 表单校验规则
const adjustRules = {
  remark: [
    { required: false, message: '请输入调整备注', trigger: 'blur' }
  ]
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 重置查询参数
const resetQuery = () => {
  Object.assign(queryParams, {
    keyword: '',
    lowStock: false,
    property: '',
    forbidden: ''
  })
  queryParams.pageNum = 1
  handleQuery()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取库存列表
const getList = async () => {
      loading.value = true
      try {
        // 创建一个新的参数对象，避免直接修改响应式对象
        const params = { ...queryParams }
        
        // 处理数字类型的forbidden参数：只在有值时传递
        if (params.forbidden === null || params.forbidden === undefined || params.forbidden === '') {
          delete params.forbidden
        } else {
          // 确保值为数字类型
          params.forbidden = Number(params.forbidden)
        }
        
        // 处理property参数：只在有值时转换并传递
        if (params.property === null || params.property === undefined || params.property === '') {
          delete params.property
        } else {
          params.property = Number(params.property)
        }
        
        // 发送请求并获取数据
        const res = await getInventoryList(params)
        let records = res.data.records
        let filteredTotal = res.data.total
          
        // 前端额外筛选确保显示正确结果
        if (queryParams && queryParams.forbidden !== '' && queryParams.forbidden !== null && queryParams.forbidden !== undefined) {
          const forbiddenValue = Number(queryParams.forbidden)
          records = records.filter(item => Number(item.forbidden) === forbiddenValue)
          filteredTotal = records.length
        }
          
        inventoryList.value = records
        total.value = filteredTotal
  } catch (error) {
        ElMessage.error('获取库存列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  queryParams.pageNum = current
  getList()
}

// 打开调整对话框
const openAdjustDialog = async (row) => {
  adjustForm.productId = row.id
  adjustForm.productName = row.name
  adjustForm.category = row.category || ''
  adjustForm.code = row.code || ''
  adjustForm.property = row.property || 1
  adjustForm.price = row.price || 0
  adjustForm.currentStock = row.stock || 0
  adjustForm.supplierId = undefined
  adjustForm.adjustAmount = 0
  adjustForm.newStock = row.stock || 0
  adjustForm.status = row.status ?? 1
  adjustForm.description = row.description || ''
  adjustForm.library_coding = row.library_coding || ''
  adjustForm.forbidden = row.forbidden ?? 0
  adjustForm.image = row.image || ''
  adjustForm.remark = ''
  adjustDialog.title = `调整商品"${row.name}"的库存`
  
  // 获取供应商列表
  try {
    const response = await getSuppliersByProductName(row.name) // 根据产品名称获取相关供应商
    supplierList.value = response.data || []
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    supplierList.value = []
  }
  
  adjustDialog.visible = true
}

// 重置调整表单
const resetAdjustForm = () => {
  if (adjustFormRef.value) {
    adjustFormRef.value.resetFields()
  }
  adjustForm.productId = undefined
  adjustForm.productName = ''
  adjustForm.category = ''
  adjustForm.code = ''
  adjustForm.property = 1
  adjustForm.price = 0
  adjustForm.currentStock = 0
  adjustForm.supplierAdjustments = [] // 清空供应商调整数组
  adjustForm.adjustAmount = 0
  adjustForm.newStock = 0
  adjustForm.status = 1
  adjustForm.description = ''
  adjustForm.library_coding = ''
  adjustForm.forbidden = 0
  adjustForm.image = ''
  adjustForm.remark = ''
}

// 处理库存调整
const handleAdjust = async () => {
  if (!await adjustFormRef.value.validate()) {
    return
  }

  try {
    // 验证供应商调整项
    if ([1, 2].includes(adjustForm.property)) {
      // 检查是否有任何调整项
      const hasAdjustments = (adjustForm.supplierAdjustments || []).length > 0;
      // 检查是否有增加数量的调整项
      const hasIncreaseAdjustments = adjustForm.supplierAdjustments.some(item => item.adjustAmount > 0);
      
      // 如果有增加数量的调整项，必须至少有一个
      if (hasIncreaseAdjustments && (adjustForm.supplierAdjustments || []).length === 0) {
        ElMessage.warning('请添加至少一个供应商调整项')
        return
      }
      
      // 验证每个供应商调整项
      for (const item of adjustForm.supplierAdjustments) {
        // 除了调整数量为0外，其他所有情况（包括增加和减少）都需要选择供应商
        if (item.adjustAmount !== 0 && !item.supplierId) {
          ElMessage.warning('请选择供应商')
          return
        }
        if (item.adjustAmount === 0) {
          ElMessage.warning('调整数量不能为0')
          return
        }
      }
    } else {
      // 非原料和半成品类型，允许调整数量为0
      // 无需验证调整数量是否为0
    }

    // 准备调整数据
    const adjustments = []
    
    if ([1, 2].includes(adjustForm.property)) {
      // 多个供应商调整
      for (const item of adjustForm.supplierAdjustments) {
        const adjustment = {
          productId: adjustForm.productId,
          adjustAmount: item.adjustAmount,
          remark: adjustForm.remark,
          supplierId: item.supplierId,
          // 添加所有可能修改的属性
          name: adjustForm.productName,
          category: adjustForm.category,
          code: adjustForm.code,
          property: adjustForm.property,
          price: adjustForm.price,
          status: adjustForm.status,
          description: adjustForm.description,
          library_coding: adjustForm.library_coding,
          forbidden: adjustForm.forbidden,
          image: adjustForm.image
        }
        
        // 移除未定义或null值，但保留空字符串
        Object.keys(adjustment).forEach(key => {
          if (adjustment[key] === undefined || adjustment[key] === null) {
            delete adjustment[key];
          }
        });
        
        adjustments.push(adjustment)
      }
    } else {
      // 单个调整（非原料和半成品类型）
      const adjustment = {
        productId: adjustForm.productId,
        adjustAmount: adjustForm.adjustAmount,
        remark: adjustForm.remark,
        // 添加所有可能修改的属性
        name: adjustForm.productName,
        category: adjustForm.category,
        code: adjustForm.code,
        property: adjustForm.property,
        price: adjustForm.price,
        status: adjustForm.status,
        description: adjustForm.description,
        library_coding: adjustForm.library_coding,
        forbidden: adjustForm.forbidden,
        image: adjustForm.image
      }
      
      // 移除未定义或null值，但保留空字符串
      Object.keys(adjustment).forEach(key => {
        if (adjustment[key] === undefined || adjustment[key] === null) {
          delete adjustment[key];
        }
      });
      
      adjustments.push(adjustment)
    }

    // 执行调整
    for (const adjustment of adjustments) {
      // 先调整库存
      await adjustStock(adjustment)
      
      // 然后更新商品属性
      const productUpdate = {
        id: adjustment.productId,
        name: adjustment.name,
        category: adjustment.category,
        code: adjustment.code,
        property: adjustment.property,
        price: adjustment.price,
        status: adjustment.status,
        description: adjustment.description,
        library_coding: adjustment.library_coding,
        forbidden: adjustment.forbidden,
        image: adjustment.image
      }
      
      // 移除未定义或null值，但保留空字符串
      Object.keys(productUpdate).forEach(key => {
        if (productUpdate[key] === undefined || productUpdate[key] === null) {
          delete productUpdate[key]
        }
      })
      
      // 只有当有属性需要更新时才调用接口
      if (Object.keys(productUpdate).length > 1) { // 至少有id和一个其他属性
        await updateProduct(productUpdate)
      }
    }
    
    ElMessage.success('库存调整成功')
    adjustDialog.visible = false
    getList()
    getStatistics()
  } catch (error) {
    ElMessage.error('库存调整失败：' + (error.response?.data?.message || '未知错误'))
  }
}

// 添加批量调整供应商调整项
const addBatchSupplierAdjustment = (row) => {
  if (!row.supplierAdjustments) {
    row.supplierAdjustments = []
  }
  row.supplierAdjustments.push({
    supplierId: undefined,
    adjustAmount: 0
  })
}

// 删除批量调整供应商调整项
const removeBatchSupplierAdjustment = (row, index) => {
  if (row.supplierAdjustments && row.supplierAdjustments.length > 0) {
    row.supplierAdjustments.splice(index, 1)
  }
}

// 打开批量调整对话框
const openBatchAdjustDialog = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要调整的商品')
    return
  }

  try {
    for (const row of selectedRows.value) {
      // 初始化所有属性
      row.currentStock = row.stock
      // 初始化图片属性
      row.image = row.image || ''
      row.adjustAmount = 0
      row.remark = ''
      // 确保所有可编辑属性都已初始化
      if (row.property === undefined) row.property = 1
      if (row.status === undefined) row.status = 1
      if (row.forbidden === undefined) row.forbidden = 0
      if (row.price === undefined) row.price = 0
      row.supplierId = undefined
      // 初始化供应商调整列表
      row.supplierAdjustments = []

      // 获取供应商列表
      if ([1, 2].includes(row.property)) {
        const response = await getSuppliersByProductName(row.name)
        row.supplierList = response.data || []
      } else {
        row.supplierList = []
      }
    }

    batchDialog.visible = true
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败，请稍后重试')
  }
}

// 打开新增物品对话框
const openAddItemDialog = async () => {
  addItemDialog.visible = true
  try {
    // 并行获取供应商列表和产品名称列表
    const [suppliersResponse, productNamesResponse] = await Promise.all([
      getSuppliersByProductName(''),
      getProductNames()
    ])
    supplierList.value = suppliersResponse.data || []
    productNameList.value = productNamesResponse.data || []
  } catch (error) {
    console.error('获取数据失败:', error)
    supplierList.value = []
    productNameList.value = []
  }
}

// 处理物品选择变化
const handleItemChange = async (itemName) => {
  try {
    // 并行获取供应商列表和产品属性
    const [suppliersResponse, propertyResponse] = await Promise.all([
      getSuppliersByProductName(itemName),
      getPropertyByProductName(itemName)
    ])
    supplierList.value = suppliersResponse.data || []
    // 设置属性值
    if (propertyResponse.data !== null && propertyResponse.data !== undefined) {
      addItemForm.property = propertyResponse.data
    } else if (supplierList.value.length > 0) {
      // 兼容旧逻辑，如果属性获取失败，尝试从供应商列表中获取
      addItemForm.property = supplierList.value[0].property
    }
  } catch (error) {
    console.error('获取供应商列表或属性失败:', error)
    supplierList.value = []
  }
}

// 重置新增物品表单
const resetAddItemForm = () => {
  if (addItemFormRef.value) {
    addItemFormRef.value.clearValidate()
  }
  Object.assign(addItemForm, {
    name: '',
    category: '',
    code: '',
    property: 1,
    price: 0,
    stock: 0,
    supplierId: undefined,
    status: 0,
    description: '',
    library_coding: '',
    forbidden: 1,
    image: ''
  })
}

// 处理新增物品
const handleAddItem = async () => {
  if (!addItemFormRef.value) return
  if (!(await addItemFormRef.value.validate())) return

  try {
    await addProduct(addItemForm)
    ElMessage.success('新增物品成功')
    addItemDialog.visible = false
    resetAddItemForm()
    getList()
    getStatistics()
  } catch (error) {
    console.error('新增物品失败:', error)
    ElMessage.error('新增物品失败，请稍后重试')
  }
}

// 重置批量表单
const resetBatchForm = () => {
  selectedRows.value.forEach(row => {
    // 删除所有临时添加的属性
    delete row.currentStock
    delete row.adjustAmount
    delete row.remark
    delete row.supplierAdjustments
    delete row.supplierList
    // 注意：我们不删除其他属性，因为它们是原始数据的一部分
    // 但我们确保它们具有正确的类型
    if (row.property !== undefined) row.property = Number(row.property)
    if (row.status !== undefined) row.status = Number(row.status)
    if (row.forbidden !== undefined) row.forbidden = Number(row.forbidden)
    if (row.price !== undefined) row.price = Number(row.price)
  })
}

// 处理批量调整
const handleBatchAdjust = async () => {
  // 检查是否有任何修改
  const hasModifications = selectedRows.value.some(row => {
    // 对于原料和半成品，检查supplierAdjustments是否有修改
    if ([1, 2].includes(row.property)) {
      return (row.supplierAdjustments && row.supplierAdjustments.some(item => item.adjustAmount !== 0)) ||
             row.code !== undefined || 
             row.property !== undefined ||
             row.price !== undefined ||
             row.status !== undefined ||
             row.description !== undefined ||
             row.library_coding !== undefined ||
             row.forbidden !== undefined ||
             row.image !== undefined;
    } else {
      // 对于成品，检查adjustAmount是否有修改
      return row.adjustAmount !== 0 || 
             row.code !== undefined || 
             row.property !== undefined ||
             row.price !== undefined ||
             row.status !== undefined ||
             row.description !== undefined ||
             row.library_coding !== undefined ||
             row.forbidden !== undefined ||
             row.image !== undefined;
    }
  })
  
  if (!hasModifications) {
    ElMessage.warning('请对至少一项内容进行修改')
    return
  }

  // 验证：property为1或2的物品，只有在增加数量时才需要供应商调整项
  for (const row of selectedRows.value) {
    if ([1, 2].includes(row.property)) {
      // 检查是否有供应商调整项
      const hasSupplierAdjustments = row.supplierAdjustments && row.supplierAdjustments.length > 0;
      // 检查是否有增加数量的调整项
      const hasIncreaseAdjustments = hasSupplierAdjustments && row.supplierAdjustments.some(item => item.adjustAmount > 0);
      
      // 如果有增加数量的调整项，必须至少有一个供应商调整项
      if (hasIncreaseAdjustments && !hasSupplierAdjustments) {
        ElMessage.error(`商品 "${row.name}" 必须添加至少一个供应商调整项`)
        return
      }
      
      // 检查每个供应商调整项
      if (hasSupplierAdjustments) {
        for (const item of row.supplierAdjustments) {
          // 无论增加还是减少数量都需要选择供应商
          if (item.adjustAmount !== 0 && !item.supplierId) {
            ElMessage.error(`商品 "${row.name}" 的供应商调整项必须选择供应商`)
            return
          }
          if (item.adjustAmount === 0) {
            ElMessage.error(`商品 "${row.name}" 的供应商调整项调整数量不能为0`)
            return
          }
        }
      }
    }
  }

  // 准备所有修改的数据
  const allAdjustments = []
  
  for (const row of selectedRows.value) {
    if ([1, 2].includes(row.property)) {
      // 对于原料和半成品，为每个供应商调整项创建一个调整记录
      for (const item of row.supplierAdjustments) {
        const adjustment = {
          id: row.id,
          adjustAmount: item.adjustAmount,
          remark: row.remark || '',
          supplierId: item.supplierId,
          // 添加所有可能修改的属性
          code: row.code,
          name: row.name,
          category: row.category,
          property: row.property,
          price: row.price,
          status: row.status,
          description: row.description,
          library_coding: row.library_coding,
          forbidden: row.forbidden,
          image: row.image
        }
        
        // 移除未定义的值，避免覆盖原有数据
        Object.keys(adjustment).forEach(key => {
          if (adjustment[key] === undefined || adjustment[key] === null) {
            delete adjustment[key]
          }
        })
        
        allAdjustments.push(adjustment)
      }
    } else {
      // 对于成品，直接创建一个调整记录
      const adjustment = {
        id: row.id,
        adjustAmount: row.adjustAmount || 0,
        remark: row.remark || '',
        // 添加所有可能修改的属性
        code: row.code,
        name: row.name,
        category: row.category,
        property: row.property,
        price: row.price,
        status: row.status,
        description: row.description,
        library_coding: row.library_coding,
        forbidden: row.forbidden,
        image: row.image
      }
      
      // 移除未定义的值，避免覆盖原有数据
      Object.keys(adjustment).forEach(key => {
        if (adjustment[key] === undefined || adjustment[key] === null) {
          delete adjustment[key]
        }
      })
      
      allAdjustments.push(adjustment)
    }
  }

  batchLoading.value = true
  try {
    console.log('发送到后端的调整数据:', allAdjustments)
    const res = await batchAdjustStock(allAdjustments)
    ElMessage.success(`批量调整完成：成功 ${res.data.successCount} 项，失败 ${res.data.failCount} 项`)
    
    if (res.data.failCount > 0) {
      ElMessage.warning(`失败详情：${res.data.failDetails}`)
    }
    
    batchDialog.visible = false
    getList()
    getStatistics()
  } catch (error) {
    ElMessage.error('批量调整失败')
  } finally {
    batchLoading.value = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    const res = await getInventoryStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}



// 导出库存数据
const handleExport = () => {
  ElMessageBox.confirm('确定要导出当前库存数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      // 导入exportToExcel函数
      const { exportToExcel } = await import('@/utils/export')
      // 使用当前列表数据导出
      const data = inventoryList.value
      // 调用导出函数，生成Excel文件
      exportToExcel(data, '库存数据')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
    }
  }).catch(() => {})
}

// 图片上传相关
const uploadUrl = import.meta.env.VITE_API_URL + '/upload'

const beforeUpload = (file) => {
  const isImage = file && file.type && file.type.startsWith('image/')
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

const handleUploadSuccess = (response, row) => {
  const imagePath = response.data;
  console.log('上传成功后的图片路径:', imagePath);
  
  // 1. 更新inventoryList中的原始行对象
  const originalRow = inventoryList.value.find(item => item.id === row.id);
  if (originalRow) {
    originalRow.image = imagePath;
  }
  
  // 2. 更新selectedRows中的行对象（同一引用，直接更新）
  const selectedRow = selectedRows.value.find(item => item.id === row.id);
  if (selectedRow) {
    selectedRow.image = imagePath;
    console.log('selectedRows中的行更新后的image字段:', selectedRow.image);
  }
  
  // 打印调试信息
  console.log('上传成功后的原始行:', originalRow);
  console.log('selectedRows.value中的行:', selectedRows.value);
}

// 单个调整对话框图片上传成功处理
const handleUploadSuccessSingle = (response) => {
  const imagePath = response.data;
  console.log('单个调整对话框图片上传成功:', imagePath);
  adjustForm.image = imagePath;
}

// 新增物品对话框图片上传成功处理
const handleUploadSuccessAddItem = (response) => {
  const imagePath = response.data;
  console.log('新增物品对话框图片上传成功:', imagePath);
  addItemForm.image = imagePath;
}

// 获取图片URL
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  return import.meta.env.VITE_API_URL + imagePath
}

// 处理禁用状态变化
const handleForbiddenChange = async (row) => {
  const newStatus = row.forbidden
  try {
    const response = await updateProductForbiddenStatus(row.id, newStatus)
    // 由于使用模拟API，这里直接显示成功消息
    ElMessage.success(`商品${newStatus === 1 ? '启用' : '禁用'}成功`)
  } catch (error) {
    // 失败时恢复原状态
    row.forbidden = newStatus === 1 ? 0 : 1
    ElMessage.error(`操作失败：${error.message || '未知错误'}`)
  }
}

// 打开生产计划对话框
const openProductionDialog = async (row) => {
  // 重置表单
  resetProductionForm()
  
  // 设置默认值
  productionForm.productName = row.name
  productionForm.productProperty = row.property
  
  try {
    // 加载物料列表
    await loadMaterials()
    // 物料加载完成后查找对应的物料
    selectedMaterial.value = materials.value.find(m => m.materialName === row.name)
    if (selectedMaterial.value) {
      // 计算所需材料
      calculateRequiredMaterials()
      // 设置制作时间
      productionForm.productionTime = parseFloat((selectedMaterial.value.requiredTime * productionForm.quantity).toFixed(2))
    }
    // 生成计划名称
    generatePlanName()
    // 打开对话框
    productionDialog.visible = true
  } catch (error) {
    console.error('打开生产计划对话框失败:', error)
    ElMessage.error('打开生产计划对话框失败: ' + error.message)
  }
}

// 重置生产计划表单
const resetProductionForm = () => {
  if (productionFormRef.value) {
    productionFormRef.value.resetFields()
  }
  productionForm.id = ''
  productionForm.productName = ''
  productionForm.quantity = 1
  productionForm.productProperty = 3 // 默认设为成品
  productionForm.planName = ''
  productionForm.requiredMaterials = ''
  productionForm.productionTime = 0
  productionForm.startTime = ''
  productionForm.expectedEndTime = ''
  productionForm.status = 1
  selectedMaterial.value = null
}

// 加载物料列表
const loadMaterials = () => {
  return new Promise((resolve, reject) => {
    getMaterials({ page: 1, pageSize: 100 }).then(response => {
      if (response.code === 200) {
        // 加载所有类型的物料
        const allMaterials = response.data.records || [];
        
        // 只保留物料类型为2(半成品)和3(成品)的物料用于下拉选择
        materials.value = allMaterials.filter(material => 
          material.materialType === 2 || material.materialType === 3
        ) || []
        
        // 建立所有物料ID到名称的映射，以便正确显示所需材料
        materialMap.value = {}
        allMaterials.forEach(material => {
          materialMap.value[material.id] = material.materialName
        })
        
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

// 处理产物选择变化
const handleProductChange = (value) => {
  if (!value) {
    selectedMaterial.value = null
    productionForm.requiredMaterials = ''
    productionForm.productionTime = 0
    generatePlanName()
    return
  }
  
  // 查找选中的物料
  selectedMaterial.value = materials.value.find(m => m.materialName === value)
  if (selectedMaterial.value) {
    // 计算所需材料（乘以数量）
    calculateRequiredMaterials()
    // 设置制作时间（乘以数量），保留两位小数
    productionForm.productionTime = parseFloat((selectedMaterial.value.requiredTime * productionForm.quantity).toFixed(2))
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
  productionForm.productionTime = parseFloat((selectedMaterial.value.requiredTime * value).toFixed(2))
  // 重新生成计划名称
  generatePlanName()
  // 重新计算预计完成时间
  calculateExpectedEndTime()
}

// 计算所需材料
const calculateRequiredMaterials = () => {
  if (!selectedMaterial.value || !productionForm.quantity) return
  
  const baseMaterials = selectedMaterial.value.requiredMaterials
  if (!baseMaterials) {
    productionForm.requiredMaterials = ''
    return
  }
  
  // 解析基础材料并乘以数量
  const materialsArray = baseMaterials.split('/')
  const calculatedMaterials = materialsArray.map(material => {
    const [materialId, amount] = material.split('*')
    if (!materialId || !amount) return material
    return `${materialId}*${parseFloat(amount) * productionForm.quantity}`
  })
  
  productionForm.requiredMaterials = calculatedMaterials.join('/')
}

// 生成计划名称
const generatePlanName = () => {
  if (!productionForm.productName || !productionForm.quantity) {
    productionForm.planName = ''
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
  
  productionForm.planName = `${productionForm.productName}-${productionForm.quantity}-${dateStr}`
}

// 计算预计完成时间
const calculateExpectedEndTime = () => {
  if (!productionForm.startTime || !productionForm.productionTime) {
    productionForm.expectedEndTime = ''
    return
  }
  
  const startTime = new Date(productionForm.startTime)
  const endTime = new Date(startTime)
  // 确保productionTime是数字类型
  const productionTime = Number(productionForm.productionTime)
  
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
  
  productionForm.expectedEndTime = `${year}-${month}-${day} ${formattedHours}:${formattedMinutes}:${seconds}`
}

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

// 提交生产计划表单
const submitProductionForm = () => {
  if (!productionFormRef.value) return
  
  productionFormRef.value.validate((valid) => {
    if (valid) {
        const form = { ...productionForm }
        
        // 将前端的quantity字段映射到后端的productQuantity字段
        form.productQuantity = form.quantity
        // 移除前端不需要传递给后端的quantity字段
        delete form.quantity
        
        // 新增生产计划
        createProductionPlan(form).then(response => {
          if (response.code === 200) {
            ElMessage.success('新增生产计划成功')
            productionDialog.visible = false
            resetProductionForm()
          } else {
            ElMessage.error('新增生产计划失败: ' + response.message)
          }
        }).catch(error => {
          ElMessage.error('新增生产计划失败: ' + error.message)
        })
    }
  })
}

// 生命周期
onMounted(() => {
  getList()
  getStatistics()
})
</script>

<style lang="scss" scoped>
.low-stock-checkbox {
  :deep(.el-checkbox__label) {
    font-size: 14px;
    color: #e6a23c !important;
  }
}

/* 库存日志对话框样式 */
.log-content-container {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
}

.log-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #606266;
  font-size: 14px;
}

.log-loading .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.log-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  font-size: 14px;
}

.log-empty .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.log-content {
  width: fit-content; /* 根据内容自动调整宽度 */
  min-width: 100%; /* 最小宽度为容器宽度 */
  padding: 15px;
  background-color: #48494c;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #ffffff;
  white-space: pre; /* 保留原始格式，不自动换行 */
  overflow-x: auto; /* 显示水平滚动条 */
  overflow-y: hidden; /* 隐藏垂直滚动条（由容器控制） */
}

.log-content-container {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
  overflow-x: auto; /* 显示水平滚动条 */
}

/* 为搜索框的内部div设置背景颜色和白色边框 */
:deep(.custom-bg-input .el-input__wrapper),
:deep(.custom-bg-input .el-input-number__wrapper),
:deep(.custom-bg-input .el-textarea__wrapper) {
  background-color: #48494c !important;
  --el-input-bg-color: #48494c;
  border-color: white !important;
}

/* 搜索框聚焦时的白色边框 */
:deep(.custom-bg-input .el-input__wrapper:focus-within),
:deep(.custom-bg-input .el-input__wrapper.is-focus),
:deep(.custom-bg-input .el-input__wrapper:focus),
:deep(.custom-bg-input.is-focus .el-input__wrapper),
:deep(.custom-bg-input .el-input-number__wrapper:focus-within),
:deep(.custom-bg-input .el-input-number__wrapper.is-focus),
:deep(.custom-bg-input .el-input-number__wrapper:focus),
:deep(.custom-bg-input.is-focus .el-input-number__wrapper),
:deep(.custom-bg-input .el-textarea__wrapper:focus-within),
:deep(.custom-bg-input .el-textarea__wrapper.is-focus),
:deep(.custom-bg-input .el-textarea__wrapper:focus),
:deep(.custom-bg-input.is-focus .el-textarea__wrapper) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset;
}

/* 确保内部textarea聚焦时边框白色 */
:deep(.custom-bg-input .el-textarea__inner:focus),
:deep(.custom-bg-input .el-textarea__wrapper .el-textarea__inner:focus),
:deep(.custom-bg-input.is-focus .el-textarea__wrapper .el-textarea__inner:focus) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset !important;
}

/* 确保只读textarea在鼠标点击时边框也为白色 */
:deep(.custom-bg-input .el-textarea.is-readonly .el-textarea__wrapper),
:deep(.custom-bg-input .el-textarea.is-readonly:focus-within .el-textarea__wrapper),
:deep(.custom-bg-input .el-textarea.is-readonly .el-textarea__wrapper.is-focus),
:deep(.custom-bg-input .el-textarea.is-readonly .el-textarea__wrapper:focus) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset;
}

:deep(.custom-bg-input .el-textarea.is-readonly .el-textarea__inner),
:deep(.custom-bg-input .el-textarea.is-readonly .el-textarea__inner:focus) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset !important;
}

/* 为下拉框的内部div设置背景颜色和聚焦边框 */
:deep(.custom-bg-select .el-select__wrapper) {
  background-color: #48494c !important;
  --el-select-bg-color: #48494c;
}

/* 下拉框聚焦时的白色边框 */
:deep(.custom-bg-select .el-select__wrapper:focus-within),
:deep(.custom-bg-select .el-select__wrapper.is-focus),
:deep(.custom-bg-select .el-select__wrapper.is-active),
:deep(.custom-bg-select .el-select__wrapper.is-open),
:deep(.custom-bg-select .el-select__wrapper:focus) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset;
}

/* 确保输入文字可见 */
:deep(.custom-bg-input .el-input__inner),
:deep(.custom-bg-input .el-input-number__inner),
:deep(.custom-bg-input .el-textarea__inner),
:deep(.custom-bg-select .el-select__input) {
  color: white !important;
  background-color: #48494c !important;
}

/* 确保占位符可见 - 修改为白色 */
:deep(.custom-bg-input .el-input__placeholder),
:deep(.custom-bg-input .el-input-number__placeholder),
:deep(.custom-bg-input .el-textarea__placeholder),
:deep(.custom-bg-select .el-select__placeholder) {
  color: white !important;
}

/* 设置下拉菜单文字颜色为白色 */
:deep(.custom-bg-select .el-select__selection span),
:deep(.custom-bg-select .el-select__selected-item span) {
  color: white !important;
}

/* 确保下拉菜单输入框文字为白色 */
:deep(.custom-bg-select .el-select__input) {
  color: white !important;
}

/* 下拉菜单选项悬停时的背景颜色 - 使用:global提高优先级 */
:global(.el-select-dropdown) {
  --el-select-dropdown-item-hover-bg-color: #5A5B5E !important;
}

:global(.el-select-dropdown .el-select-dropdown__item:hover),
:global(.el-select-dropdown__item.is-hover),
:global(.el-select-dropdown__item:hover:not(.is-disabled)) {
  background-color: #5A5B5E !important;
  color: white !important;
  --el-select-dropdown-item-hover-color: white !important;
}

.inventory-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.stat-content {
  width: 100%;
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-card.low-stock .stat-number {
  color: #e6a23c;
}

.stat-card.zero-stock .stat-number {
  color: #f56c6c;
}

.batch-operations {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 40px;
  height: 40px;
  margin-right: 10px;
  border-radius: 4px;
  display: inline-block;
  vertical-align: middle;
}

.product-name {
  display: inline-block;
  vertical-align: middle;
  flex: 1;
}

.stock-low {
  color: #e6a23c;
  font-weight: bold;
}

.stock-zero {
  color: #f56c6c;
  font-weight: bold;
}

.stock-normal {
  color: #67c23a;
}

.description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.adjust-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.batch-tip {
  margin-bottom: 15px;
  color: #606266;
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

/* 顶部操作容器背景样式 */
.top-operations-container {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  color: white;
}

/* 确保内部元素文字颜色正确 */
.top-operations-container .el-input__inner {
  color: #333;
}

.top-operations-container .el-checkbox__label {
  color: white;
}

/* 圆角按钮样式 */
.rounded-button {
  background-color: #ffffff !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  transition: all 0.3s ease !important;
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

/* 导出按钮文字颜色设置 */
:global(.el-button.export-button span) {
  color: #67C23A !important;
}

/* 表格渐变背景支持 - 确保所有表格内部容器透明 */
:global(.el-card__body) {
  background: transparent !important;
}

/* 日期选择器面板背景颜色 */
:deep(.el-picker-panel) {
  background-color: #48494c !important;
}

/* 日期选择器面板底部背景颜色 */
:deep(.el-picker-panel__footer) {
  background-color: #48494c !important;
}

/* 日期选择器面板头部背景颜色 */
:deep(.el-picker-panel__header) {
  background-color: #48494c !important;
}

/* 日期选择器面板内容背景颜色 */
:deep(.el-picker-panel__content) {
  background-color: #48494c !important;
}

/* 日期选择器面板底部区域 - 包含Now和OK按钮的区域 */
:deep(.el-picker-panel .el-picker-panel__footer) {
  background-color: #48494c !important;
  border-top-color: #48494c !important;
}

/* 日期选择器面板按钮样式 */
:deep(.el-picker-panel__link-btn) {
  color: white !important;
  background-color: #48494c !important;
  border-color: white !important;
}

/* 日期选择器面板按钮悬停样式 */
:deep(.el-picker-panel__link-btn:hover) {
  background-color: #5a5b5e !important;
  border-color: white !important;
  color: white !important;
}

/* 日期选择器面板按钮激活样式 */
:deep(.el-picker-panel__link-btn:active) {
  background-color: #3a3b3e !important;
  border-color: white !important;
  color: white !important;
}

/* 日期选择器面板文字颜色 */
:deep(.el-picker-panel *),
:deep(.el-date-picker__header-label),
:deep(.el-date-table th),
:deep(.el-date-table td),
:deep(.el-time-spinner__item) {
  color: white !important;
}

/* 日期选择器单元格鼠标悬停样式 - 确保td内所有元素背景色一致 */
:global(.el-date-table td:hover),
:global(.el-date-table td:hover .el-date-table-cell),
:global(.el-date-table td:hover .el-date-table-cell__text) {
  background-color: #5A5B5E !important;
}

/* 确保当鼠标在td区域内移动时，内部所有元素保持相同背景色 */
:global(.el-date-table td:hover > div),
:global(.el-date-table td:hover > div > div) {
  background-color: #5A5B5E !important;
}

/* 点击选中状态 */
:global(.el-date-table-cell.is-hover),
:global(.el-date-table-cell.is-hover .el-date-table-cell__text) {
  background-color: #5A5B5E !important;
}

/* 日期选择器面板边框颜色 */
:deep(.el-picker-panel),
:deep(.el-picker-panel__header),
:deep(.el-picker-panel__footer),
:deep(.el-picker-panel__content) {
  border-color: white !important;
}

/* 确保日期选择器所有部分都是深色背景 */
:deep(.el-date-picker) {
  background-color: #48494c !important;
}

:deep(.el-picker-panel__body-wrapper) {
  background-color: #48494c !important;
}

/* 确保底部区域没有白色背景 - 最高优先级 */
:global(.el-picker-panel__footer),
:global(.el-date-picker .el-picker-panel__footer),
:global(.el-time-picker .el-picker-panel__footer) {
  background-color: #48494c !important;
  border-top-color: white !important;
  border-top: 1px solid white !important;
}

/* 确保底部按钮区域也是深色 */
:global(.el-picker-panel__footer) {
  background-color: #48494c !important;
  border-top: 1px solid #48494c !important;
}

/* 时间选择器底部区域背景颜色 - 最高优先级 */
:global(.el-time-panel .el-time-panel__footer),
:global(.el-picker-panel__content .el-time-panel__footer) {
  background-color: #48494c !important;
  border-top: 1px solid white !important;
}

/* 时间选择器面板背景颜色 */
:global(.el-time-panel) {
  background-color: #48494c !important;
}

/* 时间选择器内容区域背景颜色 */
:global(.el-time-spinner) {
  background-color: #48494c !important;
}

/* 时间选择器按钮样式 */
:global(.el-time-panel__btn) {
  color: white !important;
  background-color: #48494c !important;
}

/* 确保时间选择器所有子元素都是深色背景 */
:global(.el-time-panel *),
:global(.el-picker-panel .el-time-panel *),
:global(.el-picker-panel__content .el-time-panel *) {
  background-color: #48494c !important;
}

/* 时间选择器数字项目悬停样式 - 放在通用规则之后确保优先级更高 */
:global(.el-time-spinner__item:hover),
:global(.el-time-spinner__item.is-hover) {
  background-color: #5A5B5E !important;
}

/* 最高优先级：直接针对时间选择器底部区域div */
:global(.el-time-panel__footer),
:global(.el-picker-panel .el-time-panel__footer),
:global(.el-picker-panel__content .el-time-panel__footer),
:global(.el-time-panel .el-time-panel__footer div),
:global(.el-picker-panel .el-time-panel__footer div),
:global(.el-picker-panel__content .el-time-panel__footer div) {
  background-color: #48494c !important;
  background-image: none !important;
}

/* 终极解决方案：针对所有时间选择器相关元素 */
:global(.el-picker-panel__content),
:global(.el-picker-panel__content *),
:global(.el-time-panel),
:global(.el-time-panel *),
:global(.el-time-panel__footer),
:global(.el-time-panel__footer *),
:global(.el-time-spinner),
:global(.el-time-spinner *) {
  background-color: #48494c !important;
  background-image: none !important;
}

/* 时间选择器数字项目悬停样式 - 放在终极解决方案之后确保最高优先级 */
:global(.el-time-spinner__item:hover),
:global(.el-time-spinner__item.is-hover) {
  background-color: #5A5B5E !important;
}

/* 时间选择器滚动条完整样式 - 滑块和导轨 */
:global(.el-time-spinner .el-scrollbar__bar) {
  background: transparent !important;
}

:global(.el-time-spinner .el-scrollbar__bar .el-scrollbar__track) {
  background-color: #48494c !important; /* 默认与背景色一致 */
}

:global(.el-time-spinner .el-scrollbar__bar .el-scrollbar__track:hover),
:global(.el-time-spinner:hover .el-scrollbar__bar .el-scrollbar__track),
:global(.el-time-spinner__item:hover ~ .el-scrollbar__bar .el-scrollbar__track) {
  background-color: #5A5B5E !important; /* 悬停时与数字项背景色一致 */
}

:global(.el-time-spinner .el-scrollbar__thumb),
:global(.el-time-spinner ::-webkit-scrollbar-thumb) {
  background-color: #A4A3A6 !important;
}

/* 生产计划对话框所需材料textarea背景颜色 */
:global(.custom-dialog .el-textarea__inner) {
  background-color: #48494c !important;
  color: white !important;
}

:global(.el-table) {
  background: transparent !important;
}

:global(.el-table__inner-wrapper) {
  background: transparent !important;
}

:global(.el-table__header-wrapper) {
  background: transparent !important;
}

:global(.el-table__body-wrapper) {
  background: transparent !important;
}

:global(.el-table__footer-wrapper) {
  background: transparent !important;
}

:global(.el-table__header) {
  background: transparent !important;
}

:global(.el-table__body) {
  background: transparent !important;
}

:global(.el-table__body-wrapper .el-table__body .el-table__cell) {
  color: white !important;
}

:global(.el-table__row) {
  background: transparent !important;
}

:global(.el-table__cell) {
  background-color: transparent !important;
}

:global(.el-table__header-wrapper .el-table__header .el-table__cell) {
  background-color: transparent !important;
  color: white !important;
}

/* 表头行背景渐变 */
:global(.el-table__header-wrapper thead tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}
/* 表格渐变背景支持 - 确保所有表格内部容器透明 */
:global(.el-card__body) {
  background: transparent !important;
}

:global(.el-table) {
  background: transparent !important;
}

:global(.el-table__inner-wrapper) {
  background: transparent !important;
}

:global(.el-table__header-wrapper) {
  background: transparent !important;
}

:global(.el-table__body-wrapper) {
  background: transparent !important;
}

:global(.el-table__footer-wrapper) {
  background: transparent !important;
}

:global(.el-table__header) {
  background: transparent !important;
}

:global(.el-table__body) {
  background: transparent !important;
}

:global(.el-table__row) {
  background: transparent !important;
}

:global(.el-table__cell) {
  background-color: transparent !important;
}

/* 表格渐变背景支持 - 确保所有表格内部容器透明 */
:global(.el-card__body) {
  background: transparent !important;
}

:global(.el-table) {
  background: transparent !important;
}

:global(.el-table__inner-wrapper) {
  background: transparent !important;
}

:global(.el-table__header-wrapper) {
  background: transparent !important;
}

:global(.el-table__body-wrapper) {
  background: transparent !important;
}

:global(.el-table__footer-wrapper) {
  background: transparent !important;
}

:global(.el-table__header) {
  background: transparent !important;
}

:global(.el-table__body) {
  background: transparent !important;
}

:global(.el-table__row) {
  background: transparent !important;
}

:global(.el-table__cell) {
  background-color: transparent !important;
}

:global(.el-table__header-wrapper .el-table__header .el-table__cell) {
  background-color: transparent !important;
  color: white !important;
}

/* 复选框标签文字颜色 */
:global(.el-checkbox__label) {
  color: white !important;
}

/* 固定右列“操作”列背景渐变 */
:global(.el-table-fixed-column--right.is-first-column.el-table__cell) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
}

:global(.el-pagination) {
  background: transparent !important;
  color: white !important;
}

:global(.el-pagination button) {
  color: white !important;
  background-color: #48494c !important;
  margin: 0 2px;
  border: 1px solid white !important;
}

:global(.el-pagination .btn-prev) {
  margin-right: 8px;
}

:global(.el-pagination .btn-next) {
  margin-left: 8px;
}

:global(.el-pagination span) {
  color: white !important;
}

:global(.el-pagination .el-pager li) {
  background-color: #48494c !important;
  margin: 0 4px;
  color: white !important;
  border: 1px solid white !important;
}

:global(.el-pagination .el-pager li.is-active) {
  background-color: #48494c !important;
}

:global(.el-pagination .el-select__wrapper) {
  background-color: #48494c !important;
  --el-select-bg-color: #48494c;
}

:global(.el-pagination .el-select__wrapper:focus-within),
:global(.el-pagination .el-select__wrapper.is-focus),
:global(.el-pagination .el-select__wrapper.is-active),
:global(.el-pagination .el-select__wrapper.is-open),
:global(.el-pagination .el-select__wrapper:focus) {
  border-color: white !important;
  box-shadow: 0 0 0 1px white inset;
}

:global(.el-pagination .el-select__input) {
  color: white !important;
  background-color: #48494c !important;
}

:global(.el-pagination .el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid white !important;
}

:global(.el-pagination .el-input__wrapper:active),
:global(.el-pagination .el-input__wrapper:focus),
:global(.el-pagination .el-input__wrapper.is-focus) {
  border-color: white !important;
  box-shadow: none !important;
}

:global(.el-pagination .el-input__inner) {
  color: white !important;
  background-color: #48494c !important;
  border: none !important;
}

:global(.el-pagination .el-select__placeholder) {
  color: rgba(255, 255, 255, 0.85) !important;
}

:global(.el-table__column-filter-trigger) {
  color: white !important;
}
</style>
<style lang="scss">
.custom-dialog.el-dialog {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  border: none;
}
.custom-dialog {
  .el-dialog__header {
    background: #464e58;
    border-bottom: none;
  }
  .el-dialog__title {
    color: white;
  }
  .el-dialog__body {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    min-height: 400px;
    color: white;
  }
  .el-dialog__footer {
    background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
    border-top: none;
  }
  /* Make table transparent */
  .el-table {
    background-color: transparent;
  }
  /* Make table rows and cells transparent */
  .el-table__row, .el-table__cell {
    background-color: transparent !important;
  }
  /* Ensure table text is white */
  .el-table__cell {
    color: white !important;
  }
  /* Ensure input and select text is white */
  .el-input__inner, .el-select__input {
    color: white !important;
  }
  /* Set background color and border for input and select wrappers */
  .el-input__wrapper, .el-select__wrapper {
    background-color: #48494c !important;
    border: 1px solid white !important;
  }
  /* Ensure border remains white on all interaction states */
  .custom-dialog .el-input__wrapper.is-focus, .custom-dialog .el-select__wrapper.is-focus,
  .custom-dialog .el-select__wrapper.is-active, .custom-dialog .el-select__wrapper.is-open,
  .custom-dialog .el-input__wrapper:focus, .custom-dialog .el-select__wrapper:focus {
    border: 1px solid white !important;
    box-shadow: none !important;
    outline: none !important;
  }
  /* Ensure switch is visible */
  .el-switch__core {
    background-color: #c0c0c0;
  }
  .el-switch.is-checked .el-switch__core {
    background-color: #67c23a;
  }
  /* 设置图片上传图标背景颜色和白色边框 */
  .avatar-uploader-icon {
    background-color: #48494c !important;
    border: 1.8px solid white !important;
    border-radius: 4px !important;
  }
  /* Ensure batch tip text is white */
  .batch-tip {
    color: white;
  }
  /* Ensure dialog title is white */
  .el-dialog__title {
    color: white !important;
  }
  /* Ensure form labels, error messages, and input text are white */
  .el-form-item__label,
  .el-input__inner,
  .el-input-number {
    color: white !important;
  }
  /* Ensure error messages are red */
  .el-form-item__error {
    color: #F56C6C !important;
  }
  /* Ensure select text and options are white */
  .el-select__input,
  .el-option {
    color: white !important;
  }
  /* Ensure select placeholder is white */
  .el-select__placeholder {
    color: rgb(255, 255, 255) !important;
  }
  /* Ensure select selected value is white */
  .el-select__selection-item {
    color: white !important;
  }
  /* Ensure buttons in dialog have white text */
  .custom-dialog .el-button {
    color: white !important;
  }
  /* Set table cell text color to white in custom dialog except for stock level colors */
  :deep(.custom-dialog .el-table__cell .cell) {
    color: white !important;
  }
  /* Ensure stock level colors are applied correctly */
  :deep(.custom-dialog .stock-low) {
    color: #e6a23c !important;
    font-weight: bold;
  }
  :deep(.custom-dialog .stock-zero) {
    color: #f56c6c !important;
    font-weight: bold;
  }
  :deep(.custom-dialog .stock-normal) {
    color: #67c23a !important;
  }
  /* 设置图片上传图标背景颜色 */
  .avatar-uploader-icon {
    background-color: #48494c !important;
  }
}

/* 供应商调整列表样式 */
.supplier-adjustments-container {
  margin-top: 10px;
}

.supplier-adjustment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
}

.supplier-adjustment-item {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #48494c;
  border: 1px solid white;
  border-radius: 4px;
}

.no-supplier-adjustments {
  text-align: center;
  color: #999;
  padding: 20px;
  font-style: italic;
}
</style>
