package com.temulapak.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** Satu baris rincian biaya. */
@Getter
@AllArgsConstructor
public class BarisBiaya {
    private final String jenis;     // POKOK, LISTRIK, AIR, LAIN
    private final String label;
    private final String rumus;     // contoh: "3 bulan × Rp 1.800.000"
    private final String catatan;   // contoh: "Biaya tetap"
    private final BigDecimal nominal;
    private final boolean estimasi;
    private final boolean tanpaNominal; // true: "sesuai pemakaian" tanpa angka
}
