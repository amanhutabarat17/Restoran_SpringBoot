// Lapisan pemanggilan API terpusat. Semua request ke backend Spring Boot
// lewat sini supaya header Authorization (JWT) otomatis disisipkan.

export const API_ORIGIN = 'http://localhost:8080'
export const API_BASE_URL = `${API_ORIGIN}/api`

/** Ubah path gambar relatif ("/uploads/menu/x.jpg") dari backend jadi URL absolut yang bisa dipakai di <img src>. */
export function urlGambar(path) {
    if (!path) return null
    if (path.startsWith('http://') || path.startsWith('https://')) return path
    return `${API_ORIGIN}${path}`
}

function getToken() {
    return localStorage.getItem('restoku_token')
}

async function request(path, { method = 'GET', body, auth = true, params } = {}) {
    let url = `${API_BASE_URL}${path}`

    if (params) {
        const query = new URLSearchParams(
            Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
        ).toString()
        if (query) url += `?${query}`
    }

    const headers = { 'Content-Type': 'application/json' }
    if (auth) {
        const token = getToken()
        if (token) headers['Authorization'] = `Bearer ${token}`
    }

    const res = await fetch(url, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined
    })

    // 204 No Content
    if (res.status === 204) return null

    let data = null
    try {
        data = await res.json()
    } catch {
        // respons tanpa body JSON, biarkan null
    }

    if (!res.ok) {
        const message = data?.message || `Permintaan gagal (${res.status})`
        const error = new Error(message)
        error.status = res.status
        error.data = data
        throw error
    }

    return data
}

/** Upload file (multipart/form-data) — dipisah dari request() karena TIDAK boleh set Content-Type manual
 * (browser yang mengisi boundary multipart-nya sendiri). */
async function uploadFile(path, file) {
    const formData = new FormData()
    formData.append('file', file)

    const headers = {}
    const token = getToken()
    if (token) headers['Authorization'] = `Bearer ${token}`

    const res = await fetch(`${API_BASE_URL}${path}`, { method: 'POST', headers, body: formData })

    let data = null
    try { data = await res.json() } catch { /* tanpa body */ }

    if (!res.ok) {
        const message = data?.message || `Upload gagal (${res.status})`
        const error = new Error(message)
        error.status = res.status
        throw error
    }
    return data
}

export const api = {
    get: (path, params) => request(path, { method: 'GET', params }),
    post: (path, body, opts = {}) => request(path, { method: 'POST', body, ...opts }),
    put: (path, body) => request(path, { method: 'PUT', body }),
    patch: (path, body) => request(path, { method: 'PATCH', body }),
    delete: (path) => request(path, { method: 'DELETE' }),
    upload: (path, file) => uploadFile(path, file)
}