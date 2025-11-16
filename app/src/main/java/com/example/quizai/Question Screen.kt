package com.example.quizai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizai.GenerateQuizViewModel
import com.example.quizai.QuizQuestionViewModel
import com.example.quizai.Screen
import com.example.quizai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionScreen(
    navController: NavController,
    quizTitle: String = "Quiz",
    generateVM: GenerateQuizViewModel,
    quizVM: QuizQuestionViewModel
) {

    // Load quiz only once
    LaunchedEffect(Unit) {
        generateVM.parsedQuiz.value?.let {
            quizVM.loadQuiz(it.questions)
            // Store topic and difficulty for history
            quizVM.topic = generateVM.topic.value
            quizVM.difficulty = generateVM.selectedDifficulty.value
        }
    }

    if (quizVM.questionsList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No quiz data found!",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    val question = quizVM.currentQuestion

    var showQuitDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    val capitalizedTitle = quizTitle.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        capitalizedTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    TextButton(onClick = { showQuitDialog = true }) {
                        Text("Quit", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // ---------------- PROGRESS ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                LinearProgressIndicator(
                    progress = (quizVM.currentIndex.value + 1f) / quizVM.questionCount,
                    color = PrimaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .weight(1f)
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    "${quizVM.currentIndex.value + 1}/${quizVM.questionCount}",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- QUESTION ----------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PrimaryColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    question.question,
                    modifier = Modifier.padding(20.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- OPTIONS ----------------
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                question.options.forEach { option ->

                    // ✅ FIXED: Compare full option text directly
                    val isSelected = quizVM.selectedAnswer.value?.trim()?.equals(option.trim(), ignoreCase = true) == true

                    val bgColor = if (isSelected) PrimaryColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface

                    val borderColor = if (isSelected) PrimaryColor
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // ✅ FIXED: Pass full option text directly
                                quizVM.selectAnswer(option)
                            },
                        color = bgColor,
                        border = BorderStroke(2.dp, borderColor),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            option,
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected) PrimaryColor
                            else MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ---------------- BUTTONS ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    onClick = { quizVM.previousQuestion() },
                    enabled = quizVM.currentIndex.value > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    border = BorderStroke(2.dp, PrimaryColor),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Text("Previous", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (quizVM.isLastQuestion())
                            showSubmitDialog = true
                        else quizVM.nextQuestion()
                    },
                    enabled = quizVM.selectedAnswer.value != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        if (quizVM.isLastQuestion()) "Submit" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // ---------------- QUIT DIALOG ----------------
        if (showQuitDialog) {
            AlertDialog(
                onDismissRequest = { showQuitDialog = false },
                title = { Text("Quit Quiz?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to quit? Your progress will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        showQuitDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Quit", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ---------------- SUBMIT DIALOG ----------------
        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Submit Quiz?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to submit your answers?") },
                confirmButton = {
                    TextButton(onClick = {
                        showSubmitDialog = false
                        val score = quizVM.calculateScore()
                        navController.navigate(Screen.Result.route + "?score=$score")
                    }) {
                        Text("Submit", color = PrimaryColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}