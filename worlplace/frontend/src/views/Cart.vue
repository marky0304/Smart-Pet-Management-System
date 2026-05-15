<template>
  <div class="cart-page">
    <!-- 头部卡片 -->
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>🛒 购物车</h2>
          <p>{{ cartItems.length }} 件商品</p>
        </div>
        <el-button @click="$router.push('/dashboard/shop')">继续购物</el-button>
      </div>
    </el-card>

    <!-- 购物车列表 -->
    <el-card v-if="cartItems.length > 0" class="cart-card">
      <el-table 
        :data="cartItems" 
        style="width: 100%" 
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品信息" min-width="350">
          <template #default="{ row }">
            <div class="product-info">
              <img :src="row.image || defaultImage" class="product-image" />
              <div class="product-details">
                <div class="product-name">
                  {{ row.name }}
                  <el-tag v-if="row.status === 0" type="danger" size="small" style="margin-left:6px">已下架</el-tag>
                </div>
                <div class="product-stock">库存：{{ row.stock }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="180" align="center">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.quantity" 
              :min="1" 
              :max="row.stock"
              @change="handleQuantityChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="center">
          <template #default="{ row }">
            <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 购物车底部 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <el-button link type="danger" @click="clearSelected" :disabled="selectedItems.length === 0">
            删除选中
          </el-button>
          <el-button link @click="clearCartFn">清空购物车</el-button>
        </div>
        <div class="footer-right">
          <div class="total-info">
            <span class="total-label">已选 {{ selectedItems.length }} 件商品，总计：</span>
            <span class="total-price">¥{{ totalPrice }}</span>
          </div>
          <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedItems.length === 0">
            去结算 ({{ selectedItems.length }})
          </el-button>
          <el-button size="large" @click="router.push('/dashboard/my-orders')">
            查看订单
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 最近订单 -->
    <el-card v-if="recentOrders.length > 0" class="recent-orders-card">
      <template #header>
        <div class="recent-orders-header">
          <span>📋 最近订单</span>
          <el-button link type="primary" @click="router.push('/dashboard/my-orders')">查看全部 →</el-button>
        </div>
      </template>
      <div class="recent-order-item" v-for="order in recentOrders" :key="order.id" @click="router.push('/dashboard/my-orders')">
        <div class="ro-left">
          <span class="ro-no">{{ order.orderNo }}</span>
          <span class="ro-time">{{ formatTime(order.createTime) }}</span>
        </div>
        <div class="ro-mid">
          <span v-for="item in (order.items || []).slice(0, 3)" :key="item.id" class="ro-product">{{ item.productName }}</span>
          <span v-if="(order.items || []).length > 3" class="ro-more">等{{ order.items.length }}件商品</span>
        </div>
        <div class="ro-right">
          <span class="ro-amount">¥{{ order.totalAmount }}</span>
          <el-tag :type="statusType(order.status)" size="small">{{ order.statusName || order.status }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 空购物车 -->
    <el-empty v-else description="购物车是空的" :image-size="200">
      <el-button type="primary" @click="$router.push('/dashboard/shop')">
        去购物
      </el-button>
    </el-empty>

    <!-- 结算对话框 -->
    <el-dialog 
      v-model="checkoutDialogVisible" 
      title="确认订单" 
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
        <el-form-item label="收货地址" prop="shippingAddress">
          <div v-if="selectedAddress" class="address-display">
            <div class="address-display-info">
              <span class="address-contact">{{ selectedAddress.receiverName }} {{ selectedAddress.receiverPhone }}</span>
              <span class="address-full">{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}</span>
            </div>
            <el-button link type="primary" @click="openAddressPicker">更换地址</el-button>
          </div>
          <div v-else class="address-display" @click="openAddressPicker">
            <span class="address-placeholder">请选择收货地址</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="orderForm.paymentMethod">
            <el-radio label="ONLINE">在线支付</el-radio>
            <el-radio label="COD">货到付款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订单备注">
          <el-input 
            v-model="orderForm.notes" 
            type="textarea" 
            :rows="2"
            placeholder="选填，可以告诉我们您的特殊需求"
          />
        </el-form-item>
        
        <el-divider />
        
        <div class="order-summary">
          <h4>订单商品</h4>
          <div class="summary-item" v-for="item in selectedItems" :key="item.id">
            <span>{{ item.name }} × {{ item.quantity }}</span>
            <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
          <el-divider />
          <div class="summary-total">
            <span>订单总额：</span>
            <span class="total-amount">¥{{ totalPrice }}</span>
          </div>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
      </template>
    </el-dialog>

    <!-- 地址选择弹窗 -->
    <el-dialog
      v-model="addressPickerVisible"
      title="选择收货地址"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="addressList.length === 0" style="text-align: center; padding: 40px 0;">
        <p style="color: #909399; margin-bottom: 16px;">暂无收货地址</p>
        <el-button type="primary" @click="$router.push('/dashboard/address')">去添加地址</el-button>
      </div>
      <div class="address-picker-list" v-else>
        <div
          class="address-picker-item"
          v-for="item in addressList"
          :key="item.id"
          :class="{ selected: tempSelectedId === item.id }"
          @click="tempSelectedId = item.id"
        >
          <div class="picker-item-top">
            <span class="picker-receiver">{{ item.receiverName }}</span>
            <span class="picker-phone">{{ item.receiverPhone }}</span>
            <el-tag v-if="item.isDefault === 1" size="small" type="primary">默认</el-tag>
          </div>
          <div class="picker-item-addr">
            {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detailAddress }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="addressPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddressPick" :disabled="!tempSelectedId">
          确认使用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOrder, getMyOrders } from '@/api/order'
import { getAddresses } from '@/api/address'
import { getCartItems, updateCartQuantity, removeCartItem, clearCart } from '@/api/cart'

const router = useRouter()
const isAuth = computed(() => !!localStorage.getItem('token'))

const mapCartItem = (apiItem) => ({
  id: apiItem.id,
  productId: apiItem.productId,
  name: apiItem.productName,
  image: apiItem.productImage,
  price: apiItem.price,
  stock: apiItem.stock,
  status: apiItem.productStatus,
  quantity: apiItem.quantity
})

const cartItems = ref([])
const selectedItems = ref([])
const selectAll = ref(false)
const tableRef = ref(null)
const checkoutDialogVisible = ref(false)
const submitting = ref(false)
const defaultImage = '/shop image/default-product.svg'
const recentOrders = ref([])
const addressList = ref([])
const selectedAddress = ref(null)
const addressPickerVisible = ref(false)
const tempSelectedId = ref(null)

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const statusType = (s) => {
  const map = { PENDING: 'warning', PAID: 'success', SHIPPED: '', COMPLETED: 'info', CANCELLED: 'danger', RETURNING: 'warning', RETURNED: 'info' }
  return map[s] || 'info'
}

const orderForm = reactive({
  addressId: null,
  shippingName: '',
  shippingPhone: '',
  shippingAddress: '',
  paymentMethod: 'ONLINE',
  notes: ''
})

const orderRules = {
  shippingAddress: [
    { validator: (rule, value, callback) => {
      if (!selectedAddress.value) { callback(new Error('请选择收货地址')) }
      else { callback() }
    }, trigger: 'change' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

const orderFormRef = ref(null)

// 计算总价
const totalPrice = computed(() => {
  return selectedItems.value
    .reduce((total, item) => total + item.price * item.quantity, 0)
    .toFixed(2)
})

onMounted(() => {
  loadCart()
  loadUserInfo()
  if (isAuth.value) {
    loadRecentOrders()
    loadUserAddresses()
  }
})

// 加载购物车
const loadCart = async () => {
  if (isAuth.value) {
    try {
      const res = await getCartItems()
      cartItems.value = (res.data || []).map(mapCartItem)
      return
    } catch (e) {
      console.warn('API cart load failed, falling back to localStorage', e)
    }
  }
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  cartItems.value = cart
}

// 加载用户信息
const loadUserInfo = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.phone) {
    orderForm.shippingPhone = userInfo.phone
  }
  if (userInfo.nickname) {
    orderForm.shippingName = userInfo.nickname
  }
}

// 加载用户地址
const loadUserAddresses = async () => {
  try {
    const res = await getAddresses()
    addressList.value = res.data || []
    const def = addressList.value.find(a => a.isDefault === 1)
    if (def) {
      selectedAddress.value = def
      orderForm.addressId = def.id
      orderForm.shippingName = def.receiverName
      orderForm.shippingPhone = def.receiverPhone
      orderForm.shippingAddress = `${def.province} ${def.city} ${def.district} ${def.detailAddress}`
    }
  } catch (e) { ElMessage.warning('加载收货地址失败') }
}

const openAddressPicker = () => {
  tempSelectedId.value = selectedAddress.value?.id || null
  addressPickerVisible.value = true
}

const confirmAddressPick = () => {
  const addr = addressList.value.find(a => a.id === tempSelectedId.value)
  if (addr) {
    selectedAddress.value = addr
    orderForm.addressId = addr.id
    orderForm.shippingName = addr.receiverName
    orderForm.shippingPhone = addr.receiverPhone
    orderForm.shippingAddress = `${addr.province} ${addr.city} ${addr.district} ${addr.detailAddress}`
  }
  addressPickerVisible.value = false
}

// 加载最近订单
const loadRecentOrders = async () => {
  try {
    const res = await getMyOrders({ page: 1, pageSize: 3 })
    recentOrders.value = res.data?.records || res.data || []
  } catch (e) {
    // silently ignore
  }
}

// 保存购物车（仅未登录时写入localStorage）
const saveCart = () => {
  if (!isAuth.value) {
    localStorage.setItem('cart', JSON.stringify(cartItems.value))
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
  selectAll.value = selection.length === cartItems.value.length && cartItems.value.length > 0
}

// 全选/取消全选
const handleSelectAll = () => {
  if (selectAll.value) {
    tableRef.value.toggleAllSelection()
  } else {
    tableRef.value.clearSelection()
  }
}

// 数量变化
const handleQuantityChange = (row) => {
  if (isAuth.value && row) {
    updateCartQuantity(row.id, row.quantity).catch(e => {
      ElMessage.error(e?.message || '更新数量失败')
    })
  } else {
    saveCart()
  }
}

// 删除商品
const removeItem = (item) => {
  ElMessageBox.confirm('确定要删除这件商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (isAuth.value) {
      try {
        await removeCartItem(item.id)
        await loadCart()
        ElMessage.success('删除成功')
      } catch (e) {
        ElMessage.error(e?.message || '删除失败')
      }
      return
    }
    const index = cartItems.value.findIndex(i => i.id === item.id)
    if (index > -1) {
      cartItems.value.splice(index, 1)
      saveCart()
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

// 删除选中
const clearSelected = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (isAuth.value) {
      try {
        for (const item of selectedItems.value) {
          await removeCartItem(item.id)
        }
        await loadCart()
        selectedItems.value = []
        ElMessage.success('删除成功')
      } catch (e) {
        ElMessage.error(e?.message || '删除失败')
      }
      return
    }
    selectedItems.value.forEach(item => {
      const index = cartItems.value.findIndex(i => i.id === item.id)
      if (index > -1) {
        cartItems.value.splice(index, 1)
      }
    })
    saveCart()
    selectedItems.value = []
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 清空购物车
const clearCartFn = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (isAuth.value) {
      try {
        await clearCart()
        cartItems.value = []
        selectedItems.value = []
        ElMessage.success('购物车已清空')
      } catch (e) {
        ElMessage.error(e?.message || '清空失败')
      }
      return
    }
    cartItems.value = []
    selectedItems.value = []
    saveCart()
    ElMessage.success('购物车已清空')
  }).catch(() => {})
}

// 去结算
const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  // 检查是否有下架商品
  const offShelf = selectedItems.value.filter(i => i.status === 0)
  if (offShelf.length > 0) {
    ElMessage.error(`以下商品已下架，请移除后再结算：${offShelf.map(i => i.name).join('、')}`)
    return
  }
  checkoutDialogVisible.value = true
}

// 提交订单
const submitOrder = async () => {
  try {
    await orderFormRef.value.validate()
    
    submitting.value = true
    
    // 构建订单数据
    const orderData = {
      addressId: orderForm.addressId,
      items: selectedItems.value.map(item => ({
        productId: item.productId || item.id,
        quantity: item.quantity
      })),
      shippingName: orderForm.shippingName,
      shippingPhone: orderForm.shippingPhone,
      shippingAddress: orderForm.shippingAddress,
      paymentMethod: orderForm.paymentMethod,
      notes: orderForm.notes
    }

    const res = await createOrder(orderData)

    ElMessage.success('订单创建成功！')
    
    // 从购物车中移除已购买的商品
    if (isAuth.value) {
      for (const item of selectedItems.value) {
        await removeCartItem(item.id).catch(() => {})
      }
      loadCart()
    } else {
      selectedItems.value.forEach(item => {
        const index = cartItems.value.findIndex(i => i.id === item.id)
        if (index > -1) {
          cartItems.value.splice(index, 1)
        }
      })
      saveCart()
    }

    checkoutDialogVisible.value = false
    
    // 跳转到订单列表
    setTimeout(() => {
      router.push('/dashboard/my-orders')
    }, 1500)
    
  } catch (error) {
    console.error('提交订单失败:', error)
    // request.js 拦截器已经把错误信息放在 error.message 里
    const msg = error?.message || error?.response?.data?.message || '提交订单失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.cart-page {
  padding: 0;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #5C8AEB 0%, #7BAAF2 100%);
  border: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 28px;
  font-weight: 700;
}

.header-content p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.cart-card {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.product-stock {
  font-size: 12px;
  color: #999;
}

.price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.subtotal {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-top: 20px;
}

.footer-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.footer-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

.total-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.order-summary {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.order-summary h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #666;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.total-amount {
  font-size: 24px;
  color: #f56c6c;
}

.address-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-display:hover {
  background: #e8f0fe;
}

.address-display-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.address-contact {
  font-weight: 600;
  color: #303133;
}

.address-full {
  font-size: 13px;
  color: #606266;
}

.address-placeholder {
  color: #909399;
}

.address-picker-list {
  max-height: 360px;
  overflow-y: auto;
}

.address-picker-item {
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-picker-item:hover {
  border-color: #5C8AEB;
}

.address-picker-item.selected {
  border-color: #5C8AEB;
  background: #f0f5ff;
}

.picker-item-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.picker-receiver {
  font-weight: 600;
  color: #303133;
}

.picker-phone {
  font-size: 14px;
  color: #606266;
}

.picker-item-addr {
  font-size: 13px;
  color: #909399;
}
</style>
