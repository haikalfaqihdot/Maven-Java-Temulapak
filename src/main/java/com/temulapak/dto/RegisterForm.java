package com.temulapak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama maksimal 100 karakter")
    private String nama;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 150, message = "Email maksimal 150 karakter")
    private String email;

    @NotBlank(message = "Nomor telepon wajib diisi")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Nomor telepon harus 10 sampai 15 digit angka")
    private String noTelepon;

    @NotBlank(message = "Kata sandi wajib diisi")
    @Size(min = 8, max = 100, message = "Kata sandi minimal 8 karakter")
    private String password;

    @NotBlank(message = "Pilih peran")
    private String role = "PENYEWA";
}
