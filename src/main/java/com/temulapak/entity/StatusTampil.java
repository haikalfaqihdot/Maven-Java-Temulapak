package com.temulapak.entity;

/** Status tempat yang tampil ke pengguna, diturunkan dari booking yang ada. */
public enum StatusTampil {
    TERSEDIA("Tersedia", "available"),
    MENUNGGU("Menunggu konfirmasi", "pending"),
    TERISI("Terisi", "terisi");

    private final String label;
    private final String css;

    StatusTampil(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() {
        return label;
    }

    public String getCss() {
        return css;
    }
}
