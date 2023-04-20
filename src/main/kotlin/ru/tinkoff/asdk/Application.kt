package ru.tinkoff.asdk

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.network.tls.certificates.*
import io.ktor.serialization.kotlinx.json.*
import ru.tinkoff.asdk.plugins.configureProxy
import ru.tinkoff.asdk.plugins.configureSerialization
import java.io.File

fun main() {
    val keyStoreFile = File("build/keystore.jks")
    val keyStore = buildKeyStore {
        certificate("sampleAlias") {
            password = "foobar"
            domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
        }
    }

    val environment = applicationEngineEnvironment {
        connector {
            port = 8080
        }
        sslConnector(
            keyStore = keyStore,
            keyAlias = "sampleAlias",
            keyStorePassword = { "123456".toCharArray() },
            privateKeyPassword = { "foobar".toCharArray() }) {
            port = 8443
            keyStorePath = keyStoreFile
        }
        module(Application::module)
    }

    embeddedServer(Netty, environment).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureProxy(
        HttpClient(CIO) {
            install(ContentNegotiation){
                json()
            }
        },
        loadServerConfig()
    )
}
