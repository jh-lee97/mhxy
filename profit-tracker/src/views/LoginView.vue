<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useProfitStore } from '../stores/profitStore.js'

const router = useRouter()
const store = useProfitStore()

// 三种模式：'login' | 'register' | 'reset'
const mode = ref('login')
const username = ref('')
const password = ref('')
const nickname = ref('')
const confirmPassword = ref('')
const phone = ref('')
const verificationCode = ref('')
const loading = ref(false)
const errorMsg = ref('')
const sendingCode = ref(false)
const countdown = ref(0)
const phoneLocked = ref(false)
let countdownTimer = null

function toggleMode() {
  if (mode.value === 'login') mode.value = 'register'
  else if (mode.value === 'register') mode.value = 'login'
  else mode.value = 'login'
  resetForm()
}

function toggleReset() {
  if (mode.value === 'reset') {
    mode.value = 'login'
  } else {
    mode.value = 'reset'
  }
  resetForm()
}

function resetForm() {
  errorMsg.value = ''
  password.value = ''
  nickname.value = ''
  confirmPassword.value = ''
  phone.value = ''
  phoneLocked.value = false
  verificationCode.value = ''
  loading.value = false
  sendingCode.value = false
  countdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function startCountdown() {
  countdown.value = 60
  sendingCode.value = true
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
      sendingCode.value = false
    }
  }, 1000)
}

async function handleSendCode() {
  if (!phone.value.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }
  sendingCode.value = true
  errorMsg.value = ''
  try {
    await store.sendVerificationCode(username.value, phone.value)
    phoneLocked.value = true
    startCountdown()
    errorMsg.value = '验证码已发送至控制台，请查看'
  } catch (err) {
    errorMsg.value = err.message || '发送验证码失败'
  } finally {
    sendingCode.value = false
  }
}

async function handleRegisterSendCode() {
  if (!phone.value.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }
  sendingCode.value = true
  errorMsg.value = ''
  try {
    await store.sendRegisterCode(phone.value)
    phoneLocked.value = true
    startCountdown()
    errorMsg.value = '验证码已发送至控制台，请查看'
  } catch (err) {
    errorMsg.value = err.message || '发送验证码失败'
  } finally {
    sendingCode.value = false
  }
}

async function handleSubmit() {
  if (mode.value !== 'reset') {
    if (!username.value.trim() || !password.value.trim()) {
      errorMsg.value = '请填写完整信息'
      return
    }
  }

  loading.value = true
  errorMsg.value = ''

  try {
    if (mode.value === 'login') {
      await store.login(username.value, password.value)
    } else if (mode.value === 'register') {
      if (!nickname.value.trim()) {
        errorMsg.value = '请输入昵称'
        loading.value = false
        return
      }
      if (!phoneLocked.value || !verificationCode.value.trim()) {
        errorMsg.value = '请先获取并填写验证码'
        loading.value = false
        return
      }
      await store.register(username.value, password.value, nickname.value, phone.value, verificationCode.value)
    } else {
      // reset 模式 - 第一步：发送验证码
      if (!phone.value.trim() || !verificationCode.value.trim()) {
        errorMsg.value = '请输入手机号和验证码'
        loading.value = false
        return
      }
      if (password.value.length < 6) {
        errorMsg.value = '新密码至少6位'
        loading.value = false
        return
      }
      if (password.value !== confirmPassword.value) {
        errorMsg.value = '两次输入的密码不一致'
        loading.value = false
        return
      }
      await store.resetPasswordWithCode(phone.value, verificationCode.value, password.value)
      errorMsg.value = '密码重置成功，即将跳转登录...'
      setTimeout(() => {
        mode.value = 'login'
        resetForm()
      }, 2000)
      return
    }
    // 登录/注册成功，跳转首页
    router.push('/')
  } catch (err) {
    if (mode.value === 'login') {
      errorMsg.value = err.message || '用户名或密码错误'
    } else if (mode.value === 'register') {
      errorMsg.value = err.message || '注册失败'
    }
  } finally {
    if (mode.value !== 'reset') loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>💰 梦幻西游 · 五开收益记录</h2>
        <p class="login-subtitle">
          {{ mode === 'login' ? '欢迎回来' : mode === 'register' ? '创建新账号' : '忘记密码？' }}
        </p>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <!-- 登录表单 -->
        <template v-if="mode === 'login'">
          <div class="form-item">
            <label>用户名</label>
            <input v-model="username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-item">
            <label>密码</label>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </div>
        </template>

        <!-- 注册表单 -->
        <template v-if="mode === 'register'">
          <div class="form-item">
            <label>手机号</label>
            <input v-model="phone" type="text" placeholder="请输入手机号" :readonly="phoneLocked" required />
          </div>
          <div class="form-row" v-if="!phoneLocked">
            <div class="form-item code-field">
              <label>验证码</label>
              <input v-model="verificationCode" type="text" placeholder="请输入6位验证码" required />
            </div>
            <div class="form-item code-btn-wrap">
              <label>&nbsp;</label>
              <button class="code-btn" :disabled="sendingCode || countdown > 0" @click.stop="handleRegisterSendCode">
                {{ countdown > 0 ? countdown + 's' : (sendingCode ? '发送中...' : '获取验证码') }}
              </button>
            </div>
          </div>
          <div class="form-item" v-if="phoneLocked">
            <label>验证码</label>
            <input v-model="verificationCode" type="text" placeholder="请输入验证码" required />
          </div>
          <div class="form-item">
            <label>用户名</label>
            <input v-model="username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-item">
            <label>密码</label>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </div>
          <div class="form-item">
            <label>昵称</label>
            <input v-model="nickname" type="text" placeholder="请输入昵称" />
          </div>
        </template>

        <!-- 重置密码表单 -->
        <template v-if="mode === 'reset'">
          <div class="form-item">
            <label>手机号</label>
            <input :value="phone" type="text" placeholder="请输入注册时的手机号" :readonly="phoneLocked" required />
          </div>
          <div class="form-row">
            <div class="form-item code-field">
              <label>验证码</label>
              <input v-model="verificationCode" type="text" placeholder="请输入6位验证码" required />
            </div>
            <div class="form-item code-btn-wrap">
              <label>&nbsp;</label>
              <button class="code-btn" :disabled="sendingCode || countdown > 0" @click.stop="handleSendCode">
                {{ countdown > 0 ? countdown + 's' : (sendingCode ? '发送中...' : '获取验证码') }}
              </button>
            </div>
          </div>
          <div class="form-item">
            <label>新密码</label>
            <input v-model="password" type="password" placeholder="请输入新密码（至少6位）" required />
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" required />
          </div>
        </template>

        <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? '处理中...' : (mode === 'login' ? '登录' : mode === 'register' ? '注册' : '重置密码') }}
        </button>

        <!-- 操作链接 -->
        <template v-if="mode === 'login'">
          <div class="toggle-link" @click="toggleMode">没有账号？点击注册</div>
          <div class="toggle-link" @click="toggleReset">忘记密码？</div>
        </template>
        <template v-else-if="mode === 'register'">
          <div class="toggle-link" @click="toggleMode">已有账号？点击登录</div>
        </template>
        <template v-else>
          <div class="toggle-link" @click="toggleReset">回到登录</div>
        </template>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #303133;
}

.login-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.form-row .code-field {
  flex: 1;
}

.form-row .code-btn-wrap {
  flex-shrink: 0;
}

.code-btn {
  padding: 10px 16px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  height: 38px;
}

.code-btn:hover:not(:disabled) {
  background: #66b1ff;
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #c0c4cc;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 13px;
  color: #606266;
}

.form-item input {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-item input:focus {
  border-color: #409eff;
}

.error-msg {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  padding: 8px;
  background: #fef0f0;
  border-radius: 6px;
}

.submit-btn {
  padding: 12px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #66b1ff;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-link {
  text-align: center;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
  margin-top: 8px;
}

.toggle-link:hover {
  text-decoration: underline;
}
</style>
