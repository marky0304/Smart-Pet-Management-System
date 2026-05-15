<template>
  <div class="register-page">
    <!-- 左侧装饰区 -->
    <div class="register-visual">
      <div class="visual-inner">
        <!-- 品牌标识 -->
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
        <h1 class="visual-title">欢迎加入<br>PetCare 大家庭</h1>
        <p class="visual-desc">开启您的智慧养宠之旅<br>一站式管理宠物健康、服务、购物</p>
        <!-- 装饰卡片预览 -->
        <div class="preview-cards">
          <div class="preview-card">
            <div class="preview-icon">🐕</div>
            <div>宠物档案</div>
          </div>
          <div class="preview-card">
            <div class="preview-icon">❤️</div>
            <div>健康追踪</div>
          </div>
          <div class="preview-card">
            <div class="preview-icon">📅</div>
            <div>服务预约</div>
          </div>
        </div>
        <div class="visual-badge">
          <span>已服务</span>
          <strong>10,000+</strong>
          <span>位宠物主人</span>
        </div>
      </div>
      <!-- 背景装饰圆 -->
      <div class="deco-circle deco-1"></div>
      <div class="deco-circle deco-2"></div>
      <div class="deco-circle deco-3"></div>
    </div>

    <!-- 右侧表单区 -->
    <div class="register-main">
      <div class="register-card">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>加入智慧宠物管理系统</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              :prefix-icon="UserFilled"
            />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              :prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="gender">
            <el-radio-group v-model="registerForm.gender" class="gender-group">
              <el-radio-button :label="0">保密</el-radio-button>
              <el-radio-button :label="1">男</el-radio-button>
              <el-radio-button :label="2">女</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（6-20位）"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="agree">
            <el-checkbox v-model="registerForm.agree">
              <span class="agree-text">
                我已阅读并同意
                <el-link type="primary" @click="showAgreement">《用户协议》</el-link>
                和
                <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
              </span>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              class="btn-register"
              @click="handleRegister"
              :loading="loading"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="form-footer">
              已有账号？<el-link type="primary" @click="goToLogin">立即登录</el-link>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="agreementVisible" title="用户协议" width="560px" class="policy-dialog">
      <div class="policy-content">
        <h3>智慧宠物管理系统用户协议</h3>
        <p>欢迎使用智慧宠物管理系统！</p>
        <h4>1. 服务条款</h4>
        <p>本系统为用户提供宠物管理、健康记录、预约服务等功能。</p>
        <h4>2. 用户责任</h4>
        <p>用户应确保提供信息的真实性和准确性。</p>
        <h4>3. 隐私保护</h4>
        <p>我们承诺保护用户隐私，不会泄露用户个人信息。</p>
        <h4>4. 服务变更</h4>
        <p>我们保留随时修改或终止服务的权利。</p>
      </div>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="privacyVisible" title="隐私政策" width="560px" class="policy-dialog">
      <div class="policy-content">
        <h3>隐私政策</h3>
        <p>我们重视您的隐私保护。</p>
        <h4>1. 信息收集</h4>
        <p>我们只收集必要的用户信息用于提供服务。</p>
        <h4>2. 信息使用</h4>
        <p>收集的信息仅用于改善服务质量。</p>
        <h4>3. 信息保护</h4>
        <p>我们采用安全措施保护用户信息。</p>
        <h4>4. 信息共享</h4>
        <p>未经用户同意，我们不会与第三方共享用户信息。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Phone, Message, Lock } from '@element-plus/icons-vue'
import { registerUser } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const agreementVisible = ref(false)
const privacyVisible = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  password: '',
  confirmPassword: '',
  agree: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerForm.agree) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const { confirmPassword, agree, ...registerData } = registerForm
    const response = await registerUser(registerData)

    if (response.code === 200) {
      ElMessage.success('注册成功！请登录')
      router.push('/login')
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

const showAgreement = () => {
  agreementVisible.value = true
}

const showPrivacy = () => {
  privacyVisible.value = true
}
</script>

<style scoped>
/* ===== 页面布局 ===== */
.register-page {
  min-height: 100vh;
  display: flex;
  background: #FBF9F7;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
}

/* ===== 左侧装饰区 ===== */
.register-visual {
  flex: 0 0 44%;
  background: linear-gradient(160deg, #EEF3FB 0%, #F2F6FC 40%, #FBF5F3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.visual-inner {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 48px 40px;
  max-width: 420px;
}
.visual-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 36px;
}
.visual-paw {
  width: 44px;
  height: 44px;
  color: #5C8AEB;
}
.visual-name {
  font-size: 26px;
  font-weight: 700;
  color: #5C8AEB;
  letter-spacing: -0.5px;
}
.visual-title {
  font-size: 32px;
  font-weight: 700;
  color: #1A1A1A;
  line-height: 1.35;
  margin: 0 0 16px;
  letter-spacing: -0.5px;
}
.visual-desc {
  font-size: 14px;
  color: #8C8C8C;
  line-height: 1.8;
  margin: 0 0 36px;
}

/* 功能预览卡片 */
.preview-cards {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 36px;
}
.preview-card {
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  padding: 14px 16px;
  text-align: center;
  font-size: 13px;
  color: #555;
  font-weight: 500;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  flex: 1;
  max-width: 110px;
}
.preview-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

/* 统计徽章 */
.visual-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(92,138,235,0.08);
  border: 1px solid rgba(92,138,235,0.12);
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 13px;
  color: #6b7280;
}
.visual-badge strong {
  color: #5C8AEB;
  font-size: 15px;
}

/* 背景装饰圆 */
.deco-circle {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.deco-1 {
  width: 320px; height: 320px;
  background: rgba(92,138,235,0.04);
  top: -120px; left: -80px;
}
.deco-2 {
  width: 200px; height: 200px;
  background: rgba(232,129,122,0.05);
  bottom: -60px; right: 40px;
}
.deco-3 {
  width: 100px; height: 100px;
  background: rgba(77,168,138,0.06);
  top: 50%; right: -30px;
  transform: translateY(-50%);
}

/* ===== 右侧表单区 ===== */
.register-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
}
.register-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 24px;
  padding: 44px 40px;
  box-shadow: 0 2px 24px rgba(0,0,0,0.05), 0 0 0 1px rgba(0,0,0,0.03);
}
.form-header {
  text-align: center;
  margin-bottom: 32px;
}
.form-header h2 {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #1A1A1A;
  letter-spacing: -0.3px;
}
.form-header p {
  margin: 0;
  font-size: 14px;
  color: #8C8C8C;
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}

/* 圆角输入框 */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.25s;
  background: #fafafa;
  padding: 2px 12px;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #5C8AEB inset;
  background: #fff;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(92,138,235,0.25) inset;
  background: #fff;
}
:deep(.el-input__inner) {
  color: #1A1A1A;
}
:deep(.el-input__inner::placeholder) {
  color: #B0B0B0;
}

/* 性别单选按钮组 */
.gender-group {
  width: 100%;
}
:deep(.gender-group .el-radio-button__inner) {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #fafafa;
  color: #555;
  padding: 8px 20px;
  font-weight: 500;
  transition: all 0.2s;
}
:deep(.gender-group .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #5C8AEB;
  border-color: #5C8AEB;
  color: #fff;
  box-shadow: 0 2px 8px rgba(92,138,235,0.3);
}

/* 同意协议文字 */
.agree-text {
  font-size: 13px;
  color: #6b7280;
}

/* 注册按钮 */
.btn-register {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: #5C8AEB;
  border: none;
  color: #fff;
  letter-spacing: 2px;
  transition: all 0.3s;
}
.btn-register:hover {
  background: #4A78D6;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(92,138,235,0.35);
}
.btn-register:active {
  transform: translateY(0);
}

/* 页脚链接 */
.form-footer {
  text-align: center;
  width: 100%;
  font-size: 14px;
  color: #8C8C8C;
}

/* Element Plus 链接色覆盖 */
:deep(.el-link--primary) {
  color: #5C8AEB;
  font-weight: 500;
}
:deep(.el-link--primary:hover) {
  color: #4A78D6;
}

/* 对话框 */
.policy-content {
  line-height: 1.8;
  color: #555;
}
.policy-content h3 {
  color: #5C8AEB;
  margin-bottom: 20px;
  font-size: 18px;
}
.policy-content h4 {
  color: #1A1A1A;
  margin: 16px 0 8px;
  font-size: 14px;
}
.policy-content p {
  color: #6b7280;
  margin-bottom: 8px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .register-visual {
    display: none;
  }
  .register-main {
    padding: 24px 16px;
  }
  .register-card {
    padding: 32px 24px;
    border-radius: 20px;
  }
}
</style>
