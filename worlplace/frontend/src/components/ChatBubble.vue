<template>
  <div class="chat-bubble-wrapper">
    <div
      class="chat-bubble"
      :class="{ 'has-unread': unreadCount > 0 }"
      @click="toggleWidget"
    >
      <div class="bubble-inner">
        <svg
          v-if="!widgetOpen"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          class="bubble-icon"
        >
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
        <svg
          v-else
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          class="bubble-icon"
        >
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
      <span class="bubble-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      <div class="bubble-ripple" v-if="unreadCount > 0"></div>
    </div>

    <Transition name="widget-slide">
      <ChatWidget
        v-if="widgetOpen"
        @close="widgetOpen = false"
        @new-message="onNewMessage"
      />
    </Transition>
  </div>
</template>

<script setup>
import { ref, defineAsyncComponent } from 'vue'

const ChatWidget = defineAsyncComponent(() => import('./ChatWidget.vue'))

const widgetOpen = ref(false)
const unreadCount = ref(0)

const toggleWidget = () => {
  widgetOpen.value = !widgetOpen.value
  if (widgetOpen.value) {
    unreadCount.value = 0
  }
}

const onNewMessage = () => {
  if (!widgetOpen.value) {
    unreadCount.value++
  }
}
</script>

<style scoped>
.chat-bubble-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 2000;
}

.chat-bubble {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.35);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  bottom: 0;
  right: 0;
}

.chat-bubble:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 28px rgba(30, 58, 138, 0.45);
}

.chat-bubble.has-unread {
  animation: gentleBounce 2s ease-in-out infinite;
}

@keyframes gentleBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.bubble-inner {
  width: 24px;
  height: 24px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bubble-icon {
  width: 24px;
  height: 24px;
}

.bubble-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: white;
  font-size: 12px;
  font-weight: 700;
  min-width: 22px;
  height: 22px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.bubble-ripple {
  position: absolute;
  inset: -6px;
  border-radius: 34px;
  border: 2px solid rgba(30, 58, 138, 0.3);
  animation: ripple 2s ease-out infinite;
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.widget-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.widget-slide-leave-active {
  transition: all 0.25s ease-in;
}

.widget-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.widget-slide-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>
