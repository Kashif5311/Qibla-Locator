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

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.google.ai.client.generativeai.Chat
import com.qibla.locatorar.R

private fun getDefaultGreeting(context: android.content.Context) = ChatMessage(
    context.getString(R.string.safe_chat_default_greeting),
    false
)

class ChatViewModel : ViewModel() {
    private var generativeModel: GenerativeModel? = null
    private var chat: Chat? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun init(context: android.content.Context) {
        if (generativeModel != null) return

        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-lite",
            apiKey = AppConstants.GEMINI_API_KEY,
            systemInstruction = content {
                text(context.getString(R.string.safe_chat_system_instruction))
            }
        )
        chat = generativeModel?.startChat()
        
        val saved = PreferenceHelper.getSafeChatMessages()
        _messages.value = saved.ifEmpty { listOf(getDefaultGreeting(context)) }
    }

    private fun persist() {
        PreferenceHelper.saveSafeChatMessages(_messages.value)
    }

    fun sendMessage(text: String, context: android.content.Context) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.value = _messages.value + userMessage
        persist()
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat?.sendMessage(text)
                val aiMessage = ChatMessage(response?.text ?: context.getString(R.string.safe_chat_error_no_response), false)
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(e.toString(), false)
            } finally {
                _isLoading.value = false
                persist()
            }
        }
    }

    fun clearChat(context: android.content.Context) {
        _messages.value = listOf(getDefaultGreeting(context))
        PreferenceHelper.clearSafeChatMessages()
    }
}

@Composable
fun SafeChatScreen(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.init(context)
    }

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
            Text(stringResource(R.string.safe_chat_title), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { viewModel.clearChat(context) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.safe_chat_clear_chat),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.safe_chat_clear_chat))
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
                        Text(stringResource(R.string.safe_chat_ai_thinking), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.safe_chat_placeholder)) },
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim(), context)
                        input = ""
                    }
                })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim(), context)
                        input = ""
                    }
                },
                enabled = !isLoading && input.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.safe_chat_send_description)
                )
            }
        }
    }
}
