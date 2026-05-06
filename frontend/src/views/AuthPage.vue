<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const activeTab = ref('login')

const loginForm = ref({
  email: '',
  password: '',
})

const registerForm = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
})

const localError = ref('')

const isLoading = computed(() => auth.loading)
const errorMessage = computed(() => localError.value || auth.error)

function switchTab(tab) {
  activeTab.value = tab
  localError.value = ''
}

async function submitLogin() {
  localError.value = ''
  if (!loginForm.value.email || !loginForm.value.password) {
    localError.value = 'Please fill in all fields'
    return
  }

  try {
    await auth.signIn(loginForm.value.email.trim(), loginForm.value.password)
    await router.push({ name: 'dashboard' })
  } catch {
    // error is already normalized in store
  }
}

async function submitRegister() {
  localError.value = ''

  const payload = {
    firstName: registerForm.value.firstName.trim(),
    lastName: registerForm.value.lastName.trim(),
    email: registerForm.value.email.trim(),
    password: registerForm.value.password,
  }

  if (!payload.firstName || !payload.email || !payload.password) {
    localError.value = 'Please fill in all fields'
    return
  }
  if (payload.password.length < 6) {
    localError.value = 'Password must be at least 6 characters'
    return
  }

  try {
    await auth.signUp(payload)
    await router.push({ name: 'dashboard' })
  } catch {
    // error is already normalized in store
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-left">
        <div class="auth-brand">
          <div class="auth-brand-logo">
            <svg width="24" height="24" fill="none" stroke="rgba(255,255,255,.9)" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><use href="#ic-flask"/></svg>
          </div>
          <div class="auth-brand-name">AllerScan</div>
        </div>
        <div style="margin-top: 40px">
          <div class="auth-hero-title">Stay safe,<br />eat <em>smart</em></div>
          <div class="auth-hero-sub">
            Detect food allergens instantly. Personalized alerts for your allergies.
          </div>
          <div class="auth-features">
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-shield"/></svg></div>
              EU 14 major allergens covered
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-shield"/></svg></div>
              FDA 9 major allergens covered
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-scan"/></svg></div>
              Instant ingredient analysis
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-profile"/></svg></div>
              Personal allergen profile
            </div>
          </div>
        </div>
        <div style="color: rgba(255, 255, 255, 0.3); font-size: 11px">
          © 2026 AllerScan · Diploma Project · Ala-Too University
        </div>
      </div>

      <div class="auth-right">
        <div class="auth-tabs">
          <button class="auth-tab" :class="{ active: activeTab === 'login' }" @click="switchTab('login')">
            Sign In
          </button>
          <button class="auth-tab" :class="{ active: activeTab === 'register' }" @click="switchTab('register')">
            Create Account
          </button>
        </div>

        <div class="auth-error" :class="{ show: errorMessage }">{{ errorMessage }}</div>

        <form v-if="activeTab === 'login'" class="auth-form active" @submit.prevent="submitLogin">
          <div class="auth-form-title">Welcome back</div>
          <div class="auth-form-sub">Sign in to your AllerScan account</div>

          <div class="auth-input-group">
            <label class="auth-input-label">Email Address</label>
            <input v-model="loginForm.email" type="email" class="auth-input" placeholder="your@email.com" />
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">Password</label>
            <input v-model="loginForm.password" type="password" class="auth-input" placeholder="Enter your password" />
          </div>

          <button class="auth-submit" :disabled="isLoading">{{ isLoading ? 'Signing in...' : 'Sign In' }}</button>
        </form>

        <form v-else class="auth-form active" @submit.prevent="submitRegister">
          <div class="auth-form-title">Create account</div>
          <div class="auth-form-sub">Join AllerScan - it's free!</div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px">
            <div class="auth-input-group">
              <label class="auth-input-label">First Name</label>
              <input v-model="registerForm.firstName" type="text" class="auth-input" placeholder="First name" />
            </div>
            <div class="auth-input-group">
              <label class="auth-input-label">Last Name</label>
              <input v-model="registerForm.lastName" type="text" class="auth-input" placeholder="Last name" />
            </div>
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">Email Address</label>
            <input v-model="registerForm.email" type="email" class="auth-input" placeholder="your@email.com" />
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">Password (min 6 characters)</label>
            <input v-model="registerForm.password" type="password" class="auth-input" placeholder="Create a password" />
          </div>

          <button class="auth-submit" :disabled="isLoading">
            {{ isLoading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>


