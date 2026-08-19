import { reactive, computed } from 'vue'
import { api } from '../services/api'
import { useAuth } from './useAuth'

// ====== STATE SINGLETON (dibagi ke semua komponen admin) ======
const daftarMenuAdmin = reactive([])
const daftarPromo = reactive([])
const daftarKasir = reactive([])
const dataPenjualan = reactive([]) // laporan harian dari backend
const ringkasanPenjualan = reactive({ totalOmzet: 0, totalTransaksi: 0, rataRataPerTransaksi: 0 })

// ---- helper konversi kategori menu: backend pakai MAKANAN/MINUMAN, UI pakai makanan/minuman ----
const keUiMenu = (m) => ({
    id: m.id,
    nama: m.nama,
    deskripsi: m.deskripsi || '',
    kategori: (m.kategori || 'MAKANAN').toLowerCase(),
    harga: m.harga,
    stok: m.stok,
    icon: m.icon || '🍽️',
    gambarUrl: m.gambarUrl || '',
    badge: m.badge || '',
    aktif: m.aktif
})

const keUiPromo = (p) => ({
    id: p.id,
    judul: p.judul,
    kode: p.kode,
    tipe: (p.tipe || 'PERSEN').toLowerCase(),
    nilai: p.nilai,
    berlakuSampai: p.berlakuSampai,
    aktif: p.aktif
})

const keUiKasir = (k) => ({
    id: k.id,
    nama: k.nama,
    email: k.email,
    shift: k.shift ? k.shift.charAt(0) + k.shift.slice(1).toLowerCase() : 'Pagi', // PAGI -> Pagi
    aktif: k.aktif
})

export function useAdmin() {
    const { state: authState, login: authLogin, logout: authLogout, isRole } = useAuth()

    // ---- Auth (dipakai AdminLoginView) ----
    const isAdminLoggedIn = reactive({
        get value() { return !!authState.user && authState.user.role === 'ADMIN' },
        get nama() { return authState.user?.nama || '' }
    })

    const login = async (email, password) => {
        try {
            const res = await authLogin(email, password)
            if (res.role !== 'ADMIN') {
                authLogout()
                return { sukses: false, pesan: 'Akun ini bukan akun admin' }
            }
            return { sukses: true }
        } catch (err) {
            return { sukses: false, pesan: err.message }
        }
    }

    const logout = () => authLogout()

    // ---- Menu ----
    const muatMenu = async () => {
        const data = await api.get('/menus/semua')
        daftarMenuAdmin.splice(0, daftarMenuAdmin.length, ...data.map(keUiMenu))
    }

    const tambahMenu = async (menu) => {
        await api.post('/menus', {
            nama: menu.nama,
            deskripsi: menu.deskripsi || '',
            harga: menu.harga,
            kategori: menu.kategori.toUpperCase(),
            stok: menu.stok,
            icon: menu.icon,
            gambarUrl: menu.gambarUrl || null,
            badge: menu.badge || '',
            aktif: true
        })
        await muatMenu()
    }

    const editMenu = async (id, dataBaru) => {
        await api.put(`/menus/${id}`, {
            nama: dataBaru.nama,
            deskripsi: dataBaru.deskripsi || '',
            harga: dataBaru.harga,
            kategori: dataBaru.kategori.toUpperCase(),
            stok: dataBaru.stok,
            icon: dataBaru.icon,
            gambarUrl: dataBaru.gambarUrl || null,
            badge: dataBaru.badge || '',
            aktif: dataBaru.aktif !== undefined ? dataBaru.aktif : true
        })
        await muatMenu()
    }

    /** Upload gambar menu, mengembalikan path publik (mis. "/uploads/menu/xxx.jpg") untuk disimpan ke form.gambarUrl. */
    const uploadGambarMenu = async (file) => {
        const hasil = await api.upload('/upload/menu-gambar', file)
        return hasil.url
    }

    const hapusMenu = async (id) => {
        await api.delete(`/menus/${id}`)
        const idx = daftarMenuAdmin.findIndex((m) => m.id === id)
        if (idx !== -1) daftarMenuAdmin.splice(idx, 1)
    }

    const toggleAktifMenu = async (id) => {
        await api.patch(`/menus/${id}/toggle`)
        const menu = daftarMenuAdmin.find((m) => m.id === id)
        if (menu) menu.aktif = !menu.aktif
    }

    // ---- Promo ----
    const muatPromo = async () => {
        const data = await api.get('/promo')
        daftarPromo.splice(0, daftarPromo.length, ...data.map(keUiPromo))
    }

    const tambahPromo = async (promo) => {
        await api.post('/promo', {
            judul: promo.judul,
            kode: promo.kode,
            tipe: promo.tipe.toUpperCase(),
            nilai: promo.nilai,
            berlakuSampai: promo.berlakuSampai,
            aktif: true
        })
        await muatPromo()
    }

    const editPromo = async (id, dataBaru) => {
        await api.put(`/promo/${id}`, {
            judul: dataBaru.judul,
            kode: dataBaru.kode,
            tipe: dataBaru.tipe.toUpperCase(),
            nilai: dataBaru.nilai,
            berlakuSampai: dataBaru.berlakuSampai,
            aktif: dataBaru.aktif !== undefined ? dataBaru.aktif : true
        })
        await muatPromo()
    }

    const hapusPromo = async (id) => {
        await api.delete(`/promo/${id}`)
        const idx = daftarPromo.findIndex((p) => p.id === id)
        if (idx !== -1) daftarPromo.splice(idx, 1)
    }

    const toggleAktifPromo = async (id) => {
        await api.patch(`/promo/${id}/toggle`)
        const promo = daftarPromo.find((p) => p.id === id)
        if (promo) promo.aktif = !promo.aktif
    }

    // ---- Kasir ----
    const muatKasir = async () => {
        const data = await api.get('/admin/kasir')
        daftarKasir.splice(0, daftarKasir.length, ...data.map(keUiKasir))
    }

    const tambahKasir = async (kasir) => {
        await api.post('/admin/kasir', {
            nama: kasir.nama,
            email: kasir.email,
            password: kasir.password,
            shift: kasir.shift.toUpperCase(),
            aktif: true
        })
        await muatKasir()
    }

    const hapusKasir = async (id) => {
        await api.delete(`/admin/kasir/${id}`)
        const idx = daftarKasir.findIndex((k) => k.id === id)
        if (idx !== -1) daftarKasir.splice(idx, 1)
    }

    const toggleAktifKasir = async (id) => {
        await api.patch(`/admin/kasir/${id}/toggle`)
        const kasir = daftarKasir.find((k) => k.id === id)
        if (kasir) kasir.aktif = !kasir.aktif
    }

    // ---- Laporan ----
    const muatLaporan = async (dari, sampai) => {
        const [harian, ringkasan] = await Promise.all([
            api.get('/laporan/harian', { dari, sampai }),
            api.get('/laporan/ringkasan')
        ])
        dataPenjualan.splice(0, dataPenjualan.length, ...harian)
        Object.assign(ringkasanPenjualan, ringkasan)
    }

    const totalOmzetMinggu = computed(() => ringkasanPenjualan.totalOmzet || 0)
    const totalTransaksiMinggu = computed(() => ringkasanPenjualan.totalTransaksi || 0)
    const rataRataOmzetHarian = computed(() => ringkasanPenjualan.rataRataPerTransaksi || 0)

    return {
        isAdminLoggedIn,
        login,
        logout,

        daftarMenuAdmin,
        muatMenu,
        tambahMenu,
        editMenu,
        hapusMenu,
        toggleAktifMenu,
        uploadGambarMenu,

        daftarPromo,
        muatPromo,
        tambahPromo,
        editPromo,
        hapusPromo,
        toggleAktifPromo,

        daftarKasir,
        muatKasir,
        tambahKasir,
        hapusKasir,
        toggleAktifKasir,

        dataPenjualan,
        ringkasanPenjualan,
        muatLaporan,
        totalOmzetMinggu,
        totalTransaksiMinggu,
        rataRataOmzetHarian
    }
}