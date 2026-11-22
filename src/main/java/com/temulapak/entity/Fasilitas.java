package com.temulapak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "fasilitas")
@Getter
@Setter
@NoArgsConstructor
public class Fasilitas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fasilitas")
    private Long idFasilitas;

    @Column(name = "nama_fasilitas", nullable = false, unique = true, length = 100)
    private String namaFasilitas;

    public Fasilitas(String namaFasilitas) {
        this.namaFasilitas = namaFasilitas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fasilitas other)) return false;
        return idFasilitas != null && Objects.equals(idFasilitas, other.getIdFasilitas());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
