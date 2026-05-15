<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="form-header">
        <h2>忘记密码</h2>
        <p>验证身份后即可重置密码</p>
      </div>

      <el-form ref="formRef" :model="resetForm" :rules="rules" size="large">
        <el-form-item prop="account">
          <el-input v-model="resetForm.account" placeholder="手机号或邮箱" :prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="resetForm.code" placeholder="验证码" :prefix-icon="Message" class="code-input" />
            <el-button class="code-btn" @click="sendCode" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="新密码（6-20位）" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="确认新密码" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item>
          <el-button class="btn-reset" @click="handleReset" :loading="loading">
            {{ loading ? '重置中...' : '重置密码' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="form-footer">
            <el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { resetPassword, sendVerifyCode } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Message, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)

const resetForm = reactive({
  account: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const sendCode = async () => {
  if (!resetForm.account) { ElMessage.warning('请先输入手机号或邮箱'); return }
  try {
    await sendVerifyCode(resetForm.account)
    ElMessage.success('验证码已发送，请查看')
    countdown.value = 60
    const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e) {}
}

const handleReset = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await resetPassword(resetForm)
    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: #FBF9F7;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}
.forgot-card {
  width: 100%; max-width: 420px;
  background: #fff;
  border-radius: 24px;
  padding: 44px 40px;
  box-shadow: 0 2px 24px rgba(0,0,0,0.05), 0 0 0 1px rgba(0,0,0,0.03);
}
.form-header { text-align: center; margin-bottom: 32px; }
.form-header h2 { margin: 0 0 8px; font-size: 26px; font-weight: 700; color: #1A1A1A; letter-spacing: -0.3px; }
.form-header p { margin: 0; font-size: 14px; color: #8C8C8C; }

.code-row { display: flex; gap: 10px; }
.code-input { flex: 1; }
.code-btn {
  height: 42px; border-radius: 12px;
  font-size: 13px; font-weight: 500;
  background: rgba(92,138,235,0.06);
  border: 1px solid rgba(92,138,235,0.12);
  color: #5C8AEB; white-space: nowrap;
  padding: 0 16px; transition: all 0.25s;
}
.code-btn:hover:not(:disabled) { background: rgba(92,138,235,0.12); border-color: #5C8AEB; }
.code-btn:disabled { color: #B0B0B0; background: #f5f5f5; }

:deep(.el-form-item) { margin-bottom: 18px; }
:deep(.el-input__wrapper) { border-radius: 12px; box-shadow: 0 0 0 1px #e5e7eb inset; background: #fafafa; transition: all 0.25s; }
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #5C8AEB inset; background: #fff; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(92,138,235,0.25) inset; background: #fff; }
:deep(.el-input__inner) { color: #1A1A1A; }
:deep(.el-input__inner::placeholder) { color: #B0B0B0; }

.btn-reset {
  width: 100%; height: 48px;
  font-size: 16px; font-weight: 600;
  border-radius: 14px;
  background: #5C8AEB; border: none;
  color: #fff; letter-spacing: 2px;
  transition: all 0.3s;
}
.btn-reset:hover { background: #4A78D6; transform: translateY(-1px); box-shadow: 0 6px 20px rgba(92,138,235,0.35); }

.form-footer { text-align: center; width: 100%; font-size: 14px; }
:deep(.el-link--primary) { color: #5C8AEB; font-weight: 500; }
:deep(.el-link--primary:hover) { color: #4A78D6; }
</style>
