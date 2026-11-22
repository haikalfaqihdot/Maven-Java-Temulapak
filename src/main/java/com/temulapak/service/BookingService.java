package com.temulapak.service;

import com.temulapak.entity.Booking;
import com.temulapak.entity.StatusBooking;
import com.temulapak.entity.StatusTempat;
import com.temulapak.entity.StatusVerifikasi;
import com.temulapak.entity.Tempat;
import com.temulapak.exception.BusinessException;
import com.temulapak.exception.ResourceNotFoundException;
import com.temulapak.repository.BookingRepository;
import com.temulapak.repository.PenyewaRepository;
import com.temulapak.repository.TempatRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TempatRepository tempatRepository;
    private final PenyewaRepository penyewaRepository;
    private final EntityManager entityManager;

    // ------------------------------------------------------------ penyewa

    @Transactional
    public Booking ajukan(Long idTempat, Long idPenyewa, LocalDate mulai, LocalDate selesai, String catatan) {
        if (mulai == null || selesai == null) {
            throw new BusinessException("Tanggal mulai dan tanggal selesai wajib diisi.");
        }
        if (!selesai.isAfter(mulai)) {
            throw new BusinessException("Tanggal selesai harus lebih besar dari tanggal mulai.");
        }
        if (mulai.isBefore(LocalDate.now())) {
            throw new BusinessException("Tanggal mulai tidak boleh sebelum hari ini.");
        }
        Tempat t = tempatRepository.findById(idTempat)
                .orElseThrow(() -> new ResourceNotFoundException("Tempat tidak ditemukan."));
        if (t.getStatusVerifikasi() != StatusVerifikasi.APPROVED) {
            throw new BusinessException("Booking hanya dapat diajukan untuk tempat yang sudah terverifikasi.");
        }
        if (t.getStatusTempat() != StatusTempat.AVAILABLE) {
            throw new BusinessException("Tempat ini sedang tidak tersedia untuk booking baru.");
        }
        Long minHari = t.durasiMinimalHari();
        long hari = ChronoUnit.DAYS.between(mulai, selesai) + 1;
        if (minHari != null && hari < minHari) {
            throw new BusinessException("Durasi sewa minimal " + t.getDurasiMinimal() + ".");
        }
        if (bookingRepository.hitungBentrok(idTempat, StatusBooking.APPROVED, mulai, selesai, -1L) > 0) {
            throw new BusinessException("Periode yang dipilih sudah terisi oleh booking lain yang disetujui. Pilih tanggal lain.");
        }
        Booking b = new Booking();
        b.setTempat(t);
        b.setPenyewa(penyewaRepository.getReferenceById(idPenyewa));
        b.setTanggalMulai(mulai);
        b.setTanggalSelesai(selesai);
        b.setStatusBooking(StatusBooking.PENDING);
        b.setCatatan(catatan == null || catatan.isBlank() ? null : catatan.trim());
        return bookingRepository.save(b);
    }

    @Transactional
    public void batalkan(Long idBooking, Long idPenyewa) {
        Booking b = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new ResourceNotFoundException("Booking tidak ditemukan."));
        if (!b.getPenyewa().getIdPenyewa().equals(idPenyewa)) {
            throw new AccessDeniedException("Booking ini bukan milik Anda.");
        }
        if (b.getStatusBooking() != StatusBooking.PENDING) {
            throw new BusinessException("Hanya pengajuan yang masih menunggu konfirmasi yang bisa dibatalkan.");
        }
        b.setStatusBooking(StatusBooking.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Booking ambilMilikPenyewa(Long idBooking, Long idPenyewa) {
        Booking b = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new ResourceNotFoundException("Booking tidak ditemukan."));
        if (!b.getPenyewa().getIdPenyewa().equals(idPenyewa)) {
            throw new AccessDeniedException("Booking ini bukan milik Anda.");
        }
        return b;
    }

    @Transactional(readOnly = true)
    public List<Booking> riwayatPenyewa(Long idPenyewa) {
        return bookingRepository.findByPenyewa_IdPenyewaOrderByIdBookingDesc(idPenyewa);
    }

    @Transactional(readOnly = true)
    public List<Booking> periodeTerisi(Long idTempat) {
        return bookingRepository.findByTempat_IdTempatAndStatusBookingOrderByTanggalMulaiAsc(idTempat, StatusBooking.APPROVED);
    }

    // ------------------------------------------------------------ pemilik

    @Transactional(readOnly = true)
    public List<Booking> bookingPemilik(Long idPemilik, StatusBooking status) {
        if (status == null) {
            return bookingRepository.findByTempat_Pemilik_IdPemilikOrderByIdBookingDesc(idPemilik);
        }
        return bookingRepository.findByTempat_Pemilik_IdPemilikAndStatusBookingOrderByIdBookingDesc(idPemilik, status);
    }

    @Transactional(readOnly = true)
    public Booking ambilMilikPemilik(Long idBooking, Long idPemilik) {
        Booking b = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new ResourceNotFoundException("Booking tidak ditemukan."));
        if (!b.getTempat().getPemilik().getIdPemilik().equals(idPemilik)) {
            throw new AccessDeniedException("Booking ini bukan untuk tempat milik Anda.");
        }
        return b;
    }

    /** True bila booking ini bertabrakan dengan booking lain berstatus APPROVED pada tempat yang sama. */
    @Transactional(readOnly = true)
    public boolean bentrok(Booking b) {
        return bookingRepository.hitungBentrok(b.getTempat().getIdTempat(), StatusBooking.APPROVED,
                b.getTanggalMulai(), b.getTanggalSelesai(), b.getIdBooking()) > 0;
    }

    /**
     * Menyetujui booking. Baris tempat dikunci (SELECT ... FOR UPDATE) sehingga dua persetujuan
     * pada tempat yang sama diproses bergantian dan konflik periode selalu terdeteksi.
     */
    @Transactional
    public void setujui(Long idBooking, Long idPemilik) {
        Booking b = ambilMilikPemilik(idBooking, idPemilik);
        tempatRepository.findByIdForUpdate(b.getTempat().getIdTempat())
                .orElseThrow(() -> new ResourceNotFoundException("Tempat tidak ditemukan."));
        entityManager.refresh(b); // baca ulang status terbaru setelah kunci didapat

        if (b.getStatusBooking() != StatusBooking.PENDING) {
            throw new BusinessException("Hanya booking berstatus menunggu yang dapat disetujui.");
        }
        if (b.getTanggalSelesai().isBefore(LocalDate.now())) {
            throw new BusinessException("Periode booking ini sudah lewat dan tidak dapat disetujui.");
        }
        if (bentrok(b)) {
            throw new BusinessException("Booking tidak dapat disetujui karena periodenya bertabrakan dengan booking lain yang sudah disetujui.");
        }
        b.setStatusBooking(StatusBooking.APPROVED);
    }

    @Transactional
    public void tolak(Long idBooking, Long idPemilik) {
        Booking b = ambilMilikPemilik(idBooking, idPemilik);
        if (b.getStatusBooking() != StatusBooking.PENDING) {
            throw new BusinessException("Hanya booking berstatus menunggu yang dapat ditolak.");
        }
        b.setStatusBooking(StatusBooking.REJECTED);
    }

    @Transactional
    public void selesaikan(Long idBooking, Long idPemilik) {
        Booking b = ambilMilikPemilik(idBooking, idPemilik);
        if (b.getStatusBooking() != StatusBooking.APPROVED) {
            throw new BusinessException("Hanya booking yang sudah disetujui yang dapat ditandai selesai.");
        }
        if (LocalDate.now().isBefore(b.getTanggalMulai())) {
            throw new BusinessException("Booking baru dapat ditandai selesai pada atau setelah tanggal mulai.");
        }
        b.setStatusBooking(StatusBooking.COMPLETED);
    }

    // ------------------------------------------------------------ admin

    @Transactional(readOnly = true)
    public List<Booking> semua() {
        return bookingRepository.findAllByOrderByIdBookingDesc();
    }

    // ------------------------------------------------------------ terjadwal

    /** Booking APPROVED yang masa sewanya sudah lewat otomatis menjadi COMPLETED. */
    @Scheduled(initialDelay = 30_000, fixedDelay = 3_600_000)
    @Transactional
    public void selesaikanOtomatis() {
        bookingRepository.ubahStatusKadaluarsa(StatusBooking.APPROVED, StatusBooking.COMPLETED, LocalDate.now());
    }
}
