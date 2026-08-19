<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-mark">🍽️</span>
        <h2>Selamat Datang Kembali</h2>
        <p>Masuk untuk melanjutkan pemesanan Anda di RestoKu</p>
      </div>

      <form class="login-form" @submit.prevent="submitLogin">
        <label class="field">
          <span class="field-label">Email</span>
          <input
              v-model="email"
              type="email"
              placeholder="nama@email.com"
              required
          />
        </label>

        <label class="field">
          <span class="field-label">Kata Sandi</span>
          <div class="password-wrap">
            <input
                v-model="password"
                :type="tampilkanSandi ? 'text' : 'password'"
                placeholder="Masukkan kata sandi"
                required
            />
            <button
                type="button"
                class="toggle-visibility"
                @click="tampilkanSandi = !tampilkanSandi"
            >
              {{ tampilkanSandi ? 'Sembunyikan' : 'Lihat' }}
            </button>
          </div>
        </label>

        <div class="field-row">
          <label class="checkbox">
            <input v-model="ingatSaya" type="checkbox" />
            <span>Ingat saya</span>
          </label>
          <a href="#" class="link">Lupa kata sandi?</a>
        </div>

        <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>

        <button type="submit" class="btn-submit" :disabled="sedangMasuk">
          {{ sedangMasuk ? 'Memproses...' : 'Masuk' }}
        </button>
      </form>

      <div class="divider"><span>atau</span></div>

      <p class="signup-hint">
        Belum punya akun? <RouterLink to="/daftar" class="link">Daftar sekarang</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

const email = ref('')
const password = ref('')
const ingatSaya = ref(false)
const tampilkanSandi = ref(false)
const sedangMasuk = ref(false)
const pesanError = ref('')

const submitLogin = async () => {
  pesanError.value = ''

  if (!email.value || !password.value) {
    pesanError.value = 'Mohon isi email dan kata sandi.'
    return
  }

  sedangMasuk.value = true
  try {
    const res = await login(email.value, password.value)
    if (res.role !== 'CUSTOMER') {
      pesanError.value = 'Halaman ini khusus untuk pelanggan/member. Kasir & admin punya halaman login sendiri.'
      return
    }
    const tujuan = route.query.redirect || '/akun'
    router.push(tujuan)
  } catch (err) {
    pesanError.value = err.message || 'Email atau kata sandi salah.'
  } finally {
    sedangMasuk.value = false
  }
}
</script>

<style scoped>
.login-page {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --cream: #fffbf5;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --border: #f1e4d6;
}

.login-page {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 2.5rem 2rem;
  box-shadow: 0 10px 40px rgba(28, 25, 21, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-mark {
  font-size: 2.2rem;
  display: block;
  margin-bottom: 0.75rem;
}

.login-header h2 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.6rem;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.login-header p {
  color: var(--ink-soft);
  font-size: 0.9rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--ink);
}

.field input {
  width: 100%;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  border: 1px solid var(--border);
  font-size: 0.95rem;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.field input:focus {
  border-color: var(--spice);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.12);
}

.password-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrap input {
  flex: 1;
  padding-right: 5.5rem;
}

.toggle-visibility {
  position: absolute;
  right: 0.6rem;
  background: none;
  border: none;
  color: var(--spice);
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--ink-soft);
  cursor: pointer;
}

.link {
  color: var(--spice);
  text-decoration: none;
  font-weight: 600;
}

.link:hover {
  text-decoration: underline;
}

.error-msg {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  font-size: 0.85rem;
  margin: 0;
}

.btn-submit {
  background: var(--spice);
  color: white;
  border: none;
  padding: 0.85rem;
  border-radius: 10px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: var(--spice-dark);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: var(--ink-soft);
  font-size: 0.8rem;
  margin: 1.6rem 0 1.2rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.divider span {
  padding: 0 0.8rem;
}

.signup-hint {
  text-align: center;
  font-size: 0.88rem;
  color: var(--ink-soft);
  margin: 0;
}
</style>