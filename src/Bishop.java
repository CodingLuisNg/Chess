public class Bishop extends Chess{
    public Bishop(int colour) {
        super('B', colour);
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        return super.checkMove(move, board) && Math.abs(move[0] - move[2]) == Math.abs(move[1] - move[3]);
    }
}