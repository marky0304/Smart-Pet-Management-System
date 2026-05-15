<template>
  <div class="product-mgmt">
    <div class="page-header">
      <div>
        <div class="page-title">商品管理</div>
        <div class="page-sub">管理商城商品，上下架及库存控制</div>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增商品
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="queryForm.name" placeholder="搜索商品名称" clearable style="width: 220px" @keyup.enter="handleQuery" @clear="handleQuery">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="queryForm.category" placeholder="全部分类" clearable style="width: 140px" @change="handleQuery">
        <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
      </el-select>
      <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 120px" @change="handleQuery">
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-wrap" v-loading="loading">
      <el-table :data="products" border stripe style="width: 100%">
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <img :src="row.image || defaultImage" class="table-img" />
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" min-width="160" show-overflow-tooltip />
        <el-table-column label="分类" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.categoryName || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100" align="center">
          <template #default="{ row }">
            <span class="price-text">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.stock <= 10 ? 'stock-warn' : 'stock-ok'">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="销量" prop="sales" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadProducts"
        @current-change="loadProducts"
        class="pagination"
      />
      <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="图片链接（选填）" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述（选填）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getAdminProductList, createProduct, updateProduct, deleteProduct, updateProductStatus } from '@/api/product'

const loading = ref(false)
const submitting = ref(false)
const products = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const defaultImage = '/shop image/default-product.svg'

const categories = [
  { value: 'FOOD', label: '食品' },
  { value: 'TOY', label: '玩具' },
  { value: 'SUPPLY', label: '用品' },
  { value: 'CLEAN', label: '清洁' },
  { value: 'MEDICINE', label: '药品' }
]

const queryForm = reactive({ name: '', category: '', status: '', pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, name: '', category: '', price: 1, stock: 0, image: '', description: '' })

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }],
  stock: [{ required: true, type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' }]
}

onMounted(() => loadProducts())

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getAdminProductList({ ...queryForm })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryForm.pageNum = 1; loadProducts() }
const handleReset = () => { queryForm.name = ''; queryForm.category = ''; queryForm.status = ''; queryForm.pageNum = 1; loadProducts() }

const openDialog = (row = null) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, category: row.category, price: Number(row.price), stock: row.stock, image: row.image || '', description: row.description || '' })
  } else {
    Object.assign(form, { id: null, name: '', category: '', price: 1, stock: 0, image: '', description: '' })
  }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 50)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { name: form.name, category: form.category, price: form.price, stock: form.stock, image: form.image || undefined, description: form.description || undefined }
    if (isEdit.value) {
      await updateProduct(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateProductStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadProducts()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复。', '删除确认', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      loadProducts()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.product-mgmt { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #5C8AEB 0%, #7BAAF2 100%); padding: 24px 32px; border-radius: 8px; margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 700; color: #fff; margin-bottom: 4px; }
.page-sub { font-size: 13px; color: rgba(255,255,255,0.85); }
.filter-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; background: #fff; padding: 16px 20px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.table-wrap { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.table-img { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }
.price-text { font-size: 15px; font-weight: 700; color: #dc2626; }
.stock-warn { color: #dc2626; font-weight: 700; }
.stock-ok { color: #374151; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>