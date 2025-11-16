package com.example.quizai

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
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
import com.example.quizai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptDetailScreen(
    navController: NavController,
    historyVM: QuizHistoryViewModel,
    attemptId: Int
) {
    var attempt by remember { mutableStateOf<QuizAttemptEntity?>(null) }
    var allAnswers by remember { mutableStateOf<List<DetailAnswerItem>>(emptyList()) }

    LaunchedEffect(attemptId) {
        historyVM.getHistory().collect { attempts ->
            attempt = attempts.find { it.id == attemptId }
        }

        // Get all answers (both correct and wrong)
        allAnswers = historyVM.getAllAnswersForAttempt(attemptId)
    }

    val data = attempt ?: return

    val correct = data.correctAnswers
    val total = data.totalQuestions
    val wrong = data.wrongAnswers
    val percentage = data.percentage

    // Animate progress
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
                        "Quiz Details",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
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

            // Topic & Difficulty
            Text(
                text = data.topic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PrimaryColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = data.difficulty,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // ANIMATED SCORE CIRCLE
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
                        "$correct/$total",
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

            // SUMMARY CARD
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
                    DetailSummaryRow(
                        Icons.Default.QuestionMark,
                        "Total Questions",
                        total.toString(),
                        PrimaryColor
                    )

                    DetailSummaryRow(
                        Icons.Default.CheckCircle,
                        "Correct Answers",
                        correct.toString(),
                        Color(0xFF4CAF50)
                    )

                    DetailSummaryRow(
                        Icons.Default.Close,
                        "Wrong Answers",
                        wrong.toString(),
                        Color(0xFFE53935)
                    )
                }
            }

            // Review Section Header
            Text(
                text = "Review Your Answers",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            // All Answers (Correct + Wrong)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allAnswers.forEachIndexed { index, item ->
                    DetailAnswerCard(questionNumber = index + 1, item = item)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DetailSummaryRow(
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
fun DetailAnswerCard(
    questionNumber: Int,
    item: DetailAnswerItem
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
                    Text(
                        item.yourAnswer.ifEmpty { "Not answered" },
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = if (item.yourAnswer.isEmpty()) FontStyle.Italic else FontStyle.Normal
                    )
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

// Data class for displaying answers
data class DetailAnswerItem(
    val question: String,
    val yourAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

fun getPerformanceMessage(percentage: Float): String {
    return when {
        percentage >= 0.9f -> "Outstanding! 🎉"
        percentage >= 0.75f -> "Great Job! 👏"
        percentage >= 0.6f -> "Good Work! 👍"
        percentage >= 0.5f -> "Well Done! 💪"
        else -> "Keep Practicing! 📚"
    }
}