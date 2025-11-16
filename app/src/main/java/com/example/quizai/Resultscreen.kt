package com.example.quizai.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizai.AnswerReview
import com.example.quizai.QuizHistoryViewModel
import com.example.quizai.QuizQuestionViewModel
import com.example.quizai.QuizResultViewModel
import com.example.quizai.Screen
import com.example.quizai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    score: Int,
    quizVM: QuizQuestionViewModel,
    resultVM: QuizResultViewModel,
    historyVM: QuizHistoryViewModel
) {
    val totalQuestions = quizVM.questionCount
    val correctAnswers = resultVM.calculateCorrectAnswers(quizVM)
    val wrongAnswers = totalQuestions - correctAnswers
    val percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions) else 0f

    val reviewList = resultVM.buildReviewList(quizVM)

    // 🔥 Auto-save quiz result to Room
    LaunchedEffect(Unit) {
        historyVM.saveQuizResult(
            topic = quizVM.topic,
            difficulty = quizVM.difficulty,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            wrongAnswersList = reviewList
        )
    }

    var showRetakeDialog by remember { mutableStateOf(false) }

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quiz Results",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showRetakeDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retake", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(2.dp, PrimaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Home", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            // ---------- CIRCLE SCORE ----------
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    color = PrimaryColor,
                    strokeWidth = 12.dp,
                    trackColor = Color(0xFFE0E0E0),
                    modifier = Modifier.size(160.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$correctAnswers/$totalQuestions",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Correct",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                getPerformanceMessage(percentage),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                "You have successfully completed the quiz.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            // ---------- SUMMARY ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryRow(Icons.Default.QuestionMark, "Total Questions", totalQuestions.toString(), PrimaryColor)
                    SummaryRow(Icons.Default.CheckCircle, "Correct Answers", correctAnswers.toString(), Color(0xFF4CAF50))
                    SummaryRow(Icons.Default.Close, "Wrong Answers", wrongAnswers.toString(), Color(0xFFE53935))
                }
            }

            Text(
                "Review Your Answers",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                reviewList.forEachIndexed { index, item ->
                    ReviewAnswerCard(questionNumber = index + 1, item = item)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showRetakeDialog) {
        AlertDialog(
            onDismissRequest = { showRetakeDialog = false },
            icon = {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Retake Quiz", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text("Do you want to retake this quiz or generate a new one?", textAlign = TextAlign.Center)
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showRetakeDialog = false
                            quizVM.resetQuiz()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retake This Quiz", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showRetakeDialog = false
                            navController.navigate(Screen.GenerateQuiz.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, PrimaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generate New Quiz", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showRetakeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReviewAnswerCard(
    questionNumber: Int,
    item: AnswerReview
) {
    val isCorrect = item.isCorrect

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    "Question $questionNumber",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Question
            Text(
                item.question,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Your Answer
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Your Answer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCorrect) {
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    } else {
                        Color(0xFFE53935).copy(alpha = 0.1f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.3f)
                        else Color(0xFFE53935).copy(alpha = 0.3f)
                    )
                ) {
                    if (item.yourAnswer.isNotEmpty()) {
                        Text(
                            item.yourAnswer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            "Not answered",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Correct Answer (only if wrong)
            if (!isCorrect) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Correct Answer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                    ) {
                        Text(
                            item.correctAnswer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

fun getPerformanceMessage(percentage: Float): String {
    return when {
        percentage >= 0.9f -> "Outstanding! 🎉"
        percentage >= 0.75f -> "Great Job! 👏"
        percentage >= 0.6f -> "Good Work! 👍"
        percentage >= 0.5f -> "Well Done! 💪"
        else -> "Keep Practicing! 📚"
    }
}