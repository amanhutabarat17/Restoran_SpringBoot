<template>
  <div class="admin-shell">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">🍽️</span>
        <span class="brand-text">RestoKu <em>Admin</em></span>
      </div>

      <nav class="sidebar-nav">
        <button
            v-for="tab in tabs"
            :key="tab.key"
            class="nav-item"
            :class="{ active: tabAktif === tab.key }"
            @click="tabAktif = tab.key"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          {{ tab.label }}
        </button>
      </nav>

      <button class="logout-btn" @click="handleLogout">↩ Keluar</button>
    </aside>

    <!-- Content -->
    <main class="admin-content">
      <header class="content-header">
        <div>
          <h1>{{ currentTab.label }}</h1>
          <p>{{ currentTab.desc }}</p>
        </div>
        <div class="admin-avatar">👤 {{ isAdminLoggedIn.nama || 'Admin' }}</div>
      </header>

      <KelolaMenu v-if="tabAktif === 'menu'" />
      <KelolaPromo v-else-if="tabAktif === 'promo'" />
      <LaporanPenjualan v-else-if="tabAktif === 'laporan'" />
      <KelolaKasir v-else-if="tabAktif === 'kasir'" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdmin } from '../../composables/useAdmin'
import KelolaMenu from '../../components/admin/KelolaMenu.vue'
import KelolaPromo from '../../components/admin/KelolaPromo.vue'
import LaporanPenjualan from '../../components/admin/LaporanPenjualan.vue'
import KelolaKasir from '../../components/admin/KelolaKasir.vue'

const router = useRouter()
const { isAdminLoggedIn, logout } = useAdmin()

const tabs = [
  { key: 'menu', label: 'Kelola Menu', icon: '🍛', desc: 'Tambah, ubah, dan atur ketersediaan menu.' },
  { key: 'promo', label: 'Promo', icon: '🏷️', desc: 'Buat dan kelola kode promo untuk pelanggan.' },
  { key: 'laporan', label: 'Laporan Penjualan', icon: '📊', desc: 'Pantau omzet dan jumlah transaksi.' },
  { key: 'kasir', label: 'Kasir', icon: '🧑\u200d💼', desc: 'Daftarkan dan kelola akun kasir.' }
]

const tabAktif = ref('menu')
const currentTab = computed(() => tabs.find((t) => t.key === tabAktif.value))

const handleLogout = () => {
  logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-shell {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --cream: #fffbf5;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --border: #f1e4d6;
}

.admin-shell {
  display: flex;
  min-height: 100vh;
  background: var(--cream);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Sidebar */
.sidebar {
  width: 240px;
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
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.nav-item.active {
  background: var(--spice);
  color: white;
}

.nav-icon {
  font-size: 1.05rem;
}

.logout-btn {
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  padding: 0.7rem;
  border-radius: 10px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

/* Content */
.admin-content {
  flex-grow: 1;
  padding: 2rem 2.5rem 3rem;
  max-width: 1100px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.content-header h1 {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.8rem;
  color: var(--ink);
  margin: 0 0 0.3rem;
}

.content-header p {
  color: var(--ink-soft);
  font-size: 0.92rem;
}

.admin-avatar {
  background: white;
  border: 1px solid var(--border);
  padding: 0.5rem 1rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--ink);
  white-space: nowrap;
}

@media (max-width: 800px) {
  .admin-shell {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    overflow-x: auto;
  }
  .sidebar-brand {
    display: none;
  }
  .sidebar-nav {
    flex-direction: row;
  }
  .logout-btn {
    white-space: nowrap;
  }
}
</style>