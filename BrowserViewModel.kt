package com.example.ui

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface TabScreen {
    object Browser : TabScreen
    object Vpn : TabScreen
    object Dashboard : TabScreen
}

enum class VortexAccent(val hex: Long, val nameStr: String) {
    NEXUS_BLUE(0xFF3B82F6, "Nexus Royal Blue"),
    VORTEX_BLUE(0xFF00E5FF, "Neon Vortex Blue"),
    COSMIC_MAGENTA(0xFFFF007F, "Cosmic Nitro"),
    TOXIC_GREEN(0xFF39FF14, "Acid Lime")
}

class BrowserViewModel(private val repository: BrowserRepository) : ViewModel() {

    // Global App view controller
    private val _activeTab = MutableStateFlow<TabScreen>(TabScreen.Browser)
    val activeTab: StateFlow<TabScreen> = _activeTab.asStateFlow()

    // 3D Visual theme accent
    private val _accentColor = MutableStateFlow(VortexAccent.NEXUS_BLUE)
    val accentColor: StateFlow<VortexAccent> = _accentColor.asStateFlow()

    // Browser core state
    private val _currentUrl = MutableStateFlow("vortex://home")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentTitle = MutableStateFlow("Vortex Safe Hub")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _searchQueryInput = MutableStateFlow("")
    val searchQueryInput: StateFlow<String> = _searchQueryInput.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _webViewLoading = MutableStateFlow(false)
    val webViewLoading: StateFlow<Boolean> = _webViewLoading.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _isCurrentBookmarked = MutableStateFlow(false)
    val isCurrentBookmarked: StateFlow<Boolean> = _isCurrentBookmarked.asStateFlow()

    // Search Engine selector
    private val _searchEngine = MutableStateFlow("https://google.com/search?q=")
    val searchEngine: StateFlow<String> = _searchEngine.asStateFlow()

    // AdBlock settings
    private val _adBlockLevel = MutableStateFlow(AdBlocker.BlockLevel.STANDARD)
    val adBlockLevel: StateFlow<AdBlocker.BlockLevel> = _adBlockLevel.asStateFlow()

    private val _cookieBlockEnabled = MutableStateFlow(true)
    val cookieBlockEnabled: StateFlow<Boolean> = _cookieBlockEnabled.asStateFlow()

    // Real-time blocked count for active tab Session
    private val _sessionBlockedCount = MutableStateFlow(0)
    val sessionBlockedCount: StateFlow<Int> = _sessionBlockedCount.asStateFlow()

    // Room Persistent lists
    val bookmarkList: StateFlow<List<Bookmark>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyList: StateFlow<List<HistoryItem>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedAdLogs: StateFlow<List<BlockedAdLog>> = repository.adLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlockedCountLifetime: StateFlow<Int> = repository.blockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // VPN Engine setup
    val vpnManager = VpnManager(viewModelScope)
    val vpnState = vpnManager.vpnState
    val currentVpnServer = vpnManager.selectedServer
    val vpnProtocol = vpnManager.protocol
    val vpnDownloadSpeed = vpnManager.downloadSpeed
    val vpnUploadSpeed = vpnManager.uploadSpeed
    val vpnBandwidthMb = vpnManager.bandwidthUsedMb
    val vpnDurationSeconds = vpnManager.sessionDurationSeconds
    val assignedVpnIp = vpnManager.assignedIp

    // Active reference to system WebView for back/forward navigation triggers
    var systemWebViewRef: WebView? = null

    init {
        // Initial configuration checks
        checkIfCurrentUrlIsBookmarked()
    }

    fun setTab(screen: TabScreen) {
        _activeTab.value = screen
    }

    fun setAccent(accent: VortexAccent) {
        _accentColor.value = accent
    }

    fun setSearchEngine(urlPrefix: String) {
        _searchEngine.value = urlPrefix
    }

    fun setSearchInput(input: String) {
        _searchQueryInput.value = input
    }

    fun setAdBlockLevel(level: AdBlocker.BlockLevel) {
        _adBlockLevel.value = level
        // Clear session block counters when settings re-adjust so that changes are tangible
        _sessionBlockedCount.value = 0
    }

    fun setCookieBlock(enabled: Boolean) {
        _cookieBlockEnabled.value = enabled
    }

    fun changeUrl(url: String) {
        val resolvedUrl = resolveUrl(url)
        _currentUrl.value = resolvedUrl
        _searchQueryInput.value = resolvedUrl
        _sessionBlockedCount.value = 0 // Reset visual count per page load
    }

    fun navigateUrl(url: String) {
        changeUrl(url)
        systemWebViewRef?.loadUrl(_currentUrl.value)
    }

    private fun resolveUrl(input: String): String {
        var clean = input.trim()
        if (clean.isBlank()) return "vortex://home"
        
        if (clean == "vortex://home" || clean == "about:blank") return "vortex://home"
        
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            return clean
        }
        
        // Host checking simple cases
        if (clean.contains(".") && !clean.contains(" ")) {
            return "https://$clean"
        }
        
        // Search query
        return _searchEngine.value + clean.replace(" ", "+")
    }

    fun updateLoadingProgress(progress: Int) {
        _loadingProgress.value = progress
        _webViewLoading.value = progress < 100
        if (progress == 100) {
            checkIfCurrentUrlIsBookmarked()
        }
    }

    fun updateWebPageState(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        if (url != "about:blank" && !url.startsWith("file:///android_asset")) {
            _currentUrl.value = url
            _searchQueryInput.value = url
        }
        title?.let {
            if (it.isNotBlank()) {
                _currentTitle.value = it
            }
        }
        _canGoBack.value = canBack
        _canGoForward.value = canForward
        checkIfCurrentUrlIsBookmarked()

        // Persistent update history if not our homepage
        if (url != "vortex://home" && url != "about:blank" && !url.contains("google.com/search") && !url.contains("duckduckgo.com")) {
            viewModelScope.launch {
                repository.addHistory(url, title ?: url)
            }
        }
    }

    fun triggerBlockedAd(host: String, pageUrl: String) {
        viewModelScope.launch {
            _sessionBlockedCount.value += 1
            repository.logBlockedAd(host, pageUrl)
        }
    }

    fun toggleBookmark() {
        val url = _currentUrl.value
        val title = _currentTitle.value
        viewModelScope.launch {
            if (_isCurrentBookmarked.value) {
                repository.removeBookmark(url)
                _isCurrentBookmarked.value = false
            } else {
                repository.addBookmark(url, title)
                _isCurrentBookmarked.value = true
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearBlockedLogs() {
        viewModelScope.launch {
            repository.clearAdLogs()
        }
    }

    private fun checkIfCurrentUrlIsBookmarked() {
        viewModelScope.launch {
            _isCurrentBookmarked.value = repository.isBookmarked(_currentUrl.value)
        }
    }

    fun handleBackPressed(): Boolean {
        return if (_activeTab.value != TabScreen.Browser) {
            _activeTab.value = TabScreen.Browser
            true
        } else if (_currentUrl.value != "vortex://home" && _canGoBack.value) {
            systemWebViewRef?.goBack()
            true
        } else if (_currentUrl.value != "vortex://home") {
            changeUrl("vortex://home")
            true
        } else {
            false
        }
    }
}

class BrowserViewModelFactory(private val repository: BrowserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
