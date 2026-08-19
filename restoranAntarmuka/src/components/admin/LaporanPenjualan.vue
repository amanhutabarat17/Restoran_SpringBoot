<template>
  <div class="module">
    <p v-if="sedangMemuat" class="loading-info">Memuat laporan...</p>
    <p v-if="pesanError" class="error-info">{{ pesanError }}</p>

    <div class="stat-cards">
      <div class="stat-card">
        <span class="stat-label">Total Omzet (semua waktu)</span>
        <span class="stat-value">Rp {{ totalOmzetMinggu.toLocaleString('id-ID') }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Transaksi Lunas</span>
        <span class="stat-value">{{ totalTransaksiMinggu }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Rata-rata / Transaksi</span>
        <span class="stat-value">Rp {{ rataRataOmzetHarian.toLocaleString('id-ID') }}</span>
      </div>
    </div>

    <div class="filter-bar">
      <label class="field">
        <span>Dari Tanggal</span>
        <input v-model="dari" type="date" />
      </label>
      <label class="field">
        <span>Sampai Tanggal</span>
        <input v-model="sampai" type="date" />
      </label>
      <button class="btn-primary" @click="terapkanFilter">Terapkan</button>
    </div>

    <div class="table-card">
      <table>
        <thead>
        <tr>
          <th>Tanggal</th>
          <th>Jumlah Transaksi</th>
          <th>Omzet</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="baris in dataPenjualan" :key="baris.tanggal">
          <td>{{ formatTanggal(baris.tanggal) }}</td>
          <td>{{ baris.transaksi }}</td>
          <td>Rp {{ Number(baris.omzet).toLocaleString('id-ID') }}</td>
        </tr>
        <tr v-if="dataPenjualan.length === 0 && !sedangMemuat">
          <td colspan="3" class="empty-row">Belum ada data penjualan pada rentang ini.</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdmin } from '../../composables/useAdmin'

const { dataPenjualan, muatLaporan, totalOmzetMinggu, totalTransaksiMinggu, rataRataOmzetHarian } = useAdmin()

const sedangMemuat = ref(true)
const pesanError = ref('')

const hariIni = new Date().toISOString().slice(0, 10)
const tujuhHariLalu = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)

const dari = ref(tujuhHariLalu)
const sampai = ref(hariIni)

const muat = async () => {
  sedangMemuat.value = true
  pesanError.value = ''
  try {
    await muatLaporan(dari.value, sampai.value)
  } catch (err) {
    pesanError.value = 'Gagal memuat laporan: ' + err.message
  } finally {
    sedangMemuat.value = false
  }
}

const terapkanFilter = () => muat()

const formatTanggal = (tgl) =>
    new Date(tgl).toLocaleDateString('id-ID', { weekday: 'short', day: 'numeric', month: 'short' })

onMounted(muat)
</script>

<style scoped>
.module {
  --ink: #1c1917;
  --ink-soft: #57534e;
  --peach: #ffedd5;
  --spice: #c2410c;
  --spice-dark: #9a3412;
  --border: #f1e4d6;
}

.loading-info {
  color: var(--ink-soft);
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
}

.error-info {
  color: #dc2626;
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.stat-label {
  font-size: 0.78rem;
  color: var(--ink-soft);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.stat-value {
  font-family: 'Fraunces', Georgia, serif;
  font-size: 1.5rem;
  color: var(--spice-dark);
  font-weight: 600;
}

.filter-bar {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 1.2rem;
  flex-wrap: wrap;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--ink);
}

.field input {
  padding: 0.6rem 0.8rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-size: 0.9rem;
  outline: none;
}

.field input:focus {
  border-color: var(--spice);
}

.btn-primary {
  background: var(--spice);
  color: white;
  border: none;
  padding: 0.65rem 1.2rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.88rem;
  cursor: pointer;
  height: fit-content;
}

.btn-primary:hover {
  background: var(--spice-dark);
}

.table-card {
  background: white;
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

thead {
  background: #faf6ef;
}

th {
  text-align: left;
  padding: 0.9rem 1rem;
  color: var(--ink-soft);
  font-weight: 600;
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

td {
  padding: 0.85rem 1rem;
  border-top: 1px solid var(--border);
  color: var(--ink);
}

.empty-row {
  text-align: center;
  color: var(--ink-soft);
  padding: 2rem;
}
</style>
