package ru.yole.hilangro

import com.fasterxml.jackson.databind.JsonNode

data class TransformRule(val phonemeTable: PhonemeTable,
                         val from: String,
                         val to: List<String>,
                         val wordStart: Boolean = false,
                         val wordEnd: Boolean = false,
                         val wordClass: Int? = null,
                         val partsOfSpeech: Set<PartOfSpeech>? = null,
                         val before: String? = null,
                         val after: String? = null) {

    private class MatchResult(val length: Int, val matchedGroups: Map<Char, String>)

    fun apply(concept: Concept, source: String): String {
        return buildString {
            var sourceIndex = 0
            while (sourceIndex < source.length) {
                val matchResult = matchesAtOffset(source, sourceIndex)
                if (matchResult != null) {
                    appendReplacement(matchResult)
                    sourceIndex += matchResult.length
                }
                else {
                    append(source[sourceIndex++])
                }
            }
        }
    }

    private fun matchesAtOffset(source: String, sourceStartIndex: Int): MatchResult? {
        val matchedGroups = mutableMapOf<Char, String>()
        var sourceIndex = sourceStartIndex
        for (fromChar in from) {
            if (fromChar == '*') {
                matchedGroups[fromChar] = source.substring(sourceIndex, source.length)
                return MatchResult(source.length - sourceStartIndex, matchedGroups)
            }
            else if (Character.isLowerCase(fromChar)) {
                if (source[sourceIndex] != fromChar) return null
                sourceIndex++
            }
            else {
                val phonemeSet = phonemeTable.phonemes[fromChar]!!
                var matched = false
                for (choice in phonemeSet.choices) {
                    if (source.startsWith(choice, sourceIndex)) {
                        sourceIndex += choice.length
                        matched = true
                        break
                    }
                }
                if (!matched) return null
            }
        }
        return MatchResult(sourceIndex - sourceStartIndex, matchedGroups)
    }

    private fun StringBuilder.appendReplacement(matchResult: MatchResult) {
        val selectedReplacement = to.randomElement()
        for (c in selectedReplacement) {
            if (c == '*') {
                append(matchResult.matchedGroups['*'])
            }
            else if (Character.isLowerCase(c)) {
                append(c)
            }
            else {
                append(phonemeTable.generate(c))
            }
        }
    }

    companion object {
        fun readFromJson(phonemeTable: PhonemeTable, jsonNode: JsonNode): TransformRule {
            val from = jsonNode["from"].asText()
            val toNode = jsonNode["to"]
            val to = if (toNode.isArray) toNode.map { it.asText() } else listOf(toNode.asText())
            return TransformRule(phonemeTable, from, to,
                    jsonNode["wordStart"]?.asBoolean() ?: false,
                    jsonNode["wordEnd"]?.asBoolean() ?: false,
                    jsonNode["class"]?.asInt(),
                    jsonNode["POS"]?.let { PartOfSpeech.fromJsonAsSet(it.asText()) },
                    jsonNode["before"]?.asText(),
                    jsonNode["after"]?.asText())
        }
    }
}

