package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
