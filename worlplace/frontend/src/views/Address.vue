<template>
  <div class="address-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>收货地址</h2>
          <p>管理您的收货地址，最多可添加20个地址</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增地址</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="address-list">
      <div v-if="!loading && addresses.length === 0" class="empty-wrapper">
        <el-empty description="暂无收货地址" :image-size="160">
          <el-button type="primary" @click="openCreateDialog">添加收货地址</el-button>
        </el-empty>
      </div>

      <div class="address-card" v-for="item in addresses" :key="item.id" :class="{ 'is-default': item.isDefault === 1 }">
        <div class="card-top">
          <div class="card-info">
            <div class="info-line">
              <span class="receiver-name">{{ item.receiverName }}</span>
              <span class="receiver-phone">{{ item.receiverPhone }}</span>
            </div>
            <div class="info-address">
              {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detailAddress }}
            </div>
          </div>
          <el-tag v-if="item.isDefault === 1" type="primary" size="small" class="default-tag">默认</el-tag>
        </div>
        <div class="card-actions">
          <el-button v-if="item.isDefault !== 1" link type="primary" @click="handleSetDefault(item)">设为默认</el-button>
          <el-button link type="primary" @click="openEditDialog(item)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地址' : '新增地址'"
      width="580px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" label-position="right">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="form.region"
            :options="regionData"
            :props="{ value: 'label', label: 'label' }"
            placeholder="请选择省/市/区"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="form.detailAddress" placeholder="街道、门牌号等" maxlength="100" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
          <span class="switch-hint">提示：设为默认地址后将优先使用</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'

const loading = ref(false)
const addresses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  isDefault: false
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.message || '加载地址列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (item) => {
  isEdit.value = true
  editId.value = item.id
  form.receiverName = item.receiverName
  form.receiverPhone = item.receiverPhone
  form.region = [item.province, item.city, item.district]
  form.detailAddress = item.detailAddress
  form.isDefault = item.isDefault === 1
  dialogVisible.value = true
}

const resetForm = () => {
  form.receiverName = ''
  form.receiverPhone = ''
  form.region = []
  form.detailAddress = ''
  form.isDefault = false
  formRef.value?.resetFields()
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      province: form.region[0],
      city: form.region[1],
      district: form.region[2],
      detailAddress: form.detailAddress,
      isDefault: form.isDefault ? 1 : 0
    }

    if (isEdit.value) {
      await updateAddress(editId.value, data)
      ElMessage.success('地址更新成功')
    } else {
      await createAddress(data)
      ElMessage.success('地址添加成功')
    }

    dialogVisible.value = false
    loadAddresses()
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

const handleSetDefault = async (item) => {
  try {
    await setDefaultAddress(item.id)
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAddress(item.id)
      ElMessage.success('删除成功')
      loadAddresses()
    } catch (e) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-page {
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

.empty-wrapper {
  background: white;
  border-radius: 12px;
  padding: 60px 0;
}

.address-list {
  min-height: 200px;
}

.address-card {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.address-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.address-card.is-default {
  border-color: #5C8AEB;
  background: #f0f5ff;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.card-info {
  flex: 1;
}

.info-line {
  margin-bottom: 8px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-right: 16px;
}

.receiver-phone {
  font-size: 14px;
  color: #606266;
}

.info-address {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.default-tag {
  flex-shrink: 0;
  margin-left: 12px;
}

.card-actions {
  display: flex;
  gap: 4px;
  border-top: 1px solid #f2f3f5;
  padding-top: 12px;
}

.switch-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
