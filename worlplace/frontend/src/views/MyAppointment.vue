<template>
  <div class="appointment-container">
    <el-card class="header-card">
      <h2>我的预约</h2>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="选择状态" clearable @change="handleQuery">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
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
              type="warning" 
              @click="handleCancel(row)"
              v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'"
            >
              取消
            </el-button>
            <el-button 
              link 
              type="success" 
              @click="handleReview(row)"
              v-if="row.status === 'COMPLETED' && !row.rating"
            >
              评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="600px">
      <el-descriptions :column="2" border v-if="currentAppointment">
        <el-descriptions-item label="订单号" :span="2">{{ currentAppointment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="服务">{{ currentAppointment.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="宠物">{{ currentAppointment.petName }}</el-descriptions-item>
        <el-descriptions-item label="预约时间" :span="2">
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
      </el-descriptions>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="评价服务" width="500px">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容" prop="review">
          <el-input 
            v-model="reviewForm.review" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入您的评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAppointments, cancelAppointment, addReview } from '@/api/appointment'

const loading = ref(false)
const appointments = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentAppointment = ref(null)

const queryForm = reactive({
  status: '',
  pageNum: 1,
  pageSize: 10
})

const reviewForm = reactive({
  appointmentId: null,
  rating: 5,
  review: ''
})

const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  review: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

const reviewFormRef = ref(null)

onMounted(() => {
  loadAppointments()
})

const loadAppointments = async () => {
  loading.value = true
  try {
    const res = await getMyAppointments(queryForm)

    // 适配不同的响应格式
    if (res && res.data) {
      // 如果data.records存在（分页格式）
      if (res.data.records && Array.isArray(res.data.records)) {
        appointments.value = res.data.records
        total.value = res.data.total || 0
      }
      // 如果data直接是数组
      else if (Array.isArray(res.data)) {
        appointments.value = res.data
        total.value = res.data.length
      }
      // 如果data.data存在（嵌套格式）
      else if (res.data.data) {
        if (res.data.data.records && Array.isArray(res.data.data.records)) {
          appointments.value = res.data.data.records
          total.value = res.data.data.total || 0
        } else if (Array.isArray(res.data.data)) {
          appointments.value = res.data.data
          total.value = res.data.data.length
        }
      }
      // 其他情况
      else {
        appointments.value = []
        total.value = 0
      }
    } else {
      appointments.value = []
      total.value = 0
    }
  } catch (error) {
    appointments.value = []
    total.value = 0
    ElMessage.error('加载预约列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.pageNum = 1
  loadAppointments()
}

const handleReset = () => {
  queryForm.status = ''
  queryForm.pageNum = 1
  loadAppointments()
}

const handleView = (row) => {
  currentAppointment.value = row
  detailDialogVisible.value = true
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelAppointment(row.id)
      ElMessage.success('取消成功')
      // 如果当前没有筛选"已取消"，直接从列表移除该条
      if (queryForm.status !== 'CANCELLED') {
        appointments.value = appointments.value.filter(a => a.id !== row.id)
        total.value = Math.max(0, total.value - 1)
      } else {
        // 筛选了已取消，刷新列表
        loadAppointments()
      }
    } catch (error) {
      ElMessage.error('取消失败')
    }
  })
}

const handleReview = (row) => {
  reviewForm.appointmentId = row.id
  reviewForm.rating = 5
  reviewForm.review = ''
  reviewDialogVisible.value = true
}

const handleSubmitReview = () => {
  reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addReview(reviewForm)
        ElMessage.success('评价成功')
        reviewDialogVisible.value = false
        loadAppointments()
      } catch (error) {
        ElMessage.error('评价失败')
      }
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
.appointment-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0;
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
