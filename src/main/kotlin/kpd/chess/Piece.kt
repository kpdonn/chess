package kpd.chess


data class Piece(
        val row: Int,
        val col: Int,
        val pieceType: PieceType,
        val white: Boolean
) {

    fun add(board: Array<Array<Piece?>>, whitePieces: MutableSet<Piece>, blackPieces: MutableSet<Piece>) {
        assert(board[row][col] == null)
        board[row][col] = this

        if (this.white) {
            assert(!whitePieces.contains(this))
            whitePieces.add(this)
        } else {
            assert(!blackPieces.contains(this))
            blackPieces.add(this)
        }
    }
}