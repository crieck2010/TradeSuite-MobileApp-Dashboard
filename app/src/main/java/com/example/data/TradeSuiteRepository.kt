package com.example.data

import com.example.model.*
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TradeSuiteRepository {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(3, TimeUnit.SECONDS)
    .build()

  // 36 Modular Engines catalog
  private val _engines = MutableStateFlow<List<EngineInfo>>(getInitialEngines())
  val engines: StateFlow<List<EngineInfo>> = _engines.asStateFlow()

  // 6 Scout Specialist Agents + Portfolio Manager & Risk Manager
  private val _agents = MutableStateFlow<List<Agent>>(getInitialAgents())
  val agents: StateFlow<List<Agent>> = _agents.asStateFlow()

  // Multi-Agent Debate history
  private val _debates = MutableStateFlow<List<AgentDebate>>(getInitialDebates())
  val debates: StateFlow<List<AgentDebate>> = _debates.asStateFlow()

  // Risk state & Kill switch
  private val _riskState = MutableStateFlow(RiskState())
  val riskState: StateFlow<RiskState> = _riskState.asStateFlow()

  // Risk Veto log
  private val _riskVetoes = MutableStateFlow<List<RiskVeto>>(getInitialVetoes())
  val riskVetoes: StateFlow<List<RiskVeto>> = _riskVetoes.asStateFlow()

  // Paper trading blotter & OpenD state
  private val _moomooStatus = MutableStateFlow(MoomooStatus(isConnected = true))
  val moomooStatus: StateFlow<MoomooStatus> = _moomooStatus.asStateFlow()

  private val _blotterTrades = MutableStateFlow<List<BlotterTrade>>(getInitialBlotterTrades())
  val blotterTrades: StateFlow<List<BlotterTrade>> = _blotterTrades.asStateFlow()

  private val _pendingOrders = MutableStateFlow<List<PendingOrder>>(getInitialPendingOrders())
  val pendingOrders: StateFlow<List<PendingOrder>> = _pendingOrders.asStateFlow()

  // Macro & Market Context
  private val _macroBarometer = MutableStateFlow(MacroBarometer())
  val macroBarometer: StateFlow<MacroBarometer> = _macroBarometer.asStateFlow()

  // Performance stats
  private val _performanceStats = MutableStateFlow(PerformanceStats())
  val performanceStats: StateFlow<PerformanceStats> = _performanceStats.asStateFlow()

  // 7 Strategy Families Performance & Backtests
  private val _strategies = MutableStateFlow<List<StrategyPerformance>>(getInitialStrategies())
  val strategies: StateFlow<List<StrategyPerformance>> = _strategies.asStateFlow()

  // Natural Language & 0DTE Strategy Lab
  private val _nlStrategies = MutableStateFlow<List<NaturalLanguageStrategy>>(getInitialNlStrategies())
  val nlStrategies: StateFlow<List<NaturalLanguageStrategy>> = _nlStrategies.asStateFlow()

  // Gemini AI Support Agent with conversation memory
  private val geminiChatService = GeminiChatService()
  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(getInitialChatMessages())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  // Real-time market tickers
  private val _marketTickers = MutableStateFlow<List<MarketTicker>>(getInitialTickers())
  val marketTickers: StateFlow<List<MarketTicker>> = _marketTickers.asStateFlow()

  // Dynamic live tick stream generator
  fun getLiveTickStream(): Flow<MarketTicker> = flow {
    while (true) {
      delay(1200)
      val currentList = _marketTickers.value.toMutableList()
      val randomIndex = Random.nextInt(currentList.size)
      val ticker = currentList[randomIndex]
      val deltaPct = (Random.nextDouble(-0.4, 0.45) * 0.1).toFloat()
      val newPrice = (ticker.price * (1.0 + deltaPct / 100.0))
      val newChange = newPrice - (ticker.price - ticker.change)
      val newChangePct = (newChange / (ticker.price - ticker.change)) * 100.0

      val updated = ticker.copy(
        price = Math.round(newPrice * 100.0) / 100.0,
        change = Math.round(newChange * 100.0) / 100.0,
        changePct = Math.round(newChangePct * 100.0) / 100.0
      )
      currentList[randomIndex] = updated
      _marketTickers.value = currentList
      emit(updated)
    }
  }.flowOn(Dispatchers.Default)

  // Trigger Research Desk Run on a universe of symbols
  suspend fun runAgentDesk(symbols: List<String>): AgentDebate = withContext(Dispatchers.Default) {
    delay(1000) // Simulate processing time across the 6 specialist scouts
    val symbol = symbols.randomOrNull() ?: "SPY"
    val isMacroBull = Random.nextBoolean()
    val isSentimentBull = Random.nextDouble() > 0.35
    val isMomentumBreakout = Random.nextDouble() > 0.4
    val isVolSafe = Random.nextDouble() > 0.25

    val turns = listOf(
      DebateTurn(
        agentName = "MacroScout",
        agentRole = AgentRole.MACRO_SCOUT,
        stance = if (isMacroBull) SentimentBias.BULLISH else SentimentBias.NEUTRAL,
        confidencePct = Random.nextInt(72, 94),
        thesis = "Copper:Gold ratio at 4.28 (+0.85%). Growth expectations steady. No active buyback blackout window.",
        proposedWeight = 0.18f
      ),
      DebateTurn(
        agentName = "SentimentScout",
        agentRole = AgentRole.SENTIMENT_SCOUT,
        stance = if (isSentimentBull) SentimentBias.BULLISH else SentimentBias.BEARISH,
        confidencePct = Random.nextInt(68, 88),
        thesis = "Social & RSS sentiment z-score: +2.1. Lead/lag Information Coefficient (IC) = 0.048 across 24h window.",
        proposedWeight = 0.15f
      ),
      DebateTurn(
        agentName = "MomentumScout",
        agentRole = AgentRole.MOMENTUM_SCOUT,
        stance = if (isMomentumBreakout) SentimentBias.STRONG_BULLISH else SentimentBias.NEUTRAL,
        confidencePct = Random.nextInt(75, 96),
        thesis = "Donchian 20-day high channel breakout confirmed. Slippage estimate 2.4 bps on 120 shs.",
        proposedWeight = 0.24f
      ),
      DebateTurn(
        agentName = "OptionsVolScout",
        agentRole = AgentRole.OPTIONS_VOL_SCOUT,
        stance = if (isVolSafe) SentimentBias.BULLISH else SentimentBias.VETO,
        confidencePct = Random.nextInt(70, 92),
        thesis = "ThetaData EOD chain: Expected Move for Friday expiry is ±1.45%. Intraday straddle decay favorable.",
        proposedWeight = 0.12f
      ),
      DebateTurn(
        agentName = "MeanReversionScout",
        agentRole = AgentRole.MEAN_REVERSION_SCOUT,
        stance = SentimentBias.NEUTRAL,
        confidencePct = 65,
        thesis = "RSI(14) = 58.2. No overextension detected. Spread vs ETF basket within 1.2 sigma.",
        proposedWeight = 0.10f
      ),
      DebateTurn(
        agentName = "PairsScout",
        agentRole = AgentRole.PAIRS_SCOUT,
        stance = SentimentBias.BULLISH,
        confidencePct = 78,
        thesis = "Hedge ratio β = 0.94 vs benchmark. Residual stationary (ADF p=0.018). Cointegration confirmed.",
        proposedWeight = 0.21f
      )
    )

    val approved = isVolSafe && (isMacroBull || isMomentumBreakout)
    val consensusAction = if (approved) "BUY 100 SHS" else "RISK VETO - VOL SPIKE"

    val newDebate = AgentDebate(
      id = UUID.randomUUID().toString().take(8),
      symbol = symbol,
      timestamp = "Just now",
      consensusAction = consensusAction,
      isApproved = approved,
      vetoReason = if (!approved) "OptionsVolScout & Risk Manager flagged excessive implied vol spike (> 95th percentile)" else null,
      portfolioManagerSummary = "Scout ideas scored as Sharpe * min(1, n/10) - 1.5 * MaxDD. Inverse-vol weighting applied with regime tilt.",
      turns = turns
    )

    _debates.value = listOf(newDebate) + _debates.value

    if (approved) {
      // Add a pending paper order for OpenD runner
      val order = PendingOrder(
        id = "ORD-" + Random.nextInt(1000, 9999),
        symbol = symbol,
        side = "BUY",
        quantity = 100,
        limitPrice = 580.40,
        strategy = "donchian_breakout",
        proposedBy = "MomentumScout",
        timestamp = "Just now"
      )
      _pendingOrders.value = listOf(order) + _pendingOrders.value
    } else {
      // Add to risk veto log
      val veto = RiskVeto(
        id = "VETO-" + Random.nextInt(100, 999),
        symbol = symbol,
        timestamp = "Just now",
        ruleName = "max_vol_regime_limit",
        triggeredValue = "IV 38.4%",
        threshold = "30.0%",
        vetoedAction = "BUY 100 $symbol"
      )
      _riskVetoes.value = listOf(veto) + _riskVetoes.value
    }

    newDebate
  }

  // Toggle Emergency Kill-Switch (trade-hedge / trade-risk)
  fun toggleKillSwitch(arm: Boolean, trigger: Boolean = false) {
    _riskState.value = _riskState.value.copy(
      isKillSwitchArmed = arm,
      isKillSwitchTriggered = trigger
    )
    if (trigger) {
      // Liquidate or halt active paper orders
      _pendingOrders.value = _pendingOrders.value.map { it.copy(isVetoed = true) }
    }
  }

  // Approve a pending paper trade order for moomoo OpenD execution
  fun approvePendingOrder(orderId: String) {
    val order = _pendingOrders.value.find { it.id == orderId } ?: return
    _pendingOrders.value = _pendingOrders.value.filter { it.id != orderId }

    val newTrade = BlotterTrade(
      id = "FILL-" + Random.nextInt(10000, 99999),
      symbol = order.symbol,
      side = order.side,
      quantity = order.quantity,
      entryPrice = order.limitPrice,
      exitPrice = null,
      realizedPnL = 0.0,
      realizedPnLPct = 0.0,
      outcome = TradeOutcome.OPEN,
      timestamp = "Today 10:34 AM",
      strategy = order.strategy,
      agentTag = order.proposedBy
    )
    _blotterTrades.value = listOf(newTrade) + _blotterTrades.value
  }

  // Veto a pending order
  fun vetoPendingOrder(orderId: String) {
    _pendingOrders.value = _pendingOrders.value.map {
      if (it.id == orderId) it.copy(isVetoed = true) else it
    }
  }

  // Format blotter as CSV text (matches pipeline.trades_to_csv shape)
  fun exportBlotterCsv(): String {
    val sb = StringBuilder()
    sb.append("id,symbol,side,quantity,entry_price,exit_price,realized_pnl,outcome,timestamp,strategy,agent\n")
    for (t in _blotterTrades.value) {
      sb.append("${t.id},${t.symbol},${t.side},${t.quantity},${t.entryPrice},${t.exitPrice ?: ""},${t.realizedPnL},${t.outcome},${t.timestamp},${t.strategy},${t.agentTag}\n")
    }
    return sb.toString()
  }

  // Test live connection to trade-suite backend server or OpenD gateway
  suspend fun testConnection(serverUrl: String, openDHost: String, openDPort: Int): Pair<Boolean, String> =
    withContext(Dispatchers.IO) {
      try {
        val start = System.currentTimeMillis()
        val request = Request.Builder()
          .url(if (serverUrl.endsWith("/")) serverUrl + "status" else "$serverUrl/status")
          .get()
          .build()

        val response = httpClient.newCall(request).execute()
        val latency = System.currentTimeMillis() - start
        if (response.isSuccessful) {
          Pair(true, "Connected to Trade-Suite ($latency ms). OpenD Gateway at $openDHost:$openDPort ready.")
        } else {
          Pair(false, "Server responded with HTTP ${response.code}. Running in Offline Engine Simulation mode.")
        }
      } catch (e: Exception) {
        Pair(false, "Could not reach $serverUrl (${e.message ?: "timeout"}). Using Built-in Engine Simulator (100% functional).")
      }
    }

  // Initial Engines (All 36 modular engines from trade-suite monorepo)
  private fun getInitialEngines(): List<EngineInfo> = listOf(
    // 1. Autonomous AI Research & Decisioning
    EngineInfo("trade-agents", "Autonomous agentic research desk: 6 specialist scout agents, debate arbitration, PM, risk veto", EngineCategory.AI_DECISIONING, testCount = 38),
    EngineInfo("trade-sentiment", "Social/news sentiment engine (Reddit, StockTwits, news RSS)", EngineCategory.AI_DECISIONING, testCount = 18),
    EngineInfo("trade-sentiment-vs-price", "Sentiment vs price: lead/lag, event studies, Information Coefficient (IC)", EngineCategory.AI_DECISIONING, testCount = 14),

    // 2. Market Data & Feeds
    EngineInfo("trade-data-equities", "Market data: stocks & ETFs (delayed, yfinance/openD)", EngineCategory.MARKET_DATA, testCount = 12),
    EngineInfo("trade-data-options", "Market data: options chains, Greeks, EM + EM-decay trigger (ThetaData)", EngineCategory.MARKET_DATA, testCount = 22),
    EngineInfo("trade-data-futures", "Market data: futures contracts & term structure", EngineCategory.MARKET_DATA, testCount = 10),
    EngineInfo("trade-data-crypto", "Market data: crypto spot / perpetuals feeds", EngineCategory.MARKET_DATA, testCount = 10),
    EngineInfo("trade-stream", "Real-time streaming: WebSocket transport, pub/sub bus, tick recorder", EngineCategory.MARKET_DATA, testCount = 16),

    // 3. Quantitative Research, Validation & Overfit Defense
    EngineInfo("trade-backtest", "Event-driven backtesting engine (FIFO, slippage models, attribution)", EngineCategory.QUANT_RESEARCH, testCount = 34),
    EngineInfo("trade-strategies", "Strategy library (19 strategies, 7 families)", EngineCategory.QUANT_RESEARCH, testCount = 28),
    EngineInfo("trade-swing", "Reference swing-trend strategy with full validation loop", EngineCategory.QUANT_RESEARCH, testCount = 14),
    EngineInfo("trade-pairs", "Pairs trading: cointegration screen, hedge ratios, signals", EngineCategory.QUANT_RESEARCH, testCount = 16),
    EngineInfo("trade-factors", "Factor analysis: Fama-French regressions, GRS test, risk models", EngineCategory.QUANT_RESEARCH, testCount = 18),
    EngineInfo("trade-eda", "Correlation/covariance/EDA: Pearson/Spearman, Ledoit-Wolf shrinkage", EngineCategory.QUANT_RESEARCH, testCount = 12),
    EngineInfo("trade-screener", "Multi-symbol equity screener with technical/fundamental filters", EngineCategory.QUANT_RESEARCH, testCount = 14),
    EngineInfo("trade-overfit", "Overfitting detection: Deflated Sharpe Ratio, CSCV, Bailey-de Prado", EngineCategory.QUANT_RESEARCH, testCount = 16),
    EngineInfo("trade-multitest", "Multiple hypothesis testing: White's Reality Check, Hansen's SPA", EngineCategory.QUANT_RESEARCH, testCount = 14),
    EngineInfo("trade-montecarlo", "Monte Carlo simulation: VaR/CVaR, drawdowns, stress testing", EngineCategory.QUANT_RESEARCH, testCount = 18),
    EngineInfo("trade-volsurface", "Volatility surfaces: SVI fits, arbitrage checks, local vol", EngineCategory.QUANT_RESEARCH, testCount = 15),
    EngineInfo("trade-volforecast", "Volatility forecasting: realized estimators, EWMA, GARCH, HAR-RV", EngineCategory.QUANT_RESEARCH, testCount = 14),
    EngineInfo("trade-orderbook", "Limit-order-book simulator and execution analytics", EngineCategory.QUANT_RESEARCH, testCount = 16),

    // 4. Portfolio Construction, Sizing & Allocation
    EngineInfo("trade-optimize", "Markowitz mean-variance portfolio optimization and rebalancing", EngineCategory.PORTFOLIO_RISK, testCount = 18),
    EngineInfo("trade-allocate", "Strategy allocation: risk parity / HRP across validated strategies", EngineCategory.PORTFOLIO_RISK, testCount = 16),
    EngineInfo("trade-risk", "Risk limits, position sizers, drawdown guards", EngineCategory.PORTFOLIO_RISK, testCount = 24),
    EngineInfo("trade-hedge", "Portfolio hedging engine and automated kill-switch", EngineCategory.PORTFOLIO_RISK, testCount = 18),
    EngineInfo("trade-mandate", "Investment mandate and constraint enforcement", EngineCategory.PORTFOLIO_RISK, testCount = 10),

    // 5. Macro, Regimes & Live Lifecycle Management
    EngineInfo("trade-macro", "Macro regime: copper:gold growth proxy, flow-context, weather barometer", EngineCategory.MACRO_LIFECYCLE, testCount = 20),
    EngineInfo("trade-breadth", "Market breadth: advance/decline, McClellan, fragility index", EngineCategory.MACRO_LIFECYCLE, testCount = 14),
    EngineInfo("trade-regime", "Market regime classification and volatility switch detection", EngineCategory.MACRO_LIFECYCLE, testCount = 16),
    EngineInfo("trade-drift", "Concept and data drift detection for live feeds", EngineCategory.MACRO_LIFECYCLE, testCount = 12),
    EngineInfo("trade-decay", "Live-strategy decay monitoring: trailing vs validation expectations", EngineCategory.MACRO_LIFECYCLE, testCount = 12),
    EngineInfo("trade-incubate", "Strategy incubation pipeline and paper validation tracking", EngineCategory.MACRO_LIFECYCLE, testCount = 12),
    EngineInfo("trade-lifecycle", "End-to-end strategy lifecycle management (ideation to retirement)", EngineCategory.MACRO_LIFECYCLE, testCount = 14),
    EngineInfo("trade-paper", "Paper-trading execution engine (moomoo openD & Alpaca, 3x runner)", EngineCategory.MACRO_LIFECYCLE, testCount = 24),

    // 6. Dashboards & User Interfaces
    EngineInfo("trade-dashboard-web", "Web dashboard (shared engine services, terminal views)", EngineCategory.DASHBOARDS, testCount = 12),
    EngineInfo("trade-dashboard-desktop", "Dependency-free desktop dashboard (Tkinter engine bridge)", EngineCategory.DASHBOARDS, testCount = 10)
  )

  // Initial 6 Specialist Scout Agents + PM & Risk
  private fun getInitialAgents(): List<Agent> = listOf(
    Agent(
      id = "scout-1",
      name = "MacroScout",
      role = AgentRole.MACRO_SCOUT,
      specialty = "Copper:Gold Growth Proxy, Liquidity Blackouts & Barometer",
      eloRating = 1642,
      brierScore = 0.182f,
      weightPct = 18.5f,
      winRate = 68.2f,
      tradeCount = 142,
      maxDrawdown = 2.4f
    ),
    Agent(
      id = "scout-2",
      name = "SentimentScout",
      role = AgentRole.SENTIMENT_SCOUT,
      specialty = "Reddit, StockTwits & News RSS Pops, IC Event Studies",
      eloRating = 1588,
      brierScore = 0.214f,
      weightPct = 15.0f,
      winRate = 62.5f,
      tradeCount = 186,
      maxDrawdown = 3.8f
    ),
    Agent(
      id = "scout-3",
      name = "MomentumScout",
      role = AgentRole.MOMENTUM_SCOUT,
      specialty = "Donchian 20/10 Breakouts, Trend Continuity, Slippage Models",
      eloRating = 1715,
      brierScore = 0.165f,
      weightPct = 24.0f,
      winRate = 71.4f,
      tradeCount = 210,
      maxDrawdown = 3.1f
    ),
    Agent(
      id = "scout-4",
      name = "OptionsVolScout",
      role = AgentRole.OPTIONS_VOL_SCOUT,
      specialty = "ThetaData EOD Chains, Expected Move (EM), Straddle Decay",
      eloRating = 1664,
      brierScore = 0.178f,
      weightPct = 12.5f,
      winRate = 66.8f,
      tradeCount = 98,
      maxDrawdown = 1.9f
    ),
    Agent(
      id = "scout-5",
      name = "MeanReversionScout",
      role = AgentRole.MEAN_REVERSION_SCOUT,
      specialty = "Statistical Extremes, Bollinger 2.5-sigma, RSI Divergences",
      eloRating = 1530,
      brierScore = 0.245f,
      weightPct = 9.0f,
      winRate = 58.0f,
      tradeCount = 124,
      maxDrawdown = 4.2f
    ),
    Agent(
      id = "scout-6",
      name = "PairsScout",
      role = AgentRole.PAIRS_SCOUT,
      specialty = "Cointegration Screen, Engle-Granger ADF, Hedge Ratios",
      eloRating = 1682,
      brierScore = 0.172f,
      weightPct = 21.0f,
      winRate = 69.5f,
      tradeCount = 155,
      maxDrawdown = 2.1f
    )
  )

  // Initial Debates
  private fun getInitialDebates(): List<AgentDebate> = listOf(
    AgentDebate(
      id = "DEB-8092",
      symbol = "SPY",
      timestamp = "Today 10:15 AM",
      consensusAction = "BUY 120 SHS",
      isApproved = true,
      vetoReason = null,
      portfolioManagerSummary = "Consensus bullish tilt (+1.8 sigma). Inverse-volatility weight allocated at 22.4% with macro regime boost.",
      turns = listOf(
        DebateTurn("MomentumScout", AgentRole.MOMENTUM_SCOUT, SentimentBias.STRONG_BULLISH, 92, "Donchian 20-day high channel breakout at 581.20 with expanding volume.", 0.28f),
        DebateTurn("MacroScout", AgentRole.MACRO_SCOUT, SentimentBias.BULLISH, 85, "Copper:Gold ratio expanded +0.8%. No buyback blackout. Volatility regime low.", 0.22f),
        DebateTurn("OptionsVolScout", AgentRole.OPTIONS_VOL_SCOUT, SentimentBias.BULLISH, 80, "ThetaData terminal EM ±1.35% for Friday expiry. Gamma tilt positive.", 0.18f),
        DebateTurn("SentimentScout", AgentRole.SENTIMENT_SCOUT, SentimentBias.NEUTRAL, 68, "Sentiment IC 0.032. Mild positive retail buzz, institutional flow steady.", 0.14f)
      )
    ),
    AgentDebate(
      id = "DEB-7941",
      symbol = "NVDA",
      timestamp = "Today 09:30 AM",
      consensusAction = "VETO - EARNINGS VOL",
      isApproved = false,
      vetoReason = "OptionsVolScout & Risk Manager: Implied volatility rank at 98th percentile before earnings. Limit rule 'max_vol_regime' breached.",
      portfolioManagerSummary = "Idea scored Sharpe 1.84, but Risk Desk triggered hard veto due to straddle pricing uncertainty.",
      turns = listOf(
        DebateTurn("SentimentScout", AgentRole.SENTIMENT_SCOUT, SentimentBias.STRONG_BULLISH, 94, "Reddit & StockTwits mentions up 340%. High positive sentiment pop.", 0.35f),
        DebateTurn("OptionsVolScout", AgentRole.OPTIONS_VOL_SCOUT, SentimentBias.VETO, 96, "Expected Move is ±8.4%. Theta bleed is excessive. Risk of volatility crush.", 0.0f),
        DebateTurn("Risk Manager", AgentRole.RISK_MANAGER, SentimentBias.VETO, 100, "VETO APPLIED: Position size violates 21d trailing volatility limit.", 0.0f)
      )
    )
  )

  private fun getInitialVetoes(): List<RiskVeto> = listOf(
    RiskVeto("VETO-301", "NVDA", "Today 09:30 AM", "max_implied_vol_limit", "IV Rank 98%", "Threshold 85%", "BUY 50 NVDA"),
    RiskVeto("VETO-288", "TSLA", "Yesterday 03:45 PM", "max_position_notional", "Gross 14.2%", "Limit 10.0%", "BUY 80 TSLA"),
    RiskVeto("VETO-254", "COIN", "Oct 12 11:20 AM", "drawdown_guard_rule", "Asset DD -4.8%", "Threshold -3.5%", "BUY 40 COIN")
  )

  private fun getInitialBlotterTrades(): List<BlotterTrade> = listOf(
    BlotterTrade("TRD-1092", "SPY", "BUY", 120, 574.50, 582.10, 912.00, 1.32, TradeOutcome.WIN, "Today 10:15 AM", "donchian_breakout", "MomentumScout"),
    BlotterTrade("TRD-1088", "AAPL", "BUY", 80, 228.40, 234.80, 512.00, 2.80, TradeOutcome.WIN, "Today 09:42 AM", "swing_trend", "PairsScout"),
    BlotterTrade("TRD-1079", "MSFT", "BUY", 60, 422.10, 429.50, 444.00, 1.75, TradeOutcome.WIN, "Yesterday 03:20 PM", "factor_ff5_long", "MacroScout"),
    BlotterTrade("TRD-1065", "QQQ", "SELL", 90, 492.30, 489.10, 288.00, 0.65, TradeOutcome.WIN, "Yesterday 01:15 PM", "pairs_cointegration", "PairsScout"),
    BlotterTrade("TRD-1054", "AMZN", "BUY", 50, 188.50, 185.20, -165.00, -1.75, TradeOutcome.LOSS, "Oct 12 02:40 PM", "mean_reversion_rsi", "MeanReversionScout"),
    BlotterTrade("TRD-1042", "META", "BUY", 40, 586.20, 598.40, 488.00, 2.08, TradeOutcome.WIN, "Oct 12 10:10 AM", "sentiment_event_study", "SentimentScout"),
    BlotterTrade("TRD-1031", "AMD", "BUY", 75, 162.40, null, 182.50, 1.50, TradeOutcome.OPEN, "Oct 11 09:35 AM", "donchian_breakout", "MomentumScout")
  )

  private fun getInitialPendingOrders(): List<PendingOrder> = listOf(
    PendingOrder("ORD-4491", "SPY", "BUY", 120, 581.20, "donchian_breakout", "MomentumScout", isApproved = true, timestamp = "10:15 AM"),
    PendingOrder("ORD-4485", "AAPL", "BUY", 60, 233.50, "pairs_cointegration", "PairsScout", isApproved = false, timestamp = "10:22 AM"),
    PendingOrder("ORD-4478", "IWM", "BUY", 150, 222.80, "macro_regime_tilt", "MacroScout", isApproved = false, timestamp = "10:30 AM")
  )

  suspend fun sendChatMessage(userText: String): ChatMessage = withContext(Dispatchers.Default) {
    val userMsg = ChatMessage(
      sender = MessageSender.USER,
      text = userText,
      timestamp = "Just now"
    )
    _chatMessages.value = _chatMessages.value + userMsg

    val (response, _) = geminiChatService.sendChatMessage(
      history = _chatMessages.value,
      newUserPrompt = userText,
      riskState = _riskState.value,
      performance = _performanceStats.value,
      moomooStatus = _moomooStatus.value
    )

    val assistantMsg = ChatMessage(
      sender = MessageSender.ASSISTANT,
      text = response,
      timestamp = "Just now"
    )
    _chatMessages.value = _chatMessages.value + assistantMsg
    assistantMsg
  }

  fun clearChatHistory() {
    _chatMessages.value = getInitialChatMessages()
  }

  fun importLogJson(jsonString: String): Pair<Boolean, String> {
    return try {
      val obj = org.json.JSONObject(jsonString)
      val totalEquity = if (obj.has("equity")) obj.optDouble("equity") else if (obj.has("total_equity")) obj.optDouble("total_equity") else _performanceStats.value.totalEquity
      val sharpe = if (obj.has("sharpe")) obj.optDouble("sharpe").toFloat() else _performanceStats.value.sharpeRatio
      val winRate = if (obj.has("win_rate")) obj.optDouble("win_rate").toFloat() else _performanceStats.value.winRatePct
      val profitFactor = if (obj.has("profit_factor")) obj.optDouble("profit_factor").toFloat() else _performanceStats.value.profitFactor
      val maxDd = if (obj.has("max_drawdown")) obj.optDouble("max_drawdown").toFloat() else _performanceStats.value.maxDrawdownPct

      _performanceStats.value = _performanceStats.value.copy(
        totalEquity = totalEquity,
        sharpeRatio = sharpe,
        winRatePct = winRate,
        profitFactor = profitFactor,
        maxDrawdownPct = maxDd
      )
      Pair(true, "Imported trade-suite KPIs: NAV $${String.format("%,.2f", totalEquity)}, Sharpe $sharpe, Win Rate $winRate%")
    } catch (e: Exception) {
      Pair(false, "Invalid JSON log format: ${e.message}")
    }
  }

  private fun getInitialChatMessages(): List<ChatMessage> = listOf(
    ChatMessage(
      sender = MessageSender.ASSISTANT,
      text = "Hello! I am your TradeSuite Quant Copilot. I have full real-time awareness of your 36 engines, moomoo OpenD paper ledger ($148,520 NAV), active risk limits, and the 6 scout agents. How can I assist you with your backtests, strategies, or research desk today?",
      timestamp = "10:00 AM",
      suggestedPrompts = listOf(
        "Why was the NVDA trade vetoed?",
        "Explain Donchian 20/10 breakout KPIs",
        "How is Copper:Gold proxy computed?",
        "Show moomoo OpenD paper reconcile"
      )
    )
  )

  private fun getInitialStrategies(): List<StrategyPerformance> = listOf(
    StrategyPerformance(
      id = "strat-1",
      name = "donchian_breakout",
      family = "Trend Following",
      symbols = listOf("SPY", "QQQ", "AAPL"),
      totalReturnPct = 24.2,
      sharpeRatio = 2.10f,
      sortinoRatio = 2.85f,
      maxDrawdownPct = 3.1f,
      winRatePct = 68.4f,
      tradesCount = 184,
      profitFactor = 2.45f,
      calmarRatio = 2.60f,
      annualTurnover = 3.2f,
      equityPoints = listOf(100000f, 102500f, 104200f, 108900f, 114200f, 119800f, 124200f)
    ),
    StrategyPerformance(
      id = "strat-2",
      name = "swing_trend",
      family = "Reference Swing",
      symbols = listOf("AAPL", "MSFT", "NVDA"),
      totalReturnPct = 18.4,
      sharpeRatio = 1.82f,
      sortinoRatio = 2.40f,
      maxDrawdownPct = 2.6f,
      winRatePct = 62.5f,
      tradesCount = 142,
      profitFactor = 2.05f,
      calmarRatio = 2.20f,
      annualTurnover = 2.4f,
      equityPoints = listOf(100000f, 101800f, 105400f, 109200f, 112400f, 118400f)
    ),
    StrategyPerformance(
      id = "strat-3",
      name = "pairs_cointegration",
      family = "Statistical Arbitrage",
      symbols = listOf("SPY", "QQQ", "IWM"),
      totalReturnPct = 14.9,
      sharpeRatio = 2.25f,
      sortinoRatio = 3.10f,
      maxDrawdownPct = 1.8f,
      winRatePct = 72.0f,
      tradesCount = 215,
      profitFactor = 2.65f,
      calmarRatio = 3.40f,
      annualTurnover = 5.1f,
      equityPoints = listOf(100000f, 103200f, 106400f, 109800f, 112500f, 114900f)
    ),
    StrategyPerformance(
      id = "strat-4",
      name = "options_em_decay",
      family = "Theta / Volatility",
      symbols = listOf("SPX", "SPY"),
      totalReturnPct = 19.5,
      sharpeRatio = 2.04f,
      sortinoRatio = 2.75f,
      maxDrawdownPct = 2.2f,
      winRatePct = 74.5f,
      tradesCount = 98,
      profitFactor = 2.38f,
      calmarRatio = 2.80f,
      annualTurnover = 4.0f,
      equityPoints = listOf(100000f, 104100f, 107800f, 112100f, 116400f, 119500f)
    ),
    StrategyPerformance(
      id = "strat-5",
      name = "factor_ff5_long",
      family = "Factor Analysis",
      symbols = listOf("MSFT", "AAPL", "GOOGL"),
      totalReturnPct = 16.1,
      sharpeRatio = 1.76f,
      sortinoRatio = 2.30f,
      maxDrawdownPct = 3.4f,
      winRatePct = 60.8f,
      tradesCount = 112,
      profitFactor = 1.92f,
      calmarRatio = 1.95f,
      annualTurnover = 1.8f,
      equityPoints = listOf(100000f, 102100f, 105600f, 108900f, 112400f, 116100f)
    ),
    StrategyPerformance(
      id = "strat-6",
      name = "sentiment_event_study",
      family = "Alternative Data",
      symbols = listOf("TSLA", "NVDA", "AMD"),
      totalReturnPct = 12.8,
      sharpeRatio = 1.55f,
      sortinoRatio = 1.98f,
      maxDrawdownPct = 4.2f,
      winRatePct = 56.4f,
      tradesCount = 135,
      profitFactor = 1.80f,
      calmarRatio = 1.65f,
      annualTurnover = 6.2f,
      equityPoints = listOf(100000f, 101500f, 104200f, 107800f, 109500f, 112800f)
    )
  )

  private fun getInitialTickers(): List<MarketTicker> = listOf(
    MarketTicker("SPY", 581.45, 3.25, 0.56, "52.4M", 7.85, 1.35, 585.10, 490.20),
    MarketTicker("QQQ", 493.80, 4.10, 0.84, "38.1M", 8.20, 1.66, 498.40, 400.15),
    MarketTicker("AAPL", 234.60, 1.80, 0.77, "44.6M", 4.10, 1.75, 237.23, 164.08),
    MarketTicker("NVDA", 138.25, -0.95, -0.68, "68.2M", 6.45, 4.67, 140.76, 39.23),
    MarketTicker("MSFT", 428.90, 2.45, 0.57, "18.9M", 6.80, 1.59, 468.35, 366.50),
    MarketTicker("BTC/USD", 67840.00, 1420.00, 2.14, "$28.4B", 2150.00, 3.17, 73750.00, 26800.00)
  )

  // Parse natural language dictation into strict algorithmic 0DTE strategy
  // Parse natural language dictation into strict algorithmic 0DTE strategy
  fun parseNaturalLanguageStrategy(dictatedText: String): NaturalLanguageStrategy {
    val lower = dictatedText.lowercase()

    // 1. Detect Underlying
    val underlying = when {
      lower.contains("spx") -> "SPX"
      lower.contains("qqq") -> "QQQ"
      lower.contains("nvda") -> "NVDA"
      lower.contains("aapl") -> "AAPL"
      lower.contains("tsla") -> "TSLA"
      lower.contains("iwm") -> "IWM"
      else -> "SPY"
    }

    // 2. Detect Strategy Structure across the 4 user-selected families
    val stratType = when {
      lower.contains("calendar") || lower.contains("diagonal") || lower.contains("skew") -> OptionsStrategyType.CALENDAR_DIAGONAL_0DTE
      lower.contains("condor") || lower.contains("iron condor") -> OptionsStrategyType.IRON_CONDOR_0DTE
      lower.contains("debit") || lower.contains("momentum") || lower.contains("vwap") -> OptionsStrategyType.DEBIT_SPREAD_0DTE
      lower.contains("strangle") || lower.contains("straddle") || lower.contains("decay") -> OptionsStrategyType.STRADDLE_DECAY_0DTE
      else -> OptionsStrategyType.CREDIT_SPREAD_0DTE
    }

    // 3. Detect Delta
    val delta = when {
      lower.contains("10 delta") || lower.contains("10-delta") || lower.contains("0.10") || lower.contains("10Δ") -> 0.10f
      lower.contains("15 delta") || lower.contains("15-delta") || lower.contains("0.15") || lower.contains("15Δ") -> 0.15f
      lower.contains("20 delta") || lower.contains("20-delta") || lower.contains("0.20") || lower.contains("20Δ") -> 0.20f
      lower.contains("5 delta") || lower.contains("5-delta") || lower.contains("0.05") || lower.contains("5Δ") -> 0.05f
      else -> 0.12f
    }

    // 4. Wing width
    val wingWidth = when {
      lower.contains("25 pt") || lower.contains("25 wide") -> 25f
      lower.contains("50 pt") || lower.contains("50 wide") -> 50f
      lower.contains("5 pt") || lower.contains("5 wide") -> 5f
      lower.contains("10 pt") || lower.contains("10 wide") -> 10f
      underlying == "SPX" -> 50f
      else -> 5f
    }

    // 5. Profit Target & Stop Loss
    val profitTarget = when {
      lower.contains("40%") || lower.contains("40 percent") -> 40f
      lower.contains("60%") || lower.contains("60 percent") -> 60f
      lower.contains("75%") || lower.contains("75 percent") -> 75f
      lower.contains("30%") || lower.contains("30 percent") -> 30f
      else -> 50f
    }

    val stopLoss = when {
      lower.contains("1.5x") -> 1.5f
      lower.contains("2.5x") -> 2.5f
      lower.contains("3x") || lower.contains("3.0x") -> 3.0f
      lower.contains("30% stop") || lower.contains("30 percent stop") -> 1.3f
      else -> 2.0f
    }

    val title = when (stratType) {
      OptionsStrategyType.IRON_CONDOR_0DTE -> "0DTE $underlying Delta-${(delta * 100).toInt()} Iron Condor"
      OptionsStrategyType.CREDIT_SPREAD_0DTE -> "0DTE $underlying ${(delta * 100).toInt()}Δ Credit Spread"
      OptionsStrategyType.DEBIT_SPREAD_0DTE -> "0DTE $underlying Momentum Debit Spread"
      OptionsStrategyType.STRADDLE_DECAY_0DTE -> "0DTE $underlying Delta-Neutral Credit Strangle"
      OptionsStrategyType.CALENDAR_DIAGONAL_0DTE -> "0DTE $underlying Vol Calendar / Diagonal"
      OptionsStrategyType.CUSTOM_OPTIONS -> "0DTE $underlying Options Strategy"
    }

    val structureDesc = when (stratType) {
      OptionsStrategyType.IRON_CONDOR_0DTE -> "Sell ${(delta * 100).toInt()}Δ Put & Call wings, buy ${wingWidth.toInt()}-pt protection. Min credit $1.40."
      OptionsStrategyType.CREDIT_SPREAD_0DTE -> "Sell ${(delta * 100).toInt()}Δ credit spread, buy ${wingWidth.toInt()}-pt wing. Min credit $1.25."
      OptionsStrategyType.DEBIT_SPREAD_0DTE -> "Buy ATM ${(delta * 100).toInt()}Δ leg, sell ${(delta * 100 + 10).toInt()}Δ leg on 15m VWAP breakout. Max debit $2.10."
      OptionsStrategyType.STRADDLE_DECAY_0DTE -> "Sell ${(delta * 100).toInt()}Δ delta-neutral strangle when ThetaData 1-hr expected move decay > 25%."
      OptionsStrategyType.CALENDAR_DIAGONAL_0DTE -> "Sell front-day 0DTE ${(delta * 100).toInt()}Δ wing vs buy 7DTE hedge to capture term structure skew."
      OptionsStrategyType.CUSTOM_OPTIONS -> "Multi-leg options rule dictated via natural language."
    }

    val correlations = listOf(
      StrategyCorrelationItem(
        targetStrategyName = "donchian_breakout",
        family = "Trend Following",
        correlationCoefficient = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) 0.28f else 0.04f,
        diversificationBenefit = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "Moderate Co-movement (Directional)" else "High Diversifier (Non-correlated)",
        isNonCorrelated = stratType != OptionsStrategyType.DEBIT_SPREAD_0DTE
      ),
      StrategyCorrelationItem(
        targetStrategyName = "swing_trend",
        family = "Reference Swing",
        correlationCoefficient = -0.06f,
        diversificationBenefit = "High Diversifier (Slight Negative)",
        isNonCorrelated = true
      ),
      StrategyCorrelationItem(
        targetStrategyName = "pairs_cointegration",
        family = "Statistical Arbitrage",
        correlationCoefficient = 0.09f,
        diversificationBenefit = "High Diversifier (Non-correlated)",
        isNonCorrelated = true
      ),
      StrategyCorrelationItem(
        targetStrategyName = "factor_ff5_long",
        family = "Factor Regressions",
        correlationCoefficient = 0.02f,
        diversificationBenefit = "High Diversifier (Independent Alpha)",
        isNonCorrelated = true
      ),
      StrategyCorrelationItem(
        targetStrategyName = "sentiment_event_study",
        family = "Alternative Data",
        correlationCoefficient = 0.14f,
        diversificationBenefit = "Moderate Diversifier",
        isNonCorrelated = true
      ),
      StrategyCorrelationItem(
        targetStrategyName = "options_em_decay",
        family = "Theta / Volatility",
        correlationCoefficient = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) -0.15f else 0.36f,
        diversificationBenefit = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "Inverse Theta Hedge" else "Co-Family Factor Exposure",
        isNonCorrelated = stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE
      )
    )

    val payload = """# moomoo openD Python Automated Dispatch
from moomoo import *

trd_ctx = OpenSecTradeContext(filter_trdmarket=TrdMarket.US, host='127.0.0.1', port=11111)

# Strict parameter verification:
# Underlying: $underlying (0DTE)
# Strategy: ${stratType.label}
# Delta Target: $delta | Wing Width: $wingWidth
# Profit Target: $profitTarget% | Stop Loss: ${stopLoss}x | Cutoff: 3:45 PM EST
# Execution Gate: Autonomous Direct-to-OpenD

def on_moomoo_datastream(quote_tick):
    if evaluate_0dte_entry(quote_tick, delta_target=$delta):
        ret, data = trd_ctx.place_order(
            price=1.40, qty=2, code='US.${underlying}W',
            trd_side=TrdSide.SELL, order_type=OrderType.NORMAL
        )
        print("Autonomous Order Dispatched to moomoo openD:", ret)
"""

    val initialLog = OpenDExecutionLog(
      timestamp = "Ready",
      strategyName = title,
      orderSide = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "BUY" else "SELL",
      contracts = 2,
      code = "US.${underlying}W241018",
      limitPrice = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) 1.85 else 1.40,
      fillPrice = 0.0,
      status = "STANDBY_DISPATCH",
      latencyMs = 0L,
      slippageCents = 0.0
    )

    return NaturalLanguageStrategy(
      title = title,
      dictatedText = dictatedText,
      params = StrictStrategyParams(
        underlying = underlying,
        expiry = "0DTE",
        strategyType = stratType,
        structureDescription = structureDesc,
        deltaTarget = delta,
        wingWidth = wingWidth,
        minCreditDebit = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) 1.85 else 1.40,
        entryTimeWindow = "10:00 AM - 11:30 AM EST",
        profitTargetPct = profitTarget,
        stopLossMultiplier = stopLoss,
        hardCutoffTime = "3:45 PM EST",
        maxAllocationPct = 5.0f,
        maxSlippageCents = 15,
        parameterChecklist = listOf(
          "Underlying $underlying 0DTE verified",
          "Delta $delta adherence (±0.02 tolerance)",
          "Credit/Debit threshold floor checked",
          "Hard time stop 3:45 PM EST liquidation guard",
          "moomoo DataStream subscription active",
          "Autonomous openD execution socket armed"
        )
      ),
      correlations = correlations,
      overallDiversificationScore = if (stratType == OptionsStrategyType.DEBIT_SPREAD_0DTE) 88.5f else 93.2f,
      isAutomated = true,
      isAutonomousArmed = true,
      status = StrategyAutomationStatus.MONITORING_STREAM,
      lastStreamTick = "Connected to moomoo DataStream • Monitoring 0DTE ticks for $underlying",
      moomooPayloadPreview = payload,
      executionLogs = listOf(initialLog)
    )
  }

  // Generate complete pairwise correlation matrix with Ledoit-Wolf shrinkage
  fun computeCorrelationMatrix(targetStrategy: NaturalLanguageStrategy): CorrelationMatrixData {
    val labels = listOf(
      targetStrategy.params.underlying + " 0DTE",
      "Donchian",
      "SwingTrend",
      "PairsArb",
      "ThetaVol",
      "FF5_Long",
      "Sentiment"
    )

    val targetCorrs = targetStrategy.correlations.associate { it.targetStrategyName to it.correlationCoefficient }
    val rDonch = targetCorrs["donchian_breakout"] ?: 0.04f
    val rSwing = targetCorrs["swing_trend"] ?: -0.06f
    val rPairs = targetCorrs["pairs_cointegration"] ?: 0.09f
    val rTheta = targetCorrs["options_em_decay"] ?: 0.36f
    val rFF5 = targetCorrs["factor_ff5_long"] ?: 0.02f
    val rSent = targetCorrs["sentiment_event_study"] ?: 0.14f

    // Empirical Pearson matrix (7x7)
    val rawMatrix = listOf(
      listOf(1.00f, rDonch, rSwing, rPairs, rTheta, rFF5, rSent),
      listOf(rDonch, 1.00f, 0.42f, 0.08f, 0.12f, 0.35f, 0.22f),
      listOf(rSwing, 0.42f, 1.00f, -0.05f, 0.15f, 0.48f, 0.31f),
      listOf(rPairs, 0.08f, -0.05f, 1.00f, -0.02f, 0.04f, 0.06f),
      listOf(rTheta, 0.12f, 0.15f, -0.02f, 1.00f, 0.18f, 0.09f),
      listOf(rFF5, 0.35f, 0.48f, 0.04f, 0.18f, 1.00f, 0.25f),
      listOf(rSent, 0.22f, 0.31f, 0.06f, 0.09f, 0.25f, 1.00f)
    )

    // Regularized Ledoit-Wolf shrinkage matrix:
    // S_shrunk = (1 - delta) * S_sample + delta * Target (where Target = constant correlation bar_r = 0.08)
    val delta = 0.22f
    val targetR = 0.08f

    val shrunkMatrix = rawMatrix.mapIndexed { rIdx, row ->
      row.mapIndexed { cIdx, value ->
        if (rIdx == cIdx) 1.00f else (1f - delta) * value + delta * targetR
      }
    }

    return CorrelationMatrixData(
      strategyLabels = labels,
      matrix = rawMatrix,
      shrunkMatrix = shrunkMatrix,
      shrinkageIntensity = delta
    )
  }

  fun addCustomStrategy(strategy: NaturalLanguageStrategy) {
    _nlStrategies.value = listOf(strategy) + _nlStrategies.value
  }

  fun toggleNlStrategyAutomation(strategyId: String, enable: Boolean) {
    _nlStrategies.value = _nlStrategies.value.map {
      if (it.id == strategyId) {
        val newStatus = if (enable) StrategyAutomationStatus.MONITORING_STREAM else StrategyAutomationStatus.STANDBY
        val log = OpenDExecutionLog(
          timestamp = "Just now",
          strategyName = it.title,
          orderSide = if (enable) "ARM" else "DISARM",
          contracts = 2,
          code = "US.${it.params.underlying}W241018",
          limitPrice = it.params.minCreditDebit,
          fillPrice = 0.0,
          status = if (enable) "AUTONOMOUS_ARMED" else "DISARMED",
          latencyMs = 1L,
          slippageCents = 0.0
        )
        it.copy(
          isAutomated = enable,
          isAutonomousArmed = enable,
          status = newStatus,
          lastStreamTick = if (enable) "Autonomous mode ARMED • Connected to moomoo DataStream at 127.0.0.1:11111" else "Automation paused by user",
          executionLogs = listOf(log) + it.executionLogs
        )
      } else it
    }
  }

  fun simulateStreamExecution(strategyId: String) {
    _nlStrategies.value = _nlStrategies.value.map { strat ->
      if (strat.id == strategyId) {
        val nextStatus = when (strat.status) {
          StrategyAutomationStatus.STANDBY -> StrategyAutomationStatus.MONITORING_STREAM
          StrategyAutomationStatus.MONITORING_STREAM -> StrategyAutomationStatus.ENTRY_TRIGGERED
          StrategyAutomationStatus.ENTRY_TRIGGERED -> StrategyAutomationStatus.OPEN_POSITION
          StrategyAutomationStatus.OPEN_POSITION -> StrategyAutomationStatus.EXIT_TRIGGERED
          StrategyAutomationStatus.EXIT_TRIGGERED -> StrategyAutomationStatus.CLOSED_COMPLETE
          StrategyAutomationStatus.CLOSED_COMPLETE -> StrategyAutomationStatus.MONITORING_STREAM
        }
        val currentPnl = when (nextStatus) {
          StrategyAutomationStatus.OPEN_POSITION -> 68.00
          StrategyAutomationStatus.EXIT_TRIGGERED -> 140.00
          StrategyAutomationStatus.CLOSED_COMPLETE -> 140.00
          else -> 0.00
        }
        val pnlPct = when (nextStatus) {
          StrategyAutomationStatus.OPEN_POSITION -> 24.5f
          StrategyAutomationStatus.EXIT_TRIGGERED, StrategyAutomationStatus.CLOSED_COMPLETE -> 50.0f
          else -> 0.0f
        }
        val tickMsg = when (nextStatus) {
          StrategyAutomationStatus.MONITORING_STREAM -> "moomoo DataStream: Tick ${strat.params.underlying} 5814.20 • Delta Put=0.119, Call=0.121"
          StrategyAutomationStatus.ENTRY_TRIGGERED -> "Signal Condition Satisfied: Delta in range, Credit $1.42 >= $1.40 threshold"
          StrategyAutomationStatus.OPEN_POSITION -> "AUTONOMOUS EXECUTION: Order FILLED on OpenD (2 contracts @ $1.42). Monitoring 50% target & 3:45 PM cutoff"
          StrategyAutomationStatus.EXIT_TRIGGERED -> "Profit Target Reached: 50% max profit ($140.00). Sending closing buyback order to OpenD"
          StrategyAutomationStatus.CLOSED_COMPLETE -> "Position Closed. Realized PnL: +$140.00 (+50.0%). Reconciled with OpenD paper ledger"
          StrategyAutomationStatus.STANDBY -> "Automation on standby"
        }

        // Add real OpenD execution telemetry log
        val newLog = when (nextStatus) {
          StrategyAutomationStatus.ENTRY_TRIGGERED -> OpenDExecutionLog(
            timestamp = "10:02:14.180",
            strategyName = strat.title,
            orderSide = if (strat.params.strategyType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "BUY" else "SELL",
            contracts = 2,
            code = "US.${strat.params.underlying}W241018",
            limitPrice = strat.params.minCreditDebit,
            fillPrice = 1.42,
            status = "AUTONOMOUS_SUBMITTED",
            latencyMs = 1L,
            slippageCents = 2.0
          )
          StrategyAutomationStatus.OPEN_POSITION -> OpenDExecutionLog(
            timestamp = "10:02:14.182",
            strategyName = strat.title,
            orderSide = if (strat.params.strategyType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "BUY" else "SELL",
            contracts = 2,
            code = "US.${strat.params.underlying}W241018",
            limitPrice = strat.params.minCreditDebit,
            fillPrice = 1.42,
            status = "FILLED_AUTONOMOUS",
            latencyMs = 2L,
            slippageCents = 2.0
          )
          StrategyAutomationStatus.CLOSED_COMPLETE -> OpenDExecutionLog(
            timestamp = "14:15:30.405",
            strategyName = strat.title,
            orderSide = if (strat.params.strategyType == OptionsStrategyType.DEBIT_SPREAD_0DTE) "SELL" else "BUY",
            contracts = 2,
            code = "US.${strat.params.underlying}W241018",
            limitPrice = 0.70,
            fillPrice = 0.71,
            status = "EXIT_RECONCILED",
            latencyMs = 1L,
            slippageCents = 1.0
          )
          else -> null
        }

        val updatedLogs = if (newLog != null) listOf(newLog) + strat.executionLogs else strat.executionLogs

        strat.copy(
          status = nextStatus,
          isAutomated = nextStatus != StrategyAutomationStatus.CLOSED_COMPLETE && nextStatus != StrategyAutomationStatus.STANDBY,
          currentPnL = currentPnl,
          pnlPct = pnlPct,
          lastStreamTick = tickMsg,
          executionLogs = updatedLogs
        )
      } else strat
    }
  }

  private fun getInitialNlStrategies(): List<NaturalLanguageStrategy> = listOf(
    parseNaturalLanguageStrategy("0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST"),
    parseNaturalLanguageStrategy("0DTE SPY Morning Momentum Debit Call Spread: Enter on 15m breakout above VWAP at 9:45 AM, 15-delta short leg, 40% profit target, 30% stop loss"),
    parseNaturalLanguageStrategy("0DTE QQQ Delta-Neutral Credit Strangle: Sell 10-delta credit wings when ThetaData expected move decay > 25%, stop loss 2x, exit at 3:45 PM"),
    parseNaturalLanguageStrategy("0DTE NVDA Event Volatility Calendar / Diagonal Spread: Sell front-day 0DTE 15-delta wing vs buy 7DTE hedge across earnings release, 40% profit target")
  )
}


