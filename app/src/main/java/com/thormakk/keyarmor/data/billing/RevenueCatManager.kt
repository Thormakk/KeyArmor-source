package com.thormakk.keyarmor.data.billing

import android.app.Activity
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.repository.InheritanceDisarmer
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatManager @Inject constructor(
    private val planPrefs:   PlanPrefs,
    private val drivePrefs:  DrivePrefs,
    private val familyPrefs: FamilyPrefs,
    private val inheritancePrefs: InheritancePrefs,
    private val inheritanceDisarmer: InheritanceDisarmer
) {

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings

    private val _billingEvents = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 1)
    val billingEvents: SharedFlow<BillingEvent> = _billingEvents

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    fun initialize() {
        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { customerInfo ->
                syncPlan(customerInfo)
            }
        loadCustomerInfo()
        loadOfferings()
    }

    private fun loadCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                syncPlan(customerInfo)
                _isReady.value = true
            }
            override fun onError(error: PurchasesError) {
                _isReady.value = true
            }
        })
    }

    fun loadOfferings() {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _offerings.value = offerings
                if (!_isReady.value) _isReady.value = true
            }
            override fun onError(error: PurchasesError) {
                if (!_isReady.value) _isReady.value = true
            }
        })
    }

    fun purchasePackage(activity: Activity, rcPackage: Package) {
        val params = PurchaseParams.Builder(activity, rcPackage).build()
        Purchases.sharedInstance.purchase(params, object : PurchaseCallback {
            override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                syncPlan(customerInfo)
                _billingEvents.tryEmit(BillingEvent.PurchaseSuccess)
            }
            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                if (userCancelled) {
                    _billingEvents.tryEmit(BillingEvent.PurchaseCancelled)
                } else {
                    _billingEvents.tryEmit(BillingEvent.PurchaseError(error.message))
                }
            }
        })
    }

    fun linkIdentity(sub: String, onResult: (Boolean) -> Unit = {}) {
        Purchases.sharedInstance.logIn(sub, object : LogInCallback {
            override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                syncPlan(customerInfo)
                onResult(true)
            }
            override fun onError(error: PurchasesError) {
                onResult(false)
            }
        })
    }

    fun unlinkIdentity(onResult: (Boolean) -> Unit = {}) {
        Purchases.sharedInstance.logOut(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                syncPlan(customerInfo)
                onResult(true)
            }
            override fun onError(error: PurchasesError) {
                onResult(false)
            }
        })
    }

    private fun syncPlan(customerInfo: CustomerInfo) {
        val previousPlan = planPrefs.getUserPlan()
        val entitlement  = customerInfo.entitlements[ENTITLEMENT_ID]
        val plan = when {
            entitlement?.isActive == true -> when (entitlement.productIdentifier) {
                BillingProductIds.FAMILY_YEARLY -> UserPlan.FAMILY
                else                            -> UserPlan.PREMIUM
            }
            else -> UserPlan.FREE
        }

        if (previousPlan == UserPlan.FAMILY && plan != UserPlan.FAMILY) {
            val emails = familyPrefs.getMemberEmails()
            if (emails.isNotEmpty()) {
                familyPrefs.pendingRevokeEmails       = emails
                familyPrefs.pendingMemberPremiumRevoke = true
                familyPrefs.clearAdminMemberList()
            }
        }

        planPrefs.setUserPlan(plan)

        if (entitlement?.isActive == true) {
            planPrefs.setSubscriptionInfo(
                purchaseDateMs     = entitlement.originalPurchaseDate.time,
                expirationDateMs   = entitlement.expirationDate?.time,
                willRenew          = entitlement.willRenew,
                productIdentifier  = entitlement.productIdentifier
            )
        } else {
            planPrefs.clearSubscriptionInfo()
        }

        if (plan == UserPlan.FREE
            && previousPlan != UserPlan.FREE
            && !familyPrefs.isMember()
            && drivePrefs.getConnectedEmail() != null
        ) {
            if (inheritancePrefs.isEnabled() || inheritancePrefs.isPendingDisarm()) {
                inheritanceDisarmer.requestDisarmOnDowngrade()
            } else {
                drivePrefs.clear()
            }
        }

    }

    companion object {
        private const val TAG            = "RevenueCatManager"
        const val ENTITLEMENT_ID         = "KeyArmor"
    }
}
