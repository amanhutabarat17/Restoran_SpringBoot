<template>
  <div class="kasir-login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-mark">🧾</span>
        <h2>Login Kasir</h2>
        <p>Khusus staf kasir RestoKu</p>
      </div>

      <form class="login-form" @submit.prevent="submitLogin">
        <label class="field">
          <span class="field-label">Email</span>
          <input v-model="email" type="email" placeholder="kasir@restoku.id" required />
        </label>

        <label class="field">
          <span class="field-label">Kata Sandi</span>
          <input v-model="password" type="password" placeholder="Masukkan kata sandi" required />
        </label>

        <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>

        <button type="submit" class="btn-submit" :disabled="sedangMasuk">
          {{ sedangMasuk ? 'Memproses...' : 'Masuk' }}
        </button>
      </form>

      <RouterLink to="/" class="back-link">← Kembali ke halaman utama</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth'

const router = useRouter()
const { login } = useAuth()

const email = ref('')
const password = ref('')
const sedangMasuk = ref(false)
const pesanError = ref('')

const submitLogin = async () => {
  pesanError.value = ''
  sedangMasuk.value = true
  try {
    const res = await login(email.value, password.value)
    if (res.role !== 'KASIR') {
      pesanError.value = 'Akun ini bukan akun kasir.'
      return
    }
    router.push('/kasir')
  } catch (err) {
    pesanError.value = err.message || 'Email atau kata sandi salah.'
  } finally {
    sedangMasuk.value = false
  }
}
</script>

<style scoped>
.kasir-login-page {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --border: #f1e4d6;
}

.kasir-login-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  background: #1c1917;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: white;
  border-radius: 20px;
  padding: 2.5rem 2rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-mark {
  font-size: 2rem;
  display: block;
  margin-bottom: 0.75rem;
}

.login-header h2 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.5rem;
  color: var(--ink);
  margin: 0 0 0.4rem;
}

.login-header p {
  color: var(--ink-soft);
  font-size: 0.85rem;
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
}

.field input:focus {
  border-color: var(--spice);
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
  background: var(--ink);
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

.back-link {
  display: block;
  text-align: center;
  margin-top: 1.5rem;
  color: var(--ink-soft);
  text-decoration: none;
  font-size: 0.85rem;
}

.back-link:hover {
  color: var(--spice);
}
</style>
