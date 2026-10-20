package com.aggreykibe.dayflow.domain

import kotlin.math.pow

/** WCAG 2.x contrast ratio between two 0xAARRGGBB colors: 1.0 (identical) to 21.0 (black on white). */
fun contrastRatio(a: Long, b: Long): Double {
    val la = luminance(a)
    val lb = luminance(b)
    val lighter = maxOf(la, lb)
    val darker = minOf(la, lb)
    return (lighter + 0.05) / (darker + 0.05)
}

private fun luminance(argb: Long): Double {
    fun channel(shift: Int): Double {
        val c = ((argb shr shift) and 0xFF).toDouble() / 255.0
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}
