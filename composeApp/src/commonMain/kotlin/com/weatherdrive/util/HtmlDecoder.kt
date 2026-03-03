package com.weatherdrive.util

private val HTML_ENTITY_MAP = mapOf(
    "&amp;" to "&",
    "&lt;" to "<",
    "&gt;" to ">",
    "&quot;" to "\"",
    "&apos;" to "'",
    "&nbsp;" to "\u00A0",
    "&ndash;" to "\u2013",
    "&mdash;" to "\u2014",
    "&lsquo;" to "\u2018",
    "&rsquo;" to "\u2019",
    "&ldquo;" to "\u201C",
    "&rdquo;" to "\u201D",
    "&hellip;" to "\u2026",
    "&#8211;" to "\u2013",
    "&#8212;" to "\u2014",
    "&#160;" to "\u00A0",
    "&#x2013;" to "\u2013",
    "&#x2014;" to "\u2014",
    "–" to "\u2013",
    "—" to "\u2014",
)

/**
 * Decodes common HTML entities and converts `<br>` tags to newlines,
 * then strips any remaining HTML tags. Suitable for use in commonMain
 * Compose Multiplatform code without platform-specific APIs.
 */
fun String.decodeHtml(): String {
    var result = this
    // Replace <br> variants with newlines
    result = result.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    // Strip remaining HTML tags
    result = result.replace(Regex("<[^>]+>"), "")
    // Decode named and numeric entities
    for ((entity, replacement) in HTML_ENTITY_MAP) {
        result = result.replace(entity, replacement)
    }
    // Decode decimal numeric entities: &#NNN;
    result = result.replace(Regex("&#(\\d+);")) { match ->
        val codePoint = match.groupValues[1].toIntOrNull()
        if (codePoint != null && codePoint in 0..0xFFFF) codePoint.toChar().toString() else match.value
    }
    // Decode hex numeric entities: &#xNNN;
    result = result.replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
        val codePoint = match.groupValues[1].toIntOrNull(16)
        if (codePoint != null && codePoint in 0..0xFFFF) codePoint.toChar().toString() else match.value
    }
    return result.trim()
}
