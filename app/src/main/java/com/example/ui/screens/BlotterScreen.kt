package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*

@Composable
fun BlotterScreen(
  moomooStatus: MoomooStatus,
  blotterTrades: List<BlotterTrade>,
  pendingOrders: List<PendingOrder>,
  onApproveOrder: (String) -> Unit,
  onVetoOrder: (String) -> Unit,
  onExportCsv: () -> String,
  modifier: Modifier = Modifier
) {
  var showExportDialog by remember { mutableStateOf(false) }
  var exportedCsvContent by remember { mutableStateOf("") }
  var filterOutcome by remember { mutableStateOf<TradeOutcome?>(null) }

  val filteredTrades = remember(blotterTrades, filterOutcome) {
    if (filterOutcome == null) blotterTrades else blotterTrades.filter { it.outcome == filterOutcome }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Moomoo OpenD Paper Balance Card
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
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(QuantGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "MOOMOO OPEND PAPER LEDGER",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = QuantCyan
                )
              }
              Text(
                text = "$${String.format("%,.2f", moomooStatus.accountEquity)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = QuantTextPrimary
              )
            }

            Button(
              onClick = {
                exportedCsvContent = onExportCsv()
                showExportDialog = true
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = QuantCyan.copy(alpha = 0.15f),
                contentColor = QuantCyan
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("export_csv_button")
            ) {
              Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("CSV EXPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Cash Balance", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "$${String.format("%,.2f", moomooStatus.cashBalance)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantTextPrimary
              )
            }
            Column {
              Text("Buying Power", fontSize = 10.sp, color = QuantTextMuted)
              Text(
                "$${String.format("%,.2f", moomooStatus.buyingPower)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantGreen
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Reconcile Status", fontSize = 10.sp, color = QuantTextMuted)
              Text("0 Discrepancy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantCyan)
            }
          }
        }
      }
    }

    // 2. Pending Approval Queue (from agent desk)
    if (pendingOrders.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "AGENT ORDER APPROVAL QUEUE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = QuantAmber
          )
          Text(
            text = "${pendingOrders.size} pending",
            style = MaterialTheme.typography.labelSmall,
            color = QuantAmber
          )
        }
      }

      items(pendingOrders) { order ->
        Card(
          colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${order.side} ${order.quantity} ${order.symbol}",
                  fontWeight = FontWeight.Black,
                  fontSize = 14.sp,
                  color = QuantTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "@ $${String.format("%.2f", order.limitPrice)}",
                  fontSize = 13.sp,
                  fontFamily = FontFamily.Monospace,
                  color = QuantCyan
                )
              }
              Text(
                text = "${order.strategy} • proposed by ${order.proposedBy}",
                fontSize = 11.sp,
                color = QuantTextMuted
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = { onVetoOrder(order.id) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(QuantRed.copy(alpha = 0.2f))
              ) {
                Icon(Icons.Default.Close, contentDescription = "Veto Order", tint = QuantRed, modifier = Modifier.size(18.dp))
              }

              IconButton(
                onClick = { onApproveOrder(order.id) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(QuantGreen.copy(alpha = 0.2f))
              ) {
                Icon(Icons.Default.Check, contentDescription = "Approve Order", tint = QuantGreen, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }

    // 3. Trade Blotter Header & Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "TRADE BLOTTER (FIFO MATCHED)",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          OutcomeChip("ALL", filterOutcome == null) { filterOutcome = null }
          OutcomeChip("WINS", filterOutcome == TradeOutcome.WIN) { filterOutcome = TradeOutcome.WIN }
          OutcomeChip("LOSS", filterOutcome == TradeOutcome.LOSS) { filterOutcome = TradeOutcome.LOSS }
          OutcomeChip("OPEN", filterOutcome == TradeOutcome.OPEN) { filterOutcome = TradeOutcome.OPEN }
        }
      }
    }

    // 4. Closed and Open Trade Blotter rows
    items(filteredTrades) { trade ->
      TradeBlotterCard(trade = trade)
    }
  }

  // Export CSV Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = {
        Text("Blotter CSV Export", fontWeight = FontWeight.Bold, color = QuantTextPrimary)
      },
      text = {
        Column {
          Text(
            text = "Export shape matching 'trade-suite trades export':",
            fontSize = 12.sp,
            color = QuantTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(8.dp)
          ) {
            Text(
              text = exportedCsvContent,
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
          onClick = { showExportDialog = false },
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

@Composable
fun OutcomeChip(label: String, selected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (selected) QuantCyan else QuantDarkSurfaceVariant)
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Text(
      text = label,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = if (selected) Color(0xFF00363D) else QuantTextMuted
    )
  }
}

@Composable
fun TradeBlotterCard(trade: BlotterTrade) {
  Card(
    colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Outcome icon badge
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
              when (trade.outcome) {
                TradeOutcome.WIN -> QuantGreen.copy(alpha = 0.2f)
                TradeOutcome.LOSS -> QuantRed.copy(alpha = 0.2f)
                TradeOutcome.OPEN -> QuantCyan.copy(alpha = 0.2f)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (trade.outcome) {
              TradeOutcome.WIN -> Icons.Default.TrendingUp
              TradeOutcome.LOSS -> Icons.Default.TrendingDown
              TradeOutcome.OPEN -> Icons.Default.Timelapse
            },
            contentDescription = null,
            tint = when (trade.outcome) {
              TradeOutcome.WIN -> QuantGreen
              TradeOutcome.LOSS -> QuantRed
              TradeOutcome.OPEN -> QuantCyan
            },
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = trade.symbol,
              fontWeight = FontWeight.Black,
              fontSize = 14.sp,
              color = QuantTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(QuantDarkBackground)
                .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${trade.side} ${trade.quantity}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = QuantTextSecondary
              )
            }
          }
          Text(
            text = "${trade.strategy} • ${trade.agentTag}",
            fontSize = 11.sp,
            color = QuantTextMuted
          )
          Text(
            text = "${trade.timestamp} @ $${String.format("%.2f", trade.entryPrice)}",
            fontSize = 10.sp,
            color = QuantTextMuted,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Realized P&L
      Column(horizontalAlignment = Alignment.End) {
        val pnlColor = if (trade.realizedPnL >= 0) QuantGreen else QuantRed
        Text(
          text = "${if (trade.realizedPnL >= 0) "+" else ""}$${String.format("%.2f", trade.realizedPnL)}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = pnlColor
        )
        Text(
          text = "${if (trade.realizedPnLPct >= 0) "+" else ""}${String.format("%.2f", trade.realizedPnLPct)}%",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = pnlColor
        )
        Text(
          text = trade.outcome.name,
          fontSize = 9.sp,
          fontWeight = FontWeight.Black,
          color = when (trade.outcome) {
            TradeOutcome.WIN -> QuantGreen
            TradeOutcome.LOSS -> QuantRed
            TradeOutcome.OPEN -> QuantCyan
          }
        )
      }
    }
  }
}
