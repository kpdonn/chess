package kpd.chess




fun main(args : Array<String>) {
    val board = init()

    val number = perft(board, 4)

    println(number)


}



fun perft(board: BoardState, depth: Int): Int {
    if(depth == 0) {
        return 1
    }

    val moveList = board.getPossibleMoves()

    var total = 0

    for (move in moveList) {
        val newBoard = board.doMove(move)

        total += perft(newBoard, depth - 1)
    }

    return total
}