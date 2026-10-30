package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class UserPlan { FREE, PREMIUM, FAMILY }

@Singleton
class PlanPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val familyPrefs: FamilyPrefs,
    private val drivePrefs: DrivePrefs
) {
    private val prefs = context.getSharedPreferences("plan_prefs", Context.MODE_PRIVATE)

    private val _planFlow  = MutableStateFlow(effectivePlan())
    val planFlow: StateFlow<UserPlan> = _planFlow

    var onDowngradeToFree: (() -> Unit)? = null

    fun getUserPlan(): UserPlan {
        val name = prefs.getString(KEY_PLAN, UserPlan.FREE.name) ?: UserPlan.FREE.name
        return runCatching { UserPlan.valueOf(name) }.getOrDefault(UserPlan.FREE)
    }

    fun setUserPlan(plan: UserPlan) {
        prefs.edit { putString(KEY_PLAN, plan.name) }
        refresh()
    }

    fun effectivePlan(): UserPlan {
        val hasConnectedAccount = drivePrefs.getConnectedEmail() != null

        val purchased = getUserPlan()
        if (purchased != UserPlan.FREE) {
            return if (hasConnectedAccount) purchased else UserPlan.FREE
        }

        if (familyPrefs.isMember() && familyPrefs.isPremiumMember && hasConnectedAccount) {
            return UserPlan.PREMIUM
        }

        return UserPlan.FREE
    }

    fun setSubscriptionInfo(
        purchaseDateMs: Long,
        expirationDateMs: Long?,
        willRenew: Boolean,
        productIdentifier: String
    ) {
        prefs.edit {
            putLong(KEY_SUB_PURCHASE_DATE, purchaseDateMs)
            if (expirationDateMs != null) putLong(KEY_SUB_EXPIRATION_DATE, expirationDateMs)
            else                          remove(KEY_SUB_EXPIRATION_DATE)
            putBoolean(KEY_SUB_WILL_RENEW, willRenew)
            putString(KEY_SUB_PRODUCT_ID, productIdentifier)
        }
    }

    fun clearSubscriptionInfo() {
        prefs.edit {
            remove(KEY_SUB_PURCHASE_DATE)
            remove(KEY_SUB_EXPIRATION_DATE)
            remove(KEY_SUB_WILL_RENEW)
            remove(KEY_SUB_PRODUCT_ID)
        }
    }

    fun getSubscriptionPurchaseDate(): Long = prefs.getLong(KEY_SUB_PURCHASE_DATE, -1L)

    fun getSubscriptionExpirationDate(): Long? =
        prefs.getLong(KEY_SUB_EXPIRATION_DATE, -1L).takeIf { it > 0L }

    fun getSubscriptionWillRenew(): Boolean = prefs.getBoolean(KEY_SUB_WILL_RENEW, false)

    fun getSubscriptionProductId(): String? = prefs.getString(KEY_SUB_PRODUCT_ID, null)

    fun refresh() {
        val previousPlan = _planFlow.value
        _planFlow.value          = effectivePlan()
        if (previousPlan != UserPlan.FREE && _planFlow.value == UserPlan.FREE) {
            onDowngradeToFree?.invoke()
        }
    }

    companion object {
        private const val KEY_PLAN        = "user_plan"

        private const val KEY_SUB_PURCHASE_DATE    = "sub_purchase_date"
        private const val KEY_SUB_EXPIRATION_DATE  = "sub_expiration_date"
        private const val KEY_SUB_WILL_RENEW       = "sub_will_renew"
        private const val KEY_SUB_PRODUCT_ID       = "sub_product_id"
    }
}
