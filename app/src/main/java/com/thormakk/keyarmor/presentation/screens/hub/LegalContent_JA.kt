package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentJA(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyJA()
    "terms"      -> termsJA()
    "legal"      -> legalJA()
    "license"    -> licenseJA()
    "opensource" -> opensourceJA()
    "delete"     -> deleteDataJA()
    else         -> listOf(LegalSection("ドキュメントが見つかりません", ""))
}

private fun privacyJA() = listOf(
    LegalSection(
        "プライバシーポリシー",
        ""
    ),
    LegalSection(
        "データの収集",
        """
        KeyArmorは、個人データを外部サーバーに収集、送信、または共有することは一切ありません。アプリに入力したすべての情報は、Google Driveの同期を明示的に有効にしない限り、お使いのデバイス上にのみ保存されます。

        KeyArmorは、コア機能（ボルト、オートフィル、セキュリティ監査、侵害モニタリング）についてはメールアドレスやアカウントの認証情報を一切収集しません — これらはアカウントなしで完全に動作します。デジタル相続またはファミリープランを有効にする場合、これらの機能にはGoogleサインインが必要です：GoogleアカウントのメールアドレスはGoogle Firebaseに保存され、あなたを識別し、指定した相続人や家族メンバーと紐付けるため、また当社のメールプロバイダー（Brevo）を通じて該当する通知メールを送信するために使用されます。このメールアドレスはこれら2つの機能のみに使用され、他者に販売または共有されることは一切ありません。デジタル相続とファミリープラン以外では、唯一の個人識別子は、セットアップ時に任意で設定できる名前またはニックネームであり、デバイス上にのみ暗号化されて保存されます。
        """.trimIndent()
    ),
    LegalSection(
        "ローカルストレージ",
        """
        すべてのデータ（パスワード、パスキー、メモ、クレジットカード、銀行口座、暗号ウォレット、添付ファイル、パスワード履歴）は、Argon2idによってマスターパスワードから導出され、デバイスの外に出ることのないキーを使用して、AES-256-GCMで暗号化されて保存されます。生体認証によるロック解除を有効にすると、そのキーのコピーは、お使いのデバイスのAndroid Keystoreのキーによってさらに保護されます。明示的な同意なしに、データがデバイスの外に出ることはありません。
        """.trimIndent()
    ),
    LegalSection(
        "マスターパスワード",
        """
        マスターパスワードは平文や復元可能な形式で保存されることはありません。Argon2idを介してボルト暗号化キーを導出するためのみに使用されます。マスターパスワードを紛失した場合、データの回復はできません。
        """.trimIndent()
    ),
    LegalSection(
        "生体認証",
        """
        KeyArmorにおける生体認証は、BiometricPrompt APIを介してAndroidオペレーティングシステムによって完全に管理されます。KeyArmorは、いかなる時も生体認証データにアクセス、保存、または処理することはありません。

        生体認証はアプリ内で2つのコンテキストで使用されます：

        • ボルトのロック解除：起動時のアプリへのアクセスを認証するため。全プランで利用可能。
        • 高度な生体認証保護（Premium）：ボルトの特定セクション（パスワード、クレジットカード、カスタムフィールド）の機密フィールドを表示する前に生体認証を必要とするオプションの障壁。設定から構成可能 — カスタマイズ可能なタイムアウトでアクセスごとまたはセッションごとのモードをサポートします。同じプライバシー保証が適用されます：KeyArmorは生体認証データを保存または処理しません。
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive同期",
        """
Google Drive同期は完全に任意です。有効にすると、KeyArmorは次を行います:

• あなた自身の暗号化バックアップとTime Machineの復元ポイントを、Google Drive内のアプリ専用の非公開フォルダに保存するために、単一の最小限の権限（DRIVE_APPDATA）のみを要求します。このフォルダは隠されており、他のどのアプリも、Driveを閲覧するあなた自身も、閲覧や変更はできません。
• 保管庫をアップロードする前に、12語のバックアップキーで暗号化します。このキーはご自身のデバイス上で生成され、ご本人だけが保有します。Googleも開発者も、このファイルを読み取ったり、キーを復元したりすることはできません。
• バックアップキーのPDFをGoogleドキュメント、Googleドライブ、またはその他のアプリに保存する場合は、Androidの共有画面からご自身で行ってください。そのアプリには、それぞれ独自のプライバシーポリシーが適用されます。
• キーを変更した場合、以前のキーを暗号化した状態で端末に保管しておくことができます。キットのPDFにそれらが含まれることがあります。KeyArmorがそれらをサーバーに送信することはありません。
• 他のDriveファイルには一切アクセスせず、可視フォルダを作成せず、ファイルを共有しません。
• 任意で、バックアップの2つ目のローカルコピーを端末のDownloads/KeyArmor/フォルダに保存します。これは端末から外に出ません。
• オンデマンド同期はすべてのプランで利用できます（手動実行）。自動の毎日同期はPremiumで利用できます。

デジタル相続とファミリープランでは、Google DriveではなくGoogle Firebaseが使用されます。Firebaseが、同意、招待、通知を管理します。相続用保管庫は、相続人の公開鍵を使用して端末上でエンドツーエンドで暗号化され、その結果得られた暗号化されたブロックは、有効期限が短い（15分間）署名付きリンクを介して、開発者のFirebase Cloud Storageのプライベートバケットにアップロードされます。サーバーはそのブロックを保存しますが、相続人の鍵を受け取ることは決してないため、その内容を読み取ることはできません。

同期画面からいつでもGoogleアカウントの接続を解除できます。
    """.trimIndent()
    ),
    LegalSection(
        "添付ファイル",
        """
        「添付ファイル」機能を使用すると、保管庫のエントリにファイル（1エントリにつき最大5つ、各ファイル最大5MB）を関連付けることができます。すべての添付ファイルは、デバイスの内部ストレージに保存される前にAES-256-GCMで暗号化されます。Googleドライブとの同期を有効にすると、添付ファイルやドキュメントの写真は、バックアップキーで暗号化された状態でアップロードされるバックアップに含まれます。これらは他のサービスには一切送信されません。
        """.trimIndent()
    ),
    LegalSection(
        "パスワード履歴",
        """
        KeyArmorは、各エントリの以前のパスワードのローカル履歴を保持します。この履歴はデバイス上に暗号化されて保存され、外部サーバーに送信されることはありません。
        """.trimIndent()
    ),
    LegalSection(
        "アプリ内通知",
        """
        KeyArmorは、重要なセキュリティイベントについてお知らせするために内部通知を生成します：

        • 期限切れ警告：1つ以上のパスワードが有効期限に近づいている場合に送信されます。
        • 期限切れパスワード：1つ以上のパスワードがすでに有効期限を過ぎている場合に送信されます。
        • 侵害アラート：パスワードが既知のデータ侵害で検出された場合に送信されます。

        これら3種類の通知はすべてデバイス上で生成され、外部サーバーに送信されることはありません。デジタル相続またはファミリープランを利用している場合、それらの機能に関する通知（相続人が承諾した、家族メンバーが追加されたなど）も受け取ります。これらは別のアカウントによって開始されるイベントに依存するため、Google Firebaseによってトリガーされ、Firebase Cloud Messaging経由で配信されます。発生元にかかわらず、すべての通知は30日後に自動的に削除されます。通知を横にスワイプして個別に削除するか、通知画面から一括で削除することができます。プッシュ通知を送信するにはシステム許可が必要です。
        """.trimIndent()
    ),
    LegalSection(
        "セキュリティ監査",
        """
        セキュリティ監査機能は、デバイス上でローカルにパスワードを分析します。一般的なパスワードのチェックは、アプリ自体に保存されている事前定義リストとの照合によって行われ、外部サービスへの問い合わせは一切ありません。
        """.trimIndent()
    ),
    LegalSection(
        "侵害モニタリング（HIBP）",
        """
        KeyArmorは、セキュリティ研究者Troy Huntが運営するHave I Been Pwned（HIBP）サービスを使用して、パスワードが既知のデータ侵害に登場しているかどうかを確認できます。

        このチェックはk-匿名性モデルを使用します：パスワードのSHA-1ハッシュの最初の5文字のみがHIBP APIに送信されます。完全なハッシュは送信されません。マッチングはデバイス上でローカルに行われます。ユーザー名、メールアドレス、アカウントの関連付けは一切送信されません。HIBPのレスポンスはデコイハッシュでパディングされているため、ネットワークトラフィック分析を行っても確認対象のパスワードは判明しません。
        """.trimIndent()
    ),
    LegalSection(
        "オートフィルサービス",
        """
        KeyArmorにはオプションのAndroidオートフィルサービスが含まれています。システム設定で有効にすると、現在アクティブなログイン画面のアプリパッケージ名またはウェブサイトURLを読み取り、ボルトから一致する認証情報を提案します。このマッチングは完全にデバイス上で行われます。アプリ名、ウェブサイトURL、認証情報は外部サーバーに送信されません。オートフィルサービスは、ログインコンテキストの識別に厳密に必要な範囲を超えて、他のアプリのコンテンツを読み取りません。
        """.trimIndent()
    ),
    LegalSection(
        "暗号資産ウォレットのリアルタイム残高",
        """
        暗号ウォレットエントリを保存してライブ残高モニタリングを有効にすると、KeyArmorは入力したウォレットアドレスに関連する残高を取得するために公開ブロックチェーンノードまたはAPIに問い合わせます。送信されるのはウォレットアドレスのみです。秘密鍵、シードフレーズ、個人データは送信されません。これらのクエリは公開インフラに直接送信されます。KeyArmorは中間サーバーを運営していません。
        """.trimIndent()
    ),
    LegalSection(
        "P2Pエントリ共有",
        """
        KeyArmorでは、QRコードまたは暗号化テキストを介して、他のKeyArmorユーザーとボルトエントリを直接共有できます。この転送はエンドツーエンドで暗号化されたピアツーピアです。サーバーや仲介者は関与しません。共有するエントリの相手の選択、および暗号化テキストの送信に使用するチャネルのセキュリティ（QRを直接使用しない場合）については、お客様が単独で責任を負います。
        """.trimIndent()
    ),
    LegalSection(
        "サードパーティライブラリ",
        """
        KeyArmorはオープンソースライブラリを使用しています（オープンソース表示を参照）。これらのライブラリのいずれも、このアプリで使用される設定において、テレメトリ、分析、またはデータ収集のメカニズムを含んでいません。

        KeyArmorは、特定の機能のために2つのサードパーティサービスにも依存しています：RevenueCatは、あなたのPremiumサブスクリプションの状態を確認し、購入に関連する情報のみを受け取ります（ボルト、マスターパスワード、回復キーを受け取ることは決してありません）。Brevoは、デジタル相続とファミリープランに必要なトランザクションメール（招待、同意確認、通知）を送信し、そのためにこれらの機能に関わる人々のメールアドレスを受け取ります。どちらのサービスも広告や行動分析には使用されません。
        """.trimIndent()
    ),
    LegalSection(
        "ホーム画面ウィジェット",
        """
        KeyArmorには、Androidのホーム画面にオプションのウィジェットが含まれており、保管庫に保存されているエントリの総数を表示します。このウィジェットはデバイスのローカルデータにのみアクセスします。外部サーバーへの情報送信は一切行いません。エントリ数は、個々のエントリの内容を明かさない集計データです。ウィジェットをホーム画面に配置するかどうかはご自身で選択できます。
        """.trimIndent()
    ),
    LegalSection(
        "権限",
        """
        KeyArmorは、その動作に必要な権限のみを要求します：

        • 生体認証（オプション）：指紋によるロック解除のため。
        • インターネット（オプション）：あなたが有効にした機能にのみ使用されます — Google Drive同期、デジタル相続とファミリープラン（Google Firebase経由）、暗号資産のライブ残高照会、Have I Been Pwnedによる侵害チェック、サブスクリプション検証（Google Play Billing／RevenueCat）。
        • ファイルアクセス（オプション）：ボルトのインポート/エクスポートおよび添付ファイルの追加のため（お客様が開始した場合のみ）。
        • カメラ（オプション）：二要素認証（2FA/TOTP）の設定時または共有エントリの受信時にQRコードをスキャンするため。明示的なユーザーリクエスト時のみ有効化。

        • オートフィルサービス（オプション）：Androidのシステム設定で手動で有効化します。他のアプリのログイン画面で認証情報を提案するためのみに使用されます。
        • マイク（任意）：セキュアノートの音声入力にのみ使用します。音声認識は端末上で行われ、KeyArmorは音声を録音・保存せず、サーバーにも送信しません。
        • カレンダー（読み取り専用・任意）：セーフトラベルモードが、予定の中から旅行を見つけるために使用します。予定は端末上で処理され、送信されることはありません。
        • 通知と正確なアラーム（任意）：セキュリティアラートと、ご自身が設定したリマインダーのために使用します。端末の起動イベントは、再起動後にリマインダーを再設定するために使われます。
        • ネットワーク状態（任意）：モバイルデータかWi-Fiかを判別し、大きなGoogle Driveバックアップを Wi-Fi まで待たせるためだけに使用します。ネットワークに関する情報は収集・送信されません。

        KeyArmorは、連絡先、位置情報、または上記以外の個人データへのアクセスを要求しません。
        """.trimIndent()
    ),
    LegalSection("データを受け取るその他のサービス", """
上記のサービスに加えて、KeyArmorは次のサービスを、それを必要とする機能に限って使用します。

• アプリの整合性確認（Firebase App Check / Google Play Integrity）：Googleが端末の証明を提供し、当社のサーバーが正規のアプリからのリクエストのみを受け付けるようにします。
• RevenueCat：購入情報に加え、サブスクリプションを複数の端末で関連付けるため、Googleアカウントの識別子（メールアドレスではありません）をユーザーIDとして受け取ります。
• CoinGecko：リアルタイムの暗号資産価格を使用すると、ポートフォリオのティッカーがCoinGeckoに照会され、CoinGeckoはIPアドレスを確認できます。上記のブロックチェーンエクスプローラーやノードも同様です。
• Google Fonts：アプリはGoogle Playサービス経由でInterとJetBrains Monoのフォントをダウンロードするため、その際にGoogleがIPアドレスを確認できます。
• Google Playサービスの信頼できる時刻：端末の時計の改ざんを検出するために使用します。個人データは送信されません。
• ドキュメントのテキスト認識（OCR）は、ML Kitを使って端末上で行われます。画像がサーバーに送信されることはありません。
• Ko-fi：寄付リンクを開いた場合のみ。その場合は同社のプライバシーポリシーが適用されます。

当社のサーバーは、不正利用を抑制するために、IPアドレスを一時的に記録することがあります。
    """.trimIndent()),
    LegalSection(
        "パスキー",
        """
        KeyArmorは、システム設定でお客様ご自身が選択した場合にのみ、Androidのパスキープロバイダー（Credential Manager）として機能できます。パスキーを作成または使用する際、KeyArmorは、そのパスキーをサイトに関連付け、サイトが送信するチャレンジに署名するために、要求元のサイトまたはアプリのドメインを読み取ります。秘密鍵はお使いのデバイス上で生成され、ボルトの他のデータとともに暗号化されて保存されます。サーバーや第三者に送信されることはありません。秘密鍵がデバイスの外に出るのは、お客様ご自身が作成するバックアップ（たとえばGoogle Drive上の暗号化バックアップ）に含まれる場合のみです。パスキーで署名する際、KeyArmorは指紋またはマスターパスワードによる本人確認を求めます。ログイン先のサイトが受け取るのは、他のパスキーと同様に、公開鍵と署名のみです。
        """.trimIndent()
    ),
    LegalSection(
        "データの削除と保持",
        """
        ボルトは、お客様ご自身が削除するまで、お使いのデバイス（およびバックアップを有効にしている場合はGoogle Drive）に保持されます。当社のサーバーにあるのは連携のためのデータのみです。メールアドレスで識別されるプッシュ通知トークン、そしてデジタル相続またはファミリープランをご利用の場合は、招待および同意の記録と、相続用ボルトの暗号化ブロックです。これらは、該当する機能をご利用の間、または削除をご依頼いただくまで保持されます。削除は、設定 → すべてのデータを削除 から、またはGoogleアカウントから support@thormakk.dev 宛にご連絡いただくことで行えます。これらのご依頼は30日以内に処理します。詳細は、当社ウェブサイトの「アカウントとデータの削除」ページをご覧ください。
        """.trimIndent()
    ),
    LegalSection(
        "未成年者",
        """
        KeyArmorは13歳未満のお子様（またはお住まいの国の法律でデジタル同意に必要とされる最低年齢未満の方）を対象としておらず、未成年者のデータを故意に収集することはありません。未成年者が当社にデータを提供したとお考えの場合は、support@thormakk.dev までご連絡ください。削除いたします。
        """.trimIndent()
    ),
    LegalSection(
        "お問い合わせ",
        "このポリシーに関するご質問は、support@thormakk.dev までお書きください"
    ),
    LegalSection(
        "最終更新",
        "2026年10月（第8版）"
    )
)

private fun termsJA() = listOf(
    LegalSection(
        "利用規約",
        ""
    ),
    LegalSection(
        "同意",
        """
        KeyArmorをインストールまたは使用することにより、これらの規約のすべてに同意したものとみなされます。いずれかの部分に同意しない場合は、アプリを使用しないでください。
        """.trimIndent()
    ),
    LegalSection(
        "許可された使用",
        """
        KeyArmorは、パスワードマネージャーおよびセキュアボルトとして個人使用向けに設計されています。違法な活動、違法行為を助長する情報の保存、またはアプリのリバースエンジニアリングの試みへの使用は禁止されています。
        """.trimIndent()
    ),
    LegalSection(
        "ユーザーの責任",
        """
        お客様は以下について単独で責任を負います：

        • マスターパスワードのセキュリティ。紛失した場合、回復メカニズムはなく、データは永久にアクセスできなくなります。
        • 12語のバックアップキー（その機能を利用している場合）。これを紛失し、さらにスマートフォンも紛失してしまった場合、開発者でさえもドライブのバックアップを開くことはできません。
        • エクスポート機能またはDrive同期を通じた最新のボルトバックアップの維持。
        """.trimIndent()
    ),
    LegalSection(
        "同期機能",
        """
        Google Drive同期は補助的なバックアップ機能です。適切な個人セキュリティプラクティスの代替にはなりません。この機能はサードパーティサービス（Google）に依存するため、開発者はその継続的な可用性を保証しません。
        """.trimIndent()
    ),
    LegalSection(
        "プレミアムサブスクリプション",
        """
        KeyArmorは、追加機能をアンロックするオプションの有料プラン（月額、年間）を提供しています。サブスクリプションはGoogle Play Billingを通じてのみ処理されます。サブスクリプションを購入することで、Google Playの利用規約にも同意したものとみなされます。

        • 月額プランおよび年間プランは自動更新サブスクリプションです。Google Playからいつでもキャンセルできます。キャンセルは現在の請求期間の終了時に有効になります。
        • 返金はGoogle Playの標準返金ポリシーに従います。開発者は独自に返金処理を行いません。
        • プレミアム機能は時間の経過とともに変化する場合があります。開発者は、合理的な通知をもって特定のプレミアム機能を追加、変更、または廃止する権利を留保します。
        """.trimIndent()
    ),
    LegalSection(
        "トラベルセーフモード",
        """
        トラベルセーフモードでは、特定のエントリをトラベルセーフとしてマークし、モードがアクティブな間は他のすべてのエントリを非表示にできます。どのエントリをトラベルセーフとしてマークするか、またこのモードの有効化または無効化の結果については、お客様が単独で責任を負います。開発者は、誤って表示または非表示のままになったエントリについて一切の責任を負いません。
        """.trimIndent()
    ),
    LegalSection(
        "パニックPIN",
        """
        パニックPIN機能は、指定されたPINが入力されると、不可逆的なアクション（機密ボルトデータの消去または非表示）をトリガーします。一度有効化されると、設定に応じて非表示または削除されたデータは回復できない場合があります。この機能の設定、および意図的または偶発的な有効化によるデータ損失については、お客様が全責任を負います。
        """.trimIndent()
    ),
    LegalSection(
        "暗号資産残高情報",
        """
        ライブ暗号資産残高機能は、情報提供のみを目的として公開ブロックチェーンインフラから残高情報を取得します。この情報は金融アドバイスを構成するものではありません。開発者は、残高データの正確性、完全性、または適時性を保証しません。このアプリに表示される情報のみに基づいて金融上の決定を行わないでください。
        """.trimIndent()
    ),
    LegalSection(
        "保証の否認",
        """
        アプリは「現状のまま」提供され、明示または黙示を問わず、いかなる種類の保証もありません。開発者は、アプリにエラーがないこと、中断なく動作すること、または特定の目的に適合することを保証しません。
        """.trimIndent()
    ),
    LegalSection(
        "責任の制限",
        """
        適用される法律で許容される最大限の範囲において、開発者はデータの損失、ボルトへのアクセス損失、またはアプリの使用もしくは使用不能から生じる直接的、間接的、偶発的、特別または結果的損害について責任を負いません。
        """.trimIndent()
    ),
    LegalSection(
        "規約の更新",
        """
        これらの規約はいつでも更新される場合があります。公開された更新後のアプリの継続使用は、新しい規約への同意を構成します。
        """.trimIndent()
    ),
    LegalSection(
        "最終更新",
        "2026年5月（rev. 2）"
    )
)

private fun legalJA() = listOf(
    LegalSection(
        "法的通知と免責事項",
        ""
    ),
    LegalSection(
        "現状のままのソフトウェア提供",
        """
        KeyArmorは「現状のまま」配布され、商品性、特定目的への適合性、または非侵害を含むがこれらに限定されない、明示または黙示のいかなる種類の保証もありません。
        """.trimIndent()
    ),
    LegalSection(
        "責任の制限",
        """
        KeyArmorの開発者はいかなる状況においても以下について責任を負いません：

        • アプリに保存されたデータの損失または破損
        • マスターパスワードやバックアップキーの忘れや紛失による保管庫へのアクセス不能
        • Android OSコンポーネントの脆弱性または障害
        • Google Driveサービスの障害、中断または変更
        • アプリの使用または使用不能から生じる直接的、間接的、偶発的、特別または結果的損害
        • 第三者によるユーザーデータへの不正アクセスから生じる損害
        • パニックPIN機能の意図的または偶発的な有効化によるデータ損失
        """.trimIndent()
    ),
    LegalSection(
        "Google Driveへのデータ送信",
        """
ユーザーがGoogle Drive同期（バックアップ）を有効にする、またはデジタル相続／ファミリープラン（Google Firebase）を使用すると、データはGoogleの利用規約に基づき、すでに暗号化された状態でGoogleのインフラへ送信されます。いずれの場合も、データは不透明な暗号化ブロブとして端末を離れます。開発者はGoogleのインフラにアクセスも制御もできず、送信された内容を読み取ることはできません。
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystoreとハードウェア依存性",
        """
        KeyArmorの暗号化セキュリティは、部分的にユーザーのデバイスのAndroid Keystore実装に依存しています。この実装はメーカー、チップセット、Androidバージョンによって異なります。開発者はサードパーティハードウェア実装のセキュリティを制御または保証しません。デバイスのOSを最新の状態に保つことを強くお勧めします。
        """.trimIndent()
    ),
    LegalSection(
        "オートフィルサービスの制限",
        """
        KeyArmorのオートフィルサービスは、すべてのアプリケーションまたはブラウザで正常に機能しない場合があります。特に、Google ChromeはAndroidの標準オートフィルフレームワークをサポートしていません。これはすべてのサードパーティパスワードマネージャーに等しく影響するChromeの制限であり、開発者のコントロール外です。開発者は、オートフィルサービスが特定のアプリケーションで機能することを保証しません。
        """.trimIndent()
    ),
    LegalSection(
        "暗号資産残高免責事項",
        """
        ライブ残高データは公開されているサードパーティのブロックチェーンノードおよびAPIから取得されます。開発者はこれらのノードを運営または制御しません。残高情報は情報提供のみを目的として提供されており、遅延、不正確、または利用不可能な場合があります。この情報は金融または投資アドバイスを構成するものではありません。開発者は、アプリに表示される残高データに基づいて行われた決定について一切の責任を負いません。
        """.trimIndent()
    ),
    LegalSection(
        "P2P共有",
        """
        P2Pエントリ共有機能は、ユーザー間でのボルトエントリの直接的な暗号化された交換を容易にします。開発者は、ユーザー間で共有されるエントリの内容、受信者のアイデンティティや信頼性、またはボルトデータを第三者と共有することから生じるいかなる結果についても責任を負いません。
        """.trimIndent()
    ),
    LegalSection(
        "サードパーティライブラリ",
        """
        KeyArmorは、公開時点でセキュアとみなされるサードパーティのオープンソースライブラリを使用しています。開発者は、サードパーティコンポーネントに起因する障害、脆弱性、または予期しない動作について責任を負いません。
        """.trimIndent()
    ),
    LegalSection(
        "準拠法",
        """
        この法的通知は、アルゼンチン共和国で施行されている法律に準拠します。アプリの使用から生じるいかなる紛争も、その管轄の裁判所に提出されるものとします。
        """.trimIndent()
    ),
    LegalSection(
        "最終更新",
        "2026年5月（rev. 2）"
    )
)

private fun licenseJA() = listOf(
    LegalSection("Source Availableライセンス", ""),
    LegalSection("セキュリティ監査目的のみで公開されているソースコード", """
KeyArmorのソースコードは、セキュリティ研究者やユーザーがそのセキュリティ対策を検査・監査できるようにするためだけに公開されています。それ以外の権利は一切付与されません — すべての権利は著作権者に留保されます。
    """.trimIndent()),
    LegalSection("できること", """
• セキュリティ研究および個人的な監査のためにソースコードを読み、検査する
• 公式チャンネルを通じてKeyArmorチームにセキュリティ脆弱性を報告する
    """.trimIndent()),
    LegalSection("できないこと", """
著作権者の事前の明示的な書面による許可なしに、次のことはできません:

• 公式配布以外の方法でソフトウェアを使用または実行すること
• 閲覧以外の目的でソースコードをコピー、複製、抽出すること
• それを改変、翻案、翻訳、またはそれに基づく二次的著作物を作成すること
• それを第三者に配布、公開、サブライセンス、販売、またはその他の方法で利用可能にすること
• ソースコードを他の製品、サービス、またはプロジェクトの基礎として使用すること
• KeyArmorの名前、ロゴ、または関連する商標を使用すること
    """.trimIndent()),
    LegalSection("ソースコード", """
KeyArmorのソースコードは公開されています。リポジトリのリンクについては、アプリの公式チャンネルを確認してください。
    """.trimIndent()),
    LegalSection("無保証", """
ソースコードは「現状のまま」提供され、明示または黙示を問わずいかなる種類の保証もありません。著作権者は特定の目的への適合性について一切の表明を行いません。
    """.trimIndent()),
    LegalSection("著作権", "© 2026 KeyArmor。全権利留保。Source Available。")
)

private fun opensourceJA() = listOf(
    LegalSection(
        "オープンソース表示",
        ""
    ),
    LegalSection(
        "使用ライブラリ",
        """
        KeyArmorは以下のオープンソースライブラリを使用しています。特に記載のない限り、Apache 2.0ライセンスで配布されています。
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        バージョン: BOM 2026.09.00 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        バージョン: 1.2.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        バージョン: 2.8.5 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        バージョン: 2.60.1 | ライセンス: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        バージョン: 1.11.0 | ライセンス: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        バージョン: 1.1.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        バージョン: 2.10.2 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        バージョン: 2.11.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        バージョン: 2.12.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        バージョン: 22.0.0 | ライセンス: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        バージョン: 2.9.2 | ライセンス: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        バージョン: v3-rev20240123-2.0.0 | ライセンス: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        バージョン: 2.2.0 | ライセンス: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        バージョン: 4.3.0 | ライセンス: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        バージョン: 0.37.3 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        バージョン: BOM 2026.09.00 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        バージョン: 1.19.1 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        バージョン: 1.13.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        バージョン: 1.3.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        バージョン: 1.3.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        バージョン: 1.8.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        バージョン: 1.4.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        バージョン: 2.4.21 | ライセンス: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        バージョン: 1.2.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        バージョン: 1.4.1 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        バージョン: 1.6.0 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        バージョン: 1.6.2 | ライセンス: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        バージョン: BOM 35.1.0 | ライセンス: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        バージョン: 2.15.0 | ライセンス: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        バージョン: 3.0.1 | ライセンス: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        バージョン: 2.0.27.0 | ライセンス: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        バージョン: 1.2.1 | ライセンス: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        バージョン: 1.86 | ライセンス: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        バージョン: 10.29.0 | ライセンス: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        バージョン: 5.2.0 | ライセンス: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        バージョン: 5.19.1 | ライセンス: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        バージョン: 1.2.1 | ライセンス: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        バージョン: 16.0.1 | ライセンス: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        バージョン: 16.0.1 | ライセンス: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        バージョン: 2026-07-09 | ライセンス: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Apache 2.0ライセンス全文",
        """
        Apache 2.0ライセンスの全文は以下で入手できます：
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataJA() = listOf(
    LegalSection("アカウントとデータの削除", ""),
    LegalSection("KeyArmorがユーザーについて保存している情報", """
ユーザーのデータ保管庫（パスワード、メモ、カード、ウォレット、添付ファイル、パスワード履歴）は、当社のサーバーには一切保存されません。これらはユーザーのデバイス上で暗号化された状態で保存され、バックアップ機能を有効にした場合は、ユーザー自身のGoogleドライブにも保存されます。データはデバイスから送信される前に暗号化されるため、当社がそれを読み取ったり削除したりすることはできません。

Googleアカウントに関連付けられ、当社のサーバー（Firebase）に送信されるデータは、オプション機能（Googleドライブバックアップ、デジタル遺産、ファミリープラン）の利用のためにそのアカウントを連携させる際に必要なもののみです。これらの機能のいずれかにGoogleアカウントを連携させるだけで、（相続設定やファミリープランの設定を行う前であっても）最小限の記録（メールアドレスをキーとするプッシュ通知トークン）がサーバーに登録されます。KeyArmorにGoogleアカウントを一度も連携させたことがない場合、当社のサーバーには削除すべきお客様のデータは一切存在しません。
    """.trimIndent()),
    LegalSection("アプリからの削除", """
アプリ内の「設定 → すべてのデータを削除」から、いつでもすべてのデータを削除できます。KeyArmorにGoogleアカウントを一度も連携したことがない場合（Driveバックアップ、デジタル遺産、ファミリープランなど）、これによりデバイス上の保管庫が即座に、かつ完全に削除されます。

Googleアカウントを連携している場合、アプリはGoogleへのサインインを求めます。これにより、同じ操作で、関連するサーバー側の記録（Firebase）およびKeyArmorがGoogleドライブに保存したバックアップとTime Machineのコピーを削除できます。サインインできない場合のみ、アプリはローカルデータの削除のみに切り替わります。その際、サーバー側の記録は、以下のリクエストを使用して別途削除するまで残ることになる旨、事前に警告が表示されます。
    """.trimIndent()),
    LegalSection("削除されないもの", """
データを削除しても、アプリの外にあるものは削除されません：

• ご自身でエクスポートしたファイル（保存または共有したCSV、PDF、または.kabファイル）。これらはご自身の所有物であり、保存した場所に保管されているため、その場所で削除する必要があります。
• 以前の KeyArmor インストール時に「ダウンロード」フォルダに残されたバックアップコピー。Android では、新しいインストール時にこれらにアクセスできなくなっています。ダウンロードフォルダから削除してください。
• サブスクリプション。これは Google Play によって管理されているため、データを削除しても解約されません。Google Play → 「サブスクリプション」から解約してください。
    """.trimIndent()),
    LegalSection("アカウントデータの削除依頼", """
KeyArmorにGoogleアカウントを連携しているものの、上記のアプリ内削除機能を利用するためにログインできない場合（例えば、アプリがインストールされていない場合など）、そのGoogleアカウントから当社宛てにメールを送信し、関連するサーバー側のデータの削除を依頼してください。

宛先：support@thormakk.dev

削除依頼は30日以内に処理し、完了次第メールにてご連絡いたします。この依頼を行うために、アプリがインストールされている必要はありません。
    """.trimIndent())
)
