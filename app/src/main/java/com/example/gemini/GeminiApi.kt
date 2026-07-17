package com.example.gemini

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String, model: String = "gemini-3.1-pro-preview"): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback to Zenmux using the user's provided API key and the free model!
            val zenmuxApiKey = "sk-ai-v1-6707cb54e836d3a7b2726dd11e36e937617fa672d69653d147c4b41ad8dc7ef8"
            val zenmuxModel = "z-ai/glm-4.7-flash-free"
            val zenmuxUrl = "https://zenmux.ai/api/v1/chat/completions"

            val jsonBody = JSONObject().apply {
                put("model", zenmuxModel)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(zenmuxUrl)
                .header("Authorization", "Bearer $zenmuxApiKey")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    val root = JSONObject(responseString)
                    val choices = root.optJSONArray("choices")
                    val message = choices?.optJSONObject(0)?.optJSONObject("message")
                    val text = message?.optString("content")
                    return@withContext text ?: "No response from Assistant."
                } else {
                    return@withContext "Error calling Zenmux Assistant: ${response.code} ${response.message}\n${response.body?.string()}"
                }
            } catch (e: Exception) {
                return@withContext "Failed to connect to Zenmux Assistant: ${e.message}"
            }
        }

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Add thinking mode for the pro model
            if (model.contains("pro-preview")) {
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
            
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val root = JSONObject(responseString)
                val candidates = root.optJSONArray("candidates")
                val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                return@withContext text ?: "No response from Gemini."
            } else {
                return@withContext "Error: ${response.code} ${response.message}\n${response.body?.string()}"
            }
        } catch (e: Exception) {
            return@withContext "Failed to connect to Gemini: ${e.message}"
        }
    }
}
