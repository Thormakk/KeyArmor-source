package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentFR(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyFR()
    "terms"      -> termsFR()
    "legal"      -> legalFR()
    "license"    -> licenseFR()
    "opensource" -> opensourceFR()
    "delete"     -> deleteDataFR()
    else         -> listOf(LegalSection("Document introuvable", ""))
}

private fun privacyFR() = listOf(
    LegalSection(
        "Politique de confidentialité",
        ""
    ),
    LegalSection(
        "Collecte de données",
        """
        KeyArmor ne collecte, ne transmet ni ne partage aucune donnée personnelle vers des serveurs externes. Toutes les informations que vous saisissez dans l'application restent exclusivement sur votre appareil, sauf si vous activez explicitement la synchronisation Google Drive.

        KeyArmor ne collecte pas votre adresse e-mail ni aucune information d'identification de compte pour les fonctionnalités principales de l'application (coffre, remplissage automatique, audit de sécurité, surveillance des violations) — celles-ci fonctionnent entièrement sans aucun compte. Si vous activez l'Héritage Numérique ou le Forfait Famille, ces fonctionnalités nécessitent une connexion Google : votre adresse e-mail de compte Google est alors stockée sur Google Firebase pour vous identifier et vous associer aux héritiers ou membres de la famille que vous désignez, ainsi que pour vous envoyer les e-mails de notification correspondants via notre fournisseur d'e-mails (Brevo). Cet e-mail est utilisé exclusivement pour ces deux fonctionnalités et n'est jamais vendu ni partagé avec qui que ce soit d'autre. En dehors de l'Héritage Numérique et du Forfait Famille, le seul identifiant personnel est un nom ou surnom optionnel que vous pouvez définir lors de la configuration initiale, lequel est stocké exclusivement sur votre appareil sous forme chiffrée.
        """.trimIndent()
    ),
    LegalSection(
        "Stockage local",
        """
        Toutes vos données (mots de passe, passkeys, notes, cartes de crédit, comptes bancaires, portefeuilles cryptographiques, pièces jointes et historique des mots de passe) sont stockées chiffrées avec AES-256-GCM, avec une clé dérivée de votre mot de passe maître via Argon2id, qui ne quitte jamais votre appareil. Si vous activez le déverrouillage biométrique, une copie de cette clé est en outre protégée par une clé de l'Android Keystore de votre appareil. Aucune donnée ne quitte votre appareil sans votre consentement explicite.
        """.trimIndent()
    ),
    LegalSection(
        "Mot de passe maître",
        """
        Votre mot de passe maître n'est jamais stocké en clair ni sous aucune forme récupérable. Il est utilisé uniquement pour dériver la clé de chiffrement du coffre via Argon2id. Si vous perdez votre mot de passe maître, vos données ne pourront pas être récupérées.
        """.trimIndent()
    ),
    LegalSection(
        "Données biométriques",
        """
        L'authentification biométrique dans KeyArmor est entièrement gérée par le système d'exploitation Android via l'API BiometricPrompt. KeyArmor n'accède pas, ne stocke pas et ne traite pas de données biométriques à quelque moment que ce soit.

        La biométrie est utilisée dans deux contextes au sein de l'application :

        • Déverrouillage du coffre : pour authentifier l'accès à l'application au démarrage, disponible sur tous les abonnements.
        • Protection biométrique avancée (Premium) : une barrière optionnelle exigeant une vérification biométrique avant d'afficher les champs sensibles dans des sections spécifiques du coffre (mots de passe, cartes bancaires, champs personnalisés). Configurable dans les Paramètres — prend en charge le mode par accès ou par session avec un délai d'expiration personnalisable. Les mêmes garanties de confidentialité s'appliquent : aucune donnée biométrique n'est stockée ni traitée par KeyArmor.
        """.trimIndent()
    ),
    LegalSection(
        "Synchronisation Google Drive",
        """
La synchronisation Google Drive est entièrement facultative. Lorsqu'elle est activée, KeyArmor :

• Demande une seule autorisation minimale (DRIVE_APPDATA) pour stocker vos propres sauvegardes chiffrées et vos points de restauration Time Machine dans un dossier privé, propre à l'application, dans votre Google Drive. Ce dossier est masqué — aucune autre application, ni vous-même en parcourant Drive, ne peut le voir ni le modifier.
• Chiffre le coffre-fort à l'aide d'une clé de sauvegarde composée de 12 mots avant de le transférer. La clé est générée sur votre appareil et vous êtes le seul à en disposer : ni Google ni le développeur ne peuvent lire le fichier ni récupérer la clé.
• Si vous enregistrez le fichier PDF de votre clé de sauvegarde dans Google Docs, Google Drive ou une autre application, vous le faites vous-même via l'écran de partage d'Android ; cette application est régie par sa propre politique de confidentialité.
• Si vous changez de clé, vous pouvez conserver les anciennes, chiffrées sur votre téléphone ; le fichier PDF du kit peut les inclure. KeyArmor ne les envoie à aucun serveur.
• N'accède jamais à aucun autre fichier Drive, ne crée jamais de dossier visible et ne partage jamais de fichier.
• Conserve éventuellement une seconde copie locale de la sauvegarde dans le dossier Downloads/KeyArmor/ de votre appareil, qui ne quitte jamais l'appareil.
• La synchronisation à la demande est disponible sur tous les forfaits (déclenchée manuellement). La synchronisation quotidienne automatique est disponible avec Premium.

L'Héritage Numérique et le Forfait Famille utilisent Google Firebase, et non votre Google Drive. Firebase gère les consentements, les invitations et les notifications. Le coffre-fort d'héritage est chiffré de bout en bout sur votre appareil à l'aide de la clé publique de l'héritier, et le bloc chiffré ainsi obtenu est téléchargé vers un compartiment privé de Firebase Cloud Storage appartenant au développeur via des liens signés à durée limitée (15 minutes). Le serveur conserve ce bloc mais ne reçoit jamais la clé de l'héritier ; il ne peut donc pas le lire.

Vous pouvez déconnecter votre compte Google à tout moment depuis l'écran de synchronisation.
    """.trimIndent()
    ),
    LegalSection(
        "Pièces jointes",
        """
        La fonctionnalité de pièces jointes vous permet d'associer des fichiers (jusqu'à 5 par entrée, 5 Mo maximum chacun) à vos entrées de coffre-fort. Toutes les pièces jointes sont chiffrées à l'aide de l'algorithme AES-256-GCM avant d'être enregistrées dans la mémoire interne de l'appareil. Si vous activez la synchronisation avec Google Drive, vos pièces jointes et les photos de vos documents sont incluses dans la sauvegarde que vous téléversez, chiffrées à l'aide de votre clé de sauvegarde. Elles ne sont jamais transmises à aucun autre service.
        """.trimIndent()
    ),
    LegalSection(
        "Historique des mots de passe",
        """
        KeyArmor conserve un historique local des mots de passe précédents pour chaque entrée. Cet historique est stocké de manière chiffrée sur l'appareil et n'est jamais transmis à un serveur externe.
        """.trimIndent()
    ),
    LegalSection(
        "Notifications in-app",
        """
        KeyArmor génère des notifications internes pour vous alerter sur des événements de sécurité importants :

        • Avertissements d'expiration : envoyés lorsqu'un ou plusieurs mots de passe approchent de leur date d'expiration.
        • Mots de passe expirés : envoyés lorsqu'un ou plusieurs mots de passe ont déjà dépassé leur date d'expiration.
        • Alertes de violation : envoyées lorsque vos mots de passe sont détectés dans des violations de données connues.

        Ces trois types de notifications sont générés entièrement sur votre appareil et ne sont jamais transmis à des serveurs externes. Si vous utilisez l'Héritage Numérique ou le Forfait Famille, vous recevrez également des notifications concernant ces fonctionnalités (un héritier qui accepte, un membre ajouté à la famille, etc.) ; celles-ci sont déclenchées par Google Firebase et livrées via Firebase Cloud Messaging, car elles dépendent d'événements initiés par un autre compte. Toutes les notifications, quelle que soit leur origine, sont automatiquement supprimées après 30 jours. Vous pouvez supprimer toute notification en la faisant glisser latéralement, ou tout effacer d'un coup depuis l'écran des Notifications. Les notifications push nécessitent une autorisation système pour être envoyées.
        """.trimIndent()
    ),
    LegalSection(
        "Audit de sécurité",
        """
        La fonctionnalité d'audit de sécurité analyse vos mots de passe localement sur l'appareil. La vérification des mots de passe courants s'effectue par comparaison avec une liste prédéfinie stockée dans l'application elle-même, sans requête vers des services externes.
        """.trimIndent()
    ),
    LegalSection(
        "Surveillance des violations (HIBP)",
        """
        KeyArmor peut vérifier si vos mots de passe sont apparus dans des violations de données connues en utilisant le service Have I Been Pwned (HIBP), exploité par le chercheur en sécurité Troy Hunt.

        Cette vérification utilise un modèle k-anonymat : seuls les 5 premiers caractères d'un hachage SHA-1 de votre mot de passe sont envoyés à l'API HIBP. Le hachage complet n'est jamais transmis. La correspondance est effectuée localement sur votre appareil. Aucun nom d'utilisateur, adresse e-mail ou association de compte n'est jamais envoyé. Les réponses HIBP sont complétées par des hachages leurres afin que même l'analyse du trafic réseau ne révèle rien sur le mot de passe vérifié.
        """.trimIndent()
    ),
    LegalSection(
        "Service de remplissage automatique",
        """
        KeyArmor inclut un service de remplissage automatique Android optionnel. Lorsqu'il est activé dans les paramètres système, il lit le nom du package de l'application ou l'URL du site web de l'écran de connexion actif afin de suggérer des identifiants correspondants depuis votre coffre. Cette correspondance est effectuée entièrement sur l'appareil. Aucun nom d'application, URL de site web ou identifiant n'est transmis à un serveur externe. Le service de remplissage automatique ne lit pas le contenu des autres applications au-delà de ce qui est strictement nécessaire pour identifier le contexte de connexion.
        """.trimIndent()
    ),
    LegalSection(
        "Solde en direct des portefeuilles crypto",
        """
        Lorsque vous enregistrez une entrée de portefeuille crypto et activez la surveillance du solde en direct, KeyArmor interroge des nœuds ou des API blockchain publics pour récupérer le solde associé à l'adresse de portefeuille que vous avez saisie. Seule l'adresse du portefeuille est transmise — aucune clé privée, phrase de récupération ou donnée personnelle n'est envoyée. Ces requêtes vont directement vers l'infrastructure publique ; KeyArmor n'exploite aucun serveur intermédiaire.
        """.trimIndent()
    ),
    LegalSection(
        "Partage P2P d'entrées",
        """
        KeyArmor vous permet de partager des entrées individuelles de votre coffre directement avec d'autres utilisateurs de KeyArmor via un code QR ou un texte chiffré. Ce transfert est chiffré de bout en bout et de pair à pair — aucun serveur ni intermédiaire n'est impliqué. Vous êtes seul responsable du choix des personnes avec qui vous partagez des entrées et de la sécurité du canal utilisé pour transmettre le texte chiffré (si vous n'utilisez pas directement le QR).
        """.trimIndent()
    ),
    LegalSection(
        "Bibliothèques tierces",
        """
        KeyArmor utilise des bibliothèques open source (voir Avis open source). Aucune de ces bibliothèques n'inclut de mécanismes de télémétrie, d'analyse ou de collecte de données dans la configuration utilisée par cette application.

        KeyArmor s'appuie également sur deux services tiers pour des fonctionnalités spécifiques : RevenueCat, qui vérifie le statut de votre abonnement Premium et ne reçoit que des informations liées à l'achat (jamais votre coffre, votre mot de passe maître ou votre clé de récupération) ; et Brevo, qui envoie les e-mails transactionnels requis par l'Héritage Numérique et le Forfait Famille (invitations, confirmations de consentement, notifications) et reçoit, à cette fin, l'adresse e-mail des personnes impliquées dans ces fonctionnalités. Aucun des deux services n'est utilisé à des fins publicitaires ou d'analyse comportementale.
        """.trimIndent()
    ),
    LegalSection(
        "Widget d'écran d'accueil",
        """
        KeyArmor inclut un widget facultatif pour l'écran d'accueil Android qui affiche le nombre total d'entrées enregistrées dans votre coffre. Ce widget n'accède qu'aux données locales de l'appareil. Il ne transmet aucune information à des serveurs externes. Le compteur d'entrées est une donnée agrégée qui ne révèle pas le contenu des entrées individuelles. Vous pouvez choisir de placer ou non ce widget sur votre écran d'accueil.
        """.trimIndent()
    ),
    LegalSection(
        "Autorisations",
        """
        KeyArmor ne demande que les autorisations nécessaires à son fonctionnement :

        • Biométrie (facultatif) : pour le déverrouillage par empreinte digitale.
        • Internet (facultatif) : utilisé uniquement pour les fonctionnalités que vous activez vous-même — synchronisation Google Drive, Héritage Numérique et Forfait Famille (via Google Firebase), requêtes de solde en direct des portefeuilles crypto, vérification des violations via Have I Been Pwned, et vérification des abonnements (Google Play Billing / RevenueCat).
        • Accès aux fichiers (facultatif) : pour l'importation/exportation du coffre et l'ajout de pièces jointes, uniquement lorsque vous en prenez l'initiative.
        • Appareil photo (facultatif) : pour scanner des codes QR lors de la configuration de l'authentification à deux facteurs (2FA/TOTP) ou lors de la réception d'entrées partagées. Activé uniquement à la demande explicite de l'utilisateur.
        • Service de remplissage automatique (facultatif) : activé manuellement dans les paramètres système Android. Utilisé uniquement pour suggérer des identifiants sur les écrans de connexion d'autres applications.
        • Microphone (facultatif) : uniquement pour la dictée vocale dans les Notes sécurisées. La reconnaissance se fait sur votre appareil ; KeyArmor n'enregistre, ne conserve ni n'envoie jamais d'audio à un serveur.
        • Calendrier, lecture seule (facultatif) : utilisé par le Mode Voyage sécurisé pour repérer des voyages dans vos événements. Les événements sont traités sur votre appareil et ne sont jamais transmis.
        • Notifications et alarmes exactes (facultatif) : pour les alertes de sécurité et les rappels que vous programmez. L'événement de démarrage de l'appareil sert à les reprogrammer après un redémarrage.
        • État du réseau (facultatif) : uniquement pour savoir si vous êtes en données mobiles ou en Wi-Fi, afin que les grosses sauvegardes Google Drive attendent le Wi-Fi. Aucune information sur votre réseau n'est collectée ni envoyée.

        KeyArmor ne demande pas l'accès aux contacts, à la localisation ni à toute autre donnée personnelle non mentionnée ci-dessus.
        """.trimIndent()
    ),
    LegalSection("Autres services qui reçoivent des données", """
Outre les services décrits ci-dessus, KeyArmor utilise les suivants, uniquement pour les fonctions qui en ont besoin :

• Vérification d'intégrité de l'application (Firebase App Check / Google Play Integrity) : Google fournit une attestation de votre appareil afin que notre serveur n'accepte que les requêtes de l'application authentique.
• RevenueCat : en plus des informations d'achat, il reçoit l'identifiant de votre compte Google (pas votre adresse e-mail) comme identifiant d'utilisateur, afin de lier votre abonnement entre vos appareils.
• CoinGecko : lorsque vous utilisez les cours crypto en direct, les symboles de votre portefeuille sont demandés à CoinGecko, qui peut voir votre adresse IP. Les explorateurs et nœuds blockchain mentionnés plus haut peuvent aussi la voir.
• Google Fonts : l'application télécharge ses polices Inter et JetBrains Mono via les services Google Play ; Google peut donc voir votre adresse IP lors de ces demandes.
• Heure de confiance des services Google Play : utilisée pour détecter une manipulation de l'horloge de l'appareil. Aucune donnée personnelle n'est envoyée.
• La reconnaissance de texte (OCR) des documents s'effectue sur votre appareil avec ML Kit. Aucune image n'est envoyée à un serveur.
• Ko-fi : uniquement si vous ouvrez le lien de don ; leur propre politique de confidentialité s'applique alors.

Nos serveurs peuvent enregistrer temporairement votre adresse IP afin de limiter les abus.
    """.trimIndent()),
    LegalSection(
        "Passkeys (clés d'accès)",
        """
        KeyArmor peut fonctionner comme fournisseur de passkeys Android (Credential Manager), uniquement si vous le choisissez dans les paramètres du système. Lorsque vous créez ou utilisez une passkey, KeyArmor lit le domaine du site ou de l'application qui la demande afin de l'associer à ce site et de signer le défi envoyé par le site. La clé privée est générée sur votre appareil et stockée chiffrée avec le reste de votre coffre ; elle n'est envoyée à aucun serveur ni à aucun tiers. Elle ne quitte votre appareil que dans les sauvegardes que vous créez vous-même (par exemple, la sauvegarde chiffrée sur Google Drive). Pour signer avec une passkey, KeyArmor vous demande de vérifier votre identité avec votre empreinte digitale ou votre mot de passe maître. Le site sur lequel vous vous connectez reçoit uniquement la clé publique et la signature, comme avec n'importe quelle passkey.
        """.trimIndent()
    ),
    LegalSection(
        "Suppression et conservation des données",
        """
        Votre coffre est conservé sur votre appareil (et dans votre Google Drive, si vous activez la sauvegarde) jusqu'à ce que vous le supprimiez vous-même. Sur nos serveurs, il n'y a que des données de coordination : le jeton de notifications push identifié par votre e-mail et, si vous utilisez l'Héritage Numérique ou le Forfait Famille, les enregistrements d'invitations et de consentement ainsi que le bloc chiffré du coffre d'héritage. Ils sont conservés tant que vous utilisez ces fonctions ou jusqu'à ce que vous demandiez leur suppression. Vous pouvez les supprimer depuis Paramètres → Supprimer toutes mes données, ou en écrivant à support@thormakk.dev depuis votre compte Google ; nous traitons ces demandes dans un délai de 30 jours. Le détail complet figure sur la page «Supprimer le compte et les données» de notre site web.
        """.trimIndent()
    ),
    LegalSection(
        "Mineurs",
        """
        KeyArmor ne s'adresse pas aux enfants de moins de 13 ans (ni à l'âge minimal fixé par la loi de votre pays pour donner un consentement numérique), et nous ne collectons pas sciemment de données de mineurs. Si vous pensez qu'un mineur nous a transmis des données, écrivez-nous à support@thormakk.dev et nous les supprimerons.
        """.trimIndent()
    ),
    LegalSection(
        "Contact",
        "Pour toute question relative à cette politique, vous pouvez écrire à support@thormakk.dev"
    ),
    LegalSection(
        "Dernière mise à jour",
        "Octobre 2026 (rév. 8)"
    )
)

private fun termsFR() = listOf(
    LegalSection(
        "Conditions générales d'utilisation",
        ""
    ),
    LegalSection(
        "Acceptation",
        """
        En installant ou en utilisant KeyArmor, vous acceptez intégralement ces conditions. Si vous n'êtes pas d'accord avec l'une quelconque de ses parties, n'utilisez pas l'application.
        """.trimIndent()
    ),
    LegalSection(
        "Utilisation autorisée",
        """
        KeyArmor est conçu pour un usage personnel en tant que gestionnaire de mots de passe et coffre sécurisé. Son utilisation à des fins illégales, pour stocker des informations facilitant des actes illicites, ou pour tenter de procéder à de la rétro-ingénierie de l'application est interdite.
        """.trimIndent()
    ),
    LegalSection(
        "Responsabilité de l'utilisateur",
        """
        Vous êtes seul responsable de :

        • La sécurité de votre mot de passe maître. En cas de perte, il n'existe aucun mécanisme de récupération et vos données seront définitivement inaccessibles.
        • La clé de sauvegarde à 12 mots (si vous utilisez cette fonctionnalité). Si vous la perdez et que vous perdez également votre téléphone, vos sauvegardes Drive ne pourront plus être ouvertes, pas même par le développeur.
        • La maintenance de sauvegardes à jour de votre coffre via la fonction d'exportation ou la synchronisation Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Fonctionnalité de synchronisation",
        """
        La synchronisation Google Drive est une fonctionnalité de sauvegarde auxiliaire. Elle ne remplace pas les bonnes pratiques personnelles de sécurité. Le développeur ne garantit pas la disponibilité continue de cette fonctionnalité, car elle dépend de services tiers (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Abonnements Premium",
        """
        KeyArmor propose des abonnements payants optionnels (Mensuel et Annuel) qui débloquent des fonctionnalités supplémentaires. Les abonnements sont traités exclusivement via Google Play Billing. En souscrivant un abonnement, vous acceptez également les Conditions d'utilisation de Google Play.

        • Les abonnements Mensuel et Annuel sont des abonnements à renouvellement automatique. Vous pouvez annuler à tout moment via Google Play ; l'annulation prend effet à la fin de la période de facturation en cours.
        • Les remboursements sont soumis à la politique de remboursement standard de Google Play. Le développeur ne traite pas les remboursements de manière indépendante.
        • Les fonctionnalités premium peuvent évoluer dans le temps. Le développeur se réserve le droit d'ajouter, modifier ou supprimer des fonctionnalités premium spécifiques avec un préavis raisonnable.
        """.trimIndent()
    ),
    LegalSection(
        "Mode Voyage sécurisé",
        """
        Le Mode Voyage sécurisé vous permet de marquer des entrées spécifiques comme sûres pour le voyage et de masquer toutes les autres entrées tant que le mode est actif. Vous êtes seul responsable de déterminer quelles entrées marquer comme sûres pour le voyage et des conséquences de l'activation ou désactivation de ce mode. Le développeur décline toute responsabilité pour les entrées accidentellement laissées visibles ou masquées.
        """.trimIndent()
    ),
    LegalSection(
        "Code PIN de panique",
        """
        La fonctionnalité Code PIN de panique déclenche une action irréversible (effacement ou masquage de données sensibles du coffre) lorsqu'un code PIN désigné est saisi. Une fois activées, les données masquées ou supprimées peuvent ne pas être récupérables selon votre configuration. Vous assumez l'entière responsabilité de la configuration de cette fonctionnalité et de toute perte de données résultant de son activation intentionnelle ou accidentelle.
        """.trimIndent()
    ),
    LegalSection(
        "Informations sur le solde crypto",
        """
        La fonctionnalité de solde crypto en direct récupère des informations de solde depuis l'infrastructure blockchain publique à des fins d'information uniquement. Ces informations ne constituent pas un conseil financier. Le développeur ne garantit pas l'exactitude, l'exhaustivité ni l'actualité des données de solde. Ne prenez pas de décisions financières basées uniquement sur les informations affichées dans cette application.
        """.trimIndent()
    ),
    LegalSection(
        "Absence de garanties",
        """
        L'application est fournie « en l'état », sans garantie d'aucune sorte, expresse ou implicite. Le développeur ne garantit pas que l'application est exempte d'erreurs, fonctionne sans interruption ou est adaptée à un usage particulier.
        """.trimIndent()
    ),
    LegalSection(
        "Limitation de responsabilité",
        """
        Dans toute la mesure permise par la loi applicable, le développeur ne saurait être tenu responsable de toute perte de données, de tout accès perdu au coffre, ni de tout dommage direct, indirect, accessoire, spécial ou consécutif découlant de l'utilisation ou de l'impossibilité d'utiliser l'application.
        """.trimIndent()
    ),
    LegalSection(
        "Mises à jour des conditions",
        """
        Ces conditions peuvent être mises à jour à tout moment. L'utilisation continue de l'application après la publication d'une mise à jour vaut acceptation des nouvelles conditions.
        """.trimIndent()
    ),
    LegalSection(
        "Dernière mise à jour",
        "Mai 2026 (rév. 2)"
    )
)

private fun legalFR() = listOf(
    LegalSection(
        "Mentions légales et avertissement",
        ""
    ),
    LegalSection(
        "Logiciel fourni en l'état",
        """
        KeyArmor est distribué « en l'état », sans garantie d'aucune sorte, expresse ou implicite, y compris, mais sans s'y limiter, les garanties de qualité marchande, d'adéquation à un usage particulier ou d'absence de contrefaçon.
        """.trimIndent()
    ),
    LegalSection(
        "Limitation de responsabilité",
        """
        Le développeur de KeyArmor ne saurait être tenu responsable en aucune circonstance de :

        • La perte ou la corruption de données stockées dans l'application
        • Perte d'accès au coffre-fort en raison d'un mot de passe principal ou d'une clé de sauvegarde oubliés ou perdus
        • Les vulnérabilités ou défaillances dans les composants du système d'exploitation Android
        • Les pannes, interruptions ou modifications des services Google Drive
        • Tout dommage direct, indirect, accessoire, spécial ou consécutif découlant de l'utilisation ou de l'impossibilité d'utiliser l'application
        • Tout dommage résultant d'un accès non autorisé aux données de l'utilisateur par des tiers
        • La perte de données résultant de l'activation intentionnelle ou accidentelle de la fonctionnalité Code PIN de panique
        """.trimIndent()
    ),
    LegalSection(
        "Transmission de données vers Google Drive",
        """
Lorsque l'utilisateur active la synchronisation Google Drive (sauvegardes) ou utilise l'Héritage Numérique / le Forfait Famille (Google Firebase), les données sont transmises déjà chiffrées à l'infrastructure de Google, conformément aux Conditions d'utilisation de Google. Dans tous les cas, les données quittent l'appareil sous forme de bloc chiffré opaque. Le développeur n'a aucun accès ni contrôle sur l'infrastructure de Google et ne peut pas lire le contenu transmis.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore et dépendance matérielle",
        """
        La sécurité du chiffrement de KeyArmor dépend en partie de l'implémentation de l'Android Keystore de l'appareil de l'utilisateur. Cette implémentation varie selon le fabricant, le chipset et la version d'Android. Le développeur ne contrôle pas et ne garantit pas la sécurité des implémentations matérielles tierces. Il est fortement recommandé de maintenir le système d'exploitation de l'appareil à jour.
        """.trimIndent()
    ),
    LegalSection(
        "Limitations du service de remplissage automatique",
        """
        Le service de remplissage automatique de KeyArmor peut ne pas fonctionner correctement avec toutes les applications ou tous les navigateurs. En particulier, Google Chrome ne prend pas en charge le framework de remplissage automatique standard d'Android — il s'agit d'une limitation de Chrome qui affecte de la même manière tous les gestionnaires de mots de passe tiers et échappe au contrôle du développeur. Le développeur ne garantit pas que le service de remplissage automatique fonctionnera avec une application spécifique.
        """.trimIndent()
    ),
    LegalSection(
        "Avertissement concernant le solde crypto en direct",
        """
        Les données de solde en direct sont récupérées depuis des nœuds et des API blockchain publics tiers. Le développeur n'exploite pas et ne contrôle pas ces nœuds. Les informations de solde sont fournies à titre informatif uniquement et peuvent être en retard, inexactes ou indisponibles. Ces informations ne constituent pas un conseil financier ou en investissement. Le développeur décline toute responsabilité pour les décisions prises sur la base des données de solde affichées dans l'application.
        """.trimIndent()
    ),
    LegalSection(
        "Partage pair à pair",
        """
        La fonctionnalité de partage P2P d'entrées facilite l'échange direct et chiffré d'entrées de coffre entre utilisateurs. Le développeur décline toute responsabilité quant au contenu des entrées partagées entre utilisateurs, à l'identité ou à la fiabilité du destinataire, ou aux conséquences découlant du partage de données de coffre avec des tiers.
        """.trimIndent()
    ),
    LegalSection(
        "Bibliothèques tierces",
        """
        KeyArmor utilise des bibliothèques open source tierces considérées comme sécurisées au moment de la publication. Le développeur n'assume aucune responsabilité pour les défaillances, vulnérabilités ou comportements inattendus provenant de composants tiers.
        """.trimIndent()
    ),
    LegalSection(
        "Droit applicable",
        """
        Ces mentions légales sont régies par les lois en vigueur dans la République Argentine. Tout litige découlant de l'utilisation de l'application sera soumis aux tribunaux compétents de cette juridiction.
        """.trimIndent()
    ),
    LegalSection(
        "Dernière mise à jour",
        "Mai 2026 (rév. 2)"
    )
)

private fun licenseFR() = listOf(
    LegalSection("Licence Source Available", ""),
    LegalSection("Code source publié uniquement à des fins d'audit de sécurité", """
Le code source de KeyArmor est rendu public uniquement pour permettre aux chercheurs en sécurité et aux utilisateurs d'inspecter et d'auditer ses pratiques de sécurité. Aucun droit d'aucune sorte n'est accordé au-delà de cela — tous les droits sont réservés par le titulaire du droit d'auteur.
    """.trimIndent()),
    LegalSection("Ce que vous pouvez faire", """
• Lire et inspecter le code source à des fins de recherche en sécurité et d'audit personnel
• Signaler des vulnérabilités de sécurité à l'équipe KeyArmor via les canaux officiels
    """.trimIndent()),
    LegalSection("Ce que vous ne pouvez pas faire", """
Sans autorisation écrite préalable et explicite du titulaire du droit d'auteur, vous ne pouvez pas :

• Utiliser ou exécuter le logiciel en dehors de sa distribution officielle
• Copier, reproduire ou extraire le code source à toute autre fin que la lecture
• Le modifier, l'adapter, le traduire ou créer des œuvres dérivées à partir de celui-ci
• Le distribuer, le publier, le sous-licencier, le vendre ou le mettre à disposition de tiers de quelque manière que ce soit
• Utiliser le code source comme base d'un autre produit, service ou projet
• Utiliser le nom, le logo ou toute marque associée à KeyArmor
    """.trimIndent()),
    LegalSection("Code source", """
Le code source de KeyArmor est disponible publiquement.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Aucune garantie", """
Le code source est fourni « tel quel », sans garantie d'aucune sorte, expresse ou implicite. Le titulaire du droit d'auteur ne fait aucune déclaration quant à son adéquation à un usage particulier.
    """.trimIndent()),
    LegalSection("Droits d'auteur", "© 2026 KeyArmor. Tous droits réservés. Source Available.")
)

private fun opensourceFR() = listOf(
    LegalSection(
        "Avis open source",
        ""
    ),
    LegalSection(
        "Bibliothèques utilisées",
        """
        KeyArmor utilise les bibliothèques open source suivantes. Sauf indication contraire, elles sont distribuées sous la licence Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Version : BOM 2026.09.00 | Licence : Apache 2.0
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
        Version : 2.8.5 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Version : 2.60.1 | Licence : Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Version : 1.11.0 | Licence : Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Version : 1.1.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Version : 2.10.2 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Version : 2.11.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Version : 2.12.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Version : 22.0.0 | Licence : Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Version : 2.9.2 | Licence : Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Version : v3-rev20240123-2.0.0 | Licence : Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Version : 2.2.0 | Licence : Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Version : 4.3.0 | Licence : Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Version : 0.37.3 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Version : BOM 2026.09.00 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Version : 1.19.1 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Version : 1.13.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Version : 1.3.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Version : 1.3.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Version : 1.8.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Version : 1.4.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Version : 2.4.21 | Licence : Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Version : 1.2.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Version : 1.4.1 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Version : 1.6.0 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Version : 1.6.2 | Licence : Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Version : BOM 35.1.0 | Licence : Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Version : 2.15.0 | Licence : Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Version : 3.0.1 | Licence : Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Version : 2.0.27.0 | Licence : Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Version : 1.2.1 | Licence : MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Version : 1.86 | Licence : Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Version : 10.29.0 | Licence : MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Version : 5.2.0 | Licence : MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Version : 5.19.1 | Licence : Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Version : 1.2.1 | Licence : Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Version : 16.0.1 | Licence : Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Version : 16.0.1 | Licence : ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Version : 2026-07-09 | Licence : MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Texte complet Apache 2.0",
        """
        Le texte complet de la licence Apache 2.0 est disponible à l'adresse :
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataFR() = listOf(
    LegalSection("Supprimer le compte et les données", ""),
    LegalSection("Quelles informations KeyArmor conserve-t-elle à votre sujet ?", """
Votre coffre-fort (mots de passe, notes, fiches, portefeuilles, pièces jointes et historique des mots de passe) n’est jamais stocké sur nos serveurs. Il est conservé sous forme chiffrée sur votre appareil et, si vous activez la sauvegarde, sur votre propre Google Drive — à un emplacement où nous ne pouvons ni le lire ni le supprimer, car il est chiffré avant même de quitter votre appareil.

Les seules données associées à votre compte Google qui parviennent à nos serveurs (Firebase) sont liées à la connexion de ce compte pour une fonctionnalité optionnelle : la sauvegarde sur Google Drive, l’Héritage numérique ou le Forfait familial. Le simple fait de connecter un compte Google pour l’une de ces fonctionnalités entraîne l’enregistrement d’une trace minimale sur nos serveurs (un jeton de notification push, associé à votre adresse e-mail), avant même que vous ne configuriez la succession numérique ou le forfait familial. Si vous n’avez jamais connecté de compte Google à KeyArmor, il n’y a aucune donnée vous concernant sur nos serveurs à supprimer.
    """.trimIndent()),
    LegalSection("Suppression depuis l'application", """
Vous pouvez supprimer toutes vos données à tout moment depuis Paramètres → Supprimer toutes mes données au sein de l’application. Si vous n’avez jamais associé de compte Google à KeyArmor (pour la sauvegarde sur Drive, l’héritage numérique ou le forfait familial), cela efface immédiatement et définitivement votre coffre-fort de l’appareil.

Si vous en avez associé un, l’application vous demande de vous connecter à Google afin de pouvoir, dans le cadre de cette même opération, effacer les enregistrements associés sur le serveur (Firebase) ainsi que les copies de sauvegarde et les copies Time Machine que KeyArmor a enregistrées sur votre Google Drive. Ce n’est que si vous ne parvenez pas à vous connecter que l’application se contente d’effacer les données locales — et elle vous avertit au préalable que vos enregistrements sur le serveur resteront en place jusqu’à ce qu’ils soient supprimés séparément, à l’aide de la requête ci-dessous.
    """.trimIndent()),
    LegalSection("Ce qui n'est pas supprimé", """
La suppression de vos données n'efface pas ce qui se trouve en dehors de l'application :

• Les fichiers que vous avez exportés vous-même (fichiers CSV, PDF ou .kab que vous avez enregistrés ou partagés). Ils vous appartiennent et sont stockés là où vous les avez placés ; vous devez donc les supprimer à cet emplacement.
• Les copies de sauvegarde qu’une installation antérieure de KeyArmor a laissées dans votre dossier « Téléchargements ». Android ne permet plus à une nouvelle installation d’y accéder ; supprimez-les de votre dossier « Téléchargements ».
• Votre abonnement. Il est géré par Google Play ; la suppression de vos données ne l’annule donc pas ; annulez-le dans Google Play → Abonnements.
    """.trimIndent()),
    LegalSection("Demande de suppression des données de votre compte", """
Si vous avez associé un compte Google à KeyArmor mais que vous ne parvenez pas à vous connecter pour utiliser la fonction de suppression intégrée à l'application mentionnée ci-dessus (par exemple, si l'application n'est plus installée sur votre appareil), demandez la suppression des données associées stockées sur nos serveurs en nous envoyant un e-mail depuis ce même compte Google.

Écrivez-nous à l'adresse suivante : support@thormakk.dev

Nous traitons les demandes de suppression dans un délai de 30 jours et vous envoyons une confirmation par e-mail une fois la suppression effectuée. Il n'est pas nécessaire que l'application soit installée pour effectuer cette demande.
    """.trimIndent())
)
