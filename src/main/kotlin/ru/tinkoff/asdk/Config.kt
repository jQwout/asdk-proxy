package ru.tinkoff.asdk

import com.typesafe.config.ConfigFactory

data class ServerConfig(
    val tinkoffHost: String,
    val password: String,
    val terminalKey: String,
    val ignoredFields: Set<String>,
    val ignoredPaths: Set<String>
)

fun loadServerConfig(): ServerConfig {
    val config = ConfigFactory.load() // загружаем конфигурацию из файла application.conf или другого файла по умолчанию
    return ServerConfig(
        tinkoffHost = config.getString("server.tinkoffHost"),
        password = config.getString("server.password"),
        terminalKey = config.getString("server.terminalKey"),
        ignoredFields = config.getStringList("server.ignoredFields").toSet(),
        ignoredPaths = config.getStringList("server.ignoredPaths").toSet()
    )
}