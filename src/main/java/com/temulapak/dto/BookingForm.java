package com.temulapak.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class BookingForm {

    @NotNull(message = "Tanggal mulai wajib diisi")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tanggalMulai;

    @NotNull(message = "Tanggal selesai wajib diisi")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tanggalSelesai;

    @Size(max = 1000, message = "Catatan maksimal 1000 karakter")
    private String catatan;
}
