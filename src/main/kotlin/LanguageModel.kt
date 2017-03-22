package ru.yole.hilangro

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

enum class PartOfSpeech {
    None, Verb, Noun, Pronoun, Honorific, Order, Hortative
}

val posNames = mapOf(
    "Hort" to PartOfSpeech.Hortative,
    "Hon" to PartOfSpeech.Honorific,
    "Ord" to PartOfSpeech.Order
)

data class Concept(val name: String,
                   val partOfSpeech: PartOfSpeech,
                   val wordClass: Int,
                   val tags: List<String>,
                   val phonotactics: String?,
                   val appearsIn: String?,
                   val exclusiveTo: String?,
                   val translation: String?) {
    companion object {
        fun readListFromJson(jsonNode: JsonNode): List<Concept> = jsonNode.map(this::readFromJson)

        private fun readFromJson(jsonNode: JsonNode): Concept {
            fun attr(name: String): String? = jsonNode[name]?.asText()

            val name = jsonNode["name"].asText()
            val posName = jsonNode["POS"].asText("None")
            val pos = posNames[posName] ?: PartOfSpeech.valueOf(posName)
            val wordClass = jsonNode["class"]?.asInt() ?: 0
            val tags = jsonNode["tags"]?.map { it.asText() } ?: emptyList()
            return Concept(name, pos, wordClass, tags,
                    attr("phonotactics"),
                    attr("appearsIn"),
                    attr("exclusiveTo"),
                    attr("translation"))
        }
    }
}

var sourceOfRandomness = { -> Math.random() }

private fun <T> List<T>.randomElement() = this[(sourceOfRandomness() * size).toInt()]

class PhonemeSet(val choices: List<String>) {
    fun generate() = choices.randomElement()

    companion object {
        fun readFromJson(phonemeSetNode: JsonNode): PhonemeSet {
            if (phonemeSetNode.isArray) {
                return PhonemeSet(phonemeSetNode.map { it.asText() })
            }
            val choices = phonemeSetNode["choices"].map { it.asText() }
            val sticky = phonemeSetNode["sticky"].asBoolean()
            if (sticky) {
                val choice = choices.randomElement()
                return PhonemeSet(listOf(choice))
            }
            return PhonemeSet(choices)
        }

    }
}

class PhonemeTable(val phonemes: Map<Char, PhonemeSet>) {
    companion object {
        fun readFromJson(jsonNode: JsonNode): PhonemeTable {
            val phonemes = mutableMapOf<Char, PhonemeSet>()
            for ((key, phonemeSet) in jsonNode.fields()) {
                phonemes[key.first()] = PhonemeSet.readFromJson(phonemeSet)
            }
            return PhonemeTable(phonemes)
        }
    }

    fun generate(c: Char) = phonemes[c]!!.generate()
}

data class WordType(val phonemes: String, val weight: Int) {
    companion object {
        fun readFromJson(jsonNode: JsonNode): WordType {
            return WordType(jsonNode["phonemes"].asText(),
                    jsonNode["weight"].asInt())
        }
    }
}

class Phonotactics(val phonemeTable: PhonemeTable,
                   wordTypes: List<WordType>) {

    val weighedWordTypes = repeatAccordingToWeight(wordTypes)

    companion object {
        fun readFromJson(phonemeTable: PhonemeTable, value: JsonNode): Phonotactics {
            val wordTypes = value.map { WordType.readFromJson(it) }
            return Phonotactics(phonemeTable, wordTypes)
        }

        private fun repeatAccordingToWeight(wordTypes: List<WordType>): List<WordType> {
            val weighedWordTypes = mutableListOf<WordType>()
            for (wordType in wordTypes) {
                for (i in 1..wordType.weight) {
                    weighedWordTypes.add(wordType)
                }
            }
            return weighedWordTypes
        }
    }

    fun generateWord(): String {
        val wordType = weighedWordTypes.randomElement()
        return wordType.phonemes.map { c ->
            if (c.isUpperCase())
                phonemeTable.generate(c)
            else
                c
        }.joinToString(separator = "")
    }
}

data class Language(val name: String,
                    val phonemeTable: PhonemeTable,
                    val phonotactics: Map<String, Phonotactics>) {
    companion object {
        fun readListFromJson(jsonNode: JsonNode): List<Language> = jsonNode.map(this::readFromJson)

        private fun readFromJson(jsonNode: JsonNode): Language {
            val name = jsonNode["name"].asText()
            val phonemeTable = PhonemeTable.readFromJson(jsonNode["phonemes"])
            val phonotactics = mutableMapOf<String, Phonotactics>()
            for ((name, value) in jsonNode["phonotactics"].fields()) {
                phonotactics[name] = Phonotactics.readFromJson(phonemeTable, value)
            }
            return Language(
                name,
                phonemeTable,
                phonotactics
            )
        }
    }
}

class LanguageModel(val concepts: List<Concept>,
                    val languages: List<Language>) {
    companion object {
        fun fromJson(filename: String): LanguageModel {
            val objectMapper = ObjectMapper()
            val root = objectMapper.readTree(File(filename))
            val concepts = Concept.readListFromJson(root["concepts"]!!)
            val languages = Language.readListFromJson(root["languages"]!!)
            return LanguageModel(concepts, languages)
        }
    }
}