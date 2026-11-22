package com.temulapak.security;

import com.temulapak.repository.AdminRepository;
import com.temulapak.repository.PemilikTempatRepository;
import com.temulapak.repository.PenyewaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Mencari akun pada tiga tabel (admin, pemilik_tempat, penyewa) berdasarkan email. */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PemilikTempatRepository pemilikRepository;
    private final PenyewaRepository penyewaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String e = email == null ? "" : email.trim();

        var admin = adminRepository.findByEmailIgnoreCase(e);
        if (admin.isPresent()) {
            var a = admin.get();
            return new AppUserDetails(a.getIdAdmin(), a.getNama(), a.getEmail(), a.getPassword(), Role.ADMIN);
        }
        var pemilik = pemilikRepository.findByEmailIgnoreCase(e);
        if (pemilik.isPresent()) {
            var p = pemilik.get();
            return new AppUserDetails(p.getIdPemilik(), p.getNama(), p.getEmail(), p.getPassword(), Role.PEMILIK);
        }
        var penyewa = penyewaRepository.findByEmailIgnoreCase(e);
        if (penyewa.isPresent()) {
            var p = penyewa.get();
            return new AppUserDetails(p.getIdPenyewa(), p.getNama(), p.getEmail(), p.getPassword(), Role.PENYEWA);
        }
        throw new UsernameNotFoundException("Akun tidak ditemukan");
    }
}
