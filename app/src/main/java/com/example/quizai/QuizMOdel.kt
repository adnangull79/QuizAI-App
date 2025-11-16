package com.example.quizai


data class QuizResponse(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String
)
