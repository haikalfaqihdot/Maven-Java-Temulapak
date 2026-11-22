package com.temulapak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ulasan")
@Getter
@Setter
@NoArgsConstructor
public class Ulasan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ulasan")
    private Long idUlasan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_penyewa", nullable = false)
    private Penyewa penyewa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tempat", nullable = false)
    private Tempat tempat;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "komentar")
    private String komentar;
}
