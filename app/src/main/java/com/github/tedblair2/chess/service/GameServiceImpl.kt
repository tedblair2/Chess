package com.github.tedblair2.chess.service

import com.github.tedblair2.chess.model.GameState
import com.github.tedblair2.chess.model.Move
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GameServiceImpl @Inject constructor(
    private val client: HttpClient
) : GameService {

    private var session:WebSocketSession?=null
    override fun getGameState(): Flow<GameState> = flow{
        session=client.webSocketSession {
            url("ws://192.168.8.122:8091/play")
        }
        val gameStateFlow=session!!
            .incoming
            .consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .mapNotNull { Json.decodeFromString<GameState>(it.readText()) }
        emitAll(gameStateFlow)
    }

    override suspend fun sendAction(move: Move) {
        session?.outgoing?.send(
            Frame.Text(Json.encodeToString(move))
        )
    }

    override suspend fun close() {
        session?.close()
        session=null
    }
}