package example.com.service

import example.com.model.*
import io.ktor.serialization.kotlinx.*
import io.ktor.util.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.lang.StrictMath.max
import kotlin.math.abs
import kotlin.math.min
import kotlin.text.Charsets.UTF_8

@OptIn(InternalAPI::class)
class ChessGame {

    private val gameState= MutableStateFlow(GameState())
    private val previousState=MutableStateFlow<GameState?>(null)

    private val playerSockets= ConcurrentMap<ChessPlayer, WebSocketSession>()

    private val gameScope= CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        reset()
        broadcast()
    }

    private fun broadcast(){
        gameState.onEach{sendToWhite(it.forWhitePlayer())}.launchIn(gameScope)
        gameState.onEach(::sendToBlack).launchIn(gameScope)
    }

    fun connectPlayer(session: WebSocketSession):ChessPlayer?{
        val isPlayerWhite=gameState.value.connectedPlayers.any { it==ChessPlayer.WHITE }
        val player=if (isPlayerWhite) ChessPlayer.BLACK else ChessPlayer.WHITE

        gameState.update {
            if (gameState.value.connectedPlayers.contains(player)){
                return null
            }
            if (!playerSockets.containsKey(player)){
                playerSockets[player]=session
            }
            it.copy(
                connectedPlayers = it.connectedPlayers+player
            )
        }

        return player
    }

    fun disconnectPlayer(player: ChessPlayer){
        playerSockets.remove(player)
        gameState.update { state->
            state.copy(
                connectedPlayers = state.connectedPlayers-player
            )
        }
    }

    private suspend fun sendToWhite(state: GameState){
        val socket=playerSockets[ChessPlayer.WHITE]
        socket?.sendSerializedBase<GameState>(state, KotlinxWebsocketSerializationConverter(Json),UTF_8)
    }

    private suspend fun sendToBlack(state: GameState){
        val socket=playerSockets[ChessPlayer.BLACK]
        socket?.sendSerializedBase<GameState>(state, KotlinxWebsocketSerializationConverter(Json),UTF_8)
    }

    fun makeMove(to: Square){
        val piece=gameState.value.movingPiece ?: return
        if (piece.column != to.col || piece.row != to.row){
            movePiece(piece,to.col,to.row)
        }
    }

    fun getPieceAt(square: Square,player: ChessPlayer){
        val piece=pieceAt(square.row,square.col) ?: return
        if (player==gameState.value.playerAtTurn && piece.player==player){
            gameState.update {
                it.copy(movingPiece = piece)
            }
        }
    }

    fun undo(player: ChessPlayer){
        if (gameState.value.playerAtTurn==player.oppositePlayer() && gameState.value.movingPiece==null){
            gameState.update {
                previousState.value ?: it
            }
        }
    }

    fun reset(){
        gameState.update {
            it.copy(
                pieces = emptyPieces(),
                movingPiece = null,
                playerAtTurn = ChessPlayer.WHITE
            )
        }
        previousState.update {
            null
        }

        for (i in 0..1){
            placePiece(ChessPiece(column=1+i*7,row=1, player = ChessPlayer.WHITE, rank = ChessRank.ROOK))
            placePiece(ChessPiece(column=1+i*7,row=8, player = ChessPlayer.BLACK, rank = ChessRank.ROOK))

            placePiece(ChessPiece(column=2+i*5 ,row=1, player = ChessPlayer.WHITE, rank = ChessRank.KNIGHT))
            placePiece(ChessPiece(column=2+i*5,row=8, player = ChessPlayer.BLACK, rank = ChessRank.KNIGHT))


            placePiece(ChessPiece(column=3+i*3,row=1, player = ChessPlayer.WHITE,rank= ChessRank.BISHOP))
            placePiece(ChessPiece(column=3+i*3,row=8, player = ChessPlayer.BLACK,rank= ChessRank.BISHOP))

        }

        for (i in 1..8){
            placePiece(ChessPiece(column=i,row=2, player = ChessPlayer.WHITE, rank = ChessRank.PAWN))
            placePiece(ChessPiece(column=i,row=7, player = ChessPlayer.BLACK, rank = ChessRank.PAWN))
        }

        placePiece(ChessPiece(column=4,row=1, player = ChessPlayer.WHITE, rank = ChessRank.QUEEN))
        placePiece(ChessPiece(column=4,row=8, player = ChessPlayer.BLACK, rank = ChessRank.QUEEN))
        placePiece(ChessPiece(column=5,row=1, player = ChessPlayer.WHITE, rank = ChessRank.KING))
        placePiece(ChessPiece(column=5,row=8, player = ChessPlayer.BLACK, rank = ChessRank.KING))
    }

    private fun placePiece(piece: ChessPiece) {
        if (isValidPosition(piece.column, piece.row)) {
            gameState.update {
                val board=it.pieces
                board[piece.row - 1][piece.column - 1] = piece
                it.copy(pieces = board)
            }
        }
    }

    private fun isValidPosition(column: Int, row: Int): Boolean {
        if (column in 1..8 && row in 1..8){
            return when{
                pieceAt(row, column) == null -> true
                else -> false
            }
        }
        return false
    }

    private fun pieceAt(row: Int, col: Int):ChessPiece?{
        return gameState.value.pieces[row - 1][col - 1]
    }

    private fun movePiece(piece: ChessPiece, newCol: Int, newRow: Int) {
        val from=Square(piece.column,piece.row)
        val to=Square(newCol,newRow)
        if (canMove(from, to)){
            gameState.update { state->
                val board=state.pieces.map { it.copyOf() }.toTypedArray()
                val newPiece=piece.copy(column = newCol, row = newRow)
                board[newPiece.row - 1][newPiece.column - 1] = newPiece
                board[piece.row - 1][piece.column - 1] = null
                previousState.update {
                    state.copy(pieces = state.pieces.map { it.copyOf() }.toTypedArray())
                }
                state.copy(
                    pieces = board,
                    movingPiece = null,
                    playerAtTurn = piece.player.oppositePlayer(),
                )
            }
        }
    }

    private fun GameState.forWhitePlayer():GameState{
        return copy(
            pieces = piecesForWhitePlayer(pieces),
            movingPiece = movingPiece?.copy(row = 9-movingPiece.row)
        )
    }

    private fun piecesForWhitePlayer(pieces:Array<Array<ChessPiece?>>):Array<Array<ChessPiece?>>{
        val board=pieces.map { it.copyOf() }.toTypedArray()

        for (row in 0..3){
            val bottomRowIndex = 7 - row
            val topRowPieces= board[row].map { piece ->
                piece?.copy(row = bottomRowIndex + 1)
            }.toTypedArray()

            val bottomRowPieces: Array<ChessPiece?> =board[bottomRowIndex].map { piece->
                piece?.copy(row = row + 1)
            }.toTypedArray()

            board[row] = bottomRowPieces
            board[bottomRowIndex]=topRowPieces
        }
        return board
    }

    private fun ChessPlayer.oppositePlayer():ChessPlayer{
        if (this==ChessPlayer.WHITE) return ChessPlayer.BLACK
        return ChessPlayer.WHITE
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

    private fun canMoveToDestination(from: Square , to: Square):Boolean{
        val pieceAtDestination = pieceAt(to.row, to.col)
        val movingPiece = pieceAt(from.row, from.col)

        return when{
            pieceAtDestination == null -> true
            pieceAtDestination.player == movingPiece?.player?.oppositePlayer() && pieceAtDestination.rank != ChessRank.KING -> true
            else->false
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

    private fun isClearDiagonal(from: Square,to: Square):Boolean{
        if (abs(from.col-to.col) != abs(from.row-to.row)) return false
        val gap= abs(from.col-to.col) -1
        if (gap==0) return true
        for (i in 1..gap){
            val nextColumn=if (to.col>from.col) from.col+i else from.col-i
            val nextRow=if (to.row>from.row) from.row+i else from.row-i
            if (pieceAt(nextRow,nextColumn) != null) return false
        }
        return canMoveToDestination(from, to)
    }

    private fun canKnightMove(from: Square,to: Square):Boolean{
        val deltaRow= abs(from.row-to.row)
        val deltaCol= abs(from.col-to.col)
        return ((deltaRow==2 && deltaCol==1) || (deltaRow==1 && deltaCol==2)) && canMoveToDestination(from,to)
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
        val deltaRow= abs(from.row-to.row)
        val deltaCol= abs(from.col-to.col)
        return (deltaRow ==1 || deltaCol==1) && canQueenMove(from, to)
    }

    private fun canPawnMove(from: Square,to: Square):Boolean{
        val pawnPiece=pieceAt(from.row,from.col) ?: return false

        val pieceAtDestination=pieceAt(to.row,to.col)
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
                }else if (abs(deltaCol) ==1 && deltaRow==1){
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
                }else if (abs(deltaCol) ==1 && deltaRow==-1){
                    pieceAtDestination != null && pieceAtDestination.player == ChessPlayer.WHITE && pieceAtDestination.rank != ChessRank.KING
                }else{
                    false
                }
            }
        }
    }
}