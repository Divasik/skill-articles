package ru.skillbranch.skillarticles.extensions

import ru.skillbranch.skillarticles.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

const val ONE = "one"
const val FEW = "few"
const val MANY = "many"

val UNIT_WORDS_MAP = mapOf(
        TimeUnits.SECOND to mapOf(ONE to "секунду", FEW to "секунды", MANY to "секунд"),
        TimeUnits.MINUTE to mapOf(ONE to "минуту", FEW to "минуты", MANY to "минут"),
        TimeUnits.HOUR to mapOf(ONE to "час", FEW to "часа", MANY to "часов"),
        TimeUnits.DAY to mapOf(ONE to "день", FEW to "дня", MANY to "дней")
)

enum class TimeUnits {
    SECOND,
    MINUTE,
    HOUR,
    DAY;

    fun plural(value: Int): String? {
        val unitWords = UNIT_WORDS_MAP.get(this)
        val last = value.toString().last().toString().toInt()

        return "$value " + when {
            value == 1 || value > 20 && last == 1 -> unitWords?.get(ONE)
            value in 2..4 || value > 20 && last in 2..4 -> unitWords?.get(FEW)
            else -> unitWords?.get(MANY)
        }
    }
}

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val fmt = SimpleDateFormat(pattern, Locale("ru"))
    return fmt.format(this)
}

fun Date.shortFormat(): String? {
    val pattern = if(this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    return this.format(pattern)
}

fun Date.isSameDay(date: Date): Boolean {
    val d1 = this.time / DAY
    val d2 = date.time / DAY
    return d1 == d2
}

fun Date.add(value: Int, unit: TimeUnits): Date {
    var time = this.time

    time += when(unit) {
        TimeUnits.SECOND -> value * SECOND
        TimeUnits.MINUTE -> value * MINUTE
        TimeUnits.HOUR -> value * HOUR
        TimeUnits.DAY -> value * DAY
    }

    this.time = time
    return this
}

fun Date.humanizeDiff(date: Date = Date()): String {
    var diff = this.time - date.time // milliseconds
    var isFuture = diff > 0
    var diffTpl = if(isFuture) "через %s" else "%s назад"

    if(diff < 0) {
        diff = -diff
    }

    return when(diff) {
        in 0 .. 1 * SECOND -> "только что"
        in 1 * SECOND .. 45 * SECOND -> String.format(diffTpl, "несколько секунд")
        in 45 * SECOND .. 75 * SECOND -> String.format(diffTpl, "минуту")
        in 75 * SECOND .. 45 * MINUTE -> {
            val minutes = diff / MINUTE
            String.format(diffTpl, "$minutes ${Utils.getPluralMinutes(minutes)}")
        }
        in 45 * MINUTE .. 75 * MINUTE -> String.format(diffTpl, "час")
        in 75 * MINUTE .. 22 * HOUR -> {
            val hours = diff / HOUR
            String.format(diffTpl, "$hours ${Utils.getPluralHours(hours)}")
        }
        in 22 * HOUR .. 26 * HOUR -> String.format(diffTpl, "день")
        in 26 * HOUR .. 360 * DAY -> {
            val days = diff / DAY
            String.format(diffTpl, "$days ${Utils.getPluralDays(days)}")
        }
        else -> if(isFuture) "более чем через год" else "более года назад"
    }

}