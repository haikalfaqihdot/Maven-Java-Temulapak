package com.temulapak.repository;

import com.temulapak.entity.Fasilitas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FasilitasRepository extends JpaRepository<Fasilitas, Long> {
    List<Fasilitas> findAllByOrderByNamaFasilitasAsc();

    boolean existsByNamaFasilitasIgnoreCase(String nama);
}
