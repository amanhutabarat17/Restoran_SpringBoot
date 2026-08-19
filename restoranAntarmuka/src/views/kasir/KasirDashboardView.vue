<template>
  <div class="kasir-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">🧾</span>
        <span class="brand-text">RestoKu <em>Kasir</em></span>
      </div>

      <nav class="sidebar-nav">
        <button v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: tabAktif === tab.key }" @click="pilihTab(tab.key)">
          <span class="nav-icon">{{ tab.icon }}</span>
          {{ tab.label }}
          <span v-if="tab.key === 'antrian' && jumlahAntrian > 0" class="nav-badge">{{ jumlahAntrian }}</span>
        </button>
      </nav>

      <div class="kasir-info">👤 {{ auth.state.user?.nama }}</div>
      <button class="logout-btn" @click="handleLogout">↩ Keluar</button>
    </aside>

    <main class="kasir-content">
      <!-- TAB: ANTRIAN -->
      <section v-if="tabAktif === 'antrian'">
        <header class="content-header">
          <h1>Antrian Pesanan</h1>
          <button class="btn-refresh" @click="muatSemuaPesanan">🔄 Refresh</button>
        </header>

        <p v-if="sedangMemuat" class="loading-info">Memuat pesanan...</p>

        <div class="order-grid">
          <div v-for="order in pesananAktif" :key="order.id" class="order-card">
            <div class="order-card-header">
              <div>
                <p class="order-number">
                  <span v-if="order.nomorAntrian" class="antrian-chip">#{{ order.nomorAntrian }}</span>
                  {{ order.orderNumber }}
                </p>
                <p class="order-meta">
                  {{ order.noMeja ? '📍 Meja ' + order.noMeja : '🧍 Walk-in' }}
                  · {{ formatWaktu(order.createdAt) }}
                </p>
              </div>
              <span class="badge" :class="badgeClass(order.status)">{{ labelStatus(order.status) }}</span>
            </div>

            <div class="order-items-mini">
              <p v-for="item in order.items" :key="item.id">{{ item.qty }}x {{ item.namaMenu }}</p>
            </div>

            <div class="order-card-footer">
              <div>
                <span class="total-label">Total</span>
                <span class="total-value">Rp {{ order.total.toLocaleString('id-ID') }}</span>
                <span class="pay-badge" :class="order.statusPembayaran === 'PAID' ? 'paid' : 'unpaid'">
                  {{ order.statusPembayaran === 'PAID' ? 'Lunas' : 'Belum Bayar' }}
                </span>
              </div>

              <div class="order-actions">
                <button
                    v-if="order.statusPembayaran !== 'PAID'"
                    class="btn-small btn-bayar"
                    :disabled="sedangProsesId === order.id"
                    @click="bukaModalBayar(order)"
                >
                  💳 Bayar
                </button>

                <button
                    v-if="order.status === 'MENUNGGU'"
                    class="btn-small btn-proses"
                    :disabled="sedangProsesId === order.id"
                    @click="ubahStatus(order, 'DIPROSES')"
                >
                  Mulai Proses
                </button>
                <button
                    v-if="order.status === 'DIPROSES'"
                    class="btn-small btn-selesai"
                    :disabled="sedangProsesId === order.id"
                    @click="ubahStatus(order, 'SELESAI')"
                >
                  Tandai Selesai
                </button>
                <button
                    v-if="order.status === 'MENUNGGU' || order.status === 'DIPROSES'"
                    class="btn-small btn-batal"
                    :disabled="sedangProsesId === order.id"
                    @click="ubahStatus(order, 'DIBATALKAN')"
                >
                  Batalkan
                </button>
              </div>
            </div>
          </div>

          <p v-if="!sedangMemuat && pesananAktif.length === 0" class="empty-info">Tidak ada pesanan aktif saat ini.</p>
        </div>
      </section>

      <!-- TAB: PESANAN BARU (WALK-IN) -->
      <section v-else-if="tabAktif === 'baru'">
        <header class="content-header">
          <h1>Input Pesanan Baru</h1>
          <p class="subtitle">Untuk pelanggan yang memesan langsung di kasir</p>
        </header>

        <div class="pos-layout">
          <div class="pos-menu-list">
            <div class="pos-kategori-filter">
              <button :class="{ active: filterKategori === 'semua' }" @click="filterKategori = 'semua'">Semua</button>
              <button :class="{ active: filterKategori === 'makanan' }" @click="filterKategori = 'makanan'">Makanan</button>
              <button :class="{ active: filterKategori === 'minuman' }" @click="filterKategori = 'minuman'">Minuman</button>
            </div>

            <div class="pos-menu-grid">
              <button v-for="menu in menuTersaring" :key="menu.id" class="pos-menu-item" @click="tambahKeOrderBaru(menu)">
                <span class="pos-menu-icon">{{ menu.icon || '🍽️' }}</span>
                <span class="pos-menu-nama">{{ menu.nama }}</span>
                <span class="pos-menu-harga">Rp {{ menu.harga.toLocaleString('id-ID') }}</span>
              </button>
            </div>
          </div>

          <div class="pos-cart">
            <h3>Pesanan Berjalan</h3>

            <div v-if="orderBaru.length === 0" class="pos-cart-empty">Klik menu di samping untuk menambah.</div>

            <div v-for="item in orderBaru" :key="item.id" class="pos-cart-row">
              <div>
                <p class="pos-cart-nama">{{ item.nama }}</p>
                <p class="pos-cart-harga">Rp {{ item.harga.toLocaleString('id-ID') }}</p>
              </div>
              <div class="pos-stepper">
                <button @click="kurangiOrderBaru(item.id)">−</button>
                <span>{{ item.qty }}</span>
                <button @click="tambahKeOrderBaru(item)">+</button>
              </div>
            </div>

            <div class="pos-cart-total" v-if="orderBaru.length">
              <span>Total</span>
              <span>Rp {{ totalOrderBaru.toLocaleString('id-ID') }}</span>
            </div>

            <p v-if="pesanErrorBaru" class="error-msg">{{ pesanErrorBaru }}</p>

            <button class="btn-buat-pesanan" :disabled="orderBaru.length === 0 || sedangBuatPesanan" @click="buatPesananBaru">
              {{ sedangBuatPesanan ? 'Memproses...' : 'Buat Pesanan' }}
            </button>
          </div>
        </div>
      </section>

      <!-- TAB: RIWAYAT -->
      <section v-else-if="tabAktif === 'riwayat'">
        <header class="content-header">
          <h1>Riwayat Transaksi</h1>
        </header>

        <div class="table-card">
          <table>
            <thead>
            <tr>
              <th>Antrian</th>
              <th>No. Pesanan</th>
              <th>Meja</th>
              <th>Waktu</th>
              <th>Status</th>
              <th>Bayar</th>
              <th>Total</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="order in semuaPesanan" :key="order.id">
              <td>{{ order.nomorAntrian ? '#' + order.nomorAntrian : '-' }}</td>
              <td>{{ order.orderNumber }}</td>
              <td>{{ order.noMeja || '-' }}</td>
              <td>{{ formatWaktu(order.createdAt) }}</td>
              <td><span class="badge" :class="badgeClass(order.status)">{{ labelStatus(order.status) }}</span></td>
              <td>{{ order.statusPembayaran === 'PAID' ? 'Lunas' : 'Belum' }}</td>
              <td>Rp {{ order.total.toLocaleString('id-ID') }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </section>
      <div v-if="orderModalAktif" class="modal-overlay" @click.self="tutupModalBayar">
        <div class="modal-bayar">
          <div class="modal-header">
            <h3>
              <span v-if="orderModalAktif.nomorAntrian" class="antrian-chip">#{{ orderModalAktif.nomorAntrian }}</span>
              Bayar {{ orderModalAktif.orderNumber }}
            </h3>
            <button class="modal-close" @click="tutupModalBayar">✕</button>
          </div>

          <div class="modal-total">
            <span>Total Tagihan</span>
            <span class="modal-total-value">Rp {{ orderModalAktif.total.toLocaleString('id-ID') }}</span>
          </div>

          <div class="modal-tabs">
            <button :class="{ active: modalTab === 'tunai' }" @click="modalTab = 'tunai'">💵 Tunai</button>
            <button :class="{ active: modalTab === 'midtrans' }" @click="modalTab = 'midtrans'">📱 Midtrans</button>
          </div>

          <!-- TAB TUNAI: kalkulator -->
          <div v-if="modalTab === 'tunai'" class="modal-tunai">
            <label class="modal-label">Uang diterima</label>
            <input
                type="number"
                v-model.number="uangDiterima"
                class="modal-input"
                placeholder="0"
                @keyup.enter="konfirmasiBayarTunai"
            />

            <div class="quick-cash">
              <button v-for="nominal in nominalCepat(orderModalAktif.total)" :key="nominal" @click="uangDiterima = nominal">
                {{ formatRp(nominal) }}
              </button>
            </div>

            <div class="modal-kembalian" :class="{ negatif: kembalian < 0 }">
              <span>Kembalian</span>
              <span>{{ kembalian >= 0 ? 'Rp ' + kembalian.toLocaleString('id-ID') : 'Kurang Rp ' + Math.abs(kembalian).toLocaleString('id-ID') }}</span>
            </div>

            <p v-if="errorBayar" class="error-msg">{{ errorBayar }}</p>

            <button
                class="btn-konfirmasi-bayar"
                :disabled="kembalian < 0 || sedangProsesBayar"
                @click="konfirmasiBayarTunai"
            >
              {{ sedangProsesBayar ? 'Memproses...' : 'Konfirmasi Bayar Tunai' }}
            </button>
          </div>

          <!-- TAB MIDTRANS -->
          <div v-else class="modal-midtrans">
            <p class="modal-midtrans-desc">
              Pelanggan bisa scan QRIS atau bayar kartu/e-wallet. Klik tombol di bawah untuk membuka halaman pembayaran di tab baru.
            </p>

            <p v-if="statusMenungguMidtrans" class="modal-midtrans-waiting">
              ⏳ Menunggu pembayaran dari pelanggan...
            </p>

            <p v-if="errorBayar" class="error-msg">{{ errorBayar }}</p>

            <button class="btn-konfirmasi-bayar" :disabled="sedangProsesBayar" @click="bayarMidtransKasir(orderModalAktif)">
              {{ sedangProsesBayar ? 'Membuka...' : 'Buka Halaman Pembayaran' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth'
import { api } from '../../services/api'

const router = useRouter()
const auth = useAuth()

const tabs = [
  { key: 'antrian', label: 'Antrian Pesanan', icon: '📋' },
  { key: 'baru', label: 'Pesanan Baru', icon: '➕' },
  { key: 'riwayat', label: 'Riwayat', icon: '🕘' }
]
const tabAktif = ref('antrian')

const semuaPesanan = ref([])
const sedangMemuat = ref(true)
const sedangProsesId = ref(null)

const pesananAktif = computed(() =>
    semuaPesanan.value.filter((o) => o.status === 'MENUNGGU' || o.status === 'DIPROSES')
)
const jumlahAntrian = computed(() => pesananAktif.value.length)

const muatSemuaPesanan = async () => {
  sedangMemuat.value = true
  try {
    semuaPesanan.value = await api.get('/orders')
  } catch (err) {
    console.error(err)
  } finally {
    sedangMemuat.value = false
  }
}

let pollingInterval = null
onMounted(() => {
  muatSemuaPesanan()
  muatMenu()
  pollingInterval = setInterval(muatSemuaPesanan, 8000)
})

const pilihTab = (key) => { tabAktif.value = key }

const formatWaktu = (tgl) =>
    new Date(tgl).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }) +
    ' · ' + new Date(tgl).toLocaleDateString('id-ID', { day: 'numeric', month: 'short' })

const labelStatus = (s) => ({ MENUNGGU: 'Menunggu', DIPROSES: 'Diproses', SELESAI: 'Selesai', DIBATALKAN: 'Dibatalkan' }[s] || s)
const badgeClass = (s) => ({ MENUNGGU: 'badge-menunggu', DIPROSES: 'badge-diproses', SELESAI: 'badge-selesai', DIBATALKAN: 'badge-batal' }[s] || '')

const ubahStatus = async (order, statusBaru) => {
  sedangProsesId.value = order.id
  try {
    await api.patch(`/orders/${order.id}/status`, { status: statusBaru })
    await muatSemuaPesanan()
  } catch (err) {
    alert('Gagal mengubah status: ' + err.message)
  } finally {
    sedangProsesId.value = null
  }
}

const bayarTunai = async (order) => {
  sedangProsesId.value = order.id
  try {
    await api.post(`/orders/${order.id}/bayar-tunai`)
    await muatSemuaPesanan()
  } catch (err) {
    alert('Gagal menandai lunas: ' + err.message)
  } finally {
    sedangProsesId.value = null
  }
}
// ---- Modal pembayaran (tunai + midtrans) ----
const orderModalAktif = ref(null)
const modalTab = ref('tunai')
const uangDiterima = ref(0)
const sedangProsesBayar = ref(false)
const errorBayar = ref('')
const statusMenungguMidtrans = ref(false)
let pollingMidtransInterval = null

const kembalian = computed(() => {
  if (!orderModalAktif.value) return 0
  return uangDiterima.value - orderModalAktif.value.total
})

const formatRp = (n) => 'Rp ' + n.toLocaleString('id-ID')

const nominalCepat = (total) => {
  const bulat = Math.ceil(total / 5000) * 5000
  const opsi = new Set([total, bulat, bulat + 5000, bulat + 10000, bulat + 20000])
  return [...opsi].sort((a, b) => a - b).slice(0, 4)
}

const bukaModalBayar = (order) => {
  orderModalAktif.value = order
  modalTab.value = 'tunai'
  uangDiterima.value = order.total
  errorBayar.value = ''
  statusMenungguMidtrans.value = false
}

const tutupModalBayar = () => {
  if (pollingMidtransInterval) clearInterval(pollingMidtransInterval)
  orderModalAktif.value = null
  statusMenungguMidtrans.value = false
}

const konfirmasiBayarTunai = async () => {
  if (kembalian.value < 0) return
  errorBayar.value = ''
  sedangProsesBayar.value = true
  try {
    await api.post(`/orders/${orderModalAktif.value.id}/bayar-tunai`)
    await muatSemuaPesanan()
    tutupModalBayar()
  } catch (err) {
    errorBayar.value = err.message || 'Gagal menandai lunas.'
  } finally {
    sedangProsesBayar.value = false
  }
}

const bayarMidtransKasir = async (order) => {
  errorBayar.value = ''
  sedangProsesBayar.value = true
  try {
    const hasil = await api.post(`/payment/midtrans/create/${order.id}`)
    if (!hasil.redirect_url) {
      errorBayar.value = 'Midtrans belum terkonfigurasi dengan benar.'
      return
    }
    window.open(hasil.redirect_url, '_blank', 'noopener')
    statusMenungguMidtrans.value = true

    pollingMidtransInterval = setInterval(async () => {
      try {
        const status = await api.get(`/payment/status/${order.id}`)
        if (status.statusPembayaran === 'PAID') {
          clearInterval(pollingMidtransInterval)
          await muatSemuaPesanan()
          tutupModalBayar()
        }
      } catch { /* abaikan error polling */ }
    }, 3000)
  } catch (err) {
    errorBayar.value = err.message || 'Gagal membuka pembayaran Midtrans.'
  } finally {
    sedangProsesBayar.value = false
  }
}
// ---- Pesanan baru (walk-in POS) ----
const daftarMenuPos = ref([])
const filterKategori = ref('semua')
const orderBaru = ref([])
const pesanErrorBaru = ref('')
const sedangBuatPesanan = ref(false)

const muatMenu = async () => {
  try {
    const data = await api.get('/menus')
    daftarMenuPos.value = data.map((m) => ({ ...m, kategori: (m.kategori || 'MAKANAN').toLowerCase() }))
  } catch (err) {
    console.error(err)
  }
}

const menuTersaring = computed(() =>
    filterKategori.value === 'semua'
        ? daftarMenuPos.value
        : daftarMenuPos.value.filter((m) => m.kategori === filterKategori.value)
)

const tambahKeOrderBaru = (menu) => {
  const existing = orderBaru.value.find((i) => i.id === menu.id)
  if (existing) existing.qty++
  else orderBaru.value.push({ id: menu.id, nama: menu.nama, harga: menu.harga, qty: 1 })
}

const kurangiOrderBaru = (id) => {
  const existing = orderBaru.value.find((i) => i.id === id)
  if (!existing) return
  existing.qty--
  if (existing.qty <= 0) {
    orderBaru.value = orderBaru.value.filter((i) => i.id !== id)
  }
}

const totalOrderBaru = computed(() =>
    orderBaru.value.reduce((sum, i) => sum + i.qty * i.harga, 0) + (orderBaru.value.length ? 3000 : 0)
)

const buatPesananBaru = async () => {
  pesanErrorBaru.value = ''
  sedangBuatPesanan.value = true
  try {
    const order = await api.post('/orders', {
      items: orderBaru.value.map((i) => ({ menuId: i.id, qty: i.qty }))
    })
    // Kasir langsung klaim & bisa proses bayar dari tab Antrian
    await api.patch(`/orders/${order.id}/status`, { status: 'DIPROSES' })
    orderBaru.value = []
    await muatSemuaPesanan()
    tabAktif.value = 'antrian'
    if (order.nomorAntrian) {
      alert('Pesanan dibuat. Nomor antrian: #' + order.nomorAntrian)
    }
  } catch (err) {
    pesanErrorBaru.value = err.message || 'Gagal membuat pesanan.'
  } finally {
    sedangBuatPesanan.value = false
  }
}

const handleLogout = () => {
  if (pollingInterval) clearInterval(pollingInterval)
  if (pollingMidtransInterval) clearInterval(pollingMidtransInterval)
  auth.logout()
  router.push('/kasir/login')
}
</script>

<style scoped>
.kasir-shell {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --cream: #fffbf5;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --sage: #15803d;
  --border: #f1e4d6;
}

.kasir-shell {
  display: flex;
  min-height: 100vh;
  background: var(--cream);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.sidebar {
  width: 230px;
  background: var(--ink);
  color: white;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.6rem 1.5rem;
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.1rem;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 1rem;
}

.brand-text em {
  font-style: normal;
  color: #fb923c;
  font-weight: 400;
  font-size: 0.85rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  flex-grow: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.75);
  padding: 0.75rem 0.8rem;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  position: relative;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.nav-item.active {
  background: var(--spice);
  color: white;
}

.nav-badge {
  margin-left: auto;
  background: #dc2626;
  min-width: 18px;
  height: 18px;
  border-radius: 999px;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.kasir-info {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  padding: 0.5rem 0.8rem;
}

.logout-btn {
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  padding: 0.7rem;
  border-radius: 10px;
  font-size: 0.85rem;
  cursor: pointer;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.kasir-content {
  flex-grow: 1;
  padding: 2rem 2.5rem 3rem;
  max-width: 1200px;
  overflow-x: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.content-header h1 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.7rem;
  margin: 0;
  color: var(--ink);
}

.subtitle {
  color: var(--ink-soft);
  font-size: 0.88rem;
  margin: 0.3rem 0 0;
}

.btn-refresh {
  background: white;
  border: 1px solid var(--border);
  padding: 0.55rem 1rem;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--ink-soft);
}

.btn-refresh:hover {
  background: var(--peach);
}

.loading-info, .empty-info {
  color: var(--ink-soft);
  font-size: 0.88rem;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.order-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 1.1rem 1.3rem;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.7rem;
}

.order-number {
  font-weight: 700;
  margin: 0;
  font-size: 0.92rem;
}

.antrian-chip {
  display: inline-block;
  background: var(--spice, #c2410c);
  color: white;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 0.1rem 0.5rem;
  border-radius: 999px;
  margin-right: 0.4rem;
  vertical-align: middle;
}

.order-meta {
  font-size: 0.76rem;
  color: var(--ink-soft);
  margin: 0.2rem 0 0;
}

.badge {
  padding: 0.28rem 0.65rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  white-space: nowrap;
}

.badge-menunggu { background: #fef3c7; color: #b45309; }
.badge-diproses { background: #dbeafe; color: #1d4ed8; }
.badge-selesai { background: #dcfce7; color: var(--sage); }
.badge-batal { background: #fee2e2; color: #b91c1c; }

.order-items-mini {
  font-size: 0.82rem;
  color: var(--ink-soft);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  padding: 0.6rem 0;
  margin-bottom: 0.7rem;
}

.order-items-mini p {
  margin: 0.15rem 0;
}

.order-card-footer {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.total-label {
  font-size: 0.75rem;
  color: var(--ink-soft);
  margin-right: 0.4rem;
}

.total-value {
  font-weight: 700;
  color: var(--spice-dark);
  margin-right: 0.6rem;
}

.pay-badge {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
}

.pay-badge.paid { background: #dcfce7; color: var(--sage); }
.pay-badge.unpaid { background: #fee2e2; color: #b91c1c; }

.order-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.btn-small {
  border: none;
  padding: 0.45rem 0.7rem;
  border-radius: 8px;
  font-size: 0.76rem;
  font-weight: 700;
  cursor: pointer;
}

.btn-small:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-bayar { background: var(--ink); color: white; }
.btn-proses { background: #dbeafe; color: #1d4ed8; }
.btn-selesai { background: #dcfce7; color: var(--sage); }
.btn-batal { background: #fee2e2; color: #b91c1c; }

/* POS layout */
.pos-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 1.5rem;
  align-items: start;
}

.pos-kategori-filter {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.pos-kategori-filter button {
  background: white;
  border: 1px solid var(--border);
  padding: 0.5rem 1rem;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  color: var(--ink-soft);
}

.pos-kategori-filter button.active {
  background: var(--spice);
  color: white;
  border-color: var(--spice);
}

.pos-menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.8rem;
}

.pos-menu-item {
  background: white;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 1rem 0.8rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  text-align: center;
  transition: all 0.15s;
}

.pos-menu-item:hover {
  border-color: var(--spice);
  background: var(--peach);
}

.pos-menu-icon {
  font-size: 1.8rem;
}

.pos-menu-nama {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--ink);
}

.pos-menu-harga {
  font-size: 0.78rem;
  color: var(--spice-dark);
  font-weight: 600;
}

.pos-cart {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 1.3rem;
  position: sticky;
  top: 1.5rem;
}

.pos-cart h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.1rem;
  margin: 0 0 1rem;
}

.pos-cart-empty {
  color: var(--ink-soft);
  font-size: 0.85rem;
  text-align: center;
  padding: 1.5rem 0;
}

.pos-cart-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--border);
}

.pos-cart-nama {
  font-size: 0.85rem;
  font-weight: 600;
  margin: 0;
}

.pos-cart-harga {
  font-size: 0.76rem;
  color: var(--ink-soft);
  margin: 0.15rem 0 0;
}

.pos-stepper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--peach);
  border-radius: 8px;
  padding: 0.2rem 0.4rem;
}

.pos-stepper button {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: none;
  background: white;
  color: var(--spice-dark);
  font-weight: 700;
  cursor: pointer;
}

.pos-cart-total {
  display: flex;
  justify-content: space-between;
  font-weight: 700;
  padding: 1rem 0;
  font-size: 1rem;
}

.btn-buat-pesanan {
  width: 100%;
  background: var(--spice);
  color: white;
  border: none;
  padding: 0.9rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

.btn-buat-pesanan:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  font-size: 0.82rem;
  margin-top: 0.6rem;
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
  font-size: 0.85rem;
}

thead {
  background: #faf6ef;
}

th {
  text-align: left;
  padding: 0.8rem 1rem;
  color: var(--ink-soft);
  font-weight: 600;
  font-size: 0.75rem;
  text-transform: uppercase;
}

td {
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--border);
}

@media (max-width: 900px) {
  .pos-layout {
    grid-template-columns: 1fr;
  }
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 25, 21, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-bayar {
  background: white;
  border-radius: 18px;
  padding: 1.5rem;
  width: 380px;
  max-width: 90vw;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.modal-header h3 {
  font-family: 'Fraunces', Georgia, serif;
  margin: 0;
  font-size: 1.15rem;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: var(--ink-soft);
  width: 30px;
  height: 30px;
  border-radius: 8px;
}

.modal-close:hover {
  background: var(--peach);
}

.modal-total {
  display: flex;
  justify-content: space-between;
  background: var(--peach);
  padding: 0.8rem 1rem;
  border-radius: 10px;
  margin-bottom: 1rem;
  font-weight: 600;
}

.modal-total-value {
  color: var(--spice-dark);
}

.modal-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.modal-tabs button {
  flex: 1;
  background: #faf6ef;
  border: 1px solid var(--border);
  padding: 0.6rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  color: var(--ink-soft);
}

.modal-tabs button.active {
  background: var(--spice);
  border-color: var(--spice);
  color: white;
}

.modal-label {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--ink-soft);
  margin-bottom: 0.4rem;
}

.modal-input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  border: 1px solid var(--border);
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 0.8rem;
}

.modal-input:focus {
  outline: none;
  border-color: var(--spice);
}

.quick-cash {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.quick-cash button {
  background: #faf6ef;
  border: 1px solid var(--border);
  padding: 0.4rem 0.7rem;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  color: var(--ink);
}

.quick-cash button:hover {
  border-color: var(--spice);
}

.modal-kembalian {
  display: flex;
  justify-content: space-between;
  padding: 0.8rem 1rem;
  background: #dcfce7;
  color: var(--sage);
  border-radius: 10px;
  font-weight: 700;
  margin-bottom: 1rem;
}

.modal-kembalian.negatif {
  background: #fee2e2;
  color: #b91c1c;
}

.btn-konfirmasi-bayar {
  width: 100%;
  background: var(--spice);
  color: white;
  border: none;
  padding: 0.85rem;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

.btn-konfirmasi-bayar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-midtrans-desc {
  font-size: 0.85rem;
  color: var(--ink-soft);
  margin-bottom: 1rem;
}

.modal-midtrans-waiting {
  background: #fef3c7;
  color: #b45309;
  padding: 0.7rem 1rem;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
</style>