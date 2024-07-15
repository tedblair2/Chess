package example.com.plugins

import example.com.model.ChessPlayer
import example.com.model.Move
import example.com.model.PieceMove
import example.com.model.Square
import example.com.service.ChessGame
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Application.configureRouting(
    game: ChessGame
) {
    routing {
        webSocket("/play"){
            val player=game.connectPlayer(this)
            if (player==null){
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT,"2 players already connected"))
                return@webSocket
            }
            println("Hello ${player.name}")

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text){
                        val move=Json.decodeFromString<Move>(frame.readText())
                        when(move.pieceMove){
                            PieceMove.GetPiece->{
                                val row=if (player==ChessPlayer.WHITE) 9-move.square.row else move.square.row
                                val square=Square(move.square.col,row)
                                game.getPieceAt(square,player)
                            }
                            PieceMove.MovePiece->{
                                val row=if (player==ChessPlayer.WHITE) 9-move.square.row else move.square.row
                                val square=Square(move.square.col,row)
                                game.makeMove(square)
                            }
                            PieceMove.Undo->{
                                game.undo(player)
                            }
                            PieceMove.Reset->{
                                game.reset()
                            }
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                game.disconnectPlayer(player)
            }
        }
    }
}
