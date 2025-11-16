package com.example.quizai


import android.content.Context
import androidx.room.Room

object DatabaseModule {

    @Volatile
    private var INSTANCE: QuizDatabase? = null

    fun getDatabase(context: Context): QuizDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                QuizDatabase::class.java,
                "quiz_ai_database"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}
