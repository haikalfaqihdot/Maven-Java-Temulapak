package com.temulapak.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** Hasil pemeriksaan kelengkapan listing oleh admin. */
@Getter
@AllArgsConstructor
public class PemeriksaanListing {

    @Getter
    @AllArgsConstructor
    public static class Cek {
        private final String label;
        private final boolean ok;
    }

    private final List<Cek> cek;
    private final boolean lengkap;
    private final String ringkas;
}
