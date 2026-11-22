package com.temulapak.entity;

public enum StatusTempat {
    AVAILABLE("Tersedia"),
    UNAVAILABLE("Tidak tersedia");

    private final String label;

    StatusTempat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
