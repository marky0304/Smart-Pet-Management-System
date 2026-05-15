<template>
  <div class="my-orders-page">
    <!-- 头部卡片 -->
    <el-card class="header-card">
      <h2>📦 我的订单</h2>
      <p>查看和管理您的订单</p>
    </el-card>

    <!-- 筛选卡片 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable @change="loadOrders">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退货中" value="RETURNING" />
            <el-option label="已退货" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <div v-loading="loading">
      <el-card v-for="order in orders" :key="order.id" class="order-card">
        <template #header>
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)">{{ order.statusName }}</el-tag>
          </div>
        </template>

        <div class="order-content">
          <!-- 订单商品列表 -->
          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item">
              <img :src="item.productImage || defaultImage" class="item-image" />
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
              </div>
              <div class="item-subtotal">¥{{ item.subtotal || (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>

          <!-- 订单信息 -->
          <div class="order-details">
            <div class="detail-item">
              <span class="label">收货人：</span>
              <span>{{ order.shippingName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话：</span>
              <span>{{ order.shippingPhone }}</span>
            </div>
            <div class="detail-item">
              <span class="label">收货地址：</span>
              <span>{{ order.shippingAddress }}</span>
            </div>
            <div class="detail-item" v-if="order.trackingNumber">
              <span class="label">物流单号：</span>
              <span>{{ order.trackingNumber }}</span>
            </div>
            <div class="detail-item" v-if="order.notes">
              <span class="label">订单备注：</span>
              <span>{{ order.notes }}</span>
            </div>
            <div class="detail-item" v-if="order.returnReason">
              <span class="label">退货原因：</span>
              <span style="color:#e6a23c;">{{ order.returnReason }}</span>
            </div>
            <div class="detail-item" v-if="order.returnTime">
              <span class="label">退货时间：</span>
              <span>{{ formatDateTime(order.returnTime) }}</span>
            </div>
            <div class="detail-item" v-if="order.refundAmount">
              <span class="label">退款金额：</span>
              <span style="color:#f56c6c;font-weight:700;">¥{{ order.refundAmount }}</span>
            </div>
            <div class="detail-item" v-if="order.refundNotes">
              <span class="label">退款备注：</span>
              <span>{{ order.refundNotes }}</span>
            </div>
          </div>

          <!-- 订单总额和操作 -->
          <div class="order-footer">
            <div class="order-total">
              <span class="total-label">订单总额：</span>
              <span class="total-amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="order-actions">
              <!-- 倒计时显示 -->
              <span v-if="order.status === 'PENDING'" class="countdown-text"
                :class="getCountdownClass(order)">
                {{ getCountdownText(order) }}
              </span>
              <el-button
                v-if="order.status === 'PENDING'"
                type="primary"
                :disabled="isOrderExpired(order)"
                @click="openPayDialog(order)"
              >
                去支付
              </el-button>
              <el-button
                v-if="order.status === 'PENDING'"
                @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 'SHIPPED'"
                type="success"
                @click="handleConfirm(order)"
              >
                确认收货
              </el-button>
              <el-button
                v-if="order.status === 'COMPLETED' || order.status === 'SHIPPED'"
                type="warning"
                @click="openReturnDialog(order)"
              >
                申请退货
              </el-button>
              <el-button @click="handleViewDetail(order)">查看详情</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty 
      v-if="!loading && orders.length === 0" 
      description="暂无订单"
      :image-size="200"
    >
      <el-button type="primary" @click="$router.push('/dashboard/shop')">
        去购物
      </el-button>
    </el-empty>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="queryForm.pageNum"
      v-model:page-size="queryForm.pageSize"
      :total="total"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadOrders"
      @current-change="loadOrders"
      class="pagination"
    />

    <!-- 支付弹窗 -->
    <el-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="pay-dialog-body" v-if="payingOrder.id">
        <!-- 订单信息 -->
        <div class="pay-order-info">
          <div class="pay-info-row">
            <span class="pay-label">订单号</span>
            <span class="pay-value">{{ payingOrder.orderNo }}</span>
          </div>
          <div class="pay-info-row" style="margin-top:10px">
            <span class="pay-label">支付金额</span>
            <span class="pay-amount">¥{{ payingOrder.totalAmount }}</span>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="pay-section-title">选择支付方式</div>
        <div class="pay-method-group">
          <div
            v-for="m in payMethods" :key="m.value"
            class="pay-method-item"
            :class="{ 'is-active': payForm.method === m.value }"
            @click="payForm.method = m.value"
          >
            <span class="pay-method-icon" :style="{ background: m.color }">{{ m.icon }}</span>
            <span class="pay-method-name">{{ m.label }}</span>
          </div>
        </div>

        <!-- 支付密码 -->
        <div class="pay-section-title">支付密码</div>
        <!-- 6格密码点阵 -->
        <div class="pay-pwd-dots" @click="focusPwdInput">
          <div
            v-for="i in 6" :key="i"
            class="pay-pwd-dot"
            :class="{ filled: payForm.password.length >= i }"
          ></div>
        </div>
        <!-- 隐藏的真实输入框 -->
        <input
          ref="pwdInputRef"
          v-model="payForm.password"
          type="tel"
          maxlength="6"
          class="pay-pwd-hidden"
          @input="payForm.password = payForm.password.replace(/\D/g, '').slice(0,6)"
        />
        <div class="pay-password-tip">任意6位数字即可（模拟支付）</div>
      </div>

      <template #footer>
        <div class="pay-footer">
          <el-button class="pay-cancel-btn" @click="payDialogVisible = false" :disabled="paying">取消</el-button>
          <el-button
            class="pay-confirm-btn"
            :loading="paying"
            :disabled="payForm.password.length < 6"
            @click="confirmPay"
          >
            {{ paying ? '支付中...' : '确认支付' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退货对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="申请退货"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="return-dialog-body" v-if="returningOrder.id">
        <div class="return-order-info">
          <div class="return-info-row">
            <span class="return-label">订单号</span>
            <span class="return-value">{{ returningOrder.orderNo }}</span>
          </div>
          <div class="return-info-row" style="margin-top:10px">
            <span class="return-label">订单金额</span>
            <span class="return-amount">¥{{ returningOrder.totalAmount }}</span>
          </div>
        </div>
        <div class="return-section-title">退货原因</div>
        <el-input
          v-model="returnReason"
          type="textarea"
          :rows="4"
          placeholder="请填写退货原因（如：商品与描述不符、质量问题、不想要了等）"
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="warning" :disabled="!returnReason.trim()" @click="handleReturn">
          确认退货
        </el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="700px"
    >
      <div v-if="currentOrder.id" class="order-detail-dialog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ currentOrder.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" :span="2" v-if="currentOrder.paymentTime">
            {{ formatDateTime(currentOrder.paymentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.shippingName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.shippingPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="物流单号" :span="2" v-if="currentOrder.trackingNumber">
            {{ currentOrder.trackingNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="订单备注" :span="2" v-if="currentOrder.notes">
            {{ currentOrder.notes }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h4>订单商品</h4>
        <el-table :data="currentOrder.items" style="width: 100%">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="product-info">
                <img :src="row.productImage || defaultImage" class="product-image" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="80" align="center" prop="quantity" />
          <el-table-column label="小计" width="100" align="center">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>

        <div class="detail-total">
          <span>订单总额：</span>
          <span class="total-amount">¥{{ currentOrder.totalAmount }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { getMyOrders, cancelOrder, payOrder, confirmOrder, returnOrder } from '@/api/order'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentOrder = ref({})
const defaultImage = '/shop image/default-product.svg'

// ===== 支付弹窗状态 =====
const payDialogVisible = ref(false)
const payingOrder = ref({})
const paying = ref(false)
const payForm = reactive({ method: 'ALIPAY', password: '' })
const pwdInputRef = ref(null)

const payMethods = [
  { value: 'ALIPAY', label: '支付宝', icon: '支', color: '#1677ff' },
  { value: 'WECHAT', label: '微信支付', icon: '微', color: '#07c160' },
  { value: 'BANK',   label: '银行卡',  icon: '卡', color: '#f59e0b' },
]
const focusPwdInput = () => { pwdInputRef.value?.focus() }

// ===== 退货弹窗状态 =====
const returnDialogVisible = ref(false)
const returningOrder = ref({})
const returnReason = ref('')

const openReturnDialog = (order) => {
  returningOrder.value = order
  returnReason.value = ''
  returnDialogVisible.value = true
}

const handleReturn = async () => {
  try {
    await returnOrder(returningOrder.value.id, returnReason.value.trim())
    ElMessage.success('退货申请已提交，请等待管理员处理')
    returnDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '申请退货失败')
  }
}

// ===== WebSocket =====
const userStore = useUserStore()
let ws = null

const connectWebSocket = () => {
  const userId = userStore.userInfo?.id
  if (!userId) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const port = import.meta.env.VITE_BACKEND_PORT || '8080'
  const wsUrl = `${protocol}//${window.location.hostname}:${port}/api/ws/order/${userId}`

  ws = new WebSocket(wsUrl)
  ws.onopen = () => {
    // WebSocket connected
  }
  ws.onmessage = (event) => {
    if (event.data && event.data.startsWith('NEW_ORDER:')) {
      ElNotification({
        title: '新订单通知',
        message: '您有一笔新订单，订单已自动生成！',
        type: 'success',
        duration: 5000,
      })
      loadOrders()
    }
  }
  ws.onerror = () => {
    setTimeout(connectWebSocket, 5000)
  }
  ws.onclose = () => {
    // WebSocket closed
  }
}

// ===== 倒计时 =====
const now = ref(Date.now())
let timer = null

const EXPIRE_MS = 30 * 60 * 1000 // 30分钟

const getExpireTime = (order) => new Date(order.createTime).getTime() + EXPIRE_MS

const isOrderExpired = (order) => {
  if (!order.createTime) return false
  return now.value >= getExpireTime(order)
}

const getCountdownText = (order) => {
  if (!order.createTime) return ''
  const remaining = getExpireTime(order) - now.value
  if (remaining <= 0) return '已超时'
  const m = Math.floor(remaining / 60000)
  const s = Math.floor((remaining % 60000) / 1000)
  return `剩余 ${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const getCountdownClass = (order) => {
  if (isOrderExpired(order)) return 'countdown-expired'
  const remaining = getExpireTime(order) - now.value
  return remaining < 5 * 60 * 1000 ? 'countdown-urgent' : 'countdown-normal'
}

// ===== 支付弹窗操作 =====
const openPayDialog = (order) => {
  payingOrder.value = order
  payForm.method = 'ALIPAY'
  payForm.password = ''
  payDialogVisible.value = true
}

const confirmPay = async () => {
  paying.value = true
  // 模拟支付处理延迟
  await new Promise(resolve => setTimeout(resolve, 1500))
  try {
    await payOrder(payingOrder.value.id)
    ElMessage.success('支付成功！')
    payDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '支付失败，请重试')
  } finally {
    paying.value = false
  }
}

const queryForm = reactive({
  orderNo: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  loadOrders()
  connectWebSocket()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (ws) { ws.close(); ws = null }
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getMyOrders(queryForm)
    if (res.data && res.data.records) {
      orders.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryForm.pageNum = 1; loadOrders() }
const handleReset = () => { queryForm.orderNo = ''; queryForm.status = ''; queryForm.pageNum = 1; loadOrders() }

// 取消订单
const handleCancel = (order) => {
  ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await cancelOrder(order.id)
      ElMessage.success('订单已取消')
      loadOrders()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }).catch(() => {})
}

// 确认收货
const handleConfirm = (order) => {
  ElMessageBox.confirm('确定已收到货物吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'success'
  }).then(async () => {
    try {
      await confirmOrder(order.id)
      ElMessage.success('确认收货成功')
      loadOrders()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '确认失败')
    }
  }).catch(() => {})
}

const handleViewDetail = (order) => { currentOrder.value = order; detailDialogVisible.value = true }

const getStatusType = (status) => {
  const typeMap = { 'PENDING': 'warning', 'PAID': 'primary', 'SHIPPED': 'info', 'COMPLETED': 'success', 'CANCELLED': 'danger', 'RETURNING': 'warning', 'RETURNED': 'info' }
  return typeMap[status] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.my-orders-page {
  padding: 0;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #5C8AEB 0%, #7BAAF2 100%);
  border: none;
}

.header-card h2 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 28px;
  font-weight: 700;
}

.header-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.order-no {
  font-weight: 600;
  color: #333;
}

.order-time {
  font-size: 14px;
  color: #999;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.item-price {
  font-size: 14px;
  color: #666;
}

.item-subtotal {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.order-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-item {
  font-size: 14px;
}

.detail-item .label {
  color: #666;
  margin-right: 8px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
}

.order-total {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.order-detail-dialog h4 {
  margin: 20px 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}

.order-detail-dialog .product-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.order-detail-dialog .product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.detail-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
  font-size: 18px;
  font-weight: 600;
}

.detail-total .total-amount {
  font-size: 24px;
  color: #f56c6c;
}

/* ===== 支付弹窗样式 ===== */
.pay-dialog-body { padding: 4px 0; }

.pay-order-info {
  background: #f0f4ff; border-radius: 10px; padding: 18px 20px;
  margin-bottom: 22px;
}
.pay-info-row {
  display: flex; justify-content: space-between; align-items: center;
}
.pay-label { font-size: 13px; color: #9ca3af; }
.pay-value { font-size: 13px; color: #374151; font-weight: 500; }
.pay-amount { font-size: 32px; font-weight: 800; color: #dc2626; }

.pay-section-title {
  font-size: 14px; font-weight: 700; color: #111827;
  margin-bottom: 14px;
}

/* 支付方式 */
.pay-method-group { display: flex; gap: 12px; margin-bottom: 24px; }
.pay-method-item {
  flex: 1; border: 2px solid #e5e7eb; border-radius: 12px;
  padding: 14px 8px; cursor: pointer; transition: all 0.2s;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
}
.pay-method-item:hover { border-color: #93c5fd; }
.pay-method-item.is-active { border-color: #5C8AEB; background: rgba(92,138,235,0.08); }
.pay-method-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; color: #fff;
}
.pay-method-name { font-size: 12px; font-weight: 500; color: #374151; }

/* 密码点阵 */
.pay-pwd-dots {
  display: flex; gap: 10px; justify-content: center;
  border: 1.5px solid #d1d5db; border-radius: 10px;
  padding: 16px; cursor: text; margin-bottom: 8px;
  background: white;
}
.pay-pwd-dot {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid #d1d5db; background: white;
  transition: all 0.15s;
}
.pay-pwd-dot.filled { background: #111827; border-color: #111827; }
.pay-pwd-hidden {
  position: absolute; opacity: 0; pointer-events: none;
  width: 1px; height: 1px;
}
.pay-password-tip { font-size: 12px; color: #9ca3af; margin-bottom: 4px; }

/* 底部按钮 */
.pay-footer { display: flex; gap: 12px; width: 100%; }
.pay-cancel-btn { flex: 1; height: 44px; border-radius: 10px; font-size: 15px; }
.pay-confirm-btn {
  flex: 2; height: 44px; border-radius: 10px; font-size: 15px;
  background: #5C8AEB !important; border-color: #5C8AEB !important;
  color: white !important; font-weight: 600;
}
.pay-confirm-btn:disabled {
  background: #93c5fd !important; border-color: #93c5fd !important;
}

/* ===== 倒计时样式 ===== */
.countdown-text {
  font-size: 13px; font-weight: 600; padding: 4px 10px;
  border-radius: 20px; margin-right: 4px;
}
.countdown-normal { color: #d97706; background: #fef3c7; }
.countdown-urgent { color: #dc2626; background: #fee2e2; animation: pulse 1s infinite; }
.countdown-expired { color: #6b7280; background: #f3f4f6; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* ===== 退货弹窗样式 ===== */
.return-dialog-body { padding: 4px 0; }

.return-order-info {
  background: #fef7e8; border-radius: 10px; padding: 18px 20px;
  margin-bottom: 22px;
}
.return-info-row {
  display: flex; justify-content: space-between; align-items: center;
}
.return-label { font-size: 13px; color: #9ca3af; }
.return-value { font-size: 13px; color: #374151; font-weight: 500; }
.return-amount { font-size: 28px; font-weight: 800; color: #e6a23c; }

.return-section-title {
  font-size: 14px; font-weight: 700; color: #111827;
  margin-bottom: 12px;
}
</style>
