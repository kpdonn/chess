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


    fun getPossibleMoves(): List<Move> {
        val pieces: Set<Piece>
        if (whiteToMove) {
            pieces = whitePieces
        } else {
            pieces = blackPieces
        }

        val moveList = ArrayList<Move>()

        for (piece in pieces) {
            when(piece.pieceType) {
                PAWN -> moveList.addAll(getPawnMoves(piece.row, piece.col, piece.white))
                ROOK -> moveList.addAll(getRookMoves(piece.row, piece.col, piece.white))
                KNIGHT -> moveList.addAll(getKnightMoves(piece.row, piece.col, piece.white))
                BISHOP -> moveList.addAll(getBishopMoves(piece.row, piece.col, piece.white))
                QUEEN -> moveList.addAll(getQueenMoves(piece.row, piece.col, piece.white))
                KING -> moveList.addAll(getKingMoves(piece.row, piece.col, piece.white))
            }
        }

        return moveList
    }


    fun getBishopMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        for (rowInc in arrayOf(1, -1)) {
            for (colInc in arrayOf(1, -1)) {
                var dRow = row + rowInc
                var dCol = col + colInc

                while (dRow in 0..7 && dCol in 0..7) {
                    val piece = board[dRow][dCol]
                    if (piece == null) {
                        moves.add(Move(row, col, dRow, dCol))
                        dRow += rowInc
                        dCol += colInc
                        continue
                    } else {
                        if (piece.white != white) {
                            moves.add(Move(row, col, dRow, dCol))
                        }
                        break
                    }
                }
            }
        }

        return moves
    }

    fun getQueenMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        moves.addAll(getBishopMoves(row,col,white))
        moves.addAll(getRookMoves(row,col,white))

        return moves
    }

    fun getKingMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        for (rowInc in -1..1) {
            for (colInc in -1..1) {
                var dRow = row + rowInc
                var dCol = col + colInc

                if (dRow in 0..7 && dCol in 0..7) {
                    if (board[dRow][dCol] == null || board[dRow][dCol]!!.white != white) {
                        moves.add(Move(row, col, dRow, dCol))
                    }
                }
            }
        }

        return moves
    }

    fun getRookMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        for (inc in arrayOf(1, -1)) {
            var dRow = row + inc
            var dCol = col

            while (dRow in 0..7 && dCol in 0..7) {
                val piece = board[dRow][dCol]
                if (piece == null) {
                    moves.add(Move(row, col, dRow, dCol))
                    dRow += inc
                    continue
                } else {
                    if (piece.white != white) {
                        moves.add(Move(row, col, dRow, dCol))
                    }
                    break
                }
            }

            dRow = row
            dCol = col + inc

            while (dRow in 0..7 && dCol in 0..7) {
                val piece = board[dRow][dCol]
                if (piece == null) {
                    moves.add(Move(row, col, dRow, dCol))
                    dCol += inc
                    continue
                } else {
                    if (piece.white != white) {
                        moves.add(Move(row, col, dRow, dCol))
                    }
                    break
                }
            }
        }
        return moves

    }


    fun getKnightMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        val possibleMoves = arrayOf(
                -1 to -2,
                -1 to 2,
                1 to -2,
                1 to 2,
                -2 to -1,
                -2 to 1,
                2 to -1,
                2 to 1
        )

        for ((rowInc, colInc) in possibleMoves) {
            val dRow = row + rowInc
            val dCol = col + colInc

            if (dRow in 0..7 && dCol in 0..7) {
                if (board[dRow][dCol] == null || board[dRow][dCol]!!.white != white) {
                    moves.add(Move(row, col, dRow, dCol))
                }
            }

        }

        return moves
    }


    fun getPawnMoves(row: Int, col: Int, white: Boolean): List<Move> {

        val moves = ArrayList<Move>()

        val oneRowAhead: Int
        val twoRowsAhead: Int

        if (white) {
            oneRowAhead = row + 1
            twoRowsAhead = row + 2
        } else {
            oneRowAhead = row - 1
            twoRowsAhead = row - 2
        }

        if (board[oneRowAhead][col] == null) {
            moves.add(Move(row, col, oneRowAhead, col))

            if ((white && row == 1) || (!white && row == 6)) {
                if (board[twoRowsAhead][col] == null) {
                    moves.add(Move(row, col, twoRowsAhead, col))
                }
            }
        }

        val left = col - 1
        val right = col + 1

        if (left in 0..7) {
            if (board[oneRowAhead][left]?.white == !white) {
                moves.add(Move(row, col, oneRowAhead, left))
            }
        }

        if (right in 0..7) {
            if (board[oneRowAhead][right]?.white == !white) {
                moves.add(Move(row, col, oneRowAhead, right))
            }
        }

        return moves
    }


    fun doMove(move: Move): BoardState {

        val newBoard = board.copy()

        val newWhitePieces = HashSet(whitePieces)
        val newBlackPieces = HashSet(blackPieces)


        val pieceToMove = newBoard[move.bRow][move.bCol]


        val capturedPiece = newBoard[move.aRow][move.aCol]

        if (capturedPiece != null) {
            assert(capturedPiece.white != pieceToMove!!.white)
            if (capturedPiece.white) {
                newWhitePieces.remove(capturedPiece)
            } else {
                newBlackPieces.remove(capturedPiece)
            }
        }

        val pieceAfterMove = pieceToMove!!.copy(row = move.aRow, col = move.aCol)
        newBoard[move.bRow][move.bCol] = null
        newBoard[move.aRow][move.aCol] = pieceAfterMove


        if (pieceToMove.white) {
            newWhitePieces.remove(pieceToMove)
            newWhitePieces.add(pieceAfterMove)
        } else {
            newBlackPieces.remove(pieceToMove)
            newBlackPieces.add(pieceAfterMove)
        }

        return BoardState(newBoard, newWhitePieces, newBlackPieces, whiteCastleKing, whiteCastleQueen, blackCastleKing, blackCastleQueen, enPassantColumn, !whiteToMove)

    }


    private fun verifyBoard() {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null) {
                    if (piece.white) {
                        assert(whitePieces.contains(piece))
                    } else {
                        assert(blackPieces.contains(piece))
                    }
                }
            }
        }
    }

    private fun verifyPieces() {
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
