package com.temulapak.security;

public enum Role {
    ADMIN("Admin", "/admin", "/admin/verifikasi"),
    PEMILIK("Pemilik", "/pemilik", "/pemilik/tempat"),
    PENYEWA("Penyewa", "/penyewa", "/");

    private final String label;
    private final String prefix;
    private final String home;

    Role(String label, String prefix, String home) {
        this.label = label;
        this.prefix = prefix;
        this.home = home;
    }

    public String getLabel() {
        return label;
    }

    /** Awalan URL area untuk peran ini. */
    public String getPrefix() {
        return prefix;
    }

    /** Halaman tujuan setelah masuk. */
    public String getHome() {
        return home;
    }
}
