# TemuLapak

Platform pencarian dan penyewaan tempat usaha (ruko, kios, booth).
Java 21, Maven, Spring Boot 3.4, Thymeleaf, Spring Data JPA, Spring Security, **PostgreSQL**.

## Prasyarat
- JDK 21
- PostgreSQL berjalan di `localhost:5432` dengan user `postgres` dan password `admin`
- Koneksi internet saat pertama kali (unduh dependensi Maven; Bootstrap & font dimuat dari CDN)

## Menjalankan
```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```
Buka http://localhost:8081

Saat start, aplikasi otomatis:
1. membuat database `temulapak` bila belum ada,
2. menjalankan `src/main/resources/db/schema.sql` (9 tabel sesuai ERD),
3. mengisi fasilitas, akun admin, dan data contoh (matikan data contoh dengan `app.seed-demo-data=false`).

## Akun contoh
| Peran | Email | Password |
|---|---|---|
| Admin | admin@temulapak.com | admin123 |
| Pemilik (Bu Sari) | pemilik@temulapak.com | pemilik123 |
| Pemilik | pemilik2@temulapak.com, pemilik3@temulapak.com | pemilik123 |
| Penyewa (Rina Lestari) | penyewa@temulapak.com | penyewa123 |
| Penyewa | penyewa2@temulapak.com sampai penyewa4@temulapak.com | penyewa123 |

Untuk memuat ulang data contoh yang sama dengan desain Figma, hapus database `temulapak`
(`DROP DATABASE temulapak;` lewat psql/pgAdmin) lalu jalankan aplikasi lagi.
Database lama tetap bisa dipakai: kolom baru ditambahkan otomatis dan data lama dilengkapi.

## Konfigurasi
Semua dapat diubah lewat environment variable: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `UPLOAD_DIR`
(default: localhost, 5432, temulapak, postgres, admin, `uploads`).
Foto yang diunggah disimpan di folder `uploads/` (di luar classpath) dan disajikan di `/uploads/**`.

## Skenario uji cepat
1. **Penyewa** (Rina): Booking Saya menampilkan pengajuan dengan estimasi total; coba "Batalkan Pengajuan".
2. **Pemilik** (Bu Sari): Tempat Saya menampilkan status verifikasi dan status tempat terpisah serta catatan admin; Booking Masuk dan konfirmasi/tolak.
3. **Admin**: Antrean Verifikasi. Listing tanpa foto ("Booth Festival Jajan Cilandak") hanya bisa ditolak dan wajib memakai catatan.
4. Pemilik pada "Ruko Mungil Cilandak" (pemilik2) tidak bisa mengonfirmasi booking yang bertabrakan dengan booking yang sudah dikonfirmasi.
