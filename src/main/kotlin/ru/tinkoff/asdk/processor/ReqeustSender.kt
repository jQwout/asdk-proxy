package ru.tinkoff.asdk.processor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ru.tinkoff.asdk.preprocessor.appendPath
import ru.tinkoff.asdk.preprocessor.modifyBodyIfNeed

suspend fun HttpClient.sendRequest(
    bodyAsMap: Map<String, String>,
    httpMethod: HttpMethod,
    contentType: ContentType,
    targetUrl: String,
    protocol: String,
    path: String,
    password: String,
    terminalKey: String,
    ignoredFields: Set<String>,
    ignoredPaths: Set<String>,
    modifyRequestBody: suspend (Map<String, String>) -> Map<String, String> = { map ->
        map.modifyBodyIfNeed(
            path = path,
            ignoredFields = ignoredFields,
            ignoredPaths = ignoredPaths,
            password = password,
            terminalKey = terminalKey
        )
    },
): HttpResponse {
    return request(targetUrl.appendPath(protocol, path)) {
        method = HttpMethod(httpMethod.value)
        headers.set(HttpHeaders.Host, targetUrl)
        if (bodyAsMap.isEmpty().not()) {
            contentType(contentType)
            setBody(modifyRequestBody(bodyAsMap))
        }
    }
}