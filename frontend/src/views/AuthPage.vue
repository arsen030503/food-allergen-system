<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const { t } = useI18n()

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
    localError.value = t('auth.fillAll')
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
    localError.value = t('auth.fillAll')
    return
  }
  if (payload.password.length < 6) {
    localError.value = t('auth.minPassword')
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
          <div class="auth-hero-title">{{ t('auth.staySafe') }}<br />{{ t('auth.eatSmart') }}</div>
          <div class="auth-hero-sub">
            {{ t('auth.heroSub') }}
          </div>
          <div class="auth-features">
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-shield"/></svg></div>
              {{ t('auth.euCovered') }}
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-shield"/></svg></div>
              {{ t('auth.fdaCovered') }}
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-scan"/></svg></div>
              {{ t('auth.instantAnalysis') }}
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon"><svg viewBox="0 0 24 24"><use href="#ic-profile"/></svg></div>
              {{ t('auth.personalProfile') }}
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
            {{ t('auth.signIn') }}
          </button>
          <button class="auth-tab" :class="{ active: activeTab === 'register' }" @click="switchTab('register')">
            {{ t('auth.createAccount') }}
          </button>
        </div>

        <div class="auth-error" :class="{ show: errorMessage }">{{ errorMessage }}</div>

        <form v-if="activeTab === 'login'" class="auth-form active" @submit.prevent="submitLogin">
          <div class="auth-form-title">{{ t('auth.welcomeBack') }}</div>
          <div class="auth-form-sub">{{ t('auth.signInSub') }}</div>

          <div class="auth-input-group">
            <label class="auth-input-label">{{ t('auth.emailAddress') }}</label>
            <input v-model="loginForm.email" type="email" class="auth-input" placeholder="your@email.com" />
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">{{ t('auth.password') }}</label>
            <input v-model="loginForm.password" type="password" class="auth-input" :placeholder="t('auth.enterPassword')" />
          </div>

          <button class="auth-submit" :disabled="isLoading">{{ isLoading ? t('auth.signInProgress') : t('auth.signIn') }}</button>
        </form>

        <form v-else class="auth-form active" @submit.prevent="submitRegister">
          <div class="auth-form-title">{{ t('auth.createAccountTitle') }}</div>
          <div class="auth-form-sub">{{ t('auth.createAccountSub') }}</div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px">
            <div class="auth-input-group">
              <label class="auth-input-label">{{ t('auth.firstName') }}</label>
              <input v-model="registerForm.firstName" type="text" class="auth-input" :placeholder="t('auth.firstNamePlaceholder')" />
            </div>
            <div class="auth-input-group">
              <label class="auth-input-label">{{ t('auth.lastName') }}</label>
              <input v-model="registerForm.lastName" type="text" class="auth-input" :placeholder="t('auth.lastNamePlaceholder')" />
            </div>
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">{{ t('auth.emailAddress') }}</label>
            <input v-model="registerForm.email" type="email" class="auth-input" placeholder="your@email.com" />
          </div>

          <div class="auth-input-group">
            <label class="auth-input-label">{{ t('auth.passwordMin') }}</label>
            <input v-model="registerForm.password" type="password" class="auth-input" :placeholder="t('auth.createPassword')" />
          </div>

          <button class="auth-submit" :disabled="isLoading">
            {{ isLoading ? t('auth.createProgress') : t('auth.createAccount') }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>


