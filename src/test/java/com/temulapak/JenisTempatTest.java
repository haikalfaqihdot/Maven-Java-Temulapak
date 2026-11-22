package com.temulapak;

import com.temulapak.entity.JenisTempat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JenisTempatTest {

    @Test
    void normalisasiMengembalikanNamaBaku() {
        assertEquals("Ruko", JenisTempat.normalisasi(" ruko "));
        assertEquals("Booth Event", JenisTempat.normalisasi("BOOTH"));
        assertEquals("Booth Event", JenisTempat.normalisasi("booth event"));
        assertNull(JenisTempat.normalisasi("gudang"));
        assertNull(JenisTempat.normalisasi(null));
    }
}
