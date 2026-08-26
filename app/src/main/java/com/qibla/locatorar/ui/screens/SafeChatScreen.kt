package com.qibla.locatorar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.qibla.locatorar.data.models.ChatMessage
import com.qibla.locatorar.ui.components.ChatBubble
import com.qibla.locatorar.utils.AppConstants
import com.qibla.locatorar.utils.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val DEFAULT_GREETING = ChatMessage(
    "Assalamu alaikum. I am your Islamic learning assistant. How can I help you today?",
    false
)

class ChatViewModel : ViewModel() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.5-flash-lite",
        apiKey = AppConstants.GEMINI_API_KEY,
        systemInstruction = content {
            text("You are a helpful and respectful Islamic assistant. " +
                    "Provide general information about Islamic learning, duas, prayer basics, and Quranic themes. " +
                    "Always advise the user to consult a qualified scholar for specific fatwas or complex rulings. " +
                    "Keep answers concise and accurate. Do not engage in unsafe or non-Islamic romantic chat.")
        }
    )

    private val chat = generativeModel.startChat()

    // Load any previously saved conversation, falling back to the greeting.
    private val _messages = MutableStateFlow(
        PreferenceHelper.getSafeChatMessages().ifEmpty { listOf(DEFAULT_GREETING) }
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private fun persist() {
        PreferenceHelper.saveSafeChatMessages(_messages.value)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.value = _messages.value + userMessage
        persist()
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(text)
                val aiMessage = ChatMessage(response.text ?: "I couldn't generate a response.", false)
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(e.toString(), false)
            } finally {
                _isLoading.value = false
                persist()
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(DEFAULT_GREETING)
        PreferenceHelper.clearSafeChatMessages()
    }
}

@Composable
fun SafeChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom whenever messages change (including first load) or loading indicator appears/disappears
    LaunchedEffect(messages.size, isLoading) {
        val lastIndex = messages.size - 1 + if (isLoading) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Islamic Assistant", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { viewModel.clearChat() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear chat",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Clear chat")
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("AI is thinking...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask an Islamic question") },
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim())
                        input = ""
                    }
                })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim())
                        input = ""
                    }
                },
                enabled = !isLoading && input.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}