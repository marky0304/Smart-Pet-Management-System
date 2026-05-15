<template>
  <div class="order-mgmt">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <div class="page-title">📦 订单管理</div>
        <div class="page-sub">查看所有用户订单，执行发货和状态管理</div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-loading="statsLoading">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <div class="stat-card" :class="card.class">
          <div class="stat-icon">
            <el-icon :size="32"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="queryForm.orderNo"
        placeholder="搜索订单号"
        clearable
        style="width: 220px"
        @keyup.enter="handleQuery"
      />
      <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 160px">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="退货中" value="RETURNING" />
        <el-option label="已退货" value="RETURNED" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 订单表格 -->
    <div class="table-card" v-loading="loading">
      <el-table :data="orders" style="width: 100%" stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="180" />
        <el-table-column label="用户名" prop="username" width="120" />
        <el-table-column label="商品数量" width="100" align="center">
          <template #default="{ row }">{{ row.items ? row.items.length : 0 }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="110" align="center">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button size="small" type="primary" v-if="row.status === 'PAID'" @click="openShipDialog(row)">发货</el-button>
            <el-button size="small" type="warning" @click="openStatusDialog(row)">改状态</el-button>
            <el-button
              size="small" type="success"
              v-if="row.status === 'RETURNING'"
              @click="openApproveReturnDialog(row)"
            >审批</el-button>
            <el-button
              size="small" type="danger"
              v-if="row.status === 'RETURNING'"
              @click="openRejectReturnDialog(row)"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
        class="pagination"
      />
    </div>

    <!-- 订单详情 Dialog -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="currentOrder.id">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)" size="small">
              {{ getStatusLabel(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '—' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.shippingName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.shippingPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="物流单号" :span="2" v-if="currentOrder.trackingNumber">
            {{ currentOrder.trackingNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="退货原因" :span="2" v-if="currentOrder.returnReason">
            <span style="color:#e6a23c;">{{ currentOrder.returnReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退货时间" :span="2" v-if="currentOrder.returnTime">
            {{ formatDateTime(currentOrder.returnTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="退款金额" :span="2" v-if="currentOrder.refundAmount">
            <span style="color:#dc2626;font-weight:700;">¥{{ currentOrder.refundAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款时间" :span="2" v-if="currentOrder.refundTime">
            {{ formatDateTime(currentOrder.refundTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="退款备注" :span="2" v-if="currentOrder.refundNotes">
            {{ currentOrder.refundNotes }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="detail-section-title">订单商品</div>
        <el-table :data="currentOrder.items || []" style="width: 100%">
          <el-table-column label="商品名" prop="productName" min-width="160" />
          <el-table-column label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
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

    <!-- 发货 Dialog -->
    <el-dialog v-model="shipDialogVisible" title="填写物流单号" width="420px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" :disabled="!shipForm.trackingNumber" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 改状态 Dialog -->
    <el-dialog v-model="statusDialogVisible" title="修改订单状态" width="360px">
      <el-form label-width="90px">
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width: 100%">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退货中" value="RETURNING" />
            <el-option label="已退货" value="RETURNED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="confirmStatusChange">确认</el-button>
      </template>
    </el-dialog>

    <!-- 审批退货 Dialog -->
    <el-dialog v-model="approveReturnDialogVisible" title="审批退货" width="420px">
      <div v-if="returnTargetOrder.id" style="margin-bottom:16px;">
        <div style="margin-bottom:8px;">订单号：<strong>{{ returnTargetOrder.orderNo }}</strong></div>
        <div>退货原因：{{ returnTargetOrder.returnReason }}</div>
        <div style="margin-top:8px;color:#dc2626;font-weight:700;">
          退款金额：¥{{ returnTargetOrder.totalAmount }}
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="退款备注">
          <el-input v-model="returnNotes" placeholder="可选，退款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveReturnDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="returnLoading" @click="confirmApproveReturn">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝退货 Dialog -->
    <el-dialog v-model="rejectReturnDialogVisible" title="拒绝退货" width="420px">
      <div v-if="returnTargetOrder.id" style="margin-bottom:16px;">
        <div style="margin-bottom:8px;">订单号：<strong>{{ returnTargetOrder.orderNo }}</strong></div>
        <div>退货原因：{{ returnTargetOrder.returnReason }}</div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="拒绝原因">
          <el-input v-model="returnNotes" placeholder="可选，拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectReturnDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="returnLoading" @click="confirmRejectReturn">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Money, Van, CircleCheck } from '@element-plus/icons-vue'
import { getAllOrders, shipOrder, updateOrderStatus, approveReturn, rejectReturn } from '@/api/order'

// ===== 统计 =====
const statsLoading = ref(false)
const statCards = ref([
  { title: '今日订单数', value: 0, icon: 'ShoppingCart', class: 'stat-primary' },
  { title: '总销售额（¥）', value: '0.00', icon: 'Money', class: 'stat-success' },
  { title: '待发货数量', value: 0, icon: 'Van', class: 'stat-warning' },
  { title: '已完成订单数', value: 0, icon: 'CircleCheck', class: 'stat-info' }
])

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res = await getAllOrders({ pageSize: 1000, pageNum: 1 })
    const list = res?.data?.records || res?.data?.list || []
    const today = new Date().toISOString().slice(0, 10)
    statCards.value[0].value = list.filter(o => (o.createTime || '').slice(0, 10) === today).length
    statCards.value[1].value = list
      .filter(o => ['PAID', 'SHIPPED', 'COMPLETED'].includes(o.status))
      .reduce((s, o) => s + (parseFloat(o.totalAmount) || 0), 0).toFixed(2)
    statCards.value[2].value = list.filter(o => o.status === 'PAID').length
    statCards.value[3].value = list.filter(o => o.status === 'COMPLETED').length
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    statsLoading.value = false
  }
}

// ===== 订单列表 =====
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const queryForm = reactive({ orderNo: '', status: '', pageNum: 1, pageSize: 10 })

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getAllOrders(queryForm)
    const data = res?.data || {}
    orders.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryForm.pageNum = 1; loadOrders() }
const handleReset = () => { queryForm.orderNo = ''; queryForm.status = ''; queryForm.pageNum = 1; loadOrders() }

// ===== 详情 Dialog =====
const detailDialogVisible = ref(false)
const currentOrder = ref({})

const handleViewDetail = (row) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

// ===== 发货 Dialog =====
const shipDialogVisible = ref(false)
const shipLoading = ref(false)
const shipForm = reactive({ orderId: null, trackingNumber: '' })

const openShipDialog = (row) => {
  shipForm.orderId = row.id
  shipForm.trackingNumber = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  shipLoading.value = true
  try {
    await shipOrder(shipForm.orderId, shipForm.trackingNumber)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadOrders()
    loadStats()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发货失败')
  } finally {
    shipLoading.value = false
  }
}

// ===== 改状态 Dialog =====
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const newStatus = ref('')
const statusTargetId = ref(null)

const openStatusDialog = (row) => {
  statusTargetId.value = row.id
  newStatus.value = row.status
  statusDialogVisible.value = true
}

const confirmStatusChange = async () => {
  statusLoading.value = true
  try {
    await updateOrderStatus(statusTargetId.value, newStatus.value)
    ElMessage.success('状态已更新')
    statusDialogVisible.value = false
    loadOrders()
    loadStats()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  } finally {
    statusLoading.value = false
  }
}

// ===== 退货处理 =====
const approveReturnDialogVisible = ref(false)
const rejectReturnDialogVisible = ref(false)
const returnLoading = ref(false)
const returnTargetOrder = ref({})
const returnNotes = ref('')

const openApproveReturnDialog = (row) => {
  returnTargetOrder.value = row
  returnNotes.value = ''
  approveReturnDialogVisible.value = true
}

const openRejectReturnDialog = (row) => {
  returnTargetOrder.value = row
  returnNotes.value = ''
  rejectReturnDialogVisible.value = true
}

const confirmApproveReturn = async () => {
  returnLoading.value = true
  try {
    await approveReturn(returnTargetOrder.value.id, returnNotes.value)
    ElMessage.success('退货已审批，退款完成')
    approveReturnDialogVisible.value = false
    loadOrders()
    loadStats()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '审批失败')
  } finally {
    returnLoading.value = false
  }
}

const confirmRejectReturn = async () => {
  returnLoading.value = true
  try {
    await rejectReturn(returnTargetOrder.value.id, returnNotes.value)
    ElMessage.success('退货已拒绝')
    rejectReturnDialogVisible.value = false
    loadOrders()
    loadStats()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    returnLoading.value = false
  }
}

// ===== 工具函数 =====
const STATUS_MAP = {
  PENDING: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付', type: 'primary' },
  SHIPPED: { label: '已发货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' },
  RETURNING: { label: '退货中', type: 'warning' },
  RETURNED: { label: '已退货', type: 'info' }
}
const getStatusLabel = (s) => STATUS_MAP[s]?.label || s
const getStatusType = (s) => STATUS_MAP[s]?.type || 'info'
const formatDateTime = (dt) => dt ? new Date(dt).toLocaleString('zh-CN') : '—'

onMounted(() => { loadStats(); loadOrders() })
</script>

<style scoped>
.order-mgmt { padding: 0; }

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
}
.page-title { font-size: 20px; font-weight: 700; color: #111827; }
.page-sub { font-size: 13px; color: #9ca3af; margin-top: 2px; }

/* 统计卡片 */
.stats-row { margin-bottom: 16px; }
.stat-card {
  background: white;
  border-radius: 8px;
  padding: 18px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
  height: 100%;
  box-sizing: border-box;
}
.stat-card.stat-primary .stat-icon { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.stat-card.stat-success .stat-icon { background: #dcfce7; color: #16a34a; }
.stat-card.stat-warning .stat-icon { background: #fff7ed; color: #ea580c; }
.stat-card.stat-info    .stat-icon { background: #f3e8ff; color: #9333ea; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-value { font-size: 26px; font-weight: 700; color: #111827; margin-bottom: 2px; }
.stat-title { font-size: 13px; color: #9ca3af; }

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 详情 Dialog */
.detail-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.detail-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}
.total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #dc2626;
}
</style>
