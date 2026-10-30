package com.thormakk.keyarmor.presentation.screens.crypto

import com.thormakk.keyarmor.R

object CryptoIconProvider {

    data class CryptoIconInfo(
        val brandColorArgb: Long,
        val symbol: String
    )

    data class CryptoCoin(
        val ticker: String,
        val name:   String,
        val info:   CryptoIconInfo
    )

    val coins: List<CryptoCoin> = listOf(
        CryptoCoin("BTC",   "Bitcoin",              CryptoIconInfo(0xFFF7931AL, "₿")),
        CryptoCoin("ETH",   "Ethereum",             CryptoIconInfo(0xFF627EEAL, "Ξ")),
        CryptoCoin("USDT",  "Tether",               CryptoIconInfo(0xFF26A17BL, "USDT")),
        CryptoCoin("BNB",   "BNB",                  CryptoIconInfo(0xFFF3BA2FL, "BNB")),
        CryptoCoin("SOL",   "Solana",               CryptoIconInfo(0xFF9945FFL, "◎")),
        CryptoCoin("USDC",  "USD Coin",             CryptoIconInfo(0xFF2775CAL, "USDC")),
        CryptoCoin("XRP",   "XRP",                  CryptoIconInfo(0xFF00AAE4L, "XRP")),
        CryptoCoin("DOGE",  "Dogecoin",             CryptoIconInfo(0xFFC2A633L, "Ð")),
        CryptoCoin("ADA",   "Cardano",              CryptoIconInfo(0xFF0033ADL, "ADA")),
        CryptoCoin("AVAX",  "Avalanche",            CryptoIconInfo(0xFFE84142L, "AVAX")),
        CryptoCoin("SHIB",  "Shiba Inu",            CryptoIconInfo(0xFFFF5700L, "Ð")),
        CryptoCoin("DOT",   "Polkadot",             CryptoIconInfo(0xFFE6007AL, "DOT")),
        CryptoCoin("MATIC", "Polygon",              CryptoIconInfo(0xFF8247E5L, "MATIC")),
        CryptoCoin("POL",   "Polygon (POL)",        CryptoIconInfo(0xFF8247E5L, "POL")),
        CryptoCoin("LTC",   "Litecoin",             CryptoIconInfo(0xFF9ABBC7L, "Ł")),
        CryptoCoin("LINK",  "Chainlink",            CryptoIconInfo(0xFF2A5ADAL, "LINK")),
        CryptoCoin("BCH",   "Bitcoin Cash",         CryptoIconInfo(0xFF0AC18EL, "₿")),
        CryptoCoin("UNI",   "Uniswap",              CryptoIconInfo(0xFFFF007AL, "UNI")),
        CryptoCoin("ATOM",  "Cosmos",               CryptoIconInfo(0xFF2E3148L, "ATOM")),
        CryptoCoin("XLM",   "Stellar",              CryptoIconInfo(0xFF3E4150L, "XLM")),
        CryptoCoin("TRX",   "TRON",                 CryptoIconInfo(0xFFEF0027L, "TRX")),
        CryptoCoin("NEAR",  "NEAR Protocol",        CryptoIconInfo(0xFF3F4040L, "NEAR")),
        CryptoCoin("FTM",   "Fantom",               CryptoIconInfo(0xFF13B5ECL, "FTM")),
        CryptoCoin("ALGO",  "Algorand",             CryptoIconInfo(0xFF3A3A3AL, "ALGO")),
        CryptoCoin("ICP",   "Internet Computer",    CryptoIconInfo(0xFF3B00B9L, "ICP")),
        CryptoCoin("FIL",   "Filecoin",             CryptoIconInfo(0xFF0090FFL, "FIL")),
        CryptoCoin("GRT",   "The Graph",            CryptoIconInfo(0xFF6747EDL, "GRT")),
        CryptoCoin("AAVE",  "Aave",                 CryptoIconInfo(0xFFB6509EL, "AAVE")),
        CryptoCoin("ARB",   "Arbitrum",             CryptoIconInfo(0xFF28A0F0L, "ARB")),
        CryptoCoin("OP",    "Optimism",             CryptoIconInfo(0xFFFF0420L, "OP")),
        CryptoCoin("DAI",   "DAI",                  CryptoIconInfo(0xFFF5AC37L, "DAI")),
        CryptoCoin("MKR",   "Maker",                CryptoIconInfo(0xFF1AAB9BL, "MKR")),
        CryptoCoin("COMP",  "Compound",             CryptoIconInfo(0xFF00D395L, "COMP")),
        CryptoCoin("CRV",   "Curve",                CryptoIconInfo(0xFF3A3A3AL, "CRV")),
        CryptoCoin("SNX",   "Synthetix",            CryptoIconInfo(0xFF5FCDF9L, "SNX")),
        CryptoCoin("SUSHI", "SushiSwap",            CryptoIconInfo(0xFF0E0F23L, "SUSHI")),
        CryptoCoin("YFI",   "Yearn Finance",        CryptoIconInfo(0xFF006AE3L, "YFI")),
        CryptoCoin("1INCH", "1inch",                CryptoIconInfo(0xFF1B314BL, "1INCH")),
        CryptoCoin("LDO",   "Lido DAO",             CryptoIconInfo(0xFFF0A500L, "LDO")),
        CryptoCoin("SAND",  "The Sandbox",          CryptoIconInfo(0xFF00ADEFL, "SAND")),
        CryptoCoin("MANA",  "Decentraland",         CryptoIconInfo(0xFFFF2D55L, "MANA")),
        CryptoCoin("AXS",   "Axie Infinity",        CryptoIconInfo(0xFF0055D5L, "AXS")),
        CryptoCoin("ENJ",   "Enjin",                CryptoIconInfo(0xFF7866D5L, "ENJ")),
        CryptoCoin("GALA",  "Gala",                 CryptoIconInfo(0xFF1A1A1AL, "GALA")),
        CryptoCoin("IMX",   "Immutable",            CryptoIconInfo(0xFF17B5CBL, "IMX")),
        CryptoCoin("APE",   "ApeCoin",              CryptoIconInfo(0xFF063A5EL, "APE")),
        CryptoCoin("CHZ",   "Chiliz",               CryptoIconInfo(0xFFCD0124L, "CHZ")),
        CryptoCoin("VET",   "VeChain",              CryptoIconInfo(0xFF15BDEFL, "VET")),
        CryptoCoin("EOS",   "EOS",                  CryptoIconInfo(0xFF443F54L, "EOS")),
        CryptoCoin("XTZ",   "Tezos",                CryptoIconInfo(0xFF2C7DF7L, "XTZ")),
        CryptoCoin("XMR",   "Monero",               CryptoIconInfo(0xFFFF6600L, "XMR")),
        CryptoCoin("ZEC",   "Zcash",                CryptoIconInfo(0xFFECB244L, "ZEC")),
        CryptoCoin("THETA", "Theta Network",        CryptoIconInfo(0xFF2AB8E6L, "THETA")),
        CryptoCoin("FLOW",  "Flow",                 CryptoIconInfo(0xFF00EF8BL, "FLOW")),
        CryptoCoin("KSM",   "Kusama",               CryptoIconInfo(0xFFE6007AL, "KSM")),
        CryptoCoin("EGLD",  "MultiversX",           CryptoIconInfo(0xFF23F7DDL, "EGLD")),
        CryptoCoin("HBAR",  "Hedera",               CryptoIconInfo(0xFF222222L, "HBAR")),
        CryptoCoin("CAKE",  "PancakeSwap",          CryptoIconInfo(0xFFFAD44DL, "CAKE")),
        CryptoCoin("WBTC",  "Wrapped Bitcoin",      CryptoIconInfo(0xFFF7931AL, "₿")),
        CryptoCoin("STETH", "stETH (Lido)",         CryptoIconInfo(0xFF00A3FFL, "Ξ")),
        CryptoCoin("BSV",   "Bitcoin SV",           CryptoIconInfo(0xFFEAB300L, "₿")),
        CryptoCoin("BTT",   "BitTorrent",           CryptoIconInfo(0xFFEF0027L, "BTT")),
        CryptoCoin("BUSD",  "Binance USD",          CryptoIconInfo(0xFFF3BA2FL, "BUSD")),
        CryptoCoin("FRAX",  "Frax",                 CryptoIconInfo(0xFF3C3C3CL, "FRAX")),
        CryptoCoin("TUSD",  "TrueUSD",              CryptoIconInfo(0xFF002868L, "TUSD")),
        CryptoCoin("USDD",  "USDD",                 CryptoIconInfo(0xFFEF0027L, "USDD")),
        CryptoCoin("BAL",   "Balancer",             CryptoIconInfo(0xFF1E1E1EL, "BAL")),
        CryptoCoin("TON",   "Toncoin",              CryptoIconInfo(0xFF0098EAL, "TON")),
        CryptoCoin("SUI",   "Sui",                  CryptoIconInfo(0xFF4DA2FFL, "SUI")),
        CryptoCoin("APT",   "Aptos",                CryptoIconInfo(0xFF2FCCAAL, "APT")),
        CryptoCoin("PEPE",  "Pepe",                 CryptoIconInfo(0xFF00B84CL, "PEPE")),
        CryptoCoin("RNDR",  "Render",               CryptoIconInfo(0xFFE84142L, "RNDR")),
        CryptoCoin("INJ",   "Injective",            CryptoIconInfo(0xFF00BC8EL, "INJ")),
        CryptoCoin("WIF",   "dogwifhat",            CryptoIconInfo(0xFFC9A227L, "WIF")),
        CryptoCoin("BONK",  "Bonk",                 CryptoIconInfo(0xFFFF6B00L, "BONK")),
        CryptoCoin("JUP",   "Jupiter",              CryptoIconInfo(0xFF1AC9A4L, "JUP")),
        CryptoCoin("TIA",   "Celestia",             CryptoIconInfo(0xFF7B2FBEL, "TIA")),
        CryptoCoin("RUNE",  "THORChain",            CryptoIconInfo(0xFF33FF99L, "RUNE")),
        CryptoCoin("FET",   "Fetch.ai",             CryptoIconInfo(0xFF1B1E2BL, "FET")),
        CryptoCoin("STX",   "Stacks",               CryptoIconInfo(0xFF5546FFL, "STX")),
        CryptoCoin("DASH",  "Dash",                 CryptoIconInfo(0xFF008CE7L, "DASH")),
        CryptoCoin("KAVA",  "Kava",                 CryptoIconInfo(0xFFFF564FL, "KAVA")),
        CryptoCoin("ROSE",  "Oasis Network",        CryptoIconInfo(0xFF40DCBDL, "ROSE")),
        CryptoCoin("ZRX",   "0x Protocol",          CryptoIconInfo(0xFF302C2CL, "ZRX")),
        CryptoCoin("BAT",   "Basic Attn Token",     CryptoIconInfo(0xFFFF5000L, "BAT")),
        CryptoCoin("ONE",   "Harmony",              CryptoIconInfo(0xFF00AEE9L, "ONE")),
        CryptoCoin("ONDO",  "Ondo Finance",         CryptoIconInfo(0xFF1A1B1FL, "ONDO")),
        CryptoCoin("HNT",   "Helium",               CryptoIconInfo(0xFF474DFFL, "HNT")),
        CryptoCoin("MINA",  "Mina Protocol",        CryptoIconInfo(0xFFE39844L, "MINA")),
        CryptoCoin("PYTH",  "Pyth Network",         CryptoIconInfo(0xFF6610F2L, "PYTH")),
        CryptoCoin("PAXG",  "PAX Gold",             CryptoIconInfo(0xFFF4C430L, "PAXG")),
    )

    fun suggestions(query: String, limit: Int = 6): List<CryptoCoin> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return coins
            .filter { coin ->
                coin.ticker.lowercase().startsWith(q) ||
                coin.name.lowercase().startsWith(q)   ||
                coin.ticker.lowercase().contains(q)   ||
                coin.name.lowercase().contains(q)
            }
            .sortedWith(compareBy(
                { if (it.ticker.lowercase().startsWith(q)) 0 else 1 },
                { if (it.name.lowercase().startsWith(q))   0 else 1 }
            ))
            .take(limit)
    }

    fun canonicalTicker(asset: String): String {
        val up    = asset.trim().uppercase()
        val paren = Regex("\\(([A-Z0-9]{2,6})\\)").find(up)?.groupValues?.get(1)
        val head  = up.substringBefore("(").trim()
        if (paren != null && coins.any { it.ticker == paren }) return paren
        if (coins.any { it.ticker == head }) return head
        coins.firstOrNull { it.name.uppercase() == head }?.let { return it.ticker }
        return paren ?: head
    }

    fun drawableResForTicker(ticker: String): Int? = when (ticker.trim().uppercase()) {
        "BTC"   -> R.drawable.ic_crypto_btc
        "ETH"   -> R.drawable.ic_crypto_eth
        "USDT"  -> R.drawable.ic_crypto_usdt
        "BNB"   -> R.drawable.ic_crypto_bnb
        "SOL"   -> R.drawable.ic_crypto_sol
        "USDC"  -> R.drawable.ic_crypto_usdc
        "XRP"   -> R.drawable.ic_crypto_xrp
        "DOGE"  -> R.drawable.ic_crypto_doge
        "ADA"   -> R.drawable.ic_crypto_ada
        "AVAX"  -> R.drawable.ic_crypto_avax
        "SHIB"  -> R.drawable.ic_crypto_shib
        "DOT"   -> R.drawable.ic_crypto_dot
        "MATIC" -> R.drawable.ic_crypto_matic
        "POL"   -> R.drawable.ic_crypto_pol
        "LTC"   -> R.drawable.ic_crypto_ltc
        "LINK"  -> R.drawable.ic_crypto_link
        "BCH"   -> R.drawable.ic_crypto_bch
        "UNI"   -> R.drawable.ic_crypto_uni
        "ATOM"  -> R.drawable.ic_crypto_atom
        "XLM"   -> R.drawable.ic_crypto_xlm
        "TRX"   -> R.drawable.ic_crypto_trx
        "NEAR"  -> R.drawable.ic_crypto_near
        "FTM"   -> R.drawable.ic_crypto_ftm
        "ALGO"  -> R.drawable.ic_crypto_algo
        "ICP"   -> R.drawable.ic_crypto_icp
        "FIL"   -> R.drawable.ic_crypto_fil
        "GRT"   -> R.drawable.ic_crypto_grt
        "AAVE"  -> R.drawable.ic_crypto_aave
        "ARB"   -> R.drawable.ic_crypto_arb
        "OP"    -> R.drawable.ic_crypto_op
        "DAI"   -> R.drawable.ic_crypto_dai
        "MKR"   -> R.drawable.ic_crypto_mkr
        "COMP"  -> R.drawable.ic_crypto_comp
        "CRV"   -> R.drawable.ic_crypto_crv
        "SNX"   -> R.drawable.ic_crypto_snx
        "SUSHI" -> R.drawable.ic_crypto_sushi
        "YFI"   -> R.drawable.ic_crypto_yfi
        "1INCH" -> R.drawable.ic_crypto_1inch
        "LDO"   -> R.drawable.ic_crypto_ldo
        "SAND"  -> R.drawable.ic_crypto_sand
        "MANA"  -> R.drawable.ic_crypto_mana
        "AXS"   -> R.drawable.ic_crypto_axs
        "ENJ"   -> R.drawable.ic_crypto_enj
        "GALA"  -> R.drawable.ic_crypto_gala
        "IMX"   -> R.drawable.ic_crypto_imx
        "APE"   -> R.drawable.ic_crypto_ape
        "CHZ"   -> R.drawable.ic_crypto_chz
        "VET"   -> R.drawable.ic_crypto_vet
        "EOS"   -> R.drawable.ic_crypto_eos
        "XTZ"   -> R.drawable.ic_crypto_xtz
        "XMR"   -> R.drawable.ic_crypto_xmr
        "ZEC"   -> R.drawable.ic_crypto_zec
        "THETA" -> R.drawable.ic_crypto_theta
        "FLOW"  -> R.drawable.ic_crypto_flow
        "KSM"   -> R.drawable.ic_crypto_ksm
        "EGLD"  -> R.drawable.ic_crypto_egld
        "HBAR"  -> R.drawable.ic_crypto_hbar
        "CAKE"  -> R.drawable.ic_crypto_cake
        "WBTC"  -> R.drawable.ic_crypto_wbtc
        "STETH" -> R.drawable.ic_crypto_steth
        "BSV"   -> R.drawable.ic_crypto_bsv
        "BTT"   -> R.drawable.ic_crypto_btt
        "BUSD"  -> R.drawable.ic_crypto_busd
        "FRAX"  -> R.drawable.ic_crypto_frax
        "TUSD"  -> R.drawable.ic_crypto_tusd
        "USDD"  -> R.drawable.ic_crypto_usdd
        "BAL"   -> R.drawable.ic_crypto_bal
        "TON"   -> R.drawable.ic_crypto_ton
        "SUI"   -> R.drawable.ic_crypto_sui
        "APT"   -> R.drawable.ic_crypto_apt
        "PEPE"  -> R.drawable.ic_crypto_pepe
        "RNDR"  -> R.drawable.ic_crypto_rndr
        "INJ"   -> R.drawable.ic_crypto_inj
        "WIF"   -> R.drawable.ic_crypto_wif
        "BONK"  -> R.drawable.ic_crypto_bonk
        "JUP"   -> R.drawable.ic_crypto_jup
        "TIA"   -> R.drawable.ic_crypto_tia
        "RUNE"  -> R.drawable.ic_crypto_rune
        "FET"   -> R.drawable.ic_crypto_fet
        "STX"   -> R.drawable.ic_crypto_stx
        "DASH"  -> R.drawable.ic_crypto_dash
        "KAVA"  -> R.drawable.ic_crypto_kava
        "ROSE"  -> R.drawable.ic_crypto_rose
        "ZRX"   -> R.drawable.ic_crypto_zrx
        "BAT"   -> R.drawable.ic_crypto_bat
        "ONE"   -> R.drawable.ic_crypto_one
        "ONDO"  -> R.drawable.ic_crypto_ondo
        "HNT"   -> R.drawable.ic_crypto_hnt
        "MINA"  -> R.drawable.ic_crypto_mina
        "PYTH"  -> R.drawable.ic_crypto_pyth
        "PAXG"  -> R.drawable.ic_crypto_paxg
        else    -> null
    }

    private val map: Map<String, CryptoIconInfo> = buildMap {

        put("BTC",      CryptoIconInfo(0xFFF7931AL, "₿"))
        put("BITCOIN",  CryptoIconInfo(0xFFF7931AL, "₿"))
        put("WBTC",     CryptoIconInfo(0xFFF7931AL, "₿"))
        put("TBTC",     CryptoIconInfo(0xFFF7931AL, "₿"))
        put("BCH",      CryptoIconInfo(0xFF0AC18EL, "₿"))
        put("BSV",      CryptoIconInfo(0xFFEAB300L, "₿"))

        put("ETH",       CryptoIconInfo(0xFF627EEAL, "Ξ"))
        put("ETHEREUM",  CryptoIconInfo(0xFF627EEAL, "Ξ"))
        put("WETH",      CryptoIconInfo(0xFF627EEAL, "Ξ"))
        put("STETH",     CryptoIconInfo(0xFF00A3FFL, "Ξ"))
        put("RETH",      CryptoIconInfo(0xFF627EEAL, "Ξ"))

        put("LTC",       CryptoIconInfo(0xFF9ABBC7L, "Ł"))
        put("LITECOIN",  CryptoIconInfo(0xFF9ABBC7L, "Ł"))

        put("DOGE",      CryptoIconInfo(0xFFC2A633L, "Ð"))
        put("DOGECOIN",  CryptoIconInfo(0xFFC2A633L, "Ð"))
        put("SHIB",      CryptoIconInfo(0xFFFF5700L, "Ð"))
        put("LEASH",     CryptoIconInfo(0xFFFF5700L, "Ð"))

        put("BNB",      CryptoIconInfo(0xFFF3BA2FL, "BNB"))
        put("BINANCE",  CryptoIconInfo(0xFFF3BA2FL, "BNB"))
        put("CAKE",     CryptoIconInfo(0xFFFAD44DL, "CAKE"))

        put("SOL",     CryptoIconInfo(0xFF9945FFL, "◎"))
        put("SOLANA",  CryptoIconInfo(0xFF9945FFL, "◎"))
        put("WSOL",    CryptoIconInfo(0xFF9945FFL, "◎"))

        put("XRP",    CryptoIconInfo(0xFF00AAE4L, "XRP"))
        put("RIPPLE", CryptoIconInfo(0xFF00AAE4L, "XRP"))

        put("ADA",     CryptoIconInfo(0xFF0033ADL, "ADA"))
        put("CARDANO", CryptoIconInfo(0xFF0033ADL, "ADA"))

        put("AVAX",       CryptoIconInfo(0xFFE84142L, "AVAX"))
        put("AVALANCHE",  CryptoIconInfo(0xFFE84142L, "AVAX"))

        put("DOT",      CryptoIconInfo(0xFFE6007AL, "DOT"))
        put("POLKADOT", CryptoIconInfo(0xFFE6007AL, "DOT"))

        put("MATIC",   CryptoIconInfo(0xFF8247E5L, "MATIC"))
        put("POL",     CryptoIconInfo(0xFF8247E5L, "POL"))
        put("POLYGON", CryptoIconInfo(0xFF8247E5L, "POL"))

        put("LINK",      CryptoIconInfo(0xFF2A5ADAL, "LINK"))
        put("CHAINLINK", CryptoIconInfo(0xFF2A5ADAL, "LINK"))

        put("UNI",     CryptoIconInfo(0xFFFF007AL, "UNI"))
        put("UNISWAP", CryptoIconInfo(0xFFFF007AL, "UNI"))

        put("ATOM",   CryptoIconInfo(0xFF2E3148L, "ATOM"))
        put("COSMOS", CryptoIconInfo(0xFF2E3148L, "ATOM"))

        put("NEAR", CryptoIconInfo(0xFF3F4040L, "NEAR"))

        put("FTM",    CryptoIconInfo(0xFF13B5ECL, "FTM"))
        put("FANTOM", CryptoIconInfo(0xFF13B5ECL, "FTM"))

        put("ALGO",     CryptoIconInfo(0xFF3A3A3AL, "ALGO"))
        put("ALGORAND", CryptoIconInfo(0xFF3A3A3AL, "ALGO"))

        put("TRX",  CryptoIconInfo(0xFFEF0027L, "TRX"))
        put("TRON", CryptoIconInfo(0xFFEF0027L, "TRX"))
        put("BTT",  CryptoIconInfo(0xFFEF0027L, "BTT"))

        put("XLM",     CryptoIconInfo(0xFF3E4150L, "XLM"))
        put("STELLAR", CryptoIconInfo(0xFF3E4150L, "XLM"))

        put("XMR",    CryptoIconInfo(0xFFFF6600L, "XMR"))
        put("MONERO", CryptoIconInfo(0xFFFF6600L, "XMR"))

        put("ZEC",   CryptoIconInfo(0xFFECB244L, "ZEC"))
        put("ZCASH", CryptoIconInfo(0xFFECB244L, "ZEC"))

        put("USDT",   CryptoIconInfo(0xFF26A17BL, "USDT"))
        put("TETHER", CryptoIconInfo(0xFF26A17BL, "USDT"))
        put("USDC",   CryptoIconInfo(0xFF2775CAL, "USDC"))
        put("BUSD",   CryptoIconInfo(0xFFF3BA2FL, "BUSD"))
        put("DAI",    CryptoIconInfo(0xFFF5AC37L, "DAI"))
        put("TUSD",   CryptoIconInfo(0xFF002868L, "TUSD"))
        put("FRAX",   CryptoIconInfo(0xFF3C3C3CL, "FRAX"))
        put("USDD",   CryptoIconInfo(0xFFEF0027L, "USDD"))

        put("AAVE",     CryptoIconInfo(0xFFB6509EL, "AAVE"))
        put("CRV",      CryptoIconInfo(0xFF3A3A3AL, "CRV"))
        put("CURVE",    CryptoIconInfo(0xFF3A3A3AL, "CRV"))
        put("MKR",      CryptoIconInfo(0xFF1AAB9BL, "MKR"))
        put("MAKER",    CryptoIconInfo(0xFF1AAB9BL, "MKR"))
        put("COMP",     CryptoIconInfo(0xFF00D395L, "COMP"))
        put("COMPOUND", CryptoIconInfo(0xFF00D395L, "COMP"))
        put("SNX",      CryptoIconInfo(0xFF5FCDF9L, "SNX"))
        put("SUSHI",    CryptoIconInfo(0xFF0E0F23L, "SUSHI"))
        put("YFI",      CryptoIconInfo(0xFF006AE3L, "YFI"))
        put("BAL",      CryptoIconInfo(0xFF1E1E1EL, "BAL"))
        put("1INCH",    CryptoIconInfo(0xFF1B314BL, "1INCH"))
        put("LDO",      CryptoIconInfo(0xFFF0A500L, "LDO"))

        put("SAND",      CryptoIconInfo(0xFF00ADEFL, "SAND"))
        put("SANDBOX",   CryptoIconInfo(0xFF00ADEFL, "SAND"))
        put("MANA",      CryptoIconInfo(0xFFFF2D55L, "MANA"))
        put("DECENTRALAND", CryptoIconInfo(0xFFFF2D55L, "MANA"))
        put("AXS",       CryptoIconInfo(0xFF0055D5L, "AXS"))
        put("ENJ",       CryptoIconInfo(0xFF7866D5L, "ENJ"))
        put("GALA",      CryptoIconInfo(0xFF1A1A1AL, "GALA"))
        put("IMX",       CryptoIconInfo(0xFF17B5CBL, "IMX"))
        put("APE",       CryptoIconInfo(0xFF063A5EL, "APE"))

        put("ARB",       CryptoIconInfo(0xFF28A0F0L, "ARB"))
        put("ARBITRUM",  CryptoIconInfo(0xFF28A0F0L, "ARB"))
        put("OP",        CryptoIconInfo(0xFFFF0420L, "OP"))
        put("OPTIMISM",  CryptoIconInfo(0xFFFF0420L, "OP"))

        put("VET",       CryptoIconInfo(0xFF15BDEFL, "VET"))
        put("VECHAIN",   CryptoIconInfo(0xFF15BDEFL, "VET"))
        put("EOS",       CryptoIconInfo(0xFF443F54L, "EOS"))
        put("XTZ",       CryptoIconInfo(0xFF2C7DF7L, "XTZ"))
        put("TEZOS",     CryptoIconInfo(0xFF2C7DF7L, "XTZ"))
        put("THETA",     CryptoIconInfo(0xFF2AB8E6L, "THETA"))
        put("FIL",       CryptoIconInfo(0xFF0090FFL, "FIL"))
        put("FILECOIN",  CryptoIconInfo(0xFF0090FFL, "FIL"))
        put("ICP",       CryptoIconInfo(0xFF3B00B9L, "ICP"))
        put("GRT",       CryptoIconInfo(0xFF6747EDL, "GRT"))
        put("EGLD",      CryptoIconInfo(0xFF23F7DDL, "EGLD"))
        put("MULTIVERSX",CryptoIconInfo(0xFF23F7DDL, "EGLD"))
        put("HBAR",      CryptoIconInfo(0xFF222222L, "HBAR"))
        put("HEDERA",    CryptoIconInfo(0xFF222222L, "HBAR"))
        put("FLOW",      CryptoIconInfo(0xFF00EF8BL, "FLOW"))
        put("KSM",       CryptoIconInfo(0xFFE6007AL, "KSM"))
        put("KUSAMA",    CryptoIconInfo(0xFFE6007AL, "KSM"))
        put("CHZ",       CryptoIconInfo(0xFFCD0124L, "CHZ"))
        put("CHILIZ",    CryptoIconInfo(0xFFCD0124L, "CHZ"))
    }

    fun resolve(asset: String): CryptoIconInfo? {
        if (asset.isBlank()) return null
        val upper = asset.trim().uppercase()
        map[upper]?.let { return it }
        val firstToken = upper.split(" ", ".", "-", "_").firstOrNull() ?: return null
        return map[firstToken]
    }
}
