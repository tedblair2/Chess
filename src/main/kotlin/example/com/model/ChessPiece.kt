package example.com.model

import kotlinx.serialization.Serializable

@Serializable
data class ChessPiece(
    val column:Int ,
    val row:Int ,
    val player: ChessPlayer ,
    val rank: ChessRank
)