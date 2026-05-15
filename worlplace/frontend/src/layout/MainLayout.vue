<template>
  <el-container class="modern-layout-container">
    <!-- 顶部导航栏 - 现代设计 -->
    <el-header class="modern-header">
      <div class="header-content">
        <!-- Logo区域 - 3D效果 -->
        <div class="modern-logo" @click="goHome">
          <div class="logo-paw">
            <svg viewBox="0 0 64 64" class="paw-icon">
              <path d="M32 48c-4 0-8-2-10-5-2-3-2-7 0-10 2-3 6-5 10-5s8 2 10 5c2 3 2 7 0 10-2 3-6 5-10 5z" fill="currentColor"/>
              <circle cx="20" cy="24" r="6" fill="currentColor"/>
              <circle cx="44" cy="24" r="6" fill="currentColor"/>
              <circle cx="14" cy="38" r="5" fill="currentColor"/>
              <circle cx="50" cy="38" r="5" fill="currentColor"/>
            </svg>
          </div>
          <div class="logo-text-wrapper">
            <span class="logo-title">PetCare</span>
            <span class="logo-subtitle">智慧宠物管理</span>
          </div>
        </div>

        <!-- 现代导航菜单 -->
        <nav class="modern-nav">
          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/home' }" @click="navigateTo('/dashboard/home')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </div>
            <span class="nav-label">首页</span>
          </div>
          
          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/pet' }" @click="navigateTo('/dashboard/pet')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 14c-2.5 0-4.5 1.5-5 3.5-.2.8.3 1.5 1.1 1.5h7.8c.8 0 1.3-.7 1.1-1.5-.5-2-2.5-3.5-5-3.5z"/>
                <circle cx="8" cy="8" r="2"/>
                <circle cx="16" cy="8" r="2"/>
                <circle cx="6" cy="12" r="1.5"/>
                <circle cx="18" cy="12" r="1.5"/>
              </svg>
            </div>
            <span class="nav-label">宠物</span>
          </div>
          
          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/health' }" @click="navigateTo('/dashboard/health')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
              </svg>
            </div>
            <span class="nav-label">健康</span>
          </div>
          
          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/service' }" @click="navigateTo('/dashboard/service')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <span class="nav-label">预约</span>
          </div>
          
          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/shop' }" @click="navigateTo('/dashboard/shop')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
            </div>
            <span class="nav-label">商城</span>
          </div>

          <div class="nav-item" :class="{ active: activeMenu === '/dashboard/community' }" @click="navigateTo('/dashboard/community')">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <span class="nav-label">社区</span>
          </div>
          
          <!-- 管理员菜单 -->
          <div class="nav-item nav-dropdown" v-if="userStore.isAdmin" @click="toggleAdminMenu">
            <div class="nav-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M12 1v6m0 6v6m5.2-13.2l-4.2 4.2m0 6l4.2 4.2M23 12h-6m-6 0H1m18.2 5.2l-4.2-4.2m0-6l4.2-4.2"/>
              </svg>
            </div>
            <span class="nav-label">管理</span>
            <div class="admin-dropdown" v-show="showAdminMenu">
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/user-management')">用户管理</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/pet-management')">宠物数据</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/health-management')">健康数据</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/service-management')">服务管理</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/appointment-management')">预约管理</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/product-management')">商品管理</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/order-management')">订单管理</div>
              <div class="dropdown-item" @click.stop="navigateTo('/dashboard/chat-config')">客服设置</div>
            </div>
          </div>
        </nav>

        <!-- 现代右侧功能区 -->
        <div class="modern-actions">
          <!-- 购物车 -->
          <div class="action-btn cart-btn" @click="goToCart">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="9" cy="21" r="1"/>
              <circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            <span class="badge" v-if="cartCount > 0">{{ cartCount }}</span>
          </div>
          
          <!-- 通知 -->
          <div class="action-btn notification-btn" @click="showNotifications">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span class="badge pulse" v-if="notificationCount > 0">{{ notificationCount }}</span>
          </div>

          <!-- 订单 -->
          <div class="action-btn order-btn" :class="{ active: activeMenu === '/dashboard/my-orders' }" @click="navigateTo('/dashboard/my-orders')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
          </div>

          <!-- 用户信息 -->
          <div class="user-profile" @click="toggleUserMenu">
            <div class="avatar-wrapper">
              <img v-if="userStore.userInfo.avatar" :src="userStore.userInfo.avatar" class="user-avatar" />
              <div v-else class="user-avatar-text">{{ userStore.userInfo?.nickname?.charAt(0) }}</div>
              <div class="online-indicator"></div>
            </div>
            <div class="user-details">
              <span class="user-name">{{ userStore.userInfo.nickname }}</span>
              <span class="user-role">{{ userStore.isAdmin ? '管理员' : '用户' }}</span>
            </div>
            <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
            
            <!-- 用户下拉菜单 -->
            <div class="user-dropdown-menu" v-show="showUserMenu">
              <div class="dropdown-item" @click.stop="handleCommand('profile')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <span>个人中心</span>
              </div>
              <div class="dropdown-item" @click.stop="handleCommand('my-orders')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                <span>我的订单</span>
              </div>
              <div class="dropdown-item" @click.stop="handleCommand('address')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                <span>我的地址</span>
              </div>
              <div class="dropdown-item" @click.stop="handleCommand('home')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                </svg>
                <span>首页</span>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item logout" @click.stop="handleCommand('logout')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                <span>退出登录</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrapper" v-if="breadcrumbs.length > 0">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard/home' }">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </el-main>

    <!-- 智能客服聊天 -->
    <ChatBubble />
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import ChatBubble from '@/components/ChatBubble.vue'
import { getUnreadCount } from '@/api/notification'
import { useNotificationWebSocket } from '@/composables/useNotificationWebSocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const notificationCount = ref(0)
const cartCount = ref(0)
const showAdminMenu = ref(false)
const showUserMenu = ref(false)

const navigateTo = (path) => {
  router.push(path)
  showAdminMenu.value = false
  showUserMenu.value = false
}

const toggleAdminMenu = () => {
  showAdminMenu.value = !showAdminMenu.value
  showUserMenu.value = false
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
  showAdminMenu.value = false
}

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  // 确保返回完整的路径以匹配菜单项的index
  if (path.startsWith('/dashboard/')) {
    return path
  }
  return '/dashboard/home'
})

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.slice(1).map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

const goHome = () => {
  // 返回工作台首页
  router.push('/dashboard/home')
}

const showNotifications = () => {
  router.push('/dashboard/notifications')
}

const goToCart = () => {
  router.push('/dashboard/cart')
}

// 更新购物车数量
const updateCartCount = () => {
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  cartCount.value = cart.reduce((total, item) => total + item.quantity, 0)
}

// 监听路由变化，更新购物车数量
watch(() => route.path, () => {
  updateCartCount()
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/dashboard/profile')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
      break
    case 'home':
      router.push('/dashboard/home')
      break
    case 'my-orders':
      router.push('/dashboard/my-orders')
      break
    case 'address':
      router.push('/dashboard/address')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      }).catch(() => {
        // 取消退出
      })
      break
  }
}

// WebSocket 实时通知
const { lastNotification, unreadCount: wsUnread, connect: connectWs, disconnect: disconnectWs } = useNotificationWebSocket()

let serverUnread = 0

watch(lastNotification, (n) => {
  if (n) {
    notificationCount.value = serverUnread + wsUnread.value
  }
})

watch(wsUnread, (v) => {
  notificationCount.value = serverUnread + v
})

// 组件挂载时更新购物车数量和通知数量
onMounted(async () => {
  updateCartCount()
  try {
    const res = await getUnreadCount()
    serverUnread = res.data || 0
    notificationCount.value = serverUnread + wsUnread.value
  } catch (e) { /* use default */ }
  connectWs()
})

onUnmounted(() => {
  disconnectWs()
})
</script>

<style scoped>
/* 现代布局容器 */
.modern-layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

/* 现代顶部导航栏 - 浅灰色专业风格 */
.modern-header {
  height: 72px;
  padding: 0;
  background: linear-gradient(135deg, #e8eef3 0%, #dce4ec 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1000;
}

.modern-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,0,0,0.03), transparent);
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 32px;
  max-width: 1920px;
  margin: 0 auto;
}

/* 现代Logo - 3D效果 */
.modern-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-right: 48px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-logo:hover {
  transform: translateY(-2px);
}

.logo-paw {
  width: 48px;
  height: 48px;
  background: #1e3a8a;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.2);
  transition: all 0.4s ease;
}

.logo-paw:hover {
  transform: rotate(-5deg) scale(1.05);
  box-shadow: 0 6px 16px rgba(30, 58, 138, 0.3);
}

.paw-icon {
  width: 28px;
  height: 28px;
  color: white;
  animation: pawBounce 2s ease-in-out infinite;
}

@keyframes pawBounce {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-4px) rotate(5deg); }
}

.logo-text-wrapper {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-size: 22px;
  font-weight: 800;
  color: #1e3a8a;
  letter-spacing: -0.5px;
}

.logo-subtitle {
  font-size: 11px;
  font-weight: 500;
  color: #475569;
  letter-spacing: 1px;
}

/* 现代导航菜单 */
.modern-nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  color: #475569;
  font-weight: 500;
  font-size: 14px;
}

.nav-item::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item:hover {
  color: #1e3a8a;
}

.nav-item.active {
  background: white;
  color: #1e3a8a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #1e3a8a;
  border-radius: 2px;
}

.nav-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-label {
  white-space: nowrap;
}

/* 管理员下拉菜单 */
.nav-dropdown {
  position: relative;
}

.admin-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 8px;
  min-width: 160px;
  z-index: 1001;
  animation: dropdownSlide 0.3s ease;
}

@keyframes dropdownSlide {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  padding: 10px 16px;
  border-radius: 8px;
  color: #1e293b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: #f1f5f9;
  color: #1e3a8a;
  transform: translateX(4px);
}

/* 现代右侧功能区 */
.modern-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.action-btn {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  color: #475569;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.action-btn:hover {
  background: #1e3a8a;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
}

.action-btn.active {
  background: #1e3a8a;
  color: white;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.2);
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.badge.pulse {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 用户资料卡片 */
.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 16px 6px 6px;
  border-radius: 24px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.user-profile:hover {
  background: #f8fafc;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
}

.user-avatar,
.user-avatar-text {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.user-avatar-text {
  background: #1e3a8a;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #10b981;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1;
}

.user-role {
  font-size: 11px;
  color: #475569;
  line-height: 1;
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: #475569;
  transition: transform 0.3s ease;
}

.user-profile:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 用户下拉菜单 */
.user-dropdown-menu {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 8px;
  min-width: 200px;
  z-index: 1001;
  animation: dropdownSlide 0.3s ease;
}

.user-dropdown-menu .dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  color: #1e293b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-dropdown-menu .dropdown-item svg {
  width: 18px;
  height: 18px;
  color: #64748b;
}

.user-dropdown-menu .dropdown-item:hover {
  background: #f1f5f9;
  color: #1e3a8a;
  transform: translateX(4px);
}

.user-dropdown-menu .dropdown-item:hover svg {
  color: #1e3a8a;
}

.user-dropdown-menu .dropdown-item.logout {
  color: #ef4444;
}

.user-dropdown-menu .dropdown-item.logout svg {
  color: #ef4444;
}

.user-dropdown-menu .dropdown-item.logout:hover {
  background: #fee2e2;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 0;
}

/* 现代主内容区 */
.main-content {
  flex: 1;
  background: transparent;
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 现代面包屑 */
.breadcrumb-wrapper {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 16px 32px;
  margin: 20px 20px 0;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(30, 58, 138, 0.08);
}

.breadcrumb-wrapper .el-breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

/* 现代页面内容 */
.page-content {
  padding: 20px;
  min-height: calc(100vh - 72px - 56px);
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 现代通知列表 */
.notification-list {
  padding: 16px;
}

.notification-item {
  display: flex;
  gap: 14px;
  padding: 18px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 14px;
  margin-bottom: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(30, 58, 138, 0.08);
}

.notification-item:hover {
  background: white;
  transform: translateX(8px) scale(1.02);
  box-shadow: 0 8px 24px rgba(30, 58, 138, 0.12);
  border-color: rgba(30, 58, 138, 0.15);
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.notification-icon.type-info {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
}

.notification-icon.type-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
}

.notification-icon.type-success {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.notification-desc {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 10px;
  line-height: 1.6;
}

.notification-time {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

/* 滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 6px;
}

.main-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.main-content::-webkit-scrollbar-track {
  background: transparent;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .logo-text {
    display: none;
  }
  
  .user-name {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-menu .el-menu-item span {
    display: none;
  }
  
  .header-actions {
    gap: 8px;
  }
}
</style>
