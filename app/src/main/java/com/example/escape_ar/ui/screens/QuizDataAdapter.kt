package com.example.escape_ar.ui.screens

import com.example.escape_ar.data.model.QuizQuestion as DbQuizQuestion

/**
 * UI model for quiz questions used in the screens
 * Simpler structure than database model
 */
data class UiQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // Index: 0, 1, 2, or 3
    val explanation: String = ""
)

/**
 * Convert database QuizQuestion to UI QuizQuestion
 */
fun DbQuizQuestion.toUiModel(): UiQuizQuestion {
    return UiQuizQuestion(
        id = this.id,
        question = this.questionText,
        options = this.getOptions(),
        correctAnswer = this.getCorrectAnswerIndex(),
        explanation = "" // Can add explanation field to database later
    )
}

/**
 * Convert list of database questions to UI questions
 */
fun List<DbQuizQuestion>.toUiModels(): List<UiQuizQuestion> {
    return this.map { it.toUiModel() }
}
