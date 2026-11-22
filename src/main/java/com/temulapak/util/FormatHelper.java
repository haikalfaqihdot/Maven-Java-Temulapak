package com.temulapak.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dipakai dari Thymeleaf: ${@fmt.rupiah(x)}. */
@Component("fmt")
public class FormatHelper {

    public String rupiah(BigDecimal v) {
        return Format.rupiah(v);
    }

    public String angka(BigDecimal v) {
        return Format.angka(v);
    }

    public String tanggal(LocalDate d) {
        return Format.tanggal(d);
    }

    public String tanggalSingkat(LocalDate d) {
        return Format.tanggalSingkat(d);
    }

    public String periode(LocalDate a, LocalDate b) {
        return Format.periode(a, b, false);
    }

    public String periodeSingkat(LocalDate a, LocalDate b) {
        return Format.periode(a, b, true);
    }
}
