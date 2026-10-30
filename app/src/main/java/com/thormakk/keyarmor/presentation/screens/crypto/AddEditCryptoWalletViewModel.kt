package com.thormakk.keyarmor.presentation.screens.crypto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.CryptoPortfolioPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.remote.BalanceResult
import com.thormakk.keyarmor.data.remote.CachedWalletBalance
import com.thormakk.keyarmor.data.remote.CryptoBalanceCache
import com.thormakk.keyarmor.data.remote.CryptoBalanceFetcher
import com.thormakk.keyarmor.data.remote.CryptoPriceFetcher
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class BalanceUiState {
    object Idle             : BalanceUiState()
    object Loading          : BalanceUiState()
    data class Ready(val display: String) : BalanceUiState()
    object NotSupported     : BalanceUiState()
    object Failed           : BalanceUiState()
    data class ManualOverride(val display: String) : BalanceUiState()
}

data class AddEditCryptoWalletUiState(
    val id:         String        = "",
    val network:    String        = "",
    val asset:      String        = "",
    val compatibleNetworks: Set<String> = emptySet(),
    val compatibleAssets:   Set<String> = emptySet(),
    val address:    String        = "",
    val seedPhrase: String        = "",
    val privateKey: String        = "",
    val notes:           String        = "",
    val manualBalance:   String        = "",
    val linkedPasswordId: String        = "",
    val category:   EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:       List<String>  = emptyList(),
    val isFavorite:   Boolean        = false,
    val isProtected:  Boolean        = false,
    val isTravelSafe: Boolean        = false,
    val isPinned:     Boolean       = false,
    val isHidden:     Boolean       = false,
    val isNew:             Boolean        = true,
    val isSaved:           Boolean        = false,
    val isDeleted:         Boolean        = false,
    val balanceState:      BalanceUiState = BalanceUiState.Idle,
    val fiatValue:         Double?        = null,
    val fiatCurrency:      String         = "USD",
    val createdAt:         Long           = System.currentTimeMillis(),
    val updatedAt:         Long           = System.currentTimeMillis(),
    val inheritedFrom:     String?        = null
)

@HiltViewModel
class AddEditCryptoWalletViewModel @Inject constructor(
    private val repository:       CryptoWalletRepository,
    private val tagRepository:    TagRepository,
    private val authRepository:   AuthRepository,
    vaultRepository:  VaultRepository,
    private val balanceFetcher:   CryptoBalanceFetcher,
    private val priceFetcher:     CryptoPriceFetcher,
    private val balanceCache:     CryptoBalanceCache,
    private val portfolioPrefs:   CryptoPortfolioPrefs,
    private val planPrefs:        PlanPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:             SavedStateHandle
) : ViewModel() {

    private val walletId: String = savedStateHandle["id"] ?: "new"
    private val cloneOf:  String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditCryptoWalletUiState())
    val uiState: StateFlow<AddEditCryptoWalletUiState> = _uiState

    private var originalSeedPhrase: String = ""
    private var originalPrivateKey: String = ""

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)
    val isReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE &&
        activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
    )

    val isPremium: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    val vaultEntries: StateFlow<List<VaultEntry>> = vaultRepository.observeEntries()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                repository.getWallet(cloneOf)?.let { src ->
                    _uiState.value = AddEditCryptoWalletUiState(
                        id           = UUID.randomUUID().toString(),
                        network      = src.network,
                        asset        = src.asset,
                        address      = src.address,
                        seedPhrase   = src.seedPhrase,
                        privateKey   = src.privateKey,
                        notes             = src.notes,
                        linkedPasswordId  = src.linkedPasswordId,
                        category     = src.category,
                        tags         = src.tags,
                        isFavorite   = false,
                        isTravelSafe = src.isTravelSafe,
                        isNew        = true
                    )
                }
            }
            walletId != "new" -> viewModelScope.launch {
                repository.observeWallet(walletId)
                    .filterNotNull()
                    .collect { w ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect

                        originalSeedPhrase = w.seedPhrase
                        originalPrivateKey = w.privateKey

                        val shouldRefreshBalance =
                            current.balanceState is BalanceUiState.Idle ||
                            current.network  != w.network ||
                            current.address  != w.address ||
                            current.asset    != w.asset

                        _uiState.value = current.copy(
                            id               = w.id,
                            network          = w.network,
                            asset            = w.asset,
                            address          = w.address,
                            seedPhrase       = w.seedPhrase,
                            privateKey       = w.privateKey,
                            notes            = w.notes,
                            manualBalance    = w.manualBalance,
                            linkedPasswordId = w.linkedPasswordId,
                            category         = w.category,
                            tags             = w.tags,
                            isFavorite       = w.isFavorite,
                            isProtected      = w.isProtected,
                            isTravelSafe     = w.isTravelSafe,
                            isPinned         = w.isPinned,
                            isHidden         = w.isHidden,
                            isNew            = false,
                            createdAt        = w.createdAt,
                            updatedAt        = w.updatedAt,
                            inheritedFrom    = w.inheritedFrom,
                            balanceState     = if (w.manualBalance.isNotEmpty())
                                BalanceUiState.ManualOverride("${formatDisplayAmount(w.manualBalance)} ${w.asset}")
                            else current.balanceState
                        )

                        if (shouldRefreshBalance) refreshBalance()
                    }
            }
        }
    }

    fun refreshBalance() {
        val s = _uiState.value

        if (s.manualBalance.isNotEmpty()) {
            val ticker = s.asset.uppercase().ifEmpty { nativeSymbolForNetwork(s.network) }
            val currency = portfolioPrefs.getSelectedCurrency()
            val nominal = s.manualBalance.toDoubleOrNull()
            viewModelScope.launch {
                val fiatValue = if (nominal != null && ticker.isNotBlank()) {
                    val prices = priceFetcher.fetchPrices(setOf(ticker), currency)
                    prices[ticker]?.let { it * nominal }
                } else null
                _uiState.value = _uiState.value.copy(
                    balanceState = BalanceUiState.ManualOverride("${formatDisplayAmount(s.manualBalance)} ${s.asset}"),
                    fiatValue    = fiatValue,
                    fiatCurrency = currency
                )
                if (walletId != "new") {
                    balanceCache.put(walletId, CachedWalletBalance(
                        nominalBalance = s.manualBalance,
                        fiatValue      = fiatValue,
                        isManual       = true,
                        manualBalance  = s.manualBalance
                    ))
                }
            }
            return
        }

        if (s.address.isBlank() || s.network.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(balanceState = BalanceUiState.Loading)

            if (s.network.startsWith("Solana") && s.asset.isBlank()) {
                val detected = balanceFetcher.detectToken(s.network, s.address)
                if (detected != null) {
                    _uiState.value = _uiState.value.copy(asset = detected)
                }
            }

            val current = _uiState.value
            val result  = balanceFetcher.fetch(current.network, current.address, current.asset)

            val currency = portfolioPrefs.getSelectedCurrency()
            val fiatValue: Double? = if (result is BalanceResult.Success) {
                val ticker = current.asset.uppercase()
                    .ifEmpty { result.symbol.uppercase() }
                    .ifEmpty { nativeSymbolForNetwork(current.network) }
                val nominal = result.amount.removePrefix("< ").toDoubleOrNull()
                if (ticker.isNotBlank() && nominal != null) {
                        val price = priceFetcher.getCachedPrice(ticker)
                        ?: priceFetcher.fetchPrices(setOf(ticker), currency)[ticker]
                    price?.let { it * nominal }
                } else null
            } else null

            _uiState.value = _uiState.value.copy(
                balanceState = when (result) {
                    is BalanceResult.Success     -> BalanceUiState.Ready("${formatDisplayAmount(result.amount)} ${result.symbol}")
                    is BalanceResult.Unsupported -> BalanceUiState.NotSupported
                    is BalanceResult.Failure     -> BalanceUiState.Failed
                },
                fiatValue    = fiatValue,
                fiatCurrency = currency
            )
            if (walletId != "new" && result is BalanceResult.Success) {
                balanceCache.put(walletId, CachedWalletBalance(
                    nominalBalance = result.amount,
                    fiatValue      = fiatValue,
                    isManual       = false,
                    manualBalance  = current.manualBalance
                ))
            }
        }
    }

    fun onNetworkChange(v: String) {
        val compatible = CryptoNetworkAssetRegistry.assetsForNetwork(v)
        _uiState.value = _uiState.value.copy(
            network          = v,
            compatibleAssets = compatible,
            asset = if (compatible.isEmpty() || _uiState.value.asset.uppercase() in compatible)
                        _uiState.value.asset
                    else ""
        )
    }
    fun onAssetChange(v: String) {
        val ticker     = v.trim().uppercase()
        val compatible = CryptoNetworkAssetRegistry.networksForAsset(ticker)
        _uiState.value = _uiState.value.copy(
            asset              = v,
            compatibleNetworks = compatible,
            network = if (compatible.isEmpty() || _uiState.value.network in compatible)
                          _uiState.value.network
                      else ""
        )
    }
    fun onAddressChange(v: String)    { _uiState.value = _uiState.value.copy(address = v) }
    fun onSeedPhraseChange(v: String) { _uiState.value = _uiState.value.copy(seedPhrase = v) }
    fun onPrivateKeyChange(v: String) { _uiState.value = _uiState.value.copy(privateKey = v) }
    fun onNotesChange(v: String)      { if (v.length <= 300) _uiState.value = _uiState.value.copy(notes = v) }
    fun onManualBalanceChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(manualBalance = v)
        }
    }
    fun toggleFavorite()               { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }

    val canProtectNew: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    fun setProtected(value: Boolean) {
        val s = _uiState.value
        if (s.isNew) return
        if (value && !canProtectNew) return
        viewModelScope.launch {
            repository.setProtected(s.id, value)
            _uiState.value = _uiState.value.copy(isProtected = value)
        }
    }

    fun saveFavoriteOnly(v: Boolean) {
        val s = _uiState.value.copy(isFavorite = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setFavorite(s.id, v) }
    }
    fun onLinkedPasswordIdChange(v: String) { _uiState.value = _uiState.value.copy(linkedPasswordId = v) }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun togglePin() {
        val s = _uiState.value
        if (s.isNew) return
        viewModelScope.launch {
            if (!s.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(s.id, !s.isPinned)
            _uiState.value = _uiState.value.copy(isPinned = !s.isPinned)
        }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    val isHubPinned: StateFlow<Boolean> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.any { it.id == walletId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(walletId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (walletId == "new") return
        if (pinnedHubPrefs.isPinned(walletId)) {
            pinnedHubPrefs.unpin(walletId)
        } else {
            val ok = pinnedHubPrefs.pin(walletId, com.thormakk.keyarmor.domain.model.HubEntryType.CRYPTO_WALLET, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }


    fun onTravelSafeChange(v: Boolean) { _uiState.value = _uiState.value.copy(isTravelSafe = v) }
    fun saveTravelSafeOnly(v: Boolean) {
        val s = _uiState.value.copy(isTravelSafe = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setTravelSafe(s.id, v) }
    }

    fun saveHiddenOnly(v: Boolean) {
        val s = _uiState.value.copy(isHidden = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setHidden(s.id, v) }
    }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val oldName = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.renameTag(id, newName)
            oldName?.let { name ->
                val current = _uiState.value.tags
                if (current.contains(name)) {
                    _uiState.value = _uiState.value.copy(
                        tags = current.map { if (it == name) newName.trim() else it }
                    )
                }
            }
        }
    }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val state = _uiState.value
        if (!state.isNew) {
            viewModelScope.launch {
                repository.saveWallet(buildWallet(state).copy(category = category, tags = tags), soloMetadatos = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            if (walletId != "new" && s.id.isEmpty()) return@launch
            repository.saveWallet(buildWallet(s))
            _uiState.value = _uiState.value.copy(
                isSaved   = true,
                notes     = s.notes,
                updatedAt = System.currentTimeMillis()
            )
        }
    }


    fun delete() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (id.isNotEmpty()) {
                if (!repository.softDelete(id)) return@launch
                pinnedHubPrefs.unpin(id)
            }
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun archive() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (!_uiState.value.isNew) { repository.archive(id); pinnedHubPrefs.unpin(id) }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private fun formatDisplayAmount(raw: String): String {
        if (raw.isBlank() || raw == "0") return raw
        val prefix = if (raw.startsWith("< ")) "< " else ""
        val d = raw.removePrefix("< ").toDoubleOrNull() ?: return raw
        if (d == 0.0) return "0"
        val nf = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            isGroupingUsed        = false
            maximumFractionDigits = 8
            minimumFractionDigits = 0
        }
        return "$prefix${nf.format(d)}"
    }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(walletId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(walletId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }

    private fun buildWallet(s: AddEditCryptoWalletUiState) = CryptoWallet(
        id           = if (s.id.isEmpty()) UUID.randomUUID().toString() else s.id,
        network      = s.network,
        asset        = s.asset,
        address      = s.address,
        seedPhrase   = s.seedPhrase,
        privateKey   = s.privateKey,
        notes             = s.notes,
        manualBalance     = s.manualBalance.trim(),
        linkedPasswordId  = s.linkedPasswordId,
        category     = s.category,
        tags         = s.tags,
        isFavorite   = s.isFavorite,
        isProtected  = s.isProtected,
        isTravelSafe = s.isTravelSafe,
        isPinned     = s.isPinned,
        isHidden     = s.isHidden,
        createdAt    = s.createdAt,
        updatedAt    = System.currentTimeMillis(),
        vaultId      = activeVaultPrefs.activeVaultId,
    )

    private fun nativeSymbolForNetwork(network: String): String = when {
        network.startsWith("Bitcoin Cash")     -> "BCH"
        network.startsWith("Bitcoin")          -> "BTC"
        network.startsWith("Ethereum Classic") -> "ETC"
        network.startsWith("Ethereum")         -> "ETH"
        network.startsWith("Solana")           -> "SOL"
        network.startsWith("BNB")              -> "BNB"
        network.startsWith("Polygon")          -> "POL"
        network.startsWith("Arbitrum")         -> "ETH"
        network.startsWith("Optimism")         -> "ETH"
        network.startsWith("Base")             -> "ETH"
        network.startsWith("zkSync")           -> "ETH"
        network.startsWith("Scroll")           -> "ETH"
        network.startsWith("Avalanche")        -> "AVAX"
        network.startsWith("Fantom")           -> "FTM"
        network.startsWith("Gnosis")           -> "DAI"
        network.startsWith("Celo")             -> "CELO"
        network.startsWith("Moonbeam")         -> "GLMR"
        network.startsWith("Cardano")          -> "ADA"
        network.startsWith("Ripple")           -> "XRP"
        network.startsWith("Stellar")          -> "XLM"
        network.startsWith("Algorand")         -> "ALGO"
        network.startsWith("Hedera")           -> "HBAR"
        network.startsWith("Tezos")            -> "XTZ"
        network.startsWith("Cosmos")           -> "ATOM"
        network.startsWith("TON")              -> "TON"

        else                               -> ""
    }
}
