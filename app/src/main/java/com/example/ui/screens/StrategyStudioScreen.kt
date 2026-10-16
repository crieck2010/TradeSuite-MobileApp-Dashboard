package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.PairwiseCorrelationHeatmapCard
import com.example.ui.theme.*

@Composable
fun StrategyStudioScreen(
  strategies: List<NaturalLanguageStrategy>,
  isParsing: Boolean,
  onParseStrategy: (String) -> Unit,
  onToggleAutomation: (String, Boolean) -> Unit,
  onSimulateStreamEvent: (String) -> Unit,
  getCorrelationMatrix: ((NaturalLanguageStrategy) -> CorrelationMatrixData)? = null,
  modifier: Modifier = Modifier
) {
  var dictatedInputText by remember {
    mutableStateOf("0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST")
  }
  var selectedStrategyId by remember { mutableStateOf(strategies.firstOrNull()?.id ?: "") }
  var showPayloadDialog by remember { mutableStateOf(false) }
  var isVoiceListening by remember { mutableStateOf(false) }

  val activeStrategy = remember(strategies, selectedStrategyId) {
    strategies.find { it.id == selectedStrategyId } ?: strategies.firstOrNull()
  }

  val correlationMatrix = remember(activeStrategy) {
    if (activeStrategy != null) {
      getCorrelationMatrix?.invoke(activeStrategy) ?: run {
        val labels = listOf(
          activeStrategy.params.underlying + " 0DTE",
          "Donchian", "SwingTrend", "PairsArb", "ThetaVol", "FF5_Long", "Sentiment"
        )
        val targetCorrs = activeStrategy.correlations.associate { it.targetStrategyName to it.correlationCoefficient }
        val rDonch = targetCorrs["donchian_breakout"] ?: 0.04f
        val rSwing = targetCorrs["swing_trend"] ?: -0.06f
        val rPairs = targetCorrs["pairs_cointegration"] ?: 0.09f
        val rTheta = targetCorrs["options_em_decay"] ?: 0.36f
        val rFF5 = targetCorrs["factor_ff5_long"] ?: 0.02f
        val rSent = targetCorrs["sentiment_event_study"] ?: 0.14f

        val raw = listOf(
          listOf(1.00f, rDonch, rSwing, rPairs, rTheta, rFF5, rSent),
          listOf(rDonch, 1.00f, 0.42f, 0.08f, 0.12f, 0.35f, 0.22f),
          listOf(rSwing, 0.42f, 1.00f, -0.05f, 0.15f, 0.48f, 0.31f),
          listOf(rPairs, 0.08f, -0.05f, 1.00f, -0.02f, 0.04f, 0.06f),
          listOf(rTheta, 0.12f, 0.15f, -0.02f, 1.00f, 0.18f, 0.09f),
          listOf(rFF5, 0.35f, 0.48f, 0.04f, 0.18f, 1.00f, 0.25f),
          listOf(rSent, 0.22f, 0.31f, 0.06f, 0.09f, 0.25f, 1.00f)
        )
        val delta = 0.22f
        val shrunk = raw.mapIndexed { rIdx, row ->
          row.mapIndexed { cIdx, v ->
            if (rIdx == cIdx) 1.00f else (1f - delta) * v + delta * 0.08f
          }
        }
        CorrelationMatrixData(labels, raw, shrunk, delta)
      }
    } else null
  }

  // 4 Priority 0DTE Options Families selected by user
  val promptPresets = listOf(
    "0DTE SPX Iron Condor (12Δ wings, 50-pt wide, 50% target, 2x stop)",
    "0DTE SPY Momentum Debit Call Spread (15m VWAP breakout, 40% target)",
    "0DTE QQQ Delta-Neutral Credit Strangle (10Δ wings on EM decay > 25%)",
    "0DTE NVDA Vol Calendar / Diagonal Spread (Front 0DTE vs 7DTE skew, 40% target)"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Natural Language Suggestion & Dictation Input Studio
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(if (isVoiceListening) QuantRed.copy(alpha = 0.25f) else QuantCyan.copy(alpha = 0.2f))
                  .border(1.dp, if (isVoiceListening) QuantRed else QuantCyan, CircleShape)
                  .clickable {
                    isVoiceListening = !isVoiceListening
                    if (isVoiceListening) {
                      dictatedInputText = "0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST"
                    }
                  }
                  .testTag("dictate_mic_button"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Mic,
                  contentDescription = "Voice Dictation",
                  tint = if (isVoiceListening) QuantRed else QuantCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "NATURAL LANGUAGE 0DTE STUDIO",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  color = QuantCyan
                )
                Text(
                  text = if (isVoiceListening) "Listening for dictation... Tap mic to finalize" else "Dictate 0DTE credit/debit strategies • Auto-parameterize & correlate",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isVoiceListening) QuantAmber else QuantTextSecondary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Preset suggestion chips covering all 4 priority structures
          Text(
            text = "4 Priority 0DTE Options Families:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = QuantTextMuted
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(promptPresets) { preset ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(QuantDarkBackground)
                  .border(1.dp, QuantBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    dictatedInputText = when {
                      preset.contains("SPX Iron Condor") ->
                        "0DTE SPX Iron Condor: Sell 12-delta put and call wings with 50-pt wide wings at 10:00 AM EST, profit target 50%, stop loss 2x credit, hard close at 3:45 PM EST"
                      preset.contains("SPY Momentum") ->
                        "0DTE SPY Morning Momentum Debit Call Spread: Enter on 15m breakout above VWAP at 9:45 AM, 15-delta short leg, 40% profit target, 30% stop loss"
                      preset.contains("QQQ Delta-Neutral") ->
                        "0DTE QQQ Delta-Neutral Credit Strangle: Sell 10-delta credit wings when ThetaData expected move decay > 25%, stop loss 2x, exit at 3:45 PM"
                      else ->
                        "0DTE NVDA Event Volatility Calendar / Diagonal Spread: Sell front-day 0DTE 15-delta wing vs buy 7DTE hedge across earnings release, 40% profit target"
                    }
                  }
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text(
                  text = preset,
                  fontSize = 10.sp,
                  color = QuantTextSecondary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Text area for natural language strategy
          OutlinedTextField(
            value = dictatedInputText,
            onValueChange = { dictatedInputText = it },
            placeholder = { Text("Dictate or type your strategy: e.g. 'Sell 10-delta SPX put and call credit spread at 10:00 AM...'", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = QuantDarkBackground,
              unfocusedContainerColor = QuantDarkBackground,
              focusedBorderColor = QuantCyan,
              unfocusedBorderColor = QuantBorder,
              focusedTextColor = QuantTextPrimary,
              unfocusedTextColor = QuantTextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .testTag("strategy_dictation_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { onParseStrategy(dictatedInputText) },
            enabled = dictatedInputText.isNotBlank() && !isParsing,
            colors = ButtonDefaults.buttonColors(containerColor = QuantCyan, contentColor = Color(0xFF00363D)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("analyze_strategy_button")
          ) {
            if (isParsing) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF00363D), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Compiling Strategy & Correlating...", fontWeight = FontWeight.Bold)
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("ANALYZE & COMPILE TO STRICT ALGO", fontWeight = FontWeight.Black)
            }
          }
        }
      }
    }

    // 2. Active Strategy Selector
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "YOUR ACTIVE 0DTE STRATEGIES",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )
        Text(
          text = "${strategies.size} strategies configured",
          style = MaterialTheme.typography.labelSmall,
          color = QuantCyan
        )
      }
    }

    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(strategies) { strat ->
          FilterChip(
            selected = strat.id == selectedStrategyId,
            onClick = { selectedStrategyId = strat.id },
            label = {
              Text(
                text = "${strat.params.underlying} ${strat.params.strategyType.label}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (strat.isAutomated) QuantGreen else QuantAmber)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = QuantCyan,
              selectedLabelColor = Color(0xFF00363D),
              containerColor = QuantDarkSurfaceVariant,
              labelColor = QuantTextSecondary
            )
          )
        }
      }
    }

    // 3. Strict Parameter Adherence & Execution Rules Card
    activeStrategy?.let { strat ->
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strat.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = QuantTextPrimary
                )
                Text(
                  text = strat.params.structureDescription,
                  fontSize = 12.sp,
                  color = QuantCyan
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Automation status badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    when (strat.status) {
                      StrategyAutomationStatus.OPEN_POSITION, StrategyAutomationStatus.ENTRY_TRIGGERED -> QuantGreen.copy(alpha = 0.2f)
                      StrategyAutomationStatus.MONITORING_STREAM -> QuantCyan.copy(alpha = 0.2f)
                      StrategyAutomationStatus.CLOSED_COMPLETE -> QuantPurple.copy(alpha = 0.2f)
                      else -> QuantDarkBackground
                    }
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = strat.status.label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = when (strat.status) {
                    StrategyAutomationStatus.OPEN_POSITION, StrategyAutomationStatus.ENTRY_TRIGGERED -> QuantGreen
                    StrategyAutomationStatus.MONITORING_STREAM -> QuantCyan
                    StrategyAutomationStatus.CLOSED_COMPLETE -> QuantPurple
                    else -> QuantTextMuted
                  }
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parameter Grid
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(QuantDarkBackground)
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Underlying", fontSize = 9.sp, color = QuantTextMuted)
                Text("${strat.params.underlying} (0DTE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantTextPrimary, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Target Delta", fontSize = 9.sp, color = QuantTextMuted)
                Text("${strat.params.deltaTarget}Δ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantCyan, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Profit Target", fontSize = 9.sp, color = QuantTextMuted)
                Text("${strat.params.profitTargetPct}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantGreen, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Stop Loss", fontSize = 9.sp, color = QuantTextMuted)
                Text("${strat.params.stopLossMultiplier}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantRed, fontFamily = FontFamily.Monospace)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Cutoff Stop", fontSize = 9.sp, color = QuantTextMuted)
                Text(strat.params.hardCutoffTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuantAmber, fontFamily = FontFamily.Monospace)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Strict Adherence Checklist
            Text(
              text = "STRICT PARAMETER ADHERENCE CHECKLIST",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = QuantTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            strat.params.parameterChecklist.forEach { checkItem ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = QuantGreen, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = checkItem, fontSize = 11.sp, color = QuantTextSecondary)
              }
            }
          }
        }
      }

      // 4. Interactive Pairwise Pearson Correlation Heatmap Card (Ledoit-Wolf Shrinkage)
      correlationMatrix?.let { matrix ->
        item {
          PairwiseCorrelationHeatmapCard(matrixData = matrix)
        }
      }

      // 5. Individual Cross-Strategy Correlation Breakdown List
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "CROSS-STRATEGY CORRELATION DIVERSIFIER",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Black,
                  color = QuantCyan
                )
                Text(
                  text = "Quant analysis vs running strategies (trade-eda / trade-allocate)",
                  fontSize = 11.sp,
                  color = QuantTextSecondary
                )
              }

              // Overall Non-correlation score pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(QuantGreen.copy(alpha = 0.2f))
                  .border(1.dp, QuantGreen, RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${strat.overallDiversificationScore}% NON-CORRELATED",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = QuantGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "0DTE intraday strategies profit from rapid intraday decay without holding overnight gap risk, producing low correlation (ρ < 0.15) to your equity swing & trend engines.",
              fontSize = 11.sp,
              color = QuantTextSecondary,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            strat.correlations.forEach { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (item.isNonCorrelated) QuantGreen else QuantAmber)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = item.targetStrategyName,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = QuantTextPrimary
                    )
                    Text(text = item.family, fontSize = 9.sp, color = QuantTextMuted)
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${if (item.correlationCoefficient >= 0) "+" else ""}${String.format("%.2f", item.correlationCoefficient)} ρ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (item.isNonCorrelated) QuantGreen else QuantAmber
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(QuantDarkBackground)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = item.diversificationBenefit,
                      fontSize = 9.sp,
                      color = QuantCyan,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 6. Automated Execution via moomoo openD & moomoo DataStream
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (strat.isAutomated) QuantDarkSurfaceElevated else QuantDarkSurfaceVariant
          ),
          shape = RoundedCornerShape(14.dp),
          border = if (strat.isAutomated) androidx.compose.foundation.BorderStroke(1.5.dp, QuantGreen) else null,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Bolt, contentDescription = null, tint = if (strat.isAutomated) QuantGreen else QuantCyan, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "AUTONOMOUS MOOMOO OPEND DISPATCH",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = QuantTextPrimary
                  )
                }
                Text(
                  text = "Direct-to-openD execution upon stream signal trigger (127.0.0.1:11111)",
                  fontSize = 11.sp,
                  color = QuantTextSecondary
                )
              }

              Switch(
                checked = strat.isAutomated,
                onCheckedChange = { onToggleAutomation(strat.id, it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = QuantGreen,
                  checkedTrackColor = QuantGreen.copy(alpha = 0.3f),
                  uncheckedThumbColor = QuantTextMuted,
                  uncheckedTrackColor = QuantBorder
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live stream telemetry status box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(QuantDarkBackground)
                .padding(12.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (strat.isAutomated) QuantGreen else QuantTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "moomoo DataStream Status:",
                      fontSize = 10.sp,
                      color = QuantTextMuted,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  if (strat.currentPnL != 0.0) {
                    Text(
                      text = "+$${String.format("%.2f", strat.currentPnL)} (+${strat.pnlPct}%)",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = QuantGreen
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = strat.lastStreamTick,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (strat.isAutomated) QuantCyan else QuantTextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Test stream cycle & inspect payload
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { onSimulateStreamEvent(strat.id) },
                colors = ButtonDefaults.buttonColors(containerColor = QuantCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("simulate_execution_button")
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = when (strat.status) {
                    StrategyAutomationStatus.STANDBY -> "Start DataStream"
                    StrategyAutomationStatus.MONITORING_STREAM -> "Signal Condition Match"
                    StrategyAutomationStatus.ENTRY_TRIGGERED -> "Autonomous Fill (OpenD)"
                    StrategyAutomationStatus.OPEN_POSITION -> "Trigger Exit (50%)"
                    StrategyAutomationStatus.EXIT_TRIGGERED -> "Reconcile Trade"
                    StrategyAutomationStatus.CLOSED_COMPLETE -> "Restart Cycle"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedButton(
                onClick = { showPayloadDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = QuantCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("OpenD Payload", color = QuantTextPrimary, fontSize = 11.sp)
              }
            }

            // 7. Autonomous Execution Logs table
            if (strat.executionLogs.isNotEmpty()) {
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "OPEND AUTONOMOUS EXECUTION AUDIT LOG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = QuantTextMuted
              )
              Spacer(modifier = Modifier.height(6.dp))

              strat.executionLogs.take(4).forEach { log ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(QuantDarkBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = "${log.orderSide} ${log.contracts}x ${log.code}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = QuantTextPrimary
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "${log.latencyMs}ms latency",
                        fontSize = 9.sp,
                        color = QuantCyan,
                        fontFamily = FontFamily.Monospace
                      )
                    }
                    Text(
                      text = "${log.timestamp} • Limit: $${String.format("%.2f", log.limitPrice)} | Fill: $${String.format("%.2f", log.fillPrice)}",
                      fontSize = 9.sp,
                      color = QuantTextMuted,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(if (log.status.contains("FILLED")) QuantGreen.copy(alpha = 0.2f) else QuantCyan.copy(alpha = 0.2f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = log.status,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (log.status.contains("FILLED")) QuantGreen else QuantCyan
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // OpenD API Payload Inspection Dialog
  if (showPayloadDialog && activeStrategy != null) {
    AlertDialog(
      onDismissRequest = { showPayloadDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Code, contentDescription = null, tint = QuantCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text("moomoo openD Python Dispatch", fontWeight = FontWeight.Bold, color = QuantTextPrimary)
        }
      },
      text = {
        Column {
          Text(
            text = "Code payload dispatched automatically via OpenD socket (host 127.0.0.1 : 11111):",
            fontSize = 11.sp,
            color = QuantTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(10.dp)
          ) {
            Text(
              text = activeStrategy.moomooPayloadPreview,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = QuantCyan,
              lineHeight = 14.sp
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showPayloadDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = QuantCyan, contentColor = Color(0xFF00363D))
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      containerColor = QuantDarkSurface,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
