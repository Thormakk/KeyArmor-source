package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentTR(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyTR()
    "terms"      -> termsTR()
    "legal"      -> legalTR()
    "license"    -> licenseTR()
    "opensource" -> opensourceTR()
    "delete"     -> deleteDataTR()
    else         -> listOf(LegalSection("Belge bulunamadı", ""))
}

private fun privacyTR() = listOf(
    LegalSection(
        "Gizlilik Politikası",
        ""
    ),
    LegalSection(
        "Veri Toplama",
        """
        KeyArmor, herhangi bir kişisel veriyi harici sunuculara toplamaz, iletmez veya paylaşmaz. Uygulamaya girdiğiniz tüm bilgiler, Google Drive senkronizasyonunu açıkça etkinleştirmediğiniz sürece yalnızca cihazınızda kalır.

        KeyArmor, uygulamanın temel özellikleri (kasa, otomatik doldurma, güvenlik denetimi, ihlal izleme) için e-posta adresinizi veya herhangi bir hesap kimlik bilginizi toplamaz — bunlar herhangi bir hesap olmadan tamamen çalışır. Dijital Miras veya Aile Planı'nı etkinleştirirseniz, bu özellikler Google ile Oturum Açma gerektirir: Google hesabı e-posta adresiniz, sizi tanımlamak ve belirlediğiniz mirasçılar veya aile üyeleriyle eşleştirmek amacıyla ve ilgili bildirim e-postalarını e-posta sağlayıcımız (Brevo) aracılığıyla göndermek için Google Firebase'de saklanır. Bu e-posta yalnızca bu iki özellik için kullanılır ve asla satılmaz veya başkasıyla paylaşılmaz. Dijital Miras ve Aile Planı dışında, tek kişisel tanımlayıcı, kurulum sırasında isteğe bağlı olarak ayarlayabileceğiniz bir ad veya takma addır; bu bilgi yalnızca cihazınızda şifreli biçimde saklanır.
        """.trimIndent()
    ),
    LegalSection(
        "Yerel Depolama",
        """
        Tüm verileriniz (şifreler, passkey'ler, notlar, kredi kartları, banka hesapları, kripto cüzdanlar, ekler ve şifre geçmişi), ana şifrenizden Argon2id ile türetilen ve cihazınızdan asla çıkmayan bir anahtar kullanılarak AES-256-GCM ile şifrelenerek depolanır. Biyometrik kilit açmayı etkinleştirirseniz, bu anahtarın bir kopyası ayrıca cihazınızdaki Android Keystore anahtarıyla korunur. Açık onayınız olmadan hiçbir veri cihazınızı terk etmez.
        """.trimIndent()
    ),
    LegalSection(
        "Ana Şifre",
        """
        Ana şifreniz hiçbir zaman düz metin veya kurtarılabilir bir formda depolanmaz. Yalnızca Argon2id aracılığıyla kasa şifreleme anahtarını türetmek için kullanılır. Ana şifrenizi kaybederseniz verileriniz kurtarılamaz.
        """.trimIndent()
    ),
    LegalSection(
        "Biyometrik Veriler",
        """
        KeyArmor'daki biyometrik kimlik doğrulama, BiometricPrompt API aracılığıyla tamamen Android işletim sistemi tarafından yönetilir. KeyArmor hiçbir zaman biyometrik verilere erişmez, depolamaz veya işlemez.

        Biyometri, uygulamada iki bağlamda kullanılır:

        • Kasa kilidi açma: uygulamaya başlangıçta erişimi doğrulamak için, tüm planlarda kullanılabilir.
        • Gelişmiş Biyometrik Koruma (Premium): kasanın belirli bölümlerindeki (şifreler, kredi kartları, özel alanlar) hassas alanları görüntülemeden önce biyometrik doğrulama gerektiren isteğe bağlı bir bariyer. Ayarlar'dan yapılandırılabilir — özelleştirilebilir zaman aşımıyla erişim başına veya oturum başına modu destekler. Aynı gizlilik güvenceleri geçerlidir: KeyArmor biyometrik verileri depolamaz veya işlemez.
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive Senkronizasyonu",
        """
Google Drive Senkronizasyonu tamamen isteğe bağlıdır. Etkinleştirildiğinde KeyArmor:

• Kendi şifreli yedeklerinizi ve Time Machine geri yükleme noktalarınızı, Google Drive'ınızdaki özel, uygulamaya ait bir klasörde saklamak için tek ve asgari bir izin (DRIVE_APPDATA) ister. Bu klasör gizlidir — başka hiçbir uygulama ve Drive'da gezinirken siz bile onu göremez veya değiştiremez.
• Yüklemeden önce kasayı 12 kelimelik bir yedek anahtarla şifreler. Anahtar cihazınızda oluşturulur ve sadece sizde bulunur: Ne Google ne de geliştirici dosyayı okuyabilir veya anahtarı geri getirebilir.
• Yedek anahtarınızın PDF dosyasını Google Dokümanlar, Drive veya başka bir uygulamaya kaydederseniz, bunu Android paylaşım ekranı aracılığıyla kendiniz yaparsınız; söz konusu uygulama kendi gizlilik politikasına tabidir.
• Anahtarınızı değiştirirseniz, önceki anahtarları şifrelenmiş olarak telefonunuzda saklayabilirsiniz; kitin PDF dosyası bunları içerebilir. KeyArmor bunları hiçbir sunucuya göndermez.
• Başka hiçbir Drive dosyasına asla erişmez, görünür klasörler oluşturmaz ve dosya paylaşmaz.
• İsteğe bağlı olarak yedeğin ikinci bir yerel kopyasını cihazınızın Downloads/KeyArmor/ klasöründe tutar; bu kopya cihazdan asla çıkmaz.
• İstek üzerine senkronizasyon tüm planlarda mevcuttur (elle tetiklenir). Otomatik günlük senkronizasyon Premium'da mevcuttur.

Dijital Miras ve Aile Planı, Google Drive'ınızı değil, Google Firebase'i kullanır. Firebase, onayları, davetleri ve bildirimleri koordine eder. Miras kasası, cihazınızda mirasçının açık anahtarıyla uçtan uca şifrelenir ve ortaya çıkan şifreli blok, kısa süreli (15 dakika) imzalı bağlantılar aracılığıyla geliştiricinin Firebase Cloud Storage'daki özel bir depolama kovasına yüklenir. Sunucu bu bloğu saklar ancak mirasçının anahtarını asla almaz, dolayısıyla bloğu okuyamaz.

Google hesabınızın bağlantısını senkronizasyon ekranından istediğiniz zaman kesebilirsiniz.
    """.trimIndent()
    ),
    LegalSection(
        "Ekler",
        """
        Ekler özelliği, kasadaki girişlerinize dosya eklemenizi sağlar (her giriş için en fazla 5 dosya, her biri en fazla 5 MB). Tüm ekler, cihazın dahili depolama alanına kaydedilmeden önce AES-256-GCM ile şifrelenir. Google Drive senkronizasyonunu etkinleştirirseniz, ekleriniz ve belge fotoğraflarınız, yedek anahtarınızla şifrelenmiş olarak yüklediğiniz yedeklemeye dahil edilir. Bu dosyalar hiçbir zaman başka bir hizmete gönderilmez.
        """.trimIndent()
    ),
    LegalSection(
        "Şifre Geçmişi",
        """
        KeyArmor, her giriş için önceki şifrelerin yerel geçmişini tutar. Bu geçmiş, cihazda şifreli olarak depolanır ve hiçbir zaman harici bir sunucuya iletilmez.
        """.trimIndent()
    ),
    LegalSection(
        "Uygulama İçi Bildirimler",
        """
        KeyArmor, önemli güvenlik olayları hakkında sizi uyarmak için dahili bildirimler oluşturur:

        • Sona erme uyarıları: bir veya daha fazla şifre sona erme tarihine yaklaştığında gönderilir.
        • Süresi dolan şifreler: bir veya daha fazla şifre sona erme tarihini geçtiğinde gönderilir.
        • İhlal uyarıları: şifreleriniz bilinen veri ihlallerinde tespit edildiğinde gönderilir.

        Bu üç bildirim türü tamamen cihazınızda oluşturulur ve harici sunuculara hiçbir zaman iletilmez. Dijital Miras veya Aile Planı'nı kullanıyorsanız, bu özelliklerle ilgili bildirimler de alırsınız (bir mirasçının daveti kabul etmesi, bir aile üyesinin eklenmesi vb.); bunlar başka bir hesap tarafından başlatılan olaylara bağlı olduğundan Google Firebase tarafından tetiklenir ve Firebase Cloud Messaging aracılığıyla teslim edilir. Kaynağı ne olursa olsun tüm bildirimler 30 gün sonra otomatik olarak silinir. Herhangi bir bildirimi yanlara kaydırarak silebilir veya Bildirimler ekranından hepsini aynı anda temizleyebilirsiniz. Push bildirimleri göndermek için sistem izni gereklidir.
        """.trimIndent()
    ),
    LegalSection(
        "Güvenlik Denetimi",
        """
        Güvenlik denetimi özelliği, şifrelerinizi cihazda yerel olarak analiz eder. Yaygın şifre kontrolü, harici hizmetlere sorgu göndermeksizin uygulamanın kendisinde depolanan önceden tanımlanmış bir listeyle karşılaştırılarak yapılır.
        """.trimIndent()
    ),
    LegalSection(
        "İhlal İzleme (HIBP)",
        """
        KeyArmor, güvenlik araştırmacısı Troy Hunt tarafından işletilen Have I Been Pwned (HIBP) hizmetini kullanarak şifrelerinizin bilinen veri ihlallerinde görünüp görünmediğini kontrol edebilir.

        Bu kontrol k-anonimlik modelini kullanır: Şifrenizin SHA-1 karmasının yalnızca ilk 5 karakteri HIBP API'sine gönderilir. Tam karma hiçbir zaman iletilmez. Eşleştirme cihazınızda yerel olarak gerçekleştirilir. Hiçbir zaman kullanıcı adı, e-posta adresi veya hesap ilişkilendirmesi gönderilmez. HIBP yanıtları sahte karmalarla doldurulur; böylece ağ trafiği analizi bile kontrol edilen şifre hakkında hiçbir şey ortaya koymaz.
        """.trimIndent()
    ),
    LegalSection(
        "Otomatik Doldurma Servisi",
        """
        KeyArmor isteğe bağlı bir Android Otomatik Doldurma servisi içerir. Sistem ayarlarında etkinleştirildiğinde, kasanızdan eşleşen kimlik bilgilerini önermek için o anda etkin olan giriş ekranının uygulama paket adını veya web sitesi URL'sini okur. Bu eşleştirme tamamen cihazda gerçekleştirilir. Hiçbir uygulama adı, web sitesi URL'si veya kimlik bilgisi harici bir sunucuya iletilmez. Otomatik doldurma servisi, giriş bağlamını belirlemek için kesinlikle gerekli olanın ötesinde diğer uygulamaların içeriğini okumaz.
        """.trimIndent()
    ),
    LegalSection(
        "Kripto Cüzdan Anlık Bakiye",
        """
        Bir kripto cüzdan girişi kaydettiğinizde ve anlık bakiye izlemeyi etkinleştirdiğinizde, KeyArmor girdiğiniz cüzdan adresiyle ilişkili bakiyeyi almak için genel blok zinciri düğümlerine veya API'lere sorgu gönderir. Yalnızca cüzdan adresi iletilir — özel anahtar, tohum ifadesi veya kişisel veriler gönderilmez. Bu sorgular doğrudan genel altyapıya gider; KeyArmor herhangi bir ara sunucu işletmez.
        """.trimIndent()
    ),
    LegalSection(
        "P2P Giriş Paylaşımı",
        """
        KeyArmor, bireysel kasa girişlerini QR kodu veya şifreli metin aracılığıyla diğer KeyArmor kullanıcılarıyla doğrudan paylaşmanıza olanak tanır. Bu aktarım uçtan uca şifreli ve eşler arasıdır — hiçbir sunucu veya aracı dahil değildir. Girişleri kiminle paylaştığınızı seçmekten ve şifreli metni iletmek için kullanılan kanalın güvenliğinden (QR'yi doğrudan kullanmıyorsanız) yalnızca siz sorumlusunuz.
        """.trimIndent()
    ),
    LegalSection(
        "Üçüncü Taraf Kütüphaneler",
        """
        KeyArmor açık kaynaklı kütüphaneler kullanır (bkz. Açık Kaynak Bildirimi). Bu kütüphanelerin hiçbiri, bu uygulama tarafından kullanılan yapılandırmada telemetri, analitik veya veri toplama mekanizmaları içermez.

        KeyArmor ayrıca belirli özellikler için iki üçüncü taraf hizmetine dayanır: Premium abonelik durumunuzu doğrulayan ve yalnızca satın almayla ilgili bilgileri alan (asla kasanızı, ana parolanızı veya kurtarma anahtarınızı almayan) RevenueCat; ve Dijital Miras ile Aile Planı'nın gerektirdiği işlem e-postalarını (davetler, onay doğrulamaları, bildirimler) gönderen ve bunun için bu özelliklere dahil olan kişilerin e-posta adresini alan Brevo. Bu hizmetlerin hiçbiri reklam veya davranışsal analiz için kullanılmaz.
        """.trimIndent()
    ),
    LegalSection(
        "Ana Ekran Widget'ı",
        """
        KeyArmor, kasanızda depolanan toplam giriş sayısını gösteren isteğe bağlı bir Android ana ekran widget'ı içerir. Bu widget yalnızca yerel cihaz verilerine erişir. Harici sunuculara herhangi bir bilgi iletmez. Giriş sayısı, tek tek girişlerin içeriğini açıklamayan toplu bir veridir. Widget'ı ana ekranınıza yerleştirip yerleştirmeyeceğinizi kendiniz seçebilirsiniz.
        """.trimIndent()
    ),
    LegalSection(
        "İzinler",
        """
        KeyArmor yalnızca işleyişi için gerekli izinleri talep eder:

        • Biyometrik (isteğe bağlı): parmak izi ile kilit açma için.
        • İnternet (isteğe bağlı): yalnızca sizin etkinleştirdiğiniz özellikler için kullanılır — Google Drive senkronizasyonu, Dijital Miras ve Aile Planı (Google Firebase üzerinden), canlı kripto cüzdan bakiye sorguları, Have I Been Pwned ile ihlal kontrolleri ve abonelik doğrulaması (Google Play Billing / RevenueCat).
        • Dosya erişimi (isteğe bağlı): kasa içe/dışa aktarma ve ek ekleme için, yalnızca siz başlattığınızda.
        • Kamera (isteğe bağlı): iki faktörlü kimlik doğrulama (2FA/TOTP) kurulumu sırasında veya paylaşılan girişler alınırken QR kodlarını taramak için. Yalnızca açık kullanıcı isteği üzerine etkinleştirilir.

        • Otomatik doldurma servisi (isteğe bağlı): Android sistem ayarlarından manuel olarak etkinleştirilir. Yalnızca diğer uygulamaların giriş ekranlarında kimlik bilgileri önermek için kullanılır.
        • Mikrofon (isteğe bağlı): yalnızca Güvenli Notlar'daki sesli dikte için. Konuşma tanıma cihazınızda yapılır; KeyArmor ses kaydetmez, saklamaz ve hiçbir sunucuya göndermez.
        • Takvim, salt okunur (isteğe bağlı): Güvenli Seyahat Modu, etkinlikleriniz arasında seyahatleri bulmak için kullanır. Etkinlikler cihazınızda işlenir ve asla iletilmez.
        • Bildirimler ve tam zamanlı alarmlar (isteğe bağlı): güvenlik uyarıları ve kurduğunuz hatırlatıcılar için. Cihazın açılış olayı, yeniden başlatma sonrasında bunları yeniden planlamak için kullanılır.
        • Ağ durumu (isteğe bağlı): yalnızca mobil veri mi Wi-Fi mı kullandığınızı anlamak ve büyük Google Drive yedeklerinin Wi-Fi'ı beklemesini sağlamak için. Ağınız hakkında hiçbir bilgi toplanmaz veya gönderilmez.

        KeyArmor, kişilere, konuma veya yukarıda belirtilmeyen diğer kişisel verilere erişim talep etmez.
        """.trimIndent()
    ),
    LegalSection("Veri alan diğer hizmetler", """
Yukarıda açıklanan hizmetlere ek olarak KeyArmor aşağıdakileri yalnızca ihtiyaç duyan işlevler için kullanır:

• Uygulama bütünlüğü doğrulaması (Firebase App Check / Google Play Integrity): Google, sunucumuzun yalnızca gerçek uygulamadan gelen istekleri kabul etmesi için cihazınızın bir doğrulamasını sağlar.
• RevenueCat: satın alma bilgilerine ek olarak, aboneliğinizi cihazlar arasında bağlamak için Google hesabınızın tanımlayıcısını (e-posta adresinizi değil) kullanıcı kimliği olarak alır.
• CoinGecko: canlı kripto fiyatlarını kullandığınızda portföyünüzdeki semboller CoinGecko'dan istenir ve CoinGecko IP adresinizi görebilir. Yukarıda belirtilen blokzincir gezginleri ve düğümleri de görebilir.
• Google Fonts: uygulama Inter ve JetBrains Mono yazı tiplerini Google Play hizmetleri üzerinden indirir; bu nedenle Google, istendiklerinde IP adresinizi görebilir.
• Google Play hizmetleri güvenilir zaman: cihaz saatine müdahaleyi algılamak için kullanılır. Kişisel veri gönderilmez.
• Belgelerde metin tanıma (OCR) cihazınızda ML Kit ile yapılır. Hiçbir görüntü sunucuya gönderilmez.
• Ko-fi: yalnızca bağış bağlantısını açarsanız; bu durumda kendi gizlilik politikaları geçerlidir.

Sunucularımız, kötüye kullanımı sınırlamak için IP adresinizi geçici olarak kaydedebilir.
    """.trimIndent()),
    LegalSection(
        "Passkey'ler (geçiş anahtarları)",
        """
        KeyArmor, yalnızca sistem ayarlarında siz seçerseniz, Android passkey sağlayıcısı (Credential Manager) olarak çalışabilir. Bir passkey oluşturduğunuzda veya kullandığınızda KeyArmor, onu o siteyle ilişkilendirmek ve sitenin gönderdiği meydan okumayı (challenge) imzalamak için talepte bulunan sitenin veya uygulamanın alan adını okur. Özel anahtar cihazınızda oluşturulur ve kasanızın geri kalanıyla birlikte şifreli olarak saklanır; hiçbir sunucuya veya üçüncü tarafa gönderilmez. Yalnızca sizin oluşturduğunuz yedeklerin içinde cihazınızdan çıkar (örneğin Google Drive'daki şifreli yedek). Bir passkey ile imza atmak için KeyArmor sizden parmak izinizle veya ana şifrenizle kimliğinizi doğrulamanızı ister. Giriş yaptığınız site, her passkey'de olduğu gibi yalnızca genel anahtarı ve imzayı alır.
        """.trimIndent()
    ),
    LegalSection(
        "Verilerin Silinmesi ve Saklanması",
        """
        Kasanız, siz silene kadar cihazınızda (ve yedeklemeyi etkinleştirirseniz Google Drive'ınızda) saklanır. Sunucularımızda yalnızca koordinasyon verileri bulunur: e-postanızla tanımlanan anlık bildirim (push) belirteci ve Dijital Miras veya Aile Planı kullanıyorsanız davet ve onay kayıtları ile miras kasasının şifreli bloğu. Bunlar, bu özellikleri kullandığınız sürece veya silinmesini talep edene kadar saklanır. Bunları Ayarlar → Tüm verilerimi sil yolundan ya da Google hesabınızdan support@thormakk.dev adresine yazarak silebilirsiniz; bu talepleri 30 gün içinde işleme alırız. Ayrıntılı bilgi web sitemizdeki «Hesabı ve Verileri Sil» sayfasında yer almaktadır.
        """.trimIndent()
    ),
    LegalSection(
        "Reşit Olmayanlar",
        """
        KeyArmor 13 yaşın altındaki çocuklara (veya ülkenizin yasalarının dijital rıza için belirlediği asgari yaşın altındakilere) yönelik değildir ve reşit olmayanlara ait verileri bilerek toplamayız. Bir reşit olmayanın bize veri verdiğini düşünüyorsanız support@thormakk.dev adresine yazın, verileri sileriz.
        """.trimIndent()
    ),
    LegalSection(
        "İletişim",
        "Bu politikayla ilgili herhangi bir soru için support@thormakk.dev adresine yazabilirsiniz"
    ),
    LegalSection(
        "Son güncelleme",
        "Ekim 2026 (rev. 8)"
    )
)

private fun termsTR() = listOf(
    LegalSection(
        "Hüküm ve Koşullar",
        ""
    ),
    LegalSection(
        "Kabul",
        """
        KeyArmor'ı yükleyerek veya kullanarak bu koşulların tamamını kabul etmiş olursunuz. Herhangi bir bölümüyle aynı fikirde değilseniz uygulamayı kullanmayın.
        """.trimIndent()
    ),
    LegalSection(
        "İzin Verilen Kullanım",
        """
        KeyArmor, bir şifre yöneticisi ve güvenli kasa olarak kişisel kullanım için tasarlanmıştır. Yasadışı faaliyetler için, yasadışı eylemleri kolaylaştıran bilgileri depolamak için veya uygulamanın tersine mühendisliğini yapmaya çalışmak için kullanmak yasaktır.
        """.trimIndent()
    ),
    LegalSection(
        "Kullanıcı Sorumluluğu",
        """
        Yalnızca şunlardan sorumlusunuz:

        • Ana şifrenizin güvenliği. Kaybolursa, kurtarma mekanizması yoktur ve verilerinize kalıcı olarak erişilemez hale gelir.
        • 12 kelimelik yedek anahtar (bu özelliği kullanıyorsanız). Bu anahtarı kaybederseniz ve aynı zamanda telefonunuzu da kaybederseniz, Drive yedeklemeleriniz açılmaz; bunu geliştirici bile yapamaz.
        • Dışa aktarma işlevi veya Drive senkronizasyonu aracılığıyla kasanızın güncel yedeklerini tutmak.
        """.trimIndent()
    ),
    LegalSection(
        "Senkronizasyon İşlevselliği",
        """
        Google Drive senkronizasyonu yardımcı bir yedekleme özelliğidir. İyi kişisel güvenlik uygulamalarının yerini tutmaz. Geliştirici, bu özelliğin sürekli kullanılabilirliğini garanti etmez; zira üçüncü taraf hizmetlere (Google) bağlıdır.
        """.trimIndent()
    ),
    LegalSection(
        "Premium Abonelikler",
        """
        KeyArmor, ek özelliklerin kilidini açan isteğe bağlı ücretli planlar (Aylık, Yıllık) sunar. Abonelikler yalnızca Google Play Billing aracılığıyla işlenir. Abonelik satın alarak Google Play'in Hizmet Şartlarını da kabul etmiş olursunuz.

        • Aylık ve Yıllık planlar otomatik yenilenen aboneliklerdir. Google Play aracılığıyla istediğiniz zaman iptal edebilirsiniz; iptal mevcut faturalama döneminin sonunda geçerli olur.
        • İadeler Google Play'in standart iade politikasına tabidir. Geliştirici iadeleri bağımsız olarak işlemez.
        • Premium özellikler zamanla gelişebilir. Geliştirici, makul bir bildirimle belirli premium özellikleri ekleme, değiştirme veya kullanımdan kaldırma hakkını saklı tutar.
        """.trimIndent()
    ),
    LegalSection(
        "Seyahat Güvenli Modu",
        """
        Seyahat Güvenli Modu, belirli girişleri seyahate uygun olarak işaretlemenize ve mod etkinken diğer tüm girişleri gizlemenize olanak tanır. Hangi girişleri seyahate uygun olarak işaretleyeceğinizi belirleme ve bu modu etkinleştirme veya devre dışı bırakmanın sonuçlarından yalnızca siz sorumlusunuz. Geliştirici, yanlışlıkla görünür bırakılan veya gizlenen girişlerden sorumlu tutulamaz.
        """.trimIndent()
    ),
    LegalSection(
        "Panik PIN",
        """
        Panik PIN özelliği, belirlenen PIN girildiğinde geri alınamaz bir eylem (hassas kasa verilerini silme veya gizleme) tetikler. Bir kez etkinleştirildiğinde, yapılandırmanıza bağlı olarak gizlenen veya silinen veriler kurtarılamayabilir. Bu özelliği yapılandırma ve kasıtlı veya yanlışlıkla etkinleştirilmesinden kaynaklanan veri kayıpları için tam sorumluluk size aittir.
        """.trimIndent()
    ),
    LegalSection(
        "Kripto Bakiye Bilgisi",
        """
        Anlık kripto bakiye özelliği, yalnızca bilgilendirme amacıyla genel blok zinciri altyapısından bakiye bilgilerini alır. Bu bilgiler finansal tavsiye niteliği taşımaz. Geliştirici, bakiye verilerinin doğruluğunu, eksiksizliğini veya güncelliğini garanti etmez. Bu uygulamada görüntülenen bilgilere dayanarak yalnızca finansal kararlar almayın.
        """.trimIndent()
    ),
    LegalSection(
        "Garanti Yok",
        """
        Uygulama, açık veya zımni herhangi bir türde garanti olmaksızın "olduğu gibi" sağlanmaktadır. Geliştirici, uygulamanın hatasız olduğunu, kesintisiz çalıştığını veya belirli bir amaca uygun olduğunu garanti etmez.
        """.trimIndent()
    ),
    LegalSection(
        "Sorumluluğun Sınırlandırılması",
        """
        Yürürlükteki yasaların izin verdiği azami ölçüde, geliştirici; veri kaybı, kasa erişiminin kaybı veya uygulamanın kullanımından ya da kullanılamamasından kaynaklanan doğrudan, dolaylı, arızi, özel veya sonuçsal zararlar için sorumlu tutulamaz.
        """.trimIndent()
    ),
    LegalSection(
        "Koşulların Güncellenmesi",
        """
        Bu koşullar herhangi bir zamanda güncellenebilir. Yayınlanan bir güncellemeden sonra uygulamayı kullanmaya devam etmek, yeni koşulların kabul edildiği anlamına gelir.
        """.trimIndent()
    ),
    LegalSection(
        "Son güncelleme",
        "Mayıs 2026 (rev. 2)"
    )
)

private fun legalTR() = listOf(
    LegalSection(
        "Yasal Bildirim ve Sorumluluk Reddi",
        ""
    ),
    LegalSection(
        "Yazılım Olduğu Gibi Sağlanmaktadır",
        """
        KeyArmor, satılabilirlik, belirli bir amaca uygunluk veya ihlal etmeme garantileri dahil ancak bunlarla sınırlı olmamak üzere, açık veya zımni herhangi bir türde garanti olmaksızın "olduğu gibi" dağıtılmaktadır.
        """.trimIndent()
    ),
    LegalSection(
        "Sorumluluğun Sınırlandırılması",
        """
        KeyArmor geliştiricisi hiçbir koşulda aşağıdakilerden sorumlu tutulamaz:

        • Uygulamada depolanan verilerin kaybolması veya bozulması
        • Ana şifrenin unutulması veya kaybolması ya da yedek anahtarın kaybolması nedeniyle kasaya erişimin kesilmesi
        • Android OS bileşenlerindeki güvenlik açıkları veya arızalar
        • Google Drive hizmetlerindeki arızalar, kesintiler veya değişiklikler
        • Uygulamanın kullanımından veya kullanılamamasından kaynaklanan doğrudan, dolaylı, arızi, özel veya sonuçsal zararlar
        • Üçüncü tarafların kullanıcı verilerine yetkisiz erişiminden kaynaklanan zararlar
        • Panik PIN özelliğinin kasıtlı veya yanlışlıkla etkinleştirilmesinden kaynaklanan veri kaybı
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive Veri İletimi",
        """
Kullanıcı Google Drive senkronizasyonunu (yedekler) etkinleştirdiğinde veya Dijital Miras / Aile Planı'nı (Google Firebase) kullandığında, veriler Google'ın Hizmet Şartları kapsamında zaten şifrelenmiş olarak Google altyapısına iletilir. Her durumda veriler cihazdan opak, şifreli bir blok olarak ayrılır. Geliştiricinin Google altyapısına erişimi veya denetimi yoktur ve iletilen içeriği okuyamaz.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore ve Donanım Bağımlılığı",
        """
        KeyArmor'ın şifreleme güvenliği kısmen kullanıcının cihazının Android Keystore uygulamasına bağlıdır. Bu uygulama üreticiye, yonga setine ve Android sürümüne göre farklılık gösterir. Geliştirici, üçüncü taraf donanım uygulamalarının güvenliğini kontrol edemez veya garanti edemez. Cihazın işletim sisteminin güncel tutulması şiddetle tavsiye edilir.
        """.trimIndent()
    ),
    LegalSection(
        "Otomatik Doldurma Servisi Sınırlamaları",
        """
        KeyArmor Otomatik Doldurma servisi tüm uygulamalar veya tarayıcılarla doğru çalışmayabilir. Özellikle Google Chrome, Android'in standart otomatik doldurma çerçevesini desteklememektedir — bu, tüm üçüncü taraf parola yöneticilerini eşit şekilde etkileyen bir Chrome sınırlamasıdır ve geliştiricinin kontrolü dışındadır. Geliştirici, otomatik doldurma servisinin belirli bir uygulamayla çalışacağını garanti etmez.
        """.trimIndent()
    ),
    LegalSection(
        "Anlık Kripto Bakiye Feragatnamesi",
        """
        Anlık bakiye verileri, genel üçüncü taraf blok zinciri düğümlerinden ve API'lerden alınır. Geliştirici bu düğümleri işletmez veya kontrol etmez. Bakiye bilgileri yalnızca bilgilendirme amacıyla sağlanmaktadır ve gecikmeli, hatalı veya kullanılamaz olabilir. Bu bilgiler finansal veya yatırım tavsiyesi niteliği taşımaz. Geliştirici, uygulamada görüntülenen bakiye verilerine dayanarak alınan kararlardan sorumlu tutulamaz.
        """.trimIndent()
    ),
    LegalSection(
        "P2P Paylaşımı",
        """
        P2P giriş paylaşımı özelliği, kullanıcılar arasında doğrudan şifreli kasa girişi alışverişini kolaylaştırır. Geliştirici, kullanıcılar arasında paylaşılan girişlerin içeriğinden, alıcının kimliğinden veya güvenilirliğinden ya da kasa verilerinin üçüncü taraflarla paylaşılmasından doğan sonuçlardan sorumlu tutulamaz.
        """.trimIndent()
    ),
    LegalSection(
        "Üçüncü Taraf Kütüphaneler",
        """
        KeyArmor, yayın zamanında güvenli kabul edilen üçüncü taraf açık kaynaklı kütüphaneler kullanır. Geliştirici, üçüncü taraf bileşenlerden kaynaklanan arızalar, güvenlik açıkları veya beklenmedik davranışlar için sorumluluk kabul etmez.
        """.trimIndent()
    ),
    LegalSection(
        "Uygulanacak Hukuk",
        """
        Bu yasal bildirim, Arjantin Cumhuriyeti'nde yürürlükteki yasalar tarafından yönetilmektedir. Uygulamanın kullanımından doğan herhangi bir anlaşmazlık, o yargı bölgesinin yetkili mahkemelerine sunulacaktır.
        """.trimIndent()
    ),
    LegalSection(
        "Son güncelleme",
        "Mayıs 2026 (rev. 2)"
    )
)

private fun licenseTR() = listOf(
    LegalSection("Source Available Lisansı", ""),
    LegalSection("Kaynak Kodu Yalnızca Güvenlik Denetimi Amacıyla Yayınlanmıştır", """
KeyArmor'ın kaynak kodu, güvenlik araştırmacılarının ve kullanıcıların güvenlik uygulamalarını inceleyip denetleyebilmesi amacıyla kamuya açıktır. Bunun ötesinde herhangi bir hak verilmez — tüm haklar telif hakkı sahibine aittir.
    """.trimIndent()),
    LegalSection("Yapabilecekleriniz", """
• Güvenlik araştırması ve kişisel denetim amacıyla kaynak kodunu okuma ve inceleme
• Güvenlik açıklarını resmi kanallar aracılığıyla KeyArmor ekibine bildirme
    """.trimIndent()),
    LegalSection("Yapamayacaklarınız", """
Telif hakkı sahibinin açık ve önceden yazılı izni olmadan şunları yapamazsınız:

• Yazılımı resmi dağıtımı dışında kullanmak veya çalıştırmak
• Kaynak kodunu okuma dışında herhangi bir amaçla kopyalamak, çoğaltmak veya ondan alıntı yapmak
• Onu değiştirmek, uyarlamak, çevirmek veya ona dayalı türev çalışmalar oluşturmak
• Onu üçüncü taraflara dağıtmak, yayımlamak, alt lisanslamak, satmak veya başka bir şekilde erişilebilir kılmak
• Kaynak kodunu başka bir ürün, hizmet veya proje için temel olarak kullanmak
• KeyArmor adını, logosunu veya ilişkili herhangi bir ticari markayı kullanmak
    """.trimIndent()),
    LegalSection("Kaynak Kodu", """
KeyArmor'ın kaynak kodu kamuya açık olarak mevcuttur.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Garanti Yoktur", """
Kaynak kodu, açık veya zımni herhangi bir garanti olmaksızın "olduğu gibi" sunulur. Telif hakkı sahibi, belirli bir amaca uygunluğu konusunda hiçbir beyanda bulunmaz.
    """.trimIndent()),
    LegalSection("Telif Hakkı", "© 2026 KeyArmor. Tüm hakları saklıdır. Source Available.")
)

private fun opensourceTR() = listOf(
    LegalSection(
        "Açık Kaynak Bildirimi",
        ""
    ),
    LegalSection(
        "Kullanılan Kütüphaneler",
        """
        KeyArmor aşağıdaki açık kaynaklı kütüphaneleri kullanmaktadır. Aksi belirtilmedikçe Apache 2.0 lisansı altında dağıtılmaktadır.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Sürüm: BOM 2026.09.00 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Sürüm: 1.2.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Sürüm: 2.8.5 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Sürüm: 2.60.1 | Lisans: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Sürüm: 1.11.0 | Lisans: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Sürüm: 1.1.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Sürüm: 2.10.2 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Sürüm: 2.11.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Sürüm: 2.12.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Sürüm: 22.0.0 | Lisans: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Sürüm: 2.9.2 | Lisans: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Sürüm: v3-rev20240123-2.0.0 | Lisans: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Sürüm: 2.2.0 | Lisans: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Sürüm: 4.3.0 | Lisans: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Sürüm: 0.37.3 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Sürüm: BOM 2026.09.00 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Sürüm: 1.19.1 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Sürüm: 1.13.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Sürüm: 1.3.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Sürüm: 1.3.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Sürüm: 1.8.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Sürüm: 1.4.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Sürüm: 2.4.21 | Lisans: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Sürüm: 1.2.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Sürüm: 1.4.1 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Sürüm: 1.6.0 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Sürüm: 1.6.2 | Lisans: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Sürüm: BOM 35.1.0 | Lisans: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Sürüm: 2.15.0 | Lisans: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Sürüm: 3.0.1 | Lisans: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Sürüm: 2.0.27.0 | Lisans: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Sürüm: 1.2.1 | Lisans: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Sürüm: 1.86 | Lisans: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Sürüm: 10.29.0 | Lisans: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Sürüm: 5.2.0 | Lisans: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Sürüm: 5.19.1 | Lisans: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Sürüm: 1.2.1 | Lisans: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Sürüm: 16.0.1 | Lisans: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Sürüm: 16.0.1 | Lisans: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Sürüm: 2026-07-09 | Lisans: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Apache 2.0 Lisansının Tam Metni",
        """
        Apache 2.0 lisansının tam metni şu adreste mevcuttur:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataTR() = listOf(
    LegalSection("Hesabı ve Verileri Sil", ""),
    LegalSection("KeyArmor’un sizin hakkınızda sakladığı bilgiler", """
Kasalarınız (şifreler, notlar, kartlar, cüzdanlar, ekler ve şifre geçmişi) hiçbir zaman sunucularımızda depolanmaz. Bu veriler, cihazınızda şifrelenmiş olarak saklanır ve yedeklemeyi etkinleştirirseniz kendi Google Drive’ınızda da depolanır — bizlerin okuyabileceği veya silebileceği bir yerde değil; çünkü veriler cihazınızdan ayrılmadan önce şifrelenir.

Google hesabınızla ilişkili ve sunucularımıza (Firebase) ulaşan tek veri, isteğe bağlı bir özellik için o hesabın bağlanmasıyla ilgilidir — Google Drive yedekleme, Dijital Miras veya Aile Planı. Bu özelliklerden herhangi biri için bir Google hesabını bağladığınızda, Miras veya Aile Planı'nı yapılandırmadan önce bile orada asgari düzeyde bir kayıt oluşturulur (e-postanızla anahtarlanmış bir push bildirimi jetonu). KeyArmor'a hiç Google hesabı bağlamadıysanız, sunucularımızda silinecek size ait hiçbir bilgi bulunmaz.
    """.trimIndent()),
    LegalSection("Uygulamadan silme", """
Uygulama içindeki Ayarlar → Tüm verilerimi sil bölümünden istediğiniz zaman tüm verilerinizi silebilirsiniz. KeyArmor’a (Drive yedeklemesi, Dijital Miras veya Aile Planı için) hiçbir zaman bir Google hesabı bağlamadıysanız, bu işlem cihazınızdaki kasayı anında ve kalıcı olarak siler.

Bir hesap bağladıysanız, uygulama sizden Google ile oturum açmanızı ister; böylece aynı işlemle, ilişkili sunucu tarafındaki kayıtları (Firebase) ve KeyArmor'un Google Drive'ınıza kaydettiği yedekleme ile Time Machine kopyalarını silebilir. Yalnızca oturum açamamanız durumunda uygulama yalnızca yerel verileri siler — ve sunucu tarafındaki kayıtlarınızın, aşağıdaki talebi kullanarak ayrı olarak kaldırılana kadar kalacağını önceden size bildirir.
    """.trimIndent()),
    LegalSection("Neler silinmez?", """
Verilerinizi silmek, uygulamanın dışında bulunan öğeleri kaldırmaz:

• Kendi başınıza dışa aktardığınız dosyalar (kaydettiğiniz veya paylaştığınız CSV, PDF veya .kab dosyaları). Bunlar size aittir ve koyduğunuz yerde saklanır; bu nedenle onları o yerden silmeniz gerekir.
• KeyArmor'un önceki bir kurulumunun İndirilenler klasörünüzde bıraktığı yedek kopyalar. Android, yeni bir kurulumun bu dosyalara erişmesine artık izin vermez; bunları İndirilenler klasörünüzden silin.
• Aboneliğiniz. Aboneliğiniz Google Play tarafından yönetilir, bu nedenle verilerinizi silmek aboneliğinizi iptal etmez; aboneliğinizi Google Play → Abonelikler bölümünden iptal edin.
    """.trimIndent()),
    LegalSection("Hesap verilerinizin silinmesini talep etme", """
KeyArmor'a bir Google hesabı bağladıysanız ancak yukarıda belirtilen uygulama içi silme işlemini kullanmak için oturum açamıyorsanız (örneğin, uygulama artık cihazınızda yüklü değilse), aynı Google hesabından bize e-posta göndererek ilişkili sunucu tarafındaki verilerin silinmesini talep edin.

E-posta adresi: support@thormakk.dev

Silme taleplerini 30 gün içinde işleme alıyoruz ve işlem tamamlandığında e-posta yoluyla onay veriyoruz. Bu talebi yapmak için uygulamanın cihazınızda yüklü olması gerekmez.
    """.trimIndent())
)
