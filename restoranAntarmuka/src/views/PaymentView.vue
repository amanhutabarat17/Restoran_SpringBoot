<template>
  <div class="payment-page">
    <div class="payment-card">
      <div v-if="sedangMemuat" class="loading">Memuat pesanan...</div>

      <template v-else-if="order">
        <div class="page-header">
          <span class="eyebrow">Pesanan {{ order.orderNumber }}</span>
          <h2>Selesaikan Pembayaran</h2>
          <p v-if="order.noMeja">📍 Meja {{ order.noMeja }}</p>
        </div>

        <div v-if="order.nomorAntrian" class="antrian-box">
          <span class="antrian-label">Nomor Antrian Anda</span>
          <span class="antrian-angka">{{ order.nomorAntrian }}</span>
          <span class="antrian-hint">Tunjukkan nomor ini saat mengambil pesanan</span>
        </div>

        <div class="ringkasan">
          <div class="item-baris" v-for="item in order.items" :key="item.id">
            <img v-if="item.menu?.gambarUrl" :src="urlGambar(item.menu.gambarUrl)" :alt="item.namaMenu" class="item-foto" />
            <span v-else class="item-icon-fallback">{{ item.menu?.icon || '🍽️' }}</span>
            <span class="item-nama">{{ item.qty }}x {{ item.namaMenu }}</span>
            <span class="item-subtotal">Rp {{ item.subtotal.toLocaleString('id-ID') }}</span>
          </div>
          <div class="divider"></div>
          <div class="baris"><span>Subtotal</span><span>Rp {{ order.subtotal.toLocaleString('id-ID') }}</span></div>
          <div class="baris" v-if="order.diskon > 0"><span>Diskon</span><span>-Rp {{ order.diskon.toLocaleString('id-ID') }}</span></div>
          <div class="baris"><span>Biaya layanan</span><span>Rp {{ order.biayaLayanan.toLocaleString('id-ID') }}</span></div>
          <div class="baris total"><span>Total Bayar</span><span>Rp {{ order.total.toLocaleString('id-ID') }}</span></div>
        </div>

        <div v-if="order.statusPembayaran === 'PAID'" class="status-lunas">
          ✅ Pembayaran berhasil! Pesanan Anda sedang diproses dapur.
        </div>

        <template v-else>
          <p class="pilih-label">Pilih metode pembayaran:</p>

          <div class="metode-list">
            <button class="metode-btn" :disabled="sedangProses" @click="bayarNonTunai">
              <span class="metode-icon">📱</span>
              <div>
                <p class="metode-nama">QRIS / Non-Tunai (Midtrans)</p>
                <p class="metode-desc">E-wallet, QRIS, kartu, atau transfer virtual account</p>
              </div>
            </button>

            <button class="metode-btn" :disabled="sedangProses" @click="bayarDiKasir">
              <span class="metode-icon">💵</span>
              <div>
                <p class="metode-nama">Bayar di Kasir</p>
                <p class="metode-desc">Tunjukkan nomor pesanan ini ke kasir untuk membayar tunai</p>
              </div>
            </button>
          </div>

          <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>
        </template>

        <RouterLink to="/" class="btn-back">← Kembali ke Menu</RouterLink>
      </template>

      <div v-else class="loading">{{ muatError || 'Pesanan tidak ditemukan.' }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { api, urlGambar } from '../services/api'

const route = useRoute()
const order = ref(null)
const sedangMemuat = ref(true)
const sedangProses = ref(false)
const pesanError = ref('')
const muatError = ref('')
let pollingInterval = null

const muatOrder = async () => {
  order.value = await api.get(`/orders/${route.params.id}`)
}

onMounted(async () => {
  try {
    await muatOrder()
  } catch (err) {
    muatError.value = err.message || 'Gagal memuat pesanan.'
  } finally {
    sedangMemuat.value = false
  }

  // Polling status pembayaran tiap 4 detik (berguna setelah bayar via Midtrans Snap)
  pollingInterval = setInterval(async () => {
    if (order.value && order.value.statusPembayaran !== 'PAID') {
      try { await muatOrder() } catch { /* abaikan error polling */ }
    }
  }, 4000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
})

const bayarNonTunai = async () => {
  pesanError.value = ''
  sedangProses.value = true
  try {
    const hasil = await api.post(`/payment/midtrans/create/${order.value.id}`)
    if (hasil.redirect_url && hasil.redirect_url !== 'undefined') {
      window.location.href = hasil.redirect_url
    } else {
      pesanError.value = 'Midtrans belum dikonfigurasi (server key masih placeholder). Hubungi admin untuk mengisi key asli di application.properties.'
    }
  } catch (err) {
    pesanError.value = err.message || 'Gagal memulai pembayaran non-tunai.'
  } finally {
    sedangProses.value = false
  }
}

const bayarDiKasir = () => {
  pesanError.value = 'Silakan tunjukkan nomor pesanan "' + order.value.orderNumber + '" ke kasir untuk membayar tunai.'
}
</script>

<style scoped>
:root {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --sage: #15803d;
  --border: #f1e4d6;
}

.payment-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.payment-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 2rem;
}

.loading {
  text-align: center;
  color: var(--ink-soft);
  padding: 2rem 0;
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
  font-size: 1.6rem;
  margin: 0 0 0.4rem;
  color: var(--ink);
}

.page-header p {
  color: var(--ink-soft);
  font-size: 0.9rem;
}

.antrian-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.15rem;
  background: var(--peach);
  border: 1px dashed var(--spice);
  border-radius: 14px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.antrian-label {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--spice-dark);
}

.antrian-angka {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 2.6rem;
  font-weight: 700;
  line-height: 1.15;
  color: var(--ink);
}

.antrian-hint {
  font-size: 0.78rem;
  color: var(--ink-soft);
}

.ringkasan {
  background: #faf6ef;
  border-radius: 14px;
  padding: 1.2rem;
  margin-bottom: 1.5rem;
}

.baris {
  display: flex;
  justify-content: space-between;
  font-size: 0.88rem;
  color: var(--ink-soft);
  margin-bottom: 0.5rem;
}

.item-baris {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  font-size: 0.88rem;
  color: var(--ink-soft);
  margin-bottom: 0.7rem;
}

.item-foto {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.item-icon-fallback {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: var(--peach);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.item-nama {
  flex-grow: 1;
}

.item-subtotal {
  color: var(--ink);
  font-weight: 600;
  white-space: nowrap;
}

.baris.total {
  font-weight: 700;
  color: var(--ink);
  font-size: 1rem;
}

.divider {
  height: 1px;
  background: var(--border);
  margin: 0.6rem 0;
}

.status-lunas {
  background: #dcfce7;
  color: var(--sage);
  padding: 1rem;
  border-radius: 12px;
  text-align: center;
  font-weight: 600;
}

.pilih-label {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.8rem;
  color: var(--ink);
}

.metode-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.metode-btn {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  text-align: left;
  background: white;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.metode-btn:hover:not(:disabled) {
  border-color: var(--spice);
  background: var(--peach);
}

.metode-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.metode-icon {
  font-size: 1.6rem;
}

.metode-nama {
  font-weight: 700;
  margin: 0 0 0.2rem;
  color: var(--ink);
}

.metode-desc {
  font-size: 0.8rem;
  color: var(--ink-soft);
  margin: 0;
}

.error-msg {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.7rem 0.9rem;
  font-size: 0.85rem;
  margin-top: 1rem;
}

.btn-back {
  display: block;
  text-align: center;
  margin-top: 1.5rem;
  color: var(--ink-soft);
  text-decoration: none;
  font-size: 0.85rem;
}

.btn-back:hover {
  color: var(--spice);
}
</style>