package com.temulapak.controller;

import com.temulapak.entity.SatuanPeriode;
import com.temulapak.entity.Tempat;
import com.temulapak.security.AppUserDetails;
import com.temulapak.security.Role;
import com.temulapak.service.BookingService;
import com.temulapak.service.TempatService;
import com.temulapak.service.UlasanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TempatService tempatService;
    private final BookingService bookingService;
    private final UlasanService ulasanService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("terbaru", tempatService.terbaru(3));
        return "public/index";
    }

    @GetMapping("/tempat")
    public String pencarian(@RequestParam(required = false) String lokasi,
                            @RequestParam(required = false) List<String> jenis,
                            @RequestParam(required = false) List<String> periode,
                            @RequestParam(required = false) List<Long> fas,
                            @RequestParam(required = false) String anggaran,
                            @RequestParam(required = false) BigDecimal min,
                            @RequestParam(required = false) BigDecimal max,
                            @RequestParam(required = false) String sort,
                            @RequestParam(defaultValue = "6") int tampil,
                            Model model) {
        // Pilihan "anggaran sewa" (mis. 1000000-7000000) dipakai bila min/max tidak diisi.
        if (min == null && max == null && anggaran != null && anggaran.matches("\\d*-\\d*") && anggaran.length() > 1) {
            String[] p = anggaran.split("-", -1);
            min = p[0].isEmpty() ? null : new BigDecimal(p[0]);
            max = p[1].isEmpty() ? null : new BigDecimal(p[1]);
        }

        List<SatuanPeriode> satuan = new ArrayList<>();
        if (periode != null) {
            for (String p : periode) {
                try {
                    satuan.add(SatuanPeriode.valueOf(p.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    // abaikan nilai yang tidak dikenal
                }
            }
        }
        List<String> jenisDipilih = jenis == null ? List.of() : jenis.stream().filter(j -> !j.isBlank()).toList();

        Page<Tempat> hasil = tempatService.cari(lokasi, jenisDipilih, satuan, fas, min, max, sort, tampil);
        model.addAttribute("hasil", hasil);
        model.addAttribute("lokasi", lokasi);
        model.addAttribute("jenisDipilih", jenisDipilih);
        model.addAttribute("periodeDipilih", satuan.stream().map(Enum::name).toList());
        model.addAttribute("fasDipilih", fas == null ? List.of() : fas);
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("anggaranKunci", (min == null ? "" : min.toPlainString()) + "-" + (max == null ? "" : max.toPlainString()));
        model.addAttribute("sort", sort == null || sort.isBlank() ? "harga" : sort);
        model.addAttribute("tampil", Math.max(TempatService.LANGKAH_TAMPIL, tampil));
        model.addAttribute("semuaFasilitas", tempatService.semuaFasilitas());
        return "public/pencarian";
    }

    @GetMapping("/tempat/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, Model model) {
        Tempat t = tempatService.ambilPublik(id);
        model.addAttribute("tempat", t);
        model.addAttribute("periodeTerisi", bookingService.periodeTerisi(id));
        boolean bisaUlas = user != null && user.getRole() == Role.PENYEWA
                && ulasanService.bisaMengulas(user.getId(), id);
        model.addAttribute("bisaUlas", bisaUlas);
        return "public/detail-tempat";
    }
}
