package com.temulapak.dto;

import com.temulapak.entity.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TempatForm {

    // 1. Informasi tempat
    @NotBlank(message = "Nama tempat wajib diisi")
    @Size(max = 150, message = "Nama tempat maksimal 150 karakter")
    private String namaTempat;

    @NotBlank(message = "Pilih jenis tempat")
    private String jenisTempat;

    @NotBlank(message = "Lokasi wajib diisi")
    @Size(max = 200, message = "Lokasi maksimal 200 karakter")
    private String lokasi;

    @Size(max = 50, message = "Ukuran maksimal 50 karakter")
    private String ukuran;

    @NotBlank(message = "Alamat lengkap wajib diisi")
    @Size(max = 500, message = "Alamat maksimal 500 karakter")
    private String alamatLengkap;

    @NotBlank(message = "Kondisi tempat wajib diisi")
    @Size(max = 1000, message = "Kondisi maksimal 1000 karakter")
    private String kondisi;

    @Size(max = 2000, message = "Deskripsi maksimal 2000 karakter")
    private String deskripsi;

    private StatusTempat statusTempat = StatusTempat.AVAILABLE;

    // 2. Fasilitas dan foto
    private List<Long> fasilitasIds = new ArrayList<>();
    private List<Long> hapusFotoIds = new ArrayList<>();

    // 3. Harga dan biaya
    @NotNull(message = "Harga sewa wajib diisi")
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga sewa harus lebih dari 0")
    @Digits(integer = 13, fraction = 2, message = "Harga sewa terlalu besar")
    private BigDecimal hargaSewa;

    @NotNull(message = "Pilih satuan periode")
    private SatuanPeriode satuanPeriode = SatuanPeriode.BULAN;

    @NotNull(message = "Pilih skema biaya listrik")
    private ModeBiaya listrikMode = ModeBiaya.TERMASUK;
    private BigDecimal listrikBiayaTetap;
    private BigDecimal listrikTarifKwh;
    private Integer listrikEstimasiKwh;

    @NotNull(message = "Pilih skema biaya air")
    private ModeBiaya airMode = ModeBiaya.TERMASUK;
    private BigDecimal airBiaya;

    private List<String> biayaLainNama = new ArrayList<>();
    private List<String> biayaLainNominal = new ArrayList<>();

    // 4. Aturan sewa
    @NotBlank(message = "Durasi minimal wajib diisi")
    @Size(max = 100, message = "Durasi minimal maksimal 100 karakter")
    private String durasiMinimal;

    @NotBlank(message = "Aturan perpanjangan wajib diisi")
    @Size(max = 500, message = "Maksimal 500 karakter")
    private String perpanjangan;

    @NotBlank(message = "Aturan pembayaran wajib diisi")
    @Size(max = 1000, message = "Maksimal 1000 karakter")
    private String pembayaran;

    @NotBlank(message = "Aturan pembatalan wajib diisi")
    @Size(max = 1000, message = "Maksimal 1000 karakter")
    private String pembatalan;

    @NotBlank(message = "Denda keterlambatan wajib diisi")
    @Size(max = 1000, message = "Maksimal 1000 karakter")
    private String denda;

    private static BigDecimal bersih(BigDecimal v) {
        if (v == null) {
            return null;
        }
        BigDecimal s = v.stripTrailingZeros();
        return s.scale() < 0 ? s.setScale(0) : s;
    }

    public static TempatForm dari(Tempat t) {
        TempatForm f = new TempatForm();
        f.setNamaTempat(t.getNamaTempat());
        f.setJenisTempat(t.getJenisTempat());
        f.setLokasi(t.getLokasi());
        f.setUkuran(t.getUkuran());
        f.setAlamatLengkap(t.getAlamatLengkap());
        f.setKondisi(t.getKondisi());
        f.setDeskripsi(t.getDeskripsi());
        f.setStatusTempat(t.getStatusTempat());
        t.getFasilitas().forEach(x -> f.getFasilitasIds().add(x.getIdFasilitas()));
        f.setHargaSewa(bersih(t.getHargaSewa()));
        f.setSatuanPeriode(t.satuan());
        f.setListrikMode(t.modeListrik());
        f.setListrikBiayaTetap(bersih(t.getListrikBiayaTetap()));
        f.setListrikTarifKwh(bersih(t.getListrikTarifKwh()));
        f.setListrikEstimasiKwh(t.getListrikEstimasiKwh());
        f.setAirMode(t.modeAir());
        f.setAirBiaya(bersih(t.getAirBiaya()));
        for (BiayaLain b : t.getDaftarBiayaLain()) {
            f.getBiayaLainNama().add(b.getNama());
            f.getBiayaLainNominal().add(bersih(b.getNominal()).toPlainString());
        }
        f.setDurasiMinimal(t.getDurasiMinimal());
        f.setPerpanjangan(t.getPerpanjangan());
        f.setPembayaran(t.getPembayaran());
        f.setPembatalan(t.getPembatalan());
        f.setDenda(t.getDenda());
        return f;
    }
}
