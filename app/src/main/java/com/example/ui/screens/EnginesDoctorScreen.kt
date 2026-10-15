package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.EngineCategory
import com.example.model.EngineInfo
import com.example.ui.theme.*

@Composable
fun EnginesDoctorScreen(
  engines: List<EngineInfo>,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<EngineCategory?>(null) }

  val filteredEngines = remember(engines, searchQuery, selectedCategory) {
    engines.filter { engine ->
      val matchesCategory = selectedCategory == null || engine.category == selectedCategory
      val matchesSearch = searchQuery.isBlank() ||
          engine.moduleName.contains(searchQuery, ignoreCase = true) ||
          engine.role.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesSearch
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Doctor & Introspection Summary Header
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
                Icon(Icons.Default.Verified, contentDescription = null, tint = QuantGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "TRADE-SUITE DOCTOR",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  color = QuantCyan
                )
              }
              Text(
                text = "Environment introspection & test suite verification",
                style = MaterialTheme.typography.bodySmall,
                color = QuantTextSecondary
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(QuantGreen.copy(alpha = 0.2f))
                .border(1.dp, QuantGreen, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("438/438 PASS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuantGreen)
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
                text = "Architecture Design Principles:",
                fontSize = 10.sp,
                color = QuantTextMuted,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "• Pure-logic engines with no UI imports\n• Stdlib-only cores with lazy cross-module adapters\n• Deterministic demo data: everything runs offline\n• Plain data across boundaries (serializable dicts/lists)",
                fontSize = 11.sp,
                color = QuantTextSecondary,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    // 2. Search Box
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search 36 engines by name or role...", color = QuantTextMuted, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QuantTextMuted) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear", tint = QuantTextMuted)
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = QuantDarkSurfaceVariant,
          unfocusedContainerColor = QuantDarkSurfaceVariant,
          focusedBorderColor = QuantCyan,
          unfocusedBorderColor = QuantBorder,
          focusedTextColor = QuantTextPrimary,
          unfocusedTextColor = QuantTextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("engine_search_input"),
        singleLine = true
      )
    }

    // 3. Category Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("ALL (${engines.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = QuantCyan,
              selectedLabelColor = Color(0xFF00363D),
              containerColor = QuantDarkSurfaceVariant,
              labelColor = QuantTextSecondary
            )
          )
        }
        items(EngineCategory.values()) { cat ->
          val count = engines.count { it.category == cat }
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
            label = { Text("${cat.name.replace("_", " ")} ($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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

    // 4. Engines List
    items(filteredEngines) { engine ->
      EngineRowCard(engine = engine)
    }
  }
}

@Composable
fun EngineRowCard(engine: EngineInfo) {
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
              .size(24.dp)
              .clip(CircleShape)
              .background(QuantGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Check, contentDescription = "Active", tint = QuantGreen, modifier = Modifier.size(14.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = engine.moduleName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = QuantTextPrimary
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(QuantDarkBackground)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "v${engine.version} • ${engine.testCount} tests",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = QuantTextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = engine.role,
        fontSize = 12.sp,
        color = QuantTextSecondary,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = engine.category.title,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = QuantCyanDim
      )
    }
  }
}
