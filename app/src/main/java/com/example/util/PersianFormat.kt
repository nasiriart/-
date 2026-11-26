package com.example.util

import java.text.NumberFormat
import java.util.Locale

object PersianFormat {
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else if (ch == ',') {
                sb.append('٬')
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun number(value: Int): String {
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(value)
        return toPersianDigits(formatted)
    }

    fun number(value: Long): String {
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(value)
        return toPersianDigits(formatted)
    }

    fun money(amount: Long): String = "${number(amount)} سکه"

    fun percent(value: Int): String = "${toPersianDigits(value.toString())}٪"

    fun duration(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val minutes = safe / 60
        val seconds = safe % 60
        return if (minutes > 0) {
            val minStr = minutes.toString().padStart(2, '0')
            val secStr = seconds.toString().padStart(2, '0')
            toPersianDigits("$minStr:$secStr")
        } else {
            "${toPersianDigits(seconds.toString())} ثانیه"
        }
    }
}
