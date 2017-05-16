package kpd.chess

import kpd.chess.PieceType.*


class BoardState(

        val board: Array<Array<Piece?>>,

        val whitePieces: Set<Piece>,
        val blackPieces: Set<Piece>,


        val whiteCastleKing: Boolean,
        val whiteCastleQueen: Boolean,
        val blackCastleKing: Boolean,
        val blackCastleQueen: Boolean,

        val enPassantColumn: Int?,
        val whiteToMove: Boolean
) {

    init {
        verifyBoard()
        verifyPieces()
        verifyEnPassant()
        verifyCastling()
    }



    fun move(bRow: Int, bCol: Int, aRow:Int, aCol: Int): BoardState {

        val newBoard = board.copy()

        val newWhitePieces = HashSet(whitePieces)
        val newBlackPieces = HashSet(blackPieces)


        val pieceToMove = newBoard[bRow][bCol]



        val capturedPiece = newBoard[aRow][aCol]

        if(capturedPiece != null) {
            assert(capturedPiece.white != pieceToMove!!.white)
            if(capturedPiece.white) {
                newWhitePieces.remove(capturedPiece)
            } else {
                newBlackPieces.remove(capturedPiece)
            }
        }

        val pieceAfterMove = pieceToMove!!.copy(row = aRow, col = aCol)
        newBoard[bRow][bCol] = null
        newBoard[aRow][aCol] = pieceAfterMove


        if(pieceToMove.white) {
            newWhitePieces.remove(pieceToMove)
            newWhitePieces.add(pieceAfterMove)
        } else {
            newBlackPieces.remove(pieceToMove)
            newBlackPieces.add(pieceAfterMove)
        }

        return BoardState(newBoard, newWhitePieces, newBlackPieces, whiteCastleKing, whiteCastleQueen, blackCastleKing, blackCastleQueen, enPassantColumn, !whiteToMove)

    }




    private fun verifyBoard() {
        for (row in 0..7){
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null) {
                    if(piece.white) {
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
            assert(piece.white)
            assert(board[piece.row][piece.col] === piece)
        }

        for (piece in blackPieces) {
            assert(piece.white)
            assert(board[piece.row][piece.col] === piece)
        }
    }

    private fun verifyEnPassant() {
        if (enPassantColumn != null) {
            val row: Int
            if (whiteToMove) {
                row = 4
            } else {
                row = 3
            }

            assert(board[row][enPassantColumn] == Piece(row, enPassantColumn, PAWN, !whiteToMove))
            if (enPassantColumn == 0) {
                assert(board[row][enPassantColumn + 1] == Piece(row, enPassantColumn + 1, PAWN, !whiteToMove))
            } else if (enPassantColumn == 7) {
                assert(board[row][enPassantColumn - 1] == Piece(row, enPassantColumn - 1, PAWN, !whiteToMove))
            } else {
                assert(board[row][enPassantColumn + 1] == Piece(row, enPassantColumn + 1, PAWN, !whiteToMove) ||
                        board[row][enPassantColumn - 1] == Piece(row, enPassantColumn - 1, PAWN, !whiteToMove))
            }
        }
    }

    private fun verifyCastling() {

        if (whiteCastleKing) {
            assert(board[0][4] == Piece(0, 4, KING, true))
            assert(board[0][7] == Piece(0, 7, ROOK, true))
        }

        if (whiteCastleQueen) {
            assert(board[0][4] == Piece(0, 4, KING, true))
            assert(board[0][0] == Piece(0, 0, ROOK, true))
        }

        if (blackCastleKing) {
            assert(board[7][4] == Piece(7, 4, KING, false))
            assert(board[7][7] == Piece(7, 7, ROOK, false))
        }

        if (blackCastleQueen) {
            assert(board[7][4] == Piece(7, 4, KING, false))
            assert(board[7][0] == Piece(7, 0, ROOK, false))
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
        val whitePawn = Piece(row = 1, col = col, pieceType = PAWN, white = true)
        val blackPawn = Piece(row = 6, col = col, pieceType = PAWN, white = false)
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

        val whitePiece = Piece(row = 0, col = col, pieceType = pieceType, white = true)
        val blackPiece = Piece(row = 7, col = col, pieceType = pieceType, white = false)

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
            whiteToMove = true)

}




fun Array<Array<Piece?>>.copy() = map { it.clone() }.toTypedArray()
