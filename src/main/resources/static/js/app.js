(function () {
  'use strict';

  // Menu navigasi pada layar kecil
  var toggle = document.querySelector('.nav-toggle');
  var menu = document.querySelector('.nav-menu');
  if (toggle && menu) {
    toggle.addEventListener('click', function () {
      var open = menu.classList.toggle('open');
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
  }

  // Konfirmasi sebelum aksi: <form data-confirm="Pesan">
  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!window.confirm(form.getAttribute('data-confirm'))) { e.preventDefault(); }
    });
  });

  // Kirim otomatis saat pilihan urutan diganti
  document.querySelectorAll('input[data-autosubmit]').forEach(function (i) {
    i.addEventListener('change', function () { i.form.submit(); });
  });

  // Galeri foto pada halaman detail
  var main = document.getElementById('galeriUtama');
  document.querySelectorAll('.gallery-thumbs img').forEach(function (img) {
    img.addEventListener('click', function () {
      if (main) { main.src = img.src; }
      document.querySelectorAll('.gallery-thumbs img').forEach(function (i) { i.classList.remove('on'); });
      img.classList.add('on');
    });
  });

  // Halaman pencarian: ringkasan jenis terpilih dan sinkron anggaran -> min/maks
  var ringkasJenis = document.getElementById('ringkasJenis');
  if (ringkasJenis) {
    var perbarui = function () {
      var terpilih = [];
      document.querySelectorAll('input[name="jenis"]:checked').forEach(function (c) { terpilih.push(c.value); });
      ringkasJenis.textContent = terpilih.length ? terpilih.join(', ') : 'Semua jenis';
    };
    document.querySelectorAll('input[name="jenis"]').forEach(function (c) { c.addEventListener('change', perbarui); });
    perbarui();
  }
  var anggaran = document.getElementById('anggaran');
  var minIn = document.getElementById('min');
  var maxIn = document.getElementById('max');
  if (anggaran && minIn && maxIn) {
    anggaran.addEventListener('change', function () {
      var p = anggaran.value.split('-');
      minIn.value = p[0] || '';
      maxIn.value = p.length > 1 ? (p[1] || '') : '';
    });
  }

  // Form booking: rincian biaya sesuai tanggal terpilih
  var mulai = document.getElementById('tanggalMulai');
  var selesai = document.getElementById('tanggalSelesai');
  var kotak = document.getElementById('rincianBiaya');
  if (mulai && selesai && kotak) {
    var iso = function (d) { return d.toISOString().slice(0, 10); };
    var today = new Date();
    today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
    mulai.min = iso(today);
    var durasiEl = document.getElementById('durasiPesan');
    var baris = document.getElementById('rincianBaris');
    var total = document.getElementById('rincianTotal');
    var kosong = document.getElementById('rincianKosong');
    var isi = document.getElementById('rincianIsi');
    var url = kotak.getAttribute('data-url');

    var el = function (tag, cls, text) {
      var e = document.createElement(tag);
      if (cls) { e.className = cls; }
      if (text !== undefined) { e.textContent = text; }
      return e;
    };

    var muat = function () {
      if (mulai.value) {
        var next = new Date(mulai.value);
        next.setDate(next.getDate() + 1);
        selesai.min = iso(next);
      }
      if (!mulai.value || !selesai.value) {
        isi.hidden = true; kosong.hidden = false;
        durasiEl.textContent = 'Pilih tanggal mulai dan selesai';
        return;
      }
      fetch(url + '?mulai=' + encodeURIComponent(mulai.value) + '&selesai=' + encodeURIComponent(selesai.value),
        { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
        .then(function (r) { return r.ok ? r.json() : Promise.reject(); })
        .then(function (d) {
          if (!d.valid) {
            isi.hidden = true; kosong.hidden = false; kosong.textContent = d.pesan;
            durasiEl.textContent = d.pesan;
            return;
          }
          kosong.hidden = true; isi.hidden = false;
          durasiEl.textContent = d.durasiPesan;
          durasiEl.parentElement.classList.toggle('warn', !!d.kurang);
          baris.textContent = '';
          d.baris.forEach(function (b) {
            var row = el('div', 'cost-row');
            var nm = el('div', 'nm', b.label);
            nm.appendChild(el('small', '', b.rumus));
            row.appendChild(nm);
            row.appendChild(el('div', 'nl', b.nominal));
            baris.appendChild(row);
          });
          total.textContent = d.total;
        })
        .catch(function () { kosong.hidden = false; isi.hidden = true; kosong.textContent = 'Rincian biaya belum dapat dimuat.'; });
    };
    mulai.addEventListener('change', muat);
    selesai.addEventListener('change', muat);
    muat();
  }

  // Form tempat: pratinjau foto
  var foto = document.getElementById('foto');
  var pratinjau = document.getElementById('pratinjauBaru');
  if (foto && pratinjau) {
    foto.addEventListener('change', function () {
      pratinjau.textContent = '';
      Array.prototype.forEach.call(foto.files, function (f) {
        var fig = document.createElement('figure');
        var img = document.createElement('img');
        img.src = URL.createObjectURL(f);
        img.alt = f.name;
        fig.appendChild(img);
        pratinjau.appendChild(fig);
      });
    });
  }

  // Form tempat: tampilkan isian sesuai skema biaya
  var aturMode = function (grup) {
    var dipilih = document.querySelector('input[name="' + grup + '"]:checked');
    var nilai = dipilih ? dipilih.value : '';
    document.querySelectorAll('[data-group="' + grup + '"]').forEach(function (b) {
      var untuk = (b.getAttribute('data-show-for') || '').split(',');
      b.hidden = untuk.indexOf(nilai) === -1;
    });
  };
  ['listrikMode', 'airMode'].forEach(function (g) {
    document.querySelectorAll('input[name="' + g + '"]').forEach(function (r) {
      r.addEventListener('change', function () { aturMode(g); });
    });
    aturMode(g);
  });

  // Form tempat: tambah baris biaya lain
  var tambah = document.getElementById('tambahBiaya');
  var daftarBiaya = document.getElementById('daftarBiaya');
  if (tambah && daftarBiaya) {
    tambah.addEventListener('click', function () {
      var baris = daftarBiaya.querySelector('.biaya-row');
      if (!baris) { return; }
      var baru = baris.cloneNode(true);
      baru.querySelectorAll('input').forEach(function (i) { i.value = ''; });
      daftarBiaya.appendChild(baru);
    });
  }

  // Stepper form tempat: tandai bagian aktif saat di-scroll
  var langkah = document.querySelectorAll('.stepper a');
  if (langkah.length && 'IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) {
          langkah.forEach(function (a) { a.classList.toggle('on', a.getAttribute('href') === '#' + en.target.id); });
        }
      });
    }, { rootMargin: '-20% 0px -70% 0px' });
    document.querySelectorAll('.form-card').forEach(function (s) { io.observe(s); });
  }
})();
