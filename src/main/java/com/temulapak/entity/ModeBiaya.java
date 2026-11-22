package com.temulapak.entity;

public enum ModeBiaya {
    TERMASUK("Termasuk harga sewa"),
    TETAP("Biaya tetap"),
    PEMAKAIAN("Sesuai pemakaian");

    private final String label;

    ModeBiaya(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
