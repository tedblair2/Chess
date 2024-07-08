package com.github.tedblair2.chess.service

import com.github.tedblair2.chess.R
import com.github.tedblair2.chess.events.ChessEvents
import com.github.tedblair2.chess.model.ChessPiece
import com.github.tedblair2.chess.model.ChessPlayer
import com.github.tedblair2.chess.model.ChessRank
import com.github.tedblair2.chess.model.GameState
import com.github.tedblair2.chess.model.Square
import com.github.tedblair2.chess.model.emptyPieces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ChessBoard {

    private val _gameState= MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    init {
        reset()
    }

    private fun reset(){
        _gameState.update {
            it.copy(
                pieces = emptyPieces(),
                previousPieces = emptyPieces(),
                movingPiece = null,
                selectedPiece = null
            )
        }
        for (i in 0..1){
            placePiece(ChessPiece(column=1+i*7,row=1, player = ChessPlayer.WHITE, rank = ChessRank.ROOK, resId = R.drawable.rook_white))
            placePiece(ChessPiece(column=1+i*7,row=8, player = ChessPlayer.BLACK, rank = ChessRank.ROOK, resId = R.drawable.rook_black))

            placePiece(ChessPiece(column=2+i*5 ,row=1, player = ChessPlayer.WHITE, rank = ChessRank.KNIGHT, resId = R.drawable.knight_white))
            placePiece(ChessPiece(column=2+i*5,row=8, player = ChessPlayer.BLACK, rank = ChessRank.KNIGHT, resId = R.drawable.knight_black))

            placePiece(ChessPiece(column=3+i*3,row=1, player = ChessPlayer.WHITE,rank=ChessRank.BISHOP, resId = R.drawable.bishop_white))
            placePiece(ChessPiece(column=3+i*3,row=8, player = ChessPlayer.BLACK,rank=ChessRank.BISHOP, resId = R.drawable.bishop_black))
        }

        for (i in 1..8){
            placePiece(ChessPiece(column=i,row=2, player = ChessPlayer.WHITE, rank = ChessRank.PAWN, resId = R.drawable.pawn_white))
            placePiece(ChessPiece(column=i,row=7, player = ChessPlayer.BLACK, rank = ChessRank.PAWN, resId = R.drawable.pawn_black))
        }

        placePiece(ChessPiece(column=4,row=1, player = ChessPlayer.WHITE, rank = ChessRank.QUEEN, resId = R.drawable.queen_white))
        placePiece(ChessPiece(column=4,row=8, player = ChessPlayer.BLACK, rank = ChessRank.QUEEN, resId = R.drawable.queen_black))
        placePiece(ChessPiece(column=5,row=1, player = ChessPlayer.WHITE, rank = ChessRank.KING, resId = R.drawable.king_white))
        placePiece(ChessPiece(column=5,row=8, player = ChessPlayer.BLACK, rank = ChessRank.KING, resId = R.drawable.king_black))
    }

    private fun placePiece(piece: ChessPiece) {
        if (isValidPosition(piece.column, piece.row)) {
            _gameState.update {
                val board=it.pieces
                board[piece.row - 1][piece.column - 1] = piece
                it.copy(pieces = board)
            }
        }
    }

    private fun movePiece(piece: ChessPiece, newCol: Int, newRow: Int) {
        val from=Square(piece.column,piece.row)
        val to=Square(newCol,newRow)
       if (canMove(from, to)){
           _gameState.update {
               val board=it.pieces
               val newPiece=piece.copy(column = newCol, row = newRow)
               board[newPiece.row - 1][newPiece.column - 1] = newPiece
               board[piece.row - 1][piece.column - 1] = null
               it.copy(pieces = board,movingPiece = null)
           }
       }
    }

    private fun isValidPosition(column: Int , row: Int): Boolean {
        if (column in 1..8 && row in 1..8){
            return when{
                pieceAt(row, column) == null -> true
                else -> false
            }
        }
        return false
    }

    private fun canMove(from:Square,to:Square):Boolean{
        if (from.row==to.row && from.col==to.col) return false
        val movingPiece=pieceAt(from.row,from.col) ?: return false
        return when(movingPiece.rank){
            ChessRank.ROOK-> canRookMove(from,to)
            ChessRank.BISHOP->canBishopMove(from,to)
            ChessRank.KNIGHT->canKnightMove(from, to)
            ChessRank.QUEEN->canQueenMove(from,to)
            ChessRank.KING->canKingMove(from, to)
            ChessRank.PAWN->canPawnMove(from, to)
        }
    }

    private fun pieceAt(row: Int, col: Int): ChessPiece? {
        return gameState.value.pieces[row - 1][col - 1]
    }

    private fun canKnightMove(from: Square,to: Square):Boolean{
        val deltaRow=abs(from.row-to.row)
        val deltaCol=abs(from.col-to.col)
        return ((deltaRow==2 && deltaCol==1) || (deltaRow==1 && deltaCol==2)) && canMoveToDestination(from,to)
    }

    private fun canMoveToDestination(from: Square , to: Square):Boolean{
        val pieceAtDestination = pieceAt(to.row, to.col)
        val movingPiece = pieceAt(from.row, from.col)

        return when{
            pieceAtDestination == null -> true
            pieceAtDestination.player == movingPiece?.player?.oppositePlayer() && pieceAtDestination.rank != ChessRank.KING -> true
            else->false
        }
    }

    private fun canRookMove(from:Square,to:Square):Boolean{
        return (from.col==to.col && isClearVertical(from,to)) || (from.row==to.row && isClearHorizontal(from,to))
    }

    private fun canBishopMove(from:Square,to:Square):Boolean{
        if (abs(from.col-to.col) == abs(from.row-to.row)){
            return isClearDiagonal(from,to)
        }
        return false
    }

    private fun canQueenMove(from: Square,to: Square):Boolean{
        return canRookMove(from,to) || canBishopMove(from,to)
    }

    private fun canKingMove(from: Square,to: Square):Boolean{
        val deltaRow=abs(from.row-to.row)
        val deltaCol=abs(from.col-to.col)
        return (deltaRow ==1 || deltaCol==1) && canQueenMove(from, to)
    }

    private fun canPawnMove(from: Square,to: Square):Boolean{
        val pawnPiece=pieceAt(from.row,from.col) ?: return false

        val pieceAtDestination = pieceAt(to.row, to.col)
        val deltaRow = to.row - from.row
        val deltaCol = to.col - from.col

        when(pawnPiece.player){
            ChessPlayer.WHITE->{
                return if (from.col==to.col){
                    if (from.row==2){
                        (to.row==3 || to.row==4) && pieceAtDestination==null && isClearVertical(from, to)
                    }else{
                        deltaRow==1 && pieceAtDestination==null
                    }
                }else if (abs(deltaCol)==1 && deltaRow==1){
                    pieceAtDestination != null && pieceAtDestination.player == ChessPlayer.BLACK && pieceAtDestination.rank != ChessRank.KING
                }else{
                    false
                }
            }
            ChessPlayer.BLACK->{
                return if (from.col==to.col){
                    if (from.row==7){
                        (to.row==6 || to.row==5) && pieceAtDestination==null && isClearVertical(from, to)
                    }else{
                        deltaRow==-1 && pieceAtDestination==null
                    }
                }else if (abs(deltaCol)==1 && deltaRow==-1){
                    pieceAtDestination != null && pieceAtDestination.player == ChessPlayer.WHITE && pieceAtDestination.rank != ChessRank.KING
                }else{
                    false
                }
            }
        }
    }

    private fun isClearVertical(from:Square,to:Square):Boolean{
        val start= min(from.row,to.row).plus(1)
        val end= max(from.row,to.row).minus(1)
        for (row in start..end){
            if (pieceAt(row,from.col) != null) return false
        }
        return canMoveToDestination(from, to)
    }

    private fun isClearHorizontal(from: Square,to: Square):Boolean{
        val start= min(from.col,to.col).plus(1)
        val end= max(from.col,to.col).minus(1)
        for (column in start..end){
            if (pieceAt(from.row,column) != null) return false
        }
        return canMoveToDestination(from, to)
    }

    private fun ChessPlayer.oppositePlayer():ChessPlayer{
        if (this==ChessPlayer.WHITE) return ChessPlayer.BLACK
        return ChessPlayer.WHITE
    }

    private fun isClearDiagonal(from: Square,to: Square):Boolean{
        if (abs(from.col-to.col) != abs(from.row-to.row)) return false
        val gap= abs(from.col-to.col)-1
        if (gap==0) return true
        for (i in 1..gap){
            val nextColumn=if (to.col>from.col) from.col+i else from.col-i
            val nextRow=if (to.row>from.row) from.row+i else from.row-i
            if (pieceAt(nextRow,nextColumn) != null) return false
        }
        return canMoveToDestination(from, to)
    }

    fun onEvent(action:ChessEvents){
        when(action){
            is ChessEvents.GetPieceAt -> {
                _gameState.update {
                    val piece=pieceAt(action.row,action.col)
                    it.copy(movingPiece = piece, selectedPiece = piece)
                }
            }
            ChessEvents.ResetGame -> reset()
            is ChessEvents.SetMovingPiece->{
                val movingPiece=gameState.value.movingPiece
                movingPiece?.let {
                    if (action.col != it.column || action.row != it.row){
                        movePiece(it,action.col,action.row)
                    }
                }
            }
            ChessEvents.UndoLast->{}
        }
    }
}