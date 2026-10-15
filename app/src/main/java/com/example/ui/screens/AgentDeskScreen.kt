package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.example.ui.theme.*

@Composable
fun AgentDeskScreen(
  agents: List<Agent>,
  debates: List<AgentDebate>,
  isDeskRunning: Boolean,
  onRunDesk: (List<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedUniverseSymbol by remember { mutableStateOf("SPY") }
  val availableSymbols = listOf("SPY", "AAPL", "NVDA", "QQQ", "MSFT", "TSLA")
  var expandedDebateId by remember { mutableStateOf<String?>(debates.firstOrNull()?.id) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Header Banner & Economics Formula
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
              Text(
                text = "AUTONOMOUS RESEARCH DESK",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuantCyan
              )
              Text(
                text = "6 specialist scouts • Debate arbitration • PM allocation",
                style = MaterialTheme.typography.bodySmall,
                color = QuantTextSecondary
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(QuantGreen.copy(alpha = 0.15f))
                .border(1.dp, QuantGreen, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "ELO K=32",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(QuantDarkBackground)
              .padding(10.dp)
          ) {
            Column {
              Text(
                text = "Scout Scoring Economics:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = QuantTextSecondary
              )
              Text(
                text = "score = Sharpe × min(1, n/10) - 1.5 × MaxDD\nweights w_i ∝ 1/vol_i (inverse-vol, regime-tilted, risk-vetoed)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = QuantCyanDim,
                lineHeight = 16.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Symbol universe selector & Run Desk trigger
          Text(
            text = "Trigger Research Run Universe:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = QuantTextSecondary
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(availableSymbols) { sym ->
              FilterChip(
                selected = selectedUniverseSymbol == sym,
                onClick = { selectedUniverseSymbol = sym },
                label = { Text(sym, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = QuantCyan,
                  selectedLabelColor = Color(0xFF00363D),
                  containerColor = QuantDarkBackground,
                  labelColor = QuantTextPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { onRunDesk(listOf(selectedUniverseSymbol)) },
            enabled = !isDeskRunning,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("run_agent_desk_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = QuantCyan,
              contentColor = Color(0xFF00363D)
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            if (isDeskRunning) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color(0xFF00363D),
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Agents debating $selectedUniverseSymbol...", fontWeight = FontWeight.Bold)
            } else {
              Icon(Icons.Default.Psychology, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("RUN DESK ON $selectedUniverseSymbol", fontWeight = FontWeight.Black)
            }
          }
        }
      }
    }

    // 2. The 6 Specialist Scout Leaderboard & Calibration
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SPECIALIST SCOUT AGENTS",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )
        Text(
          text = "Elo & Brier Ratings",
          style = MaterialTheme.typography.labelSmall,
          color = QuantCyan
        )
      }
    }

    items(agents) { agent ->
      AgentCard(agent = agent)
    }

    // 3. Multi-Agent Debate Arbitration & Decisions
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DEBATE ARBITRATION & VOTING LOGS",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = QuantTextSecondary
        )
        Text(
          text = "${debates.size} transcripts",
          style = MaterialTheme.typography.labelSmall,
          color = QuantTextMuted
        )
      }
    }

    items(debates) { debate ->
      DebateCard(
        debate = debate,
        isExpanded = expandedDebateId == debate.id,
        onToggle = {
          expandedDebateId = if (expandedDebateId == debate.id) null else debate.id
        }
      )
    }
  }
}

@Composable
fun AgentCard(agent: Agent) {
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
              .size(36.dp)
              .clip(CircleShape)
              .background(QuantDarkBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (agent.role) {
                AgentRole.MACRO_SCOUT -> Icons.Default.Public
                AgentRole.SENTIMENT_SCOUT -> Icons.Default.Forum
                AgentRole.MOMENTUM_SCOUT -> Icons.Default.TrendingUp
                AgentRole.OPTIONS_VOL_SCOUT -> Icons.Default.ShowChart
                AgentRole.MEAN_REVERSION_SCOUT -> Icons.Default.SwapHoriz
                AgentRole.PAIRS_SCOUT -> Icons.Default.CompareArrows
                else -> Icons.Default.Security
              },
              contentDescription = agent.name,
              tint = QuantCyan,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = agent.name,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = QuantTextPrimary
            )
            Text(
              text = agent.specialty,
              fontSize = 11.sp,
              color = QuantTextMuted,
              maxLines = 1
            )
          }
        }

        // Elo Rating pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(QuantDarkBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "${agent.eloRating} ELO",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = QuantGreen
            )
            Text(
              text = "Brier ${agent.brierScore}",
              fontSize = 9.sp,
              color = QuantTextMuted,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Stats row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(QuantDarkBackground.copy(alpha = 0.6f))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StatItem(label = "Alloc Weight", value = "${agent.weightPct}%", color = QuantCyan)
        StatItem(label = "Win Rate", value = "${agent.winRate}%", color = QuantGreen)
        StatItem(label = "Trades", value = "${agent.tradeCount}", color = QuantTextPrimary)
        StatItem(label = "Max DD", value = "-${agent.maxDrawdown}%", color = QuantRed)
      }
    }
  }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, fontSize = 9.sp, color = QuantTextMuted)
    Text(
      text = value,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = color
    )
  }
}

@Composable
fun DebateCard(
  debate: AgentDebate,
  isExpanded: Boolean,
  onToggle: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = debate.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = QuantTextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (debate.isApproved) QuantGreen.copy(alpha = 0.2f) else QuantRed.copy(alpha = 0.2f))
              .border(1.dp, if (debate.isApproved) QuantGreen else QuantRed, RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = debate.consensusAction,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (debate.isApproved) QuantGreen else QuantRed
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = debate.timestamp,
            fontSize = 11.sp,
            color = QuantTextMuted
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = QuantTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = debate.portfolioManagerSummary,
        fontSize = 12.sp,
        color = QuantTextSecondary,
        lineHeight = 16.sp
      )

      if (debate.vetoReason != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Cancel, contentDescription = null, tint = QuantRed, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = debate.vetoReason,
            fontSize = 11.sp,
            color = QuantRed,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Expandable debate rounds transcript
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
        ) {
          HorizontalDivider(color = QuantBorder, thickness = 1.dp)
          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "SCOUT CONTRIBUTIONS & STANCES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = QuantCyan,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          debate.turns.forEach { turn ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .padding(top = 4.dp)
                  .clip(CircleShape)
                  .background(
                    when (turn.stance) {
                      SentimentBias.STRONG_BULLISH, SentimentBias.BULLISH -> QuantGreen
                      SentimentBias.BEARISH, SentimentBias.STRONG_BEARISH -> QuantRed
                      SentimentBias.VETO -> QuantRed
                      else -> QuantAmber
                    }
                  )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = turn.agentName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuantTextPrimary
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${turn.stance.name} (${turn.confidencePct}%)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = when (turn.stance) {
                      SentimentBias.STRONG_BULLISH, SentimentBias.BULLISH -> QuantGreen
                      SentimentBias.VETO, SentimentBias.STRONG_BEARISH -> QuantRed
                      else -> QuantAmber
                    }
                  )
                }
                Text(
                  text = turn.thesis,
                  fontSize = 11.sp,
                  color = QuantTextSecondary,
                  lineHeight = 15.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
