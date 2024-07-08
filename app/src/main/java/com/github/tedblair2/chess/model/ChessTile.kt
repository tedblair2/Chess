package com.github.tedblair2.chess.model

import androidx.compose.ui.graphics.Color

data class ChessTile(
    val x:Int,
    val y:Int
){
    val color:Color
        get() {
            return if ((x+y)%2==0) Color.DarkGray else Color.LightGray.copy(alpha = 0.6f)
        }
}
