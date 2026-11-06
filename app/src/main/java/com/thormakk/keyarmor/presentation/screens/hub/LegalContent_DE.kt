package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentDE(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyDE()
    "terms"      -> termsDE()
    "legal"      -> legalDE()
    "license"    -> licenseDE()
    "opensource" -> opensourceDE()
    "delete"     -> deleteDataDE()
    else         -> listOf(LegalSection("Dokument nicht gefunden", ""))
}

private fun privacyDE() = listOf(
    LegalSection(
        "Datenschutzrichtlinie",
        ""
    ),
    LegalSection(
        "Datenerhebung",
        """
        KeyArmor erhebt, überträgt oder teilt keinerlei personenbezogene Daten mit externen Servern. Alle Informationen, die Sie in der App eingeben, verbleiben ausschließlich auf Ihrem Gerät, es sei denn, Sie aktivieren ausdrücklich die Google-Drive-Synchronisierung.

        KeyArmor erhebt weder Ihre E-Mail-Adresse noch Kontoanmeldedaten für die Kernfunktionen der App (Tresor, Autofill, Sicherheitsüberprüfung, Sicherheitsverletzungsüberwachung) — diese funktionieren vollständig ohne jegliches Konto. Wenn Sie das Digitale Erbe oder den Familienplan aktivieren, erfordern diese Funktionen eine Anmeldung mit Google: Ihre Google-Konto-E-Mail-Adresse wird dann in Google Firebase gespeichert, um Sie zu identifizieren und mit den von Ihnen bestimmten Erben oder Familienmitgliedern zu verknüpfen, sowie um Ihnen die entsprechenden Benachrichtigungs-E-Mails über unseren E-Mail-Anbieter (Brevo) zu senden. Diese E-Mail-Adresse wird ausschließlich für diese beiden Funktionen verwendet und niemals verkauft oder mit Dritten geteilt. Außerhalb des Digitalen Erbes und des Familienplans ist das einzige persönliche Identifikationsmerkmal ein optionaler Name oder Spitzname, den Sie bei der Einrichtung festlegen können; dieser wird ausschließlich auf Ihrem Gerät in verschlüsselter Form gespeichert.
        """.trimIndent()
    ),
    LegalSection(
        "Lokale Speicherung",
        """
        Alle Ihre Daten (Passwörter, Passkeys, Notizen, Kreditkarten, Bankkonten, Krypto-Wallets, Anhänge und Passwortverlauf) werden verschlüsselt mit AES-256-GCM gespeichert, mit einem Schlüssel, der mittels Argon2id aus Ihrem Master-Passwort abgeleitet wird und Ihr Gerät niemals verlässt. Wenn Sie die biometrische Entsperrung aktivieren, wird zusätzlich eine Kopie dieses Schlüssels mit einem Schlüssel des Android Keystore Ihres Geräts geschützt. Keine Daten verlassen Ihr Gerät ohne Ihre ausdrückliche Zustimmung.
        """.trimIndent()
    ),
    LegalSection(
        "Master-Passwort",
        """
        Ihr Master-Passwort wird niemals im Klartext oder in einer wiederherstellbaren Form gespeichert. Es wird ausschließlich dazu verwendet, den Tresor-Verschlüsselungsschlüssel über Argon2id abzuleiten. Wenn Sie Ihr Master-Passwort verlieren, können Ihre Daten nicht wiederhergestellt werden.
        """.trimIndent()
    ),
    LegalSection(
        "Biometrie",
        """
        Die biometrische Authentifizierung in KeyArmor wird vollständig vom Android-Betriebssystem über die BiometricPrompt-API verwaltet. KeyArmor greift zu keinem Zeitpunkt auf biometrische Daten zu, speichert sie oder verarbeitet sie.

        Die Biometrie wird in zwei Kontexten innerhalb der App verwendet:

        • Tresor-Entsperrung: zur Authentifizierung des App-Zugriffs beim Start, in allen Tarifen verfügbar.
        • Erweiterte biometrische Schutzfunktion (Premium): eine optionale Barriere, die eine biometrische Verifizierung erfordert, bevor sensible Felder in bestimmten Tresor-Bereichen (Passwörter, Kreditkarten, benutzerdefinierte Felder) angezeigt werden. In den Einstellungen konfigurierbar — unterstützt den Pro-Zugriff- oder Sitzungsmodus mit anpassbarem Timeout. Die gleichen Datenschutzgarantien gelten: Es werden keine biometrischen Daten von KeyArmor gespeichert oder verarbeitet.
        """.trimIndent()
    ),
    LegalSection(
        "Google-Drive-Synchronisierung",
        """
Die Google-Drive-Synchronisierung ist vollständig optional. Wenn sie aktiviert ist, führt KeyArmor Folgendes durch:

• Es wird eine einzige, minimale Berechtigung (DRIVE_APPDATA) angefordert, um Ihre eigenen verschlüsselten Sicherungen und Time-Machine-Wiederherstellungspunkte in einem privaten, app-eigenen Ordner in Ihrem Google Drive zu speichern. Dieser Ordner ist verborgen — keine andere App und auch Sie selbst beim Durchsuchen von Drive können ihn sehen oder ändern.
• Verschlüsselt den Tresor vor dem Hochladen mit einem aus 12 Wörtern bestehenden Sicherungsschlüssel. Der Schlüssel wird auf Ihrem Gerät erstellt und nur Sie verfügen darüber: Weder Google noch der Entwickler können die Datei lesen oder den Schlüssel wiederherstellen.
• Wenn Sie die PDF-Datei Ihres Sicherungsschlüssels in Google Docs, Drive oder einer anderen App speichern, tun Sie dies selbst über den Android-Freigabebildschirm; für diese App gelten deren eigene Datenschutzbestimmungen.
• Wenn Sie Ihren Schlüssel ändern, können Sie die bisherigen Schlüssel verschlüsselt auf Ihrem Smartphone behalten; die PDF-Datei des Kits kann sie enthalten. KeyArmor sendet sie an keinen Server.
• Es wird niemals auf andere Drive-Dateien zugegriffen, es werden keine sichtbaren Ordner erstellt und keine Dateien geteilt.
• Optional wird eine zweite lokale Kopie der Sicherung im Ordner Downloads/KeyArmor/ Ihres Geräts gespeichert, die das Gerät nie verlässt.
• Die Google-Drive-Sicherung – sowohl manuell (auf Ihren Wunsch) als auch automatisch täglich – ist in Premium und im Familienplan verfügbar. Im kostenlosen Plan können Sie Ihr Google-Konto verknüpfen, um eine vorhandene Sicherung wiederherzustellen.

Digitales Erbe und der Familienplan nutzen Google Firebase, nicht Ihr Google Drive. Firebase koordiniert die Einwilligungen, Einladungen und Benachrichtigungen. Der Erbschafts-Tresor wird auf Ihrem Gerät mit dem öffentlichen Schlüssel des Erben Ende-zu-Ende verschlüsselt, und der resultierende verschlüsselte Block wird über kurzlebige (15 Minuten) signierte Links in einen privaten Firebase Cloud Storage-Bucket des Entwicklers hochgeladen. Der Server speichert diesen Block, erhält jedoch niemals den Schlüssel des Erben und kann ihn daher nicht lesen.

Sie können Ihr Google-Konto jederzeit über den Synchronisierungsbildschirm trennen.
    """.trimIndent()
    ),
    LegalSection(
        "Anhänge",
        """
        Mit der Anhangsfunktion können Sie Ihren Tresoreinträgen Dateien (bis zu 5 pro Eintrag, jeweils maximal 5 MB) zuordnen. Alle Anhänge werden mit AES-256-GCM verschlüsselt, bevor sie im internen Speicher des Geräts gespeichert werden. Wenn Sie die Google Drive-Synchronisierung aktivieren, werden Ihre Anhänge und Dokumentfotos in das von Ihnen hochgeladene Backup aufgenommen und mit Ihrem Sicherungsschlüssel verschlüsselt. Sie werden niemals an einen anderen Dienst gesendet.
        """.trimIndent()
    ),
    LegalSection(
        "Passwortverlauf",
        """
        KeyArmor führt einen lokalen Verlauf früherer Passwörter für jeden Eintrag. Dieser Verlauf wird verschlüsselt auf dem Gerät gespeichert und niemals an einen externen Server übertragen.
        """.trimIndent()
    ),
    LegalSection(
        "In-App-Benachrichtigungen",
        """
        KeyArmor generiert interne Benachrichtigungen, um Sie über wichtige Sicherheitsereignisse zu informieren:

        • Ablaufwarnungen: werden gesendet, wenn eines oder mehrere Passwörter sich ihrem Ablaufdatum nähern.
        • Abgelaufene Passwörter: werden gesendet, wenn eines oder mehrere Passwörter ihr Ablaufdatum bereits überschritten haben.
        • Sicherheitsverletzungswarnungen: werden gesendet, wenn Ihre Passwörter in bekannten Datenverletzungen erkannt wurden.

        Diese drei Benachrichtigungsarten werden vollständig auf Ihrem Gerät erzeugt und niemals an externe Server übertragen. Wenn Sie das Digitale Erbe oder den Familienplan nutzen, erhalten Sie außerdem Benachrichtigungen zu diesen Funktionen (ein Erbe nimmt an, ein Familienmitglied wird hinzugefügt usw.); diese werden von Google Firebase ausgelöst und über Firebase Cloud Messaging zugestellt, da sie von Ereignissen abhängen, die von einem anderen Konto ausgehen. Alle Benachrichtigungen werden unabhängig von ihrer Herkunft nach 30 Tagen automatisch gelöscht. Sie können jede Benachrichtigung durch Seitwärtswischen löschen oder alle auf einmal über den Benachrichtigungsbildschirm entfernen. Für Push-Benachrichtigungen ist eine Systemberechtigung erforderlich.
        """.trimIndent()
    ),
    LegalSection(
        "Sicherheitsüberprüfung",
        """
        Die Sicherheitsüberprüfungsfunktion analysiert Ihre Passwörter lokal auf dem Gerät. Die Überprüfung auf häufige Passwörter erfolgt durch Abgleich mit einer vordefinierten Liste, die in der App selbst gespeichert ist, ohne Abfragen an externe Dienste.
        """.trimIndent()
    ),
    LegalSection(
        "Sicherheitsverletzungsüberwachung (HIBP)",
        """
        KeyArmor kann überprüfen, ob Ihre Passwörter in bekannten Datenschutzverletzungen aufgetaucht sind, indem der Dienst Have I Been Pwned (HIBP) des Sicherheitsforschers Troy Hunt genutzt wird.

        Diese Prüfung verwendet ein k-Anonymitäts-Modell: Nur die ersten 5 Zeichen eines SHA-1-Hashes Ihres Passworts werden an die HIBP-API übermittelt. Der vollständige Hash wird niemals übertragen. Der Abgleich erfolgt lokal auf Ihrem Gerät. Es werden keine Benutzernamen, E-Mail-Adressen oder Kontozuordnungen übermittelt. Die HIBP-Antworten werden mit Dummy-Hashes aufgefüllt, sodass selbst eine Netzwerkverkehrsanalyse nichts über das geprüfte Passwort verrät.
        """.trimIndent()
    ),
    LegalSection(
        "Autofill-Dienst",
        """
        KeyArmor enthält einen optionalen Android-Autofill-Dienst. Wenn er in den Systemeinstellungen aktiviert ist, liest er den App-Paketnamen oder die Website-URL des aktuell aktiven Anmeldebildschirms, um passende Anmeldedaten aus Ihrem Tresor vorzuschlagen. Dieser Abgleich erfolgt vollständig auf dem Gerät. Es werden keine App-Namen, Website-URLs oder Anmeldedaten an einen externen Server übertragen. Der Autofill-Dienst liest keine Inhalte anderer Apps über das für die Identifizierung des Anmeldekontexts unbedingt Erforderliche hinaus.
        """.trimIndent()
    ),
    LegalSection(
        "Krypto-Wallet Live-Saldo",
        """
        Wenn Sie einen Krypto-Wallet-Eintrag speichern und die Live-Saldo-Überwachung aktivieren, fragt KeyArmor öffentliche Blockchain-Knoten oder -APIs ab, um den Saldo der eingegebenen Wallet-Adresse abzurufen. Es wird nur die Wallet-Adresse übertragen — keine privaten Schlüssel, Seed-Phrasen oder persönlichen Daten werden gesendet. Diese Abfragen gehen direkt an öffentliche Infrastruktur; KeyArmor betreibt keinen Zwischenserver.
        """.trimIndent()
    ),
    LegalSection(
        "P2P-Eintragsfreigabe",
        """
        KeyArmor ermöglicht es Ihnen, einzelne Tresoreinträge direkt mit anderen KeyArmor-Nutzern über einen QR-Code oder verschlüsselten Text zu teilen. Diese Übertragung ist Ende-zu-Ende-verschlüsselt und peer-to-peer — kein Server oder Vermittler ist beteiligt. Sie sind allein verantwortlich für die Wahl, mit wem Sie Einträge teilen, und für die Sicherheit des Kanals, der zur Übertragung des verschlüsselten Textes verwendet wird (sofern kein QR direkt verwendet wird).
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotheken von Drittanbietern",
        """
        KeyArmor verwendet Open-Source-Bibliotheken (siehe Open-Source-Hinweis). Keine dieser Bibliotheken enthält Telemetrie-, Analyse- oder Datenerhebungsmechanismen in der von dieser App verwendeten Konfiguration.

        KeyArmor verlässt sich außerdem für bestimmte Funktionen auf zwei Drittanbieterdienste: RevenueCat, das Ihren Premium-Abonnementstatus überprüft und ausschließlich kaufbezogene Informationen erhält (niemals Ihren Tresor, Ihr Master-Passwort oder Ihren Wiederherstellungsschlüssel); und Brevo, das die für das Digitale Erbe und den Familienplan erforderlichen Transaktions-E-Mails versendet (Einladungen, Zustimmungsbestätigungen, Benachrichtigungen) und dafür die E-Mail-Adresse der an diesen Funktionen beteiligten Personen erhält. Keiner der beiden Dienste wird für Werbung oder Verhaltensanalyse genutzt.
        """.trimIndent()
    ),
    LegalSection(
        "Startbildschirm-Widget",
        """
        KeyArmor enthält ein optionales Android-Startbildschirm-Widget, das die Gesamtzahl der in Ihrem Tresor gespeicherten Einträge anzeigt. Dieses Widget greift ausschließlich auf lokale Gerätedaten zu. Es überträgt keine Informationen an externe Server. Die Eintragsanzahl ist ein aggregierter Wert, der den Inhalt einzelner Einträge nicht preisgibt. Sie können selbst entscheiden, ob Sie das Widget auf Ihrem Startbildschirm platzieren möchten.
        """.trimIndent()
    ),
    LegalSection(
        "Berechtigungen",
        """
        KeyArmor fordert nur die für seinen Betrieb notwendigen Berechtigungen an:

        • Biometrie (optional): zur Entsperrung per Fingerabdruck.
        • Internet (optional): wird nur für Funktionen verwendet, die Sie selbst aktivieren — Google-Drive-Synchronisierung, Digitales Erbe und Familienplan (über Google Firebase), Live-Saldoabfragen für Krypto-Wallets, Sicherheitsverletzungsprüfungen über Have I Been Pwned sowie Abonnementüberprüfung (Google Play Billing / RevenueCat).
        • Dateizugriff (optional): für den Import/Export des Tresors und das Hinzufügen von Anhängen, nur wenn Sie dies initiieren.
        • Kamera (optional): zum Scannen von QR-Codes beim Einrichten der Zwei-Faktor-Authentifizierung (2FA/TOTP) oder beim Empfangen geteilter Einträge. Wird nur auf ausdrückliche Benutzeranforderung aktiviert.
        • Autofill-Dienst (optional): wird manuell in den Android-Systemeinstellungen aktiviert. Dient ausschließlich dazu, Anmeldedaten auf Anmeldebildschirmen anderer Apps vorzuschlagen.
        • Mikrofon (optional): nur für die Sprachdiktat-Funktion in Sicheren Notizen. Die Spracherkennung läuft auf Ihrem Gerät; KeyArmor nimmt keine Audiodaten auf, speichert sie nicht und sendet sie an keinen Server.
        • Kalender, nur lesend (optional): wird vom Sicheren Reisemodus genutzt, um Reisen in Ihren Terminen zu erkennen. Die Termine werden auf Ihrem Gerät verarbeitet und nie übertragen.
        • Benachrichtigungen und exakte Alarme (optional): für Sicherheitswarnungen und die von Ihnen geplanten Erinnerungen. Das Boot-Ereignis des Geräts dient dazu, sie nach einem Neustart neu zu planen.
        • Netzwerkstatus (optional): nur um zu erkennen, ob Sie mobile Daten oder WLAN nutzen, damit große Google-Drive-Backups auf WLAN warten. Es werden keine Informationen über Ihr Netzwerk erhoben oder gesendet.

        KeyArmor fordert keinen Zugriff auf Kontakte, Standort oder andere oben nicht genannte personenbezogene Daten an.
        """.trimIndent()
    ),
    LegalSection("Weitere Dienste, die Daten erhalten", """
Zusätzlich zu den oben beschriebenen Diensten nutzt KeyArmor die folgenden, ausschließlich für die Funktionen, die sie benötigen:

• Integritätsprüfung der App (Firebase App Check / Google Play Integrity): Google liefert eine Bestätigung Ihres Geräts, damit unser Server nur Anfragen der echten App akzeptiert.
• RevenueCat: erhält zusätzlich zu den Kaufinformationen die Kennung Ihres Google-Kontos (nicht Ihre E-Mail-Adresse) als Nutzer-ID, um Ihr Abonnement geräteübergreifend zuzuordnen.
• CoinGecko: Wenn Sie Live-Kryptopreise nutzen, werden die Ticker Ihres Portfolios bei CoinGecko abgefragt, das Ihre IP-Adresse sehen kann. Die oben genannten Blockchain-Explorer und -Knoten können sie ebenfalls sehen.
• Google Fonts: Die App lädt ihre Schriftarten Inter und JetBrains Mono über die Google-Play-Dienste herunter; Google kann dabei Ihre IP-Adresse sehen.
• Vertrauenswürdige Zeit der Google-Play-Dienste: dient dazu, Manipulationen der Geräteuhr zu erkennen. Es werden keine personenbezogenen Daten gesendet.
• Die Texterkennung (OCR) von Dokumenten läuft mit ML Kit auf Ihrem Gerät. Es wird kein Bild an einen Server gesendet.
• Ko-fi: nur wenn Sie den Spendenlink öffnen; dann gilt deren eigene Datenschutzerklärung.

Unsere Server können Ihre IP-Adresse vorübergehend protokollieren, um Missbrauch zu begrenzen.
    """.trimIndent()),
    LegalSection(
        "Passkeys (Zugangsschlüssel)",
        """
        KeyArmor kann als Passkey-Anbieter von Android (Credential Manager) fungieren, aber nur, wenn Sie dies selbst in den Systemeinstellungen auswählen. Beim Erstellen oder Verwenden eines Passkeys liest KeyArmor die Domain der Website oder App, die ihn anfordert, um den Passkey dieser Website zuzuordnen und die von der Website gesendete Challenge zu signieren. Der private Schlüssel wird auf Ihrem Gerät erzeugt und verschlüsselt zusammen mit dem Rest Ihres Tresors gespeichert; er wird weder an einen Server noch an Dritte gesendet. Er verlässt Ihr Gerät nur innerhalb der Backups, die Sie selbst erstellen (zum Beispiel das verschlüsselte Backup in Google Drive). Zum Signieren mit einem Passkey verlangt KeyArmor, dass Sie sich per Fingerabdruck oder mit Ihrem Master-Passwort verifizieren. Die Website, bei der Sie sich anmelden, erhält wie bei jedem Passkey nur den öffentlichen Schlüssel und die Signatur.
        """.trimIndent()
    ),
    LegalSection(
        "Löschung und Aufbewahrung von Daten",
        """
        Ihr Tresor bleibt auf Ihrem Gerät (und in Ihrem Google Drive, wenn Sie das Backup aktivieren), bis Sie ihn selbst löschen. Auf unseren Servern liegen nur Koordinationsdaten: das Push-Benachrichtigungs-Token, das über Ihre E-Mail-Adresse identifiziert wird, und, wenn Sie das Digitale Erbe oder den Familienplan nutzen, die Einladungs- und Einwilligungsprotokolle sowie der verschlüsselte Block des Erbe-Tresors. Sie werden aufbewahrt, solange Sie diese Funktionen nutzen oder bis Sie deren Löschung verlangen. Sie können sie unter Einstellungen → Alle meine Daten löschen entfernen oder indem Sie von Ihrem Google-Konto aus an support@thormakk.dev schreiben; wir bearbeiten solche Anfragen innerhalb von 30 Tagen. Alle Details finden Sie auf der Seite «Konto und Daten löschen» auf unserer Website.
        """.trimIndent()
    ),
    LegalSection(
        "Minderjährige",
        """
        KeyArmor richtet sich nicht an Kinder unter 13 Jahren (oder unter dem Mindestalter, das das Recht Ihres Landes für die digitale Einwilligung vorsieht), und wir erheben nicht wissentlich Daten von Minderjährigen. Wenn Sie glauben, dass ein Minderjähriger uns Daten übermittelt hat, schreiben Sie uns an support@thormakk.dev, und wir löschen sie.
        """.trimIndent()
    ),
    LegalSection(
        "Kontakt",
        "Bei Fragen zu dieser Richtlinie können Sie an support@thormakk.dev schreiben"
    ),
    LegalSection(
        "Zuletzt aktualisiert",
        "November 2026 (Rev. 9)"
    )
)

private fun termsDE() = listOf(
    LegalSection(
        "Allgemeine Geschäftsbedingungen",
        ""
    ),
    LegalSection(
        "Annahme",
        """
        Durch die Installation oder Nutzung von KeyArmor akzeptieren Sie diese Bedingungen vollumfänglich. Wenn Sie mit einem Teil nicht einverstanden sind, verwenden Sie die App nicht.
        """.trimIndent()
    ),
    LegalSection(
        "Erlaubte Nutzung",
        """
        KeyArmor ist für den persönlichen Gebrauch als Passwort-Manager und sicheren Tresor konzipiert. Die Verwendung für illegale Aktivitäten, die Speicherung von Informationen, die rechtswidrige Handlungen erleichtern, oder der Versuch, die App zurückzuentwickeln, ist verboten.
        """.trimIndent()
    ),
    LegalSection(
        "Verantwortung des Nutzers",
        """
        Sie sind allein verantwortlich für:

        • Die Sicherheit Ihres Master-Passworts. Bei Verlust gibt es keinen Wiederherstellungsmechanismus und Ihre Daten werden dauerhaft unzugänglich.
        • Der 12-Wort-Sicherungsschlüssel (sofern Sie diese Funktion nutzen). Wenn Sie ihn verlieren und zudem Ihr Smartphone verlieren, können Ihre Drive-Sicherungen nicht mehr geöffnet werden – nicht einmal vom Entwickler.
        • Die Pflege aktueller Sicherungen Ihres Tresors über die Exportfunktion oder Drive-Synchronisierung.
        """.trimIndent()
    ),
    LegalSection(
        "Synchronisierungsfunktion",
        """
        Die Google-Drive-Synchronisierung ist eine ergänzende Sicherungsfunktion. Sie ersetzt keine guten persönlichen Sicherheitspraktiken. Der Entwickler übernimmt keine Garantie für die kontinuierliche Verfügbarkeit dieser Funktion, da sie von Drittanbieterdiensten (Google) abhängt.
        """.trimIndent()
    ),
    LegalSection(
        "Premium-Abonnements",
        """
        KeyArmor bietet optionale kostenpflichtige Tarife an (Monatlich, Jährlich), die zusätzliche Funktionen freischalten. Abonnements werden ausschließlich über Google Play Billing abgewickelt. Mit dem Kauf eines Abonnements stimmen Sie auch den Nutzungsbedingungen von Google Play zu.

        • Die Tarife Monatlich und Jährlich sind automatisch verlängernde Abonnements. Sie können jederzeit über Google Play kündigen; die Kündigung gilt ab dem Ende des aktuellen Abrechnungszeitraums.
        • Rückerstattungen unterliegen der Standard-Rückerstattungsrichtlinie von Google Play. Der Entwickler bearbeitet Rückerstattungen nicht eigenständig.
        • Premium-Funktionen können sich im Laufe der Zeit weiterentwickeln. Der Entwickler behält sich das Recht vor, bestimmte Premium-Funktionen mit angemessener Vorankündigung hinzuzufügen, zu ändern oder einzustellen.
        """.trimIndent()
    ),
    LegalSection(
        "Sicherer Reisemodus",
        """
        Der Sichere Reisemodus ermöglicht es Ihnen, bestimmte Einträge als reisesicher zu markieren und alle anderen Einträge auszublenden, solange der Modus aktiv ist. Sie sind allein verantwortlich dafür, welche Einträge Sie als reisesicher markieren, und für die Folgen des Aktivierens oder Deaktivierens dieses Modus. Der Entwickler übernimmt keine Haftung für versehentlich sichtbar oder ausgeblendet gelassene Einträge.
        """.trimIndent()
    ),
    LegalSection(
        "Panik-PIN",
        """
        Die Panik-PIN-Funktion löst eine irreversible Aktion aus (Löschen oder Ausblenden sensibler Tresordaten), wenn eine bestimmte PIN eingegeben wird. Nach der Aktivierung sind ausgeblendete oder gelöschte Daten je nach Ihrer Konfiguration möglicherweise nicht wiederherstellbar. Sie übernehmen die volle Verantwortung für die Konfiguration dieser Funktion und für etwaige Datenverluste, die durch deren absichtliche oder unbeabsichtigte Aktivierung entstehen.
        """.trimIndent()
    ),
    LegalSection(
        "Krypto-Saldoinformationen",
        """
        Die Live-Krypto-Saldofunktion ruft Saldoinformationen aus der öffentlichen Blockchain-Infrastruktur ausschließlich zu Informationszwecken ab. Diese Informationen stellen keine Finanzberatung dar. Der Entwickler übernimmt keine Garantie für die Richtigkeit, Vollständigkeit oder Aktualität der Saldodaten. Treffen Sie keine finanziellen Entscheidungen ausschließlich auf Basis der in dieser App angezeigten Informationen.
        """.trimIndent()
    ),
    LegalSection(
        "Keine Gewährleistung",
        """
        Die App wird „wie besehen" ohne jegliche ausdrückliche oder stillschweigende Gewährleistung bereitgestellt. Der Entwickler gewährleistet nicht, dass die App fehlerfrei ist, ohne Unterbrechungen funktioniert oder für einen bestimmten Zweck geeignet ist.
        """.trimIndent()
    ),
    LegalSection(
        "Haftungsbeschränkung",
        """
        Im größtmöglichen gesetzlich zulässigen Umfang haftet der Entwickler nicht für Datenverlust, Verlust des Zugangs zum Tresor oder direkte, indirekte, zufällige, besondere oder Folgeschäden, die sich aus der Nutzung oder der Unmöglichkeit der Nutzung der App ergeben.

        Wenn Sie in einem Land leben, dessen Verbraucherschutzgesetz Ihnen Rechte einräumt, auf die Sie nicht verzichten können, werden diese Rechte durch die vorliegenden Bedingungen nicht eingeschränkt. Die Bestimmungen dieser Bedingungen zu Gewährleistungen und Haftung gelten nur insoweit, wie dies gesetzlich zulässig ist.
        """.trimIndent()
    ),
    LegalSection(
        "Aktualisierungen der Bedingungen",
        """
        Diese Bedingungen können jederzeit aktualisiert werden. Die fortgesetzte Nutzung der App nach einer veröffentlichten Aktualisierung gilt als Zustimmung zu den neuen Bedingungen.
        """.trimIndent()
    ),
    LegalSection(
        "Zuletzt aktualisiert",
        "November 2026 (Rev. 3)"
    )
)

private fun legalDE() = listOf(
    LegalSection(
        "Rechtlicher Hinweis und Haftungsausschluss",
        ""
    ),
    LegalSection(
        "Software wird wie besehen bereitgestellt",
        """
        KeyArmor wird „wie besehen" ohne jegliche ausdrückliche oder stillschweigende Gewährleistung vertrieben, einschließlich, aber nicht beschränkt auf Gewährleistungen der Marktgängigkeit, Eignung für einen bestimmten Zweck oder Nichtverletzung von Rechten Dritter.
        """.trimIndent()
    ),
    LegalSection(
        "Haftungsbeschränkung",
        """
        Der Entwickler von KeyArmor haftet unter keinen Umständen für:

        • Verlust oder Beschädigung von in der App gespeicherten Daten
        • Verlust des Zugriffs auf den Tresor aufgrund eines vergessenen oder verlorenen Hauptpassworts oder Sicherungsschlüssels
        • Sicherheitslücken oder Fehler in Android-OS-Komponenten
        • Ausfälle, Unterbrechungen oder Änderungen der Google-Drive-Dienste
        • Direkte, indirekte, zufällige, besondere oder Folgeschäden, die sich aus der Nutzung oder der Unmöglichkeit der Nutzung der App ergeben
        • Schäden, die durch unbefugten Zugriff Dritter auf Nutzerdaten entstehen
        • Datenverluste, die durch die absichtliche oder unbeabsichtigte Aktivierung der Panik-PIN-Funktion entstehen
        """.trimIndent()
    ),
    LegalSection(
        "Datenübertragung zu Google Drive",
        """
Wenn der Nutzer die Google-Drive-Synchronisierung (Sicherungen) aktiviert oder das Digitale Erbe / den Familienplan (Google Firebase) nutzt, werden die Daten bereits verschlüsselt gemäß den Google-Nutzungsbedingungen an die Google-Infrastruktur übertragen. In jedem Fall verlässt das Gerät die Daten als undurchsichtiges verschlüsseltes Datenpaket. Der Entwickler hat keinen Zugriff auf die Google-Infrastruktur, kontrolliert sie nicht und kann die übertragenen Inhalte nicht lesen.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore und Hardware-Abhängigkeit",
        """
        Die Verschlüsselungssicherheit von KeyArmor hängt teilweise von der Android-Keystore-Implementierung des Nutzergeräts ab. Diese Implementierung variiert je nach Hersteller, Chipsatz und Android-Version. Der Entwickler kontrolliert oder garantiert nicht die Sicherheit von Hardware-Implementierungen Dritter. Es wird dringend empfohlen, das Betriebssystem des Geräts aktuell zu halten.
        """.trimIndent()
    ),
    LegalSection(
        "Einschränkungen des Autofill-Dienstes",
        """
        Der Autofill-Dienst von KeyArmor funktioniert möglicherweise nicht mit allen Apps oder Browsern korrekt. Insbesondere unterstützt Google Chrome das standardmäßige Android-Autofill-Framework nicht — dabei handelt es sich um eine Chrome-Einschränkung, die alle Passwort-Manager von Drittanbietern gleichermaßen betrifft und außerhalb der Kontrolle des Entwicklers liegt. Der Entwickler übernimmt keine Garantie, dass der Autofill-Dienst mit einer bestimmten App funktioniert.
        """.trimIndent()
    ),
    LegalSection(
        "Haftungsausschluss für Krypto-Saldo",
        """
        Live-Saldodaten werden von öffentlichen Blockchain-Knoten und -APIs Dritter abgerufen. Der Entwickler betreibt oder kontrolliert diese Knoten nicht. Saldoinformationen werden ausschließlich zu Informationszwecken bereitgestellt und können verzögert, ungenau oder nicht verfügbar sein. Diese Informationen stellen keine Finanz- oder Anlageberatung dar. Der Entwickler übernimmt keine Haftung für Entscheidungen, die auf der Grundlage der in der App angezeigten Saldodaten getroffen werden.
        """.trimIndent()
    ),
    LegalSection(
        "Peer-to-Peer-Freigabe",
        """
        Die P2P-Eintragsfreigabe ermöglicht den direkten, verschlüsselten Austausch von Tresoreinträgen zwischen Nutzern. Der Entwickler übernimmt keine Verantwortung für den Inhalt der zwischen Nutzern geteilten Einträge, für die Identität oder Vertrauenswürdigkeit des Empfängers oder für etwaige Folgen aus der Weitergabe von Tresordaten an Dritte.
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotheken von Drittanbietern",
        """
        KeyArmor verwendet Open-Source-Bibliotheken von Drittanbietern, die zum Zeitpunkt der Veröffentlichung als sicher galten. Der Entwickler übernimmt keine Verantwortung für Ausfälle, Sicherheitslücken oder unerwartetes Verhalten, das von Drittkomponenten ausgeht.
        """.trimIndent()
    ),
    LegalSection(
        "Anwendbares Recht",
        """
        Dieser rechtliche Hinweis unterliegt den in der Argentinischen Republik geltenden Gesetzen. Alle Streitigkeiten, die sich aus der Nutzung der App ergeben, werden den zuständigen Gerichten dieser Rechtsordnung unterbreitet.

        Wenn Sie in einem Land leben, dessen Verbraucherschutzgesetz Ihnen Rechte einräumt, auf die Sie nicht verzichten können, werden diese Rechte durch diesen Hinweis nicht eingeschränkt. Die in diesem Hinweis enthaltenen Bestimmungen zu Gewährleistungen, Haftung, anwendbarem Recht und Gerichtsstand gelten nur insoweit, wie dies gesetzlich zulässig ist.
        """.trimIndent()
    ),
    LegalSection(
        "Zuletzt aktualisiert",
        "November 2026 (Rev. 3)"
    )
)

private fun licenseDE() = listOf(
    LegalSection("Source-Available-Lizenz", ""),
    LegalSection("Quellcode nur zu Zwecken der Sicherheitsprüfung veröffentlicht", """
Der Quellcode von KeyArmor ist ausschließlich zu dem Zweck öffentlich verfügbar, dass Sicherheitsforscher und Nutzer die Sicherheitspraktiken der App einsehen und prüfen können. Darüber hinaus werden keinerlei Rechte eingeräumt — alle Rechte liegen beim Urheberrechtsinhaber.
    """.trimIndent()),
    LegalSection("Was Sie tun dürfen", """
• Den Quellcode zu Zwecken der Sicherheitsforschung und persönlichen Prüfung lesen und untersuchen
• Sicherheitslücken über die offiziellen Kanäle an das KeyArmor-Team melden
    """.trimIndent()),
    LegalSection("Was Sie nicht tun dürfen", """
Ohne ausdrückliche vorherige schriftliche Genehmigung des Urheberrechtsinhabers dürfen Sie nicht:

• Die Software außerhalb ihrer offiziellen Distribution verwenden oder ausführen
• Den Quellcode zu einem anderen Zweck als dem Lesen kopieren, reproduzieren oder extrahieren
• Ihn verändern, anpassen, übersetzen oder davon abgeleitete Werke erstellen
• Ihn Dritten zugänglich machen, verbreiten, veröffentlichen, unterlizenzieren oder verkaufen
• Den Quellcode als Grundlage für ein anderes Produkt, einen anderen Dienst oder ein anderes Projekt verwenden
• Den Namen, das Logo oder zugehörige Marken von KeyArmor verwenden
    """.trimIndent()),
    LegalSection("Quellcode", """
Der Quellcode von KeyArmor ist öffentlich verfügbar.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Keine Gewährleistung", """
Der Quellcode wird „wie besehen“ bereitgestellt, ohne jegliche Gewährleistung, weder ausdrücklich noch stillschweigend. Der Urheberrechtsinhaber macht keine Zusicherungen bezüglich seiner Eignung für einen bestimmten Zweck.
    """.trimIndent()),
    LegalSection("Urheberrecht", "© 2026 KeyArmor. Alle Rechte vorbehalten. Source Available.")
)

private fun opensourceDE() = listOf(
    LegalSection(
        "Open-Source-Hinweis",
        ""
    ),
    LegalSection(
        "Verwendete Bibliotheken",
        """
        KeyArmor verwendet die folgenden Open-Source-Bibliotheken. Sofern nicht anders angegeben, werden sie unter der Apache-2.0-Lizenz vertrieben.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Version: BOM 2026.09.00 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Version: 1.2.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Version: 2.8.5 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Version: 2.60.1 | Lizenz: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Version: 1.11.0 | Lizenz: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Version: 1.1.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Version: 2.10.2 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Version: 2.11.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Version: 2.12.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Version: 22.0.0 | Lizenz: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Version: 2.9.2 | Lizenz: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Version: v3-rev20240123-2.0.0 | Lizenz: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Version: 2.2.0 | Lizenz: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Version: 4.3.0 | Lizenz: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Version: 0.37.3 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Version: BOM 2026.09.00 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Version: 1.19.1 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Version: 1.13.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Version: 1.3.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Version: 1.3.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Version: 1.8.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Version: 1.4.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Version: 2.4.21 | Lizenz: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Version: 1.2.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Version: 1.4.1 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Version: 1.6.0 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Version: 1.6.2 | Lizenz: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Version: BOM 35.1.0 | Lizenz: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Version: 2.15.0 | Lizenz: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Version: 3.0.1 | Lizenz: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Version: 2.0.27.0 | Lizenz: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Version: 1.2.1 | Lizenz: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Version: 1.86 | Lizenz: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Version: 10.29.0 | Lizenz: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Version: 5.2.0 | Lizenz: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Version: 5.19.1 | Lizenz: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Version: 1.2.1 | Lizenz: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Version: 16.0.1 | Lizenz: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Version: 16.0.1 | Lizenz: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Version: 2026-07-09 | Lizenz: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Vollständiger Apache-2.0-Text",
        """
        Der vollständige Text der Apache-2.0-Lizenz ist verfügbar unter:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataDE() = listOf(
    LegalSection("Konto und Daten löschen", ""),
    LegalSection("Welche Daten speichert KeyArmor über Sie?", """
Ihr Tresor (Passwörter, Notizen, Karten, Wallets, Anhänge und Passwortverlauf) wird niemals auf unseren Servern gespeichert. Er befindet sich verschlüsselt auf Ihrem Gerät und, falls Sie die Datensicherung aktivieren, in Ihrem eigenen Google Drive – nicht an einem Ort, an dem wir ihn lesen oder löschen können, da er verschlüsselt wird, bevor er Ihr Gerät verlässt.

Die einzigen Daten, die mit Ihrem Google-Konto verknüpft sind und unsere Server (Firebase) erreichen, stehen im Zusammenhang mit der Verknüpfung dieses Kontos für eine optionale Funktion – Google Drive-Sicherung, digitale Nachlassverwaltung oder Familienplan. Schon die bloße Verknüpfung eines Google-Kontos für eine dieser Funktionen führt dort zur Speicherung eines minimalen Datensatzes (ein Push-Benachrichtigungstoken, das mit Ihrer E-Mail-Adresse verknüpft ist), noch bevor Sie „Digitale Nachlassverwaltung“ oder den „Familienplan“ konfigurieren. Wenn Sie noch nie ein Google-Konto mit KeyArmor verknüpft haben, gibt es auf unseren Servern keine Daten über Sie, die gelöscht werden könnten.
    """.trimIndent()),
    LegalSection("Aus der App löschen", """
Sie können Ihre Daten jederzeit über „Einstellungen → Alle meine Daten löschen“ in der App vollständig löschen. Wenn Sie noch nie ein Google-Konto mit KeyArmor verknüpft haben (für Drive-Backups, digitale Nachlassplanung oder den Familienplan), wird Ihr Tresor dadurch sofort und dauerhaft vom Gerät gelöscht.

Wenn Sie ein Konto verknüpft haben, fordert die App Sie auf, sich bei Google anzumelden, damit sie im Rahmen desselben Vorgangs die zugehörigen serverseitigen Datensätze (Firebase) sowie die Sicherungs- und Time-Machine-Kopien löschen kann, die KeyArmor in Ihrem Google Drive gespeichert hat. Nur wenn Sie sich nicht anmelden können, greift die App auf das Löschen der lokalen Daten zurück – und warnt Sie zuvor, dass Ihre serverseitigen Datensätze bestehen bleiben, bis sie separat mithilfe der unten stehenden Anfrage entfernt werden.
    """.trimIndent()),
    LegalSection("Was wird nicht gelöscht?", """
Das Löschen Ihrer Daten entfernt nicht die Inhalte, die außerhalb der App gespeichert sind:

• Dateien, die Sie selbst exportiert haben (CSV-, PDF- oder .kab-Dateien, die Sie gespeichert oder weitergegeben haben). Diese gehören Ihnen und werden dort gespeichert, wo Sie sie abgelegt haben; daher müssen Sie sie dort löschen.
• Sicherungskopien, die eine frühere Installation von KeyArmor in Ihrem Ordner „Downloads“ hinterlassen hat. Android lässt nicht mehr zu, dass eine neue Installation darauf zugreift; löschen Sie diese daher aus Ihrem Ordner „Downloads“.
• Ihr Abonnement. Es wird von Google Play verwaltet, daher wird es durch das Löschen Ihrer Daten nicht gekündigt; kündigen Sie es in Google Play → Abonnements.
    """.trimIndent()),
    LegalSection("Antrag auf Löschung Ihrer Kontodaten", """
Wenn Sie ein Google-Konto mit KeyArmor verknüpft haben, sich aber nicht anmelden können, um die oben beschriebene Löschfunktion in der App zu nutzen (z. B. weil Sie die App nicht mehr installiert haben), beantragen Sie bitte die Löschung der zugehörigen serverseitigen Daten, indem Sie uns eine E-Mail von diesem Google-Konto aus senden.

Schreiben Sie an: support@thormakk.dev

Wir bearbeiten Löschanträge innerhalb von 30 Tagen und bestätigen den Abschluss per E-Mail. Sie benötigen die App nicht, um diesen Antrag zu stellen.
    """.trimIndent())
)
