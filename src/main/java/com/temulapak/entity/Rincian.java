package com.temulapak.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class Rincian {
    private final List<BarisBiaya> baris;
    private final BigDecimal total;
    private final String dasar;
    private final boolean listrikPemakaian;
}
