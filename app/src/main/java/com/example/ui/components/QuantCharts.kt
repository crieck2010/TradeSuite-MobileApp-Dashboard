package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CorrelationMatrixData
import com.example.ui.theme.*

@Composable
fun EquityCurveChart(
  equityPoints: List<Float>,
  modifier: Modifier = Modifier,
  lineColor: Color = QuantCyan,
  areaColor: Color = QuantCyan.copy(alpha = 0.2f)
) {
  if (equityPoints.isEmpty()) return

  val minVal = equityPoints.minOrNull() ?: 0f
  val maxVal = equityPoints.maxOrNull() ?: 1f
  val range = if (maxVal - minVal > 0f) maxVal - minVal else 1f

  Box(
    modifier = modifier
      .background(QuantDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "EQUITY CURVE (CUMULATIVE)",
          style = MaterialTheme.typography.labelSmall,
          color = QuantTextSecondary,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$${String.format("%,.0f", equityPoints.last())}",
          style = MaterialTheme.typography.titleMedium,
          color = QuantGreen,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
          val y = height * (i.toFloat() / gridLines)
          drawLine(
            color = QuantBorder.copy(alpha = 0.5f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
          )
        }

        val stepX = width / (equityPoints.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        fillPath.moveTo(0f, height)

        equityPoints.forEachIndexed { index, value ->
          val normY = 1f - ((value - minVal) / range)
          val x = index * stepX
          val y = (normY * (height - 20f)) + 10f

          if (index == 0) {
            path.moveTo(x, y)
            fillPath.lineTo(x, y)
          } else {
            path.lineTo(x, y)
            fillPath.lineTo(x, y)
          }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient area
        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(areaColor, Color.Transparent),
            startY = 0f,
            endY = height
          )
        )

        // Draw line
        drawPath(
          path = path,
          color = lineColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw current price dot
        val lastY = (1f - ((equityPoints.last() - minVal) / range)) * (height - 20f) + 10f
        drawCircle(
          color = lineColor,
          radius = 5.dp.toPx(),
          center = Offset(width, lastY)
        )
        drawCircle(
          color = Color.White,
          radius = 2.5.dp.toPx(),
          center = Offset(width, lastY)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Start: $${String.format("%,.0f", equityPoints.first())}",
          fontSize = 10.sp,
          color = QuantTextMuted,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "FIFO lot accounting",
          fontSize = 10.sp,
          color = QuantTextMuted
        )
        Text(
          text = "High: $${String.format("%,.0f", maxVal)}",
          fontSize = 10.sp,
          color = QuantTextMuted,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun UnderwaterDrawdownChart(
  drawdownPoints: List<Float>,
  modifier: Modifier = Modifier
) {
  if (drawdownPoints.isEmpty()) return

  val maxDd = drawdownPoints.minOrNull() ?: -1f // Most negative
  val worstDd = if (maxDd < 0f) maxDd else -1f

  Box(
    modifier = modifier
      .background(QuantDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "UNDERWATER DRAWDOWN",
          style = MaterialTheme.typography.labelSmall,
          color = QuantTextSecondary,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${String.format("%.2f", worstDd)}% Max DD",
          style = MaterialTheme.typography.titleSmall,
          color = QuantRed,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
      ) {
        val width = size.width
        val height = size.height

        // Top zero line
        drawLine(
          color = QuantTextMuted.copy(alpha = 0.4f),
          start = Offset(0f, 4f),
          end = Offset(width, 4f),
          strokeWidth = 1.5f
        )

        val stepX = width / (drawdownPoints.size - 1).coerceAtLeast(1)
        val fillPath = Path()
        fillPath.moveTo(0f, 4f)

        drawdownPoints.forEachIndexed { index, dd ->
          val fraction = (dd / worstDd).coerceIn(0f, 1f)
          val x = index * stepX
          val y = 4f + (fraction * (height - 8f))
          fillPath.lineTo(x, y)
        }

        fillPath.lineTo(width, 4f)
        fillPath.close()

        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(QuantRed.copy(alpha = 0.6f), QuantRed.copy(alpha = 0.1f)),
            startY = 4f,
            endY = height
          )
        )
      }
    }
  }
}

@Composable
fun MetricGaugeCard(
  title: String,
  valueText: String,
  subtitle: String,
  progressPct: Float,
  color: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(QuantDarkSurfaceVariant, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = QuantTextSecondary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = valueText,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(6.dp))
      // Progress Bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .background(QuantBorder, RoundedCornerShape(2.dp))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth((progressPct / 100f).coerceIn(0.05f, 1f))
            .fillMaxHeight()
            .background(color, RoundedCornerShape(2.dp))
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = QuantTextMuted
      )
    }
  }
}

@Composable
fun PairwiseCorrelationHeatmapCard(
  matrixData: CorrelationMatrixData,
  modifier: Modifier = Modifier
) {
  val showShrunkState = remember { mutableStateOf(true) }
  val selectedCellState = remember { mutableStateOf<Pair<Int, Int>?>(Pair(0, 1)) }

  val showShrunk = showShrunkState.value
  val selectedCell = selectedCellState.value

  val activeMatrix = if (showShrunk) matrixData.shrunkMatrix else matrixData.matrix
  val labels = matrixData.strategyLabels

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(QuantDarkSurfaceVariant, RoundedCornerShape(14.dp))
      .padding(14.dp)
  ) {
    Column {
      // Header & Shrinkage toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "PEARSON CORRELATION HEATMAP",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = QuantCyan
          )
          Text(
            text = if (showShrunk) "Ledoit-Wolf Covariance Shrinkage (δ = ${matrixData.shrinkageIntensity})" else "Raw Empirical Pearson Matrix",
            fontSize = 10.sp,
            color = QuantTextSecondary
          )
        }

        // Toggle button for Raw vs Shrunk
        FilterChip(
          selected = showShrunk,
          onClick = { showShrunkState.value = !showShrunkState.value },
          label = {
            Text(
              text = if (showShrunk) "Shrunk (L-W)" else "Raw Pearson",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = QuantCyan.copy(alpha = 0.2f),
            selectedLabelColor = QuantCyan,
            containerColor = QuantDarkBackground,
            labelColor = QuantTextMuted
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Scrollable Heatmap Table
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState)
      ) {
        // Table Column Headers
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.width(62.dp).height(24.dp), contentAlignment = Alignment.CenterStart) {
            Text("Engine", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = QuantTextMuted)
          }
          labels.forEach { label ->
            Box(
              modifier = Modifier.width(44.dp).height(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label.take(5),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = QuantTextSecondary
              )
            }
          }
        }

        // Matrix Rows
        labels.forEachIndexed { rowIndex, rowLabel ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 1.dp)
          ) {
            // Row Label
            Box(
              modifier = Modifier.width(62.dp).height(30.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Text(
                text = rowLabel.take(7),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = if (rowIndex == 0) QuantCyan else QuantTextSecondary
              )
            }

            // Cells
            labels.forEachIndexed { colIndex, _ ->
              val r = activeMatrix.getOrNull(rowIndex)?.getOrNull(colIndex) ?: 0f
              val isSelected = selectedCell?.first == rowIndex && selectedCell.second == colIndex

              // Compute color based on correlation r (-1.0 to 1.0)
              val cellBg = when {
                rowIndex == colIndex -> QuantTextMuted.copy(alpha = 0.35f)
                r >= 0.70f -> QuantRed.copy(alpha = 0.75f)
                r >= 0.40f -> QuantAmber.copy(alpha = 0.65f)
                r >= 0.15f -> QuantAmber.copy(alpha = 0.35f)
                r in -0.05f..0.15f -> QuantDarkBackground
                r in -0.25f..-0.05f -> QuantGreen.copy(alpha = 0.35f)
                else -> QuantGreen.copy(alpha = 0.75f)
              }

              val textColor = when {
                rowIndex == colIndex -> QuantTextPrimary
                r >= 0.40f -> Color.White
                r <= -0.05f -> QuantGreen
                else -> QuantTextSecondary
              }

              Box(
                modifier = Modifier
                  .size(42.dp, 30.dp)
                  .padding(1.dp)
                  .background(cellBg, RoundedCornerShape(4.dp))
                  .then(
                    if (isSelected) {
                      Modifier.border(1.5.dp, QuantCyan, RoundedCornerShape(4.dp))
                    } else Modifier
                  )
                  .clickable {
                    selectedCellState.value = Pair(rowIndex, colIndex)
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (rowIndex == colIndex) "1.0" else "${if (r > 0) "+" else ""}${String.format("%.2f", r)}",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = textColor
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Inspection card for tapped cell
      if (selectedCell != null) {
        val rIdx = selectedCell.first
        val cIdx = selectedCell.second
        val stratA = labels.getOrElse(rIdx) { "Strategy A" }
        val stratB = labels.getOrElse(cIdx) { "Strategy B" }
        val rawR = matrixData.matrix.getOrNull(rIdx)?.getOrNull(cIdx) ?: 0f
        val shrunkR = matrixData.shrunkMatrix.getOrNull(rIdx)?.getOrNull(cIdx) ?: 0f

        val diversificationNote = when {
          rIdx == cIdx -> "Self-correlation (Identity)"
          shrunkR <= -0.05f -> "Negative Correlation • Superior portfolio hedge & volatility dampener"
          shrunkR in -0.05f..0.15f -> "Non-Correlated Alpha (ρ ≈ 0) • Ideal orthogonal strategy addition"
          shrunkR in 0.15f..0.45f -> "Mild Positive Exposure • Acceptable co-movement under macro trends"
          else -> "Factor Crowding Risk (ρ > 0.45) • Watch out for duplicated directional exposure"
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(QuantDarkBackground, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$stratA × $stratB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = QuantCyan
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "Raw: ${if (rawR >= 0) "+" else ""}${String.format("%.2f", rawR)}",
                  fontSize = 10.sp,
                  color = QuantTextMuted,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "Shrunk: ${if (shrunkR >= 0) "+" else ""}${String.format("%.2f", shrunkR)}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (shrunkR <= 0.15f) QuantGreen else QuantAmber,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = diversificationNote,
              fontSize = 10.sp,
              color = QuantTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Legend Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).background(QuantGreen, RoundedCornerShape(2.dp)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ρ < -0.05 (Hedge)", fontSize = 8.sp, color = QuantTextMuted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).background(QuantDarkBackground, RoundedCornerShape(2.dp)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ρ ≈ 0 (Orthogonal)", fontSize = 8.sp, color = QuantTextMuted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).background(QuantAmber, RoundedCornerShape(2.dp)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("0.15 to 0.45", fontSize = 8.sp, color = QuantTextMuted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).background(QuantRed, RoundedCornerShape(2.dp)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("ρ > 0.45 (Crowded)", fontSize = 8.sp, color = QuantTextMuted)
        }
      }
    }
  }
}
