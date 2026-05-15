<template>
  <div class="health-management-container">
    <el-card class="header-card">
      <h2>健康数据管理</h2>
    </el-card>

    <el-card class="statistics-card">
      <h3>健康数据统计</h3>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalRecords }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value warning">{{ statistics.abnormalCount }}</div>
            <div class="stat-label">异常数据</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value info">{{ statistics.upcomingVisits }}</div>
            <div class="stat-label">待复诊(7天内)</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div ref="typeChart" style="height: 200px"></div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="记录类型">
          <el-select v-model="queryForm.recordType" placeholder="选择类型" clearable @change="handleQuery">
            <el-option label="体检" value="CHECKUP" />
            <el-option label="疫苗" value="VACCINE" />
            <el-option label="疾病" value="ILLNESS" />
            <el-option label="体重记录" value="WEIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="records" style="width: 100%">
        <el-table-column prop="petName" label="宠物" width="120" />
        <el-table-column prop="recordTypeName" label="类型" width="100" />
        <el-table-column prop="recordDate" label="记录日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.recordDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="temperature" label="体温(℃)" width="100">
          <template #default="{ row }">
            <span :class="getTemperatureClass(row.temperature)">
              {{ row.temperature }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="symptom" label="症状" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
        <el-table-column prop="hospital" label="医院" width="150" show-overflow-tooltip />
        <el-table-column prop="nextVisitDate" label="下次复诊" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="暂无健康记录" />

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

    <el-dialog v-model="dialogVisible" title="健康记录详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="宠物">{{ currentRecord.petName }}</el-descriptions-item>
        <el-descriptions-item label="记录类型">{{ currentRecord.recordTypeName }}</el-descriptions-item>
        <el-descriptions-item label="记录日期">{{ formatDateTime(currentRecord.recordDate) }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentRecord.weight }} kg</el-descriptions-item>
        <el-descriptions-item label="体温">{{ currentRecord.temperature }} ℃</el-descriptions-item>
        <el-descriptions-item label="费用">{{ currentRecord.cost }} 元</el-descriptions-item>
        <el-descriptions-item label="症状" :span="2">{{ currentRecord.symptom }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ currentRecord.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案" :span="2">{{ currentRecord.treatment }}</el-descriptions-item>
        <el-descriptions-item label="医院">{{ currentRecord.hospital }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentRecord.doctor }}</el-descriptions-item>
        <el-descriptions-item label="用药记录" :span="2">{{ currentRecord.medicine }}</el-descriptions-item>
        <el-descriptions-item label="下次复诊">{{ currentRecord.nextVisitDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.notes }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllHealthRecords, getHealthStatistics } from '@/api/health'
import * as echarts from 'echarts'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRecord = ref({})
const dateRange = ref([])
const typeChart = ref(null)
let typeChartInstance = null

const statistics = reactive({
  totalRecords: 0,
  recordTypeDistribution: {},
  abnormalCount: 0,
  upcomingVisits: 0
})

const queryForm = reactive({
  recordType: '',
  startDate: null,
  endDate: null,
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  loadStatistics()
  loadRecords()
})

const loadStatistics = async () => {
  try {
    const res = await getHealthStatistics()
    Object.assign(statistics, res.data)
    await nextTick()
    renderTypeChart()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getAllHealthRecords(queryForm)
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载健康记录失败')
  } finally {
    loading.value = false
  }
}

const renderTypeChart = () => {
  if (!typeChart.value) return
  
  if (typeChartInstance) {
    typeChartInstance.dispose()
  }
  
  typeChartInstance = echarts.init(typeChart.value)
  
  const data = Object.entries(statistics.recordTypeDistribution).map(([name, value]) => ({
    name,
    value
  }))
  
  const option = {
    title: { text: '记录类型分布', left: 'center', top: 10, textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '60%'],
      data: data,
      label: { fontSize: 12 }
    }]
  }
  
  typeChartInstance.setOption(option)
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
  loadRecords()
}

const handleReset = () => {
  queryForm.recordType = ''
  queryForm.startDate = null
  queryForm.endDate = null
  dateRange.value = []
  queryForm.pageNum = 1
  loadRecords()
}

const handleView = (row) => {
  currentRecord.value = row
  dialogVisible.value = true
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getTemperatureClass = (temperature) => {
  if (!temperature) return ''
  if (temperature < 37.5 || temperature > 39.5) {
    return 'abnormal-temperature'
  }
  return ''
}
</script>

<style scoped>
.health-management-container {
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
  font-size: 32px;
  font-weight: bold;
  color: #5C8AEB;
  margin-bottom: 10px;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.info {
  color: #909399;
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

.abnormal-temperature {
  color: #F56C6C;
  font-weight: bold;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
