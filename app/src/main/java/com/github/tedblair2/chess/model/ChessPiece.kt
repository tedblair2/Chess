package com.github.tedblair2.chess.model

import androidx.annotation.DrawableRes

data class ChessPiece(
    val column:Int ,
    val row:Int ,
    val player: ChessPlayer ,
    val rank: ChessRank ,
    @DrawableRes
    val resId:Int
)
