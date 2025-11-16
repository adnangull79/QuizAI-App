package com.example.quizai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.quizai.ui.theme.QuizAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // APP-WIDE THEME STATE (Light/Dark)
            var isDarkTheme by remember { mutableStateOf(false) }

            // Apply theme and pass state downward
            QuizAITheme(darkTheme = isDarkTheme) {

                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}
