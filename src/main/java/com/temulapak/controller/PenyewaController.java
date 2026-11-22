package com.temulapak.controller;

import com.temulapak.dto.BookingForm;
import com.temulapak.dto.UlasanForm;
import com.temulapak.entity.*;
import com.temulapak.exception.BusinessException;
import com.temulapak.security.AppUserDetails;
import com.temulapak.service.BookingService;
import com.temulapak.service.TempatService;
import com.temulapak.service.UlasanService;
import com.temulapak.util.Format;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/penyewa")
@RequiredArgsConstructor
public class PenyewaController {

    private final BookingService bookingService;
    private final TempatService tempatService;
    private final UlasanService ulasanService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/";
    }

    // ---------------------------------------------------------------- booking

    @GetMapping("/booking/{idTempat}")
    public String formBooking(@PathVariable Long idTempat, Model model) {
        siapkanFormBooking(idTempat, model);
        model.addAttribute("form", new BookingForm());
        return "penyewa/booking";
    }

    @PostMapping("/booking/{idTempat}")
    public String kirimBooking(@PathVariable Long idTempat,
                               @Valid @ModelAttribute("form") BookingForm form, BindingResult result,
                               @AuthenticationPrincipal AppUserDetails user, Model model) {
        if (!result.hasErrors()) {
            try {
                Booking b = bookingService.ajukan(idTempat, user.getId(), form.getTanggalMulai(),
                        form.getTanggalSelesai(), form.getCatatan());
                return "redirect:/penyewa/pengajuan/" + b.getIdBooking();
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        siapkanFormBooking(idTempat, model);
        return "penyewa/booking";
    }

    /** Rincian biaya untuk rentang tanggal terpilih (dipakai halaman Ajukan Booking). */
    @GetMapping("/booking/{idTempat}/rincian")
    @ResponseBody
    public Map<String, Object> rincian(@PathVariable Long idTempat,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mulai,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selesai) {
        Tempat t = tempatService.ambilPublik(idTempat);
        Map<String, Object> out = new LinkedHashMap<>();
        if (!selesai.isAfter(mulai)) {
            out.put("valid", false);
            out.put("pesan", "Tanggal selesai harus setelah tanggal mulai.");
            return out;
        }
        long n = t.satuan().hitung(mulai, selesai);
        Rincian r = t.rincian(n);
        List<Map<String, String>> baris = new ArrayList<>();
        for (BarisBiaya b : r.getBaris()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("label", b.getLabel());
            row.put("rumus", b.getRumus());
            row.put("nominal", b.isTanpaNominal() ? "Sesuai pemakaian" : Format.rupiah(b.getNominal()));
            baris.add(row);
        }
        String durasi = n + " " + t.satuan().getSatuan();
        Long minHari = t.durasiMinimalHari();
        long hari = java.time.temporal.ChronoUnit.DAYS.between(mulai, selesai) + 1;
        boolean kurang = minHari != null && hari < minHari;
        out.put("valid", true);
        out.put("durasi", durasi);
        out.put("durasiPesan", kurang
                ? "Durasi sewa: " + durasi + " (kurang dari durasi minimal " + t.getDurasiMinimal() + ")"
                : "Durasi sewa: " + durasi + (t.getDurasiMinimal() == null ? "" : " (sesuai durasi minimal " + t.getDurasiMinimal() + ")"));
        out.put("kurang", kurang);
        out.put("baris", baris);
        out.put("total", Format.rupiah(r.getTotal()));
        return out;
    }

    private void siapkanFormBooking(Long idTempat, Model model) {
        Tempat t = tempatService.ambilPublik(idTempat);
        model.addAttribute("tempat", t);
        model.addAttribute("periodeTerisi", bookingService.periodeTerisi(idTempat));
    }

    @GetMapping("/pengajuan/{id}")
    public String pengajuanTersimpan(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("booking", bookingService.ambilMilikPenyewa(id, user.getId()));
        return "penyewa/booking-sukses";
    }

    @GetMapping("/riwayat-booking")
    public String riwayat(@AuthenticationPrincipal AppUserDetails user, Model model) {
        List<Booking> daftar = bookingService.riwayatPenyewa(user.getId());
        Set<Long> bisaUlas = new HashSet<>();
        for (Booking b : daftar) {
            Long idTempat = b.getTempat().getIdTempat();
            if (b.getStatusBooking() == StatusBooking.COMPLETED && ulasanService.bisaMengulas(user.getId(), idTempat)) {
                bisaUlas.add(idTempat);
            }
        }
        model.addAttribute("daftar", daftar);
        model.addAttribute("bisaUlas", bisaUlas);
        return "penyewa/riwayat-booking";
    }

    @PostMapping("/booking/{id}/batal")
    public String batal(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, RedirectAttributes ra) {
        try {
            bookingService.batalkan(id, user.getId());
            ra.addFlashAttribute("success", "Pengajuan dibatalkan.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/penyewa/riwayat-booking";
    }

    // ---------------------------------------------------------------- ulasan

    @GetMapping("/ulasan/{idTempat}")
    public String formUlasan(@PathVariable Long idTempat, @AuthenticationPrincipal AppUserDetails user,
                             Model model, RedirectAttributes ra) {
        Tempat t = tempatService.ambilPublik(idTempat);
        if (!ulasanService.bisaMengulas(user.getId(), idTempat)) {
            ra.addFlashAttribute("error", "Ulasan hanya dapat diberikan setelah masa sewa Anda pada tempat ini selesai.");
            return "redirect:/penyewa/riwayat-booking";
        }
        model.addAttribute("tempat", t);
        model.addAttribute("form", new UlasanForm());
        return "penyewa/ulasan";
    }

    @PostMapping("/ulasan/{idTempat}")
    public String kirimUlasan(@PathVariable Long idTempat,
                              @Valid @ModelAttribute("form") UlasanForm form, BindingResult result,
                              @AuthenticationPrincipal AppUserDetails user,
                              Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                ulasanService.tambah(user.getId(), idTempat, form);
                ra.addFlashAttribute("success", "Terima kasih, ulasan Anda sudah dipublikasikan.");
                return "redirect:/tempat/" + idTempat;
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("tempat", tempatService.ambilPublik(idTempat));
        return "penyewa/ulasan";
    }
}
