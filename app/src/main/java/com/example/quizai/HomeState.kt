package com.example.quizai


data class HomeState(
    val recentResults: List<QuizResult> = emptyList()
)

data class QuizResult(
    val topic: String,
    val score: Int,
    val date: String
)
