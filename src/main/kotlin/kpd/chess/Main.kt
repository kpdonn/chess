package kpd.chess




fun main(args : Array<String>) {
    val board = init()

    val number = perft(board, 5)

    println(number)
    println("captures: ${totalCaptures}")


}


var totalCaptures = 0

fun perft(board: BoardState, depth: Int): Int {
    if(depth == 0) {
        return 1
    }

    val moveList = board.getPossibleMoves()

    var total = 0

    for (move in moveList) {
        if (board.board[move.aRow][move.aCol] != null) {
            totalCaptures++
            assert(board.board[move.aRow][move.aCol]!!.pieceType != PieceType.KING)
        }
        val newBoard = board.doMove(move)

        total += perft(newBoard, depth - 1)
    }

    return total
}