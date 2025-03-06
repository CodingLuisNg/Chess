public class King extends Chess{
    private boolean canCastle  = true;
    public King(int colour) {
        super('K', colour);
    }

    public boolean canCastle() {
        return canCastle;
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        if (board[move[2]][move[3]] instanceof Rook && ((Rook) board[move[2]][move[3]]).canCastle() && board[move[2]][move[3]].colour == colour && canCastle && isPathClear(move, board)) {
            canCastle = false;
            ((Rook) board[move[2]][move[3]]).castled();
            return true;
        }
        if (super.checkMove(move, board) && Math.abs(move[0] - move[2]) <= 1 && Math.abs(move[1] - move[3]) <= 1) {
            canCastle = false;
            return true;
        }
        return false;
    }
}
