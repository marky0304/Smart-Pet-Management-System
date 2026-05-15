<template>
  <div class="service-page">
    <!-- 标签页 -->
    <div class="page-tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'services' }" @click="activeTab = 'services'">
        📅 服务预约
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'appointments' }" @click="activeTab = 'appointments'; loadAppointments()">
        📋 我的预约
        <span class="tab-badge" v-if="pendingCount > 0">{{ pendingCount }}</span>
      </button>
    </div>

    <!-- ===== 服务预约 ===== -->
    <div v-show="activeTab === 'services'">
      <!-- 分类筛选 -->
      <div class="filter-bar">
        <button v-for="cat in categories" :key="cat.value"
          class="cat-btn" :class="{ active: selectedCategory === cat.value }"
          @click="selectedCategory = cat.value; loadServices()">
          {{ cat.icon }} {{ cat.label }}
        </button>
      </div>

      <!-- 服务卡片网格 -->
      <div class="service-grid" v-loading="servicesLoading">
        <div class="service-card" v-for="svc in services" :key="svc.id" @click="showBookDialog(svc)">
          <div class="service-img-wrap">
            <img :src="getServiceImage(svc)" class="service-img" :alt="svc.name" />
            <span class="service-cat-tag" :class="'cat-' + (svc.category || '').toLowerCase()">
              {{ getCategoryName(svc.category) }}
            </span>
          </div>
          <div class="service-body">
            <div class="service-name">{{ svc.name }}</div>
            <div class="service-rating">
              <span class="stars">★★★★★</span>
              <span class="rating-num">4.9</span>
              <span class="sales">· 月销 {{ svc.id * 23 + 50 }}+</span>
            </div>
            <p class="service-desc">{{ svc.description }}</p>
            <div class="service-footer">
              <div class="service-price">
                <span class="price-symbol">¥</span>
                <span class="price-num">{{ svc.price }}</span>
                <span class="price-unit" v-if="svc.duration">/ {{ svc.duration }}分钟</span>
              </div>
              <button class="book-btn" @click.stop="showBookDialog(svc)">立即预约</button>
            </div>
          </div>
        </div>
        <el-empty v-if="!servicesLoading && services.length === 0" description="暂无服务项目" />
      </div>

      <el-pagination v-if="servicesTotal > 8"
        v-model:current-page="servicesPageNum" v-model:page-size="servicesPageSize"
        :total="servicesTotal" :page-sizes="[8, 16]"
        layout="total, prev, pager, next"
        @current-change="loadServices" class="pagination" />
    </div>

    <!-- ===== 我的预约 ===== -->
    <div v-show="activeTab === 'appointments'">
      <div class="filter-bar">
        <el-select v-model="appointmentStatus" placeholder="全部状态" clearable @change="loadAppointments" style="width:140px">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button class="btn-brand" @click="loadAppointments">刷新</el-button>
      </div>

      <div class="table-card" v-loading="appointmentsLoading">
        <el-table :data="appointments" stripe style="width:100%">
          <el-table-column prop="orderNo" label="订单号" width="170" />
          <el-table-column prop="serviceName" label="服务" width="130" />
          <el-table-column prop="petName" label="宠物" width="90" />
          <el-table-column label="预约时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.appointmentDate) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="90">
            <template #default="{ row }">
              <span class="price-text">¥{{ row.totalPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-tag" :class="'status-' + (row.status || '').toLowerCase()">
                {{ getStatusName(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="130">
            <template #default="{ row }">
              <el-rate v-if="row.rating" :model-value="row.rating" disabled size="small" />
              <span v-else class="val-empty">未评价</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewAppointment(row)">详情</el-button>
              <el-button link type="danger" @click="handleCancelAppointment(row)"
                v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'">取消</el-button>
              <el-button link type="success" @click="handleReviewAppointment(row)"
                v-if="row.status === 'COMPLETED' && !row.rating">评价</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!appointmentsLoading && appointments.length === 0" description="暂无预约记录" />
        <el-pagination v-if="appointmentsTotal > 0"
          v-model:current-page="appointmentsPageNum" v-model:page-size="appointmentsPageSize"
          :total="appointmentsTotal" :page-sizes="[10, 20]"
          layout="total, prev, pager, next"
          @current-change="loadAppointments" class="pagination" />
      </div>
    </div>

    <!-- 预约对话框 -->
    <el-dialog v-model="bookDialogVisible" title="预约服务" width="560px" :close-on-click-modal="false">
      <el-form :model="bookForm" :rules="bookRules" ref="bookFormRef" label-width="100px">
        <el-form-item label="服务">
          <div class="dialog-service-info">
            <span class="dialog-service-name">{{ currentService.name }}</span>
            <span class="dialog-service-price">¥{{ currentService.price }}</span>
          </div>
        </el-form-item>
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="bookForm.petId" placeholder="请选择宠物" style="width:100%">
            <el-option v-for="pet in pets" :key="pet.id"
              :label="`${pet.name}（${getPetTypeName(pet.type)}·${pet.breed || '未知品种'}）`"
              :value="pet.id" />
          </el-select>
          <div class="no-pet-tip" v-if="pets.length === 0">
            还没有宠物？<el-button link type="primary" @click="goToAddPet">立即添加</el-button>
          </div>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentDate">
          <el-date-picker v-model="bookForm.appointmentDate" type="datetime"
            placeholder="选择日期时间" style="width:100%"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            format="YYYY-MM-DD HH:mm" />
          <div class="time-tip">营业时间：09:00 - 18:00</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bookForm.notes" type="textarea" :rows="3" placeholder="如有特殊需求请备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button class="btn-brand" @click="handleBook" :loading="bookLoading">确认预约</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="560px">
      <el-descriptions :column="2" border v-if="currentAppointment">
        <el-descriptions-item label="订单号" :span="2">{{ currentAppointment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="服务">{{ currentAppointment.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="宠物">{{ currentAppointment.petName }}</el-descriptions-item>
        <el-descriptions-item label="预约时间" :span="2">{{ formatDateTime(currentAppointment.appointmentDate) }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentAppointment.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <span class="status-tag" :class="'status-' + (currentAppointment.status || '').toLowerCase()">
            {{ getStatusName(currentAppointment.status) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAppointment.notes || '无' }}</el-descriptions-item>
        <el-descriptions-item label="评分" :span="2" v-if="currentAppointment.rating">
          <el-rate :model-value="currentAppointment.rating" disabled />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="评价服务" width="480px">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" :colors="['#f56c6c','#e6a23c','#67c23a']" />
        </el-form-item>
        <el-form-item label="评价" prop="review">
          <el-input v-model="reviewForm.review" type="textarea" :rows="4" placeholder="分享您的服务体验..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button class="btn-brand" @click="handleSubmitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceList } from '@/api/service'
import { createAppointment, getMyAppointments, cancelAppointment, addReview } from '@/api/appointment'
import { getMyPetList } from '@/api/pet'

const activeTab = ref('services')

// 分类
const categories = [
  { value: '', label: '全部', icon: '🐾' },
  { value: 'BATH', label: '洗澡', icon: '🛁' },
  { value: 'GROOM', label: '美容', icon: '✂️' },
  { value: 'BOARD', label: '托管', icon: '🏠' },
  { value: 'MEDICAL', label: '医疗', icon: '🩺' },
  { value: 'TRAIN', label: '训练', icon: '🎓' },
  { value: 'OTHER', label: '其他', icon: '📦' }
]

// 服务列表
const services = ref([])
const servicesLoading = ref(false)
const servicesTotal = ref(0)
const servicesPageNum = ref(1)
const servicesPageSize = ref(8)
const selectedCategory = ref('')

// 预约列表
const appointments = ref([])
const appointmentsLoading = ref(false)
const appointmentsTotal = ref(0)
const appointmentsPageNum = ref(1)
const appointmentsPageSize = ref(10)
const appointmentStatus = ref('')
const pendingCount = computed(() => appointments.value.filter(a => a.status === 'PENDING').length)

// 宠物列表
const pets = ref([])

// 预约对话框
const bookDialogVisible = ref(false)
const bookLoading = ref(false)
const currentService = ref({})
const bookFormRef = ref(null)
const bookForm = reactive({ petId: null, serviceId: null, appointmentDate: null, notes: '' })
const bookRules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

// 详情/评价对话框
const detailDialogVisible = ref(false)
const currentAppointment = ref(null)
const reviewDialogVisible = ref(false)
const reviewFormRef = ref(null)
const reviewForm = reactive({ appointmentId: null, rating: 5, review: '' })
const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  review: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

onMounted(() => { loadServices(); loadPets(); loadAppointments() })

// 服务图片 - 数据库 image_url 优先，分类兜底
const getServiceImage = (svc) => {
  if (svc.image) return svc.image
  const catMap = {
    BATH:    '/service image/small dog bath.jpg',
    GROOM:   '/service image/small dog haircut.jpg',
    BOARD:   '/service image/small dog room.jpg',
    MEDICAL: '/service image/checkup-basic.jpg',
    TRAIN:   '/service image/train-basic.jpg',
    OTHER:   '/service image/home-service.jpg',
  }
  return catMap[svc.category] || '/service image/pet spa.jpg'
}

const getCategoryName = (cat) => ({ BATH:'洗澡', GROOM:'美容', BOARD:'托管', MEDICAL:'医疗', TRAIN:'训练', OTHER:'其他' }[cat] || cat)
const getPetTypeName = (type) => ({ DOG:'狗', CAT:'猫', BIRD:'鸟', OTHER:'其他' }[type] || type)

const loadServices = async () => {
  servicesLoading.value = true
  try {
    const params = { status: 1, pageNum: servicesPageNum.value, pageSize: servicesPageSize.value }
    if (selectedCategory.value) params.category = selectedCategory.value
    const res = await getServiceList(params)
    services.value = res.data?.records || []
    servicesTotal.value = res.data?.total || 0
  } catch (e) { services.value = [] }
  finally { servicesLoading.value = false }
}

const loadPets = async () => {
  try {
    const res = await getMyPetList()
    pets.value = Array.isArray(res.data) ? res.data : []
  } catch (e) { pets.value = [] }
}

const loadAppointments = async () => {
  appointmentsLoading.value = true
  try {
    const res = await getMyAppointments({ status: appointmentStatus.value, pageNum: appointmentsPageNum.value, pageSize: appointmentsPageSize.value })
    appointments.value = res.data?.records || []
    appointmentsTotal.value = res.data?.total || 0
  } catch (e) { appointments.value = [] }
  finally { appointmentsLoading.value = false }
}

const showBookDialog = (svc) => {
  currentService.value = svc
  bookForm.serviceId = svc.id
  bookForm.petId = null
  bookForm.appointmentDate = null
  bookForm.notes = ''
  bookDialogVisible.value = true
}

const handleBook = async () => {
  await bookFormRef.value.validate()
  bookLoading.value = true
  try {
    let dt = bookForm.appointmentDate
    if (dt instanceof Date) {
      const pad = n => String(n).padStart(2, '0')
      dt = `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())}T${pad(dt.getHours())}:${pad(dt.getMinutes())}:00`
    }
    await createAppointment({ petId: bookForm.petId, serviceId: bookForm.serviceId, appointmentDate: dt, notes: bookForm.notes || '' })
    ElMessage.success('预约成功！')
    bookDialogVisible.value = false
    await loadAppointments()
    activeTab.value = 'appointments'
  } catch (e) {
    ElMessage.error('预约失败，请稍后重试')
  } finally { bookLoading.value = false }
}

const handleViewAppointment = (row) => { currentAppointment.value = row; detailDialogVisible.value = true }

const handleCancelAppointment = (row) => {
  ElMessageBox.confirm('确定要取消这个预约吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      await cancelAppointment(row.id)
      ElMessage.success('取消成功')
      // 乐观更新
      const idx = appointments.value.findIndex(a => a.id === row.id)
      if (idx !== -1) appointments.value[idx].status = 'CANCELLED'
    }).catch(() => {})
}

const handleReviewAppointment = (row) => {
  reviewForm.appointmentId = row.id; reviewForm.rating = 5; reviewForm.review = ''
  reviewDialogVisible.value = true
}

const handleSubmitReview = () => {
  reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    await addReview(reviewForm)
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadAppointments()
  })
}

const disabledDate = (t) => t.getTime() < Date.now() - 8.64e7
const disabledHours = () => { const h = []; for(let i=0;i<9;i++) h.push(i); for(let i=18;i<24;i++) h.push(i); return h }

const goToAddPet = () => { bookDialogVisible.value = false; router.push('/dashboard/pet') }
const formatDateTime = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''
const getStatusName = (s) => ({ PENDING:'待确认', CONFIRMED:'已确认', COMPLETED:'已完成', CANCELLED:'已取消' }[s] || s)
</script>

<style scoped>
.service-page { padding: 0; }

/* 品牌按钮 */
.btn-brand { background: #5C8AEB !important; border-color: #5C8AEB !important; color: #fff !important; font-weight: 600; }
.btn-brand:hover { background: #4A78D6 !important; }

/* 标签页 */
.page-tabs {
  display: flex; gap: 4px;
  background: #fff; padding: 16px 24px 0;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 0;
  border-bottom: 2px solid #f0f0f0;
}
.tab-btn {
  padding: 10px 24px; border: none; background: transparent;
  font-size: 15px; font-weight: 500; color: #6b7280; cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
  transition: all 0.2s; position: relative;
}
.tab-btn.active { color: #5C8AEB; border-bottom-color: #5C8AEB; }
.tab-btn:hover { color: #5C8AEB; }
.tab-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 18px; height: 18px; border-radius: 50%;
  background: #dc2626; color: #fff; font-size: 11px; font-weight: 700;
  margin-left: 6px;
}

/* 筛选栏 */
.filter-bar {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  background: #fff; padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 16px;
}
.cat-btn {
  padding: 7px 18px; border-radius: 20px; border: 1px solid #e5e7eb;
  background: #fff; color: #374151; font-size: 14px; cursor: pointer;
  transition: all 0.2s; font-weight: 500;
}
.cat-btn.active, .cat-btn:hover { background: #5C8AEB; color: #fff; border-color: #5C8AEB; }

/* 服务网格 */
.service-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;
  margin-bottom: 16px;
}
.service-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  transition: all 0.25s; cursor: pointer;
}
.service-card:hover { transform: translateY(-6px); box-shadow: 0 10px 28px rgba(0,0,0,0.13); }
.service-img-wrap { position: relative; overflow: hidden; height: 200px; }
.service-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.service-card:hover .service-img { transform: scale(1.06); }
.service-cat-tag {
  position: absolute; top: 10px; left: 10px;
  padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600;
}
.cat-bath { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.cat-groom { background: #fdf2f8; color: #db2777; }
.cat-board { background: #fff7ed; color: #ea580c; }
.cat-medical { background: #fef2f2; color: #dc2626; }
.cat-train { background: #f0fdf4; color: #16a34a; }
.cat-other { background: #f5f3ff; color: #8b5cf6; }

.service-body { padding: 14px 16px; }
.service-name { font-size: 17px; font-weight: 700; color: #111827; margin-bottom: 6px; }
.service-rating { display: flex; align-items: center; gap: 4px; margin-bottom: 8px; }
.stars { color: #f59e0b; font-size: 13px; }
.rating-num { font-size: 13px; font-weight: 600; color: #374151; }
.sales { font-size: 12px; color: #9ca3af; }
.service-desc {
  font-size: 13px; color: #6b7280; line-height: 1.5; margin-bottom: 12px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.service-footer { display: flex; justify-content: space-between; align-items: center; }
.service-price { display: flex; align-items: baseline; gap: 2px; }
.price-symbol { font-size: 13px; color: #dc2626; font-weight: 600; }
.price-num { font-size: 24px; font-weight: 800; color: #dc2626; }
.price-unit { font-size: 12px; color: #9ca3af; }
.book-btn {
  padding: 7px 18px; background: #5C8AEB; color: #fff;
  border: none; border-radius: 20px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
}
.book-btn:hover { background: #4A78D6; transform: translateY(-1px); }

/* 预约表格 */
.table-card {
  background: #fff; border-radius: 8px; padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.price-text { font-weight: 600; color: #dc2626; }
.val-empty { color: #d1d5db; font-size: 13px; }

/* 状态标签 */
.status-tag { padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-pending { background: #fff7ed; color: #ea580c; }
.status-confirmed { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.status-completed { background: #f0fdf4; color: #16a34a; }
.status-cancelled { background: #f3f4f6; color: #9ca3af; }

/* 预约对话框 */
.dialog-service-info { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.dialog-service-name { font-size: 16px; font-weight: 600; color: #111827; }
.dialog-service-price { font-size: 20px; font-weight: 800; color: #dc2626; }
.no-pet-tip { font-size: 13px; color: #9ca3af; margin-top: 6px; }
.time-tip { font-size: 12px; color: #9ca3af; margin-top: 4px; }

.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>
