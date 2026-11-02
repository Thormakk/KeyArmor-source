package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentSK(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacySK()
    "terms"      -> termsSK()
    "legal"      -> legalSK()
    "license"    -> licenseSK()
    "opensource" -> opensourceSK()
    "delete"     -> deleteDataSK()
    else         -> listOf(LegalSection("Dokument sa nenašiel", ""))
}

private fun privacySK() = listOf(
    LegalSection(
        "Zásady ochrany osobných údajov",
        ""
    ),
    LegalSection(
        "Zhromažďovanie údajov",
        """
        KeyArmor nezhromažďuje, neprenáša ani nezdieľa žiadne osobné údaje s externými servermi. Všetky informácie, ktoré zadáte do aplikácie, zostávajú výlučne na vašom zariadení, pokiaľ výslovne nepovolíte synchronizáciu s Google Drive.

        KeyArmor nezhromažďuje vašu e-mailovú adresu ani žiadne prihlasovacie údaje účtu pre základné funkcie aplikácie (trezor, automatické vypĺňanie, bezpečnostný audit, monitorovanie únikov) — tie fungujú úplne bez akéhokoľvek účtu. Ak povolíte Digitálne dedičstvo alebo Rodinný plán, tieto funkcie vyžadujú prihlásenie cez Google: vaša e-mailová adresa Google sa potom ukladá do Google Firebase na vašu identifikáciu a priradenie k dedičom alebo členom rodiny, ktorých určíte, a na odosielanie príslušných e-mailových oznámení prostredníctvom nášho poskytovateľa e-mailu (Brevo). Táto e-mailová adresa sa používa výlučne pre tieto dve funkcie a nikdy sa nepredáva ani nezdieľa s nikým iným. Mimo Digitálneho dedičstva a Rodinného plánu je jediným osobným identifikátorom voliteľné meno alebo prezývka, ktoré môžete nastaviť pri konfigurácii; je uložené výlučne na vašom zariadení v zašifrovanej forme.
        """.trimIndent()
    ),
    LegalSection(
        "Lokálne úložisko",
        """
        Všetky vaše údaje (heslá, passkeys, poznámky, kreditné karty, bankové účty, kryptopeňaženky, prílohy a história hesiel) sú uložené zašifrovane pomocou AES-256-GCM s kľúčom odvodeným z vášho hlavného hesla pomocou Argon2id, ktorý nikdy neopustí vaše zariadenie. Ak zapnete biometrické odomykanie, kópia tohto kľúča je navyše chránená kľúčom z Android Keystore vášho zariadenia. Žiadne údaje neopustia vaše zariadenie bez vášho výslovného súhlasu.
        """.trimIndent()
    ),
    LegalSection(
        "Hlavné heslo",
        """
        Vaše hlavné heslo nie je nikdy uložené v čistom texte ani v žiadnej obnoviteľnej forme. Slúži výlučne na odvodenie šifrovacieho kľúča trezoru prostredníctvom Argon2id. Ak stratíte hlavné heslo, vaše údaje nie je možné obnoviť.
        """.trimIndent()
    ),
    LegalSection(
        "Biometria",
        """
        Biometrické overovanie v KeyArmor je úplne spravované operačným systémom Android prostredníctvom rozhrania BiometricPrompt API. KeyArmor v žiadnom čase biometrické údaje nezpristupňuje, neukladá ani nespracúva.

        Biometria sa v aplikácii používa v dvoch kontextoch:

        • Odomknutie trezoru: na overenie prístupu do aplikácie pri spustení, dostupné vo všetkých plánoch.
        • Pokročilá biometrická ochrana (Premium): voliteľná bariéra, ktorá vyžaduje biometrické overenie pred zobrazením citlivých polí v konkrétnych oddieloch trezoru (heslá, kreditné karty, vlastné polia). Konfigurovateľné v Nastaveniach — podporuje režim per-prístup alebo per-relácia s prispôsobiteľným časovým limitom. Platia rovnaké záruky ochrany súkromia: KeyArmor biometrické údaje neukladá ani nespracúva.
        """.trimIndent()
    ),
    LegalSection(
        "Synchronizácia s Google Drive",
        """
Synchronizácia s Google Drive je úplne voliteľná. Po zapnutí KeyArmor:

• Žiada jediné minimálne oprávnenie (DRIVE_APPDATA) na ukladanie vašich vlastných šifrovaných záloh a bodov obnovenia Time Machine do súkromného, aplikácii vyhradeného priečinka vo vašom Google Drive. Tento priečinok je skrytý — nevidí ho ani ho nemôže zmeniť žiadna iná aplikácia, ani vy pri prehliadaní Drive.
• Pred odoslaním trezoru ho zašifruje pomocou 12-slovného záložného kľúča. Kľúč sa vytvorí vo vašom zariadení a máte ho len vy: ani spoločnosť Google, ani vývojár nemôžu tento súbor prečítať ani kľúč obnoviť.
• Ak si PDF súbor so záložným kľúčom uložíte do služby Google Docs, na Disk alebo do inej aplikácie, urobíte to sami prostredníctvom obrazovky zdieľania v systéme Android; táto aplikácia sa riadi vlastnými zásadami ochrany osobných údajov.
• Ak kľúč zmeníte, môžete si predchádzajúce ponechať, zašifrované vo svojom telefóne; súbor PDF so sadou ich môže obsahovať. KeyArmor ich neodosiela na žiadny server.
• Nikdy nepristupuje k žiadnemu inému súboru na Drive, nikdy nevytvára viditeľné priečinky a nikdy nezdieľa súbory.
• Voliteľne uchováva druhú lokálnu kópiu zálohy v priečinku Downloads/KeyArmor/ vášho zariadenia, ktorá zariadenie nikdy neopustí.
• Synchronizácia na požiadanie je dostupná vo všetkých plánoch (spúšťa sa ručne). Automatická denná synchronizácia je dostupná v pláne Premium.

Digitálne dedičstvo a Rodinný plán využívajú Google Firebase, nie váš Google Drive. Firebase koordinuje súhlasy, pozvánky a oznámenia. Trezor dedičstva je na vašom zariadení šifrovaný od konca do konca pomocou verejného kľúča dediča a výsledný šifrovaný blok sa nahraje do súkromného úložiska Firebase Cloud Storage vývojára prostredníctvom podpísaných odkazov s krátkou platnosťou (15 minút). Server tento blok uchováva, ale nikdy nedostane kľúč dediča, takže ho nemôže prečítať.

Účet Google môžete kedykoľvek odpojiť z obrazovky synchronizácie.
    """.trimIndent()
    ),
    LegalSection(
        "Prílohy",
        """
        Funkcia príloh vám umožňuje priradiť k záznamom v trezore súbory (až 5 na jeden záznam, každý s veľkosťou maximálne 5 MB). Všetky prílohy sú pred uložením do vnútorného úložiska zariadenia zašifrované pomocou algoritmu AES-256-GCM. Ak povolíte synchronizáciu s Google Drive, vaše prílohy a fotografie dokumentov budú zahrnuté do zálohy, ktorú nahráte, a budú zašifrované vaším záložným kľúčom. Nikdy sa neposielajú do žiadnej inej služby.
        """.trimIndent()
    ),
    LegalSection(
        "História hesiel",
        """
        KeyArmor uchováva lokálnu históriu predchádzajúcich hesiel pre každú položku. Táto história je uložená šifrovane v zariadení a nikdy nie je prenášaná na žiadny externý server.
        """.trimIndent()
    ),
    LegalSection(
        "Interné oznámenia",
        """
        KeyArmor generuje interné oznámenia, ktoré vás upozornia na dôležité bezpečnostné udalosti:

        • Upozornenia na vypršanie platnosti: odoslané, keď sa jedno alebo viac hesiel blíži dátumu vypršania platnosti.
        • Vypršané heslá: odoslané, keď jedno alebo viac hesiel už prekročilo dátum vypršania platnosti.
        • Upozornenia na narušenie: odoslané, keď sú vaše heslá zistené v known únikoch dát.

        Tieto tri typy oznámení sa generujú výlučne na vašom zariadení a nikdy nie sú prenášané na externé servery. Ak používate Digitálne dedičstvo alebo Rodinný plán, budete tiež dostávať oznámenia o týchto funkciách (dedič prijímajúci pozvánku, pridanie člena rodiny atď.); tie sú vyvolané Google Firebase a doručované prostredníctvom Firebase Cloud Messaging, keďže závisia od udalostí iniciovaných iným účtom. Všetky oznámenia, bez ohľadu na pôvod, sú automaticky odstránené po 30 dňoch. Akékoľvek oznámenie môžete odstrániť potiahnutím nabok alebo vymazať všetky naraz na obrazovke Oznámenia. Na odosielanie push oznámení je potrebné systémové oprávnenie.
        """.trimIndent()
    ),
    LegalSection(
        "Bezpečnostný audit",
        """
        Funkcia bezpečnostného auditu analyzuje vaše heslá lokálne na zariadení. Kontrola bežných hesiel sa vykonáva porovnávaním s preddefinovaným zoznamom uloženým priamo v aplikácii, bez dopytov na externé služby.
        """.trimIndent()
    ),
    LegalSection(
        "Monitorovanie únikov (HIBP)",
        """
        KeyArmor môže kontrolovať, či sa vaše heslá objavili v známych únikoch údajov pomocou služby Have I Been Pwned (HIBP), ktorú prevádzkuje bezpečnostný výskumník Troy Hunt.

        Kontrola využíva model k-anonymity: do API HIBP sa posiela iba prvých 5 znakov SHA-1 hashu vášho hesla. Celý hash nie je nikdy prenášaný. Porovnávanie sa vykonáva lokálne na vašom zariadení. Žiadne používateľské mená, e-mailové adresy ani priradenia k účtom nie sú nikdy odosielané. Odpovede HIBP sú doplnené falošnými hashmi, takže ani analýza sieťovej prevádzky neodhalí kontrolované heslo.
        """.trimIndent()
    ),
    LegalSection(
        "Služba automatického dopĺňania",
        """
        KeyArmor obsahuje volitelnú službu automatického dopĺňania systému Android. Po povolení v systémových nastaveniach číta názov balíka aplikácie alebo URL adresu webu aktuálne aktívnej prihlasovacej obrazovky, aby navrhla zodpovedajúce prihlasovacie údaje z vášho trezoru. Toto porovnávanie sa vykonáva výlučne na zariadení. Žiadne názvy aplikácií, URL adresy webov ani prihlasovacie údaje nie sú prenášané na žiadny externý server. Služba automatického dopĺňania nečíta obsah iných aplikácií nad rámec toho, čo je nevyhnutne potrebné na identifikáciu kontextu prihlásenia.
        """.trimIndent()
    ),
    LegalSection(
        "Živý zostatok krypto peňaženky",
        """
        Keď uložíte záznam krypto peňaženky a povolíte sledovanie živého zostatku, KeyArmor sa pýta verejných blockchainových uzlov alebo API na získanie zostatku spojeného s adresou peňaženky, ktorú ste zadali. Prenáša sa iba adresa peňaženky — žiadne súkromné kľúče, seed frázy ani osobné údaje nie sú odosielané. Tieto dopyty idú priamo do verejnej infraštruktúry; KeyArmor neprevádzkuje žiadny sprostredkovateľský server.
        """.trimIndent()
    ),
    LegalSection(
        "Zdieľanie záznamov P2P",
        """
        KeyArmor umožňuje zdieľať jednotlivé záznamy trezoru priamo s inými používateľmi KeyArmor prostredníctvom QR kódu alebo šifrovaného textu. Prenos je end-to-end šifrovaný a peer-to-peer — žiadny server ani sprostredkovateľ nie je zapojený. Nesiete výlučnú zodpovednosť za výber osôb, s ktorými zdieľate záznamy, a za bezpečnosť kanála používaného na prenos šifrovaného textu (ak sa QR nepoužíva priamo).
        """.trimIndent()
    ),
    LegalSection(
        "Knižnice tretích strán",
        """
        KeyArmor používa open-source knižnice (pozri Oznámenie o open source). Žiadna z týchto knižníc neobsahuje telemetriu, analytiku ani mechanizmy zberu údajov v konfigurácii používanej touto aplikáciou.

        KeyArmor tiež využíva dve služby tretích strán pre konkrétne funkcie: RevenueCat, ktorá overuje stav vášho predplatného Premium a prijíma iba informácie súvisiace s nákupom (nikdy váš trezor, hlavné heslo alebo obnovovací kľúč); a Brevo, ktorá odosiela transakčné e-maily vyžadované Digitálnym dedičstvom a Rodinným plánom (pozvánky, potvrdenia súhlasu, oznámenia) a na tento účel prijíma e-mailové adresy osôb zapojených do týchto funkcií. Ani jedna z týchto služieb sa nepoužíva na reklamu ani behaviorálnu analytiku.
        """.trimIndent()
    ),
    LegalSection(
        "Widget domovskej obrazovky",
        """
        KeyArmor obsahuje voliteľný widget pre domovskú obrazovku Androidu, ktorý zobrazuje celkový počet položiek uložených vo vašom trezore. Tento widget pristupuje iba k miestnym údajom zariadenia. Neprenáša žiadne informácie na externé servery. Počet položiek je súhrnný údaj, ktorý neodhaľuje obsah jednotlivých položiek. Sami sa rozhodnete, či widget umiestnite na domovskú obrazovku.
        """.trimIndent()
    ),
    LegalSection(
        "Oprávnenia",
        """
        KeyArmor žiada iba oprávnenia nevyhnutné pre svoju prevádzku:

        • Biometria (voliteľné): na odomykanie odtlačkom prsta.
        • Internet (voliteľné): používa sa iba pre funkcie, ktoré sami povolíte — synchronizácia s Google Drive, Digitálne dedičstvo a Rodinný plán (cez Google Firebase), dopyty na aktuálny zostatok kryptopeňaženiek, kontrola únikov cez Have I Been Pwned a overenie predplatného (Google Play Billing / RevenueCat).
        • Prístup k súborom (voliteľné): na import/export trezoru a pridávanie príloh, iba keď to sami iniciujete.
        • Fotoaparát (voliteľné): na skenovanie QR kódov pri nastavovaní dvojfaktorového overovania (2FA/TOTP) alebo pri prijímaní zdieľaných položiek. Aktivuje sa iba na výslovnú žiadosť používateľa.
        • Služba automatického dopĺňania (voliteľné): aktivuje sa manuálne v systémových nastaveniach Androidu. Slúži výlučne na navrhovanie prihlasovacích údajov na prihlasovacích obrazovkách iných aplikácií.
        • Mikrofón (voliteľné): iba na hlasové diktovanie v Zabezpečených poznámkach. Rozpoznávanie reči prebieha vo vašom zariadení; KeyArmor zvuk nenahráva, neukladá ani neodosiela na žiadny server.
        • Kalendár, iba čítanie (voliteľné): používa ho Bezpečný cestovný režim na hľadanie ciest vo vašich udalostiach. Udalosti sa spracúvajú vo vašom zariadení a nikdy sa neodosielajú.
        • Oznámenia a presné budíky (voliteľné): pre bezpečnostné upozornenia a pripomienky, ktoré si naplánujete. Udalosť spustenia zariadenia slúži na ich opätovné naplánovanie po reštarte.
        • Stav siete (voliteľné): iba na zistenie, či používate mobilné dáta alebo Wi-Fi, aby veľké zálohy na Google Drive čakali na Wi-Fi. Žiadne informácie o vašej sieti sa nezhromažďujú ani neodosielajú.

        KeyArmor nežiada prístup ku kontaktom, polohe ani k iným osobným údajom, ktoré nie sú uvedené vyššie.
        """.trimIndent()
    ),
    LegalSection("Ďalšie služby, ktoré prijímajú údaje", """
Okrem vyššie opísaných služieb KeyArmor používa nasledujúce, a to len pre funkcie, ktoré ich potrebujú:

• Overenie integrity aplikácie (Firebase App Check / Google Play Integrity): Google poskytuje potvrdenie o vašom zariadení, aby náš server prijímal iba požiadavky od pravej aplikácie.
• RevenueCat: okrem údajov o nákupe prijíma identifikátor vášho účtu Google (nie váš e-mail) ako ID používateľa, aby prepojil vaše predplatné medzi zariadeniami.
• CoinGecko: keď používate kryptoceny v reálnom čase, od CoinGecko sa pýtajú symboly z vášho portfólia a CoinGecko môže vidieť vašu IP adresu. Vidieť ju môžu aj vyššie spomenuté blockchainové prehliadače a uzly.
• Google Fonts: aplikácia sťahuje písma Inter a JetBrains Mono cez služby Google Play, takže Google môže pri ich vyžiadaní vidieť vašu IP adresu.
• Dôveryhodný čas služieb Google Play: slúži na zistenie manipulácie s hodinami zariadenia. Neodosielajú sa žiadne osobné údaje.
• Rozpoznávanie textu (OCR) v dokumentoch prebieha vo vašom zariadení pomocou ML Kit. Žiadny obrázok sa neodosiela na server.
• Ko-fi: iba ak otvoríte odkaz na dary; vtedy platia ich vlastné zásady ochrany osobných údajov.

Naše servery môžu dočasne zaznamenávať vašu IP adresu, aby obmedzili zneužitie.
    """.trimIndent()),
    LegalSection(
        "Passkeys (prístupové kľúče)",
        """
        KeyArmor môže fungovať ako poskytovateľ passkeys systému Android (Credential Manager), ale iba ak si to sami zvolíte v nastaveniach systému. Pri vytvorení alebo použití passkey KeyArmor prečíta doménu webu alebo aplikácie, ktorá o ňu žiada, aby passkey k tomuto webu priradil a podpísal výzvu, ktorú web odosiela. Súkromný kľúč sa vygeneruje vo vašom zariadení a uloží sa zašifrovane spolu so zvyškom vášho trezoru; neodosiela sa na žiadny server ani žiadnej tretej strane. Vaše zariadenie opúšťa iba v rámci záloh, ktoré si sami vytvoríte (napríklad šifrovaná záloha na Google Drive). Na podpis pomocou passkey vás KeyArmor požiada o overenie totožnosti odtlačkom prsta alebo hlavným heslom. Web, na ktorom sa prihlasujete, dostane iba verejný kľúč a podpis, ako pri každej passkey.
        """.trimIndent()
    ),
    LegalSection(
        "Odstránenie a uchovávanie údajov",
        """
        Váš trezor sa uchováva vo vašom zariadení (a vo vašom Google Drive, ak zapnete zálohovanie), kým ho sami neodstránite. Na našich serveroch sú iba koordinačné údaje: token push oznámení identifikovaný vaším e-mailom a, ak používate Digitálne dedičstvo alebo Rodinný plán, záznamy o pozvánkach a súhlasoch a šifrovaný blok dedičského trezoru. Uchovávajú sa, kým tieto funkcie používate, alebo kým nepožiadate o ich odstránenie. Môžete ich odstrániť v časti Nastavenia → Odstrániť všetky moje údaje, alebo napísaním na support@thormakk.dev z vášho účtu Google; tieto žiadosti vybavujeme do 30 dní. Úplné podrobnosti nájdete na stránke „Odstránenie účtu a údajov“ na našom webe.
        """.trimIndent()
    ),
    LegalSection(
        "Maloleté osoby",
        """
        KeyArmor nie je určený osobám mladším ako 13 rokov (ani mladším, než je minimálny vek stanovený právom vašej krajiny na udelenie digitálneho súhlasu) a vedome nezhromažďujeme údaje maloletých. Ak sa domnievate, že nám maloletý poskytol údaje, napíšte nám na support@thormakk.dev a my ich odstránime.
        """.trimIndent()
    ),
    LegalSection(
        "Kontakt",
        "V prípade akýchkoľvek otázok týkajúcich sa týchto zásad nás môžete kontaktovať na support@thormakk.dev"
    ),
    LegalSection(
        "Posledná aktualizácia",
        "Október 2026 (rev. 8)"
    )
)

private fun termsSK() = listOf(
    LegalSection(
        "Podmienky používania",
        ""
    ),
    LegalSection(
        "Súhlas",
        """
        Inštaláciou alebo používaním KeyArmor prijímate tieto podmienky v celom ich rozsahu. Ak s akoukoľvek ich časťou nesúhlasíte, aplikáciu nepoužívajte.
        """.trimIndent()
    ),
    LegalSection(
        "Povolené použitie",
        """
        KeyArmor je navrhnutý na osobné použitie ako správca hesiel a zabezpečený trezor. Použitie na nezákonné aktivity, ukladanie informácií uľahčujúcich protiprávne konanie alebo pokusy o reverznú analýzu aplikácie sú zakázané.
        """.trimIndent()
    ),
    LegalSection(
        "Zodpovednosť používateľa",
        """
        Nesiete výlučnú zodpovednosť za:

        • Zabezpečenie vášho hlavného hesla. Ak bude stratené, neexistuje žiadny mechanizmus obnovenia a vaše údaje budú trvalo nedostupné.
        • 12-slovný záložný kľúč (ak túto funkciu používate). Ak ho stratíte a zároveň stratíte aj telefón, vaše zálohy na Drive nebude možné otvoriť, a to ani samotným vývojárom.
        • Udržiavanie aktuálnych záloh vášho trezoru prostredníctvom funkcie exportu alebo synchronizácie Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Funkčnosť synchronizácie",
        """
        Synchronizácia s Google Drive je pomocnou záložnou funkciou. Nenahrádza osvedčené postupy osobnej bezpečnosti. Vývojár nezaručuje nepretržitú dostupnosť tejto funkcie, keďže závisí od služieb tretích strán (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Prémiové predplatné",
        """
        KeyArmor ponúka voliteľné platené plány (Mesačný, Ročný), ktoré odomykajú ďalšie funkcie. Predplatné sú spracovávané výlučne prostredníctvom Google Play Billing. Kúpou predplatného súhlasíte aj s Podmienkami služby Google Play.

        • Mesačné a ročné plány sú automaticky obnovované predplatné. Môžete ich kedykoľvek zrušiť prostredníctvom Google Play; zrušenie nadobúda účinnosť na konci aktuálneho fakturačného obdobia.
        • Vrátenie peňazí sa riadi štandardnými zásadami vrátenia peňazí Google Play. Vývojár nespracúva vrátenia peňazí samostatne.
        • Prémiové funkcie sa môžu časom vyvíjať. Vývojár si vyhradzuje právo pridávať, upravovať alebo ukončovať konkrétne prémiové funkcie s primeraným upozornením.
        """.trimIndent()
    ),
    LegalSection(
        "Bezpečný cestovný režim",
        """
        Bezpečný cestovný režim umožňuje označiť konkrétne záznamy ako cestovne bezpečné a skryť všetky ostatné záznamy, kým je režim aktívny. Nesiete výlučnú zodpovednosť za určenie, ktoré záznamy označiť ako cestovne bezpečné, a za dôsledky aktivácie alebo deaktivácie tohto režimu. Vývojár nepreberá žiadnu zodpovednosť za záznamy náhodne ponechané viditeľné alebo skryté.
        """.trimIndent()
    ),
    LegalSection(
        "Núdzový PIN",
        """
        Funkcia núdzového PIN spúšťa nezvratné konanie (vymazanie alebo skrytie citlivých údajov trezoru) pri zadaní určeného PIN kódu. Po aktivácii môžu byť skryté alebo vymazané údaje neobnoviteľné v závislosti od vašej konfigurácie. Preberáte plnú zodpovednosť za konfiguráciu tejto funkcie a za akúkoľvek stratu údajov vyplývajúcu z jej úmyselnej alebo náhodnej aktivácie.
        """.trimIndent()
    ),
    LegalSection(
        "Informácie o krypto zostatku",
        """
        Funkcia živého zostatku načítava informácie o zostatku z verejnej blockchainovej infraštruktúry výlučne na informačné účely. Tieto informácie nepredstavujú finančné poradenstvo. Vývojár nezaručuje presnosť, úplnosť ani aktuálnosť údajov o zostatku. Neprijímajte finančné rozhodnutia výlučne na základe informácií zobrazených v tejto aplikácii.
        """.trimIndent()
    ),
    LegalSection(
        "Žiadne záruky",
        """
        Aplikácia je poskytovaná „tak ako je", bez akýchkoľvek záruk, výslovných ani predpokladaných. Vývojár nezaručuje, že aplikácia je bez chýb, funguje bez prerušenia alebo je vhodná na konkrétny účel.
        """.trimIndent()
    ),
    LegalSection(
        "Obmedzenie zodpovednosti",
        """
        V maximálnom rozsahu povolenom platným právom vývojár nezodpovedá za stratu údajov, stratu prístupu k trezoru ani za žiadne priame, nepriame, náhodné, špeciálne alebo následné škody vzniknuté v dôsledku používania aplikácie alebo nemožnosti ju používať.
        """.trimIndent()
    ),
    LegalSection(
        "Aktualizácie podmienok",
        """
        Tieto podmienky môžu byť kedykoľvek aktualizované. Pokračujúce používanie aplikácie po zverejnenej aktualizácii predstavuje súhlas s novými podmienkami.
        """.trimIndent()
    ),
    LegalSection(
        "Posledná aktualizácia",
        "Máj 2026 (rev. 2)"
    )
)

private fun legalSK() = listOf(
    LegalSection(
        "Právne upozornenie a vylúčenie zodpovednosti",
        ""
    ),
    LegalSection(
        "Softvér poskytovaný tak ako je",
        """
        KeyArmor je distribuovaný „tak ako je", bez akýchkoľvek záruk, výslovných ani predpokladaných, vrátane, ale nie výlučne, záruk predajnosti, vhodnosti na konkrétny účel alebo neporušenia práv.
        """.trimIndent()
    ),
    LegalSection(
        "Obmedzenie zodpovednosti",
        """
        Vývojár KeyArmor za žiadnych okolností nezodpovedá za:

        • Stratu alebo poškodenie údajov uložených v aplikácii
        • Strata prístupu k trezoru v dôsledku zabudnutia alebo straty hlavného hesla alebo záložného kľúča
        • Zraniteľnosti alebo zlyhania komponentov operačného systému Android
        • Zlyhania, prerušenia alebo zmeny v službách Google Drive
        • Akékoľvek priame, nepriame, náhodné, špeciálne alebo následné škody vzniknuté v dôsledku používania aplikácie alebo nemožnosti ju používať
        • Akékoľvek škody vyplývajúce z neoprávneného prístupu tretích strán k údajom používateľa
        • Stratu údajov vyplývajúcu z úmyselnej alebo náhodnej aktivácie funkcie núdzového PIN
        """.trimIndent()
    ),
    LegalSection(
        "Prenos údajov do Google Drive",
        """
Keď používateľ zapne synchronizáciu s Google Drive (zálohy) alebo použije Digitálne dedičstvo / Rodinný plán (Google Firebase), údaje sa prenášajú už zašifrované do infraštruktúry Google podľa Zmluvných podmienok Google. V každom prípade údaje opúšťajú zariadenie ako nepriehľadný zašifrovaný blok. Vývojár nemá prístup k infraštruktúre Google, neovláda ju a nedokáže čítať prenášaný obsah.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore a závislosť od hardvéru",
        """
        Bezpečnosť šifrovania KeyArmor čiastočne závisí od implementácie Android Keystore v zariadení používateľa. Táto implementácia sa líši v závislosti od výrobcu, čipovej sady a verzie Androidu. Vývojár nekontroluje ani nezaručuje bezpečnosť hardvérových implementácií tretích strán. Dôrazne odporúčame udržiavať operačný systém zariadenia aktuálny.
        """.trimIndent()
    ),
    LegalSection(
        "Obmedzenia služby automatického dopĺňania",
        """
        Služba automatického dopĺňania KeyArmor nemusí správne fungovať so všetkými aplikáciami alebo prehliadačmi. Najmä Google Chrome nepodporuje štandardný framework automatického dopĺňania systému Android — ide o obmedzenie Chrome, ktoré rovnako postihuje všetkých správcov hesiel tretích strán a je mimo kontroly vývojára. Vývojár nezaručuje, že služba automatického dopĺňania bude fungovať s akoukoľvek konkrétnou aplikáciou.
        """.trimIndent()
    ),
    LegalSection(
        "Vyhlásenie o živom krypto zostatku",
        """
        Údaje o živom zostatku sú načítavané z verejných blockchainových uzlov a API tretích strán. Vývojár tieto uzly neprevádzkuje ani nekontroluje. Informácie o zostatku sú poskytované výlučne na informačné účely a môžu byť oneskorené, nepresné alebo nedostupné. Tieto informácie nepredstavujú finančné ani investičné poradenstvo. Vývojár nepreberá žiadnu zodpovednosť za rozhodnutia prijaté na základe údajov o zostatku zobrazených v aplikácii.
        """.trimIndent()
    ),
    LegalSection(
        "P2P zdieľanie",
        """
        Funkcia P2P zdieľania záznamov uľahčuje priame, šifrované zdieľanie záznamov trezoru medzi používateľmi. Vývojár nepreberá žiadnu zodpovednosť za obsah záznamov zdieľaných medzi používateľmi, za totožnosť alebo dôveryhodnosť príjemcu ani za akékoľvek následky vyplývajúce zo zdieľania údajov trezoru s tretími stranami.
        """.trimIndent()
    ),
    LegalSection(
        "Knižnice tretích strán",
        """
        KeyArmor používa open-source knižnice tretích strán, ktoré boli v čase zverejnenia považované za bezpečné. Vývojár nepreberá žiadnu zodpovednosť za zlyhania, zraniteľnosti alebo neočakávané správanie pochádzajúce z komponentov tretích strán.
        """.trimIndent()
    ),
    LegalSection(
        "Rozhodné právo",
        """
        Toto právne upozornenie sa riadi platnou legislatívou Argentínskej republiky. Akýkoľvek spor vzniknutý v dôsledku používania aplikácie bude predložený príslušným súdom danej jurisdikcie.
        """.trimIndent()
    ),
    LegalSection(
        "Posledná aktualizácia",
        "Máj 2026 (rev. 2)"
    )
)

private fun licenseSK() = listOf(
    LegalSection("Licencia Source Available", ""),
    LegalSection("Zdrojový kód zverejnený len na účely bezpečnostného auditu", """
Zdrojový kód KeyArmor je verejne dostupný výlučne preto, aby ho bezpečnostní výskumníci a používatelia mohli skontrolovať a auditovať jeho bezpečnostné postupy. Nie sú udelené žiadne ďalšie práva — všetky práva sú vyhradené držiteľovi autorských práv.
    """.trimIndent()),
    LegalSection("Čo môžete robiť", """
• Čítať a skúmať zdrojový kód na účely bezpečnostného výskumu a osobného auditu
• Nahlásiť bezpečnostné zraniteľnosti tímu KeyArmor cez oficiálne kanály
    """.trimIndent()),
    LegalSection("Čo nesmiete robiť", """
Bez výslovného predchádzajúceho písomného súhlasu držiteľa autorských práv nesmiete:

• Používať alebo spúšťať softvér mimo jeho oficiálnej distribúcie
• Kopírovať, reprodukovať alebo extrahovať zdrojový kód na iný účel než čítanie
• Upravovať, prispôsobovať, prekladať ho alebo na jeho základe vytvárať odvodené diela
• Distribuovať, zverejňovať, sublicencovať, predávať alebo inak sprístupňovať ho tretím stranám
• Používať zdrojový kód ako základ pre iný produkt, službu alebo projekt
• Používať názov, logo alebo akúkoľvek súvisiacu ochrannú známku KeyArmor
    """.trimIndent()),
    LegalSection("Zdrojový kód", """
Zdrojový kód KeyArmor je verejne dostupný.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Žiadna záruka", """
Zdrojový kód je poskytovaný „tak ako je“, bez akejkoľvek záruky, výslovnej alebo implicitnej. Držiteľ autorských práv neposkytuje žiadne vyhlásenia týkajúce sa jeho vhodnosti na konkrétny účel.
    """.trimIndent()),
    LegalSection("Autorské práva", "© 2026 KeyArmor. Všetky práva vyhradené. Source Available.")
)

private fun opensourceSK() = listOf(
    LegalSection(
        "Oznámenie o open source",
        ""
    ),
    LegalSection(
        "Použité knižnice",
        """
        KeyArmor používa nasledujúce open-source knižnice. Pokiaľ nie je uvedené inak, sú distribuované pod licenciou Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Verzia: BOM 2026.09.00
        Licencia: Apache 2.0
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
        Verzia: 2.8.5
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Verzia: 2.60.1
        Licencia: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Verzia: 1.11.0
        Licencia: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Verzia: 1.1.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Verzia: 2.10.2
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Verzia: 2.11.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Verzia: 2.12.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Verzia: 22.0.0
        Licencia: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Verzia: 2.9.2
        Licencia: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Verzia: v3-rev20240123-2.0.0
        Licencia: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Verzia: 2.2.0
        Licencia: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Verzia: 4.3.0
        Licencia: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Verzia: 0.37.3
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Verzia: BOM 2026.09.00
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Verzia: 1.19.1
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Verzia: 1.13.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Verzia: 1.3.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Verzia: 1.3.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Verzia: 1.8.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Verzia: 1.4.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Verzia: 2.4.21
        Licencia: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Verzia: 1.2.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Verzia: 1.4.1
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Verzia: 1.6.0
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Verzia: 1.6.2
        Licencia: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Verzia: BOM 35.1.0
        Licencia: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Verzia: 2.15.0
        Licencia: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Verzia: 3.0.1
        Licencia: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Verzia: 2.0.27.0
        Licencia: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Verzia: 1.2.1
        Licencia: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Verzia: 1.86
        Licencia: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Verzia: 10.29.0
        Licencia: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Verzia: 5.2.0
        Licencia: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Verzia: 5.19.1
        Licencia: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Verzia: 1.2.1
        Licencia: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Verzia: 16.0.1
        Licencia: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Verzia: 16.0.1
        Licencia: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Verzia: 2026-07-09
        Licencia: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Úplný text licencie",
        """
        Úplný text licencie Apache 2.0 je dostupný na:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataSK() = listOf(
    LegalSection("Odstrániť účet a údaje", ""),
    LegalSection("Aké údaje o vás uchováva KeyArmor", """
Váš trezor (heslá, poznámky, karty, peňaženky, prílohy a história hesiel) sa nikdy neukladá na našich serveroch. Je šifrovaný vo vašom zariadení a ak zapnete zálohovanie, aj vo vašom vlastnom Google Drive – nie je to miesto, kde by sme ho mohli prečítať alebo vymazať, pretože sa šifruje ešte predtým, ako vôbec opustí vaše zariadenie.

Jediné údaje spojené s vaším účtom Google, ktoré sa dostanú na naše servery (Firebase), súvisia s prepojením tohto účtu pre voliteľné funkcie – zálohovanie na Google Drive, digitálne dedičstvo alebo rodinný plán. Samotné prepojenie účtu Google s niektorou z týchto funkcií tam zaregistruje minimálny záznam (token push-oznámenia, identifikovaný vašou e-mailovou adresou), a to ešte predtým, ako nakonfigurujete funkciu „Digitálne dedičstvo“ alebo „Rodinný plán“. Ak ste nikdy neprepojili účet Google s aplikáciou KeyArmor, na našich serveroch sa nenachádza žiadny údaj o vás, ktorý by bolo možné vymazať.
    """.trimIndent()),
    LegalSection("Odstránenie z aplikácie", """
Všetky svoje údaje môžete kedykoľvek odstrániť v časti Nastavenia → Odstrániť všetky moje údaje v rámci aplikácie. Ak ste nikdy nepripojili účet Google k aplikácii KeyArmor (na účely zálohovania na Drive, digitálneho dedičstva alebo rodinného plánu), týmto krokom sa váš trezor okamžite a natrvalo vymaže zo zariadenia.

Ak ste účet pripojili, aplikácia vás požiada o prihlásenie cez Google, aby mohla v rámci tej istej akcie vymazať príslušné záznamy na strane servera (Firebase) a zálohy a kópie Time Machine, ktoré KeyArmor uložil na vašom Google Drive. Iba v prípade, že sa nedokážete prihlásiť, aplikácia prejde na vymazanie iba lokálnych údajov – a vopred vás upozorní, že vaše záznamy na strane servera zostanú zachované, kým ich neodstránite samostatne pomocou nižšie uvedenej požiadavky.
    """.trimIndent()),
    LegalSection("Čo sa nevymaže", """
Vymazanie vašich údajov neodstráni to, čo sa nachádza mimo aplikácie:

• Súbory, ktoré ste sami exportovali (súbory CSV, PDF alebo .kab, ktoré ste uložili alebo zdieľali). Patria vám a sú uložené tam, kam ste ich umiestnili, takže ich musíte vymazať priamo tam.
• Záložné kópie, ktoré v priečinku „Stiahnuté“ zanechala predchádzajúca inštalácia aplikácie KeyArmor. Systém Android už neumožňuje, aby sa k nim nová inštalácia dostala; odstráňte ich z priečinka „Stiahnuté“.
• Vaše predplatné. To spravuje služba Google Play, takže vymazaním vašich údajov sa nezruší; zrušte ho v službe Google Play → Predplatné.
    """.trimIndent()),
    LegalSection("Žiadosť o vymazanie údajov vášho účtu", """
Ak ste k aplikácii KeyArmor pripojili účet Google, ale nemôžete sa prihlásiť, aby ste mohli využiť vyššie uvedenú funkciu vymazania v aplikácii (napríklad ak už nemáte aplikáciu nainštalovanú), požiadajte o vymazanie príslušných údajov na strane servera tak, že nám pošlete e-mail z toho istého účtu Google.

Napíšte na adresu: support@thormakk.dev

Žiadosti o vymazanie spracovávame do 30 dní a po ich vybavení vám pošleme potvrdenie e-mailom. Na podanie tejto žiadosti nemusíte mať aplikáciu nainštalovanú.
    """.trimIndent())
)
