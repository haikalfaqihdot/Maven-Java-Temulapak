package com.temulapak.service;

import com.temulapak.dto.TempatForm;
import com.temulapak.entity.*;
import com.temulapak.exception.BusinessException;
import com.temulapak.exception.ResourceNotFoundException;
import com.temulapak.repository.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TempatService {

    public static final int LANGKAH_TAMPIL = 6;
    private static final int MAKS_FOTO = 10;

    private final TempatRepository tempatRepository;
    private final FasilitasRepository fasilitasRepository;
    private final PemilikTempatRepository pemilikRepository;
    private final BookingRepository bookingRepository;
    private final FotoStorageService storage;

    // ---------------------------------------------------------------- publik

    @Transactional(readOnly = true)
    public Page<Tempat> cari(String lokasi, List<String> jenis, List<SatuanPeriode> periode, List<Long> fasilitasIds,
                             BigDecimal min, BigDecimal max, String sort, int tampil) {
        Specification<Tempat> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("statusVerifikasi"), StatusVerifikasi.APPROVED));
            if (StringUtils.hasText(lokasi)) {
                String pola = "%" + lokasi.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("lokasi")), pola),
                        cb.like(cb.lower(root.get("namaTempat")), pola),
                        cb.like(cb.lower(root.get("alamatLengkap")), pola)));
            }
            if (jenis != null && !jenis.isEmpty()) {
                List<String> kecil = jenis.stream().filter(StringUtils::hasText).map(j -> j.trim().toLowerCase()).toList();
                if (!kecil.isEmpty()) {
                    ps.add(cb.lower(root.<String>get("jenisTempat")).in(kecil));
                }
            }
            if (periode != null && !periode.isEmpty()) {
                ps.add(root.get("satuanPeriode").in(periode));
            }
            if (fasilitasIds != null) {
                Expression<Set<Fasilitas>> semua = root.get("fasilitas");
                for (Long id : fasilitasIds) {
                    ps.add(cb.isMember(fasilitasRepository.getReferenceById(id), semua));
                }
            }
            if (min != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("hargaSewa"), min));
            }
            if (max != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("hargaSewa"), max));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Sort urutan = switch (sort == null ? "" : sort) {
            case "harga" -> Sort.by(Sort.Direction.ASC, "hargaSewa").and(Sort.by(Sort.Direction.DESC, "idTempat"));
            case "terdekat" -> Sort.by(Sort.Direction.ASC, "lokasi").and(Sort.by(Sort.Direction.DESC, "idTempat"));
            default -> Sort.by(Sort.Direction.DESC, "idTempat");
        };
        return tempatRepository.findAll(spec, PageRequest.of(0, Math.max(LANGKAH_TAMPIL, tampil), urutan));
    }

    @Transactional(readOnly = true)
    public List<Tempat> terbaru(int jumlah) {
        return tempatRepository.findByStatusVerifikasiOrderByIdTempatDesc(StatusVerifikasi.APPROVED, PageRequest.of(0, jumlah));
    }

    /** Detail publik hanya untuk tempat yang sudah disetujui. */
    @Transactional(readOnly = true)
    public Tempat ambilPublik(Long id) {
        Tempat t = tempatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tempat tidak ditemukan."));
        if (t.getStatusVerifikasi() != StatusVerifikasi.APPROVED) {
            throw new ResourceNotFoundException("Tempat tidak ditemukan atau belum disetujui admin.");
        }
        return t;
    }

    public List<Fasilitas> semuaFasilitas() {
        return fasilitasRepository.findAllByOrderByNamaFasilitasAsc();
    }

    // ---------------------------------------------------------------- pemilik

    @Transactional(readOnly = true)
    public List<Tempat> milikPemilik(Long idPemilik) {
        return tempatRepository.findByPemilik_IdPemilikOrderByIdTempatDesc(idPemilik);
    }

    @Transactional(readOnly = true)
    public Tempat ambilMilikPemilik(Long idTempat, Long idPemilik) {
        Tempat t = tempatRepository.findById(idTempat)
                .orElseThrow(() -> new ResourceNotFoundException("Tempat tidak ditemukan."));
        if (!t.getPemilik().getIdPemilik().equals(idPemilik)) {
            throw new AccessDeniedException("Tempat ini bukan milik Anda.");
        }
        return t;
    }

    @Transactional
    public Tempat buat(Long idPemilik, TempatForm form, List<MultipartFile> foto) {
        validasiBiaya(form);
        Tempat t = new Tempat();
        t.setPemilik(pemilikRepository.getReferenceById(idPemilik));
        terapkan(t, form);
        tambahkanFoto(t, foto);
        if (t.getFotoList().isEmpty()) {
            throw new BusinessException("Tambahkan minimal 1 foto tempat (JPG atau PNG).");
        }
        t.setStatusVerifikasi(StatusVerifikasi.PENDING);
        t.setDiajukanPada(LocalDate.now());
        return tempatRepository.save(t);
    }

    /** Hasil edit kembali ke "menunggu verifikasi" sampai disetujui admin. */
    @Transactional
    public Tempat ubah(Long idTempat, Long idPemilik, TempatForm form, List<MultipartFile> foto) {
        validasiBiaya(form);
        Tempat t = ambilMilikPemilik(idTempat, idPemilik);
        terapkan(t, form);

        Set<Long> hapus = new HashSet<>(form.getHapusFotoIds() == null ? List.of() : form.getHapusFotoIds());
        List<String> urlDihapus = new ArrayList<>();
        t.getFotoList().removeIf(f -> {
            if (hapus.contains(f.getIdFoto())) {
                urlDihapus.add(f.getUrlFoto());
                return true;
            }
            return false;
        });
        tambahkanFoto(t, foto);
        if (t.getFotoList().isEmpty()) {
            throw new BusinessException("Tambahkan minimal 1 foto tempat (JPG atau PNG).");
        }
        t.setStatusVerifikasi(StatusVerifikasi.PENDING);
        t.setCatatanAdmin(null);
        t.setDiajukanPada(LocalDate.now());
        urlDihapus.forEach(storage::hapus);
        return t;
    }

    @Transactional
    public void hapus(Long idTempat, Long idPemilik) {
        Tempat t = ambilMilikPemilik(idTempat, idPemilik);
        long aktif = bookingRepository.countByTempat_IdTempatAndStatusBookingIn(
                idTempat, List.of(StatusBooking.PENDING, StatusBooking.APPROVED));
        if (aktif > 0) {
            throw new BusinessException("Tempat tidak dapat dihapus karena masih memiliki booking yang menunggu atau dikonfirmasi.");
        }
        List<String> urls = t.getFotoList().stream().map(FotoTempat::getUrlFoto).toList();
        tempatRepository.delete(t);
        tempatRepository.flush();
        urls.forEach(storage::hapus);
    }

    // ---------------------------------------------------------------- admin

    @Transactional(readOnly = true)
    public List<Tempat> daftarVerifikasi(StatusVerifikasi status) {
        return tempatRepository.findByStatusVerifikasiOrderByIdTempatAsc(status);
    }

    @Transactional(readOnly = true)
    public List<Tempat> semua() {
        return tempatRepository.findAllByOrderByIdTempatDesc();
    }

    @Transactional(readOnly = true)
    public Tempat ambil(Long id) {
        return tempatRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tempat tidak ditemukan."));
    }

    @Transactional
    public void verifikasi(Long id, boolean setuju, String catatan) {
        Tempat t = ambil(id);
        if (setuju) {
            if (!periksa(t).isLengkap()) {
                throw new BusinessException("Listing belum lengkap atau datanya rusak, sehingga hanya bisa ditolak.");
            }
            t.setStatusVerifikasi(StatusVerifikasi.APPROVED);
            t.setCatatanAdmin(null);
        } else {
            if (!StringUtils.hasText(catatan)) {
                throw new BusinessException("Catatan admin wajib diisi saat menolak listing.");
            }
            t.setStatusVerifikasi(StatusVerifikasi.REJECTED);
            t.setCatatanAdmin(catatan.trim());
        }
    }

    /** Pemeriksaan kelengkapan sesuai checklist admin. */
    @Transactional(readOnly = true)
    public PemeriksaanListing periksa(Tempat t) {
        boolean info = ada(t.getNamaTempat()) && ada(t.getJenisTempat()) && ada(t.getLokasi())
                && ada(t.getAlamatLengkap()) && ada(t.getKondisi());
        boolean adaFoto = !t.getFotoList().isEmpty();
        boolean fotoBisaDibuka = adaFoto && t.getFotoList().stream().allMatch(f -> storage.ada(f.getUrlFoto()));
        boolean harga = t.getHargaSewa() != null && t.getHargaSewa().signum() > 0;
        boolean biaya = biayaJelas(t);
        boolean aturan = ada(t.getDurasiMinimal()) && ada(t.getPerpanjangan()) && ada(t.getPembayaran())
                && ada(t.getPembatalan()) && ada(t.getDenda());

        List<PemeriksaanListing.Cek> cek = List.of(
                new PemeriksaanListing.Cek("Nama, jenis, lokasi, alamat, dan kondisi terisi", info),
                new PemeriksaanListing.Cek("Minimal 1 foto JPG atau PNG dan foto bisa dibuka", fotoBisaDibuka),
                new PemeriksaanListing.Cek("Harga pokok lebih dari 0", harga),
                new PemeriksaanListing.Cek("Biaya tambahan tercantum dan dasar perhitungannya jelas", biaya),
                new PemeriksaanListing.Cek("Aturan sewa lengkap: durasi, perpanjangan, pembayaran, pembatalan, denda", aturan));
        boolean lengkap = cek.stream().allMatch(PemeriksaanListing.Cek::isOk);

        String ringkas;
        if (lengkap) {
            ringkas = "Data lengkap";
        } else if (!adaFoto) {
            ringkas = "Data rusak: belum ada foto";
        } else if (!fotoBisaDibuka) {
            ringkas = "Data rusak: foto tidak bisa dibuka";
        } else {
            ringkas = "Data belum lengkap";
        }
        return new PemeriksaanListing(cek, lengkap, ringkas);
    }

    // ---------------------------------------------------------------- helper

    private static boolean ada(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean positif(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    private boolean biayaJelas(Tempat t) {
        boolean listrik = switch (t.modeListrik()) {
            case TERMASUK -> true;
            case TETAP -> positif(t.getListrikBiayaTetap());
            case PEMAKAIAN -> positif(t.getListrikTarifKwh());
        };
        boolean air = t.modeAir() != ModeBiaya.TETAP || positif(t.getAirBiaya());
        return listrik && air;
    }

    /** Biaya yang dihitung dari pemakaian wajib menjelaskan tarif atau dasar perhitungannya. */
    private void validasiBiaya(TempatForm f) {
        if (f.getListrikMode() == ModeBiaya.TETAP && !positif(f.getListrikBiayaTetap())) {
            throw new BusinessException("Isi biaya listrik per periode karena skema listrik adalah biaya tetap.");
        }
        if (f.getListrikMode() == ModeBiaya.PEMAKAIAN && !positif(f.getListrikTarifKwh())) {
            throw new BusinessException("Isi tarif listrik per kWh karena listrik dihitung sesuai pemakaian.");
        }
        if (f.getAirMode() == ModeBiaya.TETAP && !positif(f.getAirBiaya())) {
            throw new BusinessException("Isi biaya air per periode karena skema air adalah biaya tetap.");
        }
        List<String> nama = f.getBiayaLainNama() == null ? List.of() : f.getBiayaLainNama();
        List<String> nominal = f.getBiayaLainNominal() == null ? List.of() : f.getBiayaLainNominal();
        for (int i = 0; i < Math.max(nama.size(), nominal.size()); i++) {
            String n = i < nama.size() ? nama.get(i) : "";
            String v = i < nominal.size() ? nominal.get(i) : "";
            if (!ada(n) && !ada(v)) {
                continue;
            }
            if (!ada(n)) {
                throw new BusinessException("Nama biaya lainnya wajib diisi.");
            }
            try {
                if (new BigDecimal(v.trim()).signum() < 0) {
                    throw new BusinessException("Nominal biaya lainnya tidak boleh negatif.");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("Nominal biaya \"" + n.trim() + "\" harus berupa angka.");
            }
        }
    }

    private void terapkan(Tempat t, TempatForm f) {
        String jenis = JenisTempat.normalisasi(f.getJenisTempat());
        if (jenis == null) {
            throw new BusinessException("Jenis tempat harus Booth Event, Kios, atau Ruko.");
        }
        t.setNamaTempat(f.getNamaTempat().trim());
        t.setJenisTempat(jenis);
        t.setLokasi(f.getLokasi().trim());
        t.setUkuran(ada(f.getUkuran()) ? f.getUkuran().trim() : null);
        t.setAlamatLengkap(f.getAlamatLengkap().trim());
        t.setKondisi(f.getKondisi().trim());
        t.setDeskripsi(ada(f.getDeskripsi()) ? f.getDeskripsi().trim() : null);
        t.setStatusTempat(f.getStatusTempat() == null ? StatusTempat.AVAILABLE : f.getStatusTempat());

        t.setHargaSewa(f.getHargaSewa());
        t.setSatuanPeriode(f.getSatuanPeriode());

        t.setListrikMode(f.getListrikMode());
        t.setListrikBiayaTetap(f.getListrikMode() == ModeBiaya.TETAP ? f.getListrikBiayaTetap() : null);
        boolean pakaiListrik = f.getListrikMode() == ModeBiaya.PEMAKAIAN;
        t.setListrikTarifKwh(pakaiListrik ? f.getListrikTarifKwh() : null);
        t.setListrikEstimasiKwh(pakaiListrik ? f.getListrikEstimasiKwh() : null);

        t.setAirMode(f.getAirMode());
        t.setAirBiaya(f.getAirMode() == ModeBiaya.TERMASUK ? null : f.getAirBiaya());

        StringBuilder lain = new StringBuilder();
        List<String> nama = f.getBiayaLainNama() == null ? List.of() : f.getBiayaLainNama();
        List<String> nominal = f.getBiayaLainNominal() == null ? List.of() : f.getBiayaLainNominal();
        for (int i = 0; i < Math.min(nama.size(), nominal.size()); i++) {
            if (!ada(nama.get(i)) || !ada(nominal.get(i))) {
                continue;
            }
            BigDecimal v = new BigDecimal(nominal.get(i).trim());
            if (v.signum() == 0) {
                continue;
            }
            String bersih = nama.get(i).replace(";", " ").replaceAll("[\\r\\n]+", " ").trim();
            lain.append(lain.length() > 0 ? "\n" : "").append(bersih).append(';').append(v.toPlainString());
        }
        t.setBiayaLain(lain.length() == 0 ? null : lain.toString());

        t.setDurasiMinimal(f.getDurasiMinimal().trim());
        t.setPerpanjangan(f.getPerpanjangan().trim());
        t.setPembayaran(f.getPembayaran().trim());
        t.setPembatalan(f.getPembatalan().trim());
        t.setDenda(f.getDenda().trim());

        t.getFasilitas().clear();
        if (f.getFasilitasIds() != null && !f.getFasilitasIds().isEmpty()) {
            t.getFasilitas().addAll(fasilitasRepository.findAllById(f.getFasilitasIds()));
        }
    }

    private void tambahkanFoto(Tempat t, List<MultipartFile> files) {
        List<MultipartFile> valid = files == null ? List.of()
                : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (valid.isEmpty()) {
            return;
        }
        if (t.getFotoList().size() + valid.size() > MAKS_FOTO) {
            throw new BusinessException("Maksimal " + MAKS_FOTO + " foto per tempat.");
        }
        valid.forEach(storage::validasi);

        List<String> tersimpan = new ArrayList<>();
        try {
            for (MultipartFile f : valid) {
                String url = storage.simpan(f);
                tersimpan.add(url);
                t.getFotoList().add(new FotoTempat(t, url));
            }
        } catch (RuntimeException e) {
            tersimpan.forEach(storage::hapus);
            throw e;
        }
    }
}
