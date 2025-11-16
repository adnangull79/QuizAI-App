package com.example.quizai

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiQuizApiService {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateQuiz(
        @Query("key") apiKey: String,
        @Body request: GeminiQuizRequest
    ): GeminiQuizResponse
}