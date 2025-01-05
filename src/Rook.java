public class Rook extends Chess{
    private boolean moved  = false;
    public Rook(int colour) {
        super('R', colour);
    }

    public boolean canCastle() {
        return !moved;
    }

    public void castled() {
        moved = true;
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        if (super.checkMove(move, board) && ((move[0] == move[2] && move[1] != move[3]) || (move[0] != move[2] && move[1] == move[3]))) {
            moved = true;
            return true;
        }
        return false;
    }
}
