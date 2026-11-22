package com.temulapak.service;

import com.temulapak.dto.ProfilForm;
import com.temulapak.dto.RegisterForm;
import com.temulapak.entity.PemilikTempat;
import com.temulapak.entity.Penyewa;
import com.temulapak.exception.BusinessException;
import com.temulapak.exception.ResourceNotFoundException;
import com.temulapak.repository.AdminRepository;
import com.temulapak.repository.PemilikTempatRepository;
import com.temulapak.repository.PenyewaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AkunService {

    private final AdminRepository adminRepository;
    private final PemilikTempatRepository pemilikRepository;
    private final PenyewaRepository penyewaRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean emailTerpakai(String email) {
        return adminRepository.existsByEmailIgnoreCase(email)
                || pemilikRepository.existsByEmailIgnoreCase(email)
                || penyewaRepository.existsByEmailIgnoreCase(email);
    }

    @Transactional
    public void daftar(RegisterForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (emailTerpakai(email)) {
            throw new BusinessException("Email sudah digunakan oleh akun lain.");
        }
        String hash = passwordEncoder.encode(form.getPassword());
        String telepon = form.getNoTelepon() == null ? null : form.getNoTelepon().trim();

        switch (form.getRole().toUpperCase()) {
            case "PEMILIK" -> {
                PemilikTempat p = new PemilikTempat();
                p.setNama(form.getNama().trim());
                p.setEmail(email);
                p.setNoTelepon(telepon);
                p.setPassword(hash);
                pemilikRepository.save(p);
            }
            case "PENYEWA" -> {
                Penyewa p = new Penyewa();
                p.setNama(form.getNama().trim());
                p.setEmail(email);
                p.setNoTelepon(telepon);
                p.setPassword(hash);
                penyewaRepository.save(p);
            }
            default -> throw new BusinessException("Jenis akun tidak valid.");
        }
    }

    public PemilikTempat ambilPemilik(Long id) {
        return pemilikRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Akun tidak ditemukan."));
    }

    public Penyewa ambilPenyewa(Long id) {
        return penyewaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Akun tidak ditemukan."));
    }

    @Transactional
    public void ubahProfilPemilik(Long id, ProfilForm form) {
        PemilikTempat p = ambilPemilik(id);
        p.setNama(form.getNama().trim());
        p.setNoTelepon(form.getNoTelepon() == null ? null : form.getNoTelepon().trim());
    }

    @Transactional
    public void ubahProfilPenyewa(Long id, ProfilForm form) {
        Penyewa p = ambilPenyewa(id);
        p.setNama(form.getNama().trim());
        p.setNoTelepon(form.getNoTelepon() == null ? null : form.getNoTelepon().trim());
    }
}
