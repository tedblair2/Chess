package com.github.tedblair2.chess.model

data class GameState(
    val pieces:Array<Array<ChessPiece?>> = emptyPieces() ,
    val selectedPiece: ChessPiece?=null ,
    val movingPiece:ChessPiece?=null,
    val previousState:GameState?=null,
    val playerAtTurn:ChessPlayer=ChessPlayer.WHITE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

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
