package com.truthlens.app

import com.google.common.truth.Truth.assertThat
import com.truthlens.app.factcheck.LocalRiskScorer
import org.junit.Before
import org.junit.Test

class LocalRiskScorerTest {

    private lateinit var scorer: LocalRiskScorer

    @Before
    fun setUp() {
        scorer = LocalRiskScorer()
    }

    @Test
    fun `benign text scores low risk`() {
        val result = scorer.score("The weather today is sunny with a high of 24 degrees Celsius.")
        assertThat(result.score).isLessThan(20)
        assertThat(result.reasons).isEmpty()
    }

    @Test
    fun `sensational language increases score`() {
        val result = scorer.score("SHOCKING: You won't believe what scientists discovered!")
        assertThat(result.score).isAtLeast(20)
        assertThat(result.reasons).isNotEmpty()
    }

    @Test
    fun `known misinformation topics score high`() {
        val result = scorer.score("5G causes cancer — scientists say the government is hiding the truth.")
        assertThat(result.score).isAtLeast(40)
    }

    @Test
    fun `unattributed claims are flagged`() {
        val result = scorer.score("Studies show that eating apples cures all diseases, sources say.")
        assertThat(result.reasons.any { it.contains("Unattributed") }).isTrue()
    }

    @Test
    fun `excessive exclamation marks are flagged`() {
        val result = scorer.score("This is amazing!!! Share now!!! You need to know this!!!")
        assertThat(result.reasons.any { it.contains("exclamation") }).isTrue()
    }

    @Test
    fun `score is always clamped to 0-100`() {
        val extremeText = "BREAKING: SHOCKING!!! 5G causes plandemic they don't want you to know share before they delete" +
            " mainstream media won't tell you new world order deep state chemtrails studies show sources say experts claim!!!"
        val result = scorer.score(extremeText)
        assertThat(result.score).isAtMost(100)
        assertThat(result.score).isAtLeast(0)
    }
}
