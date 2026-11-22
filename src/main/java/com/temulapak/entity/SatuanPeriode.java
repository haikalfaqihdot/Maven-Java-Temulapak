package com.temulapak.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum SatuanPeriode {
    HARI("hari", "Per hari"),
    MINGGU("minggu", "Per minggu"),
    BULAN("bulan", "Per bulan");

    private final String satuan;
    private final String label;

    SatuanPeriode(String satuan, String label) {
        this.satuan = satuan;
        this.label = label;
    }

    public String getSatuan() {
        return satuan;
    }

    public String getLabel() {
        return label;
    }

    /** Jumlah periode untuk rentang tanggal (tanggal selesai ikut dihitung). */
    public long hitung(LocalDate mulai, LocalDate selesai) {
        long hari = ChronoUnit.DAYS.between(mulai, selesai) + 1;
        return switch (this) {
            case HARI -> hari;
            case MINGGU -> (hari + 6) / 7;
            case BULAN -> (hari + 29) / 30;
        };
    }
}
