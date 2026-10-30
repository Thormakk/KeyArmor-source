package com.thormakk.keyarmor.presentation.screens.crypto

import com.thormakk.keyarmor.security.OrdenEnSesion
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.CryptoPortfolioPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.remote.BalanceResult
import com.thormakk.keyarmor.data.remote.CachedWalletBalance
import com.thormakk.keyarmor.data.remote.CryptoBalanceCache
import com.thormakk.keyarmor.data.remote.CryptoBalanceFetcher
import com.thormakk.keyarmor.data.remote.CryptoPriceFetcher
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.screens.search.fuzzyMatchesPrimary
import com.thormakk.keyarmor.presentation.screens.search.shouldRunFuzzy
import com.thormakk.keyarmor.presentation.screens.search.textContains
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.composeTopQuickFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import com.thormakk.keyarmor.presentation.util.sinOcultos
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

private val CRYPTO_COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("es")).apply {
    strength = Collator.PRIMARY
}

enum class CryptoSortOrder { A_Z, Z_A, NEWEST, OLDEST, BALANCE_HIGH, BALANCE_LOW }

private data class CryptoFiltersSnapshot(
    val tags:               Set<String>,
    val onlyFavorites:      Boolean,
    val onlyWithAttachments: Boolean,
    val sortOrder:          CryptoSortOrder,
    val onlyProtected:      Boolean
)

data class WalletWithPrice(
    val wallet: CryptoWallet,
    val nominalBalance: String = "",
    val fiatValue: Double? = null,
    val isManual: Boolean = false,
    val isLoadingBalance: Boolean = false
)

data class CryptoAssetGroup(
    val assetTicker: String,
    val assetName: String,
    val totalFiatValue: Double?,
    val totalNominal: String,
    val wallets: List<WalletWithPrice>,
    val isExpanded: Boolean = false
)

data class CryptoWalletsUiState(
    val wallets:             List<CryptoWallet> = emptyList(),
    val fuzzyWallets:        List<CryptoWallet> = emptyList(),
    val isLoading:           Boolean            = true,
    val onlyFavorites:       Boolean            = false,
    val onlyProtected:       Boolean = false,
    val onlyWithAttachments: Boolean            = false,
    val query:               String             = "",
    val sortOrder:           CryptoSortOrder    = CryptoSortOrder.NEWEST,
    val selectedCategory:    EntryCategory?    = null,
    val selectedTags:        Set<String>       = emptySet(),
    val portfolioGroups:     List<CryptoAssetGroup> = emptyList(),
    val totalPortfolioFiat:  Double?           = null,
    val selectedCurrency:    String            = "USD",
    val isLoadingPrices:     Boolean           = false,
    val lastRefreshedAt:     Long?             = null
)

@HiltViewModel
class CryptoWalletsViewModel @Inject constructor(
    private val repository:             CryptoWalletRepository,
    private val tagRepository:          TagRepository,
    private val snapshotRepository:     SnapshotRepository,
    attachmentRepo:         AttachmentRepository,
    planPrefs:              PlanPrefs,
    private val activeVaultPrefs:       ActiveVaultPrefs,
    private val priceFetcher:           CryptoPriceFetcher,
    private val portfolioPrefs:         CryptoPortfolioPrefs,
    private val balanceFetcher:         CryptoBalanceFetcher,
    private val balanceCache:           CryptoBalanceCache,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs:              PinnedHubPrefs,
    private val balanceLookupPrefs:          com.thormakk.keyarmor.data.prefs.BalanceLookupPrefs,
) : ViewModel() {

    val pinnedIds: StateFlow<Set<String>> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _showBalanceConsent = MutableStateFlow(!balanceLookupPrefs.wasAsked())
    val showBalanceConsent: StateFlow<Boolean> = _showBalanceConsent

    private val _isBalanceConsentGranted = MutableStateFlow(balanceLookupPrefs.isConsentGranted())
    val isBalanceConsentGranted: StateFlow<Boolean> = _isBalanceConsentGranted

    fun onBalanceConsent(granted: Boolean) {
        _showBalanceConsent.value = false
        setBalanceConsent(granted)
    }

    fun onBalanceConsentDismissed() {
        _showBalanceConsent.value = false
    }

    fun onEnableLiveBalanceRequested() {
        if (balanceLookupPrefs.wasAsked()) {
            setBalanceConsent(true)
        } else {
            _showBalanceConsent.value = true
        }
    }

    fun setBalanceConsent(granted: Boolean) {
        balanceLookupPrefs.setConsent(granted)
        _isBalanceConsentGranted.value = granted
        if (granted) refreshPortfolio()
    }

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin(id: String, type: HubEntryType) {
        if (pinnedHubPrefs.isPinned(id)) {
            pinnedHubPrefs.unpin(id)
        } else {
            val ok = pinnedHubPrefs.pin(id, type, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID))

    val attachmentIds: StateFlow<Set<String>> =
        attachmentRepo.observeEntryIdsWithAttachments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())


    private val _onlyFavorites       = MutableStateFlow(false)
    private val _onlyProtected       = MutableStateFlow(false)
    private val _onlyWithAttachments = MutableStateFlow(false)
    private val _query               = MutableStateFlow("")
    private val _sortOrder           = MutableStateFlow(OrdenEnSesion.leer("crypto", CryptoSortOrder.NEWEST))
    private val _selectedCategory = MutableStateFlow<EntryCategory?>(null)
    private val _selectedTags     = MutableStateFlow<Set<String>>(emptySet())

    private val _portfolioGroups    = MutableStateFlow<List<CryptoAssetGroup>>(emptyList())
    private val _totalPortfolioFiat = MutableStateFlow<Double?>(null)
    private val _selectedCurrency   = MutableStateFlow(portfolioPrefs.getSelectedCurrency())
    private val _isLoadingPrices    = MutableStateFlow(false)
    private val _expandedAssets     = MutableStateFlow<Set<String>>(emptySet())
    private val _lastRefreshedAt    = MutableStateFlow<Long?>(null)

    private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())
    private val _bulkDeleting = MutableStateFlow(false)
    val bulkDeleting: StateFlow<Boolean> = _bulkDeleting.asStateFlow()

    private val _wallets = MutableStateFlow<List<CryptoWallet>>(emptyList())

    private val _isInitialLoad = MutableStateFlow(true)


    private var portfolioJob: Job? = null

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categoryCountMap: StateFlow<Map<EntryCategory, Int>> = _wallets
        .map { list -> list.groupingBy { it.category }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val tagCountMap: StateFlow<Map<String, Int>> = _wallets
        .map { list -> list.flatMap { it.tags }.filter { it.isNotBlank() }.groupingBy { it.trim() }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val quickFilters: StateFlow<List<QuickFilter>> = combine(
        _wallets,
        tagRepository.observeDefaultTagNames()
    ) { wallets, defaultTags ->
        val tagImportant = defaultTags.first
        val tagWork      = defaultTags.third

        val categoryCounts = EntryCategory.entries
            .asSequence()
            .filter { it != EntryCategory.SIN_CATEGORIZAR }
            .map { cat -> QuickFilter.Category(cat) to wallets.count { it.category == cat } }
            .toList()

        val tagCounts = wallets
            .flatMap { it.tags.map { t -> t.trim() } }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .map { (tag, count) -> QuickFilter.Tag(tag) to count }

        val ranked = (categoryCounts + tagCounts)
            .filter { it.second > 0 }
            .sortedWith(
                compareByDescending<Pair<QuickFilter, Int>> { it.second }
                    .thenBy { it.first.key }
            )
            .map { it.first }

        val defaults: List<QuickFilter> = listOf(
            QuickFilter.Tag(tagImportant),
            QuickFilter.Tag(defaultTags.second),
            QuickFilter.Tag(tagWork)
        )
        composeTopQuickFilters(ranked, defaults)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<CryptoWalletsUiState> = combine(
        combine(_wallets, _query, _selectedCategory, _isInitialLoad) { w, q, cat, init ->
            listOf<Any?>(w, q, cat, init)
        },
        combine(_selectedTags, _onlyFavorites, _onlyWithAttachments, _sortOrder, _onlyProtected) { t, f, a, o, p -> CryptoFiltersSnapshot(t, f, a, o, p) },
        combine(_portfolioGroups, _totalPortfolioFiat, _selectedCurrency, _isLoadingPrices, _lastRefreshedAt) { groups, total, cur, loading, refreshed ->
            listOf<Any?>(groups, total, cur, loading, refreshed)
        }
    ) { walletData, filters, portfolioData ->
        @Suppress("UNCHECKED_CAST")
        val wallets          = walletData[0] as List<CryptoWallet>
        val query            = walletData[1] as String
        val category         = walletData[2] as EntryCategory?
        val isInitialLoad    = walletData[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val portfolioGroups  = portfolioData[0] as List<CryptoAssetGroup>
        val totalFiat        = portfolioData[1] as Double?
        val selectedCurrency = portfolioData[2] as String
        val isLoadingPrices  = portfolioData[3] as Boolean
        val lastRefreshedAt  = portfolioData[4] as Long?
        val q = query.trim().lowercase(Locale.ROOT)
        val filtered = wallets
            .filter { if (filters.onlyFavorites) it.isFavorite else true }
            .filter { !filters.onlyProtected || it.isProtected }
            .filter { !filters.onlyWithAttachments || it.id in attachmentIds.value }
            .filter { if (category != null) it.category == category else true }
            .filter { w ->
                filters.tags.isEmpty() || filters.tags.all { tag ->
                    w.tags.any { it.trim().equals(tag, ignoreCase = true) }
                }
            }
            .filter { w ->
                q.isBlank() ||
                textContains(w.asset, q) || textContains(w.network, q) ||
                textContains(w.address, q) || textContains(w.exchangeUrl, q) ||
                textContains(w.notes, q)
            }
        val sorted = when (filters.sortOrder) {
            CryptoSortOrder.A_Z    -> filtered.sortedWith { a, b -> CRYPTO_COLLATOR.compare(a.asset.ifEmpty { a.network }, b.asset.ifEmpty { b.network }) }
            CryptoSortOrder.Z_A    -> filtered.sortedWith { a, b -> CRYPTO_COLLATOR.compare(b.asset.ifEmpty { b.network }, a.asset.ifEmpty { a.network }) }
            CryptoSortOrder.NEWEST -> filtered.sortedByDescending { it.updatedAt }
            CryptoSortOrder.OLDEST -> filtered.sortedBy { it.updatedAt }
            CryptoSortOrder.BALANCE_HIGH -> filtered.sortedByDescending { w ->
                portfolioGroups.flatMap { it.wallets }.find { it.wallet.id == w.id }?.fiatValue ?: -1.0
            }
            CryptoSortOrder.BALANCE_LOW -> filtered.sortedBy { w ->
                portfolioGroups.flatMap { it.wallets }.find { it.wallet.id == w.id }?.fiatValue ?: Double.MAX_VALUE
            }
        }
        val pinned   = sorted.filter { it.isPinned }
        val unpinned = sorted.filter { !it.isPinned }

        val sortedGroups = when (filters.sortOrder) {
            CryptoSortOrder.A_Z    -> portfolioGroups.sortedWith { a, b ->
                CRYPTO_COLLATOR.compare(a.assetName, b.assetName)
            }
            CryptoSortOrder.Z_A    -> portfolioGroups.sortedWith { a, b ->
                CRYPTO_COLLATOR.compare(b.assetName, a.assetName)
            }
            CryptoSortOrder.NEWEST -> portfolioGroups.sortedByDescending { group ->
                group.wallets.maxOfOrNull { it.wallet.updatedAt } ?: 0L
            }
            CryptoSortOrder.OLDEST -> portfolioGroups.sortedBy { group ->
                group.wallets.minOfOrNull { it.wallet.updatedAt } ?: Long.MAX_VALUE
            }
            CryptoSortOrder.BALANCE_HIGH -> portfolioGroups.sortedByDescending { it.totalFiatValue ?: -1.0 }
            CryptoSortOrder.BALANCE_LOW  -> portfolioGroups.sortedBy { it.totalFiatValue ?: Double.MAX_VALUE }
        }
        val pinnedGroups   = sortedGroups.filter { g -> g.wallets.any { it.wallet.isPinned } }
        val unpinnedGroups = sortedGroups.filter { g -> g.wallets.none { it.wallet.isPinned } }

        val exact = pinned + unpinned
        val fuzzyWallets = if (shouldRunFuzzy(query)) {
            val exactIds = exact.mapTo(HashSet()) { it.id }
            wallets
                .filter { it.id !in exactIds }
                .filter { if (filters.onlyFavorites) it.isFavorite else true }
                .filter { !filters.onlyProtected || it.isProtected }
                .filter { !filters.onlyWithAttachments || it.id in attachmentIds.value }
                .filter { if (category != null) it.category == category else true }
                .filter { w -> filters.tags.isEmpty() || filters.tags.all { tag -> w.tags.any { it.trim().equals(tag, ignoreCase = true) } } }
                .filter { it.fuzzyMatchesPrimary(query) }
        } else emptyList()

        CryptoWalletsUiState(
            wallets             = exact,
            fuzzyWallets        = fuzzyWallets,
            isLoading           = isInitialLoad,
            onlyFavorites       = filters.onlyFavorites,
            onlyProtected       = filters.onlyProtected,
            onlyWithAttachments = filters.onlyWithAttachments,
            query               = query,
            sortOrder           = filters.sortOrder,
            selectedCategory    = category,
            selectedTags        = filters.tags,
            portfolioGroups     = pinnedGroups + unpinnedGroups,
            totalPortfolioFiat  = totalFiat,
            selectedCurrency    = selectedCurrency,
            isLoadingPrices     = isLoadingPrices,
            lastRefreshedAt     = lastRefreshedAt
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoWalletsUiState()
    )

    init {
        _lastRefreshedAt.value = balanceCache.lastRefreshedAt

        viewModelScope.launch {
            delay(350)
            repository.observeWallets().sinOcultos(_hiddenIds) { it.id }.collect { wallets ->
                _wallets.value = wallets
                buildGroupsFromCache()
            }
        }
        viewModelScope.launch {
            _selectedCurrency.drop(1).collect {
                refreshPrices()
            }
        }
    }

    private fun buildGroupsFromCache() {
        if (portfolioJob?.isActive == true) return
        viewModelScope.launch {
            val wallets = _wallets.value
            if (wallets.isEmpty()) {
                _portfolioGroups.value    = emptyList()
                _totalPortfolioFiat.value = null
                _isInitialLoad.value      = false
                return@launch
            }
            val groups = wallets
                .groupBy { claveDeActivo(it) }
                .map { (ticker, items) ->
                    val cachedItems = items.map { w ->
                        balanceCache.get(w.id)?.let { cached ->
                            WalletWithPrice(w, cached.nominalBalance, cached.fiatValue, cached.isManual)
                        } ?: WalletWithPrice(w)
                    }
                    val totalFiat    = cachedItems.mapNotNull { it.fiatValue }.takeIf { it.isNotEmpty() }?.sum()
                    val totalNominal = run {
                        val sum = cachedItems.mapNotNull { it.nominalBalance.parseNominal() }.sum()
                        if (sum > 0) "${formatNominal(sum)} $ticker" else ""
                    }
                    CryptoAssetGroup(
                        assetTicker    = ticker,
                        assetName      = nameForTicker(ticker) ?: ticker,
                        totalFiatValue = totalFiat,
                        totalNominal   = totalNominal,
                        wallets        = cachedItems,
                        isExpanded     = ticker in _expandedAssets.value
                    )
                }
                .sortedByDescending { it.totalFiatValue ?: -1.0 }
            _portfolioGroups.value    = groups
            _totalPortfolioFiat.value = groups.mapNotNull { it.totalFiatValue }.takeIf { it.isNotEmpty() }?.sum()
            _isInitialLoad.value      = false
        }
    }

    fun refreshPrices() {
        portfolioJob?.cancel()
        balanceCache.clearFiatValues()
        refreshPortfolio()
    }

    fun refreshPortfolio() {
        portfolioJob?.cancel()
        portfolioJob = viewModelScope.launch {
            val wallets = _wallets.value.ifEmpty { repository.observeWallets().first() }
            if (wallets.isEmpty()) {
                _portfolioGroups.value = emptyList()
                _totalPortfolioFiat.value = null
                return@launch
            }
            _isLoadingPrices.value = true

            val immediateGroups = wallets
                .groupBy { claveDeActivo(it) }
                .map { (ticker, items) ->
                    val cachedItems = items.map { w ->
                        balanceCache.get(w.id)?.let { cached ->
                            WalletWithPrice(w, cached.nominalBalance, cached.fiatValue, cached.isManual)
                        } ?: WalletWithPrice(w)
                    }
                    val totalFiat = cachedItems.mapNotNull { it.fiatValue }.takeIf { it.isNotEmpty() }?.sum()
                    val totalNominal = run {
                        val sum = cachedItems.mapNotNull { it.nominalBalance.parseNominal() }.sum()
                        if (sum > 0) "${formatNominal(sum)} $ticker" else ""
                    }
                    CryptoAssetGroup(
                        assetTicker    = ticker,
                        assetName      = nameForTicker(ticker) ?: ticker,
                        totalFiatValue = totalFiat,
                        totalNominal   = totalNominal,
                        wallets        = cachedItems,
                        isExpanded     = ticker in _expandedAssets.value
                    )
                }
                .sortedByDescending { it.totalFiatValue ?: -1.0 }
            _portfolioGroups.value = immediateGroups
            _totalPortfolioFiat.value = immediateGroups.mapNotNull { it.totalFiatValue }.takeIf { it.isNotEmpty() }?.sum()

            val solanaAddresses = wallets
                .filter { it.network.startsWith("Solana") && it.asset.isNotBlank() && it.asset.uppercase() != "SOL" }
                .map { it.address }.toSet()
            val solanaBatchCache: Map<String, Map<String, Double>> =
                solanaAddresses.associateWith { addr ->
                    runCatching { balanceFetcher.fetchAllSolanaTokenBalances(addr) }.getOrElse { emptyMap() }
                }

            val fetchSemaphore = Semaphore(15)
            val walletsWithBalance = wallets.map { wallet ->
                async {
                    val cached = balanceCache.get(wallet.id)
                    if (cached != null && cached.nominalBalance.isNotEmpty() && cached.manualBalance == wallet.manualBalance) {
                        WalletWithPrice(wallet, cached.nominalBalance, cached.fiatValue, cached.isManual)
                    } else if (wallet.manualBalance.isNotEmpty()) {
                        WalletWithPrice(wallet, nominalBalance = wallet.manualBalance, isManual = true).also {
                            balanceCache.put(wallet.id, CachedWalletBalance(it.nominalBalance, it.fiatValue, it.isManual, wallet.manualBalance))
                        }
                    } else {
                        fetchSemaphore.withPermit {
                            val isSplToken = wallet.network.startsWith("Solana") &&
                                             wallet.asset.isNotBlank() &&
                                             wallet.asset.uppercase() != "SOL"
                            val result = if (isSplToken) {
                                val batchBalance = solanaBatchCache[wallet.address]?.get(wallet.asset.uppercase())
                                if (batchBalance != null)
                                    BalanceResult.Success(
                                        if (batchBalance == 0.0) "0"
                                        else balanceFetcher.formatNominalDecimal(batchBalance),
                                        wallet.asset.uppercase()
                                    )
                                else
                                    balanceFetcher.fetch(wallet.network, wallet.address, wallet.asset)
                            } else {
                                balanceFetcher.fetch(wallet.network, wallet.address, wallet.asset)
                            }
                            val nominal = if (result is BalanceResult.Success) result.amount else ""
                            WalletWithPrice(wallet, nominalBalance = nominal, isManual = false).also {
                                balanceCache.put(wallet.id, CachedWalletBalance(nominal, null, false, ""))
                            }
                        }
                    }
                }
            }.awaitAll()

            val tickers = walletsWithBalance
                .filter { it.nominalBalance.isNotEmpty() }
                .map { wwp ->
                    tickerDe(wwp.wallet)
                }
                .filter { it.isNotBlank() }
                .toSet()
            val prices = if (tickers.isNotEmpty())
                priceFetcher.fetchPrices(tickers, _selectedCurrency.value)
            else emptyMap()

            val walletsWithFiat = walletsWithBalance.map { wwp ->
                val ticker   = tickerDe(wwp.wallet)
                val price    = prices[ticker]
                val nominal  = wwp.nominalBalance.parseNominal()
                val fiatValue = if (price != null && nominal != null) price * nominal else null
                wwp.copy(fiatValue = fiatValue).also {
                    balanceCache.put(wwp.wallet.id, CachedWalletBalance(
                        nominalBalance = wwp.nominalBalance,
                        fiatValue      = fiatValue,
                        isManual       = wwp.isManual,
                        manualBalance  = wwp.wallet.manualBalance
                    ))
                }
            }

            val groups = walletsWithFiat
                .groupBy { claveDeActivo(it.wallet) }
                .map { (ticker, items) ->
                    val totalFiat = items.mapNotNull { it.fiatValue }.takeIf { it.isNotEmpty() }?.sum()
                    val totalNominal = if (items.all { it.nominalBalance.isNotEmpty() }) {
                        val sum = items.mapNotNull { it.nominalBalance.parseNominal() }.sum()
                        "${formatNominal(sum)} $ticker"
                    } else ""
                    CryptoAssetGroup(
                        assetTicker    = ticker,
                        assetName      = nameForTicker(ticker) ?: ticker,
                        totalFiatValue = totalFiat,
                        totalNominal   = totalNominal,
                        wallets        = items,
                        isExpanded     = ticker in _expandedAssets.value
                    )
                }
                .sortedByDescending { it.totalFiatValue ?: -1.0 }

            _portfolioGroups.value = groups
            _totalPortfolioFiat.value = groups.mapNotNull { it.totalFiatValue }.takeIf { it.isNotEmpty() }?.sum()
            _isLoadingPrices.value = false
            balanceCache.markRefreshed()
            _lastRefreshedAt.value = balanceCache.lastRefreshedAt

            walletsWithBalance
                .filter { !it.isManual && it.nominalBalance.isEmpty() }
                .forEach { balanceCache.remove(it.wallet.id) }
        }
    }

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
        network.startsWith("Tron")             -> "TRX"
        network.startsWith("Dogecoin")         -> "DOGE"
        network.startsWith("Litecoin")         -> "LTC"
        network.startsWith("Dash")             -> "DASH"
        network.startsWith("Zcash")            -> "ZEC"
        else -> ""
    }

    private fun String.parseNominal(): Double? =
        removePrefix("< ").toDoubleOrNull()

    private fun formatNominal(value: Double): String {
        if (value == 0.0) return "0"
        val nf = java.text.NumberFormat.getNumberInstance(AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()).apply {
            isGroupingUsed        = false
            maximumFractionDigits = if (value >= 1.0) 4 else 8
            minimumFractionDigits = 0
        }
        return nf.format(value)
    }

    private fun claveDeActivo(w: CryptoWallet): String = tickerDe(w).ifEmpty { w.network }

    private fun tickerDe(w: CryptoWallet): String {
        val texto = w.asset.trim()
        if (texto.isEmpty()) return nativeSymbolForNetwork(w.network)
        fun moneda(x: String) = CryptoIconProvider.coins.find {
            it.ticker.equals(x, ignoreCase = true) || it.name.equals(x, ignoreCase = true)
        }
        moneda(texto)?.let { return it.ticker.uppercase() }
        Regex("""^(.*?)\s*\(([^()]+)\)$""").find(texto)?.let { m ->
            val (fuera, dentro) = m.destructured
            (moneda(dentro.trim()) ?: moneda(fuera.trim()))?.let { return it.ticker.uppercase() }
        }
        return texto.uppercase()
    }

    private fun nameForTicker(ticker: String): String? {
        return CryptoIconProvider.coins.find { it.ticker.equals(ticker, ignoreCase = true) }?.name
    }

    fun onCurrencySelected(currency: String) {
        portfolioPrefs.setSelectedCurrency(currency)
        _selectedCurrency.value = currency
        balanceCache.clear()
        refreshPortfolio()
    }

    fun onToggleAssetExpanded(ticker: String) {
        _expandedAssets.update { if (ticker in it) it - ticker else it + ticker }
        _portfolioGroups.update { groups ->
            groups.map { if (it.assetTicker == ticker) it.copy(isExpanded = ticker in _expandedAssets.value) else it }
        }
    }

        fun onFavoritesToggle(value: Boolean)        { _onlyFavorites.value = value }
        fun onProtectedFilterToggle(value: Boolean) { _onlyProtected.value = value }
    fun onAttachmentFilterToggle()               { _onlyWithAttachments.value = !_onlyWithAttachments.value }
    fun onQueryChange(value: String)             { _query.value = value }
    fun onCategorySelected(cat: EntryCategory?)  { _selectedCategory.value = cat }
    fun onSortOrderChange(order: CryptoSortOrder){ OrdenEnSesion.guardar("crypto", order); _sortOrder.value = order }

    fun onTagToggled(tag: String) {
        _selectedTags.update { current ->
            if (tag in current) current - tag else current + tag
        }
    }

    fun onTagsApplied(tags: Set<String>) { _selectedTags.value = tags }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) { viewModelScope.launch { tagRepository.renameTag(id, newName) } }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached

    fun togglePin(walletId: String) {
        viewModelScope.launch {
            val wallet = _wallets.value.find { it.id == walletId } ?: return@launch
            if (!wallet.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(walletId, !wallet.isPinned)
        }
    }

    fun onPinLimitDismissed() { _pinLimitReached.value = false }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun bulkMoveTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            repository.moveEntriesToVault(ids, targetVaultId)
            _selectedIds.value = emptySet()
        }
    }

    fun bulkCopyTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
                activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
            } else null
            repository.copyEntriesToVault(ids, targetVaultId, inheritedFrom)
            _selectedIds.value = emptySet()
        }
    }

    fun moveEntryTo(entryId: String, targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(entryId), targetVaultId) }
    }

    fun copyEntryTo(entryId: String, targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        } else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    fun toggleGroupSelection(walletIds: List<String>) {
        _selectedIds.update { current ->
            if (walletIds.all { it in current }) current - walletIds.toSet()
            else current + walletIds.toSet()
        }
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.wallets.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun bulkArchive() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            repository.archiveBatch(ids)
            ids.forEach { pinnedHubPrefs.unpin(it) }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkDelete() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty() || _bulkDeleting.value) return
        _bulkDeleting.value = true
        viewModelScope.launch {
            try {
                val protegidas = repository.protectedAmong(ids)
                _hiddenIds.value = ids.filter { it !in protegidas }.toSet()
                _selectedIds.value = emptySet()
                withContext(NonCancellable) {
                    if (ids.size >= 3) {
                        snapshotRepository.createSnapshotBestEffort(SnapshotTrigger.PRE_BULK_DELETE)
                    }
                    repository.softDeleteBatch(ids)
                    ids.filter { it !in protegidas }.forEach { pinnedHubPrefs.unpin(it) }
                    if (protegidas.isNotEmpty()) _protectedSkipped.tryEmit(protegidas.size)
                    delay(400)
                }
            } finally {
                _hiddenIds.value = emptySet()
                _bulkDeleting.value = false
            }
        }
    }

    fun bulkSetProtected(value: Boolean) {
        if (value && isFreeUser.value) return
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            ids.forEach { repository.setProtected(it, value) }
            _selectedIds.value = emptySet()
        }
    }

    private val _protectedSkipped = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val protectedSkipped: SharedFlow<Int> = _protectedSkipped.asSharedFlow()

    fun bulkUpdateCategory(category: EntryCategory, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.wallets
                .filter { it.id in ids }
                .forEach { wallet ->
                    val newCategory = when (mode) {
                        BulkMode.ADDITIVE -> if (wallet.category == EntryCategory.SIN_CATEGORIZAR) category else wallet.category
                        BulkMode.REMOVE   -> EntryCategory.SIN_CATEGORIZAR
                        BulkMode.REPLACE  -> category
                    }
                    if (newCategory != wallet.category) {
                        repository.saveWallet(wallet.copy(category = newCategory), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkUpdateTags(tags: Set<String>, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.wallets
                .filter { it.id in ids }
                .forEach { wallet ->
                    val newTags = when (mode) {
                        BulkMode.ADDITIVE -> (wallet.tags + tags).distinct()
                        BulkMode.REMOVE   -> wallet.tags.filter { it !in tags }
                        BulkMode.REPLACE  -> tags.toList()
                    }
                    if (newTags != wallet.tags) {
                        repository.saveWallet(wallet.copy(tags = newTags), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }
}