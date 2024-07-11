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
import com.github.tedblair2.chess.model.ChessTile
import com.github.tedblair2.chess.model.GameStateTest
import com.github.tedblair2.chess.service.ChessBoard
import kotlin.math.ceil

@Composable
fun ChessBoardScreen(
    modifier: Modifier=Modifier,
    chessBoard: ChessBoard
) {
    val gameState by chessBoard.gameState.collectAsState()
    
    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChessBoard(
            gameStateTest = gameState ,
            onEvent = chessBoard::onEvent,
            modifier = Modifier.padding(top = 25.dp, bottom = 10.dp)
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
    gameStateTest: GameStateTest ,
    onEvent:(ChessEvents)->Unit={}
) {
    val density= LocalDensity.current
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp.dp
    val screenHeight=configuration.screenHeightDp.dp
    val pieces= loadPainters()

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
                            val img=pieces[it.resId]!!
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
                val img=pieces[piece.resId]!!
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
