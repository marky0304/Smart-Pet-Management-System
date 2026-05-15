<template>
  <div class="health-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-text">健康管理</span>
        <span class="title-count" v-if="total > 0">共 {{ total }} 条记录</span>
      </div>
      <el-button class="btn-brand" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加健康记录
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="queryForm.petId" placeholder="选择宠物" clearable @change="handleQuery" class="filter-item">
        <el-option v-for="pet in pets" :key="pet.id" :label="pet.name" :value="pet.id" />
      </el-select>
      <el-select v-model="queryForm.recordType" placeholder="记录类型" clearable @change="handleQuery" class="filter-item">
        <el-option label="体检" value="CHECKUP" />
        <el-option label="疫苗" value="VACCINE" />
        <el-option label="疾病" value="ILLNESS" />
        <el-option label="体重记录" value="WEIGHT" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期"
        @change="handleQuery" class="filter-date" />
      <el-button class="btn-brand" @click="handleQuery"><el-icon><Search /></el-icon> 查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 健康统计图表区（始终显示） -->
    <div class="charts-section">
      <div class="charts-header">
        <span class="charts-title">📊 健康数据统计</span>
        <span class="charts-sub" v-if="selectedPetId">当前宠物：{{ selectedPetName }}</span>
        <span class="charts-sub" v-else>选择宠物后可查看趋势图</span>
      </div>
      <el-row :gutter="16">
        <!-- 记录类型分布 -->
        <el-col :span="6">
          <div class="chart-card">
            <div class="chart-title">记录类型分布</div>
            <div ref="typeChart" class="chart-box"></div>
          </div>
        </el-col>
        <!-- 月度记录数量 -->
        <el-col :span="10">
          <div class="chart-card">
            <div class="chart-title">近6个月记录数量</div>
            <div ref="monthChart" class="chart-box"></div>
          </div>
        </el-col>
        <!-- 统计卡片 -->
        <el-col :span="8">
          <div class="stat-grid">
            <div class="stat-item stat-blue">
              <div class="stat-num">{{ statCounts.total }}</div>
              <div class="stat-label">总记录数</div>
            </div>
            <div class="stat-item stat-green">
              <div class="stat-num">{{ statCounts.checkup }}</div>
              <div class="stat-label">体检次数</div>
            </div>
            <div class="stat-item stat-purple">
              <div class="stat-num">{{ statCounts.vaccine }}</div>
              <div class="stat-label">疫苗次数</div>
            </div>
            <div class="stat-item stat-red">
              <div class="stat-num">{{ statCounts.illness }}</div>
              <div class="stat-label">就诊次数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 趋势图（选择宠物后显示） -->
      <el-row :gutter="16" style="margin-top:16px" v-if="selectedPetId">
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-title">体重趋势 (kg)</div>
            <div ref="weightChart" class="chart-box"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-title">体温趋势 (℃)</div>
            <div ref="temperatureChart" class="chart-box"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="records"
        stripe
        style="width:100%"
        :row-class-name="getRowClass"
        v-loading="loading"
      >
        <el-table-column prop="petName" label="宠物" width="100">
          <template #default="{ row }">
            <span class="pet-name-tag">{{ row.petName || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="recordTypeName" label="类型" width="90">
          <template #default="{ row }">
            <span class="type-tag" :class="'type-' + (row.recordType || '').toLowerCase()">
              {{ getTypeName(row.recordType) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="recordDate" label="记录日期" width="160">
          <template #default="{ row }">{{ formatDate(row.recordDate) }}</template>
        </el-table-column>

        <el-table-column prop="weight" label="体重(kg)" width="95">
          <template #default="{ row }">
            <span v-if="row.weight" :class="isAbnormalWeight(row.weight) ? 'val-abnormal' : ''">
              {{ row.weight }}
            </span>
            <span v-else class="val-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="temperature" label="体温(℃)" width="95">
          <template #default="{ row }">
            <span v-if="row.temperature">
              <el-tag v-if="isHighFever(row.temperature)" type="danger" size="small">{{ row.temperature }} 偏高</el-tag>
              <span v-else class="val-normal">{{ row.temperature }}</span>
            </span>
            <span v-else class="val-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="symptoms" label="症状" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.symptoms" :class="row.symptoms !== '无不适' ? 'val-warning' : ''">{{ row.symptoms }}</span>
            <span v-else class="val-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.diagnosis">
              <el-tag v-if="isAbnormalDiagnosis(row.diagnosis)" type="danger" size="small" effect="light">{{ row.diagnosis }}</el-tag>
              <span v-else>{{ row.diagnosis }}</span>
            </span>
            <span v-else class="val-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="hospitalName" label="医院" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.hospitalName">{{ row.hospitalName }}</span>
            <span v-else class="val-empty">—</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
      <el-empty v-if="!loading && records.length === 0" description="暂无健康记录" />
    </div>

    <!-- 添加/编辑/查看对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="宠物" prop="petId">
              <el-select v-model="form.petId" placeholder="选择宠物" style="width:100%" :disabled="isView">
                <el-option v-for="pet in pets" :key="pet.id" :label="pet.name" :value="pet.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记录类型" prop="recordType">
              <el-select v-model="form.recordType" placeholder="选择类型" style="width:100%" :disabled="isView">
                <el-option label="体检" value="CHECKUP" />
                <el-option label="疫苗" value="VACCINE" />
                <el-option label="疾病" value="ILLNESS" />
                <el-option label="体重记录" value="WEIGHT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="记录日期" prop="recordDate">
              <el-date-picker v-model="form.recordDate" type="datetime" placeholder="选择日期时间" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weight" :precision="2" :min="0" :max="100" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体温(℃)">
              <el-input-number v-model="form.temperature" :precision="1" :min="35" :max="42" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用(元)">
              <el-input-number v-model="form.cost" :precision="2" :min="0" style="width:100%" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="症状描述">
          <el-input v-model="form.symptoms" type="textarea" :rows="2" placeholder="如：无不适 / 呕吐、腹泻 / 食欲不振" :disabled="isView" />
        </el-form-item>
        <el-form-item label="诊断结果">
          <el-input v-model="form.diagnosis" type="textarea" :rows="2" :disabled="isView" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatment" type="textarea" :rows="2" :disabled="isView" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医院名称">
              <el-input v-model="form.hospitalName" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生姓名">
              <el-input v-model="form.doctorName" :disabled="isView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用药记录">
          <el-input v-model="form.medicine" type="textarea" :rows="2" :disabled="isView" />
        </el-form-item>
        <el-form-item label="下次复诊">
          <el-date-picker v-model="form.nextVisitDate" type="date" placeholder="选择日期" style="width:100%" :disabled="isView" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" :disabled="isView" />
        </el-form-item>
      </el-form>
      <template #footer v-if="!isView">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-brand" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHealthRecordList, createHealthRecord, updateHealthRecord, deleteHealthRecord, getHealthTrend } from '@/api/health'
import request from '@/utils/request'
import { Plus, Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const pets = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const typeChart = ref(null)
const monthChart = ref(null)
let typeChartInstance = null
let monthChartInstance = null

const statCounts = reactive({ total: 0, checkup: 0, vaccine: 0, illness: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isView = ref(false)
const selectedPetId = ref(null)
const dateRange = ref([])
const weightChart = ref(null)
const temperatureChart = ref(null)
let weightChartInstance = null
let temperatureChartInstance = null
const formRef = ref(null)

const selectedPetName = computed(() => {
  const pet = pets.value.find(p => p.id === selectedPetId.value)
  return pet ? pet.name : ''
})

const queryForm = reactive({ petId: null, recordType: '', startDate: null, endDate: null, pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, petId: null, recordType: '', recordDate: null, weight: null, temperature: null, symptoms: '', diagnosis: '', treatment: '', hospitalName: '', doctorName: '', medicine: '', cost: null, nextVisitDate: null, notes: '' })
const rules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  recordType: [{ required: true, message: '请选择记录类型', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
}

// 异常判断
const isHighFever = (temp) => temp > 39.5
const isAbnormalWeight = (w) => w < 0.1 || w > 80
const isAbnormalDiagnosis = (d) => d && ['炎症','肠胃炎','感染','骨折','肿瘤','异常'].some(k => d.includes(k))

const getRowClass = ({ row }) => {
  if (row.temperature && isHighFever(parseFloat(row.temperature))) return 'row-danger'
  if (isAbnormalDiagnosis(row.diagnosis)) return 'row-warning'
  return ''
}

const getTypeName = (type) => ({ CHECKUP: '体检', VACCINE: '疫苗', ILLNESS: '疾病', WEIGHT: '体重' }[type] || type)

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(() => { loadPets(); loadRecords() })
watch(() => queryForm.petId, (val) => {
  selectedPetId.value = val
  if (val) loadHealthTrend(val)
})

const loadPets = async () => {
  try {
    const res = await request({ url: '/pet/my', method: 'get' })
    // request.js 拦截器已解包，res 直接是 data 字段（数组）
    pets.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    pets.value = []
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getHealthRecordList(queryForm)
    const pageData = res?.data
    records.value = pageData?.records || []
    total.value = pageData?.total || 0
    // 更新统计
    await nextTick()
    updateStats(records.value)
  } catch (e) {
    console.error('loadRecords error:', e)
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadHealthTrend = async (petId) => {
  try {
    const res = await getHealthTrend(petId)
    const trendData = res?.data || res
    await nextTick()
    renderWeightChart(trendData?.weightTrend || [])
    renderTemperatureChart(trendData?.temperatureTrend || [])
  } catch (e) {}
}

const updateStats = (list) => {
  statCounts.total = list.length
  // 兼容中英文 record_type
  statCounts.checkup = list.filter(r => r.recordType === 'CHECKUP' || r.recordType === '体检').length
  statCounts.vaccine = list.filter(r => r.recordType === 'VACCINE' || r.recordType === '疫苗').length
  statCounts.illness = list.filter(r => r.recordType === 'ILLNESS' || r.recordType === '疾病' || r.recordType === '治疗').length
  renderTypeChart(list)
  renderMonthChart(list)
}

const renderTypeChart = (list) => {
  if (!typeChart.value) return
  if (typeChartInstance) typeChartInstance.dispose()
  typeChartInstance = echarts.init(typeChart.value)
  // 兼容中英文
  const normalize = (t) => {
    const map = { CHECKUP:'体检', VACCINE:'疫苗', ILLNESS:'疾病', WEIGHT:'体重', '治疗':'疾病' }
    return map[t] || t
  }
  const counts = {}
  list.forEach(r => {
    const k = normalize(r.recordType)
    counts[k] = (counts[k] || 0) + 1
  })
  const data = Object.entries(counts).map(([name, value]) => ({ name, value }))
  typeChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
    color: ['#5C8AEB', '#16a34a', '#dc2626', '#f59e0b'],
    series: [{
      type: 'pie', radius: ['40%', '68%'],
      center: ['50%', '44%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold' } },
      data: data.length ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e5e7eb' } }]
    }]
  })
}

const renderMonthChart = (list) => {
  if (!monthChart.value) return
  if (monthChartInstance) monthChartInstance.dispose()
  monthChartInstance = echarts.init(monthChart.value)
  const months = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`)
  }
  const normalize = (t) => {
    const map = { CHECKUP:'体检', VACCINE:'疫苗', ILLNESS:'疾病', WEIGHT:'体重', '治疗':'疾病' }
    return map[t] || t
  }
  const series = ['CHECKUP','VACCINE','ILLNESS','WEIGHT'].map((type, idx) => ({
    name: normalize(type),
    type: 'bar', stack: 'total',
    barMaxWidth: 32,
    color: ['#5C8AEB','#16a34a','#dc2626','#f59e0b'][idx],
    data: months.map(m => list.filter(r => {
      const rt = normalize(r.recordType)
      return rt === normalize(type) && (r.recordDate || '').startsWith(m)
    }).length)
  }))
  monthChartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 10 } },
    grid: { left: 30, right: 10, bottom: 40, top: 20 },
    xAxis: { type: 'category', data: months.map(m => m.slice(5)+'月'),
             axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 10 } },
    series
  })
}

const renderWeightChart = (data) => {
  if (!weightChart.value) return
  if (weightChartInstance) weightChartInstance.dispose()
  weightChartInstance = echarts.init(weightChart.value)
  weightChartInstance.setOption({
    title: { text: '体重趋势 (kg)', left: 'center', textStyle: { fontSize: 14, color: '#374151' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 40, top: 50 },
    xAxis: { type: 'category', data: data.map(i => i.date), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', name: 'kg' },
    series: [{ data: data.map(i => i.value), type: 'line', smooth: true, itemStyle: { color: '#5C8AEB' }, areaStyle: { color: 'rgba(92,138,235,0.08)' } }]
  })
}

const renderTemperatureChart = (data) => {
  if (!temperatureChart.value) return
  if (temperatureChartInstance) temperatureChartInstance.dispose()
  temperatureChartInstance = echarts.init(temperatureChart.value)
  temperatureChartInstance.setOption({
    title: { text: '体温趋势 (℃)', left: 'center', textStyle: { fontSize: 14, color: '#374151' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 40, top: 50 },
    xAxis: { type: 'category', data: data.map(i => i.date), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', name: '℃', min: 36, max: 41 },
    series: [{
      data: data.map(i => i.value), type: 'line', smooth: true,
      itemStyle: { color: '#16a34a' }, areaStyle: { color: 'rgba(22,163,74,0.08)' },
      markLine: { data: [{ yAxis: 39.5, lineStyle: { color: '#dc2626' }, label: { formatter: '发烧线 39.5℃' } }] }
    }]
  })
}

const handleQuery = () => {
  if (dateRange.value?.length === 2) { queryForm.startDate = dateRange.value[0]; queryForm.endDate = dateRange.value[1] }
  else { queryForm.startDate = null; queryForm.endDate = null }
  queryForm.pageNum = 1; loadRecords()
}

const handleReset = () => {
  queryForm.petId = null; queryForm.recordType = ''; queryForm.startDate = null; queryForm.endDate = null
  dateRange.value = []; queryForm.pageNum = 1; loadRecords()
}

const showAddDialog = () => { resetForm(); dialogTitle.value = '添加健康记录'; isView.value = false; dialogVisible.value = true }
const handleView = (row) => { Object.assign(form, row); dialogTitle.value = '查看健康记录'; isView.value = true; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑健康记录'; isView.value = false; dialogVisible.value = true }

const handleDelete = (row) => {
  if (!row?.id) { ElMessage.error('记录ID无效'); return }
  ElMessageBox.confirm('确定要删除这条健康记录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        await deleteHealthRecord(row.id)
        // 立即从本地数组移除，确保视图响应
        const idx = records.value.findIndex(r => r.id === row.id)
        if (idx !== -1) records.value.splice(idx, 1)
        total.value = Math.max(0, total.value - 1)
        ElMessage.success('删除成功')
        // 后台静默刷新同步真实数据
        loadRecords()
      } catch (e) {
        ElMessage.error('删除失败，请重试')
        console.error('delete error:', e)
      }
    })
    .catch(() => {})
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      // 格式化日期字段为 yyyy-MM-dd，避免传 ISO 字符串导致后端解析失败
      const payload = { ...form }
      if (payload.recordDate) {
        const d = new Date(payload.recordDate)
        payload.recordDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
      }
      if (payload.nextVisitDate) {
        const d = new Date(payload.nextVisitDate)
        payload.nextVisitDate = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
      }

      if (payload.id) {
        await updateHealthRecord(payload.id, payload)
        ElMessage.success('更新成功')
      } else {
        await createHealthRecord(payload)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      await loadRecords()
      if (selectedPetId.value) loadHealthTrend(selectedPetId.value)
    } catch (e) { ElMessage.error(e.message || '操作失败') }
  })
}

const resetForm = () => {
  Object.assign(form, { id: null, petId: null, recordType: '', recordDate: null, weight: null, temperature: null, symptoms: '', diagnosis: '', treatment: '', hospitalName: '', doctorName: '', medicine: '', cost: null, nextVisitDate: null, notes: '' })
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.health-page { padding: 0; }

.btn-brand { background: #5C8AEB !important; border-color: #5C8AEB !important; color: #fff !important; font-weight: 600; }
.btn-brand:hover { background: #4A78D6 !important; }

/* 页头 */
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 20px 24px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.page-title { display: flex; align-items: center; gap: 10px; }
.title-text { font-size: 20px; font-weight: 700; color: #111827; }
.title-count { font-size: 13px; color: #9ca3af; background: #f3f4f6; padding: 2px 10px; border-radius: 20px; }

/* 筛选栏 */
.filter-bar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  background: #fff; padding: 16px 24px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.filter-item { width: 140px; }
.filter-date { width: 260px; }

/* 图表区域 */
.charts-section {
  background: #fff; border-radius: 8px; padding: 20px 24px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.charts-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.charts-title { font-size: 17px; font-weight: 600; color: #111827; }
.charts-sub { font-size: 13px; color: #5C8AEB; background: rgba(92,138,235,0.08); padding: 2px 10px; border-radius: 20px; }

.chart-card {
  background: #fafafa; border-radius: 8px; padding: 16px;
  border: 1px solid #e5e7eb; height: 100%;
}
.chart-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 10px; text-align: center; }
.chart-box { height: 240px; }

/* 统计卡片网格 */
.stat-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; height: 100%;
}
.stat-item {
  background: white; border-radius: 8px; padding: 18px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 2px solid;
}
.stat-blue { border-color: #5C8AEB; background: linear-gradient(135deg, rgba(92,138,235,0.08) 0%, rgba(92,138,235,0.1) 100%); }
.stat-green { border-color: #16a34a; background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); }
.stat-purple { border-color: #9333ea; background: linear-gradient(135deg, #faf5ff 0%, #f3e8ff 100%); }
.stat-red { border-color: #dc2626; background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%); }
.stat-num { font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.stat-blue .stat-num { color: #5C8AEB; }
.stat-green .stat-num { color: #16a34a; }
.stat-purple .stat-num { color: #9333ea; }
.stat-red .stat-num { color: #dc2626; }
.stat-label { font-size: 12px; color: #6b7280; }

/* 表格卡片 */
.table-card {
  background: #fff; border-radius: 8px; padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 类型标签 */
.type-tag { padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.type-checkup { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.type-vaccine { background: #f0fdf4; color: #16a34a; }
.type-illness { background: #fef2f2; color: #dc2626; }
.type-weight  { background: #fff7ed; color: #ea580c; }

/* 宠物名 */
.pet-name-tag { font-weight: 600; color: #374151; }

/* 数值样式 */
.val-normal { color: #374151; }
.val-abnormal { color: #dc2626; font-weight: 600; }
.val-warning { color: #d97706; }
.val-empty { color: #d1d5db; }

/* 行高亮 */
:deep(.row-danger td) { background: #fef2f2 !important; }
:deep(.row-warning td) { background: #fffbeb !important; }

/* 分页 */
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
