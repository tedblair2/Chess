package com.github.tedblair2.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.tedblair2.chess.screen.ChessBoardScreen
import com.github.tedblair2.chess.service.ChessBoard
import com.github.tedblair2.chess.ui.theme.ChessTheme
import com.github.tedblair2.chess.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chessBoard=ChessBoard()
        setContent {
            val chessViewModel=hiltViewModel<GameViewModel>()

            ChessTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize() ,
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessBoardScreen(
                        chessBoard = chessBoard,
                        chessBoardViewModel = chessViewModel
                    )
                }
            }
        }
    }
}