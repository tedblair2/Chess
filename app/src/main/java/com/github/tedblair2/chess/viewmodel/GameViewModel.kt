package com.github.tedblair2.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.chess.events.ChessEvents
import com.github.tedblair2.chess.model.GameState
import com.github.tedblair2.chess.model.Move
import com.github.tedblair2.chess.model.PieceMove
import com.github.tedblair2.chess.model.Square
import com.github.tedblair2.chess.service.GameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameService: GameService
):ViewModel() {

    val gameState=gameService
        .getGameState()
        .onStart { _isConnecting.value=true }
        .onEach { _isConnecting.value=false  }
        .catch { t -> _isConnectionError.value=t is ConnectException }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameState())

    private val _isConnecting= MutableStateFlow(false)
    val isConnecting=_isConnecting.asStateFlow()

    private val _isConnectionError= MutableStateFlow(false)
    val isConnectionError=_isConnectionError.asStateFlow()

    fun onEvent(event: ChessEvents){
        when(event){
            is ChessEvents.GetPieceAt -> {
                viewModelScope.launch {
                    val move=Move(
                        square = Square(event.col,event.row),
                        pieceMove = PieceMove.GetPiece
                    )
                    gameService.sendAction(move)
                }
            }
            ChessEvents.ResetGame -> {
                viewModelScope.launch {
                    val move=Move(
                        square = Square(-1,-1),
                        pieceMove = PieceMove.Reset
                    )
                    gameService.sendAction(move)
                }
            }
            is ChessEvents.SetMovingPiece -> {
                viewModelScope.launch {
                    val move=Move(
                        square = Square(event.col,event.row),
                        pieceMove = PieceMove.MovePiece
                    )
                    gameService.sendAction(move)
                }
            }
            ChessEvents.UndoLast -> {
                viewModelScope.launch {
                    val move=Move(
                        square = Square(-1,-1),
                        pieceMove = PieceMove.Undo
                    )
                    gameService.sendAction(move)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            gameService.close()
        }
    }
}