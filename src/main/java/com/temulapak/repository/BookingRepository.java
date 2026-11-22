package com.temulapak.repository;

import com.temulapak.entity.Booking;
import com.temulapak.entity.StatusBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPenyewa_IdPenyewaOrderByIdBookingDesc(Long idPenyewa);

    List<Booking> findByTempat_Pemilik_IdPemilikOrderByIdBookingDesc(Long idPemilik);

    List<Booking> findByTempat_Pemilik_IdPemilikAndStatusBookingOrderByIdBookingDesc(Long idPemilik, StatusBooking status);

    List<Booking> findAllByOrderByIdBookingDesc();

    List<Booking> findByTempat_IdTempatAndStatusBookingOrderByTanggalMulaiAsc(Long idTempat, StatusBooking status);

    long countByPenyewa_IdPenyewa(Long idPenyewa);

    long countByPenyewa_IdPenyewaAndStatusBooking(Long idPenyewa, StatusBooking status);

    long countByTempat_Pemilik_IdPemilik(Long idPemilik);

    long countByTempat_Pemilik_IdPemilikAndStatusBooking(Long idPemilik, StatusBooking status);

    long countByStatusBooking(StatusBooking status);

    long countByTempat_IdTempatAndPenyewa_IdPenyewaAndStatusBooking(Long idTempat, Long idPenyewa, StatusBooking status);

    long countByTempat_IdTempatAndStatusBookingIn(Long idTempat, Collection<StatusBooking> status);

    /**
     * Jumlah booking dengan status tertentu (APPROVED) yang periodenya bertabrakan.
     * Bertabrakan jika existingStart <= requestedEnd && existingEnd >= requestedStart.
     */
    @Query("select count(b) from Booking b where b.tempat.idTempat = :idTempat "
            + "and b.statusBooking = :status "
            + "and b.tanggalMulai <= :selesai and b.tanggalSelesai >= :mulai "
            + "and b.idBooking <> :excludeId")
    long hitungBentrok(@Param("idTempat") Long idTempat,
                       @Param("status") StatusBooking status,
                       @Param("mulai") LocalDate mulai,
                       @Param("selesai") LocalDate selesai,
                       @Param("excludeId") Long excludeId);

    @Modifying
    @Query("update Booking b set b.statusBooking = :ke where b.statusBooking = :dari and b.tanggalSelesai < :hariIni")
    int ubahStatusKadaluarsa(@Param("dari") StatusBooking dari,
                             @Param("ke") StatusBooking ke,
                             @Param("hariIni") LocalDate hariIni);
}
