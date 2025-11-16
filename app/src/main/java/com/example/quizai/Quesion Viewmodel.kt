package com.example.quizai

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class QuizQuestionViewModel : ViewModel() {

    // Fields for history saving
    var topic: String = ""
    var difficulty: String = ""

    var questionsList = listOf<QuizQuestion>()

    var currentIndex = mutableStateOf(0)
    var selectedAnswer = mutableStateOf<String?>(null)

    // ✅ NOW stores FULL OPTION TEXT (e.g., "Balance Sheet" instead of "A")
    private val userAnswersMap = mutableMapOf<Int, String>()

    val currentQuestion: QuizQuestion
        get() = questionsList[currentIndex.value]

    val questionCount: Int
        get() = questionsList.size

    fun loadQuiz(quiz: List<QuizQuestion>) {
        questionsList = quiz
        currentIndex.value = 0
        selectedAnswer.value = null
        userAnswersMap.clear()
    }

    /**
     * ✅ FIXED: Store the FULL option text directly (no letter extraction)
     */
    fun selectAnswer(optionText: String) {
        selectedAnswer.value = optionText
        userAnswersMap[currentIndex.value] = optionText
    }

    fun nextQuestion() {
        if (currentIndex.value < questionsList.lastIndex) {
            currentIndex.value++
            // Load previous answer if exists
            selectedAnswer.value = userAnswersMap[currentIndex.value]
        }
    }

    fun previousQuestion() {
        if (currentIndex.value > 0) {
            currentIndex.value--
            // Load previous answer
            selectedAnswer.value = userAnswersMap[currentIndex.value]
        }
    }

    fun isLastQuestion(): Boolean {
        return currentIndex.value == questionsList.lastIndex
    }

    /**
     * ✅ FIXED: Compare full option text with full answer text
     */
    fun calculateScore(): Int {
        var correctCount = 0
        userAnswersMap.forEach { (index, userAnswer) ->
            val correctAnswer = questionsList[index].answer
            // Direct comparison of full text
            if (userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)) {
                correctCount++
            }
        }
        return correctCount
    }

    /**
     * Returns the full option text the user selected
     */
    fun getUserAnswer(index: Int): String {
        return userAnswersMap[index] ?: ""
    }

    fun getAnsweredCount(): Int {
        return userAnswersMap.size
    }

    fun isCurrentQuestionAnswered(): Boolean {
        return userAnswersMap.containsKey(currentIndex.value)
    }

    fun resetQuiz() {
        currentIndex.value = 0
        selectedAnswer.value = null
        userAnswersMap.clear()
    }
}