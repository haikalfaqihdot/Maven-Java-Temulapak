package com.temulapak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilForm {

    @NotBlank(message = "Nama wajib diisi")
    @Size(max = 100, message = "Nama maksimal 100 karakter")
    private String nama;

    @Pattern(regexp = "^$|^[0-9+\\-\\s]{6,20}$", message = "Nomor telepon hanya boleh berisi angka, spasi, + atau -")
    private String noTelepon;
}
