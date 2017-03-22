package ru.yole.hilangro

fun main(args: Array<String>) {

    val model = LanguageModel.fromJson("src/main/resources/LanguageModel.json")
    for (language in model.languages) {
        println(language.name)
        println(language.transformRules)
    }
}
