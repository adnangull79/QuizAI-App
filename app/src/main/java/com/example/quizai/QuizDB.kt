package com.example.quizai



import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuizAttemptEntity::class,
        WrongAnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun wrongAnswerDao(): WrongAnswerDao
}
