package com.example.quizai

sealed class Screen(val route: String) {

    object Home : Screen("home_screen")

    object GenerateQuiz : Screen("generate_quiz")

    // Quiz Screen with a dynamic argument
    object Quiz : Screen("quiz_screen/{title}") {
        fun passTitle(title: String): String {
            return "quiz_screen/$title"
        }
    }
    object History : Screen("history")
    object AttemptDetail : Screen("attempt_detail")


    // Result screen
    object Result : Screen("result_screen")
}
