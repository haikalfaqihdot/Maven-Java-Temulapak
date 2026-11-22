package com.temulapak.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UlasanForm {

    @NotNull(message = "Pilih rating 1 sampai 5")
    @Min(value = 1, message = "Rating minimal 1")
    @Max(value = 5, message = "Rating maksimal 5")
    private Integer rating;

    @Size(max = 1000, message = "Komentar maksimal 1000 karakter")
    private String komentar;
}
