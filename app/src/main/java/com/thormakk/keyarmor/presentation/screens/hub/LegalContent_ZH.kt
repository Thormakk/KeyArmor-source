package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentZH(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyZH()
    "terms"      -> termsZH()
    "legal"      -> legalZH()
    "license"    -> licenseZH()
    "opensource" -> opensourceZH()
    "delete"     -> deleteDataZH()
    else         -> listOf(LegalSection("未找到文档", ""))
}

private fun privacyZH() = listOf(
    LegalSection("隐私政策", ""),
    LegalSection("数据收集", """
KeyArmor 不会向外部服务器收集、传输或共享任何个人数据。您在应用中输入的所有信息均仅存储在您的设备上，除非您明确启用 Google Drive 同步功能。

对于应用的核心功能（保险库、自动填充、安全审计、数据泄露监控），KeyArmor 不会收集您的电子邮件地址或任何账户凭据——这些功能完全无需任何账户即可运作。如果您启用数字继承或家庭套餐，这些功能需要使用 Google 登录：届时您的 Google 账户电子邮件地址将存储在 Google Firebase 上，用于识别您并将您与您指定的继承人或家庭成员匹配，以及通过我们的邮件服务商（Brevo）发送相应的通知邮件。该电子邮件地址仅用于这两项功能，绝不会出售或与他人共享。在数字继承和家庭套餐之外，唯一的个人标识符是您在设置过程中可以选择设置的名称或昵称，该信息以加密形式仅存储在您的设备上。
    """.trimIndent()),
    LegalSection("本地存储", """
您的所有数据（密码、通行密钥、备注、信用卡、银行账户、加密钱包、附件和密码历史记录）均使用 AES-256-GCM 加密存储，密钥通过 Argon2id 从您的主密码派生，绝不会离开您的设备。如果您启用生物识别解锁，该密钥的一份副本还会受到您设备 Android Keystore 中密钥的额外保护。未经您的明确同意，任何数据不会离开您的设备。
    """.trimIndent()),
    LegalSection("主密码", """
您的主密码绝不会以明文或任何可恢复的形式存储。它仅用于通过 Argon2id 派生保险库加密密钥。如果您丢失主密码，您的数据将无法恢复。
    """.trimIndent()),
    LegalSection("生物识别", """
KeyArmor 中的生物识别认证完全由 Android 操作系统通过 BiometricPrompt API 管理。KeyArmor 在任何时候都不会访问、存储或处理生物识别数据。

生物识别在应用中用于两个场景：

• 保险库解锁：在启动时验证对应用的访问，所有套餐均可使用。
• 高级生物识别保护（Premium）：一个可选屏障，在显示保险库特定分区（密码、信用卡、自定义字段）中的敏感字段之前需要生物识别验证。可在设置中配置 — 支持按次访问或按会话模式，可自定义超时时间。同样的隐私保证适用：KeyArmor 不存储或处理生物识别数据。
    """.trimIndent()),
    LegalSection("Google Drive 同步", """
Google Drive 同步完全是可选的。启用后，KeyArmor 将：

• 仅请求一项最小权限（DRIVE_APPDATA），用于将您自己的加密备份和 Time Machine 还原点存储在您 Google Drive 中一个应用专属的私有文件夹内。该文件夹是隐藏的——任何其他应用，以及您在浏览 Drive 时，都无法看到或修改它。
• 在上传保险库之前，会使用一个由12个单词组成的备份密钥对其进行加密。该密钥在您的设备上生成，且仅您本人持有：无论是谷歌还是开发者，都无法读取该文件或恢复该密钥。
• 如果您将备份密钥的 PDF 文件保存到 Google 文档、Google 云端硬盘或其他应用中，请通过 Android 系统的分享界面自行操作；该应用受其自身的隐私政策约束。
• 如果您更换密钥，可以将之前的密钥加密保存在手机上；套件 PDF 中可能包含这些密钥。KeyArmor 不会将它们发送到任何服务器。
• 从不访问任何其他 Drive 文件，从不创建可见文件夹，也从不共享文件。
• 可选地在设备的 Downloads/KeyArmor/ 文件夹中保留备份的第二份本地副本，该副本永不离开设备。
• Google Drive 备份（包括由您发起的手动备份和每日自动备份）在 Premium 和家庭计划中提供。在免费套餐中，您可以关联您的 Google 账户来恢复现有的备份。

数字继承和家庭套餐使用的是 Google Firebase，而非您的 Google Drive。Firebase 负责协调同意、邀请和通知事宜。继承保险库会在您的设备上使用继承人的公钥进行端到端加密，生成的加密数据块会通过有效期较短（15分钟）的签名链接上传至开发者私有的 Firebase Cloud Storage 存储桶。服务器会保存该数据块，但永远不会收到继承人的密钥，因此无法读取其内容。

您可以随时在同步屏幕中断开您的 Google 账户。
    """.trimIndent()),
    LegalSection("附件", """
“附件”功能允许您将文件（每条记录最多 5 个，每个文件最大 5 MB）与保险库条目关联。所有附件在保存到设备内部存储之前，都会使用 AES-256-GCM 进行加密。如果您启用了 Google 云端硬盘同步，您的附件和文档照片将被包含在您上传的备份中，并使用您的备份密钥进行加密。这些内容绝不会被发送至任何其他服务。
    """.trimIndent()),
    LegalSection("密码历史记录", """
KeyArmor 为每条记录保留本地的历史密码记录。此历史记录以加密形式存储在设备上，绝不会传输到任何外部服务器。
    """.trimIndent()),
    LegalSection("应用内通知", """
KeyArmor生成内部通知以提醒您重要的安全事件：

• 到期警告：当一个或多个密码接近到期日期时发送。
• 已过期密码：当一个或多个密码已过到期日期时发送。
• 泄露警报：当您的密码在已知数据泄露事件中被检测到时发送。

这三种通知类型完全在您的设备上生成，从不传输到外部服务器。如果您使用数字继承或家庭套餐，您还会收到关于这些功能的通知（继承人接受邀请、家庭成员被添加等）；由于这些通知依赖于其他账户发起的事件，它们由 Google Firebase 触发并通过 Firebase Cloud Messaging 送达。无论来源如何，所有通知都会在30天后自动删除。您可以通过向侧面滑动来删除任何通知，或从通知屏幕一次性清除所有通知。推送通知需要系统权限。
    """.trimIndent()),
    LegalSection("安全审计", """
安全审计功能在设备本地分析您的密码。常见密码检查通过与存储在应用本身内的预定义列表进行比较来完成，不会查询外部服务。
    """.trimIndent()),
    LegalSection("数据泄露监控（HIBP）", """
KeyArmor 可以使用由安全研究员 Troy Hunt 运营的 Have I Been Pwned（HIBP）服务，检查您的密码是否出现在已知的数据泄露事件中。

此检查使用 k-匿名模型：仅将您密码的 SHA-1 哈希值的前 5 个字符发送到 HIBP API。完整哈希值不会被传输。匹配在您的设备上本地执行。不会发送任何用户名、电子邮件地址或账户关联信息。HIBP 的响应中填充了诱饵哈希值，因此即使进行网络流量分析也无法揭示被检查的密码。
    """.trimIndent()),
    LegalSection("自动填充服务", """
KeyArmor 包含一项可选的 Android 自动填充服务。在系统设置中启用后，它会读取当前活动登录界面的应用包名或网站 URL，以便从您的密码库中推荐匹配的凭据。此匹配完全在设备上执行。不会向任何外部服务器传输应用名称、网站 URL 或凭据。自动填充服务不会读取其他应用的内容，仅限于识别登录上下文所必需的最低限度。
    """.trimIndent()),
    LegalSection("加密钱包实时余额", """
当您存储加密钱包条目并启用实时余额监控时，KeyArmor 会查询公共区块链节点或 API，以获取您输入的钱包地址关联的余额。仅传输钱包地址——不发送私钥、助记词或个人数据。这些查询直接发送到公共基础设施；KeyArmor 不运营任何中间服务器。
    """.trimIndent()),
    LegalSection("点对点条目共享", """
KeyArmor 允许您通过二维码或加密文本直接与其他 KeyArmor 用户共享单个密码库条目。此传输采用端对端加密，且为点对点方式——不涉及任何服务器或中间方。您需独自负责选择与谁共享条目，以及传输加密文本所用渠道的安全性（如果不直接使用二维码）。
    """.trimIndent()),
    LegalSection("第三方库", """
KeyArmor 使用开源库（请参阅开源声明）。在本应用所使用的配置中，这些库均不包含遥测、分析或数据收集机制。

KeyArmor 还依赖两项第三方服务来实现特定功能：RevenueCat 用于验证您的 Premium 订阅状态，仅接收与购买相关的信息（绝不会接收您的保险库、主密码或恢复密钥）；Brevo 用于发送数字继承和家庭套餐所需的事务性邮件（邀请、同意确认、通知），为此会接收参与这些功能的相关人员的电子邮件地址。这两项服务均不用于广告或行为分析。
    """.trimIndent()),    LegalSection("主屏幕小组件", """
KeyArmor 包含一个可选的 Android 主屏幕小组件，显示您的保险库中存储的条目总数。该小组件仅访问设备本地数据，不向外部服务器传输任何信息。条目计数是汇总数据，不会泄露任何单个条目的内容。您可以自行选择是否将小组件放置在主屏幕上。
    """.trimIndent()),

    LegalSection("权限", """
KeyArmor 仅请求其运行所需的权限：

• 生物识别（可选）：用于指纹解锁。
• 互联网（可选）：仅用于您主动启用的功能——Google Drive 同步、数字继承和家庭套餐（通过 Google Firebase）、加密钱包实时余额查询、通过 Have I Been Pwned 进行的数据泄露检查，以及订阅验证（Google Play Billing / RevenueCat）。
• 文件访问（可选）：用于保险库导入/导出和添加附件，仅在您发起操作时使用。
• 相机（可选）：用于设置双因素身份验证 (2FA/TOTP) 或接收共享条目时扫描二维码。仅在用户明确请求时激活。

• 自动填充服务（可选）：在 Android 系统设置中手动启用。仅用于在其他应用的登录界面中推荐凭据。
• 麦克风（可选）：仅用于安全笔记中的语音听写。语音识别在您的设备上完成；KeyArmor 不会录制、保存音频，也不会将音频发送到任何服务器。
• 日历，只读（可选）：安全旅行模式用它在您的日程中查找行程。日程在您的设备上处理，绝不会被传输。
• 通知和精确闹钟（可选）：用于安全提醒和您设置的提醒事项。设备启动事件用于在重启后重新安排这些提醒。
• 网络状态（可选）：仅用于判断您使用的是移动数据还是 Wi-Fi，以便较大的 Google Drive 备份等待 Wi-Fi。不会收集或发送任何有关您网络的信息。

KeyArmor 不会请求访问联系人、位置信息或上述以外的任何其他个人数据。
    """.trimIndent()),
    LegalSection("接收数据的其他服务", """
除上文所述的服务外，KeyArmor 还会使用以下服务，且仅用于需要它们的功能：

• 应用完整性验证（Firebase App Check / Google Play Integrity）：Google 提供您设备的证明，使我们的服务器只接受来自正版应用的请求。
• RevenueCat：除购买信息外，还会接收您 Google 账号的标识符（不是您的电子邮件地址）作为用户 ID，以便在多台设备间关联您的订阅。
• CoinGecko：当您使用实时加密货币价格时，会向 CoinGecko 请求您投资组合的代币代码，CoinGecko 可以看到您的 IP 地址。上文提到的区块链浏览器和节点也可以看到。
• Google Fonts：应用通过 Google Play 服务下载 Inter 和 JetBrains Mono 字体，因此在请求字体时 Google 可以看到您的 IP 地址。
• Google Play 服务可信时间：用于检测设备时钟是否被篡改。不会发送个人数据。
• 文档的文字识别（OCR）使用 ML Kit 在您的设备上完成。不会向任何服务器发送图像。
• Ko-fi：仅在您打开捐赠链接时；此时适用其自身的隐私政策。

我们的服务器可能会临时记录您的 IP 地址，以限制滥用。
    """.trimIndent()),
    LegalSection("通行密钥", """
仅当您在系统设置中自行选择时，KeyArmor 才能作为 Android 通行密钥提供程序（Credential Manager）工作。创建或使用通行密钥时，KeyArmor 会读取请求该通行密钥的网站或应用的域名，以便将其与该网站关联，并对网站发送的质询进行签名。私钥在您的设备上生成，并与保险库的其余数据一起加密保存；不会发送到任何服务器或任何第三方。私钥只会包含在您自己创建的备份中（例如 Google Drive 上的加密备份）离开设备。使用通行密钥签名时，KeyArmor 会要求您通过指纹或主密码验证身份。与任何通行密钥一样，您登录的网站只会收到公钥和签名。
    """.trimIndent()),
    LegalSection("数据删除与保留", """
您的保险库会保留在您的设备上（如果您启用备份，还包括您的 Google Drive），直到您自行删除。我们的服务器上只有协调所需的数据：以您的电子邮件标识的推送通知令牌，以及（如果您使用数字继承或家庭计划）邀请和同意记录以及继承保险库的加密数据块。只要您使用这些功能，或在您要求删除之前，这些数据都会保留。您可以通过 设置 → 删除我的所有数据 删除，或使用您的 Google 账号发送邮件至 support@thormakk.dev；我们会在 30 天内处理这些请求。完整说明请见我们网站上的“删除账户和数据”页面。
    """.trimIndent()),
    LegalSection("未成年人", """
KeyArmor 不面向 13 岁以下的未成年人（也不面向低于您所在国家/地区法律规定的数字同意最低年龄的人），我们不会故意收集未成年人的数据。如果您认为有未成年人向我们提供了数据，请发送邮件至 support@thormakk.dev，我们将予以删除。
    """.trimIndent()),
    LegalSection("联系方式", "如有任何与本政策相关的问题，请发送邮件至 support@thormakk.dev"),
    LegalSection("最后更新", "2026年11月（第9版）")
)

private fun termsZH() = listOf(
    LegalSection("使用条款", ""),
    LegalSection("接受条款", """
安装或使用 KeyArmor 即表示您完全接受这些条款。如果您不同意任何部分，请勿使用该应用。
    """.trimIndent()),
    LegalSection("允许的用途", """
KeyArmor 专为作为密码管理器和安全保险库的个人使用而设计。禁止将其用于非法活动、存储便于违法行为的信息，或尝试对应用进行逆向工程。
    """.trimIndent()),
    LegalSection("用户责任", """
您对以下事项负有全部责任：

• 主密码的安全。一旦丢失，将没有恢复机制，您的数据将永久无法访问。
• 12个单词的备份密钥（如果您使用了该功能）。如果您丢失了该密钥且手机也丢失了，您的 Drive 备份将无法打开，就连开发者也无法打开。
• 通过导出功能或 Drive 同步维护保险库的最新备份。
    """.trimIndent()),
    LegalSection("同步功能", """
Google Drive 同步是一项辅助备份功能。它不能替代良好的个人安全实践。开发者不保证此功能的持续可用性，因为它依赖于第三方服务 (Google)。
    """.trimIndent()),
    LegalSection("高级订阅", """
KeyArmor 提供可选付费计划（月度和年度），可解锁更多功能。订阅仅通过 Google Play 结算处理。购买订阅即表示您同时接受 Google Play 的服务条款。

• 月度和年度计划为自动续订订阅。您可以随时通过 Google Play 取消；取消将在当前计费周期结束时生效。
• 退款须遵守 Google Play 的标准退款政策。开发者不独立处理退款。
• 高级功能可能随时间演变。开发者保留在合理通知后添加、修改或停用特定高级功能的权利。
    """.trimIndent()),
    LegalSection("旅行安全模式", """
旅行安全模式允许您将特定条目标记为旅行安全，并在模式激活时隐藏所有其他条目。您需独自负责确定哪些条目标记为旅行安全，以及激活或停用此模式的后果。开发者对意外遗留为可见或隐藏的条目不承担任何责任。
    """.trimIndent()),
    LegalSection("紧急PIN码", """
紧急PIN码功能在输入指定 PIN 时触发不可逆操作（清除或隐藏敏感密码库数据）。一旦激活，根据您的配置，隐藏或删除的数据可能无法恢复。您需对配置此功能以及因有意或意外激活导致的任何数据丢失承担全部责任。
    """.trimIndent()),
    LegalSection("加密货币余额信息", """
实时加密余额功能仅出于信息目的从公共区块链基础设施检索余额信息。此信息不构成财务建议。开发者不保证余额数据的准确性、完整性或及时性。请勿仅根据本应用中显示的信息做出财务决策。
    """.trimIndent()),
    LegalSection("无担保声明", """
该应用按"现状"提供，不提供任何明示或暗示的担保。开发者不保证该应用没有错误、运行不中断或适合特定用途。
    """.trimIndent()),
    LegalSection("责任限制", """
在适用法律允许的最大范围内，开发者不对因使用或无法使用该应用而产生的数据丢失、保险库访问丧失或任何直接、间接、偶然、特殊或后果性损害承担责任。

    如果您所在国家的消费者保护法赋予您不可放弃的权利，本条款不会削减这些权利。本条款中关于担保和责任的规定，仅在法律允许的范围内适用。
    """.trimIndent()),
    LegalSection("条款更新", """
这些条款可能随时更新。在发布更新后继续使用该应用即表示接受新条款。
    """.trimIndent()),
    LegalSection("最后更新", "2026年11月（第3版）")
)

private fun legalZH() = listOf(
    LegalSection("法律声明与免责声明", ""),
    LegalSection("软件按现状提供", """
KeyArmor 按"现状"分发，不提供任何明示或暗示的担保，包括但不限于适销性、特定用途适用性或不侵权的担保。
    """.trimIndent()),
    LegalSection("责任限制", """
在任何情况下，KeyArmor 的开发者均不对以下情况承担责任：

• 应用中存储的数据丢失或损坏
• 因忘记或丢失主密码或备份密钥而无法访问保险库
• Android OS 组件中的漏洞或故障
• Google Drive 服务的故障、中断或变更
• 因使用或无法使用该应用而产生的任何直接、间接、偶然、特殊或后果性损害
• 第三方未经授权访问用户数据造成的任何损害
• 因有意或意外激活紧急PIN码功能导致的数据丢失
    """.trimIndent()),
    LegalSection("Google Drive 数据传输", """
当用户启用 Google Drive 同步（备份）或使用数字继承／家庭套餐（Google Firebase）时，数据将根据 Google 的服务条款以已加密的形式传输到 Google 的基础设施。在任何情况下，数据都以不透明的加密数据块形式离开设备。开发者无法访问或控制 Google 的基础设施，也无法读取所传输的内容。
    """.trimIndent()),
    LegalSection("Android Keystore 与硬件依赖", """
KeyArmor 的加密安全性部分依赖于用户设备的 Android Keystore 实现。此实现因制造商、芯片组和 Android 版本而异。开发者不控制或保证第三方硬件实现的安全性。强烈建议保持设备操作系统的更新。
    """.trimIndent()),
    LegalSection("自动填充服务的限制", """
KeyArmor 自动填充服务可能无法在所有应用程序或浏览器中正常运行。特别是，Google Chrome 不支持 Android 的标准自动填充框架——这是 Chrome 的限制，同等影响所有第三方密码管理器，超出开发者控制范围。开发者不保证自动填充服务能与任何特定应用程序配合使用。
    """.trimIndent()),
    LegalSection("实时加密余额免责声明", """
实时余额数据从公共第三方区块链节点和 API 获取。开发者不运营或控制这些节点。余额信息仅供参考，可能存在延迟、不准确或不可用的情况。此信息不构成财务或投资建议。开发者对基于应用中显示的余额数据所做决策不承担任何责任。
    """.trimIndent()),
    LegalSection("点对点共享", """
P2P 条目共享功能促进用户之间直接加密交换密码库条目。开发者对用户之间共享条目的内容、接收方的身份或可信度，以及与第三方共享密码库数据产生的任何后果不承担任何责任。
    """.trimIndent()),
    LegalSection("第三方库", """
KeyArmor 使用在发布时被认为安全的第三方开源库。开发者对源自第三方组件的故障、漏洞或意外行为不承担任何责任。
    """.trimIndent()),
    LegalSection("适用法律", """
本法律声明受阿根廷共和国现行法律管辖。因使用该应用而产生的任何争议均应提交至该司法管辖区的主管法院。

    如果您所在国家的消费者保护法赋予您不可放弃的权利，本通知不会削减这些权利。本通知中关于保修、责任、适用法律和管辖法院的规定，仅在法律允许的范围内适用。
    """.trimIndent()),
    LegalSection("最后更新", "2026年11月（第3版）")
)

private fun licenseZH() = listOf(
    LegalSection("Source Available 许可证", ""),
    LegalSection("源代码仅为安全审计目的而公开", """
KeyArmor 的源代码公开的唯一目的，是让安全研究人员和用户能够检查和审计其安全实践。除此之外不授予任何权利——版权持有者保留所有权利。
    """.trimIndent()),
    LegalSection("您可以做什么", """
• 出于安全研究和个人审计目的阅读和检查源代码
• 通过官方渠道向 KeyArmor 团队报告安全漏洞
    """.trimIndent()),
    LegalSection("您不可以做什么", """
未经版权持有者事先明确书面许可，您不得：

• 在其官方分发渠道之外使用或运行该软件
• 出于阅读以外的任何目的复制、复制或提取源代码
• 修改、改编、翻译或基于其创建衍生作品
• 以任何方式向第三方分发、发布、再许可、出售或提供该源代码
• 将源代码用作其他产品、服务或项目的基础
• 使用 KeyArmor 的名称、徽标或任何相关商标
    """.trimIndent()),
    LegalSection("源代码", """
KeyArmor 的源代码公开可用。
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("无担保", """
源代码按"原样"提供，不附带任何明示或暗示的担保。版权持有者不对其特定用途的适用性做出任何陈述。
    """.trimIndent()),
    LegalSection("版权", "© 2026 KeyArmor。保留所有权利。Source Available。")
)

private fun opensourceZH() = listOf(
    LegalSection("开源声明", ""),
    LegalSection("使用的库", """
KeyArmor 使用以下开源库。除非另有说明，它们均在 Apache 2.0 许可证下分发。
    """.trimIndent()),
    LegalSection("Jetpack Compose", """
版本：BOM 2026.09.00 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose
    """.trimIndent()),
    LegalSection("Jetpack Glance (AppWidget)", """
Versión: 1.2.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/glance
    """.trimIndent()),
    LegalSection("Room Database", """
版本：2.8.5 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/data-storage/room
    """.trimIndent()),
    LegalSection("Hilt (Dagger)", """
版本：2.60.1 | 许可证：Apache 2.0
Copyright © The Dagger Authors
https://dagger.dev/hilt/
    """.trimIndent()),
    LegalSection("Kotlin Coroutines", """
版本：1.11.0 | 许可证：Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/Kotlin/kotlinx.coroutines
    """.trimIndent()),
    LegalSection("AndroidX Biometric", """
版本：1.1.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/biometric
    """.trimIndent()),
    LegalSection("AndroidX Navigation", """
版本：2.10.2 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/guide/navigation
    """.trimIndent()),
    LegalSection("AndroidX Lifecycle / ViewModel", """
版本：2.11.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/lifecycle
    """.trimIndent()),
    LegalSection("AndroidX WorkManager", """
版本：2.12.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/topic/libraries/architecture/workmanager
    """.trimIndent()),
    LegalSection("Google Play Services Auth", """
版本：22.0.0 | 许可证：Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/guides/setup
    """.trimIndent()),
    LegalSection("Google API Client Android", """
版本：2.9.2 | 许可证：Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-api-java-client
    """.trimIndent()),
    LegalSection("Google Drive API v3", """
版本：v3-rev20240123-2.0.0 | 许可证：Apache 2.0
Copyright © Google LLC
https://developers.google.com/drive/api/v3/about-sdk
    """.trimIndent()),
    LegalSection("Google HTTP Client GSON", """
版本：2.2.0 | 许可证：Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-http-java-client
    """.trimIndent()),
    LegalSection("ZXing Android Embedded", """
版本：4.3.0 | 许可证：Apache 2.0
Copyright © ZXing authors, journeyapps
https://github.com/journeyapps/zxing-android-embedded
    """.trimIndent()),
    LegalSection("Accompanist Permissions", """
版本：0.37.3 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://google.github.io/accompanist/permissions/
    """.trimIndent()),
    LegalSection("Compose Material Icons Extended", """
版本：BOM 2026.09.00 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose/graphics/images/material
    """.trimIndent()),
    LegalSection("AndroidX Core KTX", """
版本：1.19.1 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Activity Compose", """
版本：1.13.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/activity
    """.trimIndent()),
    LegalSection("AndroidX Autofill", """
版本：1.3.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/autofill
    """.trimIndent()),
    LegalSection("AndroidX DataStore", """
版本：1.3.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/datastore
    """.trimIndent()),
    LegalSection("AndroidX AppCompat", """
版本：1.8.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/appcompat
    """.trimIndent()),
    LegalSection("AndroidX Hilt (Navigation, Work)", """
版本：1.4.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/hilt
    """.trimIndent()),
    LegalSection("Kotlin Standard Library", """
版本：2.4.21 | 许可证：Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/JetBrains/kotlin
    """.trimIndent()),
    LegalSection("AndroidX Core Splashscreen", """
版本：1.2.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Profileinstaller", """
版本：1.4.1 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/profileinstaller
    """.trimIndent()),
    LegalSection("AndroidX Credential Manager", """
版本：1.6.0 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/credentials
    """.trimIndent()),
    LegalSection("CameraX", """
版本：1.6.2 | 许可证：Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/camerax
    """.trimIndent()),
    LegalSection("Firebase Android SDK (Messaging, Auth, App Check)", """
版本：BOM 35.1.0 | 许可证：Apache 2.0
Copyright © Google LLC
https://github.com/firebase/firebase-android-sdk
    """.trimIndent()),
    LegalSection("Joda-Time", """
版本：2.15.0 | 许可证：Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-time/
    """.trimIndent()),
    LegalSection("Joda-Convert", """
版本：3.0.1 | 许可证：Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-convert/
    """.trimIndent()),
    LegalSection("PDFBox-Android", """
版本：2.0.27.0 | 许可证：Apache 2.0
Copyright © Tom Roush
https://github.com/TomRoush/PdfBox-Android
    """.trimIndent()),
    LegalSection("RichEditor Compose", """
版本：1.2.1 | 许可证：MIT
Copyright © Mohamed Rejeb
https://github.com/MohamedRejeb/compose-rich-editor
    """.trimIndent()),
    LegalSection("Bouncy Castle Provider", """
版本：1.86 | 许可证：Bouncy Castle License (MIT-style)
Copyright © The Legion of the Bouncy Castle Inc.
https://www.bouncycastle.org/about/license/
    """.trimIndent()),
    LegalSection("RevenueCat Purchases Android SDK", """
版本：10.29.0 | 许可证：MIT
Copyright © RevenueCat, Inc.
https://github.com/RevenueCat/purchases-android
    """.trimIndent()),
    LegalSection("LazySodium for Android", """
版本：5.2.0 | 许可证：MPL-2.0
Copyright © Terl Tech Ltd
https://github.com/terl/lazysodium-android
    """.trimIndent()),
    LegalSection("JNA (Java Native Access)", """
版本：5.19.1 | 许可证：Apache 2.0 / LGPL 2.1 (dual-licensed)
Copyright © The JNA Project Contributors
https://github.com/java-native-access/jna
    """.trimIndent()),
    LegalSection("Google Identity Services (GoogleId)", """
版本：1.2.1 | 许可证：Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/identity/android-credential-manager
    """.trimIndent()),
    LegalSection("Play Services Time", """
版本：16.0.1 | 许可证：Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
    """.trimIndent()),
    LegalSection("ML Kit Text Recognition", """
版本：16.0.1 | 许可证：ML Kit Terms
Copyright © Google LLC
https://developers.google.com/ml-kit/terms
    """.trimIndent()),
    LegalSection("Public Suffix List", """
版本：2026-07-09 | 许可证：MPL-2.0
Copyright © Mozilla Foundation and contributors
https://publicsuffix.org
    """.trimIndent()),
    LegalSection("Apache 2.0 完整文本", """
Apache 2.0 许可证的完整文本可在以下地址获取：
https://www.apache.org/licenses/LICENSE-2.0
MIT: https://opensource.org/license/mit
MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
    """.trimIndent())
)

private fun deleteDataZH() = listOf(
    LegalSection("删除账户及数据", ""),
    LegalSection("KeyArmor 会存储您的哪些信息", """
您的保险库（密码、笔记、卡片、钱包、附件和密码历史记录）绝不会存储在我们的服务器上。它以加密形式保存在您的设备上；如果您开启了备份功能，还会保存在您自己的 Google 云端硬盘中——我们无法读取或删除这些数据，因为它们在离开您的设备之前就已经被加密了。

唯一会传输到我们服务器（Firebase）且与您的 Google 账户相关的数据，仅与为启用可选功能（如 Google 云端硬盘备份、数字遗产或家庭计划）而关联该账户有关。只需为上述任何一项功能关联 Google 账户，系统便会在服务器上记录最基本的条目（一个以您的电子邮件地址为密钥的推送通知令牌），即使您尚未配置“数字遗产”或“家庭计划”也是如此。如果您从未将 Google 账户与 KeyArmor 关联过，那么我们的服务器上根本不存在任何关于您的数据可供删除。
    """.trimIndent()),
    LegalSection("从应用中删除", """
您可以随时在应用内的 设置 → 删除我的所有数据 中删除所有数据。如果您从未将 Google 账户与 KeyArmor 关联过（用于 Drive 备份、数字遗产或家庭计划），此操作将立即且永久地从设备中清除您的保险库。

如果您已关联 Google 账户，应用会提示您使用 Google 账号登录，以便在同一操作中清除相关的服务器端记录（Firebase）以及 KeyArmor 保存在您的 Google 云端硬盘中的备份和 Time Machine 副本。只有当您无法登录时，应用才会转而仅清除本地数据——并且会事先警告您：服务器端记录将保留，除非您通过以下请求单独将其删除。
    """.trimIndent()),
    LegalSection("哪些内容不会被删除", """
删除您的数据并不会清除应用外部的内容：

• 您自行导出的文件（您保存或分享的 CSV、PDF 或 .kab 文件）。这些文件归您所有，并存储在您放置它们的位置，因此您需要在该位置将其删除。
• 早期安装的 KeyArmor 在您的“下载”文件夹中留下的备份副本。Android 系统不再允许新安装的应用程序访问这些文件；请从“下载”文件夹中将其删除。
• 您的订阅。订阅由 Google Play 管理，因此删除应用数据不会取消订阅；请在 Google Play → “订阅”中取消订阅。
    """.trimIndent()),
    LegalSection("申请删除您的账户数据", """
如果您已将 Google 账户与 KeyArmor 关联，但无法登录以使用上述应用内删除功能（例如，您已未安装该应用），请使用该 Google 账户向我们发送电子邮件，申请删除相关的服务器端数据。

请发送邮件至：support@thormakk.dev

我们将在 30 天内处理删除请求，并在处理完成后通过电子邮件发送确认通知。您无需安装该应用即可提交此请求。
    """.trimIndent())
)
