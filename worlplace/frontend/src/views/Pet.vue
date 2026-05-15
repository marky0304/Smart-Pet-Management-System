<template>
  <div class="pet-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-text">我的宠物</span>
        <span class="title-count" v-if="total > 0">共 {{ total }} 只</span>
      </div>
      <el-button class="btn-brand" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加宠物
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="queryForm.name" placeholder="搜索宠物名称..." clearable class="search-input">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="queryForm.type" placeholder="宠物类型" clearable @change="handleSearchTypeChange" class="search-select">
        <el-option label="🐕 狗" value="DOG" />
        <el-option label="🐈 猫" value="CAT" />
        <el-option label="🦜 鸟" value="BIRD" />
        <el-option label="🐾 其他" value="OTHER" />
      </el-select>
      <el-select v-model="queryForm.breed" placeholder="选择品种" clearable filterable class="search-select">
        <el-option v-for="b in searchBreedList" :key="b" :label="b" :value="b" />
      </el-select>
      <el-button class="btn-brand" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 宠物网格 -->
    <div v-loading="loading">
      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && (!petList || petList.length === 0)">
        <div class="empty-illustration">🐾</div>
        <div class="empty-title">还没有添加宠物</div>
        <div class="empty-desc">快把你的毛茸茸伙伴带回家吧～</div>
        <el-button class="btn-brand" size="large" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 立即添加宠物
        </el-button>
      </div>

      <!-- 宠物卡片网格 -->
      <div class="pet-grid" v-else>
        <div class="pet-card" v-for="pet in petList" :key="pet.id">
          <!-- 图片区 -->
          <div class="pet-image-wrapper">
            <img :src="pet.avatar || getPetImage(pet.type, pet.breed)" class="pet-image" />
            <div class="pet-badge" :class="'badge-' + pet.type.toLowerCase()">
              {{ getPetType(pet.type) }}
            </div>
            <!-- 快捷操作浮层 -->
            <div class="pet-quick-actions">
              <button class="quick-btn" @click.stop="$router.push('/dashboard/service')" title="预约服务">📅 预约</button>
              <button class="quick-btn" @click.stop="$router.push('/dashboard/health')" title="健康档案">💊 健康</button>
            </div>
          </div>

          <!-- 信息区 -->
          <div class="pet-info">
            <div class="pet-header">
              <span class="pet-name">{{ pet.name }}</span>
              <span class="pet-gender" :class="pet.gender === 1 ? 'gender-male' : 'gender-female'">
                {{ pet.gender === 1 ? '♂' : '♀' }}
              </span>
            </div>
            <div class="pet-breed">
              <el-icon><Star /></el-icon>
              <span>{{ pet.breed || '未知品种' }}</span>
            </div>
            <div class="pet-meta">
              <span class="meta-tag" v-if="pet.age">{{ pet.age }}岁</span>
              <span class="meta-tag" v-if="pet.weight">{{ pet.weight }}kg</span>
              <span class="meta-tag" v-if="pet.color">{{ pet.color }}</span>
            </div>
            <div class="pet-actions">
              <button class="action-btn btn-detail" @click.stop="showDetailDialog(pet)">详情</button>
              <button class="action-btn btn-edit" @click.stop="showEditDialog(pet)">编辑</button>
              <button class="action-btn btn-delete" @click.stop="handleDelete(pet)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="queryForm.pageNum"
      v-model:page-size="queryForm.pageSize"
      :page-sizes="[9, 18, 27]"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchPetList"
      @current-change="fetchPetList"
      class="pagination"
    />

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="petForm" :rules="petRules" ref="petFormRef" label-width="90px">
        <el-form-item label="宠物照片">
          <el-upload
            class="pet-photo-uploader"
            action="/api/upload/image"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handlePhotoSuccess"
            :on-error="handlePhotoError"
            :before-upload="beforePhotoUpload"
            accept="image/*"
          >
            <div class="upload-area">
              <img v-if="petForm.avatar" :src="petForm.avatar" class="uploaded-photo" />
              <div v-else class="upload-placeholder">
                <el-icon style="font-size:28px;color:#9ca3af"><Plus /></el-icon>
                <div style="font-size:12px;color:#9ca3af;margin-top:6px">点击上传照片</div>
              </div>
            </div>
          </el-upload>
          <div style="font-size:12px;color:#9ca3af;margin-top:4px">支持 JPG/PNG，最大 5MB</div>
        </el-form-item>
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="宠物类型" prop="type">
          <el-select v-model="petForm.type" placeholder="请选择类型" style="width:100%" @change="handleTypeChange">
            <el-option label="🐕 狗" value="DOG" />
            <el-option label="🐈 猫" value="CAT" />
            <el-option label="🦜 鸟" value="BIRD" />
            <el-option label="🐾 其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-select v-model="petForm.breed" placeholder="请选择品种" style="width:100%" filterable allow-create default-first-option>
            <el-option v-for="b in currentBreedList" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio :label="1">♂ 公</el-radio>
            <el-radio :label="2">♀ 母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker v-model="petForm.birthDate" type="date" placeholder="请选择出生日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="毛色">
          <el-input v-model="petForm.color" placeholder="请输入毛色" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="petForm.weight" :min="0" :max="200" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="芯片编号">
          <el-input v-model="petForm.chipNumber" placeholder="请输入芯片编号" />
        </el-form-item>
        <el-form-item label="过敏史">
          <el-input v-model="petForm.allergy" type="textarea" :rows="2" placeholder="请输入过敏史" />
        </el-form-item>
        <el-form-item label="特殊说明">
          <el-input v-model="petForm.specialNotes" type="textarea" :rows="2" placeholder="请输入特殊说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="btn-brand" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="宠物详情" width="560px">
      <el-descriptions :column="2" border v-if="currentPet">
        <el-descriptions-item label="宠物名称">{{ currentPet.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getPetType(currentPet.type) }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ currentPet.breed || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentPet.gender === 1 ? '♂ 公' : '♀ 母' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ currentPet.birthDate || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPet.age || 0 }}岁</el-descriptions-item>
        <el-descriptions-item label="毛色">{{ currentPet.color || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentPet.weight ? currentPet.weight + 'kg' : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="芯片编号" :span="2">{{ currentPet.chipNumber || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">{{ currentPet.allergy || '无' }}</el-descriptions-item>
        <el-descriptions-item label="特殊说明" :span="2">{{ currentPet.specialNotes || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentPet.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { petAPI, addPet, updatePet, deletePet, getPetDetail } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, Plus, Search } from '@element-plus/icons-vue'

// 上传请求头（携带 token）
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('添加宠物')
const isEdit = ref(false)
const editId = ref(null)
const petList = ref([])
const total = ref(0)
const pageSize = ref(9)
const currentPet = ref(null)
const petFormRef = ref(null)

const queryForm = reactive({ name: '', type: '', breed: '', pageNum: 1, pageSize: 9 })
const petForm = reactive({ name: '', type: '', breed: '', gender: 1, birthDate: '', color: '', weight: null, chipNumber: '', allergy: '', specialNotes: '' })
const petRules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }],
  breed: [{ required: true, message: '请选择或输入品种', trigger: 'change' }]
}

const breedOptions = {
  DOG: ['哈士奇','金毛寻回犬','拉布拉多','萨摩耶','阿拉斯加雪橇犬','边境牧羊犬','德国牧羊犬','柯基','泰迪','比熊','博美','吉娃娃','雪纳瑞','法国斗牛犬','英国斗牛犬','柴犬','秋田犬','松狮','贵宾犬','约克夏','马尔济斯','西施犬','巴哥犬','可卡犬','比格犬','腊肠犬','斑点狗','中华田园犬','其他'],
  CAT: ['英国短毛猫','美国短毛猫','苏格兰折耳猫','波斯猫','暹罗猫','布偶猫','缅因猫','挪威森林猫','俄罗斯蓝猫','孟买猫','加菲猫','金吉拉','喜马拉雅猫','阿比西尼亚猫','伯曼猫','德文卷毛猫','曼基康猫','无毛猫','中华田园猫','狸花猫','橘猫','三花猫','奶牛猫','其他'],
  BIRD: ['虎皮鹦鹉','玄凤鹦鹉','牡丹鹦鹉','金刚鹦鹉','灰鹦鹉','亚马逊鹦鹉','葵花鹦鹉','文鸟','珍珠鸟','八哥','画眉','百灵','金丝雀','相思鸟','鸽子','其他'],
  OTHER: ['仓鼠','龙猫','兔子','荷兰猪','刺猬','乌龟','蜥蜴','金鱼','锦鲤','其他']
}

const currentBreedList = ref([])
const searchBreedList = ref([])

const handleTypeChange = (type) => { currentBreedList.value = breedOptions[type] || []; petForm.breed = '' }
const handleSearchTypeChange = (type) => { searchBreedList.value = type ? breedOptions[type] || [] : []; queryForm.breed = '' }

const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await petAPI.getPetList({ pageNum: queryForm.pageNum, pageSize: queryForm.pageSize, name: queryForm.name, type: queryForm.type })
    // pet.js 拦截器返回整个 {code, message, data}，data 字段是分页对象
    const pageData = res?.data || res
    petList.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (e) { petList.value = []; total.value = 0 }
  finally { loading.value = false }
}

const handleSearch = () => { queryForm.pageNum = 1; fetchPetList() }
const handleReset = () => { queryForm.name = ''; queryForm.type = ''; queryForm.breed = ''; queryForm.pageNum = 1; fetchPetList() }

const showAddDialog = () => { isEdit.value = false; dialogTitle.value = '添加宠物'; resetForm(); dialogVisible.value = true }
const showEditDialog = (pet) => { isEdit.value = true; editId.value = pet.id; dialogTitle.value = '编辑宠物'; Object.assign(petForm, pet); currentBreedList.value = breedOptions[pet.type] || []; dialogVisible.value = true }
const showDetailDialog = async (pet) => {
  try { const res = await getPetDetail(pet.id); currentPet.value = res?.data || res; detailVisible.value = true } catch (e) {}
}

const handleSubmit = async () => {
  await petFormRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) { await updatePet(editId.value, petForm); ElMessage.success('更新成功') }
    else { await addPet(petForm); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchPetList()
  } catch (e) {} finally { submitLoading.value = false }
}

const handleDelete = async (pet) => {
  try {
    await ElMessageBox.confirm(`确定要删除宠物"${pet.name}"吗？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await deletePet(pet.id)
    ElMessage.success('删除成功')
    // 乐观更新：直接从列表移除
    const idx = petList.value.findIndex(p => p.id === pet.id)
    if (idx !== -1) petList.value.splice(idx, 1)
    total.value = Math.max(0, total.value - 1)
    if (petList.value.length === 0 && queryForm.pageNum > 1) {
      queryForm.pageNum--
      await fetchPetList()
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('删除失败，请重试')
    }
  }
}

const beforePhotoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}

const handlePhotoSuccess = (res) => {
  if (res.code === 200) {
    petForm.avatar = res.data
    ElMessage.success('照片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handlePhotoError = () => { ElMessage.error('上传失败，请重试') }

const resetForm = () => {
  Object.assign(petForm, { name: '', type: '', breed: '', gender: 1, birthDate: '', color: '', weight: null, chipNumber: '', allergy: '', specialNotes: '', avatar: '' })
  petFormRef.value?.clearValidate()
}

const getPetType = (type) => ({ DOG: '狗', CAT: '猫', BIRD: '鸟', OTHER: '其他' }[type] || type)

const getPetImage = (type, breed) => {
  const imgs = {
    DOG: { '哈士奇':'https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=400','金毛寻回犬':'https://images.unsplash.com/photo-1633722715463-d30f4f325e24?w=400','拉布拉多':'https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=400','萨摩耶':'https://images.unsplash.com/photo-1568572933382-74d440642117?w=400','泰迪':'https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=400','柯基':'https://images.unsplash.com/photo-1612536981610-4e23e0e6c0e4?w=400','柴犬':'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400','default':'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400' },
    CAT: { '英国短毛猫':'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400','布偶猫':'https://images.unsplash.com/photo-1529778873920-4da4926a72c2?w=400','橘猫':'https://images.unsplash.com/photo-1574144611937-0df059b5ef3e?w=400','苏格兰折耳猫':'https://images.unsplash.com/photo-1596854407944-bf87f6fdd49e?w=400','美国短毛猫':'https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=400','default':'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400' },
    BIRD: { 'default':'https://images.unsplash.com/photo-1444464666168-49d633b86797?w=400' },
    OTHER: { '仓鼠':'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=400','兔子':'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400','default':'https://images.unsplash.com/photo-1548681528-6a5c45b66b42?w=400' }
  }
  const t = imgs[type] || imgs.OTHER
  return t[breed] || t.default
}

onMounted(() => { fetchPetList() })
</script>

<style scoped>
.pet-page { padding: 0; }

/* 照片上传 */
.pet-photo-uploader { display: inline-block; }
.upload-area {
  width: 120px; height: 120px; border: 2px dashed #d1d5db;
  border-radius: 8px; cursor: pointer; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  transition: border-color 0.2s;
}
.upload-area:hover { border-color: #5C8AEB; }
.uploaded-photo { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; }

/* 品牌按钮 */
.btn-brand { background: #5C8AEB !important; border-color: #5C8AEB !important; color: #fff !important; font-weight: 600; }
.btn-brand:hover { background: #4A78D6 !important; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(92,138,235,0.3) !important; }

/* 页头 */
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 20px 24px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.page-title { display: flex; align-items: center; gap: 10px; }
.title-text { font-size: 20px; font-weight: 700; color: #111827; }
.title-count { font-size: 13px; color: #9ca3af; background: #f3f4f6; padding: 2px 10px; border-radius: 20px; }

/* 搜索栏 */
.search-bar {
  display: flex; align-items: center; gap: 12px;
  background: #fff; padding: 16px 24px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.search-input { flex: 1; max-width: 240px; }
.search-select { width: 140px; }

/* 空状态 */
.empty-state {
  text-align: center; padding: 80px 20px;
  background: #fff; border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.empty-illustration { font-size: 72px; margin-bottom: 16px; }
.empty-title { font-size: 18px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.empty-desc { font-size: 14px; color: #9ca3af; margin-bottom: 24px; }

/* 宠物网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 宠物卡片 */
.pet-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  transition: all 0.25s; cursor: pointer;
}
.pet-card:hover { transform: translateY(-6px); box-shadow: 0 10px 28px rgba(0,0,0,0.13); }

/* 图片区 */
.pet-image-wrapper {
  position: relative; overflow: hidden;
  aspect-ratio: 1 / 1;
}
.pet-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.pet-card:hover .pet-image { transform: scale(1.06); }

/* 类型标签 - 胶囊样式 */
.pet-badge {
  position: absolute; top: 10px; right: 10px;
  padding: 3px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600;
}
.badge-dog { background: #fff3e8; color: #ea580c; }
.badge-cat { background: #fdf2f8; color: #db2777; }
.badge-bird { background: #f0fdf4; color: #16a34a; }
.badge-other { background: rgba(92,138,235,0.08); color: #5C8AEB; }

/* 快捷操作浮层 */
.pet-quick-actions {
  position: absolute; bottom: 0; left: 0; right: 0;
  display: flex; gap: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  padding: 20px 10px 10px;
  opacity: 0; transition: opacity 0.25s;
}
.pet-card:hover .pet-quick-actions { opacity: 1; }
.quick-btn {
  flex: 1; padding: 6px 0; border: none; background: rgba(255,255,255,0.2);
  color: #fff; font-size: 12px; font-weight: 600; cursor: pointer;
  border-radius: 6px; margin: 0 4px; transition: background 0.2s;
}
.quick-btn:hover { background: rgba(255,255,255,0.35); }

/* 信息区 */
.pet-info { padding: 14px 16px; }
.pet-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.pet-name { font-size: 18px; font-weight: 700; color: #111827; }
.pet-gender {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700;
}
.gender-male { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.gender-female { background: #fdf2f8; color: #db2777; }

.pet-breed { display: flex; align-items: center; gap: 5px; font-size: 13px; color: #6b7280; margin-bottom: 10px; }
.pet-breed .el-icon { color: #f59e0b; }

.pet-meta { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.meta-tag {
  font-size: 12px; color: #374151; background: #f3f4f6;
  padding: 3px 10px; border-radius: 20px;
}

/* 操作按钮 */
.pet-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.action-btn {
  padding: 7px 0; border: none; border-radius: 6px;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.btn-detail { background: rgba(92,138,235,0.08); color: #5C8AEB; }
.btn-detail:hover { background: #5C8AEB; color: #fff; }
.btn-edit { background: transparent; color: #6b7280; border: none; }
.btn-edit:hover { color: #5C8AEB; background: transparent; text-decoration: underline; }
.btn-delete { background: #fef2f2; color: #dc2626; }
.btn-delete:hover { background: #dc2626; color: #fff; }

/* 分页 */
.pagination {
  margin-top: 20px; display: flex; justify-content: center;
  background: #fff; padding: 16px; border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
</style>
