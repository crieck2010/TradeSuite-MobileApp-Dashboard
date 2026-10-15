package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.KillSwitchConfirmDialog
import com.example.ui.components.LiveTickerTape
import com.example.ui.components.QuantTopBar
import com.example.ui.screens.*
import com.example.ui.theme.QuantCyan
import com.example.ui.theme.QuantDarkBackground
import com.example.ui.theme.QuantDarkSurface
import com.example.ui.theme.QuantTextMuted
import com.example.ui.theme.QuantTextPrimary
import com.example.ui.theme.TradeSuiteTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.TradeSuiteViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: TradeSuiteViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      TradeSuiteTheme(darkTheme = true) {
        val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
        val riskState by viewModel.riskState.collectAsStateWithLifecycle()
        val moomooStatus by viewModel.moomooStatus.collectAsStateWithLifecycle()
        val marketTickers by viewModel.marketTickers.collectAsStateWithLifecycle()
        val isDeskRunning by viewModel.deskRunInProgress.collectAsStateWithLifecycle()
        val showKillSwitchDialog by viewModel.showKillSwitchDialog.collectAsStateWithLifecycle()
        val notification by viewModel.notification.collectAsStateWithLifecycle()

        val agents by viewModel.agents.collectAsStateWithLifecycle()
        val debates by viewModel.debates.collectAsStateWithLifecycle()
        val riskVetoes by viewModel.riskVetoes.collectAsStateWithLifecycle()
        val blotterTrades by viewModel.blotterTrades.collectAsStateWithLifecycle()
        val pendingOrders by viewModel.pendingOrders.collectAsStateWithLifecycle()
        val macroBarometer by viewModel.macroBarometer.collectAsStateWithLifecycle()
        val performanceStats by viewModel.performanceStats.collectAsStateWithLifecycle()
        val strategies by viewModel.strategies.collectAsStateWithLifecycle()
        val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
        val isChatSending by viewModel.isChatSending.collectAsStateWithLifecycle()
        val engines by viewModel.engines.collectAsStateWithLifecycle()
        val connectionConfig by viewModel.connectionConfig.collectAsStateWithLifecycle()
        val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
        val connectionResult by viewModel.connectionResult.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(notification) {
          notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearNotification()
          }
        }

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(QuantDarkBackground),
          containerColor = QuantDarkBackground,
          snackbarHost = {
            SnackbarHost(
              hostState = snackbarHostState,
              modifier = Modifier.padding(16.dp)
            ) { data ->
              Snackbar(
                snackbarData = data,
                containerColor = QuantDarkSurface,
                contentColor = QuantTextPrimary,
                actionColor = QuantCyan
              )
            }
          },
          topBar = {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
              QuantTopBar(
                riskState = riskState,
                moomooStatus = moomooStatus,
                onKillSwitchClick = { viewModel.setKillSwitchDialogVisible(true) },
                onDeskRunClick = { viewModel.triggerDeskRun() },
                isDeskRunning = isDeskRunning,
                onEnginesClick = { viewModel.selectTab(MainTab.ENGINES) },
                onConnectClick = { viewModel.selectTab(MainTab.CONNECTION) }
              )
              LiveTickerTape(tickers = marketTickers)
            }
          },
          bottomBar = {
            NavigationBar(
              containerColor = QuantDarkSurface,
              modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .testTag("main_bottom_nav")
            ) {
              val tabs = listOf(
                Pair(MainTab.KPIS, Icons.Default.Insights),
                Pair(MainTab.AGENTS, Icons.Default.Psychology),
                Pair(MainTab.RISK, Icons.Default.Shield),
                Pair(MainTab.BLOTTER, Icons.AutoMirrored.Filled.ReceiptLong),
                Pair(MainTab.MACRO, Icons.AutoMirrored.Filled.ShowChart),
                Pair(MainTab.COPILOT, Icons.Default.SmartToy)
              )

              tabs.forEach { (tab, icon) ->
                val selected = activeTab == tab
                NavigationBarItem(
                  selected = selected,
                  onClick = { viewModel.selectTab(tab) },
                  icon = {
                    Icon(
                      imageVector = icon,
                      contentDescription = tab.title,
                      tint = if (selected) Color(0xFF00363D) else QuantTextMuted
                    )
                  },
                  label = {
                    Text(
                      text = tab.title,
                      fontSize = 9.sp,
                      maxLines = 1,
                      color = if (selected) QuantCyan else QuantTextMuted
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    indicatorColor = QuantCyan,
                    selectedIconColor = Color(0xFF00363D),
                    selectedTextColor = QuantCyan,
                    unselectedIconColor = QuantTextMuted,
                    unselectedTextColor = QuantTextMuted
                  ),
                  modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            Crossfade(targetState = activeTab, label = "tab_fade") { tab ->
              when (tab) {
                MainTab.KPIS -> KpiDashboardScreen(
                  performance = performanceStats,
                  strategies = strategies,
                  agents = agents,
                  onImportLogJson = { json -> viewModel.importLogJson(json) }
                )
                MainTab.AGENTS -> AgentDeskScreen(
                  agents = agents,
                  debates = debates,
                  isDeskRunning = isDeskRunning,
                  onRunDesk = { symbols -> viewModel.triggerDeskRun(symbols) }
                )
                MainTab.RISK -> RiskMonitorScreen(
                  riskState = riskState,
                  riskVetoes = riskVetoes,
                  onKillSwitchClick = { viewModel.setKillSwitchDialogVisible(true) },
                  onArmToggle = { arm -> viewModel.armKillSwitch(arm) }
                )
                MainTab.BLOTTER -> BlotterScreen(
                  moomooStatus = moomooStatus,
                  blotterTrades = blotterTrades,
                  pendingOrders = pendingOrders,
                  onApproveOrder = { id -> viewModel.approvePendingOrder(id) },
                  onVetoOrder = { id -> viewModel.vetoPendingOrder(id) },
                  onExportCsv = { viewModel.exportCsv() }
                )
                MainTab.MACRO -> MarketMacroScreen(
                  macro = macroBarometer,
                  stats = performanceStats
                )
                MainTab.COPILOT -> ChatSupportScreen(
                  messages = chatMessages,
                  isSending = isChatSending,
                  onSendMessage = { text -> viewModel.sendChatMessage(text) },
                  onClearChat = { viewModel.clearChat() }
                )
                MainTab.ENGINES -> EnginesDoctorScreen(
                  engines = engines
                )
                MainTab.CONNECTION -> SettingsConnectionScreen(
                  config = connectionConfig,
                  isTesting = isTestingConnection,
                  connectionResult = connectionResult,
                  onSaveSettings = { url, host, port, key, isDemo ->
                    viewModel.updateConnectionSettings(url, host, port, key, isDemo)
                  },
                  onTestConnection = { viewModel.testConnectionNow() },
                  onSetDemoMode = { isDemo -> viewModel.setDemoMode(isDemo) }
                )
              }
            }
          }
        }

        // Emergency Kill-Switch Confirmation Dialog
        if (showKillSwitchDialog) {
          KillSwitchConfirmDialog(
            isTriggered = riskState.isKillSwitchTriggered,
            onDismiss = { viewModel.setKillSwitchDialogVisible(false) },
            onConfirmHalt = { viewModel.triggerEmergencyKillSwitch() },
            onReset = {
              viewModel.resetKillSwitch()
              viewModel.setKillSwitchDialogVisible(false)
            }
          )
        }
      }
    }
  }
}
