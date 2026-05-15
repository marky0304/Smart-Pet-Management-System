<template>
  <el-dialog 
    v-model="visible" 
    title="快速预约" 
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form 
      :model="form" 
      :rules="rules" 
      ref="formRef" 
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="服务">
        <el-input :model-value="service.name" disabled>
          <template #prepend>
            <el-tag :type="getCategoryType(service.category)">
              {{ service.categoryName }}
            </el-tag>
          </template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="价格">
        <el-input disabled>
          <template #prepend>¥</template>
          <template #default>
            <span style="color: #f56c6c; font-weight: bold; font-size: 18px;">
              {{ service.price }}
            </span>
          </template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="选择宠物" prop="petId">
        <el-select 
          v-model="form.petId" 
          placeholder="请选择宠物" 
          style="width: 100%"
          :disabled="pets.length === 0"
        >
          <el-option 
            v-for="pet in pets" 
            :key="pet.id" 
            :label="`${pet.name} (${pet.type})`" 
            :value="pet.id"
          >
            <span style="float: left">{{ pet.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ pet.type }}
            </span>
          </el-option>
        </el-select>
        <el-alert 
          v-if="pets.length === 0" 
          title="您还没有添加宠物，请先添加宠物信息" 
          type="warning" 
          :closable="false"
          style="margin-top: 10px"
        />
      </el-form-item>
      
      <el-form-item label="预约时间" prop="appointmentDatetime">
        <el-date-picker
          v-model="form.appointmentDatetime"
          type="datetime"
          placeholder="选择日期时间"
          style="width: 100%"
          :disabled-date="disabledDate"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
        <div style="margin-top: 8px; font-size: 12px; color: #909399;">
          <el-icon><Clock /></el-icon>
          营业时间：09:00 - 18:00
        </div>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input 
          v-model="form.notes" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入备注信息（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="submitting"
          :disabled="pets.length === 0"
        >
          <el-icon v-if="!submitting"><Check /></el-icon>
          {{ submitting ? '预约中...' : '确认预约' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Check } from '@element-plus/icons-vue'
import { createAppointment } from '@/api/appointment'
import { getMyPetList } from '@/api/pet'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  service: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const pets = ref([])

const form = reactive({
  petId: null,
  serviceId: null,
  appointmentDatetime: null,
  notes: ''
})

const rules = {
  petId: [
    { required: true, message: '请选择宠物', trigger: 'change' }
  ],
  appointmentDatetime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ]
}

// 监听对话框打开
watch(visible, async (newVal) => {
  if (newVal) {
    await loadPets()
    form.serviceId = props.service.id
    // 设置默认预约时间为明天上午10点
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    tomorrow.setHours(10, 0, 0, 0)
    form.appointmentDatetime = tomorrow.toISOString().slice(0, 19)
  }
})

// 加载宠物列表
const loadPets = async () => {
  loading.value = true
  try {
    const res = await getMyPetList()
    // pet.js 拦截器统一返回 res = {code, data}，/pet/my 的 data 直接是数组
    pets.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    if (pets.value.length === 1) {
      form.petId = pets.value[0].id
    }
  } catch (error) {
    ElMessage.error('加载宠物列表失败')
  } finally {
    loading.value = false
  }
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 禁用非营业时间（9:00-18:00）
const disabledHours = () => {
  const hours = []
  for (let i = 0; i < 9; i++) {
    hours.push(i)
  }
  for (let i = 18; i < 24; i++) {
    hours.push(i)
  }
  return hours
}

// 禁用分钟（只能选择整点或半点）
const disabledMinutes = (hour) => {
  const minutes = []
  for (let i = 0; i < 60; i++) {
    if (i !== 0 && i !== 30) {
      minutes.push(i)
    }
  }
  return minutes
}

// 获取类别类型
const getCategoryType = (category) => {
  const typeMap = {
    'BATH': 'primary',
    'GROOM': 'success',
    'BOARD': 'warning',
    'MEDICAL': 'danger',
    'TRAIN': 'info',
    'OTHER': ''
  }
  return typeMap[category] || ''
}

// 提交预约
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true

    const appointmentData = {
      petId: form.petId,
      serviceId: form.serviceId,
      appointmentDatetime: form.appointmentDatetime,
      notes: form.notes || ''
    }
    
    const response = await createAppointment(appointmentData)
    
    ElMessage.success({
      message: '预约成功！',
      duration: 2000
    })
    
    // 通知父组件预约成功
    emit('success', response.data)
    
    // 关闭对话框
    handleClose()
  } catch (error) {
    console.error('预约失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('预约失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  // 重置表单
  formRef.value?.resetFields()
  form.petId = null
  form.serviceId = null
  form.appointmentDatetime = null
  form.notes = ''
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: #303133;
  font-weight: 500;
}

:deep(.el-select-dropdown__item) {
  padding: 0 20px;
}
</style>
