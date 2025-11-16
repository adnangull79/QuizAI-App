package com.example.quizai

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonInstance {
    val gson: Gson = GsonBuilder()
        .setLenient() // Allow slightly malformed JSON
        .serializeNulls() // Handle null values properly
        .create()
}