package com.thormakk.keyarmor.presentation.screens.crypto

import com.thormakk.keyarmor.presentation.screens.crypto.CryptoNetworkAssetRegistry.assetToNetworks
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoNetworkAssetRegistry.networkToAssets


object CryptoNetworkAssetRegistry {

    private val networkToAssets: Map<String, Set<String>> = mapOf(

        "Bitcoin (BTC)"      to setOf("BTC"),
        "Bitcoin Cash (BCH)" to setOf("BCH"),
        "Litecoin (LTC)"     to setOf("LTC"),
        "Dogecoin (DOGE)"    to setOf("DOGE"),
        "Dash (DASH)"        to setOf("DASH"),
        "Zcash (ZEC)"        to setOf("ZEC"),

        "Ethereum (ETH)" to setOf(
            "ETH", "USDC", "USDT", "DAI", "WBTC", "WETH",
            "LINK", "UNI", "AAVE", "SHIB", "PEPE", "PAXG", "STETH",
            "CRV", "MKR", "COMP", "SNX", "SUSHI", "YFI", "LDO", "BAL",
            "ZRX", "BAT", "ONDO", "GRT", "APE", "IMX", "MANA", "SAND",
            "AXS", "GALA", "ENJ", "CHZ", "FRAX", "1INCH", "FET", "RNDR"
        ),
        "Arbitrum One (ETH)" to setOf(
            "ETH", "ARB", "USDC", "USDT", "DAI", "WBTC", "WETH", "LINK"
        ),
        "Optimism (ETH)" to setOf(
            "ETH", "OP", "USDC", "USDT", "DAI", "WBTC", "WETH", "LINK"
        ),
        "Base (ETH)" to setOf(
            "ETH", "USDC", "USDT", "DAI", "WETH", "WBTC"
        ),
        "zkSync Era (ETH)" to setOf(
            "ETH", "USDC", "USDT", "WBTC", "WETH"
        ),
        "Scroll (ETH)" to setOf(
            "ETH", "USDC", "USDT"
        ),
        "Fantom (FTM)" to setOf(
            "FTM", "USDC", "USDT", "DAI", "WBTC", "WETH"
        ),
        "Gnosis (xDAI)" to setOf(
            "DAI", "USDC", "USDT", "WBTC", "WETH"
        ),

        "BNB Chain (BNB)" to setOf(
            "BNB", "USDC", "USDT", "DAI", "WBTC", "WETH", "CAKE", "BUSD"
        ),

        "Polygon (MATIC)" to setOf(
            "MATIC", "POL", "USDC", "USDT", "DAI", "WBTC", "WETH",
            "LINK", "SAND", "MANA"
        ),

        "Avalanche (AVAX)" to setOf(
            "AVAX", "USDC", "USDT", "DAI", "WBTC", "WETH"
        ),

        "Solana (SOL)" to setOf(
            "SOL", "USDC", "USDT", "WBTC", "WETH",
            "BONK", "JUP", "HNT", "PYTH", "WIF", "RNDR"
        ),

        "Tron (TRX)" to setOf(
            "TRX", "USDT", "USDC", "TUSD", "BTT", "USDD"
        ),

        "Cardano (ADA)" to setOf("ADA"),

        "Ripple (XRP)" to setOf("XRP"),

        "Stellar (XLM)" to setOf(
            "XLM", "USDC", "USDT"
        ),

        "Algorand (ALGO)" to setOf(
            "ALGO", "USDC", "USDT", "WBTC", "WETH"
        ),

        "Hedera (HBAR)" to setOf(
            "HBAR", "USDC", "USDT"
        ),

        "Tezos (XTZ)" to setOf(
            "XTZ", "USDC", "USDT", "WBTC", "WETH"
        ),

        "Cosmos (ATOM)" to setOf(
            "ATOM", "KAVA"
        ),

        "TON (TON)" to setOf(
            "TON", "USDT", "USDC", "WBTC", "WETH"
        )

    )

    private val assetToNetworks: Map<String, Set<String>> by lazy {
        buildMap<String, MutableSet<String>> {
            networkToAssets.forEach { (network, tickers) ->
                tickers.forEach { ticker ->
                    getOrPut(ticker) { mutableSetOf() }.add(network)
                }
            }
        }.mapValues { it.value.toSet() }
    }

    fun networksForAsset(ticker: String): Set<String> =
        assetToNetworks[ticker.trim().uppercase()] ?: emptySet()

    fun assetsForNetwork(network: String): Set<String> =
        networkToAssets[network] ?: emptySet()
}
