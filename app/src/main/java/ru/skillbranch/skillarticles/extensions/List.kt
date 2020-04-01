package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
    val list = List<MutableList<Pair<Int,Int>>>(bounds.size) { mutableListOf() }
    this.forEach { current ->
        bounds.forEachIndexed { index, bound ->
            val range = bound.first..bound.second
            if(current.first >= bound.first && current.second <= bound.second) {
                list[index].add(current)
                return@forEach
            } else if(current.first in range) {
                list[index].add(Pair(current.first, bound.second))
            } else if(current.second in range) {
                list[index].add(Pair(bound.first, current.second))
            }
        }
    }
    return list
}