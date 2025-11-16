package com.example.quizai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import com.google.gson.JsonSyntaxException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GenerateQuizViewModel : ViewModel() {

    // ---------------- USER INPUT FIELDS ----------------
    val topic = MutableStateFlow("")
    val subtopicInput = MutableStateFlow("")
    val subtopics = MutableStateFlow<List<String>>(emptyList())
    val customPrompt = MutableStateFlow("")
    val maxPromptChars = 200

    val selectedQualification = MutableStateFlow<String?>(null)
    val qualificationOptions = listOf(
        "Primary School", "High School", "College",
        "University", "Professional", "Expert"
    )

    val selectedCountry = MutableStateFlow<String?>(null)
    val countryOptions = listOf(
        "Pakistan", "India", "USA", "UK",
        "Saudi Arabia", "UAE", "Canada", "Australia", "Other"
    )

    val selectedDifficulty = MutableStateFlow("Medium")
    val difficultyLevels = listOf("Easy", "Medium", "Hard", "Mixed")

    val mcqCount = MutableStateFlow(10)

    // ---------------- UI STATE ----------------
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _generatedQuizJson = MutableStateFlow<String?>(null)
    val generatedQuizJson = _generatedQuizJson.asStateFlow()

    private val _parsedQuiz = MutableStateFlow<QuizResponse?>(null)
    val parsedQuiz = _parsedQuiz.asStateFlow()

    // Trigger navigation AFTER parsing JSON
    private val _navigateToQuiz = MutableStateFlow(false)
    val navigateToQuiz: StateFlow<Boolean> = _navigateToQuiz.asStateFlow()

    // Loading message state
    private val _loadingMessage = MutableStateFlow("Generating quiz...")
    val loadingMessage = _loadingMessage.asStateFlow()

    // ---------------- VALIDATION ----------------
    fun isFormValid(): Boolean = topic.value.trim().isNotEmpty()

    // ---------------- SUBTOPIC CONTROL ----------------
    fun addSubtopic() {
        val text = subtopicInput.value.trim()
        if (text.isNotEmpty() && !subtopics.value.contains(text) && subtopics.value.size < 5) {
            subtopics.value = subtopics.value + text
            subtopicInput.value = ""
        }
    }

    fun removeSubtopic(text: String) {
        subtopics.value = subtopics.value - text
    }

    // ---------------- PROMPT BUILDER ----------------
    fun buildQuizPrompt(): String {
        val sb = StringBuilder()

        sb.append("Generate a multiple-choice quiz.\n")
        sb.append("Topic: ${topic.value}\n")
        sb.append("Difficulty: ${selectedDifficulty.value}\n")
        sb.append("Number of Questions: ${mcqCount.value}\n")

        if (subtopics.value.isNotEmpty())
            sb.append("Subtopics: ${subtopics.value.joinToString()}\n")

        selectedQualification.value?.let { sb.append("Student Level: $it\n") }
        selectedCountry.value?.let { sb.append("Localize for: $it\n") }

        if (customPrompt.value.isNotBlank())
            sb.append("Extra instructions: ${customPrompt.value}\n")

        sb.append(
            """
            IMPORTANT:
            Return ONLY valid JSON. No text outside JSON.

            Format must be:

            {
              "questions": [
                {
                  "question": "What is 2+2?",
                  "options": ["One", "Two", "Three", "Four"],
                  "answer": "Four",
                  "explanation": "2+2 equals 4"
                }
              ]
            }

            CRITICAL RULES:
            - Exactly ${mcqCount.value} questions
            - Each question must have EXACTLY 4 options
            - Options should be PLAIN TEXT without any prefixes like "A)", "B)", "1.", "2."
            - The "answer" field must be the EXACT FULL TEXT of one of the options
            - Example: If options are ["Apple", "Banana", "Orange", "Grape"], answer must be "Apple" (not "A" or "1")
            - No markdown formatting, no code blocks, no extra text
            - Return ONLY the JSON object
            """.trimIndent()
        )

        return sb.toString()
    }

    // ---------------- JSON PARSER ----------------
    fun parseQuizJson() {
        try {
            val json = _generatedQuizJson.value ?: return

            // Clean JSON - remove markdown code blocks if present
            val cleanedJson = json
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsed = GsonInstance.gson.fromJson(cleanedJson, QuizResponse::class.java)

            // ✅ Validate and normalize the parsed quiz
            val normalizedQuestions = parsed.questions.map { question ->
                // Clean options: remove any "A)", "B)", "1.", "2." prefixes
                val cleanedOptions = question.options.map { option ->
                    option.trim()
                        .replace(Regex("^[A-D]\\)\\s*"), "") // Remove "A) ", "B) ", etc.
                        .replace(Regex("^[1-4]\\.\\s*"), "") // Remove "1. ", "2. ", etc.
                        .replace(Regex("^[A-D]\\s*[-:]\\s*"), "") // Remove "A - ", "A: ", etc.
                        .trim()
                }

                // Clean answer: remove prefixes and find matching option
                var cleanedAnswer = question.answer.trim()
                    .replace(Regex("^[A-D]\\)\\s*"), "")
                    .replace(Regex("^[1-4]\\.\\s*"), "")
                    .replace(Regex("^[A-D]\\s*[-:]\\s*"), "")
                    .trim()

                // If answer is just a letter, convert to full option text
                if (cleanedAnswer.length == 1 && cleanedAnswer.uppercase() in listOf("A", "B", "C", "D")) {
                    val index = when (cleanedAnswer.uppercase()) {
                        "A" -> 0
                        "B" -> 1
                        "C" -> 2
                        "D" -> 3
                        else -> 0
                    }
                    cleanedAnswer = cleanedOptions.getOrNull(index) ?: cleanedOptions[0]
                }

                // Ensure answer matches one of the options
                if (!cleanedOptions.contains(cleanedAnswer)) {
                    // Try to find best match
                    cleanedAnswer = cleanedOptions.firstOrNull {
                        it.contains(cleanedAnswer, ignoreCase = true) ||
                                cleanedAnswer.contains(it, ignoreCase = true)
                    } ?: cleanedOptions[0]
                }

                QuizQuestion(
                    question = question.question,
                    options = cleanedOptions,
                    answer = cleanedAnswer,
                    explanation = question.explanation
                )
            }

            _parsedQuiz.value = QuizResponse(questions = normalizedQuestions)

        } catch (e: JsonSyntaxException) {
            _errorMessage.value = "Invalid response format. Please try again."
            _parsedQuiz.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Failed to process quiz: ${e.message}"
            _parsedQuiz.value = null
        }
    }

    // ---------------- API CALL WITH RETRY LOGIC ----------------
    fun generateQuiz() {
        val apiKey = Constants.GEMINI_API_KEY
        val prompt = buildQuizPrompt()

        _isLoading.value = true
        _errorMessage.value = null
        _generatedQuizJson.value = null
        _parsedQuiz.value = null
        _navigateToQuiz.value = false
        _loadingMessage.value = "Generating quiz..."

        viewModelScope.launch {
            try {
                // Set timeout to 60 seconds
                withTimeout(60000L) {
                    _loadingMessage.value = "Connecting to AI..."

                    val request = GeminiQuizRequest(
                        contents = listOf(
                            QuizContent(
                                role = "user",
                                parts = listOf(QuizPart(prompt))
                            )
                        )
                    )

                    _loadingMessage.value = "Generating ${mcqCount.value} questions..."

                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstanceQuiz.api.generateQuiz(apiKey, request)
                    }

                    _loadingMessage.value = "Processing response..."

                    val rawText = response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text

                    if (rawText.isNullOrBlank()) {
                        _errorMessage.value = "Received empty response. Please try again."
                    } else {
                        _generatedQuizJson.value = rawText.trim()
                        parseQuizJson()

                        if (_parsedQuiz.value != null) {
                            _loadingMessage.value = "Quiz ready!"
                            _navigateToQuiz.value = true
                        } else {
                            _errorMessage.value = "Failed to parse quiz. Please try again."
                        }
                    }
                }

            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _errorMessage.value = "Request timed out. Please check your connection and try again."
            } catch (e: SocketTimeoutException) {
                _errorMessage.value = "Connection timeout. Please try again."
            } catch (e: UnknownHostException) {
                _errorMessage.value = "No internet connection. Please check your network."
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    429 -> _errorMessage.value = "Too many requests. Please wait a moment and try again."
                    403 -> _errorMessage.value = "API access denied. Please check your API key."
                    500, 502, 503 -> _errorMessage.value = "Server error. Please try again in a moment."
                    else -> _errorMessage.value = "Network error (${e.code()}). Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message ?: "Unknown error occurred"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Reset state after navigation
    fun clearAfterSuccess() {
        _navigateToQuiz.value = false
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
}