package com.temulapak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pemilik_tempat")
@Getter
@Setter
@NoArgsConstructor
public class PemilikTempat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pemilik")
    private Long idPemilik;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "no_telepon", length = 20)
    private String noTelepon;

    @Column(name = "password", nullable = false, length = 255)
    private String password;
}
