package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentPT(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyPT()
    "terms"      -> termsPT()
    "legal"      -> legalPT()
    "license"    -> licensePT()
    "opensource" -> opensourcePT()
    "delete"     -> deleteDataPT()
    else         -> listOf(LegalSection("Documento não encontrado", ""))
}

private fun privacyPT() = listOf(
    LegalSection(
        "Política de Privacidade",
        ""
    ),
    LegalSection(
        "Coleta de Dados",
        """
        O KeyArmor não coleta, transmite nem compartilha nenhum dado pessoal com servidores externos. Todas as informações que você inserir no aplicativo permanecem exclusivamente no seu dispositivo, a menos que você habilite explicitamente a sincronização com o Google Drive.

        O KeyArmor não coleta seu endereço de e-mail nem quaisquer credenciais de conta para os recursos principais do app (cofre, preenchimento automático, auditoria de segurança, monitoramento de violações) — esses funcionam completamente sem nenhuma conta. Se você ativar a Herança Digital ou o Plano Família, esses recursos exigem login com o Google: seu e-mail da conta Google é então armazenado no Google Firebase para identificá-lo e associá-lo aos herdeiros ou membros da família que você designar, e para enviar os e-mails de notificação correspondentes por meio do nosso provedor de e-mail (Brevo). Esse e-mail é usado exclusivamente para esses dois recursos e nunca é vendido ou compartilhado com terceiros. Fora da Herança Digital e do Plano Família, o único identificador pessoal é um nome ou apelido opcional que você pode definir durante a configuração inicial, armazenado exclusivamente no seu dispositivo em forma criptografada.
        """.trimIndent()
    ),
    LegalSection(
        "Armazenamento Local",
        """
        Todos os seus dados (senhas, passkeys, notas, cartões de crédito, contas bancárias, carteiras de criptomoedas, anexos e histórico de senhas) são armazenados criptografados usando AES-256-GCM com uma chave derivada da sua senha mestre usando Argon2id, que nunca sai do seu dispositivo. Se você ativar o desbloqueio biométrico, uma cópia dessa chave é protegida adicionalmente por uma chave do Android Keystore do seu dispositivo. Nenhum dado sai do seu dispositivo sem o seu consentimento explícito.
        """.trimIndent()
    ),
    LegalSection(
        "Senha Mestre",
        """
        Sua senha mestre nunca é armazenada em texto simples nem em qualquer forma recuperável. Ela é usada exclusivamente para derivar a chave de criptografia do cofre via Argon2id. Se você perder sua senha mestre, seus dados não poderão ser recuperados.
        """.trimIndent()
    ),
    LegalSection(
        "Biometria",
        """
        A autenticação biométrica no KeyArmor é gerenciada inteiramente pelo sistema operacional Android por meio da API BiometricPrompt. O KeyArmor não acessa, armazena nem processa dados biométricos em nenhum momento.

        A biometria é utilizada em dois contextos dentro do aplicativo:

        • Desbloqueio do cofre: para autenticar o acesso ao aplicativo na inicialização, disponível em todos os planos.
        • Proteção biométrica avançada (Premium): uma barreira opcional que exige verificação biométrica antes de revelar campos sensíveis em seções específicas do cofre (senhas, cartões de crédito, campos personalizados). Configurável em Configurações — suporta modo por-acesso ou por-sessão com tempo limite personalizável. As mesmas garantias de privacidade se aplicam: o KeyArmor não armazena nem processa dados biométricos.
        """.trimIndent()
    ),
    LegalSection(
        "Sincronização com o Google Drive",
        """
A sincronização com o Google Drive é totalmente opcional. Quando ativada, o KeyArmor:

• Solicita uma única permissão mínima (DRIVE_APPDATA) para armazenar os seus próprios backups criptografados e os pontos de restauração do Time Machine numa pasta privada, exclusiva do app, no seu Google Drive. Essa pasta é oculta — nenhum outro app, e nem você ao navegar no Drive, pode vê-la ou modificá-la.
• Criptografa o cofre com uma chave de backup de 12 palavras antes de enviá-lo. A chave é criada no seu dispositivo e só você a possui: nem o Google nem o desenvolvedor podem ler o arquivo ou recuperar a chave.
• Se você salvar o PDF da sua chave de backup no Google Docs, no Drive ou em outro aplicativo, faça isso por conta própria por meio da tela de compartilhamento do Android; esse aplicativo é regido por sua própria política de privacidade.
• Se você trocar sua chave, poderá manter as anteriores, criptografadas no seu celular; o PDF do kit pode incluí-las. O KeyArmor não as envia a nenhum servidor.
• Nunca acessa nenhum outro arquivo do Drive, nunca cria pastas visíveis e nunca compartilha arquivos.
• Opcionalmente mantém uma segunda cópia local do backup na pasta Downloads/KeyArmor/ do seu dispositivo, que nunca sai do dispositivo.
• A sincronização sob demanda está disponível em todos os planos (acionada manualmente). A sincronização diária automática está disponível no Premium.

A Herança Digital e o Plano Família utilizam o Google Firebase, e não o seu Google Drive. O Firebase gerencia o consentimento, os convites e as notificações. O cofre de herança é criptografado de ponta a ponta no seu dispositivo com a chave pública do herdeiro, e o bloco criptografado resultante é enviado para um bucket privado do Firebase Cloud Storage do desenvolvedor por meio de links assinados de curta duração (15 minutos). O servidor armazena esse bloco, mas nunca recebe a chave do herdeiro; portanto, não consegue lê-lo.

Você pode desconectar a sua conta Google a qualquer momento na tela de sincronização.
    """.trimIndent()
    ),
    LegalSection(
        "Anexos",
        """
        O recurso de anexos permite associar arquivos (até 5 por entrada, com no máximo 5 MB cada) às entradas do seu cofre. Todos os anexos são criptografados com AES-256-GCM antes de serem salvos no armazenamento interno do dispositivo. Se você ativar a sincronização com o Google Drive, seus anexos e fotos de documentos serão incluídos no backup que você enviar, criptografados com sua chave de backup. Eles nunca são enviados para nenhum outro serviço.
        """.trimIndent()
    ),
    LegalSection(
        "Histórico de Senhas",
        """
        O KeyArmor mantém um histórico local de senhas anteriores para cada entrada. Esse histórico é armazenado criptografado no dispositivo e nunca é transmitido a nenhum servidor externo.
        """.trimIndent()
    ),
    LegalSection(
        "Notificações in-app",
        """
        O KeyArmor gera notificações internas para alertá-lo sobre eventos de segurança importantes:

        • Avisos de expiração: enviados quando uma ou mais senhas estão se aproximando da data de expiração.
        • Senhas expiradas: enviadas quando uma ou mais senhas já passaram da data de expiração.
        • Alertas de violação: enviados quando suas senhas são detectadas em violações de dados conhecidas.

        Esses três tipos de notificação são gerados inteiramente no seu dispositivo e nunca são transmitidos para servidores externos. Se você usar a Herança Digital ou o Plano Família, também receberá notificações sobre esses recursos (um herdeiro aceitando, um membro da família sendo adicionado, etc.); essas são originadas pelo Google Firebase e entregues via Firebase Cloud Messaging, já que dependem de eventos iniciados por outra conta. Todas as notificações, independentemente da origem, são excluídas automaticamente após 30 dias. Você pode excluir qualquer notificação deslizando-a lateralmente ou apagar tudo de uma vez na tela de Notificações. As notificações push requerem permissão do sistema para serem enviadas.
        """.trimIndent()
    ),
    LegalSection(
        "Auditoria de Segurança",
        """
        O recurso de auditoria de segurança analisa suas senhas localmente no dispositivo. A verificação de senhas comuns é feita comparando com uma lista predefinida armazenada dentro do próprio aplicativo, sem consultas a serviços externos.
        """.trimIndent()
    ),
    LegalSection(
        "Monitoramento de Violações (HIBP)",
        """
        O KeyArmor pode verificar se suas senhas apareceram em violações de dados conhecidas usando o serviço Have I Been Pwned (HIBP), operado pelo pesquisador de segurança Troy Hunt.

        Esta verificação usa um modelo de k-anonimato: apenas os primeiros 5 caracteres de um hash SHA-1 da sua senha são enviados para a API do HIBP. O hash completo nunca é transmitido. A correspondência é realizada localmente no seu dispositivo. Nenhum nome de usuário, endereço de e-mail ou associação de conta é enviado. As respostas do HIBP são preenchidas com hashes falsos para que mesmo a análise de tráfego de rede não revele nada sobre a senha verificada.
        """.trimIndent()
    ),
    LegalSection(
        "Serviço de Preenchimento Automático",
        """
        O KeyArmor inclui um serviço de Preenchimento Automático Android opcional. Quando habilitado nas configurações do sistema, ele lê o nome do pacote do aplicativo ou a URL do site da tela de login ativa para sugerir credenciais correspondentes do seu cofre. Essa correspondência é realizada inteiramente no dispositivo. Nenhum nome de aplicativo, URL de site ou credencial é transmitido a qualquer servidor externo. O serviço de preenchimento automático não lê o conteúdo de outros aplicativos além do estritamente necessário para identificar o contexto de login.
        """.trimIndent()
    ),
    LegalSection(
        "Saldo em Tempo Real de Carteiras Cripto",
        """
        Quando você armazena uma entrada de carteira cripto e habilita o monitoramento de saldo em tempo real, o KeyArmor consulta nós ou APIs públicas de blockchain para obter o saldo associado ao endereço de carteira inserido. Apenas o endereço da carteira é transmitido — nenhuma chave privada, frase-semente ou dado pessoal é enviado. Essas consultas vão diretamente para infraestrutura pública; o KeyArmor não opera nenhum servidor intermediário.
        """.trimIndent()
    ),
    LegalSection(
        "Compartilhamento P2P de Entradas",
        """
        O KeyArmor permite que você compartilhe entradas individuais do cofre diretamente com outros usuários do KeyArmor via QR code ou texto criptografado. Essa transferência é criptografada de ponta a ponta e peer-to-peer — nenhum servidor ou intermediário está envolvido. Você é o único responsável por escolher com quem compartilha entradas e pela segurança do canal usado para transmitir o texto criptografado (se não usar QR diretamente).
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotecas de Terceiros",
        """
        O KeyArmor usa bibliotecas de código aberto (consulte o Aviso de Código Aberto). Nenhuma dessas bibliotecas inclui mecanismos de telemetria, análise ou coleta de dados na configuração usada por este aplicativo.

        O KeyArmor também depende de dois serviços de terceiros para recursos específicos: RevenueCat, que verifica o status da sua assinatura Premium e recebe apenas informações relacionadas à compra (nunca o seu cofre, senha mestre ou chave de recuperação); e Brevo, que envia os e-mails transacionais exigidos pela Herança Digital e pelo Plano Família (convites, confirmações de consentimento, notificações) e, para isso, recebe o endereço de e-mail das pessoas envolvidas nesses recursos. Nenhum dos dois serviços é usado para publicidade ou análise comportamental.
        """.trimIndent()
    ),
    LegalSection(
        "Widget do ecrã inicial",
        """
        KeyArmor inclui um widget opcional para o ecrã inicial do Android que mostra o número total de entradas guardadas no teu cofre. Este widget acede apenas a dados locais do dispositivo. Não transmite nenhuma informação para servidores externos. O contador de entradas é um dado agregado que não revela o conteúdo de nenhuma entrada individual. Podes escolher se queres ou não colocar o widget no teu ecrã inicial.
        """.trimIndent()
    ),
    LegalSection(
        "Permissões",
        """
        O KeyArmor solicita apenas as permissões necessárias para seu funcionamento:

        • Biometria (opcional): para desbloqueio por impressão digital.
        • Internet (opcional): usado apenas para os recursos que você mesmo ativa — sincronização com o Google Drive, Herança Digital e Plano Família (via Google Firebase), consultas de saldo em tempo real de carteiras cripto, verificação de violações via Have I Been Pwned, e verificação de assinatura (Google Play Billing / RevenueCat).
        • Acesso a arquivos (opcional): para importação/exportação do cofre e adição de anexos, somente quando você iniciar.
        • Câmera (opcional): para escanear QR codes ao configurar autenticação de dois fatores (2FA/TOTP) ou ao receber entradas compartilhadas. Ativada somente mediante solicitação explícita do usuário.
        • Serviço de preenchimento automático (opcional): habilitado manualmente nas configurações do sistema Android. Usado exclusivamente para sugerir credenciais em telas de login de outros aplicativos.
        • Microfone (opcional): apenas para o ditado por voz em Notas Seguras. O reconhecimento é feito no seu dispositivo; o KeyArmor não grava, não guarda nem envia áudio a nenhum servidor.
        • Calendário, somente leitura (opcional): usado pelo Modo Viagem Seguro para procurar viagens nos seus eventos. Os eventos são processados no seu dispositivo e nunca são transmitidos.
        • Notificações e alarmes exatos (opcional): para os alertas de segurança e os lembretes que você programa. O evento de inicialização do sistema é usado para reprogramá-los após um reinício.
        • Estado da rede (opcional): apenas para saber se está com dados móveis ou Wi-Fi, de modo que os backups pesados do Google Drive esperem pelo Wi-Fi. Nenhuma informação sobre a sua rede é recolhida nem enviada.

        O KeyArmor não solicita acesso a contatos, localização ou quaisquer outros dados pessoais não mencionados acima.
        """.trimIndent()
    ),
    LegalSection("Outros serviços que recebem dados", """
Além dos serviços descritos acima, o KeyArmor usa os seguintes, apenas para as funções que deles precisam:

• Verificação de integridade do app (Firebase App Check / Google Play Integrity): o Google fornece uma atestação do seu dispositivo para que o nosso servidor aceite apenas pedidos do app autêntico.
• RevenueCat: além das informações de compra, recebe o identificador da sua conta Google (não o seu e-mail) como ID de utilizador, para associar a sua assinatura entre dispositivos.
• CoinGecko: quando usa os preços de cripto em tempo real, os símbolos da sua carteira são pedidos à CoinGecko, que pode ver o seu endereço IP. Os exploradores e nós de blockchain mencionados acima também podem vê-lo.
• Google Fonts: o app descarrega as suas fontes Inter e JetBrains Mono através dos serviços do Google Play, pelo que o Google pode ver o seu endereço IP quando são pedidas.
• Hora de confiança dos serviços do Google Play: usada para detetar adulterações do relógio do dispositivo. Não são enviados dados pessoais.
• O reconhecimento de texto (OCR) de documentos é feito no seu dispositivo com o ML Kit. Nenhuma imagem é enviada a servidor algum.
• Ko-fi: apenas se abrir o link de doação; nesse caso aplica-se a política de privacidade deles.

Os nossos servidores podem registar temporariamente o seu endereço IP para limitar abusos.
    """.trimIndent()),
    LegalSection(
        "Passkeys (chaves de acesso)",
        """
        O KeyArmor pode funcionar como provedor de passkeys do Android (Credential Manager), somente se você escolher isso nas configurações do sistema. Ao criar ou usar uma passkey, o KeyArmor lê o domínio do site ou do app que a solicita para associá-la a esse site e para assinar o desafio que o site envia. A chave privada é gerada no seu dispositivo e guardada criptografada junto com o resto do seu cofre; não é enviada a nenhum servidor nem a terceiros. Ela só sai do seu dispositivo dentro dos backups que você mesmo criar (por exemplo, o backup criptografado no Google Drive). Para assinar com uma passkey, o KeyArmor pede que você confirme sua identidade com a sua digital ou a sua senha mestre. O site em que você entra recebe apenas a chave pública e a assinatura, como em qualquer passkey.
        """.trimIndent()
    ),
    LegalSection(
        "Exclusão e Retenção de Dados",
        """
        Seu cofre fica guardado no seu dispositivo (e no seu Google Drive, se você ativar o backup) até que você o apague. Em nossos servidores há apenas dados de coordenação: o token de notificações push identificado pelo seu e-mail e, se você usar a Herança Digital ou o Plano Família, os registros de convites e de consentimento e o bloco criptografado do cofre de herança. Eles são mantidos enquanto você usar essas funções ou até que você peça a exclusão. Você pode apagá-los em Configurações → Apagar todos os meus dados, ou escrevendo para support@thormakk.dev a partir da sua conta Google; processamos esses pedidos em até 30 dias. O detalhe completo está na página «Excluir Conta e Dados» do nosso site.
        """.trimIndent()
    ),
    LegalSection(
        "Menores de Idade",
        """
        O KeyArmor não é destinado a menores de 13 anos (nem à idade mínima definida pela lei do seu país para consentimento digital), e não coletamos conscientemente dados de menores. Se você acredita que um menor nos forneceu dados, escreva para support@thormakk.dev e os excluiremos.
        """.trimIndent()
    ),
    LegalSection(
        "Contato",
        "Para qualquer dúvida relacionada com esta política, pode escrever para support@thormakk.dev"
    ),
    LegalSection(
        "Última atualização",
        "Outubro de 2026 (rev. 8)"
    )
)

private fun termsPT() = listOf(
    LegalSection(
        "Termos e Condições",
        ""
    ),
    LegalSection(
        "Aceitação",
        """
        Ao instalar ou usar o KeyArmor, você aceita estes termos em sua totalidade. Se você discordar de qualquer parte, não use o aplicativo.
        """.trimIndent()
    ),
    LegalSection(
        "Uso Permitido",
        """
        O KeyArmor foi projetado para uso pessoal como gerenciador de senhas e cofre seguro. É proibido o uso para atividades ilegais, armazenamento de informações que facilitem atos ilícitos ou tentativas de fazer engenharia reversa do aplicativo.
        """.trimIndent()
    ),
    LegalSection(
        "Responsabilidade do Usuário",
        """
        Você é o único responsável por:

        • A segurança da sua senha mestre. Se perdida, não há mecanismo de recuperação e seus dados ficarão permanentemente inacessíveis.
        • A chave de backup de 12 palavras (caso você utilize esse recurso). Se você a perder e também perder seu celular, seus backups do Drive não poderão ser abertos, nem mesmo pelo desenvolvedor.
        • Manter backups atualizados do seu cofre por meio da função de exportação ou da sincronização com o Drive.
        """.trimIndent()
    ),
    LegalSection(
        "Funcionalidade de Sincronização",
        """
        A sincronização com o Google Drive é um recurso auxiliar de backup. Ela não substitui boas práticas pessoais de segurança. O desenvolvedor não garante a disponibilidade contínua deste recurso, pois depende de serviços de terceiros (Google).
        """.trimIndent()
    ),
    LegalSection(
        "Assinaturas Premium",
        """
        O KeyArmor oferece planos pagos opcionais (Mensal e Anual) que desbloqueiam recursos adicionais. As assinaturas são processadas exclusivamente pelo Google Play Billing. Ao adquirir uma assinatura, você também concorda com os Termos de Serviço do Google Play.

        • Os planos Mensal e Anual são assinaturas com renovação automática. Você pode cancelar a qualquer momento pelo Google Play; o cancelamento entra em vigor no final do período de cobrança vigente.
        • Os reembolsos estão sujeitos à política de reembolso padrão do Google Play. O desenvolvedor não processa reembolsos de forma independente.
        • Os recursos premium podem evoluir ao longo do tempo. O desenvolvedor reserva-se o direito de adicionar, modificar ou descontinuar recursos premium específicos com aviso razoável.
        """.trimIndent()
    ),
    LegalSection(
        "Modo Viagem Segura",
        """
        O Modo Viagem Segura permite marcar entradas específicas como seguras para viagem e ocultar todas as outras enquanto o modo estiver ativo. Você é o único responsável por determinar quais entradas marcar como seguras para viagem e pelas consequências de ativar ou desativar este modo. O desenvolvedor não assume responsabilidade por entradas acidentalmente deixadas visíveis ou ocultas.
        """.trimIndent()
    ),
    LegalSection(
        "PIN de Pânico",
        """
        O recurso PIN de Pânico aciona uma ação irreversível (apagar ou ocultar dados sensíveis do cofre) quando um PIN designado é inserido. Uma vez ativado, os dados ocultos ou excluídos podem não ser recuperáveis dependendo da sua configuração. Você assume total responsabilidade pela configuração deste recurso e por qualquer perda de dados resultante de sua ativação intencional ou acidental.
        """.trimIndent()
    ),
    LegalSection(
        "Informações de Saldo Cripto",
        """
        O recurso de saldo cripto em tempo real obtém informações de saldo da infraestrutura pública de blockchain apenas para fins informativos. Essas informações não constituem aconselhamento financeiro. O desenvolvedor não garante a precisão, integridade ou atualidade dos dados de saldo. Não tome decisões financeiras com base exclusivamente nas informações exibidas neste aplicativo.
        """.trimIndent()
    ),
    LegalSection(
        "Sem Garantias",
        """
        O aplicativo é fornecido "no estado em que se encontra", sem garantias de qualquer tipo, expressas ou implícitas. O desenvolvedor não garante que o aplicativo esteja livre de erros, opere sem interrupções ou seja adequado a uma finalidade específica.
        """.trimIndent()
    ),
    LegalSection(
        "Limitação de Responsabilidade",
        """
        Na máxima extensão permitida pela legislação aplicável, o desenvolvedor não será responsável por perda de dados, perda de acesso ao cofre ou quaisquer danos diretos, indiretos, incidentais, especiais ou consequentes decorrentes do uso ou da impossibilidade de uso do aplicativo.
        """.trimIndent()
    ),
    LegalSection(
        "Atualizações dos Termos",
        """
        Estes termos podem ser atualizados a qualquer momento. O uso continuado do aplicativo após uma atualização publicada constitui aceitação dos novos termos.
        """.trimIndent()
    ),
    LegalSection(
        "Última atualização",
        "Maio de 2026 (rev. 2)"
    )
)

private fun legalPT() = listOf(
    LegalSection(
        "Aviso Legal e Isenção de Responsabilidade",
        ""
    ),
    LegalSection(
        "Software Fornecido No Estado Em Que Se Encontra",
        """
        O KeyArmor é distribuído "no estado em que se encontra", sem garantias de qualquer tipo, expressas ou implícitas, incluindo, entre outras, garantias de comercialização, adequação a uma finalidade específica ou não violação.
        """.trimIndent()
    ),
    LegalSection(
        "Limitação de Responsabilidade",
        """
        O desenvolvedor do KeyArmor não será responsável em nenhuma circunstância por:

        • Perda ou corrupção de dados armazenados no aplicativo
        • Perda do acesso ao cofre devido ao esquecimento ou à perda da senha mestra ou da chave de backup
        • Vulnerabilidades ou falhas em componentes do sistema operacional Android
        • Falhas, interrupções ou alterações nos serviços do Google Drive
        • Quaisquer danos diretos, indiretos, incidentais, especiais ou consequentes decorrentes do uso ou da impossibilidade de uso do aplicativo
        • Qualquer dano resultante de acesso não autorizado aos dados do usuário por terceiros
        • Perda de dados resultante da ativação intencional ou acidental do recurso PIN de Pânico
        """.trimIndent()
    ),
    LegalSection(
        "Transmissão de Dados para o Google Drive",
        """
Quando o usuário ativa a sincronização com o Google Drive (backups) ou usa a Herança Digital / o Plano Família (Google Firebase), os dados são transmitidos já criptografados para a infraestrutura do Google, sob os Termos de Serviço do Google. Em todos os casos, os dados saem do dispositivo como um bloco criptografado opaco. O desenvolvedor não tem acesso nem controle sobre a infraestrutura do Google e não pode ler o conteúdo transmitido.
    """.trimIndent()
    ),
    LegalSection(
        "Android Keystore e Dependência de Hardware",
        """
        A segurança da criptografia do KeyArmor depende em parte da implementação do Android Keystore do dispositivo do usuário. Essa implementação varia por fabricante, chipset e versão do Android. O desenvolvedor não controla nem garante a segurança de implementações de hardware de terceiros. Recomenda-se fortemente manter o sistema operacional do dispositivo atualizado.
        """.trimIndent()
    ),
    LegalSection(
        "Limitações do Serviço de Preenchimento Automático",
        """
        O serviço de Preenchimento Automático do KeyArmor pode não funcionar corretamente com todos os aplicativos ou navegadores. Em particular, o Google Chrome não oferece suporte ao framework padrão de preenchimento automático do Android — esta é uma limitação do Chrome que afeta todos os gerenciadores de senhas de terceiros igualmente e está fora do controle do desenvolvedor. O desenvolvedor não garante que o serviço de preenchimento automático funcionará com nenhum aplicativo específico.
        """.trimIndent()
    ),
    LegalSection(
        "Isenção de Responsabilidade sobre Saldo Cripto",
        """
        Os dados de saldo em tempo real são obtidos de nós e APIs públicos de blockchain de terceiros. O desenvolvedor não opera nem controla esses nós. As informações de saldo são fornecidas apenas para fins informativos e podem estar desatualizadas, imprecisas ou indisponíveis. Essas informações não constituem aconselhamento financeiro ou de investimento. O desenvolvedor não assume responsabilidade por decisões tomadas com base nos dados de saldo exibidos no aplicativo.
        """.trimIndent()
    ),
    LegalSection(
        "Compartilhamento P2P",
        """
        O recurso de compartilhamento P2P de entradas facilita a troca direta e criptografada de entradas do cofre entre usuários. O desenvolvedor não assume responsabilidade pelo conteúdo das entradas compartilhadas entre usuários, pela identidade ou confiabilidade do destinatário, ou pelas consequências decorrentes do compartilhamento de dados do cofre com terceiros.
        """.trimIndent()
    ),
    LegalSection(
        "Bibliotecas de Terceiros",
        """
        O KeyArmor usa bibliotecas de código aberto de terceiros consideradas seguras no momento da publicação. O desenvolvedor não assume responsabilidade por falhas, vulnerabilidades ou comportamentos inesperados originados em componentes de terceiros.
        """.trimIndent()
    ),
    LegalSection(
        "Lei Aplicável",
        """
        Este aviso legal é regido pelas leis em vigor na República Argentina. Qualquer disputa decorrente do uso do aplicativo será submetida aos tribunais competentes dessa jurisdição.
        """.trimIndent()
    ),
    LegalSection(
        "Última atualização",
        "Maio de 2026 (rev. 2)"
    )
)

private fun licensePT() = listOf(
    LegalSection("Licença Source Available", ""),
    LegalSection("Código-Fonte Publicado Apenas para Fins de Auditoria de Segurança", """
O código-fonte do KeyArmor está publicamente disponível exclusivamente para que pesquisadores de segurança e usuários possam inspecionar e auditar suas práticas de segurança. Nenhum direito além desse é concedido — todos os direitos são reservados ao titular dos direitos autorais.
    """.trimIndent()),
    LegalSection("O Que Você Pode Fazer", """
• Ler e inspecionar o código-fonte para pesquisa de segurança e auditoria pessoal
• Reportar vulnerabilidades de segurança à equipe do KeyArmor pelos canais oficiais
    """.trimIndent()),
    LegalSection("O Que Você Não Pode Fazer", """
Sem permissão prévia e explícita por escrito do titular dos direitos autorais, você não pode:

• Usar ou executar o software fora de sua distribuição oficial
• Copiar, reproduzir ou extrair o código-fonte para qualquer finalidade que não seja a leitura
• Modificá-lo, adaptá-lo, traduzi-lo ou criar obras derivadas com base nele
• Distribuí-lo, publicá-lo, sublicenciá-lo, vendê-lo ou de outra forma disponibilizá-lo a terceiros
• Usar o código-fonte como base para outro produto, serviço ou projeto
• Usar o nome, logotipo ou qualquer marca associada ao KeyArmor
    """.trimIndent()),
    LegalSection("Código-Fonte", """
O código-fonte do KeyArmor está disponível publicamente.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Sem Garantia", """
O código-fonte é fornecido "como está", sem garantia de qualquer tipo, expressa ou implícita. O titular dos direitos autorais não faz nenhuma declaração quanto à sua adequação a qualquer finalidade específica.
    """.trimIndent()),
    LegalSection("Direitos Autorais", "© 2026 KeyArmor. Todos os direitos reservados. Source Available.")
)

private fun opensourcePT() = listOf(
    LegalSection(
        "Aviso de Código Aberto",
        ""
    ),
    LegalSection(
        "Bibliotecas Utilizadas",
        """
        O KeyArmor usa as seguintes bibliotecas de código aberto. Salvo indicação em contrário, elas são distribuídas sob a licença Apache 2.0.
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Compose",
        """
        Versão: BOM 2026.09.00 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose
        """.trimIndent()
    ),
    LegalSection(
        "Jetpack Glance (AppWidget)",
        """
        Versão: 1.2.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/glance
        """.trimIndent()
    ),

    LegalSection(
        "Room Database",
        """
        Versão: 2.8.5 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/data-storage/room
        """.trimIndent()
    ),
    LegalSection(
        "Hilt (Dagger)",
        """
        Versão: 2.60.1 | Licença: Apache 2.0
        Copyright © The Dagger Authors
        https://dagger.dev/hilt/
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Coroutines",
        """
        Versão: 1.11.0 | Licença: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/Kotlin/kotlinx.coroutines
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Biometric",
        """
        Versão: 1.1.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/biometric
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Navigation",
        """
        Versão: 2.10.2 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/guide/navigation
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Lifecycle / ViewModel",
        """
        Versão: 2.11.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/lifecycle
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX WorkManager",
        """
        Versão: 2.12.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/topic/libraries/architecture/workmanager
        """.trimIndent()
    ),
    LegalSection(
        "Google Play Services Auth",
        """
        Versão: 22.0.0 | Licença: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/guides/setup
        """.trimIndent()
    ),
    LegalSection(
        "Google API Client Android",
        """
        Versão: 2.9.2 | Licença: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-api-java-client
        """.trimIndent()
    ),
    LegalSection(
        "Google Drive API v3",
        """
        Versão: v3-rev20240123-2.0.0 | Licença: Apache 2.0
        Copyright © Google LLC
        https://developers.google.com/drive/api/v3/about-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Google HTTP Client GSON",
        """
        Versão: 2.2.0 | Licença: Apache 2.0
        Copyright © Google LLC
        https://github.com/googleapis/google-http-java-client
        """.trimIndent()
    ),
    LegalSection(
        "ZXing Android Embedded",
        """
        Versão: 4.3.0 | Licença: Apache 2.0
        Copyright © ZXing authors, journeyapps
        https://github.com/journeyapps/zxing-android-embedded
        """.trimIndent()
    ),
    LegalSection(
        "Accompanist Permissions",
        """
        Versão: 0.37.3 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://google.github.io/accompanist/permissions/
        """.trimIndent()
    ),
    LegalSection(
        "Compose Material Icons Extended",
        """
        Versão: BOM 2026.09.00 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/compose/graphics/images/material
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core KTX",
        """
        Versão: 1.19.1 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Activity Compose",
        """
        Versão: 1.13.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/activity
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Autofill",
        """
        Versão: 1.3.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/autofill
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX DataStore",
        """
        Versão: 1.3.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/datastore
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX AppCompat",
        """
        Versão: 1.8.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/appcompat
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Hilt (Navigation, Work)",
        """
        Versão: 1.4.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/hilt
        """.trimIndent()
    ),
    LegalSection(
        "Kotlin Standard Library",
        """
        Versão: 2.4.21 | Licença: Apache 2.0
        Copyright © JetBrains s.r.o.
        https://github.com/JetBrains/kotlin
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Core Splashscreen",
        """
        Versão: 1.2.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/core
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Profileinstaller",
        """
        Versão: 1.4.1 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/profileinstaller
        """.trimIndent()
    ),
    LegalSection(
        "AndroidX Credential Manager",
        """
        Versão: 1.6.0 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/jetpack/androidx/releases/credentials
        """.trimIndent()
    ),
    LegalSection(
        "CameraX",
        """
        Versão: 1.6.2 | Licença: Apache 2.0
        Copyright © The Android Open Source Project
        https://developer.android.com/training/camerax
        """.trimIndent()
    ),
    LegalSection(
        "Firebase Android SDK (Messaging, Auth, App Check)",
        """
        Versão: BOM 35.1.0 | Licença: Apache 2.0
        Copyright © Google LLC
        https://github.com/firebase/firebase-android-sdk
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Time",
        """
        Versão: 2.15.0 | Licença: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-time/
        """.trimIndent()
    ),
    LegalSection(
        "Joda-Convert",
        """
        Versão: 3.0.1 | Licença: Apache 2.0
        Copyright © Joda.org
        https://www.joda.org/joda-convert/
        """.trimIndent()
    ),
    LegalSection(
        "PDFBox-Android",
        """
        Versão: 2.0.27.0 | Licença: Apache 2.0
        Copyright © Tom Roush
        https://github.com/TomRoush/PdfBox-Android
        """.trimIndent()
    ),
    LegalSection(
        "RichEditor Compose",
        """
        Versão: 1.2.1 | Licença: MIT
        Copyright © Mohamed Rejeb
        https://github.com/MohamedRejeb/compose-rich-editor
        """.trimIndent()
    ),
    LegalSection(
        "Bouncy Castle Provider",
        """
        Versão: 1.86 | Licença: Bouncy Castle License (MIT-style)
        Copyright © The Legion of the Bouncy Castle Inc.
        https://www.bouncycastle.org/about/license/
        """.trimIndent()
    ),
    LegalSection(
        "RevenueCat Purchases Android SDK",
        """
        Versão: 10.29.0 | Licença: MIT
        Copyright © RevenueCat, Inc.
        https://github.com/RevenueCat/purchases-android
        """.trimIndent()
    ),
    LegalSection(
        "LazySodium for Android",
        """
        Versão: 5.2.0 | Licença: MPL-2.0
        Copyright © Terl Tech Ltd
        https://github.com/terl/lazysodium-android
        """.trimIndent()
    ),
    LegalSection(
        "JNA (Java Native Access)",
        """
        Versão: 5.19.1 | Licença: Apache 2.0 / LGPL 2.1 (dual-licensed)
        Copyright © The JNA Project Contributors
        https://github.com/java-native-access/jna
        """.trimIndent()
    ),
    LegalSection(
        "Google Identity Services (GoogleId)",
        """
        Versão: 1.2.1 | Licença: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/identity/android-credential-manager
        """.trimIndent()
    ),
    LegalSection(
        "Play Services Time",
        """
        Versão: 16.0.1 | Licença: Google Play Services Terms
        Copyright © Google LLC
        https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
        """.trimIndent()
    ),
    LegalSection(
        "ML Kit Text Recognition",
        """
        Versão: 16.0.1 | Licença: ML Kit Terms
        Copyright © Google LLC
        https://developers.google.com/ml-kit/terms
        """.trimIndent()
    ),
    LegalSection(
        "Public Suffix List",
        """
        Versão: 2026-07-09 | Licença: MPL-2.0
        Copyright © Mozilla Foundation and contributors
        https://publicsuffix.org
        """.trimIndent()
    ),
    LegalSection(
        "Texto Completo do Apache 2.0",
        """
        O texto completo da licença Apache 2.0 está disponível em:
        https://www.apache.org/licenses/LICENSE-2.0
        MIT: https://opensource.org/license/mit
        MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
        """.trimIndent()
    )
)

private fun deleteDataPT() = listOf(
    LegalSection("Excluir conta e dados", ""),
    LegalSection("O que a KeyArmor armazena sobre você", """
Seu cofre (senhas, notas, cartões, carteiras, anexos e histórico de senhas) nunca é armazenado em nossos servidores. Ele fica criptografado no seu dispositivo e, se você ativar o backup, no seu próprio Google Drive — não em nenhum lugar onde possamos lê-lo ou excluí-lo, pois ele é criptografado antes mesmo de sair do seu dispositivo.

Os únicos dados associados à sua conta do Google que chegam aos nossos servidores (Firebase) estão vinculados à conexão dessa conta para um recurso opcional — backup no Google Drive, Herança Digital ou Plano Familiar. O simples fato de vincular uma conta do Google a qualquer um desses recursos gera um registro mínimo lá (um token de notificação push, identificado pelo seu e-mail), mesmo antes de você configurar o Herança Digital ou o Plano Familiar. Se você nunca vinculou uma conta do Google ao KeyArmor, não há nada sobre você em nossos servidores para ser excluído.
    """.trimIndent()),
    LegalSection("Exclusão pelo aplicativo", """
Você pode excluir todos os seus dados a qualquer momento em Configurações → Apagar todos os meus dados dentro do aplicativo. Se você nunca conectou uma conta do Google ao KeyArmor (para backup no Drive, Herança Digital ou Plano Familiar), isso apaga imediata e permanentemente seu cofre do dispositivo.

Caso tenha conectado uma conta, o aplicativo solicitará que você faça login com o Google para que, na mesma ação, ele possa apagar os registros associados no servidor (Firebase) e as cópias de backup e do Time Machine que o KeyArmor salvou no seu Google Drive. Somente se você não conseguir fazer login é que o aplicativo passará a apagar apenas os dados locais — e ele avisará com antecedência que seus registros no servidor permanecerão até que sejam removidos separadamente, por meio da solicitação abaixo.
    """.trimIndent()),
    LegalSection("O que não é excluído", """
A exclusão dos seus dados não remove o que está fora do aplicativo:

• Arquivos que você mesmo exportou (arquivos CSV, PDF ou .kab que você salvou ou compartilhou). Eles pertencem a você e estão armazenados onde você os colocou; portanto, você precisa excluí-los nesse local.
• Cópias de backup que uma instalação anterior do KeyArmor deixou na sua pasta “Downloads”. O Android não permite mais que uma nova instalação altere esses arquivos; exclua-os da sua pasta “Downloads”.
• Sua assinatura. Ela é gerenciada pelo Google Play, portanto, excluir seus dados não a cancela; cancele-a no Google Play → Assinaturas.
    """.trimIndent()),
    LegalSection("Solicitação de exclusão dos dados da sua conta", """
Se você vinculou uma conta do Google ao KeyArmor, mas não consegue fazer login para usar a opção de exclusão no aplicativo mencionada acima (por exemplo, se você não tiver mais o aplicativo instalado), solicite a exclusão dos dados associados no servidor enviando um e-mail a partir dessa mesma conta do Google.

Escreva para: support@thormakk.dev

Processamos as solicitações de exclusão em até 30 dias e confirmamos por e-mail assim que concluídas. Você não precisa ter o aplicativo instalado para fazer essa solicitação.
    """.trimIndent())
)
