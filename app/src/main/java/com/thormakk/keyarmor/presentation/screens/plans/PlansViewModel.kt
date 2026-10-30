package com.thormakk.keyarmor.presentation.screens.plans

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.billing.BillingEvent
import com.thormakk.keyarmor.data.billing.BillingProductIds
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.drive.GoogleIdentityVerifier
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.presentation.util.establishFirebaseSession
import com.thormakk.keyarmor.presentation.util.registerFcmTokenFromLogin
import com.thormakk.keyarmor.data.prefs.UserPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlansUiState(
    val currentPlan: UserPlan = UserPlan.FREE,

    val premiumMonthlyPrice: String? = null,
    val premiumYearlyPrice:  String? = null,
    val premiumYearlySaving: String? = null,
    val familyYearlyPrice:   String? = null,

    val selectedBillingPeriod: BillingPeriod = BillingPeriod.MONTHLY,

    val isBillingReady: Boolean = false,

    val snackbarMessage: String? = null,

    val isPurchasing: Boolean = false,

    val hasConnectedAccount: Boolean = false
)

enum class BillingPeriod { MONTHLY, YEARLY }

@HiltViewModel
class PlansViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val rcManager:        RevenueCatManager,
    private val planPrefs:        PlanPrefs,
    private val drivePrefs:       DrivePrefs,
    private val identityVerifier: GoogleIdentityVerifier,
    private val fcmRepository:    FcmRepository,
    private val fcmPrefs:         FcmPrefs
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    private var monthlyPackage:      Package? = null
    private var yearlyPackage:       Package? = null
    private var familyYearlyPackage: Package? = null

    init {
        observePlan()
        observeOfferings()
        observeBillingEvents()
        observeConnection()
        refreshConnectedAccount()
    }

    fun refreshConnectedAccount() {
        _uiState.update { it.copy(hasConnectedAccount = drivePrefs.getConnectedEmail() != null) }
    }

    private fun observePlan() {
        viewModelScope.launch {
            planPrefs.planFlow.collect { plan ->
                _uiState.update { it.copy(currentPlan = plan) }
            }
        }
    }

    private fun observeOfferings() {
        viewModelScope.launch {
            rcManager.offerings.collect { offerings ->
                if (offerings == null) return@collect

                val packages = offerings.current?.availablePackages
                    ?: offerings.all.values.flatMap { it.availablePackages }

                monthlyPackage      = packages.firstOrNull { it.product.id.startsWith(BillingProductIds.PREMIUM_MONTHLY) }
                yearlyPackage       = packages.firstOrNull { it.product.id.startsWith(BillingProductIds.PREMIUM_YEARLY) }
                familyYearlyPackage = packages.firstOrNull { it.product.id.startsWith(BillingProductIds.FAMILY_YEARLY) }

                _uiState.update { state ->
                    state.copy(
                        premiumMonthlyPrice = monthlyPackage?.product?.price?.formatted,
                        premiumYearlyPrice  = yearlyPackage?.product?.price?.formatted,
                        familyYearlyPrice   = familyYearlyPackage?.product?.price?.formatted,
                        premiumYearlySaving = calculateSaving()
                    )
                }
            }
        }
    }

    private fun observeBillingEvents() {
        viewModelScope.launch {
            rcManager.billingEvents.collect { event ->
                when (event) {
                    is BillingEvent.PurchaseSuccess -> {
                        _uiState.update { it.copy(
                            isPurchasing    = false,
                            snackbarMessage = context.getString(R.string.plans_purchase_success)
                        ) }
                    }
                    is BillingEvent.PurchaseError -> {
                        _uiState.update { it.copy(
                            isPurchasing    = false,
                            snackbarMessage = event.message
                        ) }
                    }
                    is BillingEvent.PurchaseCancelled -> {
                        _uiState.update { it.copy(isPurchasing = false) }
                    }
                }
            }
        }
    }

    private fun observeConnection() {
        viewModelScope.launch {
            rcManager.isReady.collect { ready ->
                _uiState.update { it.copy(isBillingReady = ready) }
            }
        }
    }

    fun toggleBillingPeriod(period: BillingPeriod) {
        _uiState.update { it.copy(selectedBillingPeriod = period) }
    }

    suspend fun connectAccount(email: String, idToken: String): Boolean {
        val verified = identityVerifier.verify(idToken) ?: run {
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.error_google_verification_failed)) }
            return false
        }
        val verifiedEmail = verified.email ?: email
        drivePrefs.saveConnectedEmail(verifiedEmail)
        drivePrefs.saveOwnerGoogleSub(verified.sub)

        rcManager.linkIdentity(verified.sub)

        viewModelScope.launch { establishFirebaseSession(idToken) }

        viewModelScope.launch { registerFcmTokenFromLogin(idToken, fcmRepository, fcmPrefs) }

        _uiState.update { it.copy(hasConnectedAccount = true) }
        return true
    }

    fun purchasePremium(activity: Activity) {
        val pkg = when (_uiState.value.selectedBillingPeriod) {
            BillingPeriod.MONTHLY -> monthlyPackage
            BillingPeriod.YEARLY  -> yearlyPackage
        } ?: run {
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.billing_error_product_unavailable)) }
            return
        }
        _uiState.update { it.copy(isPurchasing = true) }
        rcManager.purchasePackage(activity, pkg)
    }

    fun purchaseFamily(activity: Activity) {
        val pkg = familyYearlyPackage ?: run {
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.billing_error_product_unavailable)) }
            return
        }
        _uiState.update { it.copy(isPurchasing = true) }
        rcManager.purchasePackage(activity, pkg)
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun calculateSaving(): String? {
        val monthlyMicros  = monthlyPackage?.product?.price?.amountMicros  ?: return null
        val yearlyMicros   = yearlyPackage?.product?.price?.amountMicros   ?: return null
        if (monthlyMicros <= 0L) return null
        val saving = ((monthlyMicros * 12L - yearlyMicros).toDouble() / (monthlyMicros * 12L) * 100).toInt()
        return if (saving > 0) context.getString(R.string.plans_saving_label, saving) else null
    }
}
