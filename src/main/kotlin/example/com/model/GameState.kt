package example.com.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val pieces:Array<Array<ChessPiece?>> = emptyPieces() ,
    val movingPiece:ChessPiece?=null,
    val playerAtTurn:ChessPlayer=ChessPlayer.WHITE,
    val connectedPlayers:List<ChessPlayer> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!pieces.contentDeepEquals(other.pieces)) return false
        if (movingPiece != other.movingPiece) return false
        if (playerAtTurn != other.playerAtTurn) return false
        if (connectedPlayers != other.connectedPlayers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pieces.contentDeepHashCode()
        result = 31 * result + (movingPiece?.hashCode() ?: 0)
        result = 31 * result + playerAtTurn.hashCode()
        result = 31 * result + connectedPlayers.hashCode()
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
