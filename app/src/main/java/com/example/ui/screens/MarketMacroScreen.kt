package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MacroBarometer
import com.example.model.PerformanceStats
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.UnderwaterDrawdownChart
import com.example.ui.theme.*

@Composable
fun MarketMacroScreen(
  macro: MacroBarometer,
  stats: PerformanceStats,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Performance Analytics (Equity Curve)
    item {
      EquityCurveChart(
        equityPoints = stats.equityHistory,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 2. Underwater Drawdown Series
    item {
      UnderwaterDrawdownChart(
        drawdownPoints = stats.drawdownHistory,
        modifier = Modifier.fillMaxWidth()
      )
    }

    // 3. Quantitative Performance Attribution Grid
    item {
      Text(
        text = "QUANT ATTRIBUTION & METRICS",
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            MetricCol("Win Rate", "${stats.winRatePct}%", QuantGreen)
            MetricCol("Profit Factor", "${stats.profitFactor}", QuantCyan)
            MetricCol("Sharpe Ratio", "${stats.sharpeRatio}", QuantGreen)
            MetricCol("Sortino", "${stats.sortinoRatio}", QuantCyan)
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = QuantBorder, thickness = 1.dp)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            MetricCol("Calmar Ratio", "${stats.calmarRatio}", QuantCyan)
            MetricCol("Max Drawdown", "-${stats.maxDrawdownPct}%", QuantRed)
            MetricCol("Expectancy", "+$${stats.expectancy}", QuantGreen)
            MetricCol("Net Profit", "+${stats.netProfitPct}%", QuantGreen)
          }
        }
      }
    }

    // 4. Macro Regime Barometer (trade-macro)
    item {
      Text(
        text = "MACRO & REGIME BAROMETER (TRADE-MACRO)",
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TRADING-WEATHER BAROMETER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantTextSecondary
              )
              Text(
                text = macro.weatherStatus,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = QuantGreen
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(QuantGreen.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("FAVORABLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuantGreen)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Copper:Gold Proxy", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "${macro.copperGoldRatio} (+${macro.copperGoldChangePct}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantCyan
              )
            }
            Column {
              Text("Advance/Decline", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "${macro.advanceDeclineRatio}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantTextPrimary
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("McClellan Osc.", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "+${macro.mcClellanOscillator}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Flow Context Flags
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            FlagItem("Buyback Blackout", if (macro.buybackBlackoutActive) "Active (Restricted)" else "Clear (Open)", !macro.buybackBlackoutActive)
            FlagItem("Forced-Selling Risk", "${macro.forcedSellingRiskPct}% (Low)", true)
          }
        }
      }
    }

    // 5. Options & ThetaData EOD expected move
    item {
      Text(
        text = "OPTIONS & EXPECTED MOVE (THETADATA)",
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
                  .background(if (macro.thetaTerminalRunning) QuantGreen else QuantAmber)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "ThetaData Terminal v3 (Local 127.0.0.1:25503)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantTextPrimary
              )
            }
            Text("Free EOD Tier", fontSize = 10.sp, color = QuantPurple, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "EOD options chains, pricing Greeks, and 0DTE EM-decay triggers are computed without credentials in code via local API bridge.",
            fontSize = 11.sp,
            color = QuantTextSecondary,
            lineHeight = 15.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("SPX Expiry EM", fontSize = 10.sp, color = QuantTextMuted)
              Text("±52.4 pts (±0.92%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantPurple, fontFamily = FontFamily.Monospace)
            }
            Column {
              Text("SPY Friday EM", fontSize = 10.sp, color = QuantTextMuted)
              Text("±7.85 pts (±1.35%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuantPurple, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("EM-Decay Trigger", fontSize = 10.sp, color = QuantTextMuted)
              Text("WAITING (1-day delay)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuantTextMuted)
            }
          }
        }
      }
    }
  }
}

@Composable
fun MetricCol(label: String, value: String, color: Color) {
  Column {
    Text(text = label, fontSize = 10.sp, color = QuantTextMuted)
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = color
    )
  }
}

@Composable
fun FlagItem(label: String, status: String, isClear: Boolean) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = if (isClear) Icons.Default.CheckCircle else Icons.Default.Warning,
      contentDescription = null,
      tint = if (isClear) QuantGreen else QuantAmber,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Column {
      Text(text = label, fontSize = 9.sp, color = QuantTextMuted)
      Text(text = status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = QuantTextPrimary)
    }
  }
}
