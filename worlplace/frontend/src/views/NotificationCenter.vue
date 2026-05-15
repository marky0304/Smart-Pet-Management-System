<template>
  <div class="notification-page">
    <div class="page-header">
      <h3>通知中心</h3>
      <el-button type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部已读
      </el-button>
    </div>

    <div class="notification-list" v-loading="loading">
      <div v-for="n in notifications" :key="n.id"
        class="notif-item" :class="{ unread: n.isRead === 0 }"
        @click="handleClick(n)">
        <div class="notif-icon">{{ typeIcon(n.type) }}</div>
        <div class="notif-body">
          <div class="notif-top">
            <span class="notif-from">{{ n.fromUsername }}</span>
            <span class="notif-type">{{ typeLabel(n.type) }}</span>
            <span class="notif-time">{{ formatTime(n.createTime) }}</span>
          </div>
          <p class="notif-content">{{ n.content }}</p>
        </div>
        <div class="notif-dot" v-if="n.isRead === 0"></div>
      </div>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
    </div>

    <el-pagination v-if="total > pageSize"
      layout="prev, pager, next" :total="total" :page-size="pageSize"
      :current-page="currentPage" @current-change="loadNotifications"
      class="pagination" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, markAsRead, markAllAsRead } from '@/api/notification'
import { useNotificationWebSocket } from '@/composables/useNotificationWebSocket'

const router = useRouter()
const notifications = ref([])
const total = ref(0)
const unreadCount = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)

const typeIcon = (type) => {
  const map = { LIKE: '❤️', COMMENT: '💬', FOLLOW: '👤', SYSTEM: '📢' }
  return map[type] || '🔔'
}

const typeLabel = (type) => {
  const map = { LIKE: '赞了你', COMMENT: '评论了', FOLLOW: '关注了你', SYSTEM: '系统通知' }
  return map[type] || '通知'
}

const loadNotifications = async (page = 1) => {
  loading.value = true
  try {
    const res = await getNotificationList({ page, size: pageSize.value })
    notifications.value = res.data?.records || []
    total.value = res.data?.total || 0
    unreadCount.value = notifications.value.filter(n => n.isRead === 0).length
    currentPage.value = page
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const handleClick = async (n) => {
  if (n.isRead === 0) {
    await markAsRead(n.id)
    n.isRead = 1
    unreadCount.value--
  }
  if (n.targetType === 'POST' && n.targetId) {
    router.push(`/dashboard/community?postId=${n.targetId}`)
  }
}

const handleMarkAllRead = async () => {
  await markAllAsRead()
  notifications.value.forEach(n => { n.isRead = 1 })
  unreadCount.value = 0
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return d.toLocaleDateString('zh-CN')
}

// WebSocket 实时通知
const { lastNotification, unreadCount: wsUnread, connect: connectWs, disconnect: disconnectWs, resetCount } = useNotificationWebSocket()

watch(lastNotification, (n) => {
  if (n) {
    const item = {
      id: Date.now(),
      type: n.type,
      content: n.content,
      fromUsername: '',
      isRead: 0,
      createTime: new Date().toISOString(),
      _local: true
    }
    notifications.value.unshift(item)
    total.value++
    unreadCount.value++
  }
})

onMounted(() => {
  loadNotifications()
  resetCount()
  connectWs()
})

onUnmounted(() => {
  disconnectWs()
})
</script>

<style scoped>
.notification-page { padding: 0; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.page-header h3 { margin: 0; font-size: 18px; color: #111827; }

.notification-list {
  display: flex; flex-direction: column; gap: 8px;
}
.notif-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; background: #fff; border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06); cursor: pointer;
  transition: background 0.15s;
}
.notif-item:hover { background: #f9fafb; }
.notif-item.unread { background: #eff6ff; border-left: 3px solid #5C8AEB; }
.notif-icon { font-size: 22px; flex-shrink: 0; }
.notif-body { flex: 1; min-width: 0; }
.notif-top { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
.notif-from { font-size: 14px; font-weight: 600; color: #111827; }
.notif-type { font-size: 12px; color: #6b7280; }
.notif-time { font-size: 12px; color: #9ca3af; margin-left: auto; }
.notif-content { font-size: 13px; color: #374151; margin: 0; line-height: 1.5; }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: #5C8AEB; flex-shrink: 0; }

.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>
