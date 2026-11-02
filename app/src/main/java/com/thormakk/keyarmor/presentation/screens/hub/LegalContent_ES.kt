package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentES(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyES()
    "terms"      -> termsES()
    "legal"      -> legalES()
    "license"    -> licenseES()
    "opensource" -> opensourceES()
    "delete"     -> deleteDataES()
    else         -> listOf(LegalSection("Documento no encontrado", ""))
}

private fun privacyES() = listOf(
    LegalSection("Política de Privacidad", ""),
    LegalSection("Recopilación de datos", """
KeyArmor no recopila, transmite ni comparte ningún dato personal a servidores externos. Toda la información que ingresás en la aplicación permanece exclusivamente en tu dispositivo, salvo que vos mismo activés la sincronización con Google Drive.

KeyArmor no recopila tu dirección de correo electrónico ni ninguna credencial de cuenta para las funciones centrales de la app (bóveda, autocompletado, auditoría de seguridad, monitoreo de brechas) — esas funcionan por completo sin ninguna cuenta. Si activás la Herencia Digital o el Plan Familiar, esas funciones requieren iniciar sesión con Google: tu dirección de correo de Google se guarda entonces en Google Firebase para identificarte y vincularte con los herederos o miembros de familia que designés, y para enviarte los correos de notificación correspondientes a través de nuestro proveedor de email (Brevo). Ese correo se usa exclusivamente para esas dos funciones y nunca se vende ni se comparte con nadie más. Fuera de la Herencia Digital y el Plan Familiar, el único identificador personal es un nombre o apodo opcional que podés configurar durante la configuración inicial, el cual se almacena exclusivamente en tu dispositivo en forma cifrada.
    """.trimIndent()),
    LegalSection("Almacenamiento local", """
Todos tus datos (contraseñas, passkeys, notas, tarjetas, cuentas bancarias, billeteras cripto, adjuntos e historial de contraseñas) se almacenan cifrados con AES-256-GCM con una clave derivada de tu contraseña maestra mediante Argon2id, que nunca sale de tu dispositivo. Si activás el desbloqueo biométrico, una copia de esa clave se protege además con una clave del Android Keystore de tu dispositivo. Ningún dato abandona tu dispositivo sin tu consentimiento explícito.
    """.trimIndent()),
    LegalSection("Contraseña maestra", """
Tu contraseña maestra nunca se almacena en texto plano ni en ninguna forma recuperable. Se utiliza únicamente para derivar la clave de cifrado de tu bóveda mediante Argon2id. Si perdés tu contraseña maestra, tus datos no pueden recuperarse.
    """.trimIndent()),
    LegalSection("Biometría", """
La autenticación biométrica en KeyArmor es gestionada íntegramente por el sistema operativo Android a través de la API BiometricPrompt. KeyArmor no accede, almacena ni procesa datos biométricos en ningún momento.

La biometría se utiliza en dos contextos dentro de la aplicación:

• Desbloqueo de bóveda: para autenticar el acceso a la app al iniciarla, disponible en todos los planes.
• Protección biométrica avanzada (Premium): una barrera opcional que requiere verificación biométrica antes de revelar campos sensibles en secciones específicas (contraseñas, tarjetas, campos personalizados). Configurable en Ajustes — admite modo por-acceso o por-sesión con tiempo de espera personalizable. Las mismas garantías de privacidad aplican: KeyArmor no almacena ni procesa datos biométricos.
    """.trimIndent()),
    LegalSection("Sincronización con Google Drive", """
La función de sincronización con Google Drive es completamente opcional. Cuando la activás, KeyArmor:

• Solicita un único permiso mínimo (DRIVE_APPDATA) para guardar tus propios respaldos cifrados y los puntos de restauración de Time Machine en una carpeta privada y exclusiva de la app dentro de tu Google Drive. Esa carpeta está oculta: ninguna otra app, ni vos navegando el Drive, puede verla ni modificarla.
• Cifra la bóveda con una clave de respaldo de 12 palabras antes de subirla. La clave se crea en tu dispositivo y solo la tenés vos: ni Google ni el desarrollador pueden leer el archivo ni recuperar la clave.
• Si guardás el PDF de tu clave de respaldo en Google Docs, Drive u otra app, lo hacés vos mismo desde la pantalla de compartir de Android; esa app se rige por su propia política de privacidad.
• Si cambiás tu clave, podés conservar las anteriores, cifradas en tu teléfono; el PDF del kit puede incluirlas. KeyArmor no las envía a ningún servidor.
• Nunca accede a ningún otro archivo de tu Drive, nunca crea carpetas visibles y nunca comparte archivos.
• Opcionalmente guarda una segunda copia local del respaldo en la carpeta Downloads/KeyArmor/ de tu dispositivo, que nunca sale del dispositivo.
• La sincronización bajo demanda está disponible en todos los planes (activada manualmente). La sincronización diaria automática está disponible en el plan Premium.

La Herencia Digital y el Plan Familiar usan Google Firebase, no tu Google Drive. Firebase coordina el consentimiento, las invitaciones y las notificaciones. La bóveda de herencia se cifra de extremo a extremo en tu dispositivo con la clave pública del heredero, y el bloque cifrado resultante se sube a un bucket privado de Firebase Cloud Storage del desarrollador mediante enlaces firmados de corta duración (15 minutos). El servidor guarda ese bloque pero nunca recibe la clave del heredero, así que no puede leerlo.

Podés desconectar tu cuenta de Google en cualquier momento desde la pantalla de sincronización.
    """.trimIndent()),
    LegalSection("Adjuntos", """
La función de adjuntos permite asociar archivos (hasta 5 por entrada, máximo 5 MB cada uno) a tus entradas de la bóveda. Todos los archivos adjuntos se cifran con AES-256-GCM antes de guardarse en el almacenamiento interno del dispositivo. Si activás la sincronización con Google Drive, tus adjuntos y las fotos de tus documentos se incluyen en el backup que subís, cifrado con tu clave de respaldo. Nunca se envían a ningún otro servicio.
    """.trimIndent()),
    LegalSection("Historial de contraseñas", """
KeyArmor guarda un historial local de contraseñas anteriores para cada entrada. Este historial se almacena cifrado en el dispositivo y nunca se transmite a ningún servidor externo.
    """.trimIndent()),
    LegalSection("Notificaciones internas", """
KeyArmor genera notificaciones internas para alertarte sobre eventos de seguridad importantes:

• Alertas de vencimiento próximo: avisamos cuando una o más contraseñas están por alcanzar su fecha de expiración.
• Contraseñas vencidas: avisamos cuando una o más contraseñas ya superaron su fecha de expiración.
• Alertas de brechas: avisamos cuando tus contraseñas son detectadas en filtraciones de datos conocidas.

Estos tres tipos de notificación se generan enteramente en tu dispositivo y nunca se transmiten a servidores externos. Si usás la Herencia Digital o el Plan Familiar, también vas a recibir notificaciones sobre esas funciones (un heredero que acepta, un miembro que se agrega a la familia, etc.); esas se originan en Google Firebase y se entregan mediante Firebase Cloud Messaging, ya que dependen de eventos iniciados por otra cuenta. Todas las notificaciones, sin importar su origen, se eliminan automáticamente a los 30 días. Podés eliminar manualmente cualquier notificación deslizándola hacia un lado, o borrar todas a la vez desde la pantalla de Notificaciones. Las notificaciones push requieren permiso del sistema operativo para enviarse.
    """.trimIndent()),
    LegalSection("Auditoría de seguridad", """
La función de auditoría analiza tus contraseñas localmente en el dispositivo. La verificación de contraseñas comunes se realiza comparando contra una lista predefinida almacenada en la propia aplicación, sin realizar ninguna consulta a servicios externos.
    """.trimIndent()),
    LegalSection("Monitoreo de brechas (HIBP)", """
KeyArmor puede verificar si tus contraseñas han aparecido en brechas de datos conocidas utilizando el servicio Have I Been Pwned (HIBP), operado por el investigador de seguridad Troy Hunt.

Esta verificación utiliza un modelo de k-anonimato: solo los primeros 5 caracteres de un hash SHA-1 de tu contraseña se envían a la API de HIBP. El hash completo nunca se transmite. La comparación se realiza localmente en tu dispositivo. No se envían nombres de usuario, direcciones de correo electrónico ni asociaciones de cuentas. Las respuestas de HIBP se rellenan con hashes señuelo para que incluso el análisis de tráfico de red no revele nada sobre la contraseña que se está verificando.
    """.trimIndent()),
    LegalSection("Servicio de Autocompletado", """
KeyArmor incluye un servicio de Autocompletado de Android opcional. Cuando se habilita en la configuración del sistema, lee el nombre del paquete de la app o la URL del sitio web de la pantalla de inicio de sesión activa para sugerir credenciales coincidentes de tu bóveda. Esta coincidencia se realiza íntegramente en el dispositivo. No se transmiten nombres de apps, URLs de sitios web ni credenciales a ningún servidor externo. El servicio de autocompletado no lee el contenido de otras apps más allá de lo estrictamente necesario para identificar el contexto de inicio de sesión.
    """.trimIndent()),
    LegalSection("Balance en Vivo de Billeteras Cripto", """
Cuando almacenás una entrada de billetera cripto y habilitás el monitoreo de balance en vivo, KeyArmor consulta nodos o APIs de blockchain públicas para obtener el balance asociado a la dirección de billetera que ingresaste. Solo se transmite la dirección de billetera — no se envían claves privadas, frases semilla ni datos personales. Estas consultas van directamente a infraestructura pública; KeyArmor no opera ningún servidor intermediario.
    """.trimIndent()),
    LegalSection("Compartición P2P de Entradas", """
KeyArmor te permite compartir entradas individuales de la bóveda directamente con otros usuarios de KeyArmor mediante código QR o texto cifrado. Esta transferencia es cifrada de extremo a extremo y peer-to-peer — no interviene ningún servidor ni intermediario. Sos el único responsable de elegir con quién compartís las entradas y de la seguridad del canal utilizado para transmitir el texto cifrado (si no usás QR directamente).
    """.trimIndent()),
    LegalSection("Librerías de terceros", """
KeyArmor utiliza librerías de código abierto (ver Aviso de código abierto). Ninguna de estas librerías incorpora mecanismos de telemetría, analítica o recopilación de datos en la configuración utilizada por esta aplicación.

KeyArmor también depende de dos servicios de terceros para funciones específicas: RevenueCat, que verifica el estado de tu suscripción Premium y solo recibe información relacionada con la compra (nunca tu bóveda, tu contraseña maestra ni tu clave de recuperación); y Brevo, que envía los correos transaccionales que requieren la Herencia Digital y el Plan Familiar (invitaciones, confirmaciones de consentimiento, notificaciones) y, para eso, recibe la dirección de correo de las personas involucradas en esas funciones. Ninguno de los dos servicios se usa para publicidad ni analítica de comportamiento.
    """.trimIndent()),    LegalSection("Widget de pantalla de inicio", """
KeyArmor incluye un widget opcional para la pantalla de inicio de Android que muestra el número total de entradas guardadas en tu bóveda. Este widget accede únicamente a datos locales del dispositivo. No transmite ninguna información a servidores externos. El contador de entradas es un dato agregado que no revela el contenido de ninguna entrada individual. Podés elegir si colocar o no el widget en tu pantalla de inicio.
    """.trimIndent()),

    LegalSection("Permisos", """
KeyArmor solicita únicamente los permisos necesarios para su funcionamiento:

• Biometría (opcional): para desbloqueo con huella digital.
• Internet (opcional): se usa solo para las funciones que activás vos mismo — sincronización con Google Drive, Herencia Digital y Plan Familiar (a través de Google Firebase), consultas de balance en vivo de billeteras cripto, verificación de brechas con Have I Been Pwned, y verificación de suscripciones (Google Play Billing / RevenueCat).
• Acceso a archivos (opcional): para importar/exportar la bóveda y para agregar adjuntos a tus entradas, únicamente cuando vos lo iniciás.
• Cámara (opcional): para escanear códigos QR al configurar autenticación de dos factores (2FA/TOTP) o al recibir entradas compartidas. Se activa únicamente a pedido explícito del usuario.
• Servicio de autocompletado (opcional): se habilita manualmente en la configuración del sistema Android. Se utiliza únicamente para sugerir credenciales en pantallas de inicio de sesión de otras apps.
• Micrófono (opcional): solo para el dictado por voz en Notas Seguras. El reconocimiento se hace en tu dispositivo; KeyArmor no graba, no guarda ni envía audio a ningún servidor.
• Calendario, solo lectura (opcional): lo usa el Modo Viaje Seguro para buscar viajes en tus eventos. Los eventos se procesan en tu dispositivo y nunca se transmiten.
• Notificaciones y alarmas exactas (opcional): para las alertas de seguridad y los recordatorios que programás. El evento de arranque del sistema se usa para reprogramarlos tras un reinicio.
• Estado de la red (opcional): solo para saber si estás con datos móviles o con Wi-Fi, y así hacer los backups pesados de Google Drive solo con Wi-Fi. No se recopila ni se envía ninguna información sobre tu red.

KeyArmor no solicita acceso a contactos, ubicación ni ningún otro dato personal no mencionado anteriormente.
    """.trimIndent()),
    LegalSection("Otros servicios que reciben datos", """
Además de los servicios descriptos arriba, KeyArmor usa los siguientes, solo para las funciones que los necesitan:

• Verificación de integridad de la app (Firebase App Check / Google Play Integrity): Google entrega una atestación de tu dispositivo para que nuestro servidor acepte solo solicitudes de la app auténtica.
• RevenueCat: además de la información de compra, recibe el identificador de tu cuenta de Google (no tu correo) como ID de usuario, para vincular tu suscripción entre dispositivos.
• CoinGecko: cuando usás los precios cripto en vivo, se le piden los tickers de tu portafolio, y puede ver tu dirección IP. Los exploradores y nodos de blockchain mencionados arriba también pueden verla.
• Google Fonts: la app descarga sus tipografías Inter y JetBrains Mono a través de los servicios de Google Play, por lo que Google puede ver tu dirección IP cuando se piden.
• Hora de confianza de los servicios de Google Play: se usa para detectar manipulaciones del reloj del dispositivo. No se envían datos personales.
• El reconocimiento de texto (OCR) de documentos se hace en tu dispositivo con ML Kit. No se envía ninguna imagen a ningún servidor.
• Ko-fi: solo si abrís el enlace de donación; en ese caso rige su propia política de privacidad.

Nuestros servidores pueden registrar temporalmente tu dirección IP para limitar abusos.
    """.trimIndent()),
    LegalSection("Passkeys (llaves de acceso)", """
KeyArmor puede funcionar como proveedor de passkeys de Android (Credential Manager), solo si lo elegís vos en la configuración del sistema. Al crear o usar una passkey, KeyArmor lee el dominio del sitio o de la app que la solicita para asociarla a ese sitio y para firmar el desafío que el sitio envía. La clave privada se genera en tu dispositivo y se guarda cifrada con el resto de tu bóveda; no se envía a ningún servidor ni a ningún tercero. Solo sale de tu dispositivo dentro de los backups que vos crees (por ejemplo, el backup cifrado en Google Drive). Para firmar con una passkey, KeyArmor te pide verificar tu identidad con tu huella o tu contraseña maestra. El sitio donde iniciás sesión recibe únicamente la clave pública y la firma, como con cualquier passkey.
    """.trimIndent()),
    LegalSection("Eliminación y conservación de datos", """
Tu bóveda se conserva en tu dispositivo (y en tu Google Drive, si activás el backup) hasta que la borres vos. En nuestros servidores solo hay datos de coordinación: el token de notificaciones push identificado por tu email y, si usás Herencia Digital o el Plan Familiar, los registros de invitaciones y consentimiento y el bloque cifrado de la bóveda de herencia. Se conservan mientras uses esas funciones o hasta que pidas eliminarlos. Podés eliminarlos desde Ajustes → Borrar todos mis datos, o escribiendo a support@thormakk.dev desde tu cuenta de Google; procesamos esos pedidos dentro de los 30 días. El detalle completo está en la página «Eliminar Cuenta y Datos» de nuestra web.
    """.trimIndent()),
    LegalSection("Menores de edad", """
KeyArmor no está dirigida a menores de 13 años (ni a la edad mínima que fije la ley de tu país para dar consentimiento digital), y no recopilamos a sabiendas datos de menores. Si creés que un menor nos entregó datos, escribinos a support@thormakk.dev y los eliminaremos.
    """.trimIndent()),
    LegalSection("Contacto", "Para cualquier consulta relacionada con esta política, podés escribir a support@thormakk.dev"),
    LegalSection("Última actualización", "Octubre de 2026 (rev. 8)")
)

private fun termsES() = listOf(
    LegalSection("Términos y Condiciones", ""),
    LegalSection("Aceptación", """
Al instalar o usar KeyArmor aceptás estos términos en su totalidad. Si no estás de acuerdo con alguna parte, no utilices la aplicación.
    """.trimIndent()),
    LegalSection("Uso permitido", """
KeyArmor está diseñado para uso personal como gestor de contraseñas y bóveda segura. Queda prohibido su uso para actividades ilegales, almacenar información que facilite actividades ilícitas, o intentar realizar ingeniería inversa de la aplicación.
    """.trimIndent()),
    LegalSection("Responsabilidad del usuario", """
Sos el único responsable de:

• La seguridad de tu contraseña maestra. Si la perdés, no existe mecanismo de recuperación y tus datos serán inaccesibles de forma permanente.
• La clave de respaldo de 12 palabras (si usás esa función). Si la perdés y además perdés el teléfono, no se pueden abrir tus respaldos de Drive, ni siquiera el desarrollador.
• Mantener respaldos actualizados de tu bóveda mediante la función de exportación o sincronización con Drive.
    """.trimIndent()),
    LegalSection("Funcionalidad de sincronización", """
La sincronización con Google Drive es una función auxiliar de respaldo. No reemplaza las buenas prácticas de seguridad personal. El desarrollador no garantiza la disponibilidad continua de esta función, ya que depende de servicios de terceros (Google).
    """.trimIndent()),
    LegalSection("Suscripciones Premium", """
KeyArmor ofrece planes de pago opcionales (Mensual y Anual) que desbloquean funciones adicionales. Las suscripciones se procesan exclusivamente a través de Google Play Billing. Al adquirir una suscripción también aceptás los Términos de Servicio de Google Play.

• Los planes Mensual y Anual son suscripciones de renovación automática. Podés cancelar en cualquier momento a través de Google Play; la cancelación tiene efecto al final del período de facturación vigente.
• Los reembolsos están sujetos a la política de reembolso estándar de Google Play. El desarrollador no procesa reembolsos de forma independiente.
• Las funciones premium pueden evolucionar con el tiempo. El desarrollador se reserva el derecho de agregar, modificar o discontinuar funciones premium específicas con un aviso razonable.
    """.trimIndent()),
    LegalSection("Modo Viaje Seguro", """
El Modo Viaje Seguro te permite marcar entradas específicas como seguras para viajar y ocultar todas las demás mientras el modo está activo. Sos el único responsable de determinar qué entradas marcar como seguras para viajar y de las consecuencias de activar o desactivar este modo. El desarrollador no asume responsabilidad por entradas accidentalmente dejadas visibles u ocultas.
    """.trimIndent()),
    LegalSection("PIN de Pánico", """
La función PIN de Pánico desencadena una acción irreversible (borrar u ocultar datos sensibles de la bóveda) cuando se ingresa un PIN designado. Una vez activado, los datos ocultos o eliminados pueden no ser recuperables según tu configuración. Asumís plena responsabilidad por configurar esta función y por cualquier pérdida de datos resultante de su activación intencional o accidental.
    """.trimIndent()),
    LegalSection("Información de Balance Cripto", """
La función de balance cripto en vivo obtiene información de balance de la infraestructura pública de blockchain únicamente con fines informativos. Esta información no constituye asesoramiento financiero. El desarrollador no garantiza la exactitud, completitud ni actualidad de los datos de balance. No tomés decisiones financieras basándote únicamente en la información mostrada en esta aplicación.
    """.trimIndent()),
    LegalSection("Sin garantías", """
La aplicación se provee "tal como es" (as is), sin garantías de ningún tipo, expresas o implícitas. El desarrollador no garantiza que la aplicación esté libre de errores, que funcione sin interrupciones, ni que sea apta para un propósito específico.
    """.trimIndent()),
    LegalSection("Limitación de responsabilidad", """
En la máxima medida permitida por la ley aplicable, el desarrollador no será responsable por pérdida de datos, pérdida de acceso a la bóveda, ni por daños directos, indirectos, incidentales, especiales o consecuentes derivados del uso o la imposibilidad de uso de la aplicación.
    """.trimIndent()),
    LegalSection("Actualizaciones de los términos", """
Estos términos pueden actualizarse en cualquier momento. El uso continuado de la aplicación tras una actualización publicada constituye aceptación de los nuevos términos.
    """.trimIndent()),
    LegalSection("Última actualización", "Mayo de 2026 (rev. 2)")
)

private fun legalES() = listOf(
    LegalSection("Aviso Legal y Descargo de Responsabilidad", ""),
    LegalSection("Software provisto tal como es", """
KeyArmor se distribuye "tal como es" (as is), sin garantías de ningún tipo, expresas ni implícitas, incluyendo pero no limitándose a garantías de comerciabilidad, aptitud para un propósito particular o ausencia de infracciones.
    """.trimIndent()),
    LegalSection("Limitación de responsabilidad", """
El desarrollador de KeyArmor no será responsable en ningún caso por:

• Pérdida o corrupción de datos almacenados en la aplicación
• Pérdida de acceso a la bóveda por olvido o pérdida de la contraseña maestra o de la clave de respaldo
• Vulnerabilidades o fallas en componentes del sistema operativo Android
• Fallas, interrupciones o cambios en los servicios de Google Drive
• Daños directos, indirectos, incidentales, especiales o consecuentes derivados del uso o imposibilidad de uso de la aplicación
• Cualquier daño derivado del acceso no autorizado a los datos del usuario por parte de terceros
• Pérdida de datos resultante de la activación intencional o accidental de la función PIN de Pánico
    """.trimIndent()),
    LegalSection("Transmisión de datos a Google Drive y Firebase", """
Cuando el usuario activa la sincronización con Google Drive (respaldos) o usa la Herencia Digital / el Plan Familiar (Google Firebase), los datos se transmiten ya cifrados a la infraestructura de Google bajo los Términos de Servicio de Google. En todos los casos los datos salen del dispositivo como un blob opaco cifrado. El desarrollador no tiene acceso ni control sobre la infraestructura de Google y no puede leer el contenido transmitido.
    """.trimIndent()),
    LegalSection("Android Keystore y dependencia de hardware", """
La seguridad del cifrado de KeyArmor depende en parte de la implementación del Android Keystore del dispositivo del usuario. Esta implementación varía según el fabricante, el chipset y la versión de Android. El desarrollador no controla ni puede garantizar la seguridad de las implementaciones de hardware de terceros. Se recomienda mantener el sistema operativo actualizado.
    """.trimIndent()),
    LegalSection("Limitaciones del Servicio de Autocompletado", """
El servicio de Autocompletado de KeyArmor puede no funcionar correctamente con todas las aplicaciones o navegadores. En particular, Google Chrome no admite el framework estándar de autocompletado de Android — esta es una limitación de Chrome que afecta por igual a todos los gestores de contraseñas de terceros y está fuera del control del desarrollador. El desarrollador no garantiza que el servicio de autocompletado funcione con ninguna aplicación específica.
    """.trimIndent()),
    LegalSection("Descargo sobre Balance Cripto", """
Los datos de balance en vivo se obtienen de nodos y APIs de blockchain públicos de terceros. El desarrollador no opera ni controla estos nodos. La información de balance se proporciona únicamente con fines informativos y puede estar desactualizada, ser inexacta o no estar disponible. Esta información no constituye asesoramiento financiero ni de inversión. El desarrollador no asume responsabilidad por decisiones tomadas en base a datos de balance mostrados en la app.
    """.trimIndent()),
    LegalSection("Compartición P2P", """
La función de compartición P2P de entradas facilita el intercambio directo y cifrado de entradas de la bóveda entre usuarios. El desarrollador no asume responsabilidad por el contenido de las entradas compartidas entre usuarios, por la identidad o confiabilidad del destinatario, ni por las consecuencias derivadas de compartir datos de la bóveda con terceros.
    """.trimIndent()),
    LegalSection("Librerías de terceros", """
KeyArmor utiliza librerías de código abierto de terceros consideradas seguras al momento de la publicación. El desarrollador no asume responsabilidad por fallas, vulnerabilidades o comportamientos inesperados originados en componentes de terceros.
    """.trimIndent()),
    LegalSection("Ley aplicable", """
Este aviso legal se rige por la legislación vigente en la República Argentina. Cualquier controversia derivada del uso de la aplicación será sometida a los tribunales competentes de dicha jurisdicción.
    """.trimIndent()),
    LegalSection("Última actualización", "Mayo de 2026 (rev. 2)")
)

private fun licenseES() = listOf(
    LegalSection("Licencia Source Available", ""),
    LegalSection("Código fuente publicado solo para fines de auditoría de seguridad", """
El código fuente de KeyArmor está disponible públicamente únicamente para que investigadores de seguridad y usuarios puedan inspeccionar y auditar sus prácticas de seguridad. No se otorga ningún derecho más allá de eso — todos los derechos están reservados por el titular del copyright.
    """.trimIndent()),
    LegalSection("Qué podés hacer", """
• Leer e inspeccionar el código fuente con fines de investigación de seguridad y auditoría personal
• Reportar vulnerabilidades de seguridad al equipo de KeyArmor por los canales oficiales
    """.trimIndent()),
    LegalSection("Qué no podés hacer", """
Sin permiso previo y explícito por escrito del titular del copyright, no podés:

• Usar, ejecutar o correr el software fuera de su distribución oficial
• Copiar, reproducir o extraer el código fuente con cualquier fin que no sea la lectura
• Modificarlo, adaptarlo, traducirlo o crear trabajos derivados a partir de él
• Distribuirlo, publicarlo, sublicenciarlo, venderlo o ponerlo a disposición de terceros de cualquier otra forma
• Usar el código fuente como base para otro producto, servicio o proyecto
• Usar el nombre, logo o cualquier marca asociada de KeyArmor
    """.trimIndent()),
    LegalSection("Código fuente", """
El código fuente de KeyArmor está disponible públicamente.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("Sin garantía", """
El código fuente se proporciona "tal cual", sin garantía de ningún tipo, expresa o implícita. El titular del copyright no hace declaraciones respecto a su idoneidad para ningún propósito particular.
    """.trimIndent()),
    LegalSection("Copyright", "© 2026 KeyArmor. Todos los derechos reservados. Source Available.")
)

private fun opensourceES() = listOf(
    LegalSection("Aviso de Código Abierto", ""),
    LegalSection("Librerías utilizadas", """
KeyArmor utiliza las siguientes librerías de código abierto. Salvo indicación contraria, están distribuidas bajo la licencia Apache 2.0.
    """.trimIndent()),
    LegalSection("Jetpack Compose", """
Versión: BOM 2026.09.00 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose
    """.trimIndent()),
    LegalSection("Jetpack Glance (AppWidget)", """
Versión: 1.2.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/glance
    """.trimIndent()),
    LegalSection("Room Database", """
Versión: 2.8.5 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/data-storage/room
    """.trimIndent()),
    LegalSection("Hilt (Dagger)", """
Versión: 2.60.1 | Licencia: Apache 2.0
Copyright © The Dagger Authors
https://dagger.dev/hilt/
    """.trimIndent()),
    LegalSection("Kotlin Coroutines", """
Versión: 1.11.0 | Licencia: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/Kotlin/kotlinx.coroutines
    """.trimIndent()),
    LegalSection("AndroidX Biometric", """
Versión: 1.1.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/biometric
    """.trimIndent()),
    LegalSection("AndroidX Navigation", """
Versión: 2.10.2 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/guide/navigation
    """.trimIndent()),
    LegalSection("AndroidX Lifecycle / ViewModel", """
Versión: 2.11.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/lifecycle
    """.trimIndent()),
    LegalSection("AndroidX WorkManager", """
Versión: 2.12.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/topic/libraries/architecture/workmanager
    """.trimIndent()),
    LegalSection("Google Play Services Auth", """
Versión: 22.0.0 | Licencia: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/guides/setup
    """.trimIndent()),
    LegalSection("Google API Client Android", """
Versión: 2.9.2 | Licencia: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-api-java-client
    """.trimIndent()),
    LegalSection("Google Drive API v3", """
Versión: v3-rev20240123-2.0.0 | Licencia: Apache 2.0
Copyright © Google LLC
https://developers.google.com/drive/api/v3/about-sdk
    """.trimIndent()),
    LegalSection("Google HTTP Client GSON", """
Versión: 2.2.0 | Licencia: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-http-java-client
    """.trimIndent()),
    LegalSection("ZXing Android Embedded", """
Versión: 4.3.0 | Licencia: Apache 2.0
Copyright © ZXing authors, journeyapps
https://github.com/journeyapps/zxing-android-embedded
    """.trimIndent()),
    LegalSection("Accompanist Permissions", """
Versión: 0.37.3 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://google.github.io/accompanist/permissions/
    """.trimIndent()),
    LegalSection("Compose Material Icons Extended", """
Versión: BOM 2026.09.00 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose/graphics/images/material
    """.trimIndent()),
    LegalSection("AndroidX Core KTX", """
Versión: 1.19.1 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Activity Compose", """
Versión: 1.13.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/activity
    """.trimIndent()),
    LegalSection("AndroidX Autofill", """
Versión: 1.3.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/autofill
    """.trimIndent()),
    LegalSection("AndroidX DataStore", """
Versión: 1.3.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/datastore
    """.trimIndent()),
    LegalSection("AndroidX AppCompat", """
Versión: 1.8.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/appcompat
    """.trimIndent()),
    LegalSection("AndroidX Hilt (Navigation, Work)", """
Versión: 1.4.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/hilt
    """.trimIndent()),
    LegalSection("Kotlin Standard Library", """
Versión: 2.4.21 | Licencia: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/JetBrains/kotlin
    """.trimIndent()),
    LegalSection("AndroidX Core Splashscreen", """
Versión: 1.2.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Profileinstaller", """
Versión: 1.4.1 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/profileinstaller
    """.trimIndent()),
    LegalSection("AndroidX Credential Manager", """
Versión: 1.6.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/credentials
    """.trimIndent()),
    LegalSection("CameraX", """
Versión: 1.6.2 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/camerax
    """.trimIndent()),
    LegalSection("Firebase Android SDK (Messaging, Auth, App Check)", """
Versión: BOM 35.1.0 | Licencia: Apache 2.0
Copyright © Google LLC
https://github.com/firebase/firebase-android-sdk
    """.trimIndent()),
    LegalSection("Joda-Time", """
Versión: 2.15.0 | Licencia: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-time/
    """.trimIndent()),
    LegalSection("Joda-Convert", """
Versión: 3.0.1 | Licencia: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-convert/
    """.trimIndent()),
    LegalSection("PDFBox-Android", """
Versión: 2.0.27.0 | Licencia: Apache 2.0
Copyright © Tom Roush
https://github.com/TomRoush/PdfBox-Android
    """.trimIndent()),
    LegalSection("RichEditor Compose", """
Versión: 1.2.1 | Licencia: MIT
Copyright © Mohamed Rejeb
https://github.com/MohamedRejeb/compose-rich-editor
    """.trimIndent()),
    LegalSection("Bouncy Castle Provider", """
Versión: 1.86 | Licencia: Bouncy Castle License (MIT-style)
Copyright © The Legion of the Bouncy Castle Inc.
https://www.bouncycastle.org/about/license/
    """.trimIndent()),
    LegalSection("RevenueCat Purchases Android SDK", """
Versión: 10.29.0 | Licencia: MIT
Copyright © RevenueCat, Inc.
https://github.com/RevenueCat/purchases-android
    """.trimIndent()),
    LegalSection("LazySodium for Android", """
Versión: 5.2.0 | Licencia: MPL-2.0
Copyright © Terl Tech Ltd
https://github.com/terl/lazysodium-android
    """.trimIndent()),
    LegalSection("JNA (Java Native Access)", """
Versión: 5.19.1 | Licencia: Apache 2.0 / LGPL 2.1 (dual-licensed)
Copyright © The JNA Project Contributors
https://github.com/java-native-access/jna
    """.trimIndent()),
    LegalSection("Google Identity Services (GoogleId)", """
Versión: 1.2.1 | Licencia: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/identity/android-credential-manager
    """.trimIndent()),
    LegalSection("Play Services Time", """
Versión: 16.0.1 | Licencia: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
    """.trimIndent()),
    LegalSection("ML Kit Text Recognition", """
Versión: 16.0.1 | Licencia: ML Kit Terms
Copyright © Google LLC
https://developers.google.com/ml-kit/terms
    """.trimIndent()),
    LegalSection("Public Suffix List", """
Versión: 2026-07-09 | Licencia: MPL-2.0
Copyright © Mozilla Foundation and contributors
https://publicsuffix.org
    """.trimIndent()),
    LegalSection("Texto completo Apache 2.0", """
El texto completo de la licencia Apache 2.0 está disponible en:
https://www.apache.org/licenses/LICENSE-2.0
MIT: https://opensource.org/license/mit
MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
    """.trimIndent())
)

private fun deleteDataES() = listOf(
    LegalSection("Eliminar cuenta y datos", ""),
    LegalSection("Qué guarda KeyArmor sobre ti", """
Tu bóveda (contraseñas, notas, tarjetas, billeteras, adjuntos e historial de contraseñas) nunca se almacena en nuestros servidores. Vive cifrada en tu dispositivo y, si activas el respaldo, en tu propio Google Drive — no en un lugar donde nosotros podamos leerla o borrarla, porque está cifrada antes de salir de tu dispositivo.

El único dato asociado a tu cuenta de Google que llega a nuestros servidores (Firebase) está ligado a conectar esa cuenta para una función opcional — respaldo en Google Drive, Herencia Digital o Plan Familiar. Simplemente conectar una cuenta de Google para cualquiera de ellas registra ahí un dato mínimo (un token de notificaciones push, identificado por tu email), incluso antes de configurar Herencia o Plan Familiar. Si nunca conectaste una cuenta de Google a KeyArmor, no hay nada sobre ti en nuestros servidores para eliminar.
    """.trimIndent()),
    LegalSection("Eliminación desde la app", """
Puedes eliminar todos tus datos en cualquier momento desde Ajustes → Borrar todos mis datos dentro de la app. Si nunca conectaste una cuenta de Google a KeyArmor (para respaldo en Drive, Herencia Digital o Plan Familiar), esto borra de inmediato y de forma permanente tu bóveda del dispositivo.

Si conectaste una, la app te pide iniciar sesión con Google para que, en la misma acción, pueda borrar los registros asociados del servidor (Firebase) y el respaldo y las copias de Máquina del tiempo que KeyArmor guardó en tu Google Drive. Solo si no puedes iniciar sesión, la app recurre a borrar únicamente los datos locales — y te avisa de antemano que tus registros del servidor quedarán hasta que los elimines por separado, usando la solicitud de abajo.
    """.trimIndent()),
    LegalSection("Qué no se borra", """
Borrar tus datos no elimina lo que vive fuera de la app:

• Los archivos que exportaste tú (CSV, PDF o archivos .kab que guardaste o compartiste). Son tuyos y están donde los dejaste, así que debes borrarlos allí.
• Las copias de respaldo que una instalación anterior de KeyArmor dejó en tu carpeta Descargas. Android ya no permite que una instalación nueva las toque; bórralas desde tu carpeta Descargas.
• Tu suscripción. La administra Google Play, así que borrar tus datos no la cancela; cancélala en Google Play → Suscripciones.
    """.trimIndent()),
    LegalSection("Solicitar la eliminación de los datos de tu cuenta", """
Si conectaste una cuenta de Google a KeyArmor pero no puedes iniciar sesión para usar la eliminación desde la app (por ejemplo, porque ya no tienes la app instalada), solicita la eliminación de los datos asociados del servidor escribiéndonos desde esa misma cuenta de Google.

Escribe a: support@thormakk.dev

Procesamos las solicitudes de eliminación dentro de los 30 días y confirmamos por email cuando esté completado. No necesitas tener la app instalada para hacer esta solicitud.
    """.trimIndent())
)
