package com.temulapak.controller;

import com.temulapak.entity.Booking;
import com.temulapak.entity.StatusBooking;
import com.temulapak.exception.BusinessException;
import com.temulapak.security.AppUserDetails;
import com.temulapak.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pemilik/booking")
@RequiredArgsConstructor
public class PemilikBookingController {

    private final BookingService bookingService;

    @GetMapping
    public String daftar(@RequestParam(required = false) String status,
                         @AuthenticationPrincipal AppUserDetails user, Model model) {
        StatusBooking filter = null;
        if (status != null && !status.isBlank()) {
            try {
                filter = StatusBooking.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // abaikan filter yang tidak dikenal
            }
        }
        model.addAttribute("daftar", bookingService.bookingPemilik(user.getId(), filter));
        model.addAttribute("filter", filter);
        model.addAttribute("semuaStatus", StatusBooking.values());
        return "pemilik/booking/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, Model model) {
        Booking b = bookingService.ambilMilikPemilik(id, user.getId());
        model.addAttribute("booking", b);
        model.addAttribute("bentrok", b.getStatusBooking() == StatusBooking.PENDING && bookingService.bentrok(b));
        return "pemilik/booking/detail";
    }

    @PostMapping("/{id}/setuju")
    public String setuju(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, RedirectAttributes ra) {
        return aksi(id, ra, () -> {
            bookingService.setujui(id, user.getId());
            return "Booking disetujui.";
        });
    }

    @PostMapping("/{id}/tolak")
    public String tolak(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, RedirectAttributes ra) {
        return aksi(id, ra, () -> {
            bookingService.tolak(id, user.getId());
            return "Booking ditolak.";
        });
    }

    @PostMapping("/{id}/selesai")
    public String selesai(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, RedirectAttributes ra) {
        return aksi(id, ra, () -> {
            bookingService.selesaikan(id, user.getId());
            return "Booking ditandai selesai. Penyewa kini dapat memberi ulasan.";
        });
    }

    private String aksi(Long id, RedirectAttributes ra, java.util.function.Supplier<String> kerja) {
        try {
            ra.addFlashAttribute("success", kerja.get());
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pemilik/booking/" + id;
    }
}
