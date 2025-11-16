package com.example.quizai



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts ORDER BY date DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("DELETE FROM quiz_attempts WHERE id = :attemptId")
    suspend fun deleteAttempt(attemptId: Int)
}
