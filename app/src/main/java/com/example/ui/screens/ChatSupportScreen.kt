package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatSupportScreen(
  messages: List<ChatMessage>,
  isSending: Boolean,
  onSendMessage: (String) -> Unit,
  onClearChat: () -> Unit,
  modifier: Modifier = Modifier
) {
  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val suggestedPrompts = listOf(
    "Why was NVDA vetoed?",
    "Explain Donchian 20/10 KPIs",
    "How is Copper:Gold proxy computed?",
    "Show moomoo OpenD paper reconcile status",
    "What are the 6 specialist scouts?"
  )

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(QuantDarkBackground)
  ) {
    // 1. Context-Aware Support Agent Header
    Surface(
      color = QuantDarkSurfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(QuantCyan.copy(alpha = 0.2f))
              .border(1.dp, QuantCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = QuantCyan, modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "TRADESUITE COPILOT",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Black,
              color = QuantTextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(QuantGreen)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Context Memory Active (36 Engines • OpenD • NAV)",
                fontSize = 10.sp,
                color = QuantGreen,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        IconButton(
          onClick = onClearChat,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = QuantTextMuted)
        }
      }
    }

    // 2. Suggested Prompt Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(suggestedPrompts) { prompt ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(QuantDarkSurfaceVariant)
            .border(1.dp, QuantBorder, RoundedCornerShape(8.dp))
            .clickable {
              if (!isSending) {
                onSendMessage(prompt)
              }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = prompt,
            fontSize = 11.sp,
            color = QuantCyan,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    HorizontalDivider(color = QuantBorder, thickness = 1.dp)

    // 3. Conversation Message History
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(vertical = 12.dp)
    ) {
      items(messages) { message ->
        ChatBubble(message = message)
      }

      if (isSending) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = QuantCyan,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Copilot analyzing system state & thinking...",
              fontSize = 12.sp,
              color = QuantTextMuted,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
          }
        }
      }
    }

    // 4. Message Input Bar
    Surface(
      color = QuantDarkSurface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = textInput,
          onValueChange = { textInput = it },
          placeholder = { Text("Ask Copilot about strategies, vetoes, OpenD...", fontSize = 12.sp, color = QuantTextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = QuantDarkSurfaceVariant,
            unfocusedContainerColor = QuantDarkSurfaceVariant,
            focusedBorderColor = QuantCyan,
            unfocusedBorderColor = QuantBorder,
            focusedTextColor = QuantTextPrimary,
            unfocusedTextColor = QuantTextPrimary
          ),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_field"),
          maxLines = 3
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (textInput.isNotBlank() && !isSending) {
              val promptToSend = textInput.trim()
              textInput = ""
              onSendMessage(promptToSend)
              coroutineScope.launch {
                listState.animateScrollToItem(messages.size)
              }
            }
          },
          enabled = textInput.isNotBlank() && !isSending,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (textInput.isNotBlank() && !isSending) QuantCyan else QuantBorder)
            .testTag("chat_send_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (textInput.isNotBlank() && !isSending) Color(0xFF00363D) else QuantTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatBubble(message: ChatMessage) {
  val isUser = message.sender == MessageSender.USER

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Row(
      verticalAlignment = Alignment.Top,
      horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
      if (!isUser) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(QuantDarkSurfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = QuantCyan,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

      Box(
        modifier = Modifier
          .widthIn(max = 290.dp)
          .clip(
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            )
          )
          .background(
            if (isUser) QuantCyan.copy(alpha = 0.25f) else QuantDarkSurfaceVariant
          )
          .border(
            1.dp,
            if (isUser) QuantCyan.copy(alpha = 0.5f) else QuantBorder,
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            )
          )
          .padding(12.dp)
      ) {
        Column {
          Text(
            text = message.text,
            fontSize = 13.sp,
            color = QuantTextPrimary,
            lineHeight = 18.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = message.timestamp,
            fontSize = 9.sp,
            color = QuantTextMuted,
            modifier = Modifier.align(Alignment.End)
          )
        }
      }
    }
  }
}
