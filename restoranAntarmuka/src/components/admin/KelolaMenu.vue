<template>
  <div class="module">
    <div class="module-toolbar">
      <p class="count-info">{{ daftarMenuAdmin.length }} menu terdaftar</p>
      <button class="btn-primary" @click="bukaFormTambah">+ Tambah Menu</button>
    </div>

    <p v-if="sedangMemuat" class="loading-info">Memuat data menu...</p>
    <p v-if="pesanError" class="error-info">{{ pesanError }}</p>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th></th>
          <th>Nama Menu</th>
          <th>Kategori</th>
          <th>Harga</th>
          <th>Stok</th>
          <th>Status</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="menu in daftarMenuAdmin" :key="menu.id">
          <td class="cell-thumb">
            <img v-if="menu.gambarUrl" :src="urlGambar(menu.gambarUrl)" :alt="menu.nama" class="thumb-img" />
            <span v-else class="thumb-icon">{{ menu.icon }}</span>
          </td>
          <td class="cell-name">{{ menu.nama }}</td>
          <td><span class="tag">{{ menu.kategori }}</span></td>
          <td>Rp {{ menu.harga.toLocaleString('id-ID') }}</td>
          <td :class="{ 'low-stock': menu.stok < 10 }">{{ menu.stok }}</td>
          <td>
            <button
                class="status-toggle"
                :class="{ on: menu.aktif }"
                @click="toggleAktifMenu(menu.id)"
            >
              {{ menu.aktif ? 'Aktif' : 'Nonaktif' }}
            </button>
          </td>
          <td class="cell-actions">
            <button class="icon-btn" @click="bukaFormEdit(menu)">✎</button>
            <button class="icon-btn danger" @click="konfirmasiHapus(menu)">🗑</button>
          </td>
        </tr>
        <tr v-if="daftarMenuAdmin.length === 0">
          <td colspan="7" class="empty-row">Belum ada menu. Tambahkan menu pertama Anda.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal tambah/edit -->
    <div v-if="formTerbuka" class="modal-overlay" @click.self="tutupForm">
      <div class="modal">
        <h3>{{ modeEdit ? 'Ubah Menu' : 'Tambah Menu Baru' }}</h3>

        <form @submit.prevent="simpanMenu">
          <label class="field">
            <span>Foto Menu <span class="required-mark">*wajib</span></span>
            <div class="upload-box" :class="{ 'has-preview': previewGambar }">
              <img v-if="previewGambar" :src="previewGambar" alt="Preview" class="upload-preview" />
              <div v-else class="upload-placeholder">
                <span class="upload-icon">📷</span>
                <span>Klik untuk upload foto asli (JPG/PNG/WEBP, maks 5MB)</span>
              </div>
              <input type="file" accept="image/jpeg,image/png,image/webp" @change="pilihGambar" class="upload-input" />
            </div>
            <span v-if="sedangUpload" class="upload-status">Mengunggah gambar...</span>
            <span v-if="uploadError" class="upload-status error">{{ uploadError }}</span>
          </label>

          <label class="field">
            <span>Nama Menu</span>
            <input v-model="form.nama" type="text" required placeholder="cth. Nasi Goreng Spesial" />
          </label>

          <label class="field">
            <span>Deskripsi</span>
            <textarea v-model="form.deskripsi" rows="2" placeholder="Deskripsi singkat menu ini"></textarea>
          </label>

          <label class="field">
            <span>Kategori</span>
            <select v-model="form.kategori">
              <option value="makanan">Makanan</option>
              <option value="minuman">Minuman</option>
            </select>
          </label>

          <div class="field-grid">
            <label class="field">
              <span>Harga (Rp)</span>
              <input v-model.number="form.harga" type="number" min="0" required />
            </label>

            <label class="field">
              <span>Stok</span>
              <input v-model.number="form.stok" type="number" min="0" required />
            </label>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="tutupForm">Batal</button>
            <button type="submit" class="btn-primary" :disabled="!form.gambarUrl || sedangUpload">{{ modeEdit ? 'Simpan Perubahan' : 'Tambah Menu' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Konfirmasi hapus -->
    <div v-if="menuAkanDihapus" class="modal-overlay" @click.self="menuAkanDihapus = null">
      <div class="modal small">
        <h3>Hapus menu ini?</h3>
        <p class="confirm-text">
          "{{ menuAkanDihapus.nama }}" akan dihapus permanen dari daftar menu.
        </p>
        <div class="modal-actions">
          <button class="btn-secondary" @click="menuAkanDihapus = null">Batal</button>
          <button class="btn-danger" @click="eksekusiHapus">Ya, Hapus</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAdmin } from '../../composables/useAdmin'
import { urlGambar } from '../../services/api'

const { daftarMenuAdmin, muatMenu, tambahMenu, editMenu, hapusMenu, toggleAktifMenu, uploadGambarMenu } = useAdmin()

const sedangMemuat = ref(true)
const pesanError = ref('')

onMounted(async () => {
  try {
    await muatMenu()
  } catch (err) {
    pesanError.value = 'Gagal memuat menu dari server: ' + err.message
  } finally {
    sedangMemuat.value = false
  }
})

const formTerbuka = ref(false)
const modeEdit = ref(false)
const idSedangDiedit = ref(null)
const menuAkanDihapus = ref(null)

// icon emoji tidak lagi diisi manual oleh admin, cukup nilai tetap sebagai
// fallback di database (tidak pernah dipakai tampil karena foto kini wajib).
const ICON_DEFAULT = '🍽️'
const form = reactive({ nama: '', deskripsi: '', kategori: 'makanan', harga: 0, stok: 0, icon: ICON_DEFAULT, gambarUrl: '' })
const previewGambar = ref('') // untuk ditampilkan di form: bisa preview lokal (base64) atau URL server
const sedangUpload = ref(false)
const uploadError = ref('')

const resetForm = () => {
  form.nama = ''
  form.deskripsi = ''
  form.kategori = 'makanan'
  form.harga = 0
  form.stok = 0
  form.icon = ICON_DEFAULT
  form.gambarUrl = ''
  previewGambar.value = ''
  uploadError.value = ''
}

const bukaFormTambah = () => {
  resetForm()
  modeEdit.value = false
  formTerbuka.value = true
}

const bukaFormEdit = (menu) => {
  form.nama = menu.nama
  form.deskripsi = menu.deskripsi || ''
  form.kategori = menu.kategori
  form.harga = menu.harga
  form.stok = menu.stok
  form.icon = menu.icon
  form.gambarUrl = menu.gambarUrl || ''
  previewGambar.value = menu.gambarUrl ? urlGambar(menu.gambarUrl) : ''
  uploadError.value = ''
  idSedangDiedit.value = menu.id
  modeEdit.value = true
  formTerbuka.value = true
}

const tutupForm = () => {
  formTerbuka.value = false
}

// Tampilkan preview lokal langsung (biar tidak nunggu upload), lalu upload ke server di background.
const pilihGambar = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  uploadError.value = ''
  const reader = new FileReader()
  reader.onload = () => { previewGambar.value = reader.result }
  reader.readAsDataURL(file)

  sedangUpload.value = true
  try {
    form.gambarUrl = await uploadGambarMenu(file)
  } catch (err) {
    uploadError.value = err.message || 'Gagal upload gambar.'
    form.gambarUrl = ''
  } finally {
    sedangUpload.value = false
  }
}

const simpanMenu = async () => {
  if (sedangUpload.value) {
    alert('Tunggu proses upload gambar selesai dulu.')
    return
  }
  if (!form.gambarUrl) {
    alert('Foto menu wajib diupload sebelum disimpan.')
    return
  }
  try {
    if (modeEdit.value) {
      await editMenu(idSedangDiedit.value, { ...form })
    } else {
      await tambahMenu({ ...form })
    }
    formTerbuka.value = false
  } catch (err) {
    alert('Gagal menyimpan menu: ' + err.message)
  }
}

const konfirmasiHapus = (menu) => {
  menuAkanDihapus.value = menu
}

const eksekusiHapus = async () => {
  try {
    await hapusMenu(menuAkanDihapus.value.id)
  } catch (err) {
    alert('Gagal menghapus menu: ' + err.message)
  }
  menuAkanDihapus.value = null
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

.error-info {
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

.btn-primary:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.required-mark {
  color: #dc2626;
  font-weight: 500;
  font-size: 0.75rem;
  text-transform: none;
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

/* Table */
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

.cell-thumb {
  width: 52px;
}

.thumb-img {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--border);
  display: block;
}

.thumb-icon {
  font-size: 1.4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: #faf6ef;
  border-radius: 10px;
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
  text-transform: capitalize;
}

.low-stock {
  color: #dc2626;
  font-weight: 700;
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
.field select,
.field textarea {
  padding: 0.65rem 0.8rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-size: 0.9rem;
  font-weight: 400;
  outline: none;
  font-family: inherit;
  resize: vertical;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: var(--spice);
}

.upload-box {
  position: relative;
  border: 2px dashed var(--border);
  border-radius: 12px;
  background: #faf6ef;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-box:hover {
  border-color: var(--spice);
}

.upload-box.has-preview {
  border-style: solid;
  min-height: 160px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  color: var(--ink-soft);
  font-size: 0.78rem;
  font-weight: 500;
  text-align: center;
  padding: 1rem;
}

.upload-icon {
  font-size: 1.6rem;
}

.upload-preview {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

.upload-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.upload-status {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--ink-soft);
}

.upload-status.error {
  color: #dc2626;
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