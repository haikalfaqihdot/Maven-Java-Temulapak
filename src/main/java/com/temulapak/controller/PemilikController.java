package com.temulapak.controller;

import com.temulapak.dto.ProfilForm;
import com.temulapak.entity.PemilikTempat;
import com.temulapak.security.AppUserDetails;
import com.temulapak.service.AkunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pemilik")
@RequiredArgsConstructor
public class PemilikController {

    private final AkunService akunService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/pemilik/tempat";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal AppUserDetails user, Model model) {
        PemilikTempat p = akunService.ambilPemilik(user.getId());
        ProfilForm form = new ProfilForm();
        form.setNama(p.getNama());
        form.setNoTelepon(p.getNoTelepon());
        model.addAttribute("form", form);
        model.addAttribute("email", p.getEmail());
        return "pemilik/profil";
    }

    @PostMapping("/profil")
    public String simpanProfil(@Valid @ModelAttribute("form") ProfilForm form, BindingResult result,
                               @AuthenticationPrincipal AppUserDetails user, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("email", user.getEmail());
            return "pemilik/profil";
        }
        akunService.ubahProfilPemilik(user.getId(), form);
        user.setNama(form.getNama().trim());
        ra.addFlashAttribute("success", "Profil diperbarui.");
        return "redirect:/pemilik/profil";
    }
}
