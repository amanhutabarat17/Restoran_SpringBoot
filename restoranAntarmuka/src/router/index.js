import { createRouter, createWebHistory } from 'vue-router'
import MenuView from '../views/MenuView.vue'
import CartView from '../views/CartView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // ---- Pelanggan (guest & member) ----
        { path: '/', name: 'menu', component: MenuView },
        { path: '/keranjang', name: 'cart', component: CartView },
        { path: '/pembayaran/:id', name: 'payment', component: () => import('../views/PaymentView.vue') },
        { path: '/login', name: 'login', component: LoginView },
        { path: '/daftar', name: 'register', component: () => import('../views/RegisterView.vue') },
        {
            path: '/akun',
            name: 'akun',
            component: () => import('../views/MemberDashboardView.vue'),
            meta: { requiresRole: 'CUSTOMER' }
        },

        // ---- Kasir ----
        { path: '/kasir/login', name: 'kasir-login', component: () => import('../views/kasir/KasirLoginView.vue') },
        {
            path: '/kasir',
            name: 'kasir-dashboard',
            component: () => import('../views/kasir/KasirDashboardView.vue'),
            meta: { requiresRole: 'KASIR' }
        },

        // ---- Admin ----
        { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLoginView.vue') },
        {
            path: '/admin',
            name: 'admin-dashboard',
            component: () => import('../views/admin/AdminDashboardView.vue'),
            meta: { requiresRole: 'ADMIN' }
        }
    ]
})

// Navigation guard: halaman ber-role hanya bisa diakses oleh role yang sesuai.
// Kalau belum login / role tidak cocok, dilempar ke halaman login yang sesuai.
router.beforeEach((to) => {
    const requiredRole = to.meta?.requiresRole
    if (!requiredRole) return true

    let user = null
    try {
        user = JSON.parse(localStorage.getItem('restoku_user') || 'null')
    } catch {
        user = null
    }

    if (!user || user.role !== requiredRole) {
        if (requiredRole === 'ADMIN') return { name: 'admin-login' }
        if (requiredRole === 'KASIR') return { name: 'kasir-login' }
        return { name: 'login', query: { redirect: to.fullPath } }
    }
    return true
})

export default router
