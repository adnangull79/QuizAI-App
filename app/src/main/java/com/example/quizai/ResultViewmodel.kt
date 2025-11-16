package com.example.quizai

import androidx.lifecycle.ViewModel

data class AnswerReview(
    val question: String,
    val yourAnswer: String,      // Full option text (e.g., "Balance Sheet")
    val correctAnswer: String    // Full option text (e.g., "Income Statement")
) {
    val isCorrect: Boolean get() = yourAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
}

class QuizResultViewModel : ViewModel() {

    fun buildReviewList(quizVM: QuizQuestionViewModel): List<AnswerReview> {
        return quizVM.questionsList.mapIndexed { index, question ->

            val userAnswer = quizVM.getUserAnswer(index)  // Full text like "Balance Sheet"
            val correctAnswer = question.answer           // Full text like "Income Statement"

            AnswerReview(
                question = question.question,
                yourAnswer = userAnswer,
                correctAnswer = correctAnswer
            )
        }
    }

    fun calculateCorrectAnswers(quizVM: QuizQuestionViewModel): Int {
        return quizVM.calculateScore()
    }
}