package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentSV(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacySV()
    "terms"      -> termsSV()
    "legal"      -> legalSV()
    "license"    -> licenseSV()
    "opensource" -> opensourceSV()
    "delete"     -> deleteDataSV()
    else         -> listOf(LegalSection("Dokumentet hittades inte", ""))
}

private fun privacySV() = listOf(
    LegalSection(
        "Integritetspolicy",
        ""
    ),
    LegalSection(
        "Datainsamling",
        """
        KeyArmor samlar inte in, överför eller delar personuppgifter med externa servrar. All information du anger i appen stannar uteslutande på din enhet, såvida du inte uttryckligen aktiverar Google Drive-synkronisering.

        KeyArmor samlar inte in din e-postadress eller några kontouppgifter för appens kärnfunktioner (valv, autofyll, säkerhetsgranskning, intrångsövervakning) — dessa fungerar helt utan konto. Om du aktiverar Digitalt arv eller Familjeplanen kräver dessa funktioner inloggning med Google: din Google-kontos e-postadress lagras då på Google Firebase för att identifiera dig och matcha dig med de arvingar eller familjemedlemmar du utser, samt för att skicka motsvarande e-postmeddelanden via vår e-postleverantör (Brevo). Denna e-postadress används uteslutande för dessa två funktioner och säljs eller delas aldrig med någon annan. Utanför Digitalt arv och Familjeplanen är den enda personliga identifieraren ett valfritt namn eller smeknamn som du kan ange vid konfigurationen, vilket lagras uteslutande på din enhet i krypterad form.
        """.trimIndent()
    ),
    LegalSection(
        "Lokal lagring",
        """
        All din data (lösenord, passkeys, anteckningar, kreditkort, bankkonton, kryptoplånböcker, bilagor och lösenordshistorik) lagras krypterat med AES-256-GCM, med en nyckel som härleds från ditt huvudlösenord med Argon2id och aldrig lämnar din enhet. Om du aktiverar biometrisk upplåsning skyddas dessutom en kopia av den nyckeln med en nyckel i Android Keystore på din enhet. Ingen data lämnar din enhet utan ditt uttryckliga samtycke.
        """.trimIndent()
    ),
    LegalSection(
        "Huvudlösenord",
        """
        Ditt huvudlösenord lagras aldrig i klartext eller i någon återställningsbar form. Det används uteslutande för att härleda valvets krypteringsnyckel via Argon2id. Om du förlorar ditt huvudlösenord kan din data inte återställas.
        """.trimIndent()
    ),
    LegalSection(
        "Biometri",
        """
        Biometrisk autentisering i KeyArmor hanteras helt av Android-operativsystemet via BiometricPrompt API. KeyArmor har aldrig tillgång till, lagrar eller behandlar biometrisk data.

        Biometri används i två sammanhang inom appen:

        • Valvupplåsning: för att autentisera åtkomst till appen vid uppstart, tillgänglig på alla planer.
        • Avancerat biometriskt skydd (Premium): en valfri barriär som kräver biometrisk verifiering innan känsliga fält i specifika valvavsnitt visas (lösenord, kreditkort, anpassade fält). Konfigurerbar i Inställningar — stöder läge per-åtkomst eller per-session med anpassningsbar tidsgräns. Samma integritetsgarantier gäller: KeyArmor lagrar eller behandlar inga biometriska data.
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive-synkronisering",
        """
Google Drive-synkronisering är helt valfri. När den är aktiverad gör KeyArmor följande:

• Begär en enda, minimal behörighet (DRIVE_APPDATA) för att lagra dina egna krypterade säkerhetskopior och Time Machine-återställningspunkter i en privat, appspecifik mapp i din Google Drive. Mappen är dold — ingen annan app, och inte heller du när du bläddrar i Drive, kan se eller ändra den.
• Krypterar valvet med en reservnyckel bestående av 12 ord innan det laddas upp. Nyckeln skapas på din enhet och endast du har tillgång till den: varken Google eller utvecklaren kan läsa filen eller återställa nyckeln.
• Om du sparar PDF-filen med din reservnyckel i Google Docs, Drive eller någon annan app gör du det själv via delningsskärmen i Android; den appen omfattas av sin egen integritetspolicy.
• Om du byter nyckel kan du behålla de tidigare nycklarna, krypterade på din telefon; kitets PDF-fil kan innehålla dem. KeyArmor skickar dem inte till någon server.
• Får aldrig åtkomst till någon annan Drive-fil, skapar aldrig synliga mappar och delar aldrig filer.
• Behåller valfritt en andra lokal kopia av säkerhetskopian i enhetens mapp Downloads/KeyArmor/, som aldrig lämnar enheten.
• Synkronisering på begäran finns i alla planer (startas manuellt). Automatisk daglig synkronisering finns i Premium.

Digitalt arv och Familjeplanen använder Google Firebase, inte ditt Google Drive. Firebase hanterar samtycke, inbjudningar och aviseringar. Arvsvalvet krypteras från ändpunkt till ändpunkt på din enhet med arvtagarens offentliga nyckel, och den resulterande krypterade datablocken laddas upp till en privat bucket i Firebase Cloud Storage som tillhör utvecklaren via signerade länkar med kort giltighetstid (15 minuter). Servern lagrar datablocken men får aldrig tillgång till arvtagarens nyckel, vilket innebär att den inte kan läsa den.

Du kan koppla från ditt Google-konto när som helst från synkroniseringsskärmen.
    """.trimIndent()
    ),
    LegalSection(
        "Bilagor",
        """
        Med funktionen för bilagor kan du koppla filer (upp till 5 per post, max 5 MB vardera) till dina valvposter. Alla bilagor krypteras med AES-256-GCM innan de sparas i enhetens interna lagringsutrymme. Om du aktiverar synkronisering med Google Drive ingår dina bilagor och dokumentbilder i den säkerhetskopia du laddar upp, krypterade med din reservnyckel. De skickas aldrig till någon annan tjänst.
        """.trimIndent()
    ),
    LegalSection(
        "Lösenordshistorik",
        """
        KeyArmor sparar en lokal historik över tidigare lösenord för varje post. Denna historik lagras krypterat på enheten och överförs aldrig till någon extern server.
        """.trimIndent()
    ),
    LegalSection(
        "In-app-aviseringar",
        """
        KeyArmor genererar interna aviseringar för att varna dig om viktiga säkerhetshändelser:

        • Utgångsdatumsvarningar: skickas när ett eller flera lösenord närmar sig sitt utgångsdatum.
        • Utgångna lösenord: skickas när ett eller flera lösenord redan har passerat sitt utgångsdatum.
        • Intrångsvarningar: skickas när dina lösenord upptäcks i kända dataintrång.

        Dessa tre aviseringstyper genereras helt på din enhet och överförs aldrig till externa servrar. Om du använder Digitalt arv eller Familjeplanen kommer du också att få aviseringar om dessa funktioner (en arvinge som accepterar, en familjemedlem som läggs till, osv.); dessa utlöses av Google Firebase och levereras via Firebase Cloud Messaging, eftersom de beror på händelser som initieras av ett annat konto. Alla aviseringar, oavsett ursprung, raderas automatiskt efter 30 dagar. Du kan ta bort valfri avisering genom att svepa den åt sidan eller rensa allt på en gång från aviseringsskärmen. Push-aviseringar kräver systembehörighet för att skickas.
        """.trimIndent()
    ),
    LegalSection(
        "Säkerhetsrevision",
        """
        Säkerhetsrevisionsfunktionen analyserar dina lösenord lokalt på enheten. Kontroll av vanliga lösenord görs genom jämförelse mot en fördefinierad lista som lagras i appen själv, utan förfrågningar till externa tjänster.
        """.trimIndent()
    ),
    LegalSection(
        "Intrångsövervakning (HIBP)",
        """
        KeyArmor kan kontrollera om dina lösenord har förekommit i kända dataintrång med hjälp av tjänsten Have I Been Pwned (HIBP), som drivs av säkerhetsforskaren Troy Hunt.

        Kontrollen använder en k-anonymitetsmodell: endast de första 5 tecknen i en SHA-1-hash av ditt lösenord skickas till HIBP API. Den fullständiga hashen överförs aldrig. Matchning utförs lokalt på din enhet. Inga användarnamn, e-postadresser eller kontokopplingar skickas någonsin. HIBP-svar är utfyllda med lockbeteshashvärden så att inte ens nätverkstrafikanalys avslöjar vilket lösenord som kontrolleras.
        """.trimIndent()
    ),
    LegalSection(
        "Autofylltjänst",
        """
        KeyArmor innehåller en valfri Android-autofylltjänst. När den är aktiverad i systeminställningarna läser den apppaketnamnet eller webbplatsens URL för den aktiva inloggningsskärmen för att föreslå matchande uppgifter från ditt valv. Denna matchning utförs helt på enheten. Inga appnamn, webbadresser eller uppgifter överförs till någon extern server. Autofylltjänsten läser inte innehållet i andra appar utöver vad som är absolut nödvändigt för att identifiera inloggningskontexten.
        """.trimIndent()
    ),
    LegalSection(
        "Kryptoplånbok live-saldo",
        """
        När du lagrar en kryptoplånbokspost och aktiverar live-saldomonitorering skickar KeyArmor förfrågningar till offentliga blockkedjenoder eller API:er för att hämta saldot kopplat till den plånboksadress du angett. Endast plånboksadressen överförs — inga privata nycklar, seed-fraser eller personuppgifter skickas. Dessa förfrågningar går direkt till offentlig infrastruktur; KeyArmor driver ingen mellanliggande server.
        """.trimIndent()
    ),
    LegalSection(
        "P2P-delning av poster",
        """
        KeyArmor låter dig dela enskilda valvposter direkt med andra KeyArmor-användare via QR-kod eller krypterad text. Överföringen är end-to-end-krypterad och peer-to-peer — ingen server eller mellanhand är inblandad. Du är ensam ansvarig för att välja vem du delar poster med och för säkerheten i den kanal som används för att överföra den krypterade texten (om inte QR används direkt).
        """.trimIndent()
    ),
    LegalSection(
        "Tredjepartsbibliotek",
        """
        KeyArmor använder bibliotek med öppen källkod (se meddelande om öppen källkod). Inget av dessa bibliotek innehåller telemetri-, analys- eller datainsamlingsmekanismer i den konfiguration som används av denna app.

        KeyArmor förlitar sig också på två tredjepartstjänster för specifika funktioner: RevenueCat, som verifierar din Premium-prenumerationsstatus och endast tar emot köprelaterad information (aldrig ditt valv, huvudlösenord eller återställningsnyckel); och Brevo, som skickar de transaktionsmejl som krävs av Digitalt arv och Familjeplanen (inbjudningar, samtyckesbekräftelser, aviseringar) och för detta ändamål tar emot e-postadressen till de personer som är involverade i dessa funktioner. Ingen av tjänsterna används för reklam eller beteendeanalys.
        """.trimIndent()
    ),
    LegalSection(
        "Hemskärmswidget",
        """
        KeyArmor innehåller en valfri Android-hemskärmswidget som visar det totala antalet poster som lagrats i ditt valv. Denna widget får endast åtkomst till lokal enhetsdata. Den överför ingen information till externa servrar. Antalet poster är aggregerade data som inte avslöjar innehållet i enskilda poster. Du väljer själv om du vill placera widgeten på din hemskärm.
        """.trimIndent()
    ),
    LegalSection(
        "Behörigheter",
        """
        KeyArmor begär bara de behörigheter som är nödvändiga för dess drift:

        • Biometri (valfritt): för upplåsning med fingeravtryck.
        • Internet (valfritt): används endast för funktioner du själv aktiverar — Google Drive-synkronisering, Digitalt arv och Familjeplanen (via Google Firebase), realtidsförfrågningar om kryptovalutasaldo, intrångskontroller via Have I Been Pwned, samt prenumerationsverifiering (Google Play Billing / RevenueCat).
        • Filåtkomst (valfritt): för import/export av valv och tillägg av bilagor, bara när du initierar det.
        • Kamera (valfritt): för att skanna QR-koder vid inställning av tvåfaktorautentisering (2FA/TOTP) eller vid mottagning av delade poster. Aktiveras bara på uttrycklig begäran av användaren.
        • Autofylltjänst (valfritt): aktiveras manuellt i Androids systeminställningar. Används uteslutande för att föreslå uppgifter på inloggningsskärmar i andra appar.
        • Mikrofon (valfritt): endast för röstdiktering i Säkra anteckningar. Taligenkänningen sker på din enhet; KeyArmor spelar inte in, sparar eller skickar ljud till någon server.
        • Kalender, endast läsning (valfritt): används av Säkert reseläge för att hitta resor i dina händelser. Händelserna bearbetas på din enhet och överförs aldrig.
        • Aviseringar och exakta alarm (valfritt): för säkerhetsvarningar och de påminnelser du schemalägger. Enhetens startevent används för att schemalägga om dem efter en omstart.
        • Nätverksstatus (valfritt): endast för att veta om du använder mobildata eller Wi-Fi, så att stora Google Drive-säkerhetskopior väntar på Wi-Fi. Ingen information om ditt nätverk samlas in eller skickas.

        KeyArmor begär inte åtkomst till kontakter, plats eller andra personuppgifter som inte nämns ovan.
        """.trimIndent()
    ),
    LegalSection("Andra tjänster som tar emot data", """
Utöver tjänsterna som beskrivs ovan använder KeyArmor följande, endast för de funktioner som behöver dem:

• Integritetskontroll av appen (Firebase App Check / Google Play Integrity): Google tillhandahåller ett intyg om din enhet så att vår server bara accepterar förfrågningar från den äkta appen.
• RevenueCat: tar utöver köpinformation emot identifieraren för ditt Google-konto (inte din e-postadress) som användar-ID, för att koppla ihop din prenumeration mellan enheter.
• CoinGecko: när du använder kryptopriser i realtid begärs tickers från din portfölj hos CoinGecko, som kan se din IP-adress. Blockkedjeutforskarna och noderna som nämns ovan kan också se den.
• Google Fonts: appen hämtar typsnitten Inter och JetBrains Mono via Google Play-tjänster, så Google kan se din IP-adress när de begärs.
• Betrodd tid via Google Play-tjänster: används för att upptäcka manipulation av enhetens klocka. Inga personuppgifter skickas.
• Textigenkänning (OCR) av dokument sker på din enhet med ML Kit. Ingen bild skickas till någon server.
• Ko-fi: endast om du öppnar donationslänken; då gäller deras egen integritetspolicy.

Våra servrar kan tillfälligt logga din IP-adress för att begränsa missbruk.
    """.trimIndent()),
    LegalSection(
        "Passkeys (åtkomstnycklar)",
        """
        KeyArmor kan fungera som Androids leverantör av passkeys (Credential Manager), men bara om du själv väljer det i systeminställningarna. När du skapar eller använder en passkey läser KeyArmor domänen för den webbplats eller app som begär den, för att koppla passkeyn till den webbplatsen och för att signera den utmaning (challenge) som webbplatsen skickar. Den privata nyckeln skapas på din enhet och lagras krypterad tillsammans med resten av ditt valv; den skickas inte till någon server eller tredje part. Den lämnar bara din enhet i de säkerhetskopior som du själv skapar (till exempel den krypterade säkerhetskopian på Google Drive). För att signera med en passkey ber KeyArmor dig verifiera din identitet med ditt fingeravtryck eller ditt huvudlösenord. Webbplatsen du loggar in på får, precis som med alla passkeys, bara den publika nyckeln och signaturen.
        """.trimIndent()
    ),
    LegalSection(
        "Radering och lagring av data",
        """
        Ditt valv finns kvar på din enhet (och i din Google Drive, om du aktiverar säkerhetskopiering) tills du själv raderar det. På våra servrar finns bara samordningsdata: token för pushnotiser, identifierad via din e-postadress, och, om du använder Digitalt arv eller Familjeplanen, loggarna över inbjudningar och samtycke samt det krypterade blocket med arvsvalvet. De sparas så länge du använder de funktionerna eller tills du begär att de raderas. Du kan radera dem via Inställningar → Radera alla mina data, eller genom att skriva till support@thormakk.dev från ditt Google-konto; vi behandlar sådana begäranden inom 30 dagar. Hela informationen finns på sidan «Radera konto och data» på vår webbplats.
        """.trimIndent()
    ),
    LegalSection(
        "Minderåriga",
        """
        KeyArmor riktar sig inte till barn under 13 år (eller under den lägsta ålder som lagen i ditt land fastställer för digitalt samtycke), och vi samlar inte medvetet in data från minderåriga. Om du tror att en minderårig har lämnat data till oss, skriv till support@thormakk.dev så raderar vi dem.
        """.trimIndent()
    ),
    LegalSection(
        "Kontakt",
        "För frågor om denna policy kan du skriva till support@thormakk.dev"
    ),
    LegalSection(
        "Senast uppdaterad",
        "Oktober 2026 (rev. 8)"
    )
)

private fun termsSV() = listOf(
    LegalSection(
        "Användarvillkor",
        ""
    ),
    LegalSection(
        "Godkännande",
        """
        Genom att installera eller använda KeyArmor accepterar du dessa villkor i sin helhet. Om du inte håller med om någon del, använd inte appen.
        """.trimIndent()
    ),
    LegalSection(
        "Tillåten användning",
        """
        KeyArmor är utformad för personligt bruk som lösenordshanterare och säkert valv. Användning för olagliga aktiviteter, lagring av information som underlättar olagliga handlingar eller försök att bakåtkompilera appen är förbjudet.
        """.trimIndent()
    ),
    LegalSection(
        "Användaransvar",
        """
        Du är ensam ansvarig för:

        • Säkerheten för ditt huvudlösenord. Om det förloras finns det ingen återställningsmekanism och din data blir permanent otillgänglig.
        • Reservnyckeln på 12 ord (om du använder den funktionen). Om du tappar bort den och dessutom tappar bort din telefon går det inte att öppna dina Drive-säkerhetskopior, inte ens för utvecklaren.
        • Att upprätthålla aktuella säkerhetskopior av ditt valv via exportfunktionen eller Drive-synkronisering.
        """.trimIndent()
    ),
    LegalSection(
        "Synkroniseringsfunktionalitet",
        """
        Google Drive-synkronisering är en hjälpfunktion för säkerhetskopiering. Det ersätter inte goda personliga säkerhetspraxis. Utvecklaren garanterar inte kontinuerlig tillgänglighet för denna funktion, eftersom den är beroende av tredjepartstjänster (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Premium-prenumerationer",
        """
        KeyArmor erbjuder valfria betalplaner (Månadsvis, Årsvis) som låser upp ytterligare funktioner. Prenumerationer behandlas uteslutande via Google Play Billing. Genom att köpa en prenumeration godkänner du också Google Plays användarvillkor.

        • Månads- och Årsplaner är prenumerationer med automatisk förnyelse. Du kan säga upp dem när som helst via Google Play; uppsägningen träder i kraft vid slutet av den aktuella faktureringsperioden.
        • Återbetalningar omfattas av Google Plays standardpolicy för återbetalningar. Utvecklaren behandlar inte återbetalningar självständigt.
        • Premiumfunktioner kan förändras över tid. Utvecklaren förbehåller sig rätten att lägga till, ändra eller avveckla specifika premiumfunktioner med rimligt varsel.
        """.trimIndent()
    ),
    LegalSection(
        "Reseläge",
        """
        Reseläge låter dig markera specifika poster som resevänliga och dölja alla andra poster medan läget är aktivt. Du är ensam ansvarig för att avgöra vilka poster som ska markeras som resevänliga och för konsekvenserna av att aktivera eller inaktivera detta läge. Utvecklaren tar inget ansvar för poster som av misstag lämnats synliga eller dolda.
        """.trimIndent()
    ),
    LegalSection(
        "Panik-PIN",
        """
        Panik-PIN-funktionen utlöser en oåterkallelig åtgärd (radering eller döljning av känsliga valvdata) när en angiven PIN-kod anges. När den aktiveras kan dolda eller raderade data vara oåterkalleliga beroende på din konfiguration. Du tar fullt ansvar för att konfigurera denna funktion och för eventuell dataförlust till följd av dess avsiktliga eller oavsiktliga aktivering.
        """.trimIndent()
    ),
    LegalSection(
        "Kryptosaldoinformation",
        """
        Funktionen för livesaldo hämtar saldoinformation från offentlig blockkedjeinfrastruktur enbart i informationssyfte. Denna information utgör inte finansiell rådgivning. Utvecklaren garanterar inte saldodatans noggrannhet, fullständighet eller aktualitet. Fatta inte finansiella beslut enbart baserat på information som visas i denna app.
        """.trimIndent()
    ),
    LegalSection(
        "Inga garantier",
        """
        Appen tillhandahålls "i befintligt skick", utan garantier av något slag, uttryckliga eller underförstådda. Utvecklaren garanterar inte att appen är felfri, fungerar utan avbrott eller är lämplig för ett specifikt ändamål.
        """.trimIndent()
    ),
    LegalSection(
        "Ansvarsbegränsning",
        """
        I den utsträckning som tillåts av tillämplig lag ska utvecklaren inte vara ansvarig för dataförlust, förlust av valvåtkomst eller några direkta, indirekta, oförutsedda, särskilda eller följdskador som uppstår till följd av användning av eller oförmåga att använda appen.
        """.trimIndent()
    ),
    LegalSection(
        "Uppdateringar av villkor",
        """
        Dessa villkor kan uppdateras när som helst. Fortsatt användning av appen efter en publicerad uppdatering utgör godkännande av de nya villkoren.
        """.trimIndent()
    ),
    LegalSection(
        "Senast uppdaterad",
        "Maj 2026 (rev. 2)"
    )
)

private fun legalSV() = listOf(
    LegalSection(
        "Juridiskt meddelande och ansvarsfriskrivning",
        ""
    ),
    LegalSection(
        "Programvara tillhandahålls i befintligt skick",
        """
        KeyArmor distribueras "i befintligt skick", utan garantier av något slag, uttryckliga eller underförstådda, inklusive men inte begränsat till garantier om säljbarhet, lämplighet för ett visst ändamål eller icke-intrång.
        """.trimIndent()
    ),
    LegalSection(
        "Ansvarsbegränsning",
        """
        Utvecklaren av KeyArmor ska under inga omständigheter vara ansvarig för:

        • Förlust eller korruption av data som lagrats i appen
        • Förlorad åtkomst till valvet på grund av glömt eller förlorat huvudlösenord eller reservnyckel
        • Sårbarheter eller fel i Android OS-komponenter
        • Fel, avbrott eller ändringar i Google Drive-tjänster
        • Några direkta, indirekta, oförutsedda, särskilda eller följdskador som uppstår till följd av användning av eller oförmåga att använda appen
        • Skador till följd av obehörig åtkomst till användardata av tredje part
        • Dataförlust till följd av avsiktlig eller oavsiktlig aktivering av Panik-PIN-funktionen
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive-dataöverföring",
        """
När användaren aktiverar Google Drive-synkronisering (säkerhetskopior) eller använder Digitalt arv / Familjeplanen (Google Firebase) överförs data redan krypterad till Googles infrastruktur enligt Googles användarvillkor. I samtliga fall lämnar data enheten som ett ogenomskinligt krypterat block. Utvecklaren har ingen åtkomst till eller kontroll över Googles infrastruktur och kan inte läsa det överförda innehållet.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore och hårdvaruberoende",
        """
        KeyArmors krypteringssäkerhet beror delvis på Android Keystore-implementeringen på användarens enhet. Denna implementering varierar beroende på tillverkare, chipset och Android-version. Utvecklaren kontrollerar eller garanterar inte säkerheten för tredjepartshårdvaruimplementeringar. Det rekommenderas starkt att hålla enhetens operativsystem uppdaterat.
        """.trimIndent()
    ),
    LegalSection(
        "Begränsningar av autofylltjänsten",
        """
        KeyArmors autofylltjänst kanske inte fungerar korrekt med alla applikationer eller webbläsare. I synnerhet stöder Google Chrome inte Androids standardramverk för autofyllning — detta är en Chrome-begränsning som drabbar alla tredjepartslösenordshanterare lika och som är utanför utvecklarens kontroll. Utvecklaren garanterar inte att autofylltjänsten fungerar med någon specifik applikation.
        """.trimIndent()
    ),
    LegalSection(
        "Friskrivning för live-kryptosaldo",
        """
        Livesaldodata hämtas från offentliga blockkedjenoder och API:er från tredje part. Utvecklaren driver eller kontrollerar inte dessa noder. Saldoinformation tillhandahålls enbart i informationssyfte och kan vara försenad, felaktig eller otillgänglig. Denna information utgör inte finansiell eller investeringsrådgivning. Utvecklaren tar inget ansvar för beslut som fattas baserade på saldodata som visas i appen.
        """.trimIndent()
    ),
    LegalSection(
        "P2P-delning",
        """
        P2P-postdelningsfunktionen underlättar direkt, krypterat utbyte av valvposter mellan användare. Utvecklaren tar inget ansvar för innehållet i poster som delas mellan användare, för mottagarens identitet eller tillförlitlighet, eller för några konsekvenser som uppstår till följd av att valvdata delas med tredje part.
        """.trimIndent()
    ),
    LegalSection(
        "Tredjepartsbibliotek",
        """
        KeyArmor använder tredjepartsbibliotek med öppen källkod som ansågs säkra vid publiceringstillfället. Utvecklaren tar inget ansvar för fel, sårbarheter eller oväntat beteende som härstammar från tredjepartskomponenter.
        """.trimIndent()
    ),
    LegalSection(
        "Tillämplig lag",
        """
        Detta juridiska meddelande regleras av de lagar som gäller i Republiken Argentina. Tvister som uppstår till följd av användningen av appen ska överlämnas till de behöriga domstolarna i den jurisdiktionen.
        """.trimIndent()
    ),
    LegalSection(
        "Senast uppdaterad",
        "Maj 2026 (rev. 2)"
    )
)

private fun licenseSV() = listOf(
    LegalSection("Source Available-licens", ""),
    LegalSection("Källkod publicerad endast för säkerhetsgranskningssyften", """
KeyArmors källkod är offentligt tillgänglig endast för att säkerhetsforskare och användare ska kunna granska och kontrollera dess säkerhetspraxis. Inga andra rättigheter beviljas — alla rättigheter förbehålls upphovsrättsinnehavaren.
    """.trimIndent()),
    LegalSection("Vad du får göra", """
• Läsa och granska källkoden för säkerhetsforskning och personlig granskning
• Rapportera säkerhetsbrister till KeyArmor-teamet via officiella kanaler
    """.trimIndent()),
    LegalSection("Vad du inte får göra", """
Utan uttryckligt föregående skriftligt tillstånd från upphovsrättsinnehavaren får du inte:

• Använda eller köra programvaran utanför dess officiella distribution
• Kopiera, reproducera eller extrahera källkoden för något annat syfte än att läsa
• Modifiera, anpassa, översätta eller skapa härledda verk baserade på den
• Distribuera, publicera, underlicensiera, sälja eller på annat sätt göra den tillgänglig för tredje part
• Använda källkoden som grund för en annan produkt, tjänst eller projekt
• Använda KeyArmors namn, logotyp eller tillhörande varumärken
    """.trimIndent()),
    LegalSection("Källkod", """
KeyArmors källkod är offentligt tillgänglig.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Ingen garanti", """
Källkoden tillhandahålls "i befintligt skick", utan någon form av garanti, uttrycklig eller underförstådd. Upphovsrättsinnehavaren gör inga utfästelser om dess lämplighet för något specifikt syfte.
    """.trimIndent()),
    LegalSection("Upphovsrätt", "© 2026 KeyArmor. Alla rättigheter förbehållna. Source Available.")
)

private fun opensourceSV() = listOf(
    LegalSection(
        "Meddelande om öppen källkod",
        ""
    ),
    LegalSection(
        "Använda bibliotek",
        """
        KeyArmor använder följande bibliotek med öppen källkod. Om inget annat anges distribueras de under Apache 2.0-licensen.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Version: BOM 2026.09.00
        Licens: Apache 2.0
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
        Version: 2.8.5
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Version: 2.60.1
        Licens: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Version: 1.11.0
        Licens: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Version: 1.1.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Version: 2.10.2
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Version: 2.11.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Version: 2.12.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Version: 22.0.0
        Licens: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Version: 2.9.2
        Licens: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Version: v3-rev20240123-2.0.0
        Licens: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Version: 2.2.0
        Licens: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Version: 4.3.0
        Licens: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Version: 0.37.3
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Version: BOM 2026.09.00
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Version: 1.19.1
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Version: 1.13.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Version: 1.3.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Version: 1.3.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Version: 1.8.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Version: 1.4.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Version: 2.4.21
        Licens: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Version: 1.2.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Version: 1.4.1
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Version: 1.6.0
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Version: 1.6.2
        Licens: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Version: BOM 35.1.0
        Licens: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Version: 2.15.0
        Licens: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Version: 3.0.1
        Licens: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Version: 2.0.27.0
        Licens: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Version: 1.2.1
        Licens: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Version: 1.86
        Licens: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Version: 10.29.0
        Licens: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Version: 5.2.0
        Licens: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Version: 5.19.1
        Licens: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Version: 1.2.1
        Licens: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Version: 16.0.1
        Licens: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Version: 16.0.1
        Licens: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Version: 2026-07-09
        Licens: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Fullständig licenstext",
        """
        Den fullständiga texten för Apache 2.0-licensen finns på:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataSV() = listOf(
    LegalSection("Ta bort konto och data", ""),
    LegalSection("Vilka uppgifter KeyArmor lagrar om dig", """
Ditt valv (lösenord, anteckningar, kort, plånböcker, bilagor och lösenordshistorik) lagras aldrig på våra servrar. Det förvaras krypterat på din enhet och, om du aktiverar säkerhetskopiering, på ditt eget Google Drive – inte någonstans där vi kan läsa eller radera det, eftersom det krypteras innan det överhuvudtaget lämnar din enhet.

De enda uppgifterna som är kopplade till ditt Google-konto och som når våra servrar (Firebase) är de som krävs för att ansluta det kontot till en valfri funktion – säkerhetskopiering till Google Drive, Digital Inheritance eller Family Plan. Enbart genom att koppla ett Google-konto till någon av dessa funktioner registreras en minimal uppgift där (en push-notifikationstoken, kopplad till din e-postadress), redan innan du konfigurerar Digital arv eller Familjeabonnemang. Om du aldrig har kopplat ett Google-konto till KeyArmor finns det ingenting om dig på våra servrar att radera.
    """.trimIndent()),
    LegalSection("Radera från appen", """
Du kan när som helst radera alla dina data från Inställningar → Radera alla mina data i appen. Om du aldrig har kopplat ett Google-konto till KeyArmor (för säkerhetskopiering till Drive, digital arvhantering eller familjeabonnemang) raderas ditt valv omedelbart och permanent från enheten.

Om du däremot har kopplat ett konto ber appen dig att logga in med Google så att den, i samma åtgärd, kan radera de tillhörande posterna på serversidan (Firebase) samt de säkerhetskopior och Time Machine-kopior som KeyArmor har sparat på din Google Drive. Endast om du inte kan logga in faller appen tillbaka till att endast radera lokala data – och den varnar dig i förväg om att dina poster på serversidan kommer att finnas kvar tills de tas bort separat, med hjälp av begäran nedan.
    """.trimIndent()),
    LegalSection("Vad som inte raderas", """
Att radera dina data innebär inte att det som finns utanför appen tas bort:

• Filer som du själv har exporterat (CSV-, PDF- eller .kab-filer som du har sparat eller delat). De tillhör dig och lagras där du har placerat dem, så du måste radera dem där.
• Säkerhetskopior som en tidigare installation av KeyArmor lämnat kvar i mappen Nedladdningar. Android tillåter inte längre att en ny installation rör dem; ta bort dem från mappen Nedladdningar.
• Ditt abonnemang. Det hanteras av Google Play, så att radera dina data innebär inte att det sägs upp; säg upp det i Google Play → Abonnemang.
    """.trimIndent()),
    LegalSection("Begäran om radering av dina kontouppgifter", """
Om du har kopplat ett Google-konto till KeyArmor men inte kan logga in för att använda raderingsfunktionen i appen enligt ovan (till exempel om du inte längre har appen installerad), kan du begära radering av de tillhörande uppgifterna på serversidan genom att skicka ett e-postmeddelande till oss från samma Google-konto.

Skriv till: support@thormakk.dev

Vi behandlar raderingsbegäranden inom 30 dagar och bekräftar via e-post när de har genomförts. Du behöver inte ha appen installerad för att göra denna begäran.
    """.trimIndent())
)
