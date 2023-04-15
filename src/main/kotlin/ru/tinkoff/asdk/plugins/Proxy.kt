package ru.tinkoff.asdk.plugins

import ru.tinkoff.asdk.ServerConfig
import ru.tinkoff.asdk.preprocessor.modifyBodyIfNeed
import ru.tinkoff.asdk.preprocessor.parseParams
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.util.*

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
    // Обработка всех запросов
    route("/") {
        handle {
            // Получение данных из входящего запроса
            val bodyAsMap = call.parseParams()
            val httpMethod = call.request.httpMethod
            // Добавление данных к запросу и переадресация на целевой сервер
            val response = client.request(targetUrl) {
                method = HttpMethod(httpMethod.value)
                headers.appendAll(call.request.headers.filter { key, _ ->
                    key.equals(HttpHeaders.Host, ignoreCase = true).not()
                })
                // Модифицируем данные если нужно
                setBody(
                    bodyAsMap.modifyBodyIfNeed(
                        path = call.request.path(),
                        ignoredFields = ignoredFields,
                        ignoredPaths = ignoredPaths,
                        password = password,
                        terminalKey = terminalKey
                    )
                )
            }
            // Копирование заголовков ответа от целевого сервера
            response.headers.forEach { name, values ->
                values.forEach { value ->
                    call.response.headers.append(name, value)
                }
            }
            // Отправка ответа клиенту
            call.respondBytes(
                bytes = response.readBytes(),
                contentType = response.contentType(),
                status = response.status,
            )
        }
    }
}
