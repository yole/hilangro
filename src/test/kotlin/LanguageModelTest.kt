package ru.yole.hilangro

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageModelTest {
    @Test fun testPhonemeSetGenerate() {
        val phonemeSet = PhonemeSet(listOf("o"))
        assertEquals("o", phonemeSet.generate())
    }

    @Test fun testPhonemeTableGenerate() {
        val phonemeSet = PhonemeSet(listOf("p"))
        val phonemeTable = PhonemeTable(mapOf('P' to phonemeSet))
        assertEquals("p", phonemeTable.generate('P'))
    }

    @Test fun testGenerateWord() {
        val phonemeSet = PhonemeSet(listOf("p"))
        val phonemeTable = PhonemeTable(mapOf('P' to phonemeSet))
        val wordType = WordType("Pi", 1)
        val pt = Phonotactics(phonemeTable, listOf(wordType))
        assertEquals("pi", pt.generateWord())
    }

    @Test fun testGenerateWordWeights() {
        val phonemeSet = PhonemeSet(listOf("p"))
        val phonemeTable = PhonemeTable(mapOf('P' to phonemeSet))
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
