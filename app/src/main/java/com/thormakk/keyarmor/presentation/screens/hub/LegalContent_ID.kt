package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentID(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyID()
    "terms"      -> termsID()
    "legal"      -> legalID()
    "license"    -> licenseID()
    "opensource" -> opensourceID()
    "delete"     -> deleteDataID()
    else         -> listOf(LegalSection("Dokumen tidak ditemukan", ""))
}

private fun privacyID() = listOf(
    LegalSection(
        "Kebijakan Privasi",
        ""
    ),
    LegalSection(
        "Pengumpulan Data",
        """
        KeyArmor tidak mengumpulkan, mengirimkan, atau membagikan data pribadi apa pun ke server eksternal. Semua informasi yang Anda masukkan ke dalam aplikasi tetap berada secara eksklusif di perangkat Anda, kecuali Anda secara eksplisit mengaktifkan sinkronisasi Google Drive.

        KeyArmor tidak mengumpulkan alamat email atau kredensial akun apa pun untuk fitur inti aplikasi (brankas, isi otomatis, audit keamanan, pemantauan pelanggaran) — fitur-fitur ini berfungsi sepenuhnya tanpa akun apa pun. Jika Anda mengaktifkan Warisan Digital atau Paket Keluarga, fitur-fitur ini memerlukan Login Google: alamat email akun Google Anda kemudian disimpan di Google Firebase untuk mengidentifikasi Anda dan mencocokkan Anda dengan ahli waris atau anggota keluarga yang Anda tunjuk, serta untuk mengirimkan email notifikasi terkait melalui penyedia email kami (Brevo). Email ini hanya digunakan untuk kedua fitur tersebut dan tidak pernah dijual atau dibagikan kepada siapa pun. Di luar Warisan Digital dan Paket Keluarga, satu-satunya pengenal pribadi adalah nama atau nama panggilan opsional yang dapat Anda tetapkan selama penyiapan, yang disimpan secara eksklusif di perangkat Anda dalam bentuk terenkripsi.
        """.trimIndent()
    ),
    LegalSection(
        "Penyimpanan Lokal",
        """
        Semua data Anda (kata sandi, passkey, catatan, kartu kredit, rekening bank, dompet kripto, lampiran, dan riwayat kata sandi) disimpan dalam bentuk terenkripsi menggunakan AES-256-GCM dengan kunci yang diturunkan dari kata sandi utama Anda menggunakan Argon2id, yang tidak pernah meninggalkan perangkat Anda. Jika Anda mengaktifkan pembukaan kunci biometrik, salinan kunci tersebut dilindungi lagi dengan kunci Android Keystore di perangkat Anda. Tidak ada data yang meninggalkan perangkat Anda tanpa persetujuan eksplisit Anda.
        """.trimIndent()
    ),
    LegalSection(
        "Kata Sandi Utama",
        """
        Kata sandi utama Anda tidak pernah disimpan dalam teks biasa atau dalam bentuk apa pun yang dapat dipulihkan. Kata sandi tersebut digunakan semata-mata untuk menurunkan kunci enkripsi brankas melalui Argon2id. Jika Anda kehilangan kata sandi utama, data Anda tidak dapat dipulihkan.
        """.trimIndent()
    ),
    LegalSection(
        "Biometrik",
        """
        Autentikasi biometrik di KeyArmor dikelola sepenuhnya oleh sistem operasi Android melalui API BiometricPrompt. KeyArmor tidak mengakses, menyimpan, atau memproses data biometrik kapan pun.

        Biometrik digunakan dalam dua konteks di dalam aplikasi:

        • Pembukaan kunci brankas: untuk mengautentikasi akses ke aplikasi saat startup, tersedia di semua paket.
        • Perlindungan Biometrik Lanjutan (Premium): penghalang opsional yang memerlukan verifikasi biometrik sebelum menampilkan kolom sensitif di bagian brankas tertentu (kata sandi, kartu kredit, kolom kustom). Dapat dikonfigurasi di Pengaturan — mendukung mode per-akses atau per-sesi dengan batas waktu yang dapat disesuaikan. Jaminan privasi yang sama berlaku: KeyArmor tidak menyimpan atau memproses data biometrik.
        """.trimIndent()
    ),
    LegalSection(
        "Sinkronisasi Google Drive",
        """
Sinkronisasi Google Drive sepenuhnya opsional. Saat diaktifkan, KeyArmor:

• Meminta satu izin minimal (DRIVE_APPDATA) untuk menyimpan cadangan terenkripsi Anda sendiri dan titik pemulihan Time Machine di folder pribadi khusus aplikasi di Google Drive Anda. Folder ini tersembunyi — tidak ada aplikasi lain, dan juga Anda saat menjelajahi Drive, yang dapat melihat atau mengubahnya.
• Mengenkripsi brankas dengan kunci cadangan yang terdiri dari 12 kata sebelum mengunggahnya. Kunci tersebut dibuat di perangkat Anda dan hanya Anda yang memilikinya: baik Google maupun pengembang tidak dapat membaca berkas tersebut atau memulihkan kuncinya.
• Jika Anda menyimpan berkas PDF kunci cadangan Anda ke Google Docs, Drive, atau aplikasi lain, Anda melakukannya sendiri melalui layar berbagi di Android; aplikasi tersebut diatur oleh kebijakan privasinya sendiri.
• Jika Anda mengganti kunci, Anda dapat menyimpan kunci sebelumnya, terenkripsi di ponsel Anda; PDF kit dapat menyertakannya. KeyArmor tidak mengirimkannya ke server mana pun.
• Tidak pernah mengakses berkas Drive lain, tidak pernah membuat folder yang terlihat, dan tidak pernah membagikan berkas.
• Secara opsional menyimpan salinan lokal kedua dari cadangan di folder Downloads/KeyArmor/ perangkat Anda, yang tidak pernah keluar dari perangkat.
• Pencadangan Google Drive, baik secara manual (saat Anda memintanya) maupun otomatis setiap hari, tersedia di paket Premium dan Paket Keluarga. Di paket gratis, Anda dapat menghubungkan akun Google Anda untuk memulihkan cadangan yang sudah ada.

Warisan Digital dan Paket Keluarga menggunakan Google Firebase, bukan Google Drive Anda. Firebase mengelola persetujuan, undangan, dan pemberitahuan. Brankas warisan dienkripsi dari ujung ke ujung di perangkat Anda menggunakan kunci publik ahli waris, dan blok terenkripsi yang dihasilkan diunggah ke bucket pribadi Firebase Cloud Storage milik pengembang melalui tautan yang ditandatangani dan berdurasi pendek (15 menit). Server menyimpan blok tersebut tetapi tidak pernah menerima kunci ahli waris, sehingga tidak dapat membacanya.

Anda dapat memutuskan koneksi akun Google kapan saja dari layar sinkronisasi.
    """.trimIndent()
    ),
    LegalSection(
        "Lampiran",
        """
        Fitur lampiran memungkinkan Anda melampirkan berkas (maksimal 5 berkas per entri, masing-masing maksimal 5 MB) ke entri brankas Anda. Semua lampiran dienkripsi menggunakan AES-256-GCM sebelum disimpan ke penyimpanan internal perangkat. Jika Anda mengaktifkan sinkronisasi Google Drive, lampiran dan foto dokumen Anda akan disertakan dalam cadangan yang Anda unggah, yang dienkripsi menggunakan kunci cadangan Anda. Data tersebut tidak akan pernah dikirim ke layanan lain mana pun.
        """.trimIndent()
    ),
    LegalSection(
        "Riwayat Kata Sandi",
        """
        KeyArmor menyimpan riwayat lokal kata sandi sebelumnya untuk setiap entri. Riwayat ini disimpan dalam bentuk terenkripsi di perangkat dan tidak pernah dikirimkan ke server eksternal mana pun.
        """.trimIndent()
    ),
    LegalSection(
        "Notifikasi In-App",
        """
        KeyArmor menghasilkan notifikasi internal untuk memberi tahu Anda tentang peristiwa keamanan penting:

        • Peringatan kedaluwarsa: dikirim ketika satu atau lebih kata sandi mendekati tanggal kedaluwarsanya.
        • Kata sandi kedaluwarsa: dikirim ketika satu atau lebih kata sandi telah melewati tanggal kedaluwarsanya.
        • Peringatan pelanggaran: dikirim ketika kata sandi Anda terdeteksi dalam pelanggaran data yang diketahui.

        Ketiga jenis notifikasi ini dibuat sepenuhnya di perangkat Anda dan tidak pernah dikirimkan ke server eksternal. Jika Anda menggunakan Warisan Digital atau Paket Keluarga, Anda juga akan menerima notifikasi tentang fitur-fitur tersebut (ahli waris menerima undangan, anggota keluarga ditambahkan, dll.); notifikasi ini dipicu oleh Google Firebase dan dikirim melalui Firebase Cloud Messaging, karena bergantung pada peristiwa yang dimulai oleh akun lain. Semua notifikasi, apa pun asalnya, dihapus secara otomatis setelah 30 hari. Anda dapat menghapus notifikasi apa pun dengan menggeseknya ke samping, atau menghapus semua sekaligus dari layar Notifikasi. Notifikasi push memerlukan izin sistem untuk dikirim.
        """.trimIndent()
    ),
    LegalSection(
        "Audit Keamanan",
        """
        Fitur audit keamanan menganalisis kata sandi Anda secara lokal di perangkat. Pemeriksaan kata sandi umum dilakukan dengan membandingkan terhadap daftar yang telah ditentukan sebelumnya yang tersimpan di dalam aplikasi itu sendiri, tanpa kueri ke layanan eksternal.
        """.trimIndent()
    ),
    LegalSection(
        "Pemantauan Kebocoran (HIBP)",
        """
        KeyArmor dapat memeriksa apakah kata sandi Anda telah muncul dalam kebocoran data yang diketahui menggunakan layanan Have I Been Pwned (HIBP), yang dioperasikan oleh peneliti keamanan Troy Hunt.

        Pemeriksaan ini menggunakan model k-anonimitas: hanya 5 karakter pertama dari hash SHA-1 kata sandi Anda yang dikirim ke API HIBP. Hash lengkap tidak pernah dikirimkan. Pencocokan dilakukan secara lokal di perangkat Anda. Tidak ada nama pengguna, alamat email, atau asosiasi akun yang pernah dikirimkan. Respons HIBP diisi dengan hash palsu sehingga bahkan analisis lalu lintas jaringan pun tidak mengungkapkan kata sandi yang sedang diperiksa.
        """.trimIndent()
    ),
    LegalSection(
        "Layanan Isi Otomatis",
        """
        KeyArmor menyertakan layanan Isi Otomatis Android opsional. Ketika diaktifkan di pengaturan sistem, layanan ini membaca nama paket aplikasi atau URL situs web dari layar login yang sedang aktif untuk menyarankan kredensial yang cocok dari brankas Anda. Pencocokan ini dilakukan sepenuhnya di perangkat. Tidak ada nama aplikasi, URL situs web, atau kredensial yang dikirimkan ke server eksternal mana pun. Layanan isi otomatis tidak membaca konten aplikasi lain di luar yang benar-benar diperlukan untuk mengidentifikasi konteks login.
        """.trimIndent()
    ),
    LegalSection(
        "Saldo Langsung Dompet Kripto",
        """
        Ketika Anda menyimpan entri dompet kripto dan mengaktifkan pemantauan saldo langsung, KeyArmor melakukan kueri ke node atau API blockchain publik untuk mengambil saldo yang terkait dengan alamat dompet yang Anda masukkan. Hanya alamat dompet yang dikirimkan — tidak ada kunci privat, frasa benih, atau data pribadi yang dikirim. Kueri ini langsung menuju infrastruktur publik; KeyArmor tidak mengoperasikan server perantara mana pun.
        """.trimIndent()
    ),
    LegalSection(
        "Berbagi Entri P2P",
        """
        KeyArmor memungkinkan Anda berbagi entri brankas individual langsung dengan pengguna KeyArmor lain melalui kode QR atau teks terenkripsi. Transfer ini dienkripsi dari ujung ke ujung dan bersifat peer-to-peer — tidak ada server atau perantara yang terlibat. Anda sepenuhnya bertanggung jawab atas pilihan dengan siapa Anda berbagi entri dan keamanan saluran yang digunakan untuk mengirimkan teks terenkripsi (jika tidak menggunakan QR secara langsung).
        """.trimIndent()
    ),
    LegalSection(
        "Pustaka Pihak Ketiga",
        """
        KeyArmor menggunakan pustaka sumber terbuka (lihat Pemberitahuan Sumber Terbuka). Tidak ada pustaka ini yang menyertakan mekanisme telemetri, analitik, atau pengumpulan data dalam konfigurasi yang digunakan oleh aplikasi ini.

        KeyArmor juga mengandalkan dua layanan pihak ketiga untuk fitur tertentu: RevenueCat, yang memverifikasi status langganan Premium Anda dan hanya menerima informasi terkait pembelian (tidak pernah brankas, kata sandi utama, atau kunci pemulihan Anda); dan Brevo, yang mengirim email transaksional yang diperlukan oleh Warisan Digital dan Paket Keluarga (undangan, konfirmasi persetujuan, notifikasi) dan, untuk tujuan itu, menerima alamat email orang-orang yang terlibat dalam fitur-fitur tersebut. Tidak satu pun dari kedua layanan ini digunakan untuk iklan atau analitik perilaku.
        """.trimIndent()
    ),
    LegalSection(
        "Widget Layar Utama",
        """
        KeyArmor menyertakan widget layar utama Android opsional yang menampilkan jumlah total entri yang tersimpan di vault Anda. Widget ini hanya mengakses data lokal perangkat. Widget tidak mengirimkan informasi apa pun ke server eksternal. Jumlah entri adalah data agregat yang tidak mengungkapkan konten entri individual. Anda dapat memilih apakah akan menempatkan widget di layar utama atau tidak.
        """.trimIndent()
    ),
    LegalSection(
        "Izin",
        """
        KeyArmor hanya meminta izin yang diperlukan untuk pengoperasiannya:

        • Biometrik (opsional): untuk membuka kunci dengan sidik jari.
        • Internet (opsional): hanya digunakan untuk fitur yang Anda aktifkan sendiri — sinkronisasi Google Drive, Warisan Digital dan Paket Keluarga (melalui Google Firebase), kueri saldo langsung dompet kripto, pemeriksaan pelanggaran Have I Been Pwned, dan verifikasi langganan (Google Play Billing / RevenueCat).
        • Akses file (opsional): untuk impor/ekspor brankas dan penambahan lampiran, hanya saat Anda memulainya.
        • Kamera (opsional): untuk memindai kode QR saat menyiapkan autentikasi dua faktor (2FA/TOTP) atau saat menerima entri yang dibagikan. Diaktifkan hanya atas permintaan eksplisit pengguna.

        • Layanan isi otomatis (opsional): diaktifkan secara manual di pengaturan sistem Android. Digunakan semata-mata untuk menyarankan kredensial di layar login aplikasi lain.
        • Mikrofon (opsional): hanya untuk dikte suara di Catatan Aman. Pengenalan suara dilakukan di perangkat Anda; KeyArmor tidak merekam, menyimpan, atau mengirim audio ke server mana pun.
        • Kalender, hanya baca (opsional): digunakan oleh Mode Perjalanan Aman untuk mencari perjalanan di acara Anda. Acara diproses di perangkat Anda dan tidak pernah dikirim.
        • Notifikasi dan alarm tepat waktu (opsional): untuk peringatan keamanan dan pengingat yang Anda jadwalkan. Peristiwa boot perangkat digunakan untuk menjadwalkan ulang pengingat setelah restart.
        • Status jaringan (opsional): hanya untuk mengetahui apakah Anda memakai data seluler atau Wi-Fi, agar cadangan Google Drive yang besar menunggu Wi-Fi. Tidak ada informasi tentang jaringan Anda yang dikumpulkan atau dikirim.

        KeyArmor tidak meminta akses ke kontak, lokasi, atau data pribadi lainnya yang tidak disebutkan di atas.
        """.trimIndent()
    ),
    LegalSection("Layanan lain yang menerima data", """
Selain layanan yang dijelaskan di atas, KeyArmor menggunakan layanan berikut, hanya untuk fungsi yang membutuhkannya:

• Verifikasi integritas aplikasi (Firebase App Check / Google Play Integrity): Google menyediakan atestasi perangkat Anda agar server kami hanya menerima permintaan dari aplikasi asli.
• RevenueCat: selain informasi pembelian, menerima pengenal akun Google Anda (bukan alamat email) sebagai ID pengguna, untuk menautkan langganan Anda di berbagai perangkat.
• CoinGecko: saat Anda menggunakan harga kripto langsung, simbol dari portofolio Anda diminta dari CoinGecko, yang dapat melihat alamat IP Anda. Penjelajah dan node blockchain yang disebutkan di atas juga dapat melihatnya.
• Google Fonts: aplikasi mengunduh font Inter dan JetBrains Mono melalui layanan Google Play, sehingga Google dapat melihat alamat IP Anda saat font diminta.
• Waktu tepercaya layanan Google Play: digunakan untuk mendeteksi manipulasi jam perangkat. Tidak ada data pribadi yang dikirim.
• Pengenalan teks (OCR) pada dokumen dilakukan di perangkat Anda dengan ML Kit. Tidak ada gambar yang dikirim ke server.
• Ko-fi: hanya jika Anda membuka tautan donasi; dalam hal itu kebijakan privasi mereka berlaku.

Server kami dapat mencatat alamat IP Anda untuk sementara guna membatasi penyalahgunaan.
    """.trimIndent()),
    LegalSection(
        "Passkey (Kunci Akses)",
        """
        KeyArmor dapat berfungsi sebagai penyedia passkey Android (Credential Manager), hanya jika Anda sendiri yang memilihnya di pengaturan sistem. Saat membuat atau menggunakan passkey, KeyArmor membaca domain situs atau aplikasi yang memintanya untuk mengaitkan passkey dengan situs tersebut dan menandatangani tantangan yang dikirim situs. Kunci privat dibuat di perangkat Anda dan disimpan terenkripsi bersama isi brankas Anda lainnya; kunci itu tidak dikirim ke server mana pun maupun pihak ketiga mana pun. Kunci itu hanya keluar dari perangkat Anda di dalam backup yang Anda buat sendiri (misalnya backup terenkripsi di Google Drive). Untuk menandatangani dengan passkey, KeyArmor meminta Anda memverifikasi identitas dengan sidik jari atau kata sandi utama. Situs tempat Anda masuk hanya menerima kunci publik dan tanda tangan, seperti pada passkey lainnya.
        """.trimIndent()
    ),
    LegalSection(
        "Penghapusan dan Penyimpanan Data",
        """
        Brankas Anda disimpan di perangkat Anda (dan di Google Drive Anda, jika Anda mengaktifkan backup) sampai Anda sendiri yang menghapusnya. Di server kami hanya ada data koordinasi: token notifikasi push yang diidentifikasi dengan email Anda dan, jika Anda menggunakan Warisan Digital atau Paket Keluarga, catatan undangan dan persetujuan serta blok terenkripsi dari brankas warisan. Data ini disimpan selama Anda menggunakan fitur tersebut atau sampai Anda meminta penghapusannya. Anda dapat menghapusnya melalui Pengaturan → Hapus semua data saya, atau dengan menulis ke support@thormakk.dev dari akun Google Anda; kami memproses permintaan tersebut dalam 30 hari. Rinciannya lengkap ada di halaman «Hapus akun dan data» di situs web kami.
        """.trimIndent()
    ),
    LegalSection(
        "Anak di Bawah Umur",
        """
        KeyArmor tidak ditujukan untuk anak di bawah 13 tahun (atau di bawah usia minimum yang ditetapkan hukum negara Anda untuk persetujuan digital), dan kami tidak dengan sengaja mengumpulkan data anak di bawah umur. Jika Anda yakin ada anak di bawah umur yang memberikan datanya kepada kami, tulis ke support@thormakk.dev dan kami akan menghapusnya.
        """.trimIndent()
    ),
    LegalSection(
        "Kontak",
        "Untuk pertanyaan terkait kebijakan ini, Anda dapat menulis ke support@thormakk.dev"
    ),
    LegalSection(
        "Terakhir diperbarui",
        "November 2026 (rev. 9)"
    )
)

private fun termsID() = listOf(
    LegalSection(
        "Syarat dan Ketentuan",
        ""
    ),
    LegalSection(
        "Penerimaan",
        """
        Dengan menginstal atau menggunakan KeyArmor, Anda menerima syarat-syarat ini sepenuhnya. Jika Anda tidak setuju dengan bagian mana pun, jangan gunakan aplikasi ini.
        """.trimIndent()
    ),
    LegalSection(
        "Penggunaan yang Diizinkan",
        """
        KeyArmor dirancang untuk penggunaan pribadi sebagai pengelola kata sandi dan brankas aman. Penggunaan untuk kegiatan ilegal, penyimpanan informasi yang memfasilitasi tindakan melanggar hukum, atau upaya merekayasa balik aplikasi dilarang.
        """.trimIndent()
    ),
    LegalSection(
        "Tanggung Jawab Pengguna",
        """
        Anda sepenuhnya bertanggung jawab atas:

        • Keamanan kata sandi utama Anda. Jika hilang, tidak ada mekanisme pemulihan dan data Anda akan menjadi tidak dapat diakses secara permanen.
        • Kunci cadangan yang terdiri dari 12 kata (jika Anda menggunakan fitur tersebut). Jika Anda kehilangannya dan juga kehilangan ponsel Anda, cadangan Drive Anda tidak dapat dibuka, bahkan oleh pengembang sekalipun.
        • Mempertahankan cadangan terkini brankas Anda melalui fungsi ekspor atau sinkronisasi Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Fungsionalitas Sinkronisasi",
        """
        Sinkronisasi Google Drive adalah fitur cadangan tambahan. Fitur ini tidak menggantikan praktik keamanan pribadi yang baik. Pengembang tidak menjamin ketersediaan berkelanjutan fitur ini, karena bergantung pada layanan pihak ketiga (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Langganan Premium",
        """
        KeyArmor menawarkan paket berbayar opsional (Bulanan, Tahunan) yang membuka fitur tambahan. Langganan diproses secara eksklusif melalui Google Play Billing. Dengan membeli langganan, Anda juga menyetujui Ketentuan Layanan Google Play.

        • Paket Bulanan dan Tahunan adalah langganan yang diperbarui otomatis. Anda dapat membatalkan kapan saja melalui Google Play; pembatalan berlaku pada akhir periode penagihan saat ini.
        • Pengembalian dana tunduk pada kebijakan pengembalian dana standar Google Play. Pengembang tidak memproses pengembalian dana secara independen.
        • Fitur premium dapat berkembang dari waktu ke waktu. Pengembang berhak untuk menambah, memodifikasi, atau menghentikan fitur premium tertentu dengan pemberitahuan yang wajar.
        """.trimIndent()
    ),
    LegalSection(
        "Mode Perjalanan Aman",
        """
        Mode Perjalanan Aman memungkinkan Anda menandai entri tertentu sebagai aman untuk perjalanan dan menyembunyikan semua entri lainnya saat mode aktif. Anda sepenuhnya bertanggung jawab untuk menentukan entri mana yang ditandai sebagai aman untuk perjalanan dan atas konsekuensi pengaktifan atau penonaktifan mode ini. Pengembang tidak bertanggung jawab atas entri yang tidak sengaja dibiarkan terlihat atau tersembunyi.
        """.trimIndent()
    ),
    LegalSection(
        "PIN Darurat",
        """
        Fitur PIN Darurat memicu tindakan tidak dapat dibatalkan (menghapus atau menyembunyikan data brankas sensitif) ketika PIN yang ditentukan dimasukkan. Setelah diaktifkan, data yang disembunyikan atau dihapus mungkin tidak dapat dipulihkan tergantung pada konfigurasi Anda. Anda bertanggung jawab penuh atas konfigurasi fitur ini dan atas kehilangan data apa pun yang diakibatkan oleh aktivasinya yang disengaja atau tidak disengaja.
        """.trimIndent()
    ),
    LegalSection(
        "Informasi Saldo Kripto",
        """
        Fitur saldo kripto langsung mengambil informasi saldo dari infrastruktur blockchain publik semata-mata untuk tujuan informasi. Informasi ini tidak merupakan saran keuangan. Pengembang tidak menjamin keakuratan, kelengkapan, atau ketepatan waktu data saldo. Jangan membuat keputusan keuangan semata-mata berdasarkan informasi yang ditampilkan di aplikasi ini.
        """.trimIndent()
    ),
    LegalSection(
        "Tanpa Jaminan",
        """
        Aplikasi ini disediakan "apa adanya", tanpa jaminan dalam bentuk apa pun, tersurat maupun tersirat. Pengembang tidak menjamin bahwa aplikasi bebas dari kesalahan, beroperasi tanpa gangguan, atau sesuai untuk tujuan tertentu.
        """.trimIndent()
    ),
    LegalSection(
        "Batasan Tanggung Jawab",
        """
        Sejauh yang diizinkan oleh hukum yang berlaku, pengembang tidak bertanggung jawab atas kehilangan data, kehilangan akses brankas, atau kerusakan langsung, tidak langsung, insidental, khusus, atau konsekuensial apa pun yang timbul dari penggunaan atau ketidakmampuan menggunakan aplikasi.

        Jika Anda tinggal di negara yang undang-undang perlindungan konsumennya memberikan hak-hak yang tidak dapat dicabut, ketentuan-ketentuan ini tidak mengurangi hak-hak tersebut. Ketentuan-ketentuan ini mengenai jaminan dan tanggung jawab hanya berlaku sejauh diizinkan oleh undang-undang.
        """.trimIndent()
    ),
    LegalSection(
        "Pembaruan Syarat",
        """
        Syarat-syarat ini dapat diperbarui kapan saja. Penggunaan aplikasi yang terus berlanjut setelah pembaruan yang diterbitkan merupakan penerimaan terhadap syarat-syarat baru.
        """.trimIndent()
    ),
    LegalSection(
        "Terakhir diperbarui",
        "November 2026 (rev. 3)"
    )
)

private fun legalID() = listOf(
    LegalSection(
        "Pemberitahuan Hukum dan Penafian",
        ""
    ),
    LegalSection(
        "Perangkat Lunak Disediakan Apa Adanya",
        """
        KeyArmor didistribusikan "apa adanya", tanpa jaminan dalam bentuk apa pun, tersurat maupun tersirat, termasuk namun tidak terbatas pada jaminan kelayakan jual, kesesuaian untuk tujuan tertentu, atau tidak adanya pelanggaran.
        """.trimIndent()
    ),
    LegalSection(
        "Batasan Tanggung Jawab",
        """
        Pengembang KeyArmor tidak bertanggung jawab dalam keadaan apa pun atas:

        • Kehilangan atau kerusakan data yang tersimpan di aplikasi
        • Hilangnya akses ke brankas akibat lupa atau kehilangan kata sandi utama atau kunci cadangan
        • Kerentanan atau kegagalan dalam komponen OS Android
        • Kegagalan, gangguan, atau perubahan layanan Google Drive
        • Kerusakan langsung, tidak langsung, insidental, khusus, atau konsekuensial apa pun yang timbul dari penggunaan atau ketidakmampuan menggunakan aplikasi
        • Kerusakan apa pun yang diakibatkan oleh akses tidak sah ke data pengguna oleh pihak ketiga
        • Kehilangan data akibat aktivasi fitur PIN Darurat yang disengaja atau tidak disengaja
        """.trimIndent()
    ),
    LegalSection(
        "Transmisi Data Google Drive",
        """
Saat pengguna mengaktifkan sinkronisasi Google Drive (cadangan) atau menggunakan Warisan Digital / Paket Keluarga (Google Firebase), data dikirim dalam keadaan sudah terenkripsi ke infrastruktur Google sesuai Ketentuan Layanan Google. Dalam setiap kasus, data meninggalkan perangkat sebagai blob terenkripsi yang buram. Pengembang tidak memiliki akses atau kendali atas infrastruktur Google dan tidak dapat membaca konten yang dikirim.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore dan Ketergantungan Perangkat Keras",
        """
        Keamanan enkripsi KeyArmor sebagian bergantung pada implementasi Android Keystore perangkat pengguna. Implementasi ini bervariasi berdasarkan produsen, chipset, dan versi Android. Pengembang tidak mengontrol atau menjamin keamanan implementasi perangkat keras pihak ketiga. Sangat disarankan untuk menjaga OS perangkat tetap diperbarui.
        """.trimIndent()
    ),
    LegalSection(
        "Batasan Layanan Isi Otomatis",
        """
        Layanan Isi Otomatis KeyArmor mungkin tidak berfungsi dengan benar pada semua aplikasi atau browser. Khususnya, Google Chrome tidak mendukung kerangka kerja isi otomatis standar Android — ini adalah keterbatasan Chrome yang mempengaruhi semua pengelola kata sandi pihak ketiga secara setara dan berada di luar kendali pengembang. Pengembang tidak menjamin bahwa layanan isi otomatis akan berfungsi dengan aplikasi tertentu mana pun.
        """.trimIndent()
    ),
    LegalSection(
        "Penafian Saldo Kripto Langsung",
        """
        Data saldo langsung diambil dari node dan API blockchain pihak ketiga publik. Pengembang tidak mengoperasikan atau mengendalikan node-node ini. Informasi saldo disediakan semata-mata untuk tujuan informasi dan mungkin tertunda, tidak akurat, atau tidak tersedia. Informasi ini tidak merupakan saran keuangan atau investasi. Pengembang tidak bertanggung jawab atas keputusan yang dibuat berdasarkan data saldo yang ditampilkan di aplikasi.
        """.trimIndent()
    ),
    LegalSection(
        "Berbagi P2P",
        """
        Fitur berbagi entri P2P memfasilitasi pertukaran langsung dan terenkripsi dari entri brankas antar pengguna. Pengembang tidak bertanggung jawab atas konten entri yang dibagikan antar pengguna, identitas atau keterpercayaan penerima, atau konsekuensi apa pun yang timbul dari berbagi data brankas dengan pihak ketiga.
        """.trimIndent()
    ),
    LegalSection(
        "Pustaka Pihak Ketiga",
        """
        KeyArmor menggunakan pustaka sumber terbuka pihak ketiga yang dianggap aman pada saat publikasi. Pengembang tidak bertanggung jawab atas kegagalan, kerentanan, atau perilaku tak terduga yang berasal dari komponen pihak ketiga.
        """.trimIndent()
    ),
    LegalSection(
        "Hukum yang Berlaku",
        """
        Pemberitahuan hukum ini diatur oleh hukum yang berlaku di Republik Argentina. Setiap perselisihan yang timbul dari penggunaan aplikasi akan diajukan ke pengadilan yang berwenang di yurisdiksi tersebut.

        Jika Anda tinggal di negara yang undang-undang perlindungan konsumennya memberikan hak-hak yang tidak dapat dicabut, pemberitahuan ini tidak mengurangi hak-hak tersebut. Ketentuan dalam pemberitahuan ini mengenai jaminan, tanggung jawab, hukum yang berlaku, dan pengadilan hanya berlaku sejauh diizinkan oleh undang-undang.
        """.trimIndent()
    ),
    LegalSection(
        "Terakhir diperbarui",
        "November 2026 (rev. 3)"
    )
)

private fun licenseID() = listOf(
    LegalSection("Lisensi Source Available", ""),
    LegalSection("Kode Sumber Dipublikasikan Hanya untuk Tujuan Audit Keamanan", """
Kode sumber KeyArmor tersedia secara publik semata-mata agar peneliti keamanan dan pengguna dapat memeriksa dan mengaudit praktik keamanannya. Tidak ada hak apa pun yang diberikan di luar itu — semua hak dilindungi oleh pemegang hak cipta.
    """.trimIndent()),
    LegalSection("Yang Dapat Anda Lakukan", """
• Membaca dan memeriksa kode sumber untuk riset keamanan dan audit pribadi
• Melaporkan kerentanan keamanan kepada tim KeyArmor melalui saluran resmi
    """.trimIndent()),
    LegalSection("Yang Tidak Boleh Anda Lakukan", """
Tanpa izin tertulis sebelumnya yang jelas dari pemegang hak cipta, Anda tidak boleh:

• Menggunakan atau menjalankan perangkat lunak di luar distribusi resminya
• Menyalin, mereproduksi, atau mengekstrak kode sumber untuk tujuan apa pun selain membaca
• Memodifikasi, mengadaptasi, menerjemahkan, atau membuat karya turunan berdasarkan kode ini
• Mendistribusikan, mempublikasikan, mensublisensikan, menjual, atau membuatnya tersedia bagi pihak ketiga dengan cara apa pun
• Menggunakan kode sumber sebagai dasar untuk produk, layanan, atau proyek lain
• Menggunakan nama, logo, atau merek dagang terkait KeyArmor
    """.trimIndent()),
    LegalSection("Kode Sumber", """
Kode sumber KeyArmor tersedia secara publik.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Tanpa Jaminan", """
Kode sumber disediakan "sebagaimana adanya", tanpa jaminan dalam bentuk apa pun, baik tersurat maupun tersirat. Pemegang hak cipta tidak membuat pernyataan mengenai kesesuaiannya untuk tujuan tertentu.
    """.trimIndent()),
    LegalSection("Hak Cipta", "© 2026 KeyArmor. Semua hak dilindungi. Source Available.")
)

private fun opensourceID() = listOf(
    LegalSection(
        "Pemberitahuan Sumber Terbuka",
        ""
    ),
    LegalSection(
        "Pustaka yang Digunakan",
        """
        KeyArmor menggunakan pustaka sumber terbuka berikut. Kecuali dinyatakan lain, semuanya didistribusikan di bawah lisensi Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Versi: BOM 2026.09.00 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Versi: 1.2.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Versi: 2.8.5 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Versi: 2.60.1 | Lisensi: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Versi: 1.11.0 | Lisensi: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Versi: 1.1.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Versi: 2.10.2 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Versi: 2.11.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Versi: 2.12.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Versi: 22.0.0 | Lisensi: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Versi: 2.9.2 | Lisensi: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Versi: v3-rev20240123-2.0.0 | Lisensi: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Versi: 2.2.0 | Lisensi: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Versi: 4.3.0 | Lisensi: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Versi: 0.37.3 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Versi: BOM 2026.09.00 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Versi: 1.19.1 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Versi: 1.13.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Versi: 1.3.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Versi: 1.3.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Versi: 1.8.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Versi: 1.4.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Versi: 2.4.21 | Lisensi: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Versi: 1.2.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Versi: 1.4.1 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Versi: 1.6.0 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Versi: 1.6.2 | Lisensi: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Versi: BOM 35.1.0 | Lisensi: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Versi: 2.15.0 | Lisensi: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Versi: 3.0.1 | Lisensi: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Versi: 2.0.27.0 | Lisensi: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Versi: 1.2.1 | Lisensi: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Versi: 1.86 | Lisensi: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Versi: 10.29.0 | Lisensi: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Versi: 5.2.0 | Lisensi: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Versi: 5.19.1 | Lisensi: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Versi: 1.2.1 | Lisensi: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Versi: 16.0.1 | Lisensi: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Versi: 16.0.1 | Lisensi: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Versi: 2026-07-09 | Lisensi: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Teks Lengkap Apache 2.0",
        """
        Teks lengkap lisensi Apache 2.0 tersedia di:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataID() = listOf(
    LegalSection("Hapus Akun & Data", ""),
    LegalSection("Informasi apa saja yang disimpan KeyArmor mengenai Anda", """
Brankas Anda (kata sandi, catatan, kartu, dompet, lampiran, dan riwayat kata sandi) tidak pernah disimpan di server kami. Data tersebut disimpan dalam bentuk terenkripsi di perangkat Anda, dan jika Anda mengaktifkan fitur pencadangan, data tersebut akan disimpan di Google Drive Anda sendiri — bukan di tempat yang memungkinkan kami membacanya atau menghapusnya, karena data tersebut telah dienkripsi sebelum meninggalkan perangkat Anda.

Satu-satunya data yang terkait dengan akun Google Anda yang sampai ke server kami (Firebase) adalah data yang terkait dengan penghubungan akun tersebut untuk fitur opsional — pencadangan Google Drive, Warisan Digital, atau Paket Keluarga. Hanya dengan menghubungkan akun Google untuk salah satu fitur tersebut, akan tercatat data minimal di sana (token notifikasi push, yang diidentifikasi berdasarkan email Anda), bahkan sebelum Anda mengonfigurasi Digital Inheritance atau Family Plan. Jika Anda belum pernah menghubungkan akun Google ke KeyArmor, tidak ada data apa pun tentang Anda di server kami yang perlu dihapus.
    """.trimIndent()),
    LegalSection("Menghapus dari aplikasi", """
Anda dapat menghapus semua data Anda kapan saja dari Pengaturan → Hapus semua data saya di dalam aplikasi. Jika Anda belum pernah menghubungkan akun Google ke KeyArmor (untuk pencadangan Drive, Warisan Digital, atau Paket Keluarga), tindakan ini akan segera dan secara permanen menghapus brankas Anda dari perangkat.

Jika Anda pernah menghubungkannya, aplikasi akan meminta Anda untuk masuk ke akun Google agar, dalam proses yang sama, aplikasi dapat menghapus catatan di sisi server (Firebase) yang terkait serta salinan cadangan dan Time Machine yang disimpan KeyArmor di Google Drive Anda. Hanya jika Anda tidak dapat masuk, aplikasi akan beralih ke penghapusan data lokal saja — dan aplikasi akan memperingatkan Anda terlebih dahulu bahwa catatan di sisi server akan tetap ada hingga dihapus secara terpisah, menggunakan permintaan di bawah ini.
    """.trimIndent()),
    LegalSection("Apa yang tidak dihapus", """
Menghapus data Anda tidak akan menghapus apa yang ada di luar aplikasi:

• File yang Anda ekspor sendiri (file CSV, PDF, atau .kab yang Anda simpan atau bagikan). File-file tersebut milik Anda dan disimpan di mana pun Anda menempatkannya, jadi Anda perlu menghapusnya di sana.
• Salinan cadangan yang ditinggalkan oleh instalasi KeyArmor sebelumnya di folder Unduhan Anda. Android tidak lagi mengizinkan instalasi baru untuk mengaksesnya; hapuslah dari folder Unduhan Anda.
• Langganan Anda. Langganan ini dikelola oleh Google Play, jadi menghapus data Anda tidak akan membatalkannya; batalkan langganan tersebut di Google Play → Langganan.
    """.trimIndent()),
    LegalSection("Permohonan penghapusan data akun Anda", """
Jika Anda telah menghubungkan akun Google ke KeyArmor tetapi tidak dapat masuk untuk menggunakan fitur penghapusan dalam aplikasi di atas (misalnya, aplikasi tersebut sudah tidak terpasang lagi di perangkat Anda), ajukan permintaan penghapusan data di sisi server yang terkait dengan mengirimkan email kepada kami dari akun Google yang sama tersebut.

Kirimkan email ke: support@thormakk.dev

Kami memproses permintaan penghapusan dalam waktu 30 hari dan akan mengirimkan konfirmasi melalui email setelah proses selesai. Anda tidak perlu menginstal aplikasi tersebut untuk mengajukan permintaan ini.
    """.trimIndent())
)
