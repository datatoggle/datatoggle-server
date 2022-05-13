package com.datatoggle.server.tools
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.Normalizer

// 12 exa = 48 bits; proba collision is 1e-11 with 100 entries. cf. "birthday paradoxe"
private const val NB_HEXA_CHAR_DISAMBIGUATION = 12

// too long uri are bad for SEO
private const val MAX_URI_SIZE = 80

fun generateUri(strToClean: String, strToHashToEnsureUniqueness: String = ""): String {
    return cleanString(strToClean) + "-" + hashString(strToClean + strToHashToEnsureUniqueness).substring(0, NB_HEXA_CHAR_DISAMBIGUATION)
}

private fun cleanString(strToClean: String): String {

    return normalizeString(strToClean) // replace accentuated chars by equivalent
        .toLowerCase()
        .replace(Regex("[ ']+"), "-") // replace separators by "-"
        .replace(Regex("[^a-z0-9-]"), "") // remove all non standards characters
        .replace(Regex("[-]+"), "-") // remove duplicated "-"
        .trim('-') // remove trailing "-"
        .take(MAX_URI_SIZE)
}

// https://www.rgagnon.com/javadetails/java-0456.html
private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
private fun normalizeString(value: String): String {
    val temp = Normalizer.normalize(value, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

private fun hashString(strToHashToEnsureUniqueness: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(strToHashToEnsureUniqueness.byteInputStream().readAllBytes())
    return bytesToHex(hashBytes)
}

// https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
private val HEX_ARRAY: ByteArray = "0123456789ABCDEF".byteInputStream(StandardCharsets.US_ASCII).readAllBytes()
private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = ByteArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v: Int = 0xFF and bytes[j].toInt()
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars, StandardCharsets.UTF_8)
}
