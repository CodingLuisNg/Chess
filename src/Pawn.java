public class Pawn extends Chess{
    private final int side;
    public Pawn(int colour, int side, ChessGame game) {
        super('P', colour);
        this.side = side;
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        if (!super.checkMove(move, board)) {
            return false;
        }
        if (!isEmptyTile(move, board) && isOpponentPiece(move, board)) {
            return isValidCapture(move, board);
        }
        if ((move[0] == board.length - 2 || move[0] == 1) && move[1] == move[3]) {
            return side * (move[0] - move[2]) <= 2;
        } else {
            return move[1] == move[3] && move[0] - move[2] == side;
        }
    }

    private boolean isValidCapture(int[] move, Chess[][] board) {
        return !isEmptyTile(move, board) && isOpponentPiece(move, board) && move[0] - move[2] == side && Math.abs(move[1] - move[3]) == 1;
    }
}
