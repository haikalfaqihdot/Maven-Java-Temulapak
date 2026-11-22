package com.temulapak.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BiayaLain {
    private final String nama;
    private final BigDecimal nominal;
}
