package com.temulapak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "penyewa")
@Getter
@Setter
@NoArgsConstructor
public class Penyewa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penyewa")
    private Long idPenyewa;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "no_telepon", length = 20)
    private String noTelepon;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Contoh: 0812-xxxx-xxxx. */
    public String getNoTeleponSamar() {
        if (noTelepon == null || noTelepon.replaceAll("\\D", "").length() < 4) {
            return "-";
        }
        return noTelepon.replaceAll("\\D", "").substring(0, 4) + "-xxxx-xxxx";
    }
}
