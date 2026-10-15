package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketTicker
import com.example.model.MoomooStatus
import com.example.model.RiskState
import com.example.ui.theme.*

@Composable
fun QuantTopBar(
  riskState: RiskState,
  moomooStatus: MoomooStatus,
  onKillSwitchClick: () -> Unit,
  onDeskRunClick: () -> Unit,
  isDeskRunning: Boolean,
  onEnginesClick: () -> Unit,
  onConnectClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = QuantDarkSurface,
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App title & monorepo badge
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(QuantCyan.copy(alpha = 0.15f))
              .border(1.dp, QuantCyan, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "TradeSuite",
              tint = QuantCyan,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "TRADE-SUITE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = QuantTextPrimary,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(QuantDarkSurfaceVariant)
                  .clickable { onEnginesClick() }
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "36 ENGINES",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = QuantCyan,
                  letterSpacing = 0.5.sp
                )
              }
            }

            // OpenD status subtext
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (moomooStatus.isConnected) QuantGreen else QuantRed)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "OpenD ${moomooStatus.host}:${moomooStatus.port} (Paper)",
                fontSize = 10.sp,
                color = QuantTextMuted,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Action controls: Kill switch and run desk
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Kill Switch Pill
          val ksBgColor by animateColorAsState(
            targetValue = if (riskState.isKillSwitchTriggered) QuantRed else if (riskState.isKillSwitchArmed) QuantDarkSurfaceVariant else QuantTextMuted.copy(alpha = 0.2f),
            animationSpec = tween(300),
            label = "ksBg"
          )
          val ksTextColor = if (riskState.isKillSwitchTriggered) Color.White else if (riskState.isKillSwitchArmed) QuantAmber else QuantTextMuted

          Row(
            modifier = Modifier
              .testTag("kill_switch_button")
              .clip(RoundedCornerShape(8.dp))
              .background(ksBgColor)
              .border(
                1.dp,
                if (riskState.isKillSwitchTriggered) QuantRed else QuantAmber.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
              )
              .clickable { onKillSwitchClick() }
              .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PowerSettingsNew,
              contentDescription = "Kill Switch",
              tint = ksTextColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (riskState.isKillSwitchTriggered) "HALTED" else "KILL-SW",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = ksTextColor
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Quick Desk Run Button
          Button(
            onClick = onDeskRunClick,
            enabled = !isDeskRunning,
            modifier = Modifier
              .testTag("quick_desk_run_button")
              .height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = QuantCyan,
              contentColor = Color(0xFF00363D)
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            if (isDeskRunning) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color(0xFF00363D),
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "RUN DESK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = onConnectClick,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SettingsInputComponent,
              contentDescription = "Connection Settings",
              tint = QuantTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      HorizontalDivider(color = QuantBorder, thickness = 1.dp)
    }
  }
}

@Composable
fun LiveTickerTape(
  tickers: List<MarketTicker>,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(QuantDarkBackground)
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Pulse indicator
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(QuantGreen)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "LIVE BUS",
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = QuantGreen,
        letterSpacing = 0.5.sp
      )
    }

    tickers.forEach { ticker ->
      val isUp = ticker.change >= 0
      val priceColor = if (isUp) QuantGreen else QuantRed

      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(QuantDarkSurfaceVariant.copy(alpha = 0.4f))
          .border(1.dp, QuantBorder, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = ticker.symbol,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = QuantTextPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (ticker.price > 1000) String.format("%,.0f", ticker.price) else String.format("%.2f", ticker.price),
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = QuantTextPrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${if (isUp) "+" else ""}${String.format("%.2f", ticker.changePct)}%",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = priceColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        // Expected Move (ThetaData)
        Text(
          text = "EM ±${String.format("%.1f", ticker.expectedMovePct)}%",
          fontSize = 9.sp,
          color = QuantPurple,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun KillSwitchConfirmDialog(
  isTriggered: Boolean,
  onDismiss: () -> Unit,
  onConfirmHalt: () -> Unit,
  onReset: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        tint = if (isTriggered) QuantGreen else QuantRed,
        modifier = Modifier.size(36.dp)
      )
    },
    title = {
      Text(
        text = if (isTriggered) "Clear Emergency Halt?" else "TRIGGER EMERGENCY KILL-SWITCH?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = QuantTextPrimary
      )
    },
    text = {
      Text(
        text = if (isTriggered)
          "The system is currently halted. Clearing the emergency halt will re-enable paper order execution via moomoo openD."
        else
          "WARNING: Triggering the kill-switch will immediately cancel all pending paper orders, veto incoming scout proposals, and activate trade-hedge portfolio safeguards. Are you sure?",
        style = MaterialTheme.typography.bodyMedium,
        color = QuantTextSecondary
      )
    },
    confirmButton = {
      Button(
        onClick = { if (isTriggered) onReset() else onConfirmHalt() },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isTriggered) QuantGreen else QuantRed,
          contentColor = Color.White
        )
      ) {
        Text(
          text = if (isTriggered) "Clear Halt & Re-arm" else "YES, HALT TRADING",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = QuantTextPrimary)
      }
    },
    containerColor = QuantDarkSurface,
    shape = RoundedCornerShape(16.dp)
  )
}
