<template>
  <div class="chat-widget">
    <div class="chat-header">
      <div class="header-left">
        <div class="header-avatar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 14c-2.5 0-4.5 1.5-5 3.5-.2.8.3 1.5 1.1 1.5h7.8c.8 0 1.3-.7 1.1-1.5-.5-2-2.5-3.5-5-3.5z"/>
            <circle cx="8" cy="8" r="2"/>
            <circle cx="16" cy="8" r="2"/>
          </svg>
        </div>
        <div class="header-info">
          <span class="header-title">PetCare 智能客服</span>
          <span class="header-status" v-if="!loading">在线</span>
          <span class="header-status typing" v-else>正在输入...</span>
        </div>
      </div>
      <button class="close-btn" @click="$emit('close')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
        </div>
        <p class="empty-text">你好，有什么可以帮你的？</p>
        <div class="quick-actions">
          <button class="quick-btn" @click="sendQuick('我的宠物最近健康状况怎么样？')">健康查询</button>
          <button class="quick-btn" @click="sendQuick('有什么适合我家宠物的商品推荐？')">商品推荐</button>
          <button class="quick-btn" @click="sendQuick('怎么训练小狗定点上厕所？')">饲养建议</button>
        </div>
      </div>

      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="message-row"
        :class="msg.role === 'USER' ? 'user-row' : 'assistant-row'"
      >
        <div class="message-bubble">
          <div class="message-text">{{ msg.content }}</div>
          <div class="message-meta" v-if="msg.agent">
            <span class="agent-tag">{{ msg.agent }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="message-row assistant-row">
        <div class="message-bubble typing-bubble">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <input
        v-model="inputText"
        class="chat-input"
        placeholder="输入你的问题..."
        @keyup.enter="sendMessage"
        :disabled="loading"
      />
      <button
        class="send-btn"
        :class="{ active: inputText.trim() && !loading }"
        @click="sendMessage"
        :disabled="loading || !inputText.trim()"
      >
        <svg viewBox="0 0 24 24" fill="currentColor" class="send-icon">
          <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { chatAPI } from '@/api/chat.js'

const router = useRouter()
const route = useRoute()

const emit = defineEmits(['close', 'newMessage'])

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const sessionId = ref(localStorage.getItem('chatSessionId') || '')
const messagesContainer = ref(null)

const currentPage = () => route.path

const pageRouteMap = {
  shop: '/dashboard/shop',
  cart: '/dashboard/cart',
  appointment: '/dashboard/services',
  pets: '/dashboard/pets',
  orders: '/dashboard/orders',
  address: '/user/address',
  health: '/dashboard/health'
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const addMessage = (role, content, agent) => {
  messages.value.push({ role, content, agent })
  scrollToBottom()
}

const sendQuick = (text) => {
  inputText.value = text
  sendMessage()
}

const executeAction = (data) => {
  if (data.navigate) {
    const url = data.navigate + (data.navigateParams ? '?' + data.navigateParams : '')
    router.push(url)
    return
  }
  if (data.action) {
    try {
      const actionJson = JSON.parse(data.action)
      const action = actionJson.action
      if (action && action.entity === 'PAGE_NAVIGATE' && action.fields) {
        const page = action.fields.page
        const params = action.fields.params || ''
        const targetUrl = pageRouteMap[page] || ('/dashboard/' + page)
        router.push(targetUrl + (params ? '?' + params : ''))
      }
    } catch (e) {
      // action JSON parse failed, ignore
    }
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  addMessage('USER', text)
  inputText.value = ''
  loading.value = true

  try {
    const res = await chatAPI.sendMessage(text, sessionId.value, currentPage())
    const data = res.data
    if (!sessionId.value && data.sessionId) {
      sessionId.value = data.sessionId
      localStorage.setItem('chatSessionId', data.sessionId)
    }
    const agentLabel = data.agentType || ''
    addMessage('ASSISTANT', data.reply, agentLabel)
    executeAction(data)
    emit('newMessage')
  } catch (e) {
    addMessage('ASSISTANT', '抱歉，服务暂时不可用，请稍后再试。', '')
  } finally {
    loading.value = false
  }
}

watch(() => messages.value.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-widget {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(30, 58, 138, 0.08);
}

.chat-header {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-avatar svg {
  width: 22px;
  height: 22px;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 15px;
  font-weight: 700;
}

.header-status {
  font-size: 12px;
  opacity: 0.85;
}

.header-status.typing {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 0.85; }
  50% { opacity: 0.4; }
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.close-btn svg {
  width: 16px;
  height: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
}

.empty-state {
  text-align: center;
  padding-top: 40px;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 100%);
  color: #1e3a8a;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-text {
  font-size: 15px;
  color: #475569;
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-btn {
  padding: 8px 14px;
  border-radius: 20px;
  border: 1px solid #cbd5e1;
  background: white;
  font-size: 13px;
  color: #1e3a8a;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #1e3a8a;
  color: white;
  border-color: #1e3a8a;
}

.message-row {
  margin-bottom: 14px;
  display: flex;
}

.user-row {
  justify-content: flex-end;
}

.assistant-row {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.user-row .message-bubble {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant-row .message-bubble {
  background: white;
  color: #1e293b;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.message-meta {
  margin-top: 6px;
}

.agent-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(30, 58, 138, 0.08);
  color: #1e3a8a;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 18px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  animation: dotPulse 1.4s infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 60%, 100% { transform: scale(1); opacity: 0.4; }
  30% { transform: scale(1.3); opacity: 1; }
}

.chat-input-area {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 1px solid #e2e8f0;
  background: white;
}

.chat-input {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  padding: 10px 18px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  background: #f8fafc;
  color: #1e293b;
}

.chat-input:focus {
  border-color: #1e3a8a;
  background: white;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #e2e8f0;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn.active {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.3);
}

.send-btn.active:hover {
  transform: scale(1.05);
}

.send-icon {
  width: 18px;
  height: 18px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
</style>
