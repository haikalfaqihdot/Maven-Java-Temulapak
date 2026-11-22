package com.temulapak.controller;

import com.temulapak.entity.StatusVerifikasi;
import com.temulapak.entity.Tempat;
import com.temulapak.exception.BusinessException;
import com.temulapak.repository.PemilikTempatRepository;
import com.temulapak.repository.PenyewaRepository;
import com.temulapak.service.BookingService;
import com.temulapak.service.PemeriksaanListing;
import com.temulapak.service.TempatService;
import com.temulapak.service.UlasanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TempatService tempatService;
    private final BookingService bookingService;
    private final UlasanService ulasanService;
    private final PemilikTempatRepository pemilikRepository;
    private final PenyewaRepository penyewaRepository;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/admin/verifikasi";
    }

    @GetMapping("/verifikasi")
    public String verifikasi(@RequestParam(required = false) String status, Model model) {
        StatusVerifikasi filter = StatusVerifikasi.PENDING;
        if (status != null && !status.isBlank()) {
            try {
                filter = StatusVerifikasi.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // pakai default
            }
        }
        List<Tempat> daftar = tempatService.daftarVerifikasi(filter);
        Map<Long, PemeriksaanListing> periksa = new LinkedHashMap<>();
        daftar.forEach(t -> periksa.put(t.getIdTempat(), tempatService.periksa(t)));
        model.addAttribute("daftar", daftar);
        model.addAttribute("periksa", periksa);
        model.addAttribute("filter", filter);
        return "admin/verifikasi";
    }

    @GetMapping("/verifikasi/{id}")
    public String detailVerifikasi(@PathVariable Long id, Model model) {
        Tempat t = tempatService.ambil(id);
        model.addAttribute("tempat", t);
        model.addAttribute("pemeriksaan", tempatService.periksa(t));
        return "admin/verifikasi-detail";
    }

    @PostMapping("/verifikasi/{id}/setuju")
    public String setuju(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tempatService.verifikasi(id, true, null);
            ra.addFlashAttribute("success", "Listing disetujui dan kini tampil di pencarian.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/verifikasi/" + id;
    }

    @PostMapping("/verifikasi/{id}/tolak")
    public String tolak(@PathVariable Long id, @RequestParam(required = false) String catatan, RedirectAttributes ra) {
        try {
            tempatService.verifikasi(id, false, catatan);
            ra.addFlashAttribute("success", "Listing ditolak. Catatan dikirim ke pemilik.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/verifikasi/" + id;
    }

    @GetMapping("/pengguna")
    public String pengguna(Model model) {
        model.addAttribute("pemilikList", pemilikRepository.findAllByOrderByIdPemilikDesc());
        model.addAttribute("penyewaList", penyewaRepository.findAllByOrderByIdPenyewaDesc());
        return "admin/pengguna";
    }

    @GetMapping("/tempat")
    public String tempat(Model model) {
        model.addAttribute("daftar", tempatService.semua());
        return "admin/tempat";
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        model.addAttribute("daftar", bookingService.semua());
        return "admin/booking";
    }

    @GetMapping("/ulasan")
    public String ulasan(Model model) {
        model.addAttribute("daftar", ulasanService.semua());
        return "admin/ulasan";
    }
}
