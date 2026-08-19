import { reactive } from 'vue'
import { api } from '../services/api'

// State singleton, dibagi ke semua komponen yang memanggil useAuth()
function muatDariStorage() {
    try {
        const raw = localStorage.getItem('restoku_user')
        return raw ? JSON.parse(raw) : null
    } catch {
        return null
    }
}

const state = reactive({
    user: muatDariStorage(), // { id, nama, email, role, poin }
    token: localStorage.getItem('restoku_token') || null
})

function simpanSesi(authResponse) {
    state.token = authResponse.token
    state.user = {
        id: authResponse.id,
        nama: authResponse.nama,
        email: authResponse.email,
        role: authResponse.role,
        poin: authResponse.poin
    }
    localStorage.setItem('restoku_token', authResponse.token)
    localStorage.setItem('restoku_user', JSON.stringify(state.user))
}

export function useAuth() {
    const login = async (email, password) => {
        const res = await api.post('/auth/login', { email, password }, { auth: false })
        simpanSesi(res)
        return res
    }

    const register = async (nama, email, password) => {
        const res = await api.post('/auth/register', { nama, email, password }, { auth: false })
        simpanSesi(res)
        return res
    }

    const logout = () => {
        state.token = null
        state.user = null
        localStorage.removeItem('restoku_token')
        localStorage.removeItem('restoku_user')
    }

    const tambahPoin = (poinBaru) => {
        if (state.user) {
            state.user.poin = poinBaru
            localStorage.setItem('restoku_user', JSON.stringify(state.user))
        }
    }

    return {
        state,
        isLoggedIn: () => !!state.token,
        isRole: (role) => state.user?.role === role,
        login,
        register,
        logout,
        tambahPoin
    }
}
