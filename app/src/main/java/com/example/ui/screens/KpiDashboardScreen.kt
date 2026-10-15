package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.MetricGaugeCard
import com.example.ui.theme.*

@Composable
fun KpiDashboardScreen(
  performance: PerformanceStats,
  strategies: List<StrategyPerformance>,
  agents: List<Agent>,
  onImportLogJson: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedStrategyId by remember { mutableStateOf(strategies.firstOrNull()?.id ?: "") }
  var showImportDialog by remember { mutableStateOf(false) }
  var jsonInputText by remember {
    mutableStateOf(
      """{
  "source": "paper",
  "equity": 154200.50,
  "sharpe": 2.24,
  "win_rate": 69.2,
  "profit_factor": 2.48,
  "max_drawdown": 2.95,
  "trades_count": 240
}"""
    )
  }

  val selectedStrategy = remember(strategies, selectedStrategyId) {
    strategies.find { it.id == selectedStrategyId } ?: strategies.firstOrNull()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Executive Portfolio KPI Scorecard
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
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = QuantCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "TRADE-SUITE KPI DASHBOARD",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  color = QuantCyan
                )
              }
              Text(
                text = "Quantitative performance, backtests & agent calibration",
                style = MaterialTheme.typography.bodySmall,
                color = QuantTextSecondary
              )
            }

            Button(
              onClick = { showImportDialog = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = QuantCyan.copy(alpha = 0.15f),
                contentColor = QuantCyan
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("import_logs_button")
            ) {
              Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("SYNC LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Primary KPIs row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(QuantDarkBackground)
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("PORTFOLIO NAV", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "$${String.format("%,.2f", performance.totalEquity)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = QuantGreen
              )
              Text("+${performance.netProfitPct}% total gain", fontSize = 10.sp, color = QuantGreen)
            }

            Column {
              Text("SHARPE RATIO", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "${performance.sharpeRatio}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = QuantCyan
              )
              Text("Sortino: ${performance.sortinoRatio}", fontSize = 10.sp, color = QuantTextSecondary)
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("MAX DRAWDOWN", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "-${performance.maxDrawdownPct}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = QuantRed
              )
              Text("Safe < -6.0%", fontSize = 10.sp, color = QuantTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Secondary KPIs Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            KpiChip("Win Rate", "${performance.winRatePct}%", QuantGreen)
            KpiChip("Profit Factor", "${performance.profitFactor}", QuantCyan)
            KpiChip("Calmar", "${performance.calmarRatio}", QuantCyan)
            KpiChip("Expectancy", "+$${performance.expectancy}", QuantGreen)
          }
        }
      }
    }

    // 2. Cumulative Equity Curve Chart
    item {
      EquityCurveChart(
        equityPoints = performance.equityHistory,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 3. Backtested Strategy Comparison & Family Explorer
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "STRATEGY BACKTESTS & VALIDATION",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )
        Text(
          text = "7 Families • 19 Strats",
          style = MaterialTheme.typography.labelSmall,
          color = QuantCyan
        )
      }
    }

    // Strategy selector chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(strategies) { strat ->
          FilterChip(
            selected = selectedStrategyId == strat.id,
            onClick = { selectedStrategyId = strat.id },
            label = {
              Text(
                text = "${strat.name} (+${strat.totalReturnPct}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
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

    // Selected Strategy Deep Dive Card
    selectedStrategy?.let { strat ->
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = strat.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = QuantTextPrimary
                )
                Text(
                  text = "Family: ${strat.family} • Universe: ${strat.symbols.joinToString(", ")}",
                  fontSize = 11.sp,
                  color = QuantTextMuted
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(QuantGreen.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "+${strat.totalReturnPct}%",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = QuantGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Strategy metrics grid
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(QuantDarkBackground)
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Sharpe", fontSize = 10.sp, color = QuantTextMuted)
                Text("${strat.sharpeRatio}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantCyan, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Sortino", fontSize = 10.sp, color = QuantTextMuted)
                Text("${strat.sortinoRatio}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantCyan, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Max DD", fontSize = 10.sp, color = QuantTextMuted)
                Text("-${strat.maxDrawdownPct}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantRed, fontFamily = FontFamily.Monospace)
              }
              Column {
                Text("Win Rate", fontSize = 10.sp, color = QuantTextMuted)
                Text("${strat.winRatePct}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantGreen, fontFamily = FontFamily.Monospace)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Profit Factor", fontSize = 10.sp, color = QuantTextMuted)
                Text("${strat.profitFactor}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantTextPrimary, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }
      }
    }

    // 4. Agent Activity & Calibration Reports
    item {
      Text(
        text = "AGENT ACTIVITY & TRACK RECORD REPORT",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = QuantTextSecondary
      )
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Scout Forecast Calibration & Elo Standings",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = QuantTextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))

          agents.forEach { agent ->
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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(QuantCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = agent.name,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = QuantTextPrimary
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${agent.eloRating} Elo",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = QuantGreen
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Brier ${agent.brierScore}",
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = QuantTextMuted
                )
              }
            }
          }
        }
      }
    }
  }

  // Import trade-suite Log / Output Dialog
  if (showImportDialog) {
    AlertDialog(
      onDismissRequest = { showImportDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CloudSync, contentDescription = null, tint = QuantCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Connect / Import trade-suite Log", fontWeight = FontWeight.Bold, color = QuantTextPrimary, fontSize = 16.sp)
        }
      },
      text = {
        Column {
          Text(
            text = "Paste JSON output from 'trade-suite performance --source paper --json' or backtest logs to sync KPIs:",
            fontSize = 12.sp,
            color = QuantTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = jsonInputText,
            onValueChange = { jsonInputText = it },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = QuantDarkBackground,
              unfocusedContainerColor = QuantDarkBackground,
              focusedBorderColor = QuantCyan,
              unfocusedBorderColor = QuantBorder,
              focusedTextColor = QuantTextPrimary,
              unfocusedTextColor = QuantTextPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onImportLogJson(jsonInputText)
            showImportDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = QuantCyan, contentColor = Color(0xFF00363D))
        ) {
          Text("PARSE & UPDATE", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showImportDialog = false }) {
          Text("Cancel", color = QuantTextPrimary)
        }
      },
      containerColor = QuantDarkSurface,
      shape = RoundedCornerShape(16.dp)
    )
  }
}

@Composable
fun KpiChip(label: String, value: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(QuantDarkBackground)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Column {
      Text(label, fontSize = 9.sp, color = QuantTextMuted)
      Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = color)
    }
  }
}
