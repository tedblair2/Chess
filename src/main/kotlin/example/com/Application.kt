package example.com

import example.com.plugins.*
import example.com.service.ChessGame
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val game=ChessGame()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(game)
}
