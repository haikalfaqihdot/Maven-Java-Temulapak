package com.temulapak.service;

import com.temulapak.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan foto tempat ke folder upload (di luar classpath) dengan validasi ketat. */
@Service
public class FotoStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> EKSTENSI = Set.of("jpg", "jpeg", "png");

    private final Path root;

    public FotoStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public void validasi(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File foto kosong.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("Ukuran foto maksimal 5 MB.");
        }
        String ext = ekstensi(file.getOriginalFilename());
        String ct = file.getContentType();
        if (!EKSTENSI.contains(ext) || ct == null || !ct.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("Format foto harus JPG atau PNG.");
        }
        try (InputStream in = file.getInputStream()) {
            byte[] h = in.readNBytes(12);
            if (!isGambarValid(h)) {
                throw new BusinessException("Isi file bukan gambar JPG atau PNG yang valid.");
            }
        } catch (IOException e) {
            throw new BusinessException("File foto tidak dapat dibaca.");
        }
    }

    /** Menyimpan file dan mengembalikan URL publik (disimpan pada kolom url_foto). */
    public String simpan(MultipartFile file) {
        validasi(file);
        String nama = UUID.randomUUID() + "." + ekstensi(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(nama));
        } catch (IOException e) {
            throw new BusinessException("Foto gagal disimpan. Coba lagi.");
        }
        return "/uploads/" + nama;
    }

    /** Menghapus file fisik. URL selain /uploads/ (mis. gambar contoh) diabaikan. */
    public void hapus(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        Path target = root.resolve(url.substring("/uploads/".length())).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // file yatim tidak mengganggu fungsi aplikasi
        }
    }

    /** True bila file ada. URL selain /uploads/ (aset bawaan aplikasi) dianggap ada. */
    public boolean ada(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (!url.startsWith("/uploads/")) {
            return true;
        }
        Path target = root.resolve(url.substring("/uploads/".length())).normalize();
        return target.startsWith(root) && Files.isRegularFile(target);
    }

    private String ekstensi(String namaFile) {
        if (namaFile == null || !namaFile.contains(".")) {
            return "";
        }
        return namaFile.substring(namaFile.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isGambarValid(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return true; // JPEG
        }
        if (h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return true; // PNG
        }
        return false;
    }
}
