package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.ConnectionConfig
import com.example.ui.theme.*

@Composable
fun SettingsConnectionScreen(
  config: ConnectionConfig,
  isTesting: Boolean,
  connectionResult: Pair<Boolean, String>?,
  onSaveSettings: (String, String, Int, String, Boolean) -> Unit,
  onTestConnection: () -> Unit,
  onSetDemoMode: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var serverUrl by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
  var openDHost by remember(config.openDHost) { mutableStateOf(config.openDHost) }
  var openDPortText by remember(config.openDPort) { mutableStateOf(config.openDPort.toString()) }
  var apiKey by remember(config.apiKey) { mutableStateOf(config.apiKey) }
  var isDemoMode by remember(config.isDemoMode) { mutableStateOf(config.isDemoMode) }
  var showPythonBridgeCode by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Connection Mode Selector
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "OPERATION MODE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = QuantCyan
          )
          Text(
            text = "Select how the mobile companion operates",
            fontSize = 12.sp,
            color = QuantTextSecondary
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isDemoMode) QuantCyan.copy(alpha = 0.15f) else QuantDarkBackground)
              .border(1.dp, if (isDemoMode) QuantCyan else QuantBorder, RoundedCornerShape(10.dp))
              .clickable {
                isDemoMode = true
                onSetDemoMode(true)
              }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isDemoMode,
              onClick = {
                isDemoMode = true
                onSetDemoMode(true)
              },
              colors = RadioButtonDefaults.colors(selectedColor = QuantCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Built-in Engine Simulator (Active)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuantTextPrimary
              )
              Text(
                text = "Self-contained quant engine simulator matching trade-suite's exact CLI & demo pipelines. 100% offline & functional.",
                fontSize = 11.sp,
                color = QuantTextSecondary,
                lineHeight = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (!isDemoMode) QuantCyan.copy(alpha = 0.15f) else QuantDarkBackground)
              .border(1.dp, if (!isDemoMode) QuantCyan else QuantBorder, RoundedCornerShape(10.dp))
              .clickable {
                isDemoMode = false
                onSetDemoMode(false)
              }
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = !isDemoMode,
              onClick = {
                isDemoMode = false
                onSetDemoMode(false)
              },
              colors = RadioButtonDefaults.colors(selectedColor = QuantCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Connect to Live trade-suite Codebase",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuantTextPrimary
              )
              Text(
                text = "Sync telemetry, blotter, and agent votes with your running trade-suite backend / moomoo OpenD gateway.",
                fontSize = 11.sp,
                color = QuantTextSecondary,
                lineHeight = 15.sp
              )
            }
          }
        }
      }
    }

    // 2. Network Endpoint Configuration
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "HOST & GATEWAY ENDPOINTS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = QuantTextPrimary
          )
          Text(
            text = "Configure API bridge and moomoo openD ports",
            fontSize = 12.sp,
            color = QuantTextSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Server URL
          Text("trade-suite API URL:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = QuantTextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            placeholder = { Text("e.g. http://10.0.2.2:8000 or http://192.168.1.50:8000", fontSize = 12.sp) },
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
              .testTag("server_url_input"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Moomoo OpenD Host & Port
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Column(modifier = Modifier.weight(2f)) {
              Text("moomoo OpenD Host:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = QuantTextSecondary)
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = openDHost,
                onValueChange = { openDHost = it },
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
                  .testTag("opend_host_input"),
                singleLine = true
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("Port:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = QuantTextSecondary)
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = openDPortText,
                onValueChange = { openDPortText = it },
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
                  .testTag("opend_port_input"),
                singleLine = true
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // API Key (Optional)
          Text("API Token (Optional):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = QuantTextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            placeholder = { Text("TS-XXXX-XXXX-XXXX or bearer token", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = QuantDarkBackground,
              unfocusedContainerColor = QuantDarkBackground,
              focusedBorderColor = QuantCyan,
              unfocusedBorderColor = QuantBorder,
              focusedTextColor = QuantTextPrimary,
              unfocusedTextColor = QuantTextPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Action buttons: Save & Test Connection
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                val port = openDPortText.toIntOrNull() ?: 11111
                onSaveSettings(serverUrl, openDHost, port, apiKey, isDemoMode)
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Save Config", color = QuantTextPrimary)
            }

            Button(
              onClick = onTestConnection,
              enabled = !isTesting,
              colors = ButtonDefaults.buttonColors(
                containerColor = QuantCyan,
                contentColor = Color(0xFF00363D)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("test_connection_button")
            ) {
              if (isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00363D), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Testing...")
              } else {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Ping", fontWeight = FontWeight.Bold)
              }
            }
          }

          // Test Result Alert
          if (connectionResult != null) {
            Spacer(modifier = Modifier.height(10.dp))
            val isSuccess = connectionResult.first
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSuccess) QuantGreen.copy(alpha = 0.15f) else QuantDarkBackground)
                .border(1.dp, if (isSuccess) QuantGreen else QuantAmber, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (isSuccess) QuantGreen else QuantAmber,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = connectionResult.second,
                  fontSize = 11.sp,
                  color = QuantTextPrimary,
                  lineHeight = 15.sp
                )
              }
            }
          }
        }
      }
    }

    // 3. Zero-Change Backend Bridge Guide
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = QuantDarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showPythonBridgeCode = !showPythonBridgeCode }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Code, contentDescription = null, tint = QuantCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "HOW TO CONNECT TRADE-SUITE",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = QuantTextPrimary
                )
                Text(
                  text = "Zero changes to your repository needed",
                  fontSize = 11.sp,
                  color = QuantTextSecondary
                )
              }
            }
            Icon(
              imageVector = if (showPythonBridgeCode) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = null,
              tint = QuantTextSecondary
            )
          }

          AnimatedVisibility(visible = showPythonBridgeCode) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
              Text(
                text = "Option A (Built-in Web Dashboard):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = QuantCyan
              )
              Text(
                text = "Run 'trade-suite launch web' in your terminal. It exposes the shared engine services at port 8000.",
                fontSize = 11.sp,
                color = QuantTextSecondary
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Option B (Lightweight 1-file standalone bridge):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = QuantCyan
              )
              Text(
                text = "Save this single Python script anywhere outside the repo to bridge the app:",
                fontSize = 11.sp,
                color = QuantTextSecondary
              )

              Spacer(modifier = Modifier.height(6.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(QuantDarkBackground)
                  .padding(10.dp)
              ) {
                Text(
                  text = """# mobile_bridge.py
from fastapi import FastAPI
import uvicorn
from trade_suite import pipeline, env

app = FastAPI()

@app.get("/status")
def status():
    return {"status": "ok", "engines": 36, "tests": 438}

@app.get("/desk")
def desk():
    return pipeline.run_desk(["SPY", "AAPL"])

@app.get("/trades")
def trades():
    return pipeline.run_trades()

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
""",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = QuantCyan,
                  lineHeight = 14.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
