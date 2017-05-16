package kpd.chess

import kpd.chess.PieceType.*
import kpd.chess.Side.BLACK
import kpd.chess.Side.WHITE

class BoardState(

        val board: Array<Array<Piece?>> = arrayOf(
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8),
                arrayOfNulls(8)
        ),

        val whitePieces: Set<Piece> = mutableSetOf<Piece>(),
        val blackPieces: Set<Piece> = mutableSetOf<Piece>(),


        val whiteCastleKing: Boolean,
        val whiteCastleQueen: Boolean,
        val blackCastleKing: Boolean,
        val blackCastleQueen: Boolean,

        val enPassantColumn: Int?,
        val toMove: Side
) {

    init {
        verifyBoard()
        verifyPieces()
        verifyEnPassant()
        verifyCastling()
    }



    private fun verifyBoard() {
        for (row in 0..7){
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null) {
                    if(piece.side == WHITE) {
                        assert(whitePieces.contains(piece))
                    } else {
                        assert(blackPieces.contains(piece))
                    }
                }
            }
        }
    }

    private fun verifyPieces () {
        for (piece in whitePieces) {
            assert(piece.side == WHITE)
            assert(board[piece.row][piece.col] === piece)
        }

        for (piece in blackPieces) {
            assert(piece.side == BLACK)
            assert(board[piece.row][piece.col] === piece)
        }
    }

    private fun verifyEnPassant() {
        if (enPassantColumn != null) {
            val justMoved: Side
            val row: Int
            if (toMove == WHITE) {
                justMoved = BLACK
                row = 4
            } else {
                justMoved = WHITE
                row = 3
            }

            assert(board[row][enPassantColumn] == Piece(row, enPassantColumn, PAWN, justMoved))
            if (enPassantColumn == 0) {
                assert(board[row][enPassantColumn + 1] == Piece(row, enPassantColumn + 1, PAWN, toMove))
            } else if (enPassantColumn == 7) {
                assert(board[row][enPassantColumn - 1] == Piece(row, enPassantColumn - 1, PAWN, toMove))
            } else {
                assert(board[row][enPassantColumn + 1] == Piece(row, enPassantColumn + 1, PAWN, toMove) ||
                        board[row][enPassantColumn - 1] == Piece(row, enPassantColumn - 1, PAWN, toMove))
            }
        }
    }

    private fun verifyCastling() {

        if (whiteCastleKing) {
            assert(board[0][4] == Piece(0, 4, KING, WHITE))
            assert(board[0][7] == Piece(0, 7, ROOK, WHITE))
        }

        if (whiteCastleQueen) {
            assert(board[0][4] == Piece(0, 4, KING, WHITE))
            assert(board[0][0] == Piece(0, 0, ROOK, WHITE))
        }

        if (blackCastleKing) {
            assert(board[7][4] == Piece(7, 4, KING, BLACK))
            assert(board[7][7] == Piece(7, 7, ROOK, BLACK))
        }

        if (blackCastleQueen) {
            assert(board[7][4] == Piece(7, 4, KING, BLACK))
            assert(board[7][0] == Piece(7, 0, ROOK, BLACK))
        }
    }
}

fun init(): BoardState {
    val board: Array<Array<Piece?>> = arrayOf(
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8)
    )

    val whitePieces: MutableSet<Piece> = mutableSetOf<Piece>()
    val blackPieces: MutableSet<Piece> = mutableSetOf<Piece>()

    for (col in 0..7) {
        val whitePawn = Piece(row = 1, col = col, pieceType = PAWN, side = WHITE)
        val blackPawn = Piece(row = 6, col = col, pieceType = PAWN, side = BLACK)
        whitePawn.add(board, whitePieces, blackPieces)
        blackPawn.add(board, whitePieces, blackPieces)

        var pieceType: PieceType

        when (col) {
            0, 7 -> pieceType = ROOK
            1, 6 -> pieceType = KNIGHT
            2, 5 -> pieceType = BISHOP
            3 -> pieceType = QUEEN
            4 -> pieceType = KING
            else -> throw Exception("Impossible column")
        }

        val whitePiece = Piece(row = 0, col = col, pieceType = pieceType, side = WHITE)
        val blackPiece = Piece(row = 7, col = col, pieceType = pieceType, side = BLACK)

        whitePiece.add(board, whitePieces, blackPieces)
        blackPiece.add(board, whitePieces, blackPieces)
    }


    return BoardState(board = board,
            whitePieces = whitePieces,
            blackPieces = blackPieces,
            whiteCastleKing = true,
            whiteCastleQueen = true,
            blackCastleKing = true,
            blackCastleQueen = true,
            enPassantColumn = null,
            toMove = WHITE)

}


data class Piece(
        val row: Int,
        val col: Int,
        val pieceType: PieceType,
        val side: Side
) {

    fun add(board: Array<Array<Piece?>>, whitePieces: MutableSet<Piece>, blackPieces: MutableSet<Piece>) {
        assert(board[row][col] == null)
        board[row][col] = this

        if (this.side == WHITE) {
            assert(!whitePieces.contains(this))
            whitePieces.add(this)
        } else {
            assert(!blackPieces.contains(this))
            blackPieces.add(this)
        }
    }
}

enum class PieceType {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}

enum class Side {
    WHITE,
    BLACK
}