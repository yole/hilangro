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
}
