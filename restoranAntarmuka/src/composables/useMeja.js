import { ref } from 'vue'

// Nomor meja didapat dari QR code di meja, misal: https://restoku.id/?meja=5
// Disimpan di localStorage supaya tetap terbawa saat pindah halaman (menu -> keranjang -> bayar).
const noMeja = ref(localStorage.getItem('restoku_meja') || '')

export function useMeja() {
    const setMeja = (value) => {
        noMeja.value = value
        if (value) {
            localStorage.setItem('restoku_meja', value)
        } else {
            localStorage.removeItem('restoku_meja')
        }
    }

    const bacaMejaDariUrl = (route) => {
        const dariUrl = route?.query?.meja
        if (dariUrl) setMeja(String(dariUrl))
    }

    return { noMeja, setMeja, bacaMejaDariUrl }
}
