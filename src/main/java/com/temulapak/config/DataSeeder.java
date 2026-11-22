package com.temulapak.config;

import com.temulapak.entity.*;
import com.temulapak.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

/**
 * Mengisi data awal: master fasilitas, akun admin, dan (opsional) data contoh.
 * Aman dijalankan berulang: hanya menambah yang belum ada.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final List<String> FASILITAS = List.of("Listrik", "Air", "Parkir motor", "Parkir mobil",
            "Toilet", "Rolling door", "Etalase", "Meja & kursi", "Tenda");

    private final AdminRepository adminRepository;
    private final PemilikTempatRepository pemilikRepository;
    private final PenyewaRepository penyewaRepository;
    private final FasilitasRepository fasilitasRepository;
    private final TempatRepository tempatRepository;
    private final BookingRepository bookingRepository;
    private final UlasanRepository ulasanRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedFasilitas();
        seedAdmin();
        lengkapiDataLama();
        if (seedDemo && pemilikRepository.count() == 0 && penyewaRepository.count() == 0) {
            seedDemoData();
        }
    }

    private void seedFasilitas() {
        for (String nama : FASILITAS) {
            if (!fasilitasRepository.existsByNamaFasilitasIgnoreCase(nama)) {
                fasilitasRepository.save(new Fasilitas(nama));
            }
        }
    }

    private void seedAdmin() {
        if (adminRepository.count() > 0) {
            return;
        }
        Admin a = new Admin();
        a.setNama("Admin TemuLapak");
        a.setEmail("admin@temulapak.com");
        a.setPassword(passwordEncoder.encode("admin123"));
        adminRepository.save(a);
    }

    /** Mengisi kolom baru pada data dari versi sebelumnya agar tampil normal. */
    private void lengkapiDataLama() {
        for (Tempat t : tempatRepository.findAll()) {
            if (t.getAlamatLengkap() != null) {
                continue;
            }
            t.setAlamatLengkap(t.getLokasi());
            t.setKondisi("Belum diisi pemilik");
            t.setSatuanPeriode(SatuanPeriode.BULAN);
            t.setListrikMode(ModeBiaya.TERMASUK);
            t.setAirMode(ModeBiaya.TERMASUK);
            aturanStandar(t, "1 hari");
            t.setDiajukanPada(LocalDate.now());
        }
    }

    private void aturanStandar(Tempat t, String durasiMinimal) {
        t.setDurasiMinimal(durasiMinimal);
        t.setPerpanjangan("Diajukan paling lambat 14 hari sebelum masa sewa berakhir, dengan persetujuan pemilik.");
        t.setPembayaran("Dilakukan langsung antara penyewa dan pemilik, di luar TemuLapak. Jadwal dan caranya disepakati sebelum mulai sewa.");
        t.setPembatalan("Pengajuan yang masih menunggu konfirmasi bisa dibatalkan tanpa biaya. Ketentuan setelah dikonfirmasi belum ditetapkan.");
        t.setDenda("1% dari harga sewa untuk setiap hari keterlambatan pembayaran.");
    }

    // ------------------------------------------------------------------ data contoh

    private void seedDemoData() {
        PemilikTempat sari = pemilik("Bu Sari", "pemilik@temulapak.com", "081234567801");
        PemilikTempat hendra = pemilik("Pak Hendra", "pemilik2@temulapak.com", "081234567802");
        PemilikTempat wati = pemilik("Bu Wati", "pemilik3@temulapak.com", "081234567803");

        Penyewa rina = penyewa("Rina Lestari", "penyewa@temulapak.com", "081234567890");
        Penyewa dimas = penyewa("Dimas Pratama", "penyewa2@temulapak.com", "081334567812");
        Penyewa sinta = penyewa("Sinta Anggraini", "penyewa3@temulapak.com", "085734567813");
        Penyewa budi = penyewa("Budi Hartono", "penyewa4@temulapak.com", "087834567814");

        Map<String, Fasilitas> fas = new HashMap<>();
        fasilitasRepository.findAll().forEach(f -> fas.put(f.getNamaFasilitas(), f));

        // ---- milik Bu Sari (sesuai halaman "Tempat Saya") ----
        Tempat kiosPondokLabu = tempat(sari, "Kios Depan Pasar Pondok Labu", "Kios", "Pondok Labu, Jakarta Selatan",
                "Jl. Contoh No. 12, Pondok Labu", "3 × 4 m", "Baik: lantai keramik, atap tidak bocor",
                "Kios di depan pasar dengan lalu lintas pejalan kaki yang ramai pada pagi hari. Cocok untuk makanan ringan, minuman, atau usaha kecil lain.",
                "1800000", SatuanPeriode.BULAN, StatusVerifikasi.APPROVED, StatusTempat.AVAILABLE,
                List.of("demo-kios-1.svg", "demo-kios-2.svg", "demo-ruko-2.svg", "demo-booth-1.svg"), fas,
                List.of("Listrik", "Air", "Rolling door", "Parkir motor"), "3 bulan", t -> {
                    t.setListrikMode(ModeBiaya.PEMAKAIAN);
                    t.setListrikTarifKwh(new BigDecimal("1500"));
                    t.setListrikEstimasiKwh(100);
                    t.setAirMode(ModeBiaya.TETAP);
                    t.setAirBiaya(new BigDecimal("75000"));
                    t.setBiayaLain("Iuran kebersihan;50000");
                });
        Tempat rukoTebet = tempat(sari, "Ruko 2 Lantai Tebet Barat", "Ruko", "Tebet, Jakarta Selatan",
                "Jl. Tebet Barat Dalam No. 8, Tebet", "5 × 12 m", "Baik: dua lantai, dinding baru dicat",
                "Ruko dua lantai di jalan utama Tebet Barat, cocok untuk toko, kafe kecil, atau kantor usaha.",
                "6500000", SatuanPeriode.BULAN, StatusVerifikasi.PENDING, StatusTempat.AVAILABLE,
                List.of("demo-ruko-1.svg"), fas, List.of("Listrik", "Air", "Toilet", "Parkir motor"), "3 bulan", t -> {
                    t.setListrikMode(ModeBiaya.PEMAKAIAN);
                    t.setListrikTarifKwh(new BigDecimal("1500"));
                    t.setAirMode(ModeBiaya.TERMASUK);
                    t.setBiayaLain("Iuran keamanan;150000");
                });
        Tempat boothKemang = tempat(sari, "Booth Bazar Kuliner Kemang", "Booth Event", "Kemang, Jakarta Selatan",
                "Jl. Kemang Raya, area bazar Kemang", "2 × 2 m", "Baik: tenda dan meja disediakan",
                "Booth di area bazar kuliner akhir pekan dengan pengunjung ramai pada sore hingga malam.",
                "750000", SatuanPeriode.HARI, StatusVerifikasi.REJECTED, StatusTempat.AVAILABLE,
                List.of("demo-booth-1.svg", "demo-booth-2.svg"), fas, List.of("Listrik", "Air", "Meja & kursi"), "1 hari", t -> {
                    t.setListrikMode(ModeBiaya.PEMAKAIAN);
                    t.setListrikTarifKwh(new BigDecimal("1500"));
                    t.setAirMode(ModeBiaya.PEMAKAIAN);
                    t.setCatatanAdmin("Foto bagian dalam belum ada dan tarif listrik belum dijelaskan. Lengkapi lalu kirim ulang.");
                });

        // ---- milik pemilik lain ----
        Tempat kiosCipete = tempat(hendra, "Kios Lorong Pasar Cipete", "Kios", "Cipete, Jakarta Selatan",
                "Lorong Pasar Cipete Blok B No. 4", "3 × 3 m", "Baik: etalase kaca, rolling door berfungsi",
                "Kios di lorong pasar yang ramai dilewati pembeli harian.",
                "1250000", SatuanPeriode.BULAN, StatusVerifikasi.APPROVED, StatusTempat.UNAVAILABLE,
                List.of("demo-kios-2.svg"), fas, List.of("Listrik", "Etalase", "Rolling door"), "1 bulan", t -> {
                    t.setListrikMode(ModeBiaya.TETAP);
                    t.setListrikBiayaTetap(new BigDecimal("150000"));
                    t.setListrikEstimasiKwh(null);
                });
        Tempat kiosJagakarsa = tempat(hendra, "Kios Sudut Kampus Jagakarsa", "Kios", "Jagakarsa, Jakarta Selatan",
                "Jl. Raya Jagakarsa No. 31, dekat gerbang kampus", "3 × 4 m", "Baik: lantai keramik, ada etalase",
                "Kios sudut yang ramai oleh mahasiswa, cocok untuk minuman dan makanan ringan.",
                "2100000", SatuanPeriode.BULAN, StatusVerifikasi.APPROVED, StatusTempat.AVAILABLE,
                List.of("demo-kios-1.svg"), fas, List.of("Listrik", "Air", "Etalase"), "1 bulan", t -> {
                    t.setAirMode(ModeBiaya.TETAP);
                    t.setAirBiaya(new BigDecimal("75000"));
                });
        Tempat rukoCilandak = tempat(hendra, "Ruko Mungil Cilandak", "Ruko", "Cilandak, Jakarta Selatan",
                "Jl. Cilandak KKO No. 17", "4 × 10 m", "Baik: satu lantai, bersih",
                "Ruko mungil satu lantai untuk usaha jasa atau toko kecil.",
                "3800000", SatuanPeriode.BULAN, StatusVerifikasi.APPROVED, StatusTempat.AVAILABLE,
                List.of("demo-ruko-2.svg"), fas, List.of("Listrik", "Air", "Parkir motor"), "3 bulan", t -> {
                    t.setListrikMode(ModeBiaya.PEMAKAIAN);
                    t.setListrikTarifKwh(new BigDecimal("1500"));
                });
        Tempat rukoKebayoran = tempat(wati, "Ruko Satu Pintu Kebayoran Lama", "Ruko", "Kebayoran Lama, Jakarta Selatan",
                "Jl. Kebayoran Lama No. 90", "4 × 12 m", "Baik: pintu besi baru, toilet dalam",
                "Ruko satu pintu dengan area depan luas untuk parkir motor.",
                "4200000", SatuanPeriode.BULAN, StatusVerifikasi.APPROVED, StatusTempat.AVAILABLE,
                List.of("demo-ruko-1.svg"), fas, List.of("Listrik", "Air", "Toilet"), "3 bulan", t ->
                        t.setBiayaLain("Iuran kebersihan;100000"));
        tempat(wati, "Booth Pasar Akhir Pekan Bintaro", "Booth Event", "Bintaro, Tangerang Selatan",
                "Lapangan Bintaro Sektor 9, area pasar akhir pekan", "2 × 2 m", "Baik: area teduh, tenda tersedia",
                "Booth di pasar akhir pekan dengan pengunjung keluarga.",
                "600000", SatuanPeriode.HARI, StatusVerifikasi.APPROVED, StatusTempat.AVAILABLE,
                List.of("demo-booth-2.svg"), fas, List.of("Listrik", "Tenda", "Meja & kursi"), "1 hari", t -> {
                    t.setListrikMode(ModeBiaya.PEMAKAIAN);
                    t.setListrikTarifKwh(new BigDecimal("1500"));
                });
        // Listing dengan foto rusak/tidak ada: hanya bisa ditolak admin
        tempat(wati, "Booth Festival Jajan Cilandak", "Booth Event", "Cilandak, Jakarta Selatan",
                "Jl. Cilandak Timur, area festival", "2 × 3 m", "Baik",
                "Booth festival jajan.", "500000", SatuanPeriode.HARI, StatusVerifikasi.PENDING, StatusTempat.AVAILABLE,
                List.of(), fas, List.of("Listrik"), "1 hari", t -> {
                });

        LocalDate hari = LocalDate.now();

        // Booking Saya (Rina Lestari)
        booking(kiosPondokLabu, rina, hari.plusDays(12), hari.plusDays(101), StatusBooking.PENDING,
                "Saya berjualan piscok dan ingin mulai di awal bulan. Apakah listriknya cukup untuk kulkas kecil?");
        booking(rukoTebet, rina, hari.plusDays(40), hari.plusDays(129), StatusBooking.REJECTED, null);
        booking(kiosJagakarsa, rina, hari.minusDays(70), hari.minusDays(41), StatusBooking.COMPLETED, null);

        // Booking untuk tempat Bu Sari
        booking(boothKemang, dimas, hari.plusDays(9), hari.plusDays(11), StatusBooking.APPROVED, null);
        booking(kiosPondokLabu, sinta, hari.plusDays(30), hari.plusDays(119), StatusBooking.REJECTED, null);
        booking(rukoTebet, budi, hari.plusDays(50), hari.plusDays(139), StatusBooking.CANCELLED, null);

        // Dua pengajuan bertabrakan pada tempat lain: menguji aturan anti double-booking
        booking(rukoCilandak, dimas, hari.plusDays(14), hari.plusDays(103), StatusBooking.APPROVED, null);
        booking(rukoCilandak, budi, hari.plusDays(20), hari.plusDays(109), StatusBooking.PENDING, "Mohon konfirmasi segera.");
        booking(rukoKebayoran, sinta, hari.plusDays(25), hari.plusDays(114), StatusBooking.PENDING, null);

        Ulasan u = new Ulasan();
        u.setPenyewa(rina);
        u.setTempat(kiosJagakarsa);
        u.setRating(5);
        u.setKomentar("Lokasi ramai, pemilik responsif, dan fasilitas sesuai deskripsi. Cocok untuk usaha minuman.");
        ulasanRepository.save(u);
        // kiosCipete dipakai sebagai contoh tempat "Terisi"
        if (kiosCipete.getIdTempat() == null) {
            throw new IllegalStateException("Seed gagal");
        }
    }

    private PemilikTempat pemilik(String nama, String email, String telp) {
        PemilikTempat p = new PemilikTempat();
        p.setNama(nama);
        p.setEmail(email);
        p.setNoTelepon(telp);
        p.setPassword(passwordEncoder.encode("pemilik123"));
        return pemilikRepository.save(p);
    }

    private Penyewa penyewa(String nama, String email, String telp) {
        Penyewa p = new Penyewa();
        p.setNama(nama);
        p.setEmail(email);
        p.setNoTelepon(telp);
        p.setPassword(passwordEncoder.encode("penyewa123"));
        return penyewaRepository.save(p);
    }

    private Tempat tempat(PemilikTempat pemilik, String nama, String jenis, String lokasi, String alamat, String ukuran,
                          String kondisi, String deskripsi, String harga, SatuanPeriode satuan, StatusVerifikasi sv,
                          StatusTempat st, List<String> foto, Map<String, Fasilitas> master, List<String> fasilitas,
                          String durasiMinimal, Consumer<Tempat> biaya) {
        Tempat t = new Tempat();
        t.setPemilik(pemilik);
        t.setNamaTempat(nama);
        t.setJenisTempat(jenis);
        t.setLokasi(lokasi);
        t.setAlamatLengkap(alamat);
        t.setUkuran(ukuran);
        t.setKondisi(kondisi);
        t.setDeskripsi(deskripsi);
        t.setHargaSewa(new BigDecimal(harga));
        t.setSatuanPeriode(satuan);
        t.setStatusVerifikasi(sv);
        t.setStatusTempat(st);
        t.setDiajukanPada(LocalDate.now().minusDays(sv == StatusVerifikasi.PENDING ? 2 : 10));
        aturanStandar(t, durasiMinimal);
        biaya.accept(t);
        for (String f : fasilitas) {
            Fasilitas fas = master.get(f);
            if (fas != null) {
                t.getFasilitas().add(fas);
            }
        }
        for (String f : foto) {
            t.getFotoList().add(new FotoTempat(t, "/images/" + f));
        }
        return tempatRepository.save(t);
    }

    private void booking(Tempat t, Penyewa p, LocalDate mulai, LocalDate selesai, StatusBooking status, String catatan) {
        Booking b = new Booking();
        b.setTempat(t);
        b.setPenyewa(p);
        b.setTanggalMulai(mulai);
        b.setTanggalSelesai(selesai);
        b.setStatusBooking(status);
        b.setCatatan(catatan);
        bookingRepository.save(b);
    }
}
