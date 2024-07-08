package com.github.tedblair2.chess.model

data class GameState(
    val pieces:Array<Array<ChessPiece?>> = emptyPieces() ,
    val selectedPiece: ChessPiece?=null ,
    val movingPiece:ChessPiece?=null,
    val previousPieces:Array<Array<ChessPiece?>> = emptyPieces()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!pieces.contentDeepEquals(other.pieces)) return false
        if (selectedPiece != other.selectedPiece) return false
        if (movingPiece != other.movingPiece) return false
        if (!previousPieces.contentDeepEquals(other.previousPieces)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pieces.contentDeepHashCode()
        result = 31 * result + (selectedPiece?.hashCode() ?: 0)
        result = 31 * result + (movingPiece?.hashCode() ?: 0)
        result = 31 * result + previousPieces.contentDeepHashCode()
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
