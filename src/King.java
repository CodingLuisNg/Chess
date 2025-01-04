public class King extends Chess{
    public King(int colour) {
        super('K', colour);
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        return super.checkMove(move, board) && Math.abs(move[0] - move[2]) <= 1 && Math.abs(move[1] - move[3]) <= 1;
    }
}
