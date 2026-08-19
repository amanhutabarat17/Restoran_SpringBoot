<template>
  <div class="module">
    <div class="module-toolbar">
      <p class="count-info">{{ daftarPromo.length }} promo terdaftar</p>
      <button class="btn-primary" @click="bukaFormTambah">+ Buat Promo</button>
    </div>

    <p v-if="sedangMemuat" class="loading-info">Memuat data promo...</p>
    <p v-if="pesanError" class="error-info">{{ pesanError }}</p>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th>Judul</th>
          <th>Kode</th>
          <th>Tipe</th>
          <th>Nilai</th>
          <th>Berlaku Sampai</th>
          <th>Status</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="promo in daftarPromo" :key="promo.id">
          <td class="cell-name">{{ promo.judul }}</td>
          <td><code class="kode">{{ promo.kode }}</code></td>
          <td><span class="tag">{{ promo.tipe }}</span></td>
          <td>{{ promo.tipe === 'persen' ? promo.nilai + '%' : 'Rp ' + promo.nilai.toLocaleString('id-ID') }}</td>
          <td :class="{ 'expired': sudahLewat(promo.berlakuSampai) }">{{ formatTanggal(promo.berlakuSampai) }}</td>
          <td>
            <button
                class="status-toggle"
                :class="{ on: promo.aktif }"
                @click="toggleAktifPromo(promo.id)"
            >
              {{ promo.aktif ? 'Aktif' : 'Nonaktif' }}
            </button>
          </td>
          <td class="cell-actions">
            <button class="icon-btn" @click="bukaFormEdit(promo)">✎</button>
            <button class="icon-btn danger" @click="konfirmasiHapus(promo)">🗑</button>
          </td>
        </tr>
        <tr v-if="daftarPromo.length === 0 && !sedangMemuat">
          <td colspan="7" class="empty-row">Belum ada promo. Buat promo pertama Anda.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal tambah/edit -->
    <div v-if="formTerbuka" class="modal-overlay" @click.self="tutupForm">
      <div class="modal">
        <h3>{{ modeEdit ? 'Ubah Promo' : 'Buat Promo Baru' }}</h3>

        <form @submit.prevent="simpanPromo">
          <label class="field">
            <span>Judul Promo</span>
            <input v-model="form.judul" type="text" required placeholder="cth. Diskon Jam Makan Siang" />
          </label>

          <label class="field">
            <span>Kode Promo</span>
            <input v-model="form.kode" type="text" required placeholder="cth. SIANG15" style="text-transform: uppercase" />
          </label>

          <div class="field-grid">
            <label class="field">
              <span>Tipe Diskon</span>
              <select v-model="form.tipe">
                <option value="persen">Persen (%)</option>
                <option value="nominal">Nominal (Rp)</option>
              </select>
            </label>

            <label class="field">
              <span>Nilai {{ form.tipe === 'persen' ? '(%, maks 100)' : '(Rp)' }}</span>
              <input
                  v-model.number="form.nilai"
                  type="number"
                  min="0"
                  :max="form.tipe === 'persen' ? 100 : undefined"
                  required
              />
            </label>
          </div>

          <label class="field">
            <span>Berlaku Sampai</span>
            <input v-model="form.berlakuSampai" type="date" required />
          </label>

          <p v-if="pesanErrorForm" class="error-msg">{{ pesanErrorForm }}</p>

          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="tutupForm">Batal</button>
            <button type="submit" class="btn-primary">{{ modeEdit ? 'Simpan Perubahan' : 'Buat Promo' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Konfirmasi hapus -->
    <div v-if="promoAkanDihapus" class="modal-overlay" @click.self="promoAkanDihapus = null">
      <div class="modal small">
        <h3>Hapus promo ini?</h3>
        <p class="confirm-text">
          "{{ promoAkanDihapus.judul }}" ({{ promoAkanDihapus.kode }}) akan dihapus permanen.
        </p>
        <div class="modal-actions">
          <button class="btn-secondary" @click="promoAkanDihapus = null">Batal</button>
          <button class="btn-danger" @click="eksekusiHapus">Ya, Hapus</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAdmin } from '../../composables/useAdmin'

const { daftarPromo, muatPromo, tambahPromo, editPromo, hapusPromo, toggleAktifPromo } = useAdmin()

const sedangMemuat = ref(true)
const pesanError = ref('')
const pesanErrorForm = ref('')

const formTerbuka = ref(false)
const modeEdit = ref(false)
const idSedangDiedit = ref(null)
const promoAkanDihapus = ref(null)

const form = reactive({ judul: '', kode: '', tipe: 'persen', nilai: 0, berlakuSampai: '' })

onMounted(async () => {
  try {
    await muatPromo()
  } catch (err) {
    pesanError.value = 'Gagal memuat data promo: ' + err.message
  } finally {
    sedangMemuat.value = false
  }
})

const formatTanggal = (tgl) => {
  if (!tgl) return '-'
  return new Date(tgl).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

const sudahLewat = (tgl) => tgl && new Date(tgl) < new Date(new Date().toDateString())

const resetForm = () => {
  form.judul = ''
  form.kode = ''
  form.tipe = 'persen'
  form.nilai = 0
  form.berlakuSampai = ''
  pesanErrorForm.value = ''
}

const bukaFormTambah = () => {
  resetForm()
  modeEdit.value = false
  formTerbuka.value = true
}

const bukaFormEdit = (promo) => {
  form.judul = promo.judul
  form.kode = promo.kode
  form.tipe = promo.tipe
  form.nilai = promo.nilai
  form.berlakuSampai = promo.berlakuSampai
  idSedangDiedit.value = promo.id
  modeEdit.value = true
  formTerbuka.value = true
}

const tutupForm = () => {
  formTerbuka.value = false
}

const simpanPromo = async () => {
  pesanErrorForm.value = ''
  try {
    const payload = { ...form, kode: form.kode.toUpperCase() }
    if (modeEdit.value) {
      await editPromo(idSedangDiedit.value, payload)
    } else {
      await tambahPromo(payload)
    }
    formTerbuka.value = false
  } catch (err) {
    pesanErrorForm.value = err.message
  }
}

const konfirmasiHapus = (promo) => {
  promoAkanDihapus.value = promo
}

const eksekusiHapus = async () => {
  try {
    await hapusPromo(promoAkanDihapus.value.id)
  } catch (err) {
    alert('Gagal menghapus promo: ' + err.message)
  }
  promoAkanDihapus.value = null
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

.loading-info {
  color: var(--ink-soft);
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
}

.error-info,
.error-msg {
  color: #dc2626;
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
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
  transition: background 0.2s;
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

.btn-danger {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.65rem 1.2rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
}

.btn-danger:hover {
  background: #b91c1c;
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
  color: var(--ink);
}

.cell-name {
  font-weight: 600;
}

.kode {
  background: #f3f4f6;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  font-size: 0.82rem;
}

.tag {
  background: var(--peach);
  color: var(--spice-dark);
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
  text-transform: capitalize;
}

.expired {
  color: #dc2626;
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
  display: flex;
  gap: 0.4rem;
  white-space: nowrap;
}

.icon-btn {
  background: none;
  border: 1px solid var(--border);
  width: 30px;
  height: 30px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
}

.icon-btn:hover {
  background: var(--peach);
}

.icon-btn.danger:hover {
  background: #fee2e2;
  border-color: #fecaca;
}

.empty-row {
  text-align: center;
  color: var(--ink-soft);
  padding: 2rem;
}

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

.modal.small {
  max-width: 360px;
}

.modal h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.25rem;
  margin: 0 0 1.2rem;
}

.confirm-text {
  color: var(--ink-soft);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
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

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.7rem;
  margin-top: 0.5rem;
}
</style>
