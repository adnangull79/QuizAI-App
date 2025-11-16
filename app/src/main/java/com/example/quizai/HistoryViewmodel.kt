package com.example.quizai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QuizHistoryViewModel(
    val repository: QuizRepository   // 🔥 Exposed for deletion and detail screens
) : ViewModel() {

    // Save result from ResultScreen
    fun saveQuizResult(
        topic: String,
        difficulty: String,
        totalQuestions: Int,
        correctAnswers: Int,
        wrongAnswersList: List<AnswerReview>
    ) {
        viewModelScope.launch {

            // Build summary entity
            val attempt = QuizAttemptEntity(
                date = System.currentTimeMillis(),
                topic = topic,
                difficulty = difficulty,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                wrongAnswers = totalQuestions - correctAnswers,
                percentage = (correctAnswers.toFloat() / totalQuestions)
            )

            // Convert wrong answers
            val wrongEntities = wrongAnswersList
                .filter { !it.isCorrect }  // store only wrong ones
                .map {
                    WrongAnswerEntity(
                        attemptId = 0, // temp, replaced inside repository
                        question = it.question,
                        yourAnswer = it.yourAnswer,
                        correctAnswer = it.correctAnswer
                    )
                }

            repository.saveQuizResult(attempt, wrongEntities)
        }
    }

    // Load all attempts for History Screen
    fun getHistory() = repository.getAllAttempts()

    // Load wrong answers for one attempt
    suspend fun getWrongAnswers(attemptId: Int) =
        repository.getWrongAnswersForAttempt(attemptId)

    // 🔥 NEW: Delete an attempt + related wrong answers
    fun deleteAttempt(attemptId: Int) {
        viewModelScope.launch {
            repository.deleteAttempt(attemptId)
        }
    }
    // Add this function to QuizHistoryViewModel
    suspend fun getAllAnswersForAttempt(attemptId: Int): List<DetailAnswerItem> {
        // Get all wrong answers
        val wrongAnswers = repository.getWrongAnswersForAttempt(attemptId)

        // Convert to DetailAnswerItem format
        return wrongAnswers.map { wrong ->
            DetailAnswerItem(
                question = wrong.question,
                yourAnswer = wrong.yourAnswer,
                correctAnswer = wrong.correctAnswer,
                isCorrect = false
            )
        }

        // Note: You'll need to store correct answers too if you want to show them
        // For now, this only shows wrong answers like before
    }

}
