package com.example.quizai



class QuizRepository(
    private val attemptDao: QuizAttemptDao,
    private val wrongDao: WrongAnswerDao
) {

    // Insert attempt + wrong answers together
    suspend fun saveQuizResult(
        attempt: QuizAttemptEntity,
        wrongAnswers: List<WrongAnswerEntity>
    ) {
        // Insert attempt summary
        val attemptId = attemptDao.insertAttempt(attempt)

        // Insert wrong answers with assigned attemptId
        wrongAnswers.forEach { item ->
            wrongDao.insertWrongAnswer(
                item.copy(attemptId = attemptId.toInt())
            )
        }
    }

    // Load all attempts
    fun getAllAttempts() = attemptDao.getAllAttempts()

    // Load wrong questions for a specific attempt
    suspend fun getWrongAnswersForAttempt(attemptId: Int) =
        wrongDao.getWrongAnswers(attemptId)

    // Delete attempt + related answers
    suspend fun deleteAttempt(attemptId: Int) {
        wrongDao.deleteAnswersForAttempt(attemptId)
        attemptDao.deleteAttempt(attemptId)
    }
}
