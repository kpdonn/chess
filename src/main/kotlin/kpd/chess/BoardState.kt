package kpd.chess

import com.sun.org.apache.xpath.internal.operations.Bool
import kpd.chess.PieceType.*


class BoardState(

        val board: Array<Array<Piece?>>,

        val whitePieces: Set<Piece>,
        val blackPieces: Set<Piece>,

        val whiteKing: Piece,
        val blackKing: Piece,

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

        var moveList = ArrayList<Move>()

        val piecesChecking = piecesWithKingCheck(whiteToMove)

        if (piecesChecking.size <= 1) {
            for (piece in pieces) {
                when (piece.pieceType) {
                    PAWN -> moveList.addAll(getPawnMoves(piece.row, piece.col, piece.white))
                    ROOK -> moveList.addAll(getRookMoves(piece.row, piece.col, piece.white))
                    KNIGHT -> moveList.addAll(getKnightMoves(piece.row, piece.col, piece.white))
                    BISHOP -> moveList.addAll(getBishopMoves(piece.row, piece.col, piece.white))
                    QUEEN -> moveList.addAll(getQueenMoves(piece.row, piece.col, piece.white))
                    KING -> moveList.addAll(getKingMoves(piece.row, piece.col, piece.white))
                }
            }

            if (piecesChecking.size == 1) {
                moveList = ArrayList(filterMovesForCheck(moveList, piecesChecking[0]))
            }
        } else {
            for (piece in pieces) {
                when (piece.pieceType) {
                    KING -> moveList.addAll(getKingMoves(piece.row, piece.col, piece.white))
                    else -> {}
                }
            }
        }



        return moveList
    }

    fun piecesWithKingCheck(whiteKing: Boolean): List<Piece> {
        val pieces: Set<Piece>
        if (whiteKing) {
            pieces = whitePieces
        } else {
            pieces = blackPieces
        }

        for (piece in pieces) {
            if (piece.pieceType == KING) {
                return piecesWithDirectAttacks(piece.row, piece.col, !whiteKing)
            }
        }

        throw Exception("No King found")
    }

    fun piecesBetweenStraightLine(row1: Int, col1: Int, row2: Int, col2: Int): List<Piece> {
        assert(onStraightLineWithSpace(row1, col1, row2, col2))


        val rowDiff = row1 - row2
        val colDiff = col1 - col2


        val piecesBetween = ArrayList<Piece>()

        if (rowDiff == 0) {
            for (colOffset in 0 until colDiff) {
                if (colOffset != 0) {
                    val maybePiece = board[row2][col2 + colOffset]
                    if(maybePiece != null) {
                        piecesBetween.add(maybePiece)
                    }

                }
            }
        }

        if (colDiff == 0) {
            for (rowOffset in 0 until rowDiff) {
                if (rowOffset != 0) {
                    val maybePiece =  board[row2 + rowOffset][col2]
                    if(maybePiece != null) {
                        piecesBetween.add(maybePiece)
                    }
                }
            }
        }

        return piecesBetween
    }

    fun piecesBetweenDiagonalLine(row1: Int, col1: Int, row2: Int, col2: Int): List<Piece> {
        assert(onDiagonalLineWithSpace(row1, col1, row2, col2))


        val rowDiff = row1 - row2
        val colDiff = col1 - col2

        val piecesBetween = ArrayList<Piece>()

        val colInc: Int
        if (Math.abs(colDiff) == colDiff) {
            colInc = 1
        } else {
            colInc = -1
        }

        var colOffset = 0

        for (rowOffset in 0 until rowDiff) {
            if (rowOffset != 0) {
                colOffset += colInc

                val maybePiece =  board[row2 + rowOffset][col2 + colOffset]
                if(maybePiece != null) {
                    piecesBetween.add(maybePiece)
                }

            }
        }

        return piecesBetween
    }

    fun piecesWithDirectAttacks(row: Int, col: Int, attackFromWhite: Boolean): List<Piece> {
        val pieces: Set<Piece>
        if (attackFromWhite) {
            pieces = whitePieces
        } else {
            pieces = blackPieces
        }


        val piecesWithAttack = ArrayList<Piece>()


        for (piece in pieces) {
            val rowDiff = piece.row - row
            val colDiff = piece.col - col

            assert(!(rowDiff == 0 && colDiff == 0))

            when (piece.pieceType) {
                KING -> {
                    if (Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1) {
                        piecesWithAttack.add(piece)
                    }
                }
                ROOK, QUEEN -> {
                    if (piecesBetweenStraightLine(piece.row, piece.col, row, col).isEmpty()) {
                        piecesWithAttack.add(piece)
                    }
                }
                BISHOP, QUEEN -> {
                    if (piecesBetweenDiagonalLine(piece.row, piece.col, row, col).isEmpty()) {
                        piecesWithAttack.add(piece)
                    }
                }
                KNIGHT -> {
                    if ((Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 1) ||
                            (Math.abs(colDiff) == 2 && Math.abs(rowDiff) == 1)) {
                        piecesWithAttack.add(piece)
                    }
                }
                PAWN -> {
                    if (colDiff == 1 || colDiff == -1) {
                        if ((attackFromWhite && rowDiff == -1) || (!attackFromWhite && rowDiff == 1)) {
                            piecesWithAttack.add(piece)
                        }
                    }

                    // TODO: En passant
                }
            }
        }
        return piecesWithAttack
    }

    fun onStraightLineWithSpace(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {

        return row1 == row2 || col1 == col2


    }

    fun onDiagonalLineWithSpace(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val rowDiff = row1 - row2
        val colDiff = col1 - col2

        return Math.abs(rowDiff) == Math.abs(colDiff)
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

        moves.addAll(getBishopMoves(row, col, white))
        moves.addAll(getRookMoves(row, col, white))

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

    fun isBetweenStraightLine(rowToTest: Int, colToTest: Int, row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val endpointsRowDiff = row1 - row2
        val endPointsColDiff = col1 - col2

        val testRowDiff = row1 - rowToTest
        val testColDiff = col1 - colToTest

        if (testColDiff != 0 && testRowDiff != 0) {
            return false
        }

        if(testColDiff == 0 && testRowDiff in 0..endpointsRowDiff) {
            return true
        } else if (testRowDiff == 0 && testColDiff in 0..endPointsColDiff) {
            return true
        }

        return false
    }

    fun isBetweenDiagonalLine(rowToTest: Int, colToTest: Int, row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val endpointsRowDiff = row1 - row2
        val endPointsColDiff = col1 - col2

        val testRowDiff = row1 - rowToTest
        val testColDiff = col1 - colToTest

        if (Math.abs(testRowDiff) != Math.abs(testColDiff)) {
            return false
        }

        if(testRowDiff in 0..endpointsRowDiff && testColDiff in 0..endPointsColDiff) {
            return true
        }

        return false
    }

    fun doesBlockAttack(rowToTest: Int, colToTest: Int, attackingRow: Int, attackingCol: Int, attackedRow: Int, attackedCol: Int): Boolean {
        val attackingPiece = board[attackingRow][attackingCol]!!

        when(attackingPiece.pieceType) {
            KING, PAWN, KNIGHT -> return false
            ROOK -> return isBetweenStraightLine(rowToTest, colToTest, attackingRow, attackingCol, attackedRow, attackedCol)
            BISHOP -> return isBetweenDiagonalLine(rowToTest, colToTest, attackingRow, attackingCol, attackedRow, attackedCol)
            QUEEN -> {
                if(onStraightLineWithSpace(attackingRow, attackingCol, attackedRow, attackedCol)) {
                    return isBetweenStraightLine(rowToTest, colToTest, attackingRow, attackingCol, attackedRow, attackedCol)
                } else {
                    return isBetweenDiagonalLine(rowToTest, colToTest, attackingRow, attackingCol, attackedRow, attackedCol)
                }
            }

        }
    }

    fun doesBlockAttack(move: Move, attackingPiece: Piece, attackedPiece: Piece): Boolean {
        return doesBlockAttack(move.aRow, move.aCol, attackingPiece.row, attackingPiece.col, attackedPiece.row, attackedPiece.col)
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

    fun filterMovesForCheck(moves: List<Move>, kingInCheckFrom: Piece?): List<Move> {
        if (kingInCheckFrom != null) {
            val king: Piece
            if (kingInCheckFrom.white) {
                king = blackKing
            } else {
                king = whiteKing
            }

            val filteredMoves = moves.filter {
                (it.aRow == kingInCheckFrom.row && it.aCol == kingInCheckFrom.col) ||
                        doesBlockAttack(it, kingInCheckFrom, king)
            }

            return filteredMoves
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

        var whiteKing = whiteKing
        var blackKing = blackKing

        if (pieceAfterMove.pieceType == KING) {
            if (pieceAfterMove.white) {
                whiteKing = pieceAfterMove
            } else {
                blackKing = pieceAfterMove
            }
        }

        if (pieceToMove.white) {
            newWhitePieces.remove(pieceToMove)
            newWhitePieces.add(pieceAfterMove)
        } else {
            newBlackPieces.remove(pieceToMove)
            newBlackPieces.add(pieceAfterMove)
        }

        return BoardState(newBoard, newWhitePieces, newBlackPieces,whiteKing, blackKing, whiteCastleKing, whiteCastleQueen, blackCastleKing, blackCastleQueen, enPassantColumn, !whiteToMove)

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
