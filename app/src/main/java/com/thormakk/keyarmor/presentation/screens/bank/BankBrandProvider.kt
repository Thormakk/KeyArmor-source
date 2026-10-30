package com.thormakk.keyarmor.presentation.screens.bank

object BankBrandProvider {

    data class BankBrandInfo(
        val brandColorArgb: Long
    )

    data class BankBrand(
        val key:  String,
        val name: String,
        val info: BankBrandInfo
    )

    val brands: List<BankBrand> = listOf(
        BankBrand("HSBC",          "HSBC",                  BankBrandInfo(0xFFDB0011L)),
        BankBrand("SANTANDER",     "Santander",             BankBrandInfo(0xFFEC0000L)),
        BankBrand("BBVA",          "BBVA",                  BankBrandInfo(0xFF072146L)),
        BankBrand("CITIBANK",      "Citibank",              BankBrandInfo(0xFF003B70L)),
        BankBrand("CHASE",         "Chase",                 BankBrandInfo(0xFF117ACAL)),
        BankBrand("BANKOFAMERICA", "Bank of America",       BankBrandInfo(0xFFE31837L)),
        BankBrand("WELLSFARGO",    "Wells Fargo",           BankBrandInfo(0xFFD71E28L)),
        BankBrand("BARCLAYS",      "Barclays",              BankBrandInfo(0xFF00AEEFL)),
        BankBrand("BNPPARIBAS",    "BNP Paribas",           BankBrandInfo(0xFF00965EL)),
        BankBrand("ING",           "ING",                   BankBrandInfo(0xFFFF6200L)),
        BankBrand("DEUTSCHE",      "Deutsche Bank",         BankBrandInfo(0xFF0018A8L)),
        BankBrand("UNICREDIT",     "UniCredit",             BankBrandInfo(0xFFE31B23L)),
        BankBrand("INTESA",        "Intesa Sanpaolo",       BankBrandInfo(0xFF006236L)),
        BankBrand("SOCIETE",       "Société Générale",      BankBrandInfo(0xFFE60028L)),
        BankBrand("ABNAMRO",       "ABN AMRO",              BankBrandInfo(0xFF009286L)),
        BankBrand("RABOBANK",      "Rabobank",              BankBrandInfo(0xFFFF6200L)),
        BankBrand("COMMERZBANK",   "Commerzbank",           BankBrandInfo(0xFFFFCC00L)),
        BankBrand("CAIXABANK",     "CaixaBank",             BankBrandInfo(0xFF00579BL)),
        BankBrand("CREDITAGRICOLE","Crédit Agricole",       BankBrandInfo(0xFF008E4FL)),
        BankBrand("LLOYDS",        "Lloyds Bank",           BankBrandInfo(0xFF006A4EL)),
        BankBrand("NATWEST",       "NatWest",               BankBrandInfo(0xFF5A1E64L)),
        BankBrand("HALIFAX",       "Halifax",               BankBrandInfo(0xFF005EB8L)),
        BankBrand("STANDARDCHARTERED","Standard Chartered", BankBrandInfo(0xFF0D9BDFL)),
        BankBrand("DBS",           "DBS Bank",              BankBrandInfo(0xFFE3001BL)),
        BankBrand("OCBC",          "OCBC Bank",             BankBrandInfo(0xFFED1C24L)),
        BankBrand("ANZ",           "ANZ",                   BankBrandInfo(0xFF007B5FL)),
        BankBrand("COMMONWEALTH",  "Commonwealth Bank",     BankBrandInfo(0xFFFFBF00L)),
        BankBrand("WESTPAC",       "Westpac",               BankBrandInfo(0xFFD5002BL)),
        BankBrand("NAB",           "NAB",                   BankBrandInfo(0xFFE4002BL)),
        BankBrand("NUBANK",        "Nubank",                BankBrandInfo(0xFF8A05BEL)),
        BankBrand("N26",           "N26",                   BankBrandInfo(0xFF26B6FFL)),
        BankBrand("MONZO",         "Monzo",                 BankBrandInfo(0xFFFF4F40L)),
        BankBrand("REVOLUT",       "Revolut",               BankBrandInfo(0xFF191C1FL)),
        BankBrand("WISE",          "Wise",                  BankBrandInfo(0xFF9FE870L)),
        BankBrand("STARLING",      "Starling Bank",         BankBrandInfo(0xFF6935D3L)),
        BankBrand("CHIME",         "Chime",                 BankBrandInfo(0xFF1EC677L)),
        BankBrand("SOFI",          "SoFi",                  BankBrandInfo(0xFF000000L)),
        BankBrand("DAVE",          "Dave",                  BankBrandInfo(0xFF3B82F6L)),
        BankBrand("GALICIA",       "Banco Galicia",         BankBrandInfo(0xFFE30613L)),
        BankBrand("NACION",        "Banco Nación",          BankBrandInfo(0xFF003DA6L)),
        BankBrand("PROVINCIA",     "Banco Provincia",       BankBrandInfo(0xFF0057A8L)),
        BankBrand("MACRO",         "Banco Macro",           BankBrandInfo(0xFFFFCC00L)),
        BankBrand("ICBC",          "ICBC",                  BankBrandInfo(0xFFC8102EL)),
        BankBrand("PATAGONIA",     "Banco Patagonia",       BankBrandInfo(0xFF00529BL)),
        BankBrand("SUPERVIELLE",   "Banco Supervielle",     BankBrandInfo(0xFF003DA6L)),
        BankBrand("ITAU",          "Itaú",                  BankBrandInfo(0xFFEC7000L)),
        BankBrand("BRADESCO",      "Bradesco",              BankBrandInfo(0xFFCC092FL)),
        BankBrand("BANCODOBRASIL", "Banco do Brasil",       BankBrandInfo(0xFFFFCC29L)),
        BankBrand("CAIXA",         "Caixa Econômica",       BankBrandInfo(0xFF005CA9L)),
        BankBrand("BANAMEX",       "Banamex",               BankBrandInfo(0xFF003DA6L)),
        BankBrand("BANCOMER",      "BBVA Bancomer",         BankBrandInfo(0xFF072146L)),
        BankBrand("BANORTE",       "Banorte",               BankBrandInfo(0xFFB22222L)),
        BankBrand("BANCOLOMBIA",   "Bancolombia",           BankBrandInfo(0xFFFFBD00L)),
        BankBrand("DAVIVIENDA",    "Davivienda",            BankBrandInfo(0xFFE8002DL)),
        BankBrand("BCP",           "BCP",                   BankBrandInfo(0xFF003DA6L)),
        BankBrand("INTERBANK",     "Interbank",             BankBrandInfo(0xFF007CC2L)),
        BankBrand("SCOTIABANK",    "Scotiabank",            BankBrandInfo(0xFFEC111AL)),
        BankBrand("ICBCCHINA",     "ICBC China",            BankBrandInfo(0xFFC8102EL)),
        BankBrand("BANKOFCHINA",   "Bank of China",         BankBrandInfo(0xFFC8102EL)),
        BankBrand("CONSTRUCAO",    "CCB",                   BankBrandInfo(0xFFC8102EL)),
        BankBrand("AGRICULTURALBANKOFCHINA","Agricultural Bank of China", BankBrandInfo(0xFF006633L)),
        BankBrand("RAKUTEN",       "Rakuten Bank",          BankBrandInfo(0xFFBF0000L)),
        BankBrand("SMBC",          "SMBC",                  BankBrandInfo(0xFF003DA6L)),
        BankBrand("MUFG",          "MUFG",                  BankBrandInfo(0xFF8B0000L)),
        BankBrand("AXIS",          "Axis Bank",             BankBrandInfo(0xFF97144DL)),
        BankBrand("HDFC",          "HDFC Bank",             BankBrandInfo(0xFF004C97L)),
        BankBrand("ICICIBANK",     "ICICI Bank",            BankBrandInfo(0xFFFA6C0AL)),
        BankBrand("SBI",           "SBI",                   BankBrandInfo(0xFF22409AL)),
    )

    private val map: Map<String, BankBrandInfo> = buildMap {
        brands.forEach { put(it.key, it.info) }

        put("BANK OF AMERICA",       BankBrandInfo(0xFFE31837L))
        put("WELLS FARGO",           BankBrandInfo(0xFFD71E28L))
        put("BNP",                   BankBrandInfo(0xFF00965EL))
        put("BNP PARIBAS",           BankBrandInfo(0xFF00965EL))
        put("ABN AMRO",              BankBrandInfo(0xFF009286L))
        put("CAISSE",                BankBrandInfo(0xFF008E4FL))
        put("CREDIT AGRICOLE",       BankBrandInfo(0xFF008E4FL))
        put("SOCIETE GENERALE",      BankBrandInfo(0xFFE60028L))
        put("SOCIÉTÉ GÉNÉRALE",      BankBrandInfo(0xFFE60028L))
        put("STANDARD CHARTERED",    BankBrandInfo(0xFF0D9BDFL))
        put("COMMONWEALTH BANK",     BankBrandInfo(0xFFFFBF00L))
        put("STARLING",              BankBrandInfo(0xFF6935D3L))
        put("DEUTSCHE BANK",         BankBrandInfo(0xFF0018A8L))
        put("UNICREDIT",             BankBrandInfo(0xFFE31B23L))
        put("INTESA SANPAOLO",       BankBrandInfo(0xFF006236L))
        put("CAIXA BANK",            BankBrandInfo(0xFF00579BL))
        put("CREDIT AGRICOLE",       BankBrandInfo(0xFF008E4FL))
        put("LLOYDS",                BankBrandInfo(0xFF006A4EL))
        put("BANCO GALICIA",         BankBrandInfo(0xFFE30613L))
        put("BANCO NACION",          BankBrandInfo(0xFF003DA6L))
        put("BANCO NACIÓN",          BankBrandInfo(0xFF003DA6L))
        put("BANCO PROVINCIA",       BankBrandInfo(0xFF0057A8L))
        put("BANCO MACRO",           BankBrandInfo(0xFFFFCC00L))
        put("BANCO PATAGONIA",       BankBrandInfo(0xFF00529BL))
        put("BANCO SUPERVIELLE",     BankBrandInfo(0xFF003DA6L))
        put("ITAÚ",                  BankBrandInfo(0xFFEC7000L))
        put("BANCO DO BRASIL",       BankBrandInfo(0xFFFFCC29L))
        put("CAIXA ECONOMICA",       BankBrandInfo(0xFF005CA9L))
        put("CAIXA ECONÔMICA",       BankBrandInfo(0xFF005CA9L))
        put("BBVA BANCOMER",         BankBrandInfo(0xFF072146L))
        put("BANK OF CHINA",         BankBrandInfo(0xFFC8102EL))
        put("AGRICULTURAL BANK",     BankBrandInfo(0xFF006633L))
        put("ICICI",                 BankBrandInfo(0xFFFA6C0AL))
        put("AXIS BANK",             BankBrandInfo(0xFF97144DL))
        put("HDFC",                  BankBrandInfo(0xFF004C97L))
        put("STATE BANK",            BankBrandInfo(0xFF22409AL))
        put("STATE BANK OF INDIA",   BankBrandInfo(0xFF22409AL))
        put("PAYPAL",                BankBrandInfo(0xFF002991L))
        put("STRIPE",                BankBrandInfo(0xFF635BFFL))
        put("MERCADO PAGO",          BankBrandInfo(0xFF00B1EAL))
        put("MERCADOPAGO",           BankBrandInfo(0xFF00B1EAL))
    }

    fun resolve(bankName: String): BankBrandInfo? {
        if (bankName.isBlank()) return null
        val upper = bankName.trim().uppercase()
        map[upper]?.let { return it }
        val firstToken = upper.split(" ", "-", "/").firstOrNull() ?: return null
        return map[firstToken]
    }

}
