package com.example.quizai

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quizai.ui.screens.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Shared ViewModels
    val resultVM: QuizResultViewModel = viewModel()
    val generateVM: GenerateQuizViewModel = viewModel()
    val quizVM: QuizQuestionViewModel = viewModel()

    // Database + History VM
    val context = LocalContext.current
    val db = DatabaseModule.getDatabase(context)

    val historyRepository = QuizRepository(
        attemptDao = db.quizAttemptDao(),
        wrongDao = db.wrongAnswerDao()
    )

    val historyVM: QuizHistoryViewModel = viewModel(
        factory = QuizHistoryViewModelFactory(historyRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // ---------------- HOME ----------------
        composable(Screen.Home.route) {

            val context = LocalContext.current
            val db = DatabaseModule.getDatabase(context)

            val homeRepository = QuizRepository(
                attemptDao = db.quizAttemptDao(),
                wrongDao = db.wrongAnswerDao()
            )

            val homeVM: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(homeRepository)
            )

            HomeScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                historyVM = historyVM,
                viewModel = homeVM
            )
        }

        // ---------------- GENERATE QUIZ ----------------
        composable(Screen.GenerateQuiz.route) {
            GenerateQuizScreen(
                navController = navController,
                viewModel = generateVM
            )
        }

        // ---------------- ATTEMPT DETAIL ----------------
        composable(
            route = Screen.AttemptDetail.route + "/{id}"
        ) { backStackEntry ->

            val attemptId = backStackEntry.arguments?.getString("id")?.toInt() ?: 0

            AttemptDetailScreen(
                navController = navController,
                historyVM = historyVM,
                attemptId = attemptId
            )
        }

        // ---------------- QUIZ ----------------
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("title") { defaultValue = "Quiz" }
            )
        ) { backStackEntry ->

            val quizTitle = backStackEntry.arguments?.getString("title") ?: "Quiz"

            QuizQuestionScreen(
                navController = navController,
                generateVM = generateVM,
                quizVM = quizVM,
                quizTitle = quizTitle
            )
        }

        // ---------------- HISTORY ----------------
        composable(Screen.History.route) {
            HistoryScreen(
                navController = navController,
                historyVM = historyVM
            )
        }

        // ---------------- RESULT ----------------
        // ---------------- RESULT ----------------
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("score") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0

            ResultScreen(
                navController = navController,
                score = score,
                quizVM = quizVM,
                resultVM = resultVM,
                historyVM = historyVM    // ✅ Added back
            )
        }
    }
}