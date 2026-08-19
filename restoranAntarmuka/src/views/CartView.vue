<template>
  <div class="cart-page">
    <div class="page-header">
      <span class="eyebrow">Pesanan Anda</span>
      <h2>Keranjang Belanja</h2>
      <p v-if="keranjang.length">{{ totalItem }} item siap dipesan</p>
      <p v-else>Belum ada apapun di sini</p>
    </div>

    <!-- Empty state -->
    <div v-if="keranjang.length === 0" class="empty-cart">
      <span class="empty-icon">🛒</span>
      <h3>Keranjang masih kosong</h3>
      <p>Yuk, lihat-lihat menu dan tambahkan hidangan favorit Anda.</p>
      <RouterLink to="/" class="btn-browse">Lihat Menu</RouterLink>
    </div>

    <!-- Cart contents -->
    <div v-else class="cart-layout">
      <div class="cart-list">
        <div v-for="item in keranjang" :key="item.id" class="cart-row">
          <img v-if="item.gambarUrl" :src="urlGambar(item.gambarUrl)" :alt="item.nama" class="cart-row-photo" />
          <span v-else class="cart-row-icon">{{ item.icon }}</span>

          <div class="cart-row-info">
            <p class="cart-row-name">{{ item.nama }}</p>
            <p class="cart-row-price">Rp {{ item.harga.toLocaleString('id-ID') }} / porsi</p>
          </div>

          <div class="stepper">
            <button class="stepper-btn" @click="kurangiJumlah(item.id)">−</button>
            <span class="stepper-count">{{ item.qty }}</span>
            <button class="stepper-btn" @click="tambahKeKeranjang(item)">+</button>
          </div>

          <p class="cart-row-subtotal">
            Rp {{ (item.harga * item.qty).toLocaleString('id-ID') }}
          </p>

          <button class="btn-remove" @click="hapusItem(item.id)" aria-label="Hapus item">
            ✕
          </button>
        </div>
      </div>

      <!-- Order summary -->
      <div class="summary-card">
        <h3>Ringkasan Pesanan</h3>

        <div v-if="noMeja" class="meja-info">📍 Meja {{ noMeja }}</div>

        <label class="promo-field">
          <span>Kode Promo (opsional)</span>
          <input v-model="kodePromo" type="text" placeholder="cth. SIANG15" style="text-transform: uppercase" />
        </label>

        <div class="summary-row">
          <span>Subtotal ({{ totalItem }} item)</span>
          <span>Rp {{ totalHarga.toLocaleString('id-ID') }}</span>
        </div>
        <div class="summary-row">
          <span>Biaya layanan</span>
          <span>Rp {{ biayaLayanan.toLocaleString('id-ID') }}</span>
        </div>

        <div class="summary-divider"></div>

        <div class="summary-row total">
          <span>Total</span>
          <span>Rp {{ totalAkhir.toLocaleString('id-ID') }}</span>
        </div>

        <p v-if="pesanError" class="error-msg">{{ pesanError }}</p>

        <button class="btn-checkout" :disabled="sedangProses" @click="checkout">
          {{ sedangProses ? 'Memproses...' : 'Pesan Sekarang' }}
        </button>
        <RouterLink to="/" class="btn-continue">+ Tambah menu lain</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '../composables/useCart'
import { useMeja } from '../composables/useMeja'
import { api, urlGambar } from '../services/api'

const router = useRouter()
const { keranjang, totalItem, totalHarga, tambahKeKeranjang, kurangiJumlah, hapusItem, kosongkanKeranjang } = useCart()
const { noMeja, setMeja } = useMeja()

// NB: nilai ini hanya untuk PERKIRAAN di ringkasan sebelum order dibuat.
// Angka final yang benar-benar dipakai selalu dihitung backend dari app.biaya-layanan
// (lihat application.properties) saat POST /orders — disamakan ke Rp 1.000 di sini
// supaya tidak beda dengan yang nanti muncul di halaman pembayaran.
const biayaLayanan = computed(() => (keranjang.length ? 1000 : 0))
const totalAkhir = computed(() => totalHarga.value + biayaLayanan.value)

const kodePromo = ref('')
const sedangProses = ref(false)
const pesanError = ref('')

const checkout = async () => {
  pesanError.value = ''
  if (keranjang.length === 0) return

  sedangProses.value = true
  try {
    const order = await api.post('/orders', {
      noMeja: noMeja.value || null,
      kodePromo: kodePromo.value || null,
      items: keranjang.map((item) => ({ menuId: item.id, qty: item.qty }))
    })
    kosongkanKeranjang()
    setMeja('') // sudah tercatat di pesanan, boleh dibersihkan
    router.push(`/pembayaran/${order.id}`)
  } catch (err) {
    pesanError.value = err.message || 'Gagal membuat pesanan. Coba lagi.'
  } finally {
    sedangProses.value = false
  }
}
</script>

<style scoped>
.cart-page {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --cream: #fffbf5;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --gold: #d97706;
  --sage: #15803d;
  --border: #f1e4d6;
  --shadow: 0 4px 20px rgba(28, 25, 21, 0.06);
}

.cart-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--ink);
}

.page-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.eyebrow {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--spice);
  margin-bottom: 0.75rem;
}

.page-header h2 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 2.4rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
}

.page-header p {
  color: var(--ink-soft);
  font-size: 1rem;
}

/* Empty state */
.empty-cart {
  text-align: center;
  padding: 4rem 1rem;
  background: white;
  border: 1px solid var(--border);
  border-radius: 18px;
}

.empty-icon {
  font-size: 3.5rem;
  display: block;
  margin-bottom: 1rem;
}

.empty-cart h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.4rem;
  margin: 0 0 0.5rem;
}

.empty-cart p {
  color: var(--ink-soft);
  margin-bottom: 1.5rem;
}

.btn-browse {
  display: inline-block;
  background: var(--spice);
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.6rem;
  border-radius: 999px;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-browse:hover {
  background: var(--spice-dark);
}

/* Layout */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 2rem;
  align-items: start;
}

.cart-list {
  background: white;
  border: 1px solid var(--border);
  border-radius: 18px;
  overflow: hidden;
}

.cart-row {
  display: grid;
  grid-template-columns: 48px 1fr auto auto auto;
  align-items: center;
  gap: 1rem;
  padding: 1.1rem 1.4rem;
  border-bottom: 1px solid var(--border);
}

.cart-row:last-child {
  border-bottom: none;
}

.cart-row-icon {
  font-size: 2.1rem;
  text-align: center;
}

.cart-row-photo {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
}

.cart-row-name {
  font-weight: 600;
  margin: 0 0 0.2rem;
}

.cart-row-price {
  color: var(--ink-soft);
  font-size: 0.82rem;
  margin: 0;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  background: var(--peach);
  border-radius: 10px;
  padding: 0.3rem 0.5rem;
}

.stepper-btn {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: none;
  background: white;
  color: var(--spice-dark);
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.stepper-btn:hover {
  background: var(--spice);
  color: white;
}

.stepper-count {
  font-weight: 700;
  min-width: 1.2rem;
  text-align: center;
}

.cart-row-subtotal {
  font-weight: 700;
  color: var(--spice-dark);
  white-space: nowrap;
}

.btn-remove {
  background: none;
  border: none;
  color: var(--ink-soft);
  font-size: 0.9rem;
  cursor: pointer;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-remove:hover {
  background: #fee2e2;
  color: #b91c1c;
}

/* Summary card */
.summary-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 1.6rem;
  position: sticky;
  top: 1.5rem;
}

.summary-card h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.2rem;
  margin: 0 0 1.2rem;
}

.meja-info {
  background: var(--peach);
  color: var(--spice-dark);
  padding: 0.5rem 0.8rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.promo-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1.2rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--ink);
}

.promo-field input {
  padding: 0.6rem 0.8rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-size: 0.9rem;
  font-weight: 400;
  outline: none;
}

.promo-field input:focus {
  border-color: var(--spice);
}

.error-msg {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  font-size: 0.85rem;
  margin: 0 0 1rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--ink-soft);
  margin-bottom: 0.7rem;
}

.summary-row.total {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--ink);
}

.summary-divider {
  height: 1px;
  background: var(--border);
  margin: 1rem 0;
}

.btn-checkout {
  width: 100%;
  background: var(--spice);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: background 0.2s;
}

.btn-checkout:hover {
  background: var(--spice-dark);
}

.btn-checkout:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-continue {
  display: block;
  text-align: center;
  margin-top: 0.9rem;
  color: var(--spice);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.88rem;
}

.btn-continue:hover {
  text-decoration: underline;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  background: var(--ink);
  color: white;
  padding: 0.8rem 1.4rem;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-weight: 600;
  font-size: 0.9rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
  z-index: 60;
}

.toast-icon {
  background: var(--sage);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 12px);
}

/* Responsive */
@media (max-width: 720px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
  .cart-row {
    grid-template-columns: 40px 1fr;
    row-gap: 0.6rem;
  }
  .cart-row-subtotal {
    grid-column: 2;
  }
  .stepper {
    grid-column: 2;
  }
  .btn-remove {
    justify-self: end;
  }
}
</style>