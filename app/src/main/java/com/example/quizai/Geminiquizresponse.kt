package com.example.quizai


data class GeminiQuizResponse(
    val candidates: List<QuizCandidate>?
)

data class QuizCandidate(
    val content: QuizContentResponse?
)

data class QuizContentResponse(
    val parts: List<QuizPartResponse>?
)

data class QuizPartResponse(
    val text: String?
)
