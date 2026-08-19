<template>
  <div class="module">
    <div class="module-toolbar">
      <p class="count-info">{{ daftarKasir.length }} kasir terdaftar</p>
      <button class="btn-primary" @click="formTerbuka = true">+ Daftarkan Kasir</button>
    </div>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th>Nama</th>
          <th>Email</th>
          <th>Shift</th>
          <th>Status</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="kasir in daftarKasir" :key="kasir.id">
          <td class="cell-name">{{ kasir.nama }}</td>
          <td>{{ kasir.email }}</td>
          <td><span class="tag">{{ kasir.shift }}</span></td>
          <td>
            <button
                class="status-toggle"
                :class="{ on: kasir.aktif }"
                @click="toggleAktifKasir(kasir.id)"
            >
              {{ kasir.aktif ? 'Aktif' : 'Nonaktif' }}
            </button>
          </td>
          <td class="cell-actions">
            <button class="icon-btn danger" @click="hapusKasir(kasir.id)">🗑 Hapus</button>
          </td>
        </tr>
        <tr v-if="daftarKasir.length === 0">
          <td colspan="5" class="empty-row">Belum ada kasir terdaftar.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal daftar kasir -->
    <div v-if="formTerbuka" class="modal-overlay" @click.self="formTerbuka = false">
      <div class="modal">
        <h3>Daftarkan Kasir Baru</h3>

        <form @submit.prevent="simpanKasir">
          <label class="field">
            <span>Nama Lengkap</span>
            <input v-model="form.nama" type="text" required placeholder="cth. Siti Nurhaliza" />
          </label>

          <label class="field">
            <span>Email</span>
            <input v-model="form.email" type="email" required placeholder="nama@restoku.id" />
          </label>

          <div class="field-grid">
            <label class="field">
              <span>Shift</span>
              <select v-model="form.shift">
                <option value="Pagi">Pagi</option>
                <option value="Siang">Siang</option>
                <option value="Malam">Malam</option>
              </select>
            </label>

            <label class="field">
              <span>Kata Sandi Awal</span>
              <input v-model="form.password" type="password" required placeholder="Min. 8 karakter" minlength="8" />
            </label>
          </div>

          <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>

          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="formTerbuka = false">Batal</button>
            <button type="submit" class="btn-primary">Daftarkan</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAdmin } from '../../composables/useAdmin'

const { daftarKasir, muatKasir, tambahKasir, hapusKasir, toggleAktifKasir } = useAdmin()

const formTerbuka = ref(false)
const pesanError = ref('')
const sedangMemuat = ref(true)
const form = reactive({ nama: '', email: '', shift: 'Pagi', password: '' })

onMounted(async () => {
  try {
    await muatKasir()
  } catch (err) {
    pesanError.value = 'Gagal memuat data kasir: ' + err.message
  } finally {
    sedangMemuat.value = false
  }
})

const resetForm = () => {
  form.nama = ''
  form.email = ''
  form.shift = 'Pagi'
  form.password = ''
  pesanError.value = ''
}

const simpanKasir = async () => {
  const sudahAda = daftarKasir.some((k) => k.email.toLowerCase() === form.email.toLowerCase())
  if (sudahAda) {
    pesanError.value = 'Email ini sudah terdaftar sebagai kasir.'
    return
  }

  try {
    await tambahKasir({ nama: form.nama, email: form.email, password: form.password, shift: form.shift })
    resetForm()
    formTerbuka.value = false
  } catch (err) {
    pesanError.value = err.message
  }
}
</script>

<style scoped>
.module {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --border: #f1e4d6;
  --sage: #15803d;
}

.module-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.count-info {
  color: var(--ink-soft);
  font-size: 0.88rem;
}

.btn-primary {
  background: var(--spice);
  color: white;
  border: none;
  padding: 0.65rem 1.2rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
}

.btn-primary:hover {
  background: var(--spice-dark);
}

.btn-secondary {
  background: white;
  border: 1px solid var(--border);
  color: var(--ink-soft);
  padding: 0.65rem 1.2rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
}

.btn-secondary:hover {
  background: var(--peach);
}

.table-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

thead {
  background: #faf6ef;
}

th {
  text-align: left;
  padding: 0.9rem 1rem;
  color: var(--ink-soft);
  font-weight: 600;
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

td {
  padding: 0.85rem 1rem;
  border-top: 1px solid var(--border);
}

.cell-name {
  font-weight: 600;
}

.tag {
  background: var(--peach);
  color: var(--spice-dark);
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status-toggle {
  border: none;
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  background: #f3f4f6;
  color: #6b7280;
}

.status-toggle.on {
  background: #dcfce7;
  color: var(--sage);
}

.cell-actions {
  white-space: nowrap;
}

.icon-btn {
  background: none;
  border: 1px solid var(--border);
  padding: 0.4rem 0.7rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--ink-soft);
}

.icon-btn.danger:hover {
  background: #fee2e2;
  color: #b91c1c;
  border-color: #fecaca;
}

.empty-row {
  text-align: center;
  color: var(--ink-soft);
  padding: 2rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 25, 21, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 18px;
  padding: 1.8rem;
  width: 100%;
  max-width: 440px;
}

.modal h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.25rem;
  margin: 0 0 1.2rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--ink);
}

.field input,
.field select {
  padding: 0.65rem 0.8rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-size: 0.9rem;
  font-weight: 400;
  outline: none;
}

.field input:focus,
.field select:focus {
  border-color: var(--spice);
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.error-msg {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.7rem;
  margin-top: 0.5rem;
}
</style>