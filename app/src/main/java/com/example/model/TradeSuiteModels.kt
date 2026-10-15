package com.example.model

enum class AgentRole(val displayName: String) {
  MACRO_SCOUT("Macro Scout"),
  SENTIMENT_SCOUT("Sentiment Scout"),
  MOMENTUM_SCOUT("Momentum Scout"),
  MEAN_REVERSION_SCOUT("Mean-Reversion Scout"),
  OPTIONS_VOL_SCOUT("Options/Vol Scout"),
  PAIRS_SCOUT("Pairs Scout"),
  PORTFOLIO_MANAGER("Portfolio Manager"),
  RISK_MANAGER("Risk Manager (Veto)")
}

data class Agent(
  val id: String,
  val name: String,
  val role: AgentRole,
  val specialty: String,
  val eloRating: Int,
  val brierScore: Float, // Lower is better calibration (0.0 to 1.0)
  val weightPct: Float,
  val winRate: Float,
  val tradeCount: Int,
  val maxDrawdown: Float,
  val isActive: Boolean = true
)

enum class SentimentBias {
  STRONG_BULLISH,
  BULLISH,
  NEUTRAL,
  BEARISH,
  STRONG_BEARISH,
  VETO
}

data class DebateTurn(
  val agentName: String,
  val agentRole: AgentRole,
  val stance: SentimentBias,
  val confidencePct: Int,
  val thesis: String,
  val proposedWeight: Float
)

data class AgentDebate(
  val id: String,
  val symbol: String,
  val timestamp: String,
  val consensusAction: String, // e.g. "BUY 120 SHS", "VETO - VOL SURGE", "PASS"
  val isApproved: Boolean,
  val vetoReason: String? = null,
  val portfolioManagerSummary: String,
  val turns: List<DebateTurn>
)

data class RiskState(
  val isKillSwitchArmed: Boolean = true,
  val isKillSwitchTriggered: Boolean = false,
  val netExposurePct: Float = 34.2f,
  val grossExposurePct: Float = 68.5f,
  val herfindahlConcentration: Float = 0.18f, // 0.0 to 1.0
  val trailingVol21d: Float = 14.8f,
  val maxDrawdownLimitPct: Float = 6.0f,
  val currentDrawdownPct: Float = 1.8f,
  val regimeConviction: Float = 82.5f,
  val regimeLabel: String = "Low Vol Expansion",
  val recentVetoCount: Int = 3
)

data class RiskVeto(
  val id: String,
  val symbol: String,
  val timestamp: String,
  val ruleName: String,
  val triggeredValue: String,
  val threshold: String,
  val vetoedAction: String
)

enum class TradeOutcome {
  WIN,
  LOSS,
  OPEN
}

data class BlotterTrade(
  val id: String,
  val symbol: String,
  val side: String, // "BUY" or "SELL"
  val quantity: Int,
  val entryPrice: Double,
  val exitPrice: Double?,
  val realizedPnL: Double,
  val realizedPnLPct: Double,
  val outcome: TradeOutcome,
  val timestamp: String,
  val strategy: String,
  val agentTag: String
)

data class PendingOrder(
  val id: String,
  val symbol: String,
  val side: String,
  val quantity: Int,
  val limitPrice: Double,
  val strategy: String,
  val proposedBy: String,
  val isApproved: Boolean = false,
  val isVetoed: Boolean = false,
  val timestamp: String
)

data class MoomooStatus(
  val isConnected: Boolean,
  val host: String = "127.0.0.1",
  val port: Int = 11111,
  val mode: String = "OpenD Paper Trading",
  val pingMs: Long = 12L,
  val accountEquity: Double = 148520.45,
  val cashBalance: Double = 52400.10,
  val buyingPower: Double = 104800.20,
  val lastHeartbeat: String = "1 sec ago"
)

enum class EngineCategory(val title: String) {
  AI_DECISIONING("Autonomous AI Research & Decisioning"),
  MARKET_DATA("Market Data & Feeds"),
  QUANT_RESEARCH("Quantitative Research & Overfit Defense"),
  PORTFOLIO_RISK("Portfolio, Sizing & Allocation"),
  MACRO_LIFECYCLE("Macro, Regimes & Live Lifecycle"),
  DASHBOARDS("Dashboards & User Interfaces")
}

data class EngineInfo(
  val moduleName: String,
  val role: String,
  val category: EngineCategory,
  val isInstalled: Boolean = true,
  val isPassing: Boolean = true,
  val version: String = "0.7.0",
  val testCount: Int = 12
)

data class MacroBarometer(
  val copperGoldRatio: Float = 4.28f,
  val copperGoldChangePct: Float = 0.85f,
  val weatherStatus: String = "CLEAR / EXPANSION", // CLEAR, CAUTIOUS, STORMY
  val buybackBlackoutActive: Boolean = false,
  val forcedSellingRiskPct: Float = 4.2f,
  val advanceDeclineRatio: Float = 1.62f,
  val mcClellanOscillator: Float = 42.5f,
  val thetaTerminalRunning: Boolean = true
)

data class PerformanceStats(
  val totalEquity: Double = 148520.45,
  val netProfit: Double = 23520.45,
  val netProfitPct: Double = 18.82,
  val winRatePct: Float = 64.8f,
  val profitFactor: Float = 2.14f,
  val sharpeRatio: Float = 1.95f,
  val sortinoRatio: Float = 2.68f,
  val calmarRatio: Float = 2.45f,
  val maxDrawdownPct: Float = 3.82f,
  val expectancy: Double = 142.50,
  val equityHistory: List<Float> = listOf(
    100000f, 101200f, 100800f, 103400f, 102900f, 105600f, 108200f,
    107400f, 111000f, 114500f, 113200f, 117800f, 122100f, 120500f,
    126400f, 129800f, 128900f, 134200f, 139100f, 137800f, 143500f, 148520f
  ),
  val drawdownHistory: List<Float> = listOf(
    0f, 0f, -0.4f, 0f, -0.5f, 0f, 0f, -0.7f, 0f, 0f, -1.1f, 0f, 0f,
    -1.3f, 0f, 0f, -0.7f, 0f, 0f, -0.9f, 0f, 0f
  )
)

data class MarketTicker(
  val symbol: String,
  val price: Double,
  val change: Double,
  val changePct: Double,
  val volume: String,
  val expectedMovePoints: Double,
  val expectedMovePct: Double,
  val high52w: Double,
  val low52w: Double
)

data class ConnectionConfig(
  val serverUrl: String = "http://10.0.2.2:8000",
  val openDHost: String = "127.0.0.1",
  val openDPort: Int = 11111,
  val apiKey: String = "",
  val isDemoMode: Boolean = true,
  val pollIntervalSec: Int = 3,
  val isConnected: Boolean = false,
  val lastLatencyMs: Long = 0L,
  val serverVersion: String = "trade-suite 0.7.0"
)

data class StrategyPerformance(
  val id: String,
  val name: String,
  val family: String,
  val symbols: List<String>,
  val totalReturnPct: Double,
  val sharpeRatio: Float,
  val sortinoRatio: Float,
  val maxDrawdownPct: Float,
  val winRatePct: Float,
  val tradesCount: Int,
  val profitFactor: Float,
  val calmarRatio: Float,
  val annualTurnover: Float,
  val equityPoints: List<Float>
)

enum class MessageSender {
  USER,
  ASSISTANT,
  SYSTEM
}

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: MessageSender,
  val text: String,
  val timestamp: String,
  val isError: Boolean = false,
  val suggestedPrompts: List<String> = emptyList()
)
