<template>
  <div class="menu-wrapper">
    <!-- Header -->
    <div class="page-header">
      <span class="eyebrow">Menu Hari Ini</span>
      <h2>Eksplorasi Rasa</h2>
      <p>Temukan hidangan favorit Anda, diracik segar setiap hari</p>
    </div>

    <!-- Controls: search + category filter -->
    <div class="controls">
      <div class="search-box">
        <svg class="icon-search" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
            v-model="kataKunci"
            type="text"
            placeholder="Cari nasi goreng, kopi, dimsum..."
        />
      </div>

      <div class="category-chips">
        <button
            v-for="kat in kategoriList"
            :key="kat.value"
            class="chip"
            :class="{ active: kategoriAktif === kat.value }"
            @click="kategoriAktif = kat.value"
        >
          {{ kat.label }}
        </button>
      </div>
    </div>

    <!-- Loading state: skeleton grid, terasa lebih profesional daripada teks polos -->
    <div v-if="sedangMemuat" class="menu-grid">
      <div v-for="n in 6" :key="n" class="menu-card skeleton-card">
        <div class="skeleton-block skeleton-image"></div>
        <div class="menu-content">
          <div class="skeleton-block skeleton-line" style="width: 70%"></div>
          <div class="skeleton-block skeleton-line" style="width: 100%"></div>
          <div class="skeleton-block skeleton-line" style="width: 40%"></div>
        </div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="errorMemuat" class="empty-state">
      <span class="empty-icon">⚠️</span>
      <p>{{ errorMemuat }}</p>
      <button class="btn-reset" @click="ambilDataMenu">Coba lagi</button>
    </div>

    <!-- Menu grid -->
    <transition-group v-else name="fade-up" tag="div" class="menu-grid">
      <div
          v-for="menu in menuTersaring"
          :key="menu.id"
          class="menu-card"
      >
        <div class="menu-image">
          <img
              v-if="menu.gambarUrl"
              :src="urlGambar(menu.gambarUrl)"
              :alt="menu.nama"
              class="menu-photo"
              loading="lazy"
          />
          <span v-else class="emoji-icon">{{ menu.icon }}</span>
          <span v-if="menu.badge" class="badge">{{ menu.badge }}</span>
          <span v-if="menu.stok === 0" class="badge-habis">Stok Habis</span>
        </div>
        <div class="menu-content">
          <div class="menu-title-row">
            <h3>{{ menu.nama }}</h3>
            <span class="rating">★ {{ menu.rating.toFixed(1) }}</span>
          </div>
          <p class="desc">{{ menu.deskripsi }}</p>

          <div class="menu-footer">
            <span class="price">Rp {{ menu.harga.toLocaleString('id-ID') }}</span>

            <button
                v-if="!jumlahDiKeranjang(menu.id)"
                class="btn-add"
                :disabled="menu.stok === 0"
                @click="tambahKeKeranjang(menu)"
            >
              {{ menu.stok === 0 ? 'Stok Habis' : '+ Tambah' }}
            </button>

            <div v-else class="stepper">
              <button class="stepper-btn" @click="kurangiJumlah(menu.id)">−</button>
              <span class="stepper-count">{{ jumlahDiKeranjang(menu.id) }}</span>
              <button class="stepper-btn" @click="tambahKeKeranjang(menu)">+</button>
            </div>
          </div>
        </div>
      </div>
    </transition-group>

    <div v-if="!sedangMemuat && !errorMemuat && menuTersaring.length === 0" class="empty-state">
      <span class="empty-icon">🍽️</span>
      <p>Tidak ada menu yang cocok dengan "{{ kataKunci }}"</p>
      <button class="btn-reset" @click="resetPencarian">Reset pencarian</button>
    </div>

    <!-- Floating cart bubble -->
    <transition name="pop">
      <button
          v-if="totalItem > 0"
          class="cart-bubble"
          :class="{ bump: bumpCart }"
          @click="keranjangTerbuka = true"
      >
        <span class="cart-icon">🛒</span>
        <span class="cart-count">{{ totalItem }}</span>
        <span class="cart-total">Rp {{ totalHarga.toLocaleString('id-ID') }}</span>
      </button>
    </transition>

    <!-- Cart side panel -->
    <transition name="slide">
      <div v-if="keranjangTerbuka" class="cart-overlay" @click.self="keranjangTerbuka = false">
        <div class="cart-panel">
          <div class="cart-panel-header">
            <h3>Pesanan Anda</h3>
            <button class="btn-close" @click="keranjangTerbuka = false">✕</button>
          </div>

          <div v-if="keranjang.length === 0" class="cart-empty">
            <span class="empty-icon">🛒</span>
            <p>Keranjang masih kosong</p>
          </div>

          <div v-else class="cart-items">
            <div v-for="item in keranjang" :key="item.id" class="cart-item">
              <img v-if="item.gambarUrl" :src="urlGambar(item.gambarUrl)" :alt="item.nama" class="cart-item-photo" />
              <span v-else class="cart-item-icon">{{ item.icon }}</span>
              <div class="cart-item-info">
                <p class="cart-item-name">{{ item.nama }}</p>
                <p class="cart-item-price">Rp {{ item.harga.toLocaleString('id-ID') }}</p>
              </div>
              <div class="stepper small">
                <button class="stepper-btn" @click="kurangiJumlah(item.id)">−</button>
                <span class="stepper-count">{{ item.qty }}</span>
                <button class="stepper-btn" @click="tambahKeKeranjang(item)">+</button>
              </div>
            </div>
          </div>

          <div v-if="keranjang.length > 0" class="cart-panel-footer">
            <div class="cart-summary-row">
              <span>Subtotal</span>
              <span>Rp {{ totalHarga.toLocaleString('id-ID') }}</span>
            </div>
            <button class="btn-checkout" @click="checkout">
              Pesan Sekarang · Rp {{ totalHarga.toLocaleString('id-ID') }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Toast notification -->
    <transition name="toast">
      <div v-if="toast.tampil" class="toast">
        <span class="toast-icon">✓</span>
        <span>{{ toast.pesan }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCart } from '../composables/useCart'
import { useMeja } from '../composables/useMeja'
import { api, urlGambar } from '../services/api'

const {
  keranjang,
  totalItem,
  totalHarga,
  jumlahDiKeranjang,
  tambahKeKeranjang: tambahKeKeranjangCart,
  kurangiJumlah
} = useCart()

const route = useRoute()
const router = useRouter()
const { noMeja, bacaMejaDariUrl } = useMeja()

const daftarMenu = ref([])
const sedangMemuat = ref(true)
const errorMemuat = ref('')

const ambilDataMenu = async () => {
  sedangMemuat.value = true
  errorMemuat.value = ''
  try {
    const data = await api.get('/menus')
    daftarMenu.value = data.map((menu) => ({
      id: menu.id,
      nama: menu.nama,
      deskripsi: menu.deskripsi,
      harga: menu.harga,
      icon: menu.icon || '🍽️',
      gambarUrl: menu.gambarUrl || '',
      kategori: (menu.kategori || 'MAKANAN').toLowerCase(),
      rating: menu.rating || 0,
      badge: menu.badge || '',
      stok: menu.stok
    }))
  } catch (err) {
    errorMemuat.value = 'Tidak bisa memuat menu. Pastikan backend sudah berjalan di port 8080.'
    console.error(err)
  } finally {
    sedangMemuat.value = false
  }
}

onMounted(() => {
  bacaMejaDariUrl(route)
  ambilDataMenu()
})

const kategoriList = [
  { label: 'Semua', value: 'semua' },
  { label: 'Makanan', value: 'makanan' },
  { label: 'Minuman', value: 'minuman' }
]

const kataKunci = ref('')
const kategoriAktif = ref('semua')
const keranjangTerbuka = ref(false)
const bumpCart = ref(false)
const toast = ref({ tampil: false, pesan: '' })

const menuTersaring = computed(() => {
  return daftarMenu.value.filter((menu) => {
    const cocokKategori = kategoriAktif.value === 'semua' || menu.kategori === kategoriAktif.value
    const cocokKataKunci = menu.nama.toLowerCase().includes(kataKunci.value.toLowerCase())
    return cocokKategori && cocokKataKunci
  })
})

const tampilkanToast = (pesan) => {
  toast.value = { tampil: true, pesan }
  setTimeout(() => { toast.value.tampil = false }, 2000)
}

const tambahKeKeranjang = (menu) => {
  tambahKeKeranjangCart(menu)
  bumpCart.value = true
  setTimeout(() => { bumpCart.value = false }, 300)
  tampilkanToast(`${menu.nama} ditambahkan`)
}

const resetPencarian = () => {
  kataKunci.value = ''
  kategoriAktif.value = 'semua'
}

const checkout = () => {
  keranjangTerbuka.value = false
  router.push('/keranjang')
}
</script>

<style scoped>
.menu-wrapper {
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

.menu-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--ink);
  background: var(--cream);
}
.menu-wrapper {
  background:
      radial-gradient(ellipse 700px 500px at 90% -5%, rgba(249, 115, 22, 0.08) 0%, transparent 55%),
      radial-gradient(ellipse 600px 450px at 5% 15%, rgba(234, 179, 8, 0.05) 0%, transparent 55%),
      radial-gradient(ellipse 650px 500px at 100% 100%, rgba(34, 197, 94, 0.045) 0%, transparent 55%),
      #fdfdfb;
}
/* Header */
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
  font-family: 'Fraunces', Georgia, 'Times New Roman', serif;
  font-size: 3rem;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 0.5rem;
  letter-spacing: -0.01em;
}

.page-header p {
  color: var(--ink-soft);
  font-size: 1.05rem;
}

/* Controls */
.controls {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1 1 320px;
  max-width: 420px;
}

.category-chips {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.icon-search {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--ink-soft);
}

.search-box input {
  width: 100%;
  padding: 0.8rem 1rem 0.8rem 2.6rem;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: white;
  font-size: 0.95rem;
  color: var(--ink);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.search-box input:focus {
  border-color: var(--spice);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.12);
}

.search-box input::placeholder {
  color: #a8a29e;
}

.chip {
  padding: 0.5rem 1.2rem;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: white;
  color: var(--ink-soft);
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all 0.2s;
}

.chip:hover {
  border-color: var(--spice);
  color: var(--spice);
}

.chip.active {
  background: var(--spice);
  border-color: var(--spice);
  color: white;
}

/* Grid */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.75rem;
}

.menu-card {
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border);
}

.menu-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 14px 28px rgba(28, 25, 21, 0.1);
}

.menu-image {
  position: relative;
  height: 170px;
  background: linear-gradient(135deg, var(--peach) 0%, #fed7aa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.menu-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.menu-card:hover .menu-photo {
  transform: scale(1.06);
}

.emoji-icon {
  font-size: 4.5rem;
  filter: drop-shadow(0 10px 8px rgb(0 0 0 / 0.12));
}

.badge-habis {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(28, 25, 21, 0.85);
  color: white;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
}

/* Skeleton loading */
.skeleton-card {
  cursor: default;
}

.skeleton-card:hover {
  transform: none;
  box-shadow: var(--shadow);
}

.skeleton-block {
  background: linear-gradient(90deg, #f1e4d6 25%, #faf1e6 37%, #f1e4d6 63%);
  background-size: 400% 100%;
  animation: skeleton-shimmer 1.4s ease infinite;
  border-radius: 8px;
}

.skeleton-image {
  height: 170px;
  border-radius: 0;
}

.skeleton-line {
  height: 0.85rem;
  margin-bottom: 0.7rem;
}

@keyframes skeleton-shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}

.badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: var(--ink);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
}

.menu-content {
  padding: 1.4rem;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.menu-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.menu-content h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.15rem;
  color: var(--ink);
  font-weight: 600;
  margin: 0;
}

.rating {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--gold);
  white-space: nowrap;
}

.desc {
  color: var(--ink-soft);
  font-size: 0.88rem;
  line-height: 1.55;
  margin-bottom: 1.4rem;
  flex-grow: 1;
}

.menu-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  gap: 0.75rem;
}

.price {
  font-weight: 700;
  color: var(--spice-dark);
  font-size: 1.1rem;
}

.btn-add {
  background-color: var(--peach);
  color: var(--spice-dark);
  border: 1px solid #fed7aa;
  padding: 0.55rem 1.1rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-add:hover:not(:disabled) {
  background-color: var(--spice);
  color: white;
  border-color: var(--spice);
}

.btn-add:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* Stepper */
.stepper {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  background: var(--peach);
  border-radius: 10px;
  padding: 0.3rem 0.5rem;
}

.stepper.small {
  padding: 0.2rem 0.4rem;
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
  color: var(--ink);
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: var(--ink-soft);
}

.empty-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
}

.btn-reset {
  margin-top: 1rem;
  background: none;
  border: 1px solid var(--border);
  padding: 0.5rem 1.2rem;
  border-radius: 999px;
  color: var(--spice);
  font-weight: 600;
  cursor: pointer;
}

.btn-reset:hover {
  border-color: var(--spice);
  background: var(--peach);
}

/* Floating cart bubble */
.cart-bubble {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  background: var(--ink);
  color: white;
  border: none;
  border-radius: 999px;
  padding: 0.9rem 1.4rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(28, 25, 21, 0.3);
  transition: transform 0.2s;
  z-index: 40;
}

.cart-bubble:hover {
  transform: translateY(-3px);
}

.cart-bubble.bump {
  animation: bump 0.3s ease;
}

@keyframes bump {
  0% { transform: scale(1); }
  50% { transform: scale(1.08); }
  100% { transform: scale(1); }
}

.cart-icon {
  font-size: 1.1rem;
}

.cart-count {
  background: var(--spice);
  border-radius: 999px;
  min-width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.cart-total {
  font-size: 0.9rem;
}

/* Cart panel */
.cart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 25, 21, 0.45);
  display: flex;
  justify-content: flex-end;
  z-index: 50;
}

.cart-panel {
  width: min(400px, 100%);
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.15);
}

.cart-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border);
}

.cart-panel-header h3 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.4rem;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  color: var(--ink-soft);
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.btn-close:hover {
  background: var(--peach);
  color: var(--spice-dark);
}

.cart-empty {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--ink-soft);
}

.cart-items {
  flex-grow: 1;
  overflow-y: auto;
  padding: 1rem 1.5rem;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 0.9rem 0;
  border-bottom: 1px solid var(--border);
}

.cart-item-icon {
  font-size: 2rem;
}

.cart-item-photo {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.cart-item-info {
  flex-grow: 1;
}

.cart-item-name {
  font-weight: 600;
  margin: 0 0 0.15rem;
  font-size: 0.92rem;
}

.cart-item-price {
  color: var(--ink-soft);
  font-size: 0.85rem;
  margin: 0;
}

.cart-panel-footer {
  padding: 1.5rem;
  border-top: 1px solid var(--border);
}

.cart-summary-row {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--ink-soft);
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
  transition: background 0.2s;
}

.btn-checkout:hover {
  background: var(--spice-dark);
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

/* Transitions */
.fade-up-enter-active {
  transition: all 0.4s ease;
}
.fade-up-enter-from {
  opacity: 0;
  transform: translateY(16px);
}

.pop-enter-active,
.pop-leave-active {
  transition: all 0.25s ease;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.slide-enter-active,
.slide-leave-active {
  transition: opacity 0.25s ease;
}
.slide-enter-active .cart-panel,
.slide-leave-active .cart-panel {
  transition: transform 0.3s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
}
.slide-enter-from .cart-panel,
.slide-leave-to .cart-panel {
  transform: translateX(100%);
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
@media (max-width: 640px) {
  .page-header h2 {
    font-size: 2.1rem;
  }
  .cart-total {
    display: none;
  }
}
</style>c