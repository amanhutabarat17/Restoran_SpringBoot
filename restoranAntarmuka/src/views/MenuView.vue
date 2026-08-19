<template>
  <div class="menu-wrapper">
    <!-- Header -->
    <div class="page-header">
      <span class="eyebrow">The Culinary Experience</span>
      <h2>Eksplorasi Rasa</h2>
      <div class="header-divider"></div>
      <p>Temukan mahakarya hidangan kami, diracik segar setiap hari dengan bahan premium.</p>
    </div>

    <!-- Controls: search + category filter -->
    <div class="controls">
      <div class="search-box">
        <svg class="icon-search" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="7" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
            v-model="kataKunci"
            type="text"
            placeholder="Cari menu favorit Anda..."
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

    <!-- Loading state: Skeleton grid elegan -->
    <div v-if="sedangMemuat" class="menu-grid">
      <div v-for="n in 6" :key="n" class="menu-card skeleton-card">
        <div class="skeleton-block skeleton-image"></div>
        <div class="menu-content">
          <div class="skeleton-block skeleton-line" style="width: 60%; height: 1.2rem; margin-bottom: 1rem;"></div>
          <div class="skeleton-block skeleton-line" style="width: 100%"></div>
          <div class="skeleton-block skeleton-line" style="width: 80%"></div>
          <div class="skeleton-block skeleton-line" style="width: 40%; margin-top: 1.5rem;"></div>
        </div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="errorMemuat" class="empty-state">
      <svg class="empty-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <p>{{ errorMemuat }}</p>
      <button class="btn-outline" @click="ambilDataMenu">Coba Lagi</button>
    </div>

    <!-- Menu grid -->
    <transition-group v-else name="fade-up" tag="div" class="menu-grid">
      <div
          v-for="menu in menuTersaring"
          :key="menu.id"
          class="menu-card"
      >
        <div class="menu-image">
          <!-- Tambahkan elemen ini untuk efek blur background -->
          <div
              v-if="menu.gambarUrl"
              class="image-backdrop"
              :style="{ backgroundImage: `url(${urlGambar(menu.gambarUrl)})` }"
          ></div>

          <!-- Gambar utamanya tetap di sini -->
          <img
              v-if="menu.gambarUrl"
              :src="urlGambar(menu.gambarUrl)"
              :alt="menu.nama"
              class="menu-photo"
              loading="lazy"
          />
          <div v-else class="placeholder-icon">
            <span>{{ menu.icon }}</span>
          </div>
          <span v-if="menu.badge" class="badge-gold">{{ menu.badge }}</span>
          <div v-if="menu.stok === 0" class="overlay-habis">
            <span>Sold Out</span>
          </div>
        </div>

        <div class="menu-content">
          <div class="menu-header-row">
            <h3>{{ menu.nama }}</h3>
            <span class="rating" v-if="menu.rating > 0">
              <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
              </svg>
              {{ menu.rating.toFixed(1) }}
            </span>
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
              {{ menu.stok === 0 ? 'Habis' : 'Tambah' }}
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
      <svg class="empty-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
        <circle cx="11" cy="11" r="8"></circle>
        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
      </svg>
      <p>Tidak ada hidangan yang sesuai dengan pencarian "{{ kataKunci }}"</p>
      <button class="btn-outline" @click="resetPencarian">Reset Pencarian</button>
    </div>

    <!-- Floating cart bubble -->
    <transition name="pop">
      <button
          v-if="totalItem > 0"
          class="cart-bubble"
          :class="{ bump: bumpCart }"
          @click="keranjangTerbuka = true"
      >
        <div class="cart-bubble-inner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
          <span class="cart-count">{{ totalItem }} Item</span>
          <span class="cart-divider">|</span>
          <span class="cart-total">Rp {{ totalHarga.toLocaleString('id-ID') }}</span>
        </div>
      </button>
    </transition>

    <!-- Cart side panel -->
    <transition name="slide">
      <div v-if="keranjangTerbuka" class="cart-overlay" @click.self="keranjangTerbuka = false">
        <div class="cart-panel">
          <div class="cart-panel-header">
            <h3>Pesanan Anda</h3>
            <button class="btn-close" @click="keranjangTerbuka = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <div v-if="keranjang.length === 0" class="cart-empty">
            <svg class="empty-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            <p>Keranjang masih kosong</p>
          </div>

          <div v-else class="cart-items">
            <div v-for="item in keranjang" :key="item.id" class="cart-item">
              <img v-if="item.gambarUrl" :src="urlGambar(item.gambarUrl)" :alt="item.nama" class="cart-item-photo" />
              <div v-else class="cart-item-placeholder">{{ item.icon }}</div>

              <div class="cart-item-info">
                <p class="cart-item-name">{{ item.nama }}</p>
                <p class="cart-item-price">Rp {{ item.harga.toLocaleString('id-ID') }}</p>
              </div>

              <div class="stepper minimal">
                <button class="stepper-btn" @click="kurangiJumlah(item.id)">−</button>
                <span class="stepper-count">{{ item.qty }}</span>
                <button class="stepper-btn" @click="tambahKeKeranjang(item)">+</button>
              </div>
            </div>
          </div>

          <div v-if="keranjang.length > 0" class="cart-panel-footer">
            <div class="cart-summary-row">
              <span>Subtotal</span>
              <span class="summary-price">Rp {{ totalHarga.toLocaleString('id-ID') }}</span>
            </div>
            <button class="btn-checkout" @click="checkout">
              Checkout Pesanan
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
    errorMemuat.value = 'Terjadi kesalahan saat memuat menu. Silakan periksa koneksi Anda.'
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
  { label: 'Semua Menu', value: 'semua' },
  { label: 'Hidangan Utama', value: 'makanan' },
  { label: 'Minuman Segar', value: 'minuman' }
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
  setTimeout(() => { toast.value.tampil = false }, 2500)
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
/* Impor font elegan dari Google Fonts */
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

.menu-wrapper {
  /* Variabel Warna Premium */
  --color-bg: #FCFCFC;
  --color-surface: #FFFFFF;
  --color-text-primary: #1A1A1A;
  --color-text-secondary: #737373;
  --color-text-light: #A3A3A3;
  --color-accent-gold: #C69C6D;
  --color-accent-gold-hover: #B0885A;
  --color-border: #EAEAEA;
  --color-border-dark: #D4D4D4;
  --color-black: #0F0F0F;
  --color-error: #8B0000;

  /* Bayangan & Radius elegan */
  --shadow-sm: 0 4px 12px rgba(0, 0, 0, 0.03);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.06);
  --shadow-hover: 0 12px 32px rgba(0, 0, 0, 0.08);
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  /* Tipografi */
  --font-serif: 'Playfair Display', Georgia, serif;
  --font-sans: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
}

.menu-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 1.5rem 6rem;
  font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg);
  min-height: 100vh;
}

/* Header */
.page-header {
  text-align: center;
  margin-bottom: 3.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.eyebrow {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--color-accent-gold);
  margin-bottom: 1rem;
}

.page-header h2 {
  font-family: var(--font-serif);
  font-size: 3.5rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.header-divider {
  width: 60px;
  height: 2px;
  background-color: var(--color-accent-gold);
  margin: 1.5rem 0;
}

.page-header p {
  color: var(--color-text-secondary);
  font-size: 1.1rem;
  max-width: 500px;
  line-height: 1.6;
}

/* Controls */
.controls {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 3rem;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1 1 320px;
  max-width: 420px;
}

.icon-search {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-light);
  width: 20px;  /* Tambahkan batas lebar */
  height: 20px; /* Tambahkan batas tinggi */
}

.search-box input {
  width: 100%;
  padding: 0.9rem 1rem 0.9rem 3rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  font-size: 0.95rem;
  color: var(--color-text-primary);
  outline: none;
  font-family: var(--font-sans);
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.search-box input:focus {
  border-color: var(--color-accent-gold);
  box-shadow: 0 0 0 3px rgba(198, 156, 109, 0.1);
}

.search-box input::placeholder {
  color: var(--color-text-light);
}

.category-chips {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.chip {
  padding: 0.6rem 1.5rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
  font-family: var(--font-sans);
  font-weight: 500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chip:hover {
  border-color: var(--color-text-primary);
  color: var(--color-text-primary);
}

.chip.active {
  background: var(--color-black);
  border-color: var(--color-black);
  color: var(--color-surface);
}

/* Grid */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 2rem;
}

.menu-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  transition: all 0.4s ease;
}

.menu-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}/* GANTI BAGIAN INI SAJA DI CSS ABANG */

.menu-image {
  position: relative;
  height: 220px;
  background: #1A1A1A; /* Warna dasar gelap agar transisi blur lebih mulus */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-bottom: 1px solid var(--color-border);
}

/* INI CSS YANG KETINGGALAN BANG */
.image-backdrop {
  position: absolute;
  inset: -20px; /* Dibuat melebar agar tepi blurnya tidak terpotong */
  background-size: cover;
  background-position: center;
  filter: blur(15px) brightness(0.6); /* Efek blur dan sedikit digelapkan agar foto utama menonjol */
  z-index: 1;
  transition: transform 0.6s ease;
}
.menu-card:hover .image-backdrop {
  transform: scale(1.1);
}

.menu-photo {
  width: 100%;
  height: 100%;
  object-fit: contain; /* Foto tetap utuh, tidak ada yang terpotong */
  position: relative;
  z-index: 2; /* Memastikan foto utama berada di atas efek blur */
  padding: 0; /* Hapus padding agar foto bisa maksimal tingginya */
  transition: transform 0.6s ease;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3)); /* Sedikit bayangan memisahkan foto dari backdrop */
}

.menu-card:hover .menu-photo {
  transform: scale(1.05); /* Efek zoom tipis saat disentuh */
}
.placeholder-icon {
  font-size: 4rem;
  opacity: 0.8;
}

.badge-gold {
  position: absolute;
  top: 16px;
  left: 16px;
  background: var(--color-accent-gold);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.overlay-habis {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-habis span {
  background: var(--color-black);
  color: white;
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-radius: var(--radius-sm);
}

.menu-content {
  padding: 1.75rem;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.menu-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.menu-content h3 {
  font-family: var(--font-serif);
  font-size: 1.3rem;
  color: var(--color-text-primary);
  font-weight: 600;
  margin: 0;
  line-height: 1.3;
}

.rating {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-accent-gold);
}

.desc {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.menu-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  border-top: 1px solid var(--color-border);
  padding-top: 1.2rem;
}

.price {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 1.15rem;
}

.btn-add {
  background-color: transparent;
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-dark);
  padding: 0.5rem 1.25rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: var(--font-sans);
}

.btn-add:hover:not(:disabled) {
  background-color: var(--color-black);
  color: white;
  border-color: var(--color-black);
}

.btn-add:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  border-color: var(--color-border);
}

/* Stepper */
.stepper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: 1px solid var(--color-border-dark);
  border-radius: var(--radius-sm);
  padding: 0.2rem;
}

.stepper.minimal {
  border: none;
  background: var(--color-bg);
  padding: 0.3rem;
}

.stepper-btn {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--color-text-primary);
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.stepper-btn:hover {
  background: var(--color-border);
}

.stepper-count {
  font-weight: 600;
  min-width: 1.5rem;
  text-align: center;
  font-size: 0.95rem;
}

/* Empty / Loading State */
.empty-state {
  text-align: center;
  padding: 6rem 1rem;
  color: var(--color-text-secondary);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon-svg {
  width: 64px;
  height: 64px;
  margin-bottom: 1.5rem;
  color: var(--color-border-dark);
}

.btn-outline {
  margin-top: 1.5rem;
  background: transparent;
  border: 1px solid var(--color-text-primary);
  padding: 0.6rem 1.5rem;
  border-radius: var(--radius-sm);
  color: var(--color-text-primary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-outline:hover {
  background: var(--color-text-primary);
  color: white;
}

/* Skeleton Premium */
.skeleton-card {
  border: 1px solid var(--color-border);
}

.skeleton-block {
  background: linear-gradient(90deg, #F0F0F0 25%, #FAFAFA 50%, #F0F0F0 75%);
  background-size: 400% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
  border-radius: var(--radius-sm);
}

.skeleton-image {
  height: 220px;
  border-radius: 0;
}

.skeleton-line {
  height: 0.9rem;
  margin-bottom: 0.8rem;
}

@keyframes skeleton-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Floating cart bubble - Premium look */
.cart-bubble {
  position: fixed;
  bottom: 2.5rem;
  right: 2.5rem;
  background: var(--color-black);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  padding: 0;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 40;
  overflow: hidden;
}

.cart-bubble-inner {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  gap: 0.75rem;
  font-weight: 600;
  font-family: var(--font-sans);
}

.cart-bubble:hover {
  transform: translateY(-5px);
}

.cart-bubble.bump {
  animation: bump 0.3s ease;
}

.cart-divider {
  color: rgba(255, 255, 255, 0.3);
  font-weight: 300;
  margin: 0 0.2rem;
}

/* Cart Panel */
.cart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: flex-end;
  z-index: 50;
}

.cart-panel {
  width: min(420px, 100%);
  height: 100%;
  background: var(--color-surface);
  display: flex;
  flex-direction: column;
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.1);
}

.cart-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  border-bottom: 1px solid var(--color-border);
}

.cart-panel-header h3 {
  font-family: var(--font-serif);
  font-size: 1.5rem;
  margin: 0;
  color: var(--color-text-primary);
}

.btn-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.btn-close:hover {
  color: var(--color-black);
  transform: rotate(90deg);
}

.cart-empty {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
}

.cart-items {
  flex-grow: 1;
  overflow-y: auto;
  padding: 1rem 2rem;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 0;
  border-bottom: 1px solid var(--color-border);
}

.cart-item-photo {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.cart-item-placeholder {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.cart-item-info {
  flex-grow: 1;
}

.cart-item-name {
  font-weight: 600;
  margin: 0 0 0.3rem;
  font-size: 0.95rem;
  color: var(--color-text-primary);
}

.cart-item-price {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.cart-panel-footer {
  padding: 2rem;
  border-top: 1px solid var(--color-border);
  background: #FAFAFA;
}

.cart-summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
  color: var(--color-text-secondary);
}

.summary-price {
  font-weight: 700;
  color: var(--color-text-primary);
}

.btn-checkout {
  width: 100%;
  background: var(--color-black);
  color: white;
  border: none;
  padding: 1.1rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 1rem;
  font-family: var(--font-sans);
  cursor: pointer;
  transition: background 0.3s;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.btn-checkout:hover {
  background: var(--color-accent-gold);
}

/* Toast Modern */
.toast {
  position: fixed;
  bottom: 2.5rem;
  left: 50%;
  transform: translateX(-50%);
  background: var(--color-surface);
  color: var(--color-text-primary);
  padding: 1rem 1.5rem;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  font-size: 0.95rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--color-border);
  z-index: 60;
}

.toast-icon {
  background: var(--color-accent-gold);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
}

/* Animasi */
.fade-up-enter-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.pop-enter-active, .pop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.pop-enter-from, .pop-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}

.slide-enter-active, .slide-leave-active {
  transition: opacity 0.4s ease;
}
.slide-enter-active .cart-panel, .slide-leave-active .cart-panel {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
}
.slide-enter-from .cart-panel, .slide-leave-to .cart-panel {
  transform: translateX(100%);
}

.toast-enter-active, .toast-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.toast-enter-from, .toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}

@keyframes bump {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

/* Responsivitas */
@media (max-width: 768px) {
  .page-header h2 { font-size: 2.5rem; }
  .menu-grid { grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); }
  .cart-bubble { bottom: 1.5rem; right: 1.5rem; }
}

@media (max-width: 480px) {
  .page-header h2 { font-size: 2rem; }
  .controls { flex-direction: column; align-items: stretch; }
  .search-box { max-width: 100%; }
  .category-chips { justify-content: flex-start; overflow-x: auto; padding-bottom: 0.5rem; }
  .chip { white-space: nowrap; }
  .cart-divider, .cart-total { display: none; }
}
</style>