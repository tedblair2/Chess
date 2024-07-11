package com.github.tedblair2.chess.service

import com.github.tedblair2.chess.model.GameState
import com.github.tedblair2.chess.model.Move
import kotlinx.coroutines.flow.Flow

interface GameService {
    fun getGameState():Flow<GameState>
    suspend fun sendAction(move: Move)

    suspend fun close()
}