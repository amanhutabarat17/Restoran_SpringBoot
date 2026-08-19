import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // Memanggil folder router yang kita buat

const app = createApp(App)

app.use(router) // Mengaktifkan router

app.mount('#app')