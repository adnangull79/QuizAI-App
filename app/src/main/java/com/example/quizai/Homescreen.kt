package com.example.quizai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.example.quizai.HomeViewModel
import com.example.quizai.Screen
import com.example.quizai.ui.theme.PrimaryColor
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.quizai.QuizAttemptEntity
import com.example.quizai.QuizHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// -------------------------------------------------------
// MAIN HOME SCREEN
// -------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    historyVM: QuizHistoryViewModel,
    viewModel: HomeViewModel
) {
    val recentResults by viewModel.recentResults.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    // Drawer width = 80%
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val drawerWidth: Dp = ((screenWidthDp * 0.80f).toInt()).dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerContainerColor = bgColor
            ) {
                DrawerHeader()
                Divider()

                DrawerThemeToggle(isDarkTheme, onToggleTheme)
                Divider()

                DrawerMenuItem(Icons.Default.Delete, "Clear History") {}
                Divider()

                DrawerMenuItem(Icons.Default.Share, "Share App") {}
                Divider()

                DrawerMenuItem(Icons.Default.Info, "About") {}
                Divider()

                Spacer(modifier = Modifier.weight(1f))

                DrawerMenuItem(Icons.Default.Settings, "Settings") {}
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    ) {

        Scaffold(
            containerColor = bgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "QuizAI",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PrimaryColor
                    )
                )
            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .background(bgColor)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // -------------------- GENERATE QUIZ CARD --------------------
                item {
                    GenerateQuizCard(
                        onStartClick = {
                            navController.navigate(Screen.GenerateQuiz.route)
                        }
                    )
                }

                // -------------------- RECENT RESULTS HEADER --------------------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Results",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { navController.navigate(Screen.History.route) }
                        ) {
                            Text("See All", color = PrimaryColor)
                        }
                    }
                }

                // -------------------- SHOW REAL RECENT RESULTS --------------------
                if (recentResults.isEmpty()) {
                    item {
                        Text(
                            "No recent quizzes yet!",
                            color = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    items(recentResults) { attempt ->
                        ResultItemFromAttempt(attempt) {
                            navController.navigate(Screen.AttemptDetail.route + "/${attempt.id}")
                        }
                    }
                }
            }
        }
    }
}



// -------------------------------------------------------
// RESULT ITEM — REAL DB DATA (FIXED CLICKABLE)
// -------------------------------------------------------
@Composable
fun ResultItemFromAttempt(
    attempt: QuizAttemptEntity,
    onClick: () -> Unit
) {
    val dateString =
        SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(attempt.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                indication = null,   // 🟢 FIX: no ripple → no crash
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(PrimaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = PrimaryColor)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        attempt.topic,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        dateString,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                "${attempt.correctAnswers}/${attempt.totalQuestions}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



// -------------------------------------------------------
// REST OF FILE (Drawer + GenerateQuizCard) IS UNCHANGED
// -------------------------------------------------------


// -------------------------------------------------------
// BELOW CONTENT IS SAME AS YOUR ORIGINAL — NOT CHANGED
// -------------------------------------------------------

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ) {
        Text(
            "QuizAI",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = PrimaryColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Your AI Quiz Companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun DrawerThemeToggle(isDark: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            "Dark Theme",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isDark,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = Color.Transparent
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun GenerateQuizCard(onStartClick: () -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {

        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Generate New Quiz",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Create AI-powered quizzes on any topic and test your knowledge instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Start", color = Color.White)
            }
        }
    }
}
