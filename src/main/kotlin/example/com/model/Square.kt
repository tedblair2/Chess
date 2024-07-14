package example.com.model

import kotlinx.serialization.Serializable

@Serializable
data class Square(
    val col:Int,
    val row:Int
)

@Serializable
data class Move(
    val square:Square,
    val pieceMove: PieceMove
)

enum class PieceMove{
    GetPiece,MovePiece,Undo,Reset
}