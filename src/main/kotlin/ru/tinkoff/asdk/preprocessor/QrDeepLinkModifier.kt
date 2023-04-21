package ru.tinkoff.asdk.preprocessor

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.net.URI


suspend fun HttpResponse.modifyGetQrBody(baseParams: Map<String, String>): ByteArray {
    val schema = baseParams.get("BankScheme")
    val json = Json { ignoreUnknownKeys = true } // создаем экземпляр JSON с возможностью игнорировать неизвестные поля
    val map: Map<String, String> = body()
    if (schema == null) return this.readBytes()
    val newMap = map.modifyGetQrResponse(schema)
    val byteOutStream = ByteArrayOutputStream()
    json.encodeToStream(newMap, byteOutStream)
    return byteOutStream.toByteArray()
}

fun Map<String, String>.modifyGetQrResponse(bankSchema: String): Map<String, String> {
    return toMutableMap().apply {
        compute(DEEPLINK_KEY) { _, value -> value?.replaceSchema(bankSchema) }
    }
}

private fun String.replaceSchema(bankSchema: String): String {
    val base = URI.create(this)
    return URI(bankSchema, base.host, base.path, base.fragment).toString()
}


private const val DEEPLINK_KEY = "Data"