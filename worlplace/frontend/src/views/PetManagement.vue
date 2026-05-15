<template>
  <div class="pet-management">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-title">宠物管理</div>
        <div class="page-sub">管理平台所有宠物信息，查看详情与数据统计</div>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="宠物名称">
          <el-input v-model="queryForm.name" placeholder="请输入宠物名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable>
            <el-option label="狗" value="DOG" />
            <el-option label="猫" value="CAT" />
            <el-option label="鸟" value="BIRD" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="已删除" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showStatistics">统计分析</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table Card -->
    <div class="table-card">
      <el-table :data="petList" style="margin-top: 0" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="宠物名称" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ getPetType(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="breed" label="品种" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '公' : row.gender === 2 ? '母' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80">
          <template #default="{ row }">
            {{ row.age || 0 }}岁
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="主人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'tag-active' : 'tag-deleted'">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button class="btn-action btn-soft-primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              class="btn-action btn-soft-success"
              size="small"
              @click="handleAudit(row, 1)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && petList.length === 0" description="暂无宠物数据" />

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchPetList"
        @current-change="fetchPetList"
        class="pagination-bar"
      />
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="宠物详情" width="600px">
      <el-descriptions :column="2" border v-if="currentPet">
        <el-descriptions-item label="宠物名称">{{ currentPet.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getPetType(currentPet.type) }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ currentPet.breed || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ currentPet.gender === 1 ? '公' : currentPet.gender === 2 ? '母' : '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ currentPet.birthDate || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPet.age || 0 }}岁</el-descriptions-item>
        <el-descriptions-item label="毛色">{{ currentPet.color || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentPet.weight || '未知' }}kg</el-descriptions-item>
        <el-descriptions-item label="主人">{{ currentPet.userName }}</el-descriptions-item>
        <el-descriptions-item label="芯片编号">{{ currentPet.chipNumber || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">{{ currentPet.allergy || '无' }}</el-descriptions-item>
        <el-descriptions-item label="特殊说明" :span="2">{{ currentPet.specialNotes || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentPet.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- Statistics Dialog -->
    <el-dialog v-model="statisticsVisible" title="宠物统计分析" width="800px">
      <el-row :gutter="20" v-if="statistics">
        <el-col :span="24">
          <el-statistic title="宠物总数" :value="statistics.totalCount" />
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="20">
        <el-col :span="12">
          <h4>类型分布</h4>
          <div v-for="(value, key) in statistics.typeDistribution" :key="key" class="stat-item">
            <span>{{ getPetType(key) }}:</span>
            <span class="stat-tag">{{ value }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <h4>性别分布</h4>
          <div v-for="(value, key) in statistics.genderDistribution" :key="key" class="stat-item">
            <span>{{ key }}:</span>
            <span class="stat-tag">{{ value }}</span>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="20">
        <el-col :span="12">
          <h4>年龄分布</h4>
          <div v-for="(value, key) in statistics.ageDistribution" :key="key" class="stat-item">
            <span>{{ key }}:</span>
            <span class="stat-tag">{{ value }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <h4>热门品种 TOP10</h4>
          <div v-for="(value, key) in statistics.breedDistribution" :key="key" class="stat-item">
            <span>{{ key }}:</span>
            <span class="stat-tag">{{ value }}</span>
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllPetList, getPetDetail, auditPet, getPetStatistics } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailVisible = ref(false)
const statisticsVisible = ref(false)
const petList = ref([])
const total = ref(0)
const currentPet = ref(null)
const statistics = ref(null)

const queryForm = reactive({
  name: '',
  type: '',
  breed: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await getAllPetList(queryForm)
    petList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  fetchPetList()
}

const handleReset = () => {
  queryForm.name = ''
  queryForm.type = ''
  queryForm.breed = ''
  queryForm.status = null
  queryForm.pageNum = 1
  fetchPetList()
}

const showDetail = async (pet) => {
  try {
    const res = await getPetDetail(pet.id)
    currentPet.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleAudit = async (pet, status) => {
  const action = status === 1 ? '恢复' : '删除'
  try {
    await ElMessageBox.confirm(`确定要${action}该宠物吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await auditPet(pet.id, status)
    ElMessage.success(`${action}成功`)
    fetchPetList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const showStatistics = async () => {
  try {
    const res = await getPetStatistics()
    statistics.value = res.data
    statisticsVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const getPetType = (type) => {
  const typeMap = {
    DOG: '狗',
    CAT: '猫',
    BIRD: '鸟',
    OTHER: '其他'
  }
  return typeMap[type] || type
}

onMounted(() => {
  fetchPetList()
})
</script>

<style scoped>
/* ===== Page Layout ===== */
.pet-management {
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}

/* ===== Page Header ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A1A;
  margin-bottom: 4px;
}

.page-sub {
  font-size: 14px;
  color: #8C8C8C;
}

/* ===== Search Card ===== */
.search-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px 4px;
  margin-bottom: 16px;
  box-shadow: 0 2px 24px rgba(0, 0, 0, 0.05);
}

/* ===== Table Card ===== */
.table-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 24px rgba(0, 0, 0, 0.05);
}

.pagination-bar {
  margin-top: 20px;
  justify-content: flex-end;
}

/* ===== Status Tags ===== */
.status-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  line-height: 20px;
}

.tag-active {
  background: #ECFDF5;
  color: #059669;
}

.tag-deleted {
  background: #FEF2F2;
  color: #DC2626;
}

/* ===== Action Buttons ===== */
.btn-action {
  border-radius: 10px;
  border: none;
  font-weight: 500;
}

.btn-soft-primary {
  background: #EFF6FF;
  color: #5C8AEB;
}

.btn-soft-primary:hover {
  background: #DBEAFE;
  color: #4A78D6;
}

.btn-soft-success {
  background: #ECFDF5;
  color: #10B981;
}

.btn-soft-success:hover {
  background: #D1FAE5;
  color: #059669;
}

/* ===== Statistics ===== */
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #F3F4F6;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  background: #F3F4F6;
  color: #8C8C8C;
}

h4 {
  margin-bottom: 12px;
  color: #1A1A1A;
  font-size: 15px;
  font-weight: 600;
}
</style>

<!-- Global overrides for Element Plus deep styles (unscoped) -->
<style>
.pet-management .el-table th.el-table__cell {
  background: #FAFAFA;
  color: #1A1A1A;
  font-weight: 600;
  border-bottom: 1px solid #EEEEEE;
}

.pet-management .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #FAFBFC;
}

.pet-management .el-table td.el-table__cell {
  border-bottom: 1px solid #F3F4F6;
}

.pet-management .el-table {
  border-radius: 12px;
  overflow: hidden;
}

.pet-management .el-input .el-input__wrapper,
.pet-management .el-select .el-select__wrapper {
  border-radius: 12px;
  background: #FAFAFA;
  box-shadow: 0 0 0 1px #EEEEEE inset;
}

.pet-management .el-input .el-input__wrapper:hover,
.pet-management .el-select .el-select__wrapper:hover {
  box-shadow: 0 0 0 1px #B0B0B0 inset;
}

.pet-management .el-input.is-focus .el-input__wrapper,
.pet-management .el-select.is-focus .el-select__wrapper {
  box-shadow: 0 0 0 1px #5C8AEB inset, 0 0 0 3px rgba(92, 138, 235, 0.25);
}

.pet-management .el-button--primary {
  --el-button-bg-color: #5C8AEB;
  --el-button-border-color: #5C8AEB;
  --el-button-hover-bg-color: #4A78D6;
  --el-button-hover-border-color: #4A78D6;
  border-radius: 12px;
  font-weight: 500;
}

.pet-management .el-button--success {
  --el-button-bg-color: #10B981;
  --el-button-border-color: #10B981;
  --el-button-hover-bg-color: #059669;
  --el-button-hover-border-color: #059669;
  border-radius: 12px;
  font-weight: 500;
}

.pet-management .el-button--default {
  border-radius: 12px;
  font-weight: 500;
  border-color: #EEEEEE;
  color: #1A1A1A;
}

.pet-management .el-button--default:hover {
  border-color: #5C8AEB;
  color: #5C8AEB;
  background: #EFF6FF;
}

.pet-management .el-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.pet-management .el-dialog__header {
  border-bottom: 1px solid #EEEEEE;
  padding: 20px 24px;
}

.pet-management .el-dialog__header .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A1A;
}

.pet-management .el-dialog__body {
  padding: 24px;
}

.pet-management .el-descriptions__label {
  font-weight: 500;
  color: #8C8C8C;
}

.pet-management .el-descriptions__content {
  color: #1A1A1A;
}

.pet-management .el-empty__description {
  color: #B0B0B0;
}

.pet-management .el-pagination .el-pager li.is-active {
  background: #5C8AEB;
  border-radius: 8px;
}

.pet-management .el-statistic__head {
  color: #8C8C8C;
  font-size: 14px;
}

.pet-management .el-statistic__number {
  color: #5C8AEB;
  font-size: 28px;
  font-weight: 700;
}
</style>
