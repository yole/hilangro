package ru.yole.hilangro

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LanguageModelTest {
    lateinit var phonemeTable: PhonemeTable
    lateinit var concept: Concept

    @Before fun setUp() {
        phonemeTable = createDefaultPhonemeTable()
        concept = Concept("See", PartOfSpeech.Verb, 1)
    }

    @Test fun testPhonemeSetGenerate() {
        val phonemeSet = PhonemeSet(listOf("o"))
        assertEquals("o", phonemeSet.generate())
    }

    @Test fun testPhonemeTableGenerate() {
        assertEquals("p", phonemeTable.generate('P'))
    }

    @Test fun testGenerateWord() {
        val wordType = WordType("Pi", 1)
        val pt = Phonotactics(phonemeTable, listOf(wordType))
        assertEquals("pi", pt.generateWord())
    }

    @Test fun testGenerateWordWeights() {
        val wordType = WordType("Pi", 8)
        val wordType2 = WordType("Po", 2)
        val pt = Phonotactics(phonemeTable, listOf(wordType, wordType2))
        withRandomValue(0.6) {
            assertEquals("pi", pt.generateWord())
        }
        withRandomValue(0.9) {
            assertEquals("po", pt.generateWord())
        }
    }

    private fun createDefaultPhonemeTable(): PhonemeTable {
        return PhonemeTable(
                mapOf(
                        'P' to PhonemeSet(listOf("p")),
                        'H' to PhonemeSet(listOf("wh"))
                )
        )
    }

    @Test fun applySimpleRule() {
        val transformRule = TransformRule(phonemeTable, "w", listOf("hw"))
        val applied = transformRule.apply(concept, "war")
        assertEquals("hwar", applied)
    }

    @Test fun applyPhonemeRule() {
        val transformRule = TransformRule(phonemeTable, "P", listOf("o"))
        val applied = transformRule.apply(concept, "happy")
        assertEquals("haooy", applied)
    }

    @Test fun applyMultiCharacterRule() {
        val transformRule = TransformRule(phonemeTable, "ha", listOf("tri"))
        assertEquals("trippy", transformRule.apply(concept, "happy"))
        assertEquals("hippy", transformRule.apply(concept, "hippy"))
    }

    @Test fun applyMultiCharacterPhonemeRule() {
        val transformRule = TransformRule(phonemeTable, "H", listOf("th"))
        assertEquals("there", transformRule.apply(concept, "where"))
    }

    @Test fun `apply multi character replacement rule`() {
        val transformRule = TransformRule(phonemeTable, "h", listOf("H"))
        assertEquals("where", transformRule.apply(concept, "here"))
    }

    @Test fun matchAsterisk() {
        val transformRule = TransformRule(phonemeTable, "*", listOf("*er"))
        assertEquals("kotliner", transformRule.apply(concept, "kotlin"))
    }

    @Test fun matchAsteriskAfterText() {
        val transformRule = TransformRule(phonemeTable, "kot*", listOf("k*"))
        assertEquals("klin", transformRule.apply(concept, "kotlin"))
    }
}

fun withRandomValue(value: Double, body: () -> Unit) {
    val oldSourceOfRandomness = sourceOfRandomness
    try {
        sourceOfRandomness = { value }
        body()
    } finally {
        sourceOfRandomness = oldSourceOfRandomness
    }
}
