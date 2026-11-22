package com.temulapak.repository;

import com.temulapak.entity.Ulasan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UlasanRepository extends JpaRepository<Ulasan, Long> {

    long countByPenyewa_IdPenyewaAndTempat_IdTempat(Long idPenyewa, Long idTempat);

    long countByPenyewa_IdPenyewa(Long idPenyewa);

    List<Ulasan> findAllByOrderByIdUlasanDesc();
}
