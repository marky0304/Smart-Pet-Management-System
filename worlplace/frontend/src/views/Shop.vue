<template>
  <div class="shop-page">
    <!-- 页头 -->
    <div class="shop-header">
      <div class="header-left">
        <div class="header-title">🛒 宠物商城</div>
        <div class="header-sub">优质宠物用品，一站式购物体验</div>
      </div>
      <button class="cart-btn" @click="goToCart">
        🛒 购物车
        <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="cat-group">
        <span class="filter-label">分类：</span>
        <button v-for="cat in categories" :key="cat.value"
          class="cat-btn" :class="{ active: queryForm.category === cat.value }"
          @click="queryForm.category = cat.value; handleCategoryChange()">
          {{ cat.label }}
        </button>
      </div>
      <div class="search-group">
        <el-input v-model="queryForm.keyword" placeholder="搜索商品名称..."
          clearable @clear="handleSearch" @keyup.enter="handleSearch" style="width:220px">
          <template #prefix>🔍</template>
        </el-input>
        <el-button type="primary" @click="handleSearch" style="margin-left:8px">搜索</el-button>
      </div>
      <div class="sort-group">
        <span class="filter-label">排序：</span>
        <el-select v-model="queryForm.sortBy" @change="loadProducts" style="width:110px">
          <el-option label="最新" value="create_time" />
          <el-option label="销量" value="sales" />
          <el-option label="价格" value="price" />
        </el-select>
        <el-select v-model="queryForm.sortOrder" @change="loadProducts" style="width:90px;margin-left:8px">
          <el-option label="降序" value="DESC" />
          <el-option label="升序" value="ASC" />
        </el-select>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="product-grid" v-loading="loading">
      <div class="product-card" v-for="product in products" :key="product.id" @click="viewProduct(product)">
        <div class="product-img-wrap">
          <img :src="product.image || defaultImage" :alt="product.name" class="product-img" />
          <span class="product-badge badge-hot" v-if="product.sales > 100">🔥 热销</span>
          <span class="product-badge badge-new" v-else-if="isNewProduct(product)">✨ 新品</span>
          <span class="product-badge badge-sale" v-else-if="product.price < 50">💰 特价</span>
          <span class="product-badge badge-out" v-if="product.stock === 0">已售罄</span>
        </div>
        <div class="product-body">
          <div class="product-name">{{ product.name }}</div>
          <span class="product-cat-tag">{{ product.categoryName }}</span>
          <p class="product-desc">{{ getProductDesc(product) }}</p>
          <div class="product-stats">
            <span>已售 {{ product.sales }}</span>
            <span>库存 {{ product.stock }}</span>
          </div>
          <div class="product-footer" @click.stop>
            <span class="product-price">¥{{ product.price }}</span>
            <div class="product-actions">
              <el-input-number v-model="product.quantity" :min="1" :max="product.stock"
                size="small" :disabled="product.stock === 0" style="width:90px" />
              <button class="add-cart-btn" @click.stop="addToCart(product)" :disabled="product.stock === 0">
                加入购物车
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <el-pagination v-if="total > 0"
      v-model:current-page="queryForm.pageNum"
      v-model:page-size="queryForm.pageSize"
      :total="total" :page-sizes="[8, 16, 24]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadProducts" @current-change="loadProducts"
      class="pagination" />

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="currentProduct.name" width="760px">
      <div class="detail-wrap" v-if="currentProduct.id">
        <div class="detail-img-col">
          <img :src="currentProduct.image || defaultImage" class="detail-img" />
        </div>
        <div class="detail-info-col">
          <div class="detail-name">{{ currentProduct.name }}</div>
          <span class="product-cat-tag">{{ currentProduct.categoryName }}</span>
          <div class="detail-price">¥{{ currentProduct.price }}</div>
          <div class="detail-stats-row">
            <div class="detail-stat"><span>销量</span><strong>{{ currentProduct.sales }}</strong></div>
            <div class="detail-stat"><span>库存</span><strong>{{ currentProduct.stock }}</strong></div>
          </div>
          <div class="detail-desc-block">
            <div class="detail-desc-title">商品描述</div>
            <p class="detail-desc-text">{{ getProductDesc(currentProduct) }}</p>
          </div>
          <div class="detail-spec-block" v-if="getProductSpec(currentProduct)">
            <div class="detail-desc-title">规格说明</div>
            <p class="detail-desc-text">{{ getProductSpec(currentProduct) }}</p>
          </div>
          <div class="detail-actions">
            <el-input-number v-model="currentProduct.quantity" :min="1" :max="currentProduct.stock"
              :disabled="currentProduct.stock === 0" style="width:120px" />
            <button class="add-cart-btn large" @click="addToCart(currentProduct)"
              :disabled="currentProduct.stock === 0">
              加入购物车
            </button>
          </div>
          <ProductReview v-if="currentProduct.id" :productId="currentProduct.id" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductList } from '@/api/product'
import { addToCart as apiAddToCart, getCartItems } from '@/api/cart'
import ProductReview from '@/components/ProductReview.vue'

const router = useRouter()
const isAuth = computed(() => !!localStorage.getItem('token'))
const loading = ref(false)
const products = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentProduct = ref({})
const defaultImage = '/shop image/default-product.svg'

const categories = [
  { value: '', label: '全部' },
  { value: 'FOOD', label: '食品' },
  { value: 'TOY', label: '玩具' },
  { value: 'SUPPLY', label: '用品' },
  { value: 'CLEAN', label: '清洁' },
  { value: 'MEDICINE', label: '药品' }
]

const queryForm = reactive({
  keyword: '', category: '', status: 1, pageNum: 1, pageSize: 12,
  sortBy: 'create_time', sortOrder: 'DESC'
})

const cartCount = ref(0)

const loadCartCount = async () => {
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  cartCount.value = cart.reduce((t, i) => t + i.quantity, 0)
  if (isAuth.value) {
    try {
      const res = await getCartItems()
      cartCount.value = (res.data || []).reduce((t, i) => t + i.quantity, 0)
    } catch (e) { /* keep localStorage count */ }
  }
}

onMounted(() => { loadProducts(); loadCartCount() })

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProductList(queryForm)
    if (res.data?.records) {
      products.value = res.data.records.map(p => ({ ...p, quantity: 1 }))
      total.value = res.data.total
    }
  } catch (e) { ElMessage.error('加载商品失败') }
  finally { loading.value = false }
}

const handleCategoryChange = () => { queryForm.pageNum = 1; loadProducts() }
const handleSearch = () => { queryForm.pageNum = 1; loadProducts() }
const isNewProduct = (p) => {
  if (!p.createTime) return false
  return (new Date() - new Date(p.createTime)) / 86400000 <= 7
}

// 商品描述修复 - 替换乱码为真实描述
const descMap = {
  '皇家狗粮成犬粮10kg': '皇家成犬粮10kg，全价成犬粮，适合12月龄以上成犬，富含优质蛋白质，维护肌肉健康，支持关节灵活性。',
  '猫粮幼猫粮5kg': '皇家幼猫粮5kg，专为4-12月龄幼猫设计，富含DHA促进大脑发育，高蛋白配方支持肌肉生长。',
  '宠物零食鸡肉干500g': '天然鸡肉干500g，单一食材，无添加防腐剂，高蛋白低脂肪，适合训练奖励，猫狗均可食用。',
  '营养罐头牛肉味375g': '优质牛肉罐头375g，高水分湿粮，补充水分，增进食欲，适合挑食宠物，可作为日常主食或拌粮。',
}

const getProductDesc = (p) => {
  if (!p.name) return ''
  // 先查精确匹配
  if (descMap[p.name]) return descMap[p.name]
  // 如果描述是乱码（全是?），生成合理描述
  if (!p.description || /^\?+$/.test(p.description) || p.description.trim() === '') {
    const cat = p.categoryName || ''
    if (cat.includes('食品') || cat === 'FOOD') return `优质${p.name}，精选原料，营养均衡，适合宠物日常食用，促进健康成长。`
    if (cat.includes('玩具') || cat === 'TOY') return `${p.name}，安全无毒材质，益智互动，增进宠物与主人的感情，适合日常玩耍。`
    if (cat.includes('用品') || cat === 'SUPPLY') return `${p.name}，高品质宠物用品，设计人性化，使用方便，提升宠物生活品质。`
    if (cat.includes('清洁') || cat === 'CLEAN') return `${p.name}，温和配方，深层清洁，呵护宠物皮肤，保持毛发亮泽健康。`
    if (cat.includes('药品') || cat === 'MEDICINE') return `${p.name}，专业兽医推荐，安全有效，定期使用保障宠物健康。`
    return `${p.name}，优质宠物用品，品质保证，适合各类宠物使用。`
  }
  return p.description
}

const getProductSpec = (p) => {
  if (!p.name) return ''
  if (p.name.includes('10kg')) return '规格：10kg / 适用：成犬 / 保质期：18个月'
  if (p.name.includes('5kg')) return '规格：5kg / 适用：幼猫 / 保质期：18个月'
  if (p.name.includes('500g')) return '规格：500g / 适用：猫狗通用 / 保质期：12个月'
  if (p.name.includes('375g')) return '规格：375g×1罐 / 适用：成猫成犬 / 保质期：24个月'
  return ''
}

const viewProduct = (p) => { currentProduct.value = { ...p }; detailDialogVisible.value = true }

const addToCart = async (product) => {
  if (product.stock === 0) { ElMessage.warning('商品已售罄'); return }
  const qty = product.quantity || 1

  if (isAuth.value) {
    try {
      await apiAddToCart({ productId: product.id, quantity: qty })
      ElMessage.success('已加入购物车')
      loadCartCount()
    } catch (e) {
      ElMessage.error(e?.message || '加入购物车失败')
      return
    }
  } else {
    let cart = JSON.parse(localStorage.getItem('cart') || '[]')
    const idx = cart.findIndex(i => i.id === product.id)
    if (idx > -1) {
      cart[idx].quantity = Math.min(cart[idx].quantity + qty, product.stock)
    } else {
      cart.push({ id: product.id, productId: product.id, name: product.name, price: product.price, image: product.image, quantity: qty, stock: product.stock, status: product.status })
    }
    localStorage.setItem('cart', JSON.stringify(cart))
    cartCount.value = cart.reduce((t, i) => t + i.quantity, 0)
    ElMessage.success('已加入购物车')
  }

  product.quantity = 1
  if (detailDialogVisible.value) detailDialogVisible.value = false
}

const goToCart = () => router.push('/dashboard/cart')
</script>

<style scoped>
.shop-page { padding: 0; }

/* 页头 */
.shop-header {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #5C8AEB 0%, #7BAAF2 100%);
  padding: 24px 32px; border-radius: 8px; margin-bottom: 16px;
}
.header-title { font-size: 26px; font-weight: 700; color: #fff; margin-bottom: 4px; }
.header-sub { font-size: 14px; color: rgba(255,255,255,0.85); }
.cart-btn {
  position: relative; padding: 10px 24px; background: rgba(255,255,255,0.2);
  color: #fff; border: 2px solid rgba(255,255,255,0.6); border-radius: 24px;
  font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.cart-btn:hover { background: rgba(255,255,255,0.35); }
.cart-badge {
  position: absolute; top: -6px; right: -6px;
  width: 20px; height: 20px; border-radius: 50%;
  background: #dc2626; color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}

/* 筛选栏 */
.filter-bar {
  display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;
  background: #fff; padding: 16px 24px; border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.cat-group, .sort-group { display: flex; align-items: center; gap: 8px; }
.filter-label { font-size: 14px; font-weight: 600; color: #374151; white-space: nowrap; }
.cat-btn {
  padding: 6px 16px; border-radius: 20px; border: 1px solid #e5e7eb;
  background: #fff; color: #374151; font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.cat-btn.active, .cat-btn:hover { background: #5C8AEB; color: #fff; border-color: #5C8AEB; }

/* 商品网格 */
.product-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 16px;
}
.product-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07); transition: all 0.25s; cursor: pointer;
}
.product-card:hover { transform: translateY(-6px); box-shadow: 0 10px 28px rgba(0,0,0,0.13); }
.product-img-wrap { position: relative; height: 200px; overflow: hidden; }
.product-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.product-card:hover .product-img { transform: scale(1.06); }

/* 标签 */
.product-badge {
  position: absolute; top: 10px; right: 10px;
  padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; color: #fff;
}
.badge-hot { background: linear-gradient(135deg, #f093fb, #f5576c); }
.badge-new { background: linear-gradient(135deg, #7BAAF2, #5C8AEB); }
.badge-sale { background: linear-gradient(135deg, #f59e0b, #ef4444); }
.badge-out { background: rgba(0,0,0,0.55); top: auto; bottom: 10px; right: 10px; }

.product-body { padding: 14px 16px; }
.product-name { font-size: 15px; font-weight: 700; color: #111827; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-cat-tag {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  background: rgba(92,138,235,0.08); color: #5C8AEB; font-size: 11px; font-weight: 600; margin-bottom: 8px;
}
.product-desc {
  font-size: 12px; color: #6b7280; line-height: 1.5; margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  height: 36px;
}
.product-stats { display: flex; justify-content: space-between; font-size: 12px; color: #9ca3af; margin-bottom: 10px; }
.product-footer { border-top: 1px solid #f3f4f6; padding-top: 10px; }
.product-price { font-size: 22px; font-weight: 800; color: #dc2626; display: block; margin-bottom: 8px; }
.product-actions { display: flex; gap: 8px; align-items: center; }
.add-cart-btn {
  flex: 1; padding: 7px 0; background: #5C8AEB; color: #fff;
  border: none; border-radius: 6px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
}
.add-cart-btn:hover:not(:disabled) { background: #4A78D6; }
.add-cart-btn:disabled { background: #d1d5db; cursor: not-allowed; }
.add-cart-btn.large { padding: 10px 24px; font-size: 15px; border-radius: 8px; }

/* 详情对话框 */
.detail-wrap { display: flex; gap: 24px; }
.detail-img-col { flex: 0 0 300px; }
.detail-img { width: 100%; border-radius: 10px; object-fit: cover; }
.detail-info-col { flex: 1; }
.detail-name { font-size: 22px; font-weight: 700; color: #111827; margin-bottom: 8px; }
.detail-price { font-size: 32px; font-weight: 800; color: #dc2626; margin: 12px 0; }
.detail-stats-row { display: flex; gap: 24px; padding: 12px 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 16px; }
.detail-stat { display: flex; flex-direction: column; gap: 2px; }
.detail-stat span { font-size: 12px; color: #9ca3af; }
.detail-stat strong { font-size: 18px; font-weight: 700; color: #111827; }
.detail-desc-block, .detail-spec-block { margin-bottom: 14px; }
.detail-desc-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 6px; }
.detail-desc-text { font-size: 13px; color: #6b7280; line-height: 1.7; margin: 0; }
.detail-actions { display: flex; gap: 12px; align-items: center; margin-top: 20px; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
