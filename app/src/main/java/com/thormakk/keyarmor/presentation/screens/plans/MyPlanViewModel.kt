package com.thormakk.keyarmor.presentation.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.billing.BillingProductIds
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SubscriptionPeriod { MONTHLY, YEARLY }

data class MyPlanUiState(
    val currentPlan: UserPlan = UserPlan.FREE,

    val purchaseDateMs: Long? = null,

    val expirationDateMs: Long? = null,

    val willRenew: Boolean = false,

    val period: SubscriptionPeriod? = null,

    val currentPrice: String? = null,

    val productId: String? = null,

    val isBillingReady: Boolean = false,

    val driveAccount: String? = null
)

@HiltViewModel
class MyPlanViewModel @Inject constructor(
    private val planPrefs:  PlanPrefs,
    private val rcManager:  RevenueCatManager,
    private val drivePrefs: DrivePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPlanUiState())
    val uiState: StateFlow<MyPlanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            planPrefs.planFlow.collect { plan -> loadSubscriptionMeta(plan) }
        }
        viewModelScope.launch {
            rcManager.offerings.collect { resolveCurrentPrice() }
        }
        viewModelScope.launch {
            rcManager.isReady.collect { ready -> _uiState.update { it.copy(isBillingReady = ready) } }
        }
        refreshConnectedAccount()
    }

    fun refreshConnectedAccount() {
        _uiState.update { it.copy(driveAccount = drivePrefs.getConnectedEmail()) }
    }

    private fun loadSubscriptionMeta(plan: UserPlan) {
        val productId = planPrefs.getSubscriptionProductId()
        val period = when {
            productId == null -> null
            productId.startsWith(BillingProductIds.PREMIUM_YEARLY) ||
                productId.startsWith(BillingProductIds.FAMILY_YEARLY)  -> SubscriptionPeriod.YEARLY
            productId.startsWith(BillingProductIds.PREMIUM_MONTHLY)    -> SubscriptionPeriod.MONTHLY
            else -> null
        }
        _uiState.update {
            it.copy(
                currentPlan      = plan,
                purchaseDateMs   = planPrefs.getSubscriptionPurchaseDate().takeIf { ms -> ms > 0L },
                expirationDateMs = planPrefs.getSubscriptionExpirationDate(),
                willRenew        = planPrefs.getSubscriptionWillRenew(),
                period           = period,
                productId        = productId
            )
        }
        resolveCurrentPrice()
    }

    private fun resolveCurrentPrice() {
        val productId = _uiState.value.productId ?: return
        val offerings = rcManager.offerings.value ?: return
        val packages = offerings.current?.availablePackages
            ?: offerings.all.values.flatMap { it.availablePackages }
        val pkg = packages.firstOrNull { it.product.id == productId }
            ?: packages.firstOrNull { it.product.id.startsWith(productId) }
        _uiState.update { it.copy(currentPrice = pkg?.product?.price?.formatted) }
    }
}
