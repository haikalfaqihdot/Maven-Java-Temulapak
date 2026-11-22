package com.temulapak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foto_tempat")
@Getter
@Setter
@NoArgsConstructor
public class FotoTempat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Long idFoto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tempat", nullable = false)
    private Tempat tempat;

    @Column(name = "url_foto", nullable = false, length = 500)
    private String urlFoto;

    public FotoTempat(Tempat tempat, String urlFoto) {
        this.tempat = tempat;
        this.urlFoto = urlFoto;
    }
}
