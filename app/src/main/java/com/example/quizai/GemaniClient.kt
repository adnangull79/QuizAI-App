package com.example.quizai


import com.google.ai.client.generativeai.GenerativeModel

object GeminiClient {

    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.GEMINI_API_KEY
    )
}
