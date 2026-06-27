package com.truthlens.app.factcheck

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight on-device heuristic scorer.
 * Returns a 0–100 risk score based on text patterns.
 * Higher = more likely to need verification.
 */
@Singleton
class LocalRiskScorer @Inject constructor() {

    data class ScoreResult(
        val score: Int,
        val reasons: List<String>
    )

    fun score(text: String): ScoreResult {
        val lower = text.lowercase()
        val reasons = mutableListOf<String>()
        var score = 0

        // Sensational language indicators
        val sensationalPhrases = listOf(
            "breaking:" to 10,
            "shocking" to 8,
            "you won't believe" to 15,
            "mainstream media won't tell you" to 25,
            "they don't want you to know" to 25,
            "bombshell" to 12,
            "going viral" to 5,
            "share before they delete" to 30,
            "urgent" to 5
        )
        sensationalPhrases.forEach { (phrase, points) ->
            if (lower.contains(phrase)) {
                score += points
                reasons.add("Contains sensational language: \"$phrase\"")
            }
        }

        // Missing source indicators
        val missingSourcePhrases = listOf(
            "studies show" to 10,
            "scientists say" to 8,
            "experts claim" to 8,
            "sources say" to 10,
            "according to insiders" to 15,
            "many people are saying" to 12
        )
        missingSourcePhrases.forEach { (phrase, points) ->
            if (lower.contains(phrase)) {
                score += points
                reasons.add("Unattributed claim: \"$phrase\"")
            }
        }

        // Known misinformation patterns
        val misinfoPatterns = listOf(
            "5g causes" to 30,
            "vaccines cause" to 25,
            "chemtrails" to 20,
            "new world order" to 20,
            "deep state" to 15,
            "plandemic" to 30,
            "great reset" to 15
        )
        misinfoPatterns.forEach { (phrase, points) ->
            if (lower.contains(phrase)) {
                score += points
                reasons.add("Matches known misinformation topic: \"$phrase\"")
            }
        }

        // All-caps words (screaming headlines)
        val allCapsWords = text.split(" ").count { it.length > 3 && it == it.uppercase() && it.all { c -> c.isLetter() } }
        if (allCapsWords >= 3) {
            score += 10
            reasons.add("Excessive use of ALL-CAPS words")
        }

        // Excessive exclamation marks
        val exclamationCount = text.count { it == '!' }
        if (exclamationCount >= 3) {
            score += 8
            reasons.add("Excessive exclamation marks")
        }

        // Very short text with extreme claims
        if (text.length < 50 && (lower.contains("fake") || lower.contains("hoax") || lower.contains("lie"))) {
            score += 10
            reasons.add("Short unsubstantiated claim")
        }

        return ScoreResult(score = score.coerceIn(0, 100), reasons = reasons)
    }
}
