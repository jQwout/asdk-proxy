package ru.tinkoff.asdk.preprocessor

import io.ktor.http.*

fun String.appendPath(protocol: String, path: String): String {
    return URLBuilder(this).apply { set(path = path, scheme = protocol, host = this@appendPath) }.buildString()
}
