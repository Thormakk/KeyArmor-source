package com.thormakk.keyarmor.data.billing

object BillingProductIds {
    const val PREMIUM_MONTHLY = "keyarmor_premium_monthly"
    const val PREMIUM_YEARLY  = "keyarmor_premium_yearly"
    const val FAMILY_YEARLY   = "keyarmor_family_yearly"
}

sealed class BillingEvent {
    object PurchaseSuccess : BillingEvent()
    data class PurchaseError(val message: String) : BillingEvent()
    object PurchaseCancelled : BillingEvent()
}
