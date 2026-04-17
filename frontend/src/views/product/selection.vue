<template>
  <div class="product-selection-container">
    <el-card class="main-container-card" style="background-color: transparent !important; background: transparent !important;">
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择商品分类" popper-class="category-select-dropdown">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="rounded-button">查询</el-button>
            <el-button @click="handleReset" class="rounded-button">重置</el-button>
            <el-button @click="goToCart" class="rounded-button">
              <el-icon><ShoppingCart /></el-icon>购物车
              <el-badge v-if="cartTotal > 0" :value="cartTotal" class="cart-badge" />
            </el-button>
            <el-button @click="openOrderDialog" class="rounded-button">
              <el-icon><Tickets /></el-icon>我的订单
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <!-- 商品列表 -->
      <div class="product-list">
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in products" :key="product.id">
            <el-card class="product-card" shadow="hover">
              <template #header>
                <div class="product-card-header">
                  <h4>{{ product.name }}</h4>
                  <span class="price">¥{{ product.price }}</span>
                </div>
              </template>
              <div class="product-image">
                <el-image
                  :src="product.image ? apiUrl + product.image : '/images/product-icon.svg'"
                  :fit="'contain'"
                  style="width: 100%; height: 150px"
                ></el-image>
              </div>
              <div class="product-info">
                <p class="description">{{ product.description || '暂无描述' }}</p>
                <div class="stock-info">
                  <span>库存: {{ product.stock || 0 }}</span>
                </div>
              </div>
              <div class="product-actions">
                <el-input-number
                  v-model="product.quantity"
                  :min="1"
                  :max="product.stock || 1"
                  :step="1"
                  size="small"
                ></el-input-number>
                <div class="button-group">
                  <el-button type="primary" size="small" @click="addToCart(product)">
                    <el-icon><ShoppingCart /></el-icon>
                  </el-button>
                  <el-button size="small" @click="viewDetail(product.id)">
                    <el-icon><InfoFilled /></el-icon>详情
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[12, 24, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 购物车对话框 -->
    <el-dialog
      v-model="cartDialogVisible"
      title="购物车"
      width="900px"
      left
      class="cart-dialog"
    >
      <div v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车为空" />
      </div>
      <div v-else class="cart-content">
        <el-table :data="cartItems" stripe style="width: 100%">
          <el-table-column prop="name" label="商品名称" min-width="200" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.quantity"
                :min="1"
                :max="scope.row.stock || 1"
                :step="1"
                size="small"
                @change="updateCartItemQuantity(scope.row)"
              ></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="scope">
              ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeCartItem(scope.row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 订单信息表单 -->
        <div class="order-info-form">
          <el-form :model="orderForm" label-width="100px" class="order-form">
            <el-form-item label="订单编号" required>
              <el-input v-model="orderForm.orderNo" placeholder="系统自动生成" readonly />
            </el-form-item>
            <el-form-item label="收货人" required>
              <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" required>
              <el-input v-model="orderForm.receiverPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="收货地址" required>
              <el-input v-model="orderForm.receiverAddress" placeholder="请输入收货地址" />
            </el-form-item>
          </el-form>
        </div>
        
        <div class="cart-footer">
          <div class="total-info">
            <span>总计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            <span>（共 {{ cartTotal }} 件商品）</span>
          </div>
          <div class="cart-actions">
            <el-button @click="cartDialogVisible = false">继续购物</el-button>
            <el-button type="primary" @click="submitOrder">提交订单</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 我的订单对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="我的订单"
      width="90%"
      :before-close="closeOrderDialog"
      class="order-dialog"
    >
      <div class="order-dialog-content">
        <el-table
          v-loading="orderLoading"
          :data="orderList"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column label="订单编号" prop="orderNo" width="180" />
          <el-table-column label="收货人" prop="consignee" width="120" />
          <el-table-column label="联系电话" prop="phone" width="120" />
          <el-table-column label="总金额" prop="totalAmount" width="120">
            <template #default="scope">
              ￥{{ scope.row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="订单状态" prop="status" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="180" />
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <el-button 
                type="primary" 
                link 
                @click="handleOrderDetail(scope.row)"
              >
                详情
              </el-button>
              <el-button 
                v-if="scope.row.status === 0"
                type="success" 
                link 
                @click="handleOrderPay(scope.row)"
              >
                支付
              </el-button>
              <el-button 
                v-if="scope.row.status === 0"
                type="danger" 
                link 
                @click="handleOrderCancel(scope.row)"
              >
                取消
              </el-button>
              <el-button 
                v-if="scope.row.status === 2"
                type="success" 
                link 
                @click="handleOrderComplete(scope.row)"
              >
                完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 订单分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="orderQueryParams.pageNum"
            v-model:page-size="orderQueryParams.pageSize"
            :total="orderTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleOrderSizeChange"
            @current-change="handleOrderCurrentChange"
            popper-class="order-page-size-dropdown"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { Plus, View, ShoppingCart } from '@element-plus/icons-vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElBadge, ElEmpty, ElForm, ElFormItem, ElInput, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProductList, getProductCategories } from '@/api/product'
import { addOrder, getOrderList, getOrderDetail, payOrder, cancelOrder, completeOrder } from '@/api/order'

export default {
  name: 'ProductSelection',
  components: {
    Plus,
    View,
    ShoppingCart,
    ElBadge,
    ElEmpty,
    ElForm,
    ElFormItem,
    ElInput
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    // API基础URL
    const apiUrl = import.meta.env.VITE_API_URL
    // 搜索表单
    const searchForm = reactive({
      name: '',
      categoryId: ''
    })
    // 商品分类
    const categories = ref([])
    // 商品列表
    const products = ref([])
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 12,
      total: 0
    })
    
    // 购物车相关数据
    const cartDialogVisible = ref(false)
    const cartItems = ref([])
    
    // 订单表单数据
    const orderForm = reactive({
      orderNo: '',
      receiverName: '',
      receiverPhone: '',
      receiverAddress: ''
    })

    // 我的订单相关数据
    const orderDialogVisible = ref(false)
    const orderList = ref([])
    const orderLoading = ref(false)
    const orderTotal = ref(0)
    const orderQueryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      userId: userStore.userInfo.id || 0 // 使用当前登录用户ID
    })

    // 获取商品分类
    const fetchCategories = async () => {
      try {
        const response = await getProductCategories()
        if (response.code === 200) {
          // 将后端返回的字符串数组转换为前端需要的格式 { id, name }
          categories.value = response.data.map((category, index) => ({
            id: index + 1,
            name: category
          }))
        } else {
          ElMessage.error('获取商品分类失败')
        }
      } catch (error) {
        ElMessage.error('获取商品分类失败')
      }
    }

    // 获取商品列表
    const fetchProducts = async () => {
      try {
        const params = {
          pageNum: pagination.currentPage,
          pageSize: pagination.pageSize,
          name: searchForm.name,
          categoryId: searchForm.categoryId ? parseInt(searchForm.categoryId) : null
        }
        const response = await getProductList(params)
        if (response.code === 200) {
          // 为每个商品添加quantity属性，默认值为1
          products.value = response.data.records.map(product => ({
            ...product,
            quantity: 1
          }))
          pagination.total = response.data.total
        } else {
          ElMessage.error('获取商品列表失败')
        }
      } catch (error) {
        ElMessage.error('获取商品列表失败')
      }
    }

    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      fetchProducts()
    }

    // 重置
    const handleReset = () => {
      Object.assign(searchForm, {
        name: '',
        categoryId: ''
      })
      handleSearch()
    }

    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchProducts()
    }

    // 当前页变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      fetchProducts()
    }

    // 购物车商品总数
    const cartTotal = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.quantity, 0)
    })
    
    // 购物车商品总价
    const totalPrice = computed(() => {
      return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
    })
    
    // 加入购物车
    const addToCart = (product) => {
      // 检查商品是否已在购物车中
      const existingItemIndex = cartItems.value.findIndex(item => item.id === product.id)
      
      if (existingItemIndex >= 0) {
        // 商品已存在，更新数量
        const newQuantity = cartItems.value[existingItemIndex].quantity + product.quantity
        const maxQuantity = product.stock || 1
        
        if (newQuantity > maxQuantity) {
          ElMessage.warning(`该商品库存不足，最多只能购买 ${maxQuantity} 件`)
          cartItems.value[existingItemIndex].quantity = maxQuantity
        } else {
          cartItems.value[existingItemIndex].quantity = newQuantity
          ElMessage.success(`已成功更新购物车，当前数量：${cartItems.value[existingItemIndex].quantity} 件`)
        }
      } else {
        // 商品不存在，添加到购物车
        cartItems.value.push({
          ...product,
          quantity: product.quantity
        })
        ElMessage.success(`已成功加入 ${product.quantity} 件商品到购物车`)
      }
    }

    // 更新购物车商品数量
    const updateCartItemQuantity = (item) => {
      // 确保数量不超过库存
      if (item.quantity > item.stock) {
        item.quantity = item.stock
        ElMessage.warning(`该商品库存不足，最多只能购买 ${item.stock} 件`)
      }
    }
    
    // 删除购物车商品
    const removeCartItem = (productId) => {
      cartItems.value = cartItems.value.filter(item => item.id !== productId)
      ElMessage.success('商品已从购物车删除')
    }
    
    // 生成唯一订单编号
    const generateOrderNo = () => {
      // 使用时间戳 + 随机数生成唯一订单编号
      const timestamp = new Date().getTime()
      const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
      const orderNo = `ORD-${timestamp}-${random}`
      return orderNo
    }

    // 前往购物车（打开对话框）
    const goToCart = () => {
      // 生成唯一订单编号
      orderForm.orderNo = generateOrderNo()
      cartDialogVisible.value = true
    }

    // 提交订单
    const submitOrder = async () => {
      if (cartItems.value.length === 0) {
        ElMessage.warning('购物车为空，无法提交订单')
        return
      }
      
      // 验证订单信息
      if (!orderForm.receiverName) {
        ElMessage.warning('请输入收货人姓名')
        return
      }
      if (!orderForm.receiverPhone) {
        ElMessage.warning('请输入联系电话')
        return
      }
      if (!orderForm.receiverAddress) {
        ElMessage.warning('请输入收货地址')
        return
      }
      
      try {
        // 构造订单数据
        const orderData = {
          userId: userStore.userInfo.id, // 使用当前登录用户ID
          orderNo: orderForm.orderNo,
          consignee: orderForm.receiverName,
          phone: orderForm.receiverPhone,
          address: orderForm.receiverAddress,
          items: cartItems.value.map(item => ({
            productId: item.id,
            quantity: item.quantity,
            price: item.price
          }))
        }
        
        // 调用API提交订单
        await addOrder(orderData)
        
        ElMessage.success('订单提交成功！')
        cartItems.value = [] // 清空购物车
        cartDialogVisible.value = false // 关闭对话框
        
        // 重置订单表单
        Object.assign(orderForm, {
          orderNo: '',
          receiverName: '',
          receiverPhone: '',
          receiverAddress: ''
        })
      } catch (error) {
        console.error('订单提交失败:', error)
        ElMessage.error('订单提交失败，请稍后重试')
      }
    }

    // 打开我的订单对话框
    const openOrderDialog = () => {
      orderDialogVisible.value = true
      fetchOrders()
    }

    // 关闭我的订单对话框
    const closeOrderDialog = () => {
      orderDialogVisible.value = false
    }

    // 获取我的订单列表
    const fetchOrders = async () => {
      orderLoading.value = true
      try {
        const response = await getOrderList(orderQueryParams)
        if (response.code === 200) {
          orderList.value = response.data.records
          orderTotal.value = response.data.total
        } else {
          ElMessage.error('获取订单列表失败')
        }
      } catch (error) {
        console.error('获取订单列表失败:', error)
        ElMessage.error('获取订单列表失败')
      } finally {
        orderLoading.value = false
      }
    }

    // 订单状态文本转换
    const getStatusText = (status) => {
      switch (status) {
        case -1:
          return '已取消'
        case 0:
          return '待支付'
        case 1:
          return '已支付'
        case 2:
          return '已发货'
        case 3:
          return '已完成'
        default:
          return '未知状态'
      }
    }

    // 订单状态类型转换
    const getStatusType = (status) => {
      switch (status) {
        case -1:
          return 'danger'
        case 0:
          return 'info'
        case 1:
          return 'success'
        case 2:
          return 'warning'
        case 3:
          return 'success'
        default:
          return 'default'
      }
    }

    // 订单分页大小变化
    const handleOrderSizeChange = (size) => {
      orderQueryParams.pageSize = size
      fetchOrders()
    }

    // 订单当前页变化
    const handleOrderCurrentChange = (current) => {
      orderQueryParams.pageNum = current
      fetchOrders()
    }

    // 订单详情
    const handleOrderDetail = async (order) => {
      try {
        // 跳转到订单详情页面
        router.push(`/order/detail/${order.id}`)
      } catch (error) {
        ElMessage.error('跳转订单详情失败')
      }
    }

    // 订单支付
    const handleOrderPay = async (order) => {
      try {
        await ElMessageBox.confirm('确认支付该订单吗？')
        await payOrder(order.id)
        ElMessage.success('支付成功')
        fetchOrders() // 刷新订单列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('支付失败')
        }
      }
    }

    // 订单取消
    const handleOrderCancel = async (order) => {
      try {
        await ElMessageBox.confirm('确认取消该订单吗？', '警告', {
          type: 'warning'
        })
        await cancelOrder(order.id)
        ElMessage.success('订单已取消')
        fetchOrders() // 刷新订单列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消订单失败')
        }
      }
    }
    
    // 订单完成
    const handleOrderComplete = async (order) => {
      try {
        await ElMessageBox.confirm('确认完成该订单吗？')
        await completeOrder(order.id)
        ElMessage.success('订单已完成')
        fetchOrders() // 刷新订单列表
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('完成订单失败')
        }
      }
    }
    
    // 查看详情
    const viewDetail = (id) => {
      router.push(`/product/detail/${id}`)
    }

    // 初始化
    onMounted(() => {
      fetchCategories()
      fetchProducts()
    })

    return {
      searchForm,
      categories,
      products,
      pagination,
      apiUrl,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      addToCart,
      goToCart,
      viewDetail,
      // 购物车相关
      cartDialogVisible,
      cartItems,
      cartTotal,
      totalPrice,
      updateCartItemQuantity,
      removeCartItem,
      submitOrder,
      // 订单表单相关
      orderForm,
      // 我的订单相关
      orderDialogVisible,
      orderList,
      orderLoading,
      orderTotal,
      orderQueryParams,
      openOrderDialog,
      closeOrderDialog,
      getStatusText,
      getStatusType,
      handleOrderSizeChange,
      handleOrderCurrentChange,
      handleOrderDetail,
      handleOrderPay,
      handleOrderCancel,
      handleOrderComplete
    }
  }
}
</script>

<style scoped>
.product-selection-container {
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

.product-list {
  margin-bottom: 20px;
}

.product-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 8px;
  overflow: hidden;
}

/* 商品卡片头部背景渐变 */
.product-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  border: none;
  padding: 15px;
}

.product-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 商品卡片内容背景渐变 */
.product-card :deep(.el-card__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 15px;
}

.product-card-header h4 {
  margin: 0;
  font-size: 16px;
  color: #ffffff;
}

.product-card-header .price {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 18px;
}

.product-image {
  margin: 10px 0;
}

.product-info {
  margin-bottom: 15px;
}

.product-info .description {
  margin: 10px 0;
  font-size: 14px;
  color: #e0e0e0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-info .stock-info {
  font-size: 14px;
  color: #67c23a;
}

/* 商品数量选择器样式 */
.product-card :deep(.el-input-number) {
  --el-input-bg-color: #48494c;
  --el-input-border-color: #ffffff;
}

.product-card :deep(.el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

.product-card :deep(.el-input__inner) {
  color: #ffffff;
  background-color: transparent;
  border: none;
}

.product-card :deep(.el-input-number__decrease),
.product-card :deep(.el-input-number__increase) {
  background-color: #48494c !important;
  border-color: #ffffff !important;
  color: #ffffff !important;
}

.product-card :deep(.el-input-number__decrease:hover),
.product-card :deep(.el-input-number__increase:hover) {
  background-color: #5a5b5e !important;
  border-color: #ffffff !important;
}

.product-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-actions .button-group {
  display: flex;
  gap: 1px;
}

.search-area {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

/* 确保表单标签在深色背景上是白色的 - 使用:global选择器确保优先级 */
.search-area :global(.el-form-item__label) {
  color: #ffffff !important;
}

/* 统一设置搜索区域内的所有输入框和选择框样式，确保固定白色边框 */
.search-area {
  /* 统一设置所有输入框和选择框的基础样式，固定白色边框 */
  :global(.el-input__wrapper),
  :global(.el-select__wrapper) {
    background-color: #48494c !important;
    border: 1px solid rgb(255, 255, 255) !important;
    box-shadow: none !important;
    width: 160px !important; /* 加宽选择框宽度 */
  }
  
  /* 确保所有交互状态下都保持固定白色边框 */
  :global(.el-input__wrapper:hover),
  :global(.el-input__wrapper:focus),
  :global(.el-input__wrapper:focus-visible),
  :global(.el-input__wrapper.is-focus),
  :global(.el-input__wrapper.is-active),
  :global(.el-input__wrapper:active),
  :global(.el-select__wrapper:hover),
  :global(.el-select__wrapper:focus),
  :global(.el-select__wrapper:focus-visible),
  :global(.el-select__wrapper.is-focus),
  :global(.el-select__wrapper.is-active),
  :global(.el-select__wrapper:active) {
    border: 1px solid rgb(255, 255, 255) !important;
    box-shadow: none !important;
  }
}

.search-area :global(.el-input__inner),
.search-area :global(.el-select__input) {
  color: #ffffff !important;
}

/* 确保输入框和选择框的占位符是浅色的 */
.search-area :global(.el-input__inner::placeholder),
.search-area :global(.el-select__input::placeholder) {
  color: rgba(255, 255, 255, 0.6) !important;
}

/* 确保选择框中已选文字的颜色为白色 */
.search-area :global(.el-select__selected-item),
.search-area :global(.el-select__selected-item span),
.search-area :global(.el-select__placeholder),
.search-area :global(.el-select__placeholder span) {
  color: #ffffff !important;
}

/* 搜索区域内的按钮样式 */
.search-area :global(.el-button) {
  margin: 0 5px;
}

/* rounded-button 样式定义 */
.rounded-button {
  background-color: #ffffff !important;
  color: #274151 !important;
  border-color: #ffffff !important;
  transition: all 0.3s ease !important;
}

.rounded-button:hover:not(.is-disabled) {
  background-color: #5a5b5f !important;
  color: #274151 !important;
  border-color: #ffffff !important;
}

.rounded-button.is-disabled {
  border: 1px solid #ffffff !important;
  background-color: #ffffff !important;
}

/* Element Plus 主按钮特殊处理 */
:global(.rounded-button.el-button--primary) {
  --el-button-text-color: #274151 !important;
}

:global(.rounded-button.el-button--primary span) {
  color: #274151 !important;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  padding: 15px;
  border-radius: 8px;
  color: #ffffff;
}

/* 分页组件内部元素样式 */
.pagination :global(.el-pagination *) {
  color: #ffffff !important;
}

/* 分页按钮样式 */
.pagination :global(.el-pagination button) {
  color: #ffffff !important;
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  border-color: #ffffff !important;
  border-radius: 4px !important;
}

/* 分页页码样式 */
.pagination :global(.el-pager li.is-active) {
  background-color: #48494c !important;
  color: #ffffff !important;
  border: 1px solid #ffffff !important;
  border-radius: 4px !important;
}

/* 分页组件元素间距调整 */
.pagination :global(.el-pagination button) {
  margin: 0 5px !important;
}

.pagination :global(.el-pager li) {
  margin: 0 5px !important;
  padding: 0 10px !important;
  border: 1px solid transparent !important;
  border-radius: 4px !important;
}

.pagination :global(.el-pager) {
  margin: 0 2px !important;
}

/* 分页选择器样式 */
.pagination :global(.el-select__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

/* 分页输入框样式 */
.pagination :global(.el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

.pagination :global(.el-input__inner) {
  color: #ffffff !important;
  background-color: transparent !important;
}

/* 直接覆盖全局的el-card样式，确保最外层卡片透明 */
:global(.product-selection-container .el-card) {
  background-color: transparent !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

/* 直接覆盖全局的el-card__body样式，确保最外层卡片内容区域透明 */
:global(.product-selection-container .el-card .el-card__body) {
  background-color: transparent !important;
  background: transparent !important;
  background-image: none !important;
  border: none !important;
  box-shadow: none !important;
}

/* 确保商品卡片保持渐变背景 */
:global(.product-selection-container .el-card.product-card) {
  background-color: transparent !important;
  background: transparent !important;
}

:global(.product-selection-container .el-card.product-card .el-card__body) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%) !important;
  background-color: transparent !important;
}

/* 购物车相关样式 */
.cart-badge {
  margin-left: 5px;
}

.empty-cart {
  text-align: center;
  padding: 40px 0;
}

.cart-content {
  padding: 10px 0;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.total-info {
  font-size: 16px;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #ff4d4f;
  margin: 0 8px;
}

.cart-actions {
  display: flex;
  gap: 10px;
}

/* 购物车对话框深色主题适配 */
:global(.cart-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.cart-dialog .el-dialog__header) {
  background: transparent;
  color: #ffffff;
  border-bottom: none;
}

:global(.cart-dialog .el-dialog__title) {
  color: #ffffff;
  text-align: left;
}

:global(.cart-dialog .el-dialog__header) {
  justify-content: flex-start;
}

:global(.cart-dialog .el-dialog__body) {
  background: transparent;
  color: #ffffff;
}

:global(.cart-dialog .el-dialog__footer) {
  background: transparent;
  border-top: none;
}

/* 订单信息表单样式 */
.order-info-form {
  margin: 20px 0;
  padding: 20px;
  background: transparent;
  border-radius: 8px;
}

/* 分类下拉菜单专用样式 */
:global(.category-select-dropdown) {
  background-color: #48494c !important;
  border: none !important;
  color: white !important;
}

:global(.category-select-dropdown .el-select-dropdown__wrap) {
  background-color: #48494c !important;
}

:global(.category-select-dropdown .el-select-dropdown__item) {
  background-color: #48494c !important;
  color: white !important;
}

:global(.category-select-dropdown .el-select-dropdown__item:hover) {
  background-color: #5A5B5F !important;
}

:global(.category-select-dropdown .el-select-dropdown__item.selected) {
  background-color: #6c6d70 !important;
}

/* 订单分页下拉菜单专用样式 */
:global(.order-page-size-dropdown) {
  background-color: #48494c !important;
  border: none !important;
  color: white !important;
}

:global(.order-page-size-dropdown .el-select-dropdown__wrap) {
  background-color: #48494c !important;
}

:global(.order-page-size-dropdown .el-select-dropdown__item) {
  background-color: #48494c !important;
  color: white !important;
}

:global(.order-page-size-dropdown .el-select-dropdown__item:hover) {
  background-color: #5A5B5F !important;
}

:global(.order-page-size-dropdown .el-select-dropdown__item.selected) {
  background-color: #6c6d70 !important;
}

.order-form {
  width: 100%;
}

/* 订单表单标签样式 */
:global(.cart-dialog .el-form-item__label) {
  color: #ffffff !important;
}

/* 订单表单输入框样式 */
:global(.cart-dialog .el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid rgb(255, 255, 255) !important;
  box-shadow: none !important;
}

:global(.cart-dialog .el-input__inner) {
  color: #ffffff !important;
  background-color: transparent !important;
}

/* 订单表单输入框占位符样式 */
:global(.cart-dialog .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6) !important;
}

/* 确保表单验证错误提示是白色的 */
:global(.cart-dialog .el-form-item__error) {
  color: #ffffff !important;
}

/* 表格样式适配 */
:global(.cart-dialog .el-table) {
  background-color: transparent;
  color: #ffffff;
}

:global(.cart-dialog .el-table__header-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.cart-dialog .el-table__header-wrapper tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.cart-dialog .el-table__header-wrapper th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #ffffff;
  border-bottom-color: #5a5b5e;
}

:global(.cart-dialog .el-table__body-wrapper tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.cart-dialog .el-table__body-wrapper tr:hover) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.cart-dialog .el-table__body-wrapper td) {
  background: transparent !important;
  border-bottom-color: #5a5b5e;
}

:global(.cart-dialog .el-table__body-wrapper tr:hover td) {
  background: transparent !important;
}

/* 表格条纹样式 - 统一使用相同渐变 */
:global(.cart-dialog .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: transparent !important;
}

/* 确保表格内部所有元素保持渐变背景 */
:global(.cart-dialog .el-table__cell) {
  background: transparent !important;
}

:global(.cart-dialog .el-table__cell:hover) {
  background: transparent !important;
}

:global(.cart-dialog .el-table__row:hover .el-table__cell) {
  background: transparent !important;
}

/* 购物车数量选择器样式，与商品列表保持一致 */
:global(.cart-dialog .el-input-number) {
  --el-input-bg-color: #48494c;
  --el-input-border-color: #ffffff;
}

:global(.cart-dialog .el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

:global(.cart-dialog .el-input__inner) {
  color: #ffffff;
  background-color: transparent;
  border: none;
}

:global(.cart-dialog .el-input-number__decrease),
:global(.cart-dialog .el-input-number__increase) {
  background-color: #48494c !important;
  border-color: #ffffff !important;
  color: #ffffff !important;
}

/* 订单对话框深色主题适配 */
:global(.order-dialog) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.order-dialog .el-dialog__header) {
  background: transparent;
  color: #ffffff;
  border-bottom: none;
}

:global(.order-dialog .el-dialog__title) {
  color: #ffffff;
  text-align: left;
}

:global(.order-dialog .el-dialog__header) {
  justify-content: flex-start;
}

:global(.order-dialog .el-dialog__body) {
  background: transparent;
  color: #ffffff;
}

:global(.order-dialog .el-dialog__footer) {
  background: transparent;
  border-top: none;
}

/* 订单对话框表格样式 */
:global(.order-dialog .el-table) {
  background-color: transparent;
  color: #ffffff;
}

:global(.order-dialog .el-table__header-wrapper) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.order-dialog .el-table__header-wrapper tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.order-dialog .el-table__header-wrapper th) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
  color: #ffffff;
  border-bottom-color: #5a5b5e;
}

:global(.order-dialog .el-table__body-wrapper tr) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.order-dialog .el-table__body-wrapper tr:hover) {
  background: linear-gradient(135deg, #464e58 0%, #434c55 100%);
}

:global(.order-dialog .el-table__body-wrapper td) {
  background: transparent !important;
  border-bottom-color: #5a5b5e;
}

:global(.order-dialog .el-table__body-wrapper tr:hover td) {
  background: transparent !important;
}

/* 订单对话框表格条纹样式 - 统一使用相同渐变 */
:global(.order-dialog .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: transparent !important;
}

/* 确保订单对话框表格内部所有元素保持渐变背景 */
:global(.order-dialog .el-table__cell) {
  background: transparent !important;
}

:global(.order-dialog .el-table__cell:hover) {
  background: transparent !important;
}

:global(.order-dialog .el-table__row:hover .el-table__cell) {
  background: transparent !important;
}

/* 订单对话框分页样式 */
:global(.order-dialog .el-pagination *) {
  color: #ffffff !important;
}

:global(.order-dialog .el-pagination button) {
  color: #ffffff !important;
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  border-color: #ffffff !important;
  border-radius: 4px !important;
}

:global(.order-dialog .el-pager li.is-active) {
  background-color: #48494c !important;
  color: #ffffff !important;
  border: 1px solid #ffffff !important;
  border-radius: 4px !important;
}

:global(.order-dialog .el-pagination button) {
  margin: 0 5px !important;
}

:global(.order-dialog .el-pager li) {
  margin: 0 5px !important;
  padding: 0 10px !important;
  border: 1px solid transparent !important;
  border-radius: 4px !important;
}

:global(.order-dialog .el-pager) {
  margin: 0 2px !important;
}

:global(.order-dialog .el-select__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

:global(.order-dialog .el-input__wrapper) {
  background-color: #48494c !important;
  border: 1px solid #ffffff !important;
  box-shadow: none !important;
}

:global(.order-dialog .el-input__inner) {
  color: #ffffff !important;
  background-color: transparent !important;
}

:global(.order-dialog .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6) !important;
}

:global(.order-dialog .el-select__selected-item),
:global(.order-dialog .el-select__selected-item span),
:global(.order-dialog .el-select__placeholder),
:global(.order-dialog .el-select__placeholder span) {
  color: #ffffff !important;
}

:global(.cart-dialog .el-input-number__decrease:hover),
:global(.cart-dialog .el-input-number__increase:hover) {
  background-color: #5a5b5e !important;
  border-color: #ffffff !important;
}
</style>