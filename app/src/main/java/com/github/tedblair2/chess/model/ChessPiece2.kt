package com.github.tedblair2.chess.model

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class ChessPiece2(
    val column:Int ,
    val row:Int ,
    val player: ChessPlayer ,
    val rank: ChessRank ,
    @DrawableRes
    val resId:Int
)

@Serializable
data class ChessPiece(
    val column:Int ,
    val row:Int ,
    val player: ChessPlayer ,
    val rank: ChessRank
)

