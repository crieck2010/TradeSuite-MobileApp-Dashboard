package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.TradeSuiteRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
  STUDIO("0DTE Studio"),
  KPIS("KPIs"),
  AGENTS("Agent Desk"),
  RISK("Risk & Halt"),
  BLOTTER("Paper Blotter"),
  COPILOT("AI Copilot"),
  MACRO("Macro & Feeds"),
  ENGINES("36 Engines"),
  CONNECTION("Connect")
}

data class UiNotification(
  val message: String,
  val isError: Boolean = false,
  val id: Long = System.currentTimeMillis()
)

class TradeSuiteViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = TradeSuiteRepository()
  private val preferencesManager = PreferencesManager(application)

  val connectionConfig = preferencesManager.connectionConfigFlow.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    ConnectionConfig()
  )

  private val _activeTab = MutableStateFlow(MainTab.STUDIO)
  val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

  val engines: StateFlow<List<EngineInfo>> = repository.engines
  val agents: StateFlow<List<Agent>> = repository.agents
  val debates: StateFlow<List<AgentDebate>> = repository.debates
  val riskState: StateFlow<RiskState> = repository.riskState
  val riskVetoes: StateFlow<List<RiskVeto>> = repository.riskVetoes
  val moomooStatus: StateFlow<MoomooStatus> = repository.moomooStatus
  val blotterTrades: StateFlow<List<BlotterTrade>> = repository.blotterTrades
  val pendingOrders: StateFlow<List<PendingOrder>> = repository.pendingOrders
  val macroBarometer: StateFlow<MacroBarometer> = repository.macroBarometer
  val performanceStats: StateFlow<PerformanceStats> = repository.performanceStats
  val marketTickers: StateFlow<List<MarketTicker>> = repository.marketTickers
  val strategies: StateFlow<List<StrategyPerformance>> = repository.strategies
  val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
  val nlStrategies: StateFlow<List<NaturalLanguageStrategy>> = repository.nlStrategies

  private val _isParsingStrategy = MutableStateFlow(false)
  val isParsingStrategy: StateFlow<Boolean> = _isParsingStrategy.asStateFlow()

  private val _deskRunInProgress = MutableStateFlow(false)
  val deskRunInProgress: StateFlow<Boolean> = _deskRunInProgress.asStateFlow()

  private val _selectedDebate = MutableStateFlow<AgentDebate?>(null)
  val selectedDebate: StateFlow<AgentDebate?> = _selectedDebate.asStateFlow()

  private val _showKillSwitchDialog = MutableStateFlow(false)
  val showKillSwitchDialog: StateFlow<Boolean> = _showKillSwitchDialog.asStateFlow()

  private val _notification = MutableStateFlow<UiNotification?>(null)
  val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

  private val _isTestingConnection = MutableStateFlow(false)
  val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

  private val _connectionResult = MutableStateFlow<Pair<Boolean, String>?>(null)
  val connectionResult: StateFlow<Pair<Boolean, String>?> = _connectionResult.asStateFlow()

  private val _searchFilter = MutableStateFlow("")
  val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

  private val _isChatSending = MutableStateFlow(false)
  val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

  init {
    // Start continuous live tick stream from repository
    viewModelScope.launch {
      repository.getLiveTickStream().collect {
        // Tickers automatically update in repository.marketTickers
      }
    }
  }

  fun selectTab(tab: MainTab) {
    _activeTab.value = tab
  }

  fun setSearchFilter(query: String) {
    _searchFilter.value = query
  }

  fun selectDebate(debate: AgentDebate?) {
    _selectedDebate.value = debate
  }

  fun triggerDeskRun(symbols: List<String> = listOf("SPY", "AAPL", "NVDA", "QQQ")) {
    if (_deskRunInProgress.value) return
    viewModelScope.launch {
      _deskRunInProgress.value = true
      _notification.value = UiNotification("Autonomous Research Desk activated across 6 specialist scouts...")
      val result = repository.runAgentDesk(symbols)
      _deskRunInProgress.value = false
      _selectedDebate.value = result
      if (result.isApproved) {
        _notification.value = UiNotification("Desk completed: ${result.consensusAction} for ${result.symbol} (Approved by PM & Risk)")
      } else {
        _notification.value = UiNotification("Risk Manager Veto: ${result.vetoReason}", isError = true)
      }
    }
  }

  fun setKillSwitchDialogVisible(visible: Boolean) {
    _showKillSwitchDialog.value = visible
  }

  fun armKillSwitch(arm: Boolean) {
    repository.toggleKillSwitch(arm = arm, trigger = false)
    _notification.value = UiNotification(if (arm) "Kill-switch ARMED: Active monitoring with automated cutoff" else "Kill-switch DISARMED")
  }

  fun triggerEmergencyKillSwitch() {
    repository.toggleKillSwitch(arm = true, trigger = true)
    _showKillSwitchDialog.value = false
    _notification.value = UiNotification("EMERGENCY KILL-SWITCH TRIGGERED: All active orders halted and positions hedged!", isError = true)
  }

  fun resetKillSwitch() {
    repository.toggleKillSwitch(arm = true, trigger = false)
    _notification.value = UiNotification("Emergency halt cleared. System re-armed.")
  }

  fun approvePendingOrder(orderId: String) {
    repository.approvePendingOrder(orderId)
    _notification.value = UiNotification("Order $orderId dispatched to OpenD paper trading engine")
  }

  fun vetoPendingOrder(orderId: String) {
    repository.vetoPendingOrder(orderId)
    _notification.value = UiNotification("Order $orderId vetoed by operator", isError = true)
  }

  fun exportCsv(): String {
    val csv = repository.exportBlotterCsv()
    _notification.value = UiNotification("Blotter exported (${blotterTrades.value.size} trades formatted as CSV)")
    return csv
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank() || _isChatSending.value) return
    viewModelScope.launch {
      _isChatSending.value = true
      repository.sendChatMessage(text)
      _isChatSending.value = false
    }
  }

  fun clearChat() {
    repository.clearChatHistory()
    _notification.value = UiNotification("Conversation history reset")
  }

  fun importLogJson(jsonText: String) {
    val (success, msg) = repository.importLogJson(jsonText)
    _notification.value = UiNotification(msg, isError = !success)
  }

  fun updateConnectionSettings(serverUrl: String, openDHost: String, openDPort: Int, apiKey: String, isDemo: Boolean) {
    viewModelScope.launch {
      val newConfig = ConnectionConfig(
        serverUrl = serverUrl,
        openDHost = openDHost,
        openDPort = openDPort,
        apiKey = apiKey,
        isDemoMode = isDemo
      )
      preferencesManager.updateConfig(newConfig)
      _notification.value = UiNotification("Connection settings updated")
    }
  }

  fun setDemoMode(isDemo: Boolean) {
    viewModelScope.launch {
      preferencesManager.setDemoMode(isDemo)
      _notification.value = UiNotification(if (isDemo) "Switched to Built-in Engine Simulator mode" else "Switched to Connected Server mode")
    }
  }

  fun testConnectionNow() {
    viewModelScope.launch {
      _isTestingConnection.value = true
      val currentConfig = connectionConfig.value
      val result = repository.testConnection(
        serverUrl = currentConfig.serverUrl,
        openDHost = currentConfig.openDHost,
        openDPort = currentConfig.openDPort
      )
      _isTestingConnection.value = false
      _connectionResult.value = result
    }
  }

  fun parseAndAddStrategy(dictatedText: String) {
    if (dictatedText.isBlank()) return
    viewModelScope.launch {
      _isParsingStrategy.value = true
      _notification.value = UiNotification("Parsing 0DTE natural language strategy & computing cross-strategy correlations...")
      kotlinx.coroutines.delay(400)
      val strategy = repository.parseNaturalLanguageStrategy(dictatedText)
      repository.addCustomStrategy(strategy)
      _isParsingStrategy.value = false
      _notification.value = UiNotification("Generated: ${strategy.title} • Diversification: ${strategy.overallDiversificationScore}%")
    }
  }

  fun toggleNlStrategyAutomation(strategyId: String, enable: Boolean) {
    repository.toggleNlStrategyAutomation(strategyId, enable)
    _notification.value = UiNotification(if (enable) "moomoo DataStream subscription active • Automated 0DTE execution ON" else "Strategy automation stopped")
  }

  fun simulateStreamExecution(strategyId: String) {
    repository.simulateStreamExecution(strategyId)
    val updated = repository.nlStrategies.value.find { it.id == strategyId }
    if (updated != null) {
      _notification.value = UiNotification("${updated.title}: ${updated.lastStreamTick}")
    }
  }

  fun getCorrelationMatrixForStrategy(strategy: NaturalLanguageStrategy): CorrelationMatrixData {
    return repository.computeCorrelationMatrix(strategy)
  }

  fun clearNotification() {
    _notification.value = null
  }
}
