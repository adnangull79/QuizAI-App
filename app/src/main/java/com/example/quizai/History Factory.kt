package com.example.quizai



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizHistoryViewModelFactory(
    private val repository: QuizRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizHistoryViewModel::class.java)) {
            return QuizHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
