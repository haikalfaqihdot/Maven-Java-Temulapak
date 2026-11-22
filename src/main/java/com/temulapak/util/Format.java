package com.temulapak.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Format angka dan tanggal bergaya Indonesia (1.800.000 dan 1 Desember 2026). */
public final class Format {

    private static final String[] BULAN = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
            "Agustus", "September", "Oktober", "November", "Desember"};
    private static final String[] BULAN_S = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep",
            "Okt", "Nov", "Des"};

    private Format() {
    }

    public static String angka(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        long x = v.setScale(0, RoundingMode.HALF_UP).longValue();
        String s = Long.toString(Math.abs(x));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(s.charAt(i));
        }
        return (x < 0 ? "-" : "") + sb;
    }

    public static String rupiah(BigDecimal v) {
        return "Rp " + angka(v);
    }

    public static String tanggal(LocalDate d) {
        return d == null ? "-" : d.getDayOfMonth() + " " + BULAN[d.getMonthValue() - 1] + " " + d.getYear();
    }

    public static String tanggalSingkat(LocalDate d) {
        return d == null ? "-" : d.getDayOfMonth() + " " + BULAN_S[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** Rentang tanggal ringkas: "12 – 14 November 2026", "1 Januari – 31 Maret 2027", dst. */
    public static String periode(LocalDate a, LocalDate b, boolean singkat) {
        if (a == null || b == null) {
            return "-";
        }
        String[] nama = singkat ? BULAN_S : BULAN;
        String ma = nama[a.getMonthValue() - 1];
        String mb = nama[b.getMonthValue() - 1];
        if (a.getYear() == b.getYear() && a.getMonthValue() == b.getMonthValue()) {
            return a.getDayOfMonth() + " – " + b.getDayOfMonth() + " " + mb + " " + b.getYear();
        }
        if (a.getYear() == b.getYear()) {
            return a.getDayOfMonth() + " " + ma + " – " + b.getDayOfMonth() + " " + mb + " " + b.getYear();
        }
        return a.getDayOfMonth() + " " + ma + " " + a.getYear() + " – " + b.getDayOfMonth() + " " + mb + " " + b.getYear();
    }

    public static String kapital(String s) {
        return (s == null || s.isEmpty()) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String kecil(String s) {
        return (s == null || s.isEmpty()) ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
