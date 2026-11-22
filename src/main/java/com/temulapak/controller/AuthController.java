package com.temulapak.controller;

import com.temulapak.dto.RegisterForm;
import com.temulapak.exception.BusinessException;
import com.temulapak.security.AppUserDetails;
import com.temulapak.service.AkunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AkunService akunService;

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AppUserDetails user) {
        if (user != null) {
            return "redirect:" + user.getRole().getHome();
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) String role,
                               @AuthenticationPrincipal AppUserDetails user, Model model) {
        if (user != null) {
            return "redirect:" + user.getRole().getHome();
        }
        RegisterForm form = new RegisterForm();
        if ("pemilik".equalsIgnoreCase(role)) {
            form.setRole("PEMILIK");
        }
        model.addAttribute("form", form);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result, Model model) {
        if (!result.hasFieldErrors("email") && form.getEmail() != null
                && akunService.emailTerpakai(form.getEmail().trim())) {
            result.rejectValue("email", "duplikat", "Email sudah digunakan oleh akun lain.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            akunService.daftar(form);
        } catch (BusinessException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof BusinessException ? e.getMessage()
                    : "Email sudah digunakan oleh akun lain.");
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
