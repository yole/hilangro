package ru.yole.hilangro

fun main(args: Array<String>) {

    val model = LanguageModel.fromJson("src/main/resources/LanguageModel.json")
    println(model.languages.joinToString("\n"))
}
