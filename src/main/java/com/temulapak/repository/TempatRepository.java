package com.temulapak.repository;

import com.temulapak.entity.StatusTempat;
import com.temulapak.entity.StatusVerifikasi;
import com.temulapak.entity.Tempat;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TempatRepository extends JpaRepository<Tempat, Long>, JpaSpecificationExecutor<Tempat> {

    List<Tempat> findByPemilik_IdPemilikOrderByIdTempatDesc(Long idPemilik);

    long countByPemilik_IdPemilik(Long idPemilik);

    long countByPemilik_IdPemilikAndStatusVerifikasi(Long idPemilik, StatusVerifikasi status);

    long countByPemilik_IdPemilikAndStatusVerifikasiAndStatusTempat(Long idPemilik, StatusVerifikasi v, StatusTempat s);

    List<Tempat> findByStatusVerifikasiOrderByIdTempatAsc(StatusVerifikasi status);

    List<Tempat> findAllByOrderByIdTempatDesc();

    List<Tempat> findByStatusVerifikasiOrderByIdTempatDesc(StatusVerifikasi status, Pageable pageable);

    long countByStatusVerifikasi(StatusVerifikasi status);

    long countByStatusVerifikasiAndJenisTempatIgnoreCase(StatusVerifikasi status, String jenis);

    /** Mengunci baris tempat agar persetujuan booking tidak berjalan bersamaan. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tempat t where t.idTempat = :id")
    Optional<Tempat> findByIdForUpdate(@Param("id") Long id);
}
