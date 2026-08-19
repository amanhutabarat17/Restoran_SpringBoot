<template>
  <!-- Navbar Profesional (disembunyikan di halaman admin/kasir yang punya layout sendiri) -->
  <header v-if="!sembunyikanNavbar" class="navbar">
    <div class="nav-container">
      <RouterLink to="/" class="logo">
        🍽️ <span class="logo-text">RestoKu</span>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/">Daftar Menu</RouterLink>
        <RouterLink to="/keranjang" class="cart-link">
          🛒 Keranjang
          <span v-if="totalItem > 0" class="cart-badge">{{ totalItem }}</span>
        </RouterLink>

        <RouterLink v-if="auth.isLoggedIn()" to="/akun" class="login-btn">
          👤 {{ auth.state.user?.nama?.split(' ')[0] }} · {{ auth.state.user?.poin || 0 }} poin
        </RouterLink>
        <RouterLink v-else to="/login" class="login-btn">Login</RouterLink>

        <RouterLink to="/kasir/login" class="staff-link">Staf</RouterLink>
      </nav>
    </div>
  </header>

  <!-- Area Konten Utama -->
  <main class="main-content">
    <RouterView />
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useCart } from './composables/useCart'
import { useAuth } from './composables/useAuth'

const route = useRoute()
const { totalItem } = useCart()
const auth = useAuth()

const sembunyikanNavbar = computed(() =>
    route.path.startsWith('/admin') || route.path.startsWith('/kasir')
)
</script>

<style>
/* Import Font Modern dari Google */
@import url('https://fonts.googleapis.com/css2?family=Fraunces:wght@500;600;700&family=Inter:wght@400;500;600;700&display=swap');

/* Reset CSS Dasar */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', sans-serif;
}

body {
  background-color: #fffbf5; /* Cream, selaras dengan MenuView & CartView */
  color: #1c1917;
}

/* Styling Navbar */
.navbar {
  background-color: white;
  box-shadow: 0 2px 15px rgba(28, 25, 21, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-text {
  font-family: 'Fraunces', Georgia, serif;
  font-weight: 700;
  color: #c2410c; /* Spice orange, konsisten dengan MenuView */
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-links a {
  text-decoration: none;
  color: #57534e;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-links a:hover {
  color: #c2410c;
}

.nav-links a.router-link-exact-active {
  color: #c2410c;
  font-weight: 600;
}

.cart-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.cart-badge {
  background: #c2410c;
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  min-width: 1.15rem;
  height: 1.15rem;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0.3rem;
}

/* Tombol Khusus di Navbar */
.login-btn {
  background-color: #c2410c;
  color: white !important;
  padding: 0.5rem 1.5rem;
  border-radius: 50px;
}

.login-btn:hover {
  background-color: #9a3412;
  transform: translateY(-1px);
}

.staff-link {
  font-size: 0.78rem !important;
  color: #a8a29e !important;
  font-weight: 500 !important;
}

.staff-link:hover {
  color: #57534e !important;
}

.main-content {
  padding: 0;
}
</style>