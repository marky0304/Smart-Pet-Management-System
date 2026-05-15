<template>
  <div class="appointment-management-container">
    <el-card class="header-card">
      <h2>预约管理</h2>
    </el-card>

    <el-card class="statistics-card">
      <h3>预约统计</h3>
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalAppointments }}</div>
            <div class="stat-label">总预约数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value warning">{{ statistics.pendingCount }}</div>
            <div class="stat-label">待确认</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value primary">{{ statistics.confirmedCount }}</div>
            <div class="stat-label">已确认</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value success">{{ statistics.completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value info">{{ statistics.cancelledCount }}</div>
            <div class="stat-label">已取消</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value revenue">¥{{ statistics.totalRevenue }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="选择状态" clearable>
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="appointments" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="serviceName" label="服务" width="120" />
        <el-table-column prop="petName" label="宠物" width="100" />
        <el-table-column prop="appointmentDate" label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate v-if="row.rating" :model-value="row.rating" disabled />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              @click="handleUpdateStatus(row, 'CONFIRMED')"
              v-if="row.status === 'PENDING'"
            >
              确认
            </el-button>
            <el-button 
              link 
              type="success" 
              @click="handleUpdateStatus(row, 'COMPLETED')"
              v-if="row.status === 'CONFIRMED'"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="600px">
      <el-descriptions :column="2" border v-if="currentAppointment">
        <el-descriptions-item label="订单号" :span="2">{{ currentAppointment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentAppointment.username }}</el-descriptions-item>
        <el-descriptions-item label="宠物">{{ currentAppointment.petName }}</el-descriptions-item>
        <el-descriptions-item label="服务">{{ currentAppointment.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatDateTime(currentAppointment.appointmentDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentAppointment.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentAppointment.status)">
            {{ currentAppointment.statusName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentAppointment.notes || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="评分" v-if="currentAppointment.rating">
          <el-rate :model-value="currentAppointment.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价" :span="2" v-if="currentAppointment.review">
          {{ currentAppointment.review }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentAppointment.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllAppointments, updateAppointmentStatus, getAppointmentStatistics } from '@/api/appointment'

const loading = ref(false)
const appointments = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentAppointment = ref(null)
const dateRange = ref([])

const statistics = reactive({
  totalAppointments: 0,
  pendingCount: 0,
  confirmedCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  totalRevenue: 0
})

const queryForm = reactive({
  status: '',
  startDate: null,
  endDate: null,
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  loadStatistics()
  loadAppointments()
})

const loadStatistics = async () => {
  try {
    const res = await getAppointmentStatistics()
    Object.assign(statistics, res.data)
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadAppointments = async () => {
  loading.value = true
  try {
    const res = await getAllAppointments(queryForm)
    appointments.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryForm.startDate = dateRange.value[0]
    queryForm.endDate = dateRange.value[1]
  } else {
    queryForm.startDate = null
    queryForm.endDate = null
  }
  queryForm.pageNum = 1
  loadAppointments()
}

const handleReset = () => {
  queryForm.status = ''
  queryForm.startDate = null
  queryForm.endDate = null
  dateRange.value = []
  queryForm.pageNum = 1
  loadAppointments()
}

const handleView = (row) => {
  currentAppointment.value = row
  detailDialogVisible.value = true
}

const handleUpdateStatus = (row, status) => {
  const statusText = status === 'CONFIRMED' ? '确认' : '完成'
  ElMessageBox.confirm(`确定要${statusText}这个预约吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateAppointmentStatus(row.id, status)
      ElMessage.success(`${statusText}成功`)
      loadAppointments()
      loadStatistics()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
    }
  })
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.appointment-management-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0;
}

.statistics-card {
  margin-bottom: 20px;
}

.statistics-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #5C8AEB;
  margin-bottom: 10px;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.primary {
  color: #5C8AEB;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.info {
  color: #909399;
}

.stat-value.revenue {
  color: #F56C6C;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
