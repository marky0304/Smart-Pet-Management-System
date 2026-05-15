<template>
  <div class="product-review-section">
    <div class="review-header">
      <h4>商品评价 ({{ total }})</h4>
      <div class="review-summary" v-if="total > 0">
        <span class="avg-rating">★ {{ avgRating.toFixed(1) }}</span>
        <span class="review-count">共 {{ total }} 条评价</span>
      </div>
    </div>

    <div class="review-form" v-if="isAuth && !hasReviewed">
      <h5>写评价</h5>
      <div class="rating-input">
        <span class="star-label">评分：</span>
        <span v-for="n in 5" :key="n" class="star" :class="{ active: ratingValue >= n }"
          @click="ratingValue = n" style="cursor:pointer;font-size:22px;color:#d1d5db;">
          ★
        </span>
      </div>
      <el-input v-model="reviewContent" type="textarea" :rows="3"
        placeholder="分享您的使用体验..." maxlength="500" show-word-limit />
      <el-checkbox v-model="isAnonymous" style="margin:8px 0">匿名评价</el-checkbox>
      <el-button type="primary" @click="submitReview" :loading="submitting"
        :disabled="ratingValue === 0 || !reviewContent.trim()">
        提交评价
      </el-button>
      <span class="error-msg" v-if="errorMsg">{{ errorMsg }}</span>
    </div>

    <div class="review-list" v-if="reviews.length > 0">
      <div class="review-item" v-for="r in reviews" :key="r.id">
        <div class="review-left">
          <img :src="r.userAvatar || '/default-avatar.png'" class="review-avatar" />
        </div>
        <div class="review-right">
          <div class="review-top">
            <span class="review-username">{{ r.isAnonymous ? '匿名用户' : (r.username || '宠友') }}</span>
            <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
            <span class="review-time">{{ formatTime(r.createTime) }}</span>
          </div>
          <p class="review-content-text">{{ r.content }}</p>
          <div class="review-images" v-if="r.images">
            <img v-for="(img, i) in parseImages(r.images)" :key="i" :src="img" class="review-image" />
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && reviews.length === 0 && !hasReviewed" description="暂无评价，成为第一个评价的人吧" />

    <el-pagination v-if="total > pageSize"
      layout="prev, pager, next" :total="total" :page-size="pageSize"
      :current-page="currentPage" @current-change="loadReviews"
      class="review-pagination" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductReviews, createReview } from '@/api/review'

const props = defineProps({
  productId: { type: [Number, String], required: true }
})

const isAuth = computed(() => !!localStorage.getItem('token'))
const reviews = ref([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hasReviewed = ref(false)

const ratingValue = ref(0)
const reviewContent = ref('')
const isAnonymous = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

const avgRating = computed(() => {
  if (reviews.value.length === 0) return 0
  return reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length
})

const loadReviews = async (page = 1) => {
  if (!props.productId) return
  loading.value = true
  try {
    const res = await getProductReviews(props.productId, { page, size: pageSize.value })
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
    currentPage.value = page
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const submitReview = async () => {
  if (!ratingValue.value || !reviewContent.value.trim()) return
  submitting.value = true
  errorMsg.value = ''
  try {
    await createReview({
      productId: props.productId,
      rating: ratingValue.value,
      content: reviewContent.value.trim(),
      isAnonymous: isAnonymous.value ? 1 : 0
    })
    ElMessage.success('评价提交成功')
    ratingValue.value = 0
    reviewContent.value = ''
    isAnonymous.value = false
    hasReviewed.value = true
    loadReviews(1)
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || '评价失败'
  } finally {
    submitting.value = false
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN')
}

const parseImages = (images) => {
  if (!images) return []
  try { return JSON.parse(images) } catch { return images.split(',').map(s => s.trim()) }
}

onMounted(() => loadReviews())
watch(() => props.productId, () => { loadReviews(1); hasReviewed.value = false })
</script>

<style scoped>
.product-review-section {
  margin-top: 24px;
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
}
.review-header h4 { margin: 0 0 8px 0; font-size: 16px; color: #111827; }
.review-summary { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.avg-rating { font-size: 24px; font-weight: 800; color: #f59e0b; }
.review-count { font-size: 13px; color: #6b7280; }

.review-form { background: #f9fafb; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.review-form h5 { margin: 0 0 10px 0; font-size: 14px; color: #374151; }
.rating-input { display: flex; align-items: center; gap: 4px; margin-bottom: 10px; }
.star-label { font-size: 13px; color: #374151; }
.star.active { color: #f59e0b; }

.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-item { display: flex; gap: 12px; padding: 12px; background: #f9fafb; border-radius: 8px; }
.review-left { flex-shrink: 0; }
.review-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; }
.review-right { flex: 1; }
.review-top { display: flex; gap: 10px; align-items: center; margin-bottom: 6px; }
.review-username { font-size: 13px; font-weight: 600; color: #374151; }
.review-stars { font-size: 13px; color: #f59e0b; }
.review-time { font-size: 12px; color: #9ca3af; margin-left: auto; }
.review-content-text { font-size: 13px; color: #374151; line-height: 1.6; margin: 0; }
.review-images { display: flex; gap: 6px; margin-top: 8px; }
.review-image { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.error-msg { color: #dc2626; font-size: 13px; margin-left: 10px; }
.review-pagination { margin-top: 12px; display: flex; justify-content: center; }
</style>
