package com.thormakk.keyarmor.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoPriceFetcher @Inject constructor() {

    companion object {
        private const val TIMEOUT_MS = 10_000
        private const val BASE_URL   = "https://api.coingecko.com/api/v3/simple/price"

        private val TICKER_TO_GECKO_ID: Map<String, String> = mapOf(
            "BTC"   to "bitcoin",
            "ETH"   to "ethereum",
            "USDT"  to "tether",
            "BNB"   to "binancecoin",
            "SOL"   to "solana",
            "USDC"  to "usd-coin",
            "XRP"   to "ripple",
            "DOGE"  to "dogecoin",
            "ADA"   to "cardano",
            "AVAX"  to "avalanche-2",
            "SHIB"  to "shiba-inu",
            "DOT"   to "polkadot",
            "MATIC" to "polygon-ecosystem-token",
            "POL"   to "polygon-ecosystem-token",
            "LTC"   to "litecoin",
            "LINK"  to "chainlink",
            "BCH"   to "bitcoin-cash",
            "UNI"   to "uniswap",
            "ATOM"  to "cosmos",
            "XLM"   to "stellar",
            "TRX"   to "tron",
            "NEAR"  to "near",
            "FTM"   to "fantom",
            "ALGO"  to "algorand",
            "ICP"   to "internet-computer",
            "FIL"   to "filecoin",
            "GRT"   to "the-graph",
            "AAVE"  to "aave",
            "ARB"   to "arbitrum",
            "OP"    to "optimism",
            "DAI"   to "dai",
            "MKR"   to "maker",
            "COMP"  to "compound-governance-token",
            "CRV"   to "curve-dao-token",
            "SNX"   to "havven",
            "SUSHI" to "sushi",
            "YFI"   to "yearn-finance",
            "1INCH" to "1inch",
            "LDO"   to "lido-dao",
            "SAND"  to "the-sandbox",
            "MANA"  to "decentraland",
            "AXS"   to "axie-infinity",
            "ENJ"   to "enjincoin",
            "GALA"  to "gala",
            "IMX"   to "immutable-x",
            "APE"   to "apecoin",
            "CHZ"   to "chiliz",
            "VET"   to "vechain",
            "EOS"   to "eos",
            "XTZ"   to "tezos",
            "XMR"   to "monero",
            "ZEC"   to "zcash",
            "THETA" to "theta-token",
            "FLOW"  to "flow",
            "KSM"   to "kusama",
            "EGLD"  to "elrond-erd-2",
            "HBAR"  to "hedera-hashgraph",
            "CAKE"  to "pancakeswap-token",
            "WBTC"  to "wrapped-bitcoin",
            "STETH" to "staked-ether",
            "BSV"   to "bitcoin-cash-sv",
            "BTT"   to "bittorrent",
            "BUSD"  to "binance-usd",
            "FRAX"  to "frax",
            "TUSD"  to "true-usd",
            "USDD"  to "usdd",
            "BAL"   to "balancer",
            "TON"   to "the-open-network",
            "SUI"   to "sui",
            "APT"   to "aptos",
            "PEPE"  to "pepe",
            "RNDR"  to "render-token",
            "INJ"   to "injective-protocol",
            "WIF"   to "dogwifcoin",
            "BONK"  to "bonk",
            "JUP"   to "jupiter-exchange-solana",
            "TIA"   to "celestia",
            "RUNE"  to "thorchain",
            "FET"   to "fetch-ai",
            "STX"   to "blockstack",
            "DASH"  to "dash",
            "KAVA"  to "kava",
            "ROSE"  to "oasis-network",
            "ZRX"   to "0x",
            "BAT"   to "basic-attention-token",
            "ONE"   to "harmony",
            "ONDO"  to "ondo-finance",
            "HNT"   to "helium",
            "MINA"  to "mina-protocol",
            "PYTH"  to "pyth-network",
            "PAXG"  to "pax-gold",
            "WETH"  to "weth",
            "CELO"  to "celo",
            "GLMR"  to "moonbeam",
            "ETC"   to "ethereum-classic"
        )
    }

    private val cachedPrices = mutableMapOf<String, Double>()

    fun getCachedPrice(ticker: String): Double? = cachedPrices[ticker.uppercase()]

    suspend fun fetchPrices(tickers: Set<String>, currency: String): Map<String, Double> =
        withContext(Dispatchers.IO) {
            try {
                val tickerToId = tickers.mapNotNull { ticker ->
                    val id = TICKER_TO_GECKO_ID[ticker.uppercase()]
                    if (id != null) ticker.uppercase() to id else null
                }.toMap()

                if (tickerToId.isEmpty()) return@withContext emptyMap()

                val ids = tickerToId.values.joinToString(",")
                val cur = currency.lowercase()
                val urlStr = "$BASE_URL?ids=$ids&vs_currencies=$cur"

                val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod    = "GET"
                    connectTimeout   = TIMEOUT_MS
                    readTimeout      = TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) return@withContext emptyMap()

                val body = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(body)

                val result = mutableMapOf<String, Double>()
                for ((ticker, geckoId) in tickerToId) {
                    if (json.has(geckoId)) {
                        val priceObj = json.getJSONObject(geckoId)
                        if (priceObj.has(cur)) {
                            result[ticker] = priceObj.getDouble(cur)
                        }
                    }
                }
                cachedPrices.putAll(result)
                result
            } catch (_: Exception) {
                emptyMap()
            }
        }
}
