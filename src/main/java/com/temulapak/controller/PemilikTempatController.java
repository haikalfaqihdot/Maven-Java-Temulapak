package com.temulapak.controller;

import com.temulapak.dto.TempatForm;
import com.temulapak.entity.Tempat;
import com.temulapak.exception.BusinessException;
import com.temulapak.security.AppUserDetails;
import com.temulapak.service.TempatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pemilik")
@RequiredArgsConstructor
public class PemilikTempatController {

    private final TempatService tempatService;

    @GetMapping("/tempat")
    public String daftar(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("daftar", tempatService.milikPemilik(user.getId()));
        return "pemilik/tempat/list";
    }

    @GetMapping("/tempat/baru")
    public String formBaru(Model model) {
        model.addAttribute("form", new TempatForm());
        isiFormModel(model, null);
        return "pemilik/tempat/form";
    }

    @PostMapping("/tempat")
    public String simpanBaru(@Valid @ModelAttribute("form") TempatForm form, BindingResult result,
                             @RequestParam(value = "foto", required = false) List<MultipartFile> foto,
                             @AuthenticationPrincipal AppUserDetails user, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                tempatService.buat(user.getId(), form, foto);
                ra.addFlashAttribute("success", "Listing dikirim untuk verifikasi. Listing belum tayang sampai disetujui admin.");
                return "redirect:/pemilik/tempat";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        isiFormModel(model, null);
        return "pemilik/tempat/form";
    }

    @GetMapping("/tempat/{id}/edit")
    public String formEdit(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, Model model) {
        Tempat t = tempatService.ambilMilikPemilik(id, user.getId());
        model.addAttribute("form", TempatForm.dari(t));
        isiFormModel(model, t);
        return "pemilik/tempat/form";
    }

    @PostMapping("/tempat/{id}")
    public String simpanEdit(@PathVariable Long id, @Valid @ModelAttribute("form") TempatForm form, BindingResult result,
                             @RequestParam(value = "foto", required = false) List<MultipartFile> foto,
                             @AuthenticationPrincipal AppUserDetails user, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                tempatService.ubah(id, user.getId(), form, foto);
                ra.addFlashAttribute("success", "Perubahan dikirim untuk verifikasi. Listing belum tayang sampai disetujui admin.");
                return "redirect:/pemilik/tempat";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        isiFormModel(model, tempatService.ambilMilikPemilik(id, user.getId()));
        return "pemilik/tempat/form";
    }

    @PostMapping("/tempat/{id}/hapus")
    public String hapus(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user, RedirectAttributes ra) {
        try {
            tempatService.hapus(id, user.getId());
            ra.addFlashAttribute("success", "Tempat dihapus.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pemilik/tempat";
    }

    private void isiFormModel(Model model, Tempat tempat) {
        model.addAttribute("fasilitasList", tempatService.semuaFasilitas());
        model.addAttribute("tempat", tempat);
    }
}
