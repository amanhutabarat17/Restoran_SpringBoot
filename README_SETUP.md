# RestoKu — Sistem Restoran Full-Stack

Backend: Spring Boot (Java 21) + PostgreSQL + JWT
Frontend: Vue 3 + Vite

## 1. Jalankan Backend

1. Buat database PostgreSQL:
   ```sql
   CREATE DATABASE restoran_db;
   ```
2. Edit `src/main/resources/application.properties`:
   - `spring.datasource.password` — isi password PostgreSQL kamu (Laragon).
   - `app.jwt.secret` — ganti dengan string acak panjang (min 32 karakter).
   - `midtrans.server-key` & `midtrans.client-key` — isi dengan key asli dari
     [dashboard.midtrans.com](https://dashboard.midtrans.com) (Settings → Access Keys, pakai
     Sandbox dulu untuk uji coba). Selama masih placeholder, tombol "Bayar Non-Tunai" akan
     menampilkan pesan error yang jelas — bukan gagal diam-diam.
3. **Tidak perlu menjalankan file SQL manual apapun.** `spring.jpa.hibernate.ddl-auto=update`
   membuat semua tabel otomatis dari entity Java saat aplikasi pertama kali dijalankan, dan
   `DataLoader` otomatis mengisi menu, akun, dan promo contoh.
4. Jalankan:
   ```bash
   ./mvnw spring-boot:run
   ```
   Backend jalan di `http://localhost:8080`.

### Akun awal (dari DataLoader)
| Role   | Email                | Password   |
|--------|-----------------------|------------|
| Admin  | admin@restoku.id      | admin123   |
| Kasir  | siti@restoku.id       | kasir123   |
| Kasir  | budi@restoku.id       | kasir123   |
| Member | pelanggan@mail.com    | member123  |

## 2. Jalankan Frontend

```bash
cd restoranAntarmuka
npm install
npm run dev
```
Buka `http://localhost:5173`.

## 3. Alur Penggunaan

- **Guest (non-member)**: scan QR meja → buka `http://localhost:5173/?meja=5` → pilih menu →
  keranjang → checkout → pilih bayar **Non-Tunai (Midtrans)** atau **Bayar di Kasir**.
- **Member**: daftar di `/daftar` atau login di `/login` → poin otomatis bertambah tiap
  transaksi lunas (1 poin / Rp10.000) → riwayat pesanan & beri rating di `/akun`.
- **Kasir**: login di `/kasir/login` → tab **Antrian Pesanan** untuk proses pesanan masuk &
  konfirmasi bayar tunai, tab **Pesanan Baru** untuk input pesanan walk-in manual.
- **Admin**: login di `/admin/login` → kelola Menu, Promo, akun Kasir, dan lihat Laporan
  Penjualan — semua terhubung ke data asli di database.

## 4. Integrasi Midtrans

`PaymentController` (`/api/payment/midtrans/create/{orderId}`) memanggil Snap API Midtrans
lewat `MidtransService`. Setelah kamu isi server key & client key asli:
1. Daftarkan **Payment Notification URL** di dashboard Midtrans:
   `https://domainmu.com/api/payment/midtrans/notification`
   (untuk tes lokal, pakai tool tunnel seperti ngrok agar Midtrans bisa mencapai localhost-mu).
2. Alur: checkout guest/member → pilih Non-Tunai → dialihkan ke halaman Snap Midtrans →
   setelah bayar, Midtrans memanggil webhook → status pesanan otomatis jadi **Lunas** dan
   poin member otomatis bertambah.

## 5. Catatan Teknis

- Semua tabel dibuat otomatis oleh Hibernate (`ddl-auto=update`); file `RESTOKU_SKEMA.sql`
  yang lama **tidak perlu dijalankan lagi** karena penamaan tabelnya sudah tidak sama dengan
  entity Java yang baru (mis. `pesanan` → `orders`, `detail_pesanan` → `order_items`).
- CORS diatur untuk origin `http://localhost:5173` di `SecurityConfig.java` — ubah jika
  frontend kamu jalan di port/domain lain.
- Token JWT disimpan di `localStorage` browser (`restoku_token`, `restoku_user`).
