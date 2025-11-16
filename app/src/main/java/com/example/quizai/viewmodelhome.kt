package com.example.quizai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _recentResults = MutableStateFlow<List<QuizAttemptEntity>>(emptyList())
    val recentResults: StateFlow<List<QuizAttemptEntity>> = _recentResults

    init {
        loadRecentResults()
    }

    private fun loadRecentResults() {
        viewModelScope.launch {
            // Get all attempts (Flow → List)
            val all = repository.getAllAttempts().first()

            // Take latest 3
            _recentResults.value = all.take(3)
        }
    }
}
