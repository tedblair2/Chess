package com.github.tedblair2.chess.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.tedblair2.chess.R
import com.github.tedblair2.chess.events.ChessEvents
import com.github.tedblair2.chess.model.ChessPlayer
import com.github.tedblair2.chess.model.ChessRank
import com.github.tedblair2.chess.model.ChessTile
import com.github.tedblair2.chess.model.GameState
import com.github.tedblair2.chess.model.GameStateTest
import com.github.tedblair2.chess.service.ChessBoard
import com.github.tedblair2.chess.viewmodel.GameViewModel
import kotlin.math.ceil

@Composable
fun ChessBoardScreen(
    modifier: Modifier=Modifier,
    chessBoard: ChessBoard,
    chessBoardViewModel: GameViewModel
) {
    val gameState by chessBoard.gameState.collectAsState()
    val gameStateTest by chessBoardViewModel.gameState.collectAsState()
    
    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChessBoard(
            gameState = gameState,
            onEvent = chessBoardViewModel::onEvent,
            modifier = Modifier.padding(top = 25.dp, bottom = 10.dp),
            gameStateTest = gameStateTest
        )

        Button(onClick = { chessBoard.onEvent(ChessEvents.ResetGame) }) {
            Text(text = "Reset")
        }

        Button(onClick = { chessBoard.onEvent(ChessEvents.UndoLast) }) {
            Text(text = "Undo")
        }
    }
}

@Composable
fun ChessBoard(
    modifier: Modifier=Modifier ,
    gameState: GameStateTest ,
    gameStateTest: GameState,
    onEvent:(ChessEvents)->Unit={}
) {
    val density= LocalDensity.current
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp.dp
    val screenHeight=configuration.screenHeightDp.dp
    val pieces= loadPainters()
    val images= loadImages()

    val size by remember {
        derivedStateOf {
            if (screenWidth<screenHeight) screenWidth.times(0.9f) else screenHeight.times(0.9f)
        }
    }

    val tileSize by remember {
        derivedStateOf {
            size.div(8)
        }
    }

    val boardSize= with(density){size.toPx()}
    val pieceSize= with(density){tileSize.toPx()}

    var isDragging by remember {
        mutableStateOf(false)
    }
    var offsetX by remember {
        mutableFloatStateOf(0f)
    }
    var offsetY by remember {
        mutableFloatStateOf(0f)
    }

    Box(
        modifier = modifier
            .size(size)
    ){
        for (x in 1..8){
            for (y in 1..8){
                val chessTile=ChessTile(x,y)
                ChessTileScreen(
                    chessTile =  chessTile,
                    modifier = Modifier
                        .size(tileSize)
                        .offset(
                            x = tileSize * (chessTile.x - 1) ,
                            y = tileSize * (chessTile.y - 1)
                        ))
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            isDragging = true
                            val row = (offset.y / pieceSize)
                                .toInt()
                                .plus(1)
                            val column = (offset.x / pieceSize)
                                .toInt()
                                .plus(1)
                            onEvent(ChessEvents.GetPieceAt(row , column))
                            offsetX = offset.x
                            offsetY = offset.y
                        } ,
                        onDrag = { change , dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        } ,
                        onDragEnd = {
                            isDragging = false
                            val row = ceil((offsetY / pieceSize) - 0.5)
                                .toInt()
                                .plus(1)
                                .coerceIn(1 , 8)
                            val column = ceil((offsetX / pieceSize) - 0.5)
                                .toInt()
                                .plus(1)
                                .coerceIn(1 , 8)
                            onEvent(ChessEvents.SetMovingPiece(row , column))
                        }
                    )
                }
        ) {
            repeat(gameStateTest.pieces.count()) { row->
                val chessPieces = gameStateTest.pieces[row]
                repeat(chessPieces.count()) {column->
                    val chessPiece = chessPieces[column]
                    chessPiece?.let {
                        if (it != gameStateTest.movingPiece){
                            //val img=pieces[it.resId]!!
                            val img=images[Pair(it.player,it.rank)]!!
                            translate(
                                left = pieceSize * (it.column - 1) ,
                                top = pieceSize * (it.row - 1)
                            ) {
                                with(img){
                                    draw(
                                        size = Size(
                                            width = pieceSize,
                                            height = pieceSize
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            gameStateTest.movingPiece?.let { piece->
                //val img=pieces[piece.resId]!!
                val img=images[Pair(piece.player,piece.rank)]!!
                translate(
                    left = (if (isDragging) offsetX else pieceSize * (piece.column - 1)).coerceIn(0f,boardSize-pieceSize) ,
                    top = (if (isDragging) offsetY else pieceSize * (piece.row - 1)).coerceIn(0f,boardSize-pieceSize)
                ){
                    with(img){
                        draw(
                            size = Size(
                                width = pieceSize,
                                height = pieceSize
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun loadImages():Map<Pair<ChessPlayer,ChessRank>,Painter>{
    return mapOf(
        Pair(ChessPlayer.WHITE,ChessRank.PAWN) to painterResource(id = R.drawable.pawn_white),
        Pair(ChessPlayer.BLACK,ChessRank.PAWN) to painterResource(id = R.drawable.pawn_black),
        Pair(ChessPlayer.WHITE,ChessRank.ROOK) to painterResource(id = R.drawable.rook_white),
        Pair(ChessPlayer.BLACK,ChessRank.ROOK) to painterResource(id = R.drawable.rook_black),
        Pair(ChessPlayer.WHITE,ChessRank.KNIGHT) to painterResource(id = R.drawable.knight_white),
        Pair(ChessPlayer.BLACK,ChessRank.KNIGHT) to painterResource(id = R.drawable.knight_black),
        Pair(ChessPlayer.WHITE,ChessRank.BISHOP) to painterResource(id = R.drawable.bishop_white),
        Pair(ChessPlayer.BLACK,ChessRank.BISHOP) to painterResource(id = R.drawable.bishop_black),
        Pair(ChessPlayer.WHITE,ChessRank.QUEEN) to painterResource(id = R.drawable.queen_white),
        Pair(ChessPlayer.BLACK,ChessRank.QUEEN) to painterResource(id = R.drawable.queen_black),
        Pair(ChessPlayer.WHITE,ChessRank.KING) to painterResource(id = R.drawable.king_white),
        Pair(ChessPlayer.BLACK,ChessRank.KING) to painterResource(id = R.drawable.king_black)
    )
}

@Composable
fun loadPainters(): Map<Int, Painter> {
    val painterIds = listOf(
        R.drawable.rook_white, R.drawable.rook_black,
        R.drawable.knight_white, R.drawable.knight_black,
        R.drawable.bishop_white, R.drawable.bishop_black,
        R.drawable.queen_white, R.drawable.queen_black,
        R.drawable.king_white, R.drawable.king_black,
        R.drawable.pawn_white, R.drawable.pawn_black
    )
    return painterIds.associateWith { painterResource(id = it) }
}

@Composable
fun ChessTileScreen(
    chessTile: ChessTile,
    modifier: Modifier=Modifier
) {
   Box(modifier = modifier.background(chessTile.color))
}
