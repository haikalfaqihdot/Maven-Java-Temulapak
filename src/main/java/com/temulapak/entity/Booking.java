package com.temulapak.entity;

import com.temulapak.util.Format;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_booking")
    private Long idBooking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tempat", nullable = false)
    private Tempat tempat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_penyewa", nullable = false)
    private Penyewa penyewa;

    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_booking", nullable = false, length = 30)
    private StatusBooking statusBooking = StatusBooking.PENDING;

    @Column(name = "catatan")
    private String catatan;

    /** Jumlah hari, tanggal selesai ikut dihitung. */
    public long getDurasiHari() {
        return ChronoUnit.DAYS.between(tanggalMulai, tanggalSelesai) + 1;
    }

    public long getJumlahPeriode() {
        return tempat.satuan().hitung(tanggalMulai, tanggalSelesai);
    }

    /** Contoh: "3 bulan". */
    public String getDurasiLabel() {
        return getJumlahPeriode() + " " + tempat.satuan().getSatuan();
    }

    public Rincian getRincian() {
        return tempat.rincian(getJumlahPeriode());
    }

    public BigDecimal getEstimasiTotal() {
        return getRincian().getTotal();
    }

    public String getPeriodeLabel() {
        return Format.periode(tanggalMulai, tanggalSelesai, false);
    }

    public String getPeriodeSingkat() {
        return Format.periode(tanggalMulai, tanggalSelesai, true);
    }
}
