package com.example.quizai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.quizai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GenerateQuizScreen(
    navController: NavController,
    viewModel: GenerateQuizViewModel
) {
    // ----------------- COLLECT STATE FROM VIEWMODEL -----------------
    val topic by viewModel.topic.collectAsState()
    val subtopicInput by viewModel.subtopicInput.collectAsState()
    val subtopics by viewModel.subtopics.collectAsState()
    val customPrompt by viewModel.customPrompt.collectAsState()
    val selectedQualification by viewModel.selectedQualification.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val mcqCount by viewModel.mcqCount.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val navigateToQuiz by viewModel.navigateToQuiz.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val outline = MaterialTheme.colorScheme.outline

    // ----------------- NAVIGATE WHEN READY -----------------
    LaunchedEffect(navigateToQuiz) {
        if (navigateToQuiz) {
            viewModel.clearAfterSuccess()
            navController.navigate(Screen.Quiz.passTitle(topic))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Generate Quiz",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PrimaryColor
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(bg)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ---------------- TOPIC ----------------
                Text("Topic *", fontWeight = FontWeight.SemiBold, color = onBg, fontSize = 15.sp)

                OutlinedTextField(
                    value = topic,
                    onValueChange = { viewModel.topic.value = it },
                    placeholder = { Text("e.g. World History") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = outline,
                        cursorColor = PrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                // ---------------- SUBTOPICS ----------------
                Text("Subtopics (Optional)", fontWeight = FontWeight.SemiBold, color = onBg)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = subtopicInput,
                        onValueChange = { viewModel.subtopicInput.value = it },
                        placeholder = { Text("e.g. Ancient Rome") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = outline,
                            cursorColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    )

                    IconButton(
                        onClick = { viewModel.addSubtopic() },
                        enabled = !isLoading && subtopicInput.isNotBlank() && subtopics.size < 5,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = PrimaryColor,
                            disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }

                // chips
                if (subtopics.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subtopics.forEach { sub ->
                            ChipItem(sub, enabled = !isLoading) { viewModel.removeSubtopic(sub) }
                        }
                    }
                }

                // ---------------- EXTRA INSTRUCTIONS ----------------
                Text("Additional Instructions (Optional)", fontWeight = FontWeight.SemiBold, color = onBg)

                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = {
                        if (it.length <= viewModel.maxPromptChars)
                            viewModel.customPrompt.value = it
                    },
                    placeholder = { Text("e.g. Focus on key dates and events") },
                    maxLines = 4,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = outline,
                        cursorColor = PrimaryColor
                    ),
                    enabled = !isLoading
                )

                Text(
                    "${customPrompt.length}/${viewModel.maxPromptChars}",
                    modifier = Modifier.align(Alignment.End),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // ---------------- DROPDOWNS - FIXED LAYOUT ----------------
                Text("Preferences (Optional)", fontWeight = FontWeight.SemiBold, color = onBg)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(
                        label = "Qualification Level",
                        value = selectedQualification,
                        options = viewModel.qualificationOptions,
                        onSelect = { viewModel.selectedQualification.value = it },
                        enabled = !isLoading
                    )

                    DropdownField(
                        label = "Country/Region",
                        value = selectedCountry,
                        options = viewModel.countryOptions,
                        onSelect = { viewModel.selectedCountry.value = it },
                        enabled = !isLoading
                    )
                }

                // ---------------- DIFFICULTY ----------------
                Text("Difficulty Level", fontWeight = FontWeight.SemiBold, color = onBg)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.difficultyLevels.forEach { level ->
                        FilterChip(
                            selected = (selectedDifficulty == level),
                            onClick = { viewModel.selectedDifficulty.value = level },
                            label = { Text(level) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryColor,
                                selectedLabelColor = Color.White
                            ),
                            enabled = !isLoading
                        )
                    }
                }

                // ---------------- SLIDER ----------------
                Text("Number of Questions", fontWeight = FontWeight.SemiBold, color = onBg)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = mcqCount.toFloat(),
                        onValueChange = { viewModel.mcqCount.value = it.toInt() },
                        valueRange = 5f..20f,
                        steps = 14,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryColor,
                            activeTrackColor = PrimaryColor
                        ),
                        enabled = !isLoading
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "$mcqCount",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 18.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // ---------------- ERROR MESSAGE ----------------
                if (!errorMessage.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // ---------------- GENERATE BUTTON ----------------
                Button(
                    onClick = { viewModel.generateQuiz() },
                    enabled = viewModel.isFormValid() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Generate Quiz",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Add some bottom spacing
                Spacer(Modifier.height(20.dp))
            }
        }

        // ---------------- FULL SCREEN LOADING OVERLAY ----------------
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .wrapContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            color = PrimaryColor,
                            strokeWidth = 4.dp
                        )

                        Text(
                            loadingMessage,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onBg,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            "Please wait...",
                            fontSize = 14.sp,
                            color = onBg.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChipItem(text: String, enabled: Boolean = true, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
                enabled = enabled
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String?,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value ?: "Select $label",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                enabled = enabled
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        text = { Text(option) }
                    )
                }
            }
        }
    }
}