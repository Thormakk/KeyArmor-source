package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentKO(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyKO()
    "terms"      -> termsKO()
    "legal"      -> legalKO()
    "license"    -> licenseKO()
    "opensource" -> opensourceKO()
    "delete"     -> deleteDataKO()
    else         -> listOf(LegalSection("문서를 찾을 수 없습니다", ""))
}

private fun privacyKO() = listOf(
    LegalSection(
        "개인정보 처리방침",
        ""
    ),
    LegalSection(
        "데이터 수집",
        """
        KeyArmor는 개인 데이터를 외부 서버에 수집, 전송 또는 공유하지 않습니다. 앱에 입력한 모든 정보는 Google Drive 동기화를 명시적으로 활성화하지 않는 한 기기에만 저장됩니다.

        KeyArmor는 핵심 기능(볼트, 자동 채우기, 보안 감사, 유출 모니터링)에 대해서는 이메일 주소나 계정 자격 증명을 전혀 수집하지 않습니다 — 이러한 기능은 계정 없이 완전히 작동합니다. 디지털 상속이나 패밀리 요금제를 활성화하는 경우, 이러한 기능에는 Google 로그인이 필요합니다. 이 경우 Google 계정 이메일 주소가 Google Firebase에 저장되어 사용자를 식별하고 지정한 상속인 또는 가족 구성원과 연결하며, 저희 이메일 제공업체(Brevo)를 통해 해당 알림 이메일을 보내는 데 사용됩니다. 이 이메일은 이 두 가지 기능에만 사용되며 다른 누구에게도 판매되거나 공유되지 않습니다. 디지털 상속과 패밀리 요금제를 제외하면, 유일한 개인 식별자는 설정 중 선택적으로 설정할 수 있는 이름 또는 닉네임이며, 이는 기기에만 암호화된 형태로 저장됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "로컬 저장소",
        """
        모든 데이터(비밀번호, 패스키, 메모, 신용카드, 은행 계좌, 암호화폐 지갑, 첨부 파일 및 비밀번호 기록)는 Argon2id를 통해 마스터 비밀번호에서 파생되며 기기를 벗어나지 않는 키를 사용하여 AES-256-GCM으로 암호화되어 저장됩니다. 생체 인식 잠금 해제를 활성화하면 해당 키의 사본이 기기의 Android Keystore 키로 추가 보호됩니다. 명시적인 동의 없이는 어떠한 데이터도 기기를 벗어나지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "마스터 비밀번호",
        """
        마스터 비밀번호는 평문이나 복구 가능한 형태로 저장되지 않습니다. Argon2id를 통해 볼트 암호화 키를 파생하는 데에만 사용됩니다. 마스터 비밀번호를 분실하면 데이터를 복구할 수 없습니다.
        """.trimIndent()
    ),
    LegalSection(
        "생체 인식",
        """
        KeyArmor의 생체 인식 인증은 BiometricPrompt API를 통해 Android 운영 체제에서 전적으로 관리됩니다. KeyArmor는 어떠한 경우에도 생체 인식 데이터에 액세스하거나 저장하거나 처리하지 않습니다.

        생체 인식은 앱 내 두 가지 상황에서 사용됩니다:

        • 볼트 잠금 해제: 시작 시 앱 접근을 인증하기 위해 사용되며, 모든 플랜에서 이용 가능합니다.
        • 고급 생체 인식 보호 (Premium): 볼트의 특정 섹션(비밀번호, 신용카드, 사용자 정의 필드)의 민감한 필드를 표시하기 전에 생체 인식 확인을 요구하는 선택적 장벽입니다. 설정에서 구성 가능 — 사용자 지정 시간 초과와 함께 접근별 또는 세션별 모드를 지원합니다. 동일한 개인정보 보호 보장이 적용됩니다: KeyArmor는 생체 인식 데이터를 저장하거나 처리하지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive 동기화",
        """
Google Drive 동기화는 완전히 선택 사항입니다. 활성화하면 KeyArmor는 다음을 수행합니다:

• 사용자의 암호화된 백업과 Time Machine 복원 지점을 Google Drive 내 앱 전용 비공개 폴더에 저장하기 위해 단일 최소 권한(DRIVE_APPDATA)만 요청합니다. 이 폴더는 숨겨져 있어 다른 어떤 앱도, Drive를 탐색하는 사용자 본인도 볼 수 없고 수정할 수 없습니다.
• 보관함를 업로드하기 전에 12단어 백업 키로 보관함를 암호화합니다. 이 키는 사용자의 기기에서 생성되며, 오직 사용자 본인만이 이를 보유합니다. 구글이나 개발자 모두 해당 파일을 읽거나 키를 복구할 수 없습니다.
• 백업 키가 포함된 PDF 파일을 Google 문서, Google 드라이브 또는 다른 앱에 저장하는 경우, 안드로이드 공유 화면을 통해 직접 저장해야 하며, 해당 앱은 자체 개인정보 처리방침의 적용을 받습니다.
• 키를 변경하면 이전 키를 암호화된 상태로 휴대폰에 보관할 수 있으며, 키트 PDF에 포함될 수 있습니다. KeyArmor는 이전 키를 어떤 서버로도 전송하지 않습니다.
• 다른 Drive 파일에는 절대 접근하지 않고, 표시되는 폴더를 만들지 않으며, 파일을 공유하지 않습니다.
• 선택적으로 백업의 두 번째 로컬 사본을 기기의 Downloads/KeyArmor/ 폴더에 보관하며, 이는 기기를 절대 벗어나지 않습니다.
• 온디맨드 동기화는 모든 요금제에서 제공됩니다(수동 실행). 자동 일일 동기화는 Premium에서 제공됩니다.

디지털 상속과 패밀리 요금제는 사용자의 Google 드라이브가 아닌 Google Firebase를 사용합니다. Firebase는 동의, 초대 및 알림을 관리합니다. 상속 금고는 상속인의 공개 키를 사용하여 기기에서 종단 간 암호화되며, 이렇게 생성된 암호화된 블록은 유효 기간이 짧은(15분) 서명된 링크를 통해 개발자의 Firebase Cloud Storage 비공개 버킷에 업로드됩니다. 서버는 해당 블록을 저장하지만 상속인의 키를 절대 수신하지 않으므로, 이를 읽을 수 없습니다.

동기화 화면에서 언제든지 Google 계정 연결을 해제할 수 있습니다.
    """.trimIndent()
    ),
    LegalSection(
        "첨부 파일",
        """
        첨부 파일 기능을 사용하면 보관함 항목에 파일(항목당 최대 5개, 각 파일당 최대 5MB)을 연결할 수 있습니다. 모든 첨부 파일은 기기의 내부 저장소에 저장되기 전에 AES-256-GCM 방식으로 암호화됩니다. Google 드라이브 동기화를 활성화하면, 첨부 파일과 문서 사진이 백업 키로 암호화된 상태로 업로드되는 백업에 포함됩니다. 이러한 파일은 다른 서비스로 전송되지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "비밀번호 기록",
        """
        KeyArmor는 각 항목에 대한 이전 비밀번호의 로컬 기록을 유지합니다. 이 기록은 기기에 암호화되어 저장되며 외부 서버로 전송되지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "인앱 알림",
        """
        KeyArmor는 중요한 보안 이벤트에 대해 알려주기 위해 내부 알림을 생성합니다:

        • 만료 경고: 하나 이상의 비밀번호가 만료 날짜에 가까워질 때 전송됩니다.
        • 만료된 비밀번호: 하나 이상의 비밀번호가 이미 만료 날짜를 지났을 때 전송됩니다.
        • 침해 알림: 비밀번호가 알려진 데이터 침해에서 발견될 때 전송됩니다.

        이 세 가지 유형의 알림은 모두 기기에서 생성되며 외부 서버로 전송되지 않습니다. 디지털 상속이나 패밀리 요금제를 사용하는 경우, 해당 기능과 관련된 알림(상속인이 수락함, 가족 구성원이 추가됨 등)도 받게 됩니다. 이러한 알림은 다른 계정에서 시작된 이벤트에 의존하므로 Google Firebase에서 트리거되어 Firebase Cloud Messaging을 통해 전달됩니다. 출처에 관계없이 모든 알림은 30일 후 자동으로 삭제됩니다. 옆으로 스와이프하여 알림을 개별 삭제하거나 알림 화면에서 한 번에 모두 지울 수 있습니다. 푸시 알림을 보내려면 시스템 권한이 필요합니다.
        """.trimIndent()
    ),
    LegalSection(
        "보안 감사",
        """
        보안 감사 기능은 기기에서 로컬로 비밀번호를 분석합니다. 일반적인 비밀번호 확인은 외부 서비스에 쿼리하지 않고 앱 자체에 저장된 사전 정의된 목록과 비교하여 수행됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "침해 모니터링 (HIBP)",
        """
        KeyArmor는 보안 연구원 Troy Hunt가 운영하는 Have I Been Pwned(HIBP) 서비스를 사용하여 비밀번호가 알려진 데이터 침해에 나타났는지 확인할 수 있습니다.

        이 검사는 k-익명성 모델을 사용합니다: 비밀번호의 SHA-1 해시의 처음 5자만 HIBP API로 전송됩니다. 전체 해시는 전송되지 않습니다. 매칭은 기기에서 로컬로 수행됩니다. 사용자 이름, 이메일 주소 또는 계정 연결 정보는 전혀 전송되지 않습니다. HIBP 응답에는 디코이 해시가 패딩되어 있어 네트워크 트래픽 분석으로도 확인 중인 비밀번호를 알 수 없습니다.
        """.trimIndent()
    ),
    LegalSection(
        "자동 채우기 서비스",
        """
        KeyArmor에는 선택적 Android 자동 채우기 서비스가 포함되어 있습니다. 시스템 설정에서 활성화하면 현재 활성화된 로그인 화면의 앱 패키지 이름 또는 웹사이트 URL을 읽어 보관함에서 일치하는 자격 증명을 제안합니다. 이 매칭은 완전히 기기에서 수행됩니다. 앱 이름, 웹사이트 URL 또는 자격 증명은 외부 서버로 전송되지 않습니다. 자동 채우기 서비스는 로그인 컨텍스트를 식별하는 데 엄격히 필요한 범위를 넘어 다른 앱의 콘텐츠를 읽지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "암호화폐 지갑 실시간 잔액",
        """
        암호화폐 지갑 항목을 저장하고 실시간 잔액 모니터링을 활성화하면, KeyArmor는 입력한 지갑 주소와 관련된 잔액을 가져오기 위해 공개 블록체인 노드 또는 API에 쿼리합니다. 지갑 주소만 전송됩니다. 개인 키, 시드 구문 또는 개인 데이터는 전송되지 않습니다. 이러한 쿼리는 공개 인프라로 직접 전송됩니다. KeyArmor는 중간 서버를 운영하지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "P2P 항목 공유",
        """
        KeyArmor를 사용하면 QR 코드 또는 암호화된 텍스트를 통해 다른 KeyArmor 사용자와 개별 보관함 항목을 직접 공유할 수 있습니다. 이 전송은 엔드투엔드 암호화 방식의 피어투피어입니다. 서버나 중간자가 관여하지 않습니다. 공유 대상을 선택하고 암호화된 텍스트 전송에 사용되는 채널의 보안(QR을 직접 사용하지 않는 경우)에 대한 책임은 전적으로 귀하에게 있습니다.
        """.trimIndent()
    ),
    LegalSection(
        "서드파티 라이브러리",
        """
        KeyArmor는 오픈 소스 라이브러리를 사용합니다(오픈 소스 고지 참조). 이러한 라이브러리 중 어느 것도 이 앱에서 사용되는 구성에서 원격 측정, 분석 또는 데이터 수집 메커니즘을 포함하지 않습니다.

        KeyArmor는 특정 기능을 위해 두 개의 서드파티 서비스에도 의존합니다: RevenueCat은 Premium 구독 상태를 확인하며 구매 관련 정보만 받습니다(볼트, 마스터 비밀번호, 복구 키는 절대 받지 않습니다). Brevo는 디지털 상속과 패밀리 요금제에 필요한 거래 이메일(초대, 동의 확인, 알림)을 발송하며, 이를 위해 해당 기능에 관련된 사람들의 이메일 주소를 받습니다. 두 서비스 모두 광고나 행동 분석에는 사용되지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "홈 화면 위젯",
        """
        KeyArmor에는 선택적 Android 홈 화면 위젯이 포함되어 있으며, 이 위젯은 보관함에 저장된 총 항목 수를 표시합니다. 이 위젯은 기기의 로컬 데이터에만 접근합니다. 외부 서버로 어떠한 정보도 전송하지 않습니다. 항목 수는 개별 항목의 내용을 공개하지 않는 집계 데이터입니다. 홈 화면에 위젯을 배치할지 여부는 사용자가 직접 선택할 수 있습니다.
        """.trimIndent()
    ),
    LegalSection(
        "권한",
        """
        KeyArmor는 운영에 필요한 권한만 요청합니다:

        • 생체 인식(선택 사항): 지문 잠금 해제를 위해.
        • 인터넷(선택 사항): 사용자가 직접 활성화한 기능에만 사용됩니다 — Google Drive 동기화, 디지털 상속 및 패밀리 요금제(Google Firebase 경유), 암호화폐 지갑 실시간 잔액 조회, Have I Been Pwned를 통한 유출 확인, 구독 확인(Google Play Billing / RevenueCat).
        • 파일 액세스(선택 사항): 볼트 가져오기/내보내기 및 첨부 파일 추가를 위해, 사용자가 시작할 때만.
        • 카메라(선택 사항): 이중 인증(2FA/TOTP) 설정 시 또는 공유 항목 수신 시 QR 코드를 스캔하기 위해. 명시적인 사용자 요청 시에만 활성화됩니다.

        • 자동 채우기 서비스(선택 사항): Android 시스템 설정에서 수동으로 활성화합니다. 다른 앱의 로그인 화면에서 자격 증명을 제안하는 용도로만 사용됩니다.
        • 마이크(선택): 보안 노트의 음성 받아쓰기에만 사용됩니다. 음성 인식은 기기에서 이루어지며, KeyArmor는 오디오를 녹음·저장하지 않고 어떤 서버로도 전송하지 않습니다.
        • 캘린더, 읽기 전용(선택): 안전 여행 모드가 일정에서 여행을 찾기 위해 사용합니다. 일정은 기기에서 처리되며 전송되지 않습니다.
        • 알림 및 정확한 알람(선택): 보안 알림과 직접 설정한 미리 알림을 위해 사용됩니다. 기기 부팅 이벤트는 재시작 후 미리 알림을 다시 예약하는 데 사용됩니다.
        • 네트워크 상태(선택): 모바일 데이터인지 Wi-Fi인지 확인하여 큰 Google Drive 백업이 Wi-Fi를 기다리도록 하는 용도로만 사용됩니다. 네트워크에 대한 정보는 수집되거나 전송되지 않습니다.

        KeyArmor는 연락처, 위치 정보 또는 위에 언급되지 않은 기타 개인 데이터에 대한 액세스를 요청하지 않습니다.
        """.trimIndent()
    ),
    LegalSection("데이터를 받는 기타 서비스", """
위에 설명한 서비스 외에, KeyArmor는 다음 서비스를 해당 기능에 필요한 경우에만 사용합니다.

• 앱 무결성 확인(Firebase App Check / Google Play Integrity): Google이 기기 증명을 제공하여 당사 서버가 정품 앱의 요청만 수락하도록 합니다.
• RevenueCat: 구매 정보 외에, 여러 기기에서 구독을 연결하기 위해 Google 계정 식별자(이메일 주소가 아님)를 사용자 ID로 받습니다.
• CoinGecko: 실시간 암호화폐 시세를 사용하면 포트폴리오의 티커가 CoinGecko에 요청되며, CoinGecko는 IP 주소를 볼 수 있습니다. 위에서 언급한 블록체인 탐색기와 노드도 볼 수 있습니다.
• Google Fonts: 앱은 Google Play 서비스를 통해 Inter 및 JetBrains Mono 글꼴을 다운로드하므로, 요청 시 Google이 IP 주소를 볼 수 있습니다.
• Google Play 서비스 신뢰 시간: 기기 시계 조작을 감지하는 데 사용됩니다. 개인 데이터는 전송되지 않습니다.
• 문서의 텍스트 인식(OCR)은 ML Kit를 사용해 기기에서 수행됩니다. 이미지는 서버로 전송되지 않습니다.
• Ko-fi: 후원 링크를 여는 경우에만 해당하며, 이 경우 해당 서비스의 개인정보 처리방침이 적용됩니다.

당사 서버는 악용을 제한하기 위해 IP 주소를 일시적으로 기록할 수 있습니다.
    """.trimIndent()),
    LegalSection(
        "패스키",
        """
        KeyArmor는 사용자가 시스템 설정에서 직접 선택한 경우에만 Android 패스키 제공자(Credential Manager)로 작동할 수 있습니다. 패스키를 만들거나 사용할 때 KeyArmor는 해당 사이트에 패스키를 연결하고 사이트가 보낸 챌린지에 서명하기 위해 요청한 사이트 또는 앱의 도메인을 읽습니다. 개인 키는 기기에서 생성되어 볼트의 나머지 데이터와 함께 암호화되어 저장되며, 어떤 서버나 제3자에게도 전송되지 않습니다. 개인 키는 사용자가 직접 만드는 백업(예: Google Drive의 암호화된 백업)에 포함될 때만 기기를 벗어납니다. 패스키로 서명하려면 KeyArmor가 지문 또는 마스터 비밀번호로 본인 확인을 요청합니다. 로그인하는 사이트는 다른 패스키와 마찬가지로 공개 키와 서명만 받습니다.
        """.trimIndent()
    ),
    LegalSection(
        "데이터 삭제 및 보관",
        """
        볼트는 사용자가 직접 삭제할 때까지 기기(및 백업을 활성화한 경우 Google Drive)에 보관됩니다. 당사 서버에는 조정을 위한 데이터만 있습니다. 이메일로 식별되는 푸시 알림 토큰과, 디지털 상속이나 패밀리 요금제를 사용하는 경우 초대 및 동의 기록과 상속 볼트의 암호화된 블록입니다. 이 데이터는 해당 기능을 사용하는 동안 또는 삭제를 요청할 때까지 보관됩니다. 설정 → 내 모든 데이터 삭제에서 삭제하거나, Google 계정으로 support@thormakk.dev에 이메일을 보내 삭제할 수 있으며, 이러한 요청은 30일 이내에 처리합니다. 자세한 내용은 당사 웹사이트의 「계정 및 데이터 삭제」 페이지에서 확인할 수 있습니다.
        """.trimIndent()
    ),
    LegalSection(
        "미성년자",
        """
        KeyArmor는 만 13세 미만 아동(또는 거주 국가의 법률이 디지털 동의를 위해 정한 최소 연령 미만의 사용자)을 대상으로 하지 않으며, 미성년자의 데이터를 알면서 수집하지 않습니다. 미성년자가 데이터를 제공했다고 생각되면 support@thormakk.dev로 문의해 주세요. 삭제하겠습니다.
        """.trimIndent()
    ),
    LegalSection(
        "연락처",
        "이 정책과 관련된 문의 사항은 support@thormakk.dev로 이메일을 보내주세요"
    ),
    LegalSection(
        "최종 업데이트",
        "2026년 10월 (개정 8)"
    )
)

private fun termsKO() = listOf(
    LegalSection(
        "이용 약관",
        ""
    ),
    LegalSection(
        "동의",
        """
        KeyArmor를 설치하거나 사용함으로써 이 약관 전체에 동의하는 것으로 간주됩니다. 어떤 부분에도 동의하지 않는 경우 앱을 사용하지 마십시오.
        """.trimIndent()
    ),
    LegalSection(
        "허용된 사용",
        """
        KeyArmor는 비밀번호 관리자 및 보안 볼트로 개인 사용을 위해 설계되었습니다. 불법 활동, 불법적인 행위를 조장하는 정보 저장 또는 앱의 역공학 시도에 사용하는 것은 금지됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "사용자 책임",
        """
        다음에 대해 전적으로 책임을 집니다:

        • 마스터 비밀번호의 보안. 분실 시 복구 메커니즘이 없으며 데이터에 영구적으로 액세스할 수 없게 됩니다.
        • 12단어 백업 키(해당 기능을 사용하는 경우). 이 키를 분실하고 휴대폰까지 잃어버리면, 개발자조차도 드라이브 백업을 열 수 없습니다.
        • 내보내기 기능 또는 Drive 동기화를 통한 볼트의 최신 백업 유지.
        """.trimIndent()
    ),
    LegalSection(
        "동기화 기능",
        """
        Google Drive 동기화는 보조 백업 기능입니다. 우수한 개인 보안 관행을 대체하지 않습니다. 개발자는 이 기능의 지속적인 가용성을 보장하지 않으며, 이는 서드파티 서비스(Google)에 의존하기 때문입니다.
        """.trimIndent()
    ),
    LegalSection(
        "프리미엄 구독",
        """

        • 월간 및 연간 플랜은 자동 갱신 구독입니다. Google Play를 통해 언제든지 취소할 수 있습니다. 취소는 현재 청구 기간이 끝날 때 적용됩니다.
        • 환불은 Google Play의 표준 환불 정책에 따릅니다. 개발자는 독립적으로 환불을 처리하지 않습니다.
        • 프리미엄 기능은 시간이 지남에 따라 변경될 수 있습니다. 개발자는 합리적인 통지를 통해 특정 프리미엄 기능을 추가, 수정 또는 중단할 권리를 보유합니다.
        """.trimIndent()
    ),
    LegalSection(
        "여행 안전 모드",
        """
        여행 안전 모드를 사용하면 특정 항목을 여행 안전으로 표시하고 모드가 활성화된 동안 다른 모든 항목을 숨길 수 있습니다. 어떤 항목을 여행 안전으로 표시할지, 이 모드를 활성화 또는 비활성화한 결과에 대한 책임은 전적으로 귀하에게 있습니다. 개발자는 실수로 표시되거나 숨겨진 항목에 대해 어떠한 책임도 지지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "패닉 PIN",
        """
        패닉 PIN 기능은 지정된 PIN이 입력될 때 되돌릴 수 없는 작업(민감한 보관함 데이터 삭제 또는 숨기기)을 트리거합니다. 일단 활성화되면 구성에 따라 숨겨지거나 삭제된 데이터를 복구하지 못할 수 있습니다. 이 기능을 구성하고 의도적 또는 우발적 활성화로 인한 데이터 손실에 대한 전적인 책임은 귀하에게 있습니다.
        """.trimIndent()
    ),
    LegalSection(
        "암호화폐 잔액 정보",
        """
        실시간 암호화폐 잔액 기능은 정보 제공 목적으로만 공개 블록체인 인프라에서 잔액 정보를 가져옵니다. 이 정보는 금융 조언을 구성하지 않습니다. 개발자는 잔액 데이터의 정확성, 완전성 또는 적시성을 보장하지 않습니다. 이 앱에 표시된 정보만을 근거로 금융 결정을 내리지 마십시오.
        """.trimIndent()
    ),
    LegalSection(
        "무보증",
        """
        앱은 명시적이든 묵시적이든 어떠한 종류의 보증도 없이 "있는 그대로" 제공됩니다. 개발자는 앱에 오류가 없거나 중단 없이 작동하거나 특정 목적에 적합하다는 것을 보증하지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "책임 제한",
        """
        적용 가능한 법률이 허용하는 최대 범위 내에서, 개발자는 데이터 손실, 볼트 액세스 손실 또는 앱의 사용이나 사용 불가로 인한 직접적, 간접적, 우발적, 특별 또는 결과적 손해에 대해 책임을 지지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "약관 업데이트",
        """
        이 약관은 언제든지 업데이트될 수 있습니다. 게시된 업데이트 이후 앱을 계속 사용하면 새로운 약관에 동의한 것으로 간주됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "최종 업데이트",
        "2026년 5월 (rev. 2)"
    )
)

private fun legalKO() = listOf(
    LegalSection(
        "법적 고지 및 면책 조항",
        ""
    ),
    LegalSection(
        "있는 그대로 제공되는 소프트웨어",
        """
        KeyArmor는 상품성, 특정 목적에의 적합성 또는 비침해에 대한 보증을 포함하되 이에 한정되지 않는 명시적이든 묵시적이든 어떠한 종류의 보증도 없이 "있는 그대로" 배포됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "책임 제한",
        """
        KeyArmor 개발자는 어떠한 상황에서도 다음에 대해 책임을 지지 않습니다:

        • 앱에 저장된 데이터의 손실 또는 손상
        • 마스터 비밀번호 또는 백업 키를 잊어버리거나 분실하여 보관함에 접근할 수 없게 되는 경우
        • Android OS 구성 요소의 취약점 또는 장애
        • Google Drive 서비스의 장애, 중단 또는 변경
        • 앱의 사용 또는 사용 불가로 인한 직접적, 간접적, 우발적, 특별 또는 결과적 손해
        • 제3자의 사용자 데이터에 대한 무단 액세스로 인한 손해
        • 패닉 PIN 기능의 의도적 또는 우발적 활성화로 인한 데이터 손실
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive 데이터 전송",
        """
사용자가 Google Drive 동기화(백업)를 활성화하거나 디지털 상속 / 패밀리 요금제(Google Firebase)를 사용하면, 데이터는 Google 서비스 약관에 따라 이미 암호화된 상태로 Google 인프라로 전송됩니다. 모든 경우에 데이터는 불투명한 암호화 블롭으로 기기를 떠납니다. 개발자는 Google 인프라에 접근하거나 이를 제어할 수 없으며 전송된 내용을 읽을 수 없습니다.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore 및 하드웨어 의존성",
        """
        KeyArmor의 암호화 보안은 부분적으로 사용자 기기의 Android Keystore 구현에 의존합니다. 이 구현은 제조업체, 칩셋 및 Android 버전에 따라 다릅니다. 개발자는 서드파티 하드웨어 구현의 보안을 통제하거나 보증하지 않습니다. 기기 OS를 최신 상태로 유지할 것을 강력히 권장합니다.
        """.trimIndent()
    ),
    LegalSection(
        "자동 채우기 서비스 제한",
        """
        KeyArmor 자동 채우기 서비스는 모든 응용 프로그램이나 브라우저에서 올바르게 작동하지 않을 수 있습니다. 특히 Google Chrome은 Android의 표준 자동 채우기 프레임워크를 지원하지 않습니다. 이는 모든 타사 비밀번호 관리자에게 동등하게 영향을 미치는 Chrome의 제한이며 개발자의 통제 범위 밖입니다. 개발자는 자동 채우기 서비스가 특정 응용 프로그램에서 작동할 것을 보장하지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "실시간 암호화폐 잔액 면책 조항",
        """
        실시간 잔액 데이터는 공개 타사 블록체인 노드 및 API에서 가져옵니다. 개발자는 이러한 노드를 운영하거나 제어하지 않습니다. 잔액 정보는 정보 제공 목적으로만 제공되며 지연되거나 부정확하거나 사용할 수 없을 수 있습니다. 이 정보는 금융 또는 투자 조언을 구성하지 않습니다. 개발자는 앱에 표시된 잔액 데이터를 기반으로 내린 결정에 대해 어떠한 책임도 지지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "P2P 공유",
        """
        P2P 항목 공유 기능은 사용자 간 직접적인 암호화된 보관함 항목 교환을 촉진합니다. 개발자는 사용자 간에 공유된 항목의 내용, 수신자의 신원이나 신뢰성, 또는 제3자와 보관함 데이터를 공유함으로써 발생하는 결과에 대해 어떠한 책임도 지지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "서드파티 라이브러리",
        """
        KeyArmor는 게시 시점에 안전한 것으로 간주되는 서드파티 오픈 소스 라이브러리를 사용합니다. 개발자는 서드파티 구성 요소에서 발생하는 장애, 취약점 또는 예기치 않은 동작에 대해 책임을 지지 않습니다.
        """.trimIndent()
    ),
    LegalSection(
        "준거법",
        """
        이 법적 고지는 아르헨티나 공화국에서 시행되는 법률에 의해 규율됩니다. 앱 사용으로 인해 발생하는 모든 분쟁은 해당 관할권의 관할 법원에 제출됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "최종 업데이트",
        "2026년 5월 (rev. 2)"
    )
)

private fun licenseKO() = listOf(
    LegalSection("Source Available 라이선스", ""),
    LegalSection("보안 감사 목적으로만 공개된 소스 코드", """
KeyArmor의 소스 코드는 보안 연구자와 사용자가 보안 관행을 검사하고 감사할 수 있도록 하기 위한 목적으로만 공개되어 있습니다. 그 외의 어떠한 권리도 부여되지 않으며, 모든 권리는 저작권자에게 유보됩니다.
    """.trimIndent()),
    LegalSection("할 수 있는 것", """
• 보안 연구 및 개인적인 감사를 위해 소스 코드를 읽고 검사
• 공식 채널을 통해 KeyArmor 팀에 보안 취약점 신고
    """.trimIndent()),
    LegalSection("할 수 없는 것", """
저작권자의 명시적인 사전 서면 허가 없이는 다음을 할 수 없습니다:

• 공식 배포판 이외의 방식으로 소프트웨어를 사용하거나 실행하는 것
• 읽기 외의 목적으로 소스 코드를 복사, 복제 또는 추출하는 것
• 이를 수정, 변경, 번역하거나 이를 기반으로 2차적 저작물을 만드는 것
• 이를 제3자에게 배포, 게시, 재라이선스, 판매하거나 기타 방식으로 제공하는 것
• 소스 코드를 다른 제품, 서비스 또는 프로젝트의 기반으로 사용하는 것
• KeyArmor의 이름, 로고 또는 관련 상표를 사용하는 것
    """.trimIndent()),
    LegalSection("소스 코드", """
KeyArmor의 소스 코드는 공개적으로 이용 가능합니다. 저장소 링크는 앱의 공식 채널을 확인하십시오.
    """.trimIndent()),
    LegalSection("무보증", """
소스 코드는 명시적이든 묵시적이든 어떠한 종류의 보증 없이 "있는 그대로" 제공됩니다. 저작권자는 특정 목적에의 적합성에 대해 어떠한 진술도 하지 않습니다.
    """.trimIndent()),
    LegalSection("저작권", "© 2026 KeyArmor. 모든 권리 보유. Source Available.")
)

private fun opensourceKO() = listOf(
    LegalSection(
        "오픈 소스 고지",
        ""
    ),
    LegalSection(
        "사용된 라이브러리",
        """
        KeyArmor는 다음 오픈 소스 라이브러리를 사용합니다. 별도로 명시되지 않는 한 Apache 2.0 라이선스에 따라 배포됩니다.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        버전: BOM 2026.09.00 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        버전: 1.2.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        버전: 2.8.5 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        버전: 2.60.1 | 라이선스: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        버전: 1.11.0 | 라이선스: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        버전: 1.1.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        버전: 2.10.2 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        버전: 2.11.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        버전: 2.12.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        버전: 22.0.0 | 라이선스: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        버전: 2.9.2 | 라이선스: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        버전: v3-rev20240123-2.0.0 | 라이선스: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        버전: 2.2.0 | 라이선스: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        버전: 4.3.0 | 라이선스: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        버전: 0.37.3 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        버전: BOM 2026.09.00 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        버전: 1.19.1 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        버전: 1.13.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        버전: 1.3.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        버전: 1.3.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        버전: 1.8.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        버전: 1.4.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        버전: 2.4.21 | 라이선스: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        버전: 1.2.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        버전: 1.4.1 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        버전: 1.6.0 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        버전: 1.6.2 | 라이선스: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        버전: BOM 35.1.0 | 라이선스: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        버전: 2.15.0 | 라이선스: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        버전: 3.0.1 | 라이선스: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        버전: 2.0.27.0 | 라이선스: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        버전: 1.2.1 | 라이선스: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        버전: 1.86 | 라이선스: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        버전: 10.29.0 | 라이선스: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        버전: 5.2.0 | 라이선스: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        버전: 5.19.1 | 라이선스: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        버전: 1.2.1 | 라이선스: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        버전: 16.0.1 | 라이선스: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        버전: 16.0.1 | 라이선스: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        버전: 2026-07-09 | 라이선스: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Apache 2.0 라이선스 전문",
        """
        Apache 2.0 라이선스의 전체 텍스트는 다음에서 확인할 수 있습니다:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataKO() = listOf(
    LegalSection("계정 및 데이터 삭제", ""),
    LegalSection("KeyArmor가 귀하에 대해 저장하는 정보", """
사용자의 금고(비밀번호, 메모, 카드, 지갑, 첨부 파일 및 비밀번호 기록)는 당사의 서버에 절대 저장되지 않습니다. 이 데이터는 사용자의 기기에 암호화된 상태로 저장되며, 백업 기능을 켜면 사용자의 Google 드라이브에도 저장됩니다. 데이터가 기기를 떠나기 전에 이미 암호화되므로, 당사가 이를 읽거나 삭제할 수 있는 곳에는 절대 저장되지 않습니다.

귀하의 Google 계정과 연관되어 당사 서버(Firebase)로 전송되는 유일한 데이터는 선택적 기능(Google 드라이브 백업, 디지털 상속 또는 가족 요금제)을 위해 해당 계정을 연결하는 것과 관련된 정보뿐입니다. 이러한 기능 중 하나를 위해 Google 계정을 연결하기만 해도(상속 또는 가족 요금제를 설정하기 전이라도), 최소한의 기록(이메일을 키로 사용하는 푸시 알림 토큰)이 서버에 등록됩니다. KeyArmor에 Google 계정을 한 번도 연결한 적이 없다면, 당사 서버에는 삭제할 수 있는 귀하의 정보가 전혀 없습니다.
    """.trimIndent()),
    LegalSection("앱에서 삭제하기", """
앱 내의 설정 → 내 모든 데이터 삭제에서 언제든지 모든 데이터를 삭제할 수 있습니다. KeyArmor에 Google 계정(드라이브 백업, 디지털 유산, 가족 요금제용)을 연결한 적이 없다면, 기기에서 금고가 즉시 영구적으로 삭제됩니다.

Google 계정을 연동한 경우, 앱은 Google로 로그인할 것을 요청하며, 이를 통해 동일한 작업으로 연동된 서버 측 기록(Firebase)과 KeyArmor가 Google 드라이브에 저장한 백업 및 Time Machine 사본을 삭제할 수 있습니다. 로그인이 불가능한 경우에만 앱은 로컬 데이터만 삭제하게 되며, 이 경우 아래 요청을 통해 별도로 제거하지 않는 한 서버 측 기록은 그대로 남아 있음을 사전에 경고합니다.
    """.trimIndent()),
    LegalSection("삭제되지 않는 항목", """
데이터를 삭제해도 앱 외부에 남아 있는 항목은 제거되지 않습니다:

• 직접 내보낸 파일(저장하거나 공유한 CSV, PDF 또는 .kab 파일). 이 파일들은 사용자의 소유이며 사용자가 저장한 위치에 보관되므로, 해당 위치에서 직접 삭제해야 합니다.
• 이전 버전의 KeyArmor 설치 시 ‘다운로드’ 폴더에 남겨진 백업 파일. Android에서는 새로 설치한 앱이 이 파일들을 건드리지 못하도록 제한하므로, ‘다운로드’ 폴더에서 직접 삭제해야 합니다.
• 구독 정보. 이는 Google Play에서 관리되므로, 데이터를 삭제해도 구독이 취소되지 않습니다. Google Play → ‘구독’에서 직접 취소해야 합니다.
    """.trimIndent()),
    LegalSection("계정 데이터 삭제 요청", """
KeyArmor에 Google 계정을 연동했지만, 위의 앱 내 삭제 기능을 사용하기 위해 로그인할 수 없는 경우(예: 앱이 더 이상 설치되어 있지 않은 경우), 해당 Google 계정으로 이메일을 보내 관련 서버 측 데이터의 삭제를 요청해 주십시오.

이메일 주소: support@thormakk.dev

삭제 요청은 30일 이내에 처리되며, 처리가 완료되면 이메일로 확인해 드립니다. 이 요청을 하기 위해 앱을 설치해 둘 필요는 없습니다.
    """.trimIndent())
)
