package com.temulapak.entity;

public enum StatusVerifikasi {
    PENDING("Menunggu verifikasi"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak");

    private final String label;

    StatusVerifikasi(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
