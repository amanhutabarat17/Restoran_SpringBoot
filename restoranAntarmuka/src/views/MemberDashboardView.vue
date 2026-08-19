<template>
  <div class="member-page">
    <div class="page-header">
      <span class="eyebrow">Akun Saya</span>
      <h2>Halo, {{ auth.state.user?.nama }} 👋</h2>
    </div>

    <div class="poin-card">
      <div>
        <p class="poin-label">Poin Member Anda</p>
        <p class="poin-value">{{ auth.state.user?.poin || 0 }} poin</p>
      </div>
      <button class="btn-logout" @click="handleLogout">Keluar</button>
    </div>

    <h3 class="section-title">Riwayat Pesanan</h3>

    <p v-if="sedangMemuat" class="loading-info">Memuat riwayat pesanan...</p>
    <p v-if="pesanError" class="error-info">{{ pesanError }}</p>
    <p v-if="!sedangMemuat && pesanan.length === 0" class="empty-info">Anda belum pernah memesan.</p>

    <div class="order-list">
      <div v-for="order in pesanan" :key="order.id" class="order-card">
        <div class="order-header">
          <div>
            <p class="order-number">{{ order.orderNumber }}</p>
            <p class="order-date">{{ formatTanggal(order.createdAt) }}</p>
          </div>
          <span class="badge" :class="badgeClass(order.status)">{{ labelStatus(order.status) }}</span>
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item-row">
            <span>{{ item.qty }}x {{ item.namaMenu }}</span>
            <div class="item-right">
              <span>Rp {{ item.subtotal.toLocaleString('id-ID') }}</span>
              <template v-if="order.status === 'SELESAI' && item.menu">
                <button
                    v-if="!sudahDinilai(order.id, item.menu.id)"
                    class="btn-rate"
                    @click="bukaFormRating(order, item)"
                >
                  ⭐ Beri Rating
                </button>
                <span v-else class="sudah-dinilai">✓ Sudah dinilai</span>
              </template>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <span>Total</span>
          <span class="order-total">Rp {{ order.total.toLocaleString('id-ID') }}</span>
        </div>
      </div>
    </div>

    <!-- Modal rating -->
    <div v-if="ratingTarget" class="modal-overlay" @click.self="ratingTarget = null">
      <div class="modal">
        <h3>Beri Rating: {{ ratingTarget.item.namaMenu }}</h3>

        <div class="bintang-picker">
          <button
              v-for="n in 5"
              :key="n"
              class="bintang-btn"
              :class="{ aktif: n <= ratingForm.bintang }"
              @click="ratingForm.bintang = n"
          >★</button>
        </div>

        <label class="field">
          <span>Komentar (opsional)</span>
          <textarea v-model="ratingForm.komentar" rows="3" placeholder="Bagaimana rasanya?"></textarea>
        </label>

        <p v-if="ratingError" class="error-msg">{{ ratingError }}</p>

        <div class="modal-actions">
          <button class="btn-secondary" @click="ratingTarget = null">Batal</button>
          <button class="btn-primary" :disabled="sedangKirimRating" @click="kirimRating">
            {{ sedangKirimRating ? 'Mengirim...' : 'Kirim Rating' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { api } from '../services/api'

const router = useRouter()
const auth = useAuth()

const pesanan = ref([])
const sedangMemuat = ref(true)
const pesanError = ref('')
const ratingTerkirim = ref(new Set()) // "orderId-menuId" yang sudah dinilai di sesi ini

const muatPesanan = async () => {
  sedangMemuat.value = true
  pesanError.value = ''
  try {
    pesanan.value = await api.get('/orders/saya')
  } catch (err) {
    pesanError.value = 'Gagal memuat riwayat pesanan: ' + err.message
  } finally {
    sedangMemuat.value = false
  }
}

onMounted(muatPesanan)

const formatTanggal = (tgl) =>
    new Date(tgl).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const labelStatus = (s) => ({
  MENUNGGU: 'Menunggu', DIPROSES: 'Diproses', SELESAI: 'Selesai', DIBATALKAN: 'Dibatalkan'
}[s] || s)

const badgeClass = (s) => ({
  MENUNGGU: 'badge-menunggu', DIPROSES: 'badge-diproses', SELESAI: 'badge-selesai', DIBATALKAN: 'badge-batal'
}[s] || '')

const sudahDinilai = (orderId, menuId) => ratingTerkirim.value.has(`${orderId}-${menuId}`)

const ratingTarget = ref(null)
const ratingForm = reactive({ bintang: 5, komentar: '' })
const ratingError = ref('')
const sedangKirimRating = ref(false)

const bukaFormRating = (order, item) => {
  ratingTarget.value = { order, item }
  ratingForm.bintang = 5
  ratingForm.komentar = ''
  ratingError.value = ''
}

const kirimRating = async () => {
  ratingError.value = ''
  sedangKirimRating.value = true
  try {
    const { order, item } = ratingTarget.value
    await api.post('/ratings', {
      orderId: order.id,
      menuId: item.menu.id,
      bintang: ratingForm.bintang,
      komentar: ratingForm.komentar
    })
    ratingTerkirim.value.add(`${order.id}-${item.menu.id}`)
    ratingTarget.value = null
  } catch (err) {
    ratingError.value = err.message || 'Gagal mengirim rating.'
  } finally {
    sedangKirimRating.value = false
  }
}

const handleLogout = () => {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.member-page {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --sage: #15803d;
  --border: #f1e4d6;
}

.member-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--ink);
}

.page-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.eyebrow {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--spice);
  margin-bottom: 0.5rem;
}

.page-header h2 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.9rem;
  margin: 0;
}

.poin-card {
  background: var(--ink);
  color: white;
  border-radius: 16px;
  padding: 1.4rem 1.6rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.poin-label {
  font-size: 0.8rem;
  opacity: 0.75;
  margin: 0 0 0.3rem;
}

.poin-value {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.6rem;
  margin: 0;
  color: #fb923c;
}

.btn-logout {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.55rem 1rem;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.18);
}

.section-title {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.2rem;
  margin: 0 0 1rem;
}

.loading-info, .empty-info {
  color: var(--ink-soft);
  font-size: 0.88rem;
}

.error-info {
  color: #dc2626;
  font-size: 0.88rem;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 1.2rem 1.4rem;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.8rem;
}

.order-number {
  font-weight: 700;
  margin: 0;
}

.order-date {
  font-size: 0.78rem;
  color: var(--ink-soft);
  margin: 0.2rem 0 0;
}

.badge {
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
}

.badge-menunggu { background: #fef3c7; color: #b45309; }
.badge-diproses { background: #dbeafe; color: #1d4ed8; }
.badge-selesai { background: #dcfce7; color: var(--sage); }
.badge-batal { background: #fee2e2; color: #b91c1c; }

.order-items {
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  padding: 0.7rem 0;
  margin-bottom: 0.7rem;
}

.order-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  padding: 0.35rem 0;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.btn-rate {
  background: var(--peach);
  color: var(--spice-dark);
  border: none;
  padding: 0.3rem 0.6rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 700;
  cursor: pointer;
}

.btn-rate:hover {
  background: #fed7aa;
}

.sudah-dinilai {
  font-size: 0.75rem;
  color: var(--sage);
  font-weight: 600;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  font-weight: 700;
}

.order-total {
  color: var(--spice-dark);
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
  max-width: 400px;
}

.modal h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.15rem;
  margin: 0 0 1.2rem;
}

.bintang-picker {
  display: flex;
  gap: 0.4rem;
  justify-content: center;
  margin-bottom: 1.2rem;
}

.bintang-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #d1d5db;
  cursor: pointer;
  transition: color 0.15s;
}

.bintang-btn.aktif {
  color: #f59e0b;
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

.field textarea:focus {
  border-color: var(--spice);
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

.btn-primary:hover:not(:disabled) {
  background: var(--spice-dark);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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
</style>
