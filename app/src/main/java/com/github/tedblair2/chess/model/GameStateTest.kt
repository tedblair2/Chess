package com.github.tedblair2.chess.model

import kotlinx.serialization.Serializable

data class GameStateTest(
    val pieces:Array<Array<ChessPiece2?>> = emptyPieces2() ,
    val selectedPiece: ChessPiece2?=null ,
    val movingPiece:ChessPiece2?=null ,
    val previousState:GameStateTest?=null ,
    val playerAtTurn:ChessPlayer=ChessPlayer.WHITE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameStateTest

        if (!pieces.contentDeepEquals(other.pieces)) return false
        if (selectedPiece != other.selectedPiece) return false
        if (movingPiece != other.movingPiece) return false
        if (previousState != other.previousState) return false
        if (playerAtTurn != other.playerAtTurn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pieces.contentDeepHashCode()
        result = 31 * result + (selectedPiece?.hashCode() ?: 0)
        result = 31 * result + (movingPiece?.hashCode() ?: 0)
        result = 31 * result + (previousState?.hashCode() ?: 0)
        result = 31 * result + playerAtTurn.hashCode()
        return result
    }

}

@Serializable
data class GameState(
    val pieces:Array<Array<ChessPiece?>> = emptyPieces() ,
    val movingPiece:ChessPiece?=null ,
    val playerAtTurn:ChessPlayer=ChessPlayer.WHITE ,
    val connectedPlayers:List<ChessPlayer> = emptyList(),
    val player: ChessPlayer=ChessPlayer.BLACK
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!pieces.contentDeepEquals(other.pieces)) return false
        if (movingPiece != other.movingPiece) return false
        if (playerAtTurn != other.playerAtTurn) return false
        if (connectedPlayers != other.connectedPlayers) return false
        if (player != other.player) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pieces.contentDeepHashCode()
        result = 31 * result + (movingPiece?.hashCode() ?: 0)
        result = 31 * result + playerAtTurn.hashCode()
        result = 31 * result + connectedPlayers.hashCode()
        result = 31 * result + player.hashCode()
        return result
    }
}

fun emptyPieces():Array<Array<ChessPiece?>>{
    return arrayOf(
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null)
    )
}

fun emptyPieces2():Array<Array<ChessPiece2?>>{
    return arrayOf(
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null),
        arrayOf(null,null,null,null,null,null,null,null)
    )
}
