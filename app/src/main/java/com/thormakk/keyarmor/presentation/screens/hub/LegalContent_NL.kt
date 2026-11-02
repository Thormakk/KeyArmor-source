package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentNL(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyNL()
    "terms"      -> termsNL()
    "legal"      -> legalNL()
    "license"    -> licenseNL()
    "opensource" -> opensourceNL()
    "delete"     -> deleteDataNL()
    else         -> listOf(LegalSection("Document niet gevonden", ""))
}

private fun privacyNL() = listOf(
    LegalSection(
        "Privacybeleid",
        ""
    ),
    LegalSection(
        "Gegevensverzameling",
        """
        KeyArmor verzamelt, verzendt of deelt geen persoonlijke gegevens met externe servers. Alle informatie die u in de app invoert, blijft uitsluitend op uw apparaat staan, tenzij u Google Drive-synchronisatie expliciet inschakelt.

        KeyArmor verzamelt uw e-mailadres niet en ook geen accountreferenties voor de kernfuncties van de app (kluis, automatisch invullen, beveiligingsaudit, datalekbewaking) — deze werken volledig zonder account. Als u Digitale Nalatenschap of het Gezinsabonnement inschakelt, vereisen deze functies aanmelden met Google: uw Google-e-mailadres wordt dan opgeslagen op Google Firebase om u te identificeren en te koppelen aan de erfgenamen of gezinsleden die u aanwijst, en om de bijbehorende meldingsmails te versturen via onze e-mailprovider (Brevo). Dit e-mailadres wordt uitsluitend voor deze twee functies gebruikt en wordt nooit verkocht of gedeeld met anderen. Buiten Digitale Nalatenschap en het Gezinsabonnement is het enige persoonlijke identificatiemiddel een optionele naam of bijnaam die u tijdens de installatie kunt instellen, die uitsluitend op uw apparaat wordt opgeslagen in versleutelde vorm.
        """.trimIndent()
    ),
    LegalSection(
        "Lokale opslag",
        """
        Al uw gegevens (wachtwoorden, passkeys, notities, creditcards, bankrekeningen, cryptowallets, bijlagen en wachtwoordgeschiedenis) worden versleuteld opgeslagen met AES-256-GCM, met een sleutel die met Argon2id is afgeleid van uw hoofdwachtwoord en uw apparaat nooit verlaat. Als u biometrische ontgrendeling inschakelt, wordt een kopie van die sleutel bovendien beschermd met een sleutel van de Android Keystore van uw apparaat. Er verlaten geen gegevens uw apparaat zonder uw uitdrukkelijke toestemming.
        """.trimIndent()
    ),
    LegalSection(
        "Hoofdwachtwoord",
        """
        Uw hoofdwachtwoord wordt nooit opgeslagen in platte tekst of in een herstelbare vorm. Het wordt uitsluitend gebruikt om de kluis-versleutelingssleutel af te leiden via Argon2id. Als u uw hoofdwachtwoord verliest, kunnen uw gegevens niet worden hersteld.
        """.trimIndent()
    ),
    LegalSection(
        "Biometrie",
        """
        Biometrische authenticatie in KeyArmor wordt volledig beheerd door het Android-besturingssysteem via de BiometricPrompt API. KeyArmor heeft op geen enkel moment toegang tot, slaat op of verwerkt biometrische gegevens.

        Biometrie wordt binnen de app in twee contexten gebruikt:

        • Kluisontgrendeling: om toegang tot de app bij het opstarten te authenticeren, beschikbaar op alle abonnementen.
        • Geavanceerde biometrische beveiliging (Premium): een optionele barrière die biometrische verificatie vereist voordat gevoelige velden in specifieke kluisonderdelen worden weergegeven (wachtwoorden, creditcards, aangepaste velden). Configureerbaar in Instellingen — ondersteunt de modus per-toegang of per-sessie met aanpasbare time-out. Dezelfde privacygaranties gelden: KeyArmor slaat geen biometrische gegevens op en verwerkt deze niet.
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive-synchronisatie",
        """
Google Drive-synchronisatie is volledig optioneel. Wanneer ingeschakeld, doet KeyArmor het volgende:

• Vraagt één enkele, minimale toestemming (DRIVE_APPDATA) om je eigen versleutelde back-ups en Time Machine-herstelpunten op te slaan in een privé, app-eigen map in je Google Drive. Deze map is verborgen — geen enkele andere app, en jij ook niet terwijl je door Drive bladert, kan hem zien of wijzigen.
• Versleutelt de kluis met een back-upsleutel van 12 woorden voordat deze wordt geüpload. De sleutel wordt op je apparaat aangemaakt en alleen jij beschikt erover: noch Google, noch de ontwikkelaar kan het bestand lezen of de sleutel achterhalen.
• Als je de PDF van je back-upsleutel opslaat in Google Docs, Drive of een andere app, doe je dat zelf via het deelvenster van Android; voor die app geldt een eigen privacybeleid.
• Als je je sleutel wijzigt, kun je de vorige sleutels versleuteld op je telefoon bewaren; de pdf van de kit kan ze bevatten. KeyArmor stuurt ze naar geen enkele server.
• Krijgt nooit toegang tot andere Drive-bestanden, maakt nooit zichtbare mappen en deelt nooit bestanden.
• Bewaart optioneel een tweede lokale kopie van de back-up in de map Downloads/KeyArmor/ van je apparaat, die het apparaat nooit verlaat.
• Synchronisatie op aanvraag is beschikbaar in alle abonnementen (handmatig gestart). Automatische dagelijkse synchronisatie is beschikbaar in Premium.

Digitale Nalatenschap en het Gezinsabonnement maken gebruik van Google Firebase, niet van je Google Drive. Firebase regelt de toestemming, uitnodigingen en meldingen. De erfeniskluis wordt op je apparaat van punt tot punt versleuteld met de openbare sleutel van de erfgenaam, en het resulterende versleutelde blok wordt via kortlopende (15 minuten) ondertekende links geüpload naar een privé-bucket van Firebase Cloud Storage van de ontwikkelaar. De server bewaart dat blok, maar ontvangt nooit de sleutel van de erfgenaam, zodat hij het niet kan lezen.

Je kunt je Google-account op elk moment loskoppelen via het synchronisatiescherm.
    """.trimIndent()
    ),
    LegalSection(
        "Bijlagen",
        """
        Met de functie ‘Bijlagen’ kun je bestanden (maximaal 5 per item, elk maximaal 5 MB) koppelen aan je kluisitems. Alle bijlagen worden versleuteld met AES-256-GCM voordat ze worden opgeslagen op het interne geheugen van het apparaat. Als je de synchronisatie met Google Drive inschakelt, worden je bijlagen en foto’s van documenten opgenomen in de back-up die je uploadt, versleuteld met je back-upsleutel. Ze worden nooit naar een andere dienst verzonden.
        """.trimIndent()
    ),
    LegalSection(
        "Wachtwoordgeschiedenis",
        """
        KeyArmor bewaart een lokale geschiedenis van eerdere wachtwoorden voor elk item. Deze geschiedenis wordt versleuteld opgeslagen op het apparaat en wordt nooit verzonden naar een externe server.
        """.trimIndent()
    ),
    LegalSection(
        "In-app meldingen",
        """
        KeyArmor genereert interne meldingen om u te informeren over belangrijke beveiligingsgebeurtenissen:

        • Vervalwaarschuwingen: verzonden wanneer een of meer wachtwoorden hun vervaldatum naderen.
        • Verlopen wachtwoorden: verzonden wanneer een of meer wachtwoorden hun vervaldatum al hebben overschreden.
        • Inbreukwaarschuwingen: verzonden wanneer uw wachtwoorden worden gedetecteerd in bekende datalekken.

        Deze drie meldingstypen worden volledig op uw apparaat gegenereerd en worden nooit naar externe servers verzonden. Als u Digitale Nalatenschap of het Gezinsabonnement gebruikt, ontvangt u ook meldingen over die functies (een erfgenaam die accepteert, een gezinslid dat wordt toegevoegd, enz.); deze zijn afkomstig van Google Firebase en worden bezorgd via Firebase Cloud Messaging, aangezien ze afhankelijk zijn van gebeurtenissen die door een ander account worden geïnitieerd. Alle meldingen, ongeacht hun oorsprong, worden na 30 dagen automatisch verwijderd. U kunt elke melding verwijderen door deze zijwaarts te vegen, of alles tegelijk wissen vanuit het Meldingenscherm. Voor pushmeldingen is systeemtoestemming vereist.
        """.trimIndent()
    ),
    LegalSection(
        "Beveiligingsaudit",
        """
        De beveiligingsauditfunctie analyseert uw wachtwoorden lokaal op het apparaat. Controle op veelgebruikte wachtwoorden gebeurt door vergelijking met een vooraf gedefinieerde lijst die is opgeslagen in de app zelf, zonder zoekopdrachten naar externe services.
        """.trimIndent()
    ),
    LegalSection(
        "Bewaking van datalekken (HIBP)",
        """
        KeyArmor kan controleren of uw wachtwoorden zijn verschenen in bekende datalekken via de Have I Been Pwned (HIBP)-service, beheerd door beveiligingsonderzoeker Troy Hunt.

        Deze controle maakt gebruik van een k-anonimiteitsmodel: alleen de eerste 5 tekens van een SHA-1-hash van uw wachtwoord worden naar de HIBP-API verzonden. De volledige hash wordt nooit verzonden. Het vergelijken gebeurt lokaal op uw apparaat. Er worden nooit gebruikersnamen, e-mailadressen of accountkoppelingen verstuurd. HIBP-reacties worden aangevuld met nep-hashes zodat zelfs netwerkverkeersanalyse niets onthult over het gecontroleerde wachtwoord.
        """.trimIndent()
    ),
    LegalSection(
        "Automatisch invullen-service",
        """
        KeyArmor bevat een optionele Android Automatisch invullen-service. Wanneer ingeschakeld in de systeeminstellingen, leest deze de naam van het app-pakket of de website-URL van het actieve inlogscherm om overeenkomende inloggegevens uit uw kluis voor te stellen. Dit vergelijken gebeurt volledig op het apparaat. Er worden geen app-namen, website-URL's of inloggegevens verzonden naar een externe server. De Automatisch invullen-service leest geen inhoud van andere apps buiten wat strikt noodzakelijk is om de inlogcontext te identificeren.
        """.trimIndent()
    ),
    LegalSection(
        "Live-saldo van cryptowallets",
        """
        Wanneer u een cryptowallet-item opslaat en live-saldobewaking inschakelt, raadpleegt KeyArmor openbare blockchain-knooppunten of API's om het saldo op te halen dat hoort bij het ingevoerde walletadres. Alleen het walletadres wordt verzonden — er worden geen privésleutels, seed-zinnen of persoonlijke gegevens verstuurd. Deze verzoeken gaan rechtstreeks naar publieke infrastructuur; KeyArmor beheert geen tussenliggende server.
        """.trimIndent()
    ),
    LegalSection(
        "P2P-deling van items",
        """
        KeyArmor stelt u in staat om individuele kluisitems rechtstreeks te delen met andere KeyArmor-gebruikers via een QR-code of versleutelde tekst. Deze overdracht is end-to-end versleuteld en peer-to-peer — er is geen server of tussenpersoon betrokken. U bent als enige verantwoordelijk voor de keuze met wie u items deelt en voor de beveiliging van het kanaal dat wordt gebruikt om de versleutelde tekst te verzenden (indien geen QR rechtstreeks wordt gebruikt).
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotheken van derden",
        """
        KeyArmor maakt gebruik van open-sourcebibliotheken (zie Open Source-kennisgeving). Geen van deze bibliotheken bevat telemetrie-, analyse- of gegevensverzamelingsmechanismen in de configuratie die door deze app wordt gebruikt.

        KeyArmor is ook afhankelijk van twee diensten van derden voor specifieke functies: RevenueCat, die de status van uw Premium-abonnement verifieert en alleen aankoopgerelateerde informatie ontvangt (nooit uw kluis, hoofdwachtwoord of herstelsleutel); en Brevo, die de transactionele e-mails verstuurt die vereist zijn voor Digitale Nalatenschap en het Gezinsabonnement (uitnodigingen, toestemmingsbevestigingen, meldingen) en daarvoor het e-mailadres ontvangt van de personen die bij deze functies betrokken zijn. Geen van beide diensten wordt gebruikt voor reclame of gedragsanalyse.
        """.trimIndent()
    ),
    LegalSection(
        "Startscherm-widget",
        """
        KeyArmor bevat een optionele Android-startscherm-widget die het totale aantal opgeslagen vermeldingen in uw kluis weergeeft. Deze widget heeft uitsluitend toegang tot lokale apparaatgegevens. Er wordt geen informatie naar externe servers verzonden. Het aantal vermeldingen is geaggregeerde data die de inhoud van individuele vermeldingen niet onthult. U kunt zelf kiezen of u de widget op uw startscherm plaatst.
        """.trimIndent()
    ),
    LegalSection(
        "Machtigingen",
        """
        KeyArmor vraagt alleen de machtigingen die nodig zijn voor de werking:

        • Biometrie (optioneel): voor ontgrendeling via vingerafdruk.
        • Internet (optioneel): uitsluitend gebruikt voor functies die u zelf inschakelt — Google Drive-synchronisatie, Digitale Nalatenschap en het Gezinsabonnement (via Google Firebase), live saldo-opvragen van cryptowallets, datalekcontroles via Have I Been Pwned, en abonnementsverificatie (Google Play Billing / RevenueCat).
        • Bestandstoegang (optioneel): voor het importeren/exporteren van kluizen en het toevoegen van bijlagen, alleen wanneer u dit initieert.
        • Camera (optioneel): om QR-codes te scannen bij het instellen van tweefactorauthenticatie (2FA/TOTP) of bij het ontvangen van gedeelde items. Alleen geactiveerd op uitdrukkelijk verzoek van de gebruiker.
        • Automatisch invullen-service (optioneel): handmatig ingeschakeld in de Android-systeeminstellingen. Uitsluitend gebruikt om inloggegevens voor te stellen op inlogschermen van andere apps.
        • Microfoon (optioneel): alleen voor spraakdictatie in Beveiligde notities. De spraakherkenning gebeurt op uw apparaat; KeyArmor neemt geen audio op, bewaart die niet en stuurt die naar geen enkele server.
        • Agenda, alleen lezen (optioneel): wordt door de Veilige Reismodus gebruikt om reizen in uw afspraken te vinden. De afspraken worden op uw apparaat verwerkt en nooit verzonden.
        • Meldingen en exacte alarmen (optioneel): voor beveiligingswaarschuwingen en de herinneringen die u instelt. De opstartgebeurtenis van het apparaat wordt gebruikt om ze na een herstart opnieuw in te plannen.
        • Netwerkstatus (optioneel): alleen om te weten of u mobiele data of wifi gebruikt, zodat grote Google Drive-back-ups op wifi wachten. Er wordt geen informatie over uw netwerk verzameld of verzonden.

        KeyArmor vraagt geen toegang tot contacten, locatie of andere persoonlijke gegevens die hierboven niet worden vermeld.
        """.trimIndent()
    ),
    LegalSection("Andere diensten die gegevens ontvangen", """
Naast de hierboven beschreven diensten gebruikt KeyArmor de volgende, alleen voor de functies die ze nodig hebben:

• Integriteitscontrole van de app (Firebase App Check / Google Play Integrity): Google levert een attestatie van uw apparaat zodat onze server alleen verzoeken van de echte app accepteert.
• RevenueCat: ontvangt naast aankoopgegevens de identificatiecode van uw Google-account (niet uw e-mailadres) als gebruikers-ID, om uw abonnement over apparaten heen te koppelen.
• CoinGecko: wanneer u live cryptokoersen gebruikt, worden de tickers van uw portfolio bij CoinGecko opgevraagd, dat uw IP-adres kan zien. De hierboven genoemde blockchain-explorers en -nodes kunnen dit ook.
• Google Fonts: de app downloadt de lettertypen Inter en JetBrains Mono via Google Play-services, waardoor Google uw IP-adres kan zien wanneer ze worden opgevraagd.
• Vertrouwde tijd van Google Play-services: wordt gebruikt om manipulatie van de apparaatklok te detecteren. Er worden geen persoonlijke gegevens verzonden.
• Tekstherkenning (OCR) van documenten gebeurt op uw apparaat met ML Kit. Er wordt geen afbeelding naar een server gestuurd.
• Ko-fi: alleen als u de donatielink opent; dan geldt hun eigen privacybeleid.

Onze servers kunnen uw IP-adres tijdelijk registreren om misbruik te beperken.
    """.trimIndent()),
    LegalSection(
        "Passkeys (toegangssleutels)",
        """
        KeyArmor kan fungeren als passkey-provider van Android (Credential Manager), maar alleen als u dat zelf kiest in de systeeminstellingen. Bij het aanmaken of gebruiken van een passkey leest KeyArmor het domein van de website of app die erom vraagt, om de passkey aan die website te koppelen en de uitdaging (challenge) te ondertekenen die de website verstuurt. De privésleutel wordt op uw apparaat gegenereerd en versleuteld opgeslagen samen met de rest van uw kluis; hij wordt niet naar een server of een derde verzonden. Hij verlaat uw apparaat alleen binnen de back-ups die u zelf maakt (bijvoorbeeld de versleutelde back-up op Google Drive). Om met een passkey te ondertekenen vraagt KeyArmor u uw identiteit te verifiëren met uw vingerafdruk of uw hoofdwachtwoord. De website waarop u inlogt ontvangt, zoals bij elke passkey, alleen de publieke sleutel en de handtekening.
        """.trimIndent()
    ),
    LegalSection(
        "Verwijdering en bewaring van gegevens",
        """
        Uw kluis blijft op uw apparaat (en in uw Google Drive, als u de back-up inschakelt) staan tot u hem zelf verwijdert. Op onze servers staan alleen coördinatiegegevens: het token voor pushmeldingen, geïdentificeerd via uw e-mailadres, en, als u Digitale Nalatenschap of het Gezinsabonnement gebruikt, de registraties van uitnodigingen en toestemming en het versleutelde blok van de nalatenschapskluis. Ze worden bewaard zolang u die functies gebruikt of tot u om verwijdering vraagt. U kunt ze verwijderen via Instellingen → Al mijn gegevens verwijderen, of door vanaf uw Google-account te schrijven naar support@thormakk.dev; wij verwerken die verzoeken binnen 30 dagen. De volledige details vindt u op de pagina «Account en gegevens verwijderen» op onze website.
        """.trimIndent()
    ),
    LegalSection(
        "Minderjarigen",
        """
        KeyArmor is niet bedoeld voor kinderen onder de 13 jaar (of onder de minimumleeftijd die de wet van uw land vaststelt voor digitale toestemming), en wij verzamelen niet bewust gegevens van minderjarigen. Als u denkt dat een minderjarige ons gegevens heeft verstrekt, schrijf ons dan op support@thormakk.dev en wij verwijderen ze.
        """.trimIndent()
    ),
    LegalSection(
        "Contact",
        "Voor vragen over dit beleid kunt u schrijven naar support@thormakk.dev"
    ),
    LegalSection(
        "Laatst bijgewerkt",
        "Oktober 2026 (rev. 8)"
    )
)

private fun termsNL() = listOf(
    LegalSection(
        "Algemene voorwaarden",
        ""
    ),
    LegalSection(
        "Aanvaarding",
        """
        Door KeyArmor te installeren of te gebruiken, accepteert u deze voorwaarden in hun geheel. Als u het niet eens bent met enig onderdeel, gebruik de app dan niet.
        """.trimIndent()
    ),
    LegalSection(
        "Toegestaan gebruik",
        """
        KeyArmor is ontworpen voor persoonlijk gebruik als wachtwoordbeheerder en beveiligde kluis. Gebruik voor illegale activiteiten, het opslaan van informatie die onwettige handelingen vergemakkelijkt, of pogingen om de app te reverse-engineeren is verboden.
        """.trimIndent()
    ),
    LegalSection(
        "Verantwoordelijkheid van de gebruiker",
        """
        U bent als enige verantwoordelijk voor:

        • De beveiliging van uw hoofdwachtwoord. Als dit verloren gaat, is er geen herstelmechanisme en zijn uw gegevens permanent ontoegankelijk.
        • De back-upsleutel van 12 woorden (als je die functie gebruikt). Als je deze kwijtraakt en ook je telefoon kwijtraakt, kunnen je Drive-back-ups niet meer worden geopend, zelfs niet door de ontwikkelaar.
        • Het bijhouden van actuele back-ups van uw kluis via de exportfunctie of Drive-synchronisatie.
        """.trimIndent()
    ),
    LegalSection(
        "Synchronisatiefunctionaliteit",
        """
        Google Drive-synchronisatie is een aanvullende back-upfunctie. Het vervangt goede persoonlijke beveiligingspraktijken niet. De ontwikkelaar garandeert geen continue beschikbaarheid van deze functie, omdat deze afhankelijk is van diensten van derden (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Premium-abonnementen",
        """
        KeyArmor biedt optionele betaalde abonnementen aan (Maandelijks, Jaarlijks) die extra functies ontgrendelen. Abonnementen worden uitsluitend verwerkt via Google Play Billing. Door een abonnement aan te schaffen, gaat u ook akkoord met de Servicevoorwaarden van Google Play.

        • De Maandelijkse en Jaarlijkse abonnementen worden automatisch verlengd. U kunt op elk moment opzeggen via Google Play; de opzegging gaat in aan het einde van de huidige factureringsperiode.
        • Terugbetalingen zijn onderworpen aan het standaard terugbetalingsbeleid van Google Play. De ontwikkelaar verwerkt terugbetalingen niet zelfstandig.
        • Premium-functies kunnen in de loop van de tijd veranderen. De ontwikkelaar behoudt zich het recht voor om specifieke premium-functies met redelijke kennisgeving toe te voegen, te wijzigen of te beëindigen.
        """.trimIndent()
    ),
    LegalSection(
        "Veilige reismodus",
        """
        De Veilige reismodus stelt u in staat om specifieke items als reisklaar te markeren en alle andere items te verbergen terwijl de modus actief is. U bent als enige verantwoordelijk voor het bepalen welke items u als reisklaar markeert en voor de gevolgen van het activeren of deactiveren van deze modus. De ontwikkelaar aanvaardt geen aansprakelijkheid voor items die per ongeluk zichtbaar of verborgen zijn gelaten.
        """.trimIndent()
    ),
    LegalSection(
        "Paniek-PIN",
        """
        De Paniek-PIN-functie activeert een onomkeerbare actie (wissen of verbergen van gevoelige kluisgegevens) wanneer een aangewezen pincode wordt ingevoerd. Eenmaal geactiveerd, zijn verborgen of verwijderde gegevens mogelijk niet herstelbaar afhankelijk van uw configuratie. U aanvaardt volledige verantwoordelijkheid voor de configuratie van deze functie en voor eventueel gegevensverlies als gevolg van de intentionele of onbedoelde activering ervan.
        """.trimIndent()
    ),
    LegalSection(
        "Informatie over cryptosaldo",
        """
        De live cryptosaldo-functie haalt saldo-informatie op van publieke blockchain-infrastructuur uitsluitend voor informatieve doeleinden. Deze informatie vormt geen financieel advies. De ontwikkelaar garandeert de nauwkeurigheid, volledigheid of actualiteit van saldogegevens niet. Neem geen financiële beslissingen uitsluitend op basis van informatie die in deze app wordt weergegeven.
        """.trimIndent()
    ),
    LegalSection(
        "Geen garanties",
        """
        De app wordt geleverd "zoals hij is", zonder garanties van welke aard dan ook, uitdrukkelijk of impliciet. De ontwikkelaar garandeert niet dat de app foutvrij is, zonder onderbreking werkt of geschikt is voor een specifiek doel.
        """.trimIndent()
    ),
    LegalSection(
        "Beperking van aansprakelijkheid",
        """
        Voor zover maximaal toegestaan door toepasselijk recht, is de ontwikkelaar niet aansprakelijk voor gegevensverlies, verlies van toegang tot de kluis, of enige directe, indirecte, incidentele, bijzondere of gevolgschade die voortvloeit uit het gebruik van of het niet kunnen gebruiken van de app.
        """.trimIndent()
    ),
    LegalSection(
        "Updates van de voorwaarden",
        """
        Deze voorwaarden kunnen op elk moment worden bijgewerkt. Voortgezet gebruik van de app na een gepubliceerde update houdt aanvaarding van de nieuwe voorwaarden in.
        """.trimIndent()
    ),
    LegalSection(
        "Laatst bijgewerkt",
        "Mei 2026 (rev. 2)"
    )
)

private fun legalNL() = listOf(
    LegalSection(
        "Juridische kennisgeving en disclaimer",
        ""
    ),
    LegalSection(
        "Software geleverd zoals ze is",
        """
        KeyArmor wordt gedistribueerd "zoals het is", zonder garanties van welke aard dan ook, uitdrukkelijk of impliciet, inclusief maar niet beperkt tot garanties van verkoopbaarheid, geschiktheid voor een bepaald doel of niet-inbreuk.
        """.trimIndent()
    ),
    LegalSection(
        "Beperking van aansprakelijkheid",
        """
        De ontwikkelaar van KeyArmor is onder geen enkele omstandigheid aansprakelijk voor:

        • Verlies of beschadiging van gegevens die in de app zijn opgeslagen
        • Geen toegang meer tot de kluis vanwege een vergeten of verloren hoofdwachtwoord of back-upsleutel
        • Kwetsbaarheden of storingen in Android OS-componenten
        • Storingen, onderbrekingen of wijzigingen in Google Drive-diensten
        • Enige directe, indirecte, incidentele, bijzondere of gevolgschade die voortvloeit uit het gebruik van of het niet kunnen gebruiken van de app
        • Enige schade als gevolg van ongeautoriseerde toegang tot gebruikersgegevens door derden
        • Gegevensverlies als gevolg van de intentionele of onbedoelde activering van de Paniek-PIN-functie
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive-gegevensoverdracht",
        """
Wanneer de gebruiker Google Drive-synchronisatie (back-ups) inschakelt of Digitale Nalatenschap / het Gezinsabonnement (Google Firebase) gebruikt, worden de gegevens reeds versleuteld verzonden naar de infrastructuur van Google, onder de Servicevoorwaarden van Google. In alle gevallen verlaten de gegevens het apparaat als een ondoorzichtig versleuteld blok. De ontwikkelaar heeft geen toegang tot of controle over de infrastructuur van Google en kan de verzonden inhoud niet lezen.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore en hardware-afhankelijkheid",
        """
        De versleutelingsbeveiliging van KeyArmor is gedeeltelijk afhankelijk van de Android Keystore-implementatie van het apparaat van de gebruiker. Deze implementatie varieert per fabrikant, chipset en Android-versie. De ontwikkelaar beheert of garandeert de beveiliging van hardware-implementaties van derden niet. Het wordt sterk aanbevolen het besturingssysteem van het apparaat up-to-date te houden.
        """.trimIndent()
    ),
    LegalSection(
        "Beperkingen van de Automatisch invullen-service",
        """
        De Automatisch invullen-service van KeyArmor werkt mogelijk niet correct met alle applicaties of browsers. In het bijzonder ondersteunt Google Chrome het standaard Android-framework voor automatisch invullen niet — dit is een Chrome-beperking die alle externe wachtwoordbeheerders gelijkelijk treft en buiten de controle van de ontwikkelaar valt. De ontwikkelaar garandeert niet dat de Automatisch invullen-service werkt met een specifieke applicatie.
        """.trimIndent()
    ),
    LegalSection(
        "Disclaimer live cryptosaldo",
        """
        Live saldogegevens worden opgehaald van openbare blockchain-knooppunten en API's van derden. De ontwikkelaar beheert of controleert deze knooppunten niet. Saldo-informatie wordt uitsluitend voor informatieve doeleinden verstrekt en kan vertraagd, onjuist of niet beschikbaar zijn. Deze informatie vormt geen financieel of beleggingsadvies. De ontwikkelaar aanvaardt geen aansprakelijkheid voor beslissingen genomen op basis van saldogegevens die in de app worden weergegeven.
        """.trimIndent()
    ),
    LegalSection(
        "Peer-to-peer-deling",
        """
        De P2P-delingsfunctie voor items faciliteert directe, versleutelde uitwisseling van kluisitems tussen gebruikers. De ontwikkelaar aanvaardt geen verantwoordelijkheid voor de inhoud van items die tussen gebruikers worden gedeeld, voor de identiteit of betrouwbaarheid van de ontvanger, of voor eventuele gevolgen van het delen van kluisgegevens met derden.
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotheken van derden",
        """
        KeyArmor maakt gebruik van open-sourcebibliotheken van derden die als veilig werden beschouwd op het moment van publicatie. De ontwikkelaar aanvaardt geen verantwoordelijkheid voor storingen, kwetsbaarheden of onverwacht gedrag afkomstig van componenten van derden.
        """.trimIndent()
    ),
    LegalSection(
        "Toepasselijk recht",
        """
        Deze juridische kennisgeving wordt beheerst door de geldende wetgeving in de Republiek Argentinië. Elk geschil dat voortvloeit uit het gebruik van de app zal worden voorgelegd aan de bevoegde rechtbanken van die jurisdictie.
        """.trimIndent()
    ),
    LegalSection(
        "Laatst bijgewerkt",
        "Mei 2026 (rev. 2)"
    )
)

private fun licenseNL() = listOf(
    LegalSection("Source Available-licentie", ""),
    LegalSection("Broncode uitsluitend gepubliceerd voor beveiligingsauditdoeleinden", """
De broncode van KeyArmor is uitsluitend openbaar beschikbaar zodat beveiligingsonderzoekers en gebruikers de beveiligingspraktijken ervan kunnen inspecteren en controleren. Er worden geen andere rechten verleend — alle rechten zijn voorbehouden aan de auteursrechthebbende.
    """.trimIndent()),
    LegalSection("Wat u wel mag doen", """
• De broncode lezen en inspecteren voor beveiligingsonderzoek en persoonlijke controle
• Beveiligingskwetsbaarheden melden aan het KeyArmor-team via officiële kanalen
    """.trimIndent()),
    LegalSection("Wat u niet mag doen", """
Zonder uitdrukkelijke voorafgaande schriftelijke toestemming van de auteursrechthebbende mag u niet:

• De software gebruiken of uitvoeren buiten de officiële distributie ervan
• De broncode kopiëren, reproduceren of extraheren voor een ander doel dan lezen
• Deze wijzigen, aanpassen, vertalen of er afgeleide werken van maken
• Deze verspreiden, publiceren, sublicentiëren, verkopen of anderszins aan derden beschikbaar stellen
• De broncode gebruiken als basis voor een ander product, dienst of project
• De naam, het logo of enig geassocieerd handelsmerk van KeyArmor gebruiken
    """.trimIndent()),
    LegalSection("Broncode", """
De broncode van KeyArmor is openbaar beschikbaar.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Geen garantie", """
De broncode wordt geleverd "zoals deze is", zonder enige vorm van garantie, expliciet of impliciet. De auteursrechthebbende doet geen uitspraken over de geschiktheid ervan voor een bepaald doel.
    """.trimIndent()),
    LegalSection("Auteursrecht", "© 2026 KeyArmor. Alle rechten voorbehouden. Source Available.")
)

private fun opensourceNL() = listOf(
    LegalSection(
        "Open Source-kennisgeving",
        ""
    ),
    LegalSection(
        "Gebruikte bibliotheken",
        """
        KeyArmor maakt gebruik van de volgende open-sourcebibliotheken. Tenzij anders vermeld, worden ze gedistribueerd onder de Apache 2.0-licentie.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Versie: BOM 2026.09.00
        Licentie: Apache 2.0
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
        Versie: 2.8.5
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Versie: 2.60.1
        Licentie: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Versie: 1.11.0
        Licentie: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Versie: 1.1.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Versie: 2.10.2
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Versie: 2.11.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Versie: 2.12.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Versie: 22.0.0
        Licentie: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Versie: 2.9.2
        Licentie: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Versie: v3-rev20240123-2.0.0
        Licentie: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Versie: 2.2.0
        Licentie: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Versie: 4.3.0
        Licentie: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Versie: 0.37.3
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Versie: BOM 2026.09.00
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Versie: 1.19.1
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Versie: 1.13.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Versie: 1.3.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Versie: 1.3.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Versie: 1.8.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Versie: 1.4.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Versie: 2.4.21
        Licentie: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Versie: 1.2.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Versie: 1.4.1
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Versie: 1.6.0
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Versie: 1.6.2
        Licentie: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Versie: BOM 35.1.0
        Licentie: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Versie: 2.15.0
        Licentie: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Versie: 3.0.1
        Licentie: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Versie: 2.0.27.0
        Licentie: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Versie: 1.2.1
        Licentie: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Versie: 1.86
        Licentie: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Versie: 10.29.0
        Licentie: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Versie: 5.2.0
        Licentie: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Versie: 5.19.1
        Licentie: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Versie: 1.2.1
        Licentie: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Versie: 16.0.1
        Licentie: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Versie: 16.0.1
        Licentie: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Versie: 2026-07-09
        Licentie: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Volledige licentietekst",
        """
        De volledige tekst van de Apache 2.0-licentie is beschikbaar op:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataNL() = listOf(
    LegalSection("Account en gegevens verwijderen", ""),
    LegalSection("Welke gegevens KeyArmor over u bewaart", """
Je kluis (wachtwoorden, notities, kaarten, portemonnees, bijlagen en wachtwoordgeschiedenis) wordt nooit op onze servers opgeslagen. Deze wordt versleuteld op je apparaat bewaard en, als je de back-upfunctie inschakelt, in je eigen Google Drive — niet op een plek waar wij deze kunnen lezen of verwijderen, omdat de gegevens worden versleuteld voordat ze je apparaat verlaten.

De enige gegevens die aan je Google-account zijn gekoppeld en die onze servers (Firebase) bereiken, hebben betrekking op het koppelen van dat account voor een optionele functie — Google Drive-back-up, Digitale Erfenis of het Gezinsabonnement. Alleen al het koppelen van een Google-account voor een van deze functies zorgt ervoor dat daar een minimaal record wordt geregistreerd (een token voor pushmeldingen, gekoppeld aan je e-mailadres), zelfs voordat je ‘Digitale erfenis’ of het ‘Gezinsabonnement’ configureert. Als je nog nooit een Google-account aan KeyArmor hebt gekoppeld, staat er niets over jou op onze servers dat verwijderd kan worden.
    """.trimIndent()),
    LegalSection("Verwijderen via de app", """
Je kunt al je gegevens op elk moment verwijderen via Instellingen → Al mijn gegevens verwijderen in de app. Als je nooit een Google-account aan KeyArmor hebt gekoppeld (voor een Drive-back-up, digitale erfenis of het gezinsabonnement), wordt je kluis hierdoor onmiddellijk en definitief van het apparaat gewist.

Als je wel een account hebt gekoppeld, vraagt de app je om in te loggen bij Google, zodat in één handeling zowel de bijbehorende gegevens op de server (Firebase) als de back-up- en Time Machine-kopieën die KeyArmor op je Google Drive heeft opgeslagen, kunnen worden gewist. Alleen als je niet kunt inloggen, wist de app alleen de lokale gegevens — en waarschuwt de app je van tevoren dat je gegevens op de server blijven staan totdat je ze apart verwijdert via het onderstaande verzoek.
    """.trimIndent()),
    LegalSection("Wat wordt niet verwijderd", """
Het verwijderen van je gegevens heeft geen invloed op wat zich buiten de app bevindt:

• Bestanden die je zelf hebt geëxporteerd (CSV-, PDF- of .kab-bestanden die je hebt opgeslagen of gedeeld). Deze zijn van jou en worden opgeslagen op de plek waar je ze hebt geplaatst; je moet ze dus daar verwijderen.
• Back-upkopieën die een eerdere installatie van KeyArmor in je map ‘Downloads’ heeft achtergelaten. Android staat niet meer toe dat een nieuwe installatie deze bestanden aanraakt; verwijder ze uit je map ‘Downloads’.
• Je abonnement. Dit wordt beheerd door Google Play, dus het verwijderen van je gegevens leidt niet tot opzegging; zeg het op via Google Play → Abonnementen.
    """.trimIndent()),
    LegalSection("Verzoek tot verwijdering van uw accountgegevens", """
Als je een Google-account aan KeyArmor hebt gekoppeld, maar je kunt niet inloggen om de hierboven beschreven verwijderingsfunctie in de app te gebruiken (bijvoorbeeld omdat je de app niet meer hebt geïnstalleerd), vraag dan de verwijdering van de bijbehorende gegevens op de server aan door ons een e-mail te sturen vanuit datzelfde Google-account.

Stuur een e-mail naar: support@thormakk.dev

We verwerken verwijderingsverzoeken binnen 30 dagen en bevestigen per e-mail zodra dit is voltooid. Je hoeft de app niet geïnstalleerd te hebben om dit verzoek in te dienen.
    """.trimIndent())
)
