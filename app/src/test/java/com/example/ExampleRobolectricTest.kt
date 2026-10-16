package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TradeSuiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TradeSuite", appName)
  }

  @Test
  fun `trade suite repository initializes all 36 engines`() {
    val repo = TradeSuiteRepository()
    val engines = repo.engines.value
    assertEquals(36, engines.size)
    assertTrue(engines.all { it.isInstalled && it.isPassing })
  }

  @Test
  fun `all 6 specialist scout agents are registered with positive Elo`() {
    val repo = TradeSuiteRepository()
    val agents = repo.agents.value
    assertEquals(6, agents.size)
    assertTrue(agents.all { it.eloRating >= 1500 })
  }

  @Test
  fun `blotter export generates valid CSV header and records`() {
    val repo = TradeSuiteRepository()
    val csv = repo.exportBlotterCsv()
    assertTrue(csv.startsWith("id,symbol,side,quantity"))
    assertTrue(csv.contains("SPY"))
  }

  @Test
  fun `kill switch halts orders and toggles state`() {
    val repo = TradeSuiteRepository()
    repo.toggleKillSwitch(arm = true, trigger = true)
    assertTrue(repo.riskState.value.isKillSwitchTriggered)
    assertTrue(repo.pendingOrders.value.all { it.isVetoed })
  }

  @Test
  fun `strategies and backtest performance metrics loaded`() {
    val repo = TradeSuiteRepository()
    val strategies = repo.strategies.value
    assertTrue(strategies.isNotEmpty())
    assertTrue(strategies.any { it.name == "donchian_breakout" })
    assertTrue(strategies.all { it.sharpeRatio > 1.0f })
  }

  @Test
  fun `chat support agent initializes with memory and suggested prompts`() {
    val repo = TradeSuiteRepository()
    val messages = repo.chatMessages.value
    assertTrue(messages.isNotEmpty())
    assertEquals(com.example.model.MessageSender.ASSISTANT, messages.first().sender)
    assertTrue(messages.first().text.contains("TradeSuite Quant Copilot"))
  }

  @Test
  fun `json log importer updates performance KPIs`() {
    val repo = TradeSuiteRepository()
    val json = """{"equity": 154200.50, "sharpe": 2.24, "win_rate": 69.2, "profit_factor": 2.48, "max_drawdown": 2.95}"""
    val (success, msg) = repo.importLogJson(json)
    assertTrue(success)
    assertEquals(154200.50, repo.performanceStats.value.totalEquity, 0.01)
    assertEquals(2.24f, repo.performanceStats.value.sharpeRatio, 0.01f)
  }

  @Test
  fun `natural language 0dte strategy parsing into strict algorithm parameters`() {
    val repo = TradeSuiteRepository()
    val input = "0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST"
    val parsed = repo.parseNaturalLanguageStrategy(input)

    assertEquals("SPX", parsed.params.underlying)
    assertEquals(com.example.model.OptionsStrategyType.IRON_CONDOR_0DTE, parsed.params.strategyType)
    assertEquals(0.12f, parsed.params.deltaTarget, 0.001f)
    assertEquals(50f, parsed.params.wingWidth, 0.001f)
    assertEquals(50f, parsed.params.profitTargetPct, 0.001f)
    assertEquals(2.0f, parsed.params.stopLossMultiplier, 0.001f)
    assertTrue(parsed.correlations.isNotEmpty())
    assertTrue(parsed.overallDiversificationScore > 80f)
    assertTrue(parsed.moomooPayloadPreview.contains("OpenSecTradeContext"))
  }

  @Test
  fun `openD automation toggle and stream execution lifecycle`() {
    val repo = TradeSuiteRepository()
    val initialList = repo.nlStrategies.value
    assertTrue(initialList.isNotEmpty())
    val stratId = initialList.first().id

    // Test toggle automation
    repo.toggleNlStrategyAutomation(stratId, true)
    var strat = repo.nlStrategies.value.find { it.id == stratId }
    assertNotNull(strat)
    assertTrue(strat!!.isAutomated)
    assertEquals(com.example.model.StrategyAutomationStatus.MONITORING_STREAM, strat.status)

    // Test stream event transition
    repo.simulateStreamExecution(stratId)
    strat = repo.nlStrategies.value.find { it.id == stratId }
    assertEquals(com.example.model.StrategyAutomationStatus.ENTRY_TRIGGERED, strat!!.status)
  }
}

