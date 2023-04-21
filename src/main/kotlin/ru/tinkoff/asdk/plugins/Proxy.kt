package ru.tinkoff.asdk.plugins

import ru.tinkoff.asdk.ServerConfig
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.tinkoff.asdk.preprocessor.*
import ru.tinkoff.asdk.processor.sendRequest

fun Application.configureProxy(
    client: HttpClient,
    serverConfig: ServerConfig,
) {
    with(serverConfig) {
        routing { proxy(client, tinkoffHost, password, terminalKey, ignoredFields, ignoredPaths) }
    }
}

fun Route.proxy(
    client: HttpClient,
    targetUrl: String,
    password: String,
    terminalKey: String,
    ignoredFields: Set<String>,
    ignoredPaths: Set<String>
) {
    route("v2/GetQr") {
        handle(client, targetUrl, password, terminalKey, ignoredFields, ignoredPaths) { baseParams, response ->
            response.modifyGetQrBody(baseParams)
        }
    }
    route("{...}") {
        handle(client, targetUrl, password, terminalKey, ignoredFields, ignoredPaths)
    }
}

private fun Route.handle(
    client: HttpClient,
    targetUrl: String,
    password: String,
    terminalKey: String,
    ignoredFields: Set<String>,
    ignoredPaths: Set<String>,
    modifyResponseBody: suspend (Map<String, String>, HttpResponse) -> ByteArray = { baseParams, response ->
        response.readBytes()
    }
) {
    handle {
        val baseParams = call.parseParams()
        val response = client.sendRequest(
            bodyAsMap = baseParams,
            httpMethod = call.request.httpMethod,
            contentType = call.request.contentType(),
            targetUrl = targetUrl,
            protocol = "https",
            path = call.request.uri,
            password = password,
            terminalKey = terminalKey,
            ignoredFields = ignoredFields,
            ignoredPaths = ignoredPaths
        )
        call.respondBytes(
            bytes = modifyResponseBody(baseParams, response),
            contentType = response.contentType(),
            status = response.status,
        )
    }
}
