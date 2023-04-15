package ru.tinkoff.asdk.preprocessor

import java.security.MessageDigest


fun Map<String, String>.modifyBodyIfNeed(
    path: String,
    ignoredFields: Set<String>,
    ignoredPaths: Set<String>,
    password: String,
    terminalKey: String
): Map<String, String> {
    val needIgnore = ignoredPaths.any { path.contains(it) }
    if (needIgnore) return this
    return modifyBody(ignoredFields, password, terminalKey)
}

fun Map<String, String>.modifyBody(
    ignoredFields: Set<String>,
    password: String,
    terminalKey: String
): Map<String, String> {
    val params = (this + mapOf(Password to password)) changeTerminalKey(terminalKey)
    val token = params.toSortedMap().ignoreFields(ignoredFields).getToken()
    return params + mapOf(Token to token)
}

fun Map<String, String>.ignoreFields(ignoredFields: Set<String>): Map<String, String> {
    return filterNot { entry -> ignoredFields.any { it.contains(entry.key) } }
}

fun Map<String, String>.getToken(): String {
    val token = buildString {
        values.forEach { append(it) }
    }

    return MessageDigest.getInstance(ALGO)
        .digest(token.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

infix fun Map<String, String>.changeTerminalKey(terminalKey: String): Map<String, String> {
    val tk = get(TerminalKey)
    return if (tk.equals(terminalKey, ignoreCase = true)) {
        this
    } else {
        val map = this.toMutableMap()
        map[TerminalKey] = terminalKey
        return map
    }
}

private const val TerminalKey = "TerminalKey"
private const val ALGO = "SHA-256"
private const val Token = "Token"
private const val Password = "Password"