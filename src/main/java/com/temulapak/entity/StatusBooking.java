package com.temulapak.entity;

public enum StatusBooking {
    PENDING("Menunggu konfirmasi"),
    APPROVED("Dikonfirmasi"),
    REJECTED("Ditolak"),
    CANCELLED("Dibatalkan"),
    COMPLETED("Selesai");

    private final String label;

    StatusBooking(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
