package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskState
import com.example.model.RiskVeto
import com.example.ui.components.MetricGaugeCard
import com.example.ui.theme.*

@Composable
fun RiskMonitorScreen(
  riskState: RiskState,
  riskVetoes: List<RiskVeto>,
  onKillSwitchClick: () -> Unit,
  onArmToggle: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Emergency Kill-Switch Control Panel
    item {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (riskState.isKillSwitchTriggered) QuantRed.copy(alpha = 0.2f) else QuantDarkSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (riskState.isKillSwitchTriggered) androidx.compose.foundation.BorderStroke(2.dp, QuantRed) else null,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (riskState.isKillSwitchTriggered) QuantRed else QuantAmber,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "PORTFOLIO KILL-SWITCH",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  color = QuantTextPrimary
                )
                Text(
                  text = "Automated shutdown & liquidation guard (trade-hedge)",
                  style = MaterialTheme.typography.bodySmall,
                  color = QuantTextSecondary
                )
              }
            }

            // Arm / Disarm Switch
            Switch(
              checked = riskState.isKillSwitchArmed,
              onCheckedChange = { onArmToggle(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = QuantAmber,
                checkedTrackColor = QuantAmber.copy(alpha = 0.3f),
                uncheckedThumbColor = QuantTextMuted,
                uncheckedTrackColor = QuantBorder
              )
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "CURRENT STATUS",
                  fontSize = 10.sp,
                  color = QuantTextMuted,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (riskState.isKillSwitchTriggered) "HALTED (TRIGGERED)" else if (riskState.isKillSwitchArmed) "ARMED & MONITORING" else "DISARMED",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black,
                  color = if (riskState.isKillSwitchTriggered) QuantRed else if (riskState.isKillSwitchArmed) QuantGreen else QuantTextMuted,
                  fontFamily = FontFamily.Monospace
                )
              }

              Button(
                onClick = onKillSwitchClick,
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (riskState.isKillSwitchTriggered) QuantGreen else QuantRed,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("emergency_halt_action_button")
              ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (riskState.isKillSwitchTriggered) "CLEAR HALT" else "EMERGENCY HALT",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // 2. Exposure & Concentration Metrics
    item {
      Text(
        text = "EXPOSURE & REGIME GAUGES",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = QuantTextSecondary
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricGaugeCard(
          title = "NET EXPOSURE",
          valueText = "${riskState.netExposurePct}%",
          subtitle = "Target: < 50%",
          progressPct = (riskState.netExposurePct / 50f) * 100f,
          color = QuantCyan,
          modifier = Modifier.weight(1f)
        )
        MetricGaugeCard(
          title = "GROSS EXPOSURE",
          valueText = "${riskState.grossExposurePct}%",
          subtitle = "Max Limit: 150%",
          progressPct = (riskState.grossExposurePct / 150f) * 100f,
          color = QuantBlue,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricGaugeCard(
          title = "HERFINDAHL CONC.",
          valueText = "${riskState.herfindahlConcentration}",
          subtitle = "Safe: < 0.25 (Diversified)",
          progressPct = (riskState.herfindahlConcentration / 0.25f) * 100f,
          color = QuantGreen,
          modifier = Modifier.weight(1f)
        )
        MetricGaugeCard(
          title = "21D REALIZED VOL",
          valueText = "${riskState.trailingVol21d}%",
          subtitle = "Vol Threshold: 25.0%",
          progressPct = (riskState.trailingVol21d / 25f) * 100f,
          color = QuantAmber,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 3. Regime Conviction & Limit Stack
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
                text = "REGIME CONVICTION & HYSTERESIS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantTextSecondary
              )
              Text(
                text = riskState.regimeLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuantCyan
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(QuantCyan.copy(alpha = 0.15f))
                .border(1.dp, QuantCyan, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${riskState.regimeConviction}% Conviction",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantCyan,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = QuantBorder, thickness = 1.dp)
          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "ACTIVE RISK LIMIT STACK",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = QuantTextMuted
          )

          Spacer(modifier = Modifier.height(6.dp))

          RiskLimitRow("Max Position Notional", "10.0% of NAV", "Compliant", true)
          RiskLimitRow("Max Portfolio Drawdown", "-${riskState.maxDrawdownLimitPct}% (Current: -${riskState.currentDrawdownPct}%)", "Compliant", true)
          RiskLimitRow("Short-First Ledgers", "Strictly prohibited", "Enforced", true)
          RiskLimitRow("Open Orders Count", "3 active", "Limit 10", true)
        }
      }
    }

    // 4. Risk Veto Audit Log
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RISK DESK VETO AUDIT LOG",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )
        Text(
          text = "${riskVetoes.size} recent vetoes",
          style = MaterialTheme.typography.labelSmall,
          color = QuantRed
        )
      }
    }

    items(riskVetoes) { veto ->
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(QuantRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Block, contentDescription = null, tint = QuantRed, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${veto.symbol} • ${veto.vetoedAction}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuantTextPrimary
              )
              Text(
                text = veto.timestamp,
                fontSize = 10.sp,
                color = QuantTextMuted
              )
            }
            Text(
              text = "Rule: ${veto.ruleName} (${veto.triggeredValue} vs limit ${veto.threshold})",
              fontSize = 11.sp,
              color = QuantTextSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
fun RiskLimitRow(title: String, spec: String, status: String, isOk: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title, fontSize = 12.sp, color = QuantTextPrimary)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = spec, fontSize = 11.sp, color = QuantTextMuted, fontFamily = FontFamily.Monospace)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
        contentDescription = null,
        tint = if (isOk) QuantGreen else QuantAmber,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}
