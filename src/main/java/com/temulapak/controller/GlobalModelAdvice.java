package com.temulapak.controller;

import com.temulapak.entity.JenisTempat;
import com.temulapak.entity.ModeBiaya;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("daftarJenis")
    public List<String> daftarJenis() {
        return JenisTempat.DAFTAR;
    }

    @ModelAttribute("daftarModeBiaya")
    public ModeBiaya[] daftarModeBiaya() {
        return ModeBiaya.values();
    }
}
