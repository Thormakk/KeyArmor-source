package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentIT(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyIT()
    "terms"      -> termsIT()
    "legal"      -> legalIT()
    "license"    -> licenseIT()
    "opensource" -> opensourceIT()
    "delete"     -> deleteDataIT()
    else         -> listOf(LegalSection("Documento non trovato", ""))
}

private fun privacyIT() = listOf(
    LegalSection(
        "Informativa sulla Privacy",
        ""
    ),
    LegalSection(
        "Raccolta dei Dati",
        """
        KeyArmor non raccoglie, trasmette né condivide alcun dato personale a server esterni. Tutte le informazioni inserite nell'app rimangono esclusivamente sul tuo dispositivo, a meno che tu non abiliti esplicitamente la sincronizzazione con Google Drive.

        KeyArmor non raccoglie il tuo indirizzo e-mail né alcuna credenziale di account per le funzionalità principali dell'app (vault, compilazione automatica, controllo di sicurezza, monitoraggio delle violazioni) — queste funzionano interamente senza alcun account. Se attivi l'Eredità Digitale o il Piano Famiglia, queste funzionalità richiedono l'accesso con Google: il tuo indirizzo e-mail dell'account Google viene quindi archiviato su Google Firebase per identificarti e abbinarti agli eredi o ai membri della famiglia che designi, e per inviarti le relative e-mail di notifica tramite il nostro provider di posta (Brevo). Questa e-mail viene utilizzata esclusivamente per queste due funzionalità e non viene mai venduta né condivisa con nessun altro. Al di fuori dell'Eredità Digitale e del Piano Famiglia, l'unico identificatore personale è un nome o soprannome opzionale che puoi impostare durante la configurazione iniziale, il quale viene archiviato esclusivamente sul tuo dispositivo in forma cifrata.
        """.trimIndent()
    ),
    LegalSection(
        "Archiviazione Locale",
        """
        Tutti i tuoi dati (password, passkey, note, carte di credito, conti bancari, portafogli crypto, allegati e cronologia delle password) sono archiviati in forma cifrata utilizzando AES-256-GCM con una chiave derivata dalla tua password master tramite Argon2id, che non lascia mai il tuo dispositivo. Se attivi lo sblocco biometrico, una copia di quella chiave è protetta inoltre da una chiave dell'Android Keystore del tuo dispositivo. Nessun dato lascia il dispositivo senza il tuo esplicito consenso.
        """.trimIndent()
    ),
    LegalSection(
        "Password Master",
        """
        La tua password master non viene mai archiviata in testo in chiaro né in alcuna forma recuperabile. Viene utilizzata esclusivamente per derivare la chiave di cifratura del vault tramite Argon2id. Se perdi la password master, i tuoi dati non potranno essere recuperati.
        """.trimIndent()
    ),
    LegalSection(
        "Dati Biometrici",
        """
        L'autenticazione biometrica in KeyArmor è gestita interamente dal sistema operativo Android tramite la BiometricPrompt API. KeyArmor non accede, non archivia né elabora dati biometrici in nessun momento.

        La biometria viene utilizzata in due contesti all'interno dell'app:

        • Sblocco del vault: per autenticare l'accesso all'app all'avvio, disponibile su tutti i piani.
        • Protezione biometrica avanzata (Premium): una barriera opzionale che richiede la verifica biometrica prima di mostrare campi sensibili in sezioni specifiche del vault (password, carte di credito, campi personalizzati). Configurabile nelle Impostazioni — supporta la modalità per-accesso o per-sessione con timeout personalizzabile. Le stesse garanzie di privacy si applicano: KeyArmor non archivia né elabora dati biometrici.
        """.trimIndent()
    ),
    LegalSection(
        "Sincronizzazione con Google Drive",
        """
La sincronizzazione con Google Drive è del tutto facoltativa. Quando è attiva, KeyArmor:

• Richiede un unico permesso minimo (DRIVE_APPDATA) per archiviare i tuoi backup crittografati e i punti di ripristino Time Machine in una cartella privata, riservata all'app, nel tuo Google Drive. Questa cartella è nascosta — nessun'altra app, e nemmeno tu navigando in Drive, può vederla o modificarla.
• Crittografa la cassaforte con una chiave di backup composta da 12 parole prima di caricarlo. La chiave viene generata sul tuo dispositivo e solo tu ne sei in possesso: né Google né lo sviluppatore possono leggere il file o recuperare la chiave.
• Se salvi il file PDF della tua chiave di backup su Google Docs, Drive o un'altra app, lo fai autonomamente tramite la schermata di condivisione di Android; tale app è regolata dalla propria informativa sulla privacy.
• Se cambi la chiave, puoi conservare quelle precedenti, crittografate sul tuo telefono; il PDF del kit può includerle. KeyArmor non le invia ad alcun server.
• Non accede mai ad altri file di Drive, non crea mai cartelle visibili e non condivide mai file.
• Facoltativamente conserva una seconda copia locale del backup nella cartella Downloads/KeyArmor/ del dispositivo, che non lascia mai il dispositivo.
• Il backup su Google Drive, sia manuale (su richiesta) che automatico giornaliero, è disponibile nei piani Premium e Famiglia. Nel piano gratuito è possibile collegare il proprio account Google per ripristinare un backup esistente.

L'Eredità Digitale e il Piano Famiglia utilizzano Google Firebase, non il tuo Google Drive. Firebase gestisce il consenso, gli inviti e le notifiche. Il deposito dell'eredità viene crittografato end-to-end sul tuo dispositivo con la chiave pubblica dell'erede, e il blocco crittografato risultante viene caricato in un bucket privato di Firebase Cloud Storage dello sviluppatore tramite link firmati a breve durata (15 minuti). Il server conserva quel blocco ma non riceve mai la chiave dell'erede, quindi non è in grado di leggerlo.

Puoi disconnettere il tuo account Google in qualsiasi momento dalla schermata di sincronizzazione.
    """.trimIndent()
    ),
    LegalSection(
        "Allegati",
        """
        La funzione "Allegati" ti consente di associare file (fino a 5 per ogni voce, con una dimensione massima di 5 MB ciascuno) alle voci del tuo archivio. Tutti gli allegati vengono crittografati con AES-256-GCM prima di essere salvati nella memoria interna del dispositivo. Se attivi la sincronizzazione con Google Drive, i tuoi allegati e le foto dei documenti vengono inclusi nel backup che carichi, crittografati con la tua chiave di backup. Non vengono mai inviati ad alcun altro servizio.
        """.trimIndent()
    ),
    LegalSection(
        "Cronologia delle Password",
        """
        KeyArmor conserva una cronologia locale delle password precedenti per ogni voce. Questa cronologia è archiviata in forma cifrata sul dispositivo e non viene mai trasmessa a server esterni.
        """.trimIndent()
    ),
    LegalSection(
        "Notifiche in-app",
        """
        KeyArmor genera notifiche interne per avvisarti di importanti eventi di sicurezza:

        • Avvisi di scadenza: inviati quando una o più password si avvicinano alla data di scadenza.
        • Password scadute: inviate quando una o più password hanno già superato la data di scadenza.
        • Avvisi di violazione: inviati quando le tue password vengono rilevate in violazioni di dati note.

        Questi tre tipi di notifica vengono generati interamente sul tuo dispositivo e non vengono mai trasmessi a server esterni. Se usi l'Eredità Digitale o il Piano Famiglia, riceverai anche notifiche relative a queste funzionalità (un erede che accetta, un membro aggiunto alla famiglia, ecc.); queste vengono attivate da Google Firebase e recapitate tramite Firebase Cloud Messaging, poiché dipendono da eventi avviati da un altro account. Tutte le notifiche, indipendentemente dalla loro origine, vengono eliminate automaticamente dopo 30 giorni. Puoi eliminare qualsiasi notifica scorrendo lateralmente o cancellare tutto in una volta dalla schermata Notifiche. Le notifiche push richiedono l'autorizzazione di sistema per essere inviate.
        """.trimIndent()
    ),
    LegalSection(
        "Audit di Sicurezza",
        """
        La funzione di audit di sicurezza analizza le tue password localmente sul dispositivo. Il controllo delle password comuni viene eseguito confrontandole con un elenco predefinito archiviato nell'app stessa, senza alcuna query a servizi esterni.
        """.trimIndent()
    ),
    LegalSection(
        "Monitoraggio delle Violazioni (HIBP)",
        """
        KeyArmor può verificare se le tue password sono apparse in violazioni di dati note utilizzando il servizio Have I Been Pwned (HIBP), gestito dal ricercatore di sicurezza Troy Hunt.

        Questa verifica utilizza un modello di k-anonimato: solo i primi 5 caratteri di un hash SHA-1 della tua password vengono inviati all'API HIBP. L'hash completo non viene mai trasmesso. La corrispondenza viene eseguita localmente sul tuo dispositivo. Non vengono mai inviati nomi utente, indirizzi e-mail o associazioni di account. Le risposte HIBP vengono completate con hash fittizi in modo che anche l'analisi del traffico di rete non riveli nulla sulla password verificata.
        """.trimIndent()
    ),
    LegalSection(
        "Servizio di Compilazione Automatica",
        """
        KeyArmor include un servizio di Compilazione Automatica Android opzionale. Quando abilitato nelle impostazioni di sistema, legge il nome del pacchetto dell'app o l'URL del sito web della schermata di accesso attiva per suggerire credenziali corrispondenti dal tuo vault. Questa corrispondenza viene eseguita interamente sul dispositivo. Nessun nome di app, URL di sito web o credenziale viene trasmesso a server esterni. Il servizio di compilazione automatica non legge il contenuto di altre app al di là di quanto strettamente necessario per identificare il contesto di accesso.
        """.trimIndent()
    ),
    LegalSection(
        "Saldo in Tempo Reale dei Wallet Crypto",
        """
        Quando archivi una voce di wallet crypto e abiliti il monitoraggio del saldo in tempo reale, KeyArmor interroga nodi o API blockchain pubblici per recuperare il saldo associato all'indirizzo del wallet inserito. Viene trasmesso solo l'indirizzo del wallet — non vengono inviati chiavi private, frasi seed o dati personali. Queste richieste vanno direttamente all'infrastruttura pubblica; KeyArmor non gestisce alcun server intermediario.
        """.trimIndent()
    ),
    LegalSection(
        "Condivisione P2P di Voci",
        """
        KeyArmor ti permette di condividere singole voci del vault direttamente con altri utenti KeyArmor tramite codice QR o testo cifrato. Questo trasferimento è cifrato end-to-end e peer-to-peer — non è coinvolto alcun server o intermediario. Sei il solo responsabile della scelta di chi condivide le voci e della sicurezza del canale utilizzato per trasmettere il testo cifrato (se non si usa direttamente il QR).
        """.trimIndent()
    ),
    LegalSection(
        "Librerie di Terze Parti",
        """
        KeyArmor utilizza librerie open-source (vedi Avviso Open Source). Nessuna di queste librerie include meccanismi di telemetria, analisi o raccolta dati nella configurazione utilizzata da questa app.

        KeyArmor si affida anche a due servizi di terze parti per funzionalità specifiche: RevenueCat, che verifica lo stato del tuo abbonamento Premium e riceve solo informazioni relative all'acquisto (mai il tuo vault, la password master o la chiave di recupero); e Brevo, che invia le e-mail transazionali richieste dall'Eredità Digitale e dal Piano Famiglia (inviti, conferme di consenso, notifiche) e, a tale scopo, riceve l'indirizzo e-mail delle persone coinvolte in queste funzionalità. Nessuno dei due servizi viene utilizzato per pubblicità o analisi comportamentale.
        """.trimIndent()
    ),
    LegalSection(
        "Widget della schermata iniziale",
        """
        KeyArmor include un widget opzionale per la schermata iniziale di Android che mostra il numero totale di voci memorizzate nel tuo vault. Questo widget accede esclusivamente ai dati locali del dispositivo. Non trasmette alcuna informazione a server esterni. Il contatore delle voci è un dato aggregato che non rivela il contenuto delle singole voci. Puoi scegliere se posizionare o meno il widget sulla schermata iniziale.
        """.trimIndent()
    ),
    LegalSection(
        "Permessi",
        """
        KeyArmor richiede solo i permessi necessari al suo funzionamento:

        • Dati biometrici (opzionale): per lo sblocco tramite impronta digitale.
        • Internet (opzionale): utilizzato solo per le funzionalità che attivi tu stesso — sincronizzazione con Google Drive, Eredità Digitale e Piano Famiglia (tramite Google Firebase), richieste di saldo in tempo reale dei wallet crypto, verifica delle violazioni tramite Have I Been Pwned e verifica degli abbonamenti (Google Play Billing / RevenueCat).
        • Accesso ai file (opzionale): per l'importazione/esportazione del vault e per aggiungere allegati, solo quando lo avvii tu.
        • Fotocamera (opzionale): per scansionare i codici QR durante la configurazione dell'autenticazione a due fattori (2FA/TOTP) o quando si ricevono voci condivise. Attivata solo su esplicita richiesta dell'utente.
        • Servizio di compilazione automatica (opzionale): abilitato manualmente nelle impostazioni di sistema Android. Utilizzato esclusivamente per suggerire credenziali nelle schermate di accesso di altre app.
        • Microfono (facoltativo): solo per la dettatura vocale nelle Note sicure. Il riconoscimento avviene sul tuo dispositivo; KeyArmor non registra, non conserva e non invia audio ad alcun server.
        • Calendario, sola lettura (facoltativo): usato dalla Modalità Viaggio sicuro per individuare viaggi tra i tuoi eventi. Gli eventi vengono elaborati sul tuo dispositivo e non vengono mai trasmessi.
        • Notifiche e sveglie esatte (facoltativo): per gli avvisi di sicurezza e per i promemoria che programmi. L'evento di avvio del dispositivo serve a riprogrammarli dopo un riavvio.
        • Stato della rete (facoltativo): solo per sapere se usi i dati mobili o il Wi-Fi, in modo che i backup pesanti su Google Drive aspettino il Wi-Fi. Nessuna informazione sulla tua rete viene raccolta o inviata.

        KeyArmor non richiede l'accesso a contatti, posizione o altri dati personali non menzionati in precedenza.
        """.trimIndent()
    ),
    LegalSection("Altri servizi che ricevono dati", """
Oltre ai servizi descritti sopra, KeyArmor usa i seguenti, solo per le funzioni che ne hanno bisogno:

• Verifica dell'integrità dell'app (Firebase App Check / Google Play Integrity): Google fornisce un'attestazione del tuo dispositivo affinché il nostro server accetti solo richieste dell'app autentica.
• RevenueCat: oltre alle informazioni sull'acquisto, riceve l'identificatore del tuo account Google (non il tuo indirizzo e-mail) come ID utente, per collegare l'abbonamento tra i dispositivi.
• CoinGecko: quando usi i prezzi cripto in tempo reale, i ticker del tuo portafoglio vengono richiesti a CoinGecko, che può vedere il tuo indirizzo IP. Anche gli explorer e i nodi blockchain citati sopra possono vederlo.
• Google Fonts: l'app scarica i caratteri Inter e JetBrains Mono tramite i servizi Google Play, quindi Google può vedere il tuo indirizzo IP quando vengono richiesti.
• Ora affidabile dei servizi Google Play: serve a rilevare manomissioni dell'orologio del dispositivo. Non vengono inviati dati personali.
• Il riconoscimento del testo (OCR) dei documenti avviene sul tuo dispositivo con ML Kit. Nessuna immagine viene inviata a un server.
• Ko-fi: solo se apri il link per le donazioni; in tal caso vale la loro informativa sulla privacy.

I nostri server possono registrare temporaneamente il tuo indirizzo IP per limitare gli abusi.
    """.trimIndent()),
    LegalSection(
        "Passkey (chiavi di accesso)",
        """
        KeyArmor può funzionare come provider di passkey di Android (Credential Manager), solo se lo scegli tu nelle impostazioni di sistema. Quando crei o usi una passkey, KeyArmor legge il dominio del sito o dell'app che la richiede per associarla a quel sito e per firmare la sfida (challenge) che il sito invia. La chiave privata viene generata sul tuo dispositivo e salvata cifrata insieme al resto della tua cassaforte; non viene inviata a nessun server né a terze parti. Lascia il tuo dispositivo solo all'interno dei backup che crei tu (ad esempio, il backup cifrato su Google Drive). Per firmare con una passkey, KeyArmor ti chiede di verificare la tua identità con la tua impronta digitale o con la password master. Il sito a cui accedi riceve solo la chiave pubblica e la firma, come con qualsiasi passkey.
        """.trimIndent()
    ),
    LegalSection(
        "Eliminazione e Conservazione dei Dati",
        """
        La tua cassaforte resta sul tuo dispositivo (e sul tuo Google Drive, se attivi il backup) finché non la elimini tu. Sui nostri server ci sono solo dati di coordinamento: il token delle notifiche push identificato dalla tua email e, se usi l'Eredità Digitale o il Piano Famiglia, i registri di inviti e consenso e il blocco cifrato della cassaforte dell'eredità. Vengono conservati finché usi queste funzioni o finché non ne chiedi l'eliminazione. Puoi eliminarli da Impostazioni → Elimina tutti i miei dati, oppure scrivendo a support@thormakk.dev dal tuo account Google; elaboriamo queste richieste entro 30 giorni. Il dettaglio completo è nella pagina «Elimina account e dati» del nostro sito web.
        """.trimIndent()
    ),
    LegalSection(
        "Minori",
        """
        KeyArmor non è rivolta a minori di 13 anni (né all'età minima stabilita dalla legge del tuo Paese per il consenso digitale) e non raccogliamo consapevolmente dati di minori. Se ritieni che un minore ci abbia fornito dati, scrivici a support@thormakk.dev e li elimineremo.
        """.trimIndent()
    ),
    LegalSection(
        "Contatto",
        "Per qualsiasi domanda relativa a questa informativa, puoi scrivere a support@thormakk.dev"
    ),
    LegalSection(
        "Ultimo aggiornamento",
        "Novembre 2026 (rev. 9)"
    )
)

private fun termsIT() = listOf(
    LegalSection(
        "Termini e Condizioni",
        ""
    ),
    LegalSection(
        "Accettazione",
        """
        Installando o utilizzando KeyArmor accetti integralmente questi termini. Se non sei d'accordo con qualsiasi parte di essi, non utilizzare l'app.
        """.trimIndent()
    ),
    LegalSection(
        "Uso Consentito",
        """
        KeyArmor è progettato per uso personale come gestore di password e vault sicuro. È vietato utilizzarlo per attività illegali, per archiviare informazioni che facilitino atti illeciti o per tentare di eseguire il reverse engineering dell'app.
        """.trimIndent()
    ),
    LegalSection(
        "Responsabilità dell'Utente",
        """
        Sei l'unico responsabile di:

        • La sicurezza della tua password master. In caso di smarrimento, non esiste alcun meccanismo di recupero e i tuoi dati saranno permanentemente inaccessibili.
        • La chiave di backup di 12 parole (se utilizzi questa funzione). Se la perdi e perdi anche il telefono, i tuoi backup su Drive non potranno essere aperti, nemmeno dallo sviluppatore.
        • Mantenere backup aggiornati del vault tramite la funzione di esportazione o la sincronizzazione con Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Funzionalità di Sincronizzazione",
        """
        La sincronizzazione con Google Drive è una funzione di backup ausiliaria. Non sostituisce buone pratiche di sicurezza personale. Lo sviluppatore non garantisce la disponibilità continuativa di questa funzione, poiché dipende da servizi di terze parti (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Abbonamenti Premium",
        """
        KeyArmor offre piani a pagamento opzionali (Mensile, Annuale) che sbloccano funzionalità aggiuntive. Gli abbonamenti vengono elaborati esclusivamente tramite Google Play Billing. Acquistando un abbonamento, accetti anche i Termini di Servizio di Google Play.

        • I piani Mensile e Annuale sono abbonamenti a rinnovo automatico. Puoi annullare in qualsiasi momento tramite Google Play; la cancellazione ha effetto alla fine del periodo di fatturazione in corso.
        • I rimborsi sono soggetti alla politica di rimborso standard di Google Play. Lo sviluppatore non elabora rimborsi in modo indipendente.
        • Le funzionalità premium possono evolversi nel tempo. Lo sviluppatore si riserva il diritto di aggiungere, modificare o interrompere specifiche funzionalità premium con ragionevole preavviso.
        """.trimIndent()
    ),
    LegalSection(
        "Modalità Viaggio Sicuro",
        """
        La Modalità Viaggio Sicuro ti consente di contrassegnare voci specifiche come sicure per il viaggio e di nascondere tutte le altre voci mentre la modalità è attiva. Sei il solo responsabile di determinare quali voci contrassegnare come sicure per il viaggio e delle conseguenze dell'attivazione o disattivazione di questa modalità. Lo sviluppatore non si assume alcuna responsabilità per voci accidentalmente lasciate visibili o nascoste.
        """.trimIndent()
    ),
    LegalSection(
        "PIN di Panico",
        """
        La funzione PIN di Panico attiva un'azione irreversibile (eliminazione o occultamento dei dati sensibili del vault) quando viene inserito un PIN designato. Una volta attivati, i dati nascosti o eliminati potrebbero non essere recuperabili in base alla tua configurazione. Ti assumi la piena responsabilità per la configurazione di questa funzione e per qualsiasi perdita di dati derivante dalla sua attivazione intenzionale o accidentale.
        """.trimIndent()
    ),
    LegalSection(
        "Informazioni sul Saldo Crypto",
        """
        La funzione di saldo crypto in tempo reale recupera informazioni sul saldo dall'infrastruttura blockchain pubblica solo a scopo informativo. Queste informazioni non costituiscono consulenza finanziaria. Lo sviluppatore non garantisce l'accuratezza, la completezza o l'aggiornamento dei dati di saldo. Non prendere decisioni finanziarie basandoti esclusivamente sulle informazioni visualizzate in questa app.
        """.trimIndent()
    ),
    LegalSection(
        "Assenza di Garanzie",
        """
        L'app è fornita "così com'è", senza garanzie di alcun tipo, esplicite o implicite. Lo sviluppatore non garantisce che l'app sia priva di errori, che funzioni senza interruzioni o che sia adatta a uno scopo specifico.
        """.trimIndent()
    ),
    LegalSection(
        "Limitazione di Responsabilità",
        """
        Nella misura massima consentita dalla legge applicabile, lo sviluppatore non sarà responsabile per perdita di dati, perdita di accesso al vault, né per danni diretti, indiretti, incidentali, speciali o consequenziali derivanti dall'uso o dall'impossibilità di utilizzare l'app.

        Se risiedi in un Paese in cui la normativa sulla tutela dei consumatori ti garantisce diritti che non possono essere oggetto di rinuncia, i presenti termini non ne limitano la portata. Quanto previsto dai presenti termini in materia di garanzie e responsabilità si applica solo nella misura consentita dalla legge.
        """.trimIndent()
    ),
    LegalSection(
        "Aggiornamenti dei Termini",
        """
        Questi termini possono essere aggiornati in qualsiasi momento. Il proseguimento dell'utilizzo dell'app dopo un aggiornamento pubblicato costituisce accettazione dei nuovi termini.
        """.trimIndent()
    ),
    LegalSection(
        "Ultimo aggiornamento",
        "Novembre 2026 (rev. 3)"
    )
)

private fun legalIT() = listOf(
    LegalSection(
        "Avviso Legale e Dichiarazione di Non Responsabilità",
        ""
    ),
    LegalSection(
        "Software Fornito Così Com'È",
        """
        KeyArmor è distribuito "così com'è", senza garanzie di alcun tipo, esplicite o implicite, incluse ma non limitate alle garanzie di commerciabilità, idoneità a uno scopo particolare o non violazione dei diritti di terzi.
        """.trimIndent()
    ),
    LegalSection(
        "Limitazione di Responsabilità",
        """
        Lo sviluppatore di KeyArmor non sarà in nessun caso responsabile per:

        • Perdita o corruzione dei dati archiviati nell'app
        • Impossibilità di accedere al vault a causa della dimenticanza o dello smarrimento della password principale o della chiave di backup
        • Vulnerabilità o guasti nei componenti del sistema operativo Android
        • Guasti, interruzioni o modifiche nei servizi di Google Drive
        • Danni diretti, indiretti, incidentali, speciali o consequenziali derivanti dall'uso o dall'impossibilità di utilizzare l'app
        • Danni derivanti da accesso non autorizzato ai dati dell'utente da parte di terzi
        • Perdita di dati derivante dall'attivazione intenzionale o accidentale della funzione PIN di Panico
        """.trimIndent()
    ),
    LegalSection(
        "Trasmissione Dati a Google Drive",
        """
Quando l'utente attiva la sincronizzazione con Google Drive (backup) o usa l'Eredità Digitale / il Piano Famiglia (Google Firebase), i dati vengono trasmessi già crittografati all'infrastruttura di Google secondo i Termini di servizio di Google. In ogni caso i dati lasciano il dispositivo come blob crittografato opaco. Lo sviluppatore non ha accesso né controllo sull'infrastruttura di Google e non può leggere i contenuti trasmessi.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore e Dipendenza dall'Hardware",
        """
        La sicurezza della cifratura di KeyArmor dipende in parte dall'implementazione dell'Android Keystore del dispositivo dell'utente. Tale implementazione varia in base al produttore, al chipset e alla versione di Android. Lo sviluppatore non controlla né garantisce la sicurezza delle implementazioni hardware di terze parti. Si raccomanda vivamente di mantenere aggiornato il sistema operativo del dispositivo.
        """.trimIndent()
    ),
    LegalSection(
        "Limitazioni del Servizio di Compilazione Automatica",
        """
        Il servizio di Compilazione Automatica di KeyArmor potrebbe non funzionare correttamente con tutte le applicazioni o i browser. In particolare, Google Chrome non supporta il framework standard di compilazione automatica di Android — si tratta di una limitazione di Chrome che riguarda allo stesso modo tutti i gestori di password di terze parti ed è al di fuori del controllo dello sviluppatore. Lo sviluppatore non garantisce che il servizio di compilazione automatica funzioni con una specifica applicazione.
        """.trimIndent()
    ),
    LegalSection(
        "Disclaimer sul Saldo Crypto in Tempo Reale",
        """
        I dati di saldo in tempo reale vengono recuperati da nodi e API blockchain pubblici di terze parti. Lo sviluppatore non gestisce né controlla questi nodi. Le informazioni sul saldo vengono fornite solo a scopo informativo e potrebbero essere ritardate, imprecise o non disponibili. Queste informazioni non costituiscono consulenza finanziaria o d'investimento. Lo sviluppatore non si assume alcuna responsabilità per le decisioni prese sulla base dei dati di saldo visualizzati nell'app.
        """.trimIndent()
    ),
    LegalSection(
        "Condivisione Peer-to-Peer",
        """
        La funzione di condivisione P2P di voci facilita lo scambio diretto e cifrato di voci del vault tra utenti. Lo sviluppatore non si assume alcuna responsabilità per il contenuto delle voci condivise tra utenti, per l'identità o l'affidabilità del destinatario, o per le conseguenze derivanti dalla condivisione di dati del vault con terze parti.
        """.trimIndent()
    ),
    LegalSection(
        "Librerie di Terze Parti",
        """
        KeyArmor utilizza librerie open-source di terze parti considerate sicure al momento della pubblicazione. Lo sviluppatore non si assume alcuna responsabilità per guasti, vulnerabilità o comportamenti imprevisti originati da componenti di terze parti.
        """.trimIndent()
    ),
    LegalSection(
        "Legge Applicabile",
        """
        Il presente avviso legale è disciplinato dalle leggi vigenti nella Repubblica Argentina. Qualsiasi controversia derivante dall'uso dell'app sarà sottoposta ai tribunali competenti di tale giurisdizione.

        Se risiedi in un Paese in cui la normativa sulla tutela dei consumatori ti garantisce diritti che non possono essere oggetto di rinuncia, il presente avviso non li limita. Quanto indicato nel presente avviso in merito a garanzie, responsabilità, legge applicabile e foro competente si applica solo nella misura consentita dalla legge.
        """.trimIndent()
    ),
    LegalSection(
        "Ultimo aggiornamento",
        "Novembre 2026 (rev. 3)"
    )
)

private fun licenseIT() = listOf(
    LegalSection("Licenza Source Available", ""),
    LegalSection("Codice Sorgente Pubblicato Solo per Scopi di Audit di Sicurezza", """
Il codice sorgente di KeyArmor è reso pubblico esclusivamente affinché ricercatori di sicurezza e utenti possano ispezionare e verificare le sue pratiche di sicurezza. Non viene concesso alcun diritto oltre a questo — tutti i diritti sono riservati al titolare del copyright.
    """.trimIndent()),
    LegalSection("Cosa Puoi Fare", """
• Leggere e ispezionare il codice sorgente per ricerca sulla sicurezza e audit personale
• Segnalare vulnerabilità di sicurezza al team KeyArmor tramite i canali ufficiali
    """.trimIndent()),
    LegalSection("Cosa Non Puoi Fare", """
Senza previa autorizzazione scritta esplicita del titolare del copyright, non puoi:

• Usare o eseguire il software al di fuori della sua distribuzione ufficiale
• Copiare, riprodurre o estrarre il codice sorgente per scopi diversi dalla lettura
• Modificarlo, adattarlo, tradurlo o creare opere derivate basate su di esso
• Distribuirlo, pubblicarlo, sublicenziarlo, venderlo o renderlo altrimenti disponibile a terzi
• Usare il codice sorgente come base per un altro prodotto, servizio o progetto
• Usare il nome, il logo o qualsiasi marchio associato a KeyArmor
    """.trimIndent()),
    LegalSection("Codice Sorgente", """
Il codice sorgente di KeyArmor è disponibile pubblicamente.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Nessuna Garanzia", """
Il codice sorgente è fornito "così com'è", senza garanzia di alcun tipo, espressa o implicita. Il titolare del copyright non fornisce alcuna dichiarazione riguardo alla sua idoneità per uno scopo particolare.
    """.trimIndent()),
    LegalSection("Copyright", "© 2026 KeyArmor. Tutti i diritti riservati. Source Available.")
)

private fun opensourceIT() = listOf(
    LegalSection(
        "Avviso Open Source",
        ""
    ),
    LegalSection(
        "Librerie Utilizzate",
        """
        KeyArmor utilizza le seguenti librerie open-source. Salvo diversa indicazione, sono distribuite con licenza Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Versione: BOM 2026.09.00 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Versione: 1.2.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Versione: 2.8.5 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Versione: 2.60.1 | Licenza: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Versione: 1.11.0 | Licenza: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Versione: 1.1.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Versione: 2.10.2 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Versione: 2.11.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Versione: 2.12.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Versione: 22.0.0 | Licenza: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Versione: 2.9.2 | Licenza: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Versione: v3-rev20240123-2.0.0 | Licenza: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Versione: 2.2.0 | Licenza: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Versione: 4.3.0 | Licenza: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Versione: 0.37.3 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Versione: BOM 2026.09.00 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Versione: 1.19.1 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Versione: 1.13.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Versione: 1.3.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Versione: 1.3.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Versione: 1.8.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Versione: 1.4.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Versione: 2.4.21 | Licenza: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Versione: 1.2.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Versione: 1.4.1 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Versione: 1.6.0 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Versione: 1.6.2 | Licenza: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Versione: BOM 35.1.0 | Licenza: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Versione: 2.15.0 | Licenza: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Versione: 3.0.1 | Licenza: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Versione: 2.0.27.0 | Licenza: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Versione: 1.2.1 | Licenza: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Versione: 1.86 | Licenza: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Versione: 10.29.0 | Licenza: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Versione: 5.2.0 | Licenza: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Versione: 5.19.1 | Licenza: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Versione: 1.2.1 | Licenza: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Versione: 16.0.1 | Licenza: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Versione: 16.0.1 | Licenza: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Versione: 2026-07-09 | Licenza: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Testo Completo della Licenza Apache 2.0",
        """
        Il testo completo della licenza Apache 2.0 è disponibile all'indirizzo:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataIT() = listOf(
    LegalSection("Elimina account e dati", ""),
    LegalSection("Quali dati su di te vengono conservati da KeyArmor", """
Il tuo archivio (password, note, schede, portafogli, allegati e cronologia delle password) non viene mai memorizzato sui nostri server. Rimane crittografato sul tuo dispositivo e, se attivi il backup, sul tuo Google Drive — in un luogo dove non possiamo né leggerlo né cancellarlo, poiché viene crittografato prima ancora di lasciare il tuo dispositivo.

Gli unici dati associati al tuo account Google che raggiungono i nostri server (Firebase) sono legati al collegamento di quell’account per una funzionalità opzionale: il backup su Google Drive, l’Eredità digitale o il Piano Famiglia. Il semplice collegamento di un account Google per una qualsiasi di queste funzionalità comporta la registrazione di un dato minimo su Firebase (un token di notifica push, associato al tuo indirizzo e-mail), anche prima che tu configuri l’Eredità digitale o il Piano Famiglia. Se non hai mai collegato un account Google a KeyArmor, sui nostri server non c’è nulla che ti riguardi da cancellare.
    """.trimIndent()),
    LegalSection("Eliminazione dall'app", """
Puoi eliminare tutti i tuoi dati in qualsiasi momento da Impostazioni → Elimina tutti i miei dati all’interno dell’app. Se non hai mai collegato un account Google a KeyArmor (per il backup su Drive, l’eredità digitale o il Piano Famiglia), questa operazione cancellerà immediatamente e definitivamente il tuo vault dal dispositivo.

Se invece ne hai collegato uno, l’app ti chiederà di effettuare l’accesso con Google in modo che, con la stessa operazione, possa cancellare i record associati sul lato server (Firebase) e le copie di backup e Time Machine che KeyArmor ha salvato nel tuo Google Drive. Solo se non riesci ad accedere, l’app procederà alla cancellazione dei soli dati locali — e ti avviserà in anticipo che i tuoi record sul lato server rimarranno fino a quando non verranno rimossi separatamente, utilizzando la richiesta riportata di seguito.
    """.trimIndent()),
    LegalSection("Cosa non viene eliminato", """
L'eliminazione dei tuoi dati non rimuove ciò che si trova al di fuori dell'app:

• I file che hai esportato tu stesso (file CSV, PDF o .kab che hai salvato o condiviso). Sono di tua proprietà e sono archiviati dove li hai salvati, quindi devi eliminarli da lì.
• Le copie di backup che una precedente installazione di KeyArmor ha lasciato nella cartella "Download". Android non consente più a una nuova installazione di modificarle; eliminale dalla cartella "Download".
• Il tuo abbonamento. È gestito da Google Play, quindi l'eliminazione dei dati non lo cancella; cancellalo in Google Play → Abbonamenti.
    """.trimIndent()),
    LegalSection("Richiesta di cancellazione dei dati del proprio account", """
Se hai collegato un account Google a KeyArmor ma non riesci ad accedere per utilizzare la funzione di cancellazione dall'app sopra indicata (ad esempio, perché non hai più l'app installata), richiedi la cancellazione dei dati associati presenti sul server inviandoci un'e-mail da quello stesso account Google.

Scrivi a: support@thormakk.dev

Elaboriamo le richieste di cancellazione entro 30 giorni e inviamo una conferma via e-mail una volta completata la procedura. Non è necessario avere l'app installata per effettuare questa richiesta.
    """.trimIndent())
)
