<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-mark">🍽️</span>
        <h2>Daftar Member RestoKu</h2>
        <p>Kumpulkan poin & nikmati pemesanan lebih cepat</p>
      </div>

      <form class="login-form" @submit.prevent="submitRegister">
        <label class="field">
          <span class="field-label">Nama Lengkap</span>
          <input v-model="nama" type="text" placeholder="Nama Anda" required />
        </label>

        <label class="field">
          <span class="field-label">Email</span>
          <input v-model="email" type="email" placeholder="nama@email.com" required />
        </label>

        <label class="field">
          <span class="field-label">Kata Sandi</span>
          <input v-model="password" type="password" placeholder="Minimal 6 karakter" minlength="6" required />
        </label>

        <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>

        <button type="submit" class="btn-submit" :disabled="sedangDaftar">
          {{ sedangDaftar ? 'Memproses...' : 'Daftar Sekarang' }}
        </button>
      </form>

      <p class="signup-hint">
        Sudah punya akun? <RouterLink to="/login" class="link">Masuk di sini</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { register } = useAuth()

const nama = ref('')
const email = ref('')
const password = ref('')
const sedangDaftar = ref(false)
const pesanError = ref('')

const submitRegister = async () => {
  pesanError.value = ''
  sedangDaftar.value = true
  try {
    await register(nama.value, email.value, password.value)
    router.push('/akun')
  } catch (err) {
    pesanError.value = err.message || 'Gagal mendaftar. Coba lagi.'
  } finally {
    sedangDaftar.value = false
  }
}
</script>

<style scoped>
.login-page {
  --ink: #1c1917;
  --ink-soft: #57534e;
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
  font-size: 1.5rem;
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

.signup-hint {
  text-align: center;
  font-size: 0.88rem;
  color: var(--ink-soft);
  margin: 1.5rem 0 0;
}

.link {
  color: var(--spice);
  text-decoration: none;
  font-weight: 600;
}

.link:hover {
  text-decoration: underline;
}
</style>
