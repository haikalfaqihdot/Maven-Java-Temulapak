package com.temulapak.entity;

import java.util.List;

public final class JenisTempat {
    public static final List<String> DAFTAR = List.of("Booth Event", "Kios", "Ruko");

    private JenisTempat() {
    }

    /** Mengembalikan nama jenis baku atau null jika tidak dikenal. "Booth" dianggap "Booth Event". */
    public static String normalisasi(String input) {
        if (input == null) {
            return null;
        }
        String v = input.trim();
        if (v.equalsIgnoreCase("Booth")) {
            return "Booth Event";
        }
        return DAFTAR.stream().filter(j -> j.equalsIgnoreCase(v)).findFirst().orElse(null);
    }
}
