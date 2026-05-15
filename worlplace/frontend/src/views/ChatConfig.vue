<template>
  <div class="management-page">
    <el-card class="header-card">
      <h2>客服设置</h2>
      <p>管理智能客服的模型参数、API密钥、聊天行为和系统提示词</p>
    </el-card>

    <el-card class="config-card">
      <el-tabs v-model="activeTab">
        <!-- 服务商 & API Key -->
        <el-tab-pane label="服务商" name="provider">
          <el-form :model="config" label-width="100px" class="config-form">
            <el-form-item label="LLM 服务商">
              <el-select v-model="provider" placeholder="选择服务商" @change="onProviderChange">
                <el-option label="DeepSeek Anthropic" value="deepseekAnthropic" />
                <el-option label="DeepSeek" value="deepseek" />
                <el-option label="Anthropic (Claude)" value="anthropic" />
                <el-option label="OpenAI" value="openai" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item label="API 地址">
              <el-input v-model="config['llm.api_url']" placeholder="https://api.deepseek.com/anthropic" />
              <div class="form-tip">Anthropic 兼容格式的 Messages 端点地址</div>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input
                v-model="config['llm.api_key']"
                type="password"
                show-password
                placeholder="请输入 API Key"
              />
              <div class="form-tip">密钥加密存储于数据库，不会在日志中暴露</div>
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="config['llm.model']" placeholder="DeepSeek-V4-pro" />
              <div class="form-tip">常见模型：DeepSeek-V4-pro、DeepSeek-V4-flash、deepseek-chat、gpt-4o</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="模型参数" name="model">
          <el-form :model="config" label-width="120px" class="config-form">
            <el-form-item label="Temperature">
              <el-row :gutter="16" style="width: 100%">
                <el-col :span="18">
                  <el-slider v-model="temperature" :min="0" :max="2" :step="0.1" show-input />
                </el-col>
              </el-row>
              <div class="form-tip">越低越稳定精确，越高越有创造性（建议 0.5-0.7）</div>
            </el-form-item>
            <el-form-item label="Max Tokens">
              <el-row :gutter="16" style="width: 100%">
                <el-col :span="18">
                  <el-slider v-model="maxTokens" :min="100" :max="8000" :step="100" show-input />
                </el-col>
              </el-row>
              <div class="form-tip">单次回复的最大 Token 数量</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="聊天行为" name="behavior">
          <el-form :model="config" label-width="100px" class="config-form">
            <el-form-item label="启用智能客服">
              <el-switch
                v-model="chatEnabled"
                active-text="开启"
                inactive-text="关闭"
              />
              <div class="form-tip" style="margin-top:4px">
                关闭后所有聊天请求将提示"智能客服功能当前已关闭"
              </div>
            </el-form-item>
            <el-form-item label="欢迎语">
              <el-input v-model="config['chat.welcome_message']" placeholder="你好，有什么可以帮你的？" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Prompt 定制" name="prompt">
          <el-form :model="config" label-width="100px" class="config-form">
            <el-form-item label="系统提示词">
              <el-input
                v-model="config['chat.system_prompt']"
                type="textarea"
                :rows="12"
                placeholder="留空则使用默认的多Agent提示词系统。填写后将覆盖所有Agent的默认Prompt。"
              />
              <div class="form-tip">
                留空使用默认的多Agent智能提示词。填写后所有对话将使用此自定义提示词。
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="save-bar">
        <el-button type="primary" size="large" @click="handleSave" :loading="saving">
          保存设置
        </el-button>
        <el-button size="large" @click="handleTest" :loading="testing">
          测试连接
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { chatConfigAPI } from '@/api/chat-config'
import axios from 'axios'

const activeTab = ref('provider')
const saving = ref(false)
const testing = ref(false)
const config = reactive({})
const provider = ref('deepseekAnthropic')

const temperature = ref(0.7)
const maxTokens = ref(1000)
const chatEnabled = ref(true)

const providerPresets = {
  deepseekAnthropic: {
    model: 'DeepSeek-V4-pro'
  },
  deepseek: {
    model: 'deepseek-chat'
  },
  anthropic: {
    model: 'claude-sonnet-4-6'
  },
  openai: {
    model: 'gpt-4o'
  },
  custom: {
    model: ''
  }
}

const onProviderChange = (val) => {
  const preset = providerPresets[val]
  if (preset && val !== 'custom') {
    config['llm.model'] = preset.model
  }
}

watch(temperature, (val) => {
  config['llm.temperature'] = String(val)
})

watch(maxTokens, (val) => {
  config['llm.max_tokens'] = String(val)
})

watch(chatEnabled, (val) => {
  config['chat.enabled'] = String(val)
})

const detectProvider = (url, model) => {
  if (url && url.includes('deepseek.com/anthropic')) return 'deepseekAnthropic'
  if (url && url.includes('deepseek')) return 'deepseek'
  if (url && url.includes('anthropic')) return 'anthropic'
  if (url && url.includes('openai')) return 'openai'
  if (!url || !model) return 'custom'
  return 'custom'
}

const normalizeAnthropicApiUrl = (url) => {
  if (!url) return ''
  const trimmedUrl = url.trim().replace(/\/$/, '')
  if (trimmedUrl.endsWith('/anthropic')) return `${trimmedUrl}/v1/messages`
  return trimmedUrl
}

const isAnthropicMessagesApi = (url) => normalizeAnthropicApiUrl(url).includes('/v1/messages')

const loadConfig = async () => {
  try {
    const res = await chatConfigAPI.getAll()
    const data = res.data
    Object.assign(config, data)
    temperature.value = parseFloat(data['llm.temperature'] || '0.7')
    maxTokens.value = parseInt(data['llm.max_tokens'] || '1000')
    chatEnabled.value = data['chat.enabled'] !== 'false'
    provider.value = detectProvider(data['llm.api_url'], data['llm.model'])
  } catch (e) {
    ElMessage.error('加载配置失败')
  }
}

const extractAnthropicText = (data) => {
  const blocks = data?.content
  if (!Array.isArray(blocks)) return ''
  return blocks
    .filter(block => block?.type === 'text' && block?.text)
    .map(block => block.text)
    .join('\n')
}

const handleSave = async () => {
  saving.value = true
  try {
    const normalizedConfig = {
      ...config,
      'llm.api_url': provider.value === 'deepseekAnthropic'
        ? 'https://api.deepseek.com/anthropic'
        : config['llm.api_url']
    }
    await chatConfigAPI.batchSave(normalizedConfig)
    Object.assign(config, normalizedConfig)
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  testing.value = true
  try {
    const apiUrl = config['llm.api_url']
    const apiKey = config['llm.api_key']
    const model = config['llm.model']
    const requestUrl = normalizeAnthropicApiUrl(apiUrl)
    const useAnthropicFormat = isAnthropicMessagesApi(apiUrl)

    if (!apiUrl || !apiKey) {
      ElMessage.warning('请先填写 API 地址和 API Key')
      testing.value = false
      return
    }

    const payload = useAnthropicFormat
      ? {
          model: model || 'DeepSeek-V4-pro',
          system: '你是智能客服，请简短回复。',
          messages: [{ role: 'user', content: '你好，请回复"连接成功"两个字' }],
          max_tokens: 50,
          temperature: 0.7
        }
      : {
          model: model || 'deepseek-chat',
          messages: [{ role: 'user', content: '你好，请回复"连接成功"两个字' }],
          max_tokens: 50
        }

    const res = await axios.post(requestUrl, payload, {
      headers: useAnthropicFormat
        ? {
            'x-api-key': apiKey,
            'anthropic-version': '2023-06-01',
            'Content-Type': 'application/json'
          }
        : {
            'Authorization': `Bearer ${apiKey}`,
            'Content-Type': 'application/json'
          },
      timeout: 15000
    })

    const content = useAnthropicFormat
      ? extractAnthropicText(res.data)
      : res.data?.choices?.[0]?.message?.content
    if (content) {
      ElMessage.success(`连接成功！模型回复: ${content}`)
    } else if (res.data?.error?.message) {
      ElMessage.error(`连接失败: ${res.data.error.message}`)
    } else {
      ElMessage.warning('API 返回格式异常，请检查 API 地址是否正确')
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 401 || status === 403) {
      ElMessage.error('认证失败，请检查 API Key 是否正确')
    } else if (status === 404) {
      ElMessage.error('API 地址不存在 (404)，请检查地址是否正确')
    } else {
      ElMessage.error(`连接失败: ${e.message}`)
    }
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.management-page {
  padding: 0;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #5C8AEB 0%, #7BAAF2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-card h2 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.header-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.config-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.config-form {
  max-width: 680px;
  padding-top: 12px;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  line-height: 1.6;
}

.save-bar {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
}
</style>
