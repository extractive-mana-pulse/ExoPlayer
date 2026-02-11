package com.example.exoplayertr.core

object KeywordMatcher {

    private val greetingKeywords = listOf("hello", "hi")
    private val weatherKeywords = listOf("weather", "today")
    private val goodbyeKeywords = listOf("goodbye", "bye", "see you", "see you later")

    fun matchKeywords(text: String): ResponseType {
        val lowerText = text.lowercase()

        return when {
            goodbyeKeywords.any { lowerText.contains(it) } -> ResponseType.GOODBYE
            greetingKeywords.any { lowerText.contains(it) } -> ResponseType.GREETING
            weatherKeywords.any { lowerText.contains(it) } -> ResponseType.WEATHER
            else -> ResponseType.GENERAL_RESPONSE
        }
    }
}