import { reactive, computed } from 'vue'

// State tunggal (singleton) di luar fungsi agar semua komponen
// yang memanggil useCart() berbagi keranjang yang sama.
const keranjang = reactive([])

export function useCart() {
    const totalItem = computed(() =>
        keranjang.reduce((sum, item) => sum + item.qty, 0)
    )

    const totalHarga = computed(() =>
        keranjang.reduce((sum, item) => sum + item.qty * item.harga, 0)
    )

    const jumlahDiKeranjang = (id) => {
        const item = keranjang.find((i) => i.id === id)
        return item ? item.qty : 0
    }

    const tambahKeKeranjang = (menu) => {
        const existing = keranjang.find((i) => i.id === menu.id)
        if (existing) {
            existing.qty++
        } else {
            keranjang.push({ ...menu, qty: 1 })
        }
    }

    const kurangiJumlah = (id) => {
        const existing = keranjang.find((i) => i.id === id)
        if (!existing) return
        existing.qty--
        if (existing.qty <= 0) {
            const idx = keranjang.findIndex((i) => i.id === id)
            if (idx !== -1) keranjang.splice(idx, 1)
        }
    }

    const hapusItem = (id) => {
        const idx = keranjang.findIndex((i) => i.id === id)
        if (idx !== -1) keranjang.splice(idx, 1)
    }

    const kosongkanKeranjang = () => {
        keranjang.splice(0, keranjang.length)
    }

    return {
        keranjang,
        totalItem,
        totalHarga,
        jumlahDiKeranjang,
        tambahKeKeranjang,
        kurangiJumlah,
        hapusItem,
        kosongkanKeranjang
    }
}