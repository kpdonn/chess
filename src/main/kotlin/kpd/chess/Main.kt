package kpd.chess




fun main(args : Array<String>) {
    val board = init()

    println("ORIGINAL BOARD BEFORE")
    board.board.forEach { it.forEach { println(it) } }

    println(board.whitePieces)
    println(board.blackPieces)

    println("NEW BOARD AFTER")

    val newBoard = board.move(1, 3, 3, 3)

    newBoard.board.forEach { it.forEach { println(it) } }

    println(newBoard.whitePieces)
    println(newBoard.blackPieces)


    println("ORIGINAL BOARD AFTER")

    board.board.forEach { it.forEach { println(it) } }

    println(board.whitePieces)
    println(board.blackPieces)
}

