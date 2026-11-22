package com.temulapak.repository;

import com.temulapak.entity.PemilikTempat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PemilikTempatRepository extends JpaRepository<PemilikTempat, Long> {
    Optional<PemilikTempat> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<PemilikTempat> findAllByOrderByIdPemilikDesc();
}
