package com.example.gemini

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun GeminiAssistantDialog(
    currentMovieTitle: String,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Hi! I'm your Netflip AI Movie Assistant. Ask me anything about ${if (currentMovieTitle.isNotEmpty()) currentMovieTitle else "movies"}!") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Netflip AI Assistant",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFE50914),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Text(
                            text = response,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Ask something...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFE50914),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color(0xFFE50914)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (prompt.isNotBlank() && !isLoading) {
                                val currentPrompt = prompt
                                prompt = ""
                                isLoading = true
                                coroutineScope.launch {
                                    val fullContextPrompt = if (currentMovieTitle.isNotEmpty()) {
                                        "Context: I am currently watching/browsing the movie '$currentMovieTitle'.\nUser query: $currentPrompt"
                                    } else {
                                        currentPrompt
                                    }
                                    response = GeminiApi.generateContent(fullContextPrompt)
                                    isLoading = false
                                }
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (prompt.isNotBlank() && !isLoading) {
                                    val currentPrompt = prompt
                                    prompt = ""
                                    isLoading = true
                                    coroutineScope.launch {
                                        val fullContextPrompt = if (currentMovieTitle.isNotEmpty()) {
                                            "Context: I am currently watching/browsing the movie '$currentMovieTitle'.\nUser query: $currentPrompt"
                                        } else {
                                            currentPrompt
                                        }
                                        response = GeminiApi.generateContent(fullContextPrompt)
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFFE50914))
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFE50914))
            }
        },
        containerColor = Color(0xFF141414),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
