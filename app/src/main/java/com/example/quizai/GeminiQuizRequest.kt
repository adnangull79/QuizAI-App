package com.example.quizai


data class GeminiQuizRequest(
    val contents: List<QuizContent>
)

data class QuizContent(
    val role: String,
    val parts: List<QuizPart>
)

data class QuizPart(
    val text: String
)
