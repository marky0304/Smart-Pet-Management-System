<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="hero-title">
          照顾好你的<br>
          <span class="highlight">毛茸茸伙伴</span>
        </h1>
        <p class="hero-subtitle">健康记录、服务预约、用品购买，一个地方搞定</p>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">10,000+</div>
            <div class="stat-label">宠物主人</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">50,000+</div>
            <div class="stat-label">服务记录</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">98%</div>
            <div class="stat-label">好评率</div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button class="btn-brand" size="large" round @click="$router.push('/dashboard/pet')">管理我的宠物</el-button>
          <el-button size="large" round plain @click="$router.push('/dashboard/service')">预约服务</el-button>
        </div>
      </div>
      <div class="welcome-image">
        <div class="carousel-wrapper">
          <el-carousel ref="carouselRef" :interval="5000" arrow="always" indicator-position="none" class="pet-carousel" :style="{ '--carousel-height': carouselHeight }">
            <el-carousel-item v-for="(image, index) in carouselImages" :key="index">
              <img :src="image.url" :alt="image.alt" class="carousel-image" @load="index === 0 ? onImgLoad($event) : null" />
              <div class="carousel-caption">{{ image.alt }}</div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </div>

    <!-- 功能导航卡片（玻璃态·治愈系配色） -->
    <div class="glass-stats-row">
      <!-- 卡片1：宠物档案管理（珊瑚粉调） -->
      <div class="glass-card glass-card--pet" @click="$router.push('/dashboard/pet')">
        <div class="glass-card-inner">
          <svg viewBox="0 0 48 48" class="glass-icon" stroke="currentColor" fill="none">
            <rect x="10" y="4" width="28" height="38" rx="4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="16" y1="14" x2="32" y2="14" stroke-width="2" stroke-linecap="round"/>
            <line x1="16" y1="21" x2="28" y2="21" stroke-width="2" stroke-linecap="round"/>
            <line x1="16" y1="28" x2="24" y2="28" stroke-width="2" stroke-linecap="round"/>
            <circle cx="18" cy="36" r="4" stroke-width="1.8"/>
            <path d="M21 39 L26 34" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <div class="glass-count">{{ displayStats[0].display }}</div>
          <div class="glass-headline">宠物档案管理</div>
          <div class="glass-desc">一键管理宠物信息</div>
          <div class="glass-subline" v-if="displayStats[0].display === 0">{{ displayStats[0].emptyTip }}</div>
          <div class="glass-subline accent" v-else>共 {{ displayStats[0].display }} 只宠物</div>
        </div>
      </div>

      <!-- 卡片2：健康数据追踪（清新绿调） -->
      <div class="glass-card glass-card--health" @click="$router.push('/dashboard/health')">
        <div class="glass-card-inner">
          <svg viewBox="0 0 48 48" class="glass-icon" stroke="currentColor" fill="none">
            <path d="M8 30 C20 16, 28 44, 40 20" stroke-width="2" stroke-linecap="round"/>
            <path d="M24 38 C12 30, 4 22, 4 16 C4 11, 8 7, 13 7 C16 7, 19 8.5, 21 11 L24 15 L27 11 C29 8.5, 32 7, 35 7 C40 7, 44 11, 44 16 C44 22, 36 30, 24 38Z" stroke-width="1.8"/>
            <line x1="20" y1="16" x2="28" y2="16" stroke-width="2" stroke-linecap="round"/>
            <line x1="24" y1="12" x2="24" y2="20" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <div class="glass-count">{{ displayStats[1].display }}</div>
          <div class="glass-headline">健康数据追踪</div>
          <div class="glass-desc">实时监测健康状况</div>
          <div class="glass-subline accent" v-if="displayStats[1].trend">{{ displayStats[1].trend }}</div>
          <div class="glass-subline muted" v-else-if="displayStats[1].display === 0">{{ displayStats[1].emptyTip }}</div>
          <div class="glass-subline muted" v-else>共 {{ displayStats[1].display }} 条记录</div>
        </div>
      </div>

      <!-- 卡片3：服务预约管理（暖橙调） -->
      <div class="glass-card glass-card--appointment" @click="$router.push('/dashboard/my-appointment')">
        <div class="glass-card-inner">
          <svg viewBox="0 0 48 48" class="glass-icon" stroke="currentColor" fill="none">
            <rect x="8" y="6" width="32" height="36" rx="5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="8" y1="16" x2="40" y2="16" stroke-width="2"/>
            <line x1="18" y1="3" x2="18" y2="10" stroke-width="2" stroke-linecap="round"/>
            <line x1="30" y1="3" x2="30" y2="10" stroke-width="2" stroke-linecap="round"/>
            <rect x="13" y="21" width="7" height="7" rx="2" stroke-width="1.6"/>
            <rect x="24" y="21" width="7" height="7" rx="2" stroke-width="1.6"/>
            <rect x="13" y="31" width="7" height="7" rx="2" stroke-width="1.6"/>
            <line x1="28" y1="31" x2="33" y2="36" stroke-width="1.6" stroke-linecap="round"/>
            <line x1="33" y1="31" x2="28" y2="36" stroke-width="1.6" stroke-linecap="round"/>
          </svg>
          <div class="glass-count">{{ displayStats[2].display }}</div>
          <div class="glass-headline">服务预约管理</div>
          <div class="glass-desc">快速预约宠物服务</div>
          <div class="glass-subline accent" v-if="displayStats[2].badge > 0">{{ displayStats[2].badge }} 待处理</div>
          <div class="glass-subline muted" v-else-if="displayStats[2].display === 0">{{ displayStats[2].emptyTip }}</div>
          <div class="glass-subline muted" v-else>共 {{ displayStats[2].display }} 条预约</div>
        </div>
      </div>

      <!-- 卡片4：社区互动交流（天空蓝调） -->
      <div class="glass-card glass-card--community" @click="$router.push('/dashboard/community')">
        <div class="glass-card-inner">
          <svg viewBox="0 0 48 48" class="glass-icon" stroke="currentColor" fill="none">
            <rect x="7" y="9" width="29" height="22" rx="10" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polygon points="17,31 12,40 25,29" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="18.5" cy="20" r="2.5" stroke-width="1.6"/>
            <circle cx="28.5" cy="20" r="2.5" stroke-width="1.6"/>
          </svg>
          <div class="glass-count">{{ displayStats[3].display }}</div>
          <div class="glass-headline">社区互动交流</div>
          <div class="glass-desc">分享养宠日常</div>
          <div class="glass-subline accent" v-if="displayStats[3].trend">{{ displayStats[3].trend }}</div>
          <div class="glass-subline muted" v-else-if="displayStats[3].display === 0">{{ displayStats[3].emptyTip }}</div>
          <div class="glass-subline muted" v-else>共 {{ displayStats[3].display }} 条动态</div>
        </div>
      </div>
    </div>

    <!-- 宠物分类板块 -->
    <div v-for="category in petCategories" :key="category.key" class="pet-category-section">
      <div class="category-header" :style="{ borderLeft: `4px solid ${category.accentColor}` }">
        <span class="category-icon">{{ category.icon }}</span>
        <span class="category-name">{{ category.name }}</span>
        <span class="category-desc">{{ category.desc }}</span>
        <span class="category-more" :style="{ color: category.accentColor }" @click="$router.push('/dashboard/shop')">进入商城 »</span>
      </div>

      <div class="category-body">
        <div class="category-cover" :style="{ background: category.coverBg }">
          <img :src="category.coverImg" :alt="category.name" class="cover-img" />
          <div class="cover-text">
            <div class="cover-title">{{ category.coverTitle }}</div>
            <div class="cover-sub">{{ category.coverSub }}</div>
          </div>
        </div>

        <div class="category-right">
          <div class="products-label">热门推荐 »</div>
          <div class="products-row">
            <div class="product-card" v-for="product in category.snacks.slice(0,4)" :key="product.name" @click="$router.push('/dashboard/shop')">
              <div class="product-img-wrap">
                <img :src="product.img" :alt="product.name" class="product-img" />
              </div>
              <div class="product-name">{{ product.name }}</div>
              <div class="product-tag">{{ product.tag }}</div>
            </div>
          </div>

          <div class="products-label" style="margin-top:14px">用品分类 »</div>
          <div class="supply-row">
            <div class="supply-card" v-for="supply in category.supplies" :key="supply.name" @click="$router.push('/dashboard/shop')">
              <div class="supply-img-wrap">
                <img :src="supply.img" :alt="supply.name" class="supply-img" />
              </div>
              <div>
                <div class="supply-name">{{ supply.name }}</div>
                <div class="supply-desc">{{ supply.desc }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全事项折叠面板 -->
      <div class="safety-section">
        <el-collapse>
          <el-collapse-item>
            <template #title>
              <span class="safety-title">⚠️ {{ category.name }}养护安全事项</span>
            </template>
            <div class="safety-grid">
              <div class="safety-item" v-for="tip in category.tips" :key="tip.title">
                <div class="safety-icon" :style="{ background: tip.bg }">{{ tip.icon }}</div>
                <div class="safety-content">
                  <div class="safety-item-title">{{ tip.title }}</div>
                  <div class="safety-item-desc">{{ tip.desc }}</div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- ==================== 页脚 ==================== -->
    <footer class="home-footer">
      <!-- 品牌服务介绍 -->
      <div class="footer-brand">
        <div class="footer-logo">
          <svg viewBox="0 0 64 64" class="footer-paw-icon">
            <path d="M32 48c-4 0-8-2-10-5-2-3-2-7 0-10 2-3 6-5 10-5s8 2 10 5c2 3 2 7 0 10-2 3-6 5-10 5z" fill="currentColor"/>
            <circle cx="20" cy="24" r="6" fill="currentColor"/>
            <circle cx="44" cy="24" r="6" fill="currentColor"/>
            <circle cx="14" cy="38" r="5" fill="currentColor"/>
            <circle cx="50" cy="38" r="5" fill="currentColor"/>
          </svg>
          <span class="footer-brand-name">PetCare</span>
        </div>
        <p class="footer-brand-desc">
          PetCare 智慧宠物管理系统，为您提供宠物健康管理、服务预约、用品购买的一站式解决方案。用心呵护每一位毛茸茸的家庭成员。
        </p>
      </div>

      <!-- 快捷导航 -->
      <div class="footer-nav">
        <div class="footer-col">
          <h4 class="footer-col-title">服务导航</h4>
          <a @click="$router.push('/dashboard/health')">健康记录</a>
          <a @click="$router.push('/dashboard/service')">预约服务</a>
          <a @click="$router.push('/dashboard/shop')">宠物商城</a>
          <a @click="$router.push('/dashboard/community')">社区互动</a>
        </div>
        <div class="footer-col">
          <h4 class="footer-col-title">帮助中心</h4>
          <a @click="$router.push('/dashboard/service')">常见问题</a>
          <a @click="$router.push('/dashboard/profile')">联系客服</a>
          <a @click="$router.push('/dashboard/pet')">新手指南</a>
          <a @click="$router.push('/dashboard/my-appointment')">预约说明</a>
        </div>
        <div class="footer-col">
          <h4 class="footer-col-title">关于我们</h4>
          <a @click="$router.push('/dashboard/profile')">项目简介</a>
          <a @click="$router.push('/dashboard/profile')">开发团队</a>
          <a @click="$router.push('/dashboard/profile')">版本更新</a>
          <a @click="$router.push('/dashboard/profile')">意见反馈</a>
        </div>
      </div>

      <!-- 版权声明 -->
      <div class="footer-bottom">
        <span>Copyright &copy; 2024 - {{ new Date().getFullYear() }} PetCare 智慧宠物管理系统</span>
        <span class="footer-divider">|</span>
        <span>Version 1.0.0</span>
        <span class="footer-divider">|</span>
        <span>毕业设计项目</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import img1 from '@/public/home image/0d971146ca5306a54d968aab2cf964f5~tplv-be4g95zd3a-image.jpeg'
import img2 from '@/public/home image/5b12b21ebae66cce6128629ae8181cf8~tplv-be4g95zd3a-image.jpeg'
import img3 from '@/public/home image/7733f45262942adf2e9885bfcb7ca1b8~tplv-be4g95zd3a-image.jpeg'
import img4 from '@/public/home image/2163207e95233bbbed7c3a4b61a9d92c~tplv-be4g95zd3a-image.jpeg'
import img5 from '@/public/home image/7cd38b5fb9ae9fda8aaa6359d1f9ec37~tplv-be4g95zd3a-image.jpeg'
import img6 from '@/public/home image/f4b5800d079fc27aa0eea0c1d7e4faa7~tplv-be4g95zd3a-image.jpeg'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TrendCharts, House } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const carouselRef = ref(null)
const carouselHeight = ref('300px')

const carouselPrev = () => carouselRef.value?.prev()
const carouselNext = () => carouselRef.value?.next()

// 图片加载后动态设置轮播高度
const onImgLoad = (e) => {
  const img = e.target
  const ratio = img.naturalHeight / img.naturalWidth
  const width = img.offsetWidth
  carouselHeight.value = Math.round(width * ratio) + 'px'
}

const carouselImages = ref([
  { url: img1, alt: '可爱的宠物', pos: 'center 40%' },
  { url: img2, alt: '可爱的宠物', pos: 'center 30%' },
  { url: img3, alt: '可爱的宠物', pos: 'center 40%' },
  { url: img4, alt: '可爱的宠物', pos: 'center 35%' },
  { url: img5, alt: '可爱的宠物', pos: 'center 40%' },
  { url: img6, alt: '可爱的宠物', pos: 'center 35%' },
])

const stats = ref([
  { title: '我的宠物', value: 0, icon: 'PetIcon', class: 'stat-primary', trend: '', route: '/dashboard/pet' },
  { title: '健康记录', value: 0, icon: 'FirstAidKit', class: 'stat-success', trend: '', route: '/dashboard/health' },
  { title: '预约订单', value: 0, icon: 'Calendar', class: 'stat-warning', trend: '', route: '/dashboard/my-appointment' },
  { title: '我的动态', value: 0, icon: 'ChatDotRound', class: 'stat-danger', trend: '', route: '/dashboard/community' }
])

const handleStatClick = (route) => { if (route) router.push(route) }

// 数字滚动动画
const displayStats = ref([
  { title: '宠物档案管理', display: 0, value: 0, route: '/dashboard/pet', trend: '', emptyTip: '还没有宠物，快去添加吧～', badge: 0 },
  { title: '健康数据追踪', display: 0, value: 0, route: '/dashboard/health', trend: '', emptyTip: '还没有健康记录，快去建档吧～', badge: 0 },
  { title: '服务预约管理', display: 0, value: 0, route: '/dashboard/my-appointment', trend: '', emptyTip: '暂无预约，去预约服务吧～', badge: 0 },
  { title: '社区互动交流', display: 0, value: 0, route: '/dashboard/community', trend: '', emptyTip: '还没有动态，去社区分享吧～', badge: 0 }
])

const animateNumber = (index, target) => {
  const duration = 1200
  const start = Date.now()
  const tick = () => {
    const elapsed = Date.now() - start
    const progress = Math.min(elapsed / duration, 1)
    const ease = 1 - Math.pow(1 - progress, 3)
    displayStats.value[index].display = Math.round(target * ease)
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

const loadStats = async () => {
  try {
    const now = new Date()
    const weekAgo = new Date(now - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const [statsRes, healthRes, appointmentRes] = await Promise.allSettled([
      request({ url: '/user/current/stats', method: 'get' }),
      request({ url: '/health/records', method: 'get', params: { pageNum: 1, pageSize: 200 } }),
      request({ url: '/appointment/my-list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    ])
    if (statsRes.status === 'fulfilled') {
      const d = statsRes.value?.data
      const petCount = d?.petCount ?? 0
      const postCount = d?.postCount ?? 0
      displayStats.value[0].value = petCount
      displayStats.value[3].value = postCount
      displayStats.value[3].trend = postCount > 0 ? `共 ${postCount} 条动态` : ''
      animateNumber(0, petCount)
      animateNumber(3, postCount)
    }
    if (healthRes.status === 'fulfilled') {
      const d = healthRes.value?.data
      const records = d?.records || []
      const total = d?.total ?? records.length
      const thisWeek = records.filter(r => { const date = r.recordDate || r.createTime; return date && date.slice(0,10) >= weekAgo }).length
      displayStats.value[1].value = total
      displayStats.value[1].trend = thisWeek > 0 ? `本周新增 ${thisWeek} 条` : ''
      animateNumber(1, total)
    }
    if (appointmentRes.status === 'fulfilled') {
      const d = appointmentRes.value?.data
      const records = d?.records || []
      const active = records.filter(a => a.status !== 'CANCELLED')
      const pending = active.filter(a => a.status === 'PENDING' || a.status === 'CONFIRMED').length
      displayStats.value[2].value = active.length
      displayStats.value[2].badge = pending
      displayStats.value[2].trend = pending > 0 ? `${pending} 待处理` : ''
      animateNumber(2, active.length)
    }
  } catch (e) {}
}

onMounted(() => { loadStats() })

// ===================== 宠物分类数据 =====================
const petCategories = ref([
  {
    key: 'dog',
    icon: '🐕',
    name: '狗狗',
    desc: '忠诚的伙伴，活力满满',
    accentColor: '#ea580c',
    accentBg: '#fff7ed',
    coverBg: 'linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%)',
    coverImg: '/shop image/1.png',
    coverTitle: '狗狗的快乐时光',
    coverSub: '精选主粮·零食·玩具',
    snacks: [
      { name: '鸡肉磨牙棒', tag: '洁齿护龈', img: '/shop image/4.png' },
      { name: '牛肉冻干', tag: '高蛋白补充', img: '/shop image/5.png' },
      { name: '三文鱼条', tag: '美毛亮泽', img: '/shop image/6.png' },
      { name: '奶酪训练豆', tag: '训练奖励', img: '/shop image/7.png' }
    ],
    supplies: [
      { name: '狗狗主粮', desc: '进口粮、国产粮、处方粮', img: '/shop image/8.png' },
      { name: '狗狗零食', desc: '磨牙棒、肉类零食、洁齿', img: '/shop image/9.png' },
      { name: '医疗护理', desc: '驱虫、皮肤护理、营养品', img: '/shop image/10.png' },
      { name: '玩具用品', desc: '益智玩具、牵引绳、衣物', img: '/shop image/11.png' }
    ],
    tips: [
      { icon: '💉', bg: '#fee2e2', title: '定期接种疫苗', desc: '每年接种狂犬疫苗及犬六联疫苗，幼犬需完成3针基础免疫，保护宠物和家人健康。' },
      { icon: '🦟', bg: '#fef3c7', title: '每月体外驱虫', desc: '定期使用体外驱虫药（滴剂/项圈）防治跳蚤、蜱虫，每季度进行一次体内驱虫。' },
      { icon: '🍫', bg: '#f3e8ff', title: '远离有毒食物', desc: '巧克力、葡萄、洋葱、大蒜、木糖醇对狗狗有毒，误食需立即就医，切勿自行催吐。' },
      { icon: '🚗', bg: 'rgba(92,138,235,0.1)', title: '外出安全防护', desc: '外出须全程佩戴牵引绳，大型犬加戴嘴套；乘车使用安全带或航空箱固定，防止意外。' },
      { icon: '🌡️', bg: '#dcfce7', title: '防暑防寒管理', desc: '夏季避免高温时段出行，严禁将狗狗单独留在密闭车内；冬季为短毛犬穿衣保暖。' },
      { icon: '🦷', bg: '#fff7ed', title: '口腔清洁护理', desc: '每周刷牙1-2次，使用宠物专用牙膏，定期喂食洁齿零食，预防牙结石和牙周病。' }
    ]
  },
  {
    key: 'cat',
    icon: '🐈',
    name: '猫咪',
    desc: '优雅独立，治愈系伙伴',
    accentColor: '#db2777',
    accentBg: '#fdf2f8',
    coverBg: 'linear-gradient(135deg, #fdf2f8 0%, #fce7f3 100%)',
    coverImg: '/shop image/12.png',
    coverTitle: '铲屎官必备清单',
    coverSub: '猫粮·猫砂·猫抓板',
    snacks: [
      { name: '冻干鸡胸肉', tag: '高蛋白低脂', img: '/shop image/14.png' },
      { name: '猫条湿粮', tag: '补水增食欲', img: '/shop image/15.png' },
      { name: '三文鱼冻干', tag: '美毛护肤', img: '/shop image/16.png' },
      { name: '化毛膏', tag: '排毛球必备', img: '/shop image/17.png' }
    ],
    supplies: [
      { name: '猫咪主粮', desc: '全价猫粮、处方粮、幼猫粮', img: '/shop image/18.png' },
      { name: '猫砂猫盆', desc: '豆腐砂、膨润土、水晶砂', img: '/shop image/19.png' },
      { name: '猫抓板', desc: '瓦楞纸、剑麻、猫爬架', img: '/shop image/20.png' },
      { name: '医疗护理', desc: '驱虫、营养膏、泌尿护理', img: '/shop image/21.png' }
    ],
    tips: [
      { icon: '💧', bg: 'rgba(92,138,235,0.1)', title: '鼓励多饮水', desc: '猫咪天生饮水量少，建议使用流动饮水机，以湿粮为主（占60%以上），预防泌尿系统疾病。' },
      { icon: '🌿', bg: '#dcfce7', title: '警惕有毒植物', desc: '百合花对猫咪毒性极强，误食可致肾衰竭；绿萝、水仙、郁金香等也有毒，家中需移除。' },
      { icon: '🏠', bg: '#fef3c7', title: '阳台窗户防护', desc: '为阳台和窗户安装防护网，防止猫咪坠落（高楼坠落综合征）；检查洗衣机等密闭空间。' },
      { icon: '✂️', bg: '#fee2e2', title: '定期修剪指甲', desc: '每2-3周修剪一次指甲，防止抓伤家人和家具；同时定期清洁耳道，预防耳螨感染。' },
      { icon: '🧹', bg: '#f3e8ff', title: '猫砂盆日常管理', desc: '每天清理结块，每周彻底换砂，每月清洗猫砂盆。多猫家庭遵循"猫数+1"原则配置猫砂盆。' },
      { icon: '💊', bg: '#fff7ed', title: '绝育与疫苗', desc: '建议4-6月龄绝育，可降低90%乳腺肿瘤风险；每年接种猫三联疫苗，预防猫瘟、猫鼻支。' }
    ]
  },
])
</script>

<style scoped>
.home { padding: 0; background: #FBF9F7; }

/* ===== 品牌按钮 ===== */
.btn-brand {
  background: #5C8AEB !important;
  border-color: #5C8AEB !important;
  color: #fff !important;
  font-weight: 600;
}
.btn-brand:hover {
  background: #4A78D6 !important;
  border-color: #4A78D6 !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(92,138,235,0.35) !important;
}

/* ===== 欢迎横幅 ===== */
.welcome-banner {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  align-items: stretch;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  min-height: 360px;
}
.welcome-content {
  flex: 0 0 44%;
  padding: 40px 32px 40px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.hero-title { font-size: 38px; font-weight: 700; line-height: 1.25; margin-bottom: 12px; color: #111827; letter-spacing: -0.5px; }
.highlight { color: #5C8AEB; }
.hero-subtitle { font-size: 15px; color: #6b7280; margin-bottom: 24px; line-height: 1.7; }
.hero-stats { display: flex; gap: 12px; margin-bottom: 28px; }
.stat-item {
  padding: 12px 18px; border-radius: 8px;
  background: #f9fafb; border: 1px solid #e5e7eb;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}
.stat-number { font-size: 24px; font-weight: 800; color: #5C8AEB; margin-bottom: 2px; }
.stat-label { font-size: 12px; color: #9ca3af; font-weight: 500; }
.hero-actions { display: flex; gap: 12px; }

/* ===== 轮播 ===== */
.welcome-image { flex: 1; min-width: 0; display: flex; align-items: center; overflow: hidden; }
.carousel-wrapper { width: 100%; height: 100%; display: flex; align-items: center; }
.pet-carousel { width: 100%; }
:deep(.pet-carousel .el-carousel__container) {
  height: var(--carousel-height, 360px) !important;
  max-height: 420px;
}
:deep(.el-carousel__arrow) { background: rgba(0,0,0,0.3); }
:deep(.el-carousel__arrow:hover) { background: #5C8AEB; }
.carousel-image { width: 100%; height: 100%; object-fit: cover; object-position: center; display: block; }
.carousel-caption {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 32px 16px 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.45));
  color: #fff; font-size: 13px; font-weight: 500;
}

/* ===== 玻璃态功能导航卡片（治愈系四色） ===== */
/* 色调声明 */
:root {
  --pet-coral:     #E8817A;
  --pet-coral-bg:  #FFF5F3;
  --pet-coral-sub: #FDEAE7;
  --health-green:  #4DA88A;
  --health-green-bg: #F2FAF6;
  --health-green-sub: #DCF2E8;
  --apt-orange:    #ED7B4A;
  --apt-orange-bg: #FFF6F0;
  --apt-orange-sub: #FDE8DC;
  --comm-blue:     #5A9FC6;
  --comm-blue-bg:  #F3F8FC;
  --comm-blue-sub: #DFEEF6;
}
.glass-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}
.glass-card {
  border-radius: 28px;
  padding: 28px 20px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.glass-card-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}
.glass-card--pet {
  background: var(--pet-coral-bg);
  color: var(--pet-coral);
  box-shadow: 0 2px 16px rgba(232, 129, 122, 0.08), 0 0 0 1px rgba(232, 129, 122, 0.06);
}
.glass-card--pet:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(232, 129, 122, 0.14), 0 0 0 1px rgba(232, 129, 122, 0.12);
}
.glass-card--health {
  background: var(--health-green-bg);
  color: var(--health-green);
  box-shadow: 0 2px 16px rgba(77, 168, 138, 0.08), 0 0 0 1px rgba(77, 168, 138, 0.06);
}
.glass-card--health:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(77, 168, 138, 0.14), 0 0 0 1px rgba(77, 168, 138, 0.12);
}
.glass-card--appointment {
  background: var(--apt-orange-bg);
  color: var(--apt-orange);
  box-shadow: 0 2px 16px rgba(237, 123, 74, 0.08), 0 0 0 1px rgba(237, 123, 74, 0.06);
}
.glass-card--appointment:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(237, 123, 74, 0.14), 0 0 0 1px rgba(237, 123, 74, 0.12);
}
.glass-card--community {
  background: var(--comm-blue-bg);
  color: var(--comm-blue);
  box-shadow: 0 2px 16px rgba(90, 159, 198, 0.08), 0 0 0 1px rgba(90, 159, 198, 0.06);
}
.glass-card--community:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(90, 159, 198, 0.14), 0 0 0 1px rgba(90, 159, 198, 0.12);
}
.glass-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  color: inherit;
}
.glass-count {
  font-size: 30px;
  font-weight: 700;
  color: #111;
  line-height: 1;
  margin-bottom: 8px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}
.glass-headline {
  font-size: 15px;
  font-weight: 700;
  color: #111;
  line-height: 1.3;
  margin-bottom: 4px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}
.glass-desc {
  font-size: 12px;
  color: #8C8C8C;
  font-weight: 400;
  line-height: 1.4;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}
.glass-subline {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 8px;
  line-height: 1.4;
  font-weight: 400;
}
.glass-subline.accent {
  color: inherit;
  font-weight: 500;
}
.glass-subline.muted {
  color: #9ca3af;
}

/* ===== 宠物分类板块 ===== */
.pet-category-section {
  background: #fff; border-radius: 8px; margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
  overflow: hidden;
}
.category-header {
  display: flex; align-items: center; gap: 10px;
  padding: 16px 24px; border-bottom: 1px solid #f3f4f6;
  border-left: 4px solid #5C8AEB;
}
.category-icon { font-size: 24px; }
.category-name { font-size: 20px; font-weight: 700; color: #111827; }
.category-desc { font-size: 13px; color: #9ca3af; margin-left: 4px; }
.category-more { margin-left: auto; font-size: 13px; color: #5C8AEB; cursor: pointer; font-weight: 500; }
.category-more:hover { text-decoration: underline; }

.category-body { display: flex; padding: 20px 24px; align-items: flex-start; }
.category-cover {
  width: 200px; min-width: 200px; border-radius: 8px; overflow: hidden;
  position: relative; height: 200px; flex-shrink: 0;
}
.cover-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.cover-text {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 20px 14px 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  color: #fff;
}
.cover-title { font-size: 14px; font-weight: 700; margin-bottom: 2px; }
.cover-sub { font-size: 12px; opacity: 0.85; }

.category-right { flex: 1; padding-left: 20px; }
.products-label { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; cursor: pointer; }
.products-label:hover { color: #5C8AEB; }

.products-row { display: flex; gap: 10px; }
.product-card {
  flex: 1; border: 1px solid #f0f0f0; border-radius: 8px; padding: 10px;
  text-align: center; cursor: pointer; transition: all 0.2s; background: #fafafa;
}
.product-card:hover { border-color: #5C8AEB; box-shadow: 0 3px 12px rgba(92,138,235,0.15); transform: translateY(-2px); }
.product-img-wrap { width: 72px; height: 72px; margin: 0 auto 8px; border-radius: 6px; overflow: hidden; }
.product-img { width: 100%; height: 100%; object-fit: cover; }
.product-name { font-size: 12px; font-weight: 600; color: #111827; margin-bottom: 2px; }
.product-tag { font-size: 11px; color: #9ca3af; }

.supply-row { display: flex; gap: 10px; }
.supply-card {
  flex: 1; display: flex; align-items: center; gap: 8px;
  border: 1px solid #f0f0f0; border-radius: 8px; padding: 10px;
  cursor: pointer; transition: all 0.2s; background: #fafafa;
}
.supply-card:hover { border-color: #5C8AEB; background: #eff6ff; }
.supply-img-wrap { width: 44px; height: 44px; border-radius: 6px; overflow: hidden; flex-shrink: 0; }
.supply-img { width: 100%; height: 100%; object-fit: cover; }
.supply-name { font-size: 12px; font-weight: 600; color: #111827; margin-bottom: 2px; }
.supply-desc { font-size: 11px; color: #9ca3af; line-height: 1.4; }

/* ===== 安全事项折叠 ===== */
.safety-section { border-top: 1px solid #f3f4f6; }
:deep(.safety-section .el-collapse) { border: none; }
:deep(.safety-section .el-collapse-item__header) {
  padding: 14px 24px;
  font-size: 14px; font-weight: 600; color: #374151;
  background: #fafafa; border: none;
}
:deep(.safety-section .el-collapse-item__wrap) { border: none; }
:deep(.safety-section .el-collapse-item__content) { padding: 16px 24px 20px; }
.safety-title { font-size: 14px; font-weight: 600; color: #374151; }
.safety-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.safety-item {
  display: flex; align-items: flex-start; gap: 10px;
  background: #fff; border-radius: 8px; padding: 12px;
  border: 1px solid #f0f0f0;
}
.safety-icon { width: 32px; height: 32px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
.safety-item-title { font-size: 12px; font-weight: 600; color: #111827; margin-bottom: 3px; }
.safety-item-desc { font-size: 11px; color: #6b7280; line-height: 1.6; }

/* ==================== 页脚 ==================== */
.home-footer {
  background: #fff;
  border-radius: 8px;
  margin-top: 20px;
  padding: 36px 40px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07), 0 0 0 1px rgba(0,0,0,0.04);
}

/* 品牌介绍区 */
.footer-brand {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding-bottom: 28px;
  margin-bottom: 28px;
  border-bottom: 1px solid #eef2f6;
}
.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.footer-paw-icon {
  width: 36px;
  height: 36px;
  color: #5C8AEB;
}
.footer-brand-name {
  font-size: 22px;
  font-weight: 700;
  color: #5C8AEB;
  letter-spacing: -0.5px;
}
.footer-brand-desc {
  max-width: 640px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
  margin: 0;
}

/* 导航链接区 */
.footer-nav {
  display: flex;
  gap: 48px;
  padding-bottom: 24px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eef2f6;
}
.footer-col-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 14px 0;
}
.footer-col {
  display: flex;
  flex-direction: column;
  gap: 9px;
  flex: 1;
}
.footer-col a {
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.15s;
  text-decoration: none;
}
.footer-col a:hover {
  color: #5C8AEB;
}

/* 版权声明区 */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 12px;
  color: #9ca3af;
}
.footer-divider {
  color: #e5e7eb;
}
</style>
