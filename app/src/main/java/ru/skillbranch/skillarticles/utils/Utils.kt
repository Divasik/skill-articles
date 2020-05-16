package ru.skillbranch.skillarticles.utils

import android.content.Context
import android.util.DisplayMetrics

object Utils {

    val translitMap = mapOf(
        "а" to "a","б" to "b","в" to "v","г" to "g","д" to "d","е" to "e","ё" to "e","ж" to "zh","з" to "z","и" to "i",
        "й" to "i","к" to "k","л" to "l","м" to "m","н" to "n","о" to "o","п" to "p","р" to "r","с" to "s","т" to "t",
        "у" to "u","ф" to "f","х" to "h","ц" to "c","ч" to "ch","ш" to "sh","щ" to "sh'","ъ" to "","ы" to "i","ь" to "",
        "э" to "e","ю" to "yu","я" to "ya"
    )

    fun parseFullName(fullName: String?): Pair<String?, String?> {
        val trimmed = fullName?.trim()

        return when(trimmed) {
            null, "" -> Pair(null, null)
            else -> {
                val parts: List<String>? = trimmed.split(" ")
                val firstName: String? = parts?.getOrNull(0)
                val lastName: String? = parts?.getOrNull(1)

                Pair(firstName, lastName)
            }
        }
    }

    fun toInitials(firstName: String?, lastName: String?): String? {
        val fnInitial = firstName?.trim()?.getOrNull(0)?.toUpperCase()
        val lnInitial = lastName?.trim()?.getOrNull(0)?.toUpperCase()

        return when {
            fnInitial == null && lnInitial == null -> null
            else -> (fnInitial?.toString() ?: "") + (lnInitial?.toString() ?: "")
        }
    }

    fun transliteration(payload: String, divider: String = " "): String {
        var sb = StringBuilder()
        for (char in payload) {
            if(char == ' ') {
                sb.append(divider)
                continue
            }
            sb.append(getTranslitChar(char))
        }
        return sb.toString()
    }

    fun getTranslitChar(char: Char): String {
        var res = translitMap.get(char.toLowerCase().toString())
        if(res != null && char.isUpperCase()) {
            res = if(res.length == 1) {
                res.toUpperCase()
            } else {
                res.substring(0,1).toUpperCase() + res.substring(1)
            }
        }
        return res?.toString() ?: char.toString()
    }

    fun getPluralMinutes(value: Long): String {
        val last = value.toString().last().toString().toInt()

        return when {
            value == 1L || value > 20 && last == 1 -> "минута"
            value in 2..4 || value > 20 && last in 2..4 -> "минуты"
            else -> "минут"
        }
    }

    fun getPluralHours(value: Long): String {
        val last = value.toString().last().toString().toInt()

        return when {
            value == 1L || value > 20 && last == 1 -> "час"
            value in 2..4 || value > 20 && last in 2..4 -> "часа"
            else -> "часов"
        }
    }

    fun getPluralDays(value: Long): String {
        val last = value.toString().last().toString().toInt()

        return when {
            value == 1L || value > 20 && last == 1 -> "день"
            value in 2..4 || value > 20 && last in 2..4 -> "дня"
            else -> "дней"
        }
    }

    fun isRepositoryValid(s:CharSequence?): Boolean {
        if(s == null || s.isEmpty()) {
            return true
        }

        val str = s.toString().toLowerCase()

        if(!str.contains("github.com")) {
            return false
        }

        // check prefix
        if(!str.startsWith("github.com")) {
            val prefix = str.substring(0, str.indexOf("github.com"))
            if(!setOf("https://", "www.", "https://www.").contains(prefix)) {
                return false
            }
        }

        // check suffix
        var suffix = str.substring(s.indexOf("github.com") + "github.com".length)

        if(!Regex("^/[a-z0-9]+[-]?[a-z0-9]+/?$").containsMatchIn(suffix)) {
            return false
        }

        if(suffix.startsWith("/")) {
            suffix = suffix.substring(1)
        }

        if(suffix.endsWith("/")) {
            suffix = suffix.substring(0, suffix.length-1)
        }

        val exclNames = setOf(
            "enterprise", "features", "topics", "collections", "trending", "events", "marketplace",
            "pricing", "nonprofit", "customer-stories", "security", "login", "join")
        for(n in exclNames) {
            if(suffix.equals(n)) {
                return false
            }
        }

        return true
    }

    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.getResources().getDisplayMetrics()
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun pxToDp(px: Int, context: Context): Int {
        val displayMetrics = context.getResources().getDisplayMetrics()
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}