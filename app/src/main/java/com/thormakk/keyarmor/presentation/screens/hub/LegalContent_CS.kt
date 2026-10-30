package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentCS(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyCS()
    "terms"      -> termsCS()
    "legal"      -> legalCS()
    "license"    -> licenseCS()
    "opensource" -> opensourceCS()
    "delete"     -> deleteDataCS()
    else         -> listOf(LegalSection("Dokument nenalezen", ""))
}

private fun privacyCS() = listOf(
    LegalSection(
        "Zásady ochrany osobních údajů",
        ""
    ),
    LegalSection(
        "Shromažďování dat",
        """
        KeyArmor neshromažďuje, nepřenáší ani nesdílí žádné osobní údaje s externími servery. Veškeré informace, které do aplikace zadáte, zůstávají výhradně na vašem zařízení, pokud výslovně nepovolíte synchronizaci s Google Drive.

        KeyArmor neshromažďuje vaši e-mailovou adresu ani žádné přihlašovací údaje k účtu pro základní funkce aplikace (trezor, automatické vyplňování, bezpečnostní audit, monitorování úniků) — ty fungují zcela bez jakéhokoli účtu. Pokud aktivujete Digitální dědictví nebo Rodinný plán, tyto funkce vyžadují přihlášení přes Google: vaše e-mailová adresa Google se pak uloží v Google Firebase, aby vás bylo možné identifikovat a spárovat s dědici nebo členy rodiny, které určíte, a aby vám mohly být zaslány příslušné e-maily s oznámeními prostřednictvím našeho poskytovatele e-mailů (Brevo). Tento e-mail se používá výhradně pro tyto dvě funkce a nikdy se neprodává ani nesdílí s nikým jiným. Mimo Digitální dědictví a Rodinný plán je jediným osobním identifikátorem volitelné jméno nebo přezdívka, které můžete nastavit při konfiguraci; jsou uloženy výhradně na vašem zařízení v zašifrované podobě.
        """.trimIndent()
    ),
    LegalSection(
        "Místní úložiště",
        """
        Veškerá vaše data (hesla, passkeys, poznámky, kreditní karty, bankovní účty, kryptopeněženky, přílohy a historie hesel) jsou uložena šifrovaně pomocí AES-256-GCM s klíčem odvozeným z vašeho hlavního hesla pomocí Argon2id, který nikdy neopustí vaše zařízení. Pokud zapnete biometrické odemykání, kopie tohoto klíče je navíc chráněna klíčem z Android Keystore vašeho zařízení. Žádná data neopustí vaše zařízení bez vašeho výslovného souhlasu.
        """.trimIndent()
    ),
    LegalSection(
        "Hlavní heslo",
        """
        Vaše hlavní heslo není nikdy uloženo v prostém textu ani v žádné obnovitelné podobě. Slouží výhradně k odvození šifrovacího klíče trezoru prostřednictvím Argon2id. Pokud hlavní heslo ztratíte, vaše data nelze obnovit.
        """.trimIndent()
    ),
    LegalSection(
        "Biometrie",
        """
        Biometrické ověřování v KeyArmoru je zcela spravováno operačním systémem Android prostřednictvím rozhraní BiometricPrompt API. KeyArmor v žádném okamžiku biometrické údaje nezpřístupňuje, neukládá ani nezpracovává.

        Biometrie se v aplikaci používá ve dvou kontextech:

        • Odemknutí trezoru: pro ověření přístupu do aplikace při spuštění, dostupné ve všech plánech.
        • Pokročilá biometrická ochrana (Premium): volitelná bariéra, která vyžaduje biometrické ověření před zobrazením citlivých polí v konkrétních oddílech trezoru (hesla, kreditní karty, vlastní pole). Konfigurovatelné v Nastavení — podporuje režim per-přístup nebo per-relace s přizpůsobitelným časovým limitem. Platí stejné záruky ochrany soukromí: KeyArmor biometrické údaje neukládá ani nezpracovává.
        """.trimIndent()
    ),
    LegalSection(
        "Synchronizace s Google Drive",
        """
Synchronizace s Google Drive je zcela volitelná. Když ji zapnete, KeyArmor:

• Žádá jediné minimální oprávnění (DRIVE_APPDATA), aby ukládal vaše vlastní šifrované zálohy a body obnovení Time Machine do soukromé složky vyhrazené aplikaci ve vašem Google Drive. Tato složka je skrytá — nemůže ji vidět ani upravit žádná jiná aplikace, ani vy při procházení Drive.
• Před nahráním trezoru jej zašifruje pomocí 12slovného záložního klíče. Klíč se vytvoří ve vašem zařízení a máte ho pouze vy: ani Google, ani vývojář nemohou soubor přečíst ani klíč obnovit.
• Pokud si soubor PDF se záložním klíčem uložíte do služby Google Docs, na Disk nebo do jiné aplikace, provedete to sami prostřednictvím obrazovky sdílení v systému Android; pro danou aplikaci platí její vlastní zásady ochrany osobních údajů.
• Pokud klíč změníte, můžete si předchozí klíče ponechat, zašifrované ve svém telefonu; soubor PDF se sadou je může obsahovat. KeyArmor je neodesílá na žádný server.
• Nikdy nepřistupuje k žádnému jinému souboru na Drive, nikdy nevytváří viditelné složky a nikdy nesdílí soubory.
• Volitelně uchovává druhou místní kopii zálohy ve složce Downloads/KeyArmor/ vašeho zařízení, která zařízení nikdy neopustí.
• Synchronizace na vyžádání je dostupná ve všech plánech (spouští se ručně). Automatická denní synchronizace je dostupná v plánu Premium.

Digitální dědictví a Rodinný plán využívají Google Firebase, nikoli váš Google Drive. Firebase koordinuje souhlasy, pozvánky a oznámení. Trezor dědictví je na vašem zařízení šifrován od konce ke konci pomocí veřejného klíče dědice a výsledný šifrovaný blok se nahrává do soukromého úložiště Firebase Cloud Storage vývojáře pomocí podepsaných odkazů s krátkou platností (15 minut). Server tento blok ukládá, ale nikdy neobdrží klíč dědice, takže jej nemůže přečíst.

Google účet můžete kdykoli odpojit z obrazovky synchronizace.
    """.trimIndent()
    ),
    LegalSection(
        "Přílohy",
        """
        Funkce příloh vám umožňuje k záznamům v trezoru připojit soubory (až 5 na jeden záznam, každý o velikosti maximálně 5 MB). Všechny přílohy jsou před uložením do interního úložiště zařízení zašifrovány pomocí algoritmu AES-256-GCM. Pokud povolíte synchronizaci s Google Drive, budou vaše přílohy a fotografie dokumentů zahrnuty do zálohy, kterou nahrajete, a budou zašifrovány vaším záložním klíčem. Nikdy nejsou odesílány do žádné jiné služby.
        """.trimIndent()
    ),
    LegalSection(
        "Historie hesel",
        """
        KeyArmor uchovává místní historii předchozích hesel pro každou položku. Tato historie je uložena šifrovaně v zařízení a nikdy není přenášena na žádný externí server.
        """.trimIndent()
    ),
    LegalSection(
        "Interní oznámení",
        """
        KeyArmor generuje interní oznámení, která vás upozorní na důležité bezpečnostní události:

        • Upozornění na vypršení platnosti: odesíláno, když se jedno nebo více hesel blíží datu vypršení platnosti.
        • Vypršená hesla: odesíláno, když jedno nebo více hesel již překročilo datum vypršení platnosti.
        • Upozornění na narušení: odesíláno, když jsou vaše hesla zjištěna v known datových únicích.

        Tyto tři typy oznámení se generují zcela na vašem zařízení a nikdy nejsou přenášeny na externí servery. Pokud používáte Digitální dědictví nebo Rodinný plán, budete také dostávat oznámení o těchto funkcích (dědic přijme pozvání, člen se přidá do rodiny atd.); tato oznámení vytváří Google Firebase a doručují se prostřednictvím Firebase Cloud Messaging, protože závisí na událostech vyvolaných jiným účtem. Všechna oznámení, bez ohledu na svůj původ, se automaticky odstraní po 30 dnech. Jakékoli oznámení můžete smazat přejetím prstem do strany nebo vymazat vše najednou na obrazovce Oznámení. K odesílání push oznámení je vyžadováno systémové oprávnění.
        """.trimIndent()
    ),
    LegalSection(
        "Bezpečnostní audit",
        """
        Funkce bezpečnostního auditu analyzuje vaše hesla místně na zařízení. Kontrola běžných hesel se provádí porovnáváním s předdefinovaným seznamem uloženým přímo v aplikaci, bez dotazů na externí služby.
        """.trimIndent()
    ),
    LegalSection(
        "Monitorování úniků (HIBP)",
        """
        KeyArmor může kontrolovat, zda se vaše hesla vyskytla ve známých únicích dat pomocí služby Have I Been Pwned (HIBP), provozované bezpečnostním výzkumníkem Troyem Huntem.

        Kontrola využívá model k-anonymity: do API HIBP je odesláno pouze prvních 5 znaků SHA-1 hashe vašeho hesla. Celý hash není nikdy přenášen. Porovnávání se provádí lokálně na vašem zařízení. Žádná uživatelská jména, e-mailové adresy ani přidružení k účtům nejsou nikdy odesílána. Odpovědi HIBP jsou doplněny falešnými hashi, takže ani analýza síťového provozu neodhalí kontrolované heslo.
        """.trimIndent()
    ),
    LegalSection(
        "Služba automatického vyplňování",
        """
        KeyArmor obsahuje volitelnou službu automatického vyplňování systému Android. Při povolení v systémovém nastavení čte název balíčku aplikace nebo URL adresu webu aktuálně aktivní přihlašovací obrazovky, aby navrhl odpovídající přihlašovací údaje z vašeho trezoru. Toto porovnávání se provádí výhradně na zařízení. Žádné názvy aplikací, URL adresy webů ani přihlašovací údaje nejsou přenášeny na žádný externí server. Služba automatického vyplňování nečte obsah jiných aplikací nad rámec toho, co je nezbytně nutné k identifikaci kontextu přihlášení.
        """.trimIndent()
    ),
    LegalSection(
        "Živý zůstatek kryptopeněženky",
        """
        Když uložíte záznam kryptopeněženky a povolíte sledování živého zůstatku, KeyArmor dotazuje veřejné blockchainové uzly nebo API pro načtení zůstatku spojeného s adresou peněženky, kterou jste zadali. Přenáší se pouze adresa peněženky — žádné soukromé klíče, seed fráze ani osobní údaje nejsou odesílány. Tyto dotazy jdou přímo do veřejné infrastruktury; KeyArmor neprovozuje žádný zprostředkující server.
        """.trimIndent()
    ),
    LegalSection(
        "Sdílení záznamů P2P",
        """
        KeyArmor umožňuje sdílet jednotlivé záznamy trezoru přímo s dalšími uživateli KeyArmor prostřednictvím QR kódu nebo šifrovaného textu. Přenos je end-to-end šifrovaný a peer-to-peer — žádný server ani zprostředkovatel není zapojen. Jste výhradně zodpovědní za výběr osob, se kterými záznamy sdílíte, a za bezpečnost kanálu použitého k přenosu šifrovaného textu (pokud QR není použito přímo).
        """.trimIndent()
    ),
    LegalSection(
        "Knihovny třetích stran",
        """
        KeyArmor používá open-source knihovny (viz Oznámení o open source). Žádná z těchto knihoven neobsahuje telemetrii, analytiku ani mechanismy sběru dat v konfiguraci používané touto aplikací.

        KeyArmor se také pro konkrétní funkce spoléhá na dvě služby třetích stran: RevenueCat, která ověřuje stav vašeho předplatného Premium a přijímá pouze informace související s nákupem (nikdy váš trezor, hlavní heslo ani obnovovací klíč); a Brevo, která odesílá transakční e-maily vyžadované Digitálním dědictvím a Rodinným plánem (pozvánky, potvrzení souhlasu, oznámení) a za tímto účelem přijímá e-mailovou adresu osob zapojených do těchto funkcí. Žádná z těchto služeb se nepoužívá pro reklamu ani behaviorální analytiku.
        """.trimIndent()
    ),
    LegalSection(
        "Widget domovské obrazovky",
        """
        KeyArmor obsahuje volitelný widget pro domovskou obrazovku Androidu, který zobrazuje celkový počet položek uložených ve vašem trezoru. Tento widget přistupuje pouze k místním datům zařízení. Nepřenáší žádné informace na externí servery. Počet položek je souhrnný údaj, který neodhaluje obsah jednotlivých položek. Sami se rozhodnete, zda widget umístíte na domovskou obrazovku.
        """.trimIndent()
    ),
    LegalSection(
        "Oprávnění",
        """
        KeyArmor požaduje pouze oprávnění nezbytná pro svůj provoz:

        • Biometrie (volitelné): pro odemykání otiskem prstu.
        • Internet (volitelné): používá se pouze pro funkce, které sami povolíte — synchronizace s Google Drive, Digitální dědictví a Rodinný plán (přes Google Firebase), dotazy na živý zůstatek kryptoměn, kontrola úniků přes Have I Been Pwned a ověřování předplatného (Google Play Billing / RevenueCat).
        • Přístup k souborům (volitelné): pro import/export trezoru a přidávání příloh, pouze když to sami iniciujete.
        • Fotoaparát (volitelné): pro skenování QR kódů při nastavování dvoufaktorového ověřování (2FA/TOTP) nebo při přijímání sdílených položek. Aktivuje se pouze na výslovnou žádost uživatele.
        • Služba automatického vyplňování (volitelné): aktivuje se ručně v systémovém nastavení Androidu. Slouží výhradně k navrhování přihlašovacích údajů na přihlašovacích obrazovkách jiných aplikací.
        • Mikrofon (volitelné): pouze pro hlasové diktování v Zabezpečených poznámkách. Rozpoznávání řeči probíhá ve vašem zařízení; KeyArmor zvuk nenahrává, neukládá ani neodesílá na žádný server.
        • Kalendář, pouze čtení (volitelné): používá ho Bezpečný cestovní režim k hledání cest ve vašich událostech. Události se zpracovávají ve vašem zařízení a nikdy se neodesílají.
        • Oznámení a přesné budíky (volitelné): pro bezpečnostní upozornění a připomínky, které si naplánujete. Událost spuštění zařízení slouží k jejich opětovnému naplánování po restartu.
        • Stav sítě (volitelné): pouze ke zjištění, zda používáte mobilní data nebo Wi-Fi, aby velké zálohy na Google Drive čekaly na Wi-Fi. Žádné informace o vaší síti se neshromažďují ani neodesílají.

        KeyArmor nepožaduje přístup ke kontaktům, poloze ani k jiným osobním údajům, které nejsou uvedeny výše.
        """.trimIndent()
    ),
    LegalSection("Další služby, které přijímají data", """
Kromě výše popsaných služeb KeyArmor používá následující, a to jen pro funkce, které je potřebují:

• Ověření integrity aplikace (Firebase App Check / Google Play Integrity): Google poskytuje potvrzení o vašem zařízení, aby náš server přijímal pouze požadavky od pravé aplikace.
• RevenueCat: kromě údajů o nákupu přijímá identifikátor vašeho účtu Google (ne váš e-mail) jako ID uživatele, aby propojil vaše předplatné napříč zařízeními.
• CoinGecko: když používáte kryptoceny v reálném čase, jsou u CoinGecko dotazovány symboly z vašeho portfolia a CoinGecko může vidět vaši IP adresu. Vidět ji mohou i výše zmíněné blockchainové průzkumníky a uzly.
• Google Fonts: aplikace stahuje písma Inter a JetBrains Mono přes služby Google Play, takže Google může při jejich vyžádání vidět vaši IP adresu.
• Důvěryhodný čas služeb Google Play: slouží k odhalení manipulace s hodinami zařízení. Neodesílají se žádné osobní údaje.
• Rozpoznávání textu (OCR) v dokumentech probíhá ve vašem zařízení pomocí ML Kit. Žádný obrázek se neodesílá na server.
• Ko-fi: pouze pokud otevřete odkaz pro dary; pak platí jejich vlastní zásady ochrany osobních údajů.

Naše servery mohou dočasně zaznamenávat vaši IP adresu, aby omezily zneužití.
    """.trimIndent()),
    LegalSection(
        "Passkeys (přístupové klíče)",
        """
        KeyArmor může fungovat jako poskytovatel passkeys systému Android (Credential Manager), ale pouze pokud si to sami zvolíte v nastavení systému. Při vytvoření nebo použití passkey KeyArmor přečte doménu webu nebo aplikace, která o ni žádá, aby passkey k tomuto webu přiřadil a podepsal výzvu, kterou web odesílá. Soukromý klíč se vygeneruje ve vašem zařízení a uloží se šifrovaně spolu se zbytkem vašeho trezoru; neodesílá se na žádný server ani žádné třetí straně. Vaše zařízení opouští pouze v rámci záloh, které sami vytvoříte (například šifrovaná záloha na Google Drive). Pro podpis pomocí passkey vás KeyArmor požádá o ověření totožnosti otiskem prstu nebo hlavním heslem. Web, na kterém se přihlašujete, obdrží pouze veřejný klíč a podpis, jako u jakékoli passkey.
        """.trimIndent()
    ),
    LegalSection(
        "Smazání a uchovávání dat",
        """
        Váš trezor je uchováván ve vašem zařízení (a na vašem Google Drive, pokud zapnete zálohování), dokud jej sami nesmažete. Na našich serverech jsou pouze koordinační data: token push oznámení identifikovaný vaším e-mailem a, pokud používáte Digitální dědictví nebo Rodinný plán, záznamy o pozvánkách a souhlasech a šifrovaný blok dědického trezoru. Uchovávají se, dokud tyto funkce používáte, nebo dokud nepožádáte o jejich smazání. Můžete je smazat v části Nastavení → Smazat všechna moje data, nebo napsáním na support@thormakk.dev z vašeho účtu Google; tyto žádosti vyřizujeme do 30 dnů. Úplné podrobnosti najdete na stránce „Smazání účtu a dat“ na našem webu.
        """.trimIndent()
    ),
    LegalSection(
        "Nezletilé osoby",
        """
        KeyArmor není určen osobám mladším 13 let (ani mladším, než je minimální věk stanovený právem vaší země pro udělení digitálního souhlasu) a vědomě neshromažďujeme údaje nezletilých. Pokud se domníváte, že nám nezletilá osoba poskytla údaje, napište nám na support@thormakk.dev a my je smažeme.
        """.trimIndent()
    ),
    LegalSection(
        "Kontakt",
        "V případě jakýchkoliv dotazů týkajících se těchto zásad nás můžete kontaktovat na support@thormakk.dev"
    ),
    LegalSection(
        "Poslední aktualizace",
        "Říjen 2026 (rev. 8)"
    )
)

private fun termsCS() = listOf(
    LegalSection(
        "Podmínky použití",
        ""
    ),
    LegalSection(
        "Souhlas",
        """
        Instalací nebo použitím KeyArmor přijímáte tyto podmínky v jejich úplnosti. Pokud s jakoukoli jejich částí nesouhlasíte, aplikaci nepoužívejte.
        """.trimIndent()
    ),
    LegalSection(
        "Povolené použití",
        """
        KeyArmor je navržen pro osobní použití jako správce hesel a zabezpečený trezor. Použití pro nezákonné aktivity, ukládání informací usnadňujících protiprávní jednání nebo pokusy o zpětnou analýzu aplikace jsou zakázány.
        """.trimIndent()
    ),
    LegalSection(
        "Odpovědnost uživatele",
        """
        Nesete výhradní odpovědnost za:

        • Zabezpečení vašeho hlavního hesla. Pokud bude ztraceno, neexistuje žádný mechanismus obnovení a vaše data budou trvale nepřístupná.
        • 12slovný záložní klíč (pokud tuto funkci používáte). Pokud o něj přijdete a zároveň ztratíte telefon, zálohy na Disku nebude možné otevřít, a to ani samotným vývojářem.
        • Udržování aktuálních záloh vašeho trezoru prostřednictvím funkce exportu nebo synchronizace Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Funkčnost synchronizace",
        """
        Synchronizace s Google Drive je pomocnou záložní funkcí. Nenahrazuje osvědčené postupy osobní bezpečnosti. Vývojář nezaručuje nepřetržitou dostupnost této funkce, protože závisí na službách třetích stran (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Prémiová předplatná",
        """
        KeyArmor nabízí volitelné placené plány (Měsíční, Roční), které odemykají další funkce. Předplatná jsou zpracovávána výhradně prostřednictvím Google Play Billing. Zakoupením předplatného souhlasíte také s Podmínkami služby Google Play.

        • Měsíční a roční plány jsou automaticky obnovovaná předplatná. Kdykoli je můžete zrušit prostřednictvím Google Play; zrušení nabývá účinnosti na konci aktuálního fakturačního období.
        • Vrácení peněz se řídí standardními zásadami vrácení peněz Google Play. Vývojář nezpracovává vrácení peněz samostatně.
        • Prémiové funkce se mohou v průběhu času vyvíjet. Vývojář si vyhrazuje právo přidávat, upravovat nebo ukončovat konkrétní prémiové funkce s přiměřeným upozorněním.
        """.trimIndent()
    ),
    LegalSection(
        "Cestovní bezpečný režim",
        """
        Cestovní bezpečný režim umožňuje označit konkrétní záznamy jako cestovně bezpečné a skrýt všechny ostatní záznamy, zatímco je režim aktivní. Nesete výhradní odpovědnost za určení, které záznamy označit jako cestovně bezpečné, a za důsledky aktivace nebo deaktivace tohoto režimu. Vývojář nepřebírá žádnou odpovědnost za záznamy náhodně ponechané viditelné nebo skryté.
        """.trimIndent()
    ),
    LegalSection(
        "Nouzový PIN",
        """
        Funkce nouzového PIN spouští nevratnou akci (vymazání nebo skrytí citlivých dat trezoru) při zadání určeného PIN kódu. Po aktivaci mohou být skrytá nebo smazaná data neobnovitelná v závislosti na vaší konfiguraci. Přijímáte plnou odpovědnost za konfiguraci této funkce a za jakoukoli ztrátu dat vyplývající z jejího úmyslného nebo náhodného aktivování.
        """.trimIndent()
    ),
    LegalSection(
        "Informace o kryptografickém zůstatku",
        """
        Funkce živého zůstatku načítá informace o zůstatku z veřejné blockchainové infrastruktury pouze pro informační účely. Tyto informace nepředstavují finanční poradenství. Vývojář nezaručuje přesnost, úplnost ani aktuálnost dat o zůstatku. Nečiňte finanční rozhodnutí výhradně na základě informací zobrazených v této aplikaci.
        """.trimIndent()
    ),
    LegalSection(
        "Žádné záruky",
        """
        Aplikace je poskytována „tak jak je", bez jakýchkoli záruk, výslovných ani předpokládaných. Vývojář nezaručuje, že aplikace je bez chyb, funguje bez přerušení nebo je vhodná pro konkrétní účel.
        """.trimIndent()
    ),
    LegalSection(
        "Omezení odpovědnosti",
        """
        V maximálním rozsahu povoleném platným právem vývojář neodpovídá za ztrátu dat, ztrátu přístupu k trezoru ani za žádné přímé, nepřímé, náhodné, zvláštní nebo následné škody vzniklé v důsledku použití aplikace nebo nemožnosti ji používat.
        """.trimIndent()
    ),
    LegalSection(
        "Aktualizace podmínek",
        """
        Tyto podmínky mohou být kdykoli aktualizovány. Pokračující používání aplikace po zveřejněné aktualizaci představuje souhlas s novými podmínkami.
        """.trimIndent()
    ),
    LegalSection(
        "Poslední aktualizace",
        "Květen 2026 (rev. 2)"
    )
)

private fun legalCS() = listOf(
    LegalSection(
        "Právní upozornění a vyloučení odpovědnosti",
        ""
    ),
    LegalSection(
        "Software poskytován tak jak je",
        """
        KeyArmor je distribuován „tak jak je", bez jakýchkoli záruk, výslovných ani předpokládaných, včetně, ale nikoli výhradně, záruk prodejnosti, vhodnosti pro konkrétní účel nebo neporušení práv.
        """.trimIndent()
    ),
    LegalSection(
        "Omezení odpovědnosti",
        """
        Vývojář KeyArmor za žádných okolností neodpovídá za:

        • Ztrátu nebo poškození dat uložených v aplikaci
        • Ztráta přístupu k trezoru v důsledku zapomenutí nebo ztráty hlavního hesla či záložního klíče
        • Zranitelnosti nebo selhání komponent operačního systému Android
        • Selhání, přerušení nebo změny ve službách Google Drive
        • Jakékoli přímé, nepřímé, náhodné, zvláštní nebo následné škody vzniklé v důsledku použití aplikace nebo nemožnosti ji používat
        • Jakékoli škody vyplývající z neoprávněného přístupu třetích stran k datům uživatele
        • Ztrátu dat vyplývající z úmyslného nebo náhodného aktivování funkce nouzového PIN
        """.trimIndent()
    ),
    LegalSection(
        "Přenos dat do Google Drive",
        """
Když uživatel zapne synchronizaci s Google Drive (zálohy) nebo použije Digitální dědictví / Rodinný plán (Google Firebase), data se přenášejí již zašifrovaná do infrastruktury Google podle Smluvních podmínek Google. V každém případě data opouštějí zařízení jako neprůhledný zašifrovaný blok. Vývojář nemá přístup k infrastruktuře Google ani ji neovládá a nemůže číst přenášený obsah.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore a závislost na hardwaru",
        """
        Bezpečnost šifrování KeyArmor závisí částečně na implementaci Android Keystore v zařízení uživatele. Tato implementace se liší v závislosti na výrobci, čipové sadě a verzi Androidu. Vývojář nekontroluje ani nezaručuje bezpečnost hardwarových implementací třetích stran. Důrazně doporučujeme udržovat operační systém zařízení aktuální.
        """.trimIndent()
    ),
    LegalSection(
        "Omezení služby automatického vyplňování",
        """
        Služba automatického vyplňování KeyArmor nemusí fungovat správně se všemi aplikacemi nebo prohlížeči. Zejména Google Chrome nepodporuje standardní framework automatického vyplňování systému Android — jedná se o omezení Chrome, které postihuje všechny správce hesel třetích stran stejně a je mimo kontrolu vývojáře. Vývojář nezaručuje, že služba automatického vyplňování bude fungovat s jakoukoli konkrétní aplikací.
        """.trimIndent()
    ),
    LegalSection(
        "Zřeknutí se odpovědnosti za živý kryptografický zůstatek",
        """
        Data o živém zůstatku jsou načítána z veřejných blockchainových uzlů a API třetích stran. Vývojář tyto uzly neprovozuje ani nekontroluje. Informace o zůstatku jsou poskytovány pouze pro informační účely a mohou být zpožděné, nepřesné nebo nedostupné. Tyto informace nepředstavují finanční ani investiční poradenství. Vývojář nepřebírá žádnou odpovědnost za rozhodnutí učiněná na základě dat o zůstatku zobrazených v aplikaci.
        """.trimIndent()
    ),
    LegalSection(
        "P2P sdílení",
        """
        Funkce P2P sdílení záznamů usnadňuje přímou, šifrovanou výměnu záznamů trezoru mezi uživateli. Vývojář nepřebírá žádnou odpovědnost za obsah záznamů sdílených mezi uživateli, za identitu nebo důvěryhodnost příjemce ani za jakékoli důsledky vyplývající ze sdílení dat trezoru s třetími stranami.
        """.trimIndent()
    ),
    LegalSection(
        "Knihovny třetích stran",
        """
        KeyArmor používá open-source knihovny třetích stran, které byly v době zveřejnění považovány za bezpečné. Vývojář nepřebírá žádnou odpovědnost za selhání, zranitelnosti nebo neočekávané chování pocházející z komponent třetích stran.
        """.trimIndent()
    ),
    LegalSection(
        "Rozhodné právo",
        """
        Toto právní upozornění se řídí platnou legislativou Argentinské republiky. Jakýkoli spor vzniklý v důsledku používání aplikace bude předložen příslušným soudům dané jurisdikce.
        """.trimIndent()
    ),
    LegalSection(
        "Poslední aktualizace",
        "Květen 2026 (rev. 2)"
    )
)

private fun licenseCS() = listOf(
    LegalSection("Licence Source Available", ""),
    LegalSection("Zdrojový kód zveřejněný pouze pro účely bezpečnostního auditu", """
Zdrojový kód KeyArmor je veřejně dostupný výhradně proto, aby jej bezpečnostní výzkumníci a uživatelé mohli zkontrolovat a auditovat jeho bezpečnostní postupy. Nejsou udělena žádná další práva — všechna práva jsou vyhrazena držiteli autorských práv.
    """.trimIndent()),
    LegalSection("Co můžete dělat", """
• Číst a zkoumat zdrojový kód pro bezpečnostní výzkum a osobní audit
• Nahlásit bezpečnostní zranitelnosti týmu KeyArmor oficiálními kanály
    """.trimIndent()),
    LegalSection("Co nesmíte dělat", """
Bez výslovného předchozího písemného souhlasu držitele autorských práv nesmíte:

• Používat nebo spouštět software mimo jeho oficiální distribuci
• Kopírovat, reprodukovat nebo extrahovat zdrojový kód za jiným účelem než čtením
• Upravovat, přizpůsobovat, překládat nebo vytvářet odvozená díla na jeho základě
• Distribuovat, zveřejňovat, sublicencovat, prodávat nebo jinak zpřístupňovat zdrojový kód třetím stranám
• Používat zdrojový kód jako základ pro jiný produkt, službu nebo projekt
• Používat název, logo nebo jakoukoli přidruženou ochrannou známku KeyArmor
    """.trimIndent()),
    LegalSection("Zdrojový kód", """
Zdrojový kód KeyArmor je veřejně dostupný. Odkaz na repozitář naleznete na oficiálním kanálu aplikace.
    """.trimIndent()),
    LegalSection("Žádná záruka", """
Zdrojový kód je poskytován „tak jak je“, bez záruky jakéhokoli druhu, výslovné či předpokládané. Držitel autorských práv neposkytuje žádná prohlášení ohledně jeho vhodnosti pro konkrétní účel.
    """.trimIndent()),
    LegalSection("Autorská práva", "© 2026 KeyArmor. Všechna práva vyhrazena. Source Available.")
)

private fun opensourceCS() = listOf(
    LegalSection(
        "Oznámení o open source",
        ""
    ),
    LegalSection(
        "Použité knihovny",
        """
        KeyArmor používá následující open-source knihovny. Pokud není uvedeno jinak, jsou distribuovány pod licencí Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Verze: BOM 2026.09.00
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Version: 1.2.0 | License: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Verze: 2.8.5
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Verze: 2.60.1
        Licence: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Verze: 1.11.0
        Licence: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Verze: 1.1.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Verze: 2.10.2
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Verze: 2.11.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Verze: 2.12.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Verze: 22.0.0
        Licence: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Verze: 2.9.2
        Licence: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Verze: v3-rev20240123-2.0.0
        Licence: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Verze: 2.2.0
        Licence: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Verze: 4.3.0
        Licence: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Verze: 0.37.3
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Verze: BOM 2026.09.00
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Verze: 1.19.1
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Verze: 1.13.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Verze: 1.3.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Verze: 1.3.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Verze: 1.8.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Verze: 1.4.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Verze: 2.4.21
        Licence: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Verze: 1.2.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Verze: 1.4.1
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Verze: 1.6.0
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Verze: 1.6.2
        Licence: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Verze: BOM 35.1.0
        Licence: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Verze: 2.15.0
        Licence: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Verze: 3.0.1
        Licence: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Verze: 2.0.27.0
        Licence: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Verze: 1.2.1
        Licence: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Verze: 1.86
        Licence: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Verze: 10.29.0
        Licence: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Verze: 5.2.0
        Licence: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Verze: 5.19.1
        Licence: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Verze: 1.2.1
        Licence: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Verze: 16.0.1
        Licence: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Verze: 16.0.1
        Licence: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Verze: 2026-07-09
        Licence: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Úplný text licence",
        """
        Úplný text licence Apache 2.0 je dostupný na:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataCS() = listOf(
    LegalSection("Smazat účet a data", ""),
    LegalSection("Jaké údaje o vás společnost KeyArmor uchovává", """
Váš trezor (hesla, poznámky, karty, peněženky, přílohy a historie hesel) se nikdy neukládá na naše servery. Je uložen v zašifrované podobě na vašem zařízení a, pokud zapnete zálohování, také na vašem vlastním Disku Google – nikde tam, kde bychom jej mohli přečíst nebo smazat, protože je zašifrován ještě předtím, než opustí vaše zařízení.

Jediná data spojená s vaším účtem Google, která se dostanou na naše servery (Firebase), souvisejí s propojením tohoto účtu pro volitelné funkce – zálohování na Disku Google, digitální dědictví nebo rodinný tarif. Pouhé propojení účtu Google pro kteroukoli z těchto funkcí tam zaznamená minimální záznam (token pro push notifikace, identifikovaný vaší e-mailovou adresou), a to ještě předtím, než nakonfigurujete funkci „Digitální dědictví“ nebo „Rodinný tarif“. Pokud jste nikdy nepřipojili účet Google k KeyArmor, na našich serverech o vás není nic, co by bylo třeba smazat.
    """.trimIndent()),
    LegalSection("Odstranění z aplikace", """
Všechna svá data můžete kdykoli odstranit v části Nastavení → Smazat všechna moje data v aplikaci. Pokud jste k aplikaci KeyArmor nikdy nepřipojili účet Google (pro zálohování na Disku, digitální dědictví nebo rodinný tarif), dojde tím k okamžitému a trvalému vymazání vašeho trezoru ze zařízení.

Pokud jste účet propojili, aplikace vás požádá o přihlášení přes Google, aby mohla v rámci téže akce vymazat související záznamy na serveru (Firebase) a záložní kopie i kopie z Time Machine, které KeyArmor uložil na vašem Google Disku. Pouze v případě, že se vám nepodaří přihlásit, aplikace přistoupí pouze k vymazání lokálních dat – a předem vás upozorní, že vaše záznamy na serveru zůstanou zachovány, dokud je samostatně neodstraníte pomocí níže uvedeného požadavku.
    """.trimIndent()),
    LegalSection("Co se neodstraní", """
Smazání vašich dat neodstraní to, co se nachází mimo aplikaci:

• Soubory, které jste sami exportovali (soubory CSV, PDF nebo .kab, které jste uložili nebo sdíleli). Patří vám a jsou uloženy tam, kam jste je umístili, takže je musíte smazat právě tam.
• Záložní kopie, které v složce „Stažené soubory“ zanechala předchozí instalace aplikace KeyArmor. Systém Android již nové instalaci nedovolí do nich zasahovat; smažte je ze složky „Stažené soubory“.
• Vaše předplatné. To spravuje služba Google Play, takže smazání vašich dat ho nezruší; zrušte ho v Google Play → Předplatná.
    """.trimIndent()),
    LegalSection("Žádost o vymazání údajů z vašeho účtu", """
Pokud jste k aplikaci KeyArmor připojili účet Google, ale nemůžete se přihlásit, abyste mohli využít výše uvedenou možnost smazání v rámci aplikace (například pokud již nemáte aplikaci nainstalovanou), požádejte o smazání souvisejících dat na straně serveru tím, že nám zašlete e-mail z téhož účtu Google.

Napište na adresu: support@thormakk.dev

Žádosti o smazání vyřizujeme do 30 dnů a po dokončení vám zašleme potvrzení e-mailem. K podání této žádosti nemusíte mít aplikaci nainstalovanou.
    """.trimIndent())
)
