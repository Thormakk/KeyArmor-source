package com.thormakk.keyarmor.presentation.screens.donate

import androidx.annotation.DrawableRes
import com.thormakk.keyarmor.R

object DonationAddresses {

    const val KOFI_URL = "https://ko-fi.com/thormakk"

    data class CryptoOption(
        val coin: String,
        val network: String,
        val address: String,
        @DrawableRes val iconRes: Int?,
        val brandColorArgb: Long,
        val symbol: String,
    )

    val OPTIONS: List<CryptoOption> = listOf(
        CryptoOption(
            coin = "USDT",
            network = "red TRON (TRC20)",
            address = "TRBEfk1zZRbWmrBLzixUrYy8QRZN3ka9mc",
            iconRes = R.drawable.ic_crypto_usdt,
            brandColorArgb = 0xFF26A17BL,
            symbol = "USDT",
        ),
        CryptoOption(
            coin = "SOL",
            network = "",
            address = "DsVTnv6ynYFkE3skEUktvUoupTbg95a6pffXjayfvcxP",
            iconRes = R.drawable.ic_crypto_sol,
            brandColorArgb = 0xFF9945FFL,
            symbol = "◎",
        ),
        CryptoOption(
            coin = "BTC",
            network = "",
            address = "bc1qtjrfa2qupee3xncfqla4jj95jtydecwfm0nrt8",
            iconRes = R.drawable.ic_crypto_btc,
            brandColorArgb = 0xFFF7931AL,
            symbol = "₿",
        ),
    )

    fun available(): List<CryptoOption> = OPTIONS.filter { it.address.isNotBlank() }
}
