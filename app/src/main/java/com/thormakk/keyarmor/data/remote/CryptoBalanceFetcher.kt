package com.thormakk.keyarmor.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class BalanceResult {
    data class Success(val amount: String, val symbol: String) : BalanceResult()
    object Unsupported : BalanceResult()
    object Failure : BalanceResult()
}

@Singleton
class CryptoBalanceFetcher @Inject constructor(
    private val balanceLookupPrefs: com.thormakk.keyarmor.data.prefs.BalanceLookupPrefs,
) {

    companion object {
        private const val TIMEOUT_MS = 10_000

        private const val GENERIC_UA = "Mozilla/5.0"

        private const val SOL_RPC = "https://api.mainnet-beta.solana.com"

        private val SOL_RPC_FALLBACKS = listOf(
            "https://rpc.ankr.com/solana",
            "https://solana-rpc.publicnode.com",
        )
    }

    private fun solanaPost(body: String): String {
        for (rpc in listOf(SOL_RPC) + SOL_RPC_FALLBACKS) {
            val result = runCatching { post(rpc, body) }.getOrNull() ?: continue
            val json   = runCatching { JSONObject(result) }.getOrNull() ?: continue
            if (!json.has("error")) return result
        }
        return post(SOL_RPC, body)
    }

    private val SPL_TOKENS = mapOf(
        "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v" to ("USDC"  to 6),
        "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"  to ("USDT"  to 6),
        "9n4nbM75f5Ui33ZbPYXn59EwSgE8CGsHtAeTH5YFeJ9E"  to ("WBTC"  to 6),
        "So11111111111111111111111111111111111111112"     to ("WSOL"  to 9),
        "mSoLzYCxHdYgdzU16g5QSh3i5K3z3KZK7ytfqcJm7So"  to ("mSOL"  to 9),
        "7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"  to ("WETH"  to 8),
        "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"  to ("RAY"   to 6),
        "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"  to ("BONK"  to 5),
        "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"   to ("JUP"   to 6),
        "hntyVP6YFm1Hg25TN9WGLqM12b8TQmcknKrdu1oxWux"   to ("HNT"   to 8),
        "bSo13r4TkiE4KumL71LsHTPpL2euBYLFx6h9HP3piy1"   to ("bSOL"  to 9),
        "jtojtomepa8b1dh4RGoZtoCEjL5KZXDSp4wMhWDCoS5"   to ("JTO"   to 9),
    )

    private val EVM_RPC_FALLBACKS = mapOf(
        "Ethereum"  to listOf(
            "https://rpc.ankr.com/eth",
            "https://ethereum.publicnode.com",
            "https://1rpc.io/eth",
            "https://eth.drpc.org",
        ),
        "BNB"       to listOf(
            "https://bsc-dataseed.binance.org",
            "https://bsc-dataseed1.defibit.io",
        ),
        "Polygon"   to listOf(
            "https://rpc.ankr.com/polygon",
            "https://polygon-bor-rpc.publicnode.com",
            "https://polygon.llamarpc.com",
            "https://polygon-rpc.com",
        ),
        "Arbitrum"  to listOf(
            "https://arb1.arbitrum.io/rpc",
            "https://rpc.ankr.com/arbitrum",
        ),
        "Optimism"  to listOf(
            "https://mainnet.optimism.io",
            "https://rpc.ankr.com/optimism",
        ),
        "Base"      to listOf("https://mainnet.base.org"),
        "zkSync"    to listOf("https://mainnet.era.zksync.io"),
        "Scroll"    to listOf("https://rpc.scroll.io"),
        "Gnosis"    to listOf(
            "https://rpc.gnosischain.com",
            "https://gnosis-rpc.publicnode.com",
        ),
        "Celo"      to listOf(
            "https://forno.celo.org",
            "https://rpc.ankr.com/celo",
        ),
        "Moonbeam"  to listOf(
            "https://rpc.api.moonbeam.network",
            "https://moonbeam.public.blastapi.io",
            "https://rpc.ankr.com/moonbeam",
        ),
        "ETC"       to listOf(
            "https://etc.rivet.link",
            "https://rpc.ankr.com/eth_classic",
        ),
        "Avalanche" to listOf("https://api.avax.network/ext/bc/C/rpc"),
        "Fantom"    to listOf(
            "https://fantom.publicnode.com",
            "https://rpc.ankr.com/fantom",
            "https://rpcapi.fantom.network",
        ),
    )

    private val ERC20: Map<String, Map<String, Pair<String, Int>>> = mapOf(
        "Ethereum" to mapOf(
            "USDC" to ("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48" to 6),
            "USDT" to ("0xdAC17F958D2ee523a2206206994597C13D831ec7" to 6),
            "DAI"  to ("0x6B175474E89094C44Da98b954EedeAC495271d0F" to 18),
            "WBTC" to ("0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599" to 8),
            "WETH" to ("0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2" to 18),
            "LINK" to ("0x514910771AF9Ca656af840dff83E8264EcF986CA" to 18),
            "UNI"  to ("0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984" to 18),
            "AAVE" to ("0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9" to 18),
            "SHIB" to ("0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE" to 18),
            "PEPE" to ("0x6982508145454Ce325dDbE47a25d4ec3d2311933" to 18),
            "PAXG" to ("0x45804880De22913dAFE09f4980848ECE6EcbAf78" to 18),
            "STETH" to ("0xae7ab96520DE3A18E5e111B5EaAb095312D7fE84" to 18),
            "CRV"   to ("0xD533a949740bb3306d119CC777fa900bA034cd52" to 18),
            "MKR"   to ("0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2" to 18),
            "LDO"   to ("0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32" to 18),
            "COMP"  to ("0xc00e94Cb662C3520282E6f5717214004A7f26888" to 18),
            "SNX"   to ("0xC011a73ee8576Fb46F5E1c5751cA3B9Fe0af2a6F" to 18),
            "SUSHI" to ("0x6B3595068778DD592e39A122f4f5a5cF09C90fE2" to 18),
            "YFI"   to ("0x0bc529c00C6401aEF6D220BE8C6Ea1667F6Ad93e" to 18),
            "BAL"   to ("0xba100000625a3754423978a60c9317c58a424e3D" to 18),
            "ZRX"   to ("0xE41d2489571d322189246DaFA5ebDe1F4699F498" to 18),
            "BAT"   to ("0x0D8775F648430679A709E98d2b0Cb6250d2887EF" to 18),
            "ONDO"  to ("0xfAbA6f8e4a5E8Ab82F62fe7C39859FA577269BE3" to 18),
            "GRT"   to ("0xc944E90C64B2c07662A292be6244BDf05Cda44a7" to 18),
            "APE"   to ("0x4d224452801ACEd8B2F0aebE155379bb5D594381" to 18),
            "IMX"   to ("0xF57e7e7C23978C3cAEC3C3548E3D615c346e79fF" to 18),
            "MANA"  to ("0x0F5D2fB29fb7d3CFeE444a200298f468908cC942" to 18),
            "SAND"  to ("0x3845badAde8e6dFF049820680d1F14bD3903a5d0" to 18),
            "AXS"   to ("0xBB0E17EF65F82Ab018d8EDd776e8DD940327B28b" to 18),
            "GALA"  to ("0xd1d2Eb1B1e90B638588728b4130137D262C87cae" to 8),
            "ENJ"   to ("0xF629cBd94d3791C9250152BD8dfBDF380E2a3B9c" to 18),
            "CHZ"   to ("0x3506424F91fD33084466F402d5D97f05F8e3b4AF" to 18),
            "FRAX"  to ("0x853d955aCEf822Db058eb8505911ED77F175b99e" to 18),
            "1INCH" to ("0x111111111117dC0aa78b770fA6A738034120C302" to 18),
            "FET"   to ("0xaea46A60368A7bD060eec7DF8CBa43b7EF41Ad85" to 18),
            "RNDR"  to ("0x6De037ef9aD2725EB40118Bb1702EBb27e4Aeb24" to 18),
        ),
        "Polygon" to mapOf(
            "USDC" to ("0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359" to 6),
            "USDT" to ("0xc2132D05D31c914a87C6611C10748AEb04B58e8F" to 6),
            "DAI"  to ("0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063" to 18),
            "WBTC" to ("0x1BFD67037B42Cf73acF2047067bd4F2C47D9BfD6" to 8),
            "WETH" to ("0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619" to 18),
            "LINK" to ("0x53E0bca35eC356BD5ddDFebbD1Fc0fD03FaBad39" to 18),
            "SAND" to ("0xBbba073C31bF03b8ACf7c28EF0738DeCF3695683" to 18),
            "MANA" to ("0xA1c57f48F0Deb89f569dFbE6E2B7f46D33606fD4" to 18),
        ),
        "BNB" to mapOf(
            "USDC" to ("0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d" to 18),
            "USDT" to ("0x55d398326f99059fF775485246999027B3197955" to 18),
            "DAI"  to ("0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3" to 18),
            "WBTC" to ("0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c" to 18),
            "WETH" to ("0x2170Ed0880ac9A755fd29B2688956BD959F933F8" to 18),
            "CAKE" to ("0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82" to 18),
            "BUSD" to ("0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56" to 18),
        ),
        "Arbitrum" to mapOf(
            "USDC" to ("0xaf88d065e77c8cC2239327C5EDb3A432268e5831" to 6),
            "USDT" to ("0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9" to 6),
            "DAI"  to ("0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1" to 18),
            "WBTC" to ("0x2f2a2543B76A4166549F7aaB2e75Bef0aefC5B0f" to 8),
            "WETH" to ("0x82aF49447D8a07e3bd95BD0d56f35241523fBab1" to 18),
            "ARB"  to ("0x912CE59144191C1204E64559FE8253a0e49E6548" to 18),
            "LINK" to ("0xf97f4df75117a78c1A5a0DBb814Af92458539FB4" to 18),
        ),
        "Optimism" to mapOf(
            "USDC" to ("0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85" to 6),
            "USDT" to ("0x94b008aA00579c1307B0EF2c499aD98a8ce58e58" to 6),
            "DAI"  to ("0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1" to 18),
            "WBTC" to ("0x68f180fcCe6836688e9084f035309E29Bf0A2095" to 8),
            "WETH" to ("0x4200000000000000000000000000000000000006" to 18),
            "OP"   to ("0x4200000000000000000000000000000000000042" to 18),
            "LINK" to ("0x350a791Bfc2C21F9Ed5d10980Dad2e2638ffa7f6" to 18),
        ),
        "Base" to mapOf(
            "USDC"  to ("0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913" to 6),
            "USDT"  to ("0xfde4C96c8593536E31F229EA8f37b2ADa2699bb2" to 6),
            "DAI"   to ("0x50c5725949A6F0c72E6C4a641F24049A917DB0Cb" to 18),
            "WETH"  to ("0x4200000000000000000000000000000000000006" to 18),
            "WBTC"  to ("0x0555E30da8f98308EdB960aa94C0Db47230d2B9c" to 8),
            "CBBTC" to ("0xcbB7C0000aB88B473b1f5aFd9ef808440eed33Bf" to 8),
        ),
        "Avalanche" to mapOf(
            "USDC" to ("0xB97EF9Ef8734C71904D8002F8b6Bc66Dd9c48a6E" to 6),
            "USDT" to ("0x9702230A8Ea53601f5cD2dc00fDBc13d4dF4A8c7" to 6),
            "DAI"  to ("0xd586E7F844cEa2F87f50152665BCbc2C279D8d70" to 18),
            "WBTC" to ("0x50b7545627a5162F82A992c33b87aDc75187B218" to 8),
            "WETH" to ("0x49D5c2BdFfac6CE2BFdB6640F4F80f226bc10bAB" to 18),
        ),
        "Fantom" to mapOf(
            "USDC" to ("0x04068DA6C83AFCFA0e13ba15A6696662335D5B75" to 6),
            "USDT" to ("0x049d68029688eAbF473097a2fC38ef61633A3C7A" to 6),
            "DAI"  to ("0x8D11eC38a3EB5E956B052f67Da8Bdc9bef8Abf3E" to 18),
            "WBTC" to ("0x321162Cd933E2Be498Cd2267a90534A804051b11" to 8),
            "WETH" to ("0x74b23882a30290451A17c44f4F05243b6b58C76d" to 18),
        ),
        "Gnosis" to mapOf(
            "USDC" to ("0xDDAfbb505ad214D7b80b1f830fcCc89B60fb7A83" to 6),
            "USDT" to ("0x4ECaBa5870353805a9F068101A40E0f32ed605C6" to 6),
            "WBTC" to ("0x8e5bBbb09Ed1ebdE8674Cda39A0c169401db4252" to 8),
            "WETH" to ("0x6A023CCd1ff6F2045C3309768eAd9E68F978f6e1" to 18),
        ),
        "Celo" to mapOf(
            "USDC" to ("0xcebA9300f2b948710d2653dD7B07f33A8B32118C" to 6),
            "USDT" to ("0x617f3112bf5397D0F8fbe1AcDA702AeE3B88d8e2" to 6),
            "DAI"  to ("0xE4fE50cdD716522A56204352f00AA110F731932d" to 18),
        ),
        "zkSync" to mapOf(
            "USDC" to ("0x1d17CBcF0D6D143135aE902365D2E5e2A16538D4" to 6),
            "USDT" to ("0x493257fD37EDB34451f62EDf8D2a0C418852bA4C" to 6),
            "WBTC" to ("0xBBeB516fb02a01611cBBE0453Fe3c580D7281011" to 8),
            "WETH" to ("0x5AEa5775959fBC2557Cc8789bC1bf90A239D9a91" to 18),
        ),
    )

    private val TRC20 = mapOf(
        "USDT"  to ("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t" to 6),
        "USDC"  to ("TEkxiTehnzSmse5XcPGWjZ9mBVSJDs6oss" to 6),
        "TUSD"  to ("TUpMhErZL2fhh4sVNULAbNKLokS4GjC1F4" to 18),
        "JST"   to ("TCFLL5dx5ZJdKnWuesXxi1VPwjLVmWZZy9" to 18),
        "SUN"   to ("TSSMHYeV2uE9qYH95DqyoCuNCzEL1NvU3S" to 18),
        "BTT"   to ("TAFjULxiVgT4qWk6UZwjqwZXTSaGaqnVp4" to 18),
        "WIN"   to ("TLa2f6VPqDgRE67v1736s7bJ8Ray5wYjU7" to 6),
        "WTRX"  to ("TNUC9Qb1rRpS5CbWLmNMxXBjyFoydXjWFR" to 6),
    )

    private val STELLAR_ASSETS = mapOf(
        "USDC" to ("USDC" to "GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"),
        "USDT" to ("USDT" to "GCQTGZQQ5G4PTM2GL7CDIFKUBIPEC52BROAQIAPW53XBRJVN6ZJVTG6"),
        "BTC"  to ("BTC"  to "GAUTUYY2THLF7SGITDFMXJVYH3LHDSMGEAKSBU267M2K7A3W543CKUEF"),
        "ETH"  to ("ETH"  to "GBDEVU63Y6NTHJQQZIKVTC23NWLQVP3WJ2RI2OTSJTNYOIGICST6DUXR"),
        "AQUA" to ("AQUA" to "GBNZILSTVQZ4R7IKQDGHYGY2QXL5QOFJYQMXPKWRRM5PAV7Y4M67AQUA"),
    )

    private val ALGORAND_ASAS = mapOf(
        "USDC"  to (31566704L   to 6),
        "USDT"  to (312769L     to 6),
        "WBTC"  to (1058926737L to 8),
        "WETH"  to (386192940L  to 8),
        "YLDY"  to (226701642L  to 6),
        "STBL"  to (465865291L  to 6),
        "PLANETS" to (27165954L to 0),
        "GARD"  to (684649988L  to 6),
    )

    private val HEDERA_TOKENS = mapOf(
        "USDC"  to ("0.0.456858"  to 6),
        "USDT"  to ("0.0.1055483" to 6),
        "HBARX" to ("0.0.834783"  to 8),
        "SAUCE" to ("0.0.731861"  to 6),
        "HELI"  to ("0.0.786931"  to 8),
        "DOVU"  to ("0.0.489284"  to 8),
    )

    private val TEZOS_TOKENS = mapOf(
        "USDC"  to Triple("KT1UsSfaXyqcjSVPeiD7U1bWgKy3taYN7NWY", "0",  6),
        "USDT"  to Triple("KT1XnTn74bUtxHfDtBmm2bGZAQfhPbvKWR8o", "0",  6),
        "tzBTC" to Triple("KT1PWx2mnDueood7fEmfEx6HGT4hqjMW9Cmx",  "0",  8),
        "WBTC"  to Triple("KT18fp5rcTW7mbWDmzFwjLDUhs5MeJmagDSZ",  "19", 8),
        "WETH"  to Triple("KT18fp5rcTW7mbWDmzFwjLDUhs5MeJmagDSZ",  "20", 18),
        "kUSD"  to Triple("KT1rXiTL2i3RNFvbwZjcTSdxhDGNdMfcFPT1", "0",  18),
        "USDtz" to Triple("KT1LN4LPSqTMS7Sd2CJw4bbDGRkMv2t68Fy9", "0",  6),
        "QUIPU" to Triple("KT193D4vozYnhGJQVtw7CoxxqphqUEEwK6Vb",  "0",  6),
    )

    private val COSMOS_DENOMS = mapOf(
        "ATOM"  to Triple("cosmoshub",     "uatom",   1_000_000L),
        "OSMO"  to Triple("osmosis",       "uosmo",   1_000_000L),
        "JUNO"  to Triple("juno",          "ujuno",   1_000_000L),
        "STARS" to Triple("stargaze",      "ustars",  1_000_000L),
        "LUNA"  to Triple("terra2",        "uluna",   1_000_000L),
        "AKT"   to Triple("akash",         "uakt",    1_000_000L),
        "SCRT"  to Triple("secretnetwork", "uscrt",   1_000_000L),
        "KAVA"  to Triple("kava",          "ukava",   1_000_000L),
        "CRO"   to Triple("cryptoorgchain","basecro", 100_000_000L),
        "BAND"  to Triple("bandchain",     "uband",   1_000_000L),
    )

    private val TON_JETTONS = mapOf(
        "USDT" to ("EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs" to 6),
        "USDC" to ("EQB-MPwrd1G5f4yflrnWjxoN2RxKoYYDqvmH4ZS7F6eFq2F3"  to 6),
        "WBTC" to ("EQDcBkGHmC4pTf34x3Gm05XvepO5w60DNxZ-XT4I6-UGG5L5"  to 8),
        "WETH" to ("EQAW42HutyDem98Be1f27PoXobghh81umTQ-cGgaKVmRLS76"   to 9),
        "NOT"  to ("EQAvlWFDxGF2lXm67y4yzC17wYKD9A0guwPkMs1gOsM66C0T"  to 9),
        "STON" to ("EQA2kCVNwVsil2EM2mB0SkXytxCqQjS4mttjDpnXmwG9T6bO"  to 9),
    )

    private val CARDANO_ASSETS = mapOf(
        "DJED"  to ("8db269c3ec630e06ae29f74bc39edd1f87c819f1056206e879a1cd6446a6564"   to 6),
        "SHEN"  to ("8db269c3ec630e06ae29f74bc39edd1f87c819f1056206e879a1cd65368656e"   to 6),
        "iUSD"  to ("f66d78b4a3cb3d37afa0ec36461e51ecbbd314f27f7a4bc33c5749245555344"   to 6),
        "MIN"   to ("29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c64d494e"    to 6),
        "AGIX"  to ("f43a62fdc3965df486de8a0d32fe800963589c41b38946602a0dc534147495834" to 8),
        "NMKR"  to ("5dac8536653edc12f6f5e1045d8164b9f59998d3bdc300fc928434174d4b4d52"  to 6),
    )

    suspend fun fetch(network: String, address: String, asset: String = ""): BalanceResult {
        if (address.isBlank()) return BalanceResult.Failure
        if (!balanceLookupPrefs.isConsentGranted()) return BalanceResult.Failure

        val cleanAsset = asset.substringBefore("(").trim()
        return withContext(Dispatchers.IO) {
            runCatching {
                when {
                    network.startsWith("Bitcoin Cash")     -> fetchBchWithFallback(address)
                    network.startsWith("Bitcoin")          -> fetchBtc(address)
                    network.startsWith("Dogecoin")         -> fetchDoge(address)
                    network.startsWith("Litecoin")         -> fetchLtc(address)
                    network.startsWith("Dash")             -> fetchDashWithFallback(address)
                    network.startsWith("Zcash")            -> fetchZecWithFallback(address)

                    network.startsWith("Ethereum Classic") -> fetchEvmSmart(address, cleanAsset, "ETC", "https://etc.blockscout.com", "ETC", useBlockscout = true)

                    network.startsWith("Ethereum")  -> fetchEvmSmart(address, cleanAsset, "Ethereum", "https://eth.blockscout.com",        "ETH",  useBlockscout = true)
                    network.startsWith("BNB")       -> fetchEvmSmart(address, cleanAsset, "BNB",      "https://bsc.blockscout.com",        "BNB",  useBlockscout = true)
                    network.startsWith("Polygon")   -> fetchEvmSmart(address, cleanAsset, "Polygon",  "https://polygon.blockscout.com",    "MATIC",useBlockscout = true)
                    network.startsWith("Arbitrum")  -> fetchEvmSmart(address, cleanAsset, "Arbitrum", "https://arbitrum.blockscout.com",   "ETH",  useBlockscout = true)
                    network.startsWith("Optimism")  -> fetchEvmSmart(address, cleanAsset, "Optimism", "https://optimism.blockscout.com",   "ETH",  useBlockscout = true)
                    network.startsWith("Base")      -> fetchEvmSmart(address, cleanAsset, "Base",     "https://base.blockscout.com",       "ETH",  useBlockscout = true)
                    network.startsWith("zkSync")    -> fetchEvmSmart(address, cleanAsset, "zkSync",   "https://zksync.blockscout.com",     "ETH",  useBlockscout = true)
                    network.startsWith("Scroll")    -> fetchEvmSmart(address, cleanAsset, "Scroll",   "https://scroll.blockscout.com",     "ETH",  useBlockscout = true)
                    network.startsWith("Gnosis")    -> fetchEvmSmart(address, cleanAsset, "Gnosis",   "https://gnosis.blockscout.com",     "xDAI", useBlockscout = true)
                    network.startsWith("Celo")      -> fetchEvmSmart(address, cleanAsset, "Celo",     "https://celo.blockscout.com",       "CELO", useBlockscout = true)
                    network.startsWith("Moonbeam")  -> fetchEvmSmart(address, cleanAsset, "Moonbeam", "https://moonbeam.blockscout.com",   "GLMR", useBlockscout = true)

                    network.startsWith("Avalanche") -> fetchEvmSmart(address, cleanAsset, "Avalanche", "https://api.avax.network/ext/bc/C/rpc",  "AVAX", useBlockscout = false)
                    network.startsWith("Fantom")    -> fetchEvmSmart(address, cleanAsset, "Fantom",    "https://fantom.blockscout.com",          "FTM",  useBlockscout = true)

                    network.startsWith("Solana")    -> fetchSolanaSmart(address, cleanAsset)

                    network.startsWith("Ripple")    -> fetchXrp(address, cleanAsset)
                    network.startsWith("Cardano")   -> fetchAda(address, cleanAsset)
                    network.startsWith("Tron")      -> fetchTrx(address, cleanAsset)
                    network.startsWith("Stellar")   -> fetchXlm(address, cleanAsset)
                    network.startsWith("Algorand")  -> fetchAlgo(address, cleanAsset)
                    network.startsWith("Hedera")    -> fetchHbar(address, cleanAsset)
                    network.startsWith("Tezos")     -> fetchXtz(address, cleanAsset)
                    network.startsWith("Cosmos")    -> fetchAtom(address, cleanAsset)
                    network.startsWith("TON")       -> fetchTon(address, cleanAsset)

                    else -> BalanceResult.Unsupported
                }
            }.getOrElse { BalanceResult.Failure }
        }
    }

    suspend fun fetchAllSolanaTokenBalances(walletAddress: String): Map<String, Double> {
        if (!balanceLookupPrefs.isConsentGranted()) return emptyMap()
        return withContext(Dispatchers.IO) {
            runCatching {
                val body = """{"jsonrpc":"2.0","id":1,"method":"getTokenAccountsByOwner","params":["$walletAddress",{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"},{"encoding":"jsonParsed"}]}"""
                val json = JSONObject(solanaPost(body))
                if (json.has("error")) return@runCatching emptyMap<String, Double>()
                val arr = json.getJSONObject("result").getJSONArray("value")
                val balances = mutableMapOf<String, Double>()
                for (i in 0 until arr.length()) {
                    runCatching {
                        val account = arr.getJSONObject(i).getJSONObject("account")
                        val data    = account.getJSONObject("data")
                        if (data.optString("program") != "spl-token") return@runCatching
                        val info    = data.getJSONObject("parsed").getJSONObject("info")
                        val mint    = info.getString("mint")
                        val symbol  = SPL_TOKENS[mint]?.first ?: return@runCatching
                        val amount  = info.getJSONObject("tokenAmount").optDouble("uiAmount", 0.0)
                        balances[symbol] = (balances[symbol] ?: 0.0) + amount
                    }
                }
                balances
            }.getOrElse { emptyMap() }
        }
    }

    suspend fun detectToken(network: String, address: String): String? {
        if (!network.startsWith("Solana")) return null
        if (!balanceLookupPrefs.isConsentGranted()) return null

        return withContext(Dispatchers.IO) {
            runCatching { detectSolanaAta(address) }.getOrNull()
        }
    }

    private fun fetchEvmSmart(
        address:       String,
        asset:         String,
        networkKey:    String,
        url:           String,
        nativeSymbol:  String,
        useBlockscout: Boolean
    ): BalanceResult {
        val symbol = asset.trim().uppercase()
        val token  = ERC20[networkKey]?.get(symbol)
        return if (token != null) {
            val (contract, decimals) = token
            val fallbacks = EVM_RPC_FALLBACKS[networkKey]
                ?: if (!useBlockscout) listOf(url) else emptyList()
            fetchErc20WithFallback(address, fallbacks, url, contract, symbol, decimals, useBlockscout)
        } else {
            if (useBlockscout) {
                val bsResult = runCatching { fetchBlockscoutNative(address, url, nativeSymbol) }.getOrNull()
                if (bsResult is BalanceResult.Success) {
                    bsResult
                } else {
                    val fallbacks = EVM_RPC_FALLBACKS[networkKey] ?: emptyList()
                    var rpcResult: BalanceResult? = null
                    for (rpc in fallbacks) {
                        rpcResult = runCatching { fetchEvmRpc(address, rpc, nativeSymbol) }.getOrNull()
                        if (rpcResult is BalanceResult.Success) break
                    }
                    rpcResult ?: bsResult ?: BalanceResult.Failure
                }
            } else {
                fetchEvmRpc(address, url, nativeSymbol)
            }
        }
    }

    private fun fetchErc20WithFallback(
        address:       String,
        rpcList:       List<String>,
        blockscoutUrl: String,
        contract:      String,
        symbol:        String,
        decimals:      Int,
        useBlockscout: Boolean
    ): BalanceResult {
        if (useBlockscout) {
            val v1Result = runCatching {
                fetchErc20Blockscout(address, blockscoutUrl, contract, symbol, decimals)
            }.getOrNull()
            if (v1Result is BalanceResult.Success) return v1Result

            val v2Result = runCatching {
                fetchErc20BlockscoutV2(address, blockscoutUrl, contract, symbol, decimals)
            }.getOrNull()
            if (v2Result is BalanceResult.Success) return v2Result
        }

        for (rpc in rpcList) {
            val result = runCatching {
                fetchErc20Rpc(address, rpc, contract, symbol, decimals)
            }.getOrNull()
            if (result is BalanceResult.Success) return result
        }

        return BalanceResult.Failure
    }

    private fun fetchErc20BlockscoutV2(
        walletAddress: String,
        blockscoutBase: String,
        contract:       String,
        symbol:         String,
        decimals:       Int
    ): BalanceResult {
        val url  = "$blockscoutBase/api/v2/addresses/$walletAddress/token-balances"
        val json = JSONObject(get(url))
        val arr  = json.optJSONArray("items") ?: return BalanceResult.Success("0", symbol)
        for (i in 0 until arr.length()) {
            val item  = arr.getJSONObject(i)
            val token = item.optJSONObject("token") ?: continue
            if (token.optString("address").equals(contract, ignoreCase = true)) {
                val raw   = item.optString("value", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO
                val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
                return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
            }
        }
        return BalanceResult.Success("0", symbol)
    }

    private fun fetchBlockscoutNative(address: String, baseUrl: String, symbol: String): BalanceResult {
        runCatching {
            val json = JSONObject(get("$baseUrl/api?module=account&action=balance&address=$address"))
            if (json.optString("status") == "1") {
                val wei   = json.getString("result").toBigDecimal()
                val value = wei.divide(BigDecimal("1000000000000000000"))
                return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
            }
        }
        runCatching {
            val json    = JSONObject(get("$baseUrl/api/v2/addresses/$address"))
            val balance = json.optString("coin_balance", "")
            if (balance.isNotEmpty()) {
                val wei   = balance.toBigDecimal()
                val value = wei.divide(BigDecimal("1000000000000000000"))
                return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
            }
        }
        return BalanceResult.Failure
    }

    private fun fetchEvmRpc(address: String, rpcUrl: String, symbol: String): BalanceResult {
        val body = """{"jsonrpc":"2.0","id":1,"method":"eth_getBalance","params":["$address","latest"]}"""
        val json = JSONObject(post(rpcUrl, body))
        val hex  = json.getString("result").removePrefix("0x")
        val wei  = hex.toBigIntegerOrNull(16)?.toBigDecimal()
            ?: return BalanceResult.Failure
        val value = wei.divide(BigDecimal("1000000000000000000"))
        return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
    }

    private fun fetchErc20Blockscout(
        walletAddress: String, baseUrl: String,
        contract: String, symbol: String, decimals: Int
    ): BalanceResult {
        val url  = "$baseUrl/api?module=account&action=tokenbalance&contractaddress=$contract&address=$walletAddress"
        val json = JSONObject(get(url))
        if (json.getString("status") != "1") return BalanceResult.Failure
        val raw   = json.getString("result").toBigDecimal()
        val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
        return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
    }

    private fun fetchErc20Rpc(
        walletAddress: String, rpcUrl: String,
        contract: String, symbol: String, decimals: Int
    ): BalanceResult {
        val padded = walletAddress.removePrefix("0x").lowercase().padStart(64, '0')
        val data   = "0x70a08231$padded"
        val body   = """{"jsonrpc":"2.0","id":1,"method":"eth_call","params":[{"to":"$contract","data":"$data"},"latest"]}"""
        val json   = JSONObject(post(rpcUrl, body))
        check(!json.has("error")) {
            json.getJSONObject("error").optString("message", "RPC error")
        }
        val result = json.optString("result", "0x")
        if (result.length <= 2) return BalanceResult.Success("0", symbol)
        val raw    = result.removePrefix("0x").toBigIntegerOrNull(16)?.toBigDecimal()
            ?: return BalanceResult.Failure
        val value  = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
        return BalanceResult.Success(formatDecimal(value.toDouble()), symbol)
    }

    private fun fetchSolanaSmart(address: String, asset: String): BalanceResult {
        val detectedSymbol = runCatching { detectSolanaAta(address) }.getOrNull()
        if (detectedSymbol != null) {
            val ataResult = runCatching { fetchSolanaAtaBalance(address, detectedSymbol) }.getOrNull()
            if (ataResult is BalanceResult.Success) return ataResult
        }

        val normalized = asset.trim().uppercase()
        if (normalized.isNotEmpty() && normalized != "SOL") {
            val mintEntry = SPL_TOKENS.entries.firstOrNull { it.value.first == normalized }
            if (mintEntry != null) {
                val balance = fetchSolanaTokenByMint(address, mintEntry.key)
                    ?: return BalanceResult.Success("0", normalized)
                return BalanceResult.Success(formatDecimal(balance), normalized)
            }
        }
        return fetchSol(address)
    }

    private fun detectSolanaAta(address: String): String? {
        val body  = """{"jsonrpc":"2.0","id":1,"method":"getAccountInfo","params":["$address",{"encoding":"jsonParsed"}]}"""
        val json  = JSONObject(solanaPost(body))
        val value = json.getJSONObject("result").optJSONObject("value") ?: return null
        val data  = value.optJSONObject("data") ?: return null
        if (data.optString("program") != "spl-token") return null
        val parsed = data.getJSONObject("parsed")
        if (parsed.optString("type") != "account") return null
        val mint = parsed.getJSONObject("info").getString("mint")
        return SPL_TOKENS[mint]?.first
    }

    private fun fetchSolanaAtaBalance(ataAddress: String, symbol: String): BalanceResult {
        val body  = """{"jsonrpc":"2.0","id":1,"method":"getAccountInfo","params":["$ataAddress",{"encoding":"jsonParsed"}]}"""
        val json  = JSONObject(solanaPost(body))
        val value = json.getJSONObject("result").optJSONObject("value")
            ?: return BalanceResult.Success("0", symbol)
        val uiAmount = value
            .getJSONObject("data")
            .getJSONObject("parsed")
            .getJSONObject("info")
            .getJSONObject("tokenAmount")
            .optDouble("uiAmount", 0.0)
        return BalanceResult.Success(formatDecimal(uiAmount), symbol)
    }

    private fun fetchSolanaTokenByMint(walletAddress: String, mint: String): Double? {
        val body = """{"jsonrpc":"2.0","id":1,"method":"getTokenAccountsByOwner","params":["$walletAddress",{"mint":"$mint"},{"encoding":"jsonParsed"}]}"""
        val json = JSONObject(solanaPost(body))
        val arr  = json.getJSONObject("result").getJSONArray("value")
        if (arr.length() == 0) return null
        return arr.getJSONObject(0)
            .getJSONObject("account")
            .getJSONObject("data")
            .getJSONObject("parsed")
            .getJSONObject("info")
            .getJSONObject("tokenAmount")
            .optDouble("uiAmount", 0.0)
    }

    private fun fetchSol(address: String): BalanceResult {
        val body  = """{"jsonrpc":"2.0","id":1,"method":"getBalance","params":["$address"]}"""
        val json  = JSONObject(solanaPost(body))
        val lamps = json.getJSONObject("result").getLong("value")
        return BalanceResult.Success(formatDecimal(lamps / 1_000_000_000.0), "SOL")
    }

    private fun fetchBtc(address: String): BalanceResult {
        val json  = JSONObject(get("https://mempool.space/api/address/$address"))
        val stats = json.getJSONObject("chain_stats")
        val sats  = stats.getLong("funded_txo_sum") - stats.getLong("spent_txo_sum")
        return BalanceResult.Success(formatSatoshi(sats), "BTC")
    }

    private fun fetchLtc(address: String): BalanceResult {
        val json  = JSONObject(get("https://litecoinspace.org/api/address/$address"))
        val stats = json.getJSONObject("chain_stats")
        val sats  = stats.getLong("funded_txo_sum") - stats.getLong("spent_txo_sum")
        return BalanceResult.Success(formatSatoshi(sats), "LTC")
    }

    private fun fetchDoge(address: String): BalanceResult {
        return runCatching {
            val json    = JSONObject(get("https://api.blockcypher.com/v1/doge/main/addrs/$address/balance"))
            val koinu   = json.getLong("balance")
            BalanceResult.Success(formatSatoshi(koinu), "DOGE")
        }.getOrElse {
            fetchBlockchair(address, "dogecoin", "DOGE")
        }
    }

    private fun fetchBchWithFallback(address: String): BalanceResult {
        val cleanAddr = address.removePrefix("bitcoincash:")

        runCatching { fetchBlockchair(cleanAddr, "bitcoin-cash", "BCH") }
            .onSuccess { if (it is BalanceResult.Success) return it }

        return runCatching {
            val json = JSONObject(get("https://api.haskoin.com/bch/address/$cleanAddr/balance"))
            check(!json.has("error")) { "Haskoin error" }
            val sats = json.getLong("confirmed")
            BalanceResult.Success(formatSatoshi(sats), "BCH")
        }.getOrElse { BalanceResult.Failure }
    }

    private fun fetchDashWithFallback(address: String): BalanceResult {
        runCatching { fetchBlockchair(address, "dash", "DASH") }
            .getOrNull()?.let { if (it is BalanceResult.Success) return it }
        return runCatching {
            val json = JSONObject(get("https://api.blockcypher.com/v1/dash/main/addrs/$address"))
            BalanceResult.Success(formatSatoshi(json.getLong("balance")), "DASH")
        }.getOrElse { BalanceResult.Failure }
    }

    private fun fetchZecWithFallback(address: String): BalanceResult {
        if (address.startsWith("zs") || address.startsWith("u1") || address.startsWith("zu")) {
            return BalanceResult.Failure
        }

        return runCatching { fetchBlockchair(address, "zcash", "ZEC") }
            .getOrElse { BalanceResult.Failure }
    }

    private fun fetchBlockchair(address: String, chain: String, symbol: String): BalanceResult {
        val cleanAddr = address.removePrefix("bitcoincash:")
        val json      = JSONObject(get("https://api.blockchair.com/$chain/dashboards/address/$cleanAddr"))

        val ctx = json.optJSONObject("context")
        val code = ctx?.optInt("code", 200) ?: 200
        if (code != 200) {
            val err = ctx?.optString("error", "Blockchair error $code") ?: "Blockchair error $code"
            error(err)
        }

        val dataObj = json.optJSONObject("data")
            ?: throw IllegalStateException("Sin datos de Blockchair ($symbol)")

        val addrObj = dataObj.optJSONObject(cleanAddr)
            ?: dataObj.optJSONObject("bitcoincash:$cleanAddr")
            ?: dataObj.keys().asSequence().firstOrNull()?.let { dataObj.optJSONObject(it) }
            ?: throw IllegalStateException("Dirección no encontrada en Blockchair ($symbol)")

        val sats = addrObj.getJSONObject("address").getLong("balance")
        return BalanceResult.Success(formatSatoshi(sats), symbol)
    }

    private fun fetchXrp(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        if (normalized.isNotEmpty() && normalized != "XRP") {
            return fetchXrpIssuedCurrency(address, normalized)
        }
        val body   = """{"method":"account_info","params":[{"account":"$address","ledger_index":"current"}]}"""
        val json   = JSONObject(post("https://xrplcluster.com", body))
        val result = json.getJSONObject("result")
        if (result.has("error")) return BalanceResult.Failure
        val drops = result.getJSONObject("account_data").getString("Balance").toLong()
        return BalanceResult.Success(formatDecimal(drops / 1_000_000.0), "XRP")
    }

    private fun fetchXrpIssuedCurrency(address: String, currency: String): BalanceResult {
        val body   = """{"method":"account_lines","params":[{"account":"$address","ledger_index":"current"}]}"""
        val json   = JSONObject(post("https://xrplcluster.com", body))
        val result = json.getJSONObject("result")
        if (result.has("error")) return BalanceResult.Failure
        val lines = result.getJSONArray("lines")
        var total = 0.0
        for (i in 0 until lines.length()) {
            val line = lines.getJSONObject(i)
            if (line.optString("currency").equals(currency, ignoreCase = true)) {
                total += line.getString("balance").toDoubleOrNull() ?: 0.0
            }
        }
        return BalanceResult.Success(formatDecimal(total.coerceAtLeast(0.0)), currency)
    }

    private fun fetchAda(address: String, asset: String = ""): BalanceResult {
        val body    = """{"_addresses":["$address"]}"""
        val jsonArr = JSONArray(post("https://api.koios.rest/api/v1/address_info", body))
        if (jsonArr.length() == 0) return BalanceResult.Success("0", "ADA")
        val obj = jsonArr.getJSONObject(0)

        val normalized = asset.trim()
        if (normalized.isNotEmpty() && normalized.uppercase() != "ADA") {
            val (unit, decimals) = CARDANO_ASSETS[normalized]
                ?: return BalanceResult.Failure
            val assetList = obj.optJSONArray("asset_list") ?: return BalanceResult.Success("0", normalized)
            for (i in 0 until assetList.length()) {
                val entry = assetList.getJSONObject(i)
                val entryUnit = entry.optString("policy_id") + entry.optString("asset_name")
                if (entryUnit.equals(unit, ignoreCase = true)) {
                    val qty = entry.getString("quantity").toBigDecimal()
                    val div = BigDecimal.TEN.pow(decimals)
                    val value = if (decimals == 0) qty else qty.divide(div, decimals, RoundingMode.HALF_DOWN)
                    return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
                }
            }
            return BalanceResult.Success("0", normalized)
        }

        val lovelace = obj.optString("balance", "0").toLong()
        return BalanceResult.Success(formatDecimal(lovelace / 1_000_000.0), "ADA")
    }

    private fun fetchTrx(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        val trc20Entry = if (normalized.isNotEmpty() && normalized != "TRX") TRC20[normalized] else null

        val json = JSONObject(get("https://api.trongrid.io/v1/accounts/$address"))
        val data = json.optJSONArray("data")
        if (data == null || data.length() == 0) {
            return if (trc20Entry != null) BalanceResult.Success("0", normalized)
            else BalanceResult.Success("0", "TRX")
        }
        val account = data.getJSONObject(0)

        if (trc20Entry != null) {
            val (contract, decimals) = trc20Entry
            val trc20Array = account.optJSONArray("trc20") ?: return BalanceResult.Success("0", normalized)
            for (i in 0 until trc20Array.length()) {
                val entry = trc20Array.getJSONObject(i)
                if (entry.has(contract)) {
                    val raw   = entry.getString(contract).toBigDecimal()
                    val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
                    return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
                }
            }
            return BalanceResult.Success("0", normalized)
        }

        val sun = account.optLong("balance", 0L)
        return BalanceResult.Success(formatDecimal(sun / 1_000_000.0), "TRX")
    }

    private fun fetchXlm(address: String, asset: String = ""): BalanceResult {
        val json     = JSONObject(get("https://horizon.stellar.org/accounts/$address"))
        val balances = json.getJSONArray("balances")

        val normalized = asset.trim().uppercase()
        val stellarEntry = if (normalized.isNotEmpty() && normalized != "XLM") STELLAR_ASSETS[normalized] else null

        for (i in 0 until balances.length()) {
            val entry = balances.getJSONObject(i)
            if (stellarEntry != null) {
                val (code, issuer) = stellarEntry
                if (entry.optString("asset_code") == code &&
                    entry.optString("asset_issuer").equals(issuer, ignoreCase = true)) {
                    return BalanceResult.Success(formatDecimal(entry.getString("balance").toDouble()), normalized)
                }
            } else {
                if (entry.optString("asset_type") == "native")
                    return BalanceResult.Success(formatDecimal(entry.getString("balance").toDouble()), "XLM")
            }
        }
        return BalanceResult.Success("0", if (stellarEntry != null) normalized else "XLM")
    }

    private fun fetchAlgo(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        val asaEntry   = if (normalized.isNotEmpty() && normalized != "ALGO") ALGORAND_ASAS[normalized] else null

        val json = JSONObject(get("https://mainnet-api.algonode.cloud/v2/accounts/$address"))

        if (asaEntry != null) {
            val (asaId, decimals) = asaEntry
            val assets = json.optJSONArray("assets") ?: return BalanceResult.Success("0", normalized)
            for (i in 0 until assets.length()) {
                val entry = assets.getJSONObject(i)
                if (entry.getLong("asset-id") == asaId) {
                    val raw   = entry.getLong("amount").toBigDecimal()
                    val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
                    return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
                }
            }
            return BalanceResult.Success("0", normalized)
        }

        val microAlgo = json.getLong("amount")
        return BalanceResult.Success(formatDecimal(microAlgo / 1_000_000.0), "ALGO")
    }

    private fun fetchHbar(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        val htsEntry   = if (normalized.isNotEmpty() && normalized != "HBAR") HEDERA_TOKENS[normalized] else null

        if (htsEntry != null) {
            val (tokenId, decimals) = htsEntry
            val json   = JSONObject(get("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts/$address/tokens?token.id=$tokenId"))
            val tokens = json.optJSONArray("tokens") ?: return BalanceResult.Success("0", normalized)
            if (tokens.length() == 0) return BalanceResult.Success("0", normalized)
            val raw   = tokens.getJSONObject(0).getLong("balance").toBigDecimal()
            val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
            return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
        }

        val json    = JSONObject(get("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts/$address"))
        val tinybar = json.getJSONObject("balance").getLong("balance")
        return BalanceResult.Success(formatDecimal(tinybar / 100_000_000.0), "HBAR")
    }

    private fun fetchXtz(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim()
        val faEntry    = if (normalized.isNotEmpty() && normalized.uppercase() != "XTZ") TEZOS_TOKENS[normalized] else null

        if (faEntry != null) {
            val (contract, tokenId, decimals) = faEntry
            val url  = "https://api.tzkt.io/v1/tokens/balances?account=$address&token.contract=$contract&token.tokenId=$tokenId"
            val arr  = JSONArray(get(url))
            if (arr.length() == 0) return BalanceResult.Success("0", normalized)
            val raw   = arr.getJSONObject(0).getString("balance").toBigDecimal()
            val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
            return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
        }

        val json  = JSONObject(get("https://api.tzkt.io/v1/accounts/$address"))
        val mutez = json.optLong("balance", 0L)
        return BalanceResult.Success(formatDecimal(mutez / 1_000_000.0), "XTZ")
    }

    private fun fetchAtom(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        val (chain, denom, divisor) = COSMOS_DENOMS[normalized]
            ?: COSMOS_DENOMS["ATOM"]!!
        val symbol = if (normalized.isEmpty() || normalized == "ATOM" || COSMOS_DENOMS[normalized] == null) "ATOM" else normalized

        return runCatching {
            val json     = JSONObject(get("https://rest.cosmos.directory/$chain/cosmos/bank/v1beta1/balances/$address"))
            val balances = json.optJSONArray("balances") ?: return@runCatching BalanceResult.Success("0", symbol)
            for (i in 0 until balances.length()) {
                val entry = balances.getJSONObject(i)
                if (entry.optString("denom") == denom)
                    return@runCatching BalanceResult.Success(formatDecimal(entry.getString("amount").toLong().toDouble() / divisor), symbol)
            }
            BalanceResult.Success("0", symbol)
        }.getOrElse {
            BalanceResult.Success("0", symbol)
        }
    }

    private fun fetchTon(address: String, asset: String = ""): BalanceResult {
        val normalized = asset.trim().uppercase()
        val jettonEntry = if (normalized.isNotEmpty() && normalized != "TON") TON_JETTONS[normalized] else null

        if (jettonEntry != null) {
            val (masterAddress, decimals) = jettonEntry
            val json     = JSONObject(get("https://tonapi.io/v2/accounts/$address/jettons"))
            val balances = json.optJSONArray("balances") ?: return BalanceResult.Success("0", normalized)
            for (i in 0 until balances.length()) {
                val entry  = balances.getJSONObject(i)
                val jetton = entry.getJSONObject("jetton")
                if (jetton.optString("address").equals(masterAddress, ignoreCase = true)) {
                    val raw   = entry.getString("balance").toBigDecimal()
                    val value = raw.divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.HALF_DOWN)
                    return BalanceResult.Success(formatDecimal(value.toDouble()), normalized)
                }
            }
            return BalanceResult.Success("0", normalized)
        }

        val json    = JSONObject(get("https://toncenter.com/api/v2/getAddressBalance?address=$address"))
        val nanoTon = json.getString("result").toBigDecimal()
        val value   = nanoTon.divide(BigDecimal("1000000000"))
        return BalanceResult.Success(formatDecimal(value.toDouble()), "TON")
    }

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = TIMEOUT_MS
        conn.readTimeout    = TIMEOUT_MS
        conn.setRequestProperty("Accept",     "application/json")
        conn.setRequestProperty("User-Agent", GENERIC_UA)
        val stream = if (conn.responseCode < 400) conn.inputStream
                     else (conn.errorStream ?: conn.inputStream)
        return stream.bufferedReader().readText().also { conn.disconnect() }
    }

    private fun post(url: String, body: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = TIMEOUT_MS
        conn.readTimeout    = TIMEOUT_MS
        conn.doOutput       = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept",       "application/json")
        conn.setRequestProperty("User-Agent",   GENERIC_UA)
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        val stream = if (conn.responseCode < 400) conn.inputStream
                     else (conn.errorStream ?: conn.inputStream)
        return stream.bufferedReader().readText().also { conn.disconnect() }
    }

    private fun formatSatoshi(satoshis: Long): String =
        formatDecimal(satoshis / 100_000_000.0)

    fun formatNominalDecimal(value: Double): String = formatDecimal(value)

    private fun formatDecimal(value: Double): String = when {
        value == 0.0     -> "0"
        value < 0.000001 -> "< 0.000001"
        else             -> String.format(java.util.Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
    }
}
