<template>
  <div class="login-page">
    <div class="login-visual">
      <div class="visual-inner">
        <div class="visual-brand">
          <svg viewBox="0 0 64 64" class="visual-paw">
            <path d="M32 48c-4 0-8-2-10-5-2-3-2-7 0-10 2-3 6-5 10-5s8 2 10 5c2 3 2 7 0 10-2 3-6 5-10 5z" fill="currentColor"/>
            <circle cx="20" cy="24" r="6" fill="currentColor"/>
            <circle cx="44" cy="24" r="6" fill="currentColor"/>
            <circle cx="14" cy="38" r="5" fill="currentColor"/>
            <circle cx="50" cy="38" r="5" fill="currentColor"/>
          </svg>
          <span class="visual-name">PetCare</span>
        </div>
        <h1 class="visual-title">智慧宠物<br>管理系统</h1>
        <p class="visual-desc">一站式宠物健康管理、服务预约、<br>商城购物、社区互动平台</p>
        <div class="visual-features">
          <div class="v-feature" v-for="f in features" :key="f.title">
            <span class="v-feat-icon">{{ f.icon }}</span>
            <div>
              <div class="v-feat-title">{{ f.title }}</div>
              <div class="v-feat-desc">{{ f.desc }}</div>
            </div>
          </div>
        </div>
        <div class="visual-badge">
          <span>已服务</span><strong>10,000+</strong><span>位宠物主人</span>
        </div>
      </div>
      <div class="deco-circle deco-1"></div>
      <div class="deco-circle deco-2"></div>
      <div class="deco-circle deco-3"></div>
    </div>

    <div class="login-main">
      <div class="login-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户以继续</p>
        </div>

        <el-form ref="formRef" :model="loginForm" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名 / 手机号 / 邮箱" :prefix-icon="User" @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="$router.push('/forgot-password')">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button class="btn-login" @click="handleLogin" :loading="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="form-footer">
              还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
            </div>
          </el-form-item>
        </el-form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({ username: '', password: '' })

const features = [
  { icon: '🏥', title: '健康管理', desc: '记录宠物健康状况，疫苗接种提醒' },
  { icon: '📅', title: '服务预约', desc: '在线预约洗澡、美容、寄养等服务' },
  { icon: '🛒', title: '商城购物', desc: '优质宠物用品，一站式购物体验' },
  { icon: '👥', title: '社区交流', desc: '分享养宠经验，结识宠友' }
]

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功！欢迎回来')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #FBF9F7;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}

.login-visual {
  flex: 0 0 44%;
  background: linear-gradient(160deg, #EEF3FB 0%, #F2F6FC 40%, #FBF5F3 100%);
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.visual-inner { position: relative; z-index: 1; padding: 48px 40px; max-width: 460px; }
.visual-brand { display: flex; align-items: center; gap: 10px; margin-bottom: 32px; }
.visual-paw { width: 44px; height: 44px; color: #5C8AEB; }
.visual-name { font-size: 26px; font-weight: 700; color: #5C8AEB; letter-spacing: -0.5px; }
.visual-title { font-size: 34px; font-weight: 700; color: #1A1A1A; line-height: 1.3; margin: 0 0 14px; letter-spacing: -0.5px; }
.visual-desc { font-size: 14px; color: #8C8C8C; line-height: 1.8; margin: 0 0 36px; }
.visual-features { display: flex; flex-direction: column; gap: 14px; margin-bottom: 36px; }
.v-feature { display: flex; align-items: center; gap: 14px; background: rgba(255,255,255,0.65); backdrop-filter: blur(6px); border-radius: 14px; padding: 14px 18px; }
.v-feat-icon { font-size: 28px; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.8); border-radius: 10px; flex-shrink: 0; }
.v-feat-title { font-size: 14px; font-weight: 600; color: #1A1A1A; }
.v-feat-desc { font-size: 12px; color: #8C8C8C; margin-top: 2px; }
.visual-badge { display: inline-flex; align-items: center; gap: 6px; background: rgba(92,138,235,0.08); border: 1px solid rgba(92,138,235,0.12); border-radius: 20px; padding: 8px 20px; font-size: 13px; color: #6b7280; }
.visual-badge strong { color: #5C8AEB; font-size: 15px; }
.deco-circle { position: absolute; border-radius: 50%; pointer-events: none; }
.deco-1 { width: 320px; height: 320px; background: rgba(92,138,235,0.04); top: -120px; left: -80px; }
.deco-2 { width: 200px; height: 200px; background: rgba(232,129,122,0.05); bottom: -60px; right: 40px; }
.deco-3 { width: 100px; height: 100px; background: rgba(77,168,138,0.06); top: 50%; right: -30px; transform: translateY(-50%); }

.login-main { flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px 48px; }
.login-card {
  width: 100%; max-width: 420px;
  background: #fff; border-radius: 24px;
  padding: 44px 40px;
  box-shadow: 0 2px 24px rgba(0,0,0,0.05), 0 0 0 1px rgba(0,0,0,0.03);
}
.form-header { text-align: center; margin-bottom: 32px; }
.form-header h2 { margin: 0 0 8px; font-size: 26px; font-weight: 700; color: #1A1A1A; letter-spacing: -0.3px; }
.form-header p { margin: 0; font-size: 14px; color: #8C8C8C; }
.form-options { display: flex; justify-content: space-between; align-items: center; margin: -6px 0 8px; font-size: 13px; }

:deep(.el-form-item) { margin-bottom: 18px; }
:deep(.el-input__wrapper) { border-radius: 12px; box-shadow: 0 0 0 1px #e5e7eb inset; background: #fafafa; transition: all 0.25s; }
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #5C8AEB inset; background: #fff; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(92,138,235,0.25) inset; background: #fff; }
:deep(.el-input__inner) { color: #1A1A1A; }
:deep(.el-input__inner::placeholder) { color: #B0B0B0; }

.btn-login {
  width: 100%; height: 48px;
  font-size: 16px; font-weight: 600;
  border-radius: 14px;
  background: #5C8AEB; border: none;
  color: #fff; letter-spacing: 2px;
  transition: all 0.3s;
}
.btn-login:hover { background: #4A78D6; transform: translateY(-1px); box-shadow: 0 6px 20px rgba(92,138,235,0.35); }
.form-footer { text-align: center; width: 100%; font-size: 14px; color: #8C8C8C; }

:deep(.el-link--primary) { color: #5C8AEB; font-weight: 500; }
:deep(.el-link--primary:hover) { color: #4A78D6; }


@media (max-width: 768px) {
  .login-visual { display: none; }
  .login-main { padding: 24px 16px; }
  .login-card { padding: 32px 24px; border-radius: 20px; }
}
</style>
