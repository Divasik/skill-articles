package ru.skillbranch.skillarticles.extensions


fun String.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> = when {
    substr.isBlank() -> listOf()
    else -> {
        var list = mutableListOf<Int>()
        val queryLen = substr.length
        var index = this.indexOf(substr, 0, ignoreCase)
        while (index >= 0) {
            list.add(index)
            index = this.indexOf(substr, index + queryLen, ignoreCase)
        }
        list
    }
}