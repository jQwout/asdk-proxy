package ru.tinkoff.asdk

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import ru.tinkoff.asdk.plugins.configureProxy
import ru.tinkoff.asdk.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureProxy(HttpClient(CIO), loadServerConfig())
}
