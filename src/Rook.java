public class Rook extends Chess{
    public Rook(int colour) {
        super('R', colour);
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        return super.checkMove(move, board) && ((move[0] == move[2] && move[1] != move[3]) || (move[0] != move[2] && move[1] == move[3]));
    }
}
