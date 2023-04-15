package ru.tinkoff.asdk.preprocessor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

suspend fun ApplicationCall.parseParams(): Map<String, String> {
    return when(request.contentType()) {
        ContentType.Application.Json -> {
            parseApplicationJson()
        }
        ContentType.Application.FormUrlEncoded -> {
            parseUrlEncoded()
        }
        else -> {
            emptyMap()
        }
    }
}

suspend fun ApplicationCall.parseApplicationJson(): Map<String, String> {
    val body = receiveText() ?: return emptyMap() // считываем тело в строку
    val json = Json { ignoreUnknownKeys = true } // создаем экземпляр JSON с возможностью игнорировать неизвестные поля
    return json.decodeFromString(body)// преобразуем строку в Map
}

suspend fun ApplicationCall.parseUrlEncoded(): Map<String, String> {
    val body = receiveText() ?: return emptyMap() // считываем тело в строку
    val params = body.split("&") // разбиваем строку на параметры
    val map = mutableMapOf<String, String>()
    for (param in params) {
        val parts = param.split("=") // разбиваем параметры на ключ и значение
        if (parts.size == 2) {
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()) // декодируем ключ
            val value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()) // декодируем значение
            map[key] = value // добавляем пару ключ-значение в map
        }
    }
    return map
}