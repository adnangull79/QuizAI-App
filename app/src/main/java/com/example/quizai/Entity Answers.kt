package com.example.quizai



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_answers")
data class WrongAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attemptId: Int,
    val question: String,
    val yourAnswer: String,
    val correctAnswer: String
)
