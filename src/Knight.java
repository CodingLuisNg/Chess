public class Knight extends Chess{
    public Knight(int colour) {
        super('N', colour);
    }

    @Override
    public boolean checkMove(int[] move, Chess[][] board) {
        return (super.checkMove(move, board) && (Math.abs(move[2] - move[0]) == 2 && Math.abs(move[3] - move[1]) == 1) || (Math.abs(move[2] - move[0]) == 1 && Math.abs(move[3] - move[1]) == 2));
    }
}
