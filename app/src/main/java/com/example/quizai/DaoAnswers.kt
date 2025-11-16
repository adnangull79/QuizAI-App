package com.example.quizai


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WrongAnswerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongAnswer(item: WrongAnswerEntity)

    @Query("SELECT * FROM wrong_answers WHERE attemptId = :attemptId")
    suspend fun getWrongAnswers(attemptId: Int): List<WrongAnswerEntity>

    @Query("DELETE FROM wrong_answers WHERE attemptId = :attemptId")
    suspend fun deleteAnswersForAttempt(attemptId: Int)
}
