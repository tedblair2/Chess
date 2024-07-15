package com.github.tedblair2.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.tedblair2.chess.screen.ChessBoardScreen
import com.github.tedblair2.chess.ui.theme.ChessTheme
import com.github.tedblair2.chess.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val chessViewModel=hiltViewModel<GameViewModel>()
            val isConnectionError by chessViewModel.isConnectionError.collectAsState()
            val snackBarHostState= remember {
                SnackbarHostState()
            }

            LaunchedEffect(key1 = isConnectionError){
                if(isConnectionError){
                    snackBarHostState.showSnackbar("Connection Error!!")
                }
            }

            ChessTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarHostState)
                    }
                ) {paddingValues->
                    ChessBoardScreen(
                        modifier = Modifier
                            .padding(paddingValues),
                        chessBoardViewModel = chessViewModel
                    )
                }
            }
        }
    }
}