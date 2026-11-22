package com.temulapak.repository;

import com.temulapak.entity.Penyewa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PenyewaRepository extends JpaRepository<Penyewa, Long> {
    Optional<Penyewa> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Penyewa> findAllByOrderByIdPenyewaDesc();
}
