package com.temulapak.service;

import com.temulapak.dto.UlasanForm;
import com.temulapak.entity.StatusBooking;
import com.temulapak.entity.Ulasan;
import com.temulapak.exception.BusinessException;
import com.temulapak.exception.ResourceNotFoundException;
import com.temulapak.repository.BookingRepository;
import com.temulapak.repository.PenyewaRepository;
import com.temulapak.repository.TempatRepository;
import com.temulapak.repository.UlasanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UlasanService {

    private final UlasanRepository ulasanRepository;
    private final BookingRepository bookingRepository;
    private final TempatRepository tempatRepository;
    private final PenyewaRepository penyewaRepository;

    /** Penyewa boleh mengulas sebanyak booking COMPLETED miliknya pada tempat tersebut. */
    @Transactional(readOnly = true)
    public boolean bisaMengulas(Long idPenyewa, Long idTempat) {
        long selesai = bookingRepository.countByTempat_IdTempatAndPenyewa_IdPenyewaAndStatusBooking(
                idTempat, idPenyewa, StatusBooking.COMPLETED);
        long sudah = ulasanRepository.countByPenyewa_IdPenyewaAndTempat_IdTempat(idPenyewa, idTempat);
        return selesai > sudah;
    }

    @Transactional
    public Ulasan tambah(Long idPenyewa, Long idTempat, UlasanForm form) {
        if (!tempatRepository.existsById(idTempat)) {
            throw new ResourceNotFoundException("Tempat tidak ditemukan.");
        }
        if (!bisaMengulas(idPenyewa, idTempat)) {
            throw new BusinessException("Ulasan hanya dapat diberikan setelah booking Anda pada tempat ini berstatus selesai.");
        }
        Ulasan u = new Ulasan();
        u.setPenyewa(penyewaRepository.getReferenceById(idPenyewa));
        u.setTempat(tempatRepository.getReferenceById(idTempat));
        u.setRating(form.getRating());
        String k = form.getKomentar() == null ? null : form.getKomentar().trim();
        u.setKomentar(k == null || k.isEmpty() ? null : k);
        return ulasanRepository.save(u);
    }

    @Transactional(readOnly = true)
    public List<Ulasan> semua() {
        return ulasanRepository.findAllByOrderByIdUlasanDesc();
    }
}
