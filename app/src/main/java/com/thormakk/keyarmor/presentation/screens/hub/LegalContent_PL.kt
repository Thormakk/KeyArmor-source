package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentPL(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyPL()
    "terms"      -> termsPL()
    "legal"      -> legalPL()
    "license"    -> licensePL()
    "opensource" -> opensourcePL()
    "delete"     -> deleteDataPL()
    else         -> listOf(LegalSection("Nie znaleziono dokumentu", ""))
}

private fun privacyPL() = listOf(
    LegalSection("Polityka prywatności", ""),
    LegalSection("Zbieranie danych", """
KeyArmor nie zbiera, nie przesyła ani nie udostępnia żadnych danych osobowych zewnętrznym serwerom. Wszystkie informacje wprowadzone w aplikacji pozostają wyłącznie na Twoim urządzeniu, chyba że wyraźnie włączysz synchronizację z Google Drive.

KeyArmor nie zbiera Twojego adresu e-mail ani żadnych danych uwierzytelniających konta w przypadku podstawowych funkcji aplikacji (skarbiec, autouzupełnianie, audyt bezpieczeństwa, monitorowanie naruszeń) — działają one w pełni bez żadnego konta. Jeśli włączysz Cyfrowe Dziedzictwo lub Plan Rodzinny, te funkcje wymagają logowania przez Google: Twój adres e-mail konta Google jest wtedy przechowywany w Google Firebase w celu Twojej identyfikacji i dopasowania Cię do wskazanych spadkobierców lub członków rodziny, a także w celu wysyłania odpowiednich powiadomień e-mail za pośrednictwem naszego dostawcy poczty (Brevo). Ten adres e-mail jest wykorzystywany wyłącznie do tych dwóch funkcji i nigdy nie jest sprzedawany ani udostępniany innym podmiotom. Poza Cyfrowym Dziedzictwem i Planem Rodzinnym jedynym osobistym identyfikatorem jest opcjonalne imię lub pseudonim, które możesz ustawić podczas konfiguracji; jest ono przechowywane wyłącznie na Twoim urządzeniu w postaci zaszyfrowanej.
    """.trimIndent()),
    LegalSection("Przechowywanie lokalne", """
Wszystkie Twoje dane (hasła, passkeys, notatki, karty kredytowe, konta bankowe, portfele kryptowalut, załączniki i historia haseł) są przechowywane w postaci zaszyfrowanej przy użyciu AES-256-GCM z kluczem wyprowadzonym z Twojego hasła głównego za pomocą Argon2id, który nigdy nie opuszcza Twojego urządzenia. Jeśli włączysz odblokowywanie biometryczne, kopia tego klucza jest dodatkowo chroniona kluczem z Android Keystore Twojego urządzenia. Żadne dane nie opuszczają Twojego urządzenia bez Twojej wyraźnej zgody.
    """.trimIndent()),
    LegalSection("Hasło główne", """
Twoje hasło główne nigdy nie jest przechowywane w postaci zwykłego tekstu ani w żadnej odwracalnej formie. Służy wyłącznie do wyprowadzenia klucza szyfrowania skarbca za pomocą Argon2id. Jeśli utracisz hasło główne, Twoich danych nie będzie można odzyskać.
    """.trimIndent()),
    LegalSection("Biometria", """
Uwierzytelnianie biometryczne w KeyArmor jest w całości zarządzane przez system operacyjny Android za pośrednictwem API BiometricPrompt. KeyArmor nigdy nie uzyskuje dostępu do danych biometrycznych, nie przechowuje ich ani nie przetwarza.

Biometria jest wykorzystywana w aplikacji w dwóch kontekstach:

• Odblokowanie skarbca: do uwierzytelnienia dostępu do aplikacji przy uruchomieniu, dostępne we wszystkich planach.
• Zaawansowana ochrona biometryczna (Premium): opcjonalna bariera wymagająca weryfikacji biometrycznej przed wyświetleniem wrażliwych pól w określonych sekcjach skarbca (hasła, karty kredytowe, pola niestandardowe). Konfigurowalne w Ustawieniach — obsługuje tryb per-dostęp lub per-sesja z konfigurowalnym limitem czasu. Te same gwarancje prywatności obowiązują: KeyArmor nie przechowuje ani nie przetwarza danych biometrycznych.
    """.trimIndent()),
    LegalSection("Synchronizacja z Google Drive", """
Synchronizacja z Google Drive jest całkowicie opcjonalna. Po włączeniu KeyArmor:

• Żąda jednego minimalnego uprawnienia (DRIVE_APPDATA), aby przechowywać Twoje własne zaszyfrowane kopie zapasowe oraz punkty przywracania Time Machine w prywatnym, dedykowanym aplikacji folderze na Twoim Google Drive. Ten folder jest ukryty — nie może go zobaczyć ani zmienić żadna inna aplikacja, ani Ty podczas przeglądania Drive.
• Przed przesłaniem skarbca szyfruje go za pomocą 12-wyrazowego klucza zapasowego. Klucz ten jest generowany na Twoim urządzeniu i tylko Ty go posiadasz: ani Google, ani twórca aplikacji nie mogą odczytać tego pliku ani odtworzyć klucza.
• Jeśli zapiszesz plik PDF zawierający klucz zapasowy w Google Docs, na Dysku Google lub w innej aplikacji, robisz to samodzielnie za pomocą ekranu udostępniania w systemie Android; aplikacja ta podlega własnej polityce prywatności.
• Jeśli zmienisz klucz, możesz zachować poprzednie, zaszyfrowane na telefonie; plik PDF z zestawem może je zawierać. KeyArmor nie wysyła ich na żaden serwer.
• Nigdy nie uzyskuje dostępu do innych plików Drive, nigdy nie tworzy widocznych folderów i nigdy nie udostępnia plików.
• Opcjonalnie przechowuje drugą lokalną kopię kopii zapasowej w folderze Downloads/KeyArmor/ Twojego urządzenia, która nigdy nie opuszcza urządzenia.
• Kopie zapasowe w Google Drive – zarówno ręczne (na żądanie), jak i automatyczne, wykonywane codziennie – są dostępne w planach Premium i Rodzinnym. W planie bezpłatnym można połączyć swoje konto Google w celu przywrócenia istniejącej kopii zapasowej.

Cyfrowe Dziedzictwo i Plan Rodzinny korzystają z Google Firebase, a nie z Twojego Google Drive. Firebase zarządza zgodami, zaproszeniami i powiadomieniami. Skarbiec spadkowy jest szyfrowany od końca do końca na Twoim urządzeniu za pomocą klucza publicznego spadkobiercy, a powstały zaszyfrowany blok jest przesyłany do prywatnego zasobnika Firebase Cloud Storage dewelopera za pomocą podpisanych linków o krótkim czasie ważności (15 minut). Serwer przechowuje ten blok, ale nigdy nie otrzymuje klucza spadkobiercy, więc nie może go odczytać.

Możesz odłączyć swoje konto Google w dowolnym momencie z ekranu synchronizacji.
    """.trimIndent()),
    LegalSection("Załączniki", """
Funkcja załączników pozwala przypisywać pliki (maksymalnie 5 na każdy wpis, każdy o rozmiarze do 5 MB) do wpisów w sejfie. Wszystkie załączniki są szyfrowane algorytmem AES-256-GCM przed zapisaniem w pamięci wewnętrznej urządzenia. Jeśli włączysz synchronizację z Dyskiem Google, Twoje załączniki i zdjęcia dokumentów zostaną uwzględnione w przesyłanej kopii zapasowej, zaszyfrowanej Twoim kluczem zapasowym. Nigdy nie są one wysyłane do żadnej innej usługi.
    """.trimIndent()),
    LegalSection("Historia haseł", """
KeyArmor przechowuje lokalną historię poprzednich haseł dla każdego wpisu. Ta historia jest przechowywana w postaci zaszyfrowanej na urządzeniu i nigdy nie jest przesyłana do żadnego zewnętrznego serwera.
    """.trimIndent()),
    LegalSection("Powiadomienia w aplikacji", """
KeyArmor generuje wewnętrzne powiadomienia, aby informować Cię o ważnych zdarzeniach bezpieczeństwa:

• Ostrzeżenia o wygaśnięciu: wysyłane, gdy jedno lub więcej haseł zbliża się do daty wygaśnięcia.
• Wygasłe hasła: wysyłane, gdy jedno lub więcej haseł przekroczyło już datę wygaśnięcia.
• Alerty o naruszeniu: wysyłane, gdy Twoje hasła zostają wykryte w znanych wyciekach danych.

Te trzy typy powiadomień są generowane w całości na Twoim urządzeniu i nigdy nie są przesyłane do zewnętrznych serwerów. Jeśli korzystasz z Cyfrowego Dziedzictwa lub Planu Rodzinnego, otrzymasz również powiadomienia dotyczące tych funkcji (spadkobierca akceptujący zaproszenie, dodanie członka rodziny itp.); są one wyzwalane przez Google Firebase i dostarczane za pośrednictwem Firebase Cloud Messaging, ponieważ zależą od zdarzeń inicjowanych przez inne konto. Wszystkie powiadomienia, niezależnie od źródła, są automatycznie usuwane po 30 dniach. Możesz usunąć dowolne powiadomienie, przesuwając je w bok, lub wyczyścić wszystkie naraz na ekranie Powiadomienia. Powiadomienia push wymagają uprawnień systemowych do wysyłania.
    """.trimIndent()),
    LegalSection("Audyt bezpieczeństwa", """
Funkcja audytu bezpieczeństwa analizuje Twoje hasła lokalnie na urządzeniu. Sprawdzanie popularnych haseł odbywa się poprzez porównanie z predefiniowaną listą przechowywaną w samej aplikacji, bez zapytań do zewnętrznych usług.
    """.trimIndent()),
    LegalSection("Monitorowanie naruszeń (HIBP)", """
KeyArmor może sprawdzać, czy Twoje hasła pojawiły się w znanych naruszeniach danych przy użyciu usługi Have I Been Pwned (HIBP), prowadzonej przez eksperta ds. bezpieczeństwa Troy'a Hunta.

Sprawdzanie wykorzystuje model k-anonimowości: do API HIBP wysyłane są tylko pierwsze 5 znaków skrótu SHA-1 Twojego hasła. Pełny skrót nigdy nie jest przesyłany. Dopasowanie jest wykonywane lokalnie na Twoim urządzeniu. Żadne nazwy użytkowników, adresy e-mail ani powiązania z kontami nie są wysyłane. Odpowiedzi HIBP są uzupełniane fałszywymi skrótami, dzięki czemu nawet analiza ruchu sieciowego nie ujawnia sprawdzanego hasła.
    """.trimIndent()),
    LegalSection("Usługa autouzupełniania", """
KeyArmor zawiera opcjonalną usługę autouzupełniania systemu Android. Po włączeniu w ustawieniach systemowych odczytuje nazwę pakietu aplikacji lub adres URL witryny aktualnie aktywnego ekranu logowania, aby zaproponować pasujące dane uwierzytelniające z Twojego skarbca. Dopasowanie jest wykonywane w całości na urządzeniu. Żadne nazwy aplikacji, adresy URL witryn ani dane uwierzytelniające nie są przesyłane do żadnego zewnętrznego serwera. Usługa autouzupełniania nie odczytuje zawartości innych aplikacji poza ściśle niezbędnym minimum do identyfikacji kontekstu logowania.
    """.trimIndent()),
    LegalSection("Saldo portfela kryptowalut na żywo", """
Gdy przechowujesz wpis portfela kryptowalut i włączysz monitorowanie salda na żywo, KeyArmor wysyła zapytania do publicznych węzłów blockchain lub API w celu pobrania salda powiązanego z wprowadzonym adresem portfela. Przesyłany jest tylko adres portfela — żadne klucze prywatne, frazy seed ani dane osobowe nie są wysyłane. Zapytania te trafiają bezpośrednio do publicznej infrastruktury; KeyArmor nie obsługuje żadnego pośredniego serwera.
    """.trimIndent()),
    LegalSection("Udostępnianie wpisów P2P", """
KeyArmor umożliwia udostępnianie poszczególnych wpisów skarbca bezpośrednio innym użytkownikom KeyArmor za pośrednictwem kodu QR lub zaszyfrowanego tekstu. Transfer jest szyfrowany end-to-end i peer-to-peer — żaden serwer ani pośrednik nie jest zaangażowany. Ponosisz wyłączną odpowiedzialność za wybór osób, którym udostępniasz wpisy, oraz za bezpieczeństwo kanału używanego do przesyłania zaszyfrowanego tekstu (jeśli nie używasz QR bezpośrednio).
    """.trimIndent()),
    LegalSection("Biblioteki zewnętrzne", """
KeyArmor korzysta z bibliotek open-source (patrz Informacja o otwartym kodzie źródłowym). Żadna z tych bibliotek nie zawiera mechanizmów telemetrii, analityki ani zbierania danych w konfiguracji używanej przez tę aplikację.

KeyArmor korzysta również z dwóch usług zewnętrznych dla konkretnych funkcji: RevenueCat, która weryfikuje status Twojej subskrypcji Premium i otrzymuje wyłącznie informacje związane z zakupem (nigdy Twój skarbiec, hasło główne ani klucz odzyskiwania); oraz Brevo, która wysyła wiadomości transakcyjne wymagane przez Cyfrowe Dziedzictwo i Plan Rodzinny (zaproszenia, potwierdzenia zgody, powiadomienia) i w tym celu otrzymuje adresy e-mail osób zaangażowanych w te funkcje. Żadna z tych usług nie jest wykorzystywana do reklamy ani analizy zachowań.
    """.trimIndent()),    LegalSection("Widget ekranu głównego", """
KeyArmor zawiera opcjonalny widget na ekran główny Androida, który wyświetla łączną liczbę wpisów przechowywanych w Twoim sejfie. Widget ten uzyskuje dostęp wyłącznie do lokalnych danych urządzenia. Nie przesyła żadnych informacji do zewnętrznych serwerów. Liczba wpisów to zagregowane dane, które nie ujawniają zawartości poszczególnych wpisów. Możesz zdecydować, czy umieścić widget na ekranie głównym.
    """.trimIndent()),

    LegalSection("Uprawnienia", """
KeyArmor żąda tylko uprawnień niezbędnych do jego działania:

• Biometria (opcjonalne): do odblokowania odciskiem palca.
• Internet (opcjonalne): używany wyłącznie dla funkcji, które sam włączasz — synchronizacja z Google Drive, Cyfrowe Dziedzictwo i Plan Rodzinny (za pośrednictwem Google Firebase), zapytania o saldo kryptowalut na żywo, sprawdzanie naruszeń Have I Been Pwned oraz weryfikacja subskrypcji (Google Play Billing / RevenueCat).
• Dostęp do plików (opcjonalne): do importu/eksportu skarbca i dodawania załączników, tylko gdy to zainicjujesz.
• Aparat (opcjonalne): do skanowania kodów QR podczas konfigurowania uwierzytelniania dwuskładnikowego (2FA/TOTP) lub odbierania udostępnionych wpisów. Aktywowany tylko na wyraźne żądanie użytkownika.
• Usługa autouzupełniania (opcjonalne): włączana ręcznie w ustawieniach systemowych Androida. Używana wyłącznie do sugerowania danych uwierzytelniających na ekranach logowania innych aplikacji.
• Mikrofon (opcjonalne): tylko do dyktowania głosowego w Notatkach zabezpieczonych. Rozpoznawanie mowy odbywa się na Twoim urządzeniu; KeyArmor nie nagrywa, nie zapisuje ani nie wysyła dźwięku na żaden serwer.
• Kalendarz, tylko odczyt (opcjonalne): używa go Bezpieczny tryb podróży do wyszukiwania podróży w Twoich wydarzeniach. Wydarzenia są przetwarzane na Twoim urządzeniu i nigdy nie są przesyłane.
• Powiadomienia i dokładne alarmy (opcjonalne): dla alertów bezpieczeństwa i przypomnień, które ustawiasz. Zdarzenie uruchomienia urządzenia służy do ponownego zaplanowania ich po restarcie.
• Stan sieci (opcjonalne): tylko po to, by wiedzieć, czy korzystasz z danych komórkowych czy z Wi-Fi, aby duże kopie zapasowe na Dysku Google czekały na Wi-Fi. Żadne informacje o Twojej sieci nie są gromadzone ani wysyłane.

KeyArmor nie żąda dostępu do kontaktów, lokalizacji ani żadnych innych danych osobowych niewymienionych powyżej.
    """.trimIndent()),
    LegalSection("Inne usługi, które otrzymują dane", """
Oprócz usług opisanych powyżej KeyArmor korzysta z poniższych, tylko w funkcjach, które ich wymagają:

• Weryfikacja integralności aplikacji (Firebase App Check / Google Play Integrity): Google dostarcza poświadczenie Twojego urządzenia, aby nasz serwer akceptował tylko żądania od oryginalnej aplikacji.
• RevenueCat: oprócz informacji o zakupie otrzymuje identyfikator Twojego konta Google (nie adres e-mail) jako ID użytkownika, aby powiązać subskrypcję między urządzeniami.
• CoinGecko: gdy korzystasz z cen kryptowalut na żywo, symbole z Twojego portfela są pobierane z CoinGecko, który może zobaczyć Twój adres IP. Wspomniane wyżej eksploratory i węzły blockchain również mogą go zobaczyć.
• Google Fonts: aplikacja pobiera czcionki Inter i JetBrains Mono przez usługi Google Play, więc Google może zobaczyć Twój adres IP podczas ich pobierania.
• Zaufany czas usług Google Play: służy do wykrywania manipulacji zegarem urządzenia. Nie są wysyłane żadne dane osobowe.
• Rozpoznawanie tekstu (OCR) w dokumentach odbywa się na Twoim urządzeniu przy użyciu ML Kit. Żaden obraz nie jest wysyłany na serwer.
• Ko-fi: tylko jeśli otworzysz link do darowizn; wtedy obowiązuje ich własna polityka prywatności.

Nasze serwery mogą tymczasowo rejestrować Twój adres IP, aby ograniczać nadużycia.
    """.trimIndent()),
    LegalSection("Passkeys (klucze dostępu)", """
KeyArmor może działać jako dostawca passkeys w systemie Android (Credential Manager), ale tylko wtedy, gdy sam tak wybierzesz w ustawieniach systemu. Podczas tworzenia lub użycia passkey KeyArmor odczytuje domenę witryny lub aplikacji, która o nią prosi, aby powiązać passkey z tą witryną i podpisać wyzwanie wysyłane przez witrynę. Klucz prywatny jest generowany na Twoim urządzeniu i zapisywany w postaci zaszyfrowanej razem z resztą Twojego skarbca; nie jest wysyłany do żadnego serwera ani do żadnej osoby trzeciej. Opuszcza Twoje urządzenie wyłącznie w ramach kopii zapasowych, które sam tworzysz (na przykład zaszyfrowanej kopii na Google Drive). Aby podpisać za pomocą passkey, KeyArmor prosi Cię o weryfikację tożsamości odciskiem palca lub hasłem głównym. Witryna, w której się logujesz, otrzymuje wyłącznie klucz publiczny i podpis, jak w przypadku każdego passkey.
    """.trimIndent()),
    LegalSection("Usuwanie i przechowywanie danych", """
Twój skarbiec jest przechowywany na Twoim urządzeniu (oraz na Twoim Google Drive, jeśli włączysz kopię zapasową), dopóki sam go nie usuniesz. Na naszych serwerach znajdują się wyłącznie dane koordynacyjne: token powiadomień push powiązany z Twoim adresem e-mail oraz, jeśli korzystasz z Cyfrowego Dziedzictwa lub Planu Rodzinnego, rejestry zaproszeń i zgód oraz zaszyfrowany blok skarbca dziedzictwa. Są przechowywane, dopóki korzystasz z tych funkcji lub dopóki nie poprosisz o ich usunięcie. Możesz je usunąć w sekcji Ustawienia → Usuń wszystkie moje dane albo pisząc na support@thormakk.dev ze swojego konta Google; takie prośby realizujemy w ciągu 30 dni. Pełne informacje znajdziesz na stronie „Usuwanie konta i danych” na naszej stronie internetowej.
    """.trimIndent()),
    LegalSection("Osoby niepełnoletnie", """
KeyArmor nie jest skierowany do osób poniżej 13. roku życia (ani poniżej minimalnego wieku określonego przez prawo Twojego kraju dla wyrażenia zgody w środowisku cyfrowym) i świadomie nie zbieramy danych osób niepełnoletnich. Jeśli uważasz, że osoba niepełnoletnia przekazała nam dane, napisz do nas na support@thormakk.dev, a my je usuniemy.
    """.trimIndent()),
    LegalSection("Kontakt", "W przypadku jakichkolwiek pytań dotyczących niniejszej polityki możesz napisać na adres support@thormakk.dev"),
    LegalSection("Ostatnia aktualizacja", "Listopad 2026 (wer. 9)")
)

private fun termsPL() = listOf(
    LegalSection("Warunki korzystania", ""),
    LegalSection("Akceptacja warunków", """
Instalując lub używając KeyArmor, akceptujesz niniejsze warunki w całości. Jeśli nie zgadzasz się z jakąkolwiek częścią, nie używaj aplikacji.
    """.trimIndent()),
    LegalSection("Dozwolone użytkowanie", """
KeyArmor jest przeznaczony do osobistego użytku jako menedżer haseł i bezpieczny skarbiec. Zabrania się używania go do działań niezgodnych z prawem, przechowywania informacji ułatwiających działania niezgodne z prawem lub prób odtworzenia kodu aplikacji metodą inżynierii wstecznej.
    """.trimIndent()),
    LegalSection("Odpowiedzialność użytkownika", """
Ponosisz wyłączną odpowiedzialność za:

• Bezpieczeństwo hasła głównego. W przypadku jego utraty nie istnieje mechanizm odzyskiwania, a Twoje dane staną się trwale niedostępne.
• 12-wyrazowy klucz zapasowy (jeśli korzystasz z tej funkcji). Jeśli go zgubisz i jednocześnie zgubisz telefon, kopie zapasowe na Dysku nie będą mogły zostać otwarte – nawet przez twórcę aplikacji.
• Utrzymywanie aktualnych kopii zapasowych skarbca za pomocą funkcji eksportu lub synchronizacji z Drive.
    """.trimIndent()),
    LegalSection("Funkcja synchronizacji", """
Synchronizacja z Google Drive jest pomocniczą funkcją tworzenia kopii zapasowych. Nie zastępuje dobrych osobistych praktyk bezpieczeństwa. Deweloper nie gwarantuje ciągłej dostępności tej funkcji, ponieważ zależy ona od usług zewnętrznych (Google).
    """.trimIndent()),
    LegalSection("Subskrypcje Premium", """
KeyArmor oferuje opcjonalne płatne plany (Miesięczny, Roczny), które odblokowują dodatkowe funkcje. Subskrypcje są przetwarzane wyłącznie przez Google Play Billing. Kupując subskrypcję, zgadzasz się również z Warunkami korzystania z usług Google Play.

• Plany Miesięczny i Roczny są subskrypcjami z automatycznym odnowieniem. Możesz je anulować w dowolnym momencie przez Google Play; anulowanie obowiązuje po zakończeniu bieżącego okresu rozliczeniowego.
• Zwroty podlegają standardowej polityce zwrotów Google Play. Deweloper nie przetwarza zwrotów samodzielnie.
• Funkcje premium mogą ewoluować z czasem. Deweloper zastrzega sobie prawo do dodawania, modyfikowania lub wycofywania określonych funkcji premium z rozsądnym wyprzedzeniem.
    """.trimIndent()),
    LegalSection("Tryb bezpiecznej podróży", """
Tryb bezpiecznej podróży pozwala oznaczać określone wpisy jako bezpieczne dla podróży i ukrywać wszystkie inne wpisy, gdy tryb jest aktywny. Ponosisz wyłączną odpowiedzialność za określenie, które wpisy oznaczyć jako bezpieczne dla podróży, oraz za konsekwencje aktywacji lub dezaktywacji tego trybu. Deweloper nie ponosi odpowiedzialności za wpisy przypadkowo pozostawione widocznymi lub ukrytymi.
    """.trimIndent()),
    LegalSection("PIN awaryjny", """
Funkcja PIN awaryjnego uruchamia nieodwracalne działanie (wyczyszczenie lub ukrycie poufnych danych skarbca) po wprowadzeniu wyznaczonego kodu PIN. Po aktywacji ukryte lub usunięte dane mogą być nieodwracalne w zależności od konfiguracji. Przyjmujesz pełną odpowiedzialność za konfigurację tej funkcji oraz za wszelkie utracone dane wynikające z jej celowej lub przypadkowej aktywacji.
    """.trimIndent()),
    LegalSection("Informacje o saldzie kryptowalut", """
Funkcja salda na żywo pobiera informacje o saldzie z publicznej infrastruktury blockchain wyłącznie w celach informacyjnych. Informacje te nie stanowią porady finansowej. Deweloper nie gwarantuje dokładności, kompletności ani aktualności danych o saldzie. Nie podejmuj decyzji finansowych wyłącznie na podstawie informacji wyświetlanych w tej aplikacji.
    """.trimIndent()),
    LegalSection("Brak gwarancji", """
Aplikacja jest dostarczana „w stanie takim, w jakim jest", bez żadnych wyraźnych ani dorozumianych gwarancji. Deweloper nie gwarantuje, że aplikacja jest wolna od błędów, działa bez przerw ani że nadaje się do określonego celu.
    """.trimIndent()),
    LegalSection("Ograniczenie odpowiedzialności", """
W maksymalnym zakresie dozwolonym przez obowiązujące prawo deweloper nie ponosi odpowiedzialności za utratę danych, utratę dostępu do skarbca ani żadne bezpośrednie, pośrednie, przypadkowe, szczególne lub wynikowe szkody wynikające z użytkowania aplikacji lub niemożności jej użytkowania.

    Jeśli mieszkasz w kraju, w którym przepisy dotyczące ochrony konsumentów przyznają Ci prawa, których nie można zrzec się, niniejsze warunki nie ograniczają tych praw. Postanowienia niniejszych warunków dotyczące gwarancji i odpowiedzialności mają zastosowanie wyłącznie w zakresie dozwolonym przez prawo.
    """.trimIndent()),
    LegalSection("Aktualizacje warunków", """
Niniejsze warunki mogą być aktualizowane w dowolnym momencie. Dalsze korzystanie z aplikacji po opublikowanej aktualizacji stanowi akceptację nowych warunków.
    """.trimIndent()),
    LegalSection("Ostatnia aktualizacja", "Listopad 2026 (wer. 3)")
)

private fun legalPL() = listOf(
    LegalSection("Informacja prawna i zastrzeżenie odpowiedzialności", ""),
    LegalSection("Oprogramowanie dostarczane w stanie takim, w jakim jest", """
KeyArmor jest dystrybuowany „w stanie takim, w jakim jest", bez żadnych wyraźnych ani dorozumianych gwarancji, w tym między innymi gwarancji przydatności handlowej, przydatności do określonego celu lub nienaruszania praw.
    """.trimIndent()),
    LegalSection("Ograniczenie odpowiedzialności", """
Deweloper KeyArmor nie ponosi odpowiedzialności w żadnych okolicznościach za:

• Utratę lub uszkodzenie danych przechowywanych w aplikacji
• Utrata dostępu do skarbca z powodu zapomnienia lub zgubienia hasła głównego lub klucza zapasowego
• Luki w zabezpieczeniach lub awarie komponentów systemu Android OS
• Awarie, przerwy lub zmiany w usługach Google Drive
• Wszelkie bezpośrednie, pośrednie, przypadkowe, szczególne lub wynikowe szkody wynikające z użytkowania aplikacji lub niemożności jej użytkowania
• Wszelkie szkody wynikające z nieautoryzowanego dostępu stron trzecich do danych użytkownika
• Utratę danych wynikającą z celowej lub przypadkowej aktywacji funkcji PIN awaryjnego
    """.trimIndent()),
    LegalSection("Transmisja danych do Google Drive", """
Gdy użytkownik włączy synchronizację z Google Drive (kopie zapasowe) lub użyje Cyfrowego Dziedzictwa / Planu Rodzinnego (Google Firebase), dane są przesyłane już zaszyfrowane do infrastruktury Google zgodnie z Warunkami korzystania z usług Google. W każdym przypadku dane opuszczają urządzenie jako nieprzejrzysty zaszyfrowany blok. Deweloper nie ma dostępu do infrastruktury Google ani jej nie kontroluje i nie może odczytać przesyłanej treści.
    """.trimIndent()),
    LegalSection("Android Keystore i zależność sprzętowa", """
Bezpieczeństwo szyfrowania KeyArmor zależy częściowo od implementacji Android Keystore na urządzeniu użytkownika. Ta implementacja różni się w zależności od producenta, układu chipów i wersji Androida. Deweloper nie kontroluje ani nie gwarantuje bezpieczeństwa implementacji sprzętu zewnętrznego. Zdecydowanie zaleca się utrzymywanie aktualności systemu operacyjnego urządzenia.
    """.trimIndent()),
    LegalSection("Ograniczenia usługi autouzupełniania", """
Usługa autouzupełniania KeyArmor może nie działać poprawnie ze wszystkimi aplikacjami lub przeglądarkami. W szczególności Google Chrome nie obsługuje standardowego frameworku autouzupełniania systemu Android — jest to ograniczenie Chrome, które dotyczy w równym stopniu wszystkich menedżerów haseł innych firm i jest poza kontrolą dewelopera. Deweloper nie gwarantuje, że usługa autouzupełniania będzie działać z jakąkolwiek konkretną aplikacją.
    """.trimIndent()),
    LegalSection("Zastrzeżenie dotyczące salda kryptowalut", """
Dane o saldzie na żywo są pobierane z publicznych węzłów blockchain i API stron trzecich. Deweloper nie obsługuje ani nie kontroluje tych węzłów. Informacje o saldzie są dostarczane wyłącznie w celach informacyjnych i mogą być opóźnione, niedokładne lub niedostępne. Informacje te nie stanowią porady finansowej ani inwestycyjnej. Deweloper nie ponosi odpowiedzialności za decyzje podjęte na podstawie danych o saldzie wyświetlanych w aplikacji.
    """.trimIndent()),
    LegalSection("Udostępnianie P2P", """
Funkcja udostępniania wpisów P2P ułatwia bezpośrednią, zaszyfrowaną wymianę wpisów skarbca między użytkownikami. Deweloper nie ponosi odpowiedzialności za treść udostępnianych między użytkownikami wpisów, za tożsamość lub wiarygodność odbiorcy ani za jakiekolwiek konsekwencje wynikające z udostępniania danych skarbca osobom trzecim.
    """.trimIndent()),
    LegalSection("Biblioteki zewnętrzne", """
KeyArmor korzysta z zewnętrznych bibliotek open-source uznanych za bezpieczne w chwili publikacji. Deweloper nie ponosi odpowiedzialności za awarie, luki w zabezpieczeniach ani nieoczekiwane zachowanie wynikające z komponentów zewnętrznych.
    """.trimIndent()),
    LegalSection("Prawo właściwe", """
Niniejsza informacja prawna podlega prawu obowiązującemu w Republice Argentyńskiej. Wszelkie spory wynikające z korzystania z aplikacji będą rozstrzygane przez właściwe sądy tej jurysdykcji.

    Jeśli mieszkasz w kraju, w którym przepisy dotyczące ochrony konsumentów przyznają Ci prawa, których nie można zrzec się, niniejsza informacja nie ogranicza tych praw. Postanowienia niniejszej informacji dotyczące gwarancji, odpowiedzialności, prawa właściwego i sądów mają zastosowanie wyłącznie w zakresie dozwolonym przez prawo.
    """.trimIndent()),
    LegalSection("Ostatnia aktualizacja", "Listopad 2026 (wer. 3)")
)

private fun licensePL() = listOf(
    LegalSection("Licencja Source Available", ""),
    LegalSection("Kod źródłowy opublikowany wyłącznie w celach audytu bezpieczeństwa", """
Kod źródłowy KeyArmor jest publicznie dostępny wyłącznie po to, aby badacze bezpieczeństwa i użytkownicy mogli sprawdzić i zaudytować jego praktyki bezpieczeństwa. Nie są udzielane żadne inne prawa — wszelkie prawa są zastrzeżone przez właściciela praw autorskich.
    """.trimIndent()),
    LegalSection("Co możesz robić", """
• Czytać i sprawdzać kod źródłowy w celach badań bezpieczeństwa i osobistego audytu
• Zgłaszać luki bezpieczeństwa zespołowi KeyArmor oficjalnymi kanałami
    """.trimIndent()),
    LegalSection("Czego nie możesz robić", """
Bez wyraźnej uprzedniej pisemnej zgody właściciela praw autorskich nie możesz:

• Używać ani uruchamiać oprogramowania poza jego oficjalną dystrybucją
• Kopiować, powielać ani wyodrębniać kodu źródłowego w celu innym niż czytanie
• Modyfikować, adaptować, tłumaczyć ani tworzyć na jego podstawie dzieł pochodnych
• Dystrybuować, publikować, udzielać dalszych licencji, sprzedawać ani w inny sposób udostępniać go osobom trzecim
• Używać kodu źródłowego jako podstawy dla innego produktu, usługi lub projektu
• Używać nazwy, logo ani żadnego skojarzonego znaku towarowego KeyArmor
    """.trimIndent()),
    LegalSection("Kod źródłowy", """
Kod źródłowy KeyArmor jest publicznie dostępny.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Brak gwarancji", """
Kod źródłowy jest dostarczany "tak jak jest", bez jakiejkolwiek gwarancji, wyraźnej lub dorozumianej. Właściciel praw autorskich nie składa żadnych oświadczeń dotyczących jego przydatności do określonego celu.
    """.trimIndent()),
    LegalSection("Prawa autorskie", "© 2026 KeyArmor. Wszelkie prawa zastrzeżone. Source Available.")
)

private fun opensourcePL() = listOf(
    LegalSection("Informacja o otwartym kodzie źródłowym", ""),
    LegalSection("Używane biblioteki", """
KeyArmor korzysta z następujących bibliotek open-source. O ile nie zaznaczono inaczej, są one dystrybuowane na licencji Apache 2.0.
    """.trimIndent()),
    LegalSection("Jetpack Compose", """
Wersja: BOM 2026.09.00 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose
    """.trimIndent()),
    LegalSection("Jetpack Glance (AppWidget)", """
Versión: 1.2.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/glance
    """.trimIndent()),
    LegalSection("Room Database", """
Wersja: 2.8.5 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/data-storage/room
    """.trimIndent()),
    LegalSection("Hilt (Dagger)", """
Wersja: 2.60.1 | Licencja: Apache 2.0
Copyright © The Dagger Authors
https://dagger.dev/hilt/
    """.trimIndent()),
    LegalSection("Kotlin Coroutines", """
Wersja: 1.11.0 | Licencja: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/Kotlin/kotlinx.coroutines
    """.trimIndent()),
    LegalSection("AndroidX Biometric", """
Wersja: 1.1.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/biometric
    """.trimIndent()),
    LegalSection("AndroidX Navigation", """
Wersja: 2.10.2 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/guide/navigation
    """.trimIndent()),
    LegalSection("AndroidX Lifecycle / ViewModel", """
Wersja: 2.11.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/lifecycle
    """.trimIndent()),
    LegalSection("AndroidX WorkManager", """
Wersja: 2.12.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/topic/libraries/architecture/workmanager
    """.trimIndent()),
    LegalSection("Google Play Services Auth", """
Wersja: 22.0.0 | Licencja: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/guides/setup
    """.trimIndent()),
    LegalSection("Google API Client Android", """
Wersja: 2.9.2 | Licencja: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-api-java-client
    """.trimIndent()),
    LegalSection("Google Drive API v3", """
Wersja: v3-rev20240123-2.0.0 | Licencja: Apache 2.0
Copyright © Google LLC
https://developers.google.com/drive/api/v3/about-sdk
    """.trimIndent()),
    LegalSection("Google HTTP Client GSON", """
Wersja: 2.2.0 | Licencja: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-http-java-client
    """.trimIndent()),
    LegalSection("ZXing Android Embedded", """
Wersja: 4.3.0 | Licencja: Apache 2.0
Copyright © ZXing authors, journeyapps
https://github.com/journeyapps/zxing-android-embedded
    """.trimIndent()),
    LegalSection("Accompanist Permissions", """
Wersja: 0.37.3 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://google.github.io/accompanist/permissions/
    """.trimIndent()),
    LegalSection("Compose Material Icons Extended", """
Wersja: BOM 2026.09.00 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose/graphics/images/material
    """.trimIndent()),
    LegalSection("AndroidX Core KTX", """
Wersja: 1.19.1 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Activity Compose", """
Wersja: 1.13.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/activity
    """.trimIndent()),
    LegalSection("AndroidX Autofill", """
Wersja: 1.3.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/autofill
    """.trimIndent()),
    LegalSection("AndroidX DataStore", """
Wersja: 1.3.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/datastore
    """.trimIndent()),
    LegalSection("AndroidX AppCompat", """
Wersja: 1.8.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/appcompat
    """.trimIndent()),
    LegalSection("AndroidX Hilt (Navigation, Work)", """
Wersja: 1.4.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/hilt
    """.trimIndent()),
    LegalSection("Kotlin Standard Library", """
Wersja: 2.4.21 | Licencja: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/JetBrains/kotlin
    """.trimIndent()),
    LegalSection("AndroidX Core Splashscreen", """
Wersja: 1.2.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Profileinstaller", """
Wersja: 1.4.1 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/profileinstaller
    """.trimIndent()),
    LegalSection("AndroidX Credential Manager", """
Wersja: 1.6.0 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/credentials
    """.trimIndent()),
    LegalSection("CameraX", """
Wersja: 1.6.2 | Licencja: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/camerax
    """.trimIndent()),
    LegalSection("Firebase Android SDK (Messaging, Auth, App Check)", """
Wersja: BOM 35.1.0 | Licencja: Apache 2.0
Copyright © Google LLC
https://github.com/firebase/firebase-android-sdk
    """.trimIndent()),
    LegalSection("Joda-Time", """
Wersja: 2.15.0 | Licencja: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-time/
    """.trimIndent()),
    LegalSection("Joda-Convert", """
Wersja: 3.0.1 | Licencja: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-convert/
    """.trimIndent()),
    LegalSection("PDFBox-Android", """
Wersja: 2.0.27.0 | Licencja: Apache 2.0
Copyright © Tom Roush
https://github.com/TomRoush/PdfBox-Android
    """.trimIndent()),
    LegalSection("RichEditor Compose", """
Wersja: 1.2.1 | Licencja: MIT
Copyright © Mohamed Rejeb
https://github.com/MohamedRejeb/compose-rich-editor
    """.trimIndent()),
    LegalSection("Bouncy Castle Provider", """
Wersja: 1.86 | Licencja: Bouncy Castle License (MIT-style)
Copyright © The Legion of the Bouncy Castle Inc.
https://www.bouncycastle.org/about/license/
    """.trimIndent()),
    LegalSection("RevenueCat Purchases Android SDK", """
Wersja: 10.29.0 | Licencja: MIT
Copyright © RevenueCat, Inc.
https://github.com/RevenueCat/purchases-android
    """.trimIndent()),
    LegalSection("LazySodium for Android", """
Wersja: 5.2.0 | Licencja: MPL-2.0
Copyright © Terl Tech Ltd
https://github.com/terl/lazysodium-android
    """.trimIndent()),
    LegalSection("JNA (Java Native Access)", """
Wersja: 5.19.1 | Licencja: Apache 2.0 / LGPL 2.1 (dual-licensed)
Copyright © The JNA Project Contributors
https://github.com/java-native-access/jna
    """.trimIndent()),
    LegalSection("Google Identity Services (GoogleId)", """
Wersja: 1.2.1 | Licencja: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/identity/android-credential-manager
    """.trimIndent()),
    LegalSection("Play Services Time", """
Wersja: 16.0.1 | Licencja: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
    """.trimIndent()),
    LegalSection("ML Kit Text Recognition", """
Wersja: 16.0.1 | Licencja: ML Kit Terms
Copyright © Google LLC
https://developers.google.com/ml-kit/terms
    """.trimIndent()),
    LegalSection("Public Suffix List", """
Wersja: 2026-07-09 | Licencja: MPL-2.0
Copyright © Mozilla Foundation and contributors
https://publicsuffix.org
    """.trimIndent()),
    LegalSection("Pełny tekst Apache 2.0", """
Pełny tekst licencji Apache 2.0 jest dostępny pod adresem:
https://www.apache.org/licenses/LICENSE-2.0
MIT: https://opensource.org/license/mit
MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
    """.trimIndent())
)

private fun deleteDataPL() = listOf(
    LegalSection("Usuń konto i dane", ""),
    LegalSection("Jakie dane na Twój temat przechowuje KeyArmor", """
Twoja skrytka (hasła, notatki, karty, portfele, załączniki i historia haseł) nigdy nie jest przechowywana na naszych serwerach. Jest ona przechowywana w postaci zaszyfrowanej na Twoim urządzeniu, a jeśli włączysz tworzenie kopii zapasowych — również na Twoim własnym Dysku Google. Nie znajduje się w miejscu, z którego moglibyśmy ją odczytać lub usunąć, ponieważ jest szyfrowana, zanim opuści Twoje urządzenie.

Jedyne dane powiązane z Twoim kontem Google, które trafiają na nasze serwery (Firebase), są związane z połączeniem tego konta w celu korzystania z opcjonalnych funkcji — kopii zapasowej na Dysku Google, funkcji „Dziedziczenie cyfrowe” lub „Plan rodzinny”. Samo połączenie konta Google w celu skorzystania z którejkolwiek z tych funkcji powoduje zarejestrowanie tam minimalnego wpisu (tokenu powiadomień push, powiązanego z Twoim adresem e-mail), nawet przed skonfigurowaniem funkcji „Dziedziczenie cyfrowe” lub „Plan rodzinny”. Jeśli nigdy nie połączyłeś konta Google z KeyArmor, na naszych serwerach nie ma żadnych danych dotyczących Ciebie, które można by usunąć.
    """.trimIndent()),
    LegalSection("Usuwanie z aplikacji", """
W dowolnym momencie możesz usunąć wszystkie swoje dane z Ustawienia → Usuń wszystkie moje dane w aplikacji. Jeśli nigdy nie połączyłeś konta Google z KeyArmor (w celu tworzenia kopii zapasowych na Dysku, korzystania z funkcji „Cyfrowe dziedziczenie” lub planu rodzinnego), spowoduje to natychmiastowe i trwałe usunięcie Twojego sejfu z urządzenia.

Jeśli jednak połączyłeś konto, aplikacja poprosi Cię o zalogowanie się za pomocą Google, aby w ramach tej samej operacji usunąć powiązane zapisy po stronie serwera (Firebase) oraz kopie zapasowe i kopie z Time Machine, które KeyArmor zapisał na Twoim Dysku Google. Tylko w przypadku, gdy nie będziesz w stanie się zalogować, aplikacja ograniczy się do usunięcia wyłącznie danych lokalnych — i uprzedzi Cię, że zapisy po stronie serwera pozostaną, dopóki nie zostaną usunięte osobno za pomocą poniższego żądania.
    """.trimIndent()),
    LegalSection("Co nie zostaje usunięte", """
Usunięcie danych nie powoduje usunięcia elementów znajdujących się poza aplikacją:

• Pliki wyeksportowane samodzielnie (pliki CSV, PDF lub .kab, które zapisałeś lub udostępniłeś). Należą one do Ciebie i są przechowywane w miejscu, w którym je umieściłeś, więc musisz je usunąć właśnie tam.
• Kopie zapasowe pozostawione w folderze „Pobrane” przez poprzednią instalację aplikacji KeyArmor. System Android nie pozwala już nowej instalacji na dostęp do tych plików; usuń je z folderu „Pobrane”.
• Twoja subskrypcja. Jest ona zarządzana przez Google Play, więc usunięcie danych nie powoduje jej anulowania; anuluj ją w Google Play → Subskrypcje.
    """.trimIndent()),
    LegalSection("Wniosek o usunięcie danych z konta", """
Jeśli połączyłeś konto Google z aplikacją KeyArmor, ale nie możesz się zalogować, aby skorzystać z powyższej funkcji usuwania danych w aplikacji (na przykład nie masz już zainstalowanej aplikacji), poproś o usunięcie powiązanych danych przechowywanych na serwerze, wysyłając do nas wiadomość e-mail z tego samego konta Google.

Napisz na adres: support@thormakk.dev

Prośby o usunięcie danych rozpatrujemy w ciągu 30 dni, a po zakończeniu procesu wysyłamy potwierdzenie e-mailem. Do złożenia tej prośby nie jest wymagana zainstalowana aplikacja.
    """.trimIndent())
)
