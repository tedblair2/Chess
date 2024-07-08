package com.github.tedblair2.chess.events

sealed interface ChessEvents {
    data class GetPieceAt(val row:Int,val col:Int):ChessEvents
    data object ResetGame:ChessEvents
    data class SetMovingPiece(val row:Int,val col:Int):ChessEvents
    data object UndoLast:ChessEvents
}