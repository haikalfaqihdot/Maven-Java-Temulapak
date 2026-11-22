package com.temulapak.entity;

import com.temulapak.util.Format;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@Table(name = "tempat")
@Getter
@Setter
@NoArgsConstructor
public class Tempat {

    private static final Pattern POLA_DURASI = Pattern.compile("(\\d+)\\s*(hari|minggu|bulan)", Pattern.CASE_INSENSITIVE);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tempat")
    private Long idTempat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pemilik", nullable = false)
    private PemilikTempat pemilik;

    @Column(name = "nama_tempat", nullable = false, length = 150)
    private String namaTempat;

    @Column(name = "jenis_tempat", nullable = false, length = 50)
    private String jenisTempat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_verifikasi", nullable = false, length = 30)
    private StatusVerifikasi statusVerifikasi = StatusVerifikasi.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_tempat", nullable = false, length = 30)
    private StatusTempat statusTempat = StatusTempat.AVAILABLE;

    @Column(name = "harga_sewa", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSewa;

    @Column(name = "lokasi", nullable = false)
    private String lokasi;

    // ---- informasi tambahan (sesuai desain) ----
    @Column(name = "ukuran", length = 50)
    private String ukuran;

    @Column(name = "alamat_lengkap")
    private String alamatLengkap;

    @Column(name = "kondisi")
    private String kondisi;

    @Column(name = "deskripsi")
    private String deskripsi;

    // ---- harga dan biaya ----
    @Enumerated(EnumType.STRING)
    @Column(name = "satuan_periode", length = 20)
    private SatuanPeriode satuanPeriode = SatuanPeriode.BULAN;

    @Enumerated(EnumType.STRING)
    @Column(name = "listrik_mode", length = 20)
    private ModeBiaya listrikMode = ModeBiaya.TERMASUK;

    @Column(name = "listrik_biaya_tetap", precision = 15, scale = 2)
    private BigDecimal listrikBiayaTetap;

    @Column(name = "listrik_tarif_kwh", precision = 15, scale = 2)
    private BigDecimal listrikTarifKwh;

    @Column(name = "listrik_estimasi_kwh")
    private Integer listrikEstimasiKwh;

    @Enumerated(EnumType.STRING)
    @Column(name = "air_mode", length = 20)
    private ModeBiaya airMode = ModeBiaya.TERMASUK;

    @Column(name = "air_biaya", precision = 15, scale = 2)
    private BigDecimal airBiaya;

    /** Satu baris per biaya: "nama;nominal". */
    @Column(name = "biaya_lain")
    private String biayaLain;

    // ---- aturan sewa ----
    @Column(name = "durasi_minimal", length = 100)
    private String durasiMinimal;

    @Column(name = "perpanjangan")
    private String perpanjangan;

    @Column(name = "pembayaran")
    private String pembayaran;

    @Column(name = "pembatalan")
    private String pembatalan;

    @Column(name = "denda")
    private String denda;

    // ---- verifikasi ----
    @Column(name = "catatan_admin")
    private String catatanAdmin;

    @Column(name = "diajukan_pada")
    private LocalDate diajukanPada;

    @ManyToMany
    @JoinTable(name = "tempat_fasilitas",
            joinColumns = @JoinColumn(name = "id_tempat"),
            inverseJoinColumns = @JoinColumn(name = "id_fasilitas"))
    private Set<Fasilitas> fasilitas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tempat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idFoto ASC")
    private List<FotoTempat> fotoList = new ArrayList<>();

    @OneToMany(mappedBy = "tempat")
    @OrderBy("idUlasan DESC")
    private List<Ulasan> ulasanList = new ArrayList<>();

    @OneToMany(mappedBy = "tempat")
    private List<Booking> bookingList = new ArrayList<>();

    // ------------------------------------------------------------------ tampilan

    /** URL foto pertama, atau gambar placeholder sesuai jenis tempat. */
    public String getFotoUtama() {
        if (fotoList != null && !fotoList.isEmpty()) {
            return fotoList.get(0).getUrlFoto();
        }
        String jenis = jenisTempat == null ? "" : jenisTempat.toLowerCase();
        if (jenis.contains("booth")) {
            return "/images/placeholder-booth.svg";
        }
        return jenis.contains("kios") ? "/images/placeholder-kios.svg" : "/images/placeholder-ruko.svg";
    }

    public double getRataRating() {
        if (ulasanList == null || ulasanList.isEmpty()) {
            return 0;
        }
        return ulasanList.stream().mapToInt(Ulasan::getRating).average().orElse(0);
    }

    public int getJumlahUlasan() {
        return ulasanList == null ? 0 : ulasanList.size();
    }

    public boolean isTerverifikasi() {
        return statusVerifikasi == StatusVerifikasi.APPROVED;
    }

    public boolean isTersedia() {
        return statusTempat == StatusTempat.AVAILABLE;
    }

    /** Terisi: ditandai pemilik atau ada booking disetujui yang sedang berjalan. Menunggu: ada pengajuan aktif. */
    public StatusTampil getStatusTampil() {
        if (statusTempat == StatusTempat.UNAVAILABLE) {
            return StatusTampil.TERISI;
        }
        LocalDate hariIni = LocalDate.now();
        boolean menunggu = false;
        for (Booking b : bookingList) {
            if (b.getStatusBooking() == StatusBooking.APPROVED
                    && !hariIni.isBefore(b.getTanggalMulai()) && !hariIni.isAfter(b.getTanggalSelesai())) {
                return StatusTampil.TERISI;
            }
            if (b.getStatusBooking() == StatusBooking.PENDING && !b.getTanggalSelesai().isBefore(hariIni)) {
                menunggu = true;
            }
        }
        return menunggu ? StatusTampil.MENUNGGU : StatusTampil.TERSEDIA;
    }

    /** Bagian pertama lokasi, mis. "Pondok Labu" dari "Pondok Labu, Jakarta Selatan". */
    public String getAreaSingkat() {
        if (lokasi == null) {
            return "";
        }
        int i = lokasi.indexOf(',');
        return i > 0 ? lokasi.substring(0, i).trim() : lokasi;
    }

    public String getFasilitasRingkas() {
        return fasilitas.stream().map(Fasilitas::getNamaFasilitas).limit(4).collect(Collectors.joining(" · "));
    }

    // ------------------------------------------------------------------ biaya

    public SatuanPeriode satuan() {
        return satuanPeriode == null ? SatuanPeriode.BULAN : satuanPeriode;
    }

    public ModeBiaya modeListrik() {
        return listrikMode == null ? ModeBiaya.TERMASUK : listrikMode;
    }

    public ModeBiaya modeAir() {
        return airMode == null ? ModeBiaya.TERMASUK : airMode;
    }

    public List<BiayaLain> getDaftarBiayaLain() {
        List<BiayaLain> hasil = new ArrayList<>();
        if (biayaLain == null || biayaLain.isBlank()) {
            return hasil;
        }
        for (String baris : biayaLain.split("\n")) {
            int i = baris.lastIndexOf(';');
            if (i <= 0) {
                continue;
            }
            try {
                BigDecimal nominal = new BigDecimal(baris.substring(i + 1).trim());
                String nama = baris.substring(0, i).trim();
                if (!nama.isEmpty() && nominal.signum() > 0) {
                    hasil.add(new BiayaLain(nama, nominal));
                }
            } catch (NumberFormatException ignored) {
                // baris rusak dilewati
            }
        }
        return hasil;
    }

    private static boolean positif(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    /** Ringkasan satu baris untuk kartu: "Air Rp 75.000 / bulan + listrik sesuai pemakaian". */
    public String getRingkasBiaya() {
        String u = "/ " + satuan().getSatuan();
        List<String> tetap = new ArrayList<>();
        List<String> pakai = new ArrayList<>();

        if (modeListrik() == ModeBiaya.TETAP && positif(listrikBiayaTetap)) {
            tetap.add("Listrik " + Format.rupiah(listrikBiayaTetap) + " " + u);
        } else if (modeListrik() == ModeBiaya.PEMAKAIAN) {
            pakai.add("listrik");
        }
        if (modeAir() != ModeBiaya.TERMASUK && positif(airBiaya)) {
            tetap.add("Air " + Format.rupiah(airBiaya) + " " + u);
        } else if (modeAir() == ModeBiaya.PEMAKAIAN) {
            pakai.add("air");
        }
        for (BiayaLain b : getDaftarBiayaLain()) {
            tetap.add(b.getNama() + " " + Format.rupiah(b.getNominal()) + " " + u);
        }
        List<String> bagian = new ArrayList<>(tetap);
        if (!pakai.isEmpty()) {
            bagian.add(String.join(" & ", pakai) + " sesuai pemakaian");
        }
        if (bagian.isEmpty()) {
            return "Tanpa biaya tambahan";
        }
        StringBuilder sb = new StringBuilder(Format.kapital(bagian.get(0)));
        for (int i = 1; i < bagian.size(); i++) {
            sb.append(" + ").append(Format.kecil(bagian.get(i)));
        }
        return sb.toString();
    }

    /** Rincian biaya untuk sejumlah periode sewa. */
    public Rincian rincian(long n) {
        String u = satuan().getSatuan();
        String awal = n + " " + u + " × ";
        List<BarisBiaya> baris = new ArrayList<>();
        BigDecimal total = hargaSewa.multiply(BigDecimal.valueOf(n));
        StringBuilder dasar = new StringBuilder("harga pokok");
        boolean listrikPemakaian = modeListrik() == ModeBiaya.PEMAKAIAN;

        baris.add(new BarisBiaya("POKOK", "Harga sewa pokok", awal + Format.rupiah(hargaSewa), null,
                hargaSewa.multiply(BigDecimal.valueOf(n)), false, false));

        if (modeListrik() == ModeBiaya.TETAP && positif(listrikBiayaTetap)) {
            BigDecimal x = listrikBiayaTetap.multiply(BigDecimal.valueOf(n));
            total = total.add(x);
            baris.add(new BarisBiaya("LISTRIK", "Listrik", awal + Format.rupiah(listrikBiayaTetap), "Biaya tetap", x, false, false));
            dasar.append(" + listrik tetap");
        } else if (listrikPemakaian) {
            String tarif = positif(listrikTarifKwh) ? "Tarif " + Format.rupiah(listrikTarifKwh) + " / kWh" : null;
            if (positif(listrikTarifKwh) && listrikEstimasiKwh != null && listrikEstimasiKwh > 0) {
                BigDecimal x = listrikTarifKwh.multiply(BigDecimal.valueOf(listrikEstimasiKwh)).multiply(BigDecimal.valueOf(n));
                total = total.add(x);
                baris.add(new BarisBiaya("LISTRIK", "Listrik (estimasi)",
                        awal + listrikEstimasiKwh + " kWh × " + Format.rupiah(listrikTarifKwh),
                        "Sesuai pemakaian. " + tarif, x, true, false));
                dasar.append(" + listrik ").append(listrikEstimasiKwh).append(" kWh × ").append(Format.rupiah(listrikTarifKwh));
            } else {
                baris.add(new BarisBiaya("LISTRIK", "Listrik", tarif == null ? "Sesuai pemakaian" : tarif,
                        "Sesuai pemakaian" + (tarif == null ? "" : ". " + tarif), BigDecimal.ZERO, false, true));
            }
        }

        if (modeAir() != ModeBiaya.TERMASUK) {
            if (positif(airBiaya)) {
                BigDecimal x = airBiaya.multiply(BigDecimal.valueOf(n));
                total = total.add(x);
                boolean est = modeAir() == ModeBiaya.PEMAKAIAN;
                baris.add(new BarisBiaya("AIR", est ? "Air (estimasi)" : "Air", awal + Format.rupiah(airBiaya),
                        est ? "Sesuai pemakaian" : "Biaya tetap", x, est, false));
                dasar.append(est ? " + air (estimasi)" : " + air tetap");
            } else {
                baris.add(new BarisBiaya("AIR", "Air", "Sesuai pemakaian", "Sesuai pemakaian", BigDecimal.ZERO, false, true));
            }
        }

        for (BiayaLain b : getDaftarBiayaLain()) {
            BigDecimal x = b.getNominal().multiply(BigDecimal.valueOf(n));
            total = total.add(x);
            baris.add(new BarisBiaya("LAIN", b.getNama(), awal + Format.rupiah(b.getNominal()), "Biaya lainnya", x, false, false));
            dasar.append(" + ").append(Format.kecil(b.getNama()));
        }
        return new Rincian(baris, total, dasar.toString(), listrikPemakaian);
    }

    /** Durasi minimal dalam hari bila teksnya dapat dibaca (mis. "3 bulan"), selain itu null. */
    public Long durasiMinimalHari() {
        if (durasiMinimal == null) {
            return null;
        }
        Matcher m = POLA_DURASI.matcher(durasiMinimal);
        if (!m.find()) {
            return null;
        }
        long n = Long.parseLong(m.group(1));
        return switch (m.group(2).toLowerCase()) {
            case "minggu" -> n * 7;
            case "bulan" -> n * 30;
            default -> n;
        };
    }
}
